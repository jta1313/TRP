import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Set;


//here is the iterative Bellman-Held-Karp algorithm for minimizing distance (general TSP)
public class TRP_DP{
		
	private CostInfo[][] costMatrix;
	private int numStops;
	private float serviceTime = (float)0.5;
	public float benefit;
	public int[] routing;
	public float[] benefits, times;
	public PowerSet custIndex = new PowerSet();
	
	public TRP_DP(){};
	
	public void MainTRP(CustomerList customers){
		// required variables in body
        int i, j, k, m, tempNode, index, newIndex;
        int[] S;
        float tempTime;
        
        numStops = customers.numCusts;
        
        // Set up all power sets used later
        //ReDim allPS.indexSet(0 To numStops)
        int[] indexArray = new int[numStops];
        for(i=0; i<customers.numCusts; i++){
        	indexArray[i]=i+1;
        }
        
        custIndex.PS = custIndex.getPS(indexArray);
        
        index = 0;
        for(i=1; i<= customers.numCusts;i++){
        	index = index + (int)Math.pow(2, i-1);
        }
        
        costMatrix = new CostInfo[index+1][numStops+1];
        //flag for unused array index is -1
        for(i=0;i<=index;i++){
        	for(j=0;j<=numStops;j++){
        		costMatrix[i][j]= new CostInfo();
        		costMatrix[i][j].cost= -1;
        		costMatrix[i][j].size=-1;
        	}
        }

        //build memoized costs
        boolean adding;
        maxReturn temp;

        //Call MeanVars(False)
        int numCombs;
        for(i=0;i<=numStops;i++){
            numCombs = 0;
            //if no costs yet then just apply cost from depot to jth node
            
            if(i==0){
                for(j=1; j<=numStops;j++){
                	tempTime = this.delaying(customers, j, customers.startTime, 0);
                	costMatrix[i][j].time=tempTime;
                	
                	// depends whether or not visited
					if( tempTime != customers.startTime){
						costMatrix[i][j].visited= new int[1];
						//costMatrix[i][j].cost = this.serviceLevel(customers,j, tempTime);
						costMatrix[i][j].recentVisit = j;
						costMatrix[i][j].visited[0] = j;
					}
					else{
						costMatrix[i][j].cost = 0;
						costMatrix[i][j].recentVisit = 0;
					}
                    costMatrix[i][j].cost = this.serviceLevel(customers, j, costMatrix[i][j].time);
                    costMatrix[i][j].S=new int[1];
                    costMatrix[i][j].S[0] = 0;
                    costMatrix[i][j].size = 0;
                    costMatrix[i][j].nextNode = j;
                    costMatrix[i][j].index = 0;
                    costMatrix[i][j].previous = 0;
                    numCombs = numCombs + 1;
                }
            }
            /*if have previous costs then need to shift next node to S set and add unused
            this effectively gets all costs associated with sets |S|= i*/
            else{
            	// same set S
                for(j = 0;j<=index;j++){
                    // given that all sets S have same binary total then get new sets from individual sets
                    // ending in different next nodes
                    for(k=1; k <= numStops; k++){
                        if(costMatrix[j][k].size == i - 1){
                            for(m=1;m<=numStops;m++){
	                            // adding node m as next node if not already in set or next node
	                            adding = !(SearchS(costMatrix[j][k], m));
	                            if(adding){
	                                // when adding, simply shift existing nextNode to S
	                                // increase indexing appropriate for new size
	                                tempNode = costMatrix[j][k].nextNode;
	                                newIndex = (int) (j + Math.pow(2, tempNode-1));
	                                // next node is now the "fresh" node
	                                costMatrix[newIndex][m].nextNode = m;
	                                costMatrix[newIndex][m].S = this.extendS(costMatrix[j][k].S, tempNode);
	                                // size is i
	                                costMatrix[newIndex][m].size = i;
	                                costMatrix[newIndex][m].index = newIndex;
	                                // cost of this stage is the min of last leg + rest of node
	                               
	                                temp = bestReturn(costMatrix[newIndex][m],customers);
	                                costMatrix[newIndex][m].cost = temp.returns[0];
	                                costMatrix[newIndex][m].previous = (int) temp.returns[1];
                                    costMatrix[newIndex][m].time = temp.returns[2];
                                    costMatrix[newIndex][m].visited = temp.visited;
                                    
                                    //if it raises the benefit to include the tested stage
                                    //add it to the visited set (ie if not added, abandoned)
                                    if(temp.returns[3] > 0){
                                        costMatrix[newIndex][m].recentVisit = m;
                                    }
                                    else{
                                        costMatrix[newIndex][m].recentVisit = (int) temp.returns[1];
                                    }

	                                numCombs = numCombs + 1;
	                            }
                            }
                        }
                    }
                }
            }
            //MsgBox "There are " & numCombs & " paths when |S|=" & i, vbOKOnly, "Number of Combinations"
        }
        
        int tempIndex;
        costMatrix[index][0].size = i;
        costMatrix[index][0].index = index;
        costMatrix[index][0].S= new int[numStops];
        costMatrix[index][0].nextNode = 0;
        for(i=1; i<=numStops; i++){
        	costMatrix[index][0].S[i-1] = i;
        }
        
        
        // get time of going from most recent node in previous subchain (might not be next node)
        // temp = bestCostAndTime(costs(newIndex, m))
        float maxCost,  minTime, time; 
        int previous, last,  numAbandoned;
        int[] tempIndexvisited, abandoned, visited;
        maxCost = (float) -1.0;
        minTime = (float) 1E+32;
        for(i=1; i<=numStops; i++){
	        visited = costMatrix[(int) (index - Math.pow(2, i-1))][i].visited;
	        //abandoned = costMatrix[(int) (index - Math.pow(2, i-1))][i].abandoned
	        if(costMatrix[(int) (index - Math.pow(2, i-1))][i].recentVisit == i){
	                time = costMatrix[(int) (index - Math.pow(2, i-1))][i].time + customers.distances[i][0]+ serviceTime;
	        }        		
	        else{
	                time = costMatrix[(int) (index - Math.pow(2, i-1))][i].time + customers.distances[costMatrix[(int) (index - Math.pow(2, i-1))][i].recentVisit][0];
	        }
	        if((costMatrix[(int) (index - Math.pow(2, i-1))][i].cost > maxCost) ||
	                (costMatrix[(int) (index - Math.pow(2, i-1))][i].cost == maxCost && time < minTime)){
	                maxCost = costMatrix[(int) (index - Math.pow(2, i-1))][i].cost;
	                minTime = time;
	                costMatrix[index][0].cost = maxCost;
	                costMatrix[index][0].previous = costMatrix[(int) (index - Math.pow(2, i-1))][i].recentVisit;
	                costMatrix[index][0].time = minTime;
	                //costMatrix[index][0].abandoned = abandoned
	                costMatrix[index][0].visited = costMatrix[(int) (index - Math.pow(2, i-1))][i].visited;
	                //tempIndex = index - 2 ^ (i - 1)
	//                        For j = 1 To UBound(costMatrix[index][0].visited)
	//                                tempIndex = tempIndex + 2 ^ (costMatrix[index][0].visited(j) - 1)
	//                        Next j
	        }
        }
        
        this.benefit=maxCost;
        // find this.routing and previous costs and times by backtracking through memoization
        // given that the best solution has lost customers to abandonment
        //numStops = numStops // - UBound(costMatrix[index][0].abandoned)
        int reduceIndex=0;
        reduceIndex = costMatrix[index][0].visited.length;
        
        k = 0;
        boolean out;
        abandoned = new int[numStops - reduceIndex];
        for(i=1; i<=numStops; i++){
            out = true;
            for(j=0; j<reduceIndex; j++){
                if(i == costMatrix[index][0].visited[j]){
                    out = false;
                    break;
                }
            }
            if(out == true){
                abandoned[k] = i;
                k++;
            }
        }
        
        reduceIndex = 0;
        for(i=0; i<abandoned.length; i++){
        	reduceIndex = (int) (reduceIndex + Math.pow(2, abandoned[i] - 1));
        }
        
        
        int numStopsAban; 
        numStopsAban = numStops - abandoned.length;
        this.routing = new int[numStopsAban+2];
        float[] prevCosts = new float[numStopsAban+2];
        float[] times = new float[numStopsAban+2];
        this.routing[numStopsAban] = 0;
        prevCosts[numStopsAban+1]= maxCost;
        times[numStopsAban+1] = minTime;
        
        last = costMatrix[index][0].previous;
        
        
        tempIndex = (int) (index - Math.pow(2, last-1));
        tempIndex = tempIndex - reduceIndex;
        
        i = numStopsAban;
        do {
        	this.routing[i] = last;
            prevCosts[i] = costMatrix[tempIndex][last].cost;
            times[i] = costMatrix[tempIndex][last].time;
            
            last = costMatrix[tempIndex][last].previous;
            tempIndex = (int) (tempIndex - Math.pow(2, last - 1));
            i--;
        } while (last!=0);
        
        this.routing[i] = last;
        this.benefits=prevCosts;
        times[i] = customers.startTime;
        this.times=times;

        /*//Set costs() = Nothing
        MainTRP_DP.benefit = costMatrix[index][0].cost
        MainTRP_DP.route = this.routing
        MainTRP_DP.time = costMatrix[index][0].time
        MainTRP_DP.benefits = prevCosts
        MainTRP_DP.times = times*/
	}

