package test;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.opr.api.ws.model.event.OprEvent;
import com.hp.opr.api.ws.model.event.ci.OprRelatedCi;
import com.hp.opr.api.ws.model.event.property.OprIntegerPropertyChange;

public class AstelitRulesSection {
	
	
	private transient Log log = LogFactory.getLog(this.getClass());
	private transient Log m_log = null;

	public boolean isNewIncident = false;
	public boolean default_flag = false;
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
	public OprRelatedCi relatedCi_temp = null;


	// Not compatible to groovy mapping
	// private static final EnumMap<OprSeverity, Integer> MapOPR2SMUrgency = new
	// EnumMap<OprSeverity, Integer>(OprSeverity.class);
	private Map<String, String> MapOPR2SMUrgency = null;
	OprEvent event = null;
	String externalRefId = null;
	String causeExternalRefId = null;
	OprIntegerPropertyChange duplicateChange = null;
	
	private static final String logDelimiter = "#=========ASTELIT RULES SECTION================#";
	

	public AstelitRulesSection(Map<String, String> MapOPR2SMUrgency,
			OprEvent event, String externalRefId, String causeExternalRefId,
			OprIntegerPropertyChange duplicateChange,
			Boolean isNewIncident,
			Boolean default_flag, String astl_assignment_group,
			String astl_logical_name, String astl_priority,
			String astl_urgency, String astl_title, String astl_description,
			String astl_category, String astl_sub_category,
			Boolean astl_operational_device, Log m_log) {
		this.event = event;
		this.externalRefId = externalRefId;
		this.causeExternalRefId = causeExternalRefId;
		this.duplicateChange = duplicateChange;
		// Can be truth....
		// MapOPR2SMUrgency.put(OprSeverity.critical, 1);
		// MapOPR2SMUrgency.put(OprSeverity.major, 2);
		// MapOPR2SMUrgency.put(OprSeverity.minor, 3);
		// MapOPR2SMUrgency.put(OprSeverity.warning, 3);
		// MapOPR2SMUrgency.put(OprSeverity.normal, 4);
		// MapOPR2SMUrgency.put(OprSeverity.unknown, 4);
		this.MapOPR2SMUrgency = MapOPR2SMUrgency;
		this.isNewIncident = isNewIncident;
		this.default_flag = default_flag;


		this.astl_assignment_group = astl_assignment_group;
		this.astl_logical_name = astl_logical_name;
		this.astl_priority = astl_priority;
		this.astl_urgency = astl_urgency;
		this.astl_title = astl_title;
		this.astl_description = astl_description;
		this.astl_category = astl_category;
		this.astl_sub_category = astl_sub_category;
		this.astl_operational_device = astl_operational_device;
		this.m_log = m_log;
		
		if (log.isDebugEnabled()) {
		log.debug(logDelimiter);
		log.debug(this.event);
		}
		
		m_log.info("m_log has been passed to AstelitRulesSection");

	}

	
	public void process() {
		// check if this was called by forwardEvent() to create a new SM
		// Incident

		if (externalRefId != null) {
			isNewIncident = true;
		}

		default_flag = true;

		// ##################################### ASTELIT RULES SECTION
		// #####################################
		if (isNewIncident) {

			relatedCi_temp = event.getRelatedCi();
			astl_related_ci = relatedCi_temp.getConfigurationItem().getCiName();
			astl_ci_os_name = astl_related_ci + " OS";

			// //## Rule 1:
			Rule1 r1 = new Rule1();
			r1.go();
			// //## RFC C21126: "OVO Agent is using too many system resources"
			// events ##
			// if (event.category == "Performance" && event.application ==
			// "HP OSSPI" && event.object == "CPU_ovagent") {
			//
			// astl_assignment_group = "SN-IO-ITM";
			//
			// default_flag = false;
			// }
			// //############################ END Rule 1
			// ######################################
			//
			// //## Rule 2:
			Rule2 r2 = new Rule2();
			r2.go();
			// //########### Policy "ASTL-Billing-Disk-Usage" (C18549)
			// #################
			// if (event.category == "billing_admin_team" &&
			// (MapOPR2SMUrgency[event.severity] == "1" ||
			// MapOPR2SMUrgency[event.severity] == "2")) {
			//
			// astl_logical_name = event.application;
			// astl_operational_device = true;
			//
			// if (MapOPR2SMUrgency[event.severity] == "1") {
			// astl_priority = "2";
			// }
			//
			// if (MapOPR2SMUrgency[event.severity] == "2") {
			// astl_priority = "3";
			// }
			//
			// default_flag = false;
			// }
			// //############################ END Rule 2
			// ######################################
			//
			// //## Rule 3:
			Rule3 r3 = new Rule3();
			r3.go();
			// //######################### SAP Events
			// ##################################
			// if (event.category == "SAP" && event.application == "SAP" &&
			// event.object == "R3AbapShortdumps") {
			//
			// astl_logical_name = "sapUKP";
			// astl_operational_device = true;
			// astl_priority = "4";
			//
			// default_flag = false;
			// }
			// //############################ END Rule 3
			// ######################################
			//
			// //## Rule 4:
			Rule4 r4 = new Rule4();
			r4.go();
			// //######################### TNS Events
			// ##################################
			// if (event.category == "TADIG" && event.object ==
			// "ths_datanet_file_transfer_check.sh" &&
			// (MapOPR2SMUrgency[event.severity] == "3")) {
			//
			// astl_logical_name = " ";
			// astl_assignment_group = "SN-AO-CSP-SSR";
			// astl_title = "THS-NRTRDE file transfer delay";
			// astl_operational_device = true;
			// astl_priority = "4";
			//
			// default_flag = false;
			// }
			// //############################ END Rule 4
			// ######################################
			//
			// //## Rule 5:
			Rule5 r5 = new Rule5();
			r5.go();
			// //######################## ABF Events
			// ###################################
			// if (event.category == "ORGA" && event.application == "ABF" &&
			// (MapOPR2SMUrgency[event.severity] == "2" ||
			// MapOPR2SMUrgency[event.severity] == "3")) {
			//
			// astl_assignment_group = "SN-AO-SCC";
			// astl_logical_name = "ABF application";
			// astl_category = "Service Platforms";
			// astl_sub_category = "ABF";
			//
			// astl_priority = "4";
			//
			// default_flag = false;
			// }
			// //############################ END Rule 5
			// ######################################
			//
			// //## Rule 6:
			Rule6 r6 = new Rule6();
			r6.go();
			// //####################### SL3000 Events
			// #################################
			// if (event.application == "Tape Library" && event.object ==
			// "sl3000") {
			//
			// astl_logical_name = "SL3K";
			// astl_title =
			// "SL3K Drive not unloaded for fetch - on rewindUnload";
			// astl_description =
			// "SL3K Drive not unloaded for fetch - on rewindUnload";
			// astl_category = "Infrastructure";
			// astl_sub_category = "Backups - Hardware";
			// astl_operational_device = true;
			// astl_priority = "4";
			//
			// default_flag = false;
			// }
			// //############################ END Rule 6
			// ######################################
			//
			// //## Rule 7:
			Rule7 r7 = new Rule7();
			r7.go();
			// //######################## EVA Events
			// ###################################
			//
			// if (astl_related_ci =~ /(?i)EVA/) {
			// astl_category = "Infrastructure";
			// astl_sub_category = "Storage";
			//
			// default_flag = false;
			// }
			// //############################ END Rule 7
			// ######################################
			//
			// //## Rule 8:
			Rule8 r8 = new Rule8();
			r8.go();
			// //##################### AIS Reboot Events
			// ###############################
			// if (event.category == "Monitor" && event.application ==
			// "MonitorLoger" && (MapOPR2SMUrgency[event.severity] == "3")) {
			// astl_logical_name = event.object;
			// astl_title = "Host ${event.object} was rebooted";
			// astl_priority = "3";
			//
			// default_flag = false;
			// }
			// //############################ END Rule 8
			// ######################################
			//
			// //## Rule 9:
			Rule9 r9 = new Rule9();
			r9.go();
			// //##################### Performance Events
			// ##############################
			// if (event.category == "Performance" || event.object ==
			// "Connection_check") {
			// astl_logical_name = astl_ci_os_name;
			// astl_operational_device = true;
			//
			// default_flag = false;
			// }
			// //############################ END Rule 9
			// ######################################
			//
			// //## Rule 10:
			Rule10 r10 = new Rule10();
			r10.go();
			// //##################### Temperature Events
			// ##############################
			// if (event.application == "Temp mon") {
			// if (event.title =~ /Temperature was changed/) {
			// astl_priority = "3";
			// }
			//
			// if (event.title =~ /is more then max threshold/) {
			// astl_priority = "2";
			// }
			//
			// if (event.title =~ /is lower then min threshold/) {
			// astl_priority = "2";
			// }
			//
			// astl_logical_name = event.object;
			// astl_operational_device = true;
			//
			// default_flag = false;
			// }
			// //############################ END Rule 10
			// ######################################
			//
			// //## Rule 11:
			Rule11 r11 = new Rule11();
			r11.go();
			// //###################### SAN Disk Events
			// ################################
			// if (event.category == "Hardware" && event.application ==
			// "SANdisk" && (MapOPR2SMUrgency[event.severity] == "2")) {
			//
			// astl_assignment_group = "SN-IO-SSDA-SA";
			// astl_operational_device = true;
			// astl_priority = "1";
			//
			// default_flag = false;
			// }
			// //############################ END Rule 11
			// ######################################
			//
			// //## Rule 12:
			Rule12 r12 = new Rule12();
			r12.go();
			// //####################### HP SIM Events
			// #################################
			// if (event.category == "HP_SIM" && event.application == "HP_SIM")
			// {
			//
			// astl_logical_name = event.object;
			// astl_sub_category = "HP SIM";
			//
			// //# HP SIM events with opened CASE in the HP (C20191)
			// if (event.title =~ /SEA Version:System Event Analyzer for
			// Windows/){
			// astl_operational_device = true;
			// }
			// //# Configuring Auto Incidents from Serviceguad cluster (C20026)
			// if (event.title =~ /hpmcSG/) {
			// astl_logical_name = astl_ci_os_name;
			// }
			// myMatcher = (event.title =~ /(NO_SERVER_CI_OUTAGE_FLAG)(.*)/);
			// if (myMatcher.matches()) {
			// astl_operational_device = true;
			// astl_description = myMatcher[0][2];
			// }
			//
			// if (event.title =~ /Incomplete OA XML Data/) {
			// astl_priority = "4";
			// }
			//
			// if (event.title =~ /(\(SNMP\) Process Monitor Event Trap
			// \(11011\)|HP ProLiant-HP Power-Power Supply
			// Failed|cpqHe4FltTolPowerSupplyDegraded|cpqHe4FltTolPowerSupplyFailed|\(WBEM\)
			// Power redundancy reduced|\(WBEM\) Power redundancy lost|\(WBEM\)
			// Power Supply Failed|\(SNMP\) Power Supply Failed
			// \(6050\)|\(SNMP\) Power Redundancy Lost \(6032\))/)
			// { astl_operational_device = true;}
			//
			// //# For WMI Events. If string Brief Description is in Message
			// text
			// myMatcher = (event.title =~ /Brief Description:\n\s(.*)/)
			// if (myMatcher.matches()){
			// astl_title = myMatcher[0][1];
			// }
			// //# For SNMP Traps. If string Event Name is in Message text
			// myMatcher = (event.title =~ /Event Name:\s(.*)/);
			// if (myMatcher.matches()){
			// astl_title = myMatcher[0][1];
			// }
			// //# For WMI Events. If string Summary is in Message text
			// myMatcher = (event.title =~ /Summary:\s(.*)/);
			// if (myMatcher.matches()){
			// astl_title = myMatcher[0][1];
			// }
			// //# For WMI Events. If string Caption is in Message text
			// myMatcher = (event.title =~ /Caption:\s(.*)/);
			// if (myMatcher.matches()) {
			// astl_title = myMatcher[0][1];
			// }
			// if (event.title =~ /(Severe latency bottleneck | is a congestion
			// bottleneck)/) {
			// astl_assignment_group = "SN-IO-SSDA-SB";
			// astl_category = "Infrastructure";
			// astl_sub_category = "SAN Switch";
			// astl_operational_device = true;
			//
			// myMatcher = (event.title =~ /.*(.*AN-.*Slot.*, port.*is a
			// congestion bottleneck)/);
			// if (myMatcher.matches()) {
			// astl_title = "${astl_object} ${myMatcher[0][1]}";
			//
			// myMatcher = (event.title =~ /.*(.*AN-.*Slot.*, port.*is a
			// congestion bottleneck.*percent of last.*seconds were affected by
			// this condition.)/);
			// if (myMatcher.matches())
			// astl_description = "${astl_object} ${myMatcher[0][1]}";
			// }
			//
			// myMatcher = (event.title =~ /.*(AN-.*Severe latency bottleneck
			// detected at Slot.*port.*)/);
			// if (myMatcher.matches()) {
			// astl_title = "${astl_object} ${myMatcher[0][1]}";
			// astl_description = "${astl_object} ${myMatcher[0][1]}";
			// }
			// }
			// default_flag = false;
			// }
			// //############################ END Rule 12
			// ######################################

			// ## Rule 13:
			Rule13 r13 = new Rule13();
			r13.go();
			// ### Auto Incidents for XP arrays (C17089) and AMS Storages
			// (C19906) ###
			// if (astl_related_ci =~ /phecda/ && event.category == "OS" &&
			// event.application == "Application" && event.object ==
			// "Event Log") {
			//
			// astl_assignment_group = "SN-IO-SSDA-SB";
			// astl_category = "Infrastructure";
			// astl_sub_category = "Storage";
			// astl_priority = "3";
			// astl_operational_device = true;
			//
			// myMatcher = (event.title =~
			// /.*SOURCE.*"(.*XP.*)".*STATUS.*COMPONENT.*"(.*)".*DESCRIPTION.*".*error.*"/);
			// if (myMatcher.matches()) {
			// astl_logical_name = myMatcher[0][1];
			// astl_title = "${myMatcher[0][1]}: ${myMatcher[0][2]}";
			// }
			//
			// myMatcher = (event.title =~
			// /.*SOURCE.*"(.*AMS.*)".*STATUS.*COMPONENT.*"(Disk Drive.*)".*DESCRIPTION.*/)
			// if (myMatcher.matches()) {
			// astl_logical_name = myMatcher[0][1];
			// astl_title = myMatcher[0][1]+" "+myMatcher[0][2]+" fail.";
			// }
			//
			// default_flag = false;
			// }
			// ############################ END Rule 13
			// ######################################

			// ## Rule 14:
			Rule14 r14 = new Rule14();
			r14.go();
			// ################# Policies "astl-win-procmon*"
			// ########################
			// if (event.category == "win-procmon" && event.application == "OS")
			// {
			//
			// astl_priority = "2";
			//
			// if (event.object == "startManagedWebLogic.cmd"){
			// astl_priority = "4";
			// }
			// default_flag = false;
			// }
			// ############################ END Rule 14
			// ######################################

			// ## Rule 15
			Rule15 r15 = new Rule15();
			r15.go();
			// #################### Policies "ASTL-Procmon" (C20690, C20691)
			// ##########################
			// if (event.category == "ELF-USSD" || event.category == "ELF-SMS")
			// {
			// if (MapOPR2SMUrgency[event.severity] == "2"){
			// astl_priority = "2";
			// }
			// default_flag = false;
			// }
			// ############################ END Rule 15
			// ######################################

			// ## Rule 16
			Rule16 r16 = new Rule16();
			r16.go();
			// ################ Agent Health Status Events
			// ###########################
			// if (event.category == "Agent_Healthcheck" && event.object ==
			// "opcmsg") {
			// astl_assignment_group = "SN-AO-SCC";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 16
			// ######################################

			// ## Rule 17
			Rule17 r17 = new Rule17();
			r17.go();
			// ################## ASTL-NG-BAS-Log-preparsed
			// ##########################
			// if (event.category == "Gold BAS Logs" && event.application ==
			// "Gold NG-BAS" && MapOPR2SMUrgency[event.severity] == "2") {
			// astl_assignment_group = "SN-AO-CSP-BA";
			// astl_logical_name = "OPSC Gold BAS";
			// astl_priority = "3";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 17
			// ######################################

			// ## Rule 18
			Rule18 r18 = new Rule18();
			r18.go();
			// ################## ASTL-TGW-Log-preparsed
			// #############################
			// if (event.category == "TGW" && event.application == "TGW") {
			// astl_logical_name = " ";
			// astl_assignment_group = "SN-AO-SCC";
			// astl_priority = "4";
			//
			// if (event.title =~ /RTE interaction fails and delivers no result
			// in operation \[Interaction \[RteModifyInteraction\] failed/) {
			// myMatcher = (event.title =~ /.*ERROR.*\[\[.*?,.*?,(.*?),.*/);
			// if (myMatcher.matches()){
			// astl_title = "Troubles with reload ${myMatcher[0][1]}";
			// }
			// }
			//
			// if (event.title =~ /\[ReloadBalances\] The line attribute \[RMF\]
			// has an invalid value \[null\]/) {
			// myMatcher = (event.title =~ /.*ERROR
			// \[.*\[\[\w+\,\s\*+\,\s(\d+)/);
			// if (myMatcher.matches()){
			// astl_title =
			// "${myMatcher[0][1]} attribute [RMF] has an invalid value";
			// }
			// }
			//
			// default_flag = false;
			// }
			// ############################ END Rule 18
			// ######################################

			// ## Rule 19
			Rule19 r19 = new Rule19();
			r19.go();
			// ######################## OVSC Events
			// ##################################
			// if (event.category == "OVSC" && event.application == "OVSC" &&
			// event.object == "IncorreDB") {
			// astl_assignment_group = "SN-AO-CSP-BA";
			// astl_logical_name = "OVSC";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 19
			// ######################################

			// ## Rule 20
			Rule20 r20 = new Rule20();
			r20.go();
			// ############ Policy "ASTL-IncoreDB-Usage" (C18273, C21385)
			// #############
			// if (event.category == "billing_admin_team" && event.object ==
			// "IncoreDB" && (MapOPR2SMUrgency[event.severity] == "1" ||
			// MapOPR2SMUrgency[event.severity] == "2")) {
			//
			// if (event.application == "MRTE"){
			// astl_logical_name = "OPSC Gold MRTE";
			// }
			// if (event.application == "OVSC" || event.application ==
			// "LookUp"){
			// astl_logical_name = "OVSC";
			// }
			// if (MapOPR2SMUrgency[event.severity] == "1"){
			// astl_priority = "2";
			// }
			// if (MapOPR2SMUrgency[event.severity] == "2"){
			// astl_priority = "3";
			// }
			// astl_assignment_group = "SN-AO-CSP-BA";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 20
			// ######################################

			// ## Rule 21
			Rule21 r21 = new Rule21();
			r21.go();
			// ######################## wIQ Events
			// ###################################
			// if (event.category == "wIQ" && event.application == "wIQ" &&
			// MapOPR2SMUrgency[event.severity] == "1") {
			// astl_assignment_group = "SN-AO-CSP-BA";
			// astl_priority = "4";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 21
			// ######################################

			// ## Rule 22
			Rule22 r22 = new Rule22();
			r22.go();
			// ######################## NTP Events
			// ###################################
			// if (event.category == "Time" && event.application == "NTP" &&
			// event.object == "Time") {
			// astl_logical_name = astl_ci_os_name;
			// astl_operational_device = true;
			//
			// default_flag = false;
			// }
			// ############################ END Rule 22
			// ######################################

			// ## Rule 23
			Rule23 r23 = new Rule23();
			r23.go();
			// ###################### se9985.sdab.sn
			// #################################
			// if (astl_related_ci =~ /se9985/) {
			// astl_assignment_group = "SN-IO-SSDA-SB";
			// astl_priority = "4";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 23
			// ######################################

			// ## Rule 24
			Rule24 r24 = new Rule24();
			r24.go();
			// ####################### SCOM Events
			// ###################################
			// if (event.category == "SCOM") {
			// Pattern pName = Pattern.compile("Name=(.*)");
			// Pattern pDescription = Pattern.compile("Description=(.*)Name=",
			// Pattern.DOTALL);
			//
			// Matcher mName = pName.matcher(event.title);
			// Matcher mDescription = pDescription.matcher(event.title);
			//
			// if (mName.find()) {
			// astl_title = mName[0][1];
			// }
			//
			// if (mDescription.find()) {
			// astl_description = mDescription[0][1].replace("\\u001a", '');
			// }
			//
			// if (MapOPR2SMUrgency[event.severity] == "2"){
			// astl_priority = "2";
			// }
			// if (MapOPR2SMUrgency[event.severity] == "3"){
			// astl_priority = "3";
			// }
			// if (MapOPR2SMUrgency[event.severity] == "4"){
			// astl_priority = "4";
			// }
			// default_flag = false;
			// }
			// ############################ END Rule 24
			// ######################################

			// ## Rule 25
			Rule25 r25 = new Rule25();
			r25.go();
			// ################## HP Data Protector Events
			// ###########################
			// if (event.category == "DP Session Reports") {
			//
			// Pattern pTitle = Pattern.compile("(.*)");
			// Matcher mTitle = pTitle.matcher(event.title);
			// astl_title = mTitle[0][1];
			//
			// astl_logical_name = "HP Data Protector Cell Manager " +
			// astl_related_ci;
			//
			// //#set DP_ACTION [exec /opt/OV/scauto/dp/dp_action
			// $OPCDATA_MSGTEXT]
			// //eventObject set_evfield action $OPCDATA_MSGTEXT
			//
			// //### Passing assignment group ###
			// astl_assignment_group = "SN-IO-SSDA-SB";
			// astl_category = "Infrastructure";
			// astl_sub_category = "Backups - Software";
			// astl_operational_device = true;
			// astl_priority = "4";
			//
			// default_flag = false;
			// }
			//
			// if (event.category == "Data Protector" && event.object ==
			// "Sheduler") {
			//
			// astl_logical_name = "HP Data Protector Cell Manager " +
			// astl_related_ci;
			//
			// astl_assignment_group = "SN-IO-SSDA-SB";
			// astl_category = "Infrastructure";
			// astl_sub_category = "Backups - Software";
			// astl_operational_device = true;
			// astl_priority = "3";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 25
			// ######################################

			// ## Rule 26
			Rule26 r26 = new Rule26();
			r26.go();
			// //############ Oracle Enterprise Manager Events
			// #########################
			// if (event.category == "OracleEnterpriseManager") {
			// myMatcher = (event.title =~ /Message:\s(.*)/)
			// if (myMatcher.matches()){
			// astl_title = myMatcher[0][1]
			// }
			// if (event.application == "Database Instance" || event.application
			// == "Agent" || event.application == "Listener" ||
			// event.application == "OMS and Repository" || event.application ==
			// "Oracle High Availability Service"){
			// astl_logical_name = event.object + " Instance"
			// }
			// if (event.application == "Cluster" || event.application ==
			// "Cluster Database"){
			// astl_logical_name = event.object + " DB Cluster";
			// }
			// //astl_category = "Databases"
			// astl_assignment_group = "SN-IO-SSDA-DA";
			// astl_priority = "2";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 26
			// ######################################

			// ## Rule 27
			Rule27 r27 = new Rule27();
			r27.go();

			// ################### SN-ISM Security Events
			// ############################
			// if (event.category == "SN-ISM" && event.application == "Security"
			// && event.object == "Security") {
			// astl_assignment_group = "SN-ISM";
			// astl_category = "Security";
			// astl_sub_category = "Security Systems Availability";
			//
			// astl_operational_device = true;
			//
			// default_flag = false;
			// }
			//
			// if (event.category == "Security" && event.application ==
			// "S-TAP agent") {
			// astl_assignment_group = "SN-ISM";
			// astl_category = "Security";
			// astl_sub_category = "Security Systems Availability";
			//
			// astl_operational_device = true;
			//
			// default_flag = false;
			// }
			//
			// if (event.application == "ASTL_Node_Pinger" && event.object ==
			// "Connection_check") {
			// astl_assignment_group = "SN-ISM";
			// astl_category = "Security";
			// astl_sub_category = "Security Systems Availability";
			// astl_priority = "2";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 27
			// ######################################

			// ## Rule 28
			Rule28 r28 = new Rule28();
			r28.go();
			// ######################## Agent Errors
			// #################################
			// if (event.category == "OpC" && (event.application ==
			// "HP OpenView Operations" || event.application == "OM Agent") &&
			// (MapOPR2SMUrgency[event.severity] == "1" ||
			// MapOPR2SMUrgency[event.severity] == "2")) {
			// astl_logical_name = astl_ci_os_name;
			// astl_assignment_group = "SN-AO-SCC";
			// astl_priority = "3";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 28
			// ######################################

			// ## Rule 29
			Rule29 r29 = new Rule29();
			r29.go();
			// ################# MAXIMO Services Monitoring
			// ##########################
			// if (event.application == "Service Policy") {
			//
			// astl_assignment_group = "MN-OS-MSI";
			//
			// if (astl_related_ci =~ /maximodb2/ || astl_related_ci =~
			// /maximosb/){
			// astl_priority = "3";
			// }
			// if (astl_related_ci =~ /maximo01/){
			// astl_priority = "4"
			// }
			// default_flag = false;
			// }
			//
			// if (event.application == "MaximoPing") {
			// astl_assignment_group = "MN-OS-MSI";
			// astl_priority = "4";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 29
			// ######################################

			// ## Rule 30
			Rule30 r30 = new Rule30();
			r30.go();
			// #################### Gold MRTE Event
			// ##################################
			// if (event.category == "evn_astelit" && event.application ==
			// "Gold MRTE" && (MapOPR2SMUrgency[event.severity] == "1" ||
			// MapOPR2SMUrgency[event.severity] == "2")) {
			//
			// astl_assignment_group = "SN-AO-SCC";
			// astl_priority = "3";
			//
			// if (event.object == "event-files"){
			// astl_logical_name = " ";
			// }
			// default_flag = false;
			// }
			//
			// if (event.category == "MRTE" && MapOPR2SMUrgency[event.severity]
			// == "1") {
			//
			// astl_assignment_group = "SN-AO-CSP-BA";
			// astl_priority = "1";
			//
			// if (event.application == "OPSC reg_scp rte1"){
			// astl_logical_name = "OPSC reg_scp rte1";
			// }
			// if (event.application == "OPSC reg_scp rte2"){
			// astl_logical_name = "OPSC reg_scp rte2";
			// }
			// default_flag = false;
			// }
			// ############################ END Rule 30
			// ######################################

			// ## Rule 31
			Rule31 r31 = new Rule31();
			r31.go();
			// ########### Policy "ASTL-MRTE-IncoreDB-usage" (C16928)
			// ################
			// if (event.category == "MRTE" && (event.application == "mrte1a" ||
			// event.application == "mrte2a") &&
			// MapOPR2SMUrgency[event.severity] == "1") {
			// if (event.application == "mrte1a"){
			// astl_logical_name = "MRTE1-a";
			// }
			// if (event.application == "mrte2a"){
			// astl_logical_name = "MRTE2-a";
			// }
			// astl_assignment_group = "SN-AO-CSP-BA";
			// astl_priority = "1";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 31
			// ######################################

			// ## Rule 32
			Rule32 r32 = new Rule32();
			r32.go();
			// ########### Policy "astl_win_rcu_monitoring_rcuX" (C17860)
			// ############
			// if (event.category == "Hardware" && event.application == "RCU" &&
			// MapOPR2SMUrgency[event.severity] == "3") {
			// astl_logical_name = astl_related_ci + " software";
			// astl_priority = "4";
			//
			// default_flag = false;
			// }
			// ############################ END Rule 32
			// ######################################

			// ## Rule 33
			Rule33 r33 = new Rule33();
			r33.go();
			// //############## Policy "bcp_monitoring_usage" (C18277)
			// #################
			// if (event.category == "BCP_mon" && event.application ==
			// "BCP Backup" && MapOPR2SMUrgency[event.severity] == "3") {
			// astl_logical_name = event.object;
			// astl_operational_device = true;
			// astl_priority = "3";
			//
			// default_flag = false;
			// }
			// //############################ END Rule 33
			// ######################################

			// ## Rule 34
			Rule34 r34 = new Rule34();
			r34.go();
			// //########### C20607: NNMi Management Events: SNMP_Interceptor
			// ##########
			// if (event.category == "SNMP" && event.application == "NNMi" &&
			// MapOPR2SMUrgency[event.severity] == "1" && event.title ==
			// "Node Down") {
			//
			// myMatcher = (event.object =~ /(\w+\sOS):(.*)/);
			// if (myMatcher.matches()) {
			// astl_logical_name = myMatcher[0][1];
			// astl_assignment_group = myMatcher[0][2];
			// astl_category = "Security";
			// astl_sub_category = "Security Systems Availability";
			// astl_title = event.application + ":" + event.title + ":" +
			// astl_related_ci;
			// astl_operational_device = true;
			// astl_priority = "4";
			// }
			//
			// default_flag = false;
			// }
			// ############################ END Rule 34
			// ######################################

			// ## Rule 35
			Rule35 r35 = new Rule35();
			r35.go();
			// //##################### Performance Events
			// ##############################
			// if (event.category == "Performance" && event.application ==
			// "HP OSSPI" && event.object == "CPU_Wait_Util") {
			//
			// astl_priority = "3";
			// astl_operational_device = true;
			//
			// default_flag = false;
			// }
			// //############################ END Rule 35
			// ######################################
		}
		// ##################################### END ASTELIT RULES SECTION
		// ##################################
	}

