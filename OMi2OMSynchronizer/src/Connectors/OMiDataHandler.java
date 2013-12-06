package Connectors;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.jdbc.pool.OracleDataSource;

public class OMiDataHandler {

	
	private static String username = "bsm_event";
	private static String password = "msbph";
	private static String databaseName = "HPBSM";
	private static String dbHostname = "krass.sdab.sn";
	private static int dbPort = 1521;
	
	
	public void getAllActiveMessages(Connection conn) throws SQLException{
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT id FROM all_events WHERE state = 'CLOSED' AND control_external_id IS NULL");
		int counter = 0;
		while (rs.next()){
			String msgId = rs.getString("ID");
			System.out.println(msgId);
			counter++;
			
		}
		System.out.println("Total: " + counter  + " active messages");
	}
	
	public Connection getDBConnection() throws SQLException {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);
		connectionProps.put("databaseName", databaseName);
		
		//String connectionUrl  = "jdbc:oracle:thin:" + username  + "/" + password + "@krass.sdab.sn:1521:" + databaseName;
		String connectionUrl  = "jdbc:oracle:thin:@"+dbHostname+":"+dbPort+":" + databaseName;
		OracleDataSource ds = new OracleDataSource();
		ds.setURL(connectionUrl);
		ds.setConnectionProperties(connectionProps);
		conn = ds.getConnection();
		return conn;
	}
	
	public static void main(String[] args){
		OMiDataHandler dh = new OMiDataHandler();
		try {
			dh.getAllActiveMessages(dh.getDBConnection());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
