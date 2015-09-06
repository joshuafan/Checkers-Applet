package checkersPackage;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

// A class representing a checkers game board. Stores the state of the game, and provides
// functions for game-play and for graphics (painting the board).
public class CheckersBoard {
	private CheckersPiece[][] array; // 2x2 array representing the board
	private int length; // the board's length (number of squares)
	private int width; // the board's height (number of squares)
	private Point mouseLocation; // location of mouse in the panel
	private CheckersPiece currentPiece; // the piece that the user is clicking on, if any
	private CheckersPiece requiredPiece; // the piece that is required to move, if any
	private ArrayList<ArrayList<CheckersMove>> lastMoves;
	
	// Statistics on number of pieces
	public int blackPieces; // number of black pieces on board
	public int redPieces; // number of red pieces on board
	public int blackKings; // number of black kings on board
	public int redKings; // number of red kings on board
	
	// Graphics constants
	private static final int SQUARE_LENGTH = 70;
	private static final Color LIGHT_SQUARE_COLOR = new Color(230, 177, 55);
	private static final Color DARK_SQUARE_COLOR = new Color (120, 74, 43);
	
	// AI constant: number of moves the AI should explore
	private static final int NUMBER_OF_MOVES_TO_EXPLORE = 3;
	
	// Constructs a CheckerBoard with the default length, width, and colors
	public CheckersBoard() {
		this(8, 8);
	}
	
	// Constructs a CheckerBoard with the given length and width
	public CheckersBoard(int length, int width) {
		this.array = new CheckersPiece[length][width];
		this.length = length;
		this.width = width;
		addInitialPieces();
		this.currentPiece = null;
		this.requiredPiece = null;
		this.blackPieces = 12;
		this.redPieces = 12;
		this.blackKings = 0;
		this.redKings = 0;
		this.lastMoves = new ArrayList<ArrayList<CheckersMove>>();
	}
	
	// Adds all initial pieces to the board in the standard pattern.
	public void addInitialPieces() {
		redPieces = 0;
		blackPieces = 0;
		
		// NOTE: ZERO-BASED INDEXING
		// Draw red pieces
		for (int row = 0; row <= 2; row += 2) {
			for (int col = 0; col <= 6; col += 2) {
				this.addPiece(new BoardSquare(col, row), Player.RED);
				redPieces++;
			}
		}
		for (int col = 1; col <= 7; col += 2) {
			this.addPiece(new BoardSquare(col, 1), Player.RED);
			redPieces++;
		}
		
		// Draw black pieces
		for (int row = 5; row <= 7; row += 2) {
			for (int col = 1; col <= 7; col += 2) {
				addPiece(new BoardSquare(col, row), Player.BLACK);
				blackPieces++;
			}
		}
		for (int col = 0; col <= 6; col += 2) {
			addPiece(new BoardSquare(col, 6), Player.BLACK);
			blackPieces++;
		}
	}
	
	// Removes all of the pieces from the board.
	public void removeAllPieces() {
		for (int i = 0; i < this.length; i++) {
			for (int j = 0; j < this.width; j++) {
				this.array[i][j] = null;
			}
		}
	}
	
	// Adds a piece in the given location of the given color.
	public void addPiece(BoardSquare position, Player player) {
		if (!insideBoard(position)) {
			throw new IllegalArgumentException("Point was outside of the board");
		}
		if (Player.RED == player) {
			array[position.x][position.y] = new RedCheckersPiece(position, SQUARE_LENGTH, this);
		} else {
			array[position.x][position.y] = new BlackCheckersPiece(position, SQUARE_LENGTH, this);
		}
	}
	
