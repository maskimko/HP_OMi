package emergency;

import com.hp.opr.api.Version;
import com.hp.opr.api.ws.adapter.*;
import com.hp.opr.api.ws.model.event.*;
import com.hp.opr.api.ws.model.event.ci.*;
import com.hp.opr.api.ws.model.event.property.*;
import com.hp.opr.common.ws.client.WinkClientSupport;
import com.hp.ucmdb.api.UcmdbService;
import com.hp.ucmdb.api.UcmdbServiceFactory;
import com.hp.ucmdb.api.UcmdbServiceProvider;
import com.hp.ucmdb.api.topology.*;
import com.hp.ucmdb.api.topology.indirectlink.IndirectLink;
import com.hp.ucmdb.api.topology.indirectlink.IndirectLinkStepToPart;
import com.hp.ucmdb.api.types.Property;
import com.hp.ucmdb.api.types.TopologyCI;
import com.hp.ucmdb.api.types.UcmdbId;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.Reference;
import groovy.util.MapEntry;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.MarkupBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.wink.client.*;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceManagerAdapter {


    public static Map<String, String>  map;
    public final int getRTSM_PORT() {
        return RTSM_PORT;
    }

    public final int getRTSM_MAX_CI_COUNT() {
        return RTSM_MAX_CI_COUNT;
    }

    public final String getRTSM_HOSTNAME() {
        return RTSM_HOSTNAME;
    }

    public final String getRTSM_USERNAME() {
        return RTSM_USERNAME;
    }

    public final String getRTSM_PASSWORD() {
        return RTSM_PASSWORD;
    }

    public final int getRTSM_QUERY_MAX_STEPS() {
        return RTSM_QUERY_MAX_STEPS;
    }

    public final int getMAX_DUPLICATE_COUNT_UPDATES() {
        return MAX_DUPLICATE_COUNT_UPDATES;
    }

    public static final String SM_WEB_TIER_NAME = "sm";
    public static final String BSM_ADMINISTRATOR_LOGIN_NAME = "admin";
    private final int RTSM_PORT = 80;
    private final int RTSM_MAX_CI_COUNT = 1000;
    private final String RTSM_HOSTNAME = "localhost";
    private final String RTSM_USERNAME = "";
    private final String RTSM_PASSWORD = "";
    private final int RTSM_QUERY_MAX_STEPS = 10;
    private final int MAX_DUPLICATE_COUNT_UPDATES = 10;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(4);
        map.put("open", "open");
        map.put("in_progress", "work-in-progress");
        map.put("resolved", "resolved");
        map.put("closed", "resolved");

    }

    public static final Map MapOPR2SMStatus = map;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(16);
        map.put("accepted", "open");
        map.put("assigned", "open");
        map.put("open", "open");
        map.put("reopened", "open");
        map.put("pending-change", "in_progress");
        map.put("pending-customer", "in_progress");
        map.put("pending-other", "in_progress");
        map.put("pending-vendor", "in_progress");
        map.put("referred", "in_progress");
        map.put("suspended", "in_progress");
        map.put("work-in-progress", "in_progress");
        map.put("rejected", "resolved");
        map.put("replaced-problem", "resolved");
        map.put("resolved", "closed");
        map.put("cancelled", "resolved");
        map.put("closed", "closed");

    }

    public static final Map MapSM2OPRState = map;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(6);
        map.put("critical", "1");
        map.put("major", "2");
        map.put("minor", "3");
        map.put("warning", "3");
        map.put("normal", "4");
        map.put("unknown", "4");

    }

    public static final Map MapOPR2SMUrgency = map;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(4);
        map.put("1", "critical");
        map.put("2", "major");
        map.put("3", "minor");
        map.put("4", "normal");

    }

    public static final Map MapSM2OPRSeverity = map;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(6);
        map.put("highest", "1");
        map.put("high", "2");
        map.put("medium", "3");
        map.put("low", "4");
        map.put("lowest", "4");
        map.put("none", "4");

    }

    public static final Map MapOPR2SMPriority = map;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(4);
        map.put("1", "highest");
        map.put("2", "high");
        map.put("3", "medium");
        map.put("4", "low");

    }

    public static final Map MapSM2OPRPriority = map;
    public static final String SMCompletionCode = "Automatically Closed";
    /**
     * We change it in hope to disable updating unwanted properties after incdent has been opened
     * 19 november 2013 18:42
     * Switched back at 19:11   as it disables correct behaviour of astl_operational_device flag
     */
    public static final boolean SyncAllProperties = true;
    public static final boolean UseNodeCI = false;
    public static final boolean RecursiveSearchBusinessServices = true;
    public static final boolean DisableCheckCloseOnUpdate = false;
    public static final Set SyncOPRPropertiesToSM = new TreeSet(Arrays.asList("state", "solution", "cause", "custom_attribute", "operational_device", "event_addon"));
    public static final Set SyncOPRPropertiesToSMActivityLog = new TreeSet(Arrays.asList("title", "description", "state", "severity", "priority", "annotation", "duplicate_count", "cause", "symptom", "assigned_user", "assigned_group", "event_addon", "CustomAlertID", "CustomMgmtGrp", "CustomTitle", "CustomPriority"));
    public static final Set SyncSMPropertiesToOPR = new TreeSet(Arrays.asList("incident_status", "solution", "operational_device", "event_addon", "custom_attribute"));
    public static final Set SyncOPRStatesToSM = new TreeSet(Arrays.asList("closed"));
    public static final Set SyncOPRSeveritiesToSM = new TreeSet(Arrays.asList("*"));
    public static final Set SyncOPRPrioritiesToSM = new TreeSet(Arrays.asList("*"));
    public static final Set SyncSMStatusToOPR = new TreeSet(Arrays.asList("closed"));
    public static final Set SyncSMUrgenciesToOPR = new TreeSet(Arrays.asList("*"));
    public static final Set SyncSMPrioritiesToOPR = new TreeSet(Arrays.asList("*"));


    {
        map = new LinkedHashMap<String, String>(6);
        map.put("operational_device", "OperationalDevice");
        map.put("event_addon", "EventAddon");
        map.put("CustomAlertID", "CustomAlertID");
        map.put("CustomMgmtGrp", "CustomMgmtGrp");
        map.put("CustomTitle", "CustomTitle");
        map.put("CustomPriority", "CustomPriority");

    }

    public static final Map<String, String> MapOPR2SMCustomAttribute = map;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(6);
        map.put("OperationalDevice", "operational_device");
        map.put("EventAddon", "event_addon");
        map.put("CustomAlertID", "CustomAlertID");
        map.put("CustomMgmtGrp", "CustomMgmtGrp");
        map.put("CustomTitle", "CustomTitle");
        map.put("CustomPriority", "CustomPriority");

    }

    public static final Map<String, String> MapSM2OPRCustomAttribute = map;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(4);
        map.put("1", "1 - Critical");
        map.put("2", "2 - High");
        map.put("3", "3 - Average");
        map.put("4", "4 - Low");

    }

    public static final Map SMUrgency = map;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(4);
        map.put("1", "1 - Critical");
        map.put("2", "2 - High");
        map.put("3", "3 - Average");
        map.put("4", "4 - Low");

    }

    public static final Map SMPriority = map;
    public static final Locale LOCALE = Locale.getDefault();
    public static final String ANNOTATION_DATE_FORMAT = "yyyy.MM.dd HH:mm:ss z";
    public static final String EMPTY_DESCRIPTION_OVERRIDE = "<none>";
    public String astl_operational_device = "false";
    public static final String ACTIVITY_LOG_TITLE = "[Title]";
    public static final String ACTIVITY_LOG_TITLE_CHANGE = "Event title changed to: ";
    public static final String ACTIVITY_LOG_STATE = "[State]";
    public static final String ACTIVITY_LOG_STATE_CHANGE = "Event state changed to: ";
    public static final String ACTIVITY_LOG_DESCRIPTION = "[Description]";
    public static final String ACTIVITY_LOG_DESCRIPTION_CHANGE = "Event description changed to: ";
    public static final String ACTIVITY_LOG_SOLUTION = "[Solution]";
    public static final String ACTIVITY_LOG_SOLUTION_CHANGE = "Event solution changed to: ";
    public static final String ACTIVITY_LOG_ASSIGNED_USER = "[Assigned User]";
    public static final String ACTIVITY_LOG_ASSIGNED_USER_CHANGE = "Event assigned user changed to: ";
    public static final String ACTIVITY_LOG_ASSIGNED_GROUP = "[Assigned Group]";
    public static final String ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE = "Event assigned group changed to: ";
    public static final String ACTIVITY_LOG_UNASSIGNED = "<unassigned>";
    public static final String ACTIVITY_LOG_SEVERITY = "[Severity]";
    public static final String ACTIVITY_LOG_SEVERITY_CHANGE = "Event severity changed to: ";
    public static final String ACTIVITY_LOG_PRIORITY = "[Priority]";
    public static final String ACTIVITY_LOG_PRIORITY_CHANGE = "Event priority changed to: ";
    public static final String ACTIVITY_LOG_CONTROL_TRANSFERRED_TO = "[Control Transferred To]";
    public static final String ACTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED = "Event control transfer state changed to: ";
    public static final String ACTIVITY_LOG_CATEGORY = "[Category]";
    public static final String ACTIVITY_LOG_SUBCATEGORY = "[Subcategory]";
    public static final String ACTIVITY_LOG_APPLICATION = "[Application]";
    public static final String ACTIVITY_LOG_OBJECT = "[Object]";
    public static final String ACTIVITY_LOG_ANNOTATION = "[Annotation]";
    public static final String ACTIVITY_LOG_CA = "[Custom Attribute]";
    public static final String ACTIVITY_LOG_CAUSE = "[Cause]";
    public static final String ACTIVITY_LOG_OMI_CAUSE = "[OMi Cause]";
    public static final String ACTIVITY_LOG_OMI_SYMPTOM = "[OMi Symptom]";
    public static final String ACTIVITY_LOG_DUPLICATE_COUNT = "[Duplicate Count]";
    public static final String ACTIVITY_LOG_PREVIOUS = "previous";
    public static final String ACTIVITY_LOG_CURRENT = "current";
    public static final String ACTIVITY_LOG_INITIATED_BY = "[Initiated by]";
    public static final String ACTIVITY_LOG_INITIATED_BY_RULE = "BSM forwarding rule: ";
    public static final String ACTIVITY_LOG_INITIATED_BY_USER = "BSM operator: ";
    public static final String ACTIVITY_LOG_RELATED_CI = "[BSM Related CI]";
    public static final String ACTIVITY_LOG_RELATED_CI_TYPE_LABEL = "Type label: ";
    public static final String ACTIVITY_LOG_RELATED_CI_TYPE = "Type: ";
    public static final String ACTIVITY_LOG_RELATED_CI_LABEL = "Display label: ";
    public static final String ACTIVITY_LOG_RELATED_CI_NAME = "Name: ";
    public static final String ACTIVITY_LOG_RELATED_CI_HOSTED_ON = "Hosted on: ";
    public static final String ACTIVITY_LOG_RELATED_CI_URL = "Cross launch URL: ";
    public static final String ACTIVITY_LOG_AFFECTS_SERVICES = "[BSM Affects Business Services (name : criticality)]";
    public static final String ACTIVITY_LOG_TIME_RECEIVED = "[Time OMi Received Event]";
    public static final String ACTIVITY_LOG_TIME_CREATED = "[Time OMi Event Created]";
    public static final String ACTIVITY_LOG_TIME_STATE_CHANGED = "[Time OMi Event State Changed]";
    public static final String ACTIVITY_LOG_ORIGINAL_DATA = "[Original Data]";
    public static final String ACTIVITY_LOG_OPERATIONAL_DATA = "[CI is operational]";
    public UcmdbServiceProvider ucmdbProvider = null;
    public UcmdbService ucmdbService = null;
    public static final boolean SaveTQLQuery = false;
    public static final Boolean SpecifyActiveProcess = true;
    public static final Boolean SpecifyImpactScope = true;
    public static final String DRILLDOWN_ROOT_PATH = "/" + SM_WEB_TIER_NAME + "/index.do?ctx=docEngine&file=probsummary&query=number%3D";
    public static final String OMI_ROOT_DRILLDOWN_PATH = "/opr-console/opr-evt-details.jsp?eventId=";
    public static final String BSM_CI_DRILLDOWN_PATH = "/topaz/dash/nodeDetails.do?cmdbId=";
    public static final String ROOT_PATH = "/SM/7/rest/1.1/incident_list";
    public static final String PING_QUERY = "reference_number='IM10001'";
    public static final String INCIDENT_PATH = ROOT_PATH + "/reference_number/";
    public static final String CUSTOM_PROPERTIES_FILE = "/conf/opr/integration/sm/custom.properties";
    public static final String INCIDENT_XML_NAMESPACE = "http://www.hp.com/2009/software/data_model";
    public static final String INCIDENT_TAG = "incident";
    public static final String TITLE_TAG = "name";
    public static final String DESCRIPTION_TAG = "description";
    public static final String REFERENCE_NUMBER_TAG = "reference_number";
    public static final String INCIDENT_STATUS_TAG = "incident_status";
    public static final String COMPLETION_CODE_TAG = "completion_code";
    public static final String URGENCY_TAG = "urgency";
    public static final String PRIORITY_TAG = "priority";
    public static final String SOLUTION_TAG = "solution";
    public static final String OWNER_TAG = "is_owned_by";
    public static final String ASSIGNED_TAG = "has_assigned";
    public static final String ASSIGNED_GROUP_TAG = "has_assigned_group";
    public static final String FUNCTIONAL_GROUP_TAG = "functional_group";
    public static final String RECORDED_BY_TAG = "is_recorded_by";
    public static final String REQUESTED_BY_TAG = "is_requested_by";
    public static final String PARTY_TAG = "party";
    public static final String PERSON_TAG = "person";
    public static final String UI_NAME_TAG = "display_label";
    public static final String NAME_TAG = "name";
    public static final String EXTERNAL_PROCESS_ID_TAG = "external_process_reference";
    public static final String IMPACT_SCOPE_TAG = "impact_scope";
    public static final String CONFIGURATION_ITEM_TAG = "configuration_item";
    public static final String CATEGORY_TAG = "category";
    public static final String SUB_CATEGORY_TAG = "sub_category";
    public static final String CI_RELATIONSHIP = "is_registered_for";
    public static final String CI_TARGET_TYPE_TAG = "target_type";
    public static final String CI_GLOBALID_TAG = "target_global_id";
    public static final String CI_TYPE_TAG = "type";
    public static final String CI_ID_TAG = "id";
    public static final String CI_NAME_TAG = "name";
    public static final String CI_DISPLAY_LABEL_TAG = "display_label";
    public static final String AFFECTS_RELATIONSHIP = "affects_business_service";
    public static final String NODE_RELATIONSHIP = "is_hosted_on";
    public static final String NODE_DNS_NAME_TAG = "primary_dns_name";
    public static final String ACTIVITY_LOG_TAG = "activity_log";
    public static final String ACTIVITY_LOG_DESC_TAG = "description";
    public static final String ACTIVITY_LOG_ANNO_TAG = "annotations";
    public static final String IS_CAUSED_BY = "is_caused_by";
    public static final String MASTER_REFERENCE_TAG = "master_reference_number";
    public static final String OPERATIONAL_DEVICE_TAG = "operational_device";
    public static final String IS_CAUSED_BY_ROLE = "urn:x-hp:2009:software:data_model:relationship:incident:is_caused_by:incident";
    public static final String IMPACT_LABEL_VALUE = "Enterprise";
    public static final String IT_PROCESS_CATEGORY = "incident";
    public static final String INCIDENT_TYPE = "incident";
    public static final String IMPACT_SCOPE = "site-dept";
    public static final String INCIDENT_XML_VERSION = "1.1";
    public static final String INCIDENT_XML_TYPE = "urn:x-hp:2009:software:data_model:type:incident";
    public static final String CI_TARGET_TYPE = "urn:x-hp:2009:software:data_model:type:configuration_item";
    public static final String CONFIGURATION_ITEM_ROLE = INCIDENT_XML_TYPE + ":is_registered_for:configuration_item";
    public static final String BUSINESS_SERVICE_ROLE = INCIDENT_XML_TYPE + ":affects_business_service:business_service";
    public static final String NODE_ITEM_ROLE = CI_TARGET_TYPE + ":is_hosted_on:node";
    public static final String INCIDENT_XML_RELATIONSHIPS = "false";
    public static final String TYPE_BUSINESS_SERVICE = "business_service";
    public static final String REL_IMPACT = "impact_link";
    public static final String ATTR_GLOBAL_ID = "global_id";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_LABEL = "display_label";
    public static final String ATTR_BUSINESS_CRITICALITY = "business_criticality";
    public static final String SET_COOKIE_HEADER = "Set-Cookie";
    public static final String ASTELIT_CATEGORY = "Auto";
    public static final String ASTELIT_SUB_CATEGORY = "Auto";
    public final SimpleDateFormat dateFormatter = new SimpleDateFormat(ANNOTATION_DATE_FORMAT, LOCALE);
    public final Map<String, String> m_idMap1 = new HashMap<String, String>();
    public final Map<String, String> m_idMap2 = new HashMap<String, String>();
    public Boolean m_useMap1 = true;
    public String m_connectedServerId = null;
    public String m_connectedServerName = null;
    public String m_connectedServerDisplayName = null;
    public X509Certificate m_connectedServerCertificate = null;
    public Integer m_timeout = null;
    public RestClient m_client = null;
    public String m_protocol = "http";
    public String m_node = "localhost";
    public Integer m_port = 13080;
    public String m_home = "";
    public Integer m_oprVersion = 0;
    public Set<Cookie> m_smCookies = new HashSet<Cookie>();
    public Set m_oprSyncProperties = new TreeSet();
    public Set m_smSyncProperties = new TreeSet<String>(Arrays.asList(REFERENCE_NUMBER_TAG));
    public Properties m_properties = new Properties();
    public final Map<String, String> m_OPR2SMCustomAttribute = new LinkedHashMap<String, String>();
    public final boolean syncAllOPRPropertiesToSM = SyncAllProperties || SyncOPRPropertiesToSM.contains("*");
    public final boolean syncAllOPRPropertiesToSMActivityLog = SyncAllProperties || SyncOPRPropertiesToSMActivityLog.contains("*");
    public final boolean syncAllOPRStatesToSM = SyncAllProperties || SyncOPRStatesToSM.contains("*");
    public final boolean syncAllOPRSeveritiesToSM = SyncAllProperties || SyncOPRSeveritiesToSM.contains("*");
    public final boolean syncAllOPRPrioritiesToSM = SyncAllProperties || SyncOPRPrioritiesToSM.contains("*");
    public final boolean syncAllSMPropertiesToOPR = SyncAllProperties || SyncSMPropertiesToOPR.contains("*");
    public final boolean syncAllSMStatusToOPR = SyncAllProperties || SyncSMStatusToOPR.contains("*");
    public final boolean syncAllSMUrgenciesToOPR = SyncAllProperties || SyncSMUrgenciesToOPR.contains("*");
    public final boolean syncAllSMPrioritiesToOPR = SyncAllProperties || SyncSMPrioritiesToOPR.contains("*");
    public final boolean syncCheckForClose = (!DisableCheckCloseOnUpdate && (syncAllSMPropertiesToOPR || SyncSMPropertiesToOPR.contains("incident_status") && (syncAllSMStatusToOPR || SyncSMStatusToOPR.contains("closed"))));
    public Log m_log;








    /**
     * This method returns CI logical name
     * according to ASTELIT rules
     *
     * @param currentEvent           source event of CI
     * @param currentAstlLogicalName CI name that has been modified by ASTELIT custom rules
     * @param eventLog               logger to log method processing
     * @param lineNumber             Put here line number just for reference in code
     * @return CI name
     */
    private String ciResolver(OprEvent currentEvent, String currentAstlLogicalName, Log eventLog, int lineNumber) {


        if (eventLog.isDebugEnabled()) {
            eventLog.debug("Diving into ciResolver method from line number " + lineNumber);
        }

        String ciNameToReturn = null;

        OprConfigurationItem currentCi = currentEvent.getRelatedCi().getConfigurationItem();
        String currentCiName = currentCi.getCiName();


        String fqdn = getDnsName(currentEvent);

        if (eventLog.isDebugEnabled()) {
            eventLog.debug("We got CI name " + currentAstlLogicalName + " from node fqdn " + fqdn);
            eventLog.debug("Determined CI was " + currentCiName);
        }


        if (currentAstlLogicalName != null) {
            if (DefaultGroovyMethods.contains(fqdn, currentAstlLogicalName)) {
                ciNameToReturn = fqdn;
            } else {
                ciNameToReturn = currentAstlLogicalName;

            }

        } else {
            ciNameToReturn = fqdn;
        }

        if (eventLog.isDebugEnabled()) {
            if (!currentCiName.equals(currentAstlLogicalName)) {
                eventLog.debug("So CI has been remapped");
            }

            eventLog.debug("And we choose " + ciNameToReturn);
        }


        return ciNameToReturn;

    }

    private void debugOprEvent(OprEvent event, Log eventDebugLog, int lineNumber) {
        eventDebugLog.debug("Getting custom attributes from event (line number " + lineNumber + ")");
        List<OprCustomAttribute> debugEventCustomAttributeList = event.getCustomAttributes().getCustomAttributes();
        for (OprCustomAttribute debugCustAttrItem : debugEventCustomAttributeList) {
            eventDebugLog.debug("Contains attribute name: " + debugCustAttrItem.getName() + " and value: " + debugCustAttrItem.getValue());
        }

    }

    private void debugForwardEvent(ForwardChangeArgs fca, Log eventDebugLog, int lineNumber) {
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
        Iterator<Map.Entry<QName, Object>> changeAttributesIterator = DefaultGroovyMethods.iterator(changeAttributes);
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


        if (m_log.isDebugEnabled()) {
            m_log.debug("Diving into addCustomAttribute method");
        }


        try {
            if (event.getCustomAttributes() == null) {
                if (m_log.isDebugEnabled()){
                    m_log.debug("Event doesn't have custom attributes");
                }
                customAttributeList = new OprCustomAttributeList();
            } else {
                customAttributeList = event.getCustomAttributes();
                if (m_log.isDebugEnabled()) {
                    m_log.debug("Discovering event with custom attributes");
                    debugOprEvent(event, m_log, 604);
                }

                Iterator<OprCustomAttribute> existedAttrIterator = customAttributeList.getCustomAttributes().iterator();
                while (existedAttrIterator.hasNext()) {
                    attributeArrayList.add(existedAttrIterator.next());
                }

            }


            Iterator<Map.Entry<String,String>> attributeIterator = customAttributes.entrySet().iterator();

            while (attributeIterator.hasNext()) {
                Map.Entry<String, String> pair = attributeIterator.next();
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

                    if (m_log.isDebugEnabled()){
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
                m_log.debug("Post adding custom attribute checking section Start");
                Iterator<OprCustomAttribute> checkingIterator = event.getCustomAttributes().getCustomAttributes().iterator();
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

    public synchronized void init(final InitArgs args) throws IOException {

        // ensure OPR title & description are synchronized together
        if (SyncOPRPropertiesToSM.contains("title") && !SyncOPRPropertiesToSM.contains("description"))
            SyncOPRPropertiesToSM.add("description");
        if (SyncOPRPropertiesToSM.contains("description") && !SyncOPRPropertiesToSM.contains("title"))
            SyncOPRPropertiesToSM.add("title");

        // collect the names of the OPR event properties to synchronize to SM
        m_oprSyncProperties.addAll(SyncOPRPropertiesToSM);
        m_oprSyncProperties.addAll(SyncOPRPropertiesToSMActivityLog);

        // ensure SM name & description are synchronized together
        if (SyncSMPropertiesToOPR.contains("name") && !SyncSMPropertiesToOPR.contains("description"))
            SyncSMPropertiesToOPR.add("description");
        if (SyncSMPropertiesToOPR.contains("description") && !SyncSMPropertiesToOPR.contains("name"))
            SyncSMPropertiesToOPR.add("name");

        // collect the names of the SM incident properties to synchronize to OMi
        m_smSyncProperties.addAll(SyncSMPropertiesToOPR);
        m_smSyncProperties.addAll(MapSM2OPRCustomAttribute.keySet());

        //TODO start to use variables instead of direct mapping

        // shift all CA names to lowercase
        DefaultGroovyMethods.each(MapOPR2SMCustomAttribute.entrySet(), new Closure<String>(this, this) {
            public String doCall(Map.Entry<String, String> entry) {
                final String name = entry.getKey().toLowerCase(LOCALE);
                final String value = entry.getValue();
                return m_OPR2SMCustomAttribute.put(name, value);
            }

        });

        m_log = args.getLogger();

        m_connectedServerId = args.getConnectedServerId();
        m_connectedServerName = args.getConnectedServerName();
        m_connectedServerDisplayName = args.getConnectedServerDisplayName();
        m_connectedServerCertificate = args.getConnectedServerCertificate();
        m_timeout = args.getMaxTimeout() == null ? 60 : args.getMaxTimeout();
        m_protocol = args.isNodeSsl() ? "https" : "http";
        m_node = args.getNode();
        m_port = (args.getPort() == null || args.getPort() < 1) ? (args.isNodeSsl() ? 13443 : 13080) : args.getPort();
        m_home = args.getInstallDir();

        m_client = WinkClientSupport.getRestClient(m_timeout, m_connectedServerCertificate);

        // determine the OPR version that is running
        final String versionString = Version.getProperty(Version.COMPONENT_VERSION);
        String[] version = versionString.split("\\.");
        m_oprVersion = (Integer.valueOf(version[0]) * 100) + Integer.valueOf(version[1]);

        if (m_oprVersion < 921) initUcmdbConnection();

        // load any custom properties
        final File customPropertiesFile = new File(m_home, CUSTOM_PROPERTIES_FILE);
        if (customPropertiesFile.canRead()) m_properties.load(new FileReader(customPropertiesFile));

        if (m_log.isInfoEnabled()) {
            StringBuilder initMsg = new StringBuilder();
            initMsg.append("Service Manager Adapter initialization");
            initMsg.append("\n\tConnected Server ID: " + m_connectedServerId);
            initMsg.append("\n\tConnected Server Name: " + m_connectedServerName);
            initMsg.append("\n\tConnected Server Display Name: " + m_connectedServerDisplayName);
            initMsg.append("\n\tMaximum Timeout in milliseconds: " + String.valueOf(m_timeout));
            initMsg.append("\n\tProtocol: " + m_protocol);
            initMsg.append("\n\tNode: " + m_node);
            initMsg.append("\n\tPort: " + String.valueOf(m_port));
            initMsg.append("\nService Manager Adapter initialized");
            ((Log) m_log).info(initMsg.toString());
        }


        ((Log) m_log).info("Service Manager Adapter initalized. INSTALL_DIR=" + args.getInstallDir() + ", OMi version=" + versionString);
    }

    private synchronized void initUcmdbConnection() {
        if (!DefaultGroovyMethods.asBoolean(RTSM_HOSTNAME) || !DefaultGroovyMethods.asBoolean(RTSM_USERNAME) || !DefaultGroovyMethods.asBoolean(RTSM_PASSWORD))
            return;


        try {
            ucmdbProvider = UcmdbServiceFactory.getServiceProvider(RTSM_HOSTNAME, RTSM_PORT);
            ucmdbService = ucmdbProvider.connect(ucmdbProvider.createCredentials(RTSM_USERNAME, RTSM_PASSWORD), ucmdbProvider.createClientContext(this.getClass().getName()));
        } catch (Exception e) {
            // try to re-open the connection
            final String details = e.getClass().getCanonicalName() + (DefaultGroovyMethods.asBoolean(e.getMessage()) ? ": " + e.getMessage() + "." : "");
            m_log.error("Attempt to connect to RTSM on server " + RTSM_HOSTNAME + " failed. Error details: " + details + "\n", e);
            ucmdbProvider = null;
            ucmdbService = null;
        }

    }

    private synchronized TopologyQueryService getQueryService() {
        if (!DefaultGroovyMethods.asBoolean(RTSM_HOSTNAME) || !DefaultGroovyMethods.asBoolean(RTSM_USERNAME) || !DefaultGroovyMethods.asBoolean(RTSM_PASSWORD))
            return null;

        // try to connect twice
        for (int i = 0; i < 2; i++) {
            try {
                if (ucmdbProvider == null) initUcmdbConnection();
                if (ucmdbProvider == null || ucmdbService == null) return null;

                // Get the query service
                return ucmdbService.getTopologyQueryService();
            } catch (Exception e) {
                // try to re-open the connection
                final String details = e.getClass().getCanonicalName() + (DefaultGroovyMethods.asBoolean(e.getMessage()) ? ": " + e.getMessage() + "." : "");
                if (i == 1)
                    m_log.error("Attempt to connect to RTSM on server " + RTSM_HOSTNAME + " failed. Error details: " + details + "\n", e);
                ucmdbProvider = null;
                ucmdbService = null;
            }

        }

        return null;
    }

    public Boolean ping(final PingArgs args) {
        if (m_log.isDebugEnabled())
            m_log.debug("Try to ping server: " + args.getConnectedServerName());

        // get the resource client connection to make the post
        final String protocol = args.isNodeSsl() ? "https" : "http";
        final Integer port = (args.getPort() == null || args.getPort() < 1) ? (args.isNodeSsl() ? 13443 : 13080) : args.getPort();
        final Resource resource = createRequest(protocol, args.getNode(), port, ROOT_PATH, args.getCredentials(), m_smCookies);
        resource.queryParam("query", PING_QUERY);
        try {
            final ClientResponse response = resource.get();
            getCookies(response, m_smCookies);
            checkPingResponse(response);
        } catch (ClientWebException e) {
            final String errMsg = "Node: " + m_node + ", Port: " + String.valueOf(m_port) + ", ClientWebException encountered: (" + String.valueOf(e.getResponse().getStatusCode()) + ") " + e.getResponse().getMessage();
            if (m_log.isDebugEnabled())
                m_log.debug(errMsg + " " + e);
            else m_log.error(errMsg);
            throw e;
        }

        args.setOutputDetail("Server successfully reached.");
        return true;
    }

    public synchronized void destroy() {
        m_log.debug("Service Manager Adapter destroy");
    }

    /**
     * Forwards the given event to the external process.
     *
     * @param args contains the event to forward and any other parameters
     *             needed to forward the event.
     * @return an OprEvent representing the external event. The id field must be set
     * with the id of the external event. If there is a drilldown URL path it should be
     * set in the drilldown UrlPath field. All other attributes are currently ignored.
     * If the id is set to null or null is returned it is assumed the external
     * process cannot be reached at this time. A retry will be made later.
     */
    public Boolean forwardEvent(final ForwardEventArgs args) {
        if ((m_oprVersion < 920) && (args.getEvent().getNode() != null)) getNodeProperties(args);

        final String extId = sendEvent(args.getEvent(), args.getInfo(), null, args.getCredentials(), args);
        if (extId != null) {
            args.setExternalRefId(extId);
            args.setDrilldownUrlPath(DRILLDOWN_ROOT_PATH + "%22" + extId + "%22");
            addIdToMap(args.getEvent().getId(), extId);

            // now update the cause/symptom links
            String causeExternalRefId = getCauseExternalId(args, args.getEvent());
            if (args.getEvent().getCause() != null && causeExternalRefId != null)
                linkCauseIncident(extId, args.getEvent().getCause(), causeExternalRefId, args.getCredentials());
            final OprSymptomList symptoms = args.getEvent().getSymptoms();
            if ((symptoms == null ? null : symptoms.getEventReferences()) != null && !args.getEvent().getSymptoms().getEventReferences().isEmpty()) {
                final OprEventReference cause = new OprEventReference(args.getEvent().getId());
                cause.setTitle(args.getEvent().getTitle());
                DefaultGroovyMethods.each(args.getEvent().getSymptoms().getEventReferences(), new Closure<Void>(this, this) {
                    public void doCall(OprSymptomReference symptomRef) {
                        String symptomExternalRefId = getSymptomExternalId(args, symptomRef);
                        if (DefaultGroovyMethods.asBoolean(symptomExternalRefId))
                            linkCauseIncident(symptomExternalRefId, cause, extId, args.getCredentials());
                    }

                });
            }

            return true;
        }

        return false;
    }

    private void getNodeProperties(ForwardEventArgs args) {
        // Gets all the node properties, not just the key properties
        OprNodeReference nodeRef = args.getEvent().getNode();
        OprConfigurationItem node = args.getCi(nodeRef.getTargetId());
        if ((node != null) && (node.getAny() != null) && (!node.getAny().isEmpty()))
            nodeRef.getNode().setAny(node.getAny());
    }

    public Boolean forwardEvents(final BulkForwardEventArgs args) {
        m_log.debug("***Begin Bulk Forward***");
        final Reference<Boolean> result = new Reference<Boolean>(false);
        final OprEventList events = args.getEvents();
        DefaultGroovyMethods.each((events == null ? null : events.getEventList()), new Closure<Set<OprSymptomReference>>(this, this) {




            public Set<OprSymptomReference> doCall(OprEvent event) throws MalformedURLException {
                OprForwardingInfo info = event.getForwardingInfo(m_connectedServerId);
                final String extId = sendEvent(event, info, null, args.getCredentials(), args);
                if (extId != null) {
                    args.setForwardSuccess(event.getId(), extId, DRILLDOWN_ROOT_PATH + "%22" + extId + "%22");
                    addIdToMap(event.getId(), extId);
                    result.set(true);

                    // now update the cause/symptom links
                    String causeExternalRefId = getCauseExternalId(args, event);
                    if (event.getCause() != null && causeExternalRefId)
                        linkCauseIncident(extId, event.getCause(), causeExternalRefId, args.getCredentials());
                    final OprSymptomList symptoms = event.getSymptoms();
                    if ((symptoms == null ? null : symptoms.getEventReferences()) != null && !event.getSymptoms().getEventReferences().isEmpty()) {
                        final OprEventReference cause = new OprEventReference(event.getId());
                        cause.setTitle(event.getTitle());
                        return DefaultGroovyMethods.each(event.getSymptoms().getEventReferences(), new Closure<Void>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public void doCall(OprSymptomReference symptomRef) {
                                String symptomExternalRefId = getSymptomExternalId(args, symptomRef);
                                if (DefaultGroovyMethods.asBoolean(symptomExternalRefId))
                                    linkCauseIncident(symptomExternalRefId, cause, extId, args.credentials);
                            }

                        });
                    }

                } else return;

            }

        });
        m_log.invokeMethod("debug", new Object[]{"***End Bulk Forward***"});
        return result.get();
    }

    private synchronized void addIdToMap(String id, String externalId) {
        // keep the last 500 to 999 entries in a cache
        if (m_useMap1) {
            m_idMap1.put(id, externalId);
            if (m_idMap1.size() > 400) {
                m_idMap2.clear();
                m_useMap1 = false;
            }

        } else {
            m_idMap2.put(id, externalId);
            if (m_idMap2.size() > 400) {
                m_idMap1.clear();
                m_useMap1 = true;
            }

        }

    }

    private synchronized String getCauseExternalId(ForwardEventArgs args, OprEvent event) {
        // check the id maps first
        final OprEventReference cause1 = event.getCause();
        final String causeId = (cause1 == null ? null : cause1.getTargetId());
        if (!DefaultGroovyMethods.asBoolean(causeId)) return null;

        String causeExternalRefId = m_idMap1.get(causeId);
        if (DefaultGroovyMethods.asBoolean(causeExternalRefId)) return causeExternalRefId;

        causeExternalRefId = m_idMap2.get(causeId);
        if (DefaultGroovyMethods.asBoolean(causeExternalRefId)) return causeExternalRefId;

        // check the DB
        OprEvent cause = args.invokeMethod("getEvent", new Object[]{causeId, false});
        if (DefaultGroovyMethods.asBoolean(cause)) {
            OprForwardingInfo causeInfo = cause.getForwardingInfo(m_connectedServerId);
            causeExternalRefId = (causeInfo == null ? null : causeInfo.getExternalId());
        }


        return causeExternalRefId;
    }

    private synchronized String getSymptomExternalId(ForwardEventArgs args, OprSymptomReference symptomRef) {
        // check the id maps first
        final String symptomId = (symptomRef == null ? null : symptomRef.getTargetId());
        if (!DefaultGroovyMethods.asBoolean(symptomId)) return null;

        String symptomExternalRefId = m_idMap1.get(symptomId);
        if (DefaultGroovyMethods.asBoolean(symptomExternalRefId)) return symptomExternalRefId;

        symptomExternalRefId = m_idMap2.get(symptomId);
        if (DefaultGroovyMethods.asBoolean(symptomExternalRefId)) return symptomExternalRefId;

        // check the DB
        OprEvent symptom = args.invokeMethod("getEvent", new Object[]{symptomId, false});
        if (DefaultGroovyMethods.asBoolean(symptom)) {
            OprForwardingInfo symptomInfo = symptom.getForwardingInfo(m_connectedServerId);
            symptomExternalRefId = (symptomInfo == null ? null : symptomInfo.getExternalId());
        }


        return symptomExternalRefId;
    }

    private void linkCauseIncident(String externalRefId, OprEventReference causeRef, String causeExternalRefId, PasswordAuthentication credentials) {
        final OprEvent event = new OprEvent();
        event.setCause(causeRef);

        // convert the OprEvent to an SM incident
        final String payload = toExternalEvent(event, null, causeExternalRefId, null);

        if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
            m_log.invokeMethod("debug", new Object[]{"Forward Change Request to Node: " + m_node + ", Port: " + String.valueOf(m_port) + ", XML in request:\n" + payload});
        try {
            final String path = INCIDENT_PATH + externalRefId;
            final Resource resource = createRequest(m_protocol, m_node, m_port, path, credentials, m_smCookies);
            final ClientResponse clientResponse = resource.put(payload);
            getCookies(clientResponse, m_smCookies);
            checkResponse(clientResponse);
            final String updateIncident = clientResponse.getEntity(String.class);

            if (updateIncident != null) {
                if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                    m_log.invokeMethod("debug", new Object[]{"Service Manager Incident updated: " + updateIncident});
            } else m_log.invokeMethod("error", new Object[]{"Update of Incident cause link failed."});
        } catch (ClientWebException e) {
            final String errMsg = "Node: " + m_node + ", Port: " + String.valueOf(m_port) + ", ClientWebException encountered: ".plus("(" + String.valueOf(e.getResponse().getStatusCode()) + ") " + e.getResponse().getMessage());
            if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                m_log.invokeMethod("error", new Object[]{errMsg, e});
            else m_log.invokeMethod("error", new Object[]{errMsg});
            m_log.invokeMethod("error", new Object[]{"Update of Incident cause link failed.", e});
        }

    }

    private String sendEvent(final OprEvent event, OprForwardingInfo info, String causeExternalRefId, PasswordAuthentication credentials, ForwardEventArgs args) {
        if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
            m_log.invokeMethod("debug", new Object[]{"forwardEvent() for event: " + event.getId()});

        // create the external ID for SM
        final String externalRefId;
        final String forwardingType = info.getForwardingType().toLowerCase();
        if (forwardingType.equals(OprForwardingTypeEnum.synchronize_and_transfer_control.toString())) {
            externalRefId = "urn:x-hp:2009:opr:" + m_connectedServerId + ":incident|escalated|provider:" + event.getId();
        } else {
            externalRefId = "urn:x-hp:2009:opr:" + m_connectedServerId + ":incident|informational|requestor:" + event.getId();
        }

        OprIntegerPropertyChange duplicateChange = null;
        if (event.getDuplicateCount() > 0) {
            duplicateChange = new OprIntegerPropertyChange();
            duplicateChange.setPreviousValue(null);
            duplicateChange.setCurrentValue(event.getDuplicateCount());
        }

        final String incident = toExternalEvent(event, externalRefId, causeExternalRefId, duplicateChange);

        // get the resource client connection to make the post
        final Resource resource = createRequest(m_protocol, m_node, m_port, ROOT_PATH, credentials, m_smCookies);
        final final Reference<String> response;
        if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
            m_log.invokeMethod("debug", new Object[]{"Forward Request to Node: " + m_node + ", Port: " + String.valueOf(m_port) + ", XML in request:\n" + incident});
        try {
            final ClientResponse clientResponse = resource.post(incident);
            getCookies(clientResponse, m_smCookies);
            checkResponse(clientResponse);
            response.set(clientResponse.getEntity(String.class));
            if (DefaultGroovyMethods.asBoolean(response.get())) {
                if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                    m_log.invokeMethod("debug", new Object[]{"Service Manager Incident created:\n" + response.get()});
            } else m_log.invokeMethod("warn", new Object[]{"Null response returned by server."});
        } catch (ClientWebException e) {
            final String errMsg = "Node: " + m_node + ", Port: " + String.valueOf(m_port) + ", ClientWebException encountered: ".plus("(" + String.valueOf(e.getResponse().getStatusCode()) + ") " + e.getResponse().getMessage());
            if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                m_log.invokeMethod("error", new Object[]{errMsg, e});
            else m_log.invokeMethod("error", new Object[]{errMsg});
            throw e;
        }


        // Set the return values
        if ((response.get() == null) || (response.get().length() == 0)) return null;
        else {
            final GPathResult xmlResult = new XmlSlurper().parseText(response.get());

            // check if this is an incident or a ATOM syndication entry
            final GPathResult respIncident = (xmlResult.name().equals("entry")) ? DefaultGroovyMethods.invokeMethod(xmlResult.getProperty("content"), "getProperty", new Object[]{INCIDENT_TAG}) : xmlResult;

            if (respIncident.name().equals(INCIDENT_TAG)) {
                // set the ID and drilldown URL path
                if ((m_oprVersion > 920) && !DefaultGroovyMethods.asBoolean(MapSM2OPRCustomAttribute.empty))
                    updateCustomAttributes(event.getId(), respIncident, args);
                return ((String) (DefaultGroovyMethods.invokeMethod(respIncident.getProperty(REFERENCE_NUMBER_TAG), "text", new Object[0])));
            }

            return null;
        }

    }

    public void updateCustomAttributes(String eventId, final GPathResult respIncident, Object args) {
        if ((m_oprVersion > 920) && !DefaultGroovyMethods.asBoolean(MapSM2OPRCustomAttribute.empty)) {


            final OprEvent update = new OprEvent();
            update.setId(eventId);
            update.setCustomAttributes(new OprCustomAttributeList());
            DefaultGroovyMethods.each(MapSM2OPRCustomAttribute, new Closure<Object>(this, this) {
                public Object doCall(String smPropertyName, String caName) {

                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {
                        m_log.invokeMethod("debug", new Object[]{respIncident.getProperty(smPropertyName)});
                    }

                    String caValue = DefaultGroovyMethods.invokeMethod(respIncident.getProperty(smPropertyName), "text", new Object[0]);
                    update.getCustomAttributes().getCustomAttributes().add(new OprCustomAttribute(caName, caValue));
                    /*
                    Add a custom debuger
                    to watch incident update
                     */
                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {
                        m_log.invokeMethod("debug", new Object[]{"Processing updateCustomAttributes method"});
                        return m_log.invokeMethod("debug", new Object[]{"Adding " + caName + " with value " + caValue});
                    }

                }

            });
            // Add the CAs to the event
            try {
                if (!update.getCustomAttributes().getCustomAttributes().isEmpty())
                    args.invokeMethod("submitChanges", new Object[]{update});
            } catch (Throwable t) {
                // Just log this error. Don't want the forward to about because of an error here.
                m_log.invokeMethod("error", new Object[]{"Error encountered while attempting to update event with custom attributes.", t});
            }

        }

    }

    /**
     * Send the event updates.
     *
     * @param args contains the event changes to forward and any other parameters
     *             needed to forward the changes.
     * @return true if the changes were successfully sent, otherwise false
     * If false is returned or an exception is thrown, a retry will be made later.
     */
    public Boolean forwardChange(ForwardChangeArgs args) {
        /*
        Debug injection
         */
        debugForwardEvent(args, (Log) m_log, 1095);

        return sendChange(args, args.getChanges(), args.getExternalRefId(), args.getCredentials());
    }

    public Boolean forwardChanges(final Object args) {
        m_log.invokeMethod("debug", new Object[]{"***Begin Bulk Forward Change***"});
        final Reference<Boolean> result = new Reference<Boolean>(false);

        final Object changes = args.changes;
        DefaultGroovyMethods.each((changes == null ? null : changes.eventChanges), new Closure<Object>(this, this) {
            public Object doCall(OprEventChange change) {
                result.set(sendChange(args, change, change.getEventRef().getTargetId(), args.credentials));
                if (!result.get()) return;

                return args.invokeMethod("setForwardSuccess", new Object[]{change.getId()});
            }

        });
        m_log.invokeMethod("debug", new Object[]{"***End Bulk Forward Change***"});
        return result.get();
    }

    private Boolean sendChange(final ForwardChangeArgs args, final OprEventChange changes, final String externalRefId, PasswordAuthentication credentials) {
        final Reference<Boolean> anyAttributeWasChanged = new Reference<Boolean>(false);
        if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
            m_log.invokeMethod("debug", new Object[]{"forwardChange() for incident <" + externalRefId + ">"});

        // if no changes to process then just return
        if ((changes == null) || (changes.getChangedProperties() == null)) return true;

        // create an OprEvent with the changes
        final OprEvent event = new OprEvent();
        final String eventId = DefaultGroovyMethods.asBoolean((changes.getEventRef().getTargetGlobalId())) ? changes.getEventRef().getTargetGlobalId() : changes.getEventRef().getTargetId();
        event.setId(eventId);

        final Reference<OprIntegerPropertyChange> duplicateChange = new Reference<OprIntegerPropertyChange>(null);

        final Reference<String> causeExternalRefId = new Reference<String>(null);
        DefaultGroovyMethods.each(changes.getChangedProperties(), new Closure<Boolean>(this, this) {
            public Boolean doCall(OprEventPropertyChange propChange) {
                final OprEventPropertyNameEnum name = OprEventPropertyNameEnum.valueOf(propChange.getPropertyName());

                // check if this OPR event property should be synchronized to the SM incident
                String propertyName = propChange.getPropertyName().toLowerCase();
                if (syncAllOPRPropertiesToSM || m_oprSyncProperties.contains(propertyName)) {
                    // SM is only interested in the following changes
                    Object changedValue = propChange.getCurrentValue();
                    switch (name) {
                        case OprEventPropertyNameEnum.title:
                            if (!DefaultGroovyMethods.asBoolean(event.getTitle())) {
                                final OprEvent currentEvent = args.invokeMethod("getEvent", new Object[]{eventId, false});
                                event.setTitle(currentEvent.getTitle());
                                event.setDescription(currentEvent.getDescription());
                                anyAttributeWasChanged.set(true);
                            }

                            break;
                        case OprEventPropertyNameEnum.description:
                            if (!DefaultGroovyMethods.asBoolean(event.getTitle())) {
                                final OprEvent currentEvent = args.invokeMethod("getEvent", new Object[]{eventId, false});
                                event.setTitle(currentEvent.getTitle());
                                event.setDescription(currentEvent.getDescription());
                                anyAttributeWasChanged.set(true);
                            }

                            break;
                        case OprEventPropertyNameEnum.severity:
                            if (SyncOPRPropertiesToSMActivityLog.contains("severity") || SyncOPRSeveritiesToSM.contains(changedValue.toString())) {
                                event.setSeverity((changedValue.toString()));
                                anyAttributeWasChanged.set(true);
                            }

                            break;
                        case OprEventPropertyNameEnum.priority:
                            if (SyncOPRPropertiesToSMActivityLog.contains("priority") || SyncOPRPrioritiesToSM.contains(changedValue.toString())) {
                                event.setPriority((changedValue.toString()));
                                anyAttributeWasChanged.set(true);
                            }

                            break;
                        case OprEventPropertyNameEnum.solution:
                            event.setSolution(((String) changedValue));
                            anyAttributeWasChanged.set(true);
                            break;
                        case OprEventPropertyNameEnum.assigned_user:
                            OprUserPropertyChange userPropChange = (OprUserPropertyChange) propChange;
                            OprUser assignedUser = new OprUser();
                            assignedUser.setUserName(userPropChange.getCurrentUserDisplayLabel());
                            assignedUser.setLoginName(userPropChange.getCurrentUserName());
                            assignedUser.setId((Integer) changedValue);
                            event.setAssignedUser(assignedUser);
                            anyAttributeWasChanged.set(true);
                            break;
                        case OprEventPropertyNameEnum.assigned_group:
                            OprGroupPropertyChange groupPropertyChange = (OprGroupPropertyChange) propChange;
                            OprGroup assignedGroup = new OprGroup();
                            assignedGroup.setName(groupPropertyChange.getCurrentGroupName());
                            assignedGroup.setId((Integer) changedValue);
                            event.setAssignedGroup(assignedGroup);
                            anyAttributeWasChanged.set(true);
                            break;
                        case OprEventPropertyNameEnum.state:
                            if (SyncOPRPropertiesToSMActivityLog.contains("state") || SyncOPRStatesToSM.contains(changedValue.toString())) {
                                event.setState(changedValue.toString());
                                event.setTimeStateChanged(changes.getTimeChanged());
                                anyAttributeWasChanged.set(true);
                            }

                            break;
                        case OprEventPropertyNameEnum.cause:
                            if (DefaultGroovyMethods.asBoolean(changedValue)) {
                                String causeId = changedValue.toString();
                                OprEventReference cause = new OprEventReference(causeId);
                                event.setCause(cause);
                                OprEvent causeEvent = args.invokeMethod("getEvent", new Object[]{cause.getTargetId(), false});
                                if (DefaultGroovyMethods.asBoolean(causeEvent)) {
                                    cause.setTitle(causeEvent.getTitle());
                                    OprForwardingInfo causeInfo = causeEvent.getForwardingInfo(m_connectedServerId);
                                    causeExternalRefId.set((causeInfo == null ? null : causeInfo.getExternalId()));
                                }

                                anyAttributeWasChanged.set(true);
                            } else event.setCause(null);
                            break;
                        case OprEventPropertyNameEnum.symptom:
                            if (((OprSymptomPropertyChange) propChange).getChangeOperation().equals("insert")) {
                                OprSymptomReference symptom = new OprSymptomReference(changedValue.toString());
                                symptom.setTitle(((OprSymptomPropertyChange) propChange).getSymptomTitle());
                                if (event.getSymptoms() == null) event.setSymptoms(new OprSymptomList());
                                event.getSymptoms().getEventReferences().add(symptom);
                                anyAttributeWasChanged.set(true);
                            }

                            break;
                        case OprEventPropertyNameEnum.control_transferred_to:
                            final OprControlTransferInfo transferChange = ((OprControlTransferInfo) changedValue);
                            if (!m_node.equalsIgnoreCase(transferChange.getDnsName()) && OprControlTransferStateEnum.transferred.name().equals(transferChange.getState())) {
                                event.setControlTransferredTo(transferChange);
                                anyAttributeWasChanged.set(true);
                            }

                            break;
                        case ((OprEventPropertyNameEnum) com.hp.opr.api.ws.model.event.property.OprEventPropertyNameEnum).isAnnotation():
                            final OprAnnotationPropertyChange annotationChange = (OprAnnotationPropertyChange) propChange;
                            if (annotationChange.getChangeOperation().equals(OprPropertyChangeOperationEnum.insert.name()) || annotationChange.getChangeOperation().equals(OprPropertyChangeOperationEnum.update.name())) {
                                final OprAnnotation annotation = new OprAnnotation();
                                annotation.setTimeCreated(changes.getTimeChanged());
                                annotation.setAuthor(annotationChange.getAuthor());
                                annotation.setText(((String) changedValue));
                                if (event.getAnnotations() == null) event.setAnnotations(new OprAnnotationList());
                                event.getAnnotations().getAnnotations().add(annotation);
                                anyAttributeWasChanged.set(true);
                            }

                            break;
                        case OprEventPropertyNameEnum.duplicate_count:
                            Integer newCount = (Integer) propChange.getCurrentValue();
                            if ((newCount) && (newCount < MAX_DUPLICATE_COUNT_UPDATES)) {
                                duplicateChange.set((OprIntegerPropertyChange) propChange);
                                event.setDuplicateCount(newCount);
                                anyAttributeWasChanged.set(true);
                            }

                            break;
                        default:
                            break;
                    }
                }


                //TODO Debug this method
                // handle CAs separately
                if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {
                    m_log.invokeMethod("debug", new Object[]{"We are inside sendChange method"});
                }

                if (OprEventPropertyNameEnum.custom_attribute.equals(name)) {
                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {
                        m_log.invokeMethod("debug", new Object[]{"Trying to handle custom attributes"});
                    }

                    final OprCustomAttributePropertyChange customAttributeChange = (OprCustomAttributePropertyChange) propChange;
                    final String caName = customAttributeChange.getKey();
                    if (caName && m_OPR2SMCustomAttribute.containsKey(caName.toLowerCase(LOCALE)) && (customAttributeChange.getChangeOperation().equals(OprPropertyChangeOperationEnum.insert.name()) || customAttributeChange.getChangeOperation().equals(OprPropertyChangeOperationEnum.update.name()))) {
                        final OprCustomAttribute customAttribute = new OprCustomAttribute();
                        customAttribute.setName(caName);
                        customAttribute.setValue(((String) propChange.getCurrentValue()));
                        if (event.getCustomAttributes() == null)
                            event.setCustomAttributes(new OprCustomAttributeList());
                        event.getCustomAttributes().getCustomAttributes().add(customAttribute);
                        if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {
                            m_log.invokeMethod("debug", new Object[]{"We passed custom attribute conditions \nAnd custom Attribute name: " + customAttribute.getName() + " with value " + customAttribute.getValue() + " has been inserted"});
                        }

                        return setGroovyRef(anyAttributeWasChanged, true);
                    }

                }

            }

        });

        Boolean result = true;
        if (anyAttributeWasChanged.get()) {
            // convert the OprEvent to an SM incident
            final String payload = toExternalEvent(event, null, causeExternalRefId.get(), duplicateChange.get());
            if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                m_log.invokeMethod("debug", new Object[]{"Forward Change Request to Node: " + m_node + ", Port: " + String.valueOf(m_port) + ", XML in request:\n" + payload});
            try {
                final String path = INCIDENT_PATH + externalRefId;
                final Resource resource = createRequest(m_protocol, m_node, m_port, path, credentials, m_smCookies);
                final ClientResponse clientResponse = resource.put(payload);
                getCookies(clientResponse, m_smCookies);
                checkResponse(clientResponse);
                final String updateIncident = clientResponse.getEntity(String.class);

                if (updateIncident != null) {
                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                        m_log.invokeMethod("debug", new Object[]{"Service Manager Incident updated:\n" + updateIncident});
                    if ((m_oprVersion > 920) && syncCheckForClose && !OprState.closed.name().equals(event.getState())) {
                        // check if the incident is closed now by checking the response object
                        final GPathResult xmlResult = new XmlSlurper().parseText(updateIncident);

                        // check if this is an incident or a ATOM syndication entry
                        final GPathResult incident = (xmlResult.name().equals("entry")) ? DefaultGroovyMethods.invokeMethod(xmlResult.getProperty("content"), "getProperty", new Object[]{INCIDENT_TAG}) : xmlResult;

                        if (incident != null && incident.name().equals(INCIDENT_TAG)) {
                            String status = DefaultGroovyMethods.invokeMethod(incident.getProperty(INCIDENT_STATUS_TAG), "text", new Object[0]);
                            if ("closed".equals(status)) {
                                // Incident is closed. Check if current event is also closed.
                                OprEvent current = args.invokeMethod("getEvent", new Object[]{eventId, false});
                                if (current && !OprState.closed.name().equals(current.getState())) {
                                    // Event is still open. Close it if forwarding type allows back synchronization.
                                    OprForwardingInfo info = current.getForwardingInfo(m_connectedServerId);
                                    if (info && (OprForwardingTypeEnum.synchronize.name().equals(info.getForwardingType()) || OprForwardingTypeEnum.synchronize_and_transfer_control.name().equals(info.getForwardingType()))) {
                                        m_log.invokeMethod("debug", new Object[]{"Closing event " + event.getId() + " because corresponding SM incident " + externalRefId + " is closed."});
                                        current.setState(OprState.closed.name());
                                        args.invokeMethod("submitChanges", new Object[]{current});
                                    }

                                }

                            }

                        }

                    }

                } else {
                    m_log.invokeMethod("warn", new Object[]{"Null response returned by server."});
                    result = false;
                }

            } catch (ClientWebException e) {
                if (syncCheckForClose && isIncidentClosed(externalRefId, args.credentials)) {
                    m_log.invokeMethod("info", new Object[]{"Ignoring update for SM incident " + externalRefId + " because incident is already closed."});
                    if (m_oprVersion > 920) {
                        // Incident is already closed, so just close the event, if not already closed, otherwise ignore the update
                        OprEvent current = args.invokeMethod("getEvent", new Object[]{eventId, false});
                        if (current && !OprState.closed.name().equals(current.getState())) {
                            // Event is still open. Close it if forwarding type allows back synchronization.
                            OprForwardingInfo info = current.getForwardingInfo(m_connectedServerId);
                            if (info && (OprForwardingTypeEnum.synchronize.name().equals(info.getForwardingType()) || OprForwardingTypeEnum.synchronize_and_transfer_control.name().equals(info.getForwardingType()))) {
                                m_log.invokeMethod("debug", new Object[]{"Closing event " + eventId + " because corresponding SM incident " + externalRefId + " is closed."});
                                current.setState(OprState.closed.name());
                                args.invokeMethod("submitChanges", new Object[]{current});
                            }

                        }

                    }

                    return true;
                } else {
                    // Handle the error
                    final String errMsg = "Node: " + m_node + ", Port: " + String.valueOf(m_port) + ", ClientWebException encountered: ".plus("(" + String.valueOf(e.getResponse().getStatusCode()) + ") " + e.getResponse().getMessage());
                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                        m_log.invokeMethod("error", new Object[]{errMsg, e});
                    else m_log.invokeMethod("error", new Object[]{errMsg});
                    throw e;
                }

            }

        }


        return result;
    }

    private boolean isIncidentClosed(final String externalRefId, PasswordAuthentication credentials) {
        final String path = INCIDENT_PATH + externalRefId;

        try {
            final Resource resource = createRequest(m_protocol, m_node, m_port, path, credentials, m_smCookies);
            resource.accept(MediaType.APPLICATION_ATOM_XML);
            final ClientResponse clientResponse = resource.get();
            getCookies(clientResponse, m_smCookies);
            checkResponse(clientResponse);
            final String response = clientResponse.getEntity(String.class);

            if ((response != null) && (response.length() >= 0)) {
                if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                    m_log.invokeMethod("debug", new Object[]{"Service Manager Incident successfully retrieved:\n " + response});
                final GPathResult xmlResult = new XmlSlurper().parseText(response);

                // check if this is an incident or a ATOM syndication entry
                final GPathResult incident = (xmlResult.name().equals("entry")) ? DefaultGroovyMethods.invokeMethod(xmlResult.getProperty("content"), "getProperty", new Object[]{INCIDENT_TAG}) : xmlResult;

                if (incident != null && incident.name().equals(INCIDENT_TAG)) {
                    String status = DefaultGroovyMethods.invokeMethod(incident.getProperty(INCIDENT_STATUS_TAG), "text", new Object[0]);
                    return ("closed".equals(status));
                }

            }

        } catch (ClientWebException e) {
            // If not found, then assume it is closed.
            if (e.getResponse().getStatusCode() == 404) {
                return true;
            }

            final String errMsg = "Node: " + m_node + ", Port: " + String.valueOf(m_port) + ", ClientWebException encountered: ".plus("(" + String.valueOf(e.getResponse().getStatusCode()) + ") " + e.getResponse().getMessage());
            if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                m_log.invokeMethod("error", new Object[]{errMsg, e});
            else m_log.invokeMethod("error", new Object[]{errMsg});
        }

        return false;
    }

    /**
     * Get the incident data from SM represented as an OprEvent
     *
     * @param args contains the external event ID.
     * @return the OPR Event. If no object exists a null is returned.
     */
    public Boolean getExternalEvent(final GetExternalEventArgs args) {
        final String[] idParts = args.getExternalRefId().split(":");
        final String path = INCIDENT_PATH + idParts[idParts.length - 1];

        if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
            m_log.invokeMethod("debug", new Object[]{"getExternalEvent() for event: " + args.getExternalRefId()});

        try {
            final Resource resource = createRequest(m_protocol, m_node, m_port, path, args.getCredentials(), m_smCookies);
            resource.accept(MediaType.APPLICATION_ATOM_XML);
            final ClientResponse clientResponse = resource.get();
            getCookies(clientResponse, m_smCookies);
            checkResponse(clientResponse);
            final String response = clientResponse.getEntity(String.class);

            if ((response != null) && (response.length() >= 0)) {
                if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                    m_log.invokeMethod("debug", new Object[]{"Service Manager Incident successfully retrieved:\n " + response});
                final GPathResult xmlResult = new XmlSlurper().parseText(response);

                // check if this is an incident or a ATOM syndication entry
                final GPathResult incident = (xmlResult.name().equals("entry")) ? DefaultGroovyMethods.invokeMethod(xmlResult.getProperty("content"), "getProperty", new Object[]{INCIDENT_TAG}) : xmlResult;

                if (incident != null && incident.name().equals(INCIDENT_TAG)) {
                    final Reference<String> title = new Reference<String>(DefaultGroovyMethods.invokeMethod(incident.getProperty(TITLE_TAG), "text", new Object[0]));
                    final Reference<String> description = new Reference<String>(DefaultGroovyMethods.invokeMethod(incident.getProperty(DESCRIPTION_TAG), "text", new Object[0]));
                    final Reference<String> status = new Reference<String>(DefaultGroovyMethods.invokeMethod(incident.getProperty(INCIDENT_STATUS_TAG), "text", new Object[0]));
                    final Reference<String> urgency = new Reference<String>(DefaultGroovyMethods.invokeMethod(incident.getProperty(URGENCY_TAG), "text", new Object[0]));
                    final Reference<String> priority = new Reference<String>(DefaultGroovyMethods.invokeMethod(incident.getProperty(PRIORITY_TAG), "text", new Object[0]));
                    final Reference<String> assignedUser = new Reference<String>(DefaultGroovyMethods.invokeMethod(incident.getProperty(ASSIGNED_TAG), "getProperty", new Object[]{PARTY_TAG}).invokeMethod("getProperty", new Object[]{UI_NAME_TAG}).invokeMethod("text", new Object[0]));
                    final Reference<String> assignedGroup = new Reference<String>(DefaultGroovyMethods.invokeMethod(incident.getProperty(ASSIGNED_GROUP_TAG), "getProperty", new Object[]{FUNCTIONAL_GROUP_TAG}).invokeMethod("getProperty", new Object[]{UI_NAME_TAG}).invokeMethod("text", new Object[0]));

                    if (title.get() == null) title.set("");
                    if (description.get() == null) description.set("");
                    if (status.get() == null) status.set("");
                    if (urgency.get() == null) urgency.set("");
                    if (priority.get() == null) priority.set("");
                    if (assignedUser.get() == null) assignedUser.set("");
                    if (assignedGroup.get() == null) assignedGroup.set("");

                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                        m_log.invokeMethod("debug", new Object[]{"SM Incident:\n\tTitle: " + title.get() + "\n\tDescription: " + description.get() + "\n".plus("\tState: " + status.get() + "\n\tUrgency: " + urgency.get() + "\n\tPriority: " + priority.get() + "\n") + "\tAssigned User: " + assignedUser.get() + "\n\tAssigned Group: " + assignedGroup.get()});

                    args.setTitle(title.get());
                    args.setDescription(description.get());
                    args.setState(status.get());
                    args.setSeverity(SMUrgency.get(urgency.get()) == null ? urgency.get() : SMUrgency.get(urgency.get()));
                    args.setPriority(SMPriority.get(priority.get()) == null ? priority.get() : SMPriority.get(priority.get()));
                    args.setAssignedUser(assignedUser.get());
                    args.setAssignedGroup(assignedGroup.get());

                    return true;
                }

                return false;
            } else {
                return false;
            }

        } catch (ClientWebException e) {
            final String errMsg = "Node: " + m_node + ", Port: " + String.valueOf(m_port) + ", ClientWebException encountered: ".plus("(" + String.valueOf(e.getResponse().getStatusCode()) + ") " + e.getResponse().getMessage());
            if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                m_log.invokeMethod("error", new Object[]{errMsg, e});
            else m_log.invokeMethod("error", new Object[]{errMsg});
            throw e;
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
        final OprEvent event = args.getEvent();
        final String externalEventChange = args.getExternalEventChange();
        final boolean isDebugLogEnabled = m_log.invokeMethod("isDebugEnabled", new Object[0]);

        if (isDebugLogEnabled)
            m_log.invokeMethod("debug", new Object[]{"receiveChange() for external event: " + args.getExternalRefId() + "\n" + externalEventChange});

        // ignore the request if forwarding type is notify or notify_and_update
        OprForwardingInfo info = args.getInfo();
        if (info.getForwardingType().equals(OprForwardingTypeEnum.notify.name()) || info.getForwardingType().equals(OprForwardingTypeEnum.notify_and_update.name()))
            return true;

        if ((externalEventChange == null) || (externalEventChange.length() == 0) || (event == null)) return false;

        final GPathResult xmlResult = new XmlSlurper().parseText(externalEventChange);

        // check if this is an incident or a ATOM syndication entry
        final GPathResult incident = (xmlResult.name().equals("entry")) ? DefaultGroovyMethods.invokeMethod(xmlResult.getProperty("content"), "getProperty", new Object[]{INCIDENT_TAG}) : xmlResult;

        if (incident.name().equals(INCIDENT_TAG)) {
            DefaultGroovyMethods.each(incident.childNodes(), new Closure<Void>(this, this) {
                public void doCall(Object child) {

                    String propertyName = child.name;
                    String propertyValue = DefaultGroovyMethods.invokeMethod(child, "text", new Object[0]);

                    if (syncAllSMPropertiesToOPR || m_smSyncProperties.contains(propertyName) || REFERENCE_NUMBER_TAG.equals(propertyName)) {
                        // check if the property has changed
                        if (DefaultGroovyMethods.isCase(REFERENCE_NUMBER_TAG, propertyName)) {
                            final String extId = propertyValue;
                            if (isDebugLogEnabled)
                                m_log.invokeMethod("debug", new Object[]{"Processing update for SM incident: " + extId});
                        } else if (DefaultGroovyMethods.isCase(TITLE_TAG, propertyName)) {
                            final String title = propertyValue;
                            if (!event.getTitle().equals(title)) args.setTitle(title);
                        } else if (DefaultGroovyMethods.isCase(DESCRIPTION_TAG, propertyName)) {
                            final String description = propertyValue;
                            if (!event.getDescription().equals(description)) args.setDescription(description);
                        } else if (DefaultGroovyMethods.isCase(INCIDENT_STATUS_TAG, propertyName)) {
                            final String bdmIncidentState = propertyValue;
                            if (DefaultGroovyMethods.asBoolean(bdmIncidentState)) {
                                if (syncAllSMStatusToOPR || SyncSMStatusToOPR.contains(bdmIncidentState)) {
                                    final String oprEventState = MapSM2OPRState.get(bdmIncidentState);

                                    if (DefaultGroovyMethods.asBoolean(oprEventState)) {
                                        if (isDebugLogEnabled)
                                            m_log.invokeMethod("debug", new Object[]{"OPR event state change to: <" + oprEventState + ">"});
                                        args.setState(OprState.valueOf(oprEventState));
                                    }

                                }

                            }

                            boolean isTransferred = event.isControlTransferred() == null ? false : event.isControlTransferred();
                            if (!isTransferred && !bdmIncidentState.equals("open") && !bdmIncidentState.equals("closed")) {
                                if (isDebugLogEnabled)
                                    m_log.invokeMethod("debug", new Object[]{"Transferring event ownership to Connected Server <" + m_connectedServerName + ">: " + event.getId()});
                                args.requestControl();
                            }

                        } else if (DefaultGroovyMethods.isCase(URGENCY_TAG, propertyName)) {
                            final String urgency = propertyValue;
                            if (urgency && (syncAllSMUrgenciesToOPR || SyncSMUrgenciesToOPR.contains(urgency))) {
                                // map BDM urgency to opr event severity
                                final String severity = MapSM2OPRSeverity.get(urgency);
                                if (DefaultGroovyMethods.asBoolean(severity))
                                    args.setSeverity(OprSeverity.valueOf(severity));
                            }

                        } else if (DefaultGroovyMethods.isCase(PRIORITY_TAG, propertyName)) {
                            final String smPriority = propertyValue;
                            if (smPriority && (syncAllSMPrioritiesToOPR || SyncSMPrioritiesToOPR.contains(smPriority))) {
                                final String oprPriority = MapSM2OPRPriority.get(smPriority);
                                if (DefaultGroovyMethods.asBoolean(oprPriority))
                                    args.setPriority(OprPriority.valueOf(oprPriority));
                            }

                        } else if (DefaultGroovyMethods.isCase(SOLUTION_TAG, propertyName)) {
                            final String solution = propertyValue;
                            if (!event.getSolution().equals(solution)) args.setSolution(solution);
                        } else {
                        }
                        // Check custom attributes separately. May want to sync standard property to CA, too.
                        String caName = MapSM2OPRCustomAttribute.get(propertyName);
                        if (DefaultGroovyMethods.asBoolean(caName)) args.addCustomAttribute(caName, propertyValue);
                    }

                }

            });
            return true;
        } else return false;
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
        return toExternalEvent(event, null, null, null);
    }

    /**
     * Convert the opr event into an BDM compliant incident object
     *
     * @param event         object to convert
     * @param externalRefId and processing status
     * @return the converted external event object
     */
    public String toExternalEvent(final OprEvent event, final String externalRefId, final String causeExternalRefId, final OprIntegerPropertyChange duplicateChange) {
        if (event == null) return null;

        // check if this was called by forwardEvent() to create a new SM Incident
        final boolean isNewIncident = (externalRefId != null);

        boolean default_flag = true;

        String astl_related_ci = null;

        final Reference<String> astl_assignment_group = new Reference<String>(null);
        final Reference<String> astl_logical_name = new Reference<String>(null);
        final Reference<String> astl_priority = new Reference<String>(null);
        final Reference<String> astl_urgency = new Reference<String>(null);
        String astl_title = null;
        String astl_description = null;
        final Reference<String> astl_category = new Reference<String>(null);
        final Reference<String> astl_sub_category = new Reference<String>(null);

        final Reference<Matcher> myMatcher = new Reference<Matcher>(null);

//##################################### ASTELIT RULES SECTION #####################################
        if (isNewIncident) {

            final OprRelatedCi relatedCi_temp = event.getRelatedCi();
            astl_related_ci = relatedCi_temp.getConfigurationItem().getCiName();

            //## Rule 1:
            //## RFC C21126: "OVO Agent is using too many system resources" events ##
            if (event.getCategory().equals("Performance") && event.getApplication().equals("HP OSSPI") && event.getObject().equals("CPU_ovagent")) {

                astl_assignment_group.set("SN-IO-ITM");

                default_flag = false;
            }

            //############################ END Rule 1 ######################################

            //## Rule 2:
            //########### Policy "ASTL-Billing-Disk-Usage" (C18549) #################
            if (event.getCategory().equals("billing_admin_team") && (MapOPR2SMUrgency.get(event.getSeverity()).equals("1") || MapOPR2SMUrgency.get(event.getSeverity()).equals("2"))) {

                astl_logical_name.set(event.getApplication());
                astl_operational_device = "true";

                if (MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
                    astl_priority.set("2");
                }


                if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
                    astl_priority.set("3");
                }


                default_flag = false;
            }

            //############################ END Rule 2 ######################################

            //## Rule 3:
            //######################### SAP Events ##################################
            if (event.getCategory().equals("SAP") && event.getApplication().equals("SAP") && event.getObject().equals("R3AbapShortdumps")) {

                astl_logical_name.set("sapUKP");
                astl_operational_device = "true";
                astl_priority.set("4");

                default_flag = false;
            }

            //############################ END Rule 3 ######################################

            //## Rule 4:
            //######################### TNS Events ##################################
            if (event.getCategory().equals("TADIG") && event.getObject().equals("ths_datanet_file_transfer_check.sh") && (MapOPR2SMUrgency.get(event.getSeverity()).equals("3"))) {

                astl_logical_name.set(" ");
                astl_assignment_group.set("SN-AO-CSP-SSR");
                astl_title = "THS-NRTRDE file transfer delay";
                astl_operational_device = "true";
                astl_priority.set("4");

                default_flag = false;
            }

            //############################ END Rule 4 ######################################

            //## Rule 5:
            //######################## ABF Events ###################################
            if (event.getCategory().equals("ORGA") && event.getApplication().equals("ABF") && (MapOPR2SMUrgency.get(event.getSeverity()).equals("2") || MapOPR2SMUrgency.get(event.getSeverity()).equals("3"))) {

                astl_assignment_group.set("SN-AO-SCC");
                astl_logical_name.set("ABF application");
                astl_category.set("Service Platforms");
                astl_sub_category.set("ABF");

                astl_priority.set("4");

                default_flag = false;
            }

            //############################ END Rule 5 ######################################

            //## Rule 6:
            //####################### SL3000 Events #################################
            if (event.getApplication().equals("Tape Library") && event.getObject().equals("sl3000")) {

                astl_logical_name.set("SL3K");
                astl_title = "SL3K Drive not unloaded for fetch - on rewindUnload";
                astl_description = "SL3K Drive not unloaded for fetch - on rewindUnload";
                astl_category.set("Infrastructure");
                astl_sub_category.set("Backups - Hardware");
                astl_operational_device = "true";
                astl_priority.set("4");

                default_flag = false;
            }

            //############################ END Rule 6 ######################################

            //## Rule 7:
            //######################## EVA Events ###################################

            if (DefaultGroovyMethods.asBoolean(Pattern.compile( / ( ? i) EVA /).matcher(astl_related_ci))){
                astl_category.set("Infrastructure");
                astl_sub_category.set("Storage");

                default_flag = false;
            }

            //############################ END Rule 7 ######################################

            //## Rule 8:
            //##################### AIS Reboot Events ###############################
            if (event.getCategory().equals("Monitor") && event.getApplication().equals("MonitorLoger") && (MapOPR2SMUrgency.get(event.getSeverity()).equals("3"))) {
                astl_logical_name.set(event.getObject());
                astl_title = "Host " + event.getObject() + " was rebooted";
                astl_priority.set("3");

                default_flag = false;
            }

            //############################ END Rule 8 ######################################

            //## Rule 9:
            //##################### Performance Events ##############################
            if (event.getCategory().equals("Performance") || event.getObject().equals("Connection_check")) {
                astl_logical_name.set(astl_related_ci);
                astl_operational_device = "true";

                default_flag = false;
            }

            //############################ END Rule 9 ######################################

            //## Rule 10:
            //##################### Temperature Events ##############################
            if (event.getApplication().equals("Temp mon")) {
                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / Temperature was changed /).matcher(event.getTitle())))
                {
                    astl_priority.set("3");
                }


                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / is more then max threshold /).matcher(event.getTitle())))
                {
                    astl_priority.set("2");
                }


                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / is lower then min threshold /).matcher(event.getTitle())))
                {
                    astl_priority.set("2");
                }


                astl_logical_name.set(event.getObject());
                astl_operational_device = "true";

                default_flag = false;
            }

            //############################ END Rule 10 ######################################

            //## Rule 11:
            //###################### SAN Disk Events ################################
            if (event.getCategory().equals("Hardware") && event.getApplication().equals("SANdisk") && (MapOPR2SMUrgency.get(event.getSeverity()).equals("2"))) {

                astl_assignment_group.set("SN-IO-SSDA-SA");
                astl_operational_device = "true";
                astl_priority.set("1");

                default_flag = false;
            }

            //############################ END Rule 11 ######################################

            //## Rule 12:
            //####################### HP SIM Events #################################
            if (event.getCategory().equals("HP_SIM") && event.getApplication().equals("HP_SIM")) {

                astl_logical_name.set(event.getObject());
                astl_sub_category.set("HP SIM");

                //# HP SIM events with opened CASE in the HP (C20191)
                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / SEA Version:System Event Analyzer for Windows /).
                matcher(event.getTitle())))astl_operational_device = "true";

                //# Configuring Auto Incidents from Serviceguard cluster (C20026)
                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / hpmcSG /).matcher(event.getTitle())))
                astl_logical_name.set(astl_related_ci);

                myMatcher.set((Pattern.compile( / (NO_SERVER_CI_OUTAGE_FLAG) (. *) /).matcher(event.getTitle())));
                if (myMatcher.get().matches()) {
                    astl_operational_device = "true";
                    astl_description = DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 2);
                }


                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / Incomplete OA XML Data /).matcher(event.getTitle())))
                astl_priority.set("4");

                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / (\ (SNMP\)Process Monitor Event Trap\(11011\)|
                HP ProLiant -HP Power - Power Supply Failed|
                cpqHe4FltTolPowerSupplyDegraded | cpqHe4FltTolPowerSupplyFailed |\(WBEM\)Power redundancy reduced |\
                (WBEM\)Power redundancy lost |\(WBEM\)Power Supply Failed |\(SNMP\)Power Supply Failed\(6050\)|\(SNMP\)
                Power Redundancy Lost\(6032\))/).matcher(event.getTitle())))astl_operational_device = "true";

                //# For WMI Events. If string Brief Description is in Message text
                myMatcher.set((Pattern.compile( / Brief Description:\n\s(. *)/).matcher(event.getTitle())));
                if (myMatcher.get().matches())
                    astl_title = DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1);

                //# For SNMP Traps. If string Event Name is in Message text
                myMatcher.set((Pattern.compile( / Event Name:\s(. *)/).matcher(event.getTitle())));
                if (myMatcher.get().matches())
                    astl_title = DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1);

                //# For WMI Events. If string Summary is in Message text
                myMatcher.set((Pattern.compile( / Summary:\s(. *)/).matcher(event.getTitle())));
                if (myMatcher.get().matches())
                    astl_title = DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1);

                //# For WMI Events. If string Caption is in Message text
                myMatcher.set((Pattern.compile( / Caption:\s(. *)/).matcher(event.getTitle())));
                if (myMatcher.get().matches())
                    astl_title = DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1);

                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / (Severe latency bottleneck | is a congestion bottleneck) /).
                matcher(event.getTitle()))){
                    astl_assignment_group.set("SN-IO-SSDA-SB");
                    astl_category.set("Infrastructure");
                    astl_sub_category.set("SAN Switch");
                    astl_operational_device = "true";

                    myMatcher.set((Pattern.compile( /. * (. * AN -. * Slot. *, port. * is a congestion bottleneck)/).
                    matcher(event.getTitle())));
                    if (myMatcher.get().matches()) {
                        astl_title = String.valueOf(getProperty("astl_object")) + " " + String.valueOf(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1));

                        myMatcher.set((Pattern.compile( /. * (. * AN -. * Slot. *, port. * is a congestion
                        bottleneck. * percent of last.*seconds were affected by this condition.)/).
                        matcher(event.getTitle())));
                        if (myMatcher.get().matches())
                            astl_description = String.valueOf(getProperty("astl_object")) + " " + String.valueOf(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1));
                    }


                    myMatcher.set((Pattern.compile( /. * (AN -. * Severe latency bottleneck detected at Slot. * port. *) /).
                    matcher(event.getTitle())));
                    if (myMatcher.get().matches()) {
                        astl_title = String.valueOf(getProperty("astl_object")) + " " + String.valueOf(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1));
                        astl_description = String.valueOf(getProperty("astl_object")) + " " + String.valueOf(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1));
                    }

                }

                default_flag = false;
            }

            //############################ END Rule 12 ######################################

            //## Rule 13:
            //### Auto Incidents for XP arrays (C17089) and AMS Storages (C19906) ###
            if (Pattern.compile( / phecda /).
            matcher(astl_related_ci) && event.getCategory().equals("OS") && event.getApplication().equals("Application") && event.getObject().equals("Event Log"))
            {

                astl_assignment_group.set("SN-IO-SSDA-SB");
                astl_category.set("Infrastructure");
                astl_sub_category.set("Storage");
                astl_priority.set("3");
                astl_operational_device = "true";

                myMatcher.set((Pattern.compile( /. * SOURCE. * "(.*XP.*)". * STATUS. * COMPONENT. * "(.*)". * DESCRIPTION. * ".*error.*" /).matcher(event.getTitle())))
                ;
                if (myMatcher.get().matches()) {
                    astl_logical_name.set(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1));
                    astl_title = String.valueOf(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1)) + ": " + String.valueOf(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 2));
                }


                myMatcher.set((Pattern.compile( /. * SOURCE. * "(.*AMS.*)". * STATUS. * COMPONENT. * "(Disk Drive.*)". * DESCRIPTION. * /).
                matcher(event.getTitle())));
                if (myMatcher.get().matches()) {
                    astl_logical_name.set(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1));
                    astl_title = DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1) + " " + DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 2) + " fail.";
                }


                default_flag = false;
            }

            //############################ END Rule 13 ######################################

            //## Rule 14:
            //################# Policies "astl-win-procmon*" ########################
            if (event.getCategory().equals("win-procmon") && event.getApplication().equals("OS")) {

                astl_priority.set("2");

                if (event.getObject().equals("startManagedWebLogic.cmd")) astl_priority.set("4");

                default_flag = false;
            }

            //############################ END Rule 14 ######################################

            //## Rule 15
            //#################### Policies "ASTL-Procmon" (C20690, C20691) ##########################
            if (event.getCategory().equals("ELF-USSD") || event.getCategory().equals("ELF-SMS")) {
                if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) astl_priority.set("2");

                default_flag = false;
            }

            //############################ END Rule 15 ######################################

            //## Rule 16
            //################ Agent Health Status Events ###########################
            if (event.getCategory().equals("Agent_Healthcheck") && event.getObject().equals("opcmsg")) {
                astl_assignment_group.set("SN-AO-SCC");

                default_flag = false;
            }

            //############################ END Rule 16 ######################################

            //## Rule 17
            //################## ASTL-NG-BAS-Log-preparsed ##########################
            if (event.getCategory().equals("Gold BAS Logs") && event.getApplication().equals("Gold NG-BAS") && MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
                astl_assignment_group.set("SN-AO-CSP-BA");
                astl_logical_name.set("OPSC Gold BAS");
                astl_priority.set("3");

                default_flag = false;
            }

            //############################ END Rule 17 ######################################

            //## Rule 18
            //################## ASTL-TGW-Log-preparsed #############################
            if (event.getCategory().equals("TGW") && event.getApplication().equals("TGW")) {
                astl_logical_name.set(" ");
                astl_assignment_group.set("SN-AO-SCC");
                astl_priority.set("4");

                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / RTE interaction fails and delivers no result in operation \[Interaction\[
                RteModifyInteraction\]failed /).matcher(event.getTitle()))){
                    myMatcher.set((Pattern.compile( /. * ERROR. *\[\[.*?,.*?,(. * ?),.*/).matcher(event.getTitle())));
                    if (myMatcher.get().matches())
                        astl_title = "Troubles with reload " + String.valueOf(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1));
                }


                if (DefaultGroovyMethods.asBoolean(Pattern.compile( /\[ReloadBalances\]The line attribute\[RMF\]has an
                invalid value\[null\]/).matcher(event.getTitle()))){
                    myMatcher.set((Pattern.compile( /. * ERROR \[.*\[\[\w +\,\s\*+\,\s(\d +) /).
                    matcher(event.getTitle())));
                    if (myMatcher.get().matches())
                        astl_title = String.valueOf(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1)) + " attribute [RMF] has an invalid value";
                }


                default_flag = false;
            }

            //############################ END Rule 18 ######################################

            //## Rule 19
            //######################## OVSC Events ##################################
            if (event.getCategory().equals("OVSC") && event.getApplication().equals("OVSC") && event.getObject().equals("IncorreDB")) {
                astl_assignment_group.set("SN-AO-CSP-BA");
                astl_logical_name.set("OVSC");

                default_flag = false;
            }

            //############################ END Rule 19 ######################################

            //## Rule 20
            //############ Policy "ASTL-IncoreDB-Usage" (C18273, C21385) #############
            if (event.getCategory().equals("billing_admin_team") && event.getObject().equals("IncoreDB") && (MapOPR2SMUrgency.get(event.getSeverity()).equals("1") || MapOPR2SMUrgency.get(event.getSeverity()).equals("2"))) {

                if (event.getApplication().equals("MRTE")) astl_logical_name.set("OPSC Gold MRTE");

                if (event.getApplication().equals("OVSC") || event.getApplication().equals("LookUp"))
                    astl_logical_name.set("OVSC");

                if (MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) astl_priority.set("2");

                if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) astl_priority.set("3");

                astl_assignment_group.set("SN-AO-CSP-BA");

                default_flag = false;
            }

            //############################ END Rule 20 ######################################

            //## Rule 21
            //######################## wIQ Events ###################################
            if (event.getCategory().equals("wIQ") && event.getApplication().equals("wIQ") && MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
                astl_assignment_group.set("SN-AO-CSP-BA");
                astl_priority.set("4");

                default_flag = false;
            }

            //############################ END Rule 21 ######################################

            //## Rule 22
            //######################## NTP Events ###################################
            if (event.getCategory().equals("Time") && event.getApplication().equals("NTP") && event.getObject().equals("Time")) {
                astl_logical_name.set(astl_related_ci);
                astl_operational_device = "true";

                default_flag = false;
            }

            //############################ END Rule 22 ######################################

            //## Rule 23
            //###################### se9985.sdab.sn #################################
            if (DefaultGroovyMethods.asBoolean(Pattern.compile( / se9985 /).matcher(astl_related_ci))){
                astl_assignment_group.set("SN-IO-SSDA-SB");
                astl_priority.set("4");

                default_flag = false;
            }

            //############################ END Rule 23 ######################################

            //## Rule 24
            //####################### SCOM Events ###################################

            if (event.getCategory().equals("SCOM")) {

                OprCustomAttributeList eventAttrList = event.getCustomAttributes();
                if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {
                    m_log.invokeMethod("debug", new Object[]{"SCOM rules section:"});
                    m_log.invokeMethod("debug", new Object[]{"Event OprCustomAttributeList is: " + eventAttrList});
                    if (eventAttrList != null) {
                        m_log.invokeMethod("debug", new Object[]{"and it is not null!"});
                    } else {
                        m_log.invokeMethod("debug", new Object[]{"and it is null! Problem here!"});
                    }

                }

                try {
                    ArrayList<OprCustomAttribute> caList = eventAttrList.getCustomAttributes();
                    OprCustomAttribute customPriority = null;
                    OprCustomAttribute customTitle = null;
                    for (OprCustomAttribute ca : caList) {
                        if (ca.getName().equals("CustomPriority")) {
                            customPriority = ca;
                        } else if (ca.getName().equals("CustomTitle")) {
                            customTitle = ca;
                        }

                    }


                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {

                        m_log.invokeMethod("debug", new Object[]{"Event severity is " + event.getSeverity()});
                    }

                    astl_description = event.getTitle();
                    astl_title = customTitle.getValue();
                    event.setTitle(astl_title);
                    event.setDescription(astl_description);
                    if (event.getSeverity().toLowerCase().equals("critical")) {
                        if (customPriority.getValue().toLowerCase().equals("high")) {
                            event.setSeverity("major");
                            event.setPriority("high");
                        } else if (customPriority.getValue().toLowerCase().equals("medium") || customPriority.getValue().toLowerCase().equals("normal")) {
                            event.setSeverity("minor");
                            event.setPriority("medium");
                        } else if (customPriority.getValue().toLowerCase().equals("low")) {
                            event.setSeverity("warning");
                            event.setPriority("low");
                        }

                    } else if (event.getSeverity().toLowerCase().equals("warning")) {
                        if (customPriority.getValue().toLowerCase().equals("high")) {
                            event.setSeverity("minor");
                            event.setPriority("medium");
                        } else if (customPriority.getValue().toLowerCase().equals("medium") || customPriority.getValue().toLowerCase().equals("normal")) {
                            event.setSeverity("minor");
                            event.setPriority("medium");
                        } else if (customPriority.getValue().toLowerCase().equals("low")) {
                            event.setSeverity("warning");
                            event.setPriority("low");
                        }

                    }

                } catch (NullPointerException npe) {
                    event.setDescription(event.getDescription() + "Cannot Parse custom attributes" + npe.getMessage());
                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {

                        m_log.invokeMethod("debug", new Object[]{"SCOM processing error: " + npe.getStackTrace()});
                    }

                }

                /*
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
      */
                default_flag = true;
            }

            //############################ END Rule 24 ######################################

            //## Rule 25
            //################## HP Data Protector Events ###########################
            if (event.getCategory().equals("DP Session Reports")) {

                Pattern pTitle = Pattern.compile("(.*)");
                Matcher mTitle = pTitle.matcher(event.getTitle());
                astl_title = DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(mTitle, 0), 1);

                astl_logical_name.set("HP Data Protector Cell Manager " + astl_related_ci);

                //#set DP_ACTION [exec /opt/OV/scauto/dp/dp_action $OPCDATA_MSGTEXT]
                //eventObject set_evfield action $OPCDATA_MSGTEXT

                //### Passing assignment group ###
                astl_assignment_group.set("SN-IO-SSDA-SB");
                astl_category.set("Infrastructure");
                astl_sub_category.set("Backups - Software");
                astl_operational_device = "true";
                astl_priority.set("4");

                default_flag = false;
            }


            if (event.getCategory().equals("Data Protector") && event.getObject().equals("Sheduler")) {

                astl_logical_name.set("HP Data Protector Cell Manager " + astl_related_ci);

                astl_assignment_group.set("SN-IO-SSDA-SB");
                astl_category.set("Infrastructure");
                astl_sub_category.set("Backups - Software");
                astl_operational_device = "true";
                astl_priority.set("3");

                default_flag = false;
            }

            //############################ END Rule 25 ######################################

            //## Rule 26
            //############ Oracle Enterprise Manager Events #########################
            if (event.getCategory().equals("OracleEnterpriseManager")) {
                myMatcher.set((Pattern.compile( / Message:\s(. *)/).matcher(event.getTitle())));
                if (myMatcher.get().matches())
                    astl_title = DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1);

                if (event.getApplication().equals("Database Instance") || event.getApplication().equals("Agent") || event.getApplication().equals("Listener") || event.getApplication().equals("OMS and Repository") || event.getApplication().equals("Oracle High Availability Service"))
                    astl_logical_name.set(event.getObject() + " Instance");

                if (event.getApplication().equals("Cluster") || event.getApplication().equals("Cluster Database"))
                    astl_logical_name.set(event.getObject() + " DB Cluster");

                //astl_category = "Databases"
                astl_assignment_group.set("SN-IO-SSDA-DA");
                astl_priority.set("2");

                default_flag = false;
            }

            //############################ END Rule 26 ######################################

            //## Rule 26
            //################### SN-ISM Security Events ############################
            if (event.getCategory().equals("SN-ISM") && event.getApplication().equals("Security") && event.getObject().equals("Security")) {
                astl_assignment_group.set("SN-ISM");
                astl_category.set("Security");
                astl_sub_category.set("Security Systems Availability");

                astl_operational_device = "true";

                default_flag = false;
            }


            if (event.getCategory().equals("Security") && event.getApplication().equals("S-TAP agent")) {
                astl_assignment_group.set("SN-ISM");
                astl_category.set("Security");
                astl_sub_category.set("Security Systems Availability");

                astl_operational_device = "true";

                default_flag = false;
            }


            if (event.getApplication().equals("ASTL_Node_Pinger") && event.getObject().equals("Connection_check")) {
                astl_assignment_group.set("SN-ISM");
                astl_category.set("Security");
                astl_sub_category.set("Security Systems Availability");
                astl_priority.set("2");

                default_flag = false;
            }

            //############################ END Rule 27 ######################################

            //## Rule 28
            //######################## Agent Errors #################################
            if (event.getCategory().equals("OpC") && (event.getApplication().equals("HP OpenView Operations") || event.getApplication().equals("OM Agent")) && (MapOPR2SMUrgency.get(event.getSeverity()).equals("1") || MapOPR2SMUrgency.get(event.getSeverity()).equals("2"))) {
                astl_logical_name.set(astl_related_ci);
                astl_assignment_group.set("SN-AO-SCC");
                astl_priority.set("3");

                default_flag = false;
            }

            //############################ END Rule 28 ######################################

            //## Rule 29
            //################# MAXIMO Services Monitoring ##########################
            if (event.getApplication().equals("Service Policy")) {

                astl_assignment_group.set("MN-OS-MSI");

                if (Pattern.compile( / maximodb2 /).matcher(astl_related_ci) || Pattern.compile( / maximosb /).
                matcher(astl_related_ci))astl_priority.set("3");

                if (DefaultGroovyMethods.asBoolean(Pattern.compile( / maximo01 /).matcher(astl_related_ci)))
                astl_priority.set("4");

                default_flag = false;
            }


            if (event.getApplication().equals("MaximoPing")) {
                astl_assignment_group.set("MN-OS-MSI");
                astl_priority.set("4");

                default_flag = false;
            }

            //############################ END Rule 29 ######################################

            //## Rule 30
            //#################### Gold MRTE Event ##################################
            if (event.getCategory().equals("evn_astelit") && event.getApplication().equals("Gold MRTE") && (MapOPR2SMUrgency.get(event.getSeverity()).equals("1") || MapOPR2SMUrgency.get(event.getSeverity()).equals("2"))) {

                astl_assignment_group.set("SN-AO-SCC");
                astl_priority.set("3");

                if (event.getObject().equals("event-files")) astl_logical_name.set(" ");

                default_flag = false;
            }


            if (event.getCategory().equals("MRTE") && MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {

                astl_assignment_group.set("SN-AO-CSP-BA");
                astl_priority.set("1");

                if (event.getApplication().equals("OPSC reg_scp rte1")) astl_logical_name.set("OPSC reg_scp rte1");

                if (event.getApplication().equals("OPSC reg_scp rte2")) astl_logical_name.set("OPSC reg_scp rte2");

                default_flag = false;
            }

            //############################ END Rule 30 ######################################

            //## Rule 31
            //########### Policy "ASTL-MRTE-IncoreDB-usage" (C16928) ################
            if (event.getCategory().equals("MRTE") && (event.getApplication().equals("mrte1a") || event.getApplication().equals("mrte2a")) && MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
                if (event.getApplication().equals("mrte1a")) astl_logical_name.set("MRTE1-a");

                if (event.getApplication().equals("mrte2a")) astl_logical_name.set("MRTE2-a");

                astl_assignment_group.set("SN-AO-CSP-BA");
                astl_priority.set("1");

                default_flag = false;
            }

            //############################ END Rule 31 ######################################

            //## Rule 32
            //########### Policy "astl_win_rcu_monitoring_rcuX" (C17860) ############
            if (event.getCategory().equals("Hardware") && event.getApplication().equals("RCU") && MapOPR2SMUrgency.get(event.getSeverity()).equals("3")) {
                astl_logical_name.set(astl_related_ci + " software");
                astl_priority.set("4");

                default_flag = false;
            }

            //############################ END Rule 32 ######################################

            //## Rule 33
            //############## Policy "bcp_monitoring_usage" (C18277) #################
            if (event.getCategory().equals("BCP_mon") && event.getApplication().equals("BCP Backup") && MapOPR2SMUrgency.get(event.getSeverity()).equals("3")) {
                astl_logical_name.set(event.getObject());
                astl_operational_device = "true";
                astl_priority.set("3");

                default_flag = false;
            }

            //############################ END Rule 33 ######################################

            //## Rule 34
            //########### C20607: NNMi Management Events: SNMP_Interceptor ##########
            if (event.getCategory().equals("SNMP") && event.getApplication().equals("NNMi") && MapOPR2SMUrgency.get(event.getSeverity()).equals("1") && event.getTitle().equals("Node Down")) {

                myMatcher.set((Pattern.compile( / (\w +\sOS):(. *)/).matcher(event.getObject())));
                if (myMatcher.get().matches()) {
                    String oldLogicalName = DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 1);

                    astl_logical_name.set(oldLogicalName.replaceAll(" OS", ""));
                    astl_assignment_group.set(DefaultGroovyMethods.getAt(DefaultGroovyMethods.getAt(myMatcher.get(), 0), 2));
                    astl_category.set("Security");
                    astl_sub_category.set("Security Systems Availability");
                    astl_title = event.getApplication() + ":" + event.getTitle() + ":" + astl_related_ci;
                    astl_operational_device = "true";
                    astl_priority.set("4");
                }


                default_flag = false;
            }

            //############################ END Rule 34 ######################################

            //## Rule 35
            //##################### Performance Events ##############################
            if (event.getCategory().equals("Performance") && event.getApplication().equals("HP OSSPI") && event.getObject().equals("CPU_Wait_Util")) {

                astl_priority.set("3");
                astl_operational_device = "true";

                default_flag = false;
            }

            //############################ END Rule 35 ######################################

            //Decide which name we would use


            astl_logical_name.set(ciResolver(event, astl_logical_name.get(), (Log) m_log, 2388));

            //Add custom attributes


            addCustomAttribute(event, "operational_device", astl_operational_device);
            addCustomAttribute(event, "event_addon", "Field for custom information");
            debugOprEvent(event, (Log) m_log, 2318);
        }


