package CheckersPackage;

import java.awt.Graphics2D;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class CheckersPiece {
	private Color color;
	private int x;
	private int y;
	private int squareLength;
	private boolean isKing;
	
	public CheckersPiece(Color color, int x, int y, int squareLength) {
		this(color, x, y, squareLength, false);
	}
	
	public CheckersPiece(Color color, int x, int y, int squareLength, boolean isKing) {
		this.color = color;
		this.x = x;
		this.y = y;
		this.squareLength = squareLength;
		this.isKing = isKing;
	}
	
	public void move(int x2, int y2) {
		this.x = x2;
		this.y = y2;
	}
	
	// Draws the piece in the center of the square it is in
	public void draw(Graphics2D g2) { 
		Ellipse2D.Double ellipse = new Ellipse2D.Double(squareLength * (x + 0.2), squareLength * (y + 0.2), squareLength * 0.6, squareLength * 0.6);
		g2.setPaint(color);
		g2.fill(ellipse);
		
		if (isKing) {
			Ellipse2D.Double kingEllipse = new Ellipse2D.Double(squareLength * (x + 0.3), squareLength * (y + 0.3), squareLength * 0.4, squareLength * 0.4);
			g2.setPaint(Color.WHITE);
			g2.draw(kingEllipse);
		}
	}
	
	// Draws the piece if it is being dragged around, centered at the current point
	public void drawCenteredAtGivenPoint(Graphics2D g2, int centerX, int centerY) { 
		Ellipse2D.Double ellipse = new Ellipse2D.Double(centerX - (squareLength * 0.3), centerY - (squareLength * 0.3), squareLength * 0.6, squareLength * 0.6);
		g2.setPaint(color);
		g2.fill(ellipse);
		
		if (isKing) {
			Ellipse2D.Double kingEllipse = new Ellipse2D.Double(centerX - (squareLength * 0.2), centerY - (squareLength * 0.2), squareLength * 0.4, squareLength * 0.4);
			g2.setPaint(Color.WHITE);
			g2.draw(kingEllipse);
		}
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
		
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public boolean isKing() {
		return isKing;
	}
	
	public void makeKing() {
		isKing = true;
	}
}
