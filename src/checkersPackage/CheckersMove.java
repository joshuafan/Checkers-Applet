package checkersPackage;

// Represents a single move (step or capture) made by a player.
public class CheckersMove {
	public BoardSquare start; // start position
	public BoardSquare destination; // end position
	public double moveScore; // a measure of how favorable the move's outcome is towards a player (assuming optimal play by both sides)
	public boolean isCapture;
	public CheckersPiece captured; // optional
	public boolean madeKing; 

	public CheckersMove(BoardSquare start, BoardSquare destination) {
		this(start, destination, null);
	}
	
	public CheckersMove(BoardSquare start, BoardSquare destination, CheckersPiece captured) {
		this(start, destination, captured, false);
	}
	
	public CheckersMove(BoardSquare start, BoardSquare destination, CheckersPiece captured, boolean madeKing) {
		this.start = start;
		this.destination = destination;
		if (Math.abs(destination.x - start.x) != 1) {
			isCapture = true;
		} else {
			isCapture = false;
		}
		this.moveScore = 0.0;
		this.captured = captured;
		this.madeKing = madeKing;
	}
}
