package checkersPackage;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// A Component that wraps the entire checkers game and allows the
// user to drag and drop moves.
@SuppressWarnings("serial")
public class CheckersBoardComponent extends JComponent {
	public static final int SQUARE_LENGTH = 70; // side-length of each square in board
	public boolean AI;
	private CheckersBoard board;
	private Player turn;
	private String message;
	private boolean gameOver;
	
	// Constructs a CheckersBoardComponent object
	public CheckersBoardComponent() {
		this.board = new CheckersBoard();
		this.addMouseListener(new MouseHandler());
		this.addMouseMotionListener(new MouseMotionHandler());
		this.turn = Player.BLACK;
		this.gameOver = false;
		this.AI = true;
	}
	
	// Paints the component. Calls the CheckersBoard's paintBoard method to paint most of the board, but also prints 
	// messages saying if a player has won yet, who is to move, or if captures are required.
	public void paintComponent(Graphics g) {
		// Paint the board itself
		Graphics2D g2 = (Graphics2D) g;
		board.paintBoard(g2);
		
		// Adjust the message, if a player has lost all pieces
		if (0 == board.blackPieces) {
			gameOver = true;
			message = "GAME OVER. Sorry, you lost.";
		} else if (0 == board.redPieces) {
			gameOver = true;
			message = "GAME OVER. You won!";
		}
		
		// Print Illegal Move or Game Over message, if applicable
		if (message != null) {
			g2.drawString(message, 10, board.getWidth() * SQUARE_LENGTH + 20);
		}
		
		// Print who is to move, if the game isn't over yet
		String turnString = "";
		if (!gameOver) {
			if (Player.BLACK == turn) {
				turnString = "Your move!";
				if (board.areAnyCapturesPossible(turn)) {
					turnString += " (a capture is required)";
				}
				g2.setPaint(Color.BLACK);
			} else {
				turnString = "The computer is thinking...";
			}
		} else {
			turnString += "Press 'New Game' to play again.";
		}
		g2.drawString(turnString, 10, board.getWidth() * SQUARE_LENGTH + 40);
	}
	
	// Handles events where the mouse is pressed or dragged.
	private class MouseHandler extends MouseAdapter {
		
		// Handles the event where the mouse is pressed. 
		public void mousePressed(MouseEvent event) {
			if (!gameOver) {
				message = "";
				
				// Find which piece the user is clicking on, and if that piece is of the
				// correct color, set the board's current piece to that piece 
				CheckersPiece selectedPiece = board.find(event.getPoint(), SQUARE_LENGTH, turn);
				if (null != selectedPiece && Player.BLACK == selectedPiece.getPlayer())  {
					board.setCurrentPiece(selectedPiece);
				}
			}
		}
		
		// Handles the event where the mouse is released.
		public void mouseReleased(MouseEvent event) {
			if (!gameOver) {
				message = "";
				
				// Calculate which square the user attempted to move the piece to
				BoardSquare destination = new BoardSquare(event.getX() / SQUARE_LENGTH,
						                                  event.getY() / SQUARE_LENGTH);
				
				// Check if the user attempted to move the piece off the board
				if (!board.insideBoard(destination)) {
					board.setCurrentPiece(null);
				}
				
				// Figure out what move the player intended to make
				CheckersPiece currentPiece = board.getCurrentPiece();
				if (null != currentPiece) {
					BoardSquare start = currentPiece.getPosition();
					
					// Check if the intended move is legal
					if (board.isLegalMove(start, destination, turn)) {
						// If the move is legal, make the move
						board.makeMove(new CheckersMove(start, destination), turn, true);
						board.setCurrentPiece(null);
						
						// If the move was a step or if the move was a jump and no further captures are available: switch turn
						if (1 == Math.abs(start.y - destination.y) || !currentPiece.areAnyCapturesPossibleForPiece()) {
							board.setRequiredPiece(null);
							
							// If the human player just made a move, make the computer move and then allow the human
							// player to make the next move
							if (Player.BLACK == turn){
								turn = Player.RED;
								repaint();
								checkIfAreAnyMovesPossible(turn);
								if (gameOver) {
									return;
								}
								board.makeAIMove(Player.RED);
								turn = Player.BLACK;
								repaint();
							} else {
								turn = Player.BLACK;
							}
							checkIfAreAnyMovesPossible(turn);
							
						// Otherwise: require additional capture(s)	if further captures are available and the move was a jump
						} else {
							board.setRequiredPiece(currentPiece);
							message = "Note: additional captures are required.";
						}
					} else {
						board.setCurrentPiece(null);
						
						// If the user attempted to make an illegal move, display a message
						if (!start.equals(destination)) { 
							message = "Illegal move. Try again.";
						}
					}
				}
				repaint();
			}
		}
	}
	
	// Handles events where the mouse is dragged or moved.
	private class MouseMotionHandler implements MouseMotionListener {
		
		// Handles the event where the mouse is dragged
		public void mouseDragged(MouseEvent event) {
			// If the user is dragging a piece, center it to the current mouse location
			if (null != board.getCurrentPiece()) {
				board.setMouseLocation(new Point(event.getX(), event.getY()));
				repaint();
			}
		}
		
		// Handles the event where the mouse is moved, by changing the cursor to a hand cursor if
		// the user is pointing at a piece
		public void mouseMoved(MouseEvent event) {
			if (board.find(event.getPoint(), SQUARE_LENGTH, turn) != null && !gameOver) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}
	
	// This method is called when the "New Game" button is pressed, and resets the game.
	public void newGame() {
		
		// Reset pieces to their starting positions
		board.removeAllPieces();
		board.addInitialPieces();
		
		// Clean up the state of the game
		board.removeLastMoves();
		board.setRequiredPiece(null);
		turn = Player.BLACK;
		gameOver = false;
		message = "";
		repaint();
	}
	
	// Sets the displayed message to the given value.
	public void setMessage(String message) {
		this.message = message;
	}
	
	// Undoes the last user move.
	public void undoSeriesOfMoves() {
		boolean somethingToUndo = board.undoSeriesOfMoves();
		if (!somethingToUndo) {
			message = "Nothing to undo!";
		}
		
		// If the game just ended, but the user undid their move, indicate that the 
		// game is no longer over thanks to the undo
		if (gameOver) {
			gameOver = false;
			message = "";
		}
		repaint();
	}
	
	// Check if the player who is now to move actually has any moves available
	public void checkIfAreAnyMovesPossible(Player turn) {
		if (!board.areAnyMovesPossible(turn)) {
			gameOver = true;
			if (Player.BLACK == turn) {
				message = "GAME OVER. Sorry, you lost.";
			} else {
				message = "GAME OVER. You won!";
			}
		}
	}
}
