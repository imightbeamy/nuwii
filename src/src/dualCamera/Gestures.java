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

package dualCamera;

import util.XYZ;

public interface Gestures
{

	/**
	 * The pinch gesture is activated when two IR points are brought close
	 * together until they as one point. Although location is included
	 * in this method, it is generally easier to use the current point from 
	 * DualMoteCamera when the pinch is detected. This is called from 
	 * DualMoteCamera and implemented in your class.
	 * @param location
	 */
	public void pinch(XYZ location);
	
	/**
	 * Unpinch is called from DualMoteCamera when a pinched point separates into
	 * two points. Will only be called after a pinch.  
	 * @param location
	 */
	public void unPinch(XYZ location);
	
	/**
	 * SwipeInX is called from DualMoteCamera when a point is moved for a given
	 * distance at a given speed in the X direction.
	 * is detected.
	 * @param origin The approximate starting point of the swipe.  
	 * @param direction Negative value means right swipe, positive means left swipe. 
	 */
	public void swipeInX(XYZ origin, int direction);
	
	/**
	 * SwipeInY is called from DualMoteCamera when a point is moved for a given
	 * distance at a given speed in the X direction.
	 * @param origin The approximate starting point of the swipe.
	 * @param direction Negative value means down swipe, positive means up swipe. 
	 */
	public void swipeInY(XYZ origin, int direction);
	
	/**
	 * SwipeInZ is called from DualMoteCamera when a point is moved for a given
	 * distance at a given speed in the X direction.
	 * @param origin The approximate starting point of the swipe.
	 * @param direction Negative value means forward swipe, positive means backward swipe. 
	 */
	public void swipeInZ(XYZ origin, int direction);
}
