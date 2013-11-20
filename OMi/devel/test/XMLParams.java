package test;

public class XMLParams {

	private String INCIDENT_XML_NAMESPACE = "http://www.hp.com/2009/software/data_model";
	private String INCIDENT_TAG = "incident";
	private String TITLE_TAG = "name";
	private String DESCRIPTION_TAG = "description";
	private String REFERENCE_NUMBER_TAG = "reference_number";
	private String INCIDENT_STATUS_TAG = "incident_status";
	private String COMPLETION_CODE_TAG = "completion_code";
	private String URGENCY_TAG = "urgency";
	private String PRIORITY_TAG = "priority";
	private String SOLUTION_TAG = "solution";
	private String OWNER_TAG = "is_owned_by";
	private String ASSIGNED_TAG = "has_assigned";
	private String ASSIGNED_GROUP_TAG = "has_assigned_group";
	private String FUNCTIONAL_GROUP_TAG = "functional_group";
	private String RECORDED_BY_TAG = "is_recorded_by";
	private String REQUESTED_BY_TAG = "is_requested_by";
	private String PARTY_TAG = "party";
	private String PERSON_TAG = "person";
	private String UI_NAME_TAG = "display_label";
	private String NAME_TAG = "name";
	private String EXTERNAL_PROCESS_ID_TAG = "external_process_reference";
	private String IMPACT_SCOPE_TAG = "impact_scope";
	private String CONFIGURATION_ITEM_TAG = "configuration_item";
	private String CATEGORY_TAG = "category";
	private String SUB_CATEGORY_TAG = "sub_category";
	private String CI_RELATIONSHIP = "is_registered_for";
	private String CI_TARGET_TYPE_TAG = "target_type";
	private String CI_GLOBALID_TAG = "target_global_id";
	private String CI_TYPE_TAG = "type";
	private String CI_ID_TAG = "id";
	private String CI_NAME_TAG = "name";
	private String CI_DISPLAY_LABEL_TAG = "display_label";
	private String AFFECTS_RELATIONSHIP = "affects_business_service";
	private String NODE_RELATIONSHIP = "is_hosted_on";
	private String NODE_DNS_NAME_TAG = "primary_dns_name";
	private String ACTIVITY_LOG_TAG = "activity_log";
	private String ACTIVITY_LOG_DESC_TAG = "description";
	private String ACTIVITY_LOG_ANNO_TAG = "annotations";
	private String IS_CAUSED_BY = "is_caused_by";
	private String MASTER_REFERENCE_TAG = "master_reference_number";
	/*
	 * Asteli custom tags
	 */

	private String OPERATIONAL_DEVICE_TAG = "operational_device";

	private String NODE_RTSM_ID = "node_rtsm_id";

	private String IS_CAUSED_BY_ROLE = "urn:x-hp:2009:software:data_model:relationship:incident:is_caused_by:incident";

	// Constant values
	private String IMPACT_LABEL_VALUE = "Enterprise";
	private String IT_PROCESS_CATEGORY = "incident";
	private String INCIDENT_TYPE = "incident";
	private String IMPACT_SCOPE = "site-dept";
	private String INCIDENT_XML_VERSION = "1.1";
	private String INCIDENT_XML_TYPE = "urn:x-hp:2009:software:data_model:type:incident";
	private String CI_TARGET_TYPE = "urn:x-hp:2009:software:data_model:type:configuration_item";
	private String CONFIGURATION_ITEM_ROLE = INCIDENT_XML_TYPE
			+ ":is_registered_for:configuration_item";
	private String BUSINESS_SERVICE_ROLE = INCIDENT_XML_TYPE
			+ ":affects_business_service:business_service";
	private String NODE_ITEM_ROLE = CI_TARGET_TYPE + ":is_hosted_on:node";
	private String INCIDENT_XML_RELATIONSHIPS = "false";
	private String TYPE_BUSINESS_SERVICE = "business_service";
	private String REL_IMPACT = "impact_link";
	private String ATTR_GLOBAL_ID = "global_id";
	private String ATTR_NAME = "name";
	private String ATTR_LABEL = "display_label";
	private String ATTR_BUSINESS_CRITICALITY = "business_criticality";

	private String SET_COOKIE_HEADER = "Set-Cookie";

	// Astelit Default Variable
	private String ASTELIT_CATEGORY = "Auto";
	private String ASTELIT_SUB_CATEGORY = "Auto";

