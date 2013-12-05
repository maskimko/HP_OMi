package opr;

public interface BusinessLogicDelegateMBean {

	public String addAnnotation(String eventId, String user, String annotationText);
	public void reinitIncidentCache();
	public String openIncident(String eventId, String user);
	public String workOnIncident(String eventId, String user);
	public String resolveIncident(String eventId, String user);
	public String closeIncident(String eventId, String user);
	public String closeIncidentandresetHI(String eventId, String user);
	public String showNumberOfTopLevelEvents();
	public String showNumberOfEvents();
	public String showIncidentHierarchyOfIncidentCache();
}
