package test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.hp.ucmdb.api.UcmdbService;
import com.hp.ucmdb.api.UcmdbServiceProvider;

public class LocalizationParams {
	// SM Urgency values:
	// The text value will be displayed in the external info tab.
	// NOTE: This text may be localized for the desired locale.
	private   Map<String,String> SMUrgency =null;
	//{"1": "1 - Critical", "2": "2 - High", "3": "3 - Average", "4": "4 - Low"};

	// SM Priority values:
	// The text value will be displayed in the external info tab.
	// NOTE: This text may be localized for the desired locale.
	private   Map<String,String> SMPriority = null;
	//["1": "1 - Critical", "2": "2 - High", "3": "3 - Average", "4": "4 - Low"]

	// Change the following to customize the date format in the annotation entries synchronized to the SM activity log
	// See Java SimpleDateFormat for details on the syntax of the two parameters.
	// e.g., to set Japanese locale: LOCALE = Locale.JAPAN
	private   Locale LOCALE = Locale.getDefault();
	private   String ANNOTATION_DATE_FORMAT = "yyyy.MM.dd HH:mm:ss z";

	// In SM the description is a required attribute. In case it is not set in BSM this value is taken.
	// An empty string is NOT allowed.
	private   String EMPTY_DESCRIPTION_OVERRIDE = "<none>";

	// SM Incident Activity Log text.
	// This text is prefixed to the appropriate OPR event property when synchronizing it to an SM Incident activity log.
	// NOTE: This text may be localized for the desired locale.
	private   String ACTIVITY_LOG_TITLE = "[Title]";
	private   String ACTIVITY_LOG_TITLE_CHANGE = "Event title changed to: ";
	private   String ACTIVITY_LOG_STATE = "[State]";
	private   String ACTIVITY_LOG_STATE_CHANGE = "Event state changed to: ";
	private   String ACTIVITY_LOG_DESCRIPTION = "[Description]";
	private   String ACTIVITY_LOG_DESCRIPTION_CHANGE = "Event description changed to: ";
	private   String ACTIVITY_LOG_SOLUTION = "[Solution]";
	private   String ACTIVITY_LOG_SOLUTION_CHANGE = "Event solution changed to: ";
	private   String ACTIVITY_LOG_ASSIGNED_USER = "[Assigned User]";
	private   String ACTIVITY_LOG_ASSIGNED_USER_CHANGE = "Event assigned user changed to: ";
	private   String ACTIVITY_LOG_ASSIGNED_GROUP = "[Assigned Group]";;
	private   String ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE = "Event assigned group changed to: ";
	private   String ACTIVITY_LOG_UNASSIGNED = "<unassigned>";
	private   String ACTIVITY_LOG_SEVERITY = "[Severity]";
	private   String ACTIVITY_LOG_SEVERITY_CHANGE = "Event severity changed to: ";
	private   String ACTIVITY_LOG_PRIORITY = "[Priority]";
	private   String ACTIVITY_LOG_PRIORITY_CHANGE = "Event priority changed to: ";
	private   String ACTIVITY_LOG_CONTROL_TRANSFERRED_TO = "[Control Transferred To]";
	private   String ACTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED = "Event control transfer state changed to: ";
	private   String ACTIVITY_LOG_CATEGORY = "[Category]";
	private   String ACTIVITY_LOG_SUBCATEGORY = "[Subcategory]";
	private   String ACTIVITY_LOG_APPLICATION = "[Application]";
	private   String ACTIVITY_LOG_OBJECT = "[Object]";
	private   String ACTIVITY_LOG_ANNOTATION = "[Annotation]";
	private   String ACTIVITY_LOG_CA = "[Custom Attribute]";
	private   String ACTIVITY_LOG_CAUSE = "[Cause]";
	private   String ACTIVITY_LOG_OMI_CAUSE = "[OMi Cause]";
	private   String ACTIVITY_LOG_OMI_SYMPTOM = "[OMi Symptom]";
	private   String ACTIVITY_LOG_DUPLICATE_COUNT = "[Duplicate Count]";
	private   String ACTIVITY_LOG_PREVIOUS = "previous";
	private   String ACTIVITY_LOG_CURRENT = "current";
	private   String ACTIVITY_LOG_INITIATED_BY = "[Initiated by]";
	private   String ACTIVITY_LOG_INITIATED_BY_RULE = "BSM forwarding rule: ";
	private   String ACTIVITY_LOG_INITIATED_BY_USER = "BSM operator: ";
	private   String ACTIVITY_LOG_RELATED_CI = "[BSM Related CI]";
	private   String ACTIVITY_LOG_RELATED_CI_TYPE_LABEL = "Type label: ";
	private   String ACTIVITY_LOG_RELATED_CI_TYPE = "Type: ";
	private   String ACTIVITY_LOG_RELATED_CI_LABEL = "Display label: ";
	private   String ACTIVITY_LOG_RELATED_CI_NAME = "Name: ";
	private   String ACTIVITY_LOG_RELATED_CI_HOSTED_ON = "Hosted on: ";
	private   String ACTIVITY_LOG_RELATED_CI_URL = "Cross launch URL: ";
	private   String ACTIVITY_LOG_AFFECTS_SERVICES = "[BSM Affects Business Services (name : criticality)]";
	private   String ACTIVITY_LOG_TIME_RECEIVED = "[Time OMi Received Event]";
	private   String ACTIVITY_LOG_TIME_CREATED = "[Time OMi Event Created]";
	private   String ACTIVITY_LOG_TIME_STATE_CHANGED = "[Time OMi Event State Changed]";
	private   String ACTIVITY_LOG_ORIGINAL_DATA = "[Original Data]";
	private   String ACTIVITY_LOG_OPERATIONAL_DATA = "[CI is operational]";

