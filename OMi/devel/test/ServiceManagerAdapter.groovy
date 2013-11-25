package test;




import com.hp.opr.api.Version
import com.hp.opr.api.ws.adapter.ForwardChangeArgs
import com.hp.opr.api.ws.adapter.ForwardEventArgs
import com.hp.opr.api.ws.adapter.GetExternalEventArgs
import com.hp.opr.api.ws.adapter.InitArgs
import com.hp.opr.api.ws.adapter.PingArgs
import com.hp.opr.api.ws.adapter.ReceiveChangeArgs
import com.hp.opr.api.ws.model.event.OprAnnotation
import com.hp.opr.api.ws.model.event.OprAnnotationList
import com.hp.opr.api.ws.model.event.OprControlTransferInfo
import com.hp.opr.api.ws.model.event.OprControlTransferStateEnum
import com.hp.opr.api.ws.model.event.OprCustomAttribute
import com.hp.opr.api.ws.model.event.OprCustomAttributeList
import com.hp.opr.api.ws.model.event.OprEvent
import com.hp.opr.api.ws.model.event.OprEventChange
import com.hp.opr.api.ws.model.event.OprEventReference
import com.hp.opr.api.ws.model.event.OprGroup
import com.hp.opr.api.ws.model.event.OprPriority
import com.hp.opr.api.ws.model.event.OprSeverity
import com.hp.opr.api.ws.model.event.OprState
import com.hp.opr.api.ws.model.event.OprSymptomList
import com.hp.opr.api.ws.model.event.OprSymptomReference
import com.hp.opr.api.ws.model.event.OprUser
import com.hp.opr.api.ws.model.event.ci.OprConfigurationItem
import com.hp.opr.api.ws.model.event.ci.OprForwardingInfo
import com.hp.opr.api.ws.model.event.ci.OprForwardingTypeEnum
import com.hp.opr.api.ws.model.event.ci.OprNodeReference
import com.hp.opr.api.ws.model.event.ci.OprRelatedCi
import com.hp.opr.api.ws.model.event.property.OprAnnotationPropertyChange
import com.hp.opr.api.ws.model.event.property.OprCustomAttributePropertyChange
import com.hp.opr.api.ws.model.event.property.OprEventPropertyChange
import com.hp.opr.api.ws.model.event.property.OprEventPropertyNameEnum
import com.hp.opr.api.ws.model.event.property.OprGroupPropertyChange
import com.hp.opr.api.ws.model.event.property.OprIntegerPropertyChange
import com.hp.opr.api.ws.model.event.property.OprPropertyChangeOperationEnum
import com.hp.opr.api.ws.model.event.property.OprSymptomPropertyChange
import com.hp.opr.api.ws.model.event.property.OprUserPropertyChange
import com.hp.opr.common.ws.client.WinkClientSupport
import com.hp.ucmdb.api.UcmdbService
import com.hp.ucmdb.api.UcmdbServiceFactory
import com.hp.ucmdb.api.UcmdbServiceProvider
import com.hp.ucmdb.api.topology.QueryDefinition
import com.hp.ucmdb.api.topology.QueryLink
import com.hp.ucmdb.api.topology.QueryNode
import com.hp.ucmdb.api.topology.Topology
import com.hp.ucmdb.api.topology.TopologyCount
import com.hp.ucmdb.api.topology.TopologyQueryService
import com.hp.ucmdb.api.topology.TopologyUpdateFactory
import com.hp.ucmdb.api.topology.indirectlink.IndirectLink
import com.hp.ucmdb.api.topology.indirectlink.IndirectLinkStepToPart
import com.hp.ucmdb.api.types.TopologyCI
import com.hp.ucmdb.api.types.UcmdbId
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import javax.ws.rs.core.Cookie
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.xml.bind.JAXBElement
import org.apache.commons.codec.binary.Base64
import org.apache.wink.client.ClientRequest
import org.apache.wink.client.ClientResponse
import org.apache.wink.client.ClientWebException
import org.apache.wink.client.Resource
import org.apache.wink.client.RestClient
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.xml.namespace.QName;
import org.apache.commons.logging.Log;

public class ServiceManagerAdapter {

    // ************************************************************************
    // * BEGIN Configuration: Customization of properties for synchronization *
    // ************************************************************************

    // See the OMi Extensibility Guide document for complete details on customizing the OMi to SM integration

    // Change 'webtier-9.30' in the following to match the name of the web application installed on the target SM server
    public static final String SM_WEB_TIER_NAME = 'sm'

    // BSM Administrator login name
    // For OMi events that are forwarded via a forwarding rule, the SM "is_recorded_by" field is set to this user,
    // otherwise it is set to the OMi operator that initiated the manual transfer.
    public static final String BSM_ADMINISTRATOR_LOGIN_NAME = 'admin'

    // The following RTSM variables are used on pre 9.21 releases to determine the affected business service.
    // If set, the most critically affected business service, in relation to the related CI of the event,
    // will be set in the SM incident created. This will appear as the "Affected Service" in the Incident.
    // These values are ignored on release 9.21 or later, as this information is already passed to the
    // forwardEvent() method in release 9.21 or greater.
    //
    final int RTSM_PORT = 80
    final int RTSM_MAX_CI_COUNT = 1000

    final String RTSM_HOSTNAME = "localhost"
    final String RTSM_USERNAME = ""
    final String RTSM_PASSWORD = ""

    // Sets the maximum number of relation steps between Related CI and affected business service.
    // If set to zero then the query is disabled. Default is 10.
    final int RTSM_QUERY_MAX_STEPS = 10

    // Sets the maximum number of duplicate count updates to send to SM. Should the duplicate count entry exceed this
    // number, no new activity log entry for duplicate count changes will be made to the SM incident.
    final int MAX_DUPLICATE_COUNT_UPDATES = 10

    // Maps the OPR event 'state' to the SM incident 'status'
    //
    public static final Map MapOPR2SMStatus = ["open": "open", "in_progress": "work-in-progress",
            "resolved": "resolved", "closed": "resolved"]

    // Maps the SM incident 'status' to OPR event 'state'
    //
    public static final Map MapSM2OPRState = ["accepted": "open", "assigned": "open", "open": "open", "reopened": "open",
            "pending-change": "in_progress", "pending-customer": "in_progress", "pending-other": "in_progress",
            "pending-vendor": "in_progress", "referred": "in_progress", "suspended": "in_progress",
            "work-in-progress": "in_progress", "rejected": "resolved", "replaced-problem": "resolved",
            "resolved": "closed", "cancelled": "resolved", "closed": "closed"]

    // Maps OPR event 'severity' to SM incident 'urgency'
    //
    public static final Map MapOPR2SMUrgency = ["critical": "1", "major": "2", "minor": "3", "warning": "3",
            "normal": "4", "unknown": "4"]

    // Maps SM incident 'urgency' to OPR event 'severity'
    //
    public static final Map MapSM2OPRSeverity = ["1": "critical", "2": "major", "3": "minor", "4": "normal"]

    // Maps OPR event 'priority' to SM incident 'priority'
    //
    public static final Map MapOPR2SMPriority = ["highest": "1", "high": "2", "medium": "3", "low": "4",
            "lowest": "4", "none": "4"]

    // Maps SM incident 'priority' to OPR event 'priority'
    //
    public static final Map MapSM2OPRPriority = ["1": "highest", "2": "high", "3": "medium", "4": "low"]

    // SM Completion Code value to use on close of the SM incident
    //
    public static final String SMCompletionCode = 'Automatically Closed'

    // ****************************************************************************************************
    // * The following sets control which properties and enumerated values are synchronized on change.    *
    // * They are therefore not relevant for an Event Channel deployment. They are only relevant for OMi. *
    // ****************************************************************************************************

    // Only "closed" state and some properties are synchronized by default.
    // Set this values to true if you wish all states and properties to be synchronized in both directions.
    // This value will override all synchronization settings listed below.

    /**
     * We change it in hope to disable updating unwanted properties after incdent has been opened
     * 19 november 2013 18:42
     * Switched back at 19:11   as it disables correct behaviour of astl_operational_device flag
     *
     */
    public static final boolean SyncAllProperties = true

    // Specifies that the node should be used instead of the event related CI
    // as the "is_registered_for" CI in the incident
    public static final boolean UseNodeCI = false

    // In 9.21 or greater the most critical affected business service is added to the Incident.
    // The following flag control whether the business service most closely related to the Related CI is
    // sent or are all affected business services recursively searched for the most critically affected.
    public static final boolean RecursiveSearchBusinessServices = true

    // Flag to disable checking for out of sync state of 'closed'.
    // The response of the SM incident update is checked if the state of the Incident is 'closed'.
    // If the incident state is 'closed', but the event state is not 'closed', the event will be
    // automatically closed by the script. Requires SyncSMStatusToOPR to contain 'closed' state
    // and SyncSMPropertiesToOPR to contain 'incident_status'.
    // This feature is only available on release 9.21 or later.
    public static final boolean DisableCheckCloseOnUpdate = false

    // OPR event properties to synchronize to a corresponding SM incident property on change:
    //
    // "title", "description", "state", "severity", "priority", "solution", "cause", "assigned_user", "assigned_group"
    //
    // If synchronization on change is desired for an OPR event property, add the property to the list. It will
    // then be synchronized to a corresponding SM property whenever the event property is changed in OMi.
    //
    public static final Set SyncOPRPropertiesToSM = ["state", "solution", "cause", "custom_attribute", "operational_device", "event_addon"]

    // OPR event properties to synchronize to a corresponding SM Incident "activity log" on change:
    //
    // "title", "description", "state", "severity", "priority", "solution", "annotation", "duplicate_count",
    // "assigned_user", "assigned_group", "cause", "symptom", "control_transferred_to", "time_state_changed"
    //
    // If synchronization on change is desired for an OPR event property, add the property to the list. It will
    // then be synchronized to a corresponding SM property whenever the event property is changed in OMi.
    //


    public static final Set SyncOPRPropertiesToSMActivityLog = ["title", "description", "state", "severity", "priority",
            "annotation", "duplicate_count", "cause", "symptom", "assigned_user", "assigned_group", "custom_attribute", "event_addon"]

    // SM Incident properties to synchronize to a corresponding OPR Event property on change:
    //
    // "name", "description", "incident_status", "urgency", "priority", "solution"
    //
    // If synchronization on change is desired for an SM incident property, add the property to the list. It will
    // then be synchronized to a corresponding OPR event property whenever the incident property is changed in SM.
    //

    //TODO check Opr or SM syntax of field Event Addon
    public static final Set SyncSMPropertiesToOPR = ["incident_status", "solution", "operational_device", "event_addon", "custom_attribute"]

    // OPR event states to synchronize to the SM incident status on change.
    //
    // If synchronize on change for an event 'state' is desired, add it to the list. "*" specifies all states.
    // NOTE: "state" must be included in SyncOPRPropertiesToSM or this list is ignored.
    public static final Set SyncOPRStatesToSM = ["closed"]

    // OPR event severities to synchronize to the SM incident urgency on change.
    //
    // If synchronize on change for an event 'severity' is desired, add it to the list. "*" specifies all severities.
    // NOTE: "severity" must be included in SyncOPRPropertiesToSM or this list is ignored.
    public static final Set SyncOPRSeveritiesToSM = ["*"]

    // OPR event priorities to synchronize to the SM incident priority on change.
    //
    // If synchronize on change for an event 'priority' is desired, add it to the list. "*" specifies all priorities.
    // NOTE: "priority" must be included in SyncOPRPropertiesToSM or this list is ignored.
    public static final Set SyncOPRPrioritiesToSM = ["*"]

    // SM incident status to synchronize to the OM event states on change to this incident property:
    //
    // If synchronize on change for an incident 'status' is desired, add it to the list. "*" specifies all status.
    // NOTE: "status" must be included in SyncSMPropertiesToOPR or this list is ignored.
    public static final Set SyncSMStatusToOPR = ["closed"]

    // SM incident urgencies to synchronize to the OM event severities on change to this incident property:
    //
    // If synchronize on change for an incident 'urgency' is desired, add it to the list. "*" specifies all urgencies.
    // NOTE: "urgency" must be included in SyncSMPropertiesToOPR or this list is ignored.
    public static final Set SyncSMUrgenciesToOPR = ["*"]

    // SM incident priorities to synchronize to the OM event priorities on change to this incident property:
    //
    // If synchronize on change for an incident 'priority' is desired, add it to the list. "*" specifies all priorities.
    // NOTE: "priority" must be included in SyncSMPropertiesToOPR or this list is ignored.
    public static final Set SyncSMPrioritiesToOPR = ["*"]

    // Map the specified OPR custom attributes to an SM incident property for synchronization.
    // Add a CA name to the map along with SM incident property name (XML tag name).
    // Target SM Incident property name of "activity_log" will append the CA change to the SM incident activity log.
    //
    // NOTE: Only top-level SM incident properties are supported in this map.
    // EXAMPLE: ["MyCustomCA" : "activity_log", "MyCustomCA_1" : "SMCustomAttribute" ]
    //public static final Map<String, String> MapOPR2SMCustomAttribute = ["operational_device": ASTL_OPERATIONAL_DEVICE_TAG, "operational_device": ACTIVITY_LOG_TAG, "event_addon": "EventAddon", "event_addon": ACTIVITY_LOG_TAG ]
    public static final Map<String, String> MapOPR2SMCustomAttribute = ["operational_device": "OperationalDevice", "event_addon": "EventAddon"]

    // Map the specified SM incident properties to an OPR event custom attribute for synchronization.
    // Add an SM incident property name to the map along with OPR event custom attribute name.
    //
    // EXAMPLE: ["incident_status" : "SMIncidentStatus"]
    public static final Map<String, String> MapSM2OPRCustomAttribute = ["OperationalDevice": "operational_device", "EventAddon": "event_addon"]
    // **********************************************************************
    // * END Configuration: Customization of properties for synchronization *
    // **********************************************************************

    // **********************************************************************   ********
    // * BEGIN Localization: Customization of text values for language localization *
    // ******************************************************************************

    // SM Urgency values:
    // The text value will be displayed in the external info tab.
    // NOTE: This text may be localized for the desired locale.
    public static final Map SMUrgency = ["1": "1 - Critical", "2": "2 - High", "3": "3 - Average", "4": "4 - Low"]

    // SM Priority values:
    // The text value will be displayed in the external info tab.
    // NOTE: This text may be localized for the desired locale.
    public static final Map SMPriority = ["1": "1 - Critical", "2": "2 - High", "3": "3 - Average", "4": "4 - Low"]

    // Change the following to customize the date format in the annotation entries synchronized to the SM activity log
    // See Java SimpleDateFormat for details on the syntax of the two parameters.
    // e.g., to set Japanese locale: LOCALE = Locale.JAPAN
    public static final Locale LOCALE = Locale.getDefault()
    public static final String ANNOTATION_DATE_FORMAT = "yyyy.MM.dd HH:mm:ss z"

    // In SM the description is a required attribute. In case it is not set in BSM this value is taken.
    // An empty string is NOT allowed.
    public static final String EMPTY_DESCRIPTION_OVERRIDE = "<none>"





    public String astl_operational_device = "false"

    // SM Incident Activity Log text.
    // This text is prefixed to the appropriate OPR event property when synchronizing it to an SM Incident activity log.
    // NOTE: This text may be localized for the desired locale.
    public static final String ACTIVITY_LOG_TITLE = "[Title]"
    public static final String ACTIVITY_LOG_TITLE_CHANGE = "Event title changed to: "
    public static final String ACTIVITY_LOG_STATE = "[State]"
    public static final String ACTIVITY_LOG_STATE_CHANGE = "Event state changed to: "
    public static final String ACTIVITY_LOG_DESCRIPTION = "[Description]"
    public static final String ACTIVITY_LOG_DESCRIPTION_CHANGE = "Event description changed to: "
    public static final String ACTIVITY_LOG_SOLUTION = "[Solution]"
    public static final String ACTIVITY_LOG_SOLUTION_CHANGE = "Event solution changed to: "
    public static final String ACTIVITY_LOG_ASSIGNED_USER = "[Assigned User]"
    public static final String ACTIVITY_LOG_ASSIGNED_USER_CHANGE = "Event assigned user changed to: "
    public static final String ACTIVITY_LOG_ASSIGNED_GROUP = "[Assigned Group]"
    public static final String ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE = "Event assigned group changed to: "
    public static final String ACTIVITY_LOG_UNASSIGNED = "<unassigned>"
    public static final String ACTIVITY_LOG_SEVERITY = "[Severity]"
    public static final String ACTIVITY_LOG_SEVERITY_CHANGE = "Event severity changed to: "
    public static final String ACTIVITY_LOG_PRIORITY = "[Priority]"
    public static final String ACTIVITY_LOG_PRIORITY_CHANGE = "Event priority changed to: "
    public static final String ACTIVITY_LOG_CONTROL_TRANSFERRED_TO = "[Control Transferred To]"
    public static final String ACTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED = "Event control transfer state changed to: "
    public static final String ACTIVITY_LOG_CATEGORY = "[Category]"
    public static final String ACTIVITY_LOG_SUBCATEGORY = "[Subcategory]"
    public static final String ACTIVITY_LOG_APPLICATION = "[Application]"
    public static final String ACTIVITY_LOG_OBJECT = "[Object]"
    public static final String ACTIVITY_LOG_ANNOTATION = "[Annotation]"
    public static final String ACTIVITY_LOG_CA = "[Custom Attribute]"
    public static final String ACTIVITY_LOG_CAUSE = "[Cause]"
    public static final String ACTIVITY_LOG_OMI_CAUSE = "[OMi Cause]"
    public static final String ACTIVITY_LOG_OMI_SYMPTOM = "[OMi Symptom]"
    public static final String ACTIVITY_LOG_DUPLICATE_COUNT = "[Duplicate Count]"
    public static final String ACTIVITY_LOG_PREVIOUS = "previous"
    public static final String ACTIVITY_LOG_CURRENT = "current"
    public static final String ACTIVITY_LOG_INITIATED_BY = "[Initiated by]"
    public static final String ACTIVITY_LOG_INITIATED_BY_RULE = "BSM forwarding rule: "
    public static final String ACTIVITY_LOG_INITIATED_BY_USER = "BSM operator: "
    public static final String ACTIVITY_LOG_RELATED_CI = "[BSM Related CI]"
    public static final String ACTIVITY_LOG_RELATED_CI_TYPE_LABEL = "Type label: "
    public static final String ACTIVITY_LOG_RELATED_CI_TYPE = "Type: "
    public static final String ACTIVITY_LOG_RELATED_CI_LABEL = "Display label: "
    public static final String ACTIVITY_LOG_RELATED_CI_NAME = "Name: "
    public static final String ACTIVITY_LOG_RELATED_CI_HOSTED_ON = "Hosted on: "
    public static final String ACTIVITY_LOG_RELATED_CI_URL = "Cross launch URL: "
    public static final String ACTIVITY_LOG_AFFECTS_SERVICES = "[BSM Affects Business Services (name : criticality)]"
    public static final String ACTIVITY_LOG_TIME_RECEIVED = "[Time OMi Received Event]"
    public static final String ACTIVITY_LOG_TIME_CREATED = "[Time OMi Event Created]"
    public static final String ACTIVITY_LOG_TIME_STATE_CHANGED = "[Time OMi Event State Changed]"
    public static final String ACTIVITY_LOG_ORIGINAL_DATA = "[Original Data]"
    public static final String ACTIVITY_LOG_OPERATIONAL_DATA = "[CI is operational]"

    // ****************************************************************************
    // * END Localization: Customization of text values for language localization *
    // ****************************************************************************

    public UcmdbServiceProvider ucmdbProvider = null
    public UcmdbService ucmdbService = null

    // For debugging purposes. Saves the TQLs for analysis in the UI.
    public static final boolean SaveTQLQuery = false

    // Specify ActiveProcess in request
    public static final Boolean SpecifyActiveProcess = true

    // Specify ImpactScope in request
    public static final Boolean SpecifyImpactScope = true

