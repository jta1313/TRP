import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


//here is the iterative Bellman-Held-Karp algorithm for minimizing distance (general TSP)
public class TRP_DP{
	
	public ArrayList<bestRoute> solutionSet = new ArrayList<bestRoute>();
	private CostInfo[][] costMatrix;
	private int numStops;
	public float benefit;
	public int[] routing;
	public int[] abandoned;
	public float[] benefits, times;
	public PowerSet custIndex = new PowerSet();
	
	public TRP_DP(){};
	
	public void MainTRP(CustomerList customers){
		// required variables in body
        int i, j, k, index;
        float tempTime;
        
        numStops = customers.numCusts;
        
        //ArrayList<PowerSet> indices = new ArrayList<PowerSet>();
        
      //This code all works if number of Customers is greater than 2
        // Set up all power sets used later
        //ReDim allPS.indexSet(0 To numStops)
        /*int[] indexArray;
        for(i=1; i<customers.numCusts; i++){
        	indexArray = new int[numStops];
        	//PowerSet custIndex = new PowerSet();
        	for(j=0; j<i; j++){
        		indexArray[j]=j+1;
        	}
        	indices.add(new PowerSet ());
        	indices.get(i-1).PS=indices.get(i-1).getPS(indexArray);
        }*/
        
        // set up FULL customer power set (ie all customers included) for use throughout the algorithm
        int[] indexArray = new int[numStops];
        for(i=0; i<customers.numCusts; i++){
        	indexArray[i]=i+1;
        }
        custIndex.PS = custIndex.getPS(indexArray);
        
        // find upper bound on indexing given sum(2^(index-1))
        index = 0;
        for(i=1; i<= customers.numCusts;i++){
        	index = index + (int)Math.pow(2, i-1);
        }
        
        // initialize 2D cost matrix to hold info using the index hash above for i elements, ending element for j
        costMatrix = new CostInfo[index+1][numStops+1];

        //build memoized costs     
        maxReturn temp;

        for(i=0;i<numStops;i++){

        	// from depot to any one customer
            if(i==0){
                for(j=1; j<=numStops;j++){
                	tempTime = this.delaying(customers, j, customers.startTime, 0);
                	costMatrix[i][j]=new CostInfo();
                	costMatrix[i][j].time=tempTime;
                	
                	// depends whether or not visited
					if( tempTime != customers.startTime){
						costMatrix[i][j].visited= new int[1];
						//costMatrix[i][j].cost = this.serviceLevel(customers,j, tempTime);
						costMatrix[i][j].recentVisit = j;
						costMatrix[i][j].visited[0] = j;
						costMatrix[i][j].cost = this.serviceLevel(customers, j, costMatrix[i][j].time);
					}
					// cannot possibly visit customer so infeasible total solution
					//*****this else block handled earlier now*****
					else{
						costMatrix[i][j].cost = 0;
						costMatrix[i][j].recentVisit = 0;
						costMatrix[i][j].abandoned = new int[1];
						costMatrix[i][j].abandoned[0] = j;
					}
                    costMatrix[i][j].S=new int[1];
                    costMatrix[i][j].S[0] = 0;
                    costMatrix[i][j].size = 0;
                    costMatrix[i][j].nextNode = j;
                    costMatrix[i][j].index = 0;
                    costMatrix[i][j].previous = 0;           
                }
            }
            /*if have previous costs then need to shift next node to S set and add unused
            this effectively gets all costs associated with sets |S|= i*/
            else{
                // iterate over all power sets of size j
                for (Set<Integer> element : custIndex.PS.get(i)) {
            		index=0;

            		int count= 0;
            		//Set<Integer> tempSet = new HashSet<Integer>();
            		// get index of the previous set S
            		int[] tempS = new int[i];
            		for (Integer element1 : element) {
            			index = index + (int) (Math.pow(2, element1-1));
            			tempS[count]= (int) element1;
            			count++;
                    }
            		// if k not already included in the set, then treat it as next node
            		for(k=1; k <= numStops; k++){
	            		if(!(element.contains(k))){
	            			// next node is always k anyways, but this is a little clearer
	            			costMatrix[index][k]=new CostInfo();
	            			costMatrix[index][k].nextNode = k;
	            			
	            			// deep copy of tempS
	                        //costMatrix[index][k].S = Arrays.copyOf(tempS, element.size());
	                        costMatrix[index][k].S=tempS;
	                        
	                        // size is i
	                        costMatrix[index][k].size = i;
	                        costMatrix[index][k].index = index;
	                        // cost of this stage is the min of last leg + rest of node 
	                        //maxReturn temp = new maxReturn();

	                        temp = bestReturn(costMatrix[index][k],customers);
	                        costMatrix[index][k].cost = temp.returns[0];
	                        costMatrix[index][k].previous = (int) temp.returns[1];
	                        costMatrix[index][k].time = temp.returns[2];
	                        costMatrix[index][k].visited = temp.visited;
	                        //costMatrix[index][k].abandoned= temp.abandoned;
	                        
	                        // the abandoned set S U {k}\{visited}
	                        if(index == 95 && k ==7){
	                        	k=7;
	                        }
	                        costMatrix[index][k].abandoned = this.visitToAbandon(temp.visited, tempS);
	                        
	                        //if it raises the benefit to include the tested stage
	                        //add it to the visited set (ie if not added, abandoned)
	                        if(temp.returns[3] > 0){
	                            costMatrix[index][k].recentVisit = k;
	                        }
	                        else{
	                            costMatrix[index][k].recentVisit = (int) temp.returns[1];
	                        }
	            		}
            		}
                }
            }
        }
        
        bestRoute routingInfo = new bestRoute();
        routingInfo = this.findBestRoute(customers, customers.indices);
        solutionSet.add(routingInfo);
        
        while(routingInfo.abandoned != null){// || routingInfo){
        	routingInfo = this.findBestRoute(customers,routingInfo.abandoned);
        	solutionSet.add(routingInfo);
        }


        /*int tempIndex;
        
        index = 0;
        for(i=1; i<= customers.numCusts;i++){
        	index = index + (int)Math.pow(2, i-1);
        }
        costMatrix[index][0]=new CostInfo();
        
        costMatrix[index][0].size = i-1;
        costMatrix[index][0].index = index;
        costMatrix[index][0].S= new int[numStops];
        costMatrix[index][0].nextNode = 0;
        for(i=1; i<=numStops; i++){
        	costMatrix[index][0].S[i-1] = i;
        }
        
        
        // get time of going from most recent node in previous subchain (might not be next node)
        // temp = bestCostAndTime(costs(newIndex, m))
        float maxCost,  minTime, time; 
        int last;
        int[] abandoned;
        maxCost = (float) -1.0;
        minTime = (float) 1E+32;
        for(i=1; i<=numStops; i++){
	        //abandoned = costMatrix[(int) (index - Math.pow(2, i-1))][i].abandoned
	        if(costMatrix[(int) (index - Math.pow(2, i-1))][i].recentVisit == i){
	                time = costMatrix[(int) (index - Math.pow(2, i-1))][i].time + customers.distances[i][0]+ customers.customers[i].serviceTime;
	        }        		
	        else{
	                time = costMatrix[(int) (index - Math.pow(2, i-1))][i].time + customers.distances[costMatrix[(int) (index - Math.pow(2, i-1))][i].recentVisit][0];
	        }
	        if((costMatrix[(int) (index - Math.pow(2, i-1))][i].cost > maxCost) ||
	                (costMatrix[(int) (index - Math.pow(2, i-1))][i].cost == maxCost && (time < minTime))){
	                maxCost = costMatrix[(int) (index - Math.pow(2, i-1))][i].cost;
	                minTime = time;
	                costMatrix[index][0].cost = maxCost;
	                costMatrix[index][0].previous = costMatrix[(int) (index - Math.pow(2, i-1))][i].recentVisit;
	                costMatrix[index][0].time = minTime;
	                //costMatrix[index][0].abandoned = abandoned
	                costMatrix[index][0].visited = costMatrix[(int) (index - Math.pow(2, i-1))][i].visited;
	                costMatrix[index][0].abandoned = costMatrix[(int) (index - Math.pow(2, i-1))][i].abandoned;
	        }
        }
        
        this.benefit=maxCost;
        // find this.routing and previous costs and times by backtracking through memoization
        // given that the best solution has lost customers to abandonment
        //numStops = numStops // - UBound(costMatrix[index][0].abandoned)
        int reduceIndex=0;
        
        
        int numStopsWithAban;
        // infeasible so abandon all customers
        if(costMatrix[index][0].visited == null){
        	reduceIndex = 0;
        	abandoned = new int[numStops - reduceIndex];
        	for(i=1; i<=numStops; i++){
        		abandoned[i-1]=i;
        	}
        	this.routing = new int[1];
        	this.routing[0]=0;
        	this.benefits = new float[1];
        	this.benefits[0]=0;
        	this.times = new float[1];
        	this.times[0]=customers.startTime;
        	
        }
        // at least 1 feasible customer
        else{
        	if(costMatrix[index][0].abandoned!=null){
	            abandoned = new int[costMatrix[index][0].abandoned.length];
	            abandoned = costMatrix[index][0].abandoned;
	            reduceIndex = 0;
	            for(i=0; i<costMatrix[index][0].abandoned.length; i++){
	            	reduceIndex = (int) (reduceIndex + Math.pow(2, costMatrix[index][0].abandoned[i] - 1));
	            }
	            numStopsWithAban = numStops - costMatrix[index][0].abandoned.length;
        	}
        	else{
        		numStopsWithAban = numStops;
        	}
        	// add two for beginning and end at depot 0
            this.routing = new int[numStopsWithAban+2];
            float[] prevCosts = new float[numStopsWithAban+2];
            float[] times = new float[numStopsWithAban+2];
            
            this.routing[numStopsWithAban+1] = 0;
            prevCosts[numStopsWithAban+1]= maxCost;
            times[numStopsWithAban+1] = minTime;
            
            last = costMatrix[index][0].previous;
            
            
            // go to subproblem ending in "last", without all the abandoned customers
            tempIndex = (int) (index - Math.pow(2, last-1));
            tempIndex = tempIndex - reduceIndex;
            
            i = numStopsWithAban;
            do {
            	this.routing[i] = last;
                prevCosts[i] = costMatrix[tempIndex][last].cost;
                times[i] = costMatrix[tempIndex][last].time;
                
                last = costMatrix[tempIndex][last].previous;
                tempIndex = (int) (tempIndex - Math.pow(2, last - 1));
                i--;
            } while (last!=0);
            
            times[i] = customers.startTime;
            this.routing[i] = last;
            this.abandoned = costMatrix[index][0].abandoned;
            // translate from indices to actual customer numbers before returning
        	for(i=1;i<this.routing.length-1;i++){
            	this.routing[i]= customers.indices[this.routing[i]-1];
            }

            if(this.abandoned != null){
		        for(i=0;i<this.abandoned.length;i++){
		        	this.abandoned[i] = customers.indices[this.abandoned[i]-1];
		        }
            }
            this.benefits=prevCosts;
            this.times=times;
        }*/

        /*//Set costs() = Nothing
        MainTRP_DP.benefit = costMatrix[index][0].cost
        MainTRP_DP.route = this.routing
        MainTRP_DP.time = costMatrix[index][0].time
        MainTRP_DP.benefits = prevCosts
        MainTRP_DP.times = times*/
	}
	
