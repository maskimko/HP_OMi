package Connectors;

import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

public class DiffActiveMessages {

	
	public static void main(String[] args){
		OMiDataHandler dhOmi = new OMiDataHandler();
		OMDataHandler dhOm = new OMDataHandler();
		Set<String> omiMsg = null;
		Set<String> omMsg = null;
		Set<String> diff = new TreeSet<String>();
		try {
			System.out.println("Getting OM active messages");
			omMsg = dhOm.getAllActiveMessages(dhOm.getDBConnection());
			System.out.println("Getting OMi active messages");
			omiMsg = dhOmi.getAllActiveMessages(dhOmi.getDBConnection());
			
			
			System.out.println("OMi active messages quantity: " + omiMsg.size());
			System.out.println("OM active messages quantity: " + omMsg.size());
			for (String msgId : omiMsg){
				if (!omMsg.contains(msgId)){
					diff.add(msgId);
				}
			}
			System.out.println("Difference is: " + diff.size());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