//##################################### END ASTELIT RULES SECTION ##################################
        //TODO Custom section
//##################################### ASTELIT CUSTOM SECTION #####################################
        if (!default_flag) {

            //FIXME Truncater to cope from delev package
            // get the title & description.
            final Reference<String> title = new Reference<String>((event.getTitle() && event.getTitle().trim()) ? event.getTitle().trim().replace("\r", "\n") : null);
            final Reference<String> description = new Reference<String>((event.getDescription() && event.getDescription().trim()) ? event.getDescription().trim() : null);

            if (astl_title != null) title.set(astl_title);

            if (DefaultGroovyMethods.asBoolean(astl_description)) description.set(astl_description);

            if (title.get() && (title.get().length() > 256 || DefaultGroovyMethods.contains(title.get(), "\n"))) {
                // truncate the title and put it in the description
                if (DefaultGroovyMethods.contains(title.get(), "\n")) title.set(title.get().split("\n")[0].trim());
                if (title.get().length() > 256) title.set(title.get().substring(0, 252) + "...");
                if (!DefaultGroovyMethods.asBoolean(description.get())) description.set(event.getTitle().trim());
                else description.set(event.getTitle().trim() + "\n" + event.getDescription().trim());
            }


            // if the description is not set on Incident creation then set it to some default value
            if (isNewIncident && ((description.get() == null) || (description.get().trim().length() == 0)))
                if (astl_description == null) description.set(event.getTitle());
            // create the XML payload using the MarkupBuilder
            final StringWriter writer = new StringWriter();
            final MarkupBuilder builder = new MarkupBuilder(writer);
            final StringBuffer activityLog = new StringBuffer();

            LinkedHashMap<String, GString> map = new LinkedHashMap<String, GString>(4);
            map.put("relationships_included", String.valueOf(getProperty("INCIDENT_XML_RELATIONSHIPS")));
            map.put("type", String.valueOf(getProperty("INCIDENT_XML_TYPE")));
            map.put("version", String.valueOf(getProperty("INCIDENT_XML_VERSION")));
            map.put("xmlns", String.valueOf(getProperty("INCIDENT_XML_NAMESPACE")));
            .call(map, new Closure<Object>(this, this) {
                public Object doCall(Object it) {

                    ((MarkupBuilder) builder).it_process_category(IT_PROCESS_CATEGORY);
                    ((MarkupBuilder) builder).incident_type(INCIDENT_TYPE);
                    if (SpecifyActiveProcess) ((MarkupBuilder) builder).active_process("true");
                    //TODO Move this to custom attribute

                    // builder."${OPERATIONAL_DEVICE_TAG}"(astl_operational_device)

                    activityLog.append("\n").append(ACTIVITY_LOG_OPERATIONAL_DATA).append("\n").append(astl_operational_device).append("\n");

                    if (DefaultGroovyMethods.asBoolean(astl_priority.get())) {
                        if (astl_priority.get().equals("1")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "Enterprise");
                            .call(map1, "enterprise");
                        }

                        if (astl_priority.get().equals("2")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "Site/Dept");
                            .call(map1, "site-dept");
                        }

                        if (astl_priority.get().equals("3")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "Multiple Users");
                            .call(map1, "multiple-users");
                        }

                        if (astl_priority.get().equals("4")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "User");
                            .call(map1, "user");
                        }

                    } else {
                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "Site/Dept");
                            .call(map1, "site-dept");
                        }

                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "Multiple Users");
                            .call(map1, "multiple-users");
                        }

                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("3")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "User");
                            .call(map1, "user");
                        }

                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("4")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "User");
                            .call(map1, "user");
                        }

                    }


                    if (isNewIncident) {
                        // Add 'Time OMi Event Created' to activity log
                        if (DefaultGroovyMethods.asBoolean(event.getTimeCreated())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_TIME_CREATED).append("\n").append(dateFormatter.format(event.getTimeCreated())).append("\n");
                        }

                        // Add 'Time OMi Event Received' to activity log
                        if (DefaultGroovyMethods.asBoolean(event.getTimeReceived())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_TIME_RECEIVED).append("\n").append(dateFormatter.format(event.getTimeReceived())).append("\n");
                        }

                        // set the external process id, category, subCategory and related CI for new Incidents
                        .call(externalRefId);

                        // set the related CI on new incidents
                        final OprNodeReference nodeRef = event.getNode();
                        final OprRelatedCi relatedCi = event.getRelatedCi();
                        final String dnsName = getDnsName(event);
                        // Astelit's Default Related CI Name
                        final String astelitRelatedCI = relatedCi.getConfigurationItem().getCiName();

                        if (relatedCi != null && !UseNodeCI) {
                            // send 'is_registered_for' CI information using event related CI
                            LinkedHashMap<String, GString> map1 = new LinkedHashMap<String, GString>(1);
                            map1.put("target_role", String.valueOf(getProperty("CONFIGURATION_ITEM_ROLE")));
                            .call(map1, new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                public Object doCall(Object it) {
                                    if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getGlobalId())).
                                    call(relatedCi.getConfigurationItem().getGlobalId());
                                    .call(CI_TARGET_TYPE);
                                    return.
                                    call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                        public Object doCall(Object it) {
                                            if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getCiType())).
                                            call(relatedCi.getConfigurationItem().getCiType());
                                            if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getId())).
                                            call(relatedCi.getConfigurationItem().getId());

                                            //if (relatedCi.configurationItem.ciName)
                                            //builder."${CI_NAME_TAG}"(relatedCi.configurationItem.ciName)

                                            //Astelit Related CI
                                            if (astl_logical_name.get() != null) {
                                                .call(astl_logical_name.get());
                                                .call(astl_logical_name.get());
                                            } else {
                                                if (DefaultGroovyMethods.asBoolean(astelitRelatedCI)).
                                                call(astelitRelatedCI);
                                                .call(astelitRelatedCI);
                                            }


                                            if (DefaultGroovyMethods.asBoolean(dnsName)).call(dnsName);
                                            if (nodeRef != null && !relatedCi.getConfigurationItem().getId().equals(nodeRef.getNode().getId())) {
                                                // send 'is_hosted_on' CI information using event node CI
                                                LinkedHashMap<String, GString> map2 = new LinkedHashMap<String, GString>(1);
                                                map2.put("target_role", String.valueOf(getProperty("NODE_ITEM_ROLE")));
                                                return.
                                                call(map2, new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                                    public Object doCall(Object it) {
                                                        if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getGlobalId())).
                                                        call(nodeRef.getNode().getGlobalId());
                                                        .call(nodeRef.getNode().getSelfType().toString());
                                                        return.
                                                        call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                                            public Object doCall(Object it) {
                                                                if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiType())).
                                                                call(nodeRef.getNode().getCiType());
                                                                if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getId())).
                                                                call(nodeRef.getNode().getId());
                                                                if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiName())).
                                                                call(nodeRef.getNode().getCiName());
                                                                if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiDisplayLabel())).
                                                                call(nodeRef.getNode().getCiDisplayLabel());
                                                                if (DefaultGroovyMethods.asBoolean(dnsName)) return.
                                                                call(dnsName);
                                                            }

                                                            public void doCall() {
                                                                doCall(null);
                                                            }

                                                        });
                                                    }

                                                    public void doCall() {
                                                        doCall(null);
                                                    }

                                                });
                                            }

                                        }

                                        public void doCall() {
                                            doCall(null);
                                        }

                                    });
                                }

                                public void doCall() {
                                    doCall(null);
                                }

                            });

                            activityLog.append("\n");
                            activityLog.append(ACTIVITY_LOG_RELATED_CI);
                            if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getCiDisplayLabel()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_LABEL).append(relatedCi.getConfigurationItem().getCiDisplayLabel());

                            //          if (relatedCi.configurationItem.ciName)
                            //            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
                            if (DefaultGroovyMethods.asBoolean(astl_logical_name.get())) {
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_NAME).append(astl_logical_name.get());
                            } else {
                                if (DefaultGroovyMethods.asBoolean(astelitRelatedCI))
                                    activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_NAME).append(astelitRelatedCI);
                            }


                            if (m_oprVersion >= 920 && relatedCi.getConfigurationItem().getCiTypeLabel())
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).append(relatedCi.getConfigurationItem().getCiTypeLabel());
                            if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getCiType()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_TYPE).append(relatedCi.getConfigurationItem().getCiType());
                            if (event.getDrilldownUrl() && relatedCi.getConfigurationItem().getId()) {
                                final URL eventUrl = event.getDrilldownUrl();
                                final URL ciUrl = new URL(eventUrl.getProtocol(), eventUrl.getHost(), eventUrl.getPort(), BSM_CI_DRILLDOWN_PATH + relatedCi.getConfigurationItem().getId());
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString());
                            }

                            if (DefaultGroovyMethods.asBoolean(dnsName))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName);
                            activityLog.append("\n");
                        } else if (nodeRef != null) {
                            // send 'is_registered_for' CI information using event node CI
                            LinkedHashMap<String, GString> map1 = new LinkedHashMap<String, GString>(1);
                            map1.put("target_role", String.valueOf(getProperty("CONFIGURATION_ITEM_ROLE")));
                            .call(map1, new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                public Object doCall(Object it) {
                                    if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getGlobalId())).
                                    call(nodeRef.getNode().getGlobalId());
                                    .call(CI_TARGET_TYPE);
                                    return.
                                    call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                        public Object doCall(Object it) {
                                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiType())).
                                            call(nodeRef.getNode().getCiType());
                                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getId())).
                                            call(nodeRef.getNode().getId());
                                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiName())).
                                            call(nodeRef.getNode().getCiName());
                                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiDisplayLabel())).
                                            call(nodeRef.getNode().getCiDisplayLabel());
                                            if (DefaultGroovyMethods.asBoolean(dnsName)) return.call(dnsName);
                                        }

                                        public void doCall() {
                                            doCall(null);
                                        }

                                    });
                                }

                                public void doCall() {
                                    doCall(null);
                                }

                            });
                            activityLog.append("\n");
                            activityLog.append(ACTIVITY_LOG_RELATED_CI);
                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiDisplayLabel()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_LABEL).append(nodeRef.getNode().getCiDisplayLabel());
                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiName()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_NAME).append(nodeRef.getNode().getCiName());
                            if (m_oprVersion >= 920 && nodeRef.getNode().getCiTypeLabel())
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).append(nodeRef.getNode().getCiTypeLabel());
                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiType()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_TYPE).append(nodeRef.getNode().getCiType());
                            if (event.getDrilldownUrl() && nodeRef.getNode().getId()) {
                                final URL eventUrl = event.getDrilldownUrl();
                                final URL ciUrl = new URL(eventUrl.getProtocol(), eventUrl.getHost(), eventUrl.getPort(), BSM_CI_DRILLDOWN_PATH + nodeRef.getNode().getId());
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString());
                            }

                            if (DefaultGroovyMethods.asBoolean(dnsName))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName);
                            activityLog.append("\n");
                        }

                        // check for most affected business service in 9.21 or greater
                        if (m_oprVersion > 920) setBusinessService(event, builder, activityLog);
                        else {
                            if ((RTSM_QUERY_MAX_STEPS > 0) && (relatedCi != null) && relatedCi.getTargetId())
                                setBusinessServicePre921(relatedCi.getConfigurationItem(), event.getDrilldownUrl(), builder, activityLog);
                        }


                        if (DefaultGroovyMethods.asBoolean(astl_category.get())) {
                            .call(astl_category.get());
                            activityLog.append("\n").append(ACTIVITY_LOG_CATEGORY).append("\n").append(astl_category.get()).append("\n");
                        } else {
                            .call(ASTELIT_CATEGORY);
                            activityLog.append("\n").append(ACTIVITY_LOG_CATEGORY).append("\n").append(ASTELIT_CATEGORY).append("\n");
                        }


                        if (DefaultGroovyMethods.asBoolean(astl_sub_category.get())) {
                            .call(astl_sub_category.get());
                            activityLog.append("\n").append(ACTIVITY_LOG_SUBCATEGORY).append("\n").append(astl_sub_category.get()).append("\n");
                        } else {
                            .call(ASTELIT_SUB_CATEGORY);
                            activityLog.append("\n").append(ACTIVITY_LOG_SUBCATEGORY).append("\n").append(ASTELIT_SUB_CATEGORY).append("\n");
                        }


                        if (DefaultGroovyMethods.asBoolean(event.getApplication())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_APPLICATION).append("\n").append(event.getApplication()).append("\n");
                        }

                        if (DefaultGroovyMethods.asBoolean(event.getObject())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_OBJECT).append("\n").append(event.getObject()).append("\n");
                        }

                        // Add 'Original Data' to activity log
                        if (DefaultGroovyMethods.asBoolean(event.getOriginalData())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_ORIGINAL_DATA).append("\n").append(event.getOriginalData()).append("\n");
                        }

                    }


                    // Add 'Time OMi Event State Changed' to activity log
                    if (event.getTimeStateChanged() && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("time_state_changed"))) {
                        activityLog.append("\n").append(ACTIVITY_LOG_TIME_STATE_CHANGED).append("\n").append(dateFormatter.format(event.getTimeStateChanged())).append(" : ").append(event.getState()).append("\n");
                    }


                    // check title
                    if (title.get() && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("title"))).
                    call(title.get());

                    if (event.getTitle() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("title"))) {
                        // synchronize the title to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_TITLE).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_TITLE_CHANGE);
                        activityLog.append(event.getTitle().trim());
                        activityLog.append("\n");
                    }


                    // check description
                    if (description.get() && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("description"))).
                    call(description.get());

                    if (event.getDescription() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("description"))) {
                        // synchronize the description to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_DESCRIPTION).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_DESCRIPTION_CHANGE);
                        activityLog.append(event.getDescription().trim());
                        activityLog.append("\n");
                    }


                    // check solution
                    if (event.getSolution() && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("solution"))).
                    call(event.getSolution());

                    if (event.getSolution() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("solution"))) {
                        // synchronize the solution to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_SOLUTION).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_SOLUTION_CHANGE);
                        activityLog.append(event.getSolution().trim());
                        activityLog.append("\n");
                    }


                    // check assigned user
                    final OprUser user = event.getAssignedUser();
                    final OprUser user1 = event.getAssignedUser();
                    final OprUser user2 = event.getAssignedUser();
                    if ((((user == null ? null : user.getId()) >= 0) && ((user1 == null ? null : user1.getUserName()) || (user2 == null ? null : user2.getLoginName()))) && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_user"))).
                    call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                        public Object doCall(Object it) {
                            return.call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                public Object doCall(Object it) {
                                    final OprUser user3 = event.getAssignedUser();
                                    if (DefaultGroovyMethods.asBoolean((user3 == null ? null : user3.getUserName()))).
                                    call(event.getAssignedUser().getUserName());
                                    final OprUser user4 = event.getAssignedUser();
                                    if (DefaultGroovyMethods.asBoolean((user4 == null ? null : user4.getLoginName())))
                                        return.call(event.getAssignedUser().getLoginName());
                                }

                                public void doCall() {
                                    doCall(null);
                                }

                            });
                        }

                        public void doCall() {
                            doCall(null);
                        }

                    });

                    // set the contact name to the BSM assigned user
                    final OprUser user3 = event.getAssignedUser();
                    if ((user3 == null ? null : user3.getUserName()) && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_user"))) {
                        // synchronize the assigned user to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_ASSIGNED_USER).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_ASSIGNED_USER_CHANGE);
                        final OprUser user4 = event.getAssignedUser();
                        if ((user4 == null ? null : user4.getId()) < 0) activityLog.append(ACTIVITY_LOG_UNASSIGNED);
                        else activityLog.append(event.getAssignedUser().getUserName().trim());
                        activityLog.append("\n");
                    }


                    // check assigned group
                    if (astl_assignment_group.get() && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_group"))).
                    call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                        public Object doCall(Object it) {
                            return.call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                public Object doCall(Object it) {
                                    .call(astl_assignment_group.get());
                                    return.call(astl_assignment_group.get());
                                }

                                public void doCall() {
                                    doCall(null);
                                }

                            });
                        }

                        public void doCall() {
                            doCall(null);
                        }

                    });

                    // set the functional group name to the BSM assigned group
                    final OprGroup group = event.getAssignedGroup();
                    if ((group == null ? null : group.getName()) && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_group"))) {
                        // synchronize the assigned user to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE);

                        if (DefaultGroovyMethods.asBoolean(astl_assignment_group.get())) {
                            activityLog.append(astl_assignment_group.get());
                            activityLog.append("\n");
                        } else {
                            final OprGroup group1 = event.getAssignedGroup();
                            if ((group1 == null ? null : group1.getId()) < 0)
                                activityLog.append(ACTIVITY_LOG_UNASSIGNED);
                            else activityLog.append(event.getAssignedGroup().getName().trim());
                            activityLog.append("\n");
                        }

                    }


                    // check state
                    if (event.getState() && (isNewIncident || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("state")) && (syncAllOPRStatesToSM || SyncOPRStatesToSM.contains(event.getState()))))) {
                        String status = MapOPR2SMStatus.get(event.getState());
                        .call(status);
                        if ("closed".equals(status)) {
                            .call(SMCompletionCode);
                        }

                    }


                    if (event.getState() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("state"))) {
                        // synchronize the state to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_STATE).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_STATE_CHANGE);
                        activityLog.append(event.getState().trim());
                        activityLog.append("\n");
                    }


                    // check urgency/severity

                    if (DefaultGroovyMethods.asBoolean(astl_priority.get())) {
                        if (astl_priority.get() && (isNewIncident || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("severity")) && (syncAllOPRSeveritiesToSM || SyncOPRSeveritiesToSM.contains(astl_priority.get()))))).
                        call(astl_priority.get());

                        if (astl_priority.get() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("severity"))) {
                            // synchronize the severity to the incident activity log
                            activityLog.append("\n");
                            activityLog.append(ACTIVITY_LOG_SEVERITY).append("\n");
                            if (!isNewIncident) activityLog.append(ACTIVITY_LOG_SEVERITY_CHANGE);
                            activityLog.append(event.getSeverity());
                            activityLog.append("\n");
                        }

                    } else {
                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
                            astl_urgency.set("2");
                        }

                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
                            astl_urgency.set("3");
                        }

                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("3")) {
                            astl_urgency.set("4");
                        }

                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("4")) {
                            astl_urgency.set("4");
                        }


                        if (astl_urgency.get() && (isNewIncident || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("severity")) && (syncAllOPRSeveritiesToSM || SyncOPRSeveritiesToSM.contains(astl_urgency.get()))))).
                        call(astl_urgency.get());

                        if (astl_urgency.get() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("severity"))) {
                            // synchronize the severity to the incident activity log
                            activityLog.append("\n");
                            activityLog.append(ACTIVITY_LOG_SEVERITY).append("\n");
                            if (!isNewIncident) activityLog.append(ACTIVITY_LOG_SEVERITY_CHANGE);
                            activityLog.append(event.getSeverity());
                            activityLog.append("\n");
                        }

                    }


                    // check priority
                    //if (event.priority
                    //&& (isNewIncident
                    //|| ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("priority"))
                    //&& (syncAllOPRPrioritiesToSM || SyncOPRPrioritiesToSM.contains(event.priority)))))
                    //builder."${PRIORITY_TAG}"(MapOPR2SMPriority[event.priority])

                    if (event.getPriority() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("priority"))) {
                        // synchronize the priority to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_PRIORITY).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_PRIORITY_CHANGE);
                        activityLog.append(event.getPriority());
                        activityLog.append("\n");
                    }


                    // set is_recorded_by (opened.by) to "admin" or Control Transfer initiator
                    if (isNewIncident) {
                        final OprControlTransferInfo to = event.getControlTransferredTo();
                        boolean initiatedBySystem = ((to == null ? null : to.getInitiatedBy()) == null) || ("system".equals(event.getControlTransferredTo().getInitiatedBy()));
                        final String recorder = initiatedBySystem ? BSM_ADMINISTRATOR_LOGIN_NAME : event.getControlTransferredTo().getInitiatedBy();
                        .call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public Object doCall(Object it) {
                                return.
                                call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                    public Object doCall(Object it) {
                                        .call(recorder);
                                        return.call(recorder);
                                    }

                                    public void doCall() {
                                        doCall(null);
                                    }

                                });
                            }

                            public void doCall() {
                                doCall(null);
                            }

                        });
                        // Add initiator info to Activity Log
                        OprForwardingInfo forwardingInfo = event.getForwardingInfo(m_connectedServerId);
                        if (initiatedBySystem && (m_oprVersion >= 910) && (forwardingInfo == null ? null : forwardingInfo.getRuleName())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_INITIATED_BY).append("\n").append(ACTIVITY_LOG_INITIATED_BY_RULE).append(forwardingInfo.getRuleName());
                        } else {
                            activityLog.append("\n").append(ACTIVITY_LOG_INITIATED_BY).append("\n").append(ACTIVITY_LOG_INITIATED_BY_USER).append(recorder);
                        }

                        activityLog.append("\n");
                    }


                    if (event.getControlTransferredTo() && !m_node.equalsIgnoreCase(event.getControlTransferredTo().getDnsName()) && OprControlTransferStateEnum.transferred.name().equals(event.getControlTransferredTo().getState()) && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("control_transferred_to"))) {
                        // synchronize the priority to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_CONTROL_TRANSFERRED_TO).append("\n");
                        activityLog.append(event.getControlTransferredTo().getDnsName()).append(":").append(event.getControlTransferredTo().getState());
                        activityLog.append("\n");
                    }


                    // check if there are any annotations to add to the activity log
                    if ((event.getAnnotations() != null) && (isNewIncident || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("annotation"))) {
                        // append any annotations
                        final OprAnnotationList annotations = event.getAnnotations();
                        DefaultGroovyMethods.each((annotations == null ? null : annotations.getAnnotations()), new Closure<StringBuffer>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public StringBuffer doCall(OprAnnotation annotation) {
                                final String text = annotation.getText();
                                if (text.length() > 0) {
                                    final String date = dateFormatter.format(annotation.getTimeCreated());
                                    final String author = annotation.getAuthor();
                                    activityLog.append("\n");
                                    activityLog.append(ACTIVITY_LOG_ANNOTATION).append("\n - " + date + " - " + author + " - " + text);
                                    return activityLog.append("\n");
                                }

                            }

                        });
                    }


                    // check if there are any custom attributes to add to the activity log

                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {
                        m_log.invokeMethod("debug", new Object[]{"We are inside toExternalEvent method"});

                        if (!m_OPR2SMCustomAttribute.isEmpty() && (event.getCustomAttributes() != null)) {
                            m_log.invokeMethod("debug", new Object[]{"We passed (!m_OPR2SMCustomAttribute.isEmpty() && (event.customAttributes != null)) condition"});
                        }

                        debugOprEvent(event, (Log) m_log, 2849);
                    }


                    //TODO check oprational device custom attribute
                    if (!m_OPR2SMCustomAttribute.isEmpty() && (event.getCustomAttributes() != null)) {
                        DefaultGroovyMethods.each(event.getCustomAttributes().getCustomAttributes(), new Closure<StringBuffer>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public StringBuffer doCall(final OprCustomAttribute customAttribute) {
                                final String caName = customAttribute.getName().toLowerCase(LOCALE);

                                if (m_OPR2SMCustomAttribute.containsKey(caName)) {
                                    final String smIncidentProperty = m_OPR2SMCustomAttribute.get(caName);
                                    if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean()) {
                                        m_log.invokeMethod("debug", new Object[]{"We passed custom attribute conditions"});
                                        m_log.invokeMethod("debug", new Object[]{"Now we processing CA name: " + customAttribute.getName() + " with value " + customAttribute.getValue()});
                                    }

                                    // synchronize this CA to SM
                                    if (ACTIVITY_LOG_TAG.equals(smIncidentProperty)) {
                                        // synchronize the CA to the SM incident activity log
                                        activityLog.append("\n");
                                        activityLog.append(ACTIVITY_LOG_CA).append("\n" + customAttribute.getName() + "=" + customAttribute.getValue());
                                        return activityLog.append("\n");
                                    } else {
                                        // synchronize to the specified SM incident property
                                        return.call(customAttribute.getValue());
                                    }

                                }

                            }

                        });
                    }


                    final Reference<String> drilldownUrl = new Reference<String>(event.getDrilldownUrl());
                    if (drilldownUrl.get() && drilldownUrl.get().lastIndexOf("=") > 0)
                        drilldownUrl.set(drilldownUrl.get().substring(0, drilldownUrl.get().lastIndexOf("=") + 1));

                    if (DefaultGroovyMethods.asBoolean(event.getCause())) {
                        if (DefaultGroovyMethods.asBoolean(causeExternalRefId)) {
                            if (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("cause")) {
                                LinkedHashMap<String, GString> map1 = new LinkedHashMap<String, GString>(1);
                                map1.put("target_role", String.valueOf(getProperty("IS_CAUSED_BY_ROLE")));
                                .
                                call(map1, new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                    public Object doCall(Object it) {
                                        return.call(causeExternalRefId);
                                    }

                                    public void doCall() {
                                        doCall(null);
                                    }

                                });
                            }

                            if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
                                // synchronize the SM cause to the incident activity log
                                activityLog.append("\n");
                                activityLog.append(ACTIVITY_LOG_CAUSE).append("\n").append(causeExternalRefId);
                                activityLog.append("\n");
                            }

                        } else if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
                            // synchronize the OPR cause to the incident activity log
                            activityLog.append("\n");
                            final String causeTitle = event.getCause().getTitle();
                            final String causeUrl = DefaultGroovyMethods.asBoolean((drilldownUrl.get())) ? drilldownUrl.get() + event.getCause().getTargetId() : null;
                            if (DefaultGroovyMethods.asBoolean(causeUrl))
                                activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append(causeTitle + "\n\t" + causeUrl);
                            else activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append(causeTitle);
                            activityLog.append("\n");
                        }

                    }


                    if ((event.getSymptoms() != null) && (isNewIncident || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("symptom"))) {
                        // synchronize the OPR symptom to the incident activity log
                        DefaultGroovyMethods.each(event.getSymptoms().getEventReferences(), new Closure<StringBuffer>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public StringBuffer doCall(OprSymptomReference symptomRef) {
                                activityLog.append("\n");
                                final String symptomTitle = symptomRef.getTitle();
                                final String symptomUrl = DefaultGroovyMethods.asBoolean((drilldownUrl.get())) ? drilldownUrl.get() + symptomRef.getTargetId() : null;
                                if (DefaultGroovyMethods.asBoolean(symptomUrl))
                                    activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append(symptomTitle + "\n\t" + symptomUrl);
                                else activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append(symptomTitle);
                                return activityLog.append("\n");
                            }

                        });
                    }


                    if (duplicateChange && (isNewIncident || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("duplicate_count"))) {
                        final Integer previousCount = (Integer) duplicateChange.getPreviousValue();
                        final Integer currentCount = (Integer) duplicateChange.getCurrentValue();

                        if (currentCount > 0) {
                            // synchronize the duplicate count to the incident activity log
                            if (previousCount == null) {
                                activityLog.append("\n");
                                activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n").append(currentCount);
                            } else {
                                activityLog.append("\n");
                                activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n");
                                activityLog.append(ACTIVITY_LOG_PREVIOUS + " " + String.valueOf(previousCount) + " " + ACTIVITY_LOG_CURRENT + " " + String.valueOf(currentCount));
                            }

                            activityLog.append("\n");
                        }

                    }


                    // set any activityLog
                    if (activityLog.length() > 0) {
                        return.call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public Object doCall(Object it) {
                                return.call(activityLog.toString());
                            }

                            public void doCall() {
                                doCall(null);
                            }

                        });
                    }

                }

                public void doCall() {
                    doCall(null);
                }

            });
            final String output = writer.toString();
            return output;

        }

