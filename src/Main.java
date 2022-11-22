public class Main {
    public static void main(String[] args) {
        TetrisSolver t = new TetrisSolver(4, 2);
        t.addPuzzleRow("**  ");
        //t.addPuzzlePiece("*\n*\n*\n", 1);
        t.addPuzzlePiece("*", 1);
        t.addPuzzlePiece("**\n", 1);
        System.out.println(t.placePiece(0, 2));
        System.out.println(t.showPuzzle());
    }
}
