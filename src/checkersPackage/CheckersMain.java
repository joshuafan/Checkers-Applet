package checkersPackage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

// A high-level driver class that sets up the graphics behind the checkers game.
@SuppressWarnings("serial")
public class CheckersMain extends JApplet {	
	private CheckersBoardComponent component; // the main component containing the board
	private JPanel buttonPanel; // panel containing "undo move" and "new game" buttons
	
	// Initializes the graphics associated with the checkers game
	public void init() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Create a CheckersBoardComponent that wraps most of the gameplay
				component = new CheckersBoardComponent();
				
				// Create the button panel, with New Game and Undo Move buttons (each with
				// a method callback for when the button is pressed)
				buttonPanel = new JPanel();
				JButton newGameButton = new JButton("New Game");
				newGameButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						component.newGame();
					}
				});
				JButton undoMoveButton = new JButton("Undo move");
				undoMoveButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						component.undoSeriesOfMoves();
					}
				});
				buttonPanel.add(newGameButton);
				buttonPanel.add(undoMoveButton);
				
				// Add the button panel and the CheckersBoardComponent to the window
				add(component);
				add(buttonPanel, BorderLayout.SOUTH);
			}
		});
	}
}
