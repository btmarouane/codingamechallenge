import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Game game = initGame(in);

        int choice = -1;
        // game loop
        while (true) {
            int day = in.nextInt(); // the game lasts 24 days: 0-23
            int nutrients = in.nextInt(); // the base score you gain from the next COMPLETE action
            int sun = in.nextInt(); // your sun points
            int score = in.nextInt(); // your current score
            int oppSun = in.nextInt(); // opponent's sun points
            int oppScore = in.nextInt(); // opponent's score
            boolean oppIsWaiting = in.nextInt() != 0; // whether your opponent is asleep until the next day
            int numberOfTrees = in.nextInt(); // the current amount of trees
            for (int i = 0; i < numberOfTrees; i++) {
                int cellIndex = in.nextInt(); // location of this tree
                int size = in.nextInt(); // size of this tree: 0-3
                boolean isMine = in.nextInt() != 0; // 1 if this is your tree
                boolean isDormant = in.nextInt() != 0; // 1 if this tree is dormant
                if(isMine){
                    if(choice == -1){
                        choice = cellIndex;
                    }
                    else{
                        if(game.cells[cellIndex].richness > game.cells[choice].richness){
                            choice = cellIndex;
                        }

                    }
                } 
            }
            int numberOfPossibleMoves = in.nextInt();
            if (in.hasNextLine()) {
                in.nextLine();
            }
            for (int i = 0; i < numberOfPossibleMoves; i++) {
                String possibleMove = in.nextLine();
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");


            // GROW cellIdx | SEED sourceIdx targetIdx | COMPLETE cellIdx | WAIT <message>
            if(choice > -1) System.out.printf("COMPLETE %d \n", choice);
            else System.out.println("WAIT");
            choice = -1;
            
        }

    }
    private static Game initGame(Scanner in){
        Game game = new Game();
        int numberOfCells = in.nextInt(); // 37
        for (int i = 0; i < numberOfCells; i++) {
            int index = in.nextInt(); // 0 is the center cell, the next cells spiral outwards
            int richness = in.nextInt(); // 0 if the cell is unusable, 1-3 for usable cells
            game.cells[i].index = index;
            game.cells[i].richness = richness;
            for(int j = 0 ; j < Cell.MAX_NEIGH_CELL ; j++){
                int neighIndex = in.nextInt();
                if(neighIndex != -1){
                    game.cells[i].neighs[j] = game.cells[neighIndex];
                }
                else{
                    game.cells[i].neighs[j] = null;
                }
            }
        }
        return game;
    }
}

class Cell{
    public static final int MAX_NEIGH_CELL = 6;
    public int index;
    public int richness;
    public Cell[] neighs = new Cell[MAX_NEIGH_CELL];

    public Cell(int index, int richness){
        this.index = index;
        this.richness = richness;
    }
}

class Game{
    public int nbRound;
    public int nbPlayers;
    public int nbCells;
    public Cell[] cells = new Cell[37];

    public Game(){
        for(int i = 0 ; i < this.cells.length ; i++){
            this.cells[i] = new Cell(-1, -1);
        }
    }
}

