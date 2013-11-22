package template

import com.hp.opr.api.ws.adapter.*
import com.hp.opr.api.ws.model.event.*

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException

class TemplateAdapter {
    private static final String LOG_DIR_REL = "log${File.separator}opr${File.separator}integration"
    private static final String LOGFILE_NAME = "TemplateAdapter.log"
    private static final JAXBContext m_jaxbContext =
        JAXBContext.newInstance(OprEventList.class, OprEventChangeList.class, OprHistoryLineList.class)

    private def m_logfile = null
    private def m_logger = null
    private InitArgs m_initArgs = null

    void init(final InitArgs args) {
        m_logger = args.logger
        m_initArgs = args

        def logfileDir = new File("${args.installDir}${File.separator}${LOG_DIR_REL}")
        if (!logfileDir.exists())
            logfileDir.mkdirs()

        m_logfile = new File(logfileDir, LOGFILE_NAME)
        if (!m_logfile.exists())
            m_logfile.createNewFile()

        m_logger.debug("Logfile Adapter initalized. INSTALL_DIR=${args.installDir}")

        def timestamp = new Date()
        def msg = """### ${timestamp.toString()}: init() called ###
parameter connected server ID: ${m_initArgs.connectedServerId}
parameter connected server name: ${m_initArgs.connectedServerName}
parameter connected server display name: ${m_initArgs.connectedServerDisplayName}
parameter node: ${m_initArgs.node}
parameter port: ${m_initArgs.port}
parameter ssl:  ${m_initArgs.nodeSsl}
parameter drilldown node: ${m_initArgs.drilldownNode}
parameter drilldown port: ${m_initArgs.drilldownPort}
parameter drilldown ssl:  ${m_initArgs.drilldownNodeSsl}

"""
        m_logfile.append(msg)
    }

    void destroy() {
        m_logger.debug("Logfile Adapter destroy.")

        def timestamp = new Date()
        def msg = """### ${timestamp.toString()}: destroy() called ###

"""
        m_logfile.append(msg)
    }

    Boolean ping(final PingArgs args) {
        // TODO: 1. Implement this to support "Test Connection" in Connected Server Administration

        args.setOutputDetail("Unsupported opration. Edit Groovy script 'TemplateAdapter.groovy' to add ping support.")
        return false
    }

    Boolean forwardEvent(final ForwardEventArgs args) {
        // TODO: 2. Implement this to forward events and support NOTIFY

        // Send the event to the target server
        OprEvent event = args.event

        return false
    }

    Boolean getExternalEvent(final GetExternalEventArgs args) {
        // TODO: 3. Implement this to support "External Info" tab in event browser.

        return false
    }

    Boolean forwardChange(final ForwardChangeArgs args) {
        // TODO: 4. Implement this to forward changes and support NOTIFY & UPDATE

        // Send the updates to the target server
        final OprEventChange changes = args.changes

        return false
    }

    Boolean receiveChange(final ReceiveChangeArgs args) {
        // TODO: 5. Implement this to support SYNCHRONIZE, SYNCHRONIZE & TRANSFER COTRNOL

        // This method will receive & process the changes from the target server.
        return true;
    }

    String toExternalEvent(final OprEvent event) {
        // TODO: 6. Implement this method if the External Process makes a GET call to:
        //          /opg-gateway/synchronization/event/{id}
        //          to get a copy of the Event. Default is to return XML in of the OPR event.
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream()
            OutputStreamWriter osw = new OutputStreamWriter(bos, "utf-8")
            m_jaxbContext.createMarshaller().marshal(event, osw)
            return osw.toString()
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e)
        }
        return "";
    }

    // TODO: 7. The following are optional to support Bulk operations
    Boolean forwardEvents(final BulkForwardEventArgs args) {
        return false;
    }

    Boolean forwardChanges(final BulkForwardChangeArgs args) {
        return false;
    }

    Boolean receiveChanges(final BulkReceiveChangeArgs args) {
        throw new UnsupportedOperationException("Bulk change receipt not supported by this groovy script.");
    }
}