    // URL paths
    public static final String DRILLDOWN_ROOT_PATH =
        "/${SM_WEB_TIER_NAME}/index.do?ctx=docEngine&file=probsummary&query=number%3D"
    public static final String OMI_ROOT_DRILLDOWN_PATH = '/opr-console/opr-evt-details.jsp?eventId='
    public static final String BSM_CI_DRILLDOWN_PATH = '/topaz/dash/nodeDetails.do?cmdbId='
    public static final String ROOT_PATH = '/SM/7/rest/1.1/incident_list'
    public static final String PING_QUERY = "reference_number='IM10001'"
    public static final String INCIDENT_PATH = ROOT_PATH + '/reference_number/'

    // Custom properties file path: This is relative to the BSM install directory.
    public static final String CUSTOM_PROPERTIES_FILE = '/conf/opr/integration/sm/custom.properties'

    // *****************************************
    // * SM Incident XML tag names & constants *
    // *****************************************

    // SM supports the BDM 1.0 and 1.1 model, the groovy script deals only with the 1.1 specification
    public static final String INCIDENT_XML_NAMESPACE = 'http://www.hp.com/2009/software/data_model'
    public static final String INCIDENT_TAG = 'incident'
    public static final String TITLE_TAG = 'name'
    public static final String DESCRIPTION_TAG = 'description'
    public static final String REFERENCE_NUMBER_TAG = 'reference_number'
    public static final String INCIDENT_STATUS_TAG = 'incident_status'
    public static final String COMPLETION_CODE_TAG = 'completion_code'
    public static final String URGENCY_TAG = 'urgency'
    public static final String PRIORITY_TAG = 'priority'
    public static final String SOLUTION_TAG = 'solution'
    public static final String OWNER_TAG = 'is_owned_by'
    public static final String ASSIGNED_TAG = 'has_assigned'
    public static final String ASSIGNED_GROUP_TAG = 'has_assigned_group'
    public static final String FUNCTIONAL_GROUP_TAG = 'functional_group'
    public static final String RECORDED_BY_TAG = 'is_recorded_by'
    public static final String REQUESTED_BY_TAG = 'is_requested_by'
    public static final String PARTY_TAG = 'party'
    public static final String PERSON_TAG = 'person'
    public static final String UI_NAME_TAG = 'display_label'
    public static final String NAME_TAG = 'name'
    public static final String EXTERNAL_PROCESS_ID_TAG = 'external_process_reference'
    public static final String IMPACT_SCOPE_TAG = 'impact_scope'
    public static final String CONFIGURATION_ITEM_TAG = 'configuration_item'
    public static final String CATEGORY_TAG = 'category'
    public static final String SUB_CATEGORY_TAG = 'sub_category'
    public static final String CI_RELATIONSHIP = 'is_registered_for'
    public static final String CI_TARGET_TYPE_TAG = 'target_type'
    public static final String CI_GLOBALID_TAG = 'target_global_id'
    public static final String CI_TYPE_TAG = 'type'
    public static final String CI_ID_TAG = 'id'
    public static final String CI_NAME_TAG = 'name'
    public static final String CI_DISPLAY_LABEL_TAG = 'display_label'
    public static final String AFFECTS_RELATIONSHIP = 'affects_business_service'
    public static final String NODE_RELATIONSHIP = 'is_hosted_on'
    public static final String NODE_DNS_NAME_TAG = 'primary_dns_name'
    public static final String ACTIVITY_LOG_TAG = 'activity_log'
    public static final String ACTIVITY_LOG_DESC_TAG = 'description'
    public static final String ACTIVITY_LOG_ANNO_TAG = 'annotations'
    public static final String IS_CAUSED_BY = 'is_caused_by'
    public static final String MASTER_REFERENCE_TAG = 'master_reference_number'
    public static final String OPERATIONAL_DEVICE_TAG = 'operational_device'

    public static final String IS_CAUSED_BY_ROLE =
        'urn:x-hp:2009:software:data_model:relationship:incident:is_caused_by:incident'

    // Constant values
    public static final String IMPACT_LABEL_VALUE = 'Enterprise'
    public static final String IT_PROCESS_CATEGORY = 'incident'
    public static final String INCIDENT_TYPE = 'incident'
    public static final String IMPACT_SCOPE = 'site-dept'
    public static final String INCIDENT_XML_VERSION = '1.1'
    public static final String INCIDENT_XML_TYPE = 'urn:x-hp:2009:software:data_model:type:incident'
    public static final String CI_TARGET_TYPE = 'urn:x-hp:2009:software:data_model:type:configuration_item'
    public static final String CONFIGURATION_ITEM_ROLE = INCIDENT_XML_TYPE + ':is_registered_for:configuration_item'
    public static final String BUSINESS_SERVICE_ROLE = INCIDENT_XML_TYPE + ':affects_business_service:business_service'
    public static final String NODE_ITEM_ROLE = CI_TARGET_TYPE + ':is_hosted_on:node'
    public static final String INCIDENT_XML_RELATIONSHIPS = 'false'
    public static final String TYPE_BUSINESS_SERVICE = 'business_service'
    public static final String REL_IMPACT = 'impact_link'
    public static final String ATTR_GLOBAL_ID = 'global_id'
    public static final String ATTR_NAME = 'name'
    public static final String ATTR_LABEL = 'display_label'
    public static final String ATTR_BUSINESS_CRITICALITY = 'business_criticality'

    public static final String SET_COOKIE_HEADER = "Set-Cookie"

    // Astelit Default Variable
    public static final String ASTELIT_CATEGORY = 'Auto'
    public static final String ASTELIT_SUB_CATEGORY = 'Auto'

    // ***********************************
    // * Class instance member variables *
    // ***********************************

    // date formatter
    public final SimpleDateFormat dateFormatter = new SimpleDateFormat(ANNOTATION_DATE_FORMAT, LOCALE)
    public final Map<String, String> m_idMap1 = new HashMap<String, String>()
    public final Map<String, String> m_idMap2 = new HashMap<String, String>()
    public Boolean m_useMap1 = true

    public String m_connectedServerId = null
    public String m_connectedServerName = null
    public String m_connectedServerDisplayName = null
    public X509Certificate m_connectedServerCertificate = null
    public Integer m_timeout = null
    public RestClient m_client = null

    public String m_protocol = 'http'
    public String m_node = 'localhost'
    public Integer m_port = 13080
    public String m_home = ''
    public Integer m_oprVersion = 0

    // Maintain Cookies
    public Set<Cookie> m_smCookies = new HashSet<Cookie>()

    // Sets of properties to synchronize in each direction
    public Set m_oprSyncProperties = []
    public Set m_smSyncProperties = [REFERENCE_NUMBER_TAG]

    // custom properties
    public Properties m_properties = new Properties()

    // OPR CA synchronize to SM map
    public final Map<String, String> m_OPR2SMCustomAttribute = [:]

    // Sync 'all' boolean flags
    public final boolean syncAllOPRPropertiesToSM = SyncAllProperties || SyncOPRPropertiesToSM.contains("*")
    public final boolean syncAllOPRPropertiesToSMActivityLog = SyncAllProperties || SyncOPRPropertiesToSMActivityLog.contains("*")
    public final boolean syncAllOPRStatesToSM = SyncAllProperties || SyncOPRStatesToSM.contains("*")
    public final boolean syncAllOPRSeveritiesToSM = SyncAllProperties || SyncOPRSeveritiesToSM.contains("*")
    public final boolean syncAllOPRPrioritiesToSM = SyncAllProperties || SyncOPRPrioritiesToSM.contains("*")
    public final boolean syncAllSMPropertiesToOPR = SyncAllProperties || SyncSMPropertiesToOPR.contains("*")
    public final boolean syncAllSMStatusToOPR = SyncAllProperties || SyncSMStatusToOPR.contains("*")
    public final boolean syncAllSMUrgenciesToOPR = SyncAllProperties || SyncSMUrgenciesToOPR.contains("*")
    public final boolean syncAllSMPrioritiesToOPR = SyncAllProperties || SyncSMPrioritiesToOPR.contains("*")
    public final boolean syncCheckForClose = (!DisableCheckCloseOnUpdate &&
            (syncAllSMPropertiesToOPR || SyncSMPropertiesToOPR.contains("incident_status") &&
                    (syncAllSMStatusToOPR || SyncSMStatusToOPR.contains("closed"))))

    // Important to use "def" here, otherwise a cast exception will be thrown and the m_log is set to <null>
    public def m_log



    private void debugOprEvent(OprEvent event, Log eventDebugLog, int lineNumber) {
        eventDebugLog.debug("Getting custom attributes from event (line number " + lineNumber + ")");
        ArrayList<OprCustomAttribute> debugEventCustomAttributeList = event.getCustomAttributes().getCustomAttributes();
        for (OprCustomAttribute debugCustAttrItem : debugEventCustomAttributeList) {
            eventDebugLog.debug("Contains attribute name: " + debugCustAttrItem.getName() + " and value: " + debugCustAttrItem.getValue());
        }
    }


    private void debugForwardEvent(ForwardChangeArgs fca, org.apache.commons.logging.Log eventDebugLog, int lineNumber) {
        eventDebugLog.debug("Diving into debugForwardEvent method (line number " + lineNumber + " )");
        OprEventChange debugChanges = fca.getChanges();
        /*
         OprConfigurationItem debugCI = fca.getCI
         Don't now how to get it, yet
          */
        OprEvent debugFrowardedEvent = fca.getEvent();
        eventDebugLog.debug("External reference id is " + fca.getExternalRefId());
        OprForwardingInfo debugInfo = fca.getInfo();




        eventDebugLog.debug("Event was modified by " + debugChanges.getModifiedBy());
        Map<QName, Object> changeAttributes = debugChanges.getAnyAttribute();
        Iterator<Map.Entry<QName, Object>> changeAttributesIterator = changeAttributes.iterator();
        while (changeAttributesIterator.hasNext()) {
            Map.Entry<QName, Object> debugAttrEntry = changeAttributesIterator.next();
            eventDebugLog.debug("Attribute QName: " + debugAttrEntry.getKey().toString() + " and value: " + debugAttrEntry.getValue().toString());
        }
        eventDebugLog.debug("Head line: " + debugChanges.getHeadline());
        eventDebugLog.debug("Creation time: " + debugChanges.getTimeCreated());
        Set<OprEventPropertyChange> changeProperties = debugChanges.getChangedProperties();
        Iterator<OprEventPropertyChange> changePropertiesIterator = changeProperties.iterator();
        while (changePropertiesIterator.hasNext()) {
            OprEventPropertyChange debugPropChange = changePropertiesIterator.next();
            eventDebugLog.debug("Property change name: " + debugPropChange.getPropertyName());
            eventDebugLog.debug("Property change current value: " + debugPropChange.getCurrentValue());
            eventDebugLog.debug("Property change previous value: " + debugPropChange.getPreviousValue());
        }

        debugOprEvent(debugFrowardedEvent, eventDebugLog, 552);

        eventDebugLog.debug("Starting Debug of OprForwardingInfo");

        eventDebugLog.debug("OprForwardingInfo ServerId: " + debugInfo.getConnectedServerId());
        eventDebugLog.debug("OprForwardingInfo DisplayLabel: " + debugInfo.getDisplayLabel());
        eventDebugLog.debug("OprForwardingInfo ExternalId: " + debugInfo.getExternalId());
        eventDebugLog.debug("OprForwardingInfo ExternalUrl: " + debugInfo.getExternalUrl());
        eventDebugLog.debug("OprForwardingInfo Forwarding Type: " + debugInfo.getForwardingType());
        eventDebugLog.debug("OprForwardingInfo Name: " + debugInfo.getName());
        eventDebugLog.debug("OprForwardingInfo Queue Identifier: " + debugInfo.getQueueIdentifier());
        eventDebugLog.debug("OprForwardingInfo Rule name: " + debugInfo.getRuleName());
        eventDebugLog.debug("OprForwardingInfo State: " + debugInfo.getState());
        eventDebugLog.debug("OprForwardingInfo Time Last Changed: " + debugInfo.getTimeLastChangeSent());


    }

    private synchronized void addCustomAttribute(OprEvent event, String key, String value) {
        HashMap<String, String> singleAttribute = new HashMap<String, String>();
        singleAttribute.put(key, value);
        addCustomAttribute(event, singleAttribute);
    }


