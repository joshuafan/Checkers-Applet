package checkersPackage;

// Represents a square on the board, for the back-end. The reason why this class exists
// (instead of simply using Point, which has the same fields) is because in this program,
// Point is mainly used for front-end contexts (such as the pixel where the mouse is
// pointing to). However, BoardSquare has a different semantic meaning.
public class BoardSquare {
	int x;
	int y;
	
	public BoardSquare(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	// Two squares are considered equal if they occupy the same board position
	@Override
	public boolean equals(Object other) {
		if (other == null || other.getClass() != this.getClass()) {
			return false;
		}
		BoardSquare otherSquare = (BoardSquare) other;
		return this.x == otherSquare.x && this.y == otherSquare.y;
	}
	
	@Override
	public int hashCode() {
		return 10 * x + y;
	}
}
