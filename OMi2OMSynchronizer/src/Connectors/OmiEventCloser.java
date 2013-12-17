/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Connectors;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.swing.JTextArea;
import opr.BusinessLogicDelegateMBean;

/**
 *
 * @author maskimko
 */
public class OmiEventCloser implements Runnable {

    private MBeanServerConnection mbsc = null;
    public static final int BSMJBOSSJMXPORT = 29601;
    private static final String mbn = "opr.console:name=BusinessLogicDelegateMBean";
    private Set<String> events = null;
    private JTextArea userLog = null;
    private String user = null;

    public OmiEventCloser(String url) throws IOException {
        mbsc = BsmJmxConnectorFactory.getMBeanConnection(url);
    }

    public OmiEventCloser(String hostname, int port) throws IOException {
        mbsc = BsmJmxConnectorFactory.getMBeanConnection(hostname, port);
    }

    public OmiEventCloser(String url, Set<String> eventsToClose, String user, JTextArea ta) throws IOException {
        mbsc = BsmJmxConnectorFactory.getMBeanConnection(url);
        events = eventsToClose;
        userLog = ta;
    }

    public OmiEventCloser(String hostname, int port, Set<String> eventsToClose, String user, JTextArea ta) throws IOException {
        mbsc = BsmJmxConnectorFactory.getMBeanConnection(hostname, port);
        events = eventsToClose;
        userLog = ta;
    }

    public String closeIncident(String eventId, String user) throws MalformedObjectNameException {

        ObjectName mBeanName = new ObjectName(mbn);

        BusinessLogicDelegateMBean bldmb = JMX.newMBeanProxy(mbsc, mBeanName, BusinessLogicDelegateMBean.class, false);

        String result = bldmb.closeIncident(eventId, user);
        return result;

    }

    @Override
    public void run() {
        String result = "Dry Run";
        try {
            if (user == null) {
                user = "admin";
            }
            for (String eventId : events) {
                //result = closeIncident(eventId, user);
                if (userLog != null) {
                    userLog.append("Event " + eventId + "has been closed with result " + result + "\n");
                }
            }
        } catch (NullPointerException npe) {
            System.err.println(npe.getMessage());
            if (userLog != null) {
                userLog.append(npe.getMessage() + "\n");
            }
        } /*catch (MalformedObjectNameException ex) {
            System.err.println(ex.getMessage());
            if (userLog != null) {
                userLog.append(ex.getMessage() + "\n");
            }
        } */
    }
}
