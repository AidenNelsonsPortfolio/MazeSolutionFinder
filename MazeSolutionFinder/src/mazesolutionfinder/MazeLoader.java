// File             : MazeLoader.java
// Author           : Aiden Nelson && David W. Collins Jr.
// Date Created     : 03/01/2016 
// Last Modified    : By Aiden Nelson on 3/18/2022
// Description      : This is the MazeLoader file for Math 271 where students
//                    will implement the recursive routine to "solve" the maze. 
//                    The timer will then show the solution progressively!
package mazesolutionfinder;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;


/** This is the main class that defines the window to load the maze
 * 
 * @author collindw
 */
public class MazeLoader {
    
    private JFrame window;
    private Scanner fileToRead;
    private JPanel[][] grid;
    private static final Color WALL_COLOR = Color.BLUE.darker();
    private static final Color PATH_COLOR = Color.GREEN.brighter();
    private static final Color OPEN_COLOR = Color.WHITE;
    private static final Color BAD_PATH_COLOR  = Color.RED;
    private static int ROW;
    private static int COL;
    private String data;
    private Point start;
    private boolean allowMazeUpdate;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem[] loadMaze;
    private Timer timer;
    private JFileChooser mazeFile;
    private ArrayList<Point> spaces;
    private ArrayList<Color> colors;
    private String lastDirectory = null;
    private File chosenFile;
    private int count = 0, delay = 50;
    
