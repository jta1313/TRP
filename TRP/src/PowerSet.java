import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class PowerSet {
	public HashMap<Integer,Set<Set<Integer>>> PS;
	//Set<int>[] PS;
	
	public HashMap<Integer,Set<Set<Integer>>> getPS(int[]arr){
	    int i, index, size, n, numElements=0, j,k;
	    Set<Integer> tempSet;
	    //ArrayList<Set<Set<Integer>>>
	    //tempIndices As abandonedIndices
	
	    n = arr.length;
	    for(i = 1; i<=n; i++){
	    	numElements = (int) (numElements + Math.pow(2, i - 1));
	    }
	    
	    // Look at all subsets
	    this.PS= new HashMap<Integer,Set<Set<Integer>>>();
	    //ReDim tempIndices.indices(numElements)
	    for(k=1; k<=n; k++){
	    	Set<Set<Integer>> sizeKCombinations = new HashSet<Set<Integer>>();
		    for(i=1; i<=numElements;i++){
		    	tempSet = new HashSet<Integer>();
		        index = i;
		        size=0;
		        for(j=n; j >= 1; j--){
		                if(index >= Math.pow(2, j-1)){
		                	size++;
	                        tempSet.add(arr[j-1]);
	                        index = (int) (index - Math.pow(2, j-1));
		                }
		        }
		        if(size==k){
		        	sizeKCombinations.add(tempSet);
		        } 
		    }
		    this.PS.put(k, sizeKCombinations);
	    }
	    
	    return this.PS;
	}
	public HashMap<Integer,Set<Set<Integer>>> translate(int[]arr, PowerSet indices){
		HashMap<Integer,Set<Set<Integer>>> reducedPS=new HashMap<Integer,Set<Set<Integer>>>();;
		Set<Integer> tempSet;
	    //tempIndices As abandonedIndices
	
	    int i, numIndices;
	    // find total number of elements in customer set arr (0 if empty)
	    try{
	    	numIndices=arr.length;
	    }
	    finally{
	    	numIndices=0;
	    }

        for(i=1; i<=numIndices; i++){
        	Set<Set<Integer>> sizeICombinations = new HashSet<Set<Integer>>();
        	
        	for (Set<Integer> element : indices.PS.get(i)) {
        		tempSet = new HashSet<Integer>();
        		for (Integer element1 : element) {
            		tempSet.add(arr[element1]);	
                }
        		sizeICombinations.add(tempSet);
            }
        	reducedPS.put(i, sizeICombinations);
        }
        
	    return reducedPS;
	}
}
