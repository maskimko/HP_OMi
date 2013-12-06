package Connectors;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import oracle.jdbc.pool.OracleDataSource;

public class OMDataHandler {

	
	private static String username = "opc_op";
	private static String password = "opc_op";
	private static String databaseName = "openview";
	private static String dbHostname = "10.1.50.191";
	private static int dbPort = 1521;
	
	public Set<String> getAllActiveMessages(Connection conn) throws SQLException{
		Set<String> activeMessageId = new TreeSet<String>();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT message_number FROM opc_act_messages");
		while (rs.next()){
			String msgId = rs.getString("MESSAGE_NUMBER");
			activeMessageId.add(msgId);
		}
		return activeMessageId;
	}
	
	public Connection getDBConnection() throws SQLException {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);
		connectionProps.put("databaseName", databaseName);
		
		String connectionUrl  = "jdbc:oracle:thin:@"+dbHostname+":"+dbPort+":" + databaseName;
		OracleDataSource ds = new OracleDataSource();
		ds.setURL(connectionUrl);
		ds.setConnectionProperties(connectionProps);
		conn = ds.getConnection();
		return conn;
	}
	
}
