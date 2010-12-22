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

/**
* File:    DualCameraEvent.java
*/
package dualCamera;

import util.XYZ;

/**
 * DualCameraEvent is used to provide contextual information about an event
 * to the handler processing the event
 *
 */
public class DualCameraEvent 
{
	/**
	 * The DualMoteCamera which does most of the work
	 */
	DualMoteCamera source;
	
	/**
	 * Constructor which takes in a DualMoteCamera
	 * @param source
	 */
	DualCameraEvent(DualMoteCamera source)
	{
		this.source = source;
	}
	
	/**
	 * Returns the DualMoteCamera this event is using
	 * @return
	 */
	public DualMoteCamera getSource() {
		return source;
	}
	
	/**
	 * Retrieves a point from the left Wii Remote. The point retrieved
	 * corresponds to the slot number passed in.
	 * @param slot is the index into the point array within the Wii Remote
	 * @return
	 */
	public XYZ getLeftPoint(int slot)
	{
		return source.getLeftMotePoint(slot); 
	}
	
	/**
	 * Retrieves a point from the right Wii Remote. The point
	 * retrieved corresponds to the slot number passed in.
	 * @param slotis the index into the point array within the Wii Remote
	 * @return
	 */
	public XYZ getRightPoint(int slot)
	{
		return source.getRightMotePoint(slot); 
	}
	
	/**
	 * Retrieves the real point in space where the infrared light is being detected
	 * See getRealPoints in DualMoteCamera for details
	 * @return
	 */
	public XYZ[] getRealPoints()
	{
		return source.getRealPoints();
	}
	
	/**
	 * Averages together the points returned from getRealPoints.
	 * Useful for interpretting multiple points from the Wii Remotes 
	 * as a single point. 
	 * See getRealPoints in DualMoteCamera for details of how that works.
	 * @return
	 */
	public XYZ getAveragePoint()
	{
		XYZ[] points = source.getRealPoints();
		
		if(points[0] == null && points[1] == null)
			return new XYZ();
		
		if(points[0] == null)
			return points[1];
		
		if(points[1] == null)
			return points[0];
		
		return points[0].average(points[1]);
	}
	
	
}
