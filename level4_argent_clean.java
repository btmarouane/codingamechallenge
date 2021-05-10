import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Scanner;
import java.util.Comparator;
import java.util.Arrays;

class Game {

    public int day;
    public int nutrients;
    public Board board;
    public PossibleActions possibleActions;
    public Trees trees;
    public int mySun, opponentSun;
    public int myScore, opponentScore;
    public boolean opponentIsWaiting;

    private int successiveCompletes = 2;

    public Game() {
        board = new Board();
        possibleActions = new PossibleActions();
        trees = new Trees();
    }

    public Action getNextAction() {
        //System.err.println(possibleActions.getPossibleActions());
        System.err.println(nutrients);
        Action action;
        List<Action> growActions = possibleActions.of(Action.GROW);
        List<Action> seedActions = possibleActions.of(Action.SEED);
        List<Action> completeActions = possibleActions.of(Action.COMPLETE);

        
        
        if(! completeActions.isEmpty() && nutrients < 19 && successiveCompletes != 0){
            successiveCompletes--;
            return possibleActions.complete(board);
        }

        if (!growActions.isEmpty()) {
            List<Tree> treesOfSizeGreaterThan0 = trees.mineOfSizeGreaterThan(0);
            if(!treesOfSizeGreaterThan0.isEmpty()){
                try{
                    action = possibleActions.grow(new Trees(treesOfSizeGreaterThan0), board);//grow size > 0
                }catch (UnsupportedOperationException unsupportedOperationException){
                    action = possibleActions.grow(trees, board);
                }
            }
            else{
                action = possibleActions.grow(trees, board);
            }
        }
        else {
            if (!seedActions.isEmpty() && trees.mineOfSize(0).isEmpty()) {
                action = possibleActions.seed(board);
            }
            else {

                if(completeActions.isEmpty()){
                    action = new WaitAction();
                }
                else{
                    action = possibleActions.complete(board);
                }
            }
        }
        if(successiveCompletes == 0)
            successiveCompletes = 2;
        return action;
    }
}
class CompleteAction extends Action {

    public CompleteAction(Integer targetCellIdx) {
        super(targetCellIdx);
    }

    @Override
    public String getType() {
        return "COMPLETE";
    }

    @Override
    public String toString() {
        return String.format("%s %d", COMPLETE, targetCellIdx);
    }
}
class GrowAction extends Action {

    public GrowAction(Integer targetCellIdx) {
        super(targetCellIdx);
    }

    @Override
    public String getType() {
        return "GROW";
    }

    @Override
    public String toString() {
        return String.format("%s %d", GROW, targetCellIdx);
    }
}
class SeedAction extends Action {

    public SeedAction(Integer sourceCellIdx, Integer targetCellIdx) {
        super(sourceCellIdx,targetCellIdx);
    }

    @Override
    public String getType() {
        return "SEED";
    }

    @Override
    public String toString() {
        return String.format("%s %d %d", SEED, sourceCellIdx, targetCellIdx);
    }
}
class WaitAction extends Action {

    public WaitAction() {
        super();
    }

    @Override
    public String getType() {
        return "WAIT";
    }

    @Override
    public String toString() {
        return Action.WAIT;
    }
}
class Board {

    private List<Cell> cells;

    public Board() {
        this.cells = new ArrayList<>();
    }

    public void addCell(Cell cell){
        cells.add(cell);
    }

    public Cell findByIndex(int cellIdx){
        return cells.stream().filter(cell -> cell.getIndex() == cellIdx).collect(Collectors.toList()).stream().findFirst().orElse(null);
    }

    public void clear() {
        cells.clear();
    }

    public List<Cell> getCells(){
        return cells;
    }
}
class PossibleActions {

    private List<Action> possibleActions;
    private ActionComparator actionComparator;

    public PossibleActions() {
        this.possibleActions = new ArrayList<>();
    }

    public PossibleActions(List<Action> possibleActions) {
        this.possibleActions = possibleActions;
    }

    public void addPossibleAction(Action action){
        possibleActions.add(action);
    }

    public List<Action> of(String type){
        return possibleActions.stream().filter(possibleAction -> possibleAction.getType().equalsIgnoreCase(type)).collect(Collectors.toList());
    }

    public List<Action> getPossibleActions(){
        return possibleActions;
    }

    public void clear() {
        possibleActions.clear();
    }

    private PossibleActions filterPossibleActionsByTrees(Trees trees){
        int[] idx = trees.getTrees().stream().map(tree -> tree.getCellIndx()).mapToInt(v->v).toArray();
        List<Action> actions = possibleActions.stream().
                filter(possibleAction -> Arrays.stream(idx).anyMatch(i -> i == possibleAction.getTargetCellIdx()))
                .collect(Collectors.toList());
        if(actions == null || actions.isEmpty())
            throw new UnsupportedOperationException();
        return new PossibleActions(actions);
    }

    //Grow in the optimal position
    public Action grow(Trees trees, Board board){
        ActionComparator actionComparator = new GrowActionComparator(board, trees);
        PossibleActions filterPossibleActionsByTrees = filterPossibleActionsByTrees(trees);
        List<Action> growActions = filterPossibleActionsByTrees.of("grow");
        if(growActions == null || growActions.isEmpty())
            throw new UnsupportedOperationException();
        List<Action> sortedGrowActions = growActions.stream().sorted(actionComparator).collect(Collectors.toList());
        return sortedGrowActions.get(0);
    }

