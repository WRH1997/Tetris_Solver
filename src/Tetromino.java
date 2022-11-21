import java.util.*;

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
        piece = new int[pieceHeight][pieceWidth];
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
            while(col<pieceWidth){
                piece[row][col] = 0;
                col++;
            }
        }
        if(!isPieceValid(piece)){
            throw new IllegalArgumentException("Piece is invalid (hanging blocks or not tight)!");
        }
        calculatePieceRotations(piece, gridWidth, gridHeight);
        if(pieceOrientations.isEmpty()){
            throw new IllegalArgumentException("Piece does not fit in grid dimensions!");
        }
        this.pieceId = pieceId;
        this.relativeFrequency = relativeFrequency;
    }


    private int calculatePieceWidth(String[] pieceRows){
        int pieceWidth = 0;
        for(int i=0; i<pieceRows.length; i++){
            if(pieceRows[i].length()>pieceWidth){
                pieceWidth = pieceRows[i].length();
            }
        }
        return pieceWidth;
    }


    private boolean isPieceValid(int[][] piece){
        int[] blankRowCheck = new int[pieceWidth];
        for(int i=0; i<pieceWidth; i++){
            blankRowCheck[i] = 0;
        }
        for(int row=0; row<pieceHeight; row++){
            //https://www.geeksforgeeks.org/compare-two-arrays-java/
            if(Arrays.equals(piece[row], blankRowCheck)){
                return false;
            }
        }
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
        if(piece.length==1){
            return true;
        }
        if(!blocksAreConnected(piece, pieceHeight, pieceWidth)){
            return false;
        }
        return true;
    }


    private int[][] rotatePiece(int[][] piece){
        int height = piece.length;
        int width = piece[0].length;
        int[][] rotatedPiece = new int[width][height];
        //https://stackoverflow.com/questions/2799755/rotate-array-clockwise
        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                rotatedPiece[col][height-1-row] = piece[row][col];
            }
        }
        return rotatedPiece;
    }



    //https://www.geeksforgeeks.org/number-of-connected-components-in-a-2-d-matrix-of-strings/
    //https://www.javatpoint.com/find-number-of-island-in-java
    private boolean blocksAreConnected(int[][] piece, int pieceHeight, int pieceWidth){
        int[][] tempPiece = new int[pieceHeight][pieceWidth];
        for(int row=0; row<pieceHeight; row++){
            for(int col=0; col<pieceWidth; col++){
                tempPiece[row][col] = piece[row][col];
            }
        }
        int connectedComponents = 0;
        for(int row=0; row<pieceHeight; row++){
            for(int col=0; col<pieceWidth; col++){
                if(tempPiece[row][col]==1){
                    traverseConnectedBlocks(tempPiece, row, col, pieceHeight, pieceWidth);
                    connectedComponents++;
                }
            }
        }
        if(connectedComponents!=1){
            return false;
        }
        return true;
    }


    //https://www.geeksforgeeks.org/number-of-connected-components-in-a-2-d-matrix-of-strings/
    //https://www.javatpoint.com/find-number-of-island-in-java
    private void traverseConnectedBlocks(int[][] tempPiece, int row, int col, int pieceHeight, int pieceWidth){
        if(row<0 || col<0 || row>=pieceHeight || col>=pieceWidth || tempPiece[row][col]!=1){
            return;
        }
        tempPiece[row][col] = 0;
        traverseConnectedBlocks(tempPiece, row+1, col, pieceHeight, pieceWidth);
        traverseConnectedBlocks(tempPiece, row-1, col, pieceHeight, pieceWidth);
        traverseConnectedBlocks(tempPiece, row, col+1, pieceHeight, pieceWidth);
        traverseConnectedBlocks(tempPiece, row, col-1, pieceHeight, pieceWidth);
    }


    private void calculatePieceRotations(int[][] piece, int gridWidth, int gridHeight){
        pieceOrientations = new ArrayList<>();
        if(pieceWidth<=gridWidth && pieceHeight<=gridHeight){
            pieceOrientations.add(piece);
        }
        int[][] tempPiece = piece;
        for(int i=0; i<3; i++){
            int[][] rotatedPiece = rotatePiece(tempPiece);
            if(isDuplicateRotation(rotatedPiece)){
                continue;
            }
            if(rotatedPiece.length<=gridHeight && rotatedPiece[0].length<=gridWidth){
                pieceOrientations.add(rotatedPiece);
            }
            tempPiece = rotatedPiece;
        }
    }


    private boolean isDuplicateRotation(int[][] rotatedPiece){
        for(int i=0; i<pieceOrientations.size(); i++){
            //https://stackoverflow.com/questions/2721033/java-arrays-equals-returns-false-for-two-dimensional-arrays
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
