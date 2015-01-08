package CheckersPackage;

public class CheckersMove {
	public int x1;
	public int y1;
	public int x2;
	public int y2;
	public double moveScore;
	public boolean isCapture;
	public CheckersPiece captured; // optional
	public boolean madeKing; 

	public CheckersMove(int x1, int y1, int x2, int y2) {
		this(x1, y1, x2, y2, null);
	}
	
	public CheckersMove(int x1, int y1, int x2, int y2, CheckersPiece captured) {
		this(x1, y1, x2, y2, captured, false);
	}
	
	public CheckersMove(int x1, int y1, int x2, int y2, CheckersPiece captured, boolean madeKing) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		if (Math.abs(x1 - x2) != 1) {
			isCapture = true;
		} else {
			isCapture = false;
		}
		this.moveScore = 0.0;
		//this.captured = captured;
		this.madeKing = madeKing;
	}
}
