package ru.codeunited.wmq.mqi;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;

import java.io.Closeable;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public interface MessageConsumer extends Closeable {

    MQMessage get() throws MQException, NoMessageAvailableException;

    MQMessage get(MQGetMessageOptions mqGMO) throws MQException, NoMessageAvailableException;

    MQMessage get(MQGetMessageOptions mqGMO, int wait) throws MQException, NoMessageAvailableException;

    MQMessage get(MessageSetup messageSetup) throws MQException, NoMessageAvailableException;

}
