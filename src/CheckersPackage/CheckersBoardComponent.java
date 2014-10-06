package CheckersPackage;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class CheckersBoardComponent extends JComponent {
	public static final int SQUARE_LENGTH = 50; // sidelength of each square in board
	private CheckersBoard board;
	private Color turn;
	private String message;
	private boolean gameOver;
	
	// Constructs a CheckersBoardComponent object
	public CheckersBoardComponent() {
		this.board = new CheckersBoard();
		addMouseListener(new MouseHandler());
		addMouseMotionListener(new MouseMotionHandler());
		this.turn = Color.BLACK;
		this.gameOver = false;
	}
	
	// Paints the component. Calls the CheckersBoard's paintBoard method to paint most of the board, but also prints 
	// messages saying if a player has won yet, who is to move, or if captures are required.
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		board.paintBoard(g2);
		
		if (board.blackPieces == 0) {
			gameOver = true;
			message = "GAME OVER. Red won!";
		} else if (board.redPieces == 0) {
			gameOver = true;
			message = "GAME OVER. Black won!";
			
		}
		// Messages
		g2.setPaint(Color.RED);
		if (message != null) {
			g2.drawString(message, 10, board.getWidth() * SQUARE_LENGTH + 20); // print Illegal Move message or Game Over
		}
		String turnString = "";
		if (!gameOver) {
			if (turn.equals(Color.BLACK)) {
				turnString = "Black";
				g2.setPaint(Color.BLACK);
			} else {
				turnString = "Red";
			}
			turnString += " to move";
			if (board.areAnyCapturesPossible(turn)) {
				turnString += " (a capture is required)";
			}
		} else {
			turnString += "Press 'New Game' to play again.";
		}
		
		g2.drawString(turnString, 10, board.getWidth() * SQUARE_LENGTH + 40); // print who is to move.
	}
	
	private class MouseHandler extends MouseAdapter {
		public void mousePressed(MouseEvent event) {
			if (!gameOver) {
				message = "";
				CheckersPiece selected = board.find(event.getPoint(), SQUARE_LENGTH, turn);
				if (selected != null) {
					board.setCurrentPiece(selected);
				}
			}
		}
		
		public void mouseReleased(MouseEvent event) {
			if (!gameOver) {
				int destinationX = event.getX() / SQUARE_LENGTH; 
				int destinationY = event.getY() / SQUARE_LENGTH;
				if (destinationX > board.getLength() || destinationY > board.getWidth()) { // mouse is "out-of-bounds"
					board.setCurrentPiece(null);
				}
				CheckersPiece currentPiece = board.getCurrentPiece();
				CheckersPiece requiredPiece = board.getRequiredPiece();
				if (currentPiece != null) {
					int originX = currentPiece.getX();
					int originY = currentPiece.getY();
					if (board.isValidMove(originX, originY, destinationX, destinationY, turn, requiredPiece)) {
						currentPiece.setX(destinationX);
						currentPiece.setY(destinationY);
						board.makeMove(originX, originY, destinationX, destinationY, turn);
						board.setCurrentPiece(null);
						
						// if the move was a step or if the move was a jump and no further captures are available: switch turn
						if (Math.abs(originY - destinationY) == 1 || !board.areCapturesPossibleForPiece(currentPiece)) {
							board.setRequiredPiece(null);
							// Change turn
							if (turn.equals(Color.BLACK)){
								turn = Color.RED;
							} else {
								turn = Color.BLACK;
							}
							
							// check if the player who is now to move actually has any moves available
							if (!board.areAnyMovesPossible(turn)) {
								gameOver = true;
								if (turn.equals(Color.BLACK)) {
									message = "GAME OVER. Red won!";
								} else {
									message = "GAME OVER. Black won!";
								}
							}
						// otherwise: require additional capture(s)	if further captures are available and the move was a jump
						} else { // check if the prior move was a capture
							board.setRequiredPiece(currentPiece);
							message = "Note: additional captures are required.";
						}
					} else {
						board.setCurrentPiece(null);
						message = "Illegal move. Try again.";
					}
				}
				repaint();
			}
		}
	}
	
	private class MouseMotionHandler implements MouseMotionListener {
		public void mouseDragged(MouseEvent event) {
			if (board.getCurrentPiece() != null) {
				board.setMouseX(event.getX());
				board.setMouseY(event.getY());
				repaint();
			}
		}
		
		public void mouseMoved(MouseEvent event) {
			if (board.find(event.getPoint(), SQUARE_LENGTH, turn) != null) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}
	
	public void newGame() {
		board.removeAllPieces();
		board.addInitialPieces();
		turn = Color.BLACK;
		gameOver = false;
		message = "";
		repaint();
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
