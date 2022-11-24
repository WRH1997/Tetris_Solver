import java.util.*;

public class TetrisSolver{

    private int height;   //grid height
    private int width;   //grid width
    private String checkEmptyRow;   //string of width length spaces (i.e., "   ") to check if rows are empty
    private String checkFullRow;   //string of width length chars (i.e., "****") to check if rows are full
    private Map<Integer, Tetromino> puzzlePieces;   //map to store <piece id, piece object>
    private char[][] grid;   //2d matrix to store the Tetris grid

    TetrisSolver(int width, int height) throws IllegalArgumentException{
        if(width<1 || height<1){
            throw new IllegalArgumentException("Illegal dimension (height or width less than 1)!");   //invalid grid dimensions
        }
        this.height = height;
        this.width = width;
        grid = new char[height][width];   //instantiate grid
        //fill initial grid's cells with spaces (denotes empty cells)
        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                grid[row][col] = ' ';
            }
        }
        checkEmptyRow = "";
        checkFullRow = "";
        //create strings to check if a row is empty or full based on the width of the grid
        for(int i=0; i<width; i++){
            checkEmptyRow += ' ';
            checkFullRow += '*';
        }
        puzzlePieces = new HashMap<>();
    }


    String showPuzzle(){
        String puzzleStr = "";
        //iterate through the grid bottom-up right-to-left
        for(int row=0; row<height; row++){
            String rowStr = "";
            for(int col=width-1; col>=0; col--){
                rowStr += grid[row][col];   //add each row's column values to the string
            }
            if(rowStr.equals(checkEmptyRow)){
                break;   //denotes that a row that is empty has been reached (we dont need to add any more rows to the return string)
            }
            puzzleStr += rowStr + "\n";
        }
        if(puzzleStr.equals("")){   //denotes grid is empty
            return puzzleStr;   //returns an empty string
        }
        //CITATION NOTE: I referenced the following URL for the method used below to reverse a string
        //URL: https://stackoverflow.com/questions/7569335/reverse-a-string-in-java
        //Accessed: November 17, 2022
        puzzleStr = new StringBuilder(puzzleStr).reverse().toString();   //reverse the string so that the grid is printed from top to bottom
        puzzleStr = puzzleStr.substring(1);   //remove the first "\n" char since we reversed the string
        //System.out.println(gridPenalty(grid));
        return puzzleStr;
    }


    void addPuzzleRow(String nextRow) throws IllegalArgumentException{
        if(nextRow==null){
            throw new IllegalArgumentException("Next row is null!");
        }
        if(nextRow.length()!=width){   //nextRow's length does not match the grid's width
            throw new IllegalArgumentException("Next row does not match grid width!");
        }
        if(nextRow.equals(checkEmptyRow)){  //nextRow is an empty row (all spaces)
            throw new IllegalArgumentException("Next row is empty row (invalid)!");
        }
        if(nextRow.equals(checkFullRow)){   //nextRow is a full row (all chars)
            throw new IllegalArgumentException("Next row is full row (invalid)!");
        }
        int nextUsableRowInGrid = topRowInGrid(grid);   //get the next top row available (still empty) in the current grid configuration
        if(nextUsableRowInGrid>=height){   //denotes the top row in the grid is already in use (some blocks filled in grid's topmost row)
            throw new IllegalArgumentException("Grid's top row already full (cannot add another row to current configuration!");
        }
        //fill grid's next usable top row based on nextRow supplied
        for(int col=0; col<width; col++){
            if(nextRow.charAt(col)!=' '){
                grid[nextUsableRowInGrid][col] = '*';
            }
        }
    }


    //method to find grid's topmost available row (topmost row that is still empty)
    private int topRowInGrid(char[][] currGrid){
        //iterate through grid top-down left-to-right
        for(int row=height-1; row>=0; row--){
            for(int col=0; col<width; col++){
                if(currGrid[row][col]!=' '){   // a non-empty cell is found
                    return row + 1;   //return the row above this row (this row being the topmost row that is already in use)
                }
            }
        }
        return 0;   //grid is empty so return 0 which denotes the bottommost row of the grid (its "floor")
    }


    int addPuzzlePiece(String piece, int relativeFrequency) throws IllegalArgumentException{
        if(piece==null){
            throw new IllegalArgumentException("Piece is null!");
        }
        if(piece.trim().equals("")){
            throw new IllegalArgumentException("Piece is empty string!");
        }
        if(relativeFrequency<=0){
            //invalid because this means piece never appears in game (only if we place it manually)

            //(alternative, but causes problems calc val) --> relative frequency of 0 would be valid because it means the piece is stored
            //in the Tetris game but never used in play. It can, however, be used in debugging.
            //in the case of this program, relative freq. of 0 means we can place the piece using placePiece,
            //but it will not have any effect on lookahead calculations since its (val*freq)/totalFreq will always be 0
            throw new IllegalArgumentException("Relative frequency is less than 1 (invalid)!");
        }
        String[] pieceRows = piece.split("\n");   //separate the piece string into the piece's individual rows
        Tetromino newTetromino;
        try{
            //pass information about the piece being added and the grid to the Tetromino class which
            //will check whether the piece is valid. If it is valid, a new Tetromino object of this piece will be created
            newTetromino = new Tetromino(pieceRows, puzzlePieces.size(), relativeFrequency, width, height);
        }
        catch(IllegalArgumentException e){
            throw e;   //Tetromino class threw an exception because the piece is invalid
        }
        if(isDuplicateTetromino(newTetromino)){   //check that the new piece is not a duplicate of an existing piece that was added
            throw new IllegalArgumentException("Piece is a duplicate (identical piece already stored)!");
        }
        else{
            puzzlePieces.put(puzzlePieces.size(), newTetromino);   //add the new piece to the map that aggregates all the puzzle pieces added
        }
        int pieceId = newTetromino.getPieceId();
        return pieceId;
    }



    //method used to check whether a new piece is a duplicate of an existing piece
    //this is used when validating a piece in addPuzzlePiece()
    private boolean isDuplicateTetromino(Tetromino newPiece){
        for(Map.Entry<Integer, Tetromino> existingTetromino: puzzlePieces.entrySet()){   //iterate through each existing piece
            List<int[][]> existingTetrominoOrientations = existingTetromino.getValue().getPieceOrientations();
            for(int[][] existingOrientation: existingTetrominoOrientations){    //iterate through each of the existing piece's orientations (different rotational positions)
                for(int[][] newPieceOrientation: newPiece.getPieceOrientations()){   //iterate through each of the new piece's orientations
                    if(Arrays.deepEquals(newPieceOrientation, existingOrientation)){   //check whether the two orientations match
                        return true;   //the piece is a duplicate
                    }
                }

            }
        }
        return false;   //the piece is not a duplicate
    }



    int placePiece(int pieceId, int lookahead) throws IllegalArgumentException{
        if(!puzzlePieces.containsKey(pieceId)){   //check whether the pieceId supplied exists
            throw new IllegalArgumentException("PieceId does not exist!");
        }
        if(lookahead<0){
            throw new IllegalArgumentException("Lookahead is negative (invalid)!");
        }
        List<PiecePlacement> initialPlacements = new ArrayList<>();
        Tetromino firstPiece = puzzlePieces.get(pieceId);   //get the object of the first piece being placed
        for(int[][] pieceOrientation: firstPiece.getPieceOrientations()){   //iterate through the piece's orientations
            initialPlacements.addAll(fitPiece(pieceOrientation, grid, pieceId));   //add all the possible placements of that orientation in the grid into a list
        }
        if(initialPlacements.isEmpty()){   //if this list is empty, then the first piece cannot be placed due to the grid's current configuration meaning its game over
            throw new IllegalArgumentException("GAME OVER: Cannot fit piece placed into current grid configuration!");
        }
        //when the lookahead is zero, simply calculate the value of each of the initial placements and get the best one. Then, set the grid to reflect
        //that best placement and return that placement's value
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
            List<List<PiecePlacement>> lookaheadCombinations = new ArrayList<>();   //list to store all lookahead placement combinations
            //add initial piece's placements to that list (these form the base of the potential future placement combinations)
            for(int i=0; i<initialPlacements.size(); i++){
                List<PiecePlacement> firstPlacement = new ArrayList<>();
                firstPlacement.add(initialPlacements.get(i));
                lookaheadCombinations.add(firstPlacement);
            }
            int counter = 1;
            //find the lookahead placement combinations <lookahead> times (go forward <lookahead> times)
            while(counter<=lookahead){
                //get the next potential placement combinations based on the current combinations
                List<List<PiecePlacement>> nextPlacementCombinations = feedForward(lookaheadCombinations, counter);
                if(nextPlacementCombinations.isEmpty()){   //denotes that no more pieces can fit in any existing combination because of the grid configurations of each combination
                    break;                                 //this is rare and only happens in extreme circumstances where the grid is rather small, pieces are big, and lookahead is a large value
                }
                lookaheadCombinations = nextPlacementCombinations;   //update combinations
                counter++;   //increment "depth" counter
            }
            //find the best placement combination between all the lookahead combinations identified in the last step
            List<PiecePlacement> bestLookaheadCombination = findBestLookahead(lookaheadCombinations);
            if(bestLookaheadCombination==null){   //no best combination exists (this happens when no piece can be fit after the first initial piece)
                return placePiece(pieceId, 0);  //so, instead just redo this method but with a lookahead of 0
            }
            grid = bestLookaheadCombination.get(0).getGridAfterPlacement();  //set the grid to reflect the best combination's initial piece placement
            return bestLookaheadCombination.get(0).getPlacementValue() - gridPenalty(grid);   //return that initial piece placement's value
        }
    }


    //method used by placePiece to return all possible placements for that piece in a grid
    private List<PiecePlacement> fitPiece(int[][] piece, char[][] currGrid, int pieceId){
        int pieceHeight = piece.length;
        int pieceWidth = piece[0].length;
        List<PiecePlacement> possiblePlacements = new ArrayList<>();   //list to store all possible placement of piece
        //check all valid possible placements in current grid config
        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                if(pieceHeight>height-row || pieceWidth>width-col){
                    continue;   //piece cannot fit in the grid using this starting cell
                }
                boolean overlapsExistingBlocks = false;
                for(int y=0; y<pieceHeight; y++){
                    if(overlapsExistingBlocks){   //break loop if overlap detected (reduces iterations)
                        break;
                    }
                    //check if this place would cause the piece to overlap with existing blocks
                    for(int x=0; x<pieceWidth; x++){
                        if(piece[y][x]==1){
                            if(currGrid[row+y][col+x]=='*'){
                                overlapsExistingBlocks = true;   //the piece would overlap with existing blocks
                                break;
                            }
                        }
                    }
                }
                if(overlapsExistingBlocks){   //overlap would occur with this starting coordinate
                    continue;   //try next column in row as starting coordinate
                }
                //create deep copy of grid to perform placement validation operations on
                char[][] tempGrid = deepCopyGrid(currGrid);
                if(isPlacementValid(piece, row, col, tempGrid)){   //this placement would be valid
                    char[][] gridAfterPlacement = deepCopyGrid(currGrid);   //create deep copy of grid to place piece in (need copy next potential placements need to check against this placement)
                    PiecePlacement placement = findPlacementValue(piece, row, col, gridAfterPlacement, pieceId);  //create the PiecePlacement object to store this placement
                    possiblePlacements.add(placement);  //add that object to the list of possible placements
                }
            }
        }
        return possiblePlacements;
    }



    //method to check next potential placement combinations based on the previous placement combinations identified while "looking ahead"
    private List<List<PiecePlacement>> feedForward(List<List<PiecePlacement>> lookaheadCombinations, int counter){
        List<List<PiecePlacement>> nextPlacementCombinations = new ArrayList<>();   //list to store the next round of placement combinations
        //iterate over the list of previous placement combinations
        for(int i=0; i<lookaheadCombinations.size(); i++){
            //iterate over each puzzle piece stored (i.e., each puzzle piece added using addPuzzlePiece)
            for(int j=0; j<puzzlePieces.size(); j++){
                Tetromino nextPiece = puzzlePieces.get(j);
                //iterate over each of the piece's rotational orientations
                for(int k=0; k<nextPiece.getPieceOrientations().size(); k++){
                    char[][] lastGrid = deepCopyGrid(lookaheadCombinations.get(i).get(counter-1).getGridAfterPlacement());   //deep copy the last placement in the combination's grid (i.e., the grid after the last piece placed in this combination)
                    List<PiecePlacement> existingCombination = lookaheadCombinations.get(i);   //fetch this combination's ordered list of piece placements
                    List<PiecePlacement> nextPossiblePlacements = fitPiece(nextPiece.getPieceOrientations().get(k), lastGrid, nextPiece.getPieceId());   //get all the possible placements of this piece's orientation
                    if(nextPossiblePlacements.isEmpty()){   //this piece orientation cannot fit in the previous placement's grid (lastGrid)
                        continue;   //go to the next piece orientation of this piece
                    }
                    //iterate over the list of next possible placements
                    for(int f1=0; f1<nextPossiblePlacements.size(); f1++){
                        //create a new list for each next possible placement
                        List<PiecePlacement> newCombination = new ArrayList<>();
                        //add all the existing placements in the existing combination to that list
                        newCombination.addAll(existingCombination);
                        //add the new placement at the end of that list
                        newCombination.add(nextPossiblePlacements.get(f1));
                        //add that list to the list of ALL next possible combinations
                        nextPlacementCombinations.add(newCombination);
                    }
                }
            }
        }
        return nextPlacementCombinations;
    }



    //method used by placePiece() that calculates the overall value of all placement combinations found when looking ahead
    //and identifies the best combination based on those calculated values
    private List<PiecePlacement> findBestLookahead(List<List<PiecePlacement>> lookaheadCombinations){
        Map<List<PiecePlacement>, Double> lookaheadValues = new HashMap<>();   //map to store each combination and its total value
        //get the sum of the relative piece frequencies
        double sumOfPieceFrequencies = 0;
        for(int i=0; i<puzzlePieces.size(); i++){
            sumOfPieceFrequencies += puzzlePieces.get(i).getRelativeFrequency();
        }
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
            lookaheadValues.put(placementCombination, totalValue);
        }
        //CITATION NOTE: Used the below shorthand way to find the first entry in a map from the following URL:
        //URL: https://stackoverflow.com/questions/26230225/hashmap-getting-first-key-value
        //Accessed: November 19, 2022
        Map.Entry<List<PiecePlacement>, Double> firstCombination = lookaheadValues.entrySet().iterator().next();
        //set an initial best value and best placement as the first entry in the <placement-value> map
        double bestValue = firstCombination.getValue();
        List<PiecePlacement> bestCombination = firstCombination.getKey();
        //iterate through that map to find the best value
        for(Map.Entry<List<PiecePlacement>, Double> entry: lookaheadValues.entrySet()){
            if(entry.getValue()>bestValue){
                bestValue = entry.getValue();
                bestCombination = entry.getKey();
            }
        }
        return bestCombination;
    }



    //method to fully copy a grid object (matrix)
    //this is used when performing operations on a grid are not permanent like
    //calculating lookahead placements
    private char[][] deepCopyGrid(char[][] gridToCopy){
        char[][] copyGrid = new char[gridToCopy.length][gridToCopy[0].length];
        for(int row=0; row<gridToCopy.length; row++){
            for(int col=0; col<gridToCopy[0].length; col++){
                copyGrid[row][col] = gridToCopy[row][col];
            }
        }
        return copyGrid;
    }


    //method to check that a piece placement is valid used during fitPiece.
    //Specifically, it checks that the piece isn't "floating" (i.e., it is connected the top of the existing grid configuration or the grid's floor (bottom row)
    private boolean isPlacementValid(int[][] piece, int startingY, int startingX, char[][] currGrid){
        boolean floatingPiece = true;
        for(int y=0; y<piece.length; y++){
            for(int x=0; x<piece[0].length; x++){
                if(startingY + y == nextRowInExistingColumn(currGrid, startingX + x)){   //check whether this block in the piece is located one row above the
                    floatingPiece = false;                                                   //the topmost filled row in the column it is being placed in
                }
                if(piece[y][x] == 1){
                    currGrid[startingY + y][startingX + x] = '*';   //place the piece block in the grid to reflect the placement
                }
            }
        }
        if(!floatingPiece){   //at least one of the piece's blocks is connected to the grid's topmost blocks
            return true;      //the piece does not "float" in this placement --> placement is valid
        }
        return false;
    }


    //method used during isPlacementValid that returns the next available top row in a column
    private int nextRowInExistingColumn(char[][] currGrid, int col){
        //iterate through rows of a particular column top-bottom
        for(int row=height-1; row>=0; row--){
            if(currGrid[row][col]!=' '){   //topmost filled row in column found
                return row + 1;    //return the row above that topmost filled row
            }
        }
        return 0;   //column is empty (return grid's "floor")
    }


    /*COMMENT AFTER PLACEPIECE LOOKAHEAD*/
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


    //method used by findPlacementValue to identify and clear a grid's full rows
    private int clearFullRows(char[][] gridAfterPlacement){
        int rowsCleared = 0;
        while(findFullRow(gridAfterPlacement)!=-1){   //while the grid still has full rows
            rowsCleared++;
            int fullRow = findFullRow(gridAfterPlacement);   //get the position of the full row
            for(int col=0; col<width; col++){
                gridAfterPlacement[fullRow][col] = ' ';   //clear the full row
            }
            if(fullRow==height-1){   //row filled was the top row (no need to shift other rows down)
                continue;
            }
            //shift all remaining rows down one space in the grid
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
        return rowsCleared;  //return the number of rows cleared (used to calculate placement values)
    }


    //method used by clearFullRows to identify the position of full rows
    private int findFullRow(char[][] gridAfterPlacement){
        //iterate through the grid bottom-up left-to-right
        for(int row=0; row<height; row++){
            String rowStr = "";
            for(int col=0; col<width; col++){
                //add the row's column values to a string
                rowStr += gridAfterPlacement[row][col];
            }
            if(rowStr.equals(checkFullRow)){   //if that string denotes a full row
                return row;   //return the row where the full row was found
            }
        }
        return -1;   //no full rows in the grid
    }


    //method used throughout various methods to calculate a grid configuration's penalty based on the rules
    //outlined in the assignment instructions
    private int gridPenalty(char[][] currGrid){
        int penalty = 0;
        //check if the grid has an even number of cells per row
        boolean gridIsEven = false;
        if(width%2==0){
            gridIsEven = true;
        }
        int center = width/2;
        //iterate through the grid bottom-up left-to-right
        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                if(currGrid[row][col]==' '){   //cell is empty
                    int topRowInCol = nextRowInExistingColumn(currGrid, col) - 1;   //get the column's top filled row (the highest row above the empty cell that is filled)
                    if(row<topRowInCol){
                        penalty += (topRowInCol - row) * 7;   //increment the penalty counter based on the number of rows between the top filled row and the empty cell
                    }
                }
                else{   //cell is filled
                    penalty += row * 10;   //increment penalty based on how high the block is in the grid

                    //increment penalty based on how far block is from center
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