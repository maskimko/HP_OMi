package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigurationParams {

	// See the OMi Extensibility Guide document for complete details on
	// customizing the OMi to SM integration

	// Change "webtier-9.30" in the following to match the name of the web
	// application installed on the target SM server
	private String SM_WEB_TIER_NAME = "sm";

	// BSM Administrator login name
	// For OMi events that are forwarded via a forwarding rule, the SM
	// "is_recorded_by" field is set to this user,
	// otherwise it is set to the OMi operator that initiated the manual
	// transfer.
	private String BSM_ADMINISTRATOR_LOGIN_NAME = "admin";

	// The following RTSM variables are used on pre 9.21 releases to determine
	// the affected business service.
	// If set, the most critically affected business service, in relation to the
	// related CI of the event,
	// will be set in the SM incident created. This will appear as the
	// "Affected Service" in the Incident.
	// These values are ignored on release 9.21 or later, as this information is
	// already passed to the
	// forwardEvent() method in release 9.21 or greater.
	//
	int RTSM_PORT = 80;
	int RTSM_MAX_CI_COUNT = 1000;

	String RTSM_HOSTNAME = "localhost";
	String RTSM_USERNAME = "";
	String RTSM_PASSWORD = "";
	// Sets the maximum number of relation steps between Related CI and affected
	// business service.
	// If set to zero then the query is disabled. Default is 10.
	int RTSM_QUERY_MAX_STEPS = 10;

	// Sets the maximum number of duplicate count updates to send to SM. Should
	// the duplicate count entry exceed this
	// number, no new activity log entry for duplicate count changes will be
	// made to the SM incident.
	int MAX_DUPLICATE_COUNT_UPDATES = 10;

	// Maps the OPR event "state" to the SM incident "status"
	//
	private Map<String, String> MapOPR2SMStatus = null;

	// Maps the SM incident "status" to OPR event "state"
	//
	private Map<String, String> MapSM2OPRState = null;

	// Maps OPR event "severity" to SM incident "urgency"
	//
	private Map<String, String> MapOPR2SMUrgency = null;

	// Maps SM incident "urgency" to OPR event "severity"
	//
	private Map<String, String> MapSM2OPRSeverity = null;

	// Maps OPR event "priority" to SM incident "priority"
	//
	private Map<String, String> MapOPR2SMPriority = null;

	// Maps SM incident "priority" to OPR event "priority"
	//
	private Map<String, String> MapSM2OPRPriority = null;

	// SM Completion Code value to use on close of the SM incident
	//
	private String SMCompletionCode = "Automatically Closed";

	// ****************************************************************************************************
	// * The following sets control which properties and enumerated values are
	// synchronized on change. *
	// * They are therefore not relevant for an Event Channel deployment. They
	// are only relevant for OMi. *
	// ****************************************************************************************************

	// Only "closed" state and some properties are synchronized by default.
	// Set this values to true if you wish all states and properties to be
	// synchronized in both directions.
	// This value will override all synchronization settings listed below.
	private boolean SyncAllProperties = true;


	// Specifies that the node should be used instead of the event related CI
	// as the "is_registered_for" CI in the incident
	private boolean UseNodeCI = false;

	// In 9.21 or greater the most critical affected business service is added
	// to the Incident.
	// The following flag control whether the business service most closely
	// related to the Related CI is
	// sent or are all affected business services recursively searched for the
	// most critically affected.
	private boolean RecursiveSearchBusinessServices = true;

	// Flag to disable checking for out of sync state of "closed".
	// The response of the SM incident update is checked if the state of the
	// Incident is "closed".
	// If the incident state is "closed", but the event state is not "closed",
	// the event will be
	// automatically closed by the script. Requires SyncSMStatusToOPR to contain
	// "closed" state
	// and SyncSMPropertiesToOPR to contain "incident_status".
	// This feature is only available on release 9.21 or later.
	private boolean DisableCheckCloseOnUpdate = false;

	// OPR event properties to synchronize to a corresponding SM incident
	// property on change:
	//
	// "title", "description", "state", "severity", "priority", "solution",
	// "cause", "assigned_user", "assigned_group"
	//
	// If synchronization on change is desired for an OPR event property, add
	// the property to the list. It will
	// then be synchronized to a corresponding SM property whenever the event
	// property is changed in OMi.
	//
	private Set<String> SyncOPRPropertiesToSM = null;

	// OPR event properties to synchronize to a corresponding SM Incident
	// "activity log" on change:
	//
	// "title", "description", "state", "severity", "priority", "solution",
	// "annotation", "duplicate_count",
	// "assigned_user", "assigned_group", "cause", "symptom",
	// "control_transferred_to", "time_state_changed"
	//
	// If synchronization on change is desired for an OPR event property, add
	// the property to the list. It will
	// then be synchronized to a corresponding SM property whenever the event
	// property is changed in OMi.
	//
	private Set<String> SyncOPRPropertiesToSMActivityLog = null;

	// SM Incident properties to synchronize to a corresponding OPR Event
	// property on change:
	//
	// "name", "description", "incident_status", "urgency", "priority",
	// "solution"
	//
	// If synchronization on change is desired for an SM incident property, add
	// the property to the list. It will
	// then be synchronized to a corresponding OPR event property whenever the
	// incident property is changed in SM.
	//
	private Set<String> SyncSMPropertiesToOPR = null;

	// OPR event states to synchronize to the SM incident status on change.
	//
	// If synchronize on change for an event "state" is desired, add it to the
	// list. "*" specifies all states.
	// NOTE: "state" must be included in SyncOPRPropertiesToSM or this list is
	// ignored.
	private Set<String> SyncOPRStatesToSM = null;

	// OPR event severities to synchronize to the SM incident urgency on change.
	//
	// If synchronize on change for an event "severity" is desired, add it to
	// the list. "*" specifies all severities.
	// NOTE: "severity" must be included in SyncOPRPropertiesToSM or this list
	// is ignored.
	private Set<String> SyncOPRSeveritiesToSM = null;

	// OPR event priorities to synchronize to the SM incident priority on
	// change.
	//
	// If synchronize on change for an event "priority" is desired, add it to
	// the list. "*" specifies all priorities.
	// NOTE: "priority" must be included in SyncOPRPropertiesToSM or this list
	// is ignored.
	private Set<String> SyncOPRPrioritiesToSM = null;

	// SM incident status to synchronize to the OM event states on change to
	// this incident property:
	//
	// If synchronize on change for an incident "status" is desired, add it to
	// the list. "*" specifies all status.
	// NOTE: "status" must be included in SyncSMPropertiesToOPR or this list is
	// ignored.
	private Set<String> SyncSMStatusToOPR = null;

	// SM incident urgencies to synchronize to the OM event severities on change
	// to this incident property:
	//
	// If synchronize on change for an incident "urgency" is desired, add it to
	// the list. "*" specifies all urgencies.
	// NOTE: "urgency" must be included in SyncSMPropertiesToOPR or this list is
	// ignored.
	private Set<String> SyncSMUrgenciesToOPR = null;

	// SM incident priorities to synchronize to the OM event priorities on
	// change to this incident property:
	//
	// If synchronize on change for an incident "priority" is desired, add it to
	// the list. "*" specifies all priorities.
	// NOTE: "priority" must be included in SyncSMPropertiesToOPR or this list
	// is ignored.
	private Set<String> SyncSMPrioritiesToOPR = null;

	// Map the specified OPR custom attributes to an SM incident property for
	// synchronization.
	// Add a CA name to the map along with SM incident property name (XML tag
	// name).
	// Target SM Incident property name of "activity_log" will append the CA
	// change to the SM incident activity log.
	//
	// NOTE: Only top-level SM incident properties are supported in this map.
	// EXAMPLE: ["MyCustomCA" : "activity_log", "MyCustomCA_1" :
	// "SMCustomAttribute" ]
	private Map<String, String> MapOPR2SMCustomAttribute = null;

	// Map the specified SM incident properties to an OPR event custom attribute
	// for synchronization.
	// Add an SM incident property name to the map along with OPR event custom
	// attribute name.
	//
	// EXAMPLE: ["incident_status" : "SMIncidentStatus"]
	private Map<String, String> MapSM2OPRCustomAttribute = null;

	public ConfigurationParams() {
		MapOPR2SMStatus = new HashMap<String, String>();
		MapOPR2SMStatus.put("open", "open");
		MapOPR2SMStatus.put("in_progress", "work-in-progress");
		MapOPR2SMStatus.put("resolved", "resolved");
		MapOPR2SMStatus.put("closed", "resolved");

		MapSM2OPRState = new HashMap<String, String>();
		MapSM2OPRState.put("accepted", "open");
		MapSM2OPRState.put("assigned", "open");
		MapSM2OPRState.put("open", "open");
		MapSM2OPRState.put("reopened", "open");
		MapSM2OPRState.put("pending-change", "in_progress");

		MapSM2OPRState.put("pending-customer", "in_progress");
		MapSM2OPRState.put("pending-other", "in_progress");
		MapSM2OPRState.put("pending-vendor", "in_progress");
		MapSM2OPRState.put("referred", "in_progress");

		MapSM2OPRState.put("suspended", "in_progress");
		MapSM2OPRState.put("work-in-progress", "in_progress");
		MapSM2OPRState.put("rejected", "resolved");
		MapSM2OPRState.put("replaced-problem", "resolved");
		MapSM2OPRState.put("resolved", "closed");
		MapSM2OPRState.put("cancelled", "resolved");
		MapSM2OPRState.put("closed", "closed");

		MapOPR2SMUrgency = new HashMap<String, String>();
		MapOPR2SMUrgency.put("critical", "1");
		MapOPR2SMUrgency.put("major", "2");
		MapOPR2SMUrgency.put("minor", "3");
		MapOPR2SMUrgency.put("warning", "3");
		MapOPR2SMUrgency.put("normal", "4");
		MapOPR2SMUrgency.put("unknown", "4");

		MapSM2OPRSeverity = new HashMap<String, String>();
		MapSM2OPRSeverity.put("1", "critical");
		MapSM2OPRSeverity.put("2", "major");
		MapSM2OPRSeverity.put("3", "minor");
		MapSM2OPRSeverity.put("4", "normal");

		MapOPR2SMPriority = new HashMap<String, String>();
		MapOPR2SMPriority.put("highest", "1");
		MapOPR2SMPriority.put("high", "2");
		MapOPR2SMPriority.put("medium", "3");
		MapOPR2SMPriority.put("low", "4");
		MapOPR2SMPriority.put("lowest", "4");
		MapOPR2SMPriority.put("none", "4");

		MapSM2OPRPriority = new HashMap<String, String>();
		MapSM2OPRPriority.put("1", "highest");
		MapSM2OPRPriority.put("2", "high");
		MapSM2OPRPriority.put("3", "medium");
		MapSM2OPRPriority.put("4", "low");

		SyncOPRPropertiesToSM = new HashSet<String>();
		SyncOPRPropertiesToSM.add("state");
		SyncOPRPropertiesToSM.add("solution");
		SyncOPRPropertiesToSM.add("cause");

		SyncOPRPropertiesToSMActivityLog = new HashSet<String>();
		SyncOPRPropertiesToSMActivityLog.add("title");
		SyncOPRPropertiesToSMActivityLog.add("description");
		SyncOPRPropertiesToSMActivityLog.add("state");
		SyncOPRPropertiesToSMActivityLog.add("severity");
		SyncOPRPropertiesToSMActivityLog.add("priority");
		SyncOPRPropertiesToSMActivityLog.add("annotation");
		SyncOPRPropertiesToSMActivityLog.add("duplicate_count");
		SyncOPRPropertiesToSMActivityLog.add("cause");
		SyncOPRPropertiesToSMActivityLog.add("symptom");
		SyncOPRPropertiesToSMActivityLog.add("assigned_user");
		SyncOPRPropertiesToSMActivityLog.add("assigned_group");
		SyncOPRPropertiesToSMActivityLog.add("astl_operational_device");

		SyncSMPropertiesToOPR = new HashSet<String>();
		SyncSMPropertiesToOPR.add("incident_status");
		SyncSMPropertiesToOPR.add("solution");

		SyncOPRStatesToSM = new HashSet<String>();
		SyncOPRStatesToSM.add("closed");

		SyncOPRSeveritiesToSM = new HashSet<String>();
		SyncOPRSeveritiesToSM.add("*");

		// OPR event priorities to synchronize to the SM incident priority on
		// change.
		//
		// If synchronize on change for an event "priority" is desired, add it
		// to the list. "*" specifies all priorities.
		// NOTE: "priority" must be included in SyncOPRPropertiesToSM or this
		// list is ignored.
		SyncOPRPrioritiesToSM = new HashSet<String>();
		SyncOPRPrioritiesToSM.add("*");
		// SM incident status to synchronize to the OM event states on change to
		// this incident property:
		//
		// If synchronize on change for an incident "status" is desired, add it
		// to the list. "*" specifies all status.
		// NOTE: "status" must be included in SyncSMPropertiesToOPR or this list
		// is ignored.
		SyncSMStatusToOPR = new HashSet<String>();
		SyncSMStatusToOPR.add("closed");

		// SM incident urgencies to synchronize to the OM event severities on
		// change to this incident property:
		//
		// If synchronize on change for an incident "urgency" is desired, add it
		// to the list. "*" specifies all urgencies.
		// NOTE: "urgency" must be included in SyncSMPropertiesToOPR or this
		// list is ignored.
		SyncSMUrgenciesToOPR = new HashSet<String>();
		SyncSMUrgenciesToOPR.add("*");
		// SM incident priorities to synchronize to the OM event priorities on
		// change to this incident property:
		//
		// If synchronize on change for an incident "priority" is desired, add
		// it to the list. "*" specifies all priorities.
		// NOTE: "priority" must be included in SyncSMPropertiesToOPR or this
		// list is ignored.
		SyncSMPrioritiesToOPR = new HashSet<String>();
		SyncSMPrioritiesToOPR.add("*");

		// Map the specified OPR custom attributes to an SM incident property
		// for synchronization.
		// Add a CA name to the map along with SM incident property name (XML
		// tag name).
		// Target SM Incident property name of "activity_log" will append the CA
		// change to the SM incident activity log.
		//
		// NOTE: Only top-level SM incident properties are supported in this
		// map.
		// EXAMPLE: ["MyCustomCA" : "activity_log", "MyCustomCA_1" :
		// "SMCustomAttribute" ]
		MapOPR2SMCustomAttribute = new HashMap<String, String>();

		// Map the specified SM incident properties to an OPR event custom
		// attribute for synchronization.
		// Add an SM incident property name to the map along with OPR event
		// custom attribute name.
		//
		// EXAMPLE: ["incident_status" : "SMIncidentStatus"]
		MapSM2OPRCustomAttribute = new HashMap<String, String>();

	}

	/**
	 * @param sM_WEB_TIER_NAME
	 * @param bSM_ADMINISTRATOR_LOGIN_NAME
	 * @param rTSM_PORT
	 * @param rTSM_MAX_CI_COUNT
	 * @param rTSM_HOSTNAME
	 * @param rTSM_USERNAME
	 * @param rTSM_PASSWORD
	 * @param rTSM_QUERY_MAX_STEPS
	 * @param mAX_DUPLICATE_COUNT_UPDATES
	 * @param mapOPR2SMStatus
	 * @param mapSM2OPRState
	 * @param mapOPR2SMUrgency
	 * @param mapSM2OPRSeverity
	 * @param mapOPR2SMPriority
	 * @param mapSM2OPRPriority
	 * @param sMCompletionCode
	 * @param syncAllProperties
	 * @param useNodeCI
	 * @param recursiveSearchBusinessServices
	 * @param disableCheckCloseOnUpdate
	 * @param syncOPRPropertiesToSM
	 * @param syncOPRPropertiesToSMActivityLog
	 * @param syncSMPropertiesToOPR
	 * @param syncOPRStatesToSM
	 * @param syncOPRSeveritiesToSM
	 * @param syncOPRPrioritiesToSM
	 * @param syncSMStatusToOPR
	 * @param syncSMUrgenciesToOPR
	 * @param syncSMPrioritiesToOPR
	 * @param mapOPR2SMCustomAttribute
	 * @param mapSM2OPRCustomAttribute
	 */
	public ConfigurationParams(String sM_WEB_TIER_NAME,
			String bSM_ADMINISTRATOR_LOGIN_NAME, int rTSM_PORT,
			int rTSM_MAX_CI_COUNT, String rTSM_HOSTNAME, String rTSM_USERNAME,
			String rTSM_PASSWORD, int rTSM_QUERY_MAX_STEPS,
			int mAX_DUPLICATE_COUNT_UPDATES,
			Map<String, String> mapOPR2SMStatus,
			Map<String, String> mapSM2OPRState,
			Map<String, String> mapOPR2SMUrgency,
			Map<String, String> mapSM2OPRSeverity,
			Map<String, String> mapOPR2SMPriority,
			Map<String, String> mapSM2OPRPriority, String sMCompletionCode,
			boolean syncAllProperties, boolean useNodeCI,
			boolean recursiveSearchBusinessServices,
			boolean disableCheckCloseOnUpdate,
			Set<String> syncOPRPropertiesToSM,
			Set<String> syncOPRPropertiesToSMActivityLog,
			Set<String> syncSMPropertiesToOPR, Set<String> syncOPRStatesToSM,
			Set<String> syncOPRSeveritiesToSM,
			Set<String> syncOPRPrioritiesToSM, Set<String> syncSMStatusToOPR,
			Set<String> syncSMUrgenciesToOPR,
			Set<String> syncSMPrioritiesToOPR,
			Map<String, String> mapOPR2SMCustomAttribute,
			Map<String, String> mapSM2OPRCustomAttribute) {
		super();
		SM_WEB_TIER_NAME = sM_WEB_TIER_NAME;
		BSM_ADMINISTRATOR_LOGIN_NAME = bSM_ADMINISTRATOR_LOGIN_NAME;
		RTSM_PORT = rTSM_PORT;
		RTSM_MAX_CI_COUNT = rTSM_MAX_CI_COUNT;
		RTSM_HOSTNAME = rTSM_HOSTNAME;
		RTSM_USERNAME = rTSM_USERNAME;
		RTSM_PASSWORD = rTSM_PASSWORD;
		RTSM_QUERY_MAX_STEPS = rTSM_QUERY_MAX_STEPS;
		MAX_DUPLICATE_COUNT_UPDATES = mAX_DUPLICATE_COUNT_UPDATES;
		MapOPR2SMStatus = mapOPR2SMStatus;
		MapSM2OPRState = mapSM2OPRState;
		MapOPR2SMUrgency = mapOPR2SMUrgency;
		MapSM2OPRSeverity = mapSM2OPRSeverity;
		MapOPR2SMPriority = mapOPR2SMPriority;
		MapSM2OPRPriority = mapSM2OPRPriority;
		SMCompletionCode = sMCompletionCode;
		SyncAllProperties = syncAllProperties;
		UseNodeCI = useNodeCI;
		RecursiveSearchBusinessServices = recursiveSearchBusinessServices;
		DisableCheckCloseOnUpdate = disableCheckCloseOnUpdate;
		SyncOPRPropertiesToSM = syncOPRPropertiesToSM;
		SyncOPRPropertiesToSMActivityLog = syncOPRPropertiesToSMActivityLog;
		SyncSMPropertiesToOPR = syncSMPropertiesToOPR;
		SyncOPRStatesToSM = syncOPRStatesToSM;
		SyncOPRSeveritiesToSM = syncOPRSeveritiesToSM;
		SyncOPRPrioritiesToSM = syncOPRPrioritiesToSM;
		SyncSMStatusToOPR = syncSMStatusToOPR;
		SyncSMUrgenciesToOPR = syncSMUrgenciesToOPR;
		SyncSMPrioritiesToOPR = syncSMPrioritiesToOPR;
		MapOPR2SMCustomAttribute = mapOPR2SMCustomAttribute;
		MapSM2OPRCustomAttribute = mapSM2OPRCustomAttribute;
	}

	/**
	 * @return the sM_WEB_TIER_NAME
	 */
	public String getSM_WEB_TIER_NAME() {
		return SM_WEB_TIER_NAME;
	}

	/**
	 * @return the bSM_ADMINISTRATOR_LOGIN_NAME
	 */
	public String getBSM_ADMINISTRATOR_LOGIN_NAME() {
		return BSM_ADMINISTRATOR_LOGIN_NAME;
	}

	/**
	 * @return the rTSM_PORT
	 */
	public int getRTSM_PORT() {
		return RTSM_PORT;
	}

	/**
	 * @return the rTSM_MAX_CI_COUNT
	 */
	public int getRTSM_MAX_CI_COUNT() {
		return RTSM_MAX_CI_COUNT;
	}

	/**
	 * @return the rTSM_HOSTNAME
	 */
	public String getRTSM_HOSTNAME() {
		return RTSM_HOSTNAME;
	}

	/**
	 * @return the rTSM_USERNAME
	 */
	public String getRTSM_USERNAME() {
		return RTSM_USERNAME;
	}

	/**
	 * @return the rTSM_PASSWORD
	 */
	public String getRTSM_PASSWORD() {
		return RTSM_PASSWORD;
	}

	/**
	 * @return the rTSM_QUERY_MAX_STEPS
	 */
	public int getRTSM_QUERY_MAX_STEPS() {
		return RTSM_QUERY_MAX_STEPS;
	}

	/**
	 * @return the mAX_DUPLICATE_COUNT_UPDATES
	 */
	public int getMAX_DUPLICATE_COUNT_UPDATES() {
		return MAX_DUPLICATE_COUNT_UPDATES;
	}

	/**
	 * @return the mapOPR2SMStatus
	 */
	public Map<String, String> getMapOPR2SMStatus() {
		return MapOPR2SMStatus;
	}

	/**
	 * @return the mapSM2OPRState
	 */
	public Map<String, String> getMapSM2OPRState() {
		return MapSM2OPRState;
	}

	/**
	 * @return the mapOPR2SMUrgency
	 */
	public Map<String, String> getMapOPR2SMUrgency() {
		return MapOPR2SMUrgency;
	}

	/**
	 * @return the mapSM2OPRSeverity
	 */
	public Map<String, String> getMapSM2OPRSeverity() {
		return MapSM2OPRSeverity;
	}

	/**
	 * @return the mapOPR2SMPriority
	 */
	public Map<String, String> getMapOPR2SMPriority() {
		return MapOPR2SMPriority;
	}

	/**
	 * @return the mapSM2OPRPriority
	 */
	public Map<String, String> getMapSM2OPRPriority() {
		return MapSM2OPRPriority;
	}

	/**
	 * @return the sMCompletionCode
	 */
	public String getSMCompletionCode() {
		return SMCompletionCode;
	}

	/**
	 * @return the syncAllProperties
	 */
	public boolean isSyncAllProperties() {
		return SyncAllProperties;
	}

	/**
	 * @return the useNodeCI
	 */
	public boolean isUseNodeCI() {
		return UseNodeCI;
	}

	/**
	 * @return the recursiveSearchBusinessServices
	 */
	public boolean isRecursiveSearchBusinessServices() {
		return RecursiveSearchBusinessServices;
	}

	/**
	 * @return the disableCheckCloseOnUpdate
	 */
	public boolean isDisableCheckCloseOnUpdate() {
		return DisableCheckCloseOnUpdate;
	}

	/**
	 * @return the syncOPRPropertiesToSM
	 */
	public Set<String> getSyncOPRPropertiesToSM() {
		return SyncOPRPropertiesToSM;
	}

	/**
	 * @return the syncOPRPropertiesToSMActivityLog
	 */
	public Set<String> getSyncOPRPropertiesToSMActivityLog() {
		return SyncOPRPropertiesToSMActivityLog;
	}

	/**
	 * @return the syncSMPropertiesToOPR
	 */
	public Set<String> getSyncSMPropertiesToOPR() {
		return SyncSMPropertiesToOPR;
	}

	/**
	 * @return the syncOPRStatesToSM
	 */
	public Set<String> getSyncOPRStatesToSM() {
		return SyncOPRStatesToSM;
	}

	/**
	 * @return the syncOPRSeveritiesToSM
	 */
	public Set<String> getSyncOPRSeveritiesToSM() {
		return SyncOPRSeveritiesToSM;
	}

	/**
	 * @return the syncOPRPrioritiesToSM
	 */
	public Set<String> getSyncOPRPrioritiesToSM() {
		return SyncOPRPrioritiesToSM;
	}

	/**
	 * @return the syncSMStatusToOPR
	 */
	public Set<String> getSyncSMStatusToOPR() {
		return SyncSMStatusToOPR;
	}

	/**
	 * @return the syncSMUrgenciesToOPR
	 */
	public Set<String> getSyncSMUrgenciesToOPR() {
		return SyncSMUrgenciesToOPR;
	}

	/**
	 * @return the syncSMPrioritiesToOPR
	 */
	public Set<String> getSyncSMPrioritiesToOPR() {
		return SyncSMPrioritiesToOPR;
	}

	/**
	 * @return the mapOPR2SMCustomAttribute
	 */
	public Map<String, String> getMapOPR2SMCustomAttribute() {
		return MapOPR2SMCustomAttribute;
	}

	/**
	 * @return the mapSM2OPRCustomAttribute
	 */
	public Map<String, String> getMapSM2OPRCustomAttribute() {
		return MapSM2OPRCustomAttribute;
	}
	
	
	

}
