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
                if(original - m > 0) { neighborIndex = original - m; }
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
     * All-around helper method. Clear the wall pertaining to a specific cell and its neighbor. Only called when pre-checks have already been passed.
     * Given the two cells to check, it should be immediately clear whether it's a horizontal or vertical boundary. Then, just clear the appropriate bit. Straightforward in nature.
     */
    private void clearWall(int c1, int c2){
        int lower = (c1 < c2) ? c1 : c2;

        if(c1 / m == c2 / m){
            // Dealing with a horizontal boundary - lower cell needs its western wall cleared, and higher eastern
            mazeArray[lower / m][lower % m] &= 0b11111110;
            mazeArray[lower / m][(lower % m) + 1] &= 0b11111011;
            return;
        }

        // Dealing with a vertical boundary - lower cell needs its northern boundary removed, and higher southern
        mazeArray[lower / m][lower % m] &= 0b11110111;
        mazeArray[(lower / m) + 1][lower % m] &= 0b11111101;
        return;
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
                triedDirections |= (1 << tryDirection);
                tryNeighbor = getNeighborIndex(tryCell, tryDirection);
                if(tryNeighbor != -1 && s.find(tryCell) != s.find(tryNeighbor)){
                    clearWall(tryCell, tryNeighbor);
                    s.union(tryCell, tryNeighbor);
                    break;
                }

            }
            
        }
        return;
    }

    // Accessors for other classes
    public byte mazeStatus(int i, int j) { return mazeArray[i][j]; }

    // Solver's gonna want this one
    public int dsFindWrapper(int toFind) { return s.find(toFind); }


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
        mazeArray[0][0] = 0b00000110; // North and west are open
        mazeArray[n-1][m-1] = 0b00001001; // South and east are open

        // Populate the maze
        populateMaze();
    }
}