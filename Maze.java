import java.util.Random;

public class Maze{
    /*
     * We're going to need to talk for a second about how I'm choosing to represent open paths.
     * There were two approaches in mind, but I went with the one that seemed the best to me to implement.
     * The directional paths open from each point in the matrix will be represented with the following bit string:
     * 
     * 0000 0000
     * ---- NESW
     * 
     * Where the first four bits are padding, and the last four represent the four cardinal directions.
     * A one represents an open path, while a 0 represents a wall.
     * 
     * For example: if there is a path open east, then (byte & 0x04) would evalute to true.
     * This is far more compact than creating an entirely new object with four variables - not even mentioning any access controls (public, private, etc).
     * 
     * Any algorithm concerning direction access would still be O(1) - just as fast as the object approach. Less overhead, but more programming - a tradeoff I'll take.
     * 
     * It's essentially a struct contained entirely within one byte.
     * 
     * To keep it a buck, it's the simplest way for me to think about storing this info. I'm a very low-level-oriented guy.
     */

    private byte[][] mazeArray;
    DisjointSet s;
    private int n;
    private int m;

    /*
     * All-around helper method. Returning a value of -1 symbolizes no neighbor in that direction. Else, returns cell number (i*m + j) of found neighbor.
     * Takes a cell number (i*m + j) as the argument.
     */
    private int getNeighborIndex(int original, int direction){
        int neighborIndex = -1;
        
        switch(direction){
            case 0:{
                // North: If original is not on first row
                if(original - m >= 0) { neighborIndex = original - m; }
                break;
            }
            case 1:{
                // East: If original is on same row as original + 1
                if((original + 1) / m == original / m) { neighborIndex = original + 1; }
                break;
            }
            case 2:{
                // South: If original is not on last row
                if(original + m < n*m) { neighborIndex = original + m; }
                break;
            }
            case 3:{
                // West: If original is on same row as original - 1
                if((original - 1) / m == original / m) { neighborIndex = original-1; }
                break;
            }
            default:{
                throw new IllegalArgumentException("You have got to be playing. Failure in getNeighborIndex on direction. Stop passing weird stuff.");
            }
        }
        return neighborIndex;
    }

    /*
     * Helper method for maze generation. Clear the wall pertaining to a specific cell and its neighbor. Only called when pre-checks have already been passed.
     * Given the two cells to check, it should be immediately clear whether it's a horizontal or vertical boundary. Then, just clear the appropriate bit. Straightforward in nature.
     */
    private void clearWall(int c1, int c2){
        int lower = (c1 < c2) ? c1 : c2;

        if(c1 / m == c2 / m){
            // Dealing with a horizontal boundary - lower cell needs its eastern wall cleared, and higher western
            mazeArray[lower / m][lower % m] &= 0b11111011;
            mazeArray[lower / m][(lower % m) + 1] &= 0b111111110;
            return;
        }

        // Dealing with a vertical boundary - lower-indexed cell needs its southern boundary removed, and higher northern
        mazeArray[lower / m][lower % m] &= 0b11111101;
        mazeArray[(lower / m) + 1][lower % m] &= 0b11110111;
        return;
    }

    /*
     * Solve/Draw helper method. Outputs a truth value of whether a step from the passed cell is possible in the passed direction.
     */
    private boolean canStep(int cell, int direction){
        int neighbor = getNeighborIndex(cell, direction);
        if(neighbor == -1) return false; // If it's a boundary cell trying to do a boundary step we already implemented that logic
        byte cellWalls = mazeArray[cell / m][cell % m];
        switch(direction){
            case 0:{
                // N - check for north closure on current cell
                if((cellWalls & 0b00001000) > 0) { return false; } break;
            }
            case 1:{
                // E - check for east closure on cell
                if((cellWalls & 0b00000100) > 0) { return false; } break;
            }
            case 2:{
                // S - check for south closure
                if((cellWalls & 0b00000010) > 0) { return false; } break;
            }
            case 3:{
                // W - check west
                if((cellWalls & 0b00000001) > 0) { return false; } break;
            }
        }

        // If we fell through to here we are allowed to step
        return true;
    }

