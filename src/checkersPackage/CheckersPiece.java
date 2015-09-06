package checkersPackage;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;

public abstract class CheckersPiece {
	protected BoardSquare position; // the square the piece is in, as an (x, y) coordinate
	protected CheckersBoard board;
	protected int squareLength;
	protected boolean isKing;
	
	public CheckersPiece(BoardSquare position, int squareLength, CheckersBoard board) {
		this(position, squareLength, false, board);
	}
	
	public CheckersPiece(BoardSquare position, int squareLength, boolean isKing, CheckersBoard board) {
		this.position = position;
		this.squareLength = squareLength;
		this.isKing = isKing;
		this.board = board;
	}
	
	// Moves the current piece to a new square.
	public void move(BoardSquare newPosition) {
		if (!board.insideBoard(newPosition)) {
			throw new IllegalArgumentException("Attempted to move the piece off the board.");
		}
		this.position = newPosition;
	}
	
	// Draws the piece in the center of the square it is in
	public void draw(Graphics2D g2) { 
		int x = position.x; // just for convenience
		int y = position.y;
		Ellipse2D.Double ellipse = new Ellipse2D.Double(squareLength * (x + 0.2), squareLength * (y + 0.2), squareLength * 0.6, squareLength * 0.6);
		g2.setPaint(getColor());
		g2.fill(ellipse);
		
		if (isKing) {
			Ellipse2D.Double kingEllipse = new Ellipse2D.Double(squareLength * (x + 0.3), squareLength * (y + 0.3), squareLength * 0.4, squareLength * 0.4);
			g2.setPaint(Color.WHITE);
			g2.draw(kingEllipse);
		}
	}
	
	// Draws the piece if it is being dragged around, centered at the current location of the mouse.
	// Note that "mouseLocation" is the coordinates within the Graphics, NOT what board square the mouse is in.
	public void drawCenteredAtGivenPoint(Graphics2D g2, Point mouseLocation) {
		
		// Draw the piece
		Ellipse2D.Double ellipse = new Ellipse2D.Double(mouseLocation.x - (squareLength * 0.3),
					                                    mouseLocation.y - (squareLength * 0.3),
					                                    squareLength * 0.6,
					                                    squareLength * 0.6);
		g2.setPaint(this.getColor());
		g2.fill(ellipse);
		
		// If the piece is a king, then add an inner circle to indicate this
		if (this.isKing) {
			Ellipse2D.Double kingEllipse = new Ellipse2D.Double(mouseLocation.x - (squareLength * 0.2), mouseLocation.y - (squareLength * 0.2), squareLength * 0.4, squareLength * 0.4);
			g2.setPaint(Color.WHITE);
			g2.draw(kingEllipse);
		}
	}
	
	// Determines if the move to "destination" is legal, given whose turn it is.
	// NOTE: assumes that this piece may move, and that destination is inside the board
	public boolean isValidMove(BoardSquare destination, Player turn) {
		// Check if the player is to move
		if (this.getPlayer() != turn) return false;
		
		// Capturing is mandatory; if there are any captures available, then the move is only legal if it is a capture
		if (board.areAnyCapturesPossible(turn)) {
			return (capturePossible(destination));
		}
		
		// Otherwise: there are no captures available, so any move is legal 
		else {
			return (stepPossible(destination));
		}
	}
	
	// Determines if moving the current piece from (x2, y2) is legal for the given player as a MOVE (not a jump).
	// This method ignores any "required" piece.
	public boolean stepPossible(BoardSquare destination) {
		Set<BoardSquare> potentials = getPotentialSteps();
		// If the step doesn't satisfy the rules of movement, return false
		if (!potentials.contains(destination)) {
			return false;
		}
		
		// If the destination is not even inside the board, return false
		if (!board.insideBoard(destination)) {
			return false;
		}
		
		// Return true only if the destination is empty
		return (board.getPieceAtPosition(destination) == null);
	}
	
	// Determines if a capture is possible from (x1, y1) to (x2, y2) for the given player.
	public boolean capturePossible(BoardSquare destination) {
		// If the capture doesn't satisfy the rules of movement, return false
		if (!getPotentialCaptures().contains(destination)) {
			return false;
		}
		
		// If the destination is not even inside the board, return false
		if (!board.insideBoard(destination)) {
			return false;
		}
		
		// Capturing is only allowed if the destination square is empty
		if (null != board.getPieceAtPosition(destination)) {
			return false;
		}
		
		// Capturing is only allowed if there exists a piece of the opposite color right
		// between this piece's current location and the destination
		BoardSquare midpoint = new BoardSquare((position.x + destination.x) / 2, (position.y + destination.y) / 2);
		CheckersPiece inBetweenPiece = board.getPieceAtPosition(midpoint);
		if (inBetweenPiece == null) {
			return false;
		}
		
		// Capturing is only allowed if the piece between this piece's current location
		// and the destination is of the opposite color as this piece
		return (inBetweenPiece.getPlayer() != this.getPlayer());
	}
	
	// Gets all potential steps (in other words, the squares that the piece would
	// be allowed to step to if the board was empty and there were no restrictions).
	// Should be overridden by the subclass.
	public abstract Set<BoardSquare> getPotentialSteps();
	
	// Gets all possible squares that the piece could possibly capture to. Should
	// be overridden by the subclass.
	public abstract Set<BoardSquare> getPotentialCaptures();
	
	// Assumes that no captures are available
	public Set<BoardSquare> getAllLegalStepsForPiece() {
		Set<BoardSquare> legalDestinations = new HashSet<BoardSquare>();
		Set<BoardSquare> potentialSteps = this.getPotentialSteps();
		
		for (BoardSquare destination : potentialSteps) {
			if (this.stepPossible(destination)) {
				legalDestinations.add(destination);
			}
		}
		return legalDestinations;
	}
	
	// Assumes that this piece is allowed to capture (no "required piece", unless it is this piece)
	public Set<BoardSquare> getAllLegalCapturesForPiece() {
		Set<BoardSquare> legalDestinations = new HashSet<BoardSquare>();
		Set<BoardSquare> potentialCaptures = getPotentialCaptures();
		
		for (BoardSquare destination : potentialCaptures) {
			if (capturePossible(destination)) {
				legalDestinations.add(destination);
			}
		}
		return legalDestinations;
	}
	
	public boolean areAnyCapturesPossibleForPiece() {
		return (0 < getAllLegalCapturesForPiece().size());
	}
	
	public boolean areAnyMovesPossibleForPiece() {
		return (0 < getAllLegalCapturesForPiece().size() || 0 < this.getAllLegalStepsForPiece().size());
	}
	
	public abstract Player getPlayer();
	
	// Returns the Color that the piece should be painted with, based on which player it belongs to
	public abstract Color getColor();
	
	public BoardSquare getPosition() {
		return position;
	}
	
	public boolean isKing() {
		return this.isKing;
	}
	
	public void makeKing() {
		this.isKing = true;
	}
	
	public void makeRegular() {
		this.isKing = false;
	}
}
