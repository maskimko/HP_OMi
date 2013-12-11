package engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OMDBConnector {

	private Connection con = null;
	private String driverName = "oracle.jdbc.driver.OracleDriver";
	String serverName = "omdb.sdab.sn";
	int portNumber = 1521;
	String sid  = "openview";
	public OMDBConnector() {}
	
	public boolean doConnection(String username, String password){
		
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e){
			System.out.println("ClassNotFoundException: " + e.getMessage());
			return false;
		}
		
		String url = "jdbc:oracle:thin:@" + serverName + ":" + portNumber + ":" + sid;
		//For mySQL
		//String uri = "jdbc:mysql://server ip address:port/database name"
		try {
		
			con = DriverManager.getConnection(url, username, password);
		} catch (SQLException sqle){
			sqle.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void closeConnection(){
		try {
			con.close();
		} catch (NullPointerException npe){
			npe.printStackTrace();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	
	public void doStuff(){
		String query = "SELECT DISTINCT opc_node_names.node_name, COUNT (opc_act_messages.msg_key) FROM opc_act_messages, opc_node_names WHERE opc_act_messages.node_id=opc_node_names.node_id GROUP BY opc_node_names.node_name ORDER BY 2 DESC";
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()){
				System.out.println("Node: " + rs.getString(1) + " has " + rs.getInt(2) + " active messages");
			}
		} catch (SQLException sqle){
			sqle.printStackTrace();
		}
	}
	 
	
	
	public static void main(String[] args) {
		String username = "opc_op";
		String password = "opc_op";
		OMDBConnector omCon = new OMDBConnector();
		System.out.println("Connection: "+ omCon.doConnection(username, password));
		omCon.doStuff();
		omCon.closeConnection();
	}
}
