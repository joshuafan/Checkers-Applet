package CheckersPackage;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class CheckersBoard {
	private CheckersPiece[][] array; // 2x2 array representing the board
	private int length;
	private int width;
	private Color c1;
	private Color c2;
	private int mouseX;
	private int mouseY;
	private CheckersPiece currentPiece;
	private CheckersPiece requiredPiece;
	private ArrayList<ArrayList<CheckersMove>> lastMoves;
	
	public int blackPieces; // number of black pieces on board
	public int redPieces; // number of red pieces on board
	public int blackKings; // number of black kings on board
	public int redKings; // number of red kings on board
	
	private static final int SQUARE_LENGTH = 50;
	private static final int DEFAULT_STARTING_PIECES = 12;
	
	// Constructs a CheckerBoard with the default length, width, and colors
	public CheckersBoard() {
		this(8, 8, new Color(230, 177, 55), new Color(120, 74, 43));
	}
	
	// Constructs a CheckerBoard with the given length and width, with the default colors
	public CheckersBoard(int length, int width) {
		this(length, width, new Color(230, 177, 55), new Color(120, 74, 43));
	}
	
	// Constructs a CheckerBoard with the given length, width, and colors
	public CheckersBoard(int length, int width, Color c1, Color c2) {
		this.array = new CheckersPiece[8][8];
		addInitialPieces();
		this.length = length;
		this.width = width;
		this.c1 = c1;
		this.c2 = c2;
		this.currentPiece = null;
		this.requiredPiece = null;
		this.blackKings = 0;
		this.redKings = 0;
		this.lastMoves = new ArrayList<ArrayList<CheckersMove>>();
	}
	
	
	// Adds all initial pieces to the board in the standard pattern.
	public void addInitialPieces() {
		// NOTE: ZERO-BASED INDEXING
		// draw black pieces
		for (int row = 0; row <= 2; row += 2) {
			for (int col = 0; col <= 6; col += 2) {
				addPiece(row, col, Color.RED);
			}
		}
		
		for (int col = 1; col <= 7; col += 2) {
			addPiece(1, col, Color.RED);
		}
		
		// draw red pieces
		for (int row = 5; row <= 7; row += 2) {
			for (int col = 1; col <= 7; col += 2) {
				addPiece(row, col, Color.BLACK);
			}
		}
		
		for (int col = 0; col <= 6; col += 2) {
			addPiece(6, col, Color.BLACK);
		}
		
		this.blackPieces = DEFAULT_STARTING_PIECES;
		this.redPieces = DEFAULT_STARTING_PIECES;
		this.blackKings = 0;
		this.redKings = 0;
	}
	
	// Removes all the pieces from the board.
	public void removeAllPieces() {
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < width; j++) {
				array[i][j] = null;
			}
		}
	}
	
	
	// Adds a piece in the given location of the given color.
	public void addPiece(int row, int col, Color pieceColor) {
		array[col][row] = new CheckersPiece(pieceColor, col, row, SQUARE_LENGTH);
	}
	
	// Paints the board, including the pieces in their correct locations.
	public void paintBoard(Graphics2D g2) {
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < width; j++) { // paint each square of board
				if (i % 2 == j % 2) { // alternate square colors
					g2.setPaint(c1);
				} else {
					g2.setPaint(c2);
				}
				Rectangle2D.Double square = new Rectangle2D.Double(SQUARE_LENGTH * i, SQUARE_LENGTH * j, SQUARE_LENGTH, SQUARE_LENGTH);
				g2.fill(square);
				
				CheckersPiece pieceAtIndex = array[i][j];
				// paint piece at square, if one exists and is NOT being dragged around
				if (pieceAtIndex != null && !pieceAtIndex.equals(currentPiece)) {
					g2.setPaint(pieceAtIndex.getColor());
					pieceAtIndex.draw(g2);
				}	
			}
		}
		
		// outline squares that were part of computer move
		if (lastMoves.size() >= 2) {
			for (CheckersMove move : lastMoves.get(lastMoves.size() - 1)) {
				g2.setPaint(Color.BLUE);
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(new BasicStroke(2));
				Rectangle2D.Double square1 = new Rectangle2D.Double(SQUARE_LENGTH * move.x1, SQUARE_LENGTH * move.y1, SQUARE_LENGTH, SQUARE_LENGTH);
				Rectangle2D.Double square2 = new Rectangle2D.Double(SQUARE_LENGTH * move.x2, SQUARE_LENGTH * move.y2, SQUARE_LENGTH, SQUARE_LENGTH);
				g2.draw(square1);
				g2.draw(square2);
				g2.setStroke(oldStroke);
			}
		}
		
		// if piece is being dragged around: paint it centered around the mouse
		if (currentPiece != null) {
			currentPiece.drawCenteredAtGivenPoint(g2, mouseX, mouseY);
		}
		g2.setPaint(Color.BLACK);
		g2.drawString("Black: " + blackPieces + " pieces (" + blackKings + " kings)", 250, SQUARE_LENGTH * width + 20);
		g2.setPaint(Color.RED);
		g2.drawString("Red: " + redPieces + " pieces (" + redKings + " kings)", 250, SQUARE_LENGTH * width + 40);
	}
	
	// Returns the piece that the mouse is pointing to.
	public CheckersPiece find(Point p, double squareLength, Color turn) {
		double x = p.getX();
		double y = p.getY();
		int xCoordinate = (int) x / (int) squareLength;
		int yCoordinate = (int) y / (int) squareLength;
		
		// check if the indices are within bounds, and if the user is clicking on a piece of the right color
		if (xCoordinate < length && yCoordinate < width && array[xCoordinate][yCoordinate] != null
				&& array[xCoordinate][yCoordinate].getColor().equals(turn)) {
			return array[xCoordinate][yCoordinate];
		} else {
			return null;
		}
	}
	
	
	// DETERMINING IF MOVE IS LEGAL
	
	// Determines if the move from (x1, y1) to (x2, y2) is legal
	public boolean isValidMove(int x1, int y1, int x2, int y2, Color turn, CheckersPiece requiredPiece) {
		// check if move occurs within checkers board
		if (x1 > length || x1 < 0 || x2 > length || x2 < 0 || y1 > width || y1 < 0 || y2 > width || y1 < 0) {
			return false;
		}
		
		// if requiredPiece is not null, then the move is legal ONLY if it is a capture and it involves the required piece.
		// This is because this method would only be called with a value for requiredPiece if a piece was making multiple 
		// captures in one turn.
		if (requiredPiece != null) {
			// make sure the user clicked on the required piece
			if (requiredPiece.getX() == x1 && requiredPiece.getY() == y1 && capturePossible(x1, y1, x2, y2, turn)) {
				return true;
			} else {
				return false;
			}
		}
		
		// Capturing is mandatory; if there are any captures available, then the move is only legal if it is a capture
		if (areAnyCapturesPossible(turn)) {
			if (capturePossible(x1, y1, x2, y2, turn)) {
				return true;
			} else {
				return false;
			}
		}
		
		// Otherwise: there are no captures available, so any move is legal 
		else {
			if (movePossible(x1, y1, x2, y2, turn)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	
	// Returns whether there are any moves possible for the given player.
	public boolean areAnyMovesPossible(Color turn) {
		
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < width; j++) {
				CheckersPiece piece = array[i][j];
				if (piece != null && piece.getColor().equals(turn) && areAnyMovesPossibleForPiece(piece)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	// Returns whether there are any moves or captures available for the given piece.
	public boolean areAnyMovesPossibleForPiece(CheckersPiece piece) {
		return (areCapturesPossibleForPiece(piece) || areMovesPossibleForPiece(piece));
	}
	
	// Helper method that determines if the given player has any captures possible.
	public boolean areAnyCapturesPossible(Color turn) {
		// for each checkers piece in the board that is of the same color, then check if that piece has captures available.
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (array[i][j] != null && array[i][j].getColor().equals(turn)) {
					if (areCapturesPossibleForPiece(array[i][j])) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	// Returns whether there are any captures possible for the given piece
	public boolean areCapturesPossibleForPiece(CheckersPiece piece) {
		int pieceX = piece.getX();
		int pieceY = piece.getY();
		Color pieceColor = piece.getColor();
		return capturePossible(pieceX, pieceY, pieceX + 2, pieceY - 2, pieceColor) ||
				capturePossible(pieceX, pieceY, pieceX - 2, pieceY - 2, pieceColor) ||
				capturePossible(pieceX, pieceY, pieceX + 2, pieceY + 2, pieceColor) ||
				capturePossible(pieceX, pieceY, pieceX - 2, pieceY + 2, pieceColor);	

	}
	
	
	// Returns whether there are any moves possible for the given piece
	public boolean areMovesPossibleForPiece(CheckersPiece piece) {
		int pieceX = piece.getX();
		int pieceY = piece.getY();
		Color pieceColor = piece.getColor();
		return movePossible(pieceX, pieceY, pieceX + 1, pieceY - 1, pieceColor) ||
				movePossible(pieceX, pieceY, pieceX - 1, pieceY - 1, pieceColor) ||
				movePossible(pieceX, pieceY, pieceX + 1, pieceY + 1, pieceColor) ||
				movePossible(pieceX, pieceY, pieceX - 1, pieceY + 1, pieceColor);
		
	}
	
	
	// Determines if the move from (x1, y1) to (x2, y2) is legal for the given player as a MOVE (not a jump).
	private boolean movePossible(int x1, int y1, int x2, int y2, Color turn) {
		// check if any of the indices are out of bounds
		if (x1 >= length || x1 < 0 || x2 >= length || x2 < 0 || y1 >= width || y1 < 0 || y2 >= width || y2 < 0) {
			return false;
		}
		
		// check if there is a piece there of the correct color
		if (array[x1][y1] != null && array[x1][y1].getColor().equals(turn) && array[x2][y2] == null && (x1 == x2 + 1 || x1 == x2 - 1)) {
			if (array[x1][y1].isKing()) {
				if (y2 == y1 - 1 || y2 == y1 + 1) {
					return true;
				} else {
					return false;
				}
			}
			
			if (turn.equals(Color.BLACK)) {
				if (y2 == y1 - 1) {
					return true;
				} else {
					return false;
				}
			}
			
			if (turn.equals(Color.RED)) {
				if (y2 == y1 + 1) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}
	
	
	// Determines if a capture is possible from (x1, y1) to (x2, y2) for the given player
	public boolean capturePossible(int x1, int y1, int x2, int y2, Color turn) {
		// If this was called with x or y values outside of the board, the move is automatically illegal.
		if (x1 >= length || x1 < 0 || x2 >= length || x2 < 0 || y1 >= width || y1 < 0 || y2 >= width || y2 < 0) {
			return false;
		}
		
		// check if there is a piece of the correct color on the origin square, and if there is no piece at the destination square,
		// and if the squares are two columns apart
		if (array[x1][y1] != null && array[x1][y1].getColor().equals(turn) && array[x2][y2] == null && (x1 == x2 + 2 || x1 == x2 - 2)) {
			
			CheckersPiece capturedPiece = array[(x1 + x2) / 2][(y1 + y2) / 2];
			
			if (capturedPiece == null || capturedPiece.getColor().equals(turn)) {
				return false; // captured piece does not exist, or was same color as capturing piece
			}
			
			if (array[x1][y1].isKing()) {
				if (y2 == y1 - 2 || y2 == y1 + 2) {
					return true;
				} else {
					return false;
				}
			}
			
			if (turn.equals(Color.BLACK)) { // if black is to move and piece is not a king
				if (y2 == y1 - 2) {
					return true;
				} else {
					return false;
				}
			}
			
			if (turn.equals(Color.RED)) {
				if (y2 == y1 + 2) {
					return true;
				} else {
					return false;
				}
			}
		} 
		return false;
	}
	
	
	
	// AI
	
	// Computer makes a move for red, using a simple recursive AI
	public void makeAIMove(Color turn) {
		ArrayList<CheckersMove> consolidatedMoves = new ArrayList<CheckersMove>();
		CheckersMove move = findBestMove(turn, 5, null);
		if (move != null && move.x1 != -1) {
			makeMove(move, turn, true);
			consolidatedMoves.add(move);
			if (move.isCapture) {
				requiredPiece = array[move.x2][move.y2];
				while (areCapturesPossibleForPiece(requiredPiece)) {
					CheckersMove nextMove = findBestMove(turn, 5, requiredPiece);
					makeMove(nextMove, turn, true);
					consolidatedMoves.add(nextMove);
				}
				requiredPiece = null;
			}
		}
	}
	
	// A simple recursive AI for evaluating the best move in a position, given who it is to move, if it is the first 
	// call to the method, and if any piece is required to continue capturing (the parameter "required")
	public CheckersMove findBestMove(Color turn, int numberOfMovesToExplore, CheckersPiece required) {
		
		// base case: if no more captures are possible (and this is not the first call to the method), stop analyzing, and then
		// calculate the score for this combination
		if (numberOfMovesToExplore <= 0) {
			//CheckersMove m = getRandomMove(turn);
			CheckersMove m = new CheckersMove(-1, -1, -1, -1); // doesn't matter if illegal, since this basically only stores the score
			double score = (this.redPieces + (0.8 * this.redKings) - (this.blackPieces + (0.8 * this.blackKings)));
			m.moveScore = score;
			return m;
		}
		
		// make list of all possible moves 
		ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();
		if (areAnyCapturesPossible(turn)) {
			moves = returnListOfPossibleCaptures(turn, required);
		} else {
			moves = returnListOfPossibleMoves(turn);
		}
		
		// if no moves available, that player has lost, so return an extreme score in favor of the other player.
		if (moves == null || moves.size() == 0) {
			CheckersMove nullMove = new CheckersMove(-1, -1, -1, -1);
			if (turn.equals(Color.RED)) {
				nullMove.moveScore = -99.0;
			} else {
				nullMove.moveScore = 99.0;
			}
			return nullMove;
		}
		
		// Makes sure that computer does not always play the same move if there is more than one
		// move with the best score
		Collections.shuffle(moves);
		
		CheckersMove bestMove = null;
		double bestResult; 
		if (turn.equals(Color.RED)) {
			bestResult = -99.0; 
		} else {
			bestResult = 99.0;
		}
		boolean wentThroughForLoop = false;
		// Iterate through every possible move: "choose, explore, unchoose"
		for (CheckersMove move : moves) {
			wentThroughForLoop = true;
			// "choose": try out move, and then recursively evaluate outcomes
			if (move.isCapture) {
				move.captured = array[(move.x1+move.x2)/2][(move.y1+move.y2)/2];
			}
			makeMove(move, turn, false);
			CheckersPiece req = array[move.x2][move.y2];
			CheckersMove bestNextMove;
			
			// recursive step: recursively find out the "score" of the current move being considered
			if (move.isCapture && areCapturesPossibleForPiece(req)) { // we need to examine continued captures
				bestNextMove = findBestMove(turn, numberOfMovesToExplore, req);
				
			} else { // no continued captures available, so analyze options for other player now
				bestNextMove = findBestMove(switchTurn(turn), numberOfMovesToExplore - 1, null);
			}
			double moveScore = 0;

			if (bestNextMove != null) {
				moveScore = bestNextMove.moveScore;
			} 
			
			if (turn.equals(Color.RED)) {
				if (moveScore > bestResult) { // this move yields a better result than any move examined previously
					bestMove = move;
					bestResult = moveScore;
				}
			} else {
				if (moveScore < bestResult) { // this move yields a better result than any move examined previously
					bestMove = move;
					bestResult = moveScore;
				}
			}
			undoMove(move, turn);
		}
		
		if (bestMove != null) {
			bestMove.moveScore = bestResult;
		} 
		return bestMove;
	}
	
	

	
	public ArrayList<CheckersMove> returnListOfPossibleCaptures(Color turn, CheckersPiece required) {
		ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();
		
		for (int i = 0; i < this.length; i++) {
			for (int j = 0; j < this.width; j++) {
				CheckersPiece piece = array[i][j];
				if (piece != null && piece.getColor().equals(turn) && areCapturesPossibleForPiece(piece)) {
					if (required == null || piece.equals(required)) {
						if (capturePossible(i, j, i + 2, j + 2, turn)) {
							moves.add(new CheckersMove(i, j, i + 2, j + 2));
						}
						if (capturePossible(i, j, i + 2, j - 2, turn)) {
							moves.add(new CheckersMove(i, j, i + 2, j - 2));
						}
						if (capturePossible(i, j, i - 2, j + 2, turn)) {
							moves.add(new CheckersMove(i, j, i - 2, j + 2));
						}
						if (capturePossible(i, j, i - 2, j - 2, turn)) {
							moves.add(new CheckersMove(i, j, i - 2, j - 2));
						}
					}
				}
			}
		}
		return moves;
	}
	
	// Note: only call this if no captures are available, since this method only considers moves (not jumps)
	public ArrayList<CheckersMove> returnListOfPossibleMoves(Color turn) {
		ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < width; j++) {
				CheckersPiece piece = array[i][j];
				if (piece != null && piece.getColor().equals(turn)) {
					if (movePossible(i, j, i + 1, j + 1, turn)) {
						moves.add(new CheckersMove(i, j, i + 1, j + 1));
					}
					if (movePossible(i, j, i + 1, j - 1, turn)) {
						moves.add(new CheckersMove(i, j, i + 1, j - 1));
					}
					if (movePossible(i, j, i - 1, j + 1, turn)) {
						moves.add(new CheckersMove(i, j, i - 1, j + 1));
					}
					if (movePossible(i, j, i - 1, j - 1, turn)) {
						moves.add(new CheckersMove(i, j, i - 1, j - 1));
					}
				}
			}
		}
		return moves;
	}

	
	
	
	// Note: assumes move is legal!!!
	public void makeMove(CheckersMove move, Color turn, boolean isActualMove) {
		if (move.x1 == -1) {
			return;
		}
		// if this is an actual move (and not analysis by the AI), add this to the move list
		if (isActualMove) {
			if (requiredPiece != null) {
				ArrayList<CheckersMove> last = lastMoves.get(lastMoves.size() - 1);
				last.add(move);
				lastMoves.set(lastMoves.size() - 1, last);
			} else {
				ArrayList<CheckersMove> moveList = new ArrayList<CheckersMove>();
				moveList.add(move);
				lastMoves.add(moveList);
			}
		}
		
		int x1 = move.x1; int x2 = move.x2; int y1 = move.y1; int y2 = move.y2;
		
		if (Math.abs(x1 - x2) == 2) { // capture
			move.captured = array[(x1 + x2) / 2][(y1 + y2) / 2];
			if (move.captured.getColor() == Color.RED) {
				redPieces--;
				if (move.captured.isKing()) {
					redKings--;
				}
			}
			
			if (move.captured.getColor() == Color.BLACK){
				blackPieces--;
				if (move.captured.isKing()) {
					blackKings--;
				}
			}
			array[(x1 + x2) / 2][(y1 + y2) / 2] = null; // remove captured piece, if move was a capture
			
		}
		array[x2][y2] = array[x1][y1];
		array[x2][y2].setX(x2);
		array[x2][y2].setY(y2);
		array[x1][y1] = null;
		
		if (turn.equals(Color.BLACK) && y2 == 0 && !array[x2][y2].isKing()) {
			array[x2][y2].makeKing();
			blackKings++;
			move.madeKing = true;
		}
		if (turn.equals(Color.RED) && y2 == 7 && !array[x2][y2].isKing()) {
			array[x2][y2].makeKing();
			redKings++;
			move.madeKing = true;
		}
	}
	
	// for the "undo move" button
	public boolean undoSeriesOfMoves() {
		// if there is something to undo
		if (lastMoves.size() >= 1) {
			if (requiredPiece == null) { // if this is not during a repeated capture for the player
				ArrayList<CheckersMove> computerMoves = lastMoves.get(lastMoves.size() - 1);
				ArrayList<CheckersMove> lastPlayerMoves = lastMoves.get(lastMoves.size() - 2);
				lastMoves.remove(lastMoves.size() - 1);
				lastMoves.remove(lastMoves.size() - 1);
				for (int i = computerMoves.size() - 1; i >= 0; i--) {
					undoMove(computerMoves.get(i), Color.RED);
				}
				for (int i = lastPlayerMoves.size() - 1; i >= 0; i--) {
					undoMove(lastPlayerMoves.get(i), Color.BLACK);
				}
				return true;
			} else {
				ArrayList<CheckersMove> lastPlayerMoves = lastMoves.get(lastMoves.size() - 1);
				lastMoves.remove(lastMoves.size() - 1);
				for (int i = lastPlayerMoves.size() - 1; i >= 0; i--) {
					undoMove(lastPlayerMoves.get(i), Color.BLACK);
				}
				requiredPiece = null;
				return true;
			}
		} else {
			return false;
		}
	}
	
	// both for the "undo move" button as well as for un-doing moves made in the recursive analysis for the AI
	public void undoMove(CheckersMove move, Color turn) {
		int x1 = move.x1; int x2 = move.x2; int y1 = move.y1; int y2 = move.y2; CheckersPiece captured = move.captured;
		CheckersPiece moved = array[x2][y2];
		array[x2][y2] = null;
		array[x1][y1] = moved;
		array[x1][y1].setX(x1);
		array[x1][y1].setY(y1);
		
		// move to be reversed was just a move, not a capture
		if (captured != null) { // capture
			array[(x1+x2)/2][(y1+y2)/2] = captured; // restore captured piece
			// restore counts of pieces
			if (captured.getColor() == Color.RED) {
				redPieces++;
				if (captured.isKing()) {
					redKings++;
				}
			}
			if (captured.getColor() == Color.BLACK){
				blackPieces++;
				if (captured.isKing()) {
					blackKings++;
				}
			}
		}
		
		// remove king status
		if (turn.equals(Color.BLACK) && move.madeKing) {
			array[x1][y1].makeRegular();
			blackKings--;
		}
		if (turn.equals(Color.RED) && move.madeKing) {
			array[x1][y1].makeRegular();
			redKings--;
		}
	}
	
	
	// Helper getter/setter methods
	public void setCurrentPiece(CheckersPiece piece) {
		this.currentPiece = piece;
	}
	
	public CheckersPiece getCurrentPiece() {
		return currentPiece;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setMouseX(int x) {
		this.mouseX = x;
	}
	
	public void setMouseY(int y) {
		this.mouseY = y;
	}
	
	public CheckersPiece getPieceAtIndex(int x, int y) {
		return array[x][y];
	}
	
	public Color getColor1() {
		return c1;
	}
	
	public Color getColor2() {
		return c2;
	}
	
	public CheckersPiece getRequiredPiece() {
		return requiredPiece;
	}
	
	public void setRequiredPiece(CheckersPiece requiredPiece) {
		this.requiredPiece = requiredPiece;
	}
	
	public void removeLastMoves() {
		this.lastMoves = new ArrayList<ArrayList<CheckersMove>>();
	}
	
	public Color switchTurn(Color turn) {
		if (turn.equals(Color.RED)) {
			return Color.BLACK;
		} else {
			return Color.RED;
		}
	}
}