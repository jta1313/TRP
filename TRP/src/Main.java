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
			currentCustomers = new ArrayList<CustomerList>();
			currentCustomers.add(customers);
			
			// find best route for first truck (may be all customers)
			TRP_DP problem = new TRP_DP();
			problem.MainTRP(currentCustomers.get(0));
			
			solutions = new ArrayList<TRP_DP>();
			solutions.add(problem);
			// find remaining routes for subsequent trucks until can meet all demand
			int i=1;
			while(problem.abandoned.length != 0){
				CustomerList reducedCusts = new CustomerList();
				reducedCusts = customers.reducedCustomers(problem.abandoned);
				currentCustomers.add(reducedCusts);
				problem = new TRP_DP();
				problem.MainTRP(currentCustomers.get(i));
				solutions.add(problem);
			}
			
			print(infileName,solutions);
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
	private static void print(String outfileName, ArrayList<TRP_DP> solutions) throws IOException{
		BufferedWriter writer = Files.newBufferedWriter(FileSystems.getDefault().
	    		getPath(System.getProperty("user.dir")+ "/src/output", outfileName));
		String temp = "";
		int i, count;
		count = 0;
		for(TRP_DP problem: solutions){
			count++;
			temp = temp.concat(String.format("Truck #%d%n%n",count));
			for(i=0; i<problem.routing.length; i++){
				temp = temp.concat(String.format("%d %f %f%n",problem.routing[i],problem.benefits[i],problem.times[i]));
			}
			temp = temp.concat(String.format("%n%n"));
		}
		
		writer.write(temp);
		writer.close();
	}
}
