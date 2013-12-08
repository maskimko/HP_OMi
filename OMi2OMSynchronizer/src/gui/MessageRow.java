/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

/**
 *
 * @author maskimko
 */
 public class MessageRow implements Comparable<MessageRow> {
        public boolean closeEvent = false;
        public String eventId;
        public String severity;
        public String application;
        public String category;
        public String msgObject;
        
        public MessageRow(){
        }
        
   public MessageRow(boolean closeEvent, String eventId, String severity, String application, String category, String object) throws NullPointerException{
       this.closeEvent = closeEvent;
       if(eventId != null) {
           this.eventId = eventId;
       } else {
           throw new NullPointerException("Error: eventId is null");
       }
       this.application = application;
       this.severity = severity;
       this.msgObject = object;
       this.category = category;
   }
        
        public Object getValueAt(int columnIndex) throws IllegalArgumentException{
            switch (columnIndex){
                case 0: 
                    return closeEvent;
                  
                case 1:
                    return eventId;
               
                case 2:
                    return severity;
                   
                case 3:
                    return application;
                   
                case 4: 
                    return category;
                  
                case 5:
                    return msgObject;
                default:
                    throw new IllegalArgumentException("Last column index is 5");
            }
            
        } 
        
        /*
        Critical ff0000
        Major   ff8000
        Minor   ffff00
        Warning 00ffff
        Normal  00ff00
        Unknown 0080ff
        */
        
        
        public void setValueAt(int columnIndex, Object value) throws IllegalArgumentException{
            switch (columnIndex){
                case 0: 
                    if (value.getClass().equals(Boolean.class)){
                        closeEvent=(Boolean)value;
                    }else if (value.getClass().equals(String.class)){
                            closeEvent = Boolean.parseBoolean((String)value);
                     
                    } else throw new IllegalArgumentException("Cannot parse value");
                    
                  
                case 1:
                    eventId = (String) value;
               
                case 2:
                    severity = (String) value;
                   
                case 3:
                    application = (String) value;
                   
                case 4: 
                    category = (String) value;
                  
                case 5:
                    msgObject = (String) value;
                default:
                    throw new IllegalArgumentException("Last column index is 5");
            }
            
        } 

        @Override 
        public int hashCode(){
            return eventId.hashCode();
        }
        
        @Override
        public boolean equals(Object o){
            MessageRow mr = (MessageRow) o;
           return eventId.equals(mr.eventId);
        }
        
        
    @Override
    public int compareTo(MessageRow o) {
       return this.eventId.compareTo(o.eventId);
    }
    }
