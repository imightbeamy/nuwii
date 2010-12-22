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
* File:    XYPointWindow.java
*/
package demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import util.XYZ;
import dualCamera.DualCameraEvent;
import dualCamera.DualCameraListener;
import dualCamera.DualMoteCamera;


/**    
 * XYZPointWindow is a JPanle that displays the points sent by the Wii Remotes.
 * Each point is a circle that changes size based on the Z value of the point 
 * and the X and Y values are show as the position on the window. When a point 
 * can no longer be seen by the cameras the point is shown at the last seen 
 * position and size and is displayed in red.
 * @author Amy Ciavolino
 */

@SuppressWarnings("serial")
public class XYZPointWindow extends JPanel{
	
	//The dimension of the monitor for window size and point scaling
	private static Dimension dim;
	protected static final int TEXT_SPACING = 20;
	
	private static XYZ MIN = DualMoteCamera.MIN;
	private static XYZ MAX = DualMoteCamera.MAX;
	private static JFrame frame;

	//The point being received from DualmoteCamera
	private static XYZ real_points[] = new XYZ[4];
	
	//Previous points to use when point is not in view
	private XYZ old_points[] = new XYZ[4];
		
	public static void main(String[] args)
	{
		//Gets the display size
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		dim = toolkit.getScreenSize();
		
		//Initialize real_points to avoid null pointers on start up
		for(int i = 0; i < real_points.length; i++)
			real_points[i] = new XYZ();
		
		/*Initialize DualMoteCamera with the values from the our board that 
		 * holds the Wii Remotes.
		 * 
		 * 609.6 is the distance between our Wii Remotes in mm.
		 * 22.5 is the angle our Wii remotes are at. 
		 * 
		 * The distance does not have to be mm. Whatever units you input will be
		 * the units output by DualMoteCamera. See DualMoteCamera Documentation. 
		 */
	    DualMoteCamera cameras = new DualMoteCamera(609.6, Math.toRadians(22.5));
	    
	    if(!cameras.setUpMotes())
	    	System.exit(1);
	    
		//Initialize frame 
		frame = new JFrame("Wiimote points");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new XYZPointWindow());
		frame.setSize(dim.height, dim.height);
		frame.setVisible(true);
		frame.repaint();
	    
	    cameras.addDualCameraListener(new DualCameraListener() {

			public void pointChanged(DualCameraEvent evt)
			{			 
				System.out.print("\n3D points: ");
				real_points = evt.getRealPoints();
				
				for(int i = 0; i < real_points.length; i++)	
				{	
					if(real_points[i] == null)
						System.out.print("invalid\t| ");
					else
						System.out.print(real_points[i] + "\t| ");
				}
				
				frame.repaint();
			}
	    });
	    	
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 * 
	 */
	public void paintComponent(Graphics g)
	{
		int pos = TEXT_SPACING;
		for(int i = 0; i < real_points.length; i++)
		{
			//If the point can be seen update old points 
			if(real_points[i] != null)
			{
				g.setColor(Color.BLACK);
				old_points[i] = real_points[i];
			}
			else
				g.setColor(Color.RED);
			
			int size_z = adjustToSize(old_points[i].getZ());
			int half_size = size_z/2;
			int x = adjustX(old_points[i].getX()) - half_size;
			int y = adjustY(old_points[i].getY()) - half_size; 
			
			//Draw point
			g.drawOval(x,y, size_z, size_z);
			
			//Draw point information
			g.drawString((i + 1) + ": " + old_points[i].toString(), 5, pos);
			pos+=TEXT_SPACING;
		}
	}
	
	/*
	 * Returns values adjusted for display for each dimension.   
	 */
	protected int adjustY(double p)
	{
		return dim.height - (int)((p - MIN.y) * dim.height /(MAX.y - MIN.y));
	}
	
	protected int adjustX(double p)
	{
		return dim.height - (int)((p - MIN.x) * dim.height /(MAX.x - MIN.x));
	}
	
	protected int adjustToSize(double p)
	{
		return (int)((p - MIN.z) * 100/(MAX.z - MIN.z));
	}

}