    private synchronized void addCustomAttribute(OprEvent event, Map<String, String> customAttributes) {

        String attributeName = null;
        String attributeValue = null;
        OprCustomAttribute oca = null;
        OprCustomAttributeList customAttributeList = null;

        boolean exists = false;
        ArrayList<OprCustomAttribute> attributeArrayList = new ArrayList<OprCustomAttribute>();


        if (m_log.isInfoEnabled()) {
            m_log.info("Diving into addCustomAttribute method");
        }




        try {
            if (event.getCustomAttributes() == null) {
                if (m_log.isInfoEnabled()) {
                    m_log.info("Event doesn't have custom attributes");
                }
                customAttributeList = new OprCustomAttributeList();
            } else {
                customAttributeList = event.getCustomAttributes();
                if (m_log.isInfoEnabled()) {
                    m_log.info("Discovering event with custom attributes");
                    debugOprEvent(event, m_log, 604);
                }
                Iterator<OprCustomAttribute> existedAttrIterator = customAttributeList.getCustomAttributes().iterator();
                while (existedAttrIterator.hasNext()) {
                    attributeArrayList.add(existedAttrIterator.next());
                }
            }






            Iterator attributeIterator = customAttributes.entrySet().iterator();

            while (attributeIterator.hasNext()) {
                Map.Entry pair = attributeIterator.next();
                attributeName = pair.getKey();
                attributeValue = pair.getValue();


                exists = false;
                for (OprCustomAttribute exAttr : attributeArrayList) {
                    if (exAttr.getName().equals(attributeName)) {
                        exists = true;
                    }
                }

                if (!exists) {
                    oca = new OprCustomAttribute(attributeName, attributeValue);
                    attributeArrayList.add(oca);

                    if (m_log.isDebugEnabled()) {
                        m_log.debug("Adding custom attribute " + attributeName + " with value " + attributeValue);
                    }
                } else {
                    if (m_log.isDebugEnabled()) {
                        m_log.debug("Do not adding, because we already have an attribute " + attributeName);
                    }
                }

            }

            customAttributeList.setCustomAttributes(attributeArrayList);
            event.setCustomAttributes(customAttributeList);



            if (m_log.isDebugEnabled()) {
                m_log.debug("Post adding custom attribute checking section Start")
                Iterator checkingIterator = event.getCustomAttributes().getCustomAttributes().iterator();
                while (checkingIterator.hasNext()) {
                    OprCustomAttribute currentAttribute = checkingIterator.next();
                    m_log.debug("Custom attribute " + currentAttribute.getName() + " with value " + currentAttribute.getValue());
                }
                m_log.debug("Debug of iterator");
                m_log.debug(checkingIterator);
                m_log.debug("Post adding custom attribute checking section End");

            }


        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

    }







    public synchronized void init(final InitArgs args) {

        // ensure OPR title & description are synchronized together
        if (SyncOPRPropertiesToSM.contains("title") && !SyncOPRPropertiesToSM.contains("description"))
            SyncOPRPropertiesToSM.add("description")
        if (SyncOPRPropertiesToSM.contains("description") && !SyncOPRPropertiesToSM.contains("title"))
            SyncOPRPropertiesToSM.add("title")

        // collect the names of the OPR event properties to synchronize to SM
        m_oprSyncProperties.addAll(SyncOPRPropertiesToSM)
        m_oprSyncProperties.addAll(SyncOPRPropertiesToSMActivityLog)

        // ensure SM name & description are synchronized together
        if (SyncSMPropertiesToOPR.contains("name") && !SyncSMPropertiesToOPR.contains("description"))
            SyncSMPropertiesToOPR.add("description")
        if (SyncSMPropertiesToOPR.contains("description") && !SyncSMPropertiesToOPR.contains("name"))
            SyncSMPropertiesToOPR.add("name")

        // collect the names of the SM incident properties to synchronize to OMi
        m_smSyncProperties.addAll(SyncSMPropertiesToOPR)
        m_smSyncProperties.addAll(MapSM2OPRCustomAttribute.keySet())

        //TODO start to use variables instead of direct mapping

        // shift all CA names to lowercase
        MapOPR2SMCustomAttribute.entrySet().each() { Map.Entry<String, String> entry ->
            final String name = entry.key.toLowerCase(LOCALE)
            final String value = entry.value
            m_OPR2SMCustomAttribute.put(name, value)
        }

        m_log = args.logger

        m_connectedServerId = args.connectedServerId
        m_connectedServerName = args.connectedServerName
        m_connectedServerDisplayName = args.connectedServerDisplayName
        m_connectedServerCertificate = args.connectedServerCertificate
        m_timeout = args.maxTimeout == null ? 60 : args.maxTimeout
        m_protocol = args.nodeSsl ? 'https' : 'http'
        m_node = args.node
        m_port = (args.port == null || args.port < 1) ? (args.nodeSsl ? 13443 : 13080) : args.port
        m_home = args.installDir

        m_client = WinkClientSupport.getRestClient(m_timeout, m_connectedServerCertificate)

        // determine the OPR version that is running
        String versionString = Version.getProperty(Version.COMPONENT_VERSION)
        String[] version = versionString.split('\\.')
        m_oprVersion = (Integer.valueOf(version[0]) * 100) + Integer.valueOf(version[1])

        if (m_oprVersion < 921)
            initUcmdbConnection()

        // load any custom properties
        final File customPropertiesFile = new File(m_home, CUSTOM_PROPERTIES_FILE)
        if (customPropertiesFile.canRead())
            m_properties.load(new FileReader(customPropertiesFile))

        if (m_log.infoEnabled) {
            StringBuilder initMsg = new StringBuilder()
            initMsg.append("Service Manager Adapter initialization")
            initMsg.append("\n\tConnected Server ID: ${m_connectedServerId}")
            initMsg.append("\n\tConnected Server Name: ${m_connectedServerName}")
            initMsg.append("\n\tConnected Server Display Name: ${m_connectedServerDisplayName}")
            initMsg.append("\n\tMaximum Timeout in milliseconds: ${m_timeout}")
            initMsg.append("\n\tProtocol: ${m_protocol}")
            initMsg.append("\n\tNode: ${m_node}")
            initMsg.append("\n\tPort: ${m_port}")
            initMsg.append("\nService Manager Adapter initialized")
            m_log.info(initMsg.toString())
        }

        m_log.info("Service Manager Adapter initalized. INSTALL_DIR=${args.installDir}, OMi version=${versionString}")
    }

    private synchronized void initUcmdbConnection() {
        if (!RTSM_HOSTNAME || !RTSM_USERNAME || !RTSM_PASSWORD)
            return

        try {
            ucmdbProvider = UcmdbServiceFactory.getServiceProvider(RTSM_HOSTNAME, RTSM_PORT)
            ucmdbService = ucmdbProvider.connect(ucmdbProvider.createCredentials(RTSM_USERNAME, RTSM_PASSWORD),
                    ucmdbProvider.createClientContext(this.class.getName()))
        }
        catch (Exception e) {
            // try to re-open the connection
            String details = e.class.getCanonicalName() + (e.getMessage() ? ": ${e.getMessage()}." : "")
            m_log.error("Attempt to connect to RTSM on server ${RTSM_HOSTNAME} failed. Error details: ${details}\n", e)
            ucmdbProvider = null
            ucmdbService = null
        }
    }

    private synchronized TopologyQueryService getQueryService() {
        if (!RTSM_HOSTNAME || !RTSM_USERNAME || !RTSM_PASSWORD)
            return null

        // try to connect twice
        for (int i = 0; i < 2; i++) {
            try {
                if (ucmdbProvider == null)
                    initUcmdbConnection()
                if (ucmdbProvider == null || ucmdbService == null)
                    return null

                // Get the query service
                return ucmdbService.getTopologyQueryService()
            }
            catch (Exception e) {
                // try to re-open the connection
                String details = e.class.getCanonicalName() + (e.getMessage() ? ": ${e.getMessage()}." : "")
                if (i == 1)
                    m_log.error("Attempt to connect to RTSM on server ${RTSM_HOSTNAME} failed. Error details: ${details}\n", e)
                ucmdbProvider = null
                ucmdbService = null
            }
        }
        return null
    }

    public Boolean ping(final PingArgs args) {
        if (m_log.isDebugEnabled())
            m_log.debug("Try to ping server: ${args.connectedServerName}")

        // get the resource client connection to make the post
        final String protocol = args.nodeSsl ? 'https' : 'http'
        final Integer port = (args.port == null || args.port < 1) ? (args.nodeSsl ? 13443 : 13080) : args.port
        final Resource resource = createRequest(protocol, args.node, port, ROOT_PATH, args.credentials, m_smCookies)
        resource.queryParam("query", PING_QUERY)
        try {
            final ClientResponse response = resource.get()
            getCookies(response, m_smCookies)
            checkPingResponse(response)
        }
        catch (ClientWebException e) {
            final String errMsg = "Node: ${m_node}, Port: ${m_port}, ClientWebException encountered: " +
                    "(${e.getResponse()?.getStatusCode()}) ${e.getResponse()?.getMessage()}"
            if (m_log.isDebugEnabled())
                m_log.error(errMsg, e)
            else
                m_log.error(errMsg)
            throw e
        }
        args.outputDetail = "Server successfully reached."
        return true
    }

    public synchronized void destroy() {
        m_log.debug("Service Manager Adapter destroy")
    }

    /**
     * Forwards the given event to the external process.
     *
     * @param args contains the event to forward and any other parameters
     *        needed to forward the event.
     * @return an OprEvent representing the external event. The id field must be set
     *         with the id of the external event. If there is a drilldown URL path it should be
     *         set in the drilldown UrlPath field. All other attributes are currently ignored.
     *         If the id is set to null or null is returned it is assumed the external
     *         process cannot be reached at this time. A retry will be made later.
     */
    public Boolean forwardEvent(ForwardEventArgs args) {
        if ((m_oprVersion < 920) && (args.event.node != null))
            getNodeProperties(args)

        String extId = sendEvent(args.event, args.info, null, args.credentials, args)
        if (extId != null) {
            args.externalRefId = extId
            args.drilldownUrlPath = "${DRILLDOWN_ROOT_PATH}%22${extId}%22"
            addIdToMap(args.event.id, extId)

            // now update the cause/symptom links
            String causeExternalRefId = getCauseExternalId(args, args.event)
            if (args.event.cause != null && causeExternalRefId)
                linkCauseIncident(extId, args.event.cause, causeExternalRefId, args.credentials)
            if (args.event.symptoms?.eventReferences != null && !args.event.symptoms.eventReferences.isEmpty()) {
                OprEventReference cause = new OprEventReference(args.event.id)
                cause.title = args.event.title
                args.event.symptoms.eventReferences.each() { OprSymptomReference symptomRef ->
                    String symptomExternalRefId = getSymptomExternalId(args, symptomRef)
                    if (symptomExternalRefId)
                        linkCauseIncident(symptomExternalRefId, cause, extId, args.credentials)
                }
            }
            return true
        }
        return false
    }

    private void getNodeProperties(ForwardEventArgs args) {
        // Gets all the node properties, not just the key properties
        OprNodeReference nodeRef = args.event.node
        OprConfigurationItem node = args.getCi(nodeRef.targetId)
        if ((node != null) && (node.any != null) && (!node.any.empty))
            nodeRef.node.any = node.any
    }

    // args if of type BulkForwardEventArgs. Using "def" for backwards compatibility with 9.1x
    public Boolean forwardEvents(def args) {
        m_log.debug("***Begin Bulk Forward***")
        Boolean result = false
        args.events?.eventList?.each() { OprEvent event ->
            OprForwardingInfo info = event.getForwardingInfo(m_connectedServerId)
            String extId = sendEvent(event, info, null, args.credentials, args)
            if (extId) {
                args.setForwardSuccess(event.id, extId, "${DRILLDOWN_ROOT_PATH}%22${extId}%22")
                addIdToMap(event.id, extId)
                result = true

                // now update the cause/symptom links
                String causeExternalRefId = getCauseExternalId(args, event)
                if (event.cause != null && causeExternalRefId)
                    linkCauseIncident(extId, event.cause, causeExternalRefId, args.credentials)
                if (event.symptoms?.eventReferences != null && !event.symptoms.eventReferences.isEmpty()) {
                    OprEventReference cause = new OprEventReference(event.id)
                    cause.title = event.title
                    event.symptoms.eventReferences.each() { OprSymptomReference symptomRef ->
                        String symptomExternalRefId = getSymptomExternalId(args, symptomRef)
                        if (symptomExternalRefId)
                            linkCauseIncident(symptomExternalRefId, cause, extId, args.credentials)
                    }
                }
            } else
                return
        }
        m_log.debug("***End Bulk Forward***")
        return result
    }

    private synchronized void addIdToMap(String id, String externalId) {
        // keep the last 500 to 999 entries in a cache
        if (m_useMap1) {
            m_idMap1.put(id, externalId)
            if (m_idMap1.size() > 400) {
                m_idMap2.clear()
                m_useMap1 = false
            }
        } else {
            m_idMap2.put(id, externalId)
            if (m_idMap2.size() > 400) {
                m_idMap1.clear()
                m_useMap1 = true
            }
        }
    }

    private synchronized String getCauseExternalId(def args, OprEvent event) {
        // check the id maps first
        final String causeId = event.cause?.targetId
        if (!causeId)
            return null

        String causeExternalRefId = m_idMap1.get(causeId)
        if (causeExternalRefId)
            return causeExternalRefId

        causeExternalRefId = m_idMap2.get(causeId)
        if (causeExternalRefId)
            return causeExternalRefId

        // check the DB
        OprEvent cause = args.getEvent(causeId, false)
        if (cause) {
            OprForwardingInfo causeInfo = cause.getForwardingInfo(m_connectedServerId)
            causeExternalRefId = causeInfo?.externalId
        }

        return causeExternalRefId
    }

    private synchronized String getSymptomExternalId(def args, OprSymptomReference symptomRef) {
        // check the id maps first
        final String symptomId = symptomRef?.targetId
        if (!symptomId)
            return null

        String symptomExternalRefId = m_idMap1.get(symptomId)
        if (symptomExternalRefId)
            return symptomExternalRefId

        symptomExternalRefId = m_idMap2.get(symptomId)
        if (symptomExternalRefId)
            return symptomExternalRefId

        // check the DB
        OprEvent symptom = args.getEvent(symptomId, false)
        if (symptom) {
            OprForwardingInfo symptomInfo = symptom.getForwardingInfo(m_connectedServerId)
            symptomExternalRefId = symptomInfo?.externalId
        }

        return symptomExternalRefId
    }

    private void linkCauseIncident(def externalRefId, def causeRef, def causeExternalRefId, def credentials) {
        final OprEvent event = new OprEvent()
        event.cause = causeRef

        // convert the OprEvent to an SM incident
        final String payload = toExternalEvent(event, null, causeExternalRefId, null)

        if (m_log.isDebugEnabled())
            m_log.debug("Forward Change Request to Node: ${m_node}, Port: ${m_port}, XML in request:\n${payload}")
        try {
            final String path = INCIDENT_PATH + externalRefId
            final Resource resource = createRequest(m_protocol, m_node, m_port, path, credentials, m_smCookies)
            final ClientResponse clientResponse = resource.put(payload)
            getCookies(clientResponse, m_smCookies)
            checkResponse(clientResponse)
            final String updateIncident = clientResponse.getEntity(String.class)

            if (updateIncident != null) {
                if (m_log.isDebugEnabled())
                    m_log.debug("Service Manager Incident updated: ${updateIncident}")
            } else
                m_log.error("Update of Incident cause link failed.")
        }
        catch (ClientWebException e) {
            final String errMsg = "Node: ${m_node}, Port: ${m_port}, ClientWebException encountered: " +
                    "(${e.getResponse()?.getStatusCode()}) ${e.getResponse()?.getMessage()}"
            if (m_log.isDebugEnabled())
                m_log.error(errMsg, e)
            else
                m_log.error(errMsg)
            m_log.error("Update of Incident cause link failed.", e)
        }
    }

    private String sendEvent(OprEvent event,
                             OprForwardingInfo info,
                             String causeExternalRefId,
                             PasswordAuthentication credentials,
                             def args) {
        if (m_log.isDebugEnabled())
            m_log.debug("forwardEvent() for event: ${event.id}")

        // create the external ID for SM
        final String externalRefId
        final String forwardingType = info.forwardingType.toLowerCase()
        if (forwardingType.equals(OprForwardingTypeEnum.synchronize_and_transfer_control.toString())) {
            externalRefId =
                "urn:x-hp:2009:opr:${m_connectedServerId}:incident|escalated|provider:${event.id}"
        } else {
            externalRefId =
                "urn:x-hp:2009:opr:${m_connectedServerId}:incident|informational|requestor:${event.id}"
        }
        OprIntegerPropertyChange duplicateChange = null
        if (event.duplicateCount > 0) {
            duplicateChange = new OprIntegerPropertyChange()
            duplicateChange.previousValue = null
            duplicateChange.currentValue = event.duplicateCount
        }
        final String incident = toExternalEvent(event, externalRefId, causeExternalRefId, duplicateChange)

        // get the resource client connection to make the post
        final Resource resource = createRequest(m_protocol, m_node, m_port, ROOT_PATH, credentials, m_smCookies)
        final String response
        if (m_log.isDebugEnabled())
            m_log.debug("Forward Request to Node: ${m_node}, Port: ${m_port}, XML in request:\n${incident}")
        try {
            final ClientResponse clientResponse = resource.post(incident)
            getCookies(clientResponse, m_smCookies)
            checkResponse(clientResponse)
            response = clientResponse.getEntity(String.class)
            if (response) {
                if (m_log.isDebugEnabled())
                    m_log.debug("Service Manager Incident created:\n${response}")
            } else
                m_log.warn("Null response returned by server.")
        }
        catch (ClientWebException e) {
            final String errMsg = "Node: ${m_node}, Port: ${m_port}, ClientWebException encountered: " +
                    "(${e.getResponse()?.getStatusCode()}) ${e.getResponse()?.getMessage()}"
            if (m_log.isDebugEnabled())
                m_log.error(errMsg, e)
            else
                m_log.error(errMsg)
            throw e
        }

        // Set the return values
        if ((response == null) || (response.length() == 0))
            return null
        else {
            final GPathResult xmlResult = new XmlSlurper().parseText(response)

            // check if this is an incident or a ATOM syndication entry
            final GPathResult respIncident = (xmlResult.name().equals('entry')) ?
                xmlResult.getProperty('content').getProperty(INCIDENT_TAG) : xmlResult

            if (respIncident.name().equals(INCIDENT_TAG)) {
                // set the ID and drilldown URL path
                if ((m_oprVersion > 920) && !MapSM2OPRCustomAttribute.empty)
                    updateCustomAttributes(event.id, respIncident, args)
                return respIncident.getProperty(REFERENCE_NUMBER_TAG).text()
            }
            return null
        }
    }

    void updateCustomAttributes(String eventId, GPathResult respIncident, def args) {
        if ((m_oprVersion > 920) && !MapSM2OPRCustomAttribute.empty) {


            final OprEvent update = new OprEvent()
            update.id = eventId
            update.customAttributes = new OprCustomAttributeList()
            MapSM2OPRCustomAttribute.each() {
                String smPropertyName, String caName ->
                    if (m_log.isDebugEnabled()) {
                        m_log.debug(respIncident.getProperty(smPropertyName))
                    }
                    String caValue = respIncident.getProperty(smPropertyName)?.text()
                    update.customAttributes.customAttributes.add(new OprCustomAttribute(caName, caValue))
                    /*
                    Add a custom debuger
                    to watch incident update
                     */
                    if (m_log.isDebugEnabled()) {
                        m_log.debug("Processing updateCustomAttributes method");
                        m_log.debug("Adding " + caName + " with value " + caValue);
                    }
            }
            // Add the CAs to the event
            try {
                if (!update.customAttributes.customAttributes.empty)
                    args.submitChanges(update)
            } catch (Throwable t) {
                // Just log this error. Don't want the forward to about because of an error here.
                m_log.error("Error encountered while attempting to update event with custom attributes.", t)
            }
        }
    }

    /**
     * Send the event updates.
     *
     * @param args contains the event changes to forward and any other parameters
     *        needed to forward the changes.
     * @return true if the changes were successfully sent, otherwise false
     *         If false is returned or an exception is thrown, a retry will be made later.
     */
    public Boolean forwardChange(ForwardChangeArgs args) {
        /*
        Debug injection
         */
        debugForwardEvent(args, m_log, 1095);

        return sendChange(args, args.changes, args.externalRefId, args.credentials)
    }

    //TODO debug this method

    // args if of type BulkForwardChangeArgs. Using "def" for backwards compatibility with 9.1x
    public Boolean forwardChanges(def args) {
        m_log.debug("***Begin Bulk Forward Change***")
        Boolean result = false

        args.changes?.eventChanges?.each() { OprEventChange change ->
            result = sendChange(args, change, change.eventRef.targetId, args.credentials)
            if (!result)
                return
            args.setForwardSuccess(change.id)
        }
        m_log.debug("***End Bulk Forward Change***")
        return result
    }
    //TODO debug this method
    private Boolean sendChange(def args, OprEventChange changes, String externalRefId, PasswordAuthentication credentials) {
        Boolean anyAttributeWasChanged = false
        if (m_log.isDebugEnabled())
            m_log.debug("forwardChange() for incident <${externalRefId}>")

        // if no changes to process then just return
        if ((changes == null) || (changes.changedProperties == null))
            return true

        // create an OprEvent with the changes
        final OprEvent event = new OprEvent()
        final String eventId = (changes.eventRef.targetGlobalId) ?
            changes.eventRef.targetGlobalId : changes.eventRef.targetId
        event.id = eventId

        OprIntegerPropertyChange duplicateChange = null

        String causeExternalRefId = null
        changes.changedProperties?.each() { OprEventPropertyChange propChange ->
            final OprEventPropertyNameEnum name = OprEventPropertyNameEnum.valueOf(propChange.propertyName)

            // check if this OPR event property should be synchronized to the SM incident
            String propertyName = propChange.propertyName.toLowerCase()
            if (syncAllOPRPropertiesToSM || m_oprSyncProperties.contains(propertyName)) {
                // SM is only interested in the following changes
                def changedValue = propChange.currentValue
                switch (name) {
                    case OprEventPropertyNameEnum.title:
                        // ensure title & description are synchronized together
                        if (!event.title) {
                            final OprEvent currentEvent = args.getEvent(eventId, false)
                            event.title = currentEvent.title
                            event.description = currentEvent.description
                            anyAttributeWasChanged = true
                        }
                        break
                    case OprEventPropertyNameEnum.description:
                        // ensure title & description are synchronized together
                        if (!event.title) {
                            final OprEvent currentEvent = args.getEvent(eventId, false)
                            event.title = currentEvent.title
                            event.description = currentEvent.description
                            anyAttributeWasChanged = true
                        }
                        break
                    case OprEventPropertyNameEnum.severity:
                        if (SyncOPRPropertiesToSMActivityLog.contains("severity")
                                || SyncOPRSeveritiesToSM.contains(changedValue.toString())) {
                            event.severity = (changedValue.toString())
                            anyAttributeWasChanged = true
                        }
                        break
                    case OprEventPropertyNameEnum.priority:
                        if (SyncOPRPropertiesToSMActivityLog.contains("priority")
                                || SyncOPRPrioritiesToSM.contains(changedValue.toString())) {
                            event.priority = (changedValue.toString())
                            anyAttributeWasChanged = true
                        }
                        break
                    case OprEventPropertyNameEnum.solution:
                        event.solution = ((String) changedValue)
                        anyAttributeWasChanged = true
                        break
                    case OprEventPropertyNameEnum.assigned_user:
                        OprUserPropertyChange userPropChange = (OprUserPropertyChange) propChange
                        OprUser assignedUser = new OprUser()
                        assignedUser.userName = userPropChange.currentUserDisplayLabel
                        assignedUser.loginName = userPropChange.currentUserName
                        assignedUser.id = (Integer) changedValue
                        event.assignedUser = assignedUser
                        anyAttributeWasChanged = true
                        break
                    case OprEventPropertyNameEnum.assigned_group:
                        OprGroupPropertyChange groupPropertyChange = (OprGroupPropertyChange) propChange
                        OprGroup assignedGroup = new OprGroup()
                        assignedGroup.name = groupPropertyChange.currentGroupName
                        assignedGroup.id = (Integer) changedValue
                        event.assignedGroup = assignedGroup
                        anyAttributeWasChanged = true
                        break
                    case OprEventPropertyNameEnum.state:
                        if (SyncOPRPropertiesToSMActivityLog.contains("state")
                                || SyncOPRStatesToSM.contains(changedValue.toString())) {
                            event.state = changedValue.toString()
                            event.timeStateChanged = changes.timeChanged
                            anyAttributeWasChanged = true
                        }
                        break
                    case OprEventPropertyNameEnum.cause:
                        if (changedValue) {
                            String causeId = changedValue.toString()
                            OprEventReference cause = new OprEventReference(causeId)
                            event.cause = cause
                            OprEvent causeEvent = args.getEvent(cause.targetId, false)
                            if (causeEvent) {
                                cause.title = causeEvent.title
                                OprForwardingInfo causeInfo = causeEvent.getForwardingInfo(m_connectedServerId)
                                causeExternalRefId = causeInfo?.externalId
                            }
                            anyAttributeWasChanged = true
                        } else
                            event.cause = null
                        break
                    case OprEventPropertyNameEnum.symptom:
                        if (((OprSymptomPropertyChange) propChange).changeOperation.equals("insert")) {
                            OprSymptomReference symptom = new OprSymptomReference(changedValue.toString())
                            symptom.title = ((OprSymptomPropertyChange) propChange).symptomTitle
                            if (event.symptoms == null)
                                event.symptoms = new OprSymptomList()
                            event.symptoms.eventReferences.add(symptom)
                            anyAttributeWasChanged = true
                        }
                        break
                    case OprEventPropertyNameEnum.control_transferred_to:
                        final OprControlTransferInfo transferChange = ((OprControlTransferInfo) changedValue)
                        // Only send "transferred" updates for other servers
                        if (!m_node.equalsIgnoreCase(transferChange.dnsName) &&
                                OprControlTransferStateEnum.transferred.name().equals(transferChange.state)) {
                            event.controlTransferredTo = transferChange
                            anyAttributeWasChanged = true
                        }
                        break
                    case OprEventPropertyNameEnum.annotation:
                        final OprAnnotationPropertyChange annotationChange = (OprAnnotationPropertyChange) propChange
                        if (annotationChange.changeOperation.equals(OprPropertyChangeOperationEnum.insert.name()) ||
                                annotationChange.changeOperation.equals(OprPropertyChangeOperationEnum.update.name())) {
                            final OprAnnotation annotation = new OprAnnotation()
                            annotation.timeCreated = changes.timeChanged
                            annotation.author = annotationChange.author
                            annotation.text = ((String) changedValue)
                            if (event.annotations == null)
                                event.annotations = new OprAnnotationList()
                            event.annotations.annotations.add(annotation)
                            anyAttributeWasChanged = true
                        }
                        break
                    case OprEventPropertyNameEnum.duplicate_count:
                        Integer newCount = (Integer) propChange.currentValue
                        if ((newCount) && (newCount < MAX_DUPLICATE_COUNT_UPDATES)) {
                            duplicateChange = (OprIntegerPropertyChange) propChange
                            event.duplicateCount = newCount
                            anyAttributeWasChanged = true
                        }
                        break
                    default:
                        break
                }
            }

            //TODO Debug this method
            // handle CAs separately
            if (m_log.isDebugEnabled()) {
                m_log.debug("We are inside sendChange method");
            }
            if (OprEventPropertyNameEnum.custom_attribute.equals(name)) {
                if (m_log.isDebugEnabled()) {
                    m_log.debug("Trying to handle custom attributes");
                }
                final OprCustomAttributePropertyChange customAttributeChange = (OprCustomAttributePropertyChange) propChange
                final String caName = customAttributeChange.key
                if (caName && m_OPR2SMCustomAttribute.containsKey(caName.toLowerCase(LOCALE))
                        && (customAttributeChange.changeOperation.equals(OprPropertyChangeOperationEnum.insert.name()) ||
                        customAttributeChange.changeOperation.equals(OprPropertyChangeOperationEnum.update.name()))) {
                    final OprCustomAttribute customAttribute = new OprCustomAttribute()
                    customAttribute.name = caName
                    customAttribute.value = ((String) propChange.currentValue)
                    if (event.customAttributes == null)
                        event.customAttributes = new OprCustomAttributeList()
                    event.customAttributes.customAttributes.add(customAttribute)
                    if (m_log.isDebugEnabled()) {
                        m_log.debug("We passed custom attribute conditions \nAnd custom Attribute name: " + customAttribute.getName() + " with value " + customAttribute.getValue() + " has been inserted");
                    }
                    anyAttributeWasChanged = true
                }
            }
        }

        Boolean result = true
        if (anyAttributeWasChanged) {
            // convert the OprEvent to an SM incident
            final String payload = toExternalEvent(event, null, causeExternalRefId, duplicateChange)
            if (m_log.isDebugEnabled())
                m_log.debug("Forward Change Request to Node: ${m_node}, Port: ${m_port}, XML in request:\n${payload}")
            try {
                final String path = INCIDENT_PATH + externalRefId
                final Resource resource = createRequest(m_protocol, m_node, m_port, path, credentials, m_smCookies)
                final ClientResponse clientResponse = resource.put(payload)
                getCookies(clientResponse, m_smCookies)
                checkResponse(clientResponse)
                final String updateIncident = clientResponse.getEntity(String.class)

                if (updateIncident != null) {
                    if (m_log.isDebugEnabled())
                        m_log.debug("Service Manager Incident updated:\n${updateIncident}")
                    if ((m_oprVersion > 920) && syncCheckForClose && !OprState.closed.name().equals(event.state)) {
                        // check if the incident is closed now by checking the response object
                        final GPathResult xmlResult = new XmlSlurper().parseText(updateIncident)

                        // check if this is an incident or a ATOM syndication entry
                        final GPathResult incident = (xmlResult.name().equals('entry')) ?
                            xmlResult.getProperty('content')?.getProperty(INCIDENT_TAG) : xmlResult

                        if (incident != null && incident.name().equals(INCIDENT_TAG)) {
                            String status = incident.getProperty(INCIDENT_STATUS_TAG)?.text()
                            if ("closed".equals(status)) {
                                // Incident is closed. Check if current event is also closed.
                                OprEvent current = args.getEvent(eventId, false)
                                if (current && !OprState.closed.name().equals(current.state)) {
                                    // Event is still open. Close it if forwarding type allows back synchronization.
                                    OprForwardingInfo info = current.getForwardingInfo(m_connectedServerId)
                                    if (info &&
                                            (OprForwardingTypeEnum.synchronize.name().equals(info.forwardingType)
                                                    || OprForwardingTypeEnum.synchronize_and_transfer_control.name().equals(info.forwardingType))) {
                                        m_log.debug("Closing event ${event.id} because corresponding SM incident ${externalRefId} is closed.")
                                        current.state = OprState.closed.name()
                                        args.submitChanges(current)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    m_log.warn("Null response returned by server.")
                    result = false
                }
            }
            catch (ClientWebException e) {
                if (syncCheckForClose && isIncidentClosed(externalRefId, args.credentials)) {
                    m_log.info("Ignoring update for SM incident ${externalRefId} because incident is already closed.")
                    if (m_oprVersion > 920) {
                        // Incident is already closed, so just close the event, if not already closed, otherwise ignore the update
                        OprEvent current = args.getEvent(eventId, false)
                        if (current && !OprState.closed.name().equals(current.state)) {
                            // Event is still open. Close it if forwarding type allows back synchronization.
                            OprForwardingInfo info = current.getForwardingInfo(m_connectedServerId)
                            if (info &&
                                    (OprForwardingTypeEnum.synchronize.name().equals(info.forwardingType)
                                            || OprForwardingTypeEnum.synchronize_and_transfer_control.name().equals(info.forwardingType))) {
                                m_log.debug("Closing event ${eventId} because corresponding SM incident ${externalRefId} is closed.")
                                current.state = OprState.closed.name()
                                args.submitChanges(current)
                            }
                        }
                    }
                    return true
                } else {
                    // Handle the error
                    final String errMsg = "Node: ${m_node}, Port: ${m_port}, ClientWebException encountered: " +
                            "(${e.getResponse()?.getStatusCode()}) ${e.getResponse()?.getMessage()}"
                    if (m_log.isDebugEnabled())
                        m_log.error(errMsg, e)
                    else
                        m_log.error(errMsg)
                    throw e
                }
            }
        }

        return result
    }

    private boolean isIncidentClosed(String externalRefId, PasswordAuthentication credentials) {
        final String path = "${INCIDENT_PATH}${externalRefId}"

        try {
            final Resource resource = createRequest(m_protocol, m_node, m_port, path, credentials, m_smCookies)
            resource.accept(MediaType.APPLICATION_ATOM_XML)
            final ClientResponse clientResponse = resource.get()
            getCookies(clientResponse, m_smCookies)
            checkResponse(clientResponse)
            final String response = clientResponse.getEntity(String.class)

            if ((response != null) && (response.length() >= 0)) {
                if (m_log.isDebugEnabled())
                    m_log.debug("Service Manager Incident successfully retrieved:\n ${response}")
                final GPathResult xmlResult = new XmlSlurper().parseText(response)

                // check if this is an incident or a ATOM syndication entry
                final GPathResult incident = (xmlResult.name().equals('entry')) ?
                    xmlResult.getProperty('content')?.getProperty(INCIDENT_TAG) : xmlResult

                if (incident != null && incident.name().equals(INCIDENT_TAG)) {
                    String status = incident.getProperty(INCIDENT_STATUS_TAG)?.text()
                    return ("closed".equals(status))
                }
            }
        }
        catch (ClientWebException e) {
            // If not found, then assume it is closed.
            if (e.getResponse()?.getStatusCode() == 404) {
                return true
            }
            final String errMsg = "Node: ${m_node}, Port: ${m_port}, ClientWebException encountered: " +
                    "(${e.getResponse()?.getStatusCode()}) ${e.getResponse()?.getMessage()}"
            if (m_log.isDebugEnabled())
                m_log.error(errMsg, e)
            else
                m_log.error(errMsg)
        }
        return false
    }

    /**
     * Get the incident data from SM represented as an OprEvent
     *
     * @param args contains the external event ID.
     * @return the OPR Event. If no object exists a null is returned.
     */
    public Boolean getExternalEvent(GetExternalEventArgs args) {
        final String[] idParts = args.getExternalRefId().split(':')
        final String path = "${INCIDENT_PATH}${idParts[idParts.length - 1]}"

        if (m_log.isDebugEnabled())
            m_log.debug("getExternalEvent() for event: ${args.getExternalRefId()}")

        try {
            final Resource resource = createRequest(m_protocol, m_node, m_port, path, args.credentials, m_smCookies)
            resource.accept(MediaType.APPLICATION_ATOM_XML)
            final ClientResponse clientResponse = resource.get()
            getCookies(clientResponse, m_smCookies)
            checkResponse(clientResponse)
            final String response = clientResponse.getEntity(String.class)

            if ((response != null) && (response.length() >= 0)) {
                if (m_log.isDebugEnabled())
                    m_log.debug("Service Manager Incident successfully retrieved:\n ${response}")
                final GPathResult xmlResult = new XmlSlurper().parseText(response)

                // check if this is an incident or a ATOM syndication entry
                final GPathResult incident = (xmlResult.name().equals('entry')) ?
                    xmlResult.getProperty('content')?.getProperty(INCIDENT_TAG) : xmlResult

                if (incident != null && incident.name().equals(INCIDENT_TAG)) {
                    String title = incident.getProperty(TITLE_TAG)?.text()
                    String description = incident.getProperty(DESCRIPTION_TAG)?.text()
                    String status = incident.getProperty(INCIDENT_STATUS_TAG)?.text()
                    String urgency = incident.getProperty(URGENCY_TAG)?.text()
                    String priority = incident.getProperty(PRIORITY_TAG)?.text()
                    String assignedUser = incident.getProperty(ASSIGNED_TAG)?.getProperty(PARTY_TAG)?.
                            getProperty(UI_NAME_TAG)?.text()
                    String assignedGroup = incident.getProperty(ASSIGNED_GROUP_TAG)?.getProperty(FUNCTIONAL_GROUP_TAG)?.
                            getProperty(UI_NAME_TAG)?.text()

                    if (title == null)
                        title = ""
                    if (description == null)
                        description = ""
                    if (status == null)
                        status = ""
                    if (urgency == null)
                        urgency = ""
                    if (priority == null)
                        priority = ""
                    if (assignedUser == null)
                        assignedUser = ""
                    if (assignedGroup == null)
                        assignedGroup = ""

                    if (m_log.isDebugEnabled())
                        m_log.debug("SM Incident:\n\tTitle: ${title}\n\tDescription: ${description}\n"
                                + "\tState: ${status}\n\tUrgency: ${urgency}\n\tPriority: ${priority}\n"
                                + "\tAssigned User: ${assignedUser}\n\tAssigned Group: ${assignedGroup}")

                    args.title = title
                    args.description = description
                    args.state = status
                    args.severity = SMUrgency[urgency] == null ? urgency : SMUrgency[urgency]
                    args.priority = SMPriority[priority] == null ? priority : SMPriority[priority]
                    args.assignedUser = assignedUser
                    args.assignedGroup = assignedGroup

                    return true
                }
                return false
            } else {
                return false
            }
        }
        catch (ClientWebException e) {
            final String errMsg = "Node: ${m_node}, Port: ${m_port}, ClientWebException encountered: " +
                    "(${e.getResponse()?.getStatusCode()}) ${e.getResponse()?.getMessage()}"
            if (m_log.isDebugEnabled())
                m_log.error(errMsg, e)
            else
                m_log.error(errMsg)
            throw e
        }
    }

    /**
     * Convert the external incident object into an OprEvent object.
     * Used by Event Synchronization PUT WS when event update is received.
     *
     * @param externalEvent object to convert
     * @return the converted OprEvent object
     */
    public Boolean receiveChange(final ReceiveChangeArgs args) {
        // get a copy of the original event, note SM is not able to send the changed attributes
        final OprEvent event = args.event
        final String externalEventChange = args.externalEventChange
        final boolean isDebugLogEnabled = m_log.isDebugEnabled()

        if (isDebugLogEnabled)
            m_log.debug("receiveChange() for external event: ${args.externalRefId}\n${externalEventChange}")

        // ignore the request if forwarding type is notify or notify_and_update
        OprForwardingInfo info = args.info
        if (info.forwardingType.equals(OprForwardingTypeEnum.notify.name()) ||
                info.forwardingType.equals(OprForwardingTypeEnum.notify_and_update.name()))
            return true

        if ((externalEventChange == null) || (externalEventChange.length() == 0) || (event == null))
            return false

        final GPathResult xmlResult = new XmlSlurper().parseText(externalEventChange)

        // check if this is an incident or a ATOM syndication entry
        final GPathResult incident = (xmlResult.name().equals('entry')) ?
            xmlResult.getProperty('content').getProperty(INCIDENT_TAG) : xmlResult

        if (incident.name().equals(INCIDENT_TAG)) {
            incident.childNodes().each { child ->

                String propertyName = child.name
                String propertyValue = child.text()

                if (syncAllSMPropertiesToOPR
                        || m_smSyncProperties.contains(propertyName)
                        || REFERENCE_NUMBER_TAG.equals(propertyName)) {
                    // check if the property has changed
                    switch (propertyName) {
                        case REFERENCE_NUMBER_TAG:
                            final String extId = propertyValue
                            if (isDebugLogEnabled)
                                m_log.debug("Processing update for SM incident: ${extId}")
                            break

                        case TITLE_TAG:
                            final String title = propertyValue
                            if (!event.title.equals(title))
                                args.title = title
                            break

                        case DESCRIPTION_TAG:
                            final String description = propertyValue
                            if (!event.description.equals(description))
                                args.description = description
                            break

                        case INCIDENT_STATUS_TAG:
                            final String bdmIncidentState = propertyValue
                            if (bdmIncidentState) {
                                if (syncAllSMStatusToOPR || SyncSMStatusToOPR.contains(bdmIncidentState)) {
                                    final String oprEventState = MapSM2OPRState[bdmIncidentState]

                                    if (oprEventState) {
                                        if (isDebugLogEnabled)
                                            m_log.debug("OPR event state change to: <${oprEventState}>")
                                        args.state = OprState.valueOf(oprEventState)
                                    }
                                }
                            }
                            // SM want to take the ownership of the event from BSM
                            // this is indicated by changing the incident status from open to something else other than open or closed
                            boolean isTransferred = event.isControlTransferred() == null ? false : event.isControlTransferred()
                            if (!isTransferred && !bdmIncidentState.equals("open") && !bdmIncidentState.equals("closed")) {
                                if (isDebugLogEnabled)
                                    m_log.debug("Transferring event ownership to Connected Server <${m_connectedServerName}>: ${event.id}")
                                args.requestControl()
                            }
                            break

                        case URGENCY_TAG:
                            final String urgency = propertyValue
                            if (urgency && (syncAllSMUrgenciesToOPR || SyncSMUrgenciesToOPR.contains(urgency))) {
                                // map BDM urgency to opr event severity
                                final String severity = MapSM2OPRSeverity[urgency]
                                if (severity)
                                    args.severity = OprSeverity.valueOf(severity)
                            }
                            break

                        case PRIORITY_TAG:
                            // map BDM priority to opr event priority
                            final String smPriority = propertyValue
                            if (smPriority && (syncAllSMPrioritiesToOPR || SyncSMPrioritiesToOPR.contains(smPriority))) {
                                final String oprPriority = MapSM2OPRPriority[smPriority]
                                if (oprPriority)
                                    args.priority = OprPriority.valueOf(oprPriority)
                            }
                            break

                        case SOLUTION_TAG:
                            final String solution = propertyValue
                            if (!event.solution.equals(solution))
                                args.solution = solution
                            break

                        default:
                            break
                    }
                    // Check custom attributes separately. May want to sync standard property to CA, too.
                    String caName = MapSM2OPRCustomAttribute.get(propertyName)
                    if (caName)
                        args.addCustomAttribute(caName, propertyValue)
                }
            }
            return true
        } else
            return false
    }

/**
 * Convert the event into an external object.
 * Used by Event Synchronization GET WS. The String
 * is returned to callers of this WS.
 *
 * @param event object to convert
 * @return the converted external event object
 */
    public String toExternalEvent(final OprEvent event) {
        return toExternalEvent(event, null, null, null)
    }

/**
 * Convert the opr event into an BDM compliant incident object
 *
 * @param event object to convert
 * @param externalRefId and processing status
 * @return the converted external event object
 */
    public String toExternalEvent(final OprEvent event,
                                  final String externalRefId,
                                  final String causeExternalRefId,
                                  final OprIntegerPropertyChange duplicateChange) {
        if (event == null)
            return null

        // check if this was called by forwardEvent() to create a new SM Incident
        boolean isNewIncident = (externalRefId != null)

        boolean default_flag = true

        String astl_related_ci = null
        String astl_ci_os_name = null
        String astl_assignment_group = null
        String astl_logical_name = null
        String astl_priority = null
        String astl_urgency = null
        String astl_title = null
        String astl_description = null
        String astl_category = null
        String astl_sub_category = null

        Matcher myMatcher = null

//##################################### ASTELIT RULES SECTION #####################################
        if (isNewIncident) {

            final OprRelatedCi relatedCi_temp = event.relatedCi
            astl_related_ci = relatedCi_temp.configurationItem.ciName
            astl_ci_os_name = relatedCi_temp.configurationItem.ciName + " OS"

            //## Rule 1:
            //## RFC C21126: "OVO Agent is using too many system resources" events ##
            if (event.category == "Performance" && event.application == "HP OSSPI" && event.object == "CPU_ovagent") {

                astl_assignment_group = "SN-IO-ITM"

                default_flag = false
            }
            //############################ END Rule 1 ######################################

            //## Rule 2:
            //########### Policy "ASTL-Billing-Disk-Usage" (C18549) #################
            if (event.category == "billing_admin_team" && (MapOPR2SMUrgency[event.severity] == "1" || MapOPR2SMUrgency[event.severity] == "2")) {

                astl_logical_name = event.application
                astl_operational_device = "true"

                if (MapOPR2SMUrgency[event.severity] == "1") {
                    astl_priority = "2"
                }

                if (MapOPR2SMUrgency[event.severity] == "2") {
                    astl_priority = "3"
                }

                default_flag = false
            }
            //############################ END Rule 2 ######################################

            //## Rule 3:
            //######################### SAP Events ##################################
            if (event.category == "SAP" && event.application == "SAP" && event.object == "R3AbapShortdumps") {

                astl_logical_name = "sapUKP"
                astl_operational_device = "true"
                astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 3 ######################################

            //## Rule 4:
            //######################### TNS Events ##################################
            if (event.category == "TADIG" && event.object == "ths_datanet_file_transfer_check.sh" && (MapOPR2SMUrgency[event.severity] == "3")) {

                astl_logical_name = " "
                astl_assignment_group = "SN-AO-CSP-SSR"
                astl_title = "THS-NRTRDE file transfer delay"
                astl_operational_device = "true"
                astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 4 ######################################

            //## Rule 5:
            //######################## ABF Events ###################################
            if (event.category == "ORGA" && event.application == "ABF" && (MapOPR2SMUrgency[event.severity] == "2" || MapOPR2SMUrgency[event.severity] == "3")) {

                astl_assignment_group = "SN-AO-SCC"
                astl_logical_name = "ABF application"
                astl_category = "Service Platforms"
                astl_sub_category = "ABF"

                astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 5 ######################################

            //## Rule 6:
            //####################### SL3000 Events #################################
            if (event.application == "Tape Library" && event.object == "sl3000") {

                astl_logical_name = "SL3K"
                astl_title = "SL3K Drive not unloaded for fetch - on rewindUnload"
                astl_description = "SL3K Drive not unloaded for fetch - on rewindUnload"
                astl_category = "Infrastructure"
                astl_sub_category = "Backups - Hardware"
                astl_operational_device = "true"
                astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 6 ######################################

            //## Rule 7:
            //######################## EVA Events ###################################

            if (astl_related_ci =~ /(?i)EVA/) {
                astl_category = "Infrastructure"
                astl_sub_category = "Storage"

                default_flag = false
            }
            //############################ END Rule 7 ######################################

            //## Rule 8:
            //##################### AIS Reboot Events ###############################
            if (event.category == "Monitor" && event.application == "MonitorLoger" && (MapOPR2SMUrgency[event.severity] == "3")) {
                astl_logical_name = event.object
                astl_title = "Host ${event.object} was rebooted"
                astl_priority = "3"

                default_flag = false
            }
            //############################ END Rule 8 ######################################

            //## Rule 9:
            //##################### Performance Events ##############################
            if (event.category == "Performance" || event.object == "Connection_check") {
                astl_logical_name = astl_ci_os_name
                astl_operational_device = "true"

                default_flag = false
            }
            //############################ END Rule 9 ######################################

            //## Rule 10:
            //##################### Temperature Events ##############################
            if (event.application == "Temp mon") {
                if (event.title =~ /Temperature was changed/) {
                    astl_priority = "3"
                }

                if (event.title =~ /is more then max threshold/) {
                    astl_priority = "2"
                }

                if (event.title =~ /is lower then min threshold/) {
                    astl_priority = "2"
                }

                astl_logical_name = event.object
                astl_operational_device = "true"

                default_flag = false
            }
            //############################ END Rule 10 ######################################

            //## Rule 11:
            //###################### SAN Disk Events ################################
            if (event.category == "Hardware" && event.application == "SANdisk" && (MapOPR2SMUrgency[event.severity] == "2")) {

                astl_assignment_group = "SN-IO-SSDA-SA"
                astl_operational_device = "true"
                astl_priority = "1"

                default_flag = false
            }
            //############################ END Rule 11 ######################################

            //## Rule 12:
            //####################### HP SIM Events #################################
            if (event.category == "HP_SIM" && event.application == "HP_SIM") {

                astl_logical_name = event.object
                astl_sub_category = "HP SIM"

                //# HP SIM events with opened CASE in the HP (C20191)
                if (event.title =~ /SEA Version:System Event Analyzer for Windows/)
                    astl_operational_device = "true"

                //# Configuring Auto Incidents from Serviceguard cluster (C20026)
                if (event.title =~ /hpmcSG/)
                    astl_logical_name = astl_ci_os_name

                myMatcher = (event.title =~ /(NO_SERVER_CI_OUTAGE_FLAG)(.*)/)
                if (myMatcher.matches()) {
                    astl_operational_device = "true"
                    astl_description = myMatcher[0][2]
                }

                if (event.title =~ /Incomplete OA XML Data/)
                    astl_priority = "4"

                if (event.title =~ /(\(SNMP\) Process Monitor Event Trap \(11011\)|HP ProLiant-HP Power-Power Supply Failed|cpqHe4FltTolPowerSupplyDegraded|cpqHe4FltTolPowerSupplyFailed|\(WBEM\) Power redundancy reduced|\(WBEM\) Power redundancy lost|\(WBEM\) Power Supply Failed|\(SNMP\)  Power Supply Failed \(6050\)|\(SNMP\)  Power Redundancy Lost \(6032\))/)
                    astl_operational_device = "true"

                //# For WMI Events. If string Brief Description is in Message text
                myMatcher = (event.title =~ /Brief Description:\n\s(.*)/)
                if (myMatcher.matches())
                    astl_title = myMatcher[0][1]

                //# For SNMP Traps. If string Event Name is in Message text
                myMatcher = (event.title =~ /Event Name:\s(.*)/)
                if (myMatcher.matches())
                    astl_title = myMatcher[0][1]

                //# For WMI Events. If string Summary is in Message text
                myMatcher = (event.title =~ /Summary:\s(.*)/)
                if (myMatcher.matches())
                    astl_title = myMatcher[0][1]

                //# For WMI Events. If string Caption is in Message text
                myMatcher = (event.title =~ /Caption:\s(.*)/)
                if (myMatcher.matches())
                    astl_title = myMatcher[0][1]

                if (event.title =~ /(Severe latency bottleneck | is a congestion bottleneck)/) {
                    astl_assignment_group = "SN-IO-SSDA-SB"
                    astl_category = "Infrastructure"
                    astl_sub_category = "SAN Switch"
                    astl_operational_device = "true"

                    myMatcher = (event.title =~ /.*(.*AN-.*Slot.*, port.*is a congestion bottleneck)/)
                    if (myMatcher.matches()) {
                        astl_title = "${astl_object} ${myMatcher[0][1]}"

                        myMatcher = (event.title =~ /.*(.*AN-.*Slot.*, port.*is a congestion bottleneck.*percent of last.*seconds were affected by this condition.)/)
                        if (myMatcher.matches())
                            astl_description = "${astl_object} ${myMatcher[0][1]}"
                    }

                    myMatcher = (event.title =~ /.*(AN-.*Severe latency bottleneck detected at Slot.*port.*)/)
                    if (myMatcher.matches()) {
                        astl_title = "${astl_object} ${myMatcher[0][1]}"
                        astl_description = "${astl_object} ${myMatcher[0][1]}"
                    }
                }
                default_flag = false
            }
            //############################ END Rule 12 ######################################

            //## Rule 13:
            //### Auto Incidents for XP arrays (C17089) and AMS Storages (C19906) ###
            if (astl_related_ci =~ /phecda/ && event.category == "OS" && event.application == "Application" && event.object == "Event Log") {

                astl_assignment_group = "SN-IO-SSDA-SB"
                astl_category = "Infrastructure"
                astl_sub_category = "Storage"
                astl_priority = "3"
                astl_operational_device = "true"

                myMatcher = (event.title =~ /.*SOURCE.*"(.*XP.*)".*STATUS.*COMPONENT.*"(.*)".*DESCRIPTION.*".*error.*"/)
                if (myMatcher.matches()) {
                    astl_logical_name = myMatcher[0][1]
                    astl_title = "${myMatcher[0][1]}: ${myMatcher[0][2]}"
                }

                myMatcher = (event.title =~ /.*SOURCE.*"(.*AMS.*)".*STATUS.*COMPONENT.*"(Disk Drive.*)".*DESCRIPTION.*/)
                if (myMatcher.matches()) {
                    astl_logical_name = myMatcher[0][1]
                    astl_title = myMatcher[0][1] + " " + myMatcher[0][2] + " fail."
                }

                default_flag = false
            }
            //############################ END Rule 13 ######################################

            //## Rule 14:
            //################# Policies "astl-win-procmon*" ########################
            if (event.category == "win-procmon" && event.application == "OS") {

                astl_priority = "2"

                if (event.object == "startManagedWebLogic.cmd")
                    astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 14 ######################################

            //## Rule 15
            //#################### Policies "ASTL-Procmon" (C20690, C20691) ##########################
            if (event.category == "ELF-USSD" || event.category == "ELF-SMS") {
                if (MapOPR2SMUrgency[event.severity] == "2")
                    astl_priority = "2"

                default_flag = false
            }
            //############################ END Rule 15 ######################################

            //## Rule 16
            //################ Agent Health Status Events ###########################
            if (event.category == "Agent_Healthcheck" && event.object == "opcmsg") {
                astl_assignment_group = "SN-AO-SCC"

                default_flag = false
            }
            //############################ END Rule 16 ######################################

            //## Rule 17
            //################## ASTL-NG-BAS-Log-preparsed ##########################
            if (event.category == "Gold BAS Logs" && event.application == "Gold NG-BAS" && MapOPR2SMUrgency[event.severity] == "2") {
                astl_assignment_group = "SN-AO-CSP-BA"
                astl_logical_name = "OPSC Gold BAS"
                astl_priority = "3"

                default_flag = false
            }
            //############################ END Rule 17 ######################################

            //## Rule 18
            //################## ASTL-TGW-Log-preparsed #############################
            if (event.category == "TGW" && event.application == "TGW") {
                astl_logical_name = " "
                astl_assignment_group = "SN-AO-SCC"
                astl_priority = "4"

                if (event.title =~ /RTE interaction fails and delivers no result in operation \[Interaction \[RteModifyInteraction\] failed/) {
                    myMatcher = (event.title =~ /.*ERROR.*\[\[.*?,.*?,(.*?),.*/)
                    if (myMatcher.matches())
                        astl_title = "Troubles with reload ${myMatcher[0][1]}"
                }

                if (event.title =~ /\[ReloadBalances\] The line attribute \[RMF\] has an invalid value \[null\]/) {
                    myMatcher = (event.title =~ /.*ERROR \[.*\[\[\w+\,\s\*+\,\s(\d+)/)
                    if (myMatcher.matches())
                        astl_title = "${myMatcher[0][1]} attribute [RMF] has an invalid value"
                }

                default_flag = false
            }
            //############################ END Rule 18 ######################################

            //## Rule 19
            //######################## OVSC Events ##################################
            if (event.category == "OVSC" && event.application == "OVSC" && event.object == "IncorreDB") {
                astl_assignment_group = "SN-AO-CSP-BA"
                astl_logical_name = "OVSC"

                default_flag = false
            }
            //############################ END Rule 19 ######################################

            //## Rule 20
            //############ Policy "ASTL-IncoreDB-Usage" (C18273, C21385) #############
            if (event.category == "billing_admin_team" && event.object == "IncoreDB" && (MapOPR2SMUrgency[event.severity] == "1" || MapOPR2SMUrgency[event.severity] == "2")) {

                if (event.application == "MRTE")
                    astl_logical_name = "OPSC Gold MRTE"

                if (event.application == "OVSC" || event.application == "LookUp")
                    astl_logical_name = "OVSC"

                if (MapOPR2SMUrgency[event.severity] == "1")
                    astl_priority = "2"

                if (MapOPR2SMUrgency[event.severity] == "2")
                    astl_priority = "3"

                astl_assignment_group = "SN-AO-CSP-BA"

                default_flag = false
            }
            //############################ END Rule 20 ######################################

            //## Rule 21
            //######################## wIQ Events ###################################
            if (event.category == "wIQ" && event.application == "wIQ" && MapOPR2SMUrgency[event.severity] == "1") {
                astl_assignment_group = "SN-AO-CSP-BA"
                astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 21 ######################################

            //## Rule 22
            //######################## NTP Events ###################################
            if (event.category == "Time" && event.application == "NTP" && event.object == "Time") {
                astl_logical_name = astl_ci_os_name
                astl_operational_device = "true"

                default_flag = false
            }
            //############################ END Rule 22 ######################################

            //## Rule 23
            //###################### se9985.sdab.sn #################################
            if (astl_related_ci =~ /se9985/) {
                astl_assignment_group = "SN-IO-SSDA-SB"
                astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 23 ######################################

            //## Rule 24
            //####################### SCOM Events ###################################
            if (event.category == "SCOM") {
                Pattern pName = Pattern.compile("Name=(.*)")
                Pattern pDescription = Pattern.compile("Description=(.*)Name=", Pattern.DOTALL)

                Matcher mName = pName.matcher(event.title)
                Matcher mDescription = pDescription.matcher(event.title)

                if (mName.find()) {
                    astl_title = mName[0][1]
                }

                if (mDescription.find()) {
                    astl_description = mDescription[0][1].replace("\\u001a", '')
                }

                if (MapOPR2SMUrgency[event.severity] == "2")
                    astl_priority = "2"

                if (MapOPR2SMUrgency[event.severity] == "3")
                    astl_priority = "3"

                if (MapOPR2SMUrgency[event.severity] == "4")
                    astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 24 ######################################

            //## Rule 25
            //################## HP Data Protector Events ###########################
            if (event.category == "DP Session Reports") {

                Pattern pTitle = Pattern.compile("(.*)");
                Matcher mTitle = pTitle.matcher(event.title);
                astl_title = mTitle[0][1]

                astl_logical_name = "HP Data Protector Cell Manager " + astl_related_ci

                //#set DP_ACTION [exec /opt/OV/scauto/dp/dp_action $OPCDATA_MSGTEXT]
                //eventObject set_evfield action $OPCDATA_MSGTEXT

                //### Passing assignment group ###
                astl_assignment_group = "SN-IO-SSDA-SB"
                astl_category = "Infrastructure"
                astl_sub_category = "Backups - Software"
                astl_operational_device = "true"
                astl_priority = "4"

                default_flag = false
            }

            if (event.category == "Data Protector" && event.object == "Sheduler") {

                astl_logical_name = "HP Data Protector Cell Manager " + astl_related_ci

                astl_assignment_group = "SN-IO-SSDA-SB"
                astl_category = "Infrastructure"
                astl_sub_category = "Backups - Software"
                astl_operational_device = "true"
                astl_priority = "3"

                default_flag = false
            }
            //############################ END Rule 25 ######################################

            //## Rule 26
            //############ Oracle Enterprise Manager Events #########################
            if (event.category == "OracleEnterpriseManager") {
                myMatcher = (event.title =~ /Message:\s(.*)/)
                if (myMatcher.matches())
                    astl_title = myMatcher[0][1]

                if (event.application == "Database Instance" || event.application == "Agent" || event.application == "Listener" || event.application == "OMS and Repository" || event.application == "Oracle High Availability Service")
                    astl_logical_name = event.object + " Instance"

                if (event.application == "Cluster" || event.application == "Cluster Database")
                    astl_logical_name = event.object + " DB Cluster"

                //astl_category = "Databases"
                astl_assignment_group = "SN-IO-SSDA-DA"
                astl_priority = "2"

                default_flag = false
            }
            //############################ END Rule 26 ######################################

            //## Rule 26
            //################### SN-ISM Security Events ############################
            if (event.category == "SN-ISM" && event.application == "Security" && event.object == "Security") {
                astl_assignment_group = "SN-ISM"
                astl_category = "Security"
                astl_sub_category = "Security Systems Availability"

                astl_operational_device = "true"

                default_flag = false
            }

            if (event.category == "Security" && event.application == "S-TAP agent") {
                astl_assignment_group = "SN-ISM"
                astl_category = "Security"
                astl_sub_category = "Security Systems Availability"

                astl_operational_device = "true"

                default_flag = false
            }

            if (event.application == "ASTL_Node_Pinger" && event.object == "Connection_check") {
                astl_assignment_group = "SN-ISM"
                astl_category = "Security"
                astl_sub_category = "Security Systems Availability"
                astl_priority = "2"

                default_flag = false
            }
            //############################ END Rule 27 ######################################

            //## Rule 28
            //######################## Agent Errors #################################
            if (event.category == "OpC" && (event.application == "HP OpenView Operations" || event.application == "OM Agent") && (MapOPR2SMUrgency[event.severity] == "1" || MapOPR2SMUrgency[event.severity] == "2")) {
                astl_logical_name = astl_ci_os_name
                astl_assignment_group = "SN-AO-SCC"
                astl_priority = "3"

                default_flag = false
            }
            //############################ END Rule 28 ######################################

            //## Rule 29
            //################# MAXIMO Services Monitoring ##########################
            if (event.application == "Service Policy") {

                astl_assignment_group = "MN-OS-MSI"

                if (astl_related_ci =~ /maximodb2/ || astl_related_ci =~ /maximosb/)
                    astl_priority = "3"

                if (astl_related_ci =~ /maximo01/)
                    astl_priority = "4"

                default_flag = false
            }

            if (event.application == "MaximoPing") {
                astl_assignment_group = "MN-OS-MSI"
                astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 29 ######################################

            //## Rule 30
            //#################### Gold MRTE Event ##################################
            if (event.category == "evn_astelit" && event.application == "Gold MRTE" && (MapOPR2SMUrgency[event.severity] == "1" || MapOPR2SMUrgency[event.severity] == "2")) {

                astl_assignment_group = "SN-AO-SCC"
                astl_priority = "3"

                if (event.object == "event-files")
                    astl_logical_name = " "

                default_flag = false
            }

            if (event.category == "MRTE" && MapOPR2SMUrgency[event.severity] == "1") {

                astl_assignment_group = "SN-AO-CSP-BA"
                astl_priority = "1"

                if (event.application == "OPSC reg_scp rte1")
                    astl_logical_name = "OPSC reg_scp rte1"

                if (event.application == "OPSC reg_scp rte2")
                    astl_logical_name = "OPSC reg_scp rte2"

                default_flag = false
            }
            //############################ END Rule 30 ######################################

            //## Rule 31
            //########### Policy "ASTL-MRTE-IncoreDB-usage" (C16928) ################
            if (event.category == "MRTE" && (event.application == "mrte1a" || event.application == "mrte2a") && MapOPR2SMUrgency[event.severity] == "1") {
                if (event.application == "mrte1a")
                    astl_logical_name = "MRTE1-a"

                if (event.application == "mrte2a")
                    astl_logical_name = "MRTE2-a"

                astl_assignment_group = "SN-AO-CSP-BA"
                astl_priority = "1"

                default_flag = false
            }
            //############################ END Rule 31 ######################################

            //## Rule 32
            //########### Policy "astl_win_rcu_monitoring_rcuX" (C17860) ############
            if (event.category == "Hardware" && event.application == "RCU" && MapOPR2SMUrgency[event.severity] == "3") {
                astl_logical_name = astl_related_ci + " software"
                astl_priority = "4"

                default_flag = false
            }
            //############################ END Rule 32 ######################################

            //## Rule 33
            //############## Policy "bcp_monitoring_usage" (C18277) #################
            if (event.category == "BCP_mon" && event.application == "BCP Backup" && MapOPR2SMUrgency[event.severity] == "3") {
                astl_logical_name = event.object
                astl_operational_device = "true"
                astl_priority = "3"

                default_flag = false
            }
            //############################ END Rule 33 ######################################

            //## Rule 34
            //########### C20607: NNMi Management Events: SNMP_Interceptor ##########
            if (event.category == "SNMP" && event.application == "NNMi" && MapOPR2SMUrgency[event.severity] == "1" && event.title == "Node Down") {

                myMatcher = (event.object =~ /(\w+\sOS):(.*)/)
                if (myMatcher.matches()) {
                    astl_logical_name = myMatcher[0][1]
                    astl_assignment_group = myMatcher[0][2]
                    astl_category = "Security"
                    astl_sub_category = "Security Systems Availability"
                    astl_title = event.application + ":" + event.title + ":" + astl_related_ci
                    astl_operational_device = "true"
                    astl_priority = "4"
                }

                default_flag = false
            }
            //############################ END Rule 34 ######################################

            //## Rule 35
            //##################### Performance Events ##############################
            if (event.category == "Performance" && event.application == "HP OSSPI" && event.object == "CPU_Wait_Util") {

                astl_priority = "3"
                astl_operational_device = "true"

                default_flag = false
            }
            //############################ END Rule 35 ######################################

            //Add custom attributes


            addCustomAttribute(event, "operational_device", astl_operational_device);
            addCustomAttribute(event, "event_addon", "Happy New Year!");
            debugOprEvent(event, m_log, 2318);
        }

//##################################### END ASTELIT RULES SECTION ##################################
        //TODO Custom section
//##################################### ASTELIT CUSTOM SECTION #####################################
        if (!default_flag) {

            //FIXME Truncater to cope from delev package
            // get the title & description.
            String title = (event.title && event.title.trim()) ? event.title.trim().replace('\r', '\n') : null
            String description = (event.description && event.description.trim()) ? event.description.trim() : null

            if (astl_title != null)
                title = astl_title

            if (astl_description)
                description = astl_description

            if (title && (title.length() > 256 || title.contains('\n'))) {
                // truncate the title and put it in the description
                if (title.contains('\n'))
                    title = title.split('\n')[0].trim()
                if (title.length() > 256)
                    title = title.substring(0, 252) + "..."
                if (!description)
                    description = event.title.trim()
                else
                    description = event.title.trim() + "\n" + event.description.trim()
            }

            // if the description is not set on Incident creation then set it to some default value
            if (isNewIncident && ((description == null) || (description.trim().length() == 0)))
            //description = EMPTY_DESCRIPTION_OVERRIDE
                if (astl_description == null)
                    description = event.title
            // create the XML payload using the MarkupBuilder
            final StringWriter writer = new StringWriter()
            final MarkupBuilder builder = new MarkupBuilder(writer)
            final StringBuffer activityLog = new StringBuffer()

            builder."${INCIDENT_TAG}"(relationships_included: "${INCIDENT_XML_RELATIONSHIPS}",
                    type: "${INCIDENT_XML_TYPE}",
                    version: "${INCIDENT_XML_VERSION}",
                    xmlns: "${INCIDENT_XML_NAMESPACE}") {

                builder.it_process_category(IT_PROCESS_CATEGORY)
                builder.incident_type(INCIDENT_TYPE)
                if (SpecifyActiveProcess)
                    builder.active_process("true")
                //TODO Move this to custom attribute

                // builder."${OPERATIONAL_DEVICE_TAG}"(astl_operational_device)

                activityLog.append('\n').append(ACTIVITY_LOG_OPERATIONAL_DATA).append('\n').
                        append(astl_operational_device).append('\n')

                if (astl_priority) {
                    if (astl_priority == "1") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "Enterprise", 'enterprise')
                    }
                    if (astl_priority == "2") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "Site/Dept", 'site-dept')
                    }
                    if (astl_priority == "3") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "Multiple Users", 'multiple-users')
                    }
                    if (astl_priority == "4") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "User", 'user')
                    }
                } else {
                    if (MapOPR2SMUrgency[event.severity] == "1") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "Site/Dept", 'site-dept')
                    }
                    if (MapOPR2SMUrgency[event.severity] == "2") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "Multiple Users", 'multiple-users')
                    }
                    if (MapOPR2SMUrgency[event.severity] == "3") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "User", 'user')
                    }
                    if (MapOPR2SMUrgency[event.severity] == "4") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "User", 'user')
                    }
                }

                if (isNewIncident) {
                    // Add 'Time OMi Event Created' to activity log
                    if (event.timeCreated) {
                        activityLog.append('\n').append(ACTIVITY_LOG_TIME_CREATED).append('\n').
                                append(dateFormatter.format(event.timeCreated)).append('\n')
                    }
                    // Add 'Time OMi Event Received' to activity log
                    if (event.timeReceived) {
                        activityLog.append('\n').append(ACTIVITY_LOG_TIME_RECEIVED).append('\n').
                                append(dateFormatter.format(event.timeReceived)).append('\n')
                    }
                    // set the external process id, category, subCategory and related CI for new Incidents
                    builder."${EXTERNAL_PROCESS_ID_TAG}"(externalRefId)

                    // set the related CI on new incidents
                    final OprNodeReference nodeRef = event.node
                    final OprRelatedCi relatedCi = event.relatedCi
                    final String dnsName = getDnsName(event)
                    // Astelit's Default Related CI Name
                    String astelitRelatedCI = relatedCi.configurationItem.ciName + " OS"

                    if (relatedCi != null && !UseNodeCI) {
                        // send 'is_registered_for' CI information using event related CI
                        builder."${CI_RELATIONSHIP}"(target_role: "${CONFIGURATION_ITEM_ROLE}") {
                            if (relatedCi.configurationItem.globalId)
                                builder."${CI_GLOBALID_TAG}"(relatedCi.configurationItem.globalId)
                            builder."${CI_TARGET_TYPE_TAG}"(CI_TARGET_TYPE)
                            builder."${CONFIGURATION_ITEM_TAG}" {
                                if (relatedCi.configurationItem.ciType)
                                    builder."${CI_TYPE_TAG}"(relatedCi.configurationItem.ciType)
                                if (relatedCi.configurationItem.id)
                                    builder."${CI_ID_TAG}"(relatedCi.configurationItem.id)

                                //if (relatedCi.configurationItem.ciName)
                                //builder."${CI_NAME_TAG}"(relatedCi.configurationItem.ciName)

                                //Astelit Related CI
                                if (astl_logical_name != null) {
                                    builder."${CI_NAME_TAG}"(astl_logical_name)
                                    builder."${CI_DISPLAY_LABEL_TAG}"(astl_logical_name)
                                } else {
                                    if (astelitRelatedCI)
                                        builder."${CI_NAME_TAG}"(astelitRelatedCI)
                                    builder."${CI_DISPLAY_LABEL_TAG}"(astelitRelatedCI)
                                }

                                if (dnsName)
                                    builder."${NODE_DNS_NAME_TAG}"(dnsName)
                                if (nodeRef != null && !relatedCi.configurationItem.id.equals(nodeRef.node.id)) {
                                    // send 'is_hosted_on' CI information using event node CI
                                    builder."${NODE_RELATIONSHIP}"(target_role: "${NODE_ITEM_ROLE}") {
                                        if (nodeRef.node.globalId)
                                            builder."${CI_GLOBALID_TAG}"(nodeRef.node.globalId)
                                        builder."${CI_TARGET_TYPE_TAG}"(nodeRef.node.selfType.toString())
                                        builder."${CONFIGURATION_ITEM_TAG}" {
                                            if (nodeRef.node.ciType)
                                                builder."${CI_TYPE_TAG}"(nodeRef.node.ciType)
                                            if (nodeRef.node.id)
                                                builder."${CI_ID_TAG}"(nodeRef.node.id)
                                            if (nodeRef.node.ciName)
                                                builder."${CI_NAME_TAG}"(nodeRef.node.ciName)
                                            if (nodeRef.node.ciDisplayLabel)
                                                builder."${CI_DISPLAY_LABEL_TAG}"(nodeRef.node.ciDisplayLabel)
                                            if (dnsName)
                                                builder."${NODE_DNS_NAME_TAG}"(dnsName)
                                        }
                                    }
                                }
                            }
                        }

                        activityLog.append('\n')
                        activityLog.append(ACTIVITY_LOG_RELATED_CI)
                        if (relatedCi.configurationItem.ciDisplayLabel)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_LABEL).
                                    append(relatedCi.configurationItem.ciDisplayLabel)

                        //          if (relatedCi.configurationItem.ciName)
                        //            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
                        if (astl_logical_name) {
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
                                    append(astl_logical_name)
                        } else {
                            if (astelitRelatedCI)
                                activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
                                        append(astelitRelatedCI)
                        }

                        if (m_oprVersion >= 920 && relatedCi.configurationItem.ciTypeLabel)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).
                                    append(relatedCi.configurationItem.ciTypeLabel)
                        if (relatedCi.configurationItem.ciType)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE).
                                    append(relatedCi.configurationItem.ciType)
                        if (event.drilldownUrl && relatedCi.configurationItem.id) {
                            final URL eventUrl = event.drilldownUrl
                            final URL ciUrl = new URL(eventUrl.protocol, eventUrl.host, eventUrl.port,
                                    "${BSM_CI_DRILLDOWN_PATH}${relatedCi.configurationItem.id}")
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString())
                        }
                        if (dnsName)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName)
                        activityLog.append('\n')
                    } else if (nodeRef != null) {
                        // send 'is_registered_for' CI information using event node CI
                        builder."${CI_RELATIONSHIP}"(target_role: "${CONFIGURATION_ITEM_ROLE}") {
                            if (nodeRef.node.globalId)
                                builder."${CI_GLOBALID_TAG}"(nodeRef.node.globalId)
                            builder."${CI_TARGET_TYPE_TAG}"(CI_TARGET_TYPE)
                            builder."${CONFIGURATION_ITEM_TAG}" {
                                if (nodeRef.node.ciType)
                                    builder."${CI_TYPE_TAG}"(nodeRef.node.ciType)
                                if (nodeRef.node.id)
                                    builder."${CI_ID_TAG}"(nodeRef.node.id)
                                if (nodeRef.node.ciName)
                                    builder."${CI_NAME_TAG}"(nodeRef.node.ciName)
                                if (nodeRef.node.ciDisplayLabel)
                                    builder."${CI_DISPLAY_LABEL_TAG}"(nodeRef.node.ciDisplayLabel)
                                if (dnsName)
                                    builder."${NODE_DNS_NAME_TAG}"(dnsName)
                            }
                        }
                        activityLog.append('\n')
                        activityLog.append(ACTIVITY_LOG_RELATED_CI)
                        if (nodeRef.node.ciDisplayLabel)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_LABEL).append(nodeRef.node.ciDisplayLabel)
                        if (nodeRef.node.ciName)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).append(nodeRef.node.ciName)
                        if (m_oprVersion >= 920 && nodeRef.node.ciTypeLabel)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).append(nodeRef.node.ciTypeLabel)
                        if (nodeRef.node.ciType)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE).append(nodeRef.node.ciType)
                        if (event.drilldownUrl && nodeRef.node.id) {
                            final URL eventUrl = event.drilldownUrl
                            final URL ciUrl = new URL(eventUrl.protocol, eventUrl.host, eventUrl.port,
                                    "${BSM_CI_DRILLDOWN_PATH}${nodeRef.node.id}")
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString())
                        }
                        if (dnsName)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName)
                        activityLog.append('\n')
                    }
                    // check for most affected business service in 9.21 or greater
                    if (m_oprVersion > 920)
                        setBusinessService(event, builder, activityLog)
                    else {
                        if ((RTSM_QUERY_MAX_STEPS > 0) && (relatedCi != null) && relatedCi.targetId)
                            setBusinessServicePre921(relatedCi.configurationItem, event.drilldownUrl, builder, activityLog)
                    }

                    if (astl_category) {
                        builder."${CATEGORY_TAG}"(astl_category)
                        activityLog.append('\n').append(ACTIVITY_LOG_CATEGORY).append('\n').append(astl_category).append('\n')
                    } else {
                        builder."${CATEGORY_TAG}"("${ASTELIT_CATEGORY}")
                        activityLog.append('\n').append(ACTIVITY_LOG_CATEGORY).append('\n').append("${ASTELIT_CATEGORY}").append('\n')
                    }

                    if (astl_sub_category) {
                        builder."${SUB_CATEGORY_TAG}"(astl_sub_category)
                        activityLog.append('\n').append(ACTIVITY_LOG_SUBCATEGORY).append('\n').append(astl_sub_category).append('\n')
                    } else {
                        builder."${SUB_CATEGORY_TAG}"("${ASTELIT_SUB_CATEGORY}")
                        activityLog.append('\n').append(ACTIVITY_LOG_SUBCATEGORY).append('\n').append("${ASTELIT_SUB_CATEGORY}").append('\n')
                    }

                    if (event.application) {
                        activityLog.append('\n').append(ACTIVITY_LOG_APPLICATION).append('\n').append(event.application).append('\n')
                    }
                    if (event.object) {
                        activityLog.append('\n').append(ACTIVITY_LOG_OBJECT).append('\n').append(event.object).append('\n')
                    }
                    // Add 'Original Data' to activity log
                    if (event.originalData) {
                        activityLog.append('\n').append(ACTIVITY_LOG_ORIGINAL_DATA).append('\n').
                                append(event.originalData).append('\n')
                    }
                }

                // Add 'Time OMi Event State Changed' to activity log
                if (event.timeStateChanged
                        && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("time_state_changed"))) {
                    activityLog.append('\n').append(ACTIVITY_LOG_TIME_STATE_CHANGED).append('\n').
                            append(dateFormatter.format(event.timeStateChanged)).append(' : ').append(event.state).append('\n')
                }

                // check title
                if (title
                        && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("title")))
                    builder."${TITLE_TAG}"(title)

                if (event.title
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("title"))) {
                    // synchronize the title to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_TITLE).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_TITLE_CHANGE)
                    activityLog.append(event.title.trim())
                    activityLog.append('\n')
                }

                // check description
                if (description
                        && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("description")))
                    builder."${DESCRIPTION_TAG}"(description)

                if (event.description
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("description"))) {
                    // synchronize the description to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_DESCRIPTION).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_DESCRIPTION_CHANGE)
                    activityLog.append(event.description.trim())
                    activityLog.append('\n')
                }

                // check solution
                if (event.solution
                        && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("solution")))
                    builder."${SOLUTION_TAG}"(event.solution)

                if (event.solution
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("solution"))) {
                    // synchronize the solution to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_SOLUTION).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_SOLUTION_CHANGE)
                    activityLog.append(event.solution.trim())
                    activityLog.append('\n')
                }

                // check assigned user
                if (((event.assignedUser?.id >= 0) && (event.assignedUser?.userName || event.assignedUser?.loginName))
                        && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_user")))
                    builder."${REQUESTED_BY_TAG}" {
                        builder."${PARTY_TAG}" {
                            if (event.assignedUser?.userName)
                                builder."${UI_NAME_TAG}"(event.assignedUser.userName)
                            if (event.assignedUser?.loginName)
                                builder."${NAME_TAG}"(event.assignedUser.loginName)
                        }
                    }

                // set the contact name to the BSM assigned user
                if (event.assignedUser?.userName
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_user"))) {
                    // synchronize the assigned user to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_ASSIGNED_USER).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_ASSIGNED_USER_CHANGE)
                    if (event.assignedUser?.id < 0)
                        activityLog.append(ACTIVITY_LOG_UNASSIGNED)
                    else
                        activityLog.append(event.assignedUser.userName.trim())
                    activityLog.append('\n')
                }

                // check assigned group
                if (astl_assignment_group && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_group")))
                    builder."${ASSIGNED_GROUP_TAG}" {
                        builder."${FUNCTIONAL_GROUP_TAG}" {
                            builder."${UI_NAME_TAG}"(astl_assignment_group)
                            builder."${NAME_TAG}"(astl_assignment_group)
                        }
                    }

                // set the functional group name to the BSM assigned group
                if (event.assignedGroup?.name
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_group"))) {
                    // synchronize the assigned user to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE)

                    if (astl_assignment_group) {
                        activityLog.append(astl_assignment_group)
                        activityLog.append('\n')
                    } else {
                        if (event.assignedGroup?.id < 0)
                            activityLog.append(ACTIVITY_LOG_UNASSIGNED)
                        else
                            activityLog.append(event.assignedGroup.name.trim())
                        activityLog.append('\n')
                    }
                }

                // check state
                if (event.state
                        && (isNewIncident
                        || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("state"))
                        && (syncAllOPRStatesToSM || SyncOPRStatesToSM.contains(event.state))))) {
                    String status = MapOPR2SMStatus[event.state]
                    builder."${INCIDENT_STATUS_TAG}"(status)
                    if ("closed".equals(status)) {
                        builder."${COMPLETION_CODE_TAG}"(SMCompletionCode)
                    }
                }

                if (event.state
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("state"))) {
                    // synchronize the state to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_STATE).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_STATE_CHANGE)
                    activityLog.append(event.state.trim())
                    activityLog.append('\n')
                }

                // check urgency/severity

                if (astl_priority) {
                    if (astl_priority
                            && (isNewIncident
                            || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("severity"))
                            && (syncAllOPRSeveritiesToSM || SyncOPRSeveritiesToSM.contains(astl_priority)))))
                        builder."${URGENCY_TAG}"(astl_priority)

                    if (astl_priority
                            && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("severity"))) {
                        // synchronize the severity to the incident activity log
                        activityLog.append('\n')
                        activityLog.append(ACTIVITY_LOG_SEVERITY).append("\n")
                        if (!isNewIncident)
                            activityLog.append(ACTIVITY_LOG_SEVERITY_CHANGE)
                        activityLog.append(event.severity)
                        activityLog.append('\n')
                    }
                } else {
                    if (MapOPR2SMUrgency[event.severity] == "1") {
                        astl_urgency = "2"
                    }
                    if (MapOPR2SMUrgency[event.severity] == "2") {
                        astl_urgency = "3"
                    }
                    if (MapOPR2SMUrgency[event.severity] == "3") {
                        astl_urgency = "4"
                    }
                    if (MapOPR2SMUrgency[event.severity] == "4") {
                        astl_urgency = "4"
                    }

                    if (astl_urgency
                            && (isNewIncident
                            || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("severity"))
                            && (syncAllOPRSeveritiesToSM || SyncOPRSeveritiesToSM.contains(astl_urgency)))))
                        builder."${URGENCY_TAG}"(astl_urgency)

                    if (astl_urgency
                            && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("severity"))) {
                        // synchronize the severity to the incident activity log
                        activityLog.append('\n')
                        activityLog.append(ACTIVITY_LOG_SEVERITY).append("\n")
                        if (!isNewIncident)
                            activityLog.append(ACTIVITY_LOG_SEVERITY_CHANGE)
                        activityLog.append(event.severity)
                        activityLog.append('\n')
                    }
                }

                // check priority
                //if (event.priority
                //&& (isNewIncident
                //|| ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("priority"))
                //&& (syncAllOPRPrioritiesToSM || SyncOPRPrioritiesToSM.contains(event.priority)))))
                //builder."${PRIORITY_TAG}"(MapOPR2SMPriority[event.priority])

                if (event.priority
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("priority"))) {
                    // synchronize the priority to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_PRIORITY).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_PRIORITY_CHANGE)
                    activityLog.append(event.priority)
                    activityLog.append('\n')
                }

                // set is_recorded_by (opened.by) to "admin" or Control Transfer initiator
                if (isNewIncident) {
                    boolean initiatedBySystem = (event.controlTransferredTo?.initiatedBy == null) ||
                            ("system".equals(event.controlTransferredTo.initiatedBy))
                    String recorder = initiatedBySystem ? BSM_ADMINISTRATOR_LOGIN_NAME : event.controlTransferredTo.initiatedBy
                    builder."${RECORDED_BY_TAG}" {
                        builder."${PERSON_TAG}" {
                            builder."${UI_NAME_TAG}"(recorder)
                            builder."${NAME_TAG}"(recorder)
                        }
                    }
                    // Add initiator info to Activity Log
                    OprForwardingInfo forwardingInfo = event.getForwardingInfo(m_connectedServerId)
                    if (initiatedBySystem && (m_oprVersion >= 910) && forwardingInfo?.ruleName) {
                        activityLog.append('\n').
                                append(ACTIVITY_LOG_INITIATED_BY).append("\n").
                                append(ACTIVITY_LOG_INITIATED_BY_RULE).
                                append(forwardingInfo.ruleName)
                    } else {
                        activityLog.append('\n').
                                append(ACTIVITY_LOG_INITIATED_BY).append("\n").
                                append(ACTIVITY_LOG_INITIATED_BY_USER).
                                append(recorder)
                    }
                    activityLog.append('\n')
                }

                if (event.controlTransferredTo
                        && !m_node.equalsIgnoreCase(event.controlTransferredTo.dnsName)
                        && OprControlTransferStateEnum.transferred.name().equals(event.controlTransferredTo.state)
                        && (syncAllOPRPropertiesToSMActivityLog
                        || SyncOPRPropertiesToSMActivityLog.contains("control_transferred_to"))) {
                    // synchronize the priority to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_CONTROL_TRANSFERRED_TO).append("\n")
                    activityLog.append(event.controlTransferredTo.dnsName).append(":").append(event.controlTransferredTo.state)
                    activityLog.append('\n')
                }

                // check if there are any annotations to add to the activity log
                if ((event.annotations != null)
                        && (isNewIncident
                        || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("annotation"))) {
                    // append any annotations
                    event.annotations?.annotations?.each() { OprAnnotation annotation ->
                        final String text = annotation.text
                        if (text.length() > 0) {
                            final String date = dateFormatter.format(annotation.timeCreated)
                            final String author = annotation.author
                            activityLog.append('\n')
                            activityLog.append(ACTIVITY_LOG_ANNOTATION).append("\n - ${date} - ${author} - ${text}")
                            activityLog.append('\n')
                        }
                    }
                }

                // check if there are any custom attributes to add to the activity log

                if (m_log.isDebugEnabled()) {
                    m_log.debug("We are inside toExternalEvent method")

                    if (!m_OPR2SMCustomAttribute.isEmpty() && (event.customAttributes != null)) {
                        m_log.debug("We passed (!m_OPR2SMCustomAttribute.isEmpty() && (event.customAttributes != null)) condition");
                    }
                    debugOprEvent(event, m_log, 2849);
                }

                //TODO check oprational device custom attribute
                if (!m_OPR2SMCustomAttribute.isEmpty() && (event.customAttributes != null)) {
                    event.customAttributes.customAttributes?.each() { OprCustomAttribute customAttribute ->
                        final String caName = customAttribute.name.toLowerCase(LOCALE)

                        if (m_OPR2SMCustomAttribute.containsKey(caName)) {
                            final String smIncidentProperty = m_OPR2SMCustomAttribute.get(caName)
                            if (m_log.isDebugEnabled()) {
                                m_log.debug("We passed custom attribute conditions");
                                m_log.debug("Now we processing CA name: " + customAttribute.getName() + " with value " + customAttribute.getValue());
                            }
                            // synchronize this CA to SM
                            if (ACTIVITY_LOG_TAG.equals(smIncidentProperty)) {
                                // synchronize the CA to the SM incident activity log
                                activityLog.append('\n')
                                activityLog.append(ACTIVITY_LOG_CA).append("\n${customAttribute.name}=${customAttribute.value}")
                                activityLog.append('\n')
                            } else {
                                // synchronize to the specified SM incident property
                                builder."${smIncidentProperty}"(customAttribute.value)
                            }
                        }
                    }
                }

                String drilldownUrl = event.drilldownUrl
                if (drilldownUrl && drilldownUrl.lastIndexOf('=') > 0)
                    drilldownUrl = drilldownUrl.substring(0, drilldownUrl.lastIndexOf('=') + 1)

                if (event.cause) {
                    if (causeExternalRefId) {
                        if (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("cause")) {
                            builder."${IS_CAUSED_BY}"(target_role: "${IS_CAUSED_BY_ROLE}") {
                                builder."${MASTER_REFERENCE_TAG}"(causeExternalRefId)
                            }
                        }
                        if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
                            // synchronize the SM cause to the incident activity log
                            activityLog.append('\n')
                            activityLog.append(ACTIVITY_LOG_CAUSE).append("\n").append(causeExternalRefId)
                            activityLog.append('\n')
                        }
                    } else if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
                        // synchronize the OPR cause to the incident activity log
                        activityLog.append('\n')
                        final String causeTitle = event.cause.title
                        final String causeUrl = (drilldownUrl) ? drilldownUrl + event.cause.targetId : null
                        if (causeUrl)
                            activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append("${causeTitle}\n\t${causeUrl}")
                        else
                            activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append(causeTitle)
                        activityLog.append('\n')
                    }
                }

                if ((event.symptoms != null)
                        && (isNewIncident
                        || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("symptom"))) {
                    // synchronize the OPR symptom to the incident activity log
                    event.symptoms.eventReferences?.each() { OprSymptomReference symptomRef ->
                        activityLog.append('\n')
                        final String symptomTitle = symptomRef.title
                        final String symptomUrl = (drilldownUrl) ? drilldownUrl + symptomRef.targetId : null
                        if (symptomUrl)
                            activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append("${symptomTitle}\n\t${symptomUrl}")
                        else
                            activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append(symptomTitle)
                        activityLog.append('\n')
                    }
                }

                if (duplicateChange
                        && (isNewIncident
                        || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("duplicate_count"))) {
                    final Integer previousCount = (Integer) duplicateChange.previousValue
                    final Integer currentCount = (Integer) duplicateChange.currentValue

                    if (currentCount > 0) {
                        // synchronize the duplicate count to the incident activity log
                        if (previousCount == null) {
                            activityLog.append('\n')
                            activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n").append(currentCount)
                        } else {
                            activityLog.append('\n')
                            activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n")
                            activityLog.append("${ACTIVITY_LOG_PREVIOUS} ${previousCount} ${ACTIVITY_LOG_CURRENT} ${currentCount}")
                        }
                        activityLog.append('\n')
                    }
                }

                // set any activityLog
                if (activityLog.length() > 0) {
                    builder."${ACTIVITY_LOG_TAG}" {
                        builder."${ACTIVITY_LOG_DESC_TAG}"(activityLog.toString())
                    }
                }
            }
            final String output = writer.toString()
            return output

        }
