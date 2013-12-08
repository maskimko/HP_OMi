package Connectors;

import gui.MessageRow;
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
	
	public Set<MessageRow> getAllActiveMessages(Connection conn) throws SQLException{
		Set<MessageRow> activeMessageId = new TreeSet<MessageRow >();
		Statement stmt = conn.createStatement();
                MessageRow mr = null;
		ResultSet rs = stmt.executeQuery("SELECT message_number, message_group, application, object, severity  FROM opc_act_messages");
		while (rs.next()){
                    mr = new MessageRow(false, rs.getString("MESSAGE_NUMBER"), SeverityMapper.getOMiSeverityFromOMSeverity(rs.getInt("SEVERITY")), rs.getString("APPLICATION"), rs.getString("MESSAGE_GROUP"), rs.getString("OBJECT"));
			
			activeMessageId.add(mr);
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
