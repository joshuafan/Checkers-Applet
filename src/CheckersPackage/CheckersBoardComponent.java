package CheckersPackage;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class CheckersBoardComponent extends JComponent {
	public static final int SQUARE_LENGTH = 50; // sidelength of each square in board
	public boolean AI;
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
		this.AI = true;
	}
	
	// Paints the component. Calls the CheckersBoard's paintBoard method to paint most of the board, but also prints 
	// messages saying if a player has won yet, who is to move, or if captures are required.
	public void paintComponent(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		board.paintBoard(g2);
		
		if (board.blackPieces == 0) {
			gameOver = true;
			message = "GAME OVER. Sorry, you lost.";
		} else if (board.redPieces == 0) {
			gameOver = true;
			message = "GAME OVER. You won!";
		}
		// Messages
		if (message != null) {
			g2.drawString(message, 10, board.getWidth() * SQUARE_LENGTH + 20); // print Illegal Move message or Game Over
		}
		String turnString = "";
		if (!gameOver) {
			if (turn.equals(Color.BLACK)) {
				turnString = "Your move!";
				if (board.areAnyCapturesPossible(turn)) {
					turnString += " a capture is required)";
				}
				g2.setPaint(Color.BLACK);
			} else {
				turnString = "The computer is thinking...";
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
				if (selected != null && selected.getColor().equals(Color.BLACK)) {
					board.setCurrentPiece(selected);
				}
			}
		}
		
		public void mouseReleased(MouseEvent event) {
			if (!gameOver) {
				message = "";
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
						board.makeMove(new CheckersMove(originX, originY, destinationX, destinationY), turn, true);
						board.setCurrentPiece(null);
						
						// if the move was a step or if the move was a jump and no further captures are available: switch turn
						if (Math.abs(originY - destinationY) == 1 || !board.areCapturesPossibleForPiece(currentPiece)) {
							board.setRequiredPiece(null);
							// Change turn
							if (turn.equals(Color.BLACK)){
								turn = Color.RED;
								repaint();
								checkIfAreAnyMovesPossible(turn);
								board.makeAIMove(Color.RED);
								turn = Color.BLACK;
								repaint();
							} else {
								turn = Color.BLACK;
							}
							checkIfAreAnyMovesPossible(turn);
							
						// otherwise: require additional capture(s)	if further captures are available and the move was a jump
						} else { // check if the prior move was a capture
							board.setRequiredPiece(currentPiece);
							message = "Note: additional captures are required.";
						}
					} else {
						board.setCurrentPiece(null);
						
						// this test is designed to avoid annoying the user in case they accidentally
						// clicked on a piece
						if (originX != destinationX && originY != destinationY) { 
							message = "Illegal move. Try again.";
						}
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
		board.removeLastMoves();
		board.setRequiredPiece(null);
		turn = Color.BLACK;
		gameOver = false;
		message = "";
		repaint();
	}

	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void undoSeriesOfMoves() {
		boolean somethingToUndo = board.undoSeriesOfMoves();
		if (!somethingToUndo) {
			message = "Nothing to undo!";
		}
		if (gameOver) {
			gameOver = false;
		}
		repaint();
	}
	
	// check if the player who is now to move actually has any moves available
	public void checkIfAreAnyMovesPossible(Color turn) {
		if (!board.areAnyMovesPossible(turn)) {
			gameOver = true;
			if (turn.equals(Color.BLACK)) {
				message = "GAME OVER. Sorry, you lost.";
			} else {
				message = "GAME OVER. You won!";
			}
		}
	}
}
