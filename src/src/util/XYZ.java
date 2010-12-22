/**
* Project: NuWii
* Authors: Amy Ciavolino -   AmyCiav@gmail.com
* 		   Camille Marvin -  sapphiremirage@gmail.com
*          James Coddington- jxc6857@rit.edu
*          Jason Creighton - jtc6189@gmail.com
*          
* Developed at: NSF Computer Science REU at Rochester Institute of Technology, Summer 2010
*          
* Code Available at: http://code.google.com/p/nuwii/
* 
* Acknowledgments: This material is based upon work supported by the 
*                   National Science Foundation under Award No. CCF-0851743.
* 
* License: This work is licensed under Creative Commons GNU General Public License License
* See http://www.gnu.org/licenses/gpl-3.0.html for the full details
* See http://creativecommons.org/licenses/GPL/2.0/ for summary.
* 
*/

package util;

public class XYZ {

	/**
	 * The x, y, and z values of the point
	 */
	public double x, y, z;
	
	/**
	 * Error is used by some methods to store some extra information.
	 * Specifically information on how far off the error is in the point.
	*/
	public double error = 0; 
	
	/**
	 * used as position into array for a constructor which takes an array
	 */
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	/**
	* Size is used to help determine when the points are switched.
	* For our purposes it is the size of the blob the wiimote returns
	*/
	public double size = 0.0;

	/**
	 * Constructor One
	 * 
	 * @param x the x position
	 * @param y the y position
	 * @param z the z position
	 */
	public XYZ ( double x, double y, double z )
	{	
		this(x, y, z, 0);
	}

	/**
	 * Constructor Two
	 * 
	 * @param x the x position
	 * @param y the y position
	 * @param z the z position
	 * @param size  the size of the point. 
	 */
	public XYZ ( double x, double y, double z, double size )
	{	
		this.x = x;
		this.y = y;
		this.z = z;
		this.size = size;
	}
	
	/**
	 * Default Constructor sets everything default to 0
	 * 
	 */
	public XYZ()
	{
		this(0,0,0,0);
	}
	
    /**
     * Constructor Three
     * 
     * @param corrdArray array of size 3 which has x,y,z
     */
	public XYZ(double[] corrdArray)
	{
		this(corrdArray[X], corrdArray[Y], corrdArray[Z], 0);
	}
	
	/**
	 * Constructor sets this point equal to the point passed in
	 * @param point
	 */
	public XYZ(XYZ point)
	{
		this(point.x, point.y, point.z, point.size);
	}
	
	/**
	 * Returns the X position of the point
	 * 
	 * @return the x position
	 */
	public double getX()
	{	
		return x;	
	}
	
	/**
	 * Returns the Y position of the point
	 * 
	 * @return the y position
	 */
	public double getY()
	{	
		return y;	
	}
	
	/**
	 * Returns the Z position of the point
	 * 
	 * @return the z position
	 */
	public double getZ()
	{	
		return z;	
	}
	
	/**
	 * Setter method for x position
	 * 
	 * @param x
	 */
	public void setX( double x )
	{
		this.x = x;	
	}
	
	/**
	 * Setter method for y position
	 * 
	 * @param y
	 */
	public void setY( double y)
	{	
		this.y = y;	
	}
	
	/**
	 * Setter method for z position
	 * 
	 * @param z
	 */
	public void setZ( double z )
	{	
		this.z = z;
	}
	
	public double[] getArray()
	{
		return new double[]{x,y,z};
	}
	
	
	/**
	 * Sets the error for the point. The error is used to help find 
	 * which IR points match up from the 2 Wii Remotes
	 * 
	 * @param inerr 
	 */
	public void setError(double inerr)
	{
		error = inerr;
	}
	
	
	/**
	 * Returns the error for the point. Used to help find which
	 * IR points match up from the 2 Wii Remotes
	 * 
	 * @return The error of the point
	 */
	public double getError()
	{
		return error;
	}
	
	/**
	 * Sets the size of the point. The size is used to help find
	 * which IR point is which.
	 * 
	 * @param The size of the point
	 */
	public void setSize(double size)
	{
		this.size = size;
	}
	
	/**
	 * Returns the size of the point. Used to help find which IR points
	 * match.
	 * 
	 * @return The size of the point
	 */
	public double getSize()
	{
		return size;
	}
	
	/**
	 * Subtracts the given points from the calling point. Each 
	 * coordinate of the given point subtracted from each coordinate 
	 * of the calling point.
	 *   
	 * @return The result of the subtraction. 
	 */
	public XYZ subtract(XYZ point)
	{
		XYZ returnpoint = new XYZ();
		returnpoint.setX(x - point.getX());
		returnpoint.setY(y - point.getY());
		returnpoint.setZ(z - point.getZ());
		return returnpoint;
	}
	
	/**
	 * Returns the average between this point and the passed in point
	 * 
	 * @param point
	 * @return
	 */
	public XYZ average(XYZ point)
	{
		if(point == null)
			return this;
		XYZ returnpoint = new XYZ();
		returnpoint.setX((x + point.getX())/2);
		returnpoint.setY((y + point.getY())/2);
		returnpoint.setZ((z + point.getZ())/2);
		return returnpoint;
	}
	
	/**
	 * Sets the x,y,z values of the point to the equivalent positive values
	 * 
	 */
	public void abs()
	{
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
	}
	
	/**
	 * Takes a point and calculates the cross product of the calling 
	 * point and the given point. 
	 * @return A XYZ point that is the cross product of the two points.
	 */
	public XYZ crossProduct(XYZ point)
	{
		XYZ returnpoint = new XYZ();
		returnpoint.setX(  y*point.getZ()  -  z*point.getY()  );
		returnpoint.setY(  z*point.getX()  -  x*point.getZ()  );
		returnpoint.setZ(  x*point.getY()  -  y*point.getX()  );
		return returnpoint;
	}
	
	/**
	 * Does the |A| instruction on the 1X3 Matric A(thepoint in the method)
	 * 
	 */
	public double distanceToOrg() 
	{
		return Math.sqrt( Math.pow(x,2.0)  +   Math.pow(y,2.0)  +   Math.pow(z,2.0) );
	}
	
	/**
	 * toString method for easy output
	 * 
	 */
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ") Err: " + error ;
	}
		
}
