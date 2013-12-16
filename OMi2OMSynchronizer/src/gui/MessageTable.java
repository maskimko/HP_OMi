/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import Connectors.OmiEventCloser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 *
 * @author maskimko
 */
public class MessageTable extends JPanel {
    
    public static boolean debug = false;
    MessageTableModel tableModel = null;
    JTable msgTable = null;
    private JTextArea infoText = null;
    private JProgressBar progressBar = null;
    
    public MessageTable(MessageTableModel mtm) {
        super(new GridLayout(1, 0));
        this.tableModel = mtm;
        msgTable = new JTable(tableModel);
        msgTable.setPreferredScrollableViewportSize(new Dimension(700, 200));
        msgTable.setFillsViewportHeight(true);
        JScrollPane sp = new JScrollPane(msgTable);
        msgTable.setDefaultRenderer(MessageSeverity.class, new SeverityRenderer(true));
        infoText = new JTextArea(8, 70);
        infoText.setEditable(false);
        add(sp);
    }
    
    public void addInfoText(String text) {
        infoText.append(text);
    }
    
    public void showTableGui() {
        JFrame frame = new JFrame("Choose events to close");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel highlight = new JPanel();
        highlight.setLayout(new FlowLayout());
        JButton selectAllEvents = new JButton("Select all");
        selectAllEvents.addActionListener(new SelectAllEventsListener(selectAllEvents));
        JButton closeEvents = new JButton("Close events");
        closeEvents.addActionListener(new CloseEventsListener());
        JLabel hlabel = new JLabel("Difference table");
        highlight.add(hlabel);
        highlight.add(selectAllEvents);
        highlight.add(closeEvents);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        
        JPanel manageInfoText = new JPanel();
        manageInfoText.setLayout(new FlowLayout());
        
        JScrollPane textScroll = new JScrollPane(infoText);
        
        textScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       
     
        
        
        JButton clearText = new JButton("Clear info");
        clearText.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                infoText.setText("");
            }
            
        });
        manageInfoText.add(clearText);
        infoPanel.add(textScroll, BorderLayout.CENTER);
        infoPanel.add(manageInfoText, BorderLayout.SOUTH);
        
        mainPanel.add(highlight, BorderLayout.NORTH);
        mainPanel.add(this, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);        
    }
    
    class CloseEventsListener implements ActionListener {
        
        OmiEventCloser oec = null;
       
        public CloseEventsListener() {
           
            
            try {
                oec = new OmiEventCloser("bsm-gw1.sdab.sn", OmiEventCloser.BSMJBOSSJMXPORT, eventIds, "mshkolny", infoText);
            } catch (IOException ioe) {
                infoText.append("Error: Could not initializate OmiEventCloser\n" + ioe.getMessage());
            }
        }
        
        
       
        
        @Override
        public void actionPerformed(ActionEvent e) {
           
            
        
                Thread eventCloseWorker = new Thread(oec);
                eventCloseWorker.run();
        }
        
    }
    
    
    class ClosingProgressBar extends JPanel implements PropertyChangeListener {

        public ClosingProgressBar(){
            super(new BorderLayout());
            
            progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
        }
        
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

         
        

    }
    
   class EventPrepare extends SwingWorker<Void, Void> {

       TreeSet<String> eventIds = new TreeSet<String>();
       
        @Override
        protected Void doInBackground() throws Exception {
            infoText.append("Preparing events for closing...\n");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int progress  = 0;
                    setProgress(progress);
                
                if ((Boolean) tableModel.getValueAt(i, 0)) {
                       
                        eventIds.add((String) tableModel.getValueAt(i, 1));
                        progress = i * 100 / tableModel.getRowCount();
                        setProgress(progress);
                    }
                }
            infoText.append("Events are ready for closing\n");
            return null;
        }
       
   }
    
    class SelectAllEventsListener implements ActionListener {
        
        JButton button;
        boolean select = true;
        
        public SelectAllEventsListener(JButton btn) {
            this.button = btn;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt(select, i, 0);
            }
            if (select) {
                button.setText("Deselect all");
                
            } else {
                button.setText("Select all");
            }
            select = !select;
        }
        
    }
}
