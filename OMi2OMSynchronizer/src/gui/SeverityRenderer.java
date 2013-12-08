/*
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
    
    private static Map<String, Color> colorMap;
    static {
        colorMap = new HashMap<String, Color>();
        colorMap.put("CRITICAL", new Color(0xff0000));
        colorMap.put("MAJOR", new Color(0xff8000));
        colorMap.put("MINOR", new Color(0xffff00));
        colorMap.put("WARNING", new Color(0x00ffff));
        colorMap.put("NORMAL", new Color(0x00ff00));
        colorMap.put("UNKNOWN", new Color(0x0080ff));
    }
    
    
    public SeverityRenderer(boolean isBordered){
        this.isBordered = isBordered;
        setOpaque(true);
    }
    
   private static Color getColorForSeverity(Object value){
       Color returnColor = null;
       if (value.getClass().equals(String.class)){
           returnColor = colorMap.get((String) value);
       } else {
           returnColor = Color.WHITE;
       }
       return returnColor;
   } 
    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Color newColor = getColorForSeverity(value);
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
        setToolTipText("RGB value is: " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue());
        return this;
    }
    
}
