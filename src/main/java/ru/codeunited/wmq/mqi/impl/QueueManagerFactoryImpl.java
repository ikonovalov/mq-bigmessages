package ru.codeunited.wmq.mqi.impl;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import ru.codeunited.wmq.mqi.QueueManagerFactory;

import java.util.Properties;

/**
 * No-cached connection factory
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public class QueueManagerFactoryImpl implements QueueManagerFactory {

    @Override
    public MQQueueManager getManager(String managerName, Properties options) throws MQException {
        MQQueueManager mqQueueManager = new MQQueueManager(managerName, options);
        return mqQueueManager;
    }

}
