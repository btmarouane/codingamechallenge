import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

class Cell {

    int index;
    int richness;
    int[] neighbours;

    public Cell(int index, int richness, int[] neighbours) {
        this.index = index;
        this.richness = richness;
        this.neighbours = neighbours;
    }

    public int getIndex(){
        return index;
    }

    public int getRichness(){
        return richness;
    }

}

class Tree {

    int cellIndex;
    int size;
    boolean isMine;
    boolean isDormant;

    public Tree(int cellIndex, int size, boolean isMine, boolean isDormant) {
        this.cellIndex = cellIndex;
        this.size = size;
        this.isMine = isMine;
        this.isDormant = isDormant;
    }

    public int getCellIndx(){
        return cellIndex;
    }

    public int getSize(){
        return size;
    }

    public boolean isMine(){
        return isMine;
    }
}

class Action {

    static final String WAIT = "WAIT";
    static final String SEED = "SEED";
    static final String GROW = "GROW";
    static final String COMPLETE = "COMPLETE";
    String type;
    Integer targetCellIdx;
    Integer sourceCellIdx;

    public Action(String type, Integer sourceCellIdx, Integer targetCellIdx) {
        this.type = type;
        this.targetCellIdx = targetCellIdx;
        this.sourceCellIdx = sourceCellIdx;
    }

    public Action(String type, Integer targetCellIdx) {
        this(type, null, targetCellIdx);
    }

    public Action(String type) {
        this(type, null, null);
    }

    public Integer getTargetCellIdx(){
        return targetCellIdx;
    }

    public static Action ofWait(){
        return new Action(WAIT);
    }

    static Action parse(String action) {
        String[] parts = action.split(" ");
        switch (parts[0]) {
            case WAIT:
                return new Action(WAIT);
            case SEED:
                return new Action(SEED, Integer.valueOf(parts[1]), Integer.valueOf(parts[2]));
            case GROW:
            case COMPLETE:
            default:
                return new Action(parts[0], Integer.valueOf(parts[1]));
        }
    }

    @Override
    public String toString() {
        if (WAIT.equalsIgnoreCase(type)) {
            return Action.WAIT;
        }
        if (SEED.equalsIgnoreCase(type)) {
            return String.format("%s %d %d", SEED, sourceCellIdx, targetCellIdx);
        }
        return String.format("%s %d", type, targetCellIdx);
    }
}

class Game {

    int day;
    int nutrients;
    List<Cell> board;
    List<Action> possibleActions;
    List<Tree> trees;
    int mySun, opponentSun;
    int myScore, opponentScore;
    boolean opponentIsWaiting;

    public Game() {
        board = new ArrayList<>();
        possibleActions = new ArrayList<>();
        trees = new ArrayList<>();
    }

