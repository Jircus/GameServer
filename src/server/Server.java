/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Server
 * @author Jircus
 */
public final class Server extends JFrame implements Runnable {
    
    private final ArrayList connectedClients;
    private final ArrayList activeThreads;
    private ServerThread thread;
    private JTextArea outputArea;
    private JButton startStopButton;
    private MouseAdapter adapter;
    private ServerSocket serverSocket;
    private Thread thisThread;
    private final int port;
    
    /**
     * Creates new instance
     * @param port 
     */
    public Server(int port) {
        init();
        connectedClients = new ArrayList();
        activeThreads = new ArrayList();
        this.port = port;
    }
    
    /**
     * Waits for connections 
     */
    @Override
    public void run() {    
        try {
            System.out.println("Server started");
            addOutput("Server started");
            serverSocket = new ServerSocket(port);
            System.out.println("Listening on " + serverSocket);
            addOutput("Listening on " + serverSocket);
            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection " + socket);
                addOutput("New connection " + socket);
                connectedClients.add(socket);
                if(connectedClients.size() % 2 == 0) {
                    synchronized(thread) {
                        if(thread.isPlayerConnected() == true) {
                            thread.setSocketOfPlayer2(socket);
                            System.out.println("Added second player to thread");
                            addOutput("Added second player to thread");
                            thread.notify();
                        }
                        else {
                            thread.notify();
                            thread = new ServerThread(this, socket);
                            activeThreads.add(thread);
                            System.out.println("Created new thread [" +
                                    activeThreads.indexOf(thread) + "]");
                            addOutput("Created new thread [" +
                                    activeThreads.indexOf(thread) + "]");
                        }
                    }
                }
                else {
                    thread = new ServerThread(this, socket);
                    activeThreads.add(thread);
                    System.out.println("Created new thread [" +
                            activeThreads.indexOf(thread) + "]");
                    addOutput("Created new thread [" + activeThreads.indexOf(thread) + "]");
                }
            }
        } catch (IOException ex) {
            if(ex.toString().equals("java.net.SocketException: socket closed")) {
                System.out.println("Server stopped");
                addOutput("Server stopped");
            }
            else {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Removes socket of disconnected client
     * @param s 
     */
    public void removeConnection(Socket s) {
        synchronized(connectedClients) {
            connectedClients.remove(s);
            System.out.println(s + " removed");
            addOutput(s + " removed");
            try {
                s.close();
            }
            catch(IOException ie) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ie);
            }
        }
    }
    
    /**
     * Removes thread that stopped
     * @param thread 
     */
    public void removeThread(Thread thread) {
        synchronized(activeThreads){
            int index = activeThreads.indexOf(thread);
            activeThreads.remove(thread);
            System.out.println("Thread [" + index + "] has stopped and has been removed");
            addOutput("Thread [" + index + "] has stopped and has been removed");
        }
    }
    
    /**
     * Appends server output to GUI
     * @param message 
     */
    public void addOutput(String message) {
        outputArea.append(message + "\n");
    }
    
    /**
     * Creates GUI
     */
    private void init(){
        JScrollPane pane = new JScrollPane();  
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        pane.setViewportView(outputArea);
        this.setLayout(new BorderLayout());
        this.add(pane, BorderLayout.CENTER);
        startStopButton = new JButton("Start server");
        adapter  = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                startServer(e);
            }
        };
        startStopButton.addMouseListener(adapter);
        this.add(startStopButton, BorderLayout.SOUTH);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(500, 400);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    
    /**
     * Starts server, removes current mouse listener and adds one that stops server
     * @param e 
     */
    private void startServer(MouseEvent e){
        thisThread = new Thread(this);
        thisThread.start();
        startStopButton.removeMouseListener(adapter);
        adapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                stopServer(e);
            }
        };
        startStopButton.addMouseListener(adapter);
        startStopButton.setText("Stop server");
    }
    
    /**
     * Stops the server and all his threads, removes current mouse listener
     * and adds one that starts the server
     * @param e 
     */
    private void stopServer(MouseEvent e) {
        try {
            synchronized(activeThreads) {
                for(Object obj : activeThreads) {
                    ServerThread th = (ServerThread)obj;
                    synchronized(th) {
                        th.notify();
                        th.stopThread();    
                    }
                }
            }
            serverSocket.close();
            startStopButton.removeMouseListener(adapter);
            adapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    startServer(e);
                }
            };
            startStopButton.addMouseListener(adapter);
            startStopButton.setText("Start server");
        }
        catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