//##################################### END ASTELIT CUSTOM SECTION #####################################
        //TODO Default section
//##################################### DEFAULT SECTION #####################################
        if (default_flag) {

            // get the title & description.
            String title = (event.title && event.title.trim()) ? event.title.trim().replace('\r', '\n') : null
            String description = (event.description && event.description.trim()) ? event.description.trim() : null

            if (title && (title.length() > 256 || title.contains('\n'))) {
                // truncate the title and put it in the description
                if (title.contains('\n'))
                    title = title.split('\n')[0].trim()
                if (title.length() > 256)
                    title = title.substring(0, 252) + "..."
                if (!description)
                    description = event.title.trim()
                else
                    description = event.title.trim() + "\n" + event.description.trim()
            }

            // if the description is not set on Incident creation then set it to some default value
            if (isNewIncident && ((description == null) || (description.trim().length() == 0)))
            //description = EMPTY_DESCRIPTION_OVERRIDE
                description = event.title
            // create the XML payload using the MarkupBuilder
            final StringWriter writer = new StringWriter()
            final MarkupBuilder builder = new MarkupBuilder(writer)
            final StringBuffer activityLog = new StringBuffer()

            builder."${INCIDENT_TAG}"(relationships_included: "${INCIDENT_XML_RELATIONSHIPS}",
                    type: "${INCIDENT_XML_TYPE}",
                    version: "${INCIDENT_XML_VERSION}",
                    xmlns: "${INCIDENT_XML_NAMESPACE}") {

                builder.it_process_category(IT_PROCESS_CATEGORY)
                builder.incident_type(INCIDENT_TYPE)
                if (SpecifyActiveProcess)
                    builder.active_process("true")
                //TODO remove this custom attribute
                builder."${OPERATIONAL_DEVICE_TAG}"(astl_operational_device)

                activityLog.append('\n').append(ACTIVITY_LOG_OPERATIONAL_DATA).append('\n').
                        append(astl_operational_device).append('\n')

                if (SpecifyImpactScope) {
                    if (MapOPR2SMUrgency[event.severity] == "1") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "Site/Dept", 'site-dept')
                    }
                    if (MapOPR2SMUrgency[event.severity] == "2") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "Multiple Users", 'multiple-users')
                    }
                    if (MapOPR2SMUrgency[event.severity] == "3") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "User", 'user')
                    }
                    if (MapOPR2SMUrgency[event.severity] == "4") {
                        builder."${IMPACT_SCOPE_TAG}"(label: "User", 'user')
                    }
                }

                if (isNewIncident) {
                    // Add 'Time OMi Event Created' to activity log
                    if (event.timeCreated) {
                        activityLog.append('\n').append(ACTIVITY_LOG_TIME_CREATED).append('\n').
                                append(dateFormatter.format(event.timeCreated)).append('\n')
                    }
                    // Add 'Time OMi Event Received' to activity log
                    if (event.timeReceived) {
                        activityLog.append('\n').append(ACTIVITY_LOG_TIME_RECEIVED).append('\n').
                                append(dateFormatter.format(event.timeReceived)).append('\n')
                    }
                    // set the external process id, category, subCategory and related CI for new Incidents
                    builder."${EXTERNAL_PROCESS_ID_TAG}"(externalRefId)

                    // set the related CI on new incidents
                    final OprNodeReference nodeRef = event.node
                    final OprRelatedCi relatedCi = event.relatedCi
                    final String dnsName = getDnsName(event)
                    // Astelit's Default Related CI Name
                    String astelitRelatedCI = relatedCi.configurationItem.ciName + " OS"

                    if (relatedCi != null && !UseNodeCI) {
                        // send 'is_registered_for' CI information using event related CI
                        builder."${CI_RELATIONSHIP}"(target_role: "${CONFIGURATION_ITEM_ROLE}") {
                            if (relatedCi.configurationItem.globalId)
                                builder."${CI_GLOBALID_TAG}"(relatedCi.configurationItem.globalId)
                            builder."${CI_TARGET_TYPE_TAG}"(CI_TARGET_TYPE)
                            builder."${CONFIGURATION_ITEM_TAG}" {
                                if (relatedCi.configurationItem.ciType)
                                    builder."${CI_TYPE_TAG}"(relatedCi.configurationItem.ciType)
                                if (relatedCi.configurationItem.id)
                                    builder."${CI_ID_TAG}"(relatedCi.configurationItem.id)

                                //if (relatedCi.configurationItem.ciName)
                                //builder."${CI_NAME_TAG}"(relatedCi.configurationItem.ciName)
                                //Astelit Related CI
                                if (astelitRelatedCI)
                                    builder."${CI_NAME_TAG}"(astelitRelatedCI)

                                if (relatedCi.configurationItem.ciDisplayLabel)
                                    builder."${CI_DISPLAY_LABEL_TAG}"(relatedCi.configurationItem.ciDisplayLabel)
                                if (dnsName)
                                    builder."${NODE_DNS_NAME_TAG}"(dnsName)
                                if (nodeRef != null && !relatedCi.configurationItem.id.equals(nodeRef.node.id)) {
                                    // send 'is_hosted_on' CI information using event node CI
                                    builder."${NODE_RELATIONSHIP}"(target_role: "${NODE_ITEM_ROLE}") {
                                        if (nodeRef.node.globalId)
                                            builder."${CI_GLOBALID_TAG}"(nodeRef.node.globalId)
                                        builder."${CI_TARGET_TYPE_TAG}"(nodeRef.node.selfType.toString())
                                        builder."${CONFIGURATION_ITEM_TAG}" {
                                            if (nodeRef.node.ciType)
                                                builder."${CI_TYPE_TAG}"(nodeRef.node.ciType)
                                            if (nodeRef.node.id)
                                                builder."${CI_ID_TAG}"(nodeRef.node.id)
                                            if (nodeRef.node.ciName)
                                                builder."${CI_NAME_TAG}"(nodeRef.node.ciName)
                                            if (nodeRef.node.ciDisplayLabel)
                                                builder."${CI_DISPLAY_LABEL_TAG}"(nodeRef.node.ciDisplayLabel)
                                            if (dnsName)
                                                builder."${NODE_DNS_NAME_TAG}"(dnsName)
                                        }
                                    }
                                }
                            }
                        }
                        activityLog.append('\n')
                        activityLog.append(ACTIVITY_LOG_RELATED_CI)
                        if (relatedCi.configurationItem.ciDisplayLabel)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_LABEL).
                                    append(relatedCi.configurationItem.ciDisplayLabel)

                        //          if (relatedCi.configurationItem.ciName)
                        //            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
                        if (astelitRelatedCI)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
                                    append(astelitRelatedCI)

                        if (m_oprVersion >= 920 && relatedCi.configurationItem.ciTypeLabel)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).
                                    append(relatedCi.configurationItem.ciTypeLabel)
                        if (relatedCi.configurationItem.ciType)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE).
                                    append(relatedCi.configurationItem.ciType)
                        if (event.drilldownUrl && relatedCi.configurationItem.id) {
                            final URL eventUrl = event.drilldownUrl
                            final URL ciUrl = new URL(eventUrl.protocol, eventUrl.host, eventUrl.port,
                                    "${BSM_CI_DRILLDOWN_PATH}${relatedCi.configurationItem.id}")
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString())
                        }
                        if (dnsName)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName)
                        activityLog.append('\n')
                    } else if (nodeRef != null) {
                        // send 'is_registered_for' CI information using event node CI
                        builder."${CI_RELATIONSHIP}"(target_role: "${CONFIGURATION_ITEM_ROLE}") {
                            if (nodeRef.node.globalId)
                                builder."${CI_GLOBALID_TAG}"(nodeRef.node.globalId)
                            builder."${CI_TARGET_TYPE_TAG}"(CI_TARGET_TYPE)
                            builder."${CONFIGURATION_ITEM_TAG}" {
                                if (nodeRef.node.ciType)
                                    builder."${CI_TYPE_TAG}"(nodeRef.node.ciType)
                                if (nodeRef.node.id)
                                    builder."${CI_ID_TAG}"(nodeRef.node.id)
                                if (nodeRef.node.ciName)
                                    builder."${CI_NAME_TAG}"(nodeRef.node.ciName)
                                if (nodeRef.node.ciDisplayLabel)
                                    builder."${CI_DISPLAY_LABEL_TAG}"(nodeRef.node.ciDisplayLabel)
                                if (dnsName)
                                    builder."${NODE_DNS_NAME_TAG}"(dnsName)
                            }
                        }
                        activityLog.append('\n')
                        activityLog.append(ACTIVITY_LOG_RELATED_CI)
                        if (nodeRef.node.ciDisplayLabel)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_LABEL).append(nodeRef.node.ciDisplayLabel)
                        if (nodeRef.node.ciName)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).append(nodeRef.node.ciName)
                        if (m_oprVersion >= 920 && nodeRef.node.ciTypeLabel)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).append(nodeRef.node.ciTypeLabel)
                        if (nodeRef.node.ciType)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE).append(nodeRef.node.ciType)
                        if (event.drilldownUrl && nodeRef.node.id) {
                            final URL eventUrl = event.drilldownUrl
                            final URL ciUrl = new URL(eventUrl.protocol, eventUrl.host, eventUrl.port,
                                    "${BSM_CI_DRILLDOWN_PATH}${nodeRef.node.id}")
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString())
                        }
                        if (dnsName)
                            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName)
                        activityLog.append('\n')
                    }
                    // check for most affected business service in 9.21 or greater
                    if (m_oprVersion > 920)
                        setBusinessService(event, builder, activityLog)
                    else {
                        if ((RTSM_QUERY_MAX_STEPS > 0) && (relatedCi != null) && relatedCi.targetId)
                            setBusinessServicePre921(relatedCi.configurationItem, event.drilldownUrl, builder, activityLog)
                    }

                    if (event.category) {
                        //          builder."${CATEGORY_TAG}"(event.category)
                        builder."${CATEGORY_TAG}"("${ASTELIT_CATEGORY}")
                        activityLog.append('\n').append(ACTIVITY_LOG_CATEGORY).append('\n').append(event.category).append('\n')
                    }
                    if (event.subCategory) {
                        //          builder."${SUB_CATEGORY_TAG}"(event.subCategory)
                        builder."${CATEGORY_TAG}"("${ASTELIT_SUB_CATEGORY}")
                        activityLog.append('\n').append(ACTIVITY_LOG_SUBCATEGORY).append('\n').append(event.subCategory).append('\n')
                    }
                    if (event.application) {
                        activityLog.append('\n').append(ACTIVITY_LOG_APPLICATION).append('\n').append(event.application).append('\n')
                    }
                    if (event.object) {
                        activityLog.append('\n').append(ACTIVITY_LOG_OBJECT).append('\n').append(event.object).append('\n')
                    }
                    // Add 'Original Data' to activity log
                    if (event.originalData) {
                        activityLog.append('\n').append(ACTIVITY_LOG_ORIGINAL_DATA).append('\n').
                                append(event.originalData).append('\n')
                    }
                }

                // Add 'Time OMi Event State Changed' to activity log
                if (event.timeStateChanged
                        && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("time_state_changed"))) {
                    activityLog.append('\n').append(ACTIVITY_LOG_TIME_STATE_CHANGED).append('\n').
                            append(dateFormatter.format(event.timeStateChanged)).append(' : ').append(event.state).append('\n')
                }

                // check title
                if (title
                        && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("title")))
                    builder."${TITLE_TAG}"(title)

                if (event.title
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("title"))) {
                    // synchronize the title to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_TITLE).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_TITLE_CHANGE)
                    activityLog.append(event.title.trim())
                    activityLog.append('\n')
                }

                // check description
                if (description
                        && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("description")))
                    builder."${DESCRIPTION_TAG}"(description)

                if (event.description
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("description"))) {
                    // synchronize the description to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_DESCRIPTION).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_DESCRIPTION_CHANGE)
                    activityLog.append(event.description.trim())
                    activityLog.append('\n')
                }

                // check solution
                if (event.solution
                        && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("solution")))
                    builder."${SOLUTION_TAG}"(event.solution)

                if (event.solution
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("solution"))) {
                    // synchronize the solution to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_SOLUTION).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_SOLUTION_CHANGE)
                    activityLog.append(event.solution.trim())
                    activityLog.append('\n')
                }

                // check assigned user
