import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;


/** array to hold all customer info
 * @author Jeremy
 *
 */
public class CustomerList {
	public Customer[] customers;
	public float[][] distances;
	public int numCusts;
	public float startTime;
	public float endTime;
	private int numWindows;
	public static final int timeWindowLength=2;

	/**constructor, assume list must be generated with given number of customers
	 * 
	 */
	public CustomerList(int numCusts, float startTime, float endTime){
		this.numCusts=numCusts;
		this.startTime = startTime;
		this.endTime =endTime;
		
		//this.generateList(numCusts,startTime,endTime);
	}
	public CustomerList(){}
	
	
	/** used to generate list of customers, including their coordinates and time windows
	 * @param numCusts
	 * @param startTime
	 * @param endTime
	 */
	public void generateList(int numCusts, float startTime, float endTime){
		int i,j;
		
		// have one extra "customer" for the depot
		customers = new Customer[numCusts+1];
		// get randomized coordinates for depot (index 0) and all customers (remaining indices)
		for(i=0; i<=numCusts; i++){
			customers[i] = new Customer();
			customers[i].setxCoord((float) Math.random()*Customer.upperCoord);
			customers[i].setyCoord((float) Math.random()*Customer.upperCoord);
		}
		
		// assume that each possible time window for customers are 2 hours
		this.numWindows=(int) Math.floor((this.endTime-this.startTime)/timeWindowLength);
		
		int maxNum;
		
		// set up time windows given cannot have difference of more than one customer in each window
		maxNum = (int)Math.ceil((float)numCusts/(float)this.numWindows);
		
		int[] custInWindows = new int[this.numWindows];
		int window;
		boolean goodWindow;
		for(i=1; i<=numCusts;i++){
			goodWindow = false;
			// get random window until customer fits
			do{
				window = (int)(Math.random()*this.numWindows);
				if(custInWindows[window]< maxNum){
					goodWindow = true;
					custInWindows[window]=custInWindows[window]+1;
				}
			}while(!goodWindow);
			customers[i].setEarly(this.startTime+(window-1)*timeWindowLength);
			customers[i].setLate(this.startTime+(window)*timeWindowLength);
		}
		
		this.distances= new float[this.numCusts+1][this.numCusts+1];
		// set distances among customers
		for(i=0; i<=numCusts; i++){
			for(j=i; j<=numCusts; j++){
				this.distances[i][j]=(float)this.E2Norm(customers[i],customers[j]);
				this.distances[j][i]=(float)this.E2Norm(customers[i],customers[j]);
			}
		}
	}
	
	private double E2Norm(Customer a, Customer b){
		return Math.sqrt(Math.pow(a.getxCoord()-b.getxCoord(),2)+Math.pow(a.getyCoord()-b.getyCoord(),2));
	}
	
	/** Read in input file in specific format to get # customers, customer windows, and distances among customers
	 * @param filename is the name of the text file from which to read data
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public void InputData(String filename) throws NumberFormatException, IOException{
		BufferedReader infile =Files.newBufferedReader(FileSystems.getDefault().
	    		getPath(System.getProperty("user.dir")+ "/src/input", filename));
		String entry;
		int i, cust1, cust2, breaks=0, early, late, row = 0;
		double distance;
		int lineNum = 0;
		while((entry = infile.readLine()) != null){
			//System.out.println(entry);
			//System.out.println(entry);
			lineNum++;
			if(entry.equals("-1")){breaks++;}
			// first line is number of customers
			else if (lineNum==1){
				this.numCusts = Integer.parseInt(entry);
				this.customers = new Customer[this.numCusts+1];
				distances = new float[this.numCusts+1][this.numCusts+1];
			}
			// second line is daily start time
			else if(lineNum == 2){
				this.startTime = Float.parseFloat(entry);
			}
			// third line is daily end time
			else if(lineNum == 3){
				this.endTime = Float.parseFloat(entry);
			}
			// remaining lines are customer info
			else if (lineNum > 3 && breaks < 1){
				//read input line and split accordingly
				String[] fields = entry.split("\\s+");
				early = Integer.parseInt(fields[1]);
				late = Integer.parseInt(fields[2]);
				this.customers[Integer.parseInt(fields[0])]= new Customer(early,late);
			}
			else if (lineNum > 3 && breaks == 1){
				//read input line and split accordingly
				String[] fields = entry.split("\\s+");
				for(i=0; i <= this.numCusts; i++){
					distances[row][i]=Float.parseFloat(fields[i]);
				}
				row++;
			}
			else if(breaks == 2){break;}
		}
	}
}
