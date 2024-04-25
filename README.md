# Mazes
This project was created for CS 3345 (Data Structures and Algorithms), and aims to be a demonstration of the disjoint sets data structure as well as Dijkstra's Algorithm.

Its core functionality is to generate a maze of a user-specified size, and then, when instructed, give a solution of that maze. The solution will be given in the form of a cardinal direction path (NESW) in terminal, along with being drawn.

# Maze Generation
Maze generation is accomplished using disjoint sets, and is performed during construction of the Maze object. When a Maze is constructed, it will be populated with a randomly generated maze configuration. This configuration allows for only one entrance and exit, but ensures each and every cell within the maze is reachable from every other cell (a good maze uses all the area available to it).

Associated public-facing methods:
 - Maze(int n, int m)
    - Input: Two ints representing the horizontal and vertical size of the maze, respectively.
    - Output: a reference to a new Maze object with a randomly generated maze.

# Maze Solving
Maze solving is also implemented within the maze class. It's performed using an implementation of Dijkstra's algorithm - each cell is considered a vertex, and has a cost associated with it (distance from start to that cell). The path is a sequence which doesn't skip any steps but also doesn't take any unnecessary ones.

Associated public-facing mehtods:
 - solve()
    - Input: Nothing
    - Output: a String with the path from start to finish written in cardinal direction instructions (i.e. "EESWSEE").

# Maze Printing
TODO

# Disjoint Set Implementation
This was just a super straightforward implmentation. It was done only with ints, no generics, and employs union by rank and path compression.