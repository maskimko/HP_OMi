package Connectors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import opr.BusinessLogicDelegateMBean;

/**
 * @author maskimko
 * This class is intended to generate connection to jmx resources of java application
 */
public class BsmJmxConnectorFactory {

public static MBeanServerConnection getMBeanConnection(String resUrl) throws IOException, MalformedURLException {
	JMXServiceURL url = new JMXServiceURL(resUrl);
	JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
	MBeanServerConnection mbc = jmxc.getMBeanServerConnection();
	return mbc;
}
	
	
public static MBeanServerConnection getMBeanConnection(JMXServiceURL url) throws MalformedURLException, IOException, MalformedObjectNameException{
	JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
	MBeanServerConnection mbc = jmxc.getMBeanServerConnection();
	return mbc;
}
public static MBeanServerConnection getMBeanConnection(String hostname, int port) throws MalformedURLException, IOException{
	JMXServiceURL url = new JMXServiceURL(
			"service:jmx:rmi:///jndi/rmi://"+ hostname + ":"+ port+"/jmxrmi");
	JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
	MBeanServerConnection mbc = jmxc.getMBeanServerConnection();
	return mbc;
}
private static void usage(){
	System.err.println("Usage: java BsmJmxConnectorFactory < <host> <port> | <url to jmx resource>>");
}

	
	
//	private static final String bsmGatewayProduction = "bsm-gw1.sdab.sn";
//	private static final String bsmGatewayTest = "bsm-test1.sdab.sn";
//	private static final int bsmGatewayProductionPort = 29601;
//	private static final int bsmGatewayTestPort = 29601;
	
	public static void main(String[] args) throws MalformedURLException,
			IOException, MalformedObjectNameException {
		MBeanServerConnection mbc = null;
		if (args.length > 2 || args.length == 0) {
			usage();
			return;
		} else if (args.length == 2 ){
			mbc = getMBeanConnection(args[0], Integer.parseInt(args[1]));
		} else if (args.length == 1) {
			mbc = getMBeanConnection(args[0]);
		}
		System.out.println("Connection to " + args[0] + "has been established");
		System.out.println("Press <Enter> to continue...");

		System.in.read();

		System.out.println("Domains: ");
		String[] domains = mbc.getDomains();
		Arrays.sort(domains);
		for (int i = 0; i < domains.length; i++) {
			System.out.println("\t" + domains[i]);
		}
		System.out.println("MBean default domain: " + mbc.getDefaultDomain());

		System.out.println("Press <Enter> to continue...");
		
		
		System.in.read();

		System.out.println("MBean count: " + mbc.getMBeanCount());

		System.out.println("Query MBeanServer MBeans");

		Set<ObjectName> names = new TreeSet<ObjectName>(mbc.queryNames(null,
				null));
		for (ObjectName name : names) {
			System.out.println("MBean name: " + name);
		}

	
	}
}
