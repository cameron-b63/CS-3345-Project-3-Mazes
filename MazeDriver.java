public class MazeDriver {
    
    /*
     * Written by Cameron Bond (CNB220001) for CS 3345.002 Project 3
     * 
     * This program contains the driver code to generate, draw, and solve an m x n maze on command.
     */

    public static void main(String[] args){
        Maze maze = new Maze(5, 5);
        System.out.println("Maze is done generating!");
        maze.printMazeArray();
        System.out.println("Here's how you'd solve it.");
        System.out.println(maze.solve());
    }
}
