package ru.codeunited.wmq.mqi;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

import java.util.Properties;

import static com.ibm.mq.constants.MQConstants.*;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public final class BaseFunction {

    static MQQueueManager getManager(QueueManagerFactory managerFactory) throws MQException {
        MQQueueManager manager;
        manager = managerFactory.getManager("DEFQM", new Properties() {
            {
                put(HOST_NAME_PROPERTY, "localhost");
                put(PORT_PROPERTY, 1414);
                put(TRANSPORT_PROPERTY, TRANSPORT_MQSERIES_CLIENT);
                put(CHANNEL_PROPERTY, "JVM.DEF.SVRCONN");
                put(USER_ID_PROPERTY, "ikonovalov");
                //put(PASSWORD_PROPERTY, "");
            }
        });
        return manager;
    }

    static void close(MQQueueManager manager) {
        if (manager != null) {
            try {
                manager.close();
            } catch (MQException e) {
                e.printStackTrace();
            }
        }
    }

}