//##################################### END ASTELIT CUSTOM SECTION #####################################
        //TODO Default section
//##################################### DEFAULT SECTION #####################################
        if (default_flag) {

            // get the title & description.
            final Reference<String> title = new Reference<String>((event.getTitle() && event.getTitle().trim()) ? event.getTitle().trim().replace("\r", "\n") : null);
            final Reference<String> description = new Reference<String>((event.getDescription() && event.getDescription().trim()) ? event.getDescription().trim() : null);

            if (title.get() && (title.get().length() > 256 || DefaultGroovyMethods.contains(title.get(), "\n"))) {
                // truncate the title and put it in the description
                if (DefaultGroovyMethods.contains(title.get(), "\n")) title.set(title.get().split("\n")[0].trim());
                if (title.get().length() > 256) title.set(title.get().substring(0, 252) + "...");
                if (!DefaultGroovyMethods.asBoolean(description.get())) description.set(event.getTitle().trim());
                else description.set(event.getTitle().trim() + "\n" + event.getDescription().trim());
            }


            // if the description is not set on Incident creation then set it to some default value
            if (isNewIncident && ((description.get() == null) || (description.get().trim().length() == 0)))
                description.set(event.getTitle());
            // create the XML payload using the MarkupBuilder
            final StringWriter writer = new StringWriter();
            final MarkupBuilder builder = new MarkupBuilder(writer);
            final StringBuffer activityLog = new StringBuffer();

            LinkedHashMap<String, GString> map = new LinkedHashMap<String, GString>(4);
            map.put("relationships_included", String.valueOf(getProperty("INCIDENT_XML_RELATIONSHIPS")));
            map.put("type", String.valueOf(getProperty("INCIDENT_XML_TYPE")));
            map.put("version", String.valueOf(getProperty("INCIDENT_XML_VERSION")));
            map.put("xmlns", String.valueOf(getProperty("INCIDENT_XML_NAMESPACE")));
            .call(map, new Closure<Object>(this, this) {
                public Object doCall(Object it) {

                    ((MarkupBuilder) builder).it_process_category(IT_PROCESS_CATEGORY);
                    ((MarkupBuilder) builder).incident_type(INCIDENT_TYPE);
                    if (SpecifyActiveProcess) ((MarkupBuilder) builder).active_process("true");
                    //TODO remove this custom attribute
                    .call(astl_operational_device);

                    activityLog.append("\n").append(ACTIVITY_LOG_OPERATIONAL_DATA).append("\n").append(astl_operational_device).append("\n");

                    if (SpecifyImpactScope) {
                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "Site/Dept");
                            .call(map1, "site-dept");
                        }

                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "Multiple Users");
                            .call(map1, "multiple-users");
                        }

                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("3")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "User");
                            .call(map1, "user");
                        }

                        if (MapOPR2SMUrgency.get(event.getSeverity()).equals("4")) {
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
                            map1.put("label", "User");
                            .call(map1, "user");
                        }

                    }


                    if (isNewIncident) {
                        // Add 'Time OMi Event Created' to activity log
                        if (DefaultGroovyMethods.asBoolean(event.getTimeCreated())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_TIME_CREATED).append("\n").append(dateFormatter.format(event.getTimeCreated())).append("\n");
                        }

                        // Add 'Time OMi Event Received' to activity log
                        if (DefaultGroovyMethods.asBoolean(event.getTimeReceived())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_TIME_RECEIVED).append("\n").append(dateFormatter.format(event.getTimeReceived())).append("\n");
                        }

                        // set the external process id, category, subCategory and related CI for new Incidents
                        .call(externalRefId);

                        // set the related CI on new incidents
                        final OprNodeReference nodeRef = event.getNode();
                        final OprRelatedCi relatedCi = event.getRelatedCi();
                        final String dnsName = getDnsName(event);
                        // Astelit's Default Related CI Name
                        final String astelitRelatedCI = relatedCi.getConfigurationItem().getCiName();

                        if (relatedCi != null && !UseNodeCI) {
                            // send 'is_registered_for' CI information using event related CI
                            LinkedHashMap<String, GString> map1 = new LinkedHashMap<String, GString>(1);
                            map1.put("target_role", String.valueOf(getProperty("CONFIGURATION_ITEM_ROLE")));
                            .call(map1, new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                public Object doCall(Object it) {
                                    if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getGlobalId())).
                                    call(relatedCi.getConfigurationItem().getGlobalId());
                                    .call(CI_TARGET_TYPE);
                                    return.
                                    call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                        public Object doCall(Object it) {
                                            if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getCiType())).
                                            call(relatedCi.getConfigurationItem().getCiType());
                                            if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getId())).
                                            call(relatedCi.getConfigurationItem().getId());

                                            //if (relatedCi.configurationItem.ciName)
                                            //builder."${CI_NAME_TAG}"(relatedCi.configurationItem.ciName)
                                            //Astelit Related CI
                                            if (DefaultGroovyMethods.asBoolean(astelitRelatedCI)).
                                            call(astelitRelatedCI);

                                            if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getCiDisplayLabel())).
                                            call(relatedCi.getConfigurationItem().getCiDisplayLabel());
                                            if (DefaultGroovyMethods.asBoolean(dnsName)).call(dnsName);
                                            if (nodeRef != null && !relatedCi.getConfigurationItem().getId().equals(nodeRef.getNode().getId())) {
                                                // send 'is_hosted_on' CI information using event node CI
                                                LinkedHashMap<String, GString> map2 = new LinkedHashMap<String, GString>(1);
                                                map2.put("target_role", String.valueOf(getProperty("NODE_ITEM_ROLE")));
                                                return.
                                                call(map2, new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                                    public Object doCall(Object it) {
                                                        if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getGlobalId())).
                                                        call(nodeRef.getNode().getGlobalId());
                                                        .call(nodeRef.getNode().getSelfType().toString());
                                                        return.
                                                        call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                                            public Object doCall(Object it) {
                                                                if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiType())).
                                                                call(nodeRef.getNode().getCiType());
                                                                if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getId())).
                                                                call(nodeRef.getNode().getId());
                                                                if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiName())).
                                                                call(nodeRef.getNode().getCiName());
                                                                if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiDisplayLabel())).
                                                                call(nodeRef.getNode().getCiDisplayLabel());
                                                                if (DefaultGroovyMethods.asBoolean(dnsName)) return.
                                                                call(dnsName);
                                                            }

                                                            public void doCall() {
                                                                doCall(null);
                                                            }

                                                        });
                                                    }

                                                    public void doCall() {
                                                        doCall(null);
                                                    }

                                                });
                                            }

                                        }

                                        public void doCall() {
                                            doCall(null);
                                        }

                                    });
                                }

                                public void doCall() {
                                    doCall(null);
                                }

                            });
                            activityLog.append("\n");
                            activityLog.append(ACTIVITY_LOG_RELATED_CI);
                            if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getCiDisplayLabel()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_LABEL).append(relatedCi.getConfigurationItem().getCiDisplayLabel());

                            //          if (relatedCi.configurationItem.ciName)
                            //            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
                            if (DefaultGroovyMethods.asBoolean(astelitRelatedCI))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_NAME).append(astelitRelatedCI);

                            if (m_oprVersion >= 920 && relatedCi.getConfigurationItem().getCiTypeLabel())
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).append(relatedCi.getConfigurationItem().getCiTypeLabel());
                            if (DefaultGroovyMethods.asBoolean(relatedCi.getConfigurationItem().getCiType()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_TYPE).append(relatedCi.getConfigurationItem().getCiType());
                            if (event.getDrilldownUrl() && relatedCi.getConfigurationItem().getId()) {
                                final URL eventUrl = event.getDrilldownUrl();
                                final URL ciUrl = new URL(eventUrl.getProtocol(), eventUrl.getHost(), eventUrl.getPort(), BSM_CI_DRILLDOWN_PATH + relatedCi.getConfigurationItem().getId());
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString());
                            }

                            if (DefaultGroovyMethods.asBoolean(dnsName))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName);
                            activityLog.append("\n");
                        } else if (nodeRef != null) {
                            // send 'is_registered_for' CI information using event node CI
                            LinkedHashMap<String, GString> map1 = new LinkedHashMap<String, GString>(1);
                            map1.put("target_role", String.valueOf(getProperty("CONFIGURATION_ITEM_ROLE")));
                            .call(map1, new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                public Object doCall(Object it) {
                                    if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getGlobalId())).
                                    call(nodeRef.getNode().getGlobalId());
                                    .call(CI_TARGET_TYPE);
                                    return.
                                    call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                        public Object doCall(Object it) {
                                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiType())).
                                            call(nodeRef.getNode().getCiType());
                                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getId())).
                                            call(nodeRef.getNode().getId());
                                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiName())).
                                            call(nodeRef.getNode().getCiName());
                                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiDisplayLabel())).
                                            call(nodeRef.getNode().getCiDisplayLabel());
                                            if (DefaultGroovyMethods.asBoolean(dnsName)) return.call(dnsName);
                                        }

                                        public void doCall() {
                                            doCall(null);
                                        }

                                    });
                                }

                                public void doCall() {
                                    doCall(null);
                                }

                            });
                            activityLog.append("\n");
                            activityLog.append(ACTIVITY_LOG_RELATED_CI);
                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiDisplayLabel()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_LABEL).append(nodeRef.getNode().getCiDisplayLabel());
                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiName()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_NAME).append(nodeRef.getNode().getCiName());
                            if (m_oprVersion >= 920 && nodeRef.getNode().getCiTypeLabel())
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).append(nodeRef.getNode().getCiTypeLabel());
                            if (DefaultGroovyMethods.asBoolean(nodeRef.getNode().getCiType()))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_TYPE).append(nodeRef.getNode().getCiType());
                            if (event.getDrilldownUrl() && nodeRef.getNode().getId()) {
                                final URL eventUrl = event.getDrilldownUrl();
                                final URL ciUrl = new URL(eventUrl.getProtocol(), eventUrl.getHost(), eventUrl.getPort(), BSM_CI_DRILLDOWN_PATH + nodeRef.getNode().getId());
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString());
                            }

                            if (DefaultGroovyMethods.asBoolean(dnsName))
                                activityLog.append("\n").append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName);
                            activityLog.append("\n");
                        }

                        // check for most affected business service in 9.21 or greater
                        if (m_oprVersion > 920) setBusinessService(event, builder, activityLog);
                        else {
                            if ((RTSM_QUERY_MAX_STEPS > 0) && (relatedCi != null) && relatedCi.getTargetId())
                                setBusinessServicePre921(relatedCi.getConfigurationItem(), event.getDrilldownUrl(), builder, activityLog);
                        }


                        if (DefaultGroovyMethods.asBoolean(event.getCategory())) {
                            //          builder."${CATEGORY_TAG}"(event.category)
                            .call(ASTELIT_CATEGORY);
                            activityLog.append("\n").append(ACTIVITY_LOG_CATEGORY).append("\n").append(event.getCategory()).append("\n");
                        }

                        if (DefaultGroovyMethods.asBoolean(event.getSubCategory())) {
                            //          builder."${SUB_CATEGORY_TAG}"(event.subCategory)
                            .call(ASTELIT_SUB_CATEGORY);
                            activityLog.append("\n").append(ACTIVITY_LOG_SUBCATEGORY).append("\n").append(event.getSubCategory()).append("\n");
                        }

                        if (DefaultGroovyMethods.asBoolean(event.getApplication())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_APPLICATION).append("\n").append(event.getApplication()).append("\n");
                        }

                        if (DefaultGroovyMethods.asBoolean(event.getObject())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_OBJECT).append("\n").append(event.getObject()).append("\n");
                        }

                        // Add 'Original Data' to activity log
                        if (DefaultGroovyMethods.asBoolean(event.getOriginalData())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_ORIGINAL_DATA).append("\n").append(event.getOriginalData()).append("\n");
                        }

                    }


                    // Add 'Time OMi Event State Changed' to activity log
                    if (event.getTimeStateChanged() && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("time_state_changed"))) {
                        activityLog.append("\n").append(ACTIVITY_LOG_TIME_STATE_CHANGED).append("\n").append(dateFormatter.format(event.getTimeStateChanged())).append(" : ").append(event.getState()).append("\n");
                    }


                    // check title
                    if (title.get() && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("title"))).
                    call(title.get());

                    if (event.getTitle() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("title"))) {
                        // synchronize the title to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_TITLE).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_TITLE_CHANGE);
                        activityLog.append(event.getTitle().trim());
                        activityLog.append("\n");
                    }


                    // check description
                    if (description.get() && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("description"))).
                    call(description.get());

                    if (event.getDescription() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("description"))) {
                        // synchronize the description to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_DESCRIPTION).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_DESCRIPTION_CHANGE);
                        activityLog.append(event.getDescription().trim());
                        activityLog.append("\n");
                    }


                    // check solution
                    if (event.getSolution() && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("solution"))).
                    call(event.getSolution());

                    if (event.getSolution() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("solution"))) {
                        // synchronize the solution to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_SOLUTION).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_SOLUTION_CHANGE);
                        activityLog.append(event.getSolution().trim());
                        activityLog.append("\n");
                    }


                    // check assigned user
