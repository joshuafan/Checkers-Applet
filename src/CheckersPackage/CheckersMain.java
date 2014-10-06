package CheckersPackage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class CheckersMain extends JApplet {	
	private CheckersBoardComponent component;
	private JPanel buttonPanel;
	
	public void init() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				component = new CheckersBoardComponent();
				buttonPanel = new JPanel();
				JButton newGameButton = new JButton("New Game");
				newGameButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						component.newGame();
					}
				});
				
				buttonPanel.add(newGameButton);
				add(component);
				add(buttonPanel, BorderLayout.SOUTH);
			}
		});
	}
}
