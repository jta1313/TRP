
/** Single customer instance that holds just the x and y coordinates
 * @author Jeremy
 *
 */
public class Customer {
	
	private float xCoord;
	private float yCoord;
	public float early;
	public float late;
	public static final int upperCoord = 2;
	public static final int lowerCoord = 0;

	/** complete constructor
	 * @param xCoord
	 * @param yCoord
	 * @param early
	 * @param late
	 */
	public Customer(float xCoord, float yCoord,float early, float late){
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.early = early;
		this.late = late;
	}
	public Customer(float early, float late){
		this.early = early;
		this.late = late;
	}
	/** empty constructor
	 * 
	 */
	public Customer(){
		this.xCoord = 0;
		this.yCoord = 0;
		this.early = 0;
		this.late = 0;
	}
	
	// get methods for xCoord, yCoord
	public float getxCoord(){return this.xCoord;}
	public float getyCoord(){return this.yCoord;}
	public float getEarly(){return this.early;}
	public float getLate(){return this.late;}
	
	// set methods for xCoord, yCoord, and next node
	public void setxCoord(float xCoord){this.xCoord = xCoord;}
	public void setyCoord(float yCoord){this.yCoord = yCoord;}
	public void setEarly(float early){this.early = early;}
	public void setLate(float late){this.late = late;}
	
	public void RandCoords(){
		this.setxCoord((float) Math.random()*upperCoord);
		this.setyCoord((float) Math.random()*upperCoord);
	}
	/** This functions returns the overall service level of a route (0 to 1 based off proportion
	 * of satisfied customers)
	 * @param customer
	 * @param time
	 * @return service level
	 */
	public float serviceLevel(Customer customer, float time, int numCusts){
		
		float service = 0;
	    //arrives before time window
		if(time <= customer.early){
			service = 1;
		}
/*		If custTimeWindows(customer).early - time >= 1 Then
			serviceLevel = 0
		Else
			serviceLevel = 1 - (custTimeWindows(customer).early - time)
		End If*/
		// arrives after time window
		else if(time > customer.late){
			service = 0;
		}
/*		If time - custTimeWindows(customer).late >= 1 Then
			serviceLevel = 0
		Else
			serviceLevel = 1 - (time - custTimeWindows(customer).late)
		End If*/

		// arrives inside time window
		else{
			service = 1;
		}
		// scale each customer for whole route (ie 1 out of 10 customers only counts for 10%)
        service = 1 / numCusts * service;
        return service;
	}
}