//		  if (((event.assignedUser?.id >= 0) && (event.assignedUser?.userName || event.assignedUser?.loginName))
//			  && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_user")))
                    final OprUser user = event.getAssignedUser();
                    final OprUser user1 = event.getAssignedUser();
                    final OprUser user2 = event.getAssignedUser();
                    if (((user == null ? null : user.getId()) >= 0) && ((user1 == null ? null : user1.getUserName()) || (user2 == null ? null : user2.getLoginName())) && isNewIncident).
                    call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                        public Object doCall(Object it) {
                            return.call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                public Object doCall(Object it) {
                                    final OprUser user3 = event.getAssignedUser();
                                    if (DefaultGroovyMethods.asBoolean((user3 == null ? null : user3.getUserName()))).
                                    call(event.getAssignedUser().getUserName());
                                    final OprUser user4 = event.getAssignedUser();
                                    if (DefaultGroovyMethods.asBoolean((user4 == null ? null : user4.getLoginName())))
                                        return.call(event.getAssignedUser().getLoginName());
                                }

                                public void doCall() {
                                    doCall(null);
                                }

                            });
                        }

                        public void doCall() {
                            doCall(null);
                        }

                    });

                    // set the contact name to the BSM assigned user
                    final OprUser user3 = event.getAssignedUser();
                    if ((user3 == null ? null : user3.getUserName()) && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_user"))) {
                        // synchronize the assigned user to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_ASSIGNED_USER).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_ASSIGNED_USER_CHANGE);
                        final OprUser user4 = event.getAssignedUser();
                        if ((user4 == null ? null : user4.getId()) < 0) activityLog.append(ACTIVITY_LOG_UNASSIGNED);
                        else activityLog.append(event.getAssignedUser().getUserName().trim());
                        activityLog.append("\n");
                    }


                    // check assigned group
                    //if (((event.assignedGroup?.id >= 0) && event.assignedGroup?.name)
                    //&& (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_group")))
                    final OprGroup group = event.getAssignedGroup();
                    final OprGroup group1 = event.getAssignedGroup();
                    if (((group == null ? null : group.getId()) >= 0) && (group1 == null ? null : group1.getName()) && isNewIncident).
                    call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                        public Object doCall(Object it) {
                            return.call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                public Object doCall(Object it) {
                                    .call(event.getAssignedGroup().getName());
                                    return.call(event.getAssignedGroup().getName());
                                }

                                public void doCall() {
                                    doCall(null);
                                }

                            });
                        }

                        public void doCall() {
                            doCall(null);
                        }

                    });

                    // set the functional group name to the BSM assigned group
                    final OprGroup group2 = event.getAssignedGroup();
                    if ((group2 == null ? null : group2.getName()) && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_group"))) {
                        // synchronize the assigned user to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE);
                        final OprGroup group3 = event.getAssignedGroup();
                        if ((group3 == null ? null : group3.getId()) < 0) activityLog.append(ACTIVITY_LOG_UNASSIGNED);
                        else activityLog.append(event.getAssignedGroup().getName().trim());
                        activityLog.append("\n");
                    }


                    // check state
                    if (event.getState() && (isNewIncident || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("state")) && (syncAllOPRStatesToSM || SyncOPRStatesToSM.contains(event.getState()))))) {
                        String status = MapOPR2SMStatus.get(event.getState());
                        .call(status);
                        if ("closed".equals(status)) {
                            .call(SMCompletionCode);
                        }

                    }


                    if (event.getState() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("state"))) {
                        // synchronize the state to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_STATE).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_STATE_CHANGE);
                        activityLog.append(event.getState().trim());
                        activityLog.append("\n");
                    }


                    // check urgency/severity
                    if (MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
                        astl_urgency.set("2");
                    }

                    if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
                        astl_urgency.set("3");
                    }

                    if (MapOPR2SMUrgency.get(event.getSeverity()).equals("3")) {
                        astl_urgency.set("4");
                    }

                    if (MapOPR2SMUrgency.get(event.getSeverity()).equals("4")) {
                        astl_urgency.set("4");
                    }


                    if (astl_urgency.get() && (isNewIncident || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("severity")) && (syncAllOPRSeveritiesToSM || SyncOPRSeveritiesToSM.contains(astl_urgency.get()))))).
                    call(astl_urgency.get());

                    if (astl_urgency.get() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("severity"))) {
                        // synchronize the severity to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_SEVERITY).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_SEVERITY_CHANGE);
                        activityLog.append(event.getSeverity());
                        activityLog.append("\n");
                    }


                    // check priority
                    //if (event.priority
                    //&& (isNewIncident
                    //|| ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("priority"))
                    //&& (syncAllOPRPrioritiesToSM || SyncOPRPrioritiesToSM.contains(event.priority)))))
                    //builder."${PRIORITY_TAG}"(MapOPR2SMPriority[event.priority])

                    if (event.getPriority() && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("priority"))) {
                        // synchronize the priority to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_PRIORITY).append("\n");
                        if (!isNewIncident) activityLog.append(ACTIVITY_LOG_PRIORITY_CHANGE);
                        activityLog.append(event.getPriority());
                        activityLog.append("\n");
                    }


                    // set is_recorded_by (opened.by) to "admin" or Control Transfer initiator
                    if (isNewIncident) {
                        final OprControlTransferInfo to = event.getControlTransferredTo();
                        boolean initiatedBySystem = ((to == null ? null : to.getInitiatedBy()) == null) || ("system".equals(event.getControlTransferredTo().getInitiatedBy()));
                        final String recorder = initiatedBySystem ? BSM_ADMINISTRATOR_LOGIN_NAME : event.getControlTransferredTo().getInitiatedBy();
                        .call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public Object doCall(Object it) {
                                return.
                                call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                    public Object doCall(Object it) {
                                        .call(recorder);
                                        return.call(recorder);
                                    }

                                    public void doCall() {
                                        doCall(null);
                                    }

                                });
                            }

                            public void doCall() {
                                doCall(null);
                            }

                        });
                        // Add initiator info to Activity Log
                        OprForwardingInfo forwardingInfo = event.getForwardingInfo(m_connectedServerId);
                        if (initiatedBySystem && (m_oprVersion >= 910) && (forwardingInfo == null ? null : forwardingInfo.getRuleName())) {
                            activityLog.append("\n").append(ACTIVITY_LOG_INITIATED_BY).append("\n").append(ACTIVITY_LOG_INITIATED_BY_RULE).append(forwardingInfo.getRuleName());
                        } else {
                            activityLog.append("\n").append(ACTIVITY_LOG_INITIATED_BY).append("\n").append(ACTIVITY_LOG_INITIATED_BY_USER).append(recorder);
                        }

                        activityLog.append("\n");
                    }


                    if (event.getControlTransferredTo() && !m_node.equalsIgnoreCase(event.getControlTransferredTo().getDnsName()) && OprControlTransferStateEnum.transferred.name().equals(event.getControlTransferredTo().getState()) && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("control_transferred_to"))) {
                        // synchronize the priority to the incident activity log
                        activityLog.append("\n");
                        activityLog.append(ACTIVITY_LOG_CONTROL_TRANSFERRED_TO).append("\n");
                        activityLog.append(event.getControlTransferredTo().getDnsName()).append(":").append(event.getControlTransferredTo().getState());
                        activityLog.append("\n");
                    }


                    // check if there are any annotations to add to the activity log
                    if ((event.getAnnotations() != null) && (isNewIncident || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("annotation"))) {
                        // append any annotations
                        final OprAnnotationList annotations = event.getAnnotations();
                        DefaultGroovyMethods.each((annotations == null ? null : annotations.getAnnotations()), new Closure<StringBuffer>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public StringBuffer doCall(OprAnnotation annotation) {
                                final String text = annotation.getText();
                                if (text.length() > 0) {
                                    final String date = dateFormatter.format(annotation.getTimeCreated());
                                    final String author = annotation.getAuthor();
                                    activityLog.append("\n");
                                    activityLog.append(ACTIVITY_LOG_ANNOTATION).append("\n - " + date + " - " + author + " - " + text);
                                    return activityLog.append("\n");
                                }

                            }

                        });
                    }


                    // check if there are any custom attributes to add to the activity log
                    if (!m_OPR2SMCustomAttribute.isEmpty() && (event.getCustomAttributes() != null)) {
                        DefaultGroovyMethods.each(event.getCustomAttributes().getCustomAttributes(), new Closure<StringBuffer>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public StringBuffer doCall(final OprCustomAttribute customAttribute) {
                                final String caName = customAttribute.getName().toLowerCase(LOCALE);

                                if (m_OPR2SMCustomAttribute.containsKey(caName)) {
                                    final String smIncidentProperty = m_OPR2SMCustomAttribute.get(caName);
                                    // synchronize this CA to SM
                                    if (ACTIVITY_LOG_TAG.equals(smIncidentProperty)) {
                                        // synchronize the CA to the SM incident activity log
                                        activityLog.append("\n");
                                        activityLog.append(ACTIVITY_LOG_CA).append("\n" + customAttribute.getName() + "=" + customAttribute.getValue());
                                        return activityLog.append("\n");
                                    } else {
                                        // synchronize to the specified SM incident property
                                        return.call(customAttribute.getValue());
                                    }

                                }

                            }

                        });
                    }


                    final Reference<String> drilldownUrl = new Reference<String>(event.getDrilldownUrl());
                    if (drilldownUrl.get() && drilldownUrl.get().lastIndexOf("=") > 0)
                        drilldownUrl.set(drilldownUrl.get().substring(0, drilldownUrl.get().lastIndexOf("=") + 1));

                    if (DefaultGroovyMethods.asBoolean(event.getCause())) {
                        if (DefaultGroovyMethods.asBoolean(causeExternalRefId)) {
                            if (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("cause")) {
                                LinkedHashMap<String, GString> map1 = new LinkedHashMap<String, GString>(1);
                                map1.put("target_role", String.valueOf(getProperty("IS_CAUSED_BY_ROLE")));
                                .
                                call(map1, new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                                    public Object doCall(Object it) {
                                        return.call(causeExternalRefId);
                                    }

                                    public void doCall() {
                                        doCall(null);
                                    }

                                });
                            }

                            if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
                                // synchronize the SM cause to the incident activity log
                                activityLog.append("\n");
                                activityLog.append(ACTIVITY_LOG_CAUSE).append("\n").append(causeExternalRefId);
                                activityLog.append("\n");
                            }

                        } else if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
                            // synchronize the OPR cause to the incident activity log
                            activityLog.append("\n");
                            final String causeTitle = event.getCause().getTitle();
                            final String causeUrl = DefaultGroovyMethods.asBoolean((drilldownUrl.get())) ? drilldownUrl.get() + event.getCause().getTargetId() : null;
                            if (DefaultGroovyMethods.asBoolean(causeUrl))
                                activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append(causeTitle + "\n\t" + causeUrl);
                            else activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append(causeTitle);
                            activityLog.append("\n");
                        }

                    }


                    if ((event.getSymptoms() != null) && (isNewIncident || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("symptom"))) {
                        // synchronize the OPR symptom to the incident activity log
                        DefaultGroovyMethods.each(event.getSymptoms().getEventReferences(), new Closure<StringBuffer>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public StringBuffer doCall(OprSymptomReference symptomRef) {
                                activityLog.append("\n");
                                final String symptomTitle = symptomRef.getTitle();
                                final String symptomUrl = DefaultGroovyMethods.asBoolean((drilldownUrl.get())) ? drilldownUrl.get() + symptomRef.getTargetId() : null;
                                if (DefaultGroovyMethods.asBoolean(symptomUrl))
                                    activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append(symptomTitle + "\n\t" + symptomUrl);
                                else activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append(symptomTitle);
                                return activityLog.append("\n");
                            }

                        });
                    }


                    if (duplicateChange && (isNewIncident || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("duplicate_count"))) {
                        final Integer previousCount = (Integer) duplicateChange.getPreviousValue();
                        final Integer currentCount = (Integer) duplicateChange.getCurrentValue();

                        if (currentCount > 0) {
                            // synchronize the duplicate count to the incident activity log
                            if (previousCount == null) {
                                activityLog.append("\n");
                                activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n").append(currentCount);
                            } else {
                                activityLog.append("\n");
                                activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n");
                                activityLog.append(ACTIVITY_LOG_PREVIOUS + " " + String.valueOf(previousCount) + " " + ACTIVITY_LOG_CURRENT + " " + String.valueOf(currentCount));
                            }

                            activityLog.append("\n");
                        }

                    }


                    // set any activityLog
                    if (activityLog.length() > 0) {
                        return.call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public Object doCall(Object it) {
                                return.call(activityLog.toString());
                            }

                            public void doCall() {
                                doCall(null);
                            }

                        });
                    }

                }

                public void doCall() {
                    doCall(null);
                }

            });
            final String output = writer.toString();
            return output;
        }