	/** return the time of delaying (if any) for customer given previous time and location NOTE: need to arrive and finish service before window over in order to visit
	 * @param customers
	 * @param customer
	 * @param time
	 * @param previous
	 * @return
	 */
	private float delaying(CustomerList customers, int customer, float time, int previous){
		float delaying = time;
		// if arriving early, wait until window opens
	    if(time + customers.distances[previous][customer] < customers.customers[customer].early){
	            delaying = customers.customers[customer].early;
	    }
	    //shrink late window by forcing service to be done within window
	    else if(time + customers.distances[previous][customer] + customers.customers[customer].serviceTime > customers.customers[customer].late){
	    	delaying = time;
	    }
	    // fall within the time window
	    else{
	    	delaying = time + customers.distances[previous][customer];
	    }
	    return delaying;
	}
	/** returns total service level given trying to visiting given customer starting at given time
	 * @param customers
	 * @param customer
	 * @param time
	 * @return
	 */
	private float serviceLevel(CustomerList customers,int customer, float time){
		float serviceLevel=0f;
	    //arrives before time window
	    if(time <= customers.customers[customer].early){ 
	//                If custTimeWindows(customer).early - time >= 1 Then
	//                        serviceLevel = 0
	//                Else
	//                        serviceLevel = 1 - (custTimeWindows(customer).early - time)
	//                End If
	    	serviceLevel = 1f;
	    }
	    // arrives after time window
	    else if(time > customers.customers[customer].late){
	//                If time - custTimeWindows(customer).late >= 1 Then
	//                        serviceLevel = 0
	//                Else
	//                        serviceLevel = 1 - (time - custTimeWindows(customer).late)
	//                End If
	    	serviceLevel = 0f;
	    }
	    // arrives inside time window
	    else{
	        serviceLevel = 1f;
	    }
	    // scale each customer for whole route (ie 1 out of 10 customers only counts for 10%)
	    serviceLevel = (float)(1.0 / ((float)(customers.customers.length-1) * serviceLevel));
	    return serviceLevel;
	}
	
