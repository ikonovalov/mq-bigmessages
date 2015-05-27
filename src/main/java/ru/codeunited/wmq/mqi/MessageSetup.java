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

    public void setup(MQMessage message) {

    }

    public void setup(MQPutMessageOptions putOptions) {

    }

    public void setup(MQGetMessageOptions getOptions) {

    }
}