	// ****************************************************************************
	// * END Localization: Customization of text values for language localization *
	// ****************************************************************************

	private UcmdbServiceProvider ucmdbProvider = null;
	private UcmdbService ucmdbService = null;

	// For debugging purposes. Saves the TQLs for analysis in the UI.
	private   boolean SaveTQLQuery = false;

	// Specify ActiveProcess in request
	private   Boolean SpecifyActiveProcess = true;

	// Specify ImpactScope in request
	private   Boolean SpecifyImpactScope = true;

	// URL paths
	private   String DRILLDOWN_ROOT_PATH  = null;
	private   String OMI_ROOT_DRILLDOWN_PATH = "/opr-console/opr-evt-details.jsp?eventId=";
	private   String BSM_CI_DRILLDOWN_PATH = "/topaz/dash/nodeDetails.do?cmdbId=";
	private   String ROOT_PATH = "/SM/7/rest/1.1/incident_list";
	private   String PING_QUERY = "reference_number='IM10001'";
	private   String INCIDENT_PATH = ROOT_PATH + "/reference_number/";

	// Custom properties file path: This is relative to the BSM install directory.
	private   String CUSTOM_PROPERTIES_FILE = "/conf/opr/integration/sm/custom.properties";
	
	
	public LocalizationParams(String SM_WEB_TIER_NAME){
		SMUrgency = new HashMap<String,String>();
		SMUrgency.put("1", "1 - Critical");
		SMUrgency.put("2", "2 - High");
		SMUrgency.put("3", "3 - Average");
		SMUrgency.put("4", "4 - Low");
		
		SMPriority = new HashMap<String,String>();
		SMPriority.put("1", "1 - Critical");
		SMPriority.put("2", "2 - High");
		SMPriority.put("3", "3 - Average");
		SMPriority.put("4", "4 - Low");
		
		DRILLDOWN_ROOT_PATH = "/"+ SM_WEB_TIER_NAME +"/index.do?ctx=docEngine&file=probsummary&query=number%3D";
	}


