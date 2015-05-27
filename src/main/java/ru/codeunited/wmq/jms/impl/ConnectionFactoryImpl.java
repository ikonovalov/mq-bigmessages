package ru.codeunited.wmq.jms.impl;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import ru.codeunited.wmq.jms.ConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;

import static com.ibm.mq.constants.MQConstants.*;

import java.util.Properties;



/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public class ConnectionFactoryImpl implements ConnectionFactory {

    private MQQueueConnectionFactory factory = null;

    public ConnectionFactoryImpl(String queueManagerName, Properties properties) throws JMSException {
        factory = new MQQueueConnectionFactory();
        factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        factory.setQueueManager(queueManagerName);
        factory.setHostName(properties.getProperty(HOST_NAME_PROPERTY));
        factory.setPort((Integer) properties.get(PORT_PROPERTY));
        factory.setChannel((String) properties.get(CHANNEL_PROPERTY));
    }

    @Override
    public Connection getConnection() throws JMSException {
        return factory.createConnection();
    }

    @Override
    public Connection getConnection(String user, String password) throws JMSException {
        return factory.createConnection(user, password);
    }

}
