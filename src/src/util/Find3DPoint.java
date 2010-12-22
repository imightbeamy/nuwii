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

/**
 * This class contains a method for finding a 3D point from two rays. Two rays will
 * very rarely collide nicely. To fix that, the raysIntersectionPoint method finds
 * the midpoint of the line that is perpendicular to two rays. Mathematically where
 * the line is perpendicular is where the two rays are the closest. This takes in
 * both of the rays beginning and end points and returns the midpoint of that line.
 * 
 * This code is adapted from "The Shortest Line Between Two Lines In 3D"
 * http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline3d/ Written by Paul Bourke
 */

public class Find3DPoint {
	
	private static final double EPS = 0.001;
	
	/**
	 * Calculate the line segment PaPb that is the shortest route between two
	 * lines P1P2 and P3P4. This is done by finding the line that is perpendicular
	 * to both rays. To learn more about how this works read Paul Bourke's explanation
	 * at http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline3d/
	 *    Pa = P1 + mua (P2 - P1)
	 *    Pb = P3 + mub (P4 - P3)
	 *    
	 * @param lefto the left ray start
	 * @param leftd some point along the left ray
	 * @param righto the right ray start
	 * @param rightd some point along the right ray
	 * @return null if no solution exists or the 3D point with an error measurement
	 */
	
	public static XYZ raysIntersectionPoint( XYZ lefto, XYZ leftd,XYZ righto, XYZ rightd )
	{
		// Initialize all of the variables.
		XYZ found;
		XYZ pa = new XYZ();
		XYZ pb = new XYZ();
		XYZ p13 = new XYZ();
		XYZ p43 = new XYZ();
		XYZ p21 = new XYZ();
		double d1343,d4321,d1321,d4343,d2121, numer, denom, mua, mub;
		
		// It returns null if there is no solution.
		p13.setX( lefto.getX() - righto.getX() );
		p13.setY( lefto.getY() - righto.getY() );
		p13.setZ( lefto.getZ() - righto.getZ() );
		p43.setX( rightd.getX() - righto.getX() );
		p43.setY( rightd.getY() - righto.getY() );
		p43.setZ( rightd.getZ() - righto.getZ() );
		if (Math.abs(p43.getX())  < EPS && Math.abs(p43.getY())  < EPS && Math.abs(p43.getZ())  < EPS)
			return null;
		p21.setX( leftd.getX() - lefto.getX() );
		p21.setY( leftd.getY() - lefto.getY() );
		p21.setZ( leftd.getZ() - lefto.getZ() );
		if (Math.abs(p21.getX())  < EPS && Math.abs(p21.getY())  < EPS && Math.abs(p21.getZ())  < EPS)
			return null;

		d1343 = p13.getX() * p43.getX() + p13.getY() * p43.getY() + p13.getZ() * p43.getZ();
		d4321 = p43.getX() * p21.getX() + p43.getY() * p21.getY() + p43.getZ() * p21.getZ();
		d1321 = p13.getX() * p21.getX() + p13.getY() * p21.getY() + p13.getZ() * p21.getZ();
		d4343 = p43.getX() * p43.getX() + p43.getY() * p43.getY() + p43.getZ() * p43.getZ();
		d2121 = p21.getX() * p21.getX() + p21.getY() * p21.getY() + p21.getZ() * p21.getZ();
		
		denom = d2121 * d4343 - d4321 * d4321;
		if (Math.abs(denom) < EPS)
			return null;
		numer = d1343 * d4321 - d1321 * d4343;

		mua = numer / denom;
		mub = (d1343 + d4321 * (mua)) / d4343;

		// This sets the two points where the line between the two rays is perpendicular to both rays.
		// That line is by definition the shortest line segment and where the rays are the closest.
		pa.setX( lefto.getX() + mua * p21.getX() );
		pa.setY( lefto.getY() + mua * p21.getY() );
		pa.setZ( lefto.getZ() + mua * p21.getZ() );
		pb.setX( righto.getX() + mub * p43.getX() );
		pb.setY( righto.getY() + mub * p43.getY() );
		pb.setZ( righto.getZ() + mub * p43.getZ() );
		
		// This calculates the midpoint between the two ends of the shortest line segment.
		double midX = ( pa.getX() + pb.getX() )/2.0;
		double midY = ( pa.getY() + pb.getY() )/2.0;
		double midZ = ( pa.getZ() + pb.getZ() )/2.0;
		
		// This calculates the length of the shortest line segment.
		double length = Math.sqrt( ( pa.getX() - pb.getX() ) * ( pa.getX() - pb.getX() ) +
				( pa.getY() - pb.getY() ) * ( pa.getY() - pb.getY() ) +
				( pa.getZ() - pb.getZ() ) * ( pa.getZ() - pb.getZ() ) );
		  
		found = new XYZ( midX, midY, midZ, length );
		   
		return found;
	}

}
