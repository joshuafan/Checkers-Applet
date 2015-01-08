package CheckersPackage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class CheckersFrame extends JPanel {
	private JPanel buttonPanel;
	private CheckersBoardComponent component;
	
	public CheckersFrame() {
		component = new CheckersBoardComponent();
		buttonPanel = new JPanel();
		JButton newGameButton = new JButton("New Game");
		newGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				component.newGame();
			}
		});
		
		add(component);
		add(buttonPanel, BorderLayout.SOUTH);
	}
}