    /*
     * Helper method for the populateMaze method. Returns a truth value of whether all the elements of the disjoint set are in the same set.
     */
    private boolean cellsNotConnected(){
        int terminate = n*m;
        int parentsEncountered = 0;
        for(int i = 0; i < terminate; i++){
            if(s.find(i) == i) parentsEncountered++;
        }
        return parentsEncountered > 1;
    }

    /*
     * Helper method for the maze printing. Returns an integer representing the direction denoted by a specific character. -1 represents unrecognized s but should never be needed.
     */
    private int toDirection(String s){
        if(s.equals("N")) return 0;
        if(s.equals("E")) return 1;
        if(s.equals("S")) return 2;
        if(s.equals("W")) return 3;
        return -1;
    }
    
    /*
     * The first part of some serious code for this project.
     * This method populates the mazeArray with the information pertaining to a randomly generated maze (using the disjoint set algorithm outlined below).
     * 
     * Begin with a disjoint set of size n*m.
     * 
     * Choose a random integer in [0, size = n*m). Then, run the following checks. If it fails any, try again.
     * - Is the generated integer already 0 in the mazeArray?
     * 
     * Then, choose another random number in [0, 3) to represent a wall to knock down for that cell. Run the following checks. If it fails any, try again.
     * - Does the generated number represent a boundary wall? That is, the north edge on mazeArray[0][*], an east edge on mazeArray[*][m-1], etc?
     * - Are the two cells on either side of the wall already in the same disjoint set (i.e. connnected)?
     * 
     * If both checks have been passed, clear the correct direction bit on both adjacent cells, and perform a union on the two cells in the disjoint set.
     * 
     * Continued selection by these criteria requires us to break down size-1 walls. Once size-1 walls have been cleared, the maze is officially populated.
     */
    private void populateMaze(){
        int size = n*m;
        s = new DisjointSet(size);
        Random rand = new Random();
        int tryCell = -1;
        int tryDirection = -1;
        int tryNeighbor = -1;

        while(cellsNotConnected()){
            while(true){
                tryCell = rand.nextInt(size);
                if(mazeArray[tryCell / m][tryCell % m] != 0) break; // Performing floor, modulus division to get correct chosen n and m back out.
            }
            byte triedDirections = 0;
            while(triedDirections != 0b00001111){
                tryDirection = rand.nextInt(4);
                // The following code prevents hangs. I was pretty sure I had already checked for this case but guess not... (Looking back I understand why boundaries cause this issue)
                triedDirections |= (1 << (3 - tryDirection));
                tryNeighbor = getNeighborIndex(tryCell, tryDirection);
                if(tryNeighbor != -1 && (s.find(tryCell) != s.find(tryNeighbor))){
                    clearWall(tryCell, tryNeighbor);
                    s.union(tryCell, tryNeighbor);
                    break;
                }

            }
            
        }
        return;
    }