	/*
	 * Length of description after truncating
	 */
	private int descriptionLength = 15;

	/**
	 * @param iNCIDENT_XML_NAMESPACE
	 * @param iNCIDENT_TAG
	 * @param tITLE_TAG
	 * @param dESCRIPTION_TAG
	 * @param rEFERENCE_NUMBER_TAG
	 * @param iNCIDENT_STATUS_TAG
	 * @param cOMPLETION_CODE_TAG
	 * @param uRGENCY_TAG
	 * @param pRIORITY_TAG
	 * @param sOLUTION_TAG
	 * @param oWNER_TAG
	 * @param aSSIGNED_TAG
	 * @param aSSIGNED_GROUP_TAG
	 * @param fUNCTIONAL_GROUP_TAG
	 * @param rECORDED_BY_TAG
	 * @param rEQUESTED_BY_TAG
	 * @param pARTY_TAG
	 * @param pERSON_TAG
	 * @param uI_NAME_TAG
	 * @param nAME_TAG
	 * @param eXTERNAL_PROCESS_ID_TAG
	 * @param iMPACT_SCOPE_TAG
	 * @param cONFIGURATION_ITEM_TAG
	 * @param cATEGORY_TAG
	 * @param sUB_CATEGORY_TAG
	 * @param cI_RELATIONSHIP
	 * @param cI_TARGET_TYPE_TAG
	 * @param cI_GLOBALID_TAG
	 * @param cI_TYPE_TAG
	 * @param cI_ID_TAG
	 * @param cI_NAME_TAG
	 * @param cI_DISPLAY_LABEL_TAG
	 * @param aFFECTS_RELATIONSHIP
	 * @param nODE_RELATIONSHIP
	 * @param nODE_DNS_NAME_TAG
	 * @param aCTIVITY_LOG_TAG
	 * @param aCTIVITY_LOG_DESC_TAG
	 * @param aCTIVITY_LOG_ANNO_TAG
	 * @param iS_CAUSED_BY
	 * @param mASTER_REFERENCE_TAG
	 * @param oPERATIONAL_DEVICE_TAG
	 * @param nODE_FQDN
	 * @param nODE_RTSM_ID
	 * @param iS_CAUSED_BY_ROLE
	 * @param iMPACT_LABEL_VALUE
	 * @param iT_PROCESS_CATEGORY
	 * @param iNCIDENT_TYPE
	 * @param iMPACT_SCOPE
	 * @param iNCIDENT_XML_VERSION
	 * @param iNCIDENT_XML_TYPE
	 * @param cI_TARGET_TYPE
	 * @param cONFIGURATION_ITEM_ROLE
	 * @param bUSINESS_SERVICE_ROLE
	 * @param nODE_ITEM_ROLE
	 * @param iNCIDENT_XML_RELATIONSHIPS
	 * @param tYPE_BUSINESS_SERVICE
	 * @param rEL_IMPACT
	 * @param aTTR_GLOBAL_ID
	 * @param aTTR_NAME
	 * @param aTTR_LABEL
	 * @param aTTR_BUSINESS_CRITICALITY
	 * @param sET_COOKIE_HEADER
	 * @param aSTELIT_CATEGORY
	 * @param aSTELIT_SUB_CATEGORY
	 * @param descriptionLength
	 */
	public XMLParams(String iNCIDENT_XML_NAMESPACE, String iNCIDENT_TAG,
			String tITLE_TAG, String dESCRIPTION_TAG,
			String rEFERENCE_NUMBER_TAG, String iNCIDENT_STATUS_TAG,
			String cOMPLETION_CODE_TAG, String uRGENCY_TAG,
			String pRIORITY_TAG, String sOLUTION_TAG, String oWNER_TAG,
			String aSSIGNED_TAG, String aSSIGNED_GROUP_TAG,
			String fUNCTIONAL_GROUP_TAG, String rECORDED_BY_TAG,
			String rEQUESTED_BY_TAG, String pARTY_TAG, String pERSON_TAG,
			String uI_NAME_TAG, String nAME_TAG,
			String eXTERNAL_PROCESS_ID_TAG, String iMPACT_SCOPE_TAG,
			String cONFIGURATION_ITEM_TAG, String cATEGORY_TAG,
			String sUB_CATEGORY_TAG, String cI_RELATIONSHIP,
			String cI_TARGET_TYPE_TAG, String cI_GLOBALID_TAG,
			String cI_TYPE_TAG, String cI_ID_TAG, String cI_NAME_TAG,
			String cI_DISPLAY_LABEL_TAG, String aFFECTS_RELATIONSHIP,
			String nODE_RELATIONSHIP, String nODE_DNS_NAME_TAG,
			String aCTIVITY_LOG_TAG, String aCTIVITY_LOG_DESC_TAG,
			String aCTIVITY_LOG_ANNO_TAG, String iS_CAUSED_BY,
			String mASTER_REFERENCE_TAG, String oPERATIONAL_DEVICE_TAG,
			String nODE_RTSM_ID, String iS_CAUSED_BY_ROLE,
			String iMPACT_LABEL_VALUE, String iT_PROCESS_CATEGORY,
			String iNCIDENT_TYPE, String iMPACT_SCOPE,
			String iNCIDENT_XML_VERSION, String iNCIDENT_XML_TYPE,
			String cI_TARGET_TYPE, String cONFIGURATION_ITEM_ROLE,
			String bUSINESS_SERVICE_ROLE, String nODE_ITEM_ROLE,
			String iNCIDENT_XML_RELATIONSHIPS, String tYPE_BUSINESS_SERVICE,
			String rEL_IMPACT, String aTTR_GLOBAL_ID, String aTTR_NAME,
			String aTTR_LABEL, String aTTR_BUSINESS_CRITICALITY,
			String sET_COOKIE_HEADER, String aSTELIT_CATEGORY,
			String aSTELIT_SUB_CATEGORY, int descriptionLength) {
		super();
		INCIDENT_XML_NAMESPACE = iNCIDENT_XML_NAMESPACE;
		INCIDENT_TAG = iNCIDENT_TAG;
		TITLE_TAG = tITLE_TAG;
		DESCRIPTION_TAG = dESCRIPTION_TAG;
		REFERENCE_NUMBER_TAG = rEFERENCE_NUMBER_TAG;
		INCIDENT_STATUS_TAG = iNCIDENT_STATUS_TAG;
		COMPLETION_CODE_TAG = cOMPLETION_CODE_TAG;
		URGENCY_TAG = uRGENCY_TAG;
		PRIORITY_TAG = pRIORITY_TAG;
		SOLUTION_TAG = sOLUTION_TAG;
		OWNER_TAG = oWNER_TAG;
		ASSIGNED_TAG = aSSIGNED_TAG;
		ASSIGNED_GROUP_TAG = aSSIGNED_GROUP_TAG;
		FUNCTIONAL_GROUP_TAG = fUNCTIONAL_GROUP_TAG;
		RECORDED_BY_TAG = rECORDED_BY_TAG;
		REQUESTED_BY_TAG = rEQUESTED_BY_TAG;
		PARTY_TAG = pARTY_TAG;
		PERSON_TAG = pERSON_TAG;
		UI_NAME_TAG = uI_NAME_TAG;
		NAME_TAG = nAME_TAG;
		EXTERNAL_PROCESS_ID_TAG = eXTERNAL_PROCESS_ID_TAG;
		IMPACT_SCOPE_TAG = iMPACT_SCOPE_TAG;
		CONFIGURATION_ITEM_TAG = cONFIGURATION_ITEM_TAG;
		CATEGORY_TAG = cATEGORY_TAG;
		SUB_CATEGORY_TAG = sUB_CATEGORY_TAG;
		CI_RELATIONSHIP = cI_RELATIONSHIP;
		CI_TARGET_TYPE_TAG = cI_TARGET_TYPE_TAG;
		CI_GLOBALID_TAG = cI_GLOBALID_TAG;
		CI_TYPE_TAG = cI_TYPE_TAG;
		CI_ID_TAG = cI_ID_TAG;
		CI_NAME_TAG = cI_NAME_TAG;
		CI_DISPLAY_LABEL_TAG = cI_DISPLAY_LABEL_TAG;
		AFFECTS_RELATIONSHIP = aFFECTS_RELATIONSHIP;
		NODE_RELATIONSHIP = nODE_RELATIONSHIP;
		NODE_DNS_NAME_TAG = nODE_DNS_NAME_TAG;
		ACTIVITY_LOG_TAG = aCTIVITY_LOG_TAG;
		ACTIVITY_LOG_DESC_TAG = aCTIVITY_LOG_DESC_TAG;
		ACTIVITY_LOG_ANNO_TAG = aCTIVITY_LOG_ANNO_TAG;
		IS_CAUSED_BY = iS_CAUSED_BY;
		MASTER_REFERENCE_TAG = mASTER_REFERENCE_TAG;
		OPERATIONAL_DEVICE_TAG = oPERATIONAL_DEVICE_TAG;

		NODE_RTSM_ID = nODE_RTSM_ID;
		IS_CAUSED_BY_ROLE = iS_CAUSED_BY_ROLE;
		IMPACT_LABEL_VALUE = iMPACT_LABEL_VALUE;
		IT_PROCESS_CATEGORY = iT_PROCESS_CATEGORY;
		INCIDENT_TYPE = iNCIDENT_TYPE;
		IMPACT_SCOPE = iMPACT_SCOPE;
		INCIDENT_XML_VERSION = iNCIDENT_XML_VERSION;
		INCIDENT_XML_TYPE = iNCIDENT_XML_TYPE;
		CI_TARGET_TYPE = cI_TARGET_TYPE;
		CONFIGURATION_ITEM_ROLE = cONFIGURATION_ITEM_ROLE;
		BUSINESS_SERVICE_ROLE = bUSINESS_SERVICE_ROLE;
		NODE_ITEM_ROLE = nODE_ITEM_ROLE;
		INCIDENT_XML_RELATIONSHIPS = iNCIDENT_XML_RELATIONSHIPS;
		TYPE_BUSINESS_SERVICE = tYPE_BUSINESS_SERVICE;
		REL_IMPACT = rEL_IMPACT;
		ATTR_GLOBAL_ID = aTTR_GLOBAL_ID;
		ATTR_NAME = aTTR_NAME;
		ATTR_LABEL = aTTR_LABEL;
		ATTR_BUSINESS_CRITICALITY = aTTR_BUSINESS_CRITICALITY;
		SET_COOKIE_HEADER = sET_COOKIE_HEADER;
		ASTELIT_CATEGORY = aSTELIT_CATEGORY;
		ASTELIT_SUB_CATEGORY = aSTELIT_SUB_CATEGORY;
		this.descriptionLength = descriptionLength;
	}

