import java.awt.*;

import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class WireWorld extends JPanel implements MouseListener, MouseMotionListener, ActionListener, ChangeListener {
	
	SparseMatrix<Integer> wireBoard; // sparse matrix for game board. value 1 = conductor, 2 = head, 3 = tail, left empty for empty cell
	private int tick = 0; // current generation of board state, incremented by 1 each step
	private static final int ROWS = 50; // number of board rows TODO: Add customization
	private static final int COLS = 50; // number of board columns TODO: Add customization
    protected static int mouseX = 0; // X - coordinate for mouse in JFrame
    protected static int mouseY = 0; // Y - coordinate for mouse in JFrame
	private ImageIcon empty = new ImageIcon("empty.png"); // empty cell image
	private ImageIcon wire = new ImageIcon("wire.png");
	private ImageIcon head = new ImageIcon("head.png"); // cell images
	private ImageIcon tail = new ImageIcon("tail.png");
    private ImageIcon wirepaint = new ImageIcon("wirepaint.png");
    private ImageIcon headpaint = new ImageIcon("headpaint.png"); // paint selector icon images
    private ImageIcon tailpaint = new ImageIcon("tailpaint.png");
	private static final int SIZE = 10; // scaling factor for board elements
	private static final int MINSPEED = 10; // minimum timer delay in ms, regulated by JSlider 'speed'
	private static final int MAXSPEED = 1000; // maximum timer delay in ms, regulated by JSlider 'speed' TODO: Center value, possible reinvention of JSlider to remove overhead
	private static final int INITSPEED = 100; // initial timer delay in ms, regulated by JSlider 'speed'
	private static int SPEED = INITSPEED; // current timer delay in ms
    private static int current_paint_sel = 1; // current selection for drawing elements, toggled by JButtons
	private boolean isStarted; // true if timer is on and ticks are advancing, false otherwise
	private JButton toggle; // toggle simulation start/pause
    private JButton wirebtn;
    private JButton headbtn; // paint selectors
    private JButton tailbtn;
    private JButton save; // save current board
    private JButton load; // load board from .dat file
    private JSlider speed; // regulates time between ticks in ms. value sent to timer
    private JLabel tickdisplay; // label shows current tick count denoted in 'tick'
    private Timer t; // regulates time in between ticks

	
	public WireWorld() {
		wireBoard = new SparseMatrix<Integer>(50,50); // new game board of size 50 TODO: Add prompt for desired board size
        addMouseListener(this);
        addMouseMotionListener(this); // mouse controls
        tickdisplay = new JLabel(); add(tickdisplay); tickdisplay.setOpaque(true); tickdisplay.setBackground(Color.BLACK); tickdisplay.setForeground(Color.green); tickdisplay.setText("Tick: " + tick); // tick Jlabel
        toggle = new JButton("Start"); add(toggle); toggle.setBackground(Color.GREEN); toggle.addActionListener(this); // toggle button, timer is stopped, prompts user with option to "Start" at launch
        wirebtn = new JButton(wirepaint); add(wirebtn); wirebtn.setBorder(null); wirebtn.addActionListener(this);
        headbtn = new JButton(headpaint); add(headbtn); headbtn.setBorder(null); headbtn.addActionListener(this); // paint buttons
        tailbtn = new JButton(tailpaint); add(tailbtn); tailbtn.setBorder(null); tailbtn.addActionListener(this);
        save = new JButton("Save"); add(save); save.addActionListener(this);
        load = new JButton("Load"); add(load); load.addActionListener(this); // save/load controls
        speed = new JSlider(JSlider.HORIZONTAL, MINSPEED, MAXSPEED, INITSPEED); add(speed); speed.addChangeListener(this); //JSlider ranging
		t = new Timer(INITSPEED, new Listener()); // sets up timer with initial delay, not yet started
	}

    // pre: timer is NOT started
    // post: timer is started and toggle JButton switched to stop prompt configuration
	public void start() {
		t.start(); //start tick advancement
		toggle.setText("Stop"); // make button prompt with "Stop"
		toggle.setBackground(Color.RED);
		isStarted = true;
	}

    // pre: timer is running
    // post: timer is stopped and JButton switched to start prompt configuration
	public void stop() {
		t.stop(); // halt tick advancement
		toggle.setText("Start"); // make button prompt with "Start"
		toggle.setBackground(Color.GREEN);
		isStarted = false;
	}

    // pre: wireBoard is a valid sparse matrix, root project directory is writeable
    // post: binary file representing sparse matrix as values, 0 for unused cell O(n)
    public void saveToFile(String filename) throws IOException {
        System.setOut(new PrintStream(new FileOutputStream(filename)));
        for(int r = 0; r < ROWS; r++)
            for(int c = 0; c < COLS; c++) // for each cell of the board
                if(wireBoard.contains(r, c)) // add a value corresponding with wireBoard
                    System.out.println(wireBoard.get(r, c));
                else                         // if the cell is empty
                    System.out.println("0");
    }

    // pre: file sent as parameter exists, in same format as dictated by SaveToFile()
    // post: wireBoard nonzero values from file, a 0 will not be written and sparse matrix maintained O(n)
    public void readFile(String fileName) throws IOException {
        Scanner input = new Scanner(new FileReader(fileName));
        int next = 0;
        for(int r = 0; r < ROWS; r++)
            for(int c = 0; c < COLS; c++) { //for each cell
                next = input.nextInt();
                if(next != 0) {             //if the file at specified index is nonzero
                    if (wireBoard.contains(r, c)) // add or set the value to the wireBoard
                        wireBoard.set(r, c, next);
                    else
                        wireBoard.add(r, c, next);
                }
            }
        input.close();
    }

    // paints WireWorld object (buttons & array)
    public void paintComponent(Graphics g)
    {
       super.paintComponent(g);
       g.fillRect(0, 0, (wireBoard.getRows() * SIZE), (wireBoard.getCols() * SIZE)); //  scaling images
       showBoard(g);					//draw the contents of the array board on the screen
    }

    // pre: wireBoard of any given generation
    // post: 1. a conductor (value 1) will become an electron (value 2) if and only if exactly 1 or 2 cells in its Moore Neighborhood were electrons (value 2) in the previous generation
    //       2. an electron (value 2) will become an electron tail (value 3)
    //       3. an electron tail (value 3) will become a conductor (value 1) O(n)
    public void advanceTick() {
        tick++; //increment tick counter
        tickdisplay.setText("Tick: " + tick);
    	for(int r=0; r<ROWS; r++) {
    		for(int c=0; c<COLS; c++) {
    			if(wireBoard.contains(r,c))
    				if(wireBoard.get(r, c) == 1 && hasElectronNeighbor(r, c)) // if a cell is 1 and has one or two neighbos of value 2
    					wireBoard.set(r, c, 4); // temporarily set to 4 for elimination of previous generation electrons
    		}
    	}

    	for(int r=0; r<ROWS; r++) {
    		for(int c=0; c<COLS; c++) {
    			if(wireBoard.contains(r, c)) {
    				if(wireBoard.get(r, c) == 2)
    					wireBoard.set(r, c, 3); // electron head becomes electron tail
    				else if(wireBoard.get(r, c) == 3)
    					wireBoard.set(r, c, 1);
    			}
    		}
    	}

    	for(int r=0; r<ROWS; r++)
    		for(int c=0; c<COLS; c++)
    			if(wireBoard.contains(r, c))
    				if(wireBoard.get(r, c) == 4) // temporary value of 4 becomes an electron
    					wireBoard.set(r, c, 2);
    }

    // pre: wireBoard contains a conductor at r, c
    // post: if exactly one two neighboring cells are electrons, return true, otherwise return false O(n)
    public boolean hasElectronNeighbor(int r, int c) {
    	int trueCount = 0;
    	if(wireBoard.contains((r-1), (c-1)))
    		if(wireBoard.get((r-1), (c-1)) == 2) // traverse moore neighborhood
    			trueCount++; // add to truecount if true
    	
    	if(wireBoard.contains((r-1), c))
    		if(wireBoard.get((r-1), c) == 2)
    			trueCount++;
    	
    	if(wireBoard.contains((r-1), (c+1)))
    		if(wireBoard.get((r-1), (c+1)) == 2)
    			trueCount++;
    	
    	if(wireBoard.contains(r, (c-1)))
    		if(wireBoard.get(r, (c-1)) == 2)
    			trueCount++;
    	
    	if(wireBoard.contains(r, (c+1)))
    		if(wireBoard.get(r, (c+1)) == 2)
    			trueCount++;
    	
    	if(wireBoard.contains((r+1), (c-1)))
    		if(wireBoard.get((r+1), (c-1)) == 2)
    			trueCount++;
    	
    	if(wireBoard.contains((r+1), c))
    		if(wireBoard.get((r+1), c) == 2)
    			trueCount++;
    	
    	if(wireBoard.contains((r+1), (c+1)))
    		if(wireBoard.get((r+1), (c+1)) == 2)
    			trueCount++;
    	
    	return trueCount == 1 || trueCount == 2; // return true if truecount is 1 or 2
    }

    // action listener for buttons
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == toggle) { //Controller for toggle
            if(isStarted) {
                stop();
                tick = 0;
            }

            else
                start();
        }

        else if(e.getSource() == wirebtn) {
            current_paint_sel = 1;
        }

        else if(e.getSource() == headbtn) { // paint selector buttons
            current_paint_sel = 2; // change paint select to specified cell value
        }

        else if(e.getSource() == tailbtn) {
            current_paint_sel = 3;
        }

        else if(e.getSource() == save) { // save button
            try {
                saveToFile("world.dat"); //in root project directory
            } catch(IOException a){
                System.out.println("Failed to save file");
                a.printStackTrace();
            }
        }

        else if(e.getSource() == load) { //load button
            try {
                readFile("world.dat"); // in root project directory
            } catch(IOException a) {
                System.out.println("Failed to read file");
                a.printStackTrace();
            }
        }
    }
    // graphical representation of SparseMatrix wireBoard O(n)
	public void showBoard(Graphics g) {
		int x = 0; int y = 0;
		for(int r=0; r<ROWS; r++) {
			x = 0;
			for(int c=0; c<COLS; c++) {
				if(wireBoard.contains(r, c)) {
					if(wireBoard.get(r, c) == 1)
						g.drawImage(wire.getImage(), x, y, SIZE, SIZE, null); // draw respective imageIcons scaled by SIZE
					else if(wireBoard.get(r, c) == 2)
						g.drawImage(head.getImage(), x, y, SIZE, SIZE, null);
					else if(wireBoard.get(r, c) == 3)
						g.drawImage(tail.getImage(), x, y, SIZE, SIZE, null);
				}

				else
					g.drawImage(empty.getImage(), x, y, SIZE, SIZE, null);
				x+= SIZE;
			}
			 y+= SIZE;
		}
	}

    // if an action occurs, advance the tick and call paintComponent again to update the display
   private class Listener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         advanceTick();
         repaint();
      }
   }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO: add functionality
    }

    @Override
    // update fields x and y, corresponding with mouseR and mouseC when mouse is moved
    public void mouseMoved(MouseEvent e) {
	    mouseX = e.getX();
	    mouseY = e.getY();
	    int mouseR = mouseY / SIZE;
	    int mouseC = mouseX / SIZE;
	    repaint();
    }

    @Override
    // called on mouse click
    public void mouseClicked(MouseEvent e) {
	    int button = e.getButton();
	    int mouseR = mouseY / SIZE;
	    int mouseC = mouseX / SIZE;
	    if(button == MouseEvent.BUTTON1) // if the left button is clicked
            if(current_paint_sel == 1) // get paint selector value(toggled by jButtons)
                if(wireBoard.contains(mouseR, mouseC))
                    wireBoard.set(mouseR, mouseC, 1);
                else
                    wireBoard.add(mouseR, mouseC, 1);
            else if(current_paint_sel == 2)
                if(wireBoard.contains(mouseR, mouseC))
                    wireBoard.set(mouseR, mouseC, 2);
                else
                    wireBoard.add(mouseR, mouseC, 2);
            else if(current_paint_sel == 3)
                if(wireBoard.contains(mouseR, mouseC))
                    wireBoard.set(mouseR, mouseC, 3);
                else
                    wireBoard.add(mouseR, mouseC, 3);
	    if(button == MouseEvent.BUTTON3)
		    if(wireBoard.contains(mouseR, mouseC))
			    wireBoard.remove(mouseR, mouseC);
	    repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    // handle slider events, change current speed when slider changes
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) { // while it is not being adjusted
            SPEED = source.getValue();
            t.setDelay(SPEED);
        }
    }
}