//		  if (((event.assignedUser?.id >= 0) && (event.assignedUser?.userName || event.assignedUser?.loginName))
//			  && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_user")))
                if ((event.assignedUser?.id >= 0) && (event.assignedUser?.userName || event.assignedUser?.loginName) && isNewIncident)
                    builder."${REQUESTED_BY_TAG}" {
                        builder."${PARTY_TAG}" {
                            if (event.assignedUser?.userName)
                                builder."${UI_NAME_TAG}"(event.assignedUser.userName)
                            if (event.assignedUser?.loginName)
                                builder."${NAME_TAG}"(event.assignedUser.loginName)
                        }
                    }

                // set the contact name to the BSM assigned user
                if (event.assignedUser?.userName
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_user"))) {
                    // synchronize the assigned user to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_ASSIGNED_USER).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_ASSIGNED_USER_CHANGE)
                    if (event.assignedUser?.id < 0)
                        activityLog.append(ACTIVITY_LOG_UNASSIGNED)
                    else
                        activityLog.append(event.assignedUser.userName.trim())
                    activityLog.append('\n')
                }

                // check assigned group
                //if (((event.assignedGroup?.id >= 0) && event.assignedGroup?.name)
                //&& (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_group")))
                if ((event.assignedGroup?.id >= 0) && event.assignedGroup?.name && isNewIncident)
                    builder."${ASSIGNED_GROUP_TAG}" {
                        builder."${FUNCTIONAL_GROUP_TAG}" {
                            builder."${UI_NAME_TAG}"(event.assignedGroup.name)
                            builder."${NAME_TAG}"(event.assignedGroup.name)
                        }
                    }

                // set the functional group name to the BSM assigned group
                if (event.assignedGroup?.name
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_group"))) {
                    // synchronize the assigned user to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE)
                    if (event.assignedGroup?.id < 0)
                        activityLog.append(ACTIVITY_LOG_UNASSIGNED)
                    else
                        activityLog.append(event.assignedGroup.name.trim())
                    activityLog.append('\n')
                }

                // check state
                if (event.state
                        && (isNewIncident
                        || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("state"))
                        && (syncAllOPRStatesToSM || SyncOPRStatesToSM.contains(event.state))))) {
                    String status = MapOPR2SMStatus[event.state]
                    builder."${INCIDENT_STATUS_TAG}"(status)
                    if ("closed".equals(status)) {
                        builder."${COMPLETION_CODE_TAG}"(SMCompletionCode)
                    }
                }

                if (event.state
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("state"))) {
                    // synchronize the state to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_STATE).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_STATE_CHANGE)
                    activityLog.append(event.state.trim())
                    activityLog.append('\n')
                }

                // check urgency/severity
                if (MapOPR2SMUrgency[event.severity] == "1") {
                    astl_urgency = "2"
                }
                if (MapOPR2SMUrgency[event.severity] == "2") {
                    astl_urgency = "3"
                }
                if (MapOPR2SMUrgency[event.severity] == "3") {
                    astl_urgency = "4"
                }
                if (MapOPR2SMUrgency[event.severity] == "4") {
                    astl_urgency = "4"
                }

                if (astl_urgency
                        && (isNewIncident
                        || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("severity"))
                        && (syncAllOPRSeveritiesToSM || SyncOPRSeveritiesToSM.contains(astl_urgency)))))
                    builder."${URGENCY_TAG}"(astl_urgency)

                if (astl_urgency
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("severity"))) {
                    // synchronize the severity to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_SEVERITY).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_SEVERITY_CHANGE)
                    activityLog.append(event.severity)
                    activityLog.append('\n')
                }

                // check priority
                //if (event.priority
                //&& (isNewIncident
                //|| ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("priority"))
                //&& (syncAllOPRPrioritiesToSM || SyncOPRPrioritiesToSM.contains(event.priority)))))
                //builder."${PRIORITY_TAG}"(MapOPR2SMPriority[event.priority])

                if (event.priority
                        && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("priority"))) {
                    // synchronize the priority to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_PRIORITY).append("\n")
                    if (!isNewIncident)
                        activityLog.append(ACTIVITY_LOG_PRIORITY_CHANGE)
                    activityLog.append(event.priority)
                    activityLog.append('\n')
                }

                // set is_recorded_by (opened.by) to "admin" or Control Transfer initiator
                if (isNewIncident) {
                    boolean initiatedBySystem = (event.controlTransferredTo?.initiatedBy == null) ||
                            ("system".equals(event.controlTransferredTo.initiatedBy))
                    String recorder = initiatedBySystem ? BSM_ADMINISTRATOR_LOGIN_NAME : event.controlTransferredTo.initiatedBy
                    builder."${RECORDED_BY_TAG}" {
                        builder."${PERSON_TAG}" {
                            builder."${UI_NAME_TAG}"(recorder)
                            builder."${NAME_TAG}"(recorder)
                        }
                    }
                    // Add initiator info to Activity Log
                    OprForwardingInfo forwardingInfo = event.getForwardingInfo(m_connectedServerId)
                    if (initiatedBySystem && (m_oprVersion >= 910) && forwardingInfo?.ruleName) {
                        activityLog.append('\n').
                                append(ACTIVITY_LOG_INITIATED_BY).append("\n").
                                append(ACTIVITY_LOG_INITIATED_BY_RULE).
                                append(forwardingInfo.ruleName)
                    } else {
                        activityLog.append('\n').
                                append(ACTIVITY_LOG_INITIATED_BY).append("\n").
                                append(ACTIVITY_LOG_INITIATED_BY_USER).
                                append(recorder)
                    }
                    activityLog.append('\n')
                }

                if (event.controlTransferredTo
                        && !m_node.equalsIgnoreCase(event.controlTransferredTo.dnsName)
                        && OprControlTransferStateEnum.transferred.name().equals(event.controlTransferredTo.state)
                        && (syncAllOPRPropertiesToSMActivityLog
                        || SyncOPRPropertiesToSMActivityLog.contains("control_transferred_to"))) {
                    // synchronize the priority to the incident activity log
                    activityLog.append('\n')
                    activityLog.append(ACTIVITY_LOG_CONTROL_TRANSFERRED_TO).append("\n")
                    activityLog.append(event.controlTransferredTo.dnsName).append(":").append(event.controlTransferredTo.state)
                    activityLog.append('\n')
                }

                // check if there are any annotations to add to the activity log
                if ((event.annotations != null)
                        && (isNewIncident
                        || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("annotation"))) {
                    // append any annotations
                    event.annotations?.annotations?.each() { OprAnnotation annotation ->
                        final String text = annotation.text
                        if (text.length() > 0) {
                            final String date = dateFormatter.format(annotation.timeCreated)
                            final String author = annotation.author
                            activityLog.append('\n')
                            activityLog.append(ACTIVITY_LOG_ANNOTATION).append("\n - ${date} - ${author} - ${text}")
                            activityLog.append('\n')
                        }
                    }
                }

                // check if there are any custom attributes to add to the activity log
                if (!m_OPR2SMCustomAttribute.isEmpty() && (event.customAttributes != null)) {
                    event.customAttributes.customAttributes?.each() { OprCustomAttribute customAttribute ->
                        final String caName = customAttribute.name.toLowerCase(LOCALE)

                        if (m_OPR2SMCustomAttribute.containsKey(caName)) {
                            final String smIncidentProperty = m_OPR2SMCustomAttribute.get(caName)
                            // synchronize this CA to SM
                            if (ACTIVITY_LOG_TAG.equals(smIncidentProperty)) {
                                // synchronize the CA to the SM incident activity log
                                activityLog.append('\n')
                                activityLog.append(ACTIVITY_LOG_CA).append("\n${customAttribute.name}=${customAttribute.value}")
                                activityLog.append('\n')
                            } else {
                                // synchronize to the specified SM incident property
                                builder."${smIncidentProperty}"(customAttribute.value)
                            }
                        }
                    }
                }

                String drilldownUrl = event.drilldownUrl
                if (drilldownUrl && drilldownUrl.lastIndexOf('=') > 0)
                    drilldownUrl = drilldownUrl.substring(0, drilldownUrl.lastIndexOf('=') + 1)

                if (event.cause) {
                    if (causeExternalRefId) {
                        if (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("cause")) {
                            builder."${IS_CAUSED_BY}"(target_role: "${IS_CAUSED_BY_ROLE}") {
                                builder."${MASTER_REFERENCE_TAG}"(causeExternalRefId)
                            }
                        }
                        if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
                            // synchronize the SM cause to the incident activity log
                            activityLog.append('\n')
                            activityLog.append(ACTIVITY_LOG_CAUSE).append("\n").append(causeExternalRefId)
                            activityLog.append('\n')
                        }
                    } else if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
                        // synchronize the OPR cause to the incident activity log
                        activityLog.append('\n')
                        final String causeTitle = event.cause.title
                        final String causeUrl = (drilldownUrl) ? drilldownUrl + event.cause.targetId : null
                        if (causeUrl)
                            activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append("${causeTitle}\n\t${causeUrl}")
                        else
                            activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append(causeTitle)
                        activityLog.append('\n')
                    }
                }

                if ((event.symptoms != null)
                        && (isNewIncident
                        || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("symptom"))) {
                    // synchronize the OPR symptom to the incident activity log
                    event.symptoms.eventReferences?.each() { OprSymptomReference symptomRef ->
                        activityLog.append('\n')
                        final String symptomTitle = symptomRef.title
                        final String symptomUrl = (drilldownUrl) ? drilldownUrl + symptomRef.targetId : null
                        if (symptomUrl)
                            activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append("${symptomTitle}\n\t${symptomUrl}")
                        else
                            activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append(symptomTitle)
                        activityLog.append('\n')
                    }
                }

                if (duplicateChange
                        && (isNewIncident
                        || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("duplicate_count"))) {
                    final Integer previousCount = (Integer) duplicateChange.previousValue
                    final Integer currentCount = (Integer) duplicateChange.currentValue

                    if (currentCount > 0) {
                        // synchronize the duplicate count to the incident activity log
                        if (previousCount == null) {
                            activityLog.append('\n')
                            activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n").append(currentCount)
                        } else {
                            activityLog.append('\n')
                            activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n")
                            activityLog.append("${ACTIVITY_LOG_PREVIOUS} ${previousCount} ${ACTIVITY_LOG_CURRENT} ${currentCount}")
                        }
                        activityLog.append('\n')
                    }
                }

                // set any activityLog
                if (activityLog.length() > 0) {
                    builder."${ACTIVITY_LOG_TAG}" {
                        builder."${ACTIVITY_LOG_DESC_TAG}"(activityLog.toString())
                    }
                }
            }
            final String output = writer.toString()
            return output
        }