	/*
	 * Getters section
	 */

	/**
	 * @return the iNCIDENT_XML_NAMESPACE
	 */
	public String getINCIDENT_XML_NAMESPACE() {
		return INCIDENT_XML_NAMESPACE;
	}

	/**
	 * @return the iNCIDENT_TAG
	 */
	public String getINCIDENT_TAG() {
		return INCIDENT_TAG;
	}

	/**
	 * @return the tITLE_TAG
	 */
	public String getTITLE_TAG() {
		return TITLE_TAG;
	}

	/**
	 * @return the dESCRIPTION_TAG
	 */
	public String getDESCRIPTION_TAG() {
		return DESCRIPTION_TAG;
	}

	/**
	 * @return the rEFERENCE_NUMBER_TAG
	 */
	public String getREFERENCE_NUMBER_TAG() {
		return REFERENCE_NUMBER_TAG;
	}

	/**
	 * @return the iNCIDENT_STATUS_TAG
	 */
	public String getINCIDENT_STATUS_TAG() {
		return INCIDENT_STATUS_TAG;
	}

	/**
	 * @return the cOMPLETION_CODE_TAG
	 */
	public String getCOMPLETION_CODE_TAG() {
		return COMPLETION_CODE_TAG;
	}

	/**
	 * @return the uRGENCY_TAG
	 */
	public String getURGENCY_TAG() {
		return URGENCY_TAG;
	}