    //Seed in the optimal position
    public Action seed(Board board){
        ActionComparator actionComparator = new CellRichnessActionComparator(board);
        List<Action> seedActions = of(Action.SEED);
        List<Action> sortedSeedActions = seedActions.stream().sorted(actionComparator).collect(Collectors.toList());
        return sortedSeedActions.get(0);
    }

    public Action complete(Board board){
        ActionComparator actionComparator = new CellRichnessActionComparator(board);
        List<Action> completeActions = of(Action.COMPLETE);
        List<Action> sortedCompleteActions = completeActions.stream().sorted(actionComparator).collect(Collectors.toList());
        return sortedCompleteActions.get(0);
    }

}
class Trees {

    private List<Tree> trees;

    public Trees() {
        this.trees = new ArrayList<>();
    }

    public Trees(List<Tree> trees){
        this.trees = trees;
    }

    public void addTree(Tree tree){
        trees.add(tree);
    }

    public List<Tree> getTrees(){
        return trees;
    }

    public List<Tree> mineOfSize(int size){
        return trees.stream().filter(tree -> tree.getSize() == size && tree.isMine()).collect(Collectors.toList());
    }

    public List<Tree> mineOfSizeGreaterThan(int size){
        return trees.stream().filter(tree -> tree.getSize() > size && tree.isMine()).collect(Collectors.toList());
    }

    public Tree findByCellIndex(int index){
        return trees.stream().filter(tree -> tree.getCellIndx() == index).findFirst().orElse(null);
    }

    public void clear() {
        trees.clear();
    }
}
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
abstract class Action {

    protected int targetCellIdx;
    protected int sourceCellIdx;

    public abstract String getType();

    public static final String WAIT = "WAIT";
    public static final String SEED = "SEED";
    public static final String GROW = "GROW";
    public static final String COMPLETE = "COMPLETE";


    public Action() {
    }

    public Action(int targetCellIdx) {
        this.targetCellIdx = targetCellIdx;
    }

    public Action(int sourceCellIdx, int targetCellIdx) {
        this.sourceCellIdx = sourceCellIdx;
        this.targetCellIdx = targetCellIdx;
    }


    public Integer getTargetCellIdx(){
        return targetCellIdx;
    }

    public static Action ofWait(){
        return new WaitAction();
    }

    public static Action parse(String action) {
        String[] parts = action.split(" ");
        switch (parts[0]) {
            case WAIT:
                return new WaitAction();
            case SEED:
                return new SeedAction(Integer.valueOf(parts[1]), Integer.valueOf(parts[2]));
            case GROW:
                return new GrowAction(Integer.valueOf(parts[1]));
            case COMPLETE:
                return new CompleteAction(Integer.valueOf(parts[1]));
            default:
                throw new UnsupportedOperationException();
        }
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
            game.board.addCell(cell);
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
                game.trees.addTree(tree);
            }

            game.possibleActions.clear();
            int numberOfPossibleActions = in.nextInt();
            in.nextLine();
            for (int i = 0; i < numberOfPossibleActions; i++) {
                String possibleAction = in.nextLine();
                game.possibleActions.addPossibleAction(Action.parse(possibleAction));
            }

            Action action = game.getNextAction();
            System.out.println(action);
        }
    }
}
abstract class ActionComparator implements Comparator<Action> {

    protected Board board;
    public ActionComparator(Board board){
        this.board = board;
    }
}
class CellRichnessActionComparator extends ActionComparator{

    private Trees trees;

    public CellRichnessActionComparator(Board board){
        super(board);
    }


    @Override
    public int compare(Action action1, Action action2) {
        Cell cell1 = board.findByIndex(action1.getTargetCellIdx());
        Cell cell2 = board.findByIndex(action2.getTargetCellIdx());
        return Integer.compare(cell2.getRichness(), cell1.getRichness());
    }
}
class GrowActionComparator extends ActionComparator{

    private Trees trees;

    public GrowActionComparator(Board board){
        super(board);
    }

    public GrowActionComparator(Board board, Trees trees) {
        super(board);
        this.trees = trees;
    }

    @Override
    public int compare(Action action1, Action action2) {
        Cell cell1 = board.findByIndex(action1.getTargetCellIdx());
        Cell cell2 = board.findByIndex(action2.getTargetCellIdx());
        System.err.println("cell1 --> "+cell1.getIndex());
        System.err.println("cell2 --> "+cell2.getIndex());
        if(cell1.getRichness() == cell2.getRichness()){
            Tree tree1 = trees.findByCellIndex(cell1.getIndex());
            Tree tree2 = trees.findByCellIndex(cell2.getIndex());
            System.err.println("tree1 --> "+tree1.getCellIndx());
            System.err.println("tree2 --> "+tree2.getCellIndx());
            return Integer.compare(tree2.getSize(), tree1.getSize());
        }
        else{
            return Integer.compare(cell2.getRichness(), cell1.getRichness());
        }
    }
}
