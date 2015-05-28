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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public class ShowSegmentationMQITest extends ConnectionTools {

    private static final String QUEUE_NAME = "RFH.QTEST.SMALL";

    private static final Logger LOG = Logger.getLogger(ShowSegmentationMQITest.class.getName());

    @After
    @Before
    public void cleanupQueue() throws MQException {
        MQQueueManager manager = getManager(managerFactory);
        MessageConsumer consumer = new MessageConsumerImpl(QUEUE_NAME, manager);
        while(true) {
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
        MQQueueManager manager = null;
        byte[] sentMessageID = null;
        byte[] gotMessageID = null;

        // this is our big data block, it's bigger when MAXMSGL of the queue
        byte[] bigBlock = Utils.generateRandomBytes(BIG_BLOCK);

        try {
            // perform connection
            manager = getManager(managerFactory);

            // put oversize message
            try (final MessageProducer producer = new MessageProducerImpl(QUEUE_NAME, manager)) {

                InputStream stream = new ByteArrayInputStream(bigBlock);

                MQMessage sentMessage = producer.send(stream, new MessageSetup() {
                    @Override
                    public void setup(MQMessage message) {
                        message.messageFlags = MQMF_SEGMENTATION_ALLOWED; // THIS IS IMPORTANT!
                    }

                    @Override
                    public void setup(MQPutMessageOptions putOptions) {
                        putOptions.options = MQPMO_NONE | MQPMO_NEW_MSG_ID | MQPMO_NO_SYNCPOINT;
                    }
                });
                sentMessageID = sentMessage.messageId;
                LOG.info(String.format("Sent message %s", HexConverter.bytesToHex(sentMessage.messageId)));
            }

            // get oversize message
            try(final MessageConsumer consumer = new MessageConsumerImpl(QUEUE_NAME, manager)) {
                MQMessage gotMessage = consumer.get(new MessageSetup() {
                    @Override
                    public void setup(MQGetMessageOptions getOptions) {
                        getOptions.options = MQGMO_NONE | MQGMO_COMPLETE_MSG;
                    }
                });
                gotMessageID = gotMessage.messageId;

                assertArrayEquals(sentMessageID, gotMessageID);

                // check body
                int bodyLength = gotMessage.getDataLength();
                assertThat("Body has wrong size", bodyLength, is(BIG_BLOCK));

                byte[] body = new byte[bodyLength];
                gotMessage.readFully(body);


                LOG.info(String.format("Got message %s", HexConverter.bytesToHex(gotMessage.messageId)));
            }

        } finally {
            close(manager);
        }
    }


}
