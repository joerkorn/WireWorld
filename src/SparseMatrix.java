import java.util.ArrayList;


public class SparseMatrix<anyType> {
	
	private ArrayList<Cell> matrix;
	private int rows, cols; // maximum simulated rows and columns

	public SparseMatrix(int r, int c) {
		matrix = new ArrayList<>();
		rows = r;
		cols = c;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public int size() {
		return this.matrix.size();
	}

	// pre: matrix is an ArrayList of any size
	// post: returns value at row, col; null if nonexistent O(n)
	public anyType get(int r, int c) {
		for(Cell i : this.matrix)
			if(i.getRow() == r && i.getCol() == c)
				return (anyType) i.getVal();
		return null;
	}

	// pre: r and c are within bounds of fields 'rows' and 'cols' respectively
	// post: simulated addition to 2d ArrayList, sorted by key (current row * cols + current column)
	public void add(int r, int c, anyType v) {
		int i = 0;
		while((matrix.size() > 0) && (i<matrix.size()) && (matrix.get(i).getKey() > (r * this.cols + 1)))
			i++;
		matrix.add(new Cell(r, c, v));
		matrix.get(i).setKey(r * cols + c);
	}

	// pre: matrix contains value at specified r, c
	// post: matrix holds new value at r, c, returns old value, null if nonexistent row and column O(n)
	public anyType set(int r, int c, anyType v) {
		Cell temp;
		for(int i=0; i<matrix.size(); i++)
			if(this.matrix.get(i).getRow() == r && this.matrix.get(i).getCol() == c) {
				temp = matrix.get(i);
				matrix.set(i, new Cell(r, c, v));
				return (anyType) temp.getVal();
			}
		return null;
	}

	// pre: matrix contains value at specified r, c
	// post: matrix no longer contains value at r, c, old value returned, null if nonexistent O(n)
	public anyType remove(int r, int c) {
		Cell temp;
		for(int i=0; i<matrix.size(); i++)
			if(this.matrix.get(i).getRow() == r && this.matrix.get(i).getCol() == c) {
				temp = matrix.get(i);
				matrix.remove(i);
				return (anyType) temp.getVal();
			}
		return null;
	}

	// pre: matrix is an ArrayList of any size, may or may not contain value at r, c
	// post: returns true if value found, false if r, c is empty O(n)
	public boolean contains(int r, int c) {
		for(Cell i : this.matrix)
			if(i.getRow() == r && i.getCol() == c)
				return true;
		return false;
	}

	// Grid-like representation of ArrayList, empty cells represented as '-' O(n)
	@Override
	public String toString() {
		String str = "";
		for(int r=0; r<this.rows; r++) {
			str += "\n";
			for(int c=0; c<this.cols; c++) {
				if(this.contains(r, c))
					str+= this.get(r, c).toString();
				else
					str+= "-";
			}
		}
		return str;
	}
}
