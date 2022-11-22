import java.util.*;

//this class is used to encapsulate the data of tetromino pieces, where an object of this class is created for each tetromino piece.
//this class also provides tetromino validation and rotation functionalities to facilitate TetrisSolver's operations
public class Tetromino {
    private int pieceId;
    private int[][] piece;
    private int relativeFrequency;
    private int pieceHeight;
    private int pieceWidth;
    private List<int[][]> pieceOrientations;

    Tetromino(String[] pieceRows, int pieceId, int relativeFrequency, int gridWidth, int gridHeight) throws IllegalArgumentException{
        pieceHeight = pieceRows.length;
        pieceWidth = calculatePieceWidth(pieceRows);
        piece = new int[pieceHeight][pieceWidth];   //matrix representation of tetromino (1 = piece block, 0 = empty space)
        //fill in the tetromino matrix based on the piece String supplied in addPuzzlePiece()
        for(int row=0; row<pieceHeight; row++){
            String singlePieceRow = pieceRows[row];
            int col;
            if(singlePieceRow.trim().equals("")){
                continue;
            }
            for(col=0; col<singlePieceRow.length(); col++){
                if(singlePieceRow.charAt(col)==' '){
                    piece[row][col] = 0;
                }
                else{
                    piece[row][col] = 1;
                }
            }
            while(col<pieceWidth){   //equalize the lengths of the piece's rows so that the matrix's rows have equal widths
                piece[row][col] = 0;
                col++;
            }
        }
        if(!isPieceValid(piece)){
            throw new IllegalArgumentException("Piece is invalid (hanging blocks or not tight)!");
        }
        calculatePieceRotations(piece, gridWidth, gridHeight);
        if(pieceOrientations.isEmpty()){   //denotes that none of the rotations (including the original orientation) fit in the grid
            throw new IllegalArgumentException("Piece does not fit in grid dimensions!");
        }
        this.pieceId = pieceId;
        this.relativeFrequency = relativeFrequency;
    }


    //method to calculate the piece's maximum width which is used to equalize the piece matrix's row lengths
    //Example:
    //piece String = "*\n**\n" --> upperRowWidth=1 and lowerRowWidth=2 --> this method calculates the max width (which is 2).
    private int calculatePieceWidth(String[] pieceRows){
        int pieceWidth = 0;
        for(int i=0; i<pieceRows.length; i++){
            if(pieceRows[i].length()>pieceWidth){
                pieceWidth = pieceRows[i].length();
            }
        }
        return pieceWidth;
    }


    //this method checks if the piece is valid. A piece is deemed valid if:
    //1) it is tight (no hanging blocks)
    //2) none of its columns are empty
    //3) none of its rows are empty
    private boolean isPieceValid(int[][] piece){
        int[] blankRowCheck = new int[pieceWidth];
        for(int i=0; i<pieceWidth; i++){
            blankRowCheck[i] = 0;
        }
        //check that none of the rows are empty
        for(int row=0; row<pieceHeight; row++){
            if(Arrays.equals(piece[row], blankRowCheck)){
                return false;   //at least one row is empty --> this piece is invalid
            }
        }
        //rotate the piece 90 degrees and check whether any row is empty
        //(since it has been rotated, any empty rows corresponds to an empty column in the original piece)
        int[][] rotatedPiece = rotatePiece(piece);
        int[] blankColumnCheck = new int[pieceHeight];
        for(int i=0; i<pieceHeight; i++){
            blankColumnCheck[i] = 0;
        }
        for(int col=0; col<pieceWidth; col++){
            if(Arrays.equals(rotatedPiece[col], blankColumnCheck)){
                return false;
            }
        }
        if(piece.length==1){   //if the piece only has one row, we do not need to check if there are any hanging blocks (because we already checked for blank rows and columns)
            return true;
        }
        if(!blocksAreConnected(piece, pieceHeight, pieceWidth)){   //check if the piece has any hanging blocks
            return false;   //piece has one or more hanging blocks
        }
        return true;
    }


