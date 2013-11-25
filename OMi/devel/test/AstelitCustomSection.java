package test;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;

import com.hp.opr.api.ws.model.event.OprEvent;

public class AstelitCustomSection {

	/*private ConfigurationParams confp = null;
	private LocalizationParams localp = null;
	private XMLParams  xmlp = null;*/
	
	private OprEvent event = null;
	private String title = null;
	private String description = null;
	private String astl_title;
	private String astl_description;
	
	private StringWriter writer = null;
	private StringBuffer activityLog = null;
	private DocumentBuilder builder = null;
	private Document doc = null;

    private ServiceManagerAdapter sma;
    private Log log = null;
	
	boolean isNewIncident = false;
	int titleLength = 255;
	int descriptionLength = 255;
	


    public AstelitCustomSection(ServiceManagerAdapter sma, OprEvent event, boolean isNewIncident, String astl_title, String astl_description) throws ParserConfigurationException {
        this.event = event;
        this.isNewIncident = isNewIncident;
        this.astl_description = astl_description;
        this.astl_title = astl_title;
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.writer = new StringWriter();
        this.activityLog = new StringBuffer();
        this.sma = sma;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        builder = docFactory.newDocumentBuilder();
         //TODO implement getter for ServiceManagerAdapter
        this.log = (Log) sma.m_log;
    }
	
	public AstelitCustomSection(OprEvent event, String astl_title, String astl_description, boolean isNewIncident, ConfigurationParams confp, LocalizationParams localp, XMLParams xmlp) throws ParserConfigurationException{
		this.event = event;
		this.astl_title = astl_title;
		this.astl_description = astl_description;
		this.isNewIncident = isNewIncident;
		this.title = event.getTitle();
		this.description = event.getDescription();
		this.writer = new StringWriter();
		this.activityLog = new StringBuffer();
		
		/*this.confp = confp;
		this.localp = localp;
		this.xmlp = xmlp;*/
				
		
		//Creation of document builder
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		builder = docFactory.newDocumentBuilder();
		
	}
	
	
	private void normalizeString(String abnormal){
		if (abnormal != null) {
			if (abnormal.length() > 0) {
				abnormal = abnormal.trim().replace('\r', '\n');
			} else {
				abnormal = null;
			}
	}
	}
	
	private void truncater(String toTruncate, int length){
		if (toTruncate != null) {
			toTruncate = toTruncate.trim();
				if (toTruncate.length() > length - 1 ) {
					toTruncate.substring(0, length); }
		}
	}

	private void truncater(String toTruncate){
		truncater(toTruncate, titleLength);
	}
	
	private void truncater(OprEvent evTruncate){
		String evTitle = evTruncate.getTitle();
		if (evTitle.contains("\\n")){
			String[] titleArray = evTruncate.getTitle().split("\\n");
			evTitle = titleArray[0];
		}
		String evDescription = evTruncate.getDescription();
		if (evDescription == null) {
			evDescription = evTitle;
		}
		truncater(evTitle, titleLength);
		truncater(evDescription, descriptionLength);
		event.setTitle(evTitle);
		event.setDescription(evDescription);
		
	}
	
	
		
		

