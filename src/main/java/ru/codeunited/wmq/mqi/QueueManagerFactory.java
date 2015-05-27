package ru.codeunited.wmq.mqi;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

import java.util.Properties;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public interface QueueManagerFactory {

    MQQueueManager getManager(String managerName, Properties options) throws MQException;
}
