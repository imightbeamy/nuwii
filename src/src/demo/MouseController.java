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
* File:    	MouseController.java
*/
package demo;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

import util.XYZ;
import dualCamera.DualCameraEvent;
import dualCamera.DualCameraListener;
import dualCamera.DualMoteCamera;
import dualCamera.Gestures;

/**
 * MouseController allows the mouse to be controlled using the Wii Remotes. 
 * Pinching/Unpinching is used as a click.
 */
public class MouseController implements DualCameraListener, Gestures
{
	
	private final static XYZ MIN = DualMoteCamera.MIN, MAX = DualMoteCamera.MAX;
	
	private static Dimension dim;
	private static Robot robot;

	private int position_y = 0, position_x = 0;

	
	public static void main(String[] args)
	{	
		//Get the display size
		Toolkit toolkit = Toolkit.getDefaultToolkit ();
		dim = toolkit.getScreenSize();
		
		try
		{
			robot = new Robot();
		} 
		catch (AWTException e)
		{
			e.printStackTrace();
		}
		

		MouseController controller = new MouseController();
		
		/* Initialize DualMoteCamera with the values from the our board that 
		 * holds the Wii Remotes.
		 * 
		 * 609.6 is the distance between our Wii Remotes in mm (2 feet).
		 * 22.5 is the angle our Wii remotes are at. 
		 * 
		 * The distance does not have to be mm. Whatever units you input will be
		 * the units output by DualMoteCamera. See DualMoteCamera Documentation. 
		 */
		DualMoteCamera cameras = new DualMoteCamera(609.6, Math.toRadians(22.5), controller);
		
		if(!cameras.setUpMotes())
			System.exit(1);
		
		cameras.addDualCameraListener(controller);
		
		//Go FOREVER!!! or just turn off the Wii Remotes to quit...
		
		try{
		for(;;);
		}
		catch (Exception e)
		{
			System.out.println("Wii Remotes diconected.");
			System.exit(0);
		}
	}
 
	public void pointChanged(DualCameraEvent evt) 
	{
		XYZ point = evt.getAveragePoint();
		
		//Update if there is a valid point 
		if(point != null)
		{
			position_x = dim.width - (int)((point.x - MIN.x) * dim.width /(MAX.x - MIN.x));
			position_y = dim.height - (int)((point.y - MIN.y) * dim.height /(MAX.y - MIN.y));
		}
		
		robot.mouseMove(position_x, position_y);
	}

	public void pinch(XYZ location)
	{
		robot.mousePress(InputEvent.BUTTON1_MASK);
	}

	public void unPinch(XYZ location)
	{
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}

	//Swipe does nothing in this implementation 
	public void swipeInX(XYZ origin, int direction) {}
	public void swipeInY(XYZ origin, int direction) {}
	public void swipeInZ(XYZ origin, int direction)	{}

}
