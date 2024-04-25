import java.util.Scanner;

public class MazeDriver {
    
    /*
     * Written by Cameron Bond (CNB220001) for CS 3345.002 Project 3
     * 
     * This program contains the driver code to generate, draw, and solve an m x n maze on command.
     */

     /*
      * This method just flushes I/O for some prettier printing.
      */
    public static void flush(){
        for(int i = 0; i < 30; i++){
            System.out.println();
        }
        return;
    }

    public static void main(String[] args){
        Scanner s = new Scanner(System.in);
        int n;
        int m;
        while(true){
            System.out.print("Enter the number of rows, n (0 to exit):\n> ");
            n = s.nextInt();
            s.nextLine();
            System.out.print("Enter the number of columns, m (0 to exit):\n> ");
            m = s.nextInt();
            s.nextLine();

            if(n < 1 || m < 1){ s.close(); System.exit(0); }

            Maze maze = new Maze(n, m);
            System.out.println("Maze is done generating!");
            maze.printMaze(false);
            
            
            System.out.println("\nPress Enter to see the solution.");
            s.nextLine();
            flush();
            System.out.println("Here's how you'd solve it (Follow the '@'s).");
            maze.printMaze(true);
            System.out.println("\nYour solution string was :\n" + maze.solve());
        }
    }
}
