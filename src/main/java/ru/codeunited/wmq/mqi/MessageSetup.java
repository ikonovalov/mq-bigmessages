package ru.codeunited.wmq.mqi;

import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public abstract class MessageSetup {

    /**
     * Prepare MQMessage before MQPUT/MQGET operations.
     * @param message
     */
    public void setup(MQMessage message) {

    }

    /**
     * Prepare MQPutMessageOptions before MQPUT operation.
     * @param putOptions
     */
    public void setup(MQPutMessageOptions putOptions) {

    }

    /**
     * Prepare MQGetMessageOptions before MQGET operation.
     * @param getOptions
     */
    public void setup(MQGetMessageOptions getOptions) {

    }
}