	public String process() {
	//##################################### ASTELIT CUSTOM SECTION #####################################


			// get the title & description.
//			String title = (event.title && event.title.trim()) ? event.title.trim().replace('\r', '\n') : null;
//			String description = (event.description && event.description.trim()) ? event.description.trim() : null;
			normalizeString(event.getTitle());
			normalizeString(event.getDescription());
					
					
					
					
					
			if (astl_title != null) {
				title = astl_title;
			}
			if (astl_description != null) {
				description = astl_description;
			}
			/*
			 *Truncate event fields to suitable length 
			 */
			truncater(event);
		
			
			
			// create the XML payload using the MarkupBuilder
		
			//was created at constructor body
//			
//
//			builder."${INCIDENT_TAG}"(relationships_included: "${INCIDENT_XML_RELATIONSHIPS}",
//				type: "${INCIDENT_XML_TYPE}",
//				version: "${INCIDENT_XML_VERSION}",
//				xmlns: "${INCIDENT_XML_NAMESPACE}") {
//
			doc = builder.newDocument();
			Element incident = doc.createElement(ServiceManagerAdapter.INCIDENT_TAG);
			Attr incidentXMLRelationships = doc.createAttribute("relationships_included");
			incidentXMLRelationships.setValue(ServiceManagerAdapter.INCIDENT_XML_RELATIONSHIPS);
			incident.setAttributeNode(incidentXMLRelationships);
			incident.setAttribute("type", ServiceManagerAdapter.INCIDENT_XML_TYPE);
			incident.setAttribute("version", ServiceManagerAdapter.INCIDENT_XML_VERSION);
			incident.setAttribute("xmlns", ServiceManagerAdapter.INCIDENT_XML_NAMESPACE);
			doc.appendChild(incident);
			
			
			
			
//			  builder.it_process_category(IT_PROCESS_CATEGORY)
//			  builder.incident_type(INCIDENT_TYPE)
//			  if (SpecifyActiveProcess)
//				builder.active_process("true")
//			
//				
//				/*
//				 * Generates whether device is operational 
//				 */
//			  builder."${OPERATIONAL_DEVICE_TAG}"(astl_operational_device)
//			
//			  final String dnsName = getDnsName(event)
//			  if (dnsName != null) {
//				  		  builder."${NODE_FQDN}"(dnsName)
//			  }
//			  
//		
//			 
//			 
//			 
//			  activityLog.append('\n').append(ACTIVITY_LOG_OPERATIONAL_DATA).append('\n').
//					 append(astl_operational_device).append('\n')
//	 
//			  if (astl_priority) {
//				 if(astl_priority == "1"){
//					 builder."${IMPACT_SCOPE_TAG}"(label: "Enterprise", 'enterprise')
//			     } 
//			     if(astl_priority == "2"){
//					 builder."${IMPACT_SCOPE_TAG}"(label: "Site/Dept", 'site-dept')
//			     } 
//			     if(astl_priority == "3"){
//					 builder."${IMPACT_SCOPE_TAG}"(label: "Multiple Users", 'multiple-users')
//			     } 
//			     if(astl_priority == "4"){
//					 builder."${IMPACT_SCOPE_TAG}"(label: "User", 'user')
//			     } 
//			  } else {
//				 if(MapOPR2SMUrgency[event.severity] == "1"){
//					 builder."${IMPACT_SCOPE_TAG}"(label: "Site/Dept", 'site-dept')
//			     } 
//			     if(MapOPR2SMUrgency[event.severity] == "2"){
//					 builder."${IMPACT_SCOPE_TAG}"(label: "Multiple Users", 'multiple-users')
//			     } 
//			     if(MapOPR2SMUrgency[event.severity] == "3"){
//					 builder."${IMPACT_SCOPE_TAG}"(label: "User", 'user')
//			     } 
//			     if(MapOPR2SMUrgency[event.severity] == "4"){
//					 builder."${IMPACT_SCOPE_TAG}"(label: "User", 'user')
//			     }
//			  }
//	//TODO is new incident
//			  if (isNewIncident) {
//				// Add 'Time OMi Event Created' to activity log
//				if (event.timeCreated) {
//				  activityLog.append('\n').append(ACTIVITY_LOG_TIME_CREATED).append('\n').
//					  append(dateFormatter.format(event.timeCreated)).append('\n')
//				}
//				// Add 'Time OMi Event Received' to activity log
//				if (event.timeReceived) {
//				  activityLog.append('\n').append(ACTIVITY_LOG_TIME_RECEIVED).append('\n').
//					  append(dateFormatter.format(event.timeReceived)).append('\n')
//				}
//				// set the external process id, category, subCategory and related CI for new Incidents
//				builder."${EXTERNAL_PROCESS_ID_TAG}"(externalRefId)
//
//				// set the related CI on new incidents
//				final OprNodeReference nodeRef = event.node
//				final OprRelatedCi relatedCi = event.relatedCi
//				
//				// Astelit's Default Related CI Name
//				//TODO remove _OS part
//				String astelitRelatedCI = relatedCi.configurationItem.ciName + " OS"
//								
//				if (relatedCi  != null && !UseNodeCI) {
//				  // send 'is_registered_for' CI information using event related CI
//				  builder."${CI_RELATIONSHIP}"(target_role: "${CONFIGURATION_ITEM_ROLE}") {
//					if (relatedCi.configurationItem.globalId)
//					  builder."${CI_GLOBALID_TAG}"(relatedCi.configurationItem.globalId)
//					builder."${CI_TARGET_TYPE_TAG}"(CI_TARGET_TYPE)
//					builder."${CONFIGURATION_ITEM_TAG}" {
//					  if (relatedCi.configurationItem.ciType)
//						builder."${CI_TYPE_TAG}"(relatedCi.configurationItem.ciType)
//					  if (relatedCi.configurationItem.id)
//						builder."${CI_ID_TAG}"(relatedCi.configurationItem.id)
//
//					  //if (relatedCi.configurationItem.ciName)
//						//builder."${CI_NAME_TAG}"(relatedCi.configurationItem.ciName)
//					
//					  //Astelit Related CI
//					  if (astl_logical_name != null) {
//						
//						  
//						
//						  builder."${CI_NAME_TAG}"(astl_logical_name)
//						builder."${CI_DISPLAY_LABEL_TAG}"(astl_logical_name)
//					  } else {
//						if (astelitRelatedCI)
//							builder."${CI_NAME_TAG}"(astelitRelatedCI)
//							builder."${CI_DISPLAY_LABEL_TAG}"(astelitRelatedCI)
//					  }
//					 
//					  if (dnsName)
//						builder."${NODE_DNS_NAME_TAG}"(dnsName)
//					  if (nodeRef != null && !relatedCi.configurationItem.id.equals(nodeRef.node.id)) {
//						// send 'is_hosted_on' CI information using event node CI
//						builder."${NODE_RELATIONSHIP}"(target_role: "${NODE_ITEM_ROLE}") {
//						  if (nodeRef.node.globalId)
//							builder."${CI_GLOBALID_TAG}"(nodeRef.node.globalId)
//						  builder."${CI_TARGET_TYPE_TAG}"(nodeRef.node.selfType.toString())
//						  builder."${CONFIGURATION_ITEM_TAG}" {
//							if (nodeRef.node.ciType)
//							  builder."${CI_TYPE_TAG}"(nodeRef.node.ciType)
//							if (nodeRef.node.id)
//							  builder."${CI_ID_TAG}"(nodeRef.node.id)
//							if (nodeRef.node.ciName)
//							  builder."${CI_NAME_TAG}"(nodeRef.node.ciName)
//							if (nodeRef.node.ciDisplayLabel)
//							  builder."${CI_DISPLAY_LABEL_TAG}"(nodeRef.node.ciDisplayLabel)
//							if (dnsName)
//							  builder."${NODE_DNS_NAME_TAG}"(dnsName)
//						  }
//						}
//					  }
//					}
//				  }
//				  
//				  activityLog.append('\n')
//				  activityLog.append(ACTIVITY_LOG_RELATED_CI)
//				  if (relatedCi.configurationItem.ciDisplayLabel)
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_LABEL).
//						append(relatedCi.configurationItem.ciDisplayLabel)
//
//		//          if (relatedCi.configurationItem.ciName)
//		//            activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
//				 if (astl_logical_name) {
//					 activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
//						append(astl_logical_name)
//				} else {
//					if (astelitRelatedCI)
//						activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).
//							append(astelitRelatedCI)
//				}
//		 
//				  if (m_oprVersion >= 920 && relatedCi.configurationItem.ciTypeLabel)
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).
//						append(relatedCi.configurationItem.ciTypeLabel)
//				  if (relatedCi.configurationItem.ciType)
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE).
//						append(relatedCi.configurationItem.ciType)
//				  if (event.drilldownUrl && relatedCi.configurationItem.id) {
//					final URL eventUrl = event.drilldownUrl
//					final URL ciUrl = new URL(eventUrl.protocol, eventUrl.host, eventUrl.port,
//						"${BSM_CI_DRILLDOWN_PATH}${relatedCi.configurationItem.id}")
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString())
//				  }
//				  if (dnsName)
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName)
//				  activityLog.append('\n')
//				}
//				else if (nodeRef != null) {
//				  // send 'is_registered_for' CI information using event node CI
//				  builder."${CI_RELATIONSHIP}"(target_role: "${CONFIGURATION_ITEM_ROLE}") {
//					if (nodeRef.node.globalId)
//					  builder."${CI_GLOBALID_TAG}"(nodeRef.node.globalId)
//					builder."${CI_TARGET_TYPE_TAG}"(CI_TARGET_TYPE)
//					builder."${CONFIGURATION_ITEM_TAG}" {
//					  if (nodeRef.node.ciType)
//						builder."${CI_TYPE_TAG}"(nodeRef.node.ciType)
//		
//						 if (nodeRef.node.id)
//						builder."${CI_ID_TAG}"(nodeRef.node.id)
//					  if (nodeRef.node.ciName)
//						builder."${CI_NAME_TAG}"(nodeRef.node.ciName)
//					  if (nodeRef.node.ciDisplayLabel)
//						builder."${CI_DISPLAY_LABEL_TAG}"(nodeRef.node.ciDisplayLabel)
//					  if (dnsName)
//						builder."${NODE_DNS_NAME_TAG}"(dnsName)
//					}
//				  }
//				  activityLog.append('\n')
//				  activityLog.append(ACTIVITY_LOG_RELATED_CI)
//				  if (nodeRef.node.ciDisplayLabel)
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_LABEL).append(nodeRef.node.ciDisplayLabel)
//				  if (nodeRef.node.ciName)
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_NAME).append(nodeRef.node.ciName)
//				  if (m_oprVersion >= 920 && nodeRef.node.ciTypeLabel)
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE_LABEL).append(nodeRef.node.ciTypeLabel)
//				  if (nodeRef.node.ciType)
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_TYPE).append(nodeRef.node.ciType)
//				  if (event.drilldownUrl && nodeRef.node.id) {
//					final URL eventUrl = event.drilldownUrl
//					final URL ciUrl = new URL(eventUrl.protocol, eventUrl.host, eventUrl.port,
//						"${BSM_CI_DRILLDOWN_PATH}${nodeRef.node.id}")
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_URL).append(ciUrl.toString())
//				  }
//				  if (dnsName)
//					activityLog.append('\n').append(ACTIVITY_LOG_RELATED_CI_HOSTED_ON).append(dnsName)
//				  activityLog.append('\n')
//				}
//				// check for most affected business service in 9.21 or greater
//				if (m_oprVersion > 920)
//				  setBusinessService(event, builder, activityLog)
//				else {
//				  if ((RTSM_QUERY_MAX_STEPS > 0) && (relatedCi != null) && relatedCi.targetId)
//					setBusinessServicePre921(relatedCi.configurationItem, event.drilldownUrl, builder, activityLog)
//				}
//
//				if (astl_category) {
//				  builder."${CATEGORY_TAG}"(astl_category)
//				  activityLog.append('\n').append(ACTIVITY_LOG_CATEGORY).append('\n').append(astl_category).append('\n')
//				} else {
//				  builder."${CATEGORY_TAG}"("${ASTELIT_CATEGORY}")
//				  activityLog.append('\n').append(ACTIVITY_LOG_CATEGORY).append('\n').append("${ASTELIT_CATEGORY}").append('\n')
//				}
//			
//				if (astl_sub_category) {
//					builder."${SUB_CATEGORY_TAG}"(astl_sub_category)
//				  activityLog.append('\n').append(ACTIVITY_LOG_SUBCATEGORY).append('\n').append(astl_sub_category).append('\n')
//				} else {
//					builder."${SUB_CATEGORY_TAG}"("${ASTELIT_SUB_CATEGORY}")
//				  activityLog.append('\n').append(ACTIVITY_LOG_SUBCATEGORY).append('\n').append("${ASTELIT_SUB_CATEGORY}").append('\n')
//				}
//			
//				if (event.application) {
//				  activityLog.append('\n').append(ACTIVITY_LOG_APPLICATION).append('\n').append(event.application).append('\n')
//				}
//				if (event.object) {
//				  activityLog.append('\n').append(ACTIVITY_LOG_OBJECT).append('\n').append(event.object).append('\n')
//				}
//				// Add 'Original Data' to activity log
//				if (event.originalData) {
//				  activityLog.append('\n').append(ACTIVITY_LOG_ORIGINAL_DATA).append('\n').
//					  append(event.originalData).append('\n')
//				}
//			  }
//
//			  // Add 'Time OMi Event State Changed' to activity log
//			  if (event.timeStateChanged
//				  && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("time_state_changed"))) {
//				activityLog.append('\n').append(ACTIVITY_LOG_TIME_STATE_CHANGED).append('\n').
//					append(dateFormatter.format(event.timeStateChanged)).append(' : ').append(event.state).append('\n')
//			  }
//
//			  // check title
//			  if (title
//				  && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("title")))
//				builder."${TITLE_TAG}"(title)
//
//			  if (event.title
//				  && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("title"))) {
//				// synchronize the title to the incident activity log
//				activityLog.append('\n')
//				activityLog.append(ACTIVITY_LOG_TITLE).append("\n")
//				if (!isNewIncident)
//				  activityLog.append(ACTIVITY_LOG_TITLE_CHANGE)
//				activityLog.append(event.title.trim())
//				activityLog.append('\n')
//			  }
//
//			  // check description
//			  if (description
//				  && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("description")))
//				builder."${DESCRIPTION_TAG}"(description)
//
//			  if (event.description
//				  && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("description"))) {
//				// synchronize the description to the incident activity log
//				activityLog.append('\n')
//				activityLog.append(ACTIVITY_LOG_DESCRIPTION).append("\n")
//				if (!isNewIncident)
//				  activityLog.append(ACTIVITY_LOG_DESCRIPTION_CHANGE)
//				activityLog.append(event.description.trim())
//				activityLog.append('\n')
//			  }
//
//			  // check solution
//			  if (event.solution
//				  && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("solution")))
//				builder."${SOLUTION_TAG}"(event.solution)
//
//			  if (event.solution
//				  && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("solution"))) {
//				// synchronize the solution to the incident activity log
//				activityLog.append('\n')
//				activityLog.append(ACTIVITY_LOG_SOLUTION).append("\n")
//				if (!isNewIncident)
//				  activityLog.append(ACTIVITY_LOG_SOLUTION_CHANGE)
//				activityLog.append(event.solution.trim())
//				activityLog.append('\n')
//			  }
//
//			  // check assigned user
//			  if (((event.assignedUser?.id >= 0) && (event.assignedUser?.userName || event.assignedUser?.loginName))
//				  && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_user")))
//				builder."${REQUESTED_BY_TAG}" {
//				  builder."${PARTY_TAG}" {
//					if (event.assignedUser?.userName)
//					  builder."${UI_NAME_TAG}"(event.assignedUser.userName)
//					if (event.assignedUser?.loginName)
//					  builder."${NAME_TAG}"(event.assignedUser.loginName)
//				  }
//				}
//
//			  // set the contact name to the BSM assigned user
//			  if (event.assignedUser?.userName
//				  && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_user"))) {
//				// synchronize the assigned user to the incident activity log
//				activityLog.append('\n')
//				activityLog.append(ACTIVITY_LOG_ASSIGNED_USER).append("\n")
//				if (!isNewIncident)
//				  activityLog.append(ACTIVITY_LOG_ASSIGNED_USER_CHANGE)
//				if (event.assignedUser?.id < 0)
//				  activityLog.append(ACTIVITY_LOG_UNASSIGNED)
//				else
//				  activityLog.append(event.assignedUser.userName.trim())
//				activityLog.append('\n')
//			  }
//
//			  // check assigned group
//			  if (astl_assignment_group && (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("assigned_group")))
//				builder."${ASSIGNED_GROUP_TAG}" {
//				  builder."${FUNCTIONAL_GROUP_TAG}" {
//					builder."${UI_NAME_TAG}"(astl_assignment_group)
//					builder."${NAME_TAG}"(astl_assignment_group)
//				  }
//				}
//
//			  // set the functional group name to the BSM assigned group
//			  if (event.assignedGroup?.name
//				  && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("assigned_group"))) {
//				// synchronize the assigned user to the incident activity log
//				activityLog.append('\n')
//				activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP).append("\n")
//				if (!isNewIncident)
//				  activityLog.append(ACTIVITY_LOG_ASSIGNED_GROUP_CHANGE)
//				
//				if (astl_assignment_group) {
//					activityLog.append(astl_assignment_group)
//					activityLog.append('\n') }
//				else {
//					if (event.assignedGroup?.id < 0)
//					  activityLog.append(ACTIVITY_LOG_UNASSIGNED)
//					else
//					  activityLog.append(event.assignedGroup.name.trim())
//					activityLog.append('\n')
//				}
//			  }
//
//			  // check state
//			  if (event.state
//				  && (isNewIncident
//				  || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("state"))
//				  && (syncAllOPRStatesToSM || SyncOPRStatesToSM.contains(event.state))))) {
//				String status = MapOPR2SMStatus[event.state]
//				builder."${INCIDENT_STATUS_TAG}"(status)
//				if ("closed".equals(status)) {
//				  builder."${COMPLETION_CODE_TAG}"(SMCompletionCode)
//				}
//			  }
//
//			  if (event.state
//				  && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("state"))) {
//				// synchronize the state to the incident activity log
//				activityLog.append('\n')
//				activityLog.append(ACTIVITY_LOG_STATE).append("\n")
//				if (!isNewIncident)
//				  activityLog.append(ACTIVITY_LOG_STATE_CHANGE)
//				activityLog.append(event.state.trim())
//				activityLog.append('\n')
//			  }
//
//			  // check urgency/severity
//			  
//			  if (astl_priority) {
//				if (astl_priority
//				  && (isNewIncident
//				  || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("severity"))
//				  && (syncAllOPRSeveritiesToSM || SyncOPRSeveritiesToSM.contains(astl_priority)))))
//				builder."${URGENCY_TAG}"(astl_priority)
//				
//				if (astl_priority
//					&& (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("severity"))) {
//					// synchronize the severity to the incident activity log
//					activityLog.append('\n')
//					activityLog.append(ACTIVITY_LOG_SEVERITY).append("\n")
//					if (!isNewIncident)
//					  activityLog.append(ACTIVITY_LOG_SEVERITY_CHANGE)
//					activityLog.append(event.severity)
//					activityLog.append('\n')
//				  }
//			  } else {
//					if(MapOPR2SMUrgency[event.severity] == "1"){
//						astl_urgency = "2"
//					} 
//					if(MapOPR2SMUrgency[event.severity] == "2"){
//						astl_urgency = "3"
//					} 
//					if(MapOPR2SMUrgency[event.severity] == "3"){
//						astl_urgency = "4"
//					} 
//					if(MapOPR2SMUrgency[event.severity] == "4"){
//						astl_urgency = "4"
//					}
//			  
//				  if (astl_urgency
//					  && (isNewIncident
//					  || ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("severity"))
//					  && (syncAllOPRSeveritiesToSM || SyncOPRSeveritiesToSM.contains(astl_urgency)))))
//						builder."${URGENCY_TAG}"(astl_urgency)
//						
//				  if (astl_urgency
//					&& (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("severity"))) {
//					// synchronize the severity to the incident activity log
//					activityLog.append('\n')
//					activityLog.append(ACTIVITY_LOG_SEVERITY).append("\n")
//					if (!isNewIncident)
//					  activityLog.append(ACTIVITY_LOG_SEVERITY_CHANGE)
//					activityLog.append(event.severity)
//					activityLog.append('\n')
//				  }
//			  }
//
//			  // check priority
//			  //if (event.priority
//				  //&& (isNewIncident
//				  //|| ((syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("priority"))
//				  //&& (syncAllOPRPrioritiesToSM || SyncOPRPrioritiesToSM.contains(event.priority)))))
//				//builder."${PRIORITY_TAG}"(MapOPR2SMPriority[event.priority])
//
//			  if (event.priority
//				  && (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("priority"))) {
//				// synchronize the priority to the incident activity log
//				activityLog.append('\n')
//				activityLog.append(ACTIVITY_LOG_PRIORITY).append("\n")
//				if (!isNewIncident)
//				  activityLog.append(ACTIVITY_LOG_PRIORITY_CHANGE)
//				activityLog.append(event.priority)
//				activityLog.append('\n')
//			  }
//
//			  // set is_recorded_by (opened.by) to "admin" or Control Transfer initiator
//			  if (isNewIncident) {
//				boolean initiatedBySystem = (event.controlTransferredTo?.initiatedBy == null) ||
//					("system".equals(event.controlTransferredTo.initiatedBy))
//				String recorder = initiatedBySystem ? BSM_ADMINISTRATOR_LOGIN_NAME : event.controlTransferredTo.initiatedBy
//				builder."${RECORDED_BY_TAG}" {
//				  builder."${PERSON_TAG}" {
//					builder."${UI_NAME_TAG}"(recorder)
//					builder."${NAME_TAG}"(recorder)
//				  }
//				}
//				// Add initiator info to Activity Log
//				OprForwardingInfo forwardingInfo = event.getForwardingInfo(m_connectedServerId)
//				if (initiatedBySystem && (m_oprVersion >= 910) && forwardingInfo?.ruleName) {
//				  activityLog.append('\n').
//					  append(ACTIVITY_LOG_INITIATED_BY).append("\n").
//					  append(ACTIVITY_LOG_INITIATED_BY_RULE).
//					  append(forwardingInfo.ruleName)
//				} else {
//				  activityLog.append('\n').
//					  append(ACTIVITY_LOG_INITIATED_BY).append("\n").
//					  append(ACTIVITY_LOG_INITIATED_BY_USER).
//					  append(recorder)
//				}
//				activityLog.append('\n')
//			  }
//
//			  if (event.controlTransferredTo
//				  && !m_node.equalsIgnoreCase(event.controlTransferredTo.dnsName)
//				  && OprControlTransferStateEnum.transferred.name().equals(event.controlTransferredTo.state)
//				  && (syncAllOPRPropertiesToSMActivityLog
//				  || SyncOPRPropertiesToSMActivityLog.contains("control_transferred_to"))) {
//				// synchronize the priority to the incident activity log
//				activityLog.append('\n')
//				activityLog.append(ACTIVITY_LOG_CONTROL_TRANSFERRED_TO).append("\n")
//				activityLog.append(event.controlTransferredTo.dnsName).append(":").append(event.controlTransferredTo.state)
//				activityLog.append('\n')
//			  }
//
//			  // check if there are any annotations to add to the activity log
//			  if ((event.annotations != null)
//				  && (isNewIncident
//				  || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("annotation"))) {
//				// append any annotations
//				event.annotations?.annotations?.each() { OprAnnotation annotation ->
//				  final String text = annotation.text
//				  if (text.length() > 0) {
//					final String date = dateFormatter.format(annotation.timeCreated)
//					final String author = annotation.author
//					activityLog.append('\n')
//					activityLog.append(ACTIVITY_LOG_ANNOTATION).append("\n - ${date} - ${author} - ${text}")
//					activityLog.append('\n')
//				  }
//				}
//			  }
//
//			  // check if there are any custom attributes to add to the activity log
//			  if (!m_OPR2SMCustomAttribute.isEmpty() && (event.customAttributes != null)) {
//				event.customAttributes.customAttributes?.each() { OprCustomAttribute customAttribute ->
//				  final String caName = customAttribute.name.toLowerCase(LOCALE)
//
//				  if (m_OPR2SMCustomAttribute.containsKey(caName)) {
//					final String smIncidentProperty = m_OPR2SMCustomAttribute.get(caName)
//					// synchronize this CA to SM
//					if (ACTIVITY_LOG_TAG.equals(smIncidentProperty)) {
//					  // synchronize the CA to the SM incident activity log
//					  activityLog.append('\n')
//					  activityLog.append(ACTIVITY_LOG_CA).append("\n${customAttribute.name}=${customAttribute.value}")
//					  activityLog.append('\n')
//					}
//					else {
//					  // synchronize to the specified SM incident property
//					  builder."${smIncidentProperty}"(customAttribute.value)
//					}
//				  }
//				}
//			  }
//
//			  String drilldownUrl = event.drilldownUrl
//			  if (drilldownUrl && drilldownUrl.lastIndexOf('=') > 0)
//				drilldownUrl = drilldownUrl.substring(0, drilldownUrl.lastIndexOf('=') + 1)
//
//			  if (event.cause) {
//				if (causeExternalRefId) {
//				  if (isNewIncident || syncAllOPRPropertiesToSM || SyncOPRPropertiesToSM.contains("cause")) {
//					builder."${IS_CAUSED_BY}"(target_role: "${IS_CAUSED_BY_ROLE}") {
//					  builder."${MASTER_REFERENCE_TAG}"(causeExternalRefId)
//					}
//				  }
//				  if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
//					// synchronize the SM cause to the incident activity log
//					activityLog.append('\n')
//					activityLog.append(ACTIVITY_LOG_CAUSE).append("\n").append(causeExternalRefId)
//					activityLog.append('\n')
//				  }
//				}
//				else if (syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("cause")) {
//				  // synchronize the OPR cause to the incident activity log
//				  activityLog.append('\n')
//				  final String causeTitle = event.cause.title
//				  final String causeUrl = (drilldownUrl) ? drilldownUrl + event.cause.targetId : null
//				  if (causeUrl)
//					activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append("${causeTitle}\n\t${causeUrl}")
//				  else
//					activityLog.append(ACTIVITY_LOG_OMI_CAUSE).append("\n").append(causeTitle)
//				  activityLog.append('\n')
//				}
//			  }
//
//			  if ((event.symptoms != null)
//				  && (isNewIncident
//				  || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("symptom"))) {
//				// synchronize the OPR symptom to the incident activity log
//				event.symptoms.eventReferences?.each() { OprSymptomReference symptomRef ->
//				  activityLog.append('\n')
//				  final String symptomTitle = symptomRef.title
//				  final String symptomUrl = (drilldownUrl) ? drilldownUrl + symptomRef.targetId : null
//				  if (symptomUrl)
//					activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append("${symptomTitle}\n\t${symptomUrl}")
//				  else
//					activityLog.append(ACTIVITY_LOG_OMI_SYMPTOM).append("\n").append(symptomTitle)
//				  activityLog.append('\n')
//				}
//			  }
//
//			  if (duplicateChange
//				  && (isNewIncident
//				  || syncAllOPRPropertiesToSMActivityLog || SyncOPRPropertiesToSMActivityLog.contains("duplicate_count"))) {
//				final Integer previousCount = (Integer) duplicateChange.previousValue
//				final Integer currentCount = (Integer) duplicateChange.currentValue
//
//				if (currentCount > 0) {
//				  // synchronize the duplicate count to the incident activity log
//				  if (previousCount == null) {
//					activityLog.append('\n')
//					activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n").append(currentCount)
//				  }
//				  else {
//					activityLog.append('\n')
//					activityLog.append(ACTIVITY_LOG_DUPLICATE_COUNT).append("\n")
//					activityLog.append("${ACTIVITY_LOG_PREVIOUS} ${previousCount} ${ACTIVITY_LOG_CURRENT} ${currentCount}")
//				  }
//				  activityLog.append('\n')
//				}
//			  }
//
//			  // set any activityLog
//			  if (activityLog.length() > 0) {
//				builder."${ACTIVITY_LOG_TAG}" {
//				  builder."${ACTIVITY_LOG_DESC_TAG}"(activityLog.toString())
//				}
//			  }
//			}
//			final String output = writer.toString()
		

			return ;
	//##################################### END ASTELIT CUSTOM SECTION #####################################	
	}	
}
