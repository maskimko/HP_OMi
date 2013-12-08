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
public class MessageTableRunner implements Runnable{

    private MessageTable messageTable = null;
    
    public MessageTableRunner(MessageTable messageTable){
        this.messageTable = messageTable;
    }
    
    @Override
    public void run() {
        messageTable.showTableGui();
    }
    
}
