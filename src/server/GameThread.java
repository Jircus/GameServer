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
 * Game thread that takes care of one particular game
 * @author Jircus
 */
public class GameThread extends Thread {
    
    private final Server server;
    private final Socket gameSocket1;
    private final Socket chatSocket1;
    private Socket gameSocket2;
    private Socket chatSocket2;
    private String name1;
    private String name2;
    private Message message;
    private ObjectOutputStream output1;
    private ObjectOutputStream output2;
    private ObjectInputStream input1;
    private ObjectInputStream input2;
    private ChatThread chatThread1;
    private ChatThread chatThread2;
    
    /**
     * Creates new instance
     * @param server
     * @param gameSocket 
     * @param chatSocket 
     */
    public GameThread(Server server, Socket gameSocket, Socket chatSocket){
        this.gameSocket1 = gameSocket;
        this.chatSocket1 = chatSocket;
        this.server = server;
    }
    
    /**
     * Runs thread that communicates with client
     */
    @Override
    public void run() {
        try {
            input1 = new ObjectInputStream(gameSocket1.getInputStream());
            name1 = input1.readObject().toString();
            server.output("First player name is " + name1);
            output1 = new ObjectOutputStream(gameSocket1.getOutputStream());
            output1.writeObject("X");
            synchronized(this) {
                this.wait();
            }
            input2 = new ObjectInputStream(gameSocket2.getInputStream());
            name2 = input2.readObject().toString();
            server.output("Second player name is " + name2);
            output2 = new ObjectOutputStream(gameSocket2.getOutputStream());
            output2.writeObject("O");
            output2.writeObject(null);
            output1.writeObject(true);
            output2.writeObject(false);
            output1.writeObject(name2);
            output2.writeObject(name1);
            ObjectInputStream par1 = new ObjectInputStream(chatSocket1.getInputStream());
            ObjectInputStream par2 = new ObjectInputStream(chatSocket2.getInputStream());
            ObjectOutputStream par3 = new ObjectOutputStream(chatSocket1.getOutputStream());
            ObjectOutputStream par4 = new ObjectOutputStream(chatSocket2.getOutputStream());
            chatThread1 = new ChatThread(par1, par3, par4, server, name1, 1);
            chatThread2 = new ChatThread(par2, par3, par4, server, name2, 2);
            new Thread(chatThread1).start();
            new Thread(chatThread2).start();
            server.output("Started chat threads");
            server.output("Game is ready");
            while (true) {
                message = (Message)input1.readObject();
                server.output("Player " + name1 + " placed symbol in row " + 
                        message.getRowIndex() + " and column " + message.getColIndex());
                output2.writeObject(message);
                if(message.isWon() == true) {
                    server.output("Player " + name1 + "has won");
                }
                message = (Message)input2.readObject();
                server.output("Player " + name2 + " placed symbol in row " + 
                        message.getRowIndex() + " and column " + message.getColIndex());
                output1.writeObject(message);
                if(message.isWon() == true) {
                    server.output("Player " + name2 + " won");
                }
            }
        }
        catch(ClassNotFoundException | InterruptedException ex) {
            Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException | NullPointerException e){}
        finally {
            server.removeThread(this);
            server.removeConnection(gameSocket1, chatSocket1);
            if(gameSocket2 != null) {
                server.removeConnection(gameSocket2, chatSocket2);
            }
        }
    }
    
    /**
     * Sets sockets for second player
     * @param gameSocket 
     * @param chatSocket 
     */
    public void setSocketOfPlayer2(Socket gameSocket, Socket chatSocket) {
        gameSocket2 = gameSocket;
        chatSocket2 = chatSocket;
    }
    
    /**
     * Close all streams in order to stop thread 
     */
    public void stopThread() {
        try {
            input1.close();
            output1.close();
            if(gameSocket2 != null) {
                input2.close();
                output2.close();
            }
            if(chatThread1 != null && chatThread2 != null) {
                chatThread1.stopThread();
                chatThread2.stopThread();
            }
        } catch (IOException ex) {
            Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Checks whether is first player connected
     * @return 
     */
    public boolean isPlayerConnected() {
        try {
            output1.writeObject(null);
            return true;
        } catch (IOException ex) {
            server.output("First player is no longer connected");
            return false;
        }
    }
}