    /** Default constructor - initializes all private values
     * 
     */
    public MazeLoader() {
        // Intialize other "stuff"
        start = new Point();
        allowMazeUpdate = true;
        timer = new Timer(delay, new TimerListener());
        
        // Create the maze window
        window = new JFrame("Maze Program");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Need to define the layout - as a grid depending on the number
        // of grid squares to use. Open the file and read in the size.
        
        System.out.println(chosenFile);
        try {
          if(chosenFile == null)
          fileToRead = new Scanner(new File("maze.txt"));
          else
              fileToRead = new Scanner(chosenFile);
          ROW = fileToRead.nextInt();
          COL = fileToRead.nextInt();
        }
        catch(FileNotFoundException e) {
            JOptionPane.showMessageDialog(window,"Default maze not found. " +
                    "\nSelect a maze to solve from the menu," +
                    "\nor rename maze to maze.txt", "Error", JOptionPane.ERROR_MESSAGE);
            allowMazeUpdate = false;
        }
        
        if(allowMazeUpdate) {
            // Now establish the Layout - appropriate to the grid size
            window.setLayout(new GridLayout(ROW, COL));
            grid= new JPanel[ROW][COL];
            data = fileToRead.nextLine();
            for(int i=0; i<ROW; i++) {
                data = fileToRead.nextLine();
                for(int j=0; j<COL; j++) {
                    grid[i][j] = new JPanel();
                    grid[i][j].setName("" + i + ":" + j);
                    if(data.charAt(j) == '*') 
                        grid[i][j].setBackground(WALL_COLOR);
                    else {
                        grid[i][j].setBackground(OPEN_COLOR);
                             grid[i][j].addMouseListener(new MazeListener());
                    }
                    window.add(grid[i][j]);
                }
            }
            fileToRead.close();
            window.pack();
        }

        // Add the menu to the window
        menuBar = new JMenuBar();
        menu = new JMenu("Load Maze...");
        loadMaze = new JMenuItem[2];
        loadMaze[0] = new JMenuItem("Load New Maze from another file...");
        loadMaze[0].addActionListener(new LoadMazeFromFile());
        loadMaze[1] = new JMenuItem("Load New Maze from current maze...");
        loadMaze[1].addActionListener(new ReloadCurrentMaze());
        colors = new ArrayList<>();
        spaces = new ArrayList<>();
        menu.add(loadMaze[0]);
        menu.add(loadMaze[1]);
        menuBar.add(menu);
        window.setJMenuBar(menuBar);
        
        if(!allowMazeUpdate)
            window.setSize(100,50);
        
        //goodSpaces = new ArrayList<>();
        //badSpaces = new ArrayList<>();

        // Finally, show the maze
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
    
    /** MazeListener class reacts to mouse presses - only when the current
     *  block that is clicked is a valid starting point within the maze.
     */
    private class MazeListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        /** mousePressed method defines the (x,y) coordinate of the starting
         *  square within the maze. Note: the start Point object does NOT
         *  reference the pixel location, rather the matrix location.
         * @param e - the MouseEvent created upon mouse click.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if(((JPanel)e.getSource()).getBackground().equals(OPEN_COLOR) &&
                    !timer.isRunning()) {
                data = ((JPanel)e.getSource()).getName();
                start.x = Integer.parseInt(data.substring(0,data.indexOf(":")));
                start.y = Integer.parseInt(data.substring(data.indexOf(":")+1));
                //grid[start.x][start.y].setBackground(PATH_COLOR);
                spaces.add(start);
                colors.add(Color.GREEN);
                //Find the maze solution
                if(!findPath(start))
                    JOptionPane.showMessageDialog(window,"Cannot exit maze.");
                else{
                    for(int i = 0; i < ROW; i ++)
                        for(int j = 0; j< COL; j ++)
                            if(grid[i][j].getBackground().equals(BAD_PATH_COLOR)||grid[i][j].getBackground().equals(PATH_COLOR))
                                grid[i][j].setBackground(OPEN_COLOR);
                    menuBar.setEnabled(false);
                    timer.start();
                    menuBar.setEnabled(true);
                    
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
        
    }
    
    /** findPath is the recursive routine to find the solution through the maze 
     * The recursive solution is found only whenever the edge of the maze has 
     * been reached, returning true to all previous methods and breaking the loop
     * Each time through the recursion, the path is updated to a green path color
     * and the point is saved
     * If it reaches the end of the loop without 
     * finding any other path it can take (or the exit), the color is updated to
     * red, and the spot is saved again, so we can see it change with the timer
     * After the loop is over,the timer starts for the displaying of the path colors.
     * 
     * @author AidenNelson
     * @param p - the current Point in the maze
     * @return whether or not a solution has been found.
     */
    public boolean findPath(Point p)  {
        boolean foundSolution = false;
        
        if(p.x ==ROW-1||p.x==0||p.y==0||p.y==COL-1){
            foundSolution = true;
            System.out.println("On the border!");
            spaces.add(p);
            colors.add(Color.GREEN);
            return true;
        }
        if(!foundSolution&& p.y-1>=0&&grid[p.x][p.y-1].getBackground().equals(OPEN_COLOR)){
            grid[p.x][p.y-1].setBackground(PATH_COLOR);
            spaces.add(p);
            colors.add(Color.GREEN);
            if(findPath(new Point(p.x,p.y-1))){
                foundSolution = true;
                
                return true;
            }
        }
       if(!foundSolution&& p.x+1<=ROW&&grid[p.x+1][p.y].getBackground().equals(OPEN_COLOR)){
            grid[p.x+1][p.y].setBackground(PATH_COLOR);
            spaces.add(p);
            colors.add(Color.GREEN);
            if(findPath(new Point(p.x+1,p.y))){
                foundSolution = true;
               
                return true;
            }
        }
        if(!foundSolution&& p.x-1>=0&&grid[p.x-1][p.y].getBackground().equals(OPEN_COLOR)){
            grid[p.x-1][p.y].setBackground(PATH_COLOR);
            spaces.add(p);
            colors.add(Color.GREEN);
            if(findPath(new Point(p.x-1,p.y))){
                foundSolution = true;
                return true;
            }
        }
        if(!foundSolution&& p.y+1<=COL&&grid[p.x][p.y+1].getBackground().equals(OPEN_COLOR)){
            grid[p.x][p.y+1].setBackground(PATH_COLOR);
            spaces.add(p);
            colors.add(Color.GREEN);
            if(findPath(new Point(p.x,p.y+1))){
                foundSolution = true;
                return true;
            }
        }
        if(!foundSolution){
            spaces.add(p);
            colors.add(Color.RED);
            grid[p.x][p.y].setBackground(BAD_PATH_COLOR);
            return false;
        }
        else
            return true;
    }
    
    
    /** ReloadCurrentMaze class listens to menu clicks - simply
     *  wipes the current state of the maze.
     */
    private class ReloadCurrentMaze implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
                //colors = new ArrayList<>();
                //spaces = new ArrayList<>();
                
                for(int i=0; i<ROW; i++)
                for(int j=0; j<COL; j++)
                    if(grid[i][j].getBackground().equals(PATH_COLOR) ||
                       grid[i][j].getBackground().equals(BAD_PATH_COLOR))
                         grid[i][j].setBackground(OPEN_COLOR);
                    
        }
    }
    
    /** LoadMazeFromFile parses through a file submitted from a JFileChooser 
     * (as soon as someone presses the menu item to submit their own maze),
     * and the row/column size is gathered from the first line
     * From there, the same process as with the initial construction of the maze 
     * takes place, in which all path colors are updated, and the maze is displayed.
     * 
     */
    private class LoadMazeFromFile implements ActionListener {

        /**Just checks for an ActionEvent in the menuBar item for the Load Maze
         * From File option, will get the selected file they select then.
         * 
         * @param e 
         * @author Aiden Nelson
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            mazeFile = new JFileChooser();
            mazeFile.showOpenDialog(null);
            chosenFile = mazeFile.getSelectedFile();
            
            
            System.out.println(chosenFile);
            window.setVisible(false);
            
            window = new JFrame("Maze Program");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Need to define the layout - as a grid depending on the number
        // of grid squares to use. Open the file and read in the size.
        
        System.out.println(chosenFile);
        try {
          if(chosenFile == null)
          fileToRead = new Scanner(new File("mazeA.txt"));
          else
              fileToRead = new Scanner(chosenFile);
          ROW = fileToRead.nextInt();
          COL = fileToRead.nextInt();
        }
        catch(FileNotFoundException a) {
            JOptionPane.showMessageDialog(window,"Maze cannot be loaded to.", "Error", JOptionPane.ERROR_MESSAGE);
            allowMazeUpdate = false;
        }
        
        if(allowMazeUpdate) {
            // Now establish the Layout - appropriate to the grid size
            window.setLayout(new GridLayout(ROW, COL));
            grid= new JPanel[ROW][COL];
            data = fileToRead.nextLine();
            for(int i=0; i<ROW; i++) {
                data = fileToRead.nextLine();
                for(int j=0; j<COL; j++) {
                    grid[i][j] = new JPanel();
                    grid[i][j].setName("" + i + ":" + j);
                    if(data.charAt(j) == '*') 
                        grid[i][j].setBackground(WALL_COLOR);
                    else {
                        grid[i][j].setBackground(OPEN_COLOR);
                             grid[i][j].addMouseListener(new MazeListener());
                    }
                    window.add(grid[i][j]);
                }
            }
            fileToRead.close();
            window.pack();
        }

        // Add the menu to the window
        menuBar = new JMenuBar();
        menu = new JMenu("Load Maze...");
        loadMaze = new JMenuItem[2];
        loadMaze[0] = new JMenuItem("Load New Maze from another file...");
        loadMaze[0].addActionListener(new LoadMazeFromFile());
        loadMaze[1] = new JMenuItem("Load New Maze from current maze...");
        loadMaze[1].addActionListener(new ReloadCurrentMaze());
        menu.add(loadMaze[0]);
        menu.add(loadMaze[1]);
        menuBar.add(menu);
        window.setJMenuBar(menuBar);
        
        if(!allowMazeUpdate)
            window.setSize(100,50);
        
        //goodSpaces = new ArrayList<>();
        //badSpaces = new ArrayList<>();
        spaces = new ArrayList<>();
        colors = new ArrayList<>();
        // Finally, show the maze
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
            
        }
    // end of LoadMazeFromFile class
    
    /**TimerListener class displays the results of the found path thing that we 
     * located above
     * The two ArrayLists (one of colors, the other of points) is then cycled 
     * through every time the timer goes off, showing the good path and bad paths
     * traveled
     * The timer delay goes down the more elements there are, to a minimum of 1ms.
     * 
     * @author Aiden Nelson
     */
    private class TimerListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if(count < spaces.size()){
                grid[spaces.get(count).x][spaces.get(count).y].setBackground(colors.get(count));
                count++;
                if(count%10 == 0){
                    if(timer.getDelay()>5)
                        timer.setDelay(delay-=5);
                    else if(timer.getDelay()>1)
                        timer.setDelay(delay--);
                }
                loadMaze[0].setEnabled(false);
                loadMaze[1].setEnabled(false);
            }
            else{
                spaces = new ArrayList<>();
                colors = new ArrayList<>();
                count = 0;
                JOptionPane.showMessageDialog(window, "Maze Exited!");
                loadMaze[0].setEnabled(true);
                loadMaze[1].setEnabled(true);
                timer.stop();
            }
        }
    }
}
