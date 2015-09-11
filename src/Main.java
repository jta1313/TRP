import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;


public class Main {
	
	public static ArrayList<CustomerList> currentCustomers;
	public static ArrayList<TRP_DP> solutions;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		long start, end;
		start = System.currentTimeMillis();
		
		if(args.length != 1 ){
			System.out.println("Wrong number of files specified in input");
			System.exit(0);
		}
		String infileName = (String) args[0];
		
		try{
			
			int numCusts, startTime, endTime;
			
			//numCusts = 4; startTime = 8; endTime = 14;
			// create empty list of customers
			CustomerList customers = new CustomerList();
			
			
			//read in data from input file
			customers.InputData(infileName);
			
			TRP_DP problem = new TRP_DP();
			
			//root out infeasible customers
			int i=0,j=0,k=0;
			int[] feasible = new int[customers.numCusts];
			int[] infeasible = new int[customers.numCusts];
			for(i=1; i<=customers.numCusts; i++){
				if(customers.startTime+customers.distances[0][i]+customers.customers[i].serviceTime <= customers.customers[i].late){
					feasible[k]=i;
					k++;
				}
				else{
					infeasible[j]=i;
					j++;
				}
			}
			
			infeasible = Arrays.copyOf(infeasible, j);
			currentCustomers = new ArrayList<CustomerList>();
			if(j>0){
				CustomerList reducedCusts = new CustomerList();
				reducedCusts = customers.reducedCustomers(Arrays.copyOf(feasible, k));
				currentCustomers.add(reducedCusts);
			}
			else{
				currentCustomers.add(customers);
			}

			// find best route for first truck (may be all customers)
			
			problem.MainTRP(currentCustomers.get(0));
			
			solutions = new ArrayList<TRP_DP>();
			solutions.add(problem);
			// find remaining routes for subsequent trucks until can meet all demand
			
			// keep adding new truck routes until no more gain in service level (benefit)
			i=0;
			while(problem.benefit != 0 && problem.abandoned != null && problem.abandoned.length !=0){
				i++;
				CustomerList reducedCusts = new CustomerList();
				reducedCusts = customers.reducedCustomers(problem.abandoned);
				currentCustomers.add(reducedCusts);
				problem = new TRP_DP();
				problem.MainTRP(currentCustomers.get(i));
				solutions.add(problem);
			}
			
			print(infileName,solutions,infeasible);
			//problem.print(infileName);
			//customers.generateList(numCusts,startTime,endTime);
		}
		catch (FileNotFoundException fnfe)
		{
			//infile.close();
			System.out.println("Exception caught: " + fnfe + "\n");
			System.exit(0);
		}
		catch (Exception e)
		{
			//infile.close();
			System.out.println("Exception caught: " + e + "\n");
			System.exit(0);
		}
		end = System.currentTimeMillis();
		
		System.out.println("Total execution time: " + (end - start) );
	}
	
	
	/**print(writer) prints out the edges in an EdgeList in order
	 * @param writer BufferedWriter where output is going
	 * @throws IOException
	 */
	private static void print(String outfileName, ArrayList<TRP_DP> solutions, int[] infs) throws IOException{
		BufferedWriter writer = Files.newBufferedWriter(FileSystems.getDefault().
	    		getPath(System.getProperty("user.dir")+ "/src/output", outfileName));
		String temp = "";
		int i, count;
		count = 0;
		// solutions
		for(TRP_DP problem: solutions){
			count++;
				temp = temp.concat(String.format("Truck #%d%n%n",count));
				for(i=0; i<problem.routing.length; i++){
					temp = temp.concat(String.format("%d %f %f%n",problem.routing[i],problem.benefits[i],problem.times[i]));
				
			}
				temp = temp.concat(String.format("%n%n"));
		}
		// infeasible
		temp = temp.concat(String.format("INFEASIBLE CUSTOMERS%n%n"));
		for(i=0; i<infs.length; i++){
			temp = temp.concat(String.format("%d",infs[i]));
		}
		writer.write(temp);
		writer.close();
	}
}
