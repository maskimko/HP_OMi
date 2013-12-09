/*g
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author maskimko
 */
public class SeverityRenderer extends JLabel implements TableCellRenderer {

    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;
    
   
    
    
    public SeverityRenderer(boolean isBordered){
        this.isBordered = isBordered;
        setOpaque(true);
    }
    
  
    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        MessageSeverity ms = (MessageSeverity) value;
        Color newColor = ms.getSeverityColor();
        setBackground(newColor);
        if (isBordered){
            if (isSelected){
                if (selectedBorder == null ) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                    
                }
                setBorder(unselectedBorder);
            }
        }
        setText(ms.getSeverity());
        setToolTipText("RGB value is: " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue());
        return this;
    }
    
}
