/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 10123_000
 */
public class Cell {  
    public enum PrevcellType { Left, Above, Diagonal };
    
    private Cell prevCell;
    private List<Cell> prevCells = new ArrayList<Cell>();
    private int row;
    private int col;
    private int score;
    private PrevcellType PCType;
    
    public Cell getPrevCell() {
        return prevCell;
    }

    public void setPrevCell(Cell prevCell) {
        this.prevCell = prevCell;
    }

    public List<Cell> getPrevCells() {
        return prevCells;
    }

    public void setPrevCells(List<Cell> prevCells) {
        this.prevCells = prevCells;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public PrevcellType getPCType() {
        return PCType;
    }

    public void setPCType(PrevcellType PCType) {
        this.PCType = PCType;
    }
    
    public Cell(){
    }
    
    public Cell(int row, int Col){
        this.col = Col;
        this.row = row;
    }
    
    public Cell(int row, int Col, int sco){
        this.col = Col;
        this.row = row;
        this.score = sco;
    }
    
    public Cell(int row, int Col, int sco, Cell Prev){
        this.col = Col;
        this.row = row;
        this.score = sco;
        this.prevCell = Prev;
    }
    
    public Cell(int row, int Col, int sco, Cell Prev, PrevcellType PType){
        this.col = Col;
        this.row = row;
        this.score = sco;
        this.prevCell = Prev;
        this.PCType = PType;
    }
    
    
}
