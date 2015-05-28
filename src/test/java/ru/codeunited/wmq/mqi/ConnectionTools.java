package ru.codeunited.wmq.mqi;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import ru.codeunited.wmq.mqi.impl.QueueManagerFactoryImpl;

import java.util.Properties;

import static com.ibm.mq.constants.CMQC.*;
import static com.ibm.mq.constants.CMQC.CHANNEL_PROPERTY;
import static com.ibm.mq.constants.CMQC.USER_ID_PROPERTY;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 28.05.15.
 */
public class ConnectionTools {

    protected final QueueManagerFactory managerFactory = new QueueManagerFactoryImpl();

    MQQueueManager getManager(QueueManagerFactory managerFactory) throws MQException {
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

    void close(MQQueueManager manager) {
        if (manager != null) {
            try {
                manager.close();
            } catch (MQException e) {
                e.printStackTrace();
            }
        }
    }

}
