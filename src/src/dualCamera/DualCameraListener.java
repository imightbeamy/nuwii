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

import java.util.EventListener;

/**
 * 
 * Listener class which looks for point changes 
 * in the two Wii Remotes
 * 
 * You have to create a DualCameraListener to listen for updates from the WiiRemotes
 * You get all the information from the evt event passed into pointchanged. 
 * Used in conjunction with DualCameraEvent
 * 
 * See fireDualCameraEvent, and addDualCameraListener methods in DualMoteCamera
 */
public interface DualCameraListener extends EventListener
{
	public void pointChanged(DualCameraEvent evt);
}

