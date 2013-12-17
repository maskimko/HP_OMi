/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import Connectors.OmiEventCloser;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.BorderFactory;
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
    private ClosingProgressBar cpb = null;
    private JButton closeEvents;
    private OmiEventCloser oec = null;

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
        cpb = new ClosingProgressBar();
        add(sp);
        try {
        oec = new OmiEventCloser("bsm-gw1.sdab.sn", OmiEventCloser.BSMJBOSSJMXPORT);
        } catch (IOException ioe){
            infoText.append("Cannot initialize event closer " + ioe.getMessage());
        }
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
        closeEvents = new JButton("Close events");
        closeEvents.addActionListener(new CloseEventsListener());
        JLabel hlabel = new JLabel("Difference table");

        highlight.add(hlabel);
        highlight.add(selectAllEvents);
        highlight.add(closeEvents);
        highlight.add(cpb);
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

        @Override
        public void actionPerformed(ActionEvent e) {

            closeEvents.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            EventPrepare ep = new EventPrepare();
            ep.addPropertyChangeListener(cpb);
            ep.execute();

        }

    }

    class ClosingProgressBar extends JPanel implements PropertyChangeListener {

        JProgressBar progressBar = null;

        public ClosingProgressBar() {
            super(new BorderLayout());

            progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            this.add(progressBar);
            this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("progress")) {
                int progress = (Integer) evt.getNewValue();
                progressBar.setValue(progress);
            }
        }

    }

    class EventPrepare extends SwingWorker<Void, Void> {

        //TreeSet<String> eventIds = new TreeSet<String>();
        int cursorPoint = 0;
        
        private int getSelectedCount(MessageTableModel tabMod){
            
            int counter = 0;
            for (int i = 0; i < tabMod.getRowCount(); i++){
                if ((Boolean) tabMod.getValueAt(i, 0)){
                    counter++;
                }
            }
            return counter;
        }
        
        
        @Override
        protected Void doInBackground() throws Exception {
            
            String messageId = null;
            int progress = 0;
            int deleteCandidates = getSelectedCount(tableModel);
            int initialDeleteCandidates = deleteCandidates;
            infoText.append("Start closing "+initialDeleteCandidates+" events\n");
            setProgress(progress);
            while(deleteCandidates != 0) {
                
                if ((Boolean) tableModel.getValueAt(cursorPoint, 0)) {
                    messageId = (String) tableModel.getValueAt(cursorPoint, 1);
                    oec.closeIncident(messageId, "mshkolny");
                    tableModel.deleteRow(cursorPoint);
                    deleteCandidates--;
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ie) {
                    }
                    progress = 100 - deleteCandidates * 100 / initialDeleteCandidates;
                    setProgress(progress);
                } else {
                    cursorPoint++;
                }
            }
            setProgress(100);

            return null;
        }

        @Override
        protected void done() {
            Toolkit.getDefaultToolkit().beep();
            closeEvents.setEnabled(true);
            setCursor(null);
            infoText.append("Done");
            cursorPoint = 0;
            
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
