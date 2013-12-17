/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author maskimko
 */
public class MessageTableModel extends AbstractTableModel {

    /*
    Columns in table 
    boolean Close
    String event id
    String severity
    String application
    String Message group
    String object 
    
    
    */
      Vector<MessageRow> data;
    
    public MessageTableModel(Object[][] data){
        MessageRow mr = null;
        for (int i = 0; i < data.length; i++){
            mr = new MessageRow();
            for (int j = 0; j < data[i].length; j++){
                mr.setValueAt(j, data[i][j]);
            }
            this.data.add(mr);
        }
    }
    
    
    public MessageTableModel(List<MessageRow> data){
        this.data = new Vector<MessageRow>();
        this.data.addAll(data);
    }
    
 
    
    @Override
       public String getColumnName(int colIndex) throws IllegalArgumentException{
            switch (colIndex){
                case 0:
                    return "Close Event";
                case 1:
                    return "ID";
                case 2: 
                    return "Severity";
                case 3:
                    return "Application";
                case 4:
                    return "Message category";
                case 5: 
                    return "Object";
                default:
                    throw new IllegalArgumentException("Last column index is 5");
            }
        }
    
    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
     return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
       return data.get(rowIndex).getValueAt(columnIndex);
    }
    
    @Override
    public void setValueAt(Object value, int row, int columnIndex){
        data.get(row).setValueAt(columnIndex, value);
        fireTableCellUpdated(row, columnIndex);
    }
    
    @Override
    public Class getColumnClass(int c){
        return MessageRow.getDataClass(c);
    }
  
    @Override 
        public boolean isCellEditable(int row, int col){
        if (col == 0) {
            return true; 
        } else {
            return false;
        }
    }
        
        
        public void deleteRow(int rowNumber){
            data.remove(rowNumber);
            fireTableRowsDeleted(rowNumber, rowNumber);
        }
}