    Action getNextAction() {
        System.err.println(possibleActions);
        Action action;
        List<Action> completeActions = possibleActions.stream().filter(possibleAction -> possibleAction.type.equalsIgnoreCase("complete")).collect(Collectors.toList());
        if(! completeActions.isEmpty()){
            List<Tree> level1Trees = trees.stream().filter(tree -> tree.isMine() && tree.getSize() == 1).collect(Collectors.toList());
            List<Tree> level0Trees = trees.stream().filter(tree -> tree.isMine() && tree.getSize() == 0).collect(Collectors.toList());
            List<Tree> level2Trees = trees.stream().filter(tree -> tree.isMine() && tree.getSize() == 2).collect(Collectors.toList());
            
            //List<Action> growLevel1Actions = possibleActions.stream().filter(possibleAction -> possibleAction.type.equalsIgnoreCase("grow") && possibleAction.
            if(!level2Trees.isEmpty()){
                return new Action(Action.GROW, level2Trees.get(0).getCellIndx());
            }
            else if(!level1Trees.isEmpty()){
                return new Action(Action.GROW, level1Trees.get(0).getCellIndx());
            }
            else if(! level0Trees.isEmpty()){
                return new Action(Action.GROW, level0Trees.get(0).getCellIndx());
            }
            else{
                action = completeActions.stream().sorted((action1, action2)->{
                    Cell cell1 = board.stream().filter(cell -> cell.getIndex() == action1.getTargetCellIdx()).findFirst().orElse(null);
                    Cell cell2 = board.stream().filter(cell -> cell.getIndex() == action2.getTargetCellIdx()).findFirst().orElse(null);
                    
                    return Integer.compare(cell2.getRichness(), cell1.getRichness());
                    
                }).collect(Collectors.toList()).get(0);
            }
            
        }
        else{
            List<Action> growActions = possibleActions.stream().filter(possibleAction -> possibleAction.type.equalsIgnoreCase("grow")).collect(Collectors.toList());
            if(growActions.isEmpty()){
                //action = Action.ofWait();
                List<Action> seedActions = possibleActions.stream().filter(possibleAction -> possibleAction.type.equalsIgnoreCase("seed")).collect(Collectors.toList());
                
                List<Tree> level2Trees = trees.stream().filter(tree -> tree.getSize() > 1 && tree.isMine()).collect(Collectors.toList());
        
                if(seedActions.isEmpty() || level2Trees.size() < 2){
                    action = Action.ofWait();
                }
                else{
                    List<Tree> seeds = trees.stream().filter(tree -> tree.isMine() && tree.getSize() == 0).collect(Collectors.toList());
                    if(! seeds.isEmpty()){
                        action = Action.ofWait();
                    }
                    else{
                        Action seedInCenter = seedActions.stream().filter(seedAction -> seedAction.getTargetCellIdx() == 0).findFirst().orElse(null);
                        if(seedInCenter != null){
                            action = seedInCenter;
                        }
                        else{
                            List<Action> sortedSeedActions = seedActions.stream().sorted((action1, action2)->{
                                Cell cell1 = board.stream().filter(cell -> cell.getIndex() == action1.getTargetCellIdx()).findFirst().orElse(null);
                                Cell cell2 = board.stream().filter(cell -> cell.getIndex() == action2.getTargetCellIdx()).findFirst().orElse(null);
                                
                                return Integer.compare(cell2.getRichness(), cell1.getRichness());
                            }).collect(Collectors.toList());
                            action = sortedSeedActions.get(0);
                        }
                    }
                }
            }
            else{
                //System.err.println(growActions);
                List<Action> sortedActions = growActions.stream().sorted((action1, action2)->{
                    Cell cell1 = board.stream().filter(cell -> cell.getIndex() == action1.getTargetCellIdx()).findFirst().orElse(null);
                    Cell cell2 = board.stream().filter(cell -> cell.getIndex() == action2.getTargetCellIdx()).findFirst().orElse(null);
                    
                    if(cell1.getRichness() == cell2.getRichness()){
                        Tree tree1 = trees.stream().filter(tree -> tree.getCellIndx() == cell1.getIndex()).findFirst().orElse(null);
                        Tree tree2 = trees.stream().filter(tree -> tree.getCellIndx() == cell2.getIndex()).findFirst().orElse(null);
                        return Integer.compare(tree2.getSize(), tree1.getSize());
                    }
                    else{
                        return Integer.compare(cell2.getRichness(), cell1.getRichness());
                    }

                }).collect(Collectors.toList());
                action = sortedActions.get(0);
            }
            
        }
        return action;
    }

}

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        Game game = new Game();

        int numberOfCells = in.nextInt();
        for (int i = 0; i < numberOfCells; i++) {
            int index = in.nextInt();
            int richness = in.nextInt();
            int neigh0 = in.nextInt();
            int neigh1 = in.nextInt();
            int neigh2 = in.nextInt();
            int neigh3 = in.nextInt();
            int neigh4 = in.nextInt();
            int neigh5 = in.nextInt();
            int[] neighs = new int[]{neigh0, neigh1, neigh2, neigh3, neigh4, neigh5};
            Cell cell = new Cell(index, richness, neighs);
            game.board.add(cell);
        }

        while (true) {
            game.day = in.nextInt();
            game.nutrients = in.nextInt();
            game.mySun = in.nextInt();
            game.myScore = in.nextInt();
            game.opponentSun = in.nextInt();
            game.opponentScore = in.nextInt();
            game.opponentIsWaiting = in.nextInt() != 0;

            game.trees.clear();
            int numberOfTrees = in.nextInt();
            for (int i = 0; i < numberOfTrees; i++) {
                int cellIndex = in.nextInt();
                int size = in.nextInt();
                boolean isMine = in.nextInt() != 0;
                boolean isDormant = in.nextInt() != 0;
                Tree tree = new Tree(cellIndex, size, isMine, isDormant);
                game.trees.add(tree);
            }

            game.possibleActions.clear();
            int numberOfPossibleActions = in.nextInt();
            in.nextLine();
            for (int i = 0; i < numberOfPossibleActions; i++) {
                String possibleAction = in.nextLine();
                game.possibleActions.add(Action.parse(possibleAction));
            }

            Action action = game.getNextAction();
            System.out.println(action);
        }
    }
}
