package ru.codeunited.wmq.mqi.impl;

import com.ibm.mq.*;
import ru.codeunited.wmq.mqi.MessageProducer;
import ru.codeunited.wmq.mqi.MessageSetup;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import static com.ibm.mq.constants.MQConstants.*;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public class MessageProducerImpl implements MessageProducer {

    private static final int STREAM_BUFFER_SZ = 1024;

    private final MQQueue queue;

    private static final Logger LOG = Logger.getLogger(MessageProducerImpl.class.getName());

    public MessageProducerImpl(String queueName, MQQueueManager manager) throws MQException {
        this.queue = manager.accessQueue(queueName, MQOO_OUTPUT | MQOO_FAIL_IF_QUIESCING);
    }

    @Override
    public MQMessage send(InputStream stream, MQPutMessageOptions options) throws IOException, MQException {
        final MQMessage message = new MQMessage();
        writeStreamToMessage(stream, message);
        return putWithOptions(message, options);
    }

    @Override
    public MQMessage send(InputStream stream, MessageSetup messageSetup) throws IOException, MQException {
        final MQMessage message = new MQMessage();
        final MQPutMessageOptions options = new MQPutMessageOptions();
        writeStreamToMessage(stream, message);
        messageSetup.setup(message);
        messageSetup.setup(options);
        return putWithOptions(message, options);
    }

    private MQMessage putWithOptions(MQMessage mqMessage, MQPutMessageOptions options) throws MQException {
        queue.put(mqMessage, options);
        return mqMessage;
    }

    private void writeStreamToMessage(InputStream stream, MQMessage message) throws IOException {
        final byte[] buffer = new byte[STREAM_BUFFER_SZ];
        int readCount;
        long totalBytes = 0;
        while ((readCount = stream.read(buffer)) != -1) {
            message.write(buffer, 0, readCount);
            totalBytes += readCount;
        }
        LOG.fine("File with size " + totalBytes + "b stored in a message.");
    }

    @Override
    public void close() throws IOException {
        try {
            this.queue.close();
        } catch (MQException e) {
            throw new IOException(e);
        }
    }
}
