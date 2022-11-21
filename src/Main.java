public class Main {
    public static void main(String[] args) {
        TetrisSolver t = new TetrisSolver(3, 4);
        t.addPuzzleRow("* *");
        //t.addPuzzleRow("* *");
        System.out.println(t.showPuzzle());
        t.addPuzzlePiece("*\n", 9);
        t.addPuzzlePiece("**\n", 1);
        //t.addPuzzlePiece("\n**\n", 3);
        System.out.println(t.placePiece(0, 0));
        System.out.println(t.showPuzzle());
    }
}
