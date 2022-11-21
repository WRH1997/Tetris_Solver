import java.util.*;

public class PiecePlacement {

    private int startingX;
    private int startingY;
    private int[][] piece;
    private int pieceId;
    private int placementValue;
    private char[][] gridAfterPlacement;

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
