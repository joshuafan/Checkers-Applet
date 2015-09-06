package checkersPackage;

import java.awt.Color;
import java.util.*;

// Represents a red checkers piece
public class RedCheckersPiece extends CheckersPiece {
	public RedCheckersPiece(BoardSquare position, int squareLength, CheckersBoard board) {
		this(position, squareLength, false, board);
	}
	public RedCheckersPiece(BoardSquare position, int squareLength, boolean isKing, CheckersBoard board) {
		super(position, squareLength, isKing, board);
	}
	
	// Gets all potential steps (in other words, the squares that the piece would
	// be allowed to step to if the board was empty and there were no restrictions)
	@Override
	public Set<BoardSquare> getPotentialSteps() {
		Set<BoardSquare> potentialSteps = new HashSet<BoardSquare>();
		potentialSteps.add(new BoardSquare(this.position.x + 1, this.position.y + 1));
		potentialSteps.add(new BoardSquare(this.position.x - 1, this.position.y + 1));
		if (this.isKing) {
			potentialSteps.add(new BoardSquare(this.position.x + 1, this.position.y - 1));
			potentialSteps.add(new BoardSquare(this.position.x - 1, this.position.y - 1));
		}
		return potentialSteps;
	}
	
	// Gets all possible squares that the piece could possibly capture to.
	@Override
	public Set<BoardSquare> getPotentialCaptures() {
		Set<BoardSquare> potentialCaptures = new HashSet<BoardSquare>();
		potentialCaptures.add(new BoardSquare(this.position.x + 2, this.position.y + 2));
		potentialCaptures.add(new BoardSquare(this.position.x - 2, this.position.y + 2));
		if (this.isKing) {
			potentialCaptures.add(new BoardSquare(this.position.x + 2, this.position.y - 2));
			potentialCaptures.add(new BoardSquare(this.position.x - 2, this.position.y - 2));
		}
		return potentialCaptures;
	}
	
	@Override
	public Player getPlayer() {
		return Player.RED;
	}
	
	@Override
	public Color getColor() {
		return Color.RED;
	}
}