	// returns true if num already in cost analysis (either as part of S or the next node)
	private boolean SearchS(CostInfo x, int num){
	    boolean searchS = false;
	    for(int i = 0; i <x.S.length; i++){
	        if (num == x.S[i]){
	            searchS = true;
	            break;
	        }
	    }
	    
	    if(x.nextNode == num){searchS=true;}
	    
	    return searchS;
	}
	// extends array by 1
	private int[] extendS(int[] S, int node){
		int[] tempS= new int[S.length+1];
		int i;
	    for(i = 0; i <S.length; i++){
	    	tempS[i]=S[i];
	    }
	    tempS[i]=node;
	    return tempS;
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
	    if(time + customers.distances[customer][previous] < customers.customers[customer].early){
	            delaying = customers.customers[customer].early;
	    }
	    //shrink late window by forcing service to be done within window
	    else if(time + customers.distances[customer][previous] + serviceTime > customers.customers[customer].late){
	    	delaying = time;
	    }
	    else{
	    	delaying = time + customers.distances[customer][previous];
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
	    int i, index, tempIndex, j, size, reduceIndex, k, size2, newIndex;
	    float tempCost, newTime, tempTime, tempService;
	    maxReturn bestReturn = new maxReturn();
	    PowerSet abandoned = new PowerSet();
	    // uses visited nodes from S to get "total" index (even abandoned)
	    index = x.index;
	    
	    // first find the best solution given NEED to visit next city
	    bestReturn.returns= new float[4];
	    // -1 for service level (can only be positive so will always be at least 1 solution)
	    bestReturn.returns[0]=-1;
	    // assume very high time
	    bestReturn.returns[2] = (float) 1E+32;
	    // start at second element to avoid depot 0
	    for(i=1; i < x.S.length; i++){
            // back track in memoization to find S\{i} ending at i
            tempIndex = (int) (index - Math.pow(2, x.S[i] - 1));
//                If x.visited(i) = 4 And x.nextNode = 5 Then
//                        MsgBox "Stop"
//                End If
            
            // generate all possible abandonment sets of S
            //Call PowerSets(costs(tempIndex, x.S(i)).S)
            // from the generate power sets get the actual node numbers
            abandoned.PS = abandoned.translate(costMatrix[tempIndex][x.S[i]].S, custIndex);
            //allPS.indexSet (i)
            size = abandoned.PS.size();
            
            boolean skip = false;
            if(tempIndex == 0 || size==0){
                newIndex = tempIndex;
                size=0;
             // get time that started visit at last non-abandoned customer
				if(x.S[i] == costMatrix[newIndex][x.S[i]].recentVisit){
					tempTime = costMatrix[newIndex][x.S[i]].time + serviceTime;
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
                    
                    if(tempService > 0){
                        bestReturn.returns[3] = x.nextNode;
                        // in case when visited still empty, need to error handle appropriately
                        try{
                        	k = costMatrix[newIndex][x.S[i]].visited.length;
                        }
                        catch (Exception e){
                        	k=0;
                        }
                        bestReturn.visited=new int[k + 1];
                        if(k==0){
                        	bestReturn.visited[0] = x.nextNode;
                        }
                        else{
                            for(k=0; k <costMatrix[newIndex][x.S[i]].visited.length; k++){
                                    bestReturn.visited[k] = costMatrix[newIndex][x.S[i]].visited[k];
                            }
                            bestReturn.visited[k] = x.nextNode;
                        }
                    }                      
                    else{
                        bestReturn.returns[3] = 0;
                        bestReturn.visited = costMatrix[newIndex][x.S[i]].visited;
                    }
                }
                    
            }
            else{
	            abandoned.PS = abandoned.translate(costMatrix[tempIndex][x.S[i]].S, custIndex);
	            //allPS.indexSet (i)
	            size = abandoned.PS.size();

	            for(j=0; j <= size; j++){
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
							        tempTime = costMatrix[newIndex][x.S[i]].time + serviceTime;
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
			                    
			                    if(tempService > 0){
			                        bestReturn.returns[3] = x.nextNode;
			                        // in case when visited still empty, need to error handle appropriately
			                        try{
			                        	k = costMatrix[newIndex][x.S[i]].visited.length;
			                        }
			                        catch (Exception e){
			                        	k=0;
			                        }
			                        bestReturn.visited=new int[k + 1];
			                        if(k==0){
			                        	bestReturn.visited[0] = x.nextNode;
			                        }
			                        else{
			                            for(k=0; k <costMatrix[newIndex][x.S[i]].visited.length; k++){
			                                    bestReturn.visited[k] = costMatrix[newIndex][x.S[i]].visited[k];
			                            }
			                            bestReturn.visited[k] = x.nextNode;
			                        }
			                    }                      
			                    else{
			                        bestReturn.returns[3] = 0;
			                        bestReturn.visited = costMatrix[newIndex][x.S[i]].visited;
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
}