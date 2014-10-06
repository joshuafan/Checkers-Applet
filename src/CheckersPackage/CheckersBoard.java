package CheckersPackage;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class CheckersBoard {
	private CheckersPiece[][] array;
	private int length;
	private int width;
	private Color c1;
	private Color c2;
	private int mouseX;
	private int mouseY;
	private CheckersPiece currentPiece;
	private CheckersPiece requiredPiece;
	
	public int blackPieces; // number of black pieces on board
	public int redPieces;
	public int blackKings;
	public int redKings;
	
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
				if (pieceAtIndex != null) {
					g2.setPaint(pieceAtIndex.getColor());
					if (!pieceAtIndex.equals(currentPiece)) {
						pieceAtIndex.draw(g2);
					}
				}	
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
	
	
	// Determines if the move from (x1, y1) to (x2, y2) is legal
	public boolean isValidMove(int x1, int y1, int x2, int y2, Color turn, CheckersPiece requiredPiece) {
		// check if move occurs within checkers board
		if (x1 > length || x1 < 0 || x2 > length || x2 < 0 || y1 > width || y1 < 0 || y2 > width || y1 < 0) {
			throw new IllegalArgumentException();
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
	
	public boolean areCapturesPossibleForPiece(CheckersPiece piece) {
		int pieceX = piece.getX();
		int pieceY = piece.getY();
		Color pieceColor = piece.getColor();
		return capturePossible(pieceX, pieceY, pieceX + 2, pieceY - 2, pieceColor) ||
				capturePossible(pieceX, pieceY, pieceX - 2, pieceY - 2, pieceColor) ||
				capturePossible(pieceX, pieceY, pieceX + 2, pieceY + 2, pieceColor) ||
				capturePossible(pieceX, pieceY, pieceX - 2, pieceY + 2, pieceColor);	

	}
	
	public boolean areMovesPossibleForPiece(CheckersPiece piece) {
		int pieceX = piece.getX();
		int pieceY = piece.getY();
		Color pieceColor = piece.getColor();
		return movePossible(pieceX, pieceY, pieceX + 1, pieceY - 1, pieceColor) ||
				movePossible(pieceX, pieceY, pieceX - 1, pieceY - 1, pieceColor) ||
				movePossible(pieceX, pieceY, pieceX + 1, pieceY + 1, pieceColor) ||
				movePossible(pieceX, pieceY, pieceX - 1, pieceY + 1, pieceColor);
		
	}
	
	private boolean movePossible(int x1, int y1, int x2, int y2, Color turn) {
		// check if any of the indices are out of bounds
		if (x1 >= length || x1 < 0 || x2 >= length || x2 < 0 || y1 >= width || y1 < 0 || y2 >= width || y2 < 0) {
			return false;
		}
		
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
	
	// Note: assumes move is legal!!!
	public void makeMove(int x1, int y1, int x2, int y2, Color turn) {
		if (Math.abs(x1 - x2) == 2) { // capture
			CheckersPiece capturedPiece = array[(x1 + x2) / 2][(y1 + y2) / 2];
			if (capturedPiece.getColor() == Color.RED) {
				redPieces--;
				if (capturedPiece.isKing()) {
					redKings--;
				}
			}
			
			if (capturedPiece.getColor() == Color.BLACK){
				blackPieces--;
				if (capturedPiece.isKing()) {
					blackKings--;
				}
			}
			array[(x1 + x2) / 2][(y1 + y2) / 2] = null; // remove captured piece, if move was a capture
			
		}
		array[x2][y2] = array[x1][y1];
		array[x2][y2].setX(x2);
		array[x2][y2].setY(y2);
		array[x1][y1] = null;
		
		if (turn.equals(Color.BLACK) && y2 == 0) {
			array[x2][y2].makeKing();
			blackKings++;
		}
		if (turn.equals(Color.RED) && y2 == 7) {
			array[x2][y2].makeKing();
			redKings++;
		}
	}
	
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
}