import com.hp.opr.api.Version;
import com.hp.opr.api.ws.adapter.ForwardChangeArgs;
import com.hp.opr.api.ws.adapter.ForwardEventArgs;
import com.hp.opr.api.ws.adapter.GetExternalEventArgs;
import com.hp.opr.api.ws.adapter.InitArgs;
import com.hp.opr.api.ws.adapter.PingArgs;
import com.hp.opr.api.ws.adapter.ReceiveChangeArgs;
import com.hp.opr.api.ws.model.event.OprAnnotationList;
import com.hp.opr.api.ws.model.event.OprControlTransferInfo;
import com.hp.opr.api.ws.model.event.OprControlTransferStateEnum;
import com.hp.opr.api.ws.model.event.OprCustomAttribute;
import com.hp.opr.api.ws.model.event.OprCustomAttributeList;
import com.hp.opr.api.ws.model.event.OprEvent;
import com.hp.opr.api.ws.model.event.OprEventChange;
import com.hp.opr.api.ws.model.event.OprEventReference;
import com.hp.opr.api.ws.model.event.OprGroup;
import com.hp.opr.api.ws.model.event.OprPriority;
import com.hp.opr.api.ws.model.event.OprSeverity;
import com.hp.opr.api.ws.model.event.OprState;
import com.hp.opr.api.ws.model.event.OprSymptomList;
import com.hp.opr.api.ws.model.event.OprSymptomReference;
import com.hp.opr.api.ws.model.event.OprUser;
import com.hp.opr.api.ws.model.event.ci.OprConfigurationItem;
import com.hp.opr.api.ws.model.event.ci.OprForwardingInfo;
import com.hp.opr.api.ws.model.event.ci.OprForwardingTypeEnum;
import com.hp.opr.api.ws.model.event.ci.OprNodeReference;
import com.hp.opr.api.ws.model.event.ci.OprRelatedCi;
import com.hp.opr.api.ws.model.event.property.OprAnnotationPropertyChange;
import com.hp.opr.api.ws.model.event.property.OprCustomAttributePropertyChange;
import com.hp.opr.api.ws.model.event.property.OprEventPropertyChange;
import com.hp.opr.api.ws.model.event.property.OprEventPropertyNameEnum;
import com.hp.opr.api.ws.model.event.property.OprGroupPropertyChange;
import com.hp.opr.api.ws.model.event.property.OprIntegerPropertyChange;
import com.hp.opr.api.ws.model.event.property.OprPropertyChangeOperationEnum;
import com.hp.opr.api.ws.model.event.property.OprSymptomPropertyChange;
import com.hp.opr.api.ws.model.event.property.OprUserPropertyChange;
import com.hp.opr.common.ws.client.WinkClientSupport;
import com.hp.ucmdb.api.UcmdbService;
import com.hp.ucmdb.api.UcmdbServiceFactory;
import com.hp.ucmdb.api.UcmdbServiceProvider;
import com.hp.ucmdb.api.topology.QueryDefinition;
import com.hp.ucmdb.api.topology.QueryLink;
import com.hp.ucmdb.api.topology.QueryNode;
import com.hp.ucmdb.api.topology.Topology;
import com.hp.ucmdb.api.topology.TopologyCount;
import com.hp.ucmdb.api.topology.TopologyQueryService;
import com.hp.ucmdb.api.topology.TopologyUpdateFactory;
import com.hp.ucmdb.api.topology.indirectlink.IndirectLink;
import com.hp.ucmdb.api.topology.indirectlink.IndirectLinkStepToPart;
import com.hp.ucmdb.api.types.TopologyCI;
import com.hp.ucmdb.api.types.UcmdbId;

import groovy.util.slurpersupport.GPathResult;
import groovy.xml.MarkupBuilder;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBElement;

