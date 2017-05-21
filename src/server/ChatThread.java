/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread for chat
 * @author Jircus
 */
public class ChatThread implements Runnable {
    
    private final ObjectOutputStream output1;
    private final ObjectOutputStream output2;
    private final ObjectInputStream input;
    private final Server server;
    private final String name;
    private final String index;

    /**
     * Creates new instance
     * @param input
     * @param output1
     * @param output2
     * @param server 
     * @param name 
     * @param index 
     */
    public ChatThread(ObjectInputStream input, ObjectOutputStream output1,
            ObjectOutputStream output2, Server server, String name, int index) {
        this.input = input;
        this.output1 = output1;
        this.output2 = output2;
        this.server = server;
        this.name = name;
        if(index == 1){
            this.index = "First";
        }
        else {
            this.index = "Second";
        }
    } 

    /**
     * Receives and sends messages of clients
     */
    @Override
    public void run() {
        try {
            while(true) {
                String message = input.readObject().toString();
                server.output("Receiving message '" + message + "' from " + name);
                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                Date date = new Date();
                output1.writeObject(name + " (" + dateFormat.format(date) + ")\n" + message);
                output2.writeObject(name + " (" + dateFormat.format(date) + ")\n" + message);
                server.output("Sending message");
            }
        } catch(ClassNotFoundException ex) {
            Logger.getLogger(ChatThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch(IOException e) {
            server.output(index + " chat thread stopped");
        }
        
    }
    
    /**
     * Closes all streams in order to stop the thread
     */
    public void stopThread(){
        try {
            output1.close();
            output2.close();
            input.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
}
