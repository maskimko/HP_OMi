/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;


import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author maskimko
 */
public class MessageSeverity  {
    
    private String severity;
    private int severityValue;
    private Color severityColor;

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
    
     private static final Map<Integer, String> om2omiSeverity;
    static {
        om2omiSeverity = new HashMap<Integer, String>();
        om2omiSeverity.put(32, "CRITICAL");
        om2omiSeverity.put(128, "MAJOR");
        om2omiSeverity.put(64, "MINOR");
       om2omiSeverity.put(16, "WARNING");
       om2omiSeverity.put(8, "NORMAL");
       om2omiSeverity.put(4, "UNKNOWN");
       om2omiSeverity.put(0, "UNCHANGED");
    }
    
    private static final Map<String, Integer> omi2omSeverity;
    static {
        omi2omSeverity = new HashMap<String, Integer>();
        omi2omSeverity.put("CRITICAL", 32);
        omi2omSeverity.put("MAJOR", 128);
        omi2omSeverity.put("MINOR", 64);
        omi2omSeverity.put("WARNING", 16);
        omi2omSeverity.put("NORMAL", 8);
        omi2omSeverity.put("UNKNOWN", 4);
        omi2omSeverity.put("UNCHANGED", 0);
        
    }
    
    public static String getOMiSeverityFromOMSeverity(int severity){
        return om2omiSeverity.get(severity);
    }
    
    public static int getOMSeverityFromOMiSeverity(String severity){
        return omi2omSeverity.get(severity);
    }
    
    
    public MessageSeverity(String severity) {
        this.severity = severity;
        this.severityValue = getOMSeverityFromOMiSeverity(severity);
        this.severityColor = getColorForSeverity(severity);
    }
    
     public MessageSeverity(Integer severityValue) {
        this.severity = getOMiSeverityFromOMSeverity(severityValue);
        this.severityValue = severityValue;
        this.severityColor = getColorForSeverity(severityValue);
    }

    public MessageSeverity(String severity, Integer severityValue, Color severityColor) {
        this.severity = severity;
        this.severityValue = severityValue;
        this.severityColor = severityColor;
    }
    
    
     private static Color getColorForSeverity(Integer value){
       Color returnColor;
       if (value.getClass().equals(Integer.class)) {
           returnColor = colorMap.get(getOMiSeverityFromOMSeverity((Integer) value));
       } 
       else {
           returnColor = Color.WHITE;
       }
       return returnColor;
   } 
     
      private static Color getColorForSeverity(String value){
       Color returnColor;
       if (value.getClass().equals(String.class)){
           returnColor = colorMap.get((String) value);
       }
       else {
           returnColor = Color.WHITE;
       }
       return returnColor;
   } 

    public String getSeverity() {
        return severity;
    }

    public int getSeverityValue() {
        return severityValue;
    }

    public Color getSeverityColor() {
        return severityColor;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setSeverityValue(int severityValue) {
        this.severityValue = severityValue;
    }

    public void setSeverityColor(Color severityColor) {
        this.severityColor = severityColor;
    }
    
    @Override
    public String toString(){
        return getSeverity();
    }
}
