/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import com.caucho.quercus.statement.BlockStatement;
import com.caucho.quercus.statement.ForStatement;
import com.caucho.quercus.statement.IfStatement;
import com.caucho.quercus.statement.Statement;
import com.caucho.quercus.statement.TryStatement;
import com.caucho.quercus.statement.WhileStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author 10123_000
 */
public class StatementAlignment {
    // point for similar statement
    private static int similar = 2;
    // point for semi-similar statement (has same type but not equal)
    private static int semiSimilar = 1;
    // point for non similar statement
    private static int nonSimilar = -1;
    // point for gap statement
    private static int gap = -2;
    
    private static Cell[][] Intialization_Step(ArrayList<ArrayList<Statement>> Seqs, int index) {
        int M = Seqs.get(index).size();
        int N = Seqs.get(index + 1).size();

        Cell[][] Matrix = new Cell[N][M];

        for (int i = 0; i < M; i++) {
            Matrix[0][i] = new Cell(0, i, i * gap);
        }

        for (int i = 0; i < N; i++) {
            Matrix[i][0] = new Cell(i, 0, i * gap);
        }

        for (int j = 1; j < N; j++) {
            for (int i = 1; i < M; i++) {
                Matrix[j][i] = Get_Max(i, j, Seqs.get(index), Seqs.get(index + 1), Matrix);
            }
        }

        return Matrix;
    }

    private static Cell Get_Max(int i, int j, List<Statement> Seq1, List<Statement> Seq2, Cell[][] Matrix) {
        Cell Temp = new Cell();
        int SimPoint;

        if (Seq1.get(i).getClass() == Seq2.get(j).getClass())
        {
            if (Seq1.get(i).equal(Seq2.get(j)))
                SimPoint = similar;
            else SimPoint = semiSimilar;
        }
        else SimPoint = nonSimilar;

        int M1, M2, M3;
        M1 = Matrix[j - 1][i - 1].getScore() + SimPoint;
        M2 = Matrix[j][i - 1].getScore() + gap;
        M3 = Matrix[j - 1][i].getScore() + gap;

        int max = M1 >= M2 ? M1 : M2;
        int Mmax = M3 >= max ? M3 : max;

        if (Mmax == M1) {
            Temp = new Cell(j, i, M1, Matrix[j - 1][i - 1], Cell.PrevcellType.Diagonal);
        } else if (Mmax == M2) {
            Temp = new Cell(j, i, M2, Matrix[j][i - 1], Cell.PrevcellType.Left);
        } else if (Mmax == M3) {
            Temp = new Cell(j, i, M3, Matrix[j - 1][i], Cell.PrevcellType.Above);
        }
        
        return Temp;
    }

    private static ArrayList<ArrayList<Statement>> Traceback_Step(Cell[][] Matrix, ArrayList<ArrayList<Statement>> Sqs, int index) {

        ArrayList<ArrayList<Statement>> Seqs = new ArrayList<ArrayList<Statement>>();
        for (int i = 0; i <= index + 1; i++) {
            Seqs.add(new ArrayList<Statement>());
        }

        Cell CurrentCell = Matrix[Sqs.get(index + 1).size() - 1][Sqs.get(index).size() - 1];

        while (CurrentCell.getPrevCell() != null) {
            if (CurrentCell.getPCType() == Cell.PrevcellType.Diagonal) {

                for (int i = 0; i < index; i++) {
                    Seqs.get(i).add(Sqs.get(i).get(CurrentCell.getCol()));
                }

                Seqs.get(index).add(Sqs.get(index).get(CurrentCell.getCol()));
                Seqs.get(index + 1).add(Sqs.get(index + 1).get(CurrentCell.getRow()));
            }
            if (CurrentCell.getPCType() == Cell.PrevcellType.Left) {

                for (int i = 0; i < index; i++) {
                    Seqs.get(i).add(Sqs.get(i).get(CurrentCell.getCol()));
                }

                Seqs.get(index).add(Sqs.get(index).get(CurrentCell.getCol()));
                Seqs.get(index + 1).add(new GapStatement());
            }
            if (CurrentCell.getPCType() == Cell.PrevcellType.Above) {

                for (int i = 0; i < index; i++) {
                    Seqs.get(i).add(new GapStatement());
                }

                Seqs.get(index).add(new GapStatement());
                Seqs.get(index + 1).add(Sqs.get(index + 1).get(CurrentCell.getRow()));
            }

            CurrentCell = CurrentCell.getPrevCell();
        }

        for (int i = CurrentCell.getRow() - 1; i >= 0; i--) {

            for (int j = 0; j < index; j++) {
                Seqs.get(j).add(new GapStatement());
            }

            Seqs.get(index).add(new GapStatement());
            Seqs.get(index + 1).add(Sqs.get(index + 1).get(i + 1));
        }

        for (int i = CurrentCell.getCol() - 1; i >= 0; i--) {

            for (int j = 0; j < index; j++) {
                Seqs.get(j).add(Sqs.get(j).get(i + 1));
            }

            Seqs.get(index).add(Sqs.get(index).get(i + 1));
            Seqs.get(index + 1).add(new GapStatement());
        }

        for (int i = 0; i <= index + 1; i++) {
            Collections.reverse(Seqs.get(i));
        }

        for (int i = index + 2; i < Sqs.size(); i++) {
            Seqs.add(Sqs.get(i));
        }

        return Seqs;
    }
    
    /**
     * Align statements by modifying Needleman-Wunsch algorithm 
     * @param input list of statements to be aligned
     * @return aligned statements
     */
    public static ArrayList<ArrayList<Statement>> alignImpl(ArrayList<ArrayList<Statement>> input) {
        
    	// Align statements in input array
    	for (int i = 0; i < input.size() - 1; i++) {
            for (int j = 0; j <= i + 1; j++) {
                input.get(j).add(0, new GapStatement());
            }
            Cell[][] Matrix = StatementAlignment.Intialization_Step(input, i);
            input = StatementAlignment.Traceback_Step(Matrix, input, i);
        }
        
    	// Align child statements of each statement in input array
        int nStm = input.get(0).size();
        int nBranch = input.size();
        for (int i=0; i<nStm; i++){
            
            // Group similar statement together
            List<Integer> curColIndex = new ArrayList<Integer>();
            for (int j = 0; j < nBranch; j++) {
                curColIndex.add(j);
            }

            List<List<Integer>> stmGroupsIndex = new ArrayList<List<Integer>>();
            while (!curColIndex.isEmpty()) {
                List<Integer> curGroupIndex = new ArrayList<Integer>();
                Statement curStm = input.get(curColIndex.get(curColIndex.size() - 1)).get(i);

                for (int j = curColIndex.size() - 1; j >= 0; j--) {
                    if (input.get(curColIndex.get(j)).get(i).equal(curStm)) {
                        curGroupIndex.add(curColIndex.get(j));
                        curColIndex.remove(j);
                    }
                }
                stmGroupsIndex.add(curGroupIndex);
            }
            
            // Align each list of statements in groups
            for(List<Integer> stmIndex : stmGroupsIndex){
                if (stmIndex.size() < 2)
                    continue;
                
                ArrayList<Statement> subStms = new ArrayList<Statement>();
                for(Integer index : stmIndex)
                    subStms.add(input.get(index).get(i));
                
                subStms = input.get(stmIndex.get(0)).get(i).alignStatement(subStms);
                for(int j=0; j<subStms.size(); j++)
                    input.get(stmIndex.get(j)).set(i, subStms.get(j));
            }
        }

        return input;
    }
}