	/**
	 * @return the pRIORITY_TAG
	 */
	public String getPRIORITY_TAG() {
		return PRIORITY_TAG;
	}

	/**
	 * @return the sOLUTION_TAG
	 */
	public String getSOLUTION_TAG() {
		return SOLUTION_TAG;
	}

	/**
	 * @return the oWNER_TAG
	 */
	public String getOWNER_TAG() {
		return OWNER_TAG;
	}

	/**
	 * @return the aSSIGNED_TAG
	 */
	public String getASSIGNED_TAG() {
		return ASSIGNED_TAG;
	}

	/**
	 * @return the aSSIGNED_GROUP_TAG
	 */
	public String getASSIGNED_GROUP_TAG() {
		return ASSIGNED_GROUP_TAG;
	}

	/**
	 * @return the fUNCTIONAL_GROUP_TAG
	 */
	public String getFUNCTIONAL_GROUP_TAG() {
		return FUNCTIONAL_GROUP_TAG;
	}

	/**
	 * @return the rECORDED_BY_TAG
	 */
	public String getRECORDED_BY_TAG() {
		return RECORDED_BY_TAG;
	}

	/**
	 * @return the rEQUESTED_BY_TAG
	 */
	public String getREQUESTED_BY_TAG() {
		return REQUESTED_BY_TAG;
	}

