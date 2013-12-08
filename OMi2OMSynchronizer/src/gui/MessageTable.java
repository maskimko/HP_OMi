/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

/**
 *
 * @author maskimko
 */
public class MessageTable extends JPanel{
    
    public static boolean debug = false;
    
    public MessageTable(){
        super(new GridLayout(1, 0));
        String[] columnNames = {"Message id", "Severity", "Node", "Message text"};
        Object[][] data = {
            {"test_id", "Warning", "vm-cacti.sdab.sn", "First row message"},
            {"test_identity", "Major", "vm-cacti-new.sdab.sn", "Some text"}
        };
        JTable msgTable = new JTable(data, columnNames);
        msgTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        msgTable.setFillsViewportHeight(true);
        
        if (debug){
          MsgTableListener mtl = new MsgTableListener(msgTable);
          msgTable.addMouseListener(mtl);
        }
        JScrollPane scrollpane = new JScrollPane(msgTable);
        add(scrollpane);
    }
    
    private void printDebugData(JTable table){
        int numRows = table.getRowCount();
        int numColumns = table.getColumnCount();
        TableModel model = table.getModel();
        
        System.out.println("Value of data:");
        for (int i = 0; i < numRows; i++){
            System.out.print("   row: " + i + ":");
                for (int j=0; j < numColumns; j++){
                    System.out.print(" " + model.getValueAt(i, j));
                }
                System.out.println();
                
        }
        System.out.println("-------------------------------------------");
    }
    
    
    public static void createAndShowGui(){
        JFrame frame = new JFrame("Choose events to close");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel highlight = new JPanel();
        JPanel tablePanel = new MessageTable(); 
        JLabel hlabel = new JLabel("Difference table");
        highlight.add(hlabel);
      
        mainPanel.add(highlight, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);
        
    }
    
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable(){
        @Override
            public void run(){
            createAndShowGui();
        }});
    }
    
    class MsgTableListener implements MouseListener{

      JTable tbl = null;
        
        MsgTableListener(JTable tbl){
            this.tbl = tbl;
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            printDebugData(tbl);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseExited(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
