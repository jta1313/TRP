
public class CostInfo {
	public float cost, time; //costs(i, j).cost = meanTimes(0, j)
    public int[] S, successors,abandoned, visited;//ReDim costs(i, j).S(1) costs(i, j).S(1) = 0
    public int size, nextNode, index, previous, recentVisit;// costs(i, j).previous = 0
    public float[] succCosts;
    
    public CostInfo(){}
    
    public CostInfo(float cost,int S,int size, int nextNode, int index, int previous){
    	this.cost=cost;
    	this.S = new int[1];
    	this.S[0]=S;
    	this.size=size;
    	this.nextNode=nextNode;
    	this.index=index;
    	this.previous=previous;
    }
}