	/**
	 * @param sMUrgency
	 * @param sMPriority
	 * @param lOCALE
	 * @param aNNOTATION_DATE_FORMAT
	 * @param eMPTY_DESCRIPTION_OVERRIDE
	 * @param aCTIVITY_LOG_TITLE
	 * @param aCTIVITY_LOG_TITLE_CHANGE
	 * @param aCTIVITY_LOG_STATE
	 * @param aCTIVITY_LOG_STATE_CHANGE
	 * @param aCTIVITY_LOG_DESCRIPTION
	 * @param aCTIVITY_LOG_DESCRIPTION_CHANGE
	 * @param aCTIVITY_LOG_SOLUTION
	 * @param aCTIVITY_LOG_SOLUTION_CHANGE
	 * @param aCTIVITY_LOG_ASSIGNED_USER
	 * @param aCTIVITY_LOG_ASSIGNED_USER_CHANGE
	 * @param aCTIVITY_LOG_ASSIGNED_GROUP
	 * @param aCTIVITY_LOG_ASSIGNED_GROUP_CHANGE
	 * @param aCTIVITY_LOG_UNASSIGNED
	 * @param aCTIVITY_LOG_SEVERITY
	 * @param aCTIVITY_LOG_SEVERITY_CHANGE
	 * @param aCTIVITY_LOG_PRIORITY
	 * @param aCTIVITY_LOG_PRIORITY_CHANGE
	 * @param aCTIVITY_LOG_CONTROL_TRANSFERRED_TO
	 * @param aCTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED
	 * @param aCTIVITY_LOG_CATEGORY
	 * @param aCTIVITY_LOG_SUBCATEGORY
	 * @param aCTIVITY_LOG_APPLICATION
	 * @param aCTIVITY_LOG_OBJECT
	 * @param aCTIVITY_LOG_ANNOTATION
	 * @param aCTIVITY_LOG_CA
	 * @param aCTIVITY_LOG_CAUSE
	 * @param aCTIVITY_LOG_OMI_CAUSE
	 * @param aCTIVITY_LOG_OMI_SYMPTOM
	 * @param aCTIVITY_LOG_DUPLICATE_COUNT
	 * @param aCTIVITY_LOG_PREVIOUS
	 * @param aCTIVITY_LOG_CURRENT
	 * @param aCTIVITY_LOG_INITIATED_BY
	 * @param aCTIVITY_LOG_INITIATED_BY_RULE
	 * @param aCTIVITY_LOG_INITIATED_BY_USER
	 * @param aCTIVITY_LOG_RELATED_CI
	 * @param aCTIVITY_LOG_RELATED_CI_TYPE_LABEL
	 * @param aCTIVITY_LOG_RELATED_CI_TYPE
	 * @param aCTIVITY_LOG_RELATED_CI_LABEL
	 * @param aCTIVITY_LOG_RELATED_CI_NAME
	 * @param aCTIVITY_LOG_RELATED_CI_HOSTED_ON
	 * @param aCTIVITY_LOG_RELATED_CI_URL
	 * @param aCTIVITY_LOG_AFFECTS_SERVICES
	 * @param aCTIVITY_LOG_TIME_RECEIVED
	 * @param aCTIVITY_LOG_TIME_CREATED
	 * @param aCTIVITY_LOG_TIME_STATE_CHANGED
	 * @param aCTIVITY_LOG_ORIGINAL_DATA
	 * @param aCTIVITY_LOG_OPERATIONAL_DATA
	 * @param ucmdbProvider
	 * @param ucmdbService
	 * @param saveTQLQuery
	 * @param specifyActiveProcess
	 * @param specifyImpactScope
	 * @param dRILLDOWN_ROOT_PATH
	 * @param oMI_ROOT_DRILLDOWN_PATH
	 * @param bSM_CI_DRILLDOWN_PATH
	 * @param rOOT_PATH
	 * @param pING_QUERY
	 * @param iNCIDENT_PATH
	 * @param cUSTOM_PROPERTIES_FILE
	 */
	public LocalizationParams(Map<String, String> sMUrgency,
			Map<String, String> sMPriority, Locale lOCALE,
			String aNNOTATION_DATE_FORMAT, String eMPTY_DESCRIPTION_OVERRIDE,
			String aCTIVITY_LOG_TITLE, String aCTIVITY_LOG_TITLE_CHANGE,
			String aCTIVITY_LOG_STATE, String aCTIVITY_LOG_STATE_CHANGE,
			String aCTIVITY_LOG_DESCRIPTION,
			String aCTIVITY_LOG_DESCRIPTION_CHANGE,
			String aCTIVITY_LOG_SOLUTION, String aCTIVITY_LOG_SOLUTION_CHANGE,
			String aCTIVITY_LOG_ASSIGNED_USER,
			String aCTIVITY_LOG_ASSIGNED_USER_CHANGE,
			String aCTIVITY_LOG_ASSIGNED_GROUP,
			String aCTIVITY_LOG_ASSIGNED_GROUP_CHANGE,
			String aCTIVITY_LOG_UNASSIGNED, String aCTIVITY_LOG_SEVERITY,
			String aCTIVITY_LOG_SEVERITY_CHANGE, String aCTIVITY_LOG_PRIORITY,
			String aCTIVITY_LOG_PRIORITY_CHANGE,
			String aCTIVITY_LOG_CONTROL_TRANSFERRED_TO,
			String aCTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED,
			String aCTIVITY_LOG_CATEGORY, String aCTIVITY_LOG_SUBCATEGORY,
			String aCTIVITY_LOG_APPLICATION, String aCTIVITY_LOG_OBJECT,
			String aCTIVITY_LOG_ANNOTATION, String aCTIVITY_LOG_CA,
			String aCTIVITY_LOG_CAUSE, String aCTIVITY_LOG_OMI_CAUSE,
			String aCTIVITY_LOG_OMI_SYMPTOM,
			String aCTIVITY_LOG_DUPLICATE_COUNT, String aCTIVITY_LOG_PREVIOUS,
			String aCTIVITY_LOG_CURRENT, String aCTIVITY_LOG_INITIATED_BY,
			String aCTIVITY_LOG_INITIATED_BY_RULE,
			String aCTIVITY_LOG_INITIATED_BY_USER,
			String aCTIVITY_LOG_RELATED_CI,
			String aCTIVITY_LOG_RELATED_CI_TYPE_LABEL,
			String aCTIVITY_LOG_RELATED_CI_TYPE,
			String aCTIVITY_LOG_RELATED_CI_LABEL,
			String aCTIVITY_LOG_RELATED_CI_NAME,
			String aCTIVITY_LOG_RELATED_CI_HOSTED_ON,
			String aCTIVITY_LOG_RELATED_CI_URL,
			String aCTIVITY_LOG_AFFECTS_SERVICES,
			String aCTIVITY_LOG_TIME_RECEIVED,
			String aCTIVITY_LOG_TIME_CREATED,
			String aCTIVITY_LOG_TIME_STATE_CHANGED,
			String aCTIVITY_LOG_ORIGINAL_DATA,
			String aCTIVITY_LOG_OPERATIONAL_DATA,
			UcmdbServiceProvider ucmdbProvider, UcmdbService ucmdbService,
			boolean saveTQLQuery, Boolean specifyActiveProcess,
			Boolean specifyImpactScope, String dRILLDOWN_ROOT_PATH,
			String oMI_ROOT_DRILLDOWN_PATH, String bSM_CI_DRILLDOWN_PATH,
			String rOOT_PATH, String pING_QUERY, String iNCIDENT_PATH,
			String cUSTOM_PROPERTIES_FILE) {
		super();
		SMUrgency = sMUrgency;
		SMPriority = sMPriority;
		LOCALE = lOCALE;
		ANNOTATION_DATE_FORMAT = aNNOTATION_DATE_FORMAT;
		EMPTY_DESCRIPTION_OVERRIDE = eMPTY_DESCRIPTION_OVERRIDE;
		ACTIVITY_LOG_TITLE = aCTIVITY_LOG_TITLE;
		ACTIVITY_LOG_TITLE_CHANGE = aCTIVITY_LOG_TITLE_CHANGE;
		ACTIVITY_LOG_STATE = aCTIVITY_LOG_STATE;
		ACTIVITY_LOG_STATE_CHANGE = aCTIVITY_LOG_STATE_CHANGE;
		ACTIVITY_LOG_DESCRIPTION = aCTIVITY_LOG_DESCRIPTION;
		ACTIVITY_LOG_DESCRIPTION_CHANGE = aCTIVITY_LOG_DESCRIPTION_CHANGE;
		ACTIVITY_LOG_SOLUTION = aCTIVITY_LOG_SOLUTION;
		ACTIVITY_LOG_SOLUTION_CHANGE = aCTIVITY_LOG_SOLUTION_CHANGE;
		ACTIVITY_LOG_ASSIGNED_USER = aCTIVITY_LOG_ASSIGNED_USER;
		ACTIVITY_LOG_ASSIGNED_USER_CHANGE = aCTIVITY_LOG_ASSIGNED_USER_CHANGE;
		ACTIVITY_LOG_ASSIGNED_GROUP = aCTIVITY_LOG_ASSIGNED_GROUP;
		ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE = aCTIVITY_LOG_ASSIGNED_GROUP_CHANGE;
		ACTIVITY_LOG_UNASSIGNED = aCTIVITY_LOG_UNASSIGNED;
		ACTIVITY_LOG_SEVERITY = aCTIVITY_LOG_SEVERITY;
		ACTIVITY_LOG_SEVERITY_CHANGE = aCTIVITY_LOG_SEVERITY_CHANGE;
		ACTIVITY_LOG_PRIORITY = aCTIVITY_LOG_PRIORITY;
		ACTIVITY_LOG_PRIORITY_CHANGE = aCTIVITY_LOG_PRIORITY_CHANGE;
		ACTIVITY_LOG_CONTROL_TRANSFERRED_TO = aCTIVITY_LOG_CONTROL_TRANSFERRED_TO;
		ACTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED = aCTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED;
		ACTIVITY_LOG_CATEGORY = aCTIVITY_LOG_CATEGORY;
		ACTIVITY_LOG_SUBCATEGORY = aCTIVITY_LOG_SUBCATEGORY;
		ACTIVITY_LOG_APPLICATION = aCTIVITY_LOG_APPLICATION;
		ACTIVITY_LOG_OBJECT = aCTIVITY_LOG_OBJECT;
		ACTIVITY_LOG_ANNOTATION = aCTIVITY_LOG_ANNOTATION;
		ACTIVITY_LOG_CA = aCTIVITY_LOG_CA;
		ACTIVITY_LOG_CAUSE = aCTIVITY_LOG_CAUSE;
		ACTIVITY_LOG_OMI_CAUSE = aCTIVITY_LOG_OMI_CAUSE;
		ACTIVITY_LOG_OMI_SYMPTOM = aCTIVITY_LOG_OMI_SYMPTOM;
		ACTIVITY_LOG_DUPLICATE_COUNT = aCTIVITY_LOG_DUPLICATE_COUNT;
		ACTIVITY_LOG_PREVIOUS = aCTIVITY_LOG_PREVIOUS;
		ACTIVITY_LOG_CURRENT = aCTIVITY_LOG_CURRENT;
		ACTIVITY_LOG_INITIATED_BY = aCTIVITY_LOG_INITIATED_BY;
		ACTIVITY_LOG_INITIATED_BY_RULE = aCTIVITY_LOG_INITIATED_BY_RULE;
		ACTIVITY_LOG_INITIATED_BY_USER = aCTIVITY_LOG_INITIATED_BY_USER;
		ACTIVITY_LOG_RELATED_CI = aCTIVITY_LOG_RELATED_CI;
		ACTIVITY_LOG_RELATED_CI_TYPE_LABEL = aCTIVITY_LOG_RELATED_CI_TYPE_LABEL;
		ACTIVITY_LOG_RELATED_CI_TYPE = aCTIVITY_LOG_RELATED_CI_TYPE;
		ACTIVITY_LOG_RELATED_CI_LABEL = aCTIVITY_LOG_RELATED_CI_LABEL;
		ACTIVITY_LOG_RELATED_CI_NAME = aCTIVITY_LOG_RELATED_CI_NAME;
		ACTIVITY_LOG_RELATED_CI_HOSTED_ON = aCTIVITY_LOG_RELATED_CI_HOSTED_ON;
		ACTIVITY_LOG_RELATED_CI_URL = aCTIVITY_LOG_RELATED_CI_URL;
		ACTIVITY_LOG_AFFECTS_SERVICES = aCTIVITY_LOG_AFFECTS_SERVICES;
		ACTIVITY_LOG_TIME_RECEIVED = aCTIVITY_LOG_TIME_RECEIVED;
		ACTIVITY_LOG_TIME_CREATED = aCTIVITY_LOG_TIME_CREATED;
		ACTIVITY_LOG_TIME_STATE_CHANGED = aCTIVITY_LOG_TIME_STATE_CHANGED;
		ACTIVITY_LOG_ORIGINAL_DATA = aCTIVITY_LOG_ORIGINAL_DATA;
		ACTIVITY_LOG_OPERATIONAL_DATA = aCTIVITY_LOG_OPERATIONAL_DATA;
		this.ucmdbProvider = ucmdbProvider;
		this.ucmdbService = ucmdbService;
		SaveTQLQuery = saveTQLQuery;
		SpecifyActiveProcess = specifyActiveProcess;
		SpecifyImpactScope = specifyImpactScope;
		DRILLDOWN_ROOT_PATH = dRILLDOWN_ROOT_PATH;
		OMI_ROOT_DRILLDOWN_PATH = oMI_ROOT_DRILLDOWN_PATH;
		BSM_CI_DRILLDOWN_PATH = bSM_CI_DRILLDOWN_PATH;
		ROOT_PATH = rOOT_PATH;
		PING_QUERY = pING_QUERY;
		INCIDENT_PATH = iNCIDENT_PATH;
		CUSTOM_PROPERTIES_FILE = cUSTOM_PROPERTIES_FILE;
	}