	class Rule1 {
		void go() {
			if (event.getCategory().equals("Performance")
					&& event.getApplication().equals("HP OSSPI")
					&& event.getObject().equals("CPU_ovagent")) {

				astl_assignment_group = "SN-IO-ITM";

				default_flag = false;
			}
		}
	}

	class Rule2 {
		void go() {
			if (event.getCategory().equals("billing_admin_team")
					&& (MapOPR2SMUrgency.get(event.getSeverity()).equals("1") || MapOPR2SMUrgency
							.get(event.getSeverity()).equals("2"))) {

				astl_logical_name = event.getApplication();
				astl_operational_device = true;

				if (MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
					astl_priority = "2";
				}

				if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
					astl_priority = "3";
				}

				default_flag = false;
			}
		}
	}

	class Rule3 {
		void go() {
			if (event.getCategory().contains("SAP")
					&& event.getApplication().equals("SAP")
					&& event.getObject().equals("R3AbapShortdumps")) {

				astl_logical_name = "sapUKP";
				astl_operational_device = true;
				astl_priority = "4";

				default_flag = false;
			}
		}
	}

	class Rule4 {
		void go() {
			if (event.getCategory().equals("TADIG")
					&& event.getObject().equals(
							"ths_datanet_file_transfer_check.sh")
					&& (MapOPR2SMUrgency.get(event.getSeverity()).equals("3"))) {

				astl_logical_name = " ";
				astl_assignment_group = "SN-AO-CSP-SSR";
				astl_title = "THS-NRTRDE file transfer delay";
				astl_operational_device = true;
				astl_priority = "4";

				default_flag = false;
			}
		}
	}

	class Rule5 {
		void go() {
			if (event.getCategory() == "ORGA"
					&& event.getApplication() == "ABF"
					&& (MapOPR2SMUrgency.get(event.getSeverity()).equals("2") || MapOPR2SMUrgency
							.get(event.getSeverity()).equals("3"))) {

				astl_assignment_group = "SN-AO-SCC";
				astl_logical_name = "ABF application";
				astl_category = "Service Platforms";
				astl_sub_category = "ABF";

				astl_priority = "4";

				default_flag = false;
			}
		}
	}

	class Rule6 {
		void go() {
			if (event.getApplication().equals("Tape Library")
					&& event.getObject().equals("sl3000")) {

				astl_logical_name = "SL3K";
				astl_title = "SL3K Drive not unloaded for fetch - on rewindUnload";
				astl_description = "SL3K Drive not unloaded for fetch - on rewindUnload";
				astl_category = "Infrastructure";
				astl_sub_category = "Backups - Hardware";
				astl_operational_device = true;
				astl_priority = "4";

				default_flag = false;
			}
		}
	}

	class Rule7 {
		Pattern r7Pattern = null;
		Matcher r7Matcher = null;

		void go() {
			r7Pattern = Pattern.compile("(?i)EVA");
			r7Matcher = r7Pattern.matcher(astl_related_ci);
			if (r7Matcher.matches()) {
				astl_category = "Infrastructure";
				astl_sub_category = "Storage";

				default_flag = false;
			}
		}
	}

	class Rule8 {
		void go() {
			if (event.getCategory() == "Monitor"
					&& event.getApplication() == "MonitorLoger"
					&& (MapOPR2SMUrgency.get(event.getSeverity()).equals("3"))) {
				astl_logical_name = event.getObject();
				astl_title = "Host ${event.object} was rebooted";
				astl_priority = "3";

				default_flag = false;
			}
		}
	}

	class Rule9 {
		void go() {
			if (event.getCategory().equals("Performance")
					|| event.getObject().equals("Connection_check")) {
				astl_logical_name = astl_ci_os_name;
				astl_operational_device = true;

				default_flag = false;
			}
		}
	}

	class Rule10 {
		Pattern r10Pattern1 = null;
		Pattern r10Pattern2 = null;
		Pattern r10Pattern3 = null;
		Matcher r10Matcher1 = null;
		Matcher r10Matcher2 = null;
		Matcher r10Matcher3 = null;

		void go() {
			if (event.getApplication().equals("Temp mon")) {
				r10Pattern1 = Pattern.compile("Temperature was changed");
				r10Matcher1 = r10Pattern1.matcher(event.getTitle());
				if (r10Matcher1.matches()) {
					astl_priority = "3";
				}
				r10Pattern2 = Pattern.compile("is more then max threshold");
				r10Matcher2 = r10Pattern2.matcher(event.getTitle());
				if (r10Matcher2.matches()) {
					astl_priority = "2";
				}
				r10Pattern3 = Pattern.compile("is lower then min threshold");
				r10Matcher3 = r10Pattern3.matcher(event.getTitle());
				if (r10Matcher3.matches()) {
					astl_priority = "2";
				}

				astl_logical_name = event.getObject();
				astl_operational_device = true;

				default_flag = false;
			}
		}
	}

	class Rule11 {
		void go() {
			if (event.getCategory().equals("Hardware")
					&& event.getApplication().equals("SANdisk")
					&& (MapOPR2SMUrgency.get(event.getSeverity()).equals("2"))) {

				astl_assignment_group = "SN-IO-SSDA-SA";
				astl_operational_device = true;
				astl_priority = "1";

				default_flag = false;
			}
		}
	}

	class Rule12 {
		Pattern caseHPc20191Pattern = null;
		Pattern serviceGuardPattern = null;
		Pattern ciOutagePattern = null;
		Pattern incompletePattern = null;
		Pattern hp1Pattern = null;
		Pattern hp2Pattern = null;
		Pattern hp3Pattern = null;
		Pattern hp4Pattern = null;
		Pattern hp5Pattern = null;
		Pattern hp6Pattern = null;
		Pattern hp7Pattern = null;
		Pattern hp8Pattern = null;
		Pattern hp9Pattern = null;
		Pattern wmiEventsPattern1 = null;
		Pattern wmiEventsPattern2 = null;
		Pattern wmiEventsPattern3 = null;
		Pattern snmpTrapPattern = null;
		Pattern latencyBottleneckPattern1 = null;
		Pattern latencyBottleneckPattern2 = null;
		Pattern an1Pattern = null;
		Pattern an2Pattern = null;
		Pattern an3Pattern = null;
		Matcher caseHPc20191Matcher = null;
		Matcher serviceGuardMatcher = null;
		Matcher ciOutageMatcher = null;
		Matcher incompleteMatcher = null;
		Matcher hp1Matcher = null;
		Matcher hp2Matcher = null;
		Matcher hp3Matcher = null;
		Matcher hp4Matcher = null;
		Matcher hp5Matcher = null;
		Matcher hp6Matcher = null;
		Matcher hp7Matcher = null;
		Matcher hp8Matcher = null;
		Matcher hp9Matcher = null;
		Matcher wmiEventsMatcher1 = null;
		Matcher wmiEventsMatcher2 = null;
		Matcher wmiEventsMatcher3 = null;
		Matcher snmpTrapMatcher = null;
		Matcher latencyBottleneckMatcher1 = null;
		Matcher latencyBottleneckMatcher2 = null;
		Matcher an1Matcher = null;
		Matcher an2Matcher = null;
		Matcher an3Matcher = null;
		String astl_object = null;

		public Rule12() {
			astl_object = event.getObject();
		}

		void go() {
			// ####################### HP SIM Events
			// #################################
			if (event.getCategory().equals("HP_SIM")
					&& event.getApplication().equals("HP_SIM")) {

				astl_logical_name = event.getObject();
				astl_sub_category = "HP SIM";

				// # HP SIM events with opened CASE in the HP (C20191)
				caseHPc20191Pattern = Pattern
						.compile("SEA Version:System Event Analyzer for Windows");
				caseHPc20191Matcher = caseHPc20191Pattern.matcher(event
						.getTitle());
				if (caseHPc20191Matcher.matches()) {
					astl_operational_device = true;
				}
				// # Configuring Auto Incidents from Serviceguad cluster
				// (C20026)
				serviceGuardPattern = Pattern.compile("hpmcSG");
				serviceGuardMatcher = serviceGuardPattern.matcher(event
						.getTitle());
				if (serviceGuardMatcher.matches()) {
					astl_logical_name = astl_ci_os_name;
				}
				ciOutagePattern = Pattern
						.compile("(NO_SERVER_CI_OUTAGE_FLAG)(.*)");
				ciOutageMatcher = ciOutagePattern.matcher(event.getTitle());
				// myMatcher = (event.title =~
				// /(NO_SERVER_CI_OUTAGE_FLAG)(.*)/);
				if (ciOutageMatcher.matches()) {
					astl_operational_device = true;
					astl_description = ciOutageMatcher.group(2);
				}
				incompletePattern = Pattern.compile("Incomplete OA XML Data");
				incompleteMatcher = incompletePattern.matcher(event.getTitle());
				if (incompleteMatcher.matches()) {
					astl_priority = "4";
				}

				hp1Pattern = Pattern
						.compile("(SNMP) Process Monitor Event Trap (11011)");
				hp2Pattern = Pattern
						.compile("HP ProLiant-HP Power-Power Supply Failed");
				hp3Pattern = Pattern.compile("cpqHe4FltTolPowerSupplyDegraded");
				hp4Pattern = Pattern.compile("cpqHe4FltTolPowerSupplyFailed");
				hp5Pattern = Pattern.compile("(WBEM) Power redundancy reduced");
				hp6Pattern = Pattern.compile("(WBEM) Power Supply Failed");
				hp7Pattern = Pattern.compile("(WBEM) Power redundancy lost");
				hp8Pattern = Pattern
						.compile("(SNMP)  Power Supply Failed (6050)");
				hp9Pattern = Pattern
						.compile("(SNMP)  Power Redundancy Lost (6032)");
				hp1Matcher = hp1Pattern.matcher(event.getTitle());
				hp2Matcher = hp2Pattern.matcher(event.getTitle());
				hp3Matcher = hp3Pattern.matcher(event.getTitle());
				hp4Matcher = hp4Pattern.matcher(event.getTitle());
				hp5Matcher = hp5Pattern.matcher(event.getTitle());
				hp6Matcher = hp6Pattern.matcher(event.getTitle());
				hp7Matcher = hp7Pattern.matcher(event.getTitle());
				hp8Matcher = hp8Pattern.matcher(event.getTitle());
				hp9Matcher = hp9Pattern.matcher(event.getTitle());
				// if (event.title =~ /(\(SNMP\) Process Monitor Event Trap
				// \(11011\)|HP ProLiant-HP Power-Power Supply
				// Failed|cpqHe4FltTolPowerSupplyDegraded|cpqHe4FltTolPowerSupplyFailed|\(WBEM\)
				// Power redundancy reduced|\(WBEM\) Power redundancy
				// lost|\(WBEM\) Power Supply Failed|\(SNMP\) Power Supply
				// Failed \(6050\)|\(SNMP\) Power Redundancy Lost \(6032\))/)
				if (hp1Matcher.matches() || hp2Matcher.matches()
						|| hp3Matcher.matches() || hp4Matcher.matches()
						|| hp5Matcher.matches() || hp6Matcher.matches()
						|| hp7Matcher.matches() || hp8Matcher.matches()
						|| hp9Matcher.matches()) {
					astl_operational_device = true;
				}

				// # For WMI Events. If string Brief Description is in Message
				// text
				// myMatcher = (event.title =~ /Brief Description:\n\s(.*)/)
				wmiEventsPattern1 = Pattern
						.compile("Brief Description:\\n\\s(.*)");
				wmiEventsMatcher1 = wmiEventsPattern1.matcher(event.getTitle());
				if (wmiEventsMatcher1.matches()) {
					astl_title = wmiEventsMatcher1.group(1);
				}
				// # For SNMP Traps. If string Event Name is in Message text
				// myMatcher = (event.title =~ /Event Name:\s(.*)/);
				snmpTrapPattern = Pattern.compile("Event Name:\\s(.*)");
				snmpTrapMatcher = snmpTrapPattern.matcher(event.getTitle());
				if (snmpTrapMatcher.matches()) {
					astl_title = snmpTrapMatcher.group(1);
				}
				// # For WMI Events. If string Summary is in Message text
				// myMatcher = (event.title =~ /Summary:\s(.*)/);
				wmiEventsPattern2 = Pattern.compile("Summary:\\s(.*)");
				wmiEventsMatcher2 = wmiEventsPattern2.matcher(event.getTitle());
				if (wmiEventsMatcher2.matches()) {
					astl_title = wmiEventsMatcher2.group(2);
				}
				// # For WMI Events. If string Caption is in Message text
				// myMatcher = (event.title =~ /Caption:\s(.*)/);
				wmiEventsPattern3 = Pattern.compile("Caption:\\s(.*)");
				wmiEventsMatcher3 = wmiEventsPattern3.matcher(event.getTitle());
				if (wmiEventsMatcher3.matches()) {
					astl_title = wmiEventsMatcher3.group(1);
				}
				latencyBottleneckPattern1 = Pattern
						.compile("Severe latency bottleneck");
				latencyBottleneckPattern2 = Pattern
						.compile("is a congestion bottleneck");
				latencyBottleneckMatcher1 = latencyBottleneckPattern1
						.matcher(event.getTitle());
				latencyBottleneckMatcher2 = latencyBottleneckPattern2
						.matcher(event.getTitle());
				if (latencyBottleneckMatcher1.matches()
						|| latencyBottleneckMatcher2.matches()) {
					astl_assignment_group = "SN-IO-SSDA-SB";
					astl_category = "Infrastructure";
					astl_sub_category = "SAN Switch";
					astl_operational_device = true;

					an1Pattern = Pattern
							.compile(".*(.*AN-.*Slot.*, port.*is a congestion bottleneck)");
					an1Matcher = an1Pattern.matcher(event.getTitle());
					// = (event.title =~ /.*(.*AN-.*Slot.*, port.*is a
					// congestion bottleneck)/);
					if (an1Matcher.matches()) {
						astl_title = astl_object + " " + an1Matcher.group(1);

						an2Pattern = Pattern
								.compile(".*(.*AN-.*Slot.*, port.*is a congestion bottleneck.*percent of last.*seconds were affected by this condition.)");
						an2Matcher = an2Pattern.matcher(event.getTitle());
						// myMatcher = (event.title =~ /.*(.*AN-.*Slot.*,
						// port.*is a congestion bottleneck.*percent of
						// last.*seconds were affected by this condition.)/);
						if (an2Matcher.matches())
							// astl_description =
							// "${astl_object} ${myMatcher[0][1]}";
							astl_description = astl_object + " "
									+ an2Matcher.group(1);
					}

					an3Pattern = Pattern
							.compile(".*(AN-.*Severe latency bottleneck detected at Slot.*port.*)");
					an3Matcher = an3Pattern.matcher(event.getTitle());
					// myMatcher = (event.title =~ /.*(AN-.*Severe latency
					// bottleneck detected at Slot.*port.*)/);
					if (an3Matcher.matches()) {
						astl_title = astl_object + an3Matcher.group(1);
						astl_description = astl_object + an3Matcher.group(1);
					}
				}
				default_flag = false;
			}
		}
	}

	class Rule13 {
		Pattern r13Pattern = null;
		Pattern r13Pattern2 = null;
		Pattern r13Pattern3 = null;
		Matcher r13Matcher = null;
		Matcher r13Matcher2 = null;
		Matcher r13Matcher3 = null;

		void go() {
			r13Pattern = Pattern.compile("phecda");
			r13Matcher = r13Pattern.matcher(astl_related_ci);
			if (r13Matcher.matches() && event.getCategory().equals("OS")
					&& event.getApplication().equals("Application")
					&& event.getObject().equals("Event Log")) {

				astl_assignment_group = "SN-IO-SSDA-SB";
				astl_category = "Infrastructure";
				astl_sub_category = "Storage";
				astl_priority = "3";
				astl_operational_device = true;
				r13Pattern2 = Pattern
						.compile(".*SOURCE.*\"(.*XP.*)\".*STATUS.*COMPONENT.*\"(.*)\".*DESCRIPTION.*\".*error.*\"");
				r13Matcher2 = r13Pattern2.matcher(event.getTitle());

				if (r13Matcher2.matches()) {
					astl_logical_name = r13Matcher2.group(1);
					astl_title = r13Matcher2.group(1) + " : "
							+ r13Matcher2.group(2);
				}

				r13Pattern3 = Pattern
						.compile(".*SOURCE.*\"(.*AMS.*)\".*STATUS.*COMPONENT.*\"(Disk Drive.*)\".*DESCRIPTION.*");
				r13Matcher3 = r13Pattern3.matcher(event.getTitle());

				if (r13Matcher3.matches()) {
					astl_logical_name = r13Matcher3.group(1);
					astl_title = r13Matcher3.group(1) + " "
							+ r13Matcher3.group(2) + " fail.";
				}

				default_flag = false;
			}
		}
	}

	class Rule14 {
		void go() {
			if (event.getCategory().equals("win-procmon")
					&& event.getApplication().equals("OS")) {

				astl_priority = "2";

				if (event.getObject().equals("startManagedWebLogic.cmd")) {
					astl_priority = "4";
				}
				default_flag = false;
			}
		}
	}

	class Rule15 {
		void go() {
			if (event.getCategory().equals("ELF-USSD")
					|| event.getCategory().equals("ELF-SMS")) {
				if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
					astl_priority = "2";
				}
				default_flag = false;
			}
		}
	}

	class Rule16 {
		void go() {
			if (event.getCategory().equals("Agent_Healthcheck")
					&& event.getObject().equals("opcmsg")) {
				astl_assignment_group = "SN-AO-SCC";

				default_flag = false;
			}
		}
	}

	class Rule17 {
		void go() {
			if (event.getCategory().equals("Gold BAS Logs")
					&& event.getApplication().equals("Gold NG-BAS")
					&& MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
				astl_assignment_group = "SN-AO-CSP-BA";
				astl_logical_name = "OPSC Gold BAS";
				astl_priority = "3";

				default_flag = false;
			}
		}
	}

	// TODO test this !!!
	// TODO Check this!
	class Rule18 {
		Pattern r18Pattern = null;
		Matcher r18Matcher = null;
		Pattern r18Pattern2 = null;
		Matcher r18Matcher2 = null;
		Pattern r18Pattern3 = null;
		Matcher r18Matcher3 = null;
		Pattern r18Pattern4 = null;
		Matcher r18Matcher4 = null;

		void go() {
			if (event.getCategory().equals("TGW")
					&& event.getApplication().equals("TGW")) {

				r18Pattern = Pattern
						.compile("RTE interaction fails and delivers no result in operation [Interaction [RteModifyInteraction] failed");
				r18Matcher = r18Pattern.matcher(event.getTitle());
				astl_logical_name = " ";
				astl_assignment_group = "SN-AO-SCC";
				astl_priority = "4";

				if (r18Matcher.matches()) {
					r18Pattern2 = Pattern
							.compile(".*ERROR.*[[.*?,.*?,(.*?),.*");
					r18Matcher2 = r18Pattern2.matcher(event.getTitle());

					if (r18Matcher2.matches()) {
						astl_title = "Troubles with reload "
								+ r18Matcher2.group(1);
					}
				}
				r18Pattern3 = Pattern
						.compile("[ReloadBalances] The line attribute [RMF] has an invalid value [null]");
				r18Matcher3 = r18Pattern3.matcher(event.getTitle());
				if (r18Matcher3.matches()) {
					r18Pattern4 = Pattern.compile(".*ERROR [.*[[w+,s*+,s(d+)");
					r18Matcher4 = r18Pattern.matcher(event.getTitle());

					if (r18Matcher4.matches()) {
						astl_title = r18Matcher4.group(1)
								+ " attribute [RMF] has an invalid value";
					}
				}

				default_flag = false;
			}
		}
	}

	class Rule19 {
		void go() {
			if (event.getCategory().equals("OVSC")
					&& event.getApplication().equals("OVSC")
					&& event.getObject().equals("IncorreDB")) {
				astl_assignment_group = "SN-AO-CSP-BA";
				astl_logical_name = "OVSC";

				default_flag = false;
			}
		}
	}

	class Rule20 {
		void go() {
			if (event.getCategory().equals("billing_admin_team")
					&& event.getObject().equals("IncoreDB")
					&& (MapOPR2SMUrgency.get(event.getSeverity()).equals("1") || MapOPR2SMUrgency
							.get(event.getSeverity()).equals("2"))) {

				if (event.getApplication().equals("MRTE")) {
					astl_logical_name = "OPSC Gold MRTE";
				}
				if (event.getApplication().equals("OVSC")
						|| event.getApplication().equals("LookUp")) {
					astl_logical_name = "OVSC";
				}
				if (MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
					astl_priority = "2";
				}
				if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
					astl_priority = "3";
				}
				astl_assignment_group = "SN-AO-CSP-BA";

				default_flag = false;
			}
		}
	}

	class Rule21 {
		void go() {
			if (event.getCategory().equals("wIQ")
					&& event.getApplication().equals("wIQ")
					&& MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
				astl_assignment_group = "SN-AO-CSP-BA";
				astl_priority = "4";

				default_flag = false;
			}
		}
	}

	class Rule22 {
		void go() {
			if (event.getCategory().equals("Time")
					&& event.getApplication().equals("NTP")
					&& event.getObject().equals("Time")) {
				astl_logical_name = astl_ci_os_name;
				astl_operational_device = true;

				default_flag = false;
			}
		}
	}

	class Rule23 {
		Pattern r23Pattern = null;
		Matcher r23Matcher = null;

		void go() {
			r23Pattern = Pattern.compile("se9985");
			r23Matcher = r23Pattern.matcher(astl_related_ci);
			if (r23Matcher.matches()) {
				astl_assignment_group = "SN-IO-SSDA-SB";
				astl_priority = "4";

				default_flag = false;
			}
		}
	}

	class Rule24 {
		void go() {
			if (event.getCategory().equals("SCOM")) {
				Pattern pName = Pattern.compile("Name=(.*)");
				Pattern pDescription = Pattern.compile("Description=(.*)Name=",
						Pattern.DOTALL);

				Matcher mName = pName.matcher(event.getTitle());
				Matcher mDescription = pDescription.matcher(event.getTitle());

				if (mName.matches()) {
					astl_title = mName.group(1);
				}

				if (mDescription.find()) {

					astl_description = mDescription.group(1).replace("\\u001a",
							" ");
				}

				if (MapOPR2SMUrgency.get(event.getSeverity()).equals("2")) {
					astl_priority = "2";
				} else if (MapOPR2SMUrgency.get(event.getSeverity())
						.equals("3")) {
					astl_priority = "3";
				} else if (MapOPR2SMUrgency.get(event.getSeverity())
						.equals("4")) {
					astl_priority = "4";
				}
				default_flag = false;
			}
		}
	}

	class Rule25 {
		void go() {
			if (event.getCategory().equals("DP Session Reports")) {

				Pattern pTitle = Pattern.compile("(.*)");
				Matcher mTitle = pTitle.matcher(event.getTitle());
				astl_title = mTitle.group(1);

				astl_logical_name = "HP Data Protector Cell Manager "
						+ astl_related_ci;

				// #set DP_ACTION [exec /opt/OV/scauto/dp/dp_action
				// $OPCDATA_MSGTEXT]
				// eventObject set_evfield action $OPCDATA_MSGTEXT

				// ### Passing assignment group ###
				astl_assignment_group = "SN-IO-SSDA-SB";
				astl_category = "Infrastructure";
				astl_sub_category = "Backups - Software";
				astl_operational_device = true;
				astl_priority = "4";

				default_flag = false;
			}

			if (event.getCategory().equals("Data Protector")
					&& event.getObject().equals("Sheduler")) {

				astl_logical_name = "HP Data Protector Cell Manager "
						+ astl_related_ci;

				astl_assignment_group = "SN-IO-SSDA-SB";
				astl_category = "Infrastructure";
				astl_sub_category = "Backups - Software";
				astl_operational_device = true;
				astl_priority = "3";

				default_flag = false;
			}
		}
	}

	/**
	 * @author maskimko
	 * @version 0.1
	 * @description This class corresponds to Oracle Enterprise Manager Events
	 */
	class Rule26 {
		Pattern myPattern = null;

		void go() {

			if (event.getCategory().equals("OracleEnterpriseManager")) {
				myPattern = Pattern.compile("Message:[ \t\n\f\r](.*)");
				Matcher myMatcher = myPattern.matcher(event.getTitle());

				if (myMatcher.matches()) {
					astl_title = myMatcher.group(1);
				}
				if (event.getApplication().equals("Database Instance")
						|| event.getApplication().equals("Agent")
						|| event.getApplication().equals("Listener")
						|| event.getApplication().equals("OMS and Repository")
						|| event.getApplication().equals(
								"Oracle High Availability Service")) {
					astl_logical_name = event.getObject() + " Instance";
				}
				if (event.getApplication().equals("Cluster")
						|| event.getApplication().equals("Cluster Database")) {
					astl_logical_name = event.getObject() + " DB Cluster";
				}
				// astl_category = "Databases"
				astl_assignment_group = "SN-IO-SSDA-DA";
				astl_priority = "2";

				default_flag = false;
			}
		}
	}

	class Rule27 {
		void go() {

			if (event.getCategory().equals("SN-ISM")
					&& event.getApplication().equals("Security")
					&& event.getObject().equals("Security")) {
				astl_assignment_group = "SN-ISM";
				astl_category = "Security";
				astl_sub_category = "Security Systems Availability";

				astl_operational_device = true;

				default_flag = false;
			} else if (event.getCategory().equals("Security")
					&& event.getApplication().equals("S-TAP agent")) {
				astl_assignment_group = "SN-ISM";
				astl_category = "Security";
				astl_sub_category = "Security Systems Availability";

				astl_operational_device = true;

				default_flag = false;
			} else if (event.getApplication().equals("ASTL_Node_Pinger")
					&& event.getObject().equals("Connection_check")) {
				astl_assignment_group = "SN-ISM";
				astl_category = "Security";
				astl_sub_category = "Security Systems Availability";
				astl_priority = "2";

				default_flag = false;
			}

		}
	}

	class Rule28 {
		void go() {
			if (event.getCategory() == "OpC"
					&& (event.getApplication().equals("HP OpenView Operations") || event
							.getApplication().equals("OM Agent"))
					&& (MapOPR2SMUrgency.get(event.getSeverity()).equals("1") || MapOPR2SMUrgency
							.get(event.getSeverity()).equals("2"))) {
				astl_logical_name = astl_ci_os_name;
				astl_assignment_group = "SN-AO-SCC";
				astl_priority = "3";

				default_flag = false;
			}
		}
	}

	class Rule29 {
		Pattern maximodb2 = null;
		Pattern maximosb = null;
		Pattern maximo01 = null;

		void go() {
			if (event.getApplication().equals("Service Policy")) {

				maximodb2 = Pattern.compile("maximodb2");
				maximosb = Pattern.compile("maximodb2");
				maximo01 = Pattern.compile("maximo01");
				Matcher matchMaximodb2 = maximodb2.matcher(astl_related_ci);
				Matcher matchMaximosb = maximodb2.matcher(astl_related_ci);
				Matcher matchMaximo01 = maximodb2.matcher(astl_related_ci);

				astl_assignment_group = "MN-OS-MSI";

				if (matchMaximodb2.matches() || matchMaximosb.matches()) {
					astl_priority = "3";
				} else if (matchMaximo01.matches()) {
					astl_priority = "4";
				}

				default_flag = false;
			} else if (event.getApplication().equals("MaximoPing")) {
				astl_assignment_group = "MN-OS-MSI";
				astl_priority = "4";

				default_flag = false;
			}
		}
	}

	class Rule30 {
		void go() {
			if (event.getCategory() == "evn_astelit"
					&& event.getApplication() == "Gold MRTE"
					&& (MapOPR2SMUrgency.get(event.getSeverity()).equals("1") || MapOPR2SMUrgency
							.get(event.getSeverity()).equals("2"))) {

				astl_assignment_group = "SN-AO-SCC";
				astl_priority = "3";

				if (event.getObject() == "event-files") {
					astl_logical_name = " ";
				}
				default_flag = false;
			}

			if (event.getCategory() == "MRTE"
					&& MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {

				astl_assignment_group = "SN-AO-CSP-BA";
				astl_priority = "1";

				if (event.getApplication() == "OPSC reg_scp rte1") {
					astl_logical_name = "OPSC reg_scp rte1";
				}
				if (event.getApplication() == "OPSC reg_scp rte2") {
					astl_logical_name = "OPSC reg_scp rte2";
				}
				default_flag = false;
			}
		}
	}

	class Rule31 {
		void go() {
			if (event.getCategory().equals("MRTE")
					&& (event.getApplication().equals("mrte1a") || event
							.getApplication().equals("mrte2a"))
					&& MapOPR2SMUrgency.get(event.getSeverity()).equals("1")) {
				if (event.getApplication().equals("mrte1a")) {
					astl_logical_name = "MRTE1-a";
				}
				if (event.getApplication().equals("mrte2a")) {
					astl_logical_name = "MRTE2-a";
				}
				astl_assignment_group = "SN-AO-CSP-BA";
				astl_priority = "1";

				default_flag = false;
			}
		}
	}

	class Rule32 {
		void go() {
			if (event.getCategory() == "Hardware"
					&& event.getApplication() == "RCU"
					&& MapOPR2SMUrgency.get(event.getSeverity()).equals("3")) {
				astl_logical_name = astl_related_ci + " software";
				astl_priority = "4";

				default_flag = false;
			}
		}
	}

	class Rule33 {
		void go() {
			if (event.getCategory().equals("BCP_mon")
					&& event.getApplication().equals("BCP Backup")
					&& MapOPR2SMUrgency.get(event.getSeverity()).equals("3")) {
				astl_logical_name = event.getObject();
				astl_operational_device = true;
				astl_priority = "3";

				default_flag = false;
			}
		}
	}

	/**
	 * @author maskimko
	 * @version 0.1
	 * @description This class corresponds to NNMi Management Events It handles
	 *              SNMP_Interceptor events
	 */
	class Rule34 {

		void go() {

			if (event.getCategory().equals("SNMP")
					&& event.getApplication().equals("NNMi")
					&& MapOPR2SMUrgency.get(event.getSeverity()).equals("1")
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
			if (event.getCategory().equals("Performance")
					&& event.getApplication().equals("HP OSSPI")
					&& event.getObject().equals("CPU_Wait_Util")) {

				astl_priority = "3";
				astl_operational_device = true;

				default_flag = false;
			}
		}
	}
}
