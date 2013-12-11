/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Connectors;

import java.io.IOException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import opr.BusinessLogicDelegateMBean;

/**
 *
 * @author maskimko
 */
public class OmiEventCloser {
    
    private MBeanServerConnection mbsc = null;
    public static final int BSMJBOSSJMXPORT = 29601;
    private static final String mbn = "opr.console:name=BusinessLogicDelegateMBean";
    
    public OmiEventCloser(String url) throws IOException{
        mbsc = BsmJmxConnectorFactory.getMBeanConnection(url);
    }
    
    public OmiEventCloser(String hostname, int port) throws IOException{
        mbsc = BsmJmxConnectorFactory.getMBeanConnection(hostname, port);
    }
    
    
    public String closeIncident(String eventId, String user) throws MalformedObjectNameException{
        
        ObjectName mBeanName = new ObjectName(mbn);
        
        BusinessLogicDelegateMBean bldmb = JMX.newMBeanProxy(mbsc, mBeanName, BusinessLogicDelegateMBean.class, false);
        
        String result = bldmb.closeIncident(eventId, user);
        return result;
        
    }
}