	private maxReturn bestReturn(CostInfo x,CustomerList cust){// As maxReturn
	    int i, index, tempIndex, j, size, reduceIndex, k, newIndex;
	    float tempCost, newTime, tempTime, tempService;
	    maxReturn bestReturn = new maxReturn();
	    PowerSet abandoned = new PowerSet();
	    // uses visited nodes from S to get "total" index (even abandoned)
	    index = x.index;
	    
	    if(index == 3 && x.nextNode == 7){
	    	index = 3;
	    }
	    // first find the best solution given NEED to visit next city
	    bestReturn.returns= new float[4];
	    // -1 for service level (can only be positive so will always be at least 1 solution)
	    bestReturn.returns[0]=-1;
	    // assume very high time
	    bestReturn.returns[2] = (float) 1E+32;
	    bestReturn.abandoned = null;
	    // start at second element to avoid depot 0
	    for(i=0; i < x.S.length; i++){
	    	
            // back track in memoization to find S\{i} ending at i
            tempIndex = (int) (index - Math.pow(2, x.S[i] - 1));
//                If x.visited(i) = 4 And x.nextNode = 5 Then
//                        MsgBox "Stop"
//                End If
            
            boolean skip = false;
            //if(tempIndex==0){
            
            	// no abandonments
                newIndex = tempIndex;
                size=0;
             // get time that started visit at last non-abandoned customer
				if(x.S[i] == costMatrix[newIndex][x.S[i]].recentVisit){
					tempTime = costMatrix[newIndex][x.S[i]].time + cust.customers[x.S[i]].serviceTime;
				}
				else{
					tempTime = costMatrix[newIndex][x.S[i]].time;
				}
				
				if(x.nextNode != 0){
			        newTime = delaying(cust,x.nextNode, tempTime, costMatrix[newIndex][x.S[i]].recentVisit);
			        // if time to new node is the same, then this is abandoned
			        if(tempTime != newTime){
			        	tempService = serviceLevel(cust,x.nextNode, newTime);
			        }
			        else{
			        	tempService = 0;
			        }
			        tempCost = costMatrix[newIndex][x.S[i]].cost + tempService;
				}
				else{
			        newTime = tempTime;
			        tempService = 0;
			        tempCost = costMatrix[newIndex][x.S[i]].cost;
				}
                 
				// if this try has the same cost but lower time or better cost, replace current best
                if((tempCost == bestReturn.returns[0] && newTime < bestReturn.returns[2]) || tempCost > bestReturn.returns[0]){
                	// replace all information 
                    bestReturn.returns[0] = tempCost;
                    // previous node (could also be most recent later)
                    bestReturn.returns[1] = costMatrix[newIndex][x.S[i]].recentVisit;
                    //bestReturn.returns(2) = costs(tempIndex, x.S(i)).visited(j)
                    bestReturn.returns[2] = newTime;
                    //bestR
                    
                    if(tempService > 0){
                        bestReturn.returns[3] = x.nextNode;
                        // in case when visited still empty, need to error handle appropriately
                        try{
                        	//k = costMatrix[newIndex][x.S[i]].visited.length;
                        	bestReturn.visited=new int[costMatrix[newIndex][x.S[i]].visited.length + 1];
                        	// deep copy of previous subproblem's visited array
                        	for(k=0; k <costMatrix[newIndex][x.S[i]].visited.length; k++){
                                bestReturn.visited[k] = costMatrix[newIndex][x.S[i]].visited[k];
	                        }
                        	// add current element to visited (recent visit) since
	                        bestReturn.visited[k] = x.nextNode;
                        }
                        catch (Exception e){
                        	//k=0;
                        	bestReturn.visited=new int[1];
                        	bestReturn.visited[0] = x.nextNode;
                        }
                        //bestReturn.visited=new int[k + 1];
                        /*if(k==0){
                        	bestReturn.visited[0] = x.nextNode;
                        }
                        else{
                            for(k=0; k <costMatrix[newIndex][x.S[i]].visited.length; k++){
                                    bestReturn.visited[k] = costMatrix[newIndex][x.S[i]].visited[k];
                            }
                            bestReturn.visited[k] = x.nextNode;
                        }*/
                    }
                    // abandoning x.nextNode
                    else{
                        bestReturn.returns[3] = 0;
                        bestReturn.visited = costMatrix[newIndex][x.S[i]].visited;
                        bestReturn.abandoned = new int[1];
                        bestReturn.abandoned[0]= x.nextNode;
                    }
                }
                    
            //}
            if(tempIndex != 0){
	            //abandoned.PS = abandoned.translate(costMatrix[tempIndex][x.S[i]].S, custIndex);
            	//abandoned.PS=abandoned.getPS(costMatrix[tempIndex][x.S[i]].S);
            	abandoned.PS=abandoned.translate(costMatrix[tempIndex][x.S[i]].S,custIndex);
	            //allPS.indexSet (i)
	            size = abandoned.PS.size();

	            /*if(size == 5){
	            	size =5;
	            }*/
	            
	            for(j=1; j <= size; j++){
	            	newIndex=0;
	            		for(Set<Integer> elements: abandoned.PS.get(j)){
				            if(j != 0){
				                //size2 = abandoned.PS.get(j).size();
				                reduceIndex = 0;
				                for (Integer element : elements) {
				                	reduceIndex = (int) (reduceIndex + Math.pow(2, element - 1));
				                }
				                newIndex = tempIndex - reduceIndex;
				            }
				            else{
				                newIndex = tempIndex;
				            }
				         // get time that started visit at last non-abandoned customer
							if(x.S[i] == costMatrix[newIndex][x.S[i]].recentVisit){
							        tempTime = costMatrix[newIndex][x.S[i]].time + cust.customers[x.S[i]].serviceTime;
							}
							else{
							        tempTime = costMatrix[newIndex][x.S[i]].time;
							}
							
							if(x.nextNode != 0){
						        newTime = delaying(cust,x.nextNode, tempTime, costMatrix[newIndex][x.S[i]].recentVisit);
						        // if time to new node is the same, then this is abandoned
						        if(tempTime != newTime){
						                tempService = serviceLevel(cust,x.nextNode, newTime);
						        }
						        else{
						                tempService = 0;
						        }
						        tempCost = costMatrix[newIndex][x.S[i]].cost + tempService;
							}
							else{
						        newTime = tempTime;
						        tempService = 0;
						        tempCost = costMatrix[newIndex][x.S[i]].cost;
							}
			                 
							// if this try has the same cost but lower time or better cost, replace current best
			                if((tempCost == bestReturn.returns[0] && newTime < bestReturn.returns[2]) || tempCost > bestReturn.returns[0]){
			                	// replace all information 
			                    bestReturn.returns[0] = tempCost;
			                    bestReturn.returns[1] = costMatrix[newIndex][x.S[i]].recentVisit;
			                    //bestReturn.returns(2) = costs(tempIndex, x.S(i)).visited(j)
			                    bestReturn.returns[2] = newTime;
			                    //bestReturn.abandoned = this.SetToArray(elements);
			                    
			                    if(tempService > 0){
			                        bestReturn.returns[3] = x.nextNode;
			                        // in case when visited still empty, need to error handle appropriately
			                        try{
			                        	//k = costMatrix[newIndex][x.S[i]].visited.length;
			                        	bestReturn.visited=new int[costMatrix[newIndex][x.S[i]].visited.length + 1];
			                        	// deep copy of previous subproblem's visited array
			                        	for(k=0; k <costMatrix[newIndex][x.S[i]].visited.length; k++){
			                                bestReturn.visited[k] = costMatrix[newIndex][x.S[i]].visited[k];
				                        }
			                        	// add current element to visited (recent visit) since
				                        bestReturn.visited[k] = x.nextNode;
				                        
				                        bestReturn.abandoned=this.visitToAbandon(bestReturn.visited,x.S);
			                        }
			                        catch (Exception e){
			                        	//k=0;
			                        	bestReturn.visited=new int[1];
			                        	bestReturn.visited[0] = x.nextNode;
			                        }
			                    }                      
			                    else{
			                        bestReturn.returns[3] = 0;
			                        bestReturn.visited = costMatrix[newIndex][x.S[i]].visited;
			                        bestReturn.abandoned = this.SetToArrayAbandon(elements, x.nextNode);
			                    }  
			                }
			                    
			            if(tempIndex == 0){break;}
				            
		            	}
	            	}
            	}
	    	}
	    return bestReturn;
	}
	
	
	/**print(writer) prints out the edges in an EdgeList in order
	 * @param writer BufferedWriter where output is going
	 * @throws IOException
	 */
	public void print(String outfileName) throws IOException{
		BufferedWriter writer = Files.newBufferedWriter(FileSystems.getDefault().
	    		getPath(System.getProperty("user.dir")+ "/src/output", outfileName));
		String temp = "";
		int i;
		for(i=0; i<this.routing.length; i++){
		temp = temp.concat(String.format("%d %f %f%n",this.routing[i],this.benefits[i],this.times[i]));
		}
		writer.write(temp);
		writer.close();
	}
	
