package Connectors;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import opr.BusinessLogicDelegateMBean;

public class OmiActiveMessages {

	private MBeanServerConnection mbsc = null;
	private boolean human = true;
	private static final int BSMJBOSSJMXPORT = 29601;

	public OmiActiveMessages(String resUrl, boolean human)
			throws MalformedURLException, IOException {
		this.mbsc = BsmJmxConnectorFactory.getMBeanConnection(resUrl);
	}

	public OmiActiveMessages(String hostname, int port, boolean human)
			throws MalformedURLException, IOException {
		this.mbsc = BsmJmxConnectorFactory.getMBeanConnection(hostname, port);
		this.human = human;
	}

	public int getActiveMessagesCount() throws MalformedObjectNameException {
		
		
		if (human)System.out
				.println("Perform opeartions on opr.console:name=BusinessLogicDelegateMBean");


		ObjectName mBeanName = new ObjectName(
				"opr.console:name=BusinessLogicDelegateMBean");

		BusinessLogicDelegateMBean proxyMBean = JMX.newMBeanProxy(mbsc,
				mBeanName, BusinessLogicDelegateMBean.class, true);

		if (human) System.out.println("Number of events: "
				+ proxyMBean.showNumberOfEvents());
		return Integer.parseInt(proxyMBean.showNumberOfEvents());
	}

	private static void usage() {
		System.err
				.println("Usage: \njava OmiActiveMessages < <--host host> [--port port (default 29601)] [--human]| <url to jmx resource> [--human]>");
	}

	public static void main(String[] args) {
		OmiActiveMessages omiAM = null;
		Options cmdLineOptions = new Options();
		cmdLineOptions = cmdLineOptions.addOption("host", true,
				"JMX resource hostname");
		cmdLineOptions = cmdLineOptions.addOption("port", true,
				"JMX resource port");
		cmdLineOptions = cmdLineOptions.addOption("human", false,
				"Generate human readable output");
		cmdLineOptions = cmdLineOptions.addOption("url", true,
				"JMX resource url");
		CommandLineParser clp = new GnuParser();
		try {
			CommandLine cl = clp.parse(cmdLineOptions, args);
			if (cl.hasOption("url")) {
				if (cl.hasOption("human")) {
					omiAM = new OmiActiveMessages(cl.getOptionValue("url"),
							true);
				} else {
					omiAM = new OmiActiveMessages(cl.getOptionValue("url"),
							false);
				}
			} else if (cl.hasOption("host")) {
				int port = BSMJBOSSJMXPORT;
				if (cl.hasOption("port")) {
					port = Integer.parseInt(cl.getOptionValue("port"));
				}
				if (cl.hasOption("human")) {
					omiAM = new OmiActiveMessages(cl.getOptionValue("host"),
							port, true);
				} else {
					omiAM = new OmiActiveMessages(cl.getOptionValue("host"),
							port, false);
				}
			} else {
				usage();
				return;
			}
			
			int messagesQuantity = omiAM.getActiveMessagesCount();
			if (!omiAM.human){
				System.out.println(messagesQuantity);
			}
			
		} catch (ParseException pe) {
			pe.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		}

	}
}
