import java.util.*;

public class TetrisSolver{

    private int height;
    private int width;
    private String checkEmptyRow;
    private String checkFullRow;
    private Map<Integer, Tetromino> puzzlePieces;
    private char[][] grid;

    TetrisSolver(int width, int height) throws IllegalArgumentException{
        if(width<1 || height<1){
            throw new IllegalArgumentException("Illegal dimension (height or width less than 1)!");
        }
        this.height = height;
        this.width = width;
        grid = new char[height][width];
        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                grid[row][col] = ' ';
            }
        }
        checkEmptyRow = "";
        checkFullRow = "";
        for(int i=0; i<width; i++){
            checkEmptyRow += ' ';
            checkFullRow += '*';
        }
        puzzlePieces = new HashMap<>();
    }


    String showPuzzle(){
        String puzzleStr = "";
        for(int row=0; row<height; row++){
            String rowStr = "";
            for(int col=width-1; col>=0; col--){
                rowStr += grid[row][col];
            }
            puzzleStr += rowStr + "\n";
        }
        //https://stackoverflow.com/questions/7569335/reverse-a-string-in-java
        puzzleStr = new StringBuilder(puzzleStr).reverse().toString();
        //https://stackoverflow.com/questions/4503656/java-removing-first-character-of-a-string
        puzzleStr = puzzleStr.substring(1);
        //System.out.println(gridPenalty(grid));
        return puzzleStr;
    }


    void addPuzzleRow(String nextRow) throws IllegalArgumentException{
        if(nextRow==null){
            throw new IllegalArgumentException("Next row is null!");
        }
        if(nextRow.length()!=width){
            throw new IllegalArgumentException("Next row does not match grid width!");
        }
        if(nextRow.equals(checkEmptyRow)){
            throw new IllegalArgumentException("Next row is empty row (invalid)!");
        }
        if(nextRow.equals(checkFullRow)){
            throw new IllegalArgumentException("Next row is full row (invalid)!");
        }
        int nextUsableRowInGrid = topRowInGrid(grid);
        if(nextUsableRowInGrid>=height){
            throw new IllegalArgumentException("Grid's top row already full (cannot add another row to current configuration!");
        }
        for(int col=0; col<width; col++){
            if(nextRow.charAt(col)!=' '){
                grid[nextUsableRowInGrid][col] = '*';
            }
        }
    }


    private int topRowInGrid(char[][] currGrid){
        for(int row=height-1; row>=0; row--){
            for(int col=0; col<width; col++){
                if(currGrid[row][col]!=' '){
                    return row + 1;
                }
            }
        }
        return 0;
    }


    int addPuzzlePiece(String piece, int relativeFrequency) throws IllegalArgumentException{
        if(piece==null){
            throw new IllegalArgumentException("Piece is null!");
        }
        if(piece.trim().equals("")){
            throw new IllegalArgumentException("Piece is empty string!");
        }
        if(relativeFrequency<0){
            //relative frequency of 0 would be valid because it means the piece is stored
            //in the Tetris game but never used in play. It can, however, be used in debugging.
            //in the case of this program, relative freq. of 0 means we can place the piece using placePiece,
            //but it will not have any effect on lookahead calculations since its (val*freq)/totalFreq will always be 0
            throw new IllegalArgumentException("Relative frequency is negative (invalid)!");
        }
        String[] pieceRows = piece.split("\n");
        Tetromino newTetromino;
        try{
            newTetromino = new Tetromino(pieceRows, puzzlePieces.size(), relativeFrequency, width, height);
        }
        catch(IllegalArgumentException e){
            throw e;
        }
        if(isDuplicateTetromino(newTetromino)){
            throw new IllegalArgumentException("Piece is a duplicate (identical piece already stored)!");
        }
        else{
            puzzlePieces.put(puzzlePieces.size(), newTetromino);
        }
        int pieceId = newTetromino.getPieceId();
        return pieceId;
    }


    private boolean isDuplicateTetromino(Tetromino newPiece){
        for(Map.Entry<Integer, Tetromino> existingTetromino: puzzlePieces.entrySet()){
            List<int[][]> existingTetrominoOrientations = existingTetromino.getValue().getPieceOrientations();
            for(int[][] existingOrientation: existingTetrominoOrientations){
                for(int[][] newPieceOrientation: newPiece.getPieceOrientations()){
                    //https://stackoverflow.com/questions/2721033/java-arrays-equals-returns-false-for-two-dimensional-arrays
                    if(Arrays.deepEquals(newPieceOrientation, existingOrientation)){
                        return true;
                    }
                }

            }
        }
        return false;
    }


    int placePiece(int pieceId, int lookahead) throws IllegalArgumentException{
        if(!puzzlePieces.containsKey(pieceId)){
            throw new IllegalArgumentException("PieceId does not exist!");
        }
        if(lookahead<0){
            throw new IllegalArgumentException("Lookahead is negative (invalid)!");
        }
        List<PiecePlacement> initialPlacements = new ArrayList<>();
        Tetromino firstPiece = puzzlePieces.get(pieceId);
        for(int[][] pieceOrientation: firstPiece.getPieceOrientations()){
            initialPlacements.addAll(fitPiece(pieceOrientation, grid, pieceId));
        }
        if(initialPlacements.isEmpty()){
            throw new IllegalArgumentException("GAME OVER: Cannot fit piece placed into current grid configuration!");
        }
        if(lookahead==0){
            int bestValue = initialPlacements.get(0).getPlacementValue() - gridPenalty(initialPlacements.get(0).getGridAfterPlacement());
            char[][] bestGrid = initialPlacements.get(0).getGridAfterPlacement();
            for(int i=0; i<initialPlacements.size(); i++){
                int placementValue = initialPlacements.get(i).getPlacementValue() - gridPenalty(initialPlacements.get(i).getGridAfterPlacement());
                if(placementValue>bestValue){
                    bestValue = placementValue;
                    bestGrid = initialPlacements.get(i).getGridAfterPlacement();
                }
            }
            grid = bestGrid;
            return bestValue;
        }
        else{
            List<List<PiecePlacement>> lookaheadCombinations = new ArrayList<>();
            for(int i=0; i<initialPlacements.size(); i++){
                List<PiecePlacement> firstPlacement = new ArrayList<>();
                firstPlacement.add(initialPlacements.get(i));
                lookaheadCombinations.add(firstPlacement);
            }
            int counter = 1;
            while(counter<=lookahead){
                List<List<PiecePlacement>> nextPlacementCombinations = feedForward(lookaheadCombinations, counter);
                if(nextPlacementCombinations.isEmpty()){
                    break;
                }
                lookaheadCombinations = nextPlacementCombinations;
                counter++;
            }
            List<PiecePlacement> bestLookaheadCombination = findBestLookahead(lookaheadCombinations);
            if(bestLookaheadCombination==null){
                return placePiece(pieceId, 0);
            }
            grid = bestLookaheadCombination.get(0).getGridAfterPlacement();
            return bestLookaheadCombination.get(0).getPlacementValue();
        }
    }


    private List<PiecePlacement> fitPiece(int[][] piece, char[][] currGrid, int pieceId){
        int pieceHeight = piece.length;
        int pieceWidth = piece[0].length;
        List<PiecePlacement> possiblePlacements = new ArrayList<>();
        //check all valid possible placements in current grid config
        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                if(pieceHeight>height-row || pieceWidth>width-col){
                    continue;
                }
                boolean overlapsExistingBlocks = false;
                for(int y=0; y<pieceHeight; y++){
                    if(overlapsExistingBlocks){
                        break;
                    }
                    for(int x=0; x<pieceWidth; x++){
                        if(piece[y][x]==1){
                            if(currGrid[row+y][col+x]=='*'){
                                overlapsExistingBlocks = true;
                                break;
                            }
                        }
                    }
                }
                if(overlapsExistingBlocks){
                    continue;
                }
                char[][] tempGrid = deepCopyGrid(currGrid);
                if(isPlacementValid(piece, row, col, tempGrid)){
                    char[][] gridAfterPlacement = deepCopyGrid(currGrid);
                    PiecePlacement placement = findPlacementValue(piece, row, col, gridAfterPlacement, pieceId);
                    possiblePlacements.add(placement);
                }
            }
        }
        return possiblePlacements;
    }


    private List<List<PiecePlacement>> feedForward(List<List<PiecePlacement>> lookaheadCombinations, int counter){
        List<List<PiecePlacement>> nextPlacementCombinations = new ArrayList<>();
        for(int i=0; i<lookaheadCombinations.size(); i++){
            for(int j=0; j<puzzlePieces.size(); j++){
                Tetromino nextPiece = puzzlePieces.get(j);
                for(int k=0; k<nextPiece.getPieceOrientations().size(); k++){
                    char[][] lastGrid = deepCopyGrid(lookaheadCombinations.get(i).get(counter-1).getGridAfterPlacement());
                    List<PiecePlacement> existingCombination = lookaheadCombinations.get(i);
                    List<PiecePlacement> nextPossiblePlacements = fitPiece(nextPiece.getPieceOrientations().get(k), lastGrid, nextPiece.getPieceId());
                    if(nextPossiblePlacements.isEmpty()){
                        continue;
                    }
                    for(int f1=0; f1<nextPossiblePlacements.size(); f1++){
                        List<PiecePlacement> newCombination = new ArrayList<>();
                        newCombination.addAll(existingCombination);
                        newCombination.add(nextPossiblePlacements.get(f1));
                        nextPlacementCombinations.add(newCombination);
                    }
                }
            }
        }
        return nextPlacementCombinations;
    }



    private List<PiecePlacement> findBestLookahead(List<List<PiecePlacement>> lookaheadCombinations){
        Map<List<PiecePlacement>, Double> lookaheadValues = new HashMap<>();
        double sumOfPieceFrequencies = 0;
        for(int i=0; i<puzzlePieces.size(); i++){
            sumOfPieceFrequencies += puzzlePieces.get(i).getRelativeFrequency();
        }
        double bestValue = 0;
        List<PiecePlacement> bestCombination = null;
        for(List<PiecePlacement> placementCombination: lookaheadCombinations){
            double totalValue = 0;
            for(int i=0; i<placementCombination.size(); i++){
                if(i==0){
                    totalValue += placementCombination.get(i).getPlacementValue();
                }
                else{
                    double pieceFrequency = puzzlePieces.get(placementCombination.get(i).getPieceId()).getRelativeFrequency();
                    totalValue += (pieceFrequency * placementCombination.get(i).getPlacementValue()) / sumOfPieceFrequencies;
                }
            }
            totalValue = totalValue - gridPenalty(placementCombination.get(placementCombination.size()-1).getGridAfterPlacement());
            bestValue = totalValue;
            lookaheadValues.put(placementCombination, totalValue);
        }
        for(Map.Entry<List<PiecePlacement>, Double> entry: lookaheadValues.entrySet()){
            if(entry.getValue()>bestValue){
                bestValue = entry.getValue();
                bestCombination = entry.getKey();
            }
        }
        return bestCombination;
    }


    private char[][] deepCopyGrid(char[][] gridToCopy){
        char[][] copyGrid = new char[gridToCopy.length][gridToCopy[0].length];
        for(int row=0; row<gridToCopy.length; row++){
            for(int col=0; col<gridToCopy[0].length; col++){
                copyGrid[row][col] = gridToCopy[row][col];
            }
        }
        return copyGrid;
    }


    private boolean isPlacementValid(int[][] piece, int startingY, int startingX, char[][] currGrid){
        boolean hangingBlocks = true;
        for(int y=0; y<piece.length; y++){
            for(int x=0; x<piece[0].length; x++){
                if(startingY + y == nextRowInExistingColumn(currGrid, startingX + x)){
                    hangingBlocks = false;
                }
            }
        }
        if(hangingBlocks){
            return false;
        }
        char[][] initialGrid = deepCopyGrid(currGrid);
        for(int y=0; y<piece.length; y++){
            for(int x=0; x<piece[0].length; x++){
                if(piece[y][x] == 1){
                    currGrid[startingY + y][startingX + x] = '*';
                }
            }
        }
        if(isPlacementFloating(initialGrid, currGrid)){
            return false;
        }
        return true;
    }


    //https://www.geeksforgeeks.org/number-of-connected-components-in-a-2-d-matrix-of-strings/
    //https://www.javatpoint.com/find-number-of-island-in-java
    private boolean isPlacementFloating(char[][] initialGrid, char[][] gridAfterPlacement){
        int initialComponents = 0;
        int currentComponents = 0;
        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                if(gridAfterPlacement[row][col] == '*'){
                    traverseGrid(gridAfterPlacement, row, col);
                    currentComponents++;
                }
                if(initialGrid[row][col] == '*'){
                    traverseGrid(initialGrid, row, col);
                    initialComponents++;
                }
            }
        }
        if(currentComponents>initialComponents){
            return true;
        }
        return false;
    }


    //https://www.geeksforgeeks.org/number-of-connected-components-in-a-2-d-matrix-of-strings/
    //https://www.javatpoint.com/find-number-of-island-in-java
    private void traverseGrid(char[][] gridToTraverse, int row, int col){
        if(row<0 || col<0 || row>=height || col>=width || gridToTraverse[row][col]!='*'){
            return;
        }
        gridToTraverse[row][col] = ' ';
        traverseGrid(gridToTraverse, row+1, col);
        traverseGrid(gridToTraverse, row-1, col);
        traverseGrid(gridToTraverse, row, col+1);
        traverseGrid(gridToTraverse, row, col-1);
    }


    private int nextRowInExistingColumn(char[][] currGrid, int col){
        for(int row=height-1; row>=0; row--){
            if(grid[row][col]!=' '){
                return row + 1;
            }
        }
        return 0;
    }


    private PiecePlacement findPlacementValue(int[][] piece, int startingY, int startingX, char[][] currGrid, int pieceId){
        for(int y=0; y<piece.length; y++){
            for(int x=0; x<piece[0].length; x++){
                if(piece[y][x]==1){
                    currGrid[startingY + y][startingX + x] = '*';
                }
            }
        }
        int numberOfRowsCleared = clearFullRows(currGrid);
        int pointsEarned = 0;
        if(numberOfRowsCleared>0){
            for(int i=1; i<=numberOfRowsCleared; i++){
                if(i==1){
                    pointsEarned = 50;
                    continue;
                }
                pointsEarned = pointsEarned * 2;
            }
        }
        int placementValue = pointsEarned;
        PiecePlacement placement = new PiecePlacement(startingY, startingX, piece, pieceId, placementValue, currGrid);
        return placement;
    }


    private int clearFullRows(char[][] gridAfterPlacement){
        int rowsCleared = 0;
        while(findFullRow(gridAfterPlacement)!=-1){
            rowsCleared++;
            int fullRow = findFullRow(gridAfterPlacement);
            for(int col=0; col<width; col++){
                gridAfterPlacement[fullRow][col] = ' ';
            }
            if(fullRow==height-1){   //row filled was the top row (no need to shift other rows down)
                continue;
            }
            for(int row=fullRow; row<height; row++){
                for(int col=0; col<width; col++){
                    if(row!=height-1){
                        gridAfterPlacement[row][col] = gridAfterPlacement[row+1][col];
                    }
                    else{
                        gridAfterPlacement[row][col] = ' ';
                    }
                }
            }
        }
        return rowsCleared;
    }


    private int findFullRow(char[][] gridAfterPlacement){
        for(int row=0; row<height; row++){
            String rowStr = "";
            for(int col=0; col<width; col++){
                rowStr += gridAfterPlacement[row][col];
            }
            if(rowStr.equals(checkFullRow)){
                return row;
            }
        }
        return -1;
    }


    private int gridPenalty(char[][] currGrid){
        int topRow = topRowInGrid(currGrid);
        int penalty = 0;
        boolean gridIsEven = false;
        if(width%2==0){
            gridIsEven = true;
        }
        int center = width/2;
        for(int row=0; row<=topRow; row++){
            for(int col=0; col<width; col++){
                if(currGrid[row][col]==' '){
                    int topRowInCol = nextRowInExistingColumn(currGrid, col);
                    if(row<topRowInCol){
                        penalty += (topRowInCol - row) * 7;
                    }
                }
                else{
                    penalty += row * 10;
                    if(gridIsEven){
                        if(col<center-1){
                            penalty += center - col -1;
                        }
                        else if(col>center){
                            penalty += col - center;
                        }
                    }
                    else{
                        if(col<center){
                            penalty += center - col;
                        }
                        else if(col>center){
                            penalty += col - center;
                        }
                    }
                }
            }
        }
        return penalty;
    }
}