	/**
	 * @return the sMUrgency
	 */
	public Map<String, String> getSMUrgency() {
		return SMUrgency;
	}


	/**
	 * @return the sMPriority
	 */
	public Map<String, String> getSMPriority() {
		return SMPriority;
	}


	/**
	 * @return the lOCALE
	 */
	public Locale getLOCALE() {
		return LOCALE;
	}


	/**
	 * @return the aNNOTATION_DATE_FORMAT
	 */
	public String getANNOTATION_DATE_FORMAT() {
		return ANNOTATION_DATE_FORMAT;
	}


	/**
	 * @return the eMPTY_DESCRIPTION_OVERRIDE
	 */
	public String getEMPTY_DESCRIPTION_OVERRIDE() {
		return EMPTY_DESCRIPTION_OVERRIDE;
	}


	/**
	 * @return the aCTIVITY_LOG_TITLE
	 */
	public String getACTIVITY_LOG_TITLE() {
		return ACTIVITY_LOG_TITLE;
	}


	/**
	 * @return the aCTIVITY_LOG_TITLE_CHANGE
	 */
	public String getACTIVITY_LOG_TITLE_CHANGE() {
		return ACTIVITY_LOG_TITLE_CHANGE;
	}


	/**
	 * @return the aCTIVITY_LOG_STATE
	 */
	public String getACTIVITY_LOG_STATE() {
		return ACTIVITY_LOG_STATE;
	}


