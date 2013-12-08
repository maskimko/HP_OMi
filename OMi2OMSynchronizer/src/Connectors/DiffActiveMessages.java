package Connectors;

import gui.MessageRow;
import gui.MessageTable;
import gui.MessageTableModel;
import gui.MessageTableRunner;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;

public class DiffActiveMessages {

	
	public static void main(String[] args){
		OMiDataHandler dhOmi = new OMiDataHandler();
		OMDataHandler dhOm = new OMDataHandler();
		Set<MessageRow> omiMsg = null;
		Set<MessageRow> omMsg = null;
		List<MessageRow> diff = new ArrayList<MessageRow>();
		try {
			System.out.println("Getting OM active messages");
			omMsg = dhOm.getAllActiveMessages(dhOm.getDBConnection());
			System.out.println("Getting OMi active messages");
			omiMsg = dhOmi.getAllActiveMessages(dhOmi.getDBConnection());
			
			
			System.out.println("OMi active messages quantity: " + omiMsg.size());
			System.out.println("OM active messages quantity: " + omMsg.size());
			for (MessageRow msg : omiMsg){
				if (!omMsg.contains(msg)){
					diff.add(msg);
				}
			}
			System.out.println("Difference is: " + diff.size());
                        System.out.println("Loading GUI...");
                        MessageTable mt = new MessageTable(new MessageTableModel(diff));
                        MessageTableRunner mtr = new MessageTableRunner(mt);
                        SwingUtilities.invokeLater(mtr);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
