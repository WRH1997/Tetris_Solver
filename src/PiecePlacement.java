import java.util.*;

//this class is used to store the information about where tetromino pieces are placed
//it is used TetrisSolver primarily to store the information about previous tetromino placements when looking ahead in its placePiece() method
public class PiecePlacement {

    private int startingX;  //the grid column where a piece's bottom-leftmost block is placed
    private int startingY;  //the grid column where a piece's bottom-leftmost block is placed
    private int[][] piece;  //the actual orientation of the piece represented by a matrix

    //together, the startingX, startingY, and piece variables denote where and how the piece should be placed in the grid

    private int pieceId;
    private int placementValue;
    private char[][] gridAfterPlacement;   //stores the post-placement grid which is used when looking to next potential piece placements in the placePiece() method

    PiecePlacement(int x, int y, int[][] pieceOrientation, int pieceId, int placementValue, char[][] gridAfterPlacement){
        startingX = x;
        startingY = y;
        piece = pieceOrientation;
        this.pieceId = pieceId;
        this.placementValue = placementValue;
        this.gridAfterPlacement = gridAfterPlacement;
    }

    int getStartingX(){
        return startingX;
    }

    int getStartingY(){
        return startingY;
    }

    int getPieceId(){
        return pieceId;
    }

    int getPlacementValue(){
        return placementValue;
    }

    int[][] getPiece(){
        return piece;
    }

    char[][] getGridAfterPlacement(){
        return gridAfterPlacement;
    }
}