	/**
	 * @return the pARTY_TAG
	 */
	public String getPARTY_TAG() {
		return PARTY_TAG;
	}

	/**
	 * @return the pERSON_TAG
	 */
	public String getPERSON_TAG() {
		return PERSON_TAG;
	}

	/**
	 * @return the uI_NAME_TAG
	 */
	public String getUI_NAME_TAG() {
		return UI_NAME_TAG;
	}

	/**
	 * @return the nAME_TAG
	 */
	public String getNAME_TAG() {
		return NAME_TAG;
	}

	/**
	 * @return the eXTERNAL_PROCESS_ID_TAG
	 */
	public String getEXTERNAL_PROCESS_ID_TAG() {
		return EXTERNAL_PROCESS_ID_TAG;
	}

	/**
	 * @return the iMPACT_SCOPE_TAG
	 */
	public String getIMPACT_SCOPE_TAG() {
		return IMPACT_SCOPE_TAG;
	}

	/**
	 * @return the cONFIGURATION_ITEM_TAG
	 */
	public String getCONFIGURATION_ITEM_TAG() {
		return CONFIGURATION_ITEM_TAG;
	}

	/**
	 * @return the cATEGORY_TAG
	 */
	public String getCATEGORY_TAG() {
		return CATEGORY_TAG;
	}

	/**
	 * @return the sUB_CATEGORY_TAG
	 */
	public String getSUB_CATEGORY_TAG() {
		return SUB_CATEGORY_TAG;
	}

	/**
	 * @return the cI_RELATIONSHIP
	 */
	public String getCI_RELATIONSHIP() {
		return CI_RELATIONSHIP;
	}

	/**
	 * @return the cI_TARGET_TYPE_TAG
	 */
	public String getCI_TARGET_TYPE_TAG() {
		return CI_TARGET_TYPE_TAG;
	}

	/**
	 * @return the cI_GLOBALID_TAG
	 */
	public String getCI_GLOBALID_TAG() {
		return CI_GLOBALID_TAG;
	}

	/**
	 * @return the cI_TYPE_TAG
	 */
	public String getCI_TYPE_TAG() {
		return CI_TYPE_TAG;
	}

	/**
	 * @return the cI_ID_TAG
	 */
	public String getCI_ID_TAG() {
		return CI_ID_TAG;
	}

	/**
	 * @return the cI_NAME_TAG
	 */
	public String getCI_NAME_TAG() {
		return CI_NAME_TAG;
	}

	/**
	 * @return the cI_DISPLAY_LABEL_TAG
	 */
	public String getCI_DISPLAY_LABEL_TAG() {
		return CI_DISPLAY_LABEL_TAG;
	}

	/**
	 * @return the aFFECTS_RELATIONSHIP
	 */
	public String getAFFECTS_RELATIONSHIP() {
		return AFFECTS_RELATIONSHIP;
	}

	/**
	 * @return the nODE_RELATIONSHIP
	 */
	public String getNODE_RELATIONSHIP() {
		return NODE_RELATIONSHIP;
	}

	/**
	 * @return the nODE_DNS_NAME_TAG
	 */
	public String getNODE_DNS_NAME_TAG() {
		return NODE_DNS_NAME_TAG;
	}

	/**
	 * @return the aCTIVITY_LOG_TAG
	 */
	public String getACTIVITY_LOG_TAG() {
		return ACTIVITY_LOG_TAG;
	}

	/**
	 * @return the aCTIVITY_LOG_DESC_TAG
	 */
	public String getACTIVITY_LOG_DESC_TAG() {
		return ACTIVITY_LOG_DESC_TAG;
	}

	/**
	 * @return the aCTIVITY_LOG_ANNO_TAG
	 */
	public String getACTIVITY_LOG_ANNO_TAG() {
		return ACTIVITY_LOG_ANNO_TAG;
	}

	/**
	 * @return the iS_CAUSED_BY
	 */
	public String getIS_CAUSED_BY() {
		return IS_CAUSED_BY;
	}

