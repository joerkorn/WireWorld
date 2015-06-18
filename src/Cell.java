
public class Cell<anyType> {
	
	private int row, col, key; // Row and Column of simulated 2d array, key set by SparseMatrix Object
	private anyType val;

	public Cell(int r, int c, anyType v) {
		row = r;
		col = c;
		val = v;
		key = 0;
	}


	public int getRow() {
		return row;
	}

	public void setKey(int k) {
		key = k;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	// returns generic type value contained in Cell
	public anyType getVal() {
		return val;
	}

	public void setVal(anyType v) {
		this.val = v;
	}

	public int getKey() {
		return this.key;
	}

    @Override
    public String toString() {
        return (String)val;
    }
}