	private int[] SetToArray(Set<Integer> set){
		int[] temp = new int[set.size()];
		int i=0;
		for(Integer ints:set){
			temp[i]=ints;
			i++;
		}
		return temp;
	}
	private int[] SetToArrayAbandon(Set<Integer> set, int abandoned){
		int[] temp = new int[set.size()+1];
		int i=0;
		for(Integer ints:set){
			temp[i]=ints;
			i++;
		}
		temp[i]=abandoned;
		return temp;
	}
	private int[] visitToAbandon(int[] visit, int[] S){
		int[] abandoned = new int[S.length];
		int i,j, count=0;
		boolean found;
		for(i=0; i < S.length;i++){
			found = false;
			for(j=0; j < visit.length; j++){
				if(S[i]==visit[j]){
					found = true;
				}
			}
			if(found!=true){
				abandoned[count]= S[i];
				count++;
			}
		}
		if(count == 0){return null;}
		return Arrays.copyOf(abandoned, count);
	}
	
	private bestRoute findBestRoute(CustomerList customers, int[] included){
		int tempIndex, index, i, numStops;
		
		numStops = included.length;
		bestRoute returnInfo = new bestRoute();
	        
	    index = 0;
	    for(i=0; i< numStops; i++){
	    	index = index + (int)Math.pow(2, included[i]-1);
	    }
	    costMatrix[index][0]=new CostInfo();

	    costMatrix[index][0].size = i-1;
	    costMatrix[index][0].index = index;
	    costMatrix[index][0].S= new int[numStops];
	    costMatrix[index][0].nextNode = 0;
	    for(i=0; i<numStops; i++){
	    	costMatrix[index][0].S[i] = included[i];
	    }

	    // get time of going from most recent node in previous subchain (might not be next node)
	    // temp = bestCostAndTime(costs(newIndex, m))
	    float maxCost,  minTime, time; 
	    int last, currentLast, newIndex;
	    int[] abandoned;
	    maxCost = (float) -1.0;
	    minTime = (float) 1E+32;
	    for(i=0; i<numStops; i++){
	    	currentLast = included[i];
	    	newIndex=(int)(index - Math.pow(2, currentLast-1));
	        //abandoned = costMatrix[(int) (index - Math.pow(2, i-1))][i].abandoned
	        if(costMatrix[newIndex][currentLast].recentVisit == currentLast){
	                time = costMatrix[newIndex][currentLast].time + customers.distances[currentLast][0]+ customers.customers[currentLast].serviceTime;
	        }        		
	        else{
	                time = costMatrix[newIndex][currentLast].time + customers.distances[costMatrix[newIndex][currentLast].recentVisit][0];
	        }
	        if((costMatrix[newIndex][currentLast].cost > maxCost) ||
	                (costMatrix[newIndex][currentLast].cost == maxCost && time < minTime)){
                maxCost = costMatrix[newIndex][currentLast].cost;
                minTime = time;
                costMatrix[index][0].cost = maxCost;
                costMatrix[index][0].previous = costMatrix[newIndex][currentLast].recentVisit;
                costMatrix[index][0].time = minTime;
                //costMatrix[index][0].abandoned = abandoned
                costMatrix[index][0].visited = costMatrix[newIndex][currentLast].visited;
                //costMatrix[index][0].abandoned = costMatrix[newIndex][currentLast].abandoned;
                costMatrix[index][0].abandoned=this.visitToAbandon(costMatrix[newIndex][currentLast].visited,included);
	        }
	    }
	    
	    returnInfo.benefit=maxCost;
	    // find this.routing and previous costs and times by backtracking through memoization
	    // given that the best solution has lost customers to abandonment
	    //numStops = numStops // - UBound(costMatrix[index][0].abandoned)
	    int reduceIndex=0;
	    
	    int numStopsWithAban;
	    // infeasible so abandon all customers
	    if(costMatrix[index][0].visited == null){
	    	reduceIndex = 0;
	    	abandoned = new int[numStops - reduceIndex];
	    	for(i=0; i<numStops; i++){
	    		abandoned[i]=included[i];
	    	}
	    	returnInfo.routing = new int[1];
	    	returnInfo.routing[0]=0;
	    	returnInfo.benefits = new float[1];
	    	returnInfo.benefits[0]=0;
	    	returnInfo.times = new float[1];
	    	returnInfo.times[0]=customers.startTime;
	    	
	    }
	    // at least 1 feasible customer
	    else{
	    	if(costMatrix[index][0].abandoned!=null){
	            abandoned = new int[costMatrix[index][0].abandoned.length];
	            abandoned = costMatrix[index][0].abandoned;
	            reduceIndex = 0;
	            for(i=0; i<costMatrix[index][0].abandoned.length; i++){
	            	reduceIndex = (int) (reduceIndex + Math.pow(2, costMatrix[index][0].abandoned[i] - 1));
	            }
	            numStopsWithAban = numStops - costMatrix[index][0].abandoned.length;
	    	}
	    	else{
	    		numStopsWithAban = numStops;
	    	}
	    	// add two for beginning and end at depot 0
	        returnInfo.routing = new int[numStopsWithAban+2];
	        float[] prevCosts = new float[numStopsWithAban+2];
	        float[] times = new float[numStopsWithAban+2];
	        
	        returnInfo.routing[numStopsWithAban+1] = 0;
	        prevCosts[numStopsWithAban+1]= maxCost;
	        times[numStopsWithAban+1] = minTime;
	        
	        last = costMatrix[index][0].previous;
	        
	        
	        // go to subproblem ending in "last", without all the abandoned customers
	        tempIndex = (int) (index - Math.pow(2, last-1));
	        tempIndex = tempIndex - reduceIndex;
	        
	        i = numStopsWithAban;
	        do {
	        	returnInfo.routing[i] = last;
	            prevCosts[i] = costMatrix[tempIndex][last].cost;
	            times[i] = costMatrix[tempIndex][last].time;
	            
	            last = costMatrix[tempIndex][last].previous;
	            tempIndex = (int) (tempIndex - Math.pow(2, last - 1));
	            i--;
	        } while (last!=0);
	        
	        times[i] = customers.startTime;
	        returnInfo.routing[i] = last;
	        returnInfo.abandoned = costMatrix[index][0].abandoned;
	        // translate from indices to actual customer numbers before returning
	    	/*for(i=1;i<returnInfo.routing.length-1;i++){
	        	returnInfo.routing[i]= customers.indices[returnInfo.routing[i]-1];
	        }
	
	        if(returnInfo.abandoned != null){
		        for(i=0;i<returnInfo.abandoned.length;i++){
		        	returnInfo.abandoned[i] = customers.indices[returnInfo.abandoned[i]-1];
		        }
	        }*/
	        returnInfo.benefits=prevCosts;
	        returnInfo.times=times;
	    }
	    
	    return returnInfo;
	}
}