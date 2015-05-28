package ru.codeunited.wmq.mqi;

import com.ibm.mq.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.codeunited.wmq.Utils;
import ru.codeunited.wmq.mqi.impl.MessageConsumerImpl;
import ru.codeunited.wmq.mqi.impl.MessageProducerImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import static com.ibm.mq.constants.MQConstants.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 28.05.15.
 */
public class ShowGroupingMQITest extends ConnectionTools {

    private static final String QUEUE_NAME = "RFH.QTEST.SMALL";

    private static final Logger LOG = Logger.getLogger(ShowGroupingMQITest.class.getName());

    //@After
    @Before
    public void cleanupQueue() throws MQException {
        MQQueueManager manager = getManager(managerFactory);
        MessageConsumer consumer = new MessageConsumerImpl(QUEUE_NAME, manager);
        while (true) {
            try {
                consumer.get();
            } catch (NoMessageAvailableException noMoreMessages) {
                break;
            }
        }
        manager.close();
    }

    @Test
    public void putBigMessage() throws MQException, IOException, NoMessageAvailableException {
        final int BIG_BLOCK = 32768;
        final int SMALL_BLOCK = 256;

        // this is our big data block, it's bigger when MAXMSGL of the queue
        byte[] bigBlock = Utils.generateRandomBytes(BIG_BLOCK);

        MQQueueManager manager = getManager(managerFactory);

        final int groups = BIG_BLOCK / SMALL_BLOCK;

        // put data chunk into the queue
        try (final MessageProducer producer = new MessageProducerImpl(QUEUE_NAME, manager)) {

            MQPutMessageOptions putOptions = new MQPutMessageOptions();
            putOptions.options = MQPMO_NONE | MQPMO_LOGICAL_ORDER;
            for (int z = 0; z < groups; z++) {
                byte[] smallBlock = new byte[SMALL_BLOCK];
                System.arraycopy(bigBlock, z * SMALL_BLOCK, smallBlock, 0, SMALL_BLOCK);
                InputStream stream = new ByteArrayInputStream(smallBlock);
                final int messageIndex = z;
                MQMessage sentMessage = producer.send(stream, new MessageSetup() {
                    @Override
                    public void setup(MQPutMessageOptions putOptions) {
                        putOptions.options = MQPMO_LOGICAL_ORDER;
                    }

                    @Override
                    public void setup(MQMessage message) {
                        message.format = MQFMT_NONE;
                        if (messageIndex == (groups - 1)) {
                            message.messageFlags = MQMF_LAST_MSG_IN_GROUP;
                        } else {
                            message.messageFlags = MQMF_MSG_IN_GROUP;
                        }
                    }
                });
            }
        }

        // receive and compile messages
        try (final MessageConsumer consumer = new MessageConsumerImpl(QUEUE_NAME, manager)) {

            byte[] recoveredBlocks = new byte[BIG_BLOCK];
            int messageIndex = 0;
            // get first message
            final MQGetMessageOptions getOptions = new MQGetMessageOptions();
            getOptions.options = MQGMO_LOGICAL_ORDER | MQGMO_ALL_MSGS_AVAILABLE;
            MQMessage message = consumer.get(getOptions);
            recover(SMALL_BLOCK, recoveredBlocks, messageIndex, message);
            messageIndex++;

            // get outer messages
            getOptions.matchOptions = MQMO_MATCH_GROUP_ID; // we will get only messages from that group

            while (getOptions.groupStatus != MQGS_LAST_MSG_IN_GROUP) {
                message = consumer.get(message, getOptions);
                recover(SMALL_BLOCK, recoveredBlocks, messageIndex, message);
                messageIndex++;
            }

            assertArrayEquals(bigBlock, recoveredBlocks);
        }

        close(manager);
    }

    private void recover(int SMALL_BLOCK, byte[] recoveredBlocks, int messageIndex, MQMessage message) throws IOException {
        byte[] bodyBuffer = new byte[message.getDataLength()];
        message.readFully(bodyBuffer);
        System.arraycopy(bodyBuffer, 0, recoveredBlocks, messageIndex * SMALL_BLOCK, SMALL_BLOCK);
    }

    private void showMsgStatus(MQMessage message, MQGetMessageOptions getOptions) {
        LOG.info("Seq num: " + message.messageSequenceNumber + " GrSt [" + getOptions.groupStatus + "]");
    }

}