    //method to rotate piece 90 degrees
    private int[][] rotatePiece(int[][] piece){
        int height = piece.length;
        int width = piece[0].length;
        int[][] rotatedPiece = new int[width][height];
        //CITATION NOTE: I had a difficult time coming up with the logic of rotating a matrix, so I referenced the method described in following URL:
        //URL: https://stackoverflow.com/questions/2799755/rotate-array-clockwise
        //Accessed: November 18, 2022
        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                rotatedPiece[col][height-1-row] = piece[row][col];
            }
        }
        return rotatedPiece;
    }



    //CITATION NOTE: I was confused about how to go about checking whether all of a matrix's elements are connected.
    //So, I adapted the methods referenced in the following URLs for the TWO following methods (blocksAreConnected and traverseConnectedBlocks):
    //URLs:
    //https://www.geeksforgeeks.org/number-of-connected-components-in-a-2-d-matrix-of-strings/
    //https://www.javatpoint.com/find-number-of-island-in-java
    //Accessed: November 18, 2022

    //this method checks a piece is tight (no hanging blocks) by tallying the number of isolated components in the piece's matrix
    private boolean blocksAreConnected(int[][] piece, int pieceHeight, int pieceWidth){
        //copy the piece's matrix
        int[][] tempPiece = new int[pieceHeight][pieceWidth];
        for(int row=0; row<pieceHeight; row++){
            for(int col=0; col<pieceWidth; col++){
                tempPiece[row][col] = piece[row][col];
            }
        }
        int connectedComponents = 0;
        //feed the matrix copy to a recursive method that will traverse each connected component in the matrix
        //if the number of traversals needed is greater than 1, then there exists some hanging block(s)
        for(int row=0; row<pieceHeight; row++){
            for(int col=0; col<pieceWidth; col++){
                if(tempPiece[row][col]==1){
                    traverseConnectedBlocks(tempPiece, row, col, pieceHeight, pieceWidth);
                    connectedComponents++;
                }
            }
        }
        if(connectedComponents!=1){   //the piece has one or more hanging blocks (it is not tight) --> it is not a valid piece
            return false;
        }
        return true;
    }


    //this method is the recursive traversal through the piece matrix's connected elements to identify
    //the number of isolated components in the above (blocksAreConnected) method
    private void traverseConnectedBlocks(int[][] tempPiece, int row, int col, int pieceHeight, int pieceWidth){
        if(row<0 || col<0 || row>=pieceHeight || col>=pieceWidth || tempPiece[row][col]!=1){
            return;   // a boundary or empty space has been reached
        }
        tempPiece[row][col] = 0;   //denote to next recursive calls that this block has been traversed to
        //traverse through all of this block's connected blocks
        traverseConnectedBlocks(tempPiece, row+1, col, pieceHeight, pieceWidth);  //up
        traverseConnectedBlocks(tempPiece, row-1, col, pieceHeight, pieceWidth);  //down
        traverseConnectedBlocks(tempPiece, row, col+1, pieceHeight, pieceWidth);  //right
        traverseConnectedBlocks(tempPiece, row, col-1, pieceHeight, pieceWidth);  //left
    }


    //method to calculate the four possible orientations (90 degrees * 4) of the piece,
    //checks whether each rotation is valid, and adds the valid rotations to the Tetromino object's list of pieceRotations
    private void calculatePieceRotations(int[][] piece, int gridWidth, int gridHeight){
        pieceOrientations = new ArrayList<>();
        if(pieceWidth<=gridWidth && pieceHeight<=gridHeight){   //check original piece fits in the grid's dimensions
            pieceOrientations.add(piece);
        }
        int[][] tempPiece = piece;
        //rotate the piece 3 times (90, 180, 270)
        for(int i=0; i<3; i++){
            int[][] rotatedPiece = rotatePiece(tempPiece);
            if(isDuplicateRotation(rotatedPiece)){
                continue;   //the rotation is a duplicate of a previous rotation of this piece (this happens for symmetric pieces like ****).
                            //this is important since we save on space and reduce execution time significantly when calculating the potential placements
                            //of each piece's individual rotations
            }
            if(rotatedPiece.length<=gridHeight && rotatedPiece[0].length<=gridWidth){   //check rotated piece fits in the grid's dimensions
                pieceOrientations.add(rotatedPiece);
            }
            tempPiece = rotatedPiece;
        }
    }


    //method to check whether a rotation is identical to one that has already been recorded in the object's list
    //this method is used in the above(calculatePieceRotations) method
    private boolean isDuplicateRotation(int[][] rotatedPiece){
        for(int i=0; i<pieceOrientations.size(); i++){
            if(Arrays.deepEquals(rotatedPiece, pieceOrientations.get(i))){
                return true;
            }
        }
        return false;
    }


    int getPieceId(){
        return pieceId;
    }

    int[][] getPiece(){
        return piece;
    }

    int getPieceHeight(){
        return pieceHeight;
    }

    int getPieceWidth(){
        return pieceWidth;
    }

    List<int[][]> getPieceOrientations(){
        return pieceOrientations;
    }

    int getRelativeFrequency(){
        return relativeFrequency;
    }
}
