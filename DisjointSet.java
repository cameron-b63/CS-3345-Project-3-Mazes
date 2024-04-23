public class DisjointSet{
    
    /*
     * This is a standard disjoint sets implementation (normalized to int since I know my use case and would rather not use generics).
     * It is array-based, since needed functionality uses a known, fixed size.
     * It's been included in a separate file since that's the way I like to code. :)
     */
    private int[] trees;

    public DisjointSet(int size){
        trees = new int[size];

        // Initialize all indices to be -1 (symbolizing root nodes of respective trees)
        for(int i = 0; i < size; i++){
            trees[i] = -1;
        }
    }

    // Might very well be the simplest method I've ever written haha
    public void union(int r1, int r2){
        if(r1 != r2 && r1 < trees.length && r2 < trees.length){
            trees[r2] = r1;
        } else throw new IllegalArgumentException("Bad indices given to disjoint sets find: {r1 : " + r1 + ", r2 : " + r2 + "}");
    }

    // Perform a find operation on given element. Return root of tree containing passed argument. Simple improvement made using path compression
    public int find(int element){
        if(element >= trees.length || element < 0) throw new IllegalArgumentException("Bad element index given to disjoint sets find: " + element);
        if(trees[element] < 0) return element;
        return trees[element] = find(trees[element]);
    }

}