	/**
	 * @return the mASTER_REFERENCE_TAG
	 */
	public String getMASTER_REFERENCE_TAG() {
		return MASTER_REFERENCE_TAG;
	}

	/**
	 * @return the oPERATIONAL_DEVICE_TAG
	 */
	public String getOPERATIONAL_DEVICE_TAG() {
		return OPERATIONAL_DEVICE_TAG;
	}

	/**
	 * @return the nODE_RTSM_ID
	 */
	public String getNODE_RTSM_ID() {
		return NODE_RTSM_ID;
	}

	/**
	 * @return the iS_CAUSED_BY_ROLE
	 */
	public String getIS_CAUSED_BY_ROLE() {
		return IS_CAUSED_BY_ROLE;
	}

	/**
	 * @return the iMPACT_LABEL_VALUE
	 */
	public String getIMPACT_LABEL_VALUE() {
		return IMPACT_LABEL_VALUE;
	}

	/**
	 * @return the iT_PROCESS_CATEGORY
	 */
	public String getIT_PROCESS_CATEGORY() {
		return IT_PROCESS_CATEGORY;
	}

	/**
	 * @return the iNCIDENT_TYPE
	 */
	public String getINCIDENT_TYPE() {
		return INCIDENT_TYPE;
	}

	/**
	 * @return the iMPACT_SCOPE
	 */
	public String getIMPACT_SCOPE() {
		return IMPACT_SCOPE;
	}

	/**
	 * @return the iNCIDENT_XML_VERSION
	 */
	public String getINCIDENT_XML_VERSION() {
		return INCIDENT_XML_VERSION;
	}

	/**
	 * @return the iNCIDENT_XML_TYPE
	 */
	public String getINCIDENT_XML_TYPE() {
		return INCIDENT_XML_TYPE;
	}

	/**
	 * @return the cI_TARGET_TYPE
	 */
	public String getCI_TARGET_TYPE() {
		return CI_TARGET_TYPE;
	}

	/**
	 * @return the cONFIGURATION_ITEM_ROLE
	 */
	public String getCONFIGURATION_ITEM_ROLE() {
		return CONFIGURATION_ITEM_ROLE;
	}

	/**
	 * @return the bUSINESS_SERVICE_ROLE
	 */
	public String getBUSINESS_SERVICE_ROLE() {
		return BUSINESS_SERVICE_ROLE;
	}

	/**
	 * @return the nODE_ITEM_ROLE
	 */
	public String getNODE_ITEM_ROLE() {
		return NODE_ITEM_ROLE;
	}

	/**
	 * @return the iNCIDENT_XML_RELATIONSHIPS
	 */
	public String getINCIDENT_XML_RELATIONSHIPS() {
		return INCIDENT_XML_RELATIONSHIPS;
	}

	/**
	 * @return the tYPE_BUSINESS_SERVICE
	 */
	public String getTYPE_BUSINESS_SERVICE() {
		return TYPE_BUSINESS_SERVICE;
	}

	/**
	 * @return the rEL_IMPACT
	 */
	public String getREL_IMPACT() {
		return REL_IMPACT;
	}

	/**
	 * @return the aTTR_GLOBAL_ID
	 */
	public String getATTR_GLOBAL_ID() {
		return ATTR_GLOBAL_ID;
	}

	/**
	 * @return the aTTR_NAME
	 */
	public String getATTR_NAME() {
		return ATTR_NAME;
	}

	/**
	 * @return the aTTR_LABEL
	 */
	public String getATTR_LABEL() {
		return ATTR_LABEL;
	}

	/**
	 * @return the aTTR_BUSINESS_CRITICALITY
	 */
	public String getATTR_BUSINESS_CRITICALITY() {
		return ATTR_BUSINESS_CRITICALITY;
	}

	/**
	 * @return the sET_COOKIE_HEADER
	 */
	public String getSET_COOKIE_HEADER() {
		return SET_COOKIE_HEADER;
	}

	/**
	 * @return the aSTELIT_CATEGORY
	 */
	public String getASTELIT_CATEGORY() {
		return ASTELIT_CATEGORY;
	}

	/**
	 * @return the aSTELIT_SUB_CATEGORY
	 */
	public String getASTELIT_SUB_CATEGORY() {
		return ASTELIT_SUB_CATEGORY;
	}

	/**
	 * @return the descriptionLength
	 */
	public int getDescriptionLength() {
		return descriptionLength;
	}

}