	/**
	 * @return the aCTIVITY_LOG_STATE_CHANGE
	 */
	public String getACTIVITY_LOG_STATE_CHANGE() {
		return ACTIVITY_LOG_STATE_CHANGE;
	}


	/**
	 * @return the aCTIVITY_LOG_DESCRIPTION
	 */
	public String getACTIVITY_LOG_DESCRIPTION() {
		return ACTIVITY_LOG_DESCRIPTION;
	}


	/**
	 * @return the aCTIVITY_LOG_DESCRIPTION_CHANGE
	 */
	public String getACTIVITY_LOG_DESCRIPTION_CHANGE() {
		return ACTIVITY_LOG_DESCRIPTION_CHANGE;
	}


	/**
	 * @return the aCTIVITY_LOG_SOLUTION
	 */
	public String getACTIVITY_LOG_SOLUTION() {
		return ACTIVITY_LOG_SOLUTION;
	}


	/**
	 * @return the aCTIVITY_LOG_SOLUTION_CHANGE
	 */
	public String getACTIVITY_LOG_SOLUTION_CHANGE() {
		return ACTIVITY_LOG_SOLUTION_CHANGE;
	}


	/**
	 * @return the aCTIVITY_LOG_ASSIGNED_USER
	 */
	public String getACTIVITY_LOG_ASSIGNED_USER() {
		return ACTIVITY_LOG_ASSIGNED_USER;
	}


