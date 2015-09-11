import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;

public class Main {
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

			// find best route
			TRP_DP problem = new TRP_DP();
			problem.MainTRP(customers);
			problem.print(infileName);
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
	
	

}
