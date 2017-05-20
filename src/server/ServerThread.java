/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import protocol.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server thread that takes care of one particular game
 * @author Jircus
 */
public class ServerThread extends Thread {
    
    private final Server server;
    private final Socket socketOfPlayer1;
    private Socket socketOfPlayer2;
    private String nameOfPlayer1;
    private String nameOfPlayer2;
    private Message message;
    private ObjectOutputStream output1;
    private ObjectOutputStream output2;
    private ObjectInputStream input1;
    private ObjectInputStream input2;
    
    /**
     * Creates new instance
     * @param server
     * @param socket 
     */
    public ServerThread(Server server, Socket socket){
        this.socketOfPlayer1 = socket;
        this.server = server;
        
        start();
    }
    
    /**
     * Runs thread that communicates with client
     */
    @Override
    public void run() {
        try {
            input1 = new ObjectInputStream(socketOfPlayer1.getInputStream());
            nameOfPlayer1 = input1.readObject().toString();
            System.out.println("First player name is " + nameOfPlayer1);
            server.addOutput("First player name is " + nameOfPlayer1);
            output1 = new ObjectOutputStream(socketOfPlayer1.getOutputStream());
            output1.writeObject("X");
            System.out.println("First player symbol is X");
            server.addOutput("First player symbol is X");
            synchronized(this) {
                this.wait();
            }
            input2 = new ObjectInputStream(socketOfPlayer2.getInputStream());
            nameOfPlayer2 = input2.readObject().toString();
            System.out.println("Second player name is " + nameOfPlayer2);
            server.addOutput("Second player name is " + nameOfPlayer2);
            output2 = new ObjectOutputStream(socketOfPlayer2.getOutputStream());
            output2.writeObject("O");
            System.out.println("Second player symbol is O");
            server.addOutput("Second player symbol is O");
            output2.writeObject(null);
            output1.writeObject(true);
            output2.writeObject(false);
            output1.writeObject(nameOfPlayer2);
            output2.writeObject(nameOfPlayer1);
            while (true) {
                message = (Message)input1.readObject();
                System.out.println("Player " + nameOfPlayer1 + " placed symbol in row " + 
                        message.getRowIndex() + " and column " + message.getColIndex());
                server.addOutput("Player " + nameOfPlayer1 + " placed symbol in row " + 
                        message.getRowIndex() + " and column " + message.getColIndex());
                output2.writeObject(message);
                System.out.println("Sending move to player " + nameOfPlayer2);
                server.addOutput("Sending move to player " + nameOfPlayer2);
                if(message.isWon() == true) {
                    System.out.println("Player " + nameOfPlayer1 + "has won");
                    server.addOutput("Player " + nameOfPlayer1 + "has won");
                }
                message = (Message)input2.readObject();
                System.out.println("Player " + nameOfPlayer2 + " placed symbol in row " + 
                        message.getRowIndex() + " and column " + message.getColIndex());
                server.addOutput("Player " + nameOfPlayer2 + " placed symbol in row " + 
                        message.getRowIndex() + " and column " + message.getColIndex());
                output1.writeObject(message);
                System.out.println("Sending move to player " + nameOfPlayer1);
                server.addOutput("Sending move to player " + nameOfPlayer2);
                if(message.isWon() == true) {
                    System.out.println("Player " + nameOfPlayer2 + " won");
                    server.addOutput("Player " + nameOfPlayer2 + " won");
                }
            }
        }
        catch(ClassNotFoundException | InterruptedException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException | NullPointerException e){}
        finally {
            server.removeThread(this);
            server.removeConnection(socketOfPlayer1);
            if(socketOfPlayer2 != null) {
                server.removeConnection(socketOfPlayer2);
            }
        }
    }
    
    /**
     * Sets socket for second player
     * @param socket 
     */
    public void setSocketOfPlayer2(Socket socket) {
        socketOfPlayer2 = socket;
    }
    
    /**
     * Close all streams in order to stop thread 
     */
    public void stopThread() {
        try {
            input1.close();
            output1.close();
            if(socketOfPlayer2 != null) {
                input2.close();
                output2.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Checks whether is first player connected
     * @return 
     */
    public boolean isPlayerConnected() {
        try {
            output1.writeObject(null);
            System.out.println("First player is connected");
            server.addOutput("First player is connected");
            return true;
        } catch (IOException ex) {
            System.out.println("First player is not connected");
            server.addOutput("First player is not connected");
            return false;
        }
    }
}