	/**
	 * @return the aCTIVITY_LOG_ASSIGNED_USER_CHANGE
	 */
	public String getACTIVITY_LOG_ASSIGNED_USER_CHANGE() {
		return ACTIVITY_LOG_ASSIGNED_USER_CHANGE;
	}


	/**
	 * @return the aCTIVITY_LOG_ASSIGNED_GROUP
	 */
	public String getACTIVITY_LOG_ASSIGNED_GROUP() {
		return ACTIVITY_LOG_ASSIGNED_GROUP;
	}


	/**
	 * @return the aCTIVITY_LOG_ASSIGNED_GROUP_CHANGE
	 */
	public String getACTIVITY_LOG_ASSIGNED_GROUP_CHANGE() {
		return ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE;
	}


	/**
	 * @return the aCTIVITY_LOG_UNASSIGNED
	 */
	public String getACTIVITY_LOG_UNASSIGNED() {
		return ACTIVITY_LOG_UNASSIGNED;
	}


	/**
	 * @return the aCTIVITY_LOG_SEVERITY
	 */
	public String getACTIVITY_LOG_SEVERITY() {
		return ACTIVITY_LOG_SEVERITY;
	}


	/**
	 * @return the aCTIVITY_LOG_SEVERITY_CHANGE
	 */
	public String getACTIVITY_LOG_SEVERITY_CHANGE() {
		return ACTIVITY_LOG_SEVERITY_CHANGE;
	}


	/**
	 * @return the aCTIVITY_LOG_PRIORITY
	 */
	public String getACTIVITY_LOG_PRIORITY() {
		return ACTIVITY_LOG_PRIORITY;
	}


	/**
	 * @return the aCTIVITY_LOG_PRIORITY_CHANGE
	 */
	public String getACTIVITY_LOG_PRIORITY_CHANGE() {
		return ACTIVITY_LOG_PRIORITY_CHANGE;
	}