//##################################### END DEFAULT SECTION #####################################


    }

    private void setBusinessService(OprEvent event, MarkupBuilder builder, StringBuffer activityLog) {
        // The following class and method only exists in 9.21 or greater
        // com.hp.opr.api.ws.model.event.ci.OprAffectsBusinessService affectsService
        def affectsService = event.getMostCriticalAffectsBusinessService(RecursiveSearchBusinessServices)
        if (affectsService != null) {
            // send 'affects_business_service' CI information
            builder."${AFFECTS_RELATIONSHIP}"(target_role: "${BUSINESS_SERVICE_ROLE}") {
                if (affectsService.configurationItem.globalId)
                    builder."${CI_GLOBALID_TAG}"(affectsService.configurationItem.globalId)
                builder."${CI_TARGET_TYPE_TAG}"(CI_TARGET_TYPE)
                builder."${CONFIGURATION_ITEM_TAG}" {
                    if (affectsService.configurationItem.ciType)
                        builder."${CI_TYPE_TAG}"(affectsService.configurationItem.ciType)
                    if (affectsService.configurationItem.id)
                        builder."${CI_ID_TAG}"(affectsService.configurationItem.id)
                    if (affectsService.configurationItem.ciName)
                        builder."${CI_NAME_TAG}"(affectsService.configurationItem.ciName)
                    if (affectsService.configurationItem.ciDisplayLabel)
                        builder."${CI_DISPLAY_LABEL_TAG}"(affectsService.configurationItem.ciDisplayLabel)
                }
            }
            if (event.affectsBusinessServices != null && !event.affectsBusinessServices.isEmpty()) {
                final URL eventUrl = event.drilldownUrl
                activityLog.append('\n')
                activityLog.append(ACTIVITY_LOG_AFFECTS_SERVICES)
                for (def service : event.affectsBusinessServices) {
                    appendBusinessServiceToActivityLog(activityLog, service, eventUrl, '\t')
                }
                activityLog.append('\n')
            }
        }
    }

    private void setBusinessServicePre921(OprConfigurationItem ci, URL eventUrl,
                                          MarkupBuilder builder, StringBuffer activityLog) {
        // The following is for pre 9.21 only. Get the business service CIs
        m_log.debug("Enter setBusinessServicePre921()")
        TopologyQueryService queryService = getQueryService()
        if (queryService == null)
            return

        try {
            QueryDefinition queryDefinition = queryService.
                    getFactory().createQueryDefinition("Get Affects Business Services ${ci.ciType}-${ci.id}")
            boolean isBusinessService = TYPE_BUSINESS_SERVICE.equals(ci.ciType)

            // Key properties & ensure Global ID, Name and Label are included.
            QueryNode queryNode = queryDefinition.addNode("CI").withIdsFromStrings(ci.id)
            if (isBusinessService)
                queryNode.ofType(TYPE_BUSINESS_SERVICE)
            else
                queryNode.ofConfigurationItemType()
            queryNode.queryProperties(ATTR_GLOBAL_ID, ATTR_NAME, ATTR_LABEL, ATTR_BUSINESS_CRITICALITY)

            QueryNode businessServiceNode = queryDefinition.addNode("Business Service").ofType(TYPE_BUSINESS_SERVICE)

            businessServiceNode.queryProperties(ATTR_GLOBAL_ID, ATTR_NAME, ATTR_LABEL, ATTR_BUSINESS_CRITICALITY)

            IndirectLink businessService2me = businessServiceNode.indirectlyLinkedTo(queryNode);
            if (isBusinessService) {
                businessService2me.withStep().from(TYPE_BUSINESS_SERVICE).to(TYPE_BUSINESS_SERVICE).alongTheLink(REL_IMPACT)
                businessService2me.atLeast(1).atMost(QueryLink.UNBOUNDED).showEntirePath()
                businessService2me.withMaxNumberOfStepsMatched(RTSM_QUERY_MAX_STEPS).withTargetCardinality(0, QueryLink.UNBOUNDED)

                QueryNode relBusinessServiceNode = queryDefinition.addNode("B2B Service Related").ofType(TYPE_BUSINESS_SERVICE)

                relBusinessServiceNode.queryProperties(ATTR_GLOBAL_ID, ATTR_NAME, ATTR_LABEL, ATTR_BUSINESS_CRITICALITY)

                IndirectLink businessService2businessService = relBusinessServiceNode.indirectlyLinkedTo(businessServiceNode)
                businessService2businessService.withStep().from(TYPE_BUSINESS_SERVICE).to(TYPE_BUSINESS_SERVICE).alongTheLink(REL_IMPACT)
                businessService2businessService.atLeast(1).atMost(QueryLink.UNBOUNDED).showEntirePath()
                businessService2businessService.withMaxNumberOfStepsMatched(RTSM_QUERY_MAX_STEPS).withTargetCardinality(0, QueryLink.UNBOUNDED)
            } else {
                IndirectLinkStepToPart toPart =
                    businessService2me.withStep().fromConfigurationItemType().toConfigurationItemType()
                toPart.complexTypeConditionsSet().addComplexTypeCondition().withoutType(TYPE_BUSINESS_SERVICE)
                toPart.alongTheLink(REL_IMPACT)
                businessService2me.atLeast(1).atMost(QueryLink.UNBOUNDED)
                businessService2me.withMaxNumberOfStepsMatched(RTSM_QUERY_MAX_STEPS).withTargetCardinality(1, QueryLink.UNBOUNDED)

                QueryNode relBusinessServiceNode =
                    queryDefinition.addNode("Business Service Related").ofType(TYPE_BUSINESS_SERVICE)
                relBusinessServiceNode.queryProperties(ATTR_GLOBAL_ID, ATTR_NAME, ATTR_LABEL, ATTR_BUSINESS_CRITICALITY)

                IndirectLink businessService2businessService = relBusinessServiceNode.indirectlyLinkedTo(businessServiceNode)
                businessService2businessService.withStep().from(TYPE_BUSINESS_SERVICE).to(TYPE_BUSINESS_SERVICE).alongTheLink(REL_IMPACT)
                businessService2businessService.atLeast(1).atMost(QueryLink.UNBOUNDED).showEntirePath()
                businessService2businessService.withMaxNumberOfStepsMatched(RTSM_QUERY_MAX_STEPS).withTargetCardinality(0, QueryLink.UNBOUNDED)
            }

            TopologyCI topCriticalService = null
            int topCriticality = 0
            TopologyCount topologyCount = queryService.evaluateQuery(queryDefinition)
            // Save the query the first time only
            if (SaveTQLQuery) {
                queryDefinition.withBundles(Arrays.asList("integration_tqls_bundle"))
                ucmdbService.getQueryManagementService().saveQuery(queryDefinition)
            }
            int ciCount = topologyCount.getCIsNumber()
            m_log.debug("Query count: ${ciCount}")
            if (ciCount < RTSM_MAX_CI_COUNT) {
                // execute the query
                final Topology topology = queryService.executeQuery(queryDefinition)
                Collection<TopologyCI> cis = topology.getAllCIs()
                m_log.debug("CI count: ${cis.size()}")
                for (final TopologyCI topologyCi in cis) {
                    if ("business_service".equals(topologyCi.getType())) {
                        int criticality = (Integer) topologyCi.getPropertyValue("business_criticality")
                        if (topCriticalService == null || criticality > topCriticality) {
                            topCriticalService = topologyCi
                            topCriticality = (Integer) topologyCi.getPropertyValue("business_criticality")
                        }
                    }
                }
            } else {
                m_log.debug("Number of affected Business Services for related CI with id ${ci.id} exceeds ${RTSM_MAX_CI_COUNT}.")
                return
            }

            if (topCriticalService != null) {
                final String serviceId = topCriticalService.id.asString
                final String serviceGlobalId = getIdAsString(topCriticalService, "global_id")
                final String serviceName = topCriticalService.getProperty("name")?.value?.toString()
                final String serviceLabel = topCriticalService.getProperty("display_label")?.value?.toString()

                // send 'affects_business_service' CI information
                builder."${AFFECTS_RELATIONSHIP}"(target_role: "${BUSINESS_SERVICE_ROLE}") {
                    if (serviceGlobalId)
                        builder."${CI_GLOBALID_TAG}"(serviceGlobalId)
                    builder."${CI_TARGET_TYPE_TAG}"(CI_TARGET_TYPE)
                    builder."${CONFIGURATION_ITEM_TAG}" {
                        builder."${CI_TYPE_TAG}"(topCriticalService.type)
                        if (serviceId)
                            builder."${CI_ID_TAG}"(serviceId)
                        if (serviceName)
                            builder."${CI_NAME_TAG}"(serviceName)
                        if (serviceLabel)
                            builder."${CI_DISPLAY_LABEL_TAG}"(serviceLabel)
                    }
                }
                activityLog.append('\n')
                activityLog.append(ACTIVITY_LOG_AFFECTS_SERVICES)
                activityLog.append('\n').append('\t').append("${serviceName} : ${topCriticality}")
                if (eventUrl && serviceId) {
                    final URL ciUrl = new URL(eventUrl.protocol, eventUrl.host, eventUrl.port,
                            "${BSM_CI_DRILLDOWN_PATH}${serviceId}")
                    activityLog.append(" : ${ciUrl}")
                }
                activityLog.append('\n')
            } else
                m_log.debug("No top critical business service located.")
        }

        catch (Exception e) {
            // try to re-open the connection
            String details = e.class.canonicalName + (e.message ? ": ${e.message}." : "")
            m_log.error("Attempt to query the RTSM on server ${RTSM_HOSTNAME} failed. Error details: ${details}\n", e)
        }
    }

    private String getDnsName(OprEvent event) {
        OprNodeReference nodeRef = event.node
        OprRelatedCi relatedCi = event.relatedCi

        if (nodeRef?.node?.any == null || nodeRef.node.any.empty) {
            return (relatedCi?.configurationItem != null) ?
                relatedCi.configurationItem.getProperty(NODE_DNS_NAME_TAG) : null
        } else {
            if (m_oprVersion > 913)
                return nodeRef.node.getProperty(NODE_DNS_NAME_TAG)
            else {
                for (def prop in nodeRef.node.any) {
                    if (prop instanceof JAXBElement) {
                        final JAXBElement<?> jaxbElement = (JAXBElement<?>) prop
                        if (NODE_DNS_NAME_TAG.equals(jaxbElement.name?.localPart))
                            return (jaxbElement.value ? jaxbElement.value : null)
                    }
                }
            }
        }
        return null
    }

    private String getIdAsString(TopologyCI ci, String propertyName) {
        def propValue = "id".equals(propertyName) ? ci.id : ci.getProperty(propertyName)?.value
        if (!propValue)
            return null

        if (propValue instanceof String) {
            String value = ((String) propValue).trim()
            return (value) ? value : null
        } else if (propValue instanceof byte[]) {
            TopologyUpdateFactory topologyUpdateFactory = ucmdbService?.topologyUpdateService?.factory
            return (topologyUpdateFactory == null) ?
                null : topologyUpdateFactory.restoreCIIdFromBytes((byte[]) propValue).asString
        } else if (propValue instanceof UcmdbId) {
            return ((UcmdbId) propValue).asString
        } else {
            m_log.error("Unexpected object type for UCMDB ID: " + propValue?.class?.canonicalName)
            return null
        }
    }

