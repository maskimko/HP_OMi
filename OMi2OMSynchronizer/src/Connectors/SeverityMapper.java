/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Connectors;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author maskimko
 */
public class SeverityMapper {
    
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
}