	/**
	 * @return the aCTIVITY_LOG_CONTROL_TRANSFERRED_TO
	 */
	public String getACTIVITY_LOG_CONTROL_TRANSFERRED_TO() {
		return ACTIVITY_LOG_CONTROL_TRANSFERRED_TO;
	}


	/**
	 * @return the aCTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED
	 */
	public String getACTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED() {
		return ACTIVITY_LOG_CONTROL_TRANSFERRED_TO_CHANGED;
	}


	/**
	 * @return the aCTIVITY_LOG_CATEGORY
	 */
	public String getACTIVITY_LOG_CATEGORY() {
		return ACTIVITY_LOG_CATEGORY;
	}


	/**
	 * @return the aCTIVITY_LOG_SUBCATEGORY
	 */
	public String getACTIVITY_LOG_SUBCATEGORY() {
		return ACTIVITY_LOG_SUBCATEGORY;
	}


	/**
	 * @return the aCTIVITY_LOG_APPLICATION
	 */
	public String getACTIVITY_LOG_APPLICATION() {
		return ACTIVITY_LOG_APPLICATION;
	}


	/**
	 * @return the aCTIVITY_LOG_OBJECT
	 */
	public String getACTIVITY_LOG_OBJECT() {
		return ACTIVITY_LOG_OBJECT;
	}


	/**
	 * @return the aCTIVITY_LOG_ANNOTATION
	 */
	public String getACTIVITY_LOG_ANNOTATION() {
		return ACTIVITY_LOG_ANNOTATION;
	}


	/**
	 * @return the aCTIVITY_LOG_CA
	 */
	public String getACTIVITY_LOG_CA() {
		return ACTIVITY_LOG_CA;
	}


	/**
	 * @return the aCTIVITY_LOG_CAUSE
	 */
	public String getACTIVITY_LOG_CAUSE() {
		return ACTIVITY_LOG_CAUSE;
	}


	/**
	 * @return the aCTIVITY_LOG_OMI_CAUSE
	 */
	public String getACTIVITY_LOG_OMI_CAUSE() {
		return ACTIVITY_LOG_OMI_CAUSE;
	}


	/**
	 * @return the aCTIVITY_LOG_OMI_SYMPTOM
	 */
	public String getACTIVITY_LOG_OMI_SYMPTOM() {
		return ACTIVITY_LOG_OMI_SYMPTOM;
	}


	/**
	 * @return the aCTIVITY_LOG_DUPLICATE_COUNT
	 */
	public String getACTIVITY_LOG_DUPLICATE_COUNT() {
		return ACTIVITY_LOG_DUPLICATE_COUNT;
	}


	/**
	 * @return the aCTIVITY_LOG_PREVIOUS
	 */
	public String getACTIVITY_LOG_PREVIOUS() {
		return ACTIVITY_LOG_PREVIOUS;
	}


	/**
	 * @return the aCTIVITY_LOG_CURRENT
	 */
	public String getACTIVITY_LOG_CURRENT() {
		return ACTIVITY_LOG_CURRENT;
	}


	/**
	 * @return the aCTIVITY_LOG_INITIATED_BY
	 */
	public String getACTIVITY_LOG_INITIATED_BY() {
		return ACTIVITY_LOG_INITIATED_BY;
	}


	/**
	 * @return the aCTIVITY_LOG_INITIATED_BY_RULE
	 */
	public String getACTIVITY_LOG_INITIATED_BY_RULE() {
		return ACTIVITY_LOG_INITIATED_BY_RULE;
	}


	/**
	 * @return the aCTIVITY_LOG_INITIATED_BY_USER
	 */
	public String getACTIVITY_LOG_INITIATED_BY_USER() {
		return ACTIVITY_LOG_INITIATED_BY_USER;
	}


	/**
	 * @return the aCTIVITY_LOG_RELATED_CI
	 */
	public String getACTIVITY_LOG_RELATED_CI() {
		return ACTIVITY_LOG_RELATED_CI;
	}


	/**
	 * @return the aCTIVITY_LOG_RELATED_CI_TYPE_LABEL
	 */
	public String getACTIVITY_LOG_RELATED_CI_TYPE_LABEL() {
		return ACTIVITY_LOG_RELATED_CI_TYPE_LABEL;
	}


	/**
	 * @return the aCTIVITY_LOG_RELATED_CI_TYPE
	 */
	public String getACTIVITY_LOG_RELATED_CI_TYPE() {
		return ACTIVITY_LOG_RELATED_CI_TYPE;
	}