// com.hp.opr.api.ws.model.event.ci.OprAffectsBusinessService  affectsService
    private void appendBusinessServiceToActivityLog(StringBuffer activityLog, def affectsService,
                                                    URL eventUrl, String indent) {
        if (affectsService.configurationItem.ciDisplayLabel) {
            String name = affectsService.configurationItem.ciDisplayLabel
            int criticality = affectsService.businessCriticality
            activityLog.append('\n').append(indent).append("${name} : ${criticality}")
            if (eventUrl && affectsService.configurationItem.id) {
                final URL ciUrl = new URL(eventUrl.protocol, eventUrl.host, eventUrl.port,
                        "${BSM_CI_DRILLDOWN_PATH}${affectsService.configurationItem.id}")
                activityLog.append(" : ${ciUrl}")
            }
        }

        List services = affectsService.configurationItem?.affectsBusinessServices
        if (services != null && !services.isEmpty()) {
            for (def nextService : services) {
                appendBusinessServiceToActivityLog(activityLog, nextService, eventUrl, indent + '\t')
            }
        }
    }

    private void getCookies(final ClientResponse response, final Set<Cookie> cookies) {
        m_log.debug("Getting Cookies")
        final MultivaluedMap<String, String> headers = response?.headers
        m_log.debug("HTTP Header count: " + (headers == null ? "<null>" : headers.size()))

        if (headers != null && !headers.isEmpty()) {
            final Map<String, Cookie> cookieMap = new HashMap<String, Cookie>()
            for (final Cookie c : cookies)
                cookieMap.put(c.name, c)
            headers.each() { Map.Entry<String, List<String>> header ->
                if (header.key != null)
                    m_log.debug("Header: " + header.key + ": " + header.value)
                if (SET_COOKIE_HEADER.equalsIgnoreCase(header.key)) {
                    header.value.each() { String value ->
                        if (value != null && value.trim().length() > 0) {
                            try {
                                final Cookie cookie = Cookie.valueOf(value)
                                cookieMap.put(cookie.name, cookie)
                                if (m_log.isDebugEnabled())
                                    m_log.debug("Cookie added: ${cookie.name}=${cookie.value}")
                            }
                            catch (IllegalArgumentException e) {
                                // ignore this entry
                                m_log.debug("Invalid Cookie ignored: ${value}")
                            }
                        }
                    }
                }
            }
            cookies.clear()
            cookies.addAll(cookieMap.values())
        }
    }

    private void checkPingResponse(final ClientResponse response) {
        if (response.getStatusCode() > 299) {
            final String message = response.getEntity(String.class)
            // Workaround for older SM versions that do not have the fix: QCCR1E65559
            if (response.getStatusCode() == 500 && message && message.contains("DAOServerException: No record found."))
                return

            checkResponse(response)
        }
    }

    private void checkResponse(final ClientResponse response) {
        if (response.getStatusCode() > 299) {
            final String message = response.getEntity(String.class)
            if (message)
                m_log.error("HTTP error response - ${response.getMessage()} (${response.getStatusCode()}): ${message}")
            else
                m_log.error("HTTP error response - ${response.getMessage()} (${response.getStatusCode()})")
            throw new ClientWebException((ClientRequest) null, response)
        }
    }

