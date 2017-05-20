/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocol;

import java.io.Serializable;

/**
 * Class that represents object for communication between client and server
 * @author Jircus
 */
public class Message implements Serializable {
    
    private int rowIndex;
    private int colIndex;
    private boolean won;

    /**
     * Creates new instance
     * @param rowIndex
     * @param colIndex
     * @param won 
     */
    public Message(int rowIndex, int colIndex, boolean won) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.won = won;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public boolean isWon() {
        return won;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public void setWon(boolean isWon) {
        this.won = isWon;
    }
    
}
