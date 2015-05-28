package ru.codeunited.wmq.jms;

import com.ibm.msg.client.wmq.WMQConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.codeunited.wmq.Utils;
import ru.codeunited.wmq.jms.impl.ConnectionFactoryImpl;

import javax.jms.*;
import java.math.BigInteger;
import java.util.Properties;
import java.util.Random;

import static com.ibm.mq.constants.CMQC.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public class ShowGroupingJMSTest {

    private static final String QUEUE_NAME = "RFH.QTEST.SMALL";

    @Before
    @After
    public void cleanup() throws JMSException {
        ConnectionFactory factory = prepareFactory();
        Connection connection = factory.getConnection();
        connection.start();
        Session session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(QUEUE_NAME);
        MessageConsumer housekeeper = session.createConsumer(queue);
        while (housekeeper.receiveNoWait() != null) {

        }
        session.close();
    }

    @Test
    public void putBigMessage() throws JMSException {
        // prepare payload
        final int BIG_BLOCK = 32768;

        final int SMALL_BLOCK = 256; // REMEMBER: JMS, RFH2 and other headers has a weight too! DON'T SET MAX ALLOWED BODY SIZE!

        byte[] bigBlock = Utils.generateRandomBytes(BIG_BLOCK);


        ConnectionFactory factory = prepareFactory();

        Connection connection = factory.getConnection();
        connection.start();
        Session session = connection.createSession(true, javax.jms.Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(QUEUE_NAME);

        MessageProducer producer = session.createProducer(queue);

        final String groupId = "ID:" + new BigInteger(24 * 8, new Random()).toString(16);

        final int groups = BIG_BLOCK / SMALL_BLOCK;

        // write group
        for (int z = 0; z < groups; z++) {
            final BytesMessage message = session.createBytesMessage();
            byte[] smallBlock = new byte[SMALL_BLOCK];
            System.arraycopy(bigBlock, z * SMALL_BLOCK, smallBlock, 0, SMALL_BLOCK);
            message.writeBytes(smallBlock);
            message.setStringProperty("JMSXGroupID", groupId);
            message.setIntProperty("JMSXGroupSeq", z + 1); // first value is 1
            if (z == (groups - 1)) {
                message.setBooleanProperty("JMS_IBM_Last_Msg_In_Group", true);
            }
            producer.send(message);
        }
        session.commit();

        // assembly group back
        MessageConsumer lastMessageConsumer = session.createConsumer(queue, "JMS_IBM_Last_Msg_In_Group=TRUE");
        BytesMessage lastMessage = (BytesMessage) lastMessageConsumer.receiveNoWait();
        assertThat(lastMessage, notNullValue());

        final int receivedGroupSize = lastMessage.getIntProperty("JMSXGroupSeq");
        assertThat("Wrong partitions size", receivedGroupSize, is(groups));

        final String receivedGroupId = lastMessage.getStringProperty("JMSXGroupID");
        assertThat("Wrong message group id", receivedGroupId, is(groupId));

        lastMessageConsumer.close();



        final byte[] recoveredBlocks = new byte[BIG_BLOCK];

        for (int z = 1; z < receivedGroupSize; z++) {
            /*
                We use JMSXGroupID and JMSXGroupSeq for recovery simplicity.
                You are free to use JMSXGroupID and reorder messages manually.
             */
            MessageConsumer consumer = session.createConsumer(queue, "JMSXGroupID='" + receivedGroupId + "'AND JMSXGroupSeq=" + z);
            BytesMessage message = (BytesMessage) consumer.receiveNoWait();
            long bodyLength = message.getBodyLength();
            byte[] bodyBuffer = new byte[(int) bodyLength];
            message.readBytes(bodyBuffer);
            System.arraycopy(bodyBuffer, 0, recoveredBlocks, (z - 1) * SMALL_BLOCK, SMALL_BLOCK);
        }

        long lastMessageBodyLen = lastMessage.getBodyLength();
        byte[] bodyBuffer = new byte[(int) lastMessageBodyLen];
        lastMessage.readBytes(bodyBuffer);
        System.arraycopy(bodyBuffer, 0, recoveredBlocks, (receivedGroupSize - 1) * SMALL_BLOCK, SMALL_BLOCK);
        assertArrayEquals(bigBlock, recoveredBlocks);

        session.commit();
        session.close();
        connection.close();
    }

    private ConnectionFactoryImpl prepareFactory() throws JMSException {
        return new ConnectionFactoryImpl("DEFQM", new Properties() {
            {
                put(HOST_NAME_PROPERTY, "localhost");
                put(PORT_PROPERTY, 1414);
                put(TRANSPORT_PROPERTY, WMQConstants.WMQ_CM_CLIENT);
                put(CHANNEL_PROPERTY, "JVM.DEF.SVRCONN");
                put(USER_ID_PROPERTY, "ikonovalov");
            }
        });
    }
}