/**
 * create a request
 *
 * @param protocol - Request protocol such as http
 * @param node - The node name od the external process, if any.
 * @param port - The port number of the external process, if any
 * @param path - resource path
 * @param credentials - The login credentials
 * @param cookies - any cookies to set on this request
 * @return requested resource or null
 *
 */
    private Resource createRequest(final String protocol,
                                   final String node,
                                   final Integer port,
                                   final String path,
                                   final PasswordAuthentication credentials,
                                   final Set<Cookie> cookies) {
        final String address = "${protocol}://${node}:${port}${path}"

        if (m_log.isDebugEnabled())
            m_log.debug("Creating request for: ${address}")

        // create the resource instance to interact with
        final Resource resource = m_client.resource(address)

        // SM requires as media type application/atom+xml
        resource.accept(MediaType.APPLICATION_ATOM_XML).contentType(MediaType.APPLICATION_ATOM_XML)
        if (credentials != null) {
            // Set the username and password in the request.
            byte[] encodedUserPassword = Base64.encodeBase64((credentials.getUserName()
                    + ":" + new String(credentials.getPassword())).getBytes())
            resource.header("Authorization", "Basic " + new String(encodedUserPassword))
        }

        // Set any cookies saved from last request
        if (cookies != null && !cookies.isEmpty()) {
            for (Cookie cookie : cookies)
                resource.cookie(cookie)
        }
        return resource
    }

}