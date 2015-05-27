package ru.codeunited.wmq.mqi;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public interface MessageProducer extends Closeable {

    MQMessage send(InputStream stream, MQPutMessageOptions options) throws IOException, MQException;

    MQMessage send(InputStream stream, MessageSetup setup) throws IOException, MQException;

}