import org.apache.commons.codec.binary.Base64;
import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AstelitRulesSection {

	public boolean isNewIncident;
	public boolean default_flag;
	public String astl_related_ci = null;
	public String astl_ci_os_name = null;
	public String astl_assignment_group = null;
	public String astl_logical_name = null;
	public String astl_priority = null;
	public String astl_urgency = null;
	public String astl_title = null;
	public String astl_description = null;
	public String astl_category = null;
	public String astl_sub_category = null;
	public boolean astl_operational_device = false;
	public Matcher myMatcher = null;
	public OprRelatedCi relatedCi_temp = null;

	OprEvent event = null;
	String externalRefId = null;
	String causeExternalRefId = null;
	OprIntegerPropertyChange duplicateChange = null;

	public AstelitRulesSection(OprEvent event, String externalRefId,
			String causeExternalRefId, OprIntegerPropertyChange duplicateChange) {
		this.event = event;
		this.externalRefId = externalRefId;
		this.causeExternalRefId = causeExternalRefId;
		this.duplicateChange = duplicateChange;
	}

	/**
	 * 
	 */
	/**
	 * 
	 */
	public void process(){
		// check if this was called by forwardEvent() to create a new SM Incident
	
		
		if (externalRefId != null) {isNewIncident = true; }
			
		default_flag = true;
		
		
		
	//##################################### ASTELIT RULES SECTION #####################################
		if (isNewIncident) {

			relatedCi_temp = event.getRelatedCi();
			astl_related_ci = relatedCi_temp.getConfigurationItem().getCiName();
			astl_ci_os_name = astl_related_ci + " OS";
		
//		//## Rule 1:
//		//## RFC C21126: "OVO Agent is using too many system resources" events ##
//			if (event.category == "Performance" && event.application == "HP OSSPI" && event.object == "CPU_ovagent") {
//				
//				astl_assignment_group = "SN-IO-ITM";
//				
//				default_flag = false;
//			}
//		//############################ END Rule 1 ######################################
//
//		//## Rule 2:
//		//########### Policy "ASTL-Billing-Disk-Usage" (C18549) #################
//			if (event.category == "billing_admin_team" && (MapOPR2SMUrgency[event.severity] == "1" || MapOPR2SMUrgency[event.severity] == "2")) {
//
//				astl_logical_name = event.application;
//				astl_operational_device = true;
//				
//				if (MapOPR2SMUrgency[event.severity] == "1") {
//					astl_priority = "2";
//				}
//				
//				if (MapOPR2SMUrgency[event.severity] == "2") {
//					astl_priority = "3";
//				}
//				
//				default_flag = false;
//			}
//		//############################ END Rule 2 ######################################
//
//		//## Rule 3:
//		//######################### SAP Events ##################################
//			if (event.category == "SAP" && event.application == "SAP" && event.object == "R3AbapShortdumps") {
//			
//				astl_logical_name = "sapUKP";
//				astl_operational_device = true;
//				astl_priority = "4";
//				
//				default_flag = false;
//			}
//		//############################ END Rule 3 ######################################
//
//		//## Rule 4:
//		//######################### TNS Events ##################################
//			if (event.category == "TADIG" && event.object == "ths_datanet_file_transfer_check.sh" && (MapOPR2SMUrgency[event.severity] == "3")) {
//			
//				astl_logical_name = " ";
//				astl_assignment_group = "SN-AO-CSP-SSR";
//				astl_title = "THS-NRTRDE file transfer delay";
//				astl_operational_device = true;
//				astl_priority = "4";
//				
//				default_flag = false;
//			}
//		//############################ END Rule 4 ######################################
//
//		//## Rule 5:
//		//######################## ABF Events ###################################
//			if (event.category == "ORGA" && event.application == "ABF" && (MapOPR2SMUrgency[event.severity] == "2" || MapOPR2SMUrgency[event.severity] == "3")) {
//				
//				astl_assignment_group = "SN-AO-SCC";
//				astl_logical_name = "ABF application";
//				astl_category = "Service Platforms";
//				astl_sub_category = "ABF";
//				
//				astl_priority = "4";
//				
//				default_flag = false;
//			}
//		//############################ END Rule 5 ######################################
//
//		//## Rule 6:
//		//####################### SL3000 Events #################################
//			if (event.application == "Tape Library" && event.object == "sl3000") {
//			
//				astl_logical_name = "SL3K";
//				astl_title = "SL3K Drive not unloaded for fetch - on rewindUnload";
//				astl_description = "SL3K Drive not unloaded for fetch - on rewindUnload";
//				astl_category = "Infrastructure";
//				astl_sub_category = "Backups - Hardware";
//				astl_operational_device = true;
//				astl_priority = "4";
//			
//				default_flag = false;
//			}
//		//############################ END Rule 6 ######################################
//
//		//## Rule 7:
//		//######################## EVA Events ###################################
//
//			if (astl_related_ci =~ /(?i)EVA/) {
//				astl_category = "Infrastructure";
//				astl_sub_category = "Storage";
//				
//				default_flag = false;
//			}
//		//############################ END Rule 7 ######################################
//
//		//## Rule 8:
//		//##################### AIS Reboot Events ###############################
//			if (event.category == "Monitor" && event.application == "MonitorLoger" && (MapOPR2SMUrgency[event.severity] == "3")) {
//				astl_logical_name = event.object;
//				astl_title = "Host ${event.object} was rebooted";
//				astl_priority = "3";
//				
//				default_flag = false;
//			}
//		//############################ END Rule 8 ######################################
//
//		//## Rule 9:
//		//##################### Performance Events ##############################
//			if (event.category == "Performance" || event.object == "Connection_check") {
//				astl_logical_name = astl_ci_os_name;
//				astl_operational_device = true;
//				
//				default_flag = false;
//			}
//		//############################ END Rule 9 ######################################
//
//		//## Rule 10:
//		//##################### Temperature Events ##############################
//			if (event.application == "Temp mon") {
//				if (event.title =~ /Temperature was changed/) {
//					astl_priority = "3";
//				}
//
//				if (event.title =~ /is more then max threshold/) {
//					astl_priority = "2";
//				}
//
//				if (event.title =~ /is lower then min threshold/) {
//					astl_priority = "2";
//				}
//				
//				astl_logical_name = event.object;
//				astl_operational_device = true;
//				
//				default_flag = false;
//			}
//		//############################ END Rule 10 ######################################
//
//		//## Rule 11:
//		//###################### SAN Disk Events ################################
//			if (event.category == "Hardware" && event.application == "SANdisk" && (MapOPR2SMUrgency[event.severity] == "2")) {
//				
//				astl_assignment_group = "SN-IO-SSDA-SA";
//				astl_operational_device = true;
//				astl_priority = "1";
//				
//				default_flag = false;
//			}
//		//############################ END Rule 11 ######################################
//
//		//## Rule 12:
//		//####################### HP SIM Events #################################
//			if (event.category == "HP_SIM" && event.application == "HP_SIM") {
//
//				astl_logical_name = event.object;
//				astl_sub_category = "HP SIM";
//
//				//# HP SIM events with opened CASE in the HP (C20191)
//				if (event.title =~ /SEA Version:System Event Analyzer for Windows/){
//					astl_operational_device = true;
//				}
//				//# Configuring Auto Incidents from Serviceguad cluster (C20026)
//				if (event.title =~ /hpmcSG/) {
//					astl_logical_name = astl_ci_os_name;
//				}
//				myMatcher = (event.title =~ /(NO_SERVER_CI_OUTAGE_FLAG)(.*)/);
//				if (myMatcher.matches()) {
//					astl_operational_device = true;
//					astl_description = myMatcher[0][2];
//				}
//				
//				if (event.title =~ /Incomplete OA XML Data/) {
//					astl_priority = "4";
//				}
//					
//				if (event.title =~ /(\(SNMP\) Process Monitor Event Trap \(11011\)|HP ProLiant-HP Power-Power Supply Failed|cpqHe4FltTolPowerSupplyDegraded|cpqHe4FltTolPowerSupplyFailed|\(WBEM\) Power redundancy reduced|\(WBEM\) Power redundancy lost|\(WBEM\) Power Supply Failed|\(SNMP\)  Power Supply Failed \(6050\)|\(SNMP\)  Power Redundancy Lost \(6032\))/)
//					 { astl_operational_device = true;}
//				
//				//# For WMI Events. If string Brief Description is in Message text
//				myMatcher = (event.title =~ /Brief Description:\n\s(.*)/)
//				if (myMatcher.matches()){
//					astl_title = myMatcher[0][1];
//				}
//				//# For SNMP Traps. If string Event Name is in Message text
//				myMatcher = (event.title =~ /Event Name:\s(.*)/);
//				if (myMatcher.matches()){
//					astl_title = myMatcher[0][1];
//				}
//				//# For WMI Events. If string Summary is in Message text
//				myMatcher = (event.title =~ /Summary:\s(.*)/);
//				if (myMatcher.matches()){
//					astl_title = myMatcher[0][1];
//				}
//				//# For WMI Events. If string Caption is in Message text
//				myMatcher = (event.title =~ /Caption:\s(.*)/);
//				if (myMatcher.matches()) {
//					astl_title = myMatcher[0][1];
//				}
//				if (event.title =~ /(Severe latency bottleneck | is a congestion bottleneck)/) {
//					astl_assignment_group = "SN-IO-SSDA-SB";
//					astl_category = "Infrastructure";
//					astl_sub_category = "SAN Switch";
//					astl_operational_device = true;
//					
//					myMatcher = (event.title =~ /.*(.*AN-.*Slot.*, port.*is a congestion bottleneck)/);
//					if (myMatcher.matches()) {
//						astl_title = "${astl_object} ${myMatcher[0][1]}";
//						
//						myMatcher = (event.title =~ /.*(.*AN-.*Slot.*, port.*is a congestion bottleneck.*percent of last.*seconds were affected by this condition.)/);
//						if (myMatcher.matches())
//							astl_description = "${astl_object} ${myMatcher[0][1]}";
//					}
//								
//					myMatcher = (event.title =~ /.*(AN-.*Severe latency bottleneck detected at Slot.*port.*)/);
//					if (myMatcher.matches()) {
//						astl_title = "${astl_object} ${myMatcher[0][1]}";
//						astl_description = "${astl_object} ${myMatcher[0][1]}";
//					}
//				}
//				default_flag = false;
//			}
//		//############################ END Rule 12 ######################################

		//## Rule 13:
		//### Auto Incidents for XP arrays (C17089) and AMS Storages (C19906) ###
			if (astl_related_ci =~ /phecda/ && event.category == "OS" && event.application == "Application" && event.object == "Event Log") {
				
				astl_assignment_group = "SN-IO-SSDA-SB";
				astl_category = "Infrastructure";
				astl_sub_category = "Storage";
				astl_priority = "3";
				astl_operational_device = true;
				
				myMatcher = (event.title =~ /.*SOURCE.*"(.*XP.*)".*STATUS.*COMPONENT.*"(.*)".*DESCRIPTION.*".*error.*"/);
				if (myMatcher.matches()) {
					astl_logical_name = myMatcher[0][1];
					astl_title = "${myMatcher[0][1]}: ${myMatcher[0][2]}";
				}
							
				myMatcher = (event.title =~ /.*SOURCE.*"(.*AMS.*)".*STATUS.*COMPONENT.*"(Disk Drive.*)".*DESCRIPTION.*/)
				if (myMatcher.matches()) {
					astl_logical_name = myMatcher[0][1];
					astl_title = myMatcher[0][1]+" "+myMatcher[0][2]+" fail.";
				}
				
				default_flag = false;
			}
		//############################ END Rule 13 ######################################

		//## Rule 14:
		//################# Policies "astl-win-procmon*" ########################
			if (event.category == "win-procmon" && event.application == "OS") {

				astl_priority = "2";
				
				if (event.object == "startManagedWebLogic.cmd"){
					astl_priority = "4";
				}
				default_flag = false;
			}
		//############################ END Rule 14 ######################################
			
		//## Rule 15
		//#################### Policies "ASTL-Procmon" (C20690, C20691) ##########################
			if (event.category == "ELF-USSD" || event.category == "ELF-SMS") {
				if (MapOPR2SMUrgency[event.severity] == "2"){
					astl_priority = "2";
				}
				default_flag = false;
			}
		//############################ END Rule 15 ######################################

		//## Rule 16
		//################ Agent Health Status Events ###########################
			if (event.category == "Agent_Healthcheck" && event.object == "opcmsg") {
				astl_assignment_group = "SN-AO-SCC";
				
				default_flag = false;
			}
		//############################ END Rule 16 ######################################

		//## Rule 17
		//################## ASTL-NG-BAS-Log-preparsed ##########################
			if (event.category == "Gold BAS Logs" && event.application == "Gold NG-BAS" && MapOPR2SMUrgency[event.severity] == "2") {
				astl_assignment_group = "SN-AO-CSP-BA";
				astl_logical_name = "OPSC Gold BAS";
				astl_priority = "3";
				
				default_flag = false;
			}
		//############################ END Rule 17 ######################################

		//## Rule 18
		//################## ASTL-TGW-Log-preparsed #############################
			if (event.category == "TGW" && event.application == "TGW") {
				astl_logical_name = " ";
				astl_assignment_group = "SN-AO-SCC";
				astl_priority = "4";
				
				if (event.title =~ /RTE interaction fails and delivers no result in operation \[Interaction \[RteModifyInteraction\] failed/) {
					myMatcher = (event.title =~ /.*ERROR.*\[\[.*?,.*?,(.*?),.*/);
					if (myMatcher.matches()){
						astl_title = "Troubles with reload ${myMatcher[0][1]}";
						}
				}
				
				if (event.title =~ /\[ReloadBalances\] The line attribute \[RMF\] has an invalid value \[null\]/) {
					myMatcher = (event.title =~ /.*ERROR \[.*\[\[\w+\,\s\*+\,\s(\d+)/);
					if (myMatcher.matches()){
						astl_title = "${myMatcher[0][1]} attribute [RMF] has an invalid value";
					}
				}
			
				default_flag = false;
			}
		//############################ END Rule 18 ######################################

		//## Rule 19
		//######################## OVSC Events ##################################
			if (event.category == "OVSC" && event.application == "OVSC" && event.object == "IncorreDB") {
				astl_assignment_group = "SN-AO-CSP-BA";
				astl_logical_name = "OVSC";

				default_flag = false;
			}
		//############################ END Rule 19 ######################################

		//## Rule 20
		//############ Policy "ASTL-IncoreDB-Usage" (C18273, C21385) #############
			if (event.category == "billing_admin_team" && event.object == "IncoreDB" && (MapOPR2SMUrgency[event.severity] == "1" || MapOPR2SMUrgency[event.severity] == "2")) {
				
				if (event.application == "MRTE"){
					astl_logical_name = "OPSC Gold MRTE";
				}
				if (event.application == "OVSC" || event.application == "LookUp"){
					astl_logical_name = "OVSC";
				}	
				if (MapOPR2SMUrgency[event.severity] == "1"){
					astl_priority = "2";
				}
				if (MapOPR2SMUrgency[event.severity] == "2"){
					astl_priority = "3";
				}	
				astl_assignment_group = "SN-AO-CSP-BA";
				
				default_flag = false;
			}
		//############################ END Rule 20 ######################################

		//## Rule 21
		//######################## wIQ Events ###################################
			if (event.category == "wIQ" && event.application == "wIQ" && MapOPR2SMUrgency[event.severity] == "1") {
				astl_assignment_group = "SN-AO-CSP-BA";
				astl_priority = "4";

				default_flag = false;
			}
		//############################ END Rule 21 ######################################
			
		//## Rule 22
		//######################## NTP Events ###################################
			if (event.category == "Time" && event.application == "NTP" && event.object == "Time") {
				astl_logical_name = astl_ci_os_name;
				astl_operational_device = true;

				default_flag = false;
			}
		//############################ END Rule 22 ######################################

		//## Rule 23
		//###################### se9985.sdab.sn #################################
			if (astl_related_ci =~ /se9985/) {
				astl_assignment_group = "SN-IO-SSDA-SB";
				astl_priority = "4";
				
				default_flag = false;
			}
		//############################ END Rule 23 ######################################

		//## Rule 24
		//####################### SCOM Events ###################################
			if (event.category == "SCOM") {
				Pattern pName = Pattern.compile("Name=(.*)");
				Pattern pDescription = Pattern.compile("Description=(.*)Name=", Pattern.DOTALL);
				
				Matcher mName = pName.matcher(event.title);
				Matcher mDescription = pDescription.matcher(event.title);
				
				if (mName.find()) {
					astl_title = mName[0][1];
				}
						
				if (mDescription.find()) {
					astl_description = mDescription[0][1].replace("\\u001a", '');
				}
					
				if (MapOPR2SMUrgency[event.severity] == "2"){
					astl_priority = "2";
				}
				if (MapOPR2SMUrgency[event.severity] == "3"){
					astl_priority = "3";
				}
				if (MapOPR2SMUrgency[event.severity] == "4"){
					astl_priority = "4";
				}
				default_flag = false;
			}
		//############################ END Rule 24 ######################################

		//## Rule 25
		//################## HP Data Protector Events ###########################
			if (event.category == "DP Session Reports") {
				
				 Pattern pTitle = Pattern.compile("(.*)");
				 Matcher mTitle = pTitle.matcher(event.title);
				  astl_title = mTitle[0][1];
				  
				  astl_logical_name = "HP Data Protector Cell Manager " + astl_related_ci;
				  
				//#set DP_ACTION [exec /opt/OV/scauto/dp/dp_action $OPCDATA_MSGTEXT]
				//eventObject set_evfield action $OPCDATA_MSGTEXT

				//### Passing assignment group ###
				astl_assignment_group = "SN-IO-SSDA-SB";
				astl_category = "Infrastructure";
				astl_sub_category = "Backups - Software";
				astl_operational_device = true;
				astl_priority = "4";
				
				default_flag = false;
			}

			if (event.category == "Data Protector" && event.object == "Sheduler") {
				
				astl_logical_name = "HP Data Protector Cell Manager " + astl_related_ci;

				astl_assignment_group = "SN-IO-SSDA-SB";
				astl_category = "Infrastructure";
				astl_sub_category = "Backups - Software";
				astl_operational_device = true;
				astl_priority = "3";

				default_flag = false;
			}
		//############################ END Rule 25 ######################################

		//## Rule 26
		//############ Oracle Enterprise Manager Events #########################
			if (event.category == "OracleEnterpriseManager") {
				myMatcher = (event.title =~ /Message:\s(.*)/)
				if (myMatcher.matches()){
					astl_title = myMatcher[0][1]
				}
			if (event.application == "Database Instance" || event.application == "Agent" || event.application == "Listener" || event.application == "OMS and Repository" || event.application == "Oracle High Availability Service"){
				astl_logical_name = event.object + " Instance"
			}
			if (event.application == "Cluster" || event.application == "Cluster Database"){
				astl_logical_name = event.object + " DB Cluster";
			}
				//astl_category = "Databases"
				astl_assignment_group = "SN-IO-SSDA-DA";
				astl_priority = "2";
						
				default_flag = false;
			}
		//############################ END Rule 26 ######################################

		//## Rule 26
		//################### SN-ISM Security Events ############################
			if (event.category == "SN-ISM" && event.application == "Security" && event.object == "Security") {
				astl_assignment_group = "SN-ISM";
				astl_category = "Security";
				astl_sub_category = "Security Systems Availability";
			
				astl_operational_device = true;

				default_flag = false;
			}
			
			if (event.category == "Security" && event.application == "S-TAP agent") {
				astl_assignment_group = "SN-ISM";
				astl_category = "Security";
				astl_sub_category = "Security Systems Availability";

				astl_operational_device = true;

				default_flag = false;
			}
			
			if (event.application == "ASTL_Node_Pinger" && event.object == "Connection_check") {
				astl_assignment_group = "SN-ISM";
				astl_category = "Security";
				astl_sub_category = "Security Systems Availability";
				astl_priority = "2";

				default_flag = false;
			}
		//############################ END Rule 27 ######################################

		//## Rule 28
		//######################## Agent Errors #################################
			if (event.category == "OpC" && (event.application == "HP OpenView Operations" || event.application == "OM Agent") && (MapOPR2SMUrgency[event.severity] == "1" || MapOPR2SMUrgency[event.severity] == "2")) {
				astl_logical_name = astl_ci_os_name;
				astl_assignment_group = "SN-AO-SCC";
				astl_priority = "3";

				default_flag = false;
			}
		//############################ END Rule 28 ######################################

		//## Rule 29
		//################# MAXIMO Services Monitoring ##########################
			if (event.application == "Service Policy") {
				
				astl_assignment_group = "MN-OS-MSI";
				
				if (astl_related_ci =~ /maximodb2/ || astl_related_ci =~ /maximosb/){
					astl_priority = "3";
				}
				if (astl_related_ci =~ /maximo01/){
					astl_priority = "4"
				}
				default_flag = false;
			}

			if (event.application == "MaximoPing") {
				astl_assignment_group = "MN-OS-MSI";
				astl_priority = "4";

				default_flag = false;
			}
		//############################ END Rule 29 ######################################

		//## Rule 30
		//#################### Gold MRTE Event ##################################
			if (event.category == "evn_astelit" && event.application == "Gold MRTE" && (MapOPR2SMUrgency[event.severity] == "1" || MapOPR2SMUrgency[event.severity] == "2")) {

				astl_assignment_group = "SN-AO-SCC";
				astl_priority = "3";
				
				if (event.object == "event-files"){
					astl_logical_name = " ";
				}
				default_flag = false;
			}

			if (event.category == "MRTE" && MapOPR2SMUrgency[event.severity] == "1") {
				
				astl_assignment_group = "SN-AO-CSP-BA";
				astl_priority = "1";
				
				if (event.application == "OPSC reg_scp rte1"){
					astl_logical_name = "OPSC reg_scp rte1";
				}
				if (event.application == "OPSC reg_scp rte2"){
					astl_logical_name = "OPSC reg_scp rte2";
				}
				default_flag = false;
			}
		//############################ END Rule 30 ######################################


		//## Rule 31
		//########### Policy "ASTL-MRTE-IncoreDB-usage" (C16928) ################
			if (event.category == "MRTE" && (event.application == "mrte1a" || event.application == "mrte2a") && MapOPR2SMUrgency[event.severity] == "1") {
				if (event.application == "mrte1a"){
					astl_logical_name = "MRTE1-a";
				}
				if (event.application == "mrte2a"){
					astl_logical_name = "MRTE2-a";
				}
				astl_assignment_group = "SN-AO-CSP-BA";
				astl_priority = "1";
				
				default_flag = false;
			}
		//############################ END Rule 31 ######################################

		//## Rule 32
		//########### Policy "astl_win_rcu_monitoring_rcuX" (C17860) ############
			if (event.category == "Hardware" && event.application == "RCU" && MapOPR2SMUrgency[event.severity] == "3") {
				astl_logical_name = astl_related_ci + " software";
				astl_priority = "4";
				
				default_flag = false;
			}
		//############################ END Rule 32 ######################################

		//## Rule 33
		//############## Policy "bcp_monitoring_usage" (C18277) #################
			if (event.category == "BCP_mon" && event.application == "BCP Backup" && MapOPR2SMUrgency[event.severity] == "3") {
				astl_logical_name = event.object;
				astl_operational_device = true;
				astl_priority = "3";
				
				default_flag = false;
			}
		//############################ END Rule 33 ######################################

		//## Rule 34
			Rule43 r34 = new Rule34();
			r34.go();
//		//########### C20607: NNMi Management Events: SNMP_Interceptor ##########
//			if (event.category == "SNMP" && event.application == "NNMi" && MapOPR2SMUrgency[event.severity] == "1" && event.title == "Node Down") {
//				
//				myMatcher = (event.object =~ /(\w+\sOS):(.*)/);
//				if (myMatcher.matches()) {
//					astl_logical_name = myMatcher[0][1];
//					astl_assignment_group = myMatcher[0][2];
//					astl_category = "Security";
//					astl_sub_category = "Security Systems Availability";
//					astl_title = event.application + ":" + event.title + ":" + astl_related_ci;
//					astl_operational_device = true;
//					astl_priority = "4";
//				}
//					
//				default_flag = false;
//			}
		//############################ END Rule 34 ######################################

			
			
			
			
		//## Rule 35
			Rule35 r35 = new Rule35();
			r35.go();
//		//##################### Performance Events ##############################
//			if (event.category == "Performance" && event.application == "HP OSSPI" && event.object == "CPU_Wait_Util") {
//				
//				astl_priority = "3";
//				astl_operational_device = true;
//				
//				default_flag = false;
//			}
//		//############################ END Rule 35 ######################################
		}
	//##################################### END ASTELIT RULES SECTION ##################################
	}

	/**
	 * @author maskimko This class corresponds to NNMi Management Events It
	 *         handles SNMP_Interceptor events
	 */
	class Rule34 {

		void go() {
			//FIXME fix object MapOPR2SMUrgency
			//FIXME get familiar with event.severity
			if (event.getCategory() == "SNMP"
					&& event.getApplication() == "NNMi"
					&& MapOPR2SMUrgency[event.severity] == "1"
					&& event.getTitle() == "Node Down") {
				Pattern pattern = Pattern
						.compile("([a-zA-Z_0-9]+[ \t]OS):(.*)");
				Matcher matcher = pattern.matcher(event.getObject());
				if (matcher.matches()) {
					astl_logical_name = matcher.group(1);
					astl_assignment_group = matcher.group(2);
					astl_category = "Security";
					astl_sub_category = "Security Systems Availability";
					astl_title = event.getApplication() + ":"
							+ event.getTitle() + ":" + astl_related_ci;
					astl_operational_device = true;
					astl_priority = "4";
				}

				default_flag = false;
			}
		}
	}

	/**
	 * @author maskimko This class corresponds to Performance events
	 */
	class Rule35 {
		void go() {
			if (event.getCategory() == "Performance"
					&& event.getApplication() == "HP OSSPI"
					&& event.getObject() == "CPU_Wait_Util") {

				astl_priority = "3";
				astl_operational_device = true;

				default_flag = false;
			}
		}
	}
}