//##################################### END DEFAULT SECTION #####################################


        return null;
    }

    private void setBusinessService(OprEvent event, final MarkupBuilder builder, StringBuffer activityLog) {
        // The following class and method only exists in 9.21 or greater
        // com.hp.opr.api.ws.model.event.ci.OprAffectsBusinessService affectsService
        final OprAffectsBusinessService affectsService = event.getMostCriticalAffectsBusinessService(RecursiveSearchBusinessServices);
        if (affectsService != null) {
            // send 'affects_business_service' CI information
            LinkedHashMap<String, GString> map = new LinkedHashMap<String, GString>(1);
            map.put("target_role", String.valueOf(getProperty("BUSINESS_SERVICE_ROLE")));
            .call(map, new Closure<Object>(this, this) {
                public Object doCall(Object it) {
                    if (DefaultGroovyMethods.asBoolean(affectsService.getConfigurationItem().getGlobalId())).
                    call(affectsService.getConfigurationItem().getGlobalId());
                    .call(CI_TARGET_TYPE);
                    return.call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                        public Object doCall(Object it) {
                            if (DefaultGroovyMethods.asBoolean(affectsService.getConfigurationItem().getCiType())).
                            call(affectsService.getConfigurationItem().getCiType());
                            if (DefaultGroovyMethods.asBoolean(affectsService.getConfigurationItem().getId())).
                            call(affectsService.getConfigurationItem().getId());
                            if (DefaultGroovyMethods.asBoolean(affectsService.getConfigurationItem().getCiName())).
                            call(affectsService.getConfigurationItem().getCiName());
                            if (DefaultGroovyMethods.asBoolean(affectsService.getConfigurationItem().getCiDisplayLabel()))
                                return.call(affectsService.getConfigurationItem().getCiDisplayLabel());
                        }

                        public void doCall() {
                            doCall(null);
                        }

                    });
                }

                public void doCall() {
                    doCall(null);
                }

            });
            if (event.getAffectsBusinessServices() != null && !event.getAffectsBusinessServices().isEmpty()) {
                final URL eventUrl = event.getDrilldownUrl();
                activityLog.append("\n");
                activityLog.append(ACTIVITY_LOG_AFFECTS_SERVICES);
                for (OprAffectsBusinessService service : event.getAffectsBusinessServices()) {
                    appendBusinessServiceToActivityLog(activityLog, service, eventUrl, "\t");
                }

                activityLog.append("\n");
            }

        }

    }

    private void setBusinessServicePre921(final OprConfigurationItem ci, URL eventUrl, final MarkupBuilder builder, StringBuffer activityLog) {
        // The following is for pre 9.21 only. Get the business service CIs
        m_log.invokeMethod("debug", new Object[]{"Enter setBusinessServicePre921()"});
        TopologyQueryService queryService = getQueryService();
        if (queryService == null) return;


        try {
            QueryDefinition queryDefinition = queryService.getFactory().createQueryDefinition("Get Affects Business Services " + ci.getCiType() + "-" + ci.getId());
            boolean isBusinessService = TYPE_BUSINESS_SERVICE.equals(ci.getCiType());

            // Key properties & ensure Global ID, Name and Label are included.
            QueryNode queryNode = queryDefinition.addNode("CI").withIdsFromStrings(ci.getId());
            if (isBusinessService) queryNode.ofType(TYPE_BUSINESS_SERVICE);
            else queryNode.ofConfigurationItemType();
            queryNode.queryProperties(ATTR_GLOBAL_ID, ATTR_NAME, ATTR_LABEL, ATTR_BUSINESS_CRITICALITY);

            QueryNode businessServiceNode = queryDefinition.addNode("Business Service").ofType(TYPE_BUSINESS_SERVICE);

            businessServiceNode.queryProperties(ATTR_GLOBAL_ID, ATTR_NAME, ATTR_LABEL, ATTR_BUSINESS_CRITICALITY);

            IndirectLink businessService2me = businessServiceNode.indirectlyLinkedTo(queryNode);
            if (isBusinessService) {
                businessService2me.withStep().from(TYPE_BUSINESS_SERVICE).to(TYPE_BUSINESS_SERVICE).alongTheLink(REL_IMPACT);
                businessService2me.atLeast(1).atMost(QueryLink.UNBOUNDED).showEntirePath();
                businessService2me.withMaxNumberOfStepsMatched(RTSM_QUERY_MAX_STEPS).withTargetCardinality(0, QueryLink.UNBOUNDED);

                QueryNode relBusinessServiceNode = queryDefinition.addNode("B2B Service Related").ofType(TYPE_BUSINESS_SERVICE);

                relBusinessServiceNode.queryProperties(ATTR_GLOBAL_ID, ATTR_NAME, ATTR_LABEL, ATTR_BUSINESS_CRITICALITY);

                IndirectLink businessService2businessService = relBusinessServiceNode.indirectlyLinkedTo(businessServiceNode);
                businessService2businessService.withStep().from(TYPE_BUSINESS_SERVICE).to(TYPE_BUSINESS_SERVICE).alongTheLink(REL_IMPACT);
                businessService2businessService.atLeast(1).atMost(QueryLink.UNBOUNDED).showEntirePath();
                businessService2businessService.withMaxNumberOfStepsMatched(RTSM_QUERY_MAX_STEPS).withTargetCardinality(0, QueryLink.UNBOUNDED);
            } else {
                IndirectLinkStepToPart toPart = businessService2me.withStep().fromConfigurationItemType().toConfigurationItemType();
                toPart.complexTypeConditionsSet().addComplexTypeCondition().withoutType(TYPE_BUSINESS_SERVICE);
                toPart.alongTheLink(REL_IMPACT);
                businessService2me.atLeast(1).atMost(QueryLink.UNBOUNDED);
                businessService2me.withMaxNumberOfStepsMatched(RTSM_QUERY_MAX_STEPS).withTargetCardinality(1, QueryLink.UNBOUNDED);

                QueryNode relBusinessServiceNode = queryDefinition.addNode("Business Service Related").ofType(TYPE_BUSINESS_SERVICE);
                relBusinessServiceNode.queryProperties(ATTR_GLOBAL_ID, ATTR_NAME, ATTR_LABEL, ATTR_BUSINESS_CRITICALITY);

                IndirectLink businessService2businessService = relBusinessServiceNode.indirectlyLinkedTo(businessServiceNode);
                businessService2businessService.withStep().from(TYPE_BUSINESS_SERVICE).to(TYPE_BUSINESS_SERVICE).alongTheLink(REL_IMPACT);
                businessService2businessService.atLeast(1).atMost(QueryLink.UNBOUNDED).showEntirePath();
                businessService2businessService.withMaxNumberOfStepsMatched(RTSM_QUERY_MAX_STEPS).withTargetCardinality(0, QueryLink.UNBOUNDED);
            }


            final Reference<TopologyCI> topCriticalService = new Reference<TopologyCI>(null);
            final Reference<Integer> topCriticality = new Reference<int>(0);
            TopologyCount topologyCount = queryService.evaluateQuery(queryDefinition);
            // Save the query the first time only
            if (SaveTQLQuery) {
                queryDefinition.withBundles(Arrays.asList("integration_tqls_bundle"));
                ucmdbService.getQueryManagementService().saveQuery(queryDefinition);
            }

            final int ciCount = topologyCount.getCIsNumber();
            m_log.invokeMethod("debug", new Object[]{"Query count: " + String.valueOf(ciCount)});
            if (ciCount < RTSM_MAX_CI_COUNT) {
                // execute the query
                final Topology topology = queryService.executeQuery(queryDefinition);
                final Collection<TopologyCI> cis = topology.getAllCIs();
                m_log.invokeMethod("debug", new Object[]{"CI count: " + String.valueOf(cis.size())});
                for (final TopologyCI topologyCi : cis) {
                    if ("business_service".equals(topologyCi.getType())) {
                        int criticality = (Integer) topologyCi.getPropertyValue("business_criticality");
                        if (topCriticalService.get() == null || criticality > topCriticality.get()) {
                            topCriticalService.set(topologyCi);
                            topCriticality.set((Integer) topologyCi.getPropertyValue("business_criticality"));
                        }

                    }

                }

            } else {
                m_log.invokeMethod("debug", new Object[]{"Number of affected Business Services for related CI with id " + ci.getId() + " exceeds " + String.valueOf(RTSM_MAX_CI_COUNT) + "."});
                return;

            }


            if (topCriticalService.get() != null) {
                final String serviceId = topCriticalService.get().getId().getAsString();
                final String serviceGlobalId = getIdAsString(topCriticalService.get(), "global_id");
                final Property property = topCriticalService.get().getProperty("name");
                final String serviceName = (property == null ? null : property.getValue()).toString();
                final Property property1 = topCriticalService.get().getProperty("display_label");
                final String serviceLabel = (property1 == null ? null : property1.getValue()).toString();

                // send 'affects_business_service' CI information
                LinkedHashMap<String, GString> map = new LinkedHashMap<String, GString>(1);
                map.put("target_role", String.valueOf(getProperty("BUSINESS_SERVICE_ROLE")));
                .call(map, new Closure<Object>(this, this) {
                    public Object doCall(Object it) {
                        if (DefaultGroovyMethods.asBoolean(serviceGlobalId)).call(serviceGlobalId);
                        .call(CI_TARGET_TYPE);
                        return.call(new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public Object doCall(Object it) {
                                .call(topCriticalService.get().getType());
                                if (DefaultGroovyMethods.asBoolean(serviceId)).call(serviceId);
                                if (DefaultGroovyMethods.asBoolean(serviceName)).call(serviceName);
                                if (DefaultGroovyMethods.asBoolean(serviceLabel)) return.call(serviceLabel);
                            }

                            public void doCall() {
                                doCall(null);
                            }

                        });
                    }

                    public void doCall() {
                        doCall(null);
                    }

                });
                activityLog.append("\n");
                activityLog.append(ACTIVITY_LOG_AFFECTS_SERVICES);
                activityLog.append("\n").append("\t").append(serviceName + " : " + String.valueOf(topCriticality.get()));
                if (eventUrl && serviceId) {
                    final URL ciUrl = new URL(eventUrl.getProtocol(), eventUrl.getHost(), eventUrl.getPort(), BSM_CI_DRILLDOWN_PATH + serviceId);
                    activityLog.append(" : " + String.valueOf(ciUrl));
                }

                activityLog.append("\n");
            } else m_log.invokeMethod("debug", new Object[]{"No top critical business service located."});
        } catch (Exception e) {
            // try to re-open the connection
            final String details = e.getClass().getCanonicalName() + (DefaultGroovyMethods.asBoolean(e.getMessage()) ? ": " + e.getMessage() + "." : "");
            m_log.invokeMethod("error", new Object[]{"Attempt to query the RTSM on server " + RTSM_HOSTNAME + " failed. Error details: " + details + "\n", e});
        }

    }

    private String getDnsName(OprEvent event) {
        OprNodeReference nodeRef = event.getNode();
        OprRelatedCi relatedCi = event.getRelatedCi();

        final OprNode node = (nodeRef == null ? null : nodeRef.getNode());
        if ((node == null ? null : node.getAny()) == null || nodeRef.getNode().getAny().isEmpty()) {
            return ((relatedCi == null ? null : relatedCi.getConfigurationItem()) != null) ? relatedCi.getConfigurationItem().getProperty(NODE_DNS_NAME_TAG) : null;
        } else {
            if (m_oprVersion > 913) return nodeRef.getNode().getProperty(NODE_DNS_NAME_TAG);
            else {
                for (Object prop : nodeRef.getNode().getAny()) {
                    if (prop instanceof JAXBElement) {
                        final JAXBElement<?> jaxbElement = (JAXBElement<?>) prop;
                        final QName name = jaxbElement.getName();
                        if (NODE_DNS_NAME_TAG.equals((name == null ? null : name.getLocalPart())))
                            return (DefaultGroovyMethods.asBoolean(jaxbElement.getValue()) ? jaxbElement.getValue() : null);
                    }

                }

            }

        }

        return null;
    }

    private String getIdAsString(TopologyCI ci, String propertyName) {
        final Property property = ci.getProperty(propertyName);
        Object propValue = "id".equals(propertyName) ? ci.getId() : (property == null ? null : property.getValue());
        if (!DefaultGroovyMethods.asBoolean(propValue)) return null;

        if (propValue instanceof String) {
            String value = ((String) propValue).trim();
            return DefaultGroovyMethods.asBoolean((value)) ? value : null;
        } else if (propValue instanceof Byte[]) {
            final UcmdbService service1 = ucmdbService;
            final TopologyUpdateService service = (service1 == null ? null : service1.getTopologyUpdateService());
            TopologyUpdateFactory topologyUpdateFactory = (service == null ? null : service.getFactory());
            return (topologyUpdateFactory == null) ? null : topologyUpdateFactory.restoreCIIdFromBytes((Byte[]) propValue).getAsString();
        } else if (propValue instanceof UcmdbId) {
            return ((UcmdbId) propValue).getAsString();
        } else {
            final Class<Object> clazz = (propValue == null ? null : propValue.getClass());
            m_log.invokeMethod("error", new Object[]{"Unexpected object type for UCMDB ID: " + (clazz == null ? null : clazz.getCanonicalName())});
            return null;
        }

    }

    private void appendBusinessServiceToActivityLog(StringBuffer activityLog, final Object affectsService, URL eventUrl, String indent) {
        if (affectsService.configurationItem.ciDisplayLabel.asBoolean()) {
            final String name = affectsService.configurationItem.ciDisplayLabel;
            final int criticality = affectsService.businessCriticality;
            activityLog.append("\n").append(indent).append(name + " : " + String.valueOf(criticality));
            if (eventUrl && affectsService.configurationItem.id) {
                final URL ciUrl = new URL(eventUrl.getProtocol(), eventUrl.getHost(), eventUrl.getPort(), BSM_CI_DRILLDOWN_PATH + String.valueOf(affectsService.configurationItem.id));
                activityLog.append(" : " + String.valueOf(ciUrl));
            }

        }


        final Object item = affectsService.configurationItem;
        List services = (item == null ? null : item.affectsBusinessServices);
        if (services != null && !services.isEmpty()) {
            for (Object nextService : services) {
                appendBusinessServiceToActivityLog(activityLog, nextService, eventUrl, indent + "\t");
            }

        }

    }

    private void getCookies(final ClientResponse response, final Set<Cookie> cookies) {
        m_log.invokeMethod("debug", new Object[]{"Getting Cookies"});
        final MultivaluedMap<String, String> headers = (response == null ? null : response.getHeaders());
        m_log.invokeMethod("debug", new Object[]{"HTTP Header count: " + (headers == null ? "<null>" : headers.size())});

        if (headers != null && !headers.isEmpty()) {
            final Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
            for (final Cookie c : cookies) ((HashMap<String, Cookie>) cookieMap).put(c.getName(), c);
            DefaultGroovyMethods.each(headers, new Closure<List<String>>(this, this) {
                public List<String> doCall(Map.Entry<String, List<String>> header) {
                    if (header.getKey() != null)
                        m_log.invokeMethod("debug", new Object[]{"Header: " + header.getKey() + ": " + header.getValue()});
                    if (SET_COOKIE_HEADER.equalsIgnoreCase(header.getKey())) {
                        return DefaultGroovyMethods.each(header.getValue(), new Closure<Object>(ServiceManagerAdapter.this, ServiceManagerAdapter.this) {
                            public Object doCall(final String value) {
                                if (value != null && value.trim().length() > 0) {
                                    try {
                                        final Cookie cookie = Cookie.valueOf(value);
                                        cookieMap.put(cookie.getName(), cookie);
                                        if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
                                            return m_log.invokeMethod("debug", new Object[]{"Cookie added: " + cookie.getName() + "=" + cookie.getValue()});
                                    } catch (IllegalArgumentException e) {
                                        // ignore this entry
                                        return m_log.invokeMethod("debug", new Object[]{"Invalid Cookie ignored: " + value});
                                    }

                                }

                            }

                        });
                    }

                }

            });
            cookies.clear();
            cookies.addAll(((HashMap<String, Cookie>) cookieMap).values());
        }

    }

    private void checkPingResponse(final ClientResponse response) {
        if (response.getStatusCode() > 299) {
            final String message = response.getEntity(String.class);
            // Workaround for older SM versions that do not have the fix: QCCR1E65559
            if (response.getStatusCode() == 500 && message && DefaultGroovyMethods.contains(message, "DAOServerException: No record found."))
                return;


            checkResponse(response);
        }

    }

    private void checkResponse(final ClientResponse response) {
        if (response.getStatusCode() > 299) {
            final String message = response.getEntity(String.class);
            if (DefaultGroovyMethods.asBoolean(message))
                m_log.invokeMethod("error", new Object[]{"HTTP error response - " + response.getMessage() + " (" + String.valueOf(response.getStatusCode()) + "): " + message});
            else
                m_log.invokeMethod("error", new Object[]{"HTTP error response - " + response.getMessage() + " (" + String.valueOf(response.getStatusCode()) + ")"});
            throw new ClientWebException((ClientRequest) null, response);
        }

    }

    /**
     * create a request
     *
     * @param protocol    - Request protocol such as http
     * @param node        - The node name od the external process, if any.
     * @param port        - The port number of the external process, if any
     * @param path        - resource path
     * @param credentials - The login credentials
     * @param cookies     - any cookies to set on this request
     * @return requested resource or null
     */
    private Resource createRequest(final String protocol, final String node, final Integer port, final String path, final PasswordAuthentication credentials, final Set<Cookie> cookies) {
        final String address = protocol + "://" + node + ":" + String.valueOf(port) + path;

        if (m_log.invokeMethod("isDebugEnabled", new Object[0]).asBoolean())
            m_log.invokeMethod("debug", new Object[]{"Creating request for: " + address});

        // create the resource instance to interact with
        final Resource resource = m_client.resource(address);

        // SM requires as media type application/atom+xml
        resource.accept(MediaType.APPLICATION_ATOM_XML).contentType(MediaType.APPLICATION_ATOM_XML);
        if (credentials != null) {
            // Set the username and password in the request.



            byte[] encodedUserPassword = Base64.encodeBase64((credentials.getUserName() + ":" + new String(credentials.getPassword())).getBytes());
            resource.header("Authorization", "Basic " + new String(encodedUserPassword));
        }


        // Set any cookies saved from last request
        if (cookies != null && !cookies.isEmpty()) {
            for (Cookie cookie : cookies) resource.cookie(cookie);
        }

        return resource;
    }

    private static <T> T setGroovyRef(Reference<T> ref, T newValue) {
        ref.set(newValue);
        return newValue;
    }
}