    /*
     * This method solves the maze using Dijkstra's algorithm. It assumes each cell to be a distance 1 from its adjacent neighbors.
     * It performs a breadth-first search to fill in distances for each cell, and once the ending cell has been reached, steps backwards in distance to find the shortest path.
     * This shortest path is returned as a string of cardinal directions (i.e. NESW).
     * 
     * The breadth-first search is accomplished using a queue - if a node can be traversed to from the current cell, it is enqueued to have its unvisited neighbors traversed.
     * The first item in the queue is the starting node, which would enqueue either the south or east node, which would then enqueue all of its unvisited traversable neighbors.
     * 
     * The cost of each cell is recorded in a secondary array (default value -1 indicates not yet visited).
     * While this could have been done recursively, I chose not to define it that way.
     */
    public String solve(){
        Queue q;
        String path = "";
        int startingCell = 0;
        int targetCell = n*m - 1;
        int currentCell;
        int[] costs = new int[n*m];
        int neighborIndex;
        
        for(int i = 0; i < costs.length; i++){
            costs[i] = -1; // Indicates not visited
        }

        costs[0] = 0;
        q = new Queue();
        q.enqueue(startingCell);

        // Begin pathfinding: assign costs to non-visited neighbors until we assign a cost to the targetCell
        while(!q.isEmpty()){
            currentCell = q.dequeue();

            // Check all directions with the same logic in the order NESW
            for(int currentDirection = 0; currentDirection < 4; currentDirection++){
                if(canStep(currentCell, currentDirection)){
                    neighborIndex = getNeighborIndex(currentCell, currentDirection);
                    // If not already visited, assign it a value one step up from current cell's cost
                    if(costs[neighborIndex] == -1) {
                        costs[neighborIndex] = costs[currentCell] + 1;
                        q.enqueue(neighborIndex);
                    }
                }

                // If we just hit the ending cell, let's skidaddle by satisfying the while-loop's exit condition
                if(currentCell == targetCell) while(!q.isEmpty()) q.dequeue();
            }
        }

        // Constructing path consists of stepping backwards, as I already discussed. We know the maze is possible, so this approach is sound.
        int targetCost = costs[targetCell] - 1;
        currentCell = targetCell;
        // I made this array so I could write less code.
        String[] toPrepend = {"S", "W", "N", "E"};
        while(targetCost != -1){
            for(int i = 0; i < 4; i++){
                // If our found cell is north and we're moving backwards... we better prepend a move south to the path.
                if(canStep(currentCell, i) && costs[getNeighborIndex(currentCell, i)] == targetCost){
                    currentCell = getNeighborIndex(currentCell, i);
                    path = toPrepend[i] + path;
                    targetCost--;
                    break;
                }
            }
        }
        return path;
    }

    // Constructor
    public Maze(int n, int m){
        this.n = n;
        this.m = m;
        mazeArray = new byte[n][m];

        // Default population to initialize walls everywhere
        for(int i = 0; i < n; i++){
            for(int j = 0; j < m; j++){
                mazeArray[i][j] = 0b00001111; // 0x0F
            }
        }

        // Open the entrance and exits
        mazeArray[0][0] = 0b00000110;
        mazeArray[n-1][m-1] = 0b00001001;

        // Populate the maze
        populateMaze();
    }

    /*
     * Prints the maze. If solution mode is enabled, an '@' symbol is placed in each cell in the solution path.
     */
    public void printMaze(boolean solutionMode){
        boolean[] solutionSet = {};
        if(solutionMode){
            int currentCell = 0;
            String solution = this.solve();
            solutionSet = new boolean[n*m];
            for(int i = 0; i < solution.length(); i++){
                solutionSet[currentCell] = true;
                currentCell = getNeighborIndex(currentCell, toDirection(solution.substring(i, i+1)));
            }
            solutionSet[n*m - 1] = true;

        }
        for(int i = 0; i < n; i++){

            // Top row
            for(int j = 0; j < m; j++){
                if((i+j == 0)){
                    System.out.print("  [");
                } else if(canStep((i*m + j), 0)){
                    System.out.print("] [");
                } else {
                    System.out.print("]-[");
                }
            }
            System.out.print("\n");

            // Middle row
            for(int j = 0; j < m; j++){
                if(canStep(i*m + j, 3) || (i == 0 && j == 0)){
                    System.out.print(" ");
                } else {
                    System.out.print("]");
                }

                if(solutionMode){
                    if(solutionSet[i*m + j]){
                        System.out.print("@");
                    } else {
                        System.out.print(" ");
                    }
                } else {
                    System.out.print(" ");
                }

                if(canStep(i*m + j, 1) || (i == n-1 && j == m-1)){
                    System.out.print(" ");
                } else {
                    System.out.print("[");
                }
            }
            System.out.print("\n");
        }
        for(int i = 0; i < m-1; i++){
            System.out.print("]-[");
        }
        System.out.print("]  \n");
    }
}