	// Paints the board, including the pieces in their correct locations.
	public void paintBoard(Graphics2D g2) {
		// Paint each square
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < width; j++) {
				if (i % 2 == j % 2) { // alternate square colors
					g2.setPaint(LIGHT_SQUARE_COLOR);
				} else {
					g2.setPaint(DARK_SQUARE_COLOR);
				}
				Rectangle2D.Double square = new Rectangle2D.Double(SQUARE_LENGTH * i, SQUARE_LENGTH * j, SQUARE_LENGTH, SQUARE_LENGTH);
				g2.fill(square);
				
				CheckersPiece pieceAtIndex = array[i][j];
				
				// Paint piece at square, if one exists and is NOT being dragged around
				if (null != pieceAtIndex && !pieceAtIndex.equals(currentPiece)) {
					g2.setPaint(pieceAtIndex.getColor());
					pieceAtIndex.draw(g2);
				}	
			}
		}
		
		// Outline squares that were part of computer move
		if (lastMoves.size() >= 2) {
			for (CheckersMove move : lastMoves.get(lastMoves.size() - 1)) {
				g2.setPaint(Color.BLUE);
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(new BasicStroke(2));
				Rectangle2D.Double square1 = new Rectangle2D.Double(SQUARE_LENGTH * move.start.x,
						                                            SQUARE_LENGTH * move.start.y,
						                                            SQUARE_LENGTH,
						                                            SQUARE_LENGTH);
				Rectangle2D.Double square2 = new Rectangle2D.Double(SQUARE_LENGTH * move.destination.x,
						                                            SQUARE_LENGTH * move.destination.y,
						                                            SQUARE_LENGTH,
						                                            SQUARE_LENGTH);
				g2.draw(square1);
				g2.draw(square2);
				g2.setStroke(oldStroke);
			}
		}
		
		// If piece is being dragged around: paint it centered around the mouse
		if (currentPiece != null) {
			currentPiece.drawCenteredAtGivenPoint(g2, this.mouseLocation);
		}
		
		// Print the number of pieces each player has currently
		g2.setPaint(Color.BLACK);
		g2.drawString("Black: " + blackPieces + " pieces (" + blackKings + " kings)", 250, SQUARE_LENGTH * width + 20);
		g2.setPaint(Color.RED);
		g2.drawString("Red: " + redPieces + " pieces (" + redKings + " kings)", 250, SQUARE_LENGTH * width + 40);
	}
	
	// Returns the piece that the mouse is pointing to.
	public CheckersPiece find(Point p, int squareLength, Player turn) {
		int xCoordinate = (int) p.getX() / (int) squareLength;
		int yCoordinate = (int) p.getY() / (int) squareLength;
		
		// check if the indices are within bounds, and if the user is clicking on a piece of the right color
		if (insideBoard(new BoardSquare(xCoordinate, yCoordinate)) && 
				null != array[xCoordinate][yCoordinate] && 
				turn == array[xCoordinate][yCoordinate].getPlayer()) {
			return array[xCoordinate][yCoordinate];
		} else {
			return null;
		}
	}
	
	
	// DETERMINING IF MOVE IS LEGAL
	
	// Determines if the move between the points is legal
	public boolean isLegalMove(BoardSquare start, BoardSquare end, Player turn) {
		// Check if the start and destination points are even inside the board
		if (!insideBoard(start) || !insideBoard(end)) {
			return false;
		}
		
		CheckersPiece movedPiece = getPieceAtPosition(start);
		
		// If no piece exists at the start point, then clearly the move is illegal
		if (null == movedPiece) {
			return false;
		}
		
		// If the piece at the start point does not belong to the player that is to move, then the
		// move is illegal
		if (turn != movedPiece.getPlayer()) {
			return false;
		}
		
		// If requiredPiece is not null, that means that a piece had just made a capture, but there are
		// additional captures available for that piece (that the player is required to make). Then,
		// the move is legal ONLY if it is a capture and it involves the required piece.
		if (null != requiredPiece) {
			// Make sure the user clicked on the required piece
			return (requiredPiece.getPosition().equals(start) && requiredPiece.capturePossible(end));
		}
		
		// Capturing is mandatory; if there are any captures available, then the move is only legal if it is a capture
		if (areAnyCapturesPossible(turn)) {
			return movedPiece.capturePossible(end);
		}
		// Otherwise: there are no captures available, so any move is legal 
		else {
			return movedPiece.stepPossible(end);
		}
	}
	
	// Returns whether there are any moves possible for the given player.
	public boolean areAnyMovesPossible(Player turn) {
		
		// Check all squares on the board
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < width; j++) {
				CheckersPiece piece = array[i][j];
				
				// Check if a piece of the right color exists at that square 
				if (null != piece && turn == piece.getPlayer()) {
					
					// Determine if that piece can make any legal steps or captures
					if (piece.getAllLegalCapturesForPiece().size() > 0 || piece.getAllLegalStepsForPiece().size() > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// Helper method that determines if the given player has any captures possible.
	public boolean areAnyCapturesPossible(Player turn) {
		
		// For each checkers piece in the board that is of the same color, then check if that piece has any legal captures.
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				CheckersPiece piece = array[i][j];
				if (null != piece && turn == piece.getPlayer()) {
					if (0 != piece.getAllLegalCapturesForPiece().size()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	// AI
	
	// Makes the "best" move for the given player, using a simple recursive AI.
	public void makeAIMove(Player turn) {
		ArrayList<CheckersMove> consolidatedMoves = new ArrayList<CheckersMove>();
		
		// Find what the AI deems to be the best move
		CheckersMove move = findBestMove(turn, NUMBER_OF_MOVES_TO_EXPLORE, null);
		if (null != move) {
			makeMove(move, turn, true);
			consolidatedMoves.add(move);
			if (move.isCapture) {
				requiredPiece = array[move.destination.x][move.destination.y];
				while (requiredPiece.areAnyCapturesPossibleForPiece()) {
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
	public CheckersMove findBestMove(Player turn, int numberOfMovesToExplore, CheckersPiece required) {
		
		// Base case: if no more captures are possible (and this is not the first call to the method), stop analyzing, and then
		// calculate the score for this combination
		if (numberOfMovesToExplore <= 0) {
			
			// Return a CheckersMove that stores the "score" of this position, based on the number
			// of pieces. A positive score means that Red is winning, and a negative score
			// means that Black is winning. (The content of the move itself is irrelevant.)
			CheckersMove irrelevantMove = new CheckersMove(new BoardSquare(0, 0), new BoardSquare(0, 0)); // doesn't matter if illegal, since this basically only stores the score
			double score = (redPieces + (0.8 * redKings) - (blackPieces + (0.8 * blackKings)));
			irrelevantMove.moveScore = score;
			return irrelevantMove;
		}
		
		// Get list of all legal moves for the player
		ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();
		if (this.areAnyCapturesPossible(turn)) {
			moves = returnListOfLegalCaptures(turn, required);
		} else {
			moves = returnListOfLegalSteps(turn);
		}
		
		// If no moves available, that player has lost, so return an extreme score in favor of the other player
		if (null == moves || 0 == moves.size()) {
			CheckersMove nullMove = new CheckersMove(new BoardSquare(0, 0), new BoardSquare(0, 0));
			if (Player.RED == turn) {
				nullMove.moveScore = Double.MIN_VALUE;
			} else {
				nullMove.moveScore = Double.MAX_VALUE;
			}
			return nullMove;
		}
		
		// Makes sure that computer does not always play the same move if there is more than one
		// move with the best score
		Collections.shuffle(moves);
		
		// Start running through all legal moves, and find the best move (i.e. the one resulting
		// in the best score for the current player, even if the other player plays optimally)
		CheckersMove bestMove = null;
		double bestResult; 
		if (Player.RED == turn) {
			bestResult = -Double.MAX_VALUE; 
		} else {
			bestResult = Double.MAX_VALUE;
		}
		// Iterate through every possible move: "choose, explore, unchoose"
		for (CheckersMove move : moves) {
			// "Choose": try out move, and then recursively evaluate outcomes
			if (move.isCapture) {
				int midpointX = (move.start.x + move.destination.x) / 2;
				int midpointY = (move.start.y + move.destination.y) / 2;
				move.captured = array[midpointX][midpointY];
			}
			makeMove(move, turn, false);
			CheckersPiece newRequiredPiece = array[move.destination.x][move.destination.y];
			CheckersMove bestNextMove;
			
			// Recursive step: recursively find out the "score" of the current move being considered
			if (move.isCapture && newRequiredPiece.areAnyCapturesPossibleForPiece()) { // we need to examine continued captures
				bestNextMove = findBestMove(turn, numberOfMovesToExplore, newRequiredPiece);
			} else { // no continued captures available, so analyze options for other player now
				bestNextMove = findBestMove(switchTurn(turn), numberOfMovesToExplore - 1, null);
			}
			
			// Grab the "score" of the position the best move would result in (assuming optimal opponent play)
			double moveScore = 0;
			if (null != bestNextMove) {
				moveScore = bestNextMove.moveScore;
			} else {
				throw new IllegalStateException("null move!");
			}
			
			// If this move yields a better result than any move examined previously, record it
			if (Player.RED == turn) {
				if (moveScore >= bestResult) {
					bestMove = move;
					bestResult = moveScore;
				}
			} else {
				if (moveScore <= bestResult) {
					bestMove = move;
					bestResult = moveScore;
				}
			}
			
			// Undo the move, to explore others
			undoMove(move, turn);
		}
		
		// Record the best move's score, and return it
		if (bestMove != null) {
			bestMove.moveScore = bestResult;
		} 
		return bestMove;
	}
	
	// Returns a list of legal captures for the given player.
	public ArrayList<CheckersMove> returnListOfLegalCaptures(Player turn, CheckersPiece required) {
		ArrayList<CheckersMove> captures = new ArrayList<CheckersMove>();
		
		// Check every piece, and examine if it has any legal captures
		for (int i = 0; i < this.length; i++) {
			for (int j = 0; j < this.width; j++) {
				CheckersPiece piece = array[i][j];
				if (piece != null && turn == piece.getPlayer()) {
					
					// If there is a required piece, then ONLY that piece may make captures,
					// but otherwise all pieces may make captures
					if (required == null || piece.equals(required)) {
						
						// Grab all legal captures for piece, and store them in the list
						Set<BoardSquare> legalCapturesForPiece = piece.getAllLegalCapturesForPiece();
						for (BoardSquare destination : legalCapturesForPiece) {
							captures.add(new CheckersMove(piece.position, destination));
						}
					}
				}
			}
		}
		return captures;
	}
	
	// Returns whether any captures are possible for the given player.
	public boolean areAnyCapturesPossible(Player turn, CheckersPiece required) {
		return 0 < this.returnListOfLegalCaptures(turn, required).size();
	}
	
	// Note: only call this if no captures are available, since this method only considers moves (not jumps).
	// Returns whether any steps are possible for the given player.
	public ArrayList<CheckersMove> returnListOfLegalSteps(Player turn) {
		ArrayList<CheckersMove> steps = new ArrayList<CheckersMove>();
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < width; j++) {
				CheckersPiece piece = array[i][j];
				
				// If there is a piece of the right player at the location, add all of its legal
				// steps to the list
				if (null != piece && piece.getPlayer() == turn) {
					Set<BoardSquare> legalStepsForPiece = piece.getAllLegalStepsForPiece();
					for (BoardSquare destination : legalStepsForPiece) {
						steps.add(new CheckersMove(piece.position, destination));
					}
				}
			}
		}
		return steps;
	}
	
	// Note: assumes move is legal!!!
	public void makeMove(CheckersMove move, Player turn, boolean isActualMove) {
		if (null == move) {
			return;
		}
		
		// if this is an actual move (and not analysis by the AI), add this to the move list
		if (isActualMove) {
			if (null != requiredPiece) {
				ArrayList<CheckersMove> last = lastMoves.get(lastMoves.size() - 1);
				last.add(move);
				lastMoves.set(lastMoves.size() - 1, last);
			} else {
				ArrayList<CheckersMove> moveList = new ArrayList<CheckersMove>();
				moveList.add(move);
				lastMoves.add(moveList);
			}
		}
		
		int x1 = move.start.x; int x2 = move.destination.x; int y1 = move.start.y; int y2 = move.destination.y;
		
		// If the move is a capture, modify the statistics on how many pieces are left
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
		
		// Actually move the piece on the board
		array[x2][y2] = array[x1][y1];
		array[x2][y2].move(new BoardSquare(x2, y2));
		array[x1][y1] = null;
		
		// If the move caused the piece to become a king, record that
		if (Player.BLACK == turn && y2 == 0 && !array[x2][y2].isKing()) {
			array[x2][y2].makeKing();
			blackKings++;
			move.madeKing = true;
		}
		if (Player.RED == turn && y2 == 7 && !array[x2][y2].isKing()) {
			array[x2][y2].makeKing();
			redKings++;
			move.madeKing = true;
		}
		
		// Set "currentPiece" to null, to reflect the fact that the user isn't dragging it anymore
		currentPiece = null;
	}
	
	// Undoes the last player's moves (and the last computer moves), for the "undo move" button.
	// If the last move (either player or computer) involved a series of captures, undoes all of them.
	// Returns whether there are actually moves to undo (if the game just started, then returns false).
	public boolean undoSeriesOfMoves() {
		// Check if there is something to undo
		if (lastMoves.size() >= 1) {
			if (requiredPiece == null) { // if this is not during a repeated capture for the player
				
				// Remove the previous player and computer from the list of moves
				ArrayList<CheckersMove> computerMoves = lastMoves.get(lastMoves.size() - 1);
				ArrayList<CheckersMove> lastPlayerMoves = lastMoves.get(lastMoves.size() - 2);
				lastMoves.remove(lastMoves.size() - 1);
				lastMoves.remove(lastMoves.size() - 1);
				
				// Physically undo the moves on the board
				for (int i = computerMoves.size() - 1; i >= 0; i--) {
					undoMove(computerMoves.get(i), Player.RED);
				}
				for (int i = lastPlayerMoves.size() - 1; i >= 0; i--) {
					undoMove(lastPlayerMoves.get(i), Player.BLACK);
				}
				return true;
			} else {
				ArrayList<CheckersMove> lastPlayerMoves = lastMoves.get(lastMoves.size() - 1);
				lastMoves.remove(lastMoves.size() - 1);
				for (int i = lastPlayerMoves.size() - 1; i >= 0; i--) {
					undoMove(lastPlayerMoves.get(i), Player.BLACK);
				}
				requiredPiece = null;
				return true;
			}
		} else {
			return false;
		}
	}
	
	// Undoes the given move for the given player. This is for the "undo move" button,
	// as well as for un-doing moves made in the recursive analysis for the AI.
	public void undoMove(CheckersMove move, Player turn) {
		int x1 = move.start.x; int x2 = move.destination.x; int y1 = move.start.y; int y2 = move.destination.y; CheckersPiece captured = move.captured;
		CheckersPiece moved = array[x2][y2];
		array[x2][y2] = null;
		array[x1][y1] = moved;
		array[x1][y1].move(new BoardSquare(x1, y1));
		
		// Check if the move to be undone was a capture
		if (null != captured) { // capture
			array[(x1+x2)/2][(y1+y2)/2] = captured; // restore captured piece
			
			// Restore counts of pieces
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
		
		// Remove king status, if applicable
		if (Player.BLACK == turn && move.madeKing) {
			array[x1][y1].makeRegular();
			blackKings--;
		}
		if (Player.RED == turn && move.madeKing) {
			array[x1][y1].makeRegular();
			redKings--;
		}
	}
	
	// Returns whether the given square is inside the board.
	public boolean insideBoard(BoardSquare position) {
		if (position.x >= this.width || 0 > position.x|| position.y >= this.length || 0 > position.y) {
			return false;
		}
		return true;
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
	
	public void setMouseLocation(Point point) {
		this.mouseLocation = point;
	}
	
	public CheckersPiece getPieceAtPosition(BoardSquare position) {
		return array[position.x][position.y];
	}
	
	public Color getColor1() {
		return LIGHT_SQUARE_COLOR;
	}
	
	public Color getColor2() {
		return DARK_SQUARE_COLOR;
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
	
	public Player switchTurn(Player turn) {
		if (Player.RED == turn) {
			return Player.BLACK;
		} else {
			return Player.RED;
		}
	}
}