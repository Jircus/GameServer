/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server
 * @author Jircus
 */
public final class Server {
    
    private final ArrayList connectedClients;
    private final ArrayList activeThreads;
    private ServerThread thread;
    
    /**
     * Creates new instance
     * @param port 
     */
    public Server(int port) {
        connectedClients = new ArrayList();
        activeThreads = new ArrayList();
        listen(port);
    }
    
    /**
     * Waits for connections
     * @param port 
     */
    public void listen(int port) {    
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Listening on " + serverSocket);
            while(true) {
                Socket socket = serverSocket.accept(); 
                System.out.println("New connection " + socket);
                connectedClients.add(socket);
                if(connectedClients.size() % 2 == 0) {
                    synchronized(thread) {
                        if(thread.isPlayerConnected() == true) {
                            thread.setSocketOfPlayer2(socket);
                            System.out.println("Added second player to thread");
                            thread.notify();
                        }
                        else {
                            thread.notify();
                            thread = new ServerThread(this, socket);
                            activeThreads.add(thread);
                            System.out.println("Created new thread [" +
                                    activeThreads.indexOf(thread) + "]");
                        }
                    }
                }
                else {
                    thread = new ServerThread(this, socket);
                    activeThreads.add(thread);
                    System.out.println("Created new thread [" +
                            activeThreads.indexOf(thread) + "]");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Remove socket of disconnected client
     * @param s 
     */
    public void removeConnection(Socket s) {
        synchronized(connectedClients) {
            connectedClients.remove(s);
            System.out.println(s + " removed");
            try {
                s.close();
            }
            catch(IOException ie) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ie);
            }
        }
    }
    
    public void removeThread(Thread thread) {
        synchronized(activeThreads){
            int index = activeThreads.indexOf(thread);
            activeThreads.remove(thread);
            System.out.println("Thread [" + index + "] has stopped and has been removed");
        }
    }
}