	/**
	 * @return the aCTIVITY_LOG_RELATED_CI_LABEL
	 */
	public String getACTIVITY_LOG_RELATED_CI_LABEL() {
		return ACTIVITY_LOG_RELATED_CI_LABEL;
	}


	/**
	 * @return the aCTIVITY_LOG_RELATED_CI_NAME
	 */
	public String getACTIVITY_LOG_RELATED_CI_NAME() {
		return ACTIVITY_LOG_RELATED_CI_NAME;
	}


	/**
	 * @return the aCTIVITY_LOG_RELATED_CI_HOSTED_ON
	 */
	public String getACTIVITY_LOG_RELATED_CI_HOSTED_ON() {
		return ACTIVITY_LOG_RELATED_CI_HOSTED_ON;
	}


	/**
	 * @return the aCTIVITY_LOG_RELATED_CI_URL
	 */
	public String getACTIVITY_LOG_RELATED_CI_URL() {
		return ACTIVITY_LOG_RELATED_CI_URL;
	}


	/**
	 * @return the aCTIVITY_LOG_AFFECTS_SERVICES
	 */
	public String getACTIVITY_LOG_AFFECTS_SERVICES() {
		return ACTIVITY_LOG_AFFECTS_SERVICES;
	}


	/**
	 * @return the aCTIVITY_LOG_TIME_RECEIVED
	 */
	public String getACTIVITY_LOG_TIME_RECEIVED() {
		return ACTIVITY_LOG_TIME_RECEIVED;
	}


	/**
	 * @return the aCTIVITY_LOG_TIME_CREATED
	 */
	public String getACTIVITY_LOG_TIME_CREATED() {
		return ACTIVITY_LOG_TIME_CREATED;
	}


	/**
	 * @return the aCTIVITY_LOG_TIME_STATE_CHANGED
	 */
	public String getACTIVITY_LOG_TIME_STATE_CHANGED() {
		return ACTIVITY_LOG_TIME_STATE_CHANGED;
	}


	/**
	 * @return the aCTIVITY_LOG_ORIGINAL_DATA
	 */
	public String getACTIVITY_LOG_ORIGINAL_DATA() {
		return ACTIVITY_LOG_ORIGINAL_DATA;
	}


	/**
	 * @return the aCTIVITY_LOG_OPERATIONAL_DATA
	 */
	public String getACTIVITY_LOG_OPERATIONAL_DATA() {
		return ACTIVITY_LOG_OPERATIONAL_DATA;
	}


	/**
	 * @return the ucmdbProvider
	 */
	public UcmdbServiceProvider getUcmdbProvider() {
		return ucmdbProvider;
	}


	/**
	 * @return the ucmdbService
	 */
	public UcmdbService getUcmdbService() {
		return ucmdbService;
	}


	/**
	 * @return the saveTQLQuery
	 */
	public boolean isSaveTQLQuery() {
		return SaveTQLQuery;
	}


	/**
	 * @return the specifyActiveProcess
	 */
	public Boolean getSpecifyActiveProcess() {
		return SpecifyActiveProcess;
	}


	/**
	 * @return the specifyImpactScope
	 */
	public Boolean getSpecifyImpactScope() {
		return SpecifyImpactScope;
	}


	/**
	 * @return the dRILLDOWN_ROOT_PATH
	 */
	public String getDRILLDOWN_ROOT_PATH() {
		return DRILLDOWN_ROOT_PATH;
	}


	/**
	 * @return the oMI_ROOT_DRILLDOWN_PATH
	 */
	public String getOMI_ROOT_DRILLDOWN_PATH() {
		return OMI_ROOT_DRILLDOWN_PATH;
	}


	/**
	 * @return the bSM_CI_DRILLDOWN_PATH
	 */
	public String getBSM_CI_DRILLDOWN_PATH() {
		return BSM_CI_DRILLDOWN_PATH;
	}


	/**
	 * @return the rOOT_PATH
	 */
	public String getROOT_PATH() {
		return ROOT_PATH;
	}


	/**
	 * @return the pING_QUERY
	 */
	public String getPING_QUERY() {
		return PING_QUERY;
	}


	/**
	 * @return the iNCIDENT_PATH
	 */
	public String getINCIDENT_PATH() {
		return INCIDENT_PATH;
	}


	/**
	 * @return the cUSTOM_PROPERTIES_FILE
	 */
	public String getCUSTOM_PROPERTIES_FILE() {
		return CUSTOM_PROPERTIES_FILE;
	}
	
	
}
