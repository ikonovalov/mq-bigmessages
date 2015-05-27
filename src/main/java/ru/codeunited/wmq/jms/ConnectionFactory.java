package ru.codeunited.wmq.jms;

import javax.jms.Connection;
import javax.jms.JMSException;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public interface ConnectionFactory {
    Connection getConnection() throws JMSException;

    Connection getConnection(String user, String password) throws JMSException;
}
