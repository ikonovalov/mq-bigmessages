package ru.codeunited.wmq.mqi.impl;

import com.ibm.mq.*;
import ru.codeunited.wmq.mqi.MessageConsumer;
import ru.codeunited.wmq.mqi.MessageSetup;
import ru.codeunited.wmq.mqi.NoMessageAvailableException;

import java.io.IOException;

import static com.ibm.mq.constants.MQConstants.*;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public class MessageConsumerImpl implements MessageConsumer {

    private final MQQueue queue;

    public MessageConsumerImpl(String queueName, MQQueueManager manager) throws MQException {
        queue = manager.accessQueue(queueName, MQOO_FAIL_IF_QUIESCING | MQOO_INPUT_AS_Q_DEF);
    }

    @Override
    public MQMessage get() throws MQException, NoMessageAvailableException {
        return get(new MQGetMessageOptions());
    }

    @Override
    public MQMessage get(MQGetMessageOptions mqGMO) throws MQException, NoMessageAvailableException {
        MQMessage message = new MQMessage();
        mqGMO.options = mqGMO.options | MQGMO_NO_WAIT;
        return get(message, mqGMO);
    }

    @Override
    public MQMessage get(MQGetMessageOptions mqGMO, int wait) throws MQException, NoMessageAvailableException {
        MQMessage message = new MQMessage();
        mqGMO.options = mqGMO.options | MQGMO_WAIT;
        if (wait < 0)
            mqGMO.waitInterval = MQWI_UNLIMITED;
        else
            mqGMO.waitInterval = wait;
        return get(message, mqGMO);
    }

    @Override
    public MQMessage get(MessageSetup messageSetup) throws MQException, NoMessageAvailableException {
        final MQMessage message = new MQMessage();
        final MQGetMessageOptions options = new MQGetMessageOptions();
        messageSetup.setup(message);
        messageSetup.setup(options);
        return get(message, options);
    }

    private MQMessage get(MQMessage message, MQGetMessageOptions getMessageOptions) throws NoMessageAvailableException, MQException {
        try {
            queue.get(message, getMessageOptions);
        } catch (MQException mqe) {
            if (mqe.reasonCode == MQRC_NO_MSG_AVAILABLE)
                throw new NoMessageAvailableException(queue.getName(), mqe);
            else
                throw mqe;
        }
        return message;
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
