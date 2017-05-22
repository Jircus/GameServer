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
    private GameThread gameThread;
    private JTextArea outputArea;
    private JButton startStopButton;
    private MouseAdapter adapter;
    private ServerSocket gameServerSocket;
    private ServerSocket chatServerSocket;
    private Thread thisThread;
    private final int port;
    private final int secondPort;
    
    /**
     * Creates new instance
     * @param port 
     * @param secondPort 
     */
    public Server(int port, int secondPort) {
        init();
        connectedClients = new ArrayList();
        activeThreads = new ArrayList();
        this.port = port;
        this.secondPort = secondPort;
    }
    
    /**
     * Waits for connections 
     */
    @Override
    public void run() {    
        try {
            output("Server started");
            gameServerSocket = new ServerSocket(port);
            chatServerSocket = new ServerSocket(secondPort);
            output("Listening on " + gameServerSocket + " and " + chatServerSocket);
            while(true) {
                Socket socket = gameServerSocket.accept();
                Socket chatSocket = chatServerSocket.accept();
                output("New connection " + socket + "and " + chatSocket);
                connectedClients.add(socket);
                if(connectedClients.size() % 2 == 0) {
                    synchronized(gameThread) {
                        if(gameThread.isPlayerConnected() == true) {
                            gameThread.setSocketOfPlayer2(socket, chatSocket);
                            output("Added second player to thread");
                            gameThread.notify();
                        }
                        else {
                            gameThread.notify();
                            gameThread = new GameThread(this, socket, chatSocket);
                            gameThread.start();
                            activeThreads.add(gameThread);
                            output("Started new thread [" +
                                    activeThreads.indexOf(gameThread) + "]");
                        }
                    }
                }
                else {
                    gameThread = new GameThread(this, socket, chatSocket);
                    gameThread.start();
                    activeThreads.add(gameThread);
                    output("Started new thread [" + activeThreads.indexOf(gameThread) + "]");
                }
            }
        } catch (IOException ex) {
            if(ex.toString().equals("java.net.SocketException: socket closed")) {
                output("Server stopped");
            }
            else {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Removes sockets of disconnected client
     * @param game 
     * @param chat 
     */
    public void removeConnection(Socket game, Socket chat) {      
        connectedClients.remove(game);
        try {
            game.close();
            chat.close();
            output(game + " and " + chat + " have been removed");
        }
        catch(IOException ie) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ie);
        }      
    }
    
    /**
     * Removes thread that stopped
     * @param thread 
     */
    public void removeThread(GameThread thread) {      
        int index = activeThreads.indexOf(thread);
        activeThreads.remove(thread);
        output("Game thread [" + index + "] has stopped and has been removed");
    }
    
    /**
     * Appends server output to GUI
     * @param message 
     */
    public void output(String message) {
        outputArea.append(message + "\n");
        System.out.println(message);
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
        this.setSize(750, 500);
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
            int size = activeThreads.size();
            for(int i = 0; i < size; i++) {
                GameThread th = (GameThread)activeThreads.get(0);                    
                synchronized(th) {
                    th.notify();
                    th.stopThread();
                }
                synchronized(this) {
                    this.wait(100);
                }
            }
            gameServerSocket.close();
            chatServerSocket.close();
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
        catch (IOException | InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
