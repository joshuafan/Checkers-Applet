package CheckersPackage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class CheckersMain extends JApplet {	
	private CheckersBoardComponent component; // the main component containing the board
	private JPanel buttonPanel; // panel containing "undo move" and "new game" butons
	
	public void init() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				component = new CheckersBoardComponent();
				
				// create the button panel
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
				add(component);
				add(buttonPanel, BorderLayout.SOUTH);
			}
		});
	}
}
