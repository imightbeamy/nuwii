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
* File:    DualMoteCamera.java
*/
package dualCamera;

import java.util.List;

import javax.swing.event.EventListenerList;


import motej.IrCameraMode;
import motej.IrCameraSensitivity;
import motej.IrPoint;
import motej.Mote;
import motej.event.IrCameraEvent;
import motej.event.IrCameraListener;
import motej.request.ReportModeRequest;
import util.Find3DPoint;
import util.MultiMoteFinder;
import util.XYZ;


/**
 * This DualMoteCamera class coordinates between two Nintendo Wii Remotes used
 * as IR cameras to find a point in 3D. The motes should be parallel and the 
 * distance between the motes can be passed to the constructor. The units the 
 * distance in measured in will be the units output for the point. By default,
 * the distance is 2 ft. measured in mm.
 * 
 * @author Amy Ciavolino
 */
public class DualMoteCamera
{		
	
	/**
	 * Boolean to keep track of if pinch gesture was recognized
	 */
	private boolean pinched = false;
	
	/**
	 * points_not_found counts up the number of points that are missing. It assumes 4 points total.
	 */
	public int points_not_found = 0;
	
	/**
	 * stores what the last points were. 
	 *Warning: these can be wrong if the last points were wrong. Can also be null
	 */
	public XYZ last_left_point = new XYZ();
	public XYZ last_right_point = new XYZ();

	/**
	 * stores the latest distance between and last distance between the two points.
	 *if two points arn't found then it doesn't update the last and latest
	 *The reason for not updating when points get lost is because it is more useful to know 
	 * where they were before they were lost. Starts them at 999 to make sure a pinch doesn't get read immediately
	 */
	public double latest_distance_between_points = 999.9;
	public double last_distance_between_points   = 999.9;

	/**
	 * stores the most recent points. When a new point is found then these are moved to be the last_l/r_point
	 */
	public XYZ latestleftpoint = new XYZ();
    public XYZ latestrightpoint = new XYZ();

    /**
     * last left does not get used. 
     */
	public int lastleft = 0;

	/**
	 * Last right stores which of the two points the right camera saw was the one that corresponded to the first
	 * point the left camera saw.
	 */
	public int lastright = 0;
	public int otherlastright = 1;
	public double lastxdis = 5000;
    
	/**
	 * stores if the points might be wrong or not. This can be true when one camera 
	 * sees two points and the other sees one. It can also happen in a few other scenarios such 
	 * as when the Wii Remotes are not placed exactly correctly. While this variable is not set
	 * correctly in all situations it can still be useful
	 */
	public boolean couldbewrong = true;
	
	/**
	 * The observed minimum value of each coordinate to use for scaling.
	 */
	public static final XYZ MIN = new XYZ(15 ,-169, 330);
	
	/**
	 * The observed maximum value of each coordinate to use for scaling.
	 */
	public static final XYZ MAX = new XYZ(575, 132, 1640);

	/**
	 * Pi variable intialized to save time in later calculations
	 */
	private static final double PIE = Math.PI;
    
	/**
	 * angle between wiimote and the board holding them.
	 * It is assumed that the angle is the inner angle
	 */
	private static double wiimote_angle = .392699; //22.5 degrees;		
	private static double wiimote2_angle = 2*PIE - .392699 ; //22.5 degrees;
	 
	/**
	 * 43.6 degrees is the horizontal field of view for a wiimote camera
	 * 33.4 degrees is the vertical field of view for a wiimote camera
	 * The units below are in radians
	 */
	private static final double hfov = .761;
	private static final double vfov = .5829; 
	
	/**
	 * The center of projection. The total viewing cooridnates for a WiiRemote
	 * are 0-1024 for the domain and 0-768 for the range
	 * Halfing those values is the center.
	 */
	private static final double centerofprojectionx = 512.0; //1024 total
	private static final double centerofprojectiony = 384.0; //768 total
	
	/**
	 * The default spacing between the two Wii Remotes. For our board the
	 * spacing is 609.6 millimeters. wiimote_spacing can be assigned this 
	 * value later
	 */
	private static final double DFLT_SPACING = 609.6;
	
	/**
	 * The angle between the board and the Wii Remote when it is
	 * placed in the angled holder
	 */
	public static final double BOARD_ANGLE = Math.toRadians(22.5);

	/**
	 * The distance between the two Wii Remotes The units here define 
	 * the units of the final result.
	 */
	private double wiimote_spacing;
		
	/**
	 * Listenerlist which gets added a DuelMoteCameraListener later
	 */
	private EventListenerList listenerList = new EventListenerList();
	
	/**
	 * Gesturehandler which gets passed in. It takes care of what
	 * happens when a gesture is sensed.
	 */
	private Gestures gesturehandler;
	
	/**
	 * GesturesEnabled is set true when looking for gestures
	 * It is set false by default and set true when a gesturehandler
	 * is passed in
	 */
	private boolean gesturesEnabled = false;

	/**
	 * The two Wii Remotes used in sensing points
	 */
	private Mote left_mote, right_mote;
	
	/**
	 * Points seen by the left and right Wii Remotes respectively
	 */
	private IrPoint[] left_points  = new IrPoint[4];
	private IrPoint[] right_points = new IrPoint[4];
	
	/**
	 * Creates a DualMoteCamera object using the given length for the board.
	 * The units the distance is measured in will be the units of the output for
	 * the point.
	 * @param boardLength The distance between the wiimotes.
	 */
	public DualMoteCamera(double boardLength, double angle1)
	{
		this(boardLength, angle1, null);
	}
	
	/**
	 * Constructor 
	 * 
	 * See variable comments for details on what each one means
	 * 
	 * @param boardLength
	 * @param angle1
	 * @param gesturehandler
	 */
	public DualMoteCamera(double boardLength, double angle1, Gestures gesturehandler)
	{
		wiimote_spacing = boardLength;
		
		wiimote_angle = angle1;
		wiimote2_angle = 2*PIE - angle1;
		
		initializeIrPointArray(left_points);
		initializeIrPointArray(right_points);
		
		if(gesturehandler != null)
		{
			gesturesEnabled = true;
			this.gesturehandler = gesturehandler;
		}
	}
	
	/**
	 * Creates a DualMoteCamera object using the default length for the board.
	 * Units are in millimeters. 
	 */
	public DualMoteCamera()
	{
		this(DFLT_SPACING, 0, null);
	}
	
	/**
	 * Initialize the arrays which will contain the points read in by 
	 * each camera
	 * 
	 * @param array
	 */
	private void initializeIrPointArray(IrPoint[] array)
	{
		for(int i = 0; i < array.length; i++)
			array[i] = new IrPoint();
	}
	
	/**
	 * Connects the wii remote and sets the lights. Each mote will have 2 
	 * LEDs lit. Both motes should have the LEDs on the outside. If the 
	 * motes are giving negative Z values, the positions need to be switched.
	 * @return If the motes were successfully connected. 
	 */
	public boolean setUpMotes()
	{	
		//Connect to wii remotes
		List<Mote> motes = MultiMoteFinder.getMotes(2); 

		 //Set up the wii remote cameras
	    if(motes != null)
	    {
	    	if(motes.size() == 2)
	    	{
	    		//retrieves the points seen by the Wii Remotes
		    	left_mote  = motes.get(0);
		    	right_mote = motes.get(1);
		    	
		    	/* Each listener updates the appropriate array of points and
		    	 * tells the dual camera the points have been updated*/
				IrCameraListener left_listen = new IrCameraListener() {
					
					public void irImageChanged(IrCameraEvent evt)
					{
						left_points[0] = evt.getIrPoint(0);
						left_points[1] = evt.getIrPoint(1);
						fireDualCameraEvent();
					}
				};
		    	
		    	initializeMote(left_mote,  left_listen);
				
		    	IrCameraListener right_listen = new IrCameraListener() {
				
		    		public void irImageChanged(IrCameraEvent evt)
					{
		    			right_points[0] = evt.getIrPoint(0);
		    			right_points[1] = evt.getIrPoint(1);
						fireDualCameraEvent();
					}
				};
		    	
		    	initializeMote(right_mote, right_listen);
		    	
		    	left_mote.setPlayerLeds(new boolean[]{true,true,false,false});
		    	right_mote.setPlayerLeds(new boolean[]{false,false,true,true});
		    	return true;
		    }
	    	
			for(Mote m: motes)
				m.disconnect();
				return false;
	    }
	    return false;
	}
	
	
	/**
	 * Enables the IR camera and sets the wiimote to report data.
	 * @param m The mote to initialize.
	 * @param l The listener to add to the mote.
	 */
	private void initializeMote(Mote m, IrCameraListener l)
	{
    	//adds the listener to the remote 
		m.addIrCameraListener(l);
		m.enableIrCamera(IrCameraMode.EXTENDED, IrCameraSensitivity.INIO);
		
		//tells the remote to report data
		m.setReportMode(ReportModeRequest.DATA_REPORT_0x33);
	}

	/**
	 * Disconnects the Wii remotes that are connected. All other functions will
	 * return null if there are no motes connected.
	 */
	public void disconnectMotes()
	{
		if(left_mote != null)
			left_mote.disconnect();
		if(right_mote != null)
			right_mote.disconnect();
		
		right_mote = left_mote = null;
	}
	
	
	/**
	 * Returns whether there are motes connected.
	 * @return whether there are motes connected.
	 */
	public boolean motesConnected()
	{
		return (left_mote == null || right_mote == null);
	}
	
	/**
	 * Adds a the DualCameraListener
	 * 
	 * @param listener
	 */
	public void addDualCameraListener(DualCameraListener listener) 
	{
		listenerList.add(DualCameraListener.class, listener);
	}
	
	/**
	 * Removes the DualCameraListener
	 * 
	 * @param listener
	 */
	public void removeDualCameraListener(DualCameraListener listener) 
	{
		listenerList.remove(DualCameraListener.class, listener);
	}
	
	/**
	 * Begins the DualCameraListener which begins listening for points
	 * 
	 */
	protected void fireDualCameraEvent()
	{
		DualCameraListener[] listeners = listenerList.getListeners(DualCameraListener.class);
		DualCameraEvent evt = new DualCameraEvent(this);
		for (DualCameraListener l : listeners) 
		{
			l.pointChanged(evt);
		}
	}
	
	/**
	 * Returns the raw 2D point from the left camera. Returns null
	 *  if the index is out of rang or the left mote is not connected.
	 * @param pointIndex - The point you want from the camera, from 0 to 3.
	 * @return The X and Y from the camera with the Z value set to 0.
	 */
	public XYZ getLeftMotePoint(int pointIndex)
	{	
		if(pointIndex >= 0 && pointIndex < 4 && left_mote != null)
			return new XYZ(left_points[pointIndex].x , left_points[pointIndex].y, 0, left_points[pointIndex].size);
		return null;
	}
	
	/**
	 * Returns the raw 2D point from the right camera. Returns null
	 *  if the index is out of rang or the right mote is not connected.
	 * @param pointIndex - The point you want from the camera, from 0 to 3.
	 * @return The X and Y from the camera with the Z value set to 0.
	 */
	public XYZ getRightMotePoint(int pointIndex)
	{	
		if(pointIndex >= 0 && pointIndex < 4 && right_mote != null)
			return new XYZ(right_points[pointIndex].x , right_points[pointIndex].y,0, right_points[pointIndex].size);
		return null;
	}
	
	/**
	 * Returns the actual points in space from the points the camera read. 
	 * It uses triangulation and figures out which points match up with each other. 
	 * This method works in all situations except for when the points are near the z axis
	 *  To clarify if you draw a line between the two points and that line points into a circle 
	 *  between the two wiimotes. The circle has diameter of the wiimotes. The reason for this is
	 *  that the left most point no longer matches up with the left most point on both cameras.
	 *  There are also other situations where the points can be calculated wrong such as when the
	 *  Wii Remotes are not positioned correctly. Nearly all of the problems stem from having to 
	 *  differentiate between two or more points being read by the Wii Remotes. 
	 *  Use XYZPointWindow to get a feel for when it works
	 *  and when it doesn't.   
	 * 
	 * @return returnthis is an array of size 2 with the 1rst point read by the left camera is in index 0
	 */
	public synchronized XYZ[] getRealPoints()
	{
		/**
		 * This is the array to be returned.
		 * index 0 is the point that lines up with the first point read in by the left Wii Remote
		 * index 1 is the point that lines up with the second point read in the the left Wii Remote
		 * The points are the actual points in space with units equal to the units of wiimote_spacing
		 */
		XYZ[] returnthis = new XYZ[2];
		
		/**
		 * assigns the points sequentially so they can be tested
		 * They could be the default of x: 1023 y: 1023
		 */
		XYZ left   = getLeftMotePoint(0);
		XYZ right  = getRightMotePoint(0);
		XYZ left2  = getLeftMotePoint(1);
		XYZ right2 = getRightMotePoint(1);
		
		//reset points not found counter
		points_not_found = 0;
		
		//Test to see if all points exist. If points are missing assume they still match up the same as before
		//As far as we can tell, if one out of four points is missing, it is impossible to tell which of the remaining two
		//that final point should be matched up to if they are in the same Y plane.
		if(left.x == 1023 || left2.x == 1023 || right.x == 1023 || right2.x == 1023)
		{
			//When less then 4 points is scene it is possible the points are wrong
			couldbewrong = true;
			
			//assigns the points based on what they were previously
			if(lastright != 0)
			{
				XYZ temp = right;
				right = right2;
				right2 = temp;
			}
			
			//if the first point has a null value (1023 is the null value returned by a Wii Remote)
			//then return null for that point
			if(left.x == 1023 || right.x == 1023)
			{
				if(left.x == 1023 && right.x == 1023)
				{
					points_not_found++;
				}
				points_not_found++;
				returnthis[0] = null;
				latestleftpoint = new XYZ();
			}
			else //assign the point like normal
			{
				returnthis[0] = getRealCoordinates(left, right);
				latestleftpoint = returnthis[0];
			}
			
			//same thing as before only to the second point now
			if(left2.x == 1023 || right2.x == 1023)
			{
				if(left2.x == 1023 && right2.x == 1023)
				{
					points_not_found++;
				}
				points_not_found++;
				returnthis[1] = null;
				latestrightpoint = new XYZ();
			}
			else
			{
				returnthis[1] = getRealCoordinates(left2, right2);
				latestrightpoint = returnthis[1];
			}
			
			/**
			 * This if statement defaults if only two points are found
			 * assume they match up with each other. It creates errors at the 
			 * cameras sensing boundaries, but it fixes errors when the points are
			 * close together
			 */
			if(points_not_found == 2)
			{
				if(returnthis[0] == null && returnthis[1] == null)
				{
					if(left.x != 1023)
					{
						if(right.x != 1023)
						{
							returnthis[0] = getRealCoordinates(left, right);
						}
						else if(right2.x != 1023)
						{
							returnthis[0] = getRealCoordinates(left, right2);
						}
					}
					else if( left2.x != 1023)
					{
						if(right.x != 1023)
						{
							returnthis[1] = getRealCoordinates(left2,right);
						}
						else if(right2.x != 1023)
						{
							returnthis[1] = getRealCoordinates(left2, right2);
						}
					}
				}
			}
		    // return returnthis;
		}
		else //both cameras see 2 points
		{
			//There are two possibilities. The first point on the left camera, matches with the first point on the right camera
			//or the first point on the left camera mathes with the second point on the right camera. It looks like this
			//  Camera1 Point1 Point2
			//  Camera2 Point3 Point4
			//  Point1 matches with either Point3 or Point4
			XYZ possibility1 = getRealCoordinates(left, right);
			XYZ possibility2 = getRealCoordinates(left, right2);		
			
			
			// When the points are in the same Y plane (Horizontal)
			// Note: adding this if statement effects the error. Hypothesis is it makes the cameras become more out of synch
			double y1 = left.getY();
			double y2 = left2.getY();
			//System.out.println("Y1: "+y1 + "  Y2: " +y2);
			if( Math.abs(y1 - y2) < 30    )
			{
				int choseright = 0;
				double lx1 = left.getX();
				double lx2 = left2.getX();
				double rx1 = right.getX();
				double rx2 = right2.getX();
				//When the points are not hovering near the Z axis line. AKA almost same Y and X.
				//Then the left most point the left camera sees, matches up with the left most point
				//the right camera sees
				if(lx1 < lx2)
				{
					if(rx1 < rx2)
					{
						returnthis[0] = getRealCoordinates(left, right);
						returnthis[1] = getRealCoordinates(left2, right2);
					}
					else
					{
						returnthis[0] = getRealCoordinates(left, right2);
						returnthis[1] = getRealCoordinates(left2, right);
						choseright = 1;
					}
				}
				else
				{
					if(rx1 < rx2)
					{
						returnthis[1] = getRealCoordinates(left2, right);
						returnthis[0] = getRealCoordinates(left, right2);
						choseright = 1;
					}
					else
					{
						returnthis[1] = getRealCoordinates(left2, right2);
						returnthis[0] = getRealCoordinates(left, right);
						choseright = 0;
					}
				}
				
				//This checks for when the points are near the z axis line. This means the points are flipped.
				//The left most point no longer matches up between the two cameras.
				//This code is not quite working yet. It works sometimes but can get messed up when losing track of points 
				//which happens a lot when the points are near the z axis
				//A problem with this code is that it relies on previous points, so if they are wrong this will constantly flip it 
				// continually making it wrong
				//Warning: Basing current points off previous points has inherent problems.
				lastright = choseright;
				lastxdis = Math.abs(returnthis[0].getX() - returnthis[1].getX());
				boolean flipped = false;
				if(!couldbewrong && latestleftpoint != null && latestleftpoint.getZ() != 0)
				{
					flipped = true;
					if(Math.abs(latestleftpoint.getX() - returnthis[0].getX()) > 25 ||
					   Math.abs(latestleftpoint.getY() - returnthis[0].getY()) > 25 ||
					   Math.abs(latestleftpoint.getZ() - returnthis[0].getZ()) > 25  )
					{
						
						if(choseright == 0)
						{
							lastright = 1;
							returnthis[0] = getRealCoordinates(left, right2);
							returnthis[1] = getRealCoordinates(left2, right);
						}
						else
						{
							lastright = 0;
							returnthis[0] = getRealCoordinates(left, right);
							returnthis[1] = getRealCoordinates(left2, right2);
						}
					}	
				}
				if(!couldbewrong && latestrightpoint != null && latestrightpoint.getZ() != 0 && !flipped)
				{
					flipped = true;
					if(Math.abs(latestrightpoint.getX() - returnthis[1].getX()) > 25 ||
					   Math.abs(latestrightpoint.getY() - returnthis[1].getY()) > 25 ||
					   Math.abs(latestrightpoint.getZ() - returnthis[1].getZ()) > 25 )
					{
						if(choseright == 0)
						{
							lastright = 1;
							returnthis[0] = getRealCoordinates(left, right2);
							returnthis[1] = getRealCoordinates(left2, right);
						}
						else
						{
							lastright = 0;
							returnthis[0] = getRealCoordinates(left, right);
							returnthis[1] = getRealCoordinates(left2, right2);
						}
					}
					
				}
				//return returnthis;
			}
			
			//This is for when it will always be correct and you can compare the errors.
			//If the points are not in almost the same Y axis then the errors will be large for the wrong point
			//We tested this using a real setup involving threads. 
			else if(possibility1.getError() < possibility2.getError())
			{
				couldbewrong = false;
				returnthis[0] = possibility1;
				returnthis[1] = getRealCoordinates(left2, right2);
				lastleft  = 0;
				lastright = 0;
				lastxdis = Math.abs(returnthis[0].getX() - returnthis[1].getX());
				//return returnthis;
			}
			else
			{
				couldbewrong = false;
				returnthis[0] = possibility2;
				returnthis[1] = getRealCoordinates(left2, right);
				lastleft  = 0;
				lastright = 1;
				lastxdis = Math.abs(returnthis[0].getX() - returnthis[1].getX());
				//return returnthis;
			}	
		}
		
		last_left_point  = latestleftpoint;
		last_right_point = latestrightpoint;
		latestleftpoint  = returnthis[0];//latest_left_point;
		latestrightpoint = returnthis[1];//latest_right_point;
		if(points_not_found == 0)
		{
			last_distance_between_points = latest_distance_between_points;
			latest_distance_between_points = 	Math.sqrt(	Math.pow( latestleftpoint.getX()-latestrightpoint.getX()  , 2)    +
												Math.pow( latestleftpoint.getY()-latestrightpoint.getY()  , 2)	  +
												Math.pow( latestleftpoint.getZ()-latestrightpoint.getZ()  , 2)    );
		}
		//	latest_left_point  = returnthis[0];
		//	latest_right_point = returnthis[1];
		
		if(gesturesEnabled)
			gestureLook();
			// Using separate class to detect gestures
			//gListener.look(latest_distance_between_points, (4 - points_not_found));
		
		return returnthis;
	}
	
	/**
	 * Uses the input from both wiimotes to find a point in 3D space.
	 * This method triangulates the point and therefore does not account
	 * for lens distortion from the wiimote cameras. 
	 * 
	 * @author Jason Creighton
	 * @param left The coordinates from the left wiimote. 
	 * @param right The coordinates from the right wiimote.
	 * @return The 3D coordinates of the point seen by both cameras.
	 */
	private XYZ getRealCoordinates(XYZ left, XYZ right)
	{
		// Step1 Translate the points passed in to be at 0,0 in relation to the middle of the camera viewplane
		double xleftshift  = left.getX()  - centerofprojectionx;
		double yleftshift  = left.getY()  - centerofprojectiony;
		double xrightshift = right.getX() - centerofprojectionx;
		double yrightshift = right.getY() - centerofprojectiony;
		
		// Step2 Figure out the angle from that middle point in the view plane to the point passed in
		double xleftangle = (xleftshift/512)*(hfov/2);
		double yleftangle = (yleftshift/384)*(vfov/2);
		double xrightangle = (xrightshift/512)*(hfov/2);
		double yrightangle = (yrightshift/384)*(vfov/2); 
		
		// Step3 Normalize those points.
		double realxleftnorm  = Math.tan( xleftangle );
		double realyleftnorm  = Math.tan( yleftangle );
		double realxrightnorm = Math.tan( xrightangle ) + wiimote_spacing; 
		double realyrightnorm = Math.tan( yrightangle );
		double realzleftnorm  = 1;
		double realzrightnorm = 1;

		
		// Rotate the first point
		double rFirstX = (realxleftnorm * Math.cos(wiimote_angle) ) + ( 1 * Math.sin( wiimote_angle) );
		double rFirstY = realyleftnorm;
		double rFirstZ = ( (realxleftnorm) * Math.sin( wiimote_angle ) * -1.0) + ( 1 * Math.cos( wiimote_angle) );
		
		
		// Rotate the second point
		double rSecondX = ( (realxrightnorm - wiimote_spacing) * Math.cos(wiimote2_angle) ) + ( 1 * Math.sin(wiimote2_angle) );
		double rSecondY = realyrightnorm;
		double rSecondZ = ( ((realxrightnorm - wiimote_spacing) * Math.sin(wiimote2_angle) * -1.0) ) + ( 1 * Math.cos(wiimote2_angle) );

		// Move the second point back the distance of the wiimote_spacing
		rSecondX = rSecondX + wiimote_spacing;
		
		// Step4 Using the 4 points call the ray collision method
		XYZ lefto  = new XYZ(0,0,0);
		XYZ leftd  = new XYZ(rFirstX, rFirstY, rFirstZ);
		XYZ righto = new XYZ(wiimote_spacing, 0, 0);
		XYZ rightd = new XYZ(rSecondX, rSecondY, rSecondZ);
		
		XYZ realPoint = Find3DPoint.raysIntersectionPoint(lefto, leftd, righto, rightd );
		// Step5 Return default points for if the XYZ gets passed back as null. 
		// This happens when the Wii Remotes are not passing in points or the rays are parallel
		if(realPoint == null)
		{
			System.out.println("raysIntersectionPoint returned null");
			return new XYZ();
		}
		
		return realPoint;
	}
	

	//Gesture variables 
	private XYZ prevLeftPoint = null;
	private XYZ prevRightPoint = null;
	private int swipe[] = new int[]{0,0,0};
	private XYZ swipeOrigin = new XYZ();
	private XYZ current = null;
	
	private int swipeDetectThreshold_x = 8;
	private int swipeDetectThreshold_y = 8;
	private int swipeDetectThreshold_z = 8;
	private int swipeSensitivity = 2;
	private int pinchThreshold = 70;
	
	/**
	 * gesture look will look for different pre-defined gestures 
	 * such as the "pinch" and "swipe" gesture.
	 */
	private synchronized void gestureLook()
	{
		/*
		 * Look for un pinch
		 * pinched gets set as true above if pinch was just found but more than two points should have been found 
		 */
		if(pinched == true)
		{
			if(points_not_found == 0)
			{
				pinched = false;
				gesturehandler.unPinch(latestleftpoint); //This is the location of the pinch
			}
		}
		/*
		 * Look for pinch
		 */
		if(pinched != true)
		{	
			if(points_not_found == 2)
			{
				if(last_distance_between_points < pinchThreshold)
				{			
					pinched = true;
					if(latestleftpoint == null)
					{
						gesturehandler.pinch(latestrightpoint);
					}
					else
					{
						gesturehandler.pinch(latestleftpoint);
					}
				}

			}
		}
		
		//Holds the difference between the current point and the prev point
		XYZ difference = null;
		
		//Find the point to used for the swipe 
		//either the average point or whichever is in view
		if(prevLeftPoint != null && latestleftpoint != null)
		{
			XYZ prev = prevLeftPoint.average(prevRightPoint);
			current = latestleftpoint.average(latestrightpoint);
			difference = prev.subtract(current);
		}
		else if(prevRightPoint != null && latestrightpoint != null)
		{
			current = latestrightpoint;
			difference = prevRightPoint.subtract(latestrightpoint);
		}
		
		//Check that a point can be seen
		if(difference != null)
		{	
			//Look for a swipe in each dimension
			if(isSwiped(difference.x, XYZ.X, swipeDetectThreshold_x))	
				if(difference.x > 0)
					gesturehandler.swipeInX(swipeOrigin, 1);
				else
					gesturehandler.swipeInX(swipeOrigin, -1);
			
			if(isSwiped(difference.y, XYZ.Y, swipeDetectThreshold_y))	       
				if(difference.y > 0)
					gesturehandler.swipeInY(swipeOrigin, 1);
				else
					gesturehandler.swipeInY(swipeOrigin, -1);
			
			if(isSwiped(difference.z, XYZ.Z, swipeDetectThreshold_z))	
				if(difference.z > 0)
					gesturehandler.swipeInZ(swipeOrigin, 1);
				else
					gesturehandler.swipeInZ(swipeOrigin, -1);
		}

		prevLeftPoint = latestleftpoint;
		prevRightPoint = latestrightpoint;
	}
	
	/*Algorithm to check for a swipe in a given axis*/ 
	private boolean isSwiped(double difference, int d, int threshold)
	{
		if(Math.abs(difference) > swipeSensitivity)
		{
			if(swipe[d] == 0)
			{
				swipeOrigin = current;
			}
			
			if(Math.abs(swipe[d]) > threshold)
			{
				swipe = new int[]{0,0,0};
				return true;
			}
			else if(difference > 0)
			{
				if(swipe[d] < 0)
					swipe[d] = 0;
				swipe[d]++;
			}
			else if(difference < 0)
			{
				if(swipe[d] > 0)
					swipe[d] = 0;
				swipe[d]--;
			}
		}
		return false;
	}
	
	/* Set Methods */
	
	/**
	 * Sets the duration of movement in the X direction to trigger a swipe. If 
	 * the swipeSensitivity is set correctly this should not be effected by the
	 * units used. Default value is 8 and must be positive if changed. 
	 * @param x_thresh The duration of movement in the X direction to trigger a swipe.
	 */
	public void setXthreshold(int x_thresh)
	{
		if(x_thresh > 0)
			swipeDetectThreshold_x = x_thresh;
	}

	/**
	 * Sets the duration of movement in the Y direction to trigger a swipe. If 
	 * the swipeSensitivity is set correctly this should not be effected by the
	 * units used. Default value is 8 and must be positive if changed.  
	 * @param y_thresh The duration of movement in the Y direction to trigger a swipe.
	 */
	public void setYthreshold(int y_thresh)
	{
		if(y_thresh > 0)
			swipeDetectThreshold_y = y_thresh;
	}
	
	/**
	 * Sets the duration of movement in the Z direction to trigger a swipe. If 
	 * the swipeSensitivity is set correctly this should not be effected by the
	 * units used. Default value is 8 and must be positive if changed.
	 * @param z_thresh The duration of movement in the Z direction to trigger a swipe.
	 */
	public void setZthreshold(int z_thresh)
	{
		if(z_thresh > 0)
			swipeDetectThreshold_z = z_thresh;
	}
	
	/**
	 * Sets the sensitivity of the swipe gestures i.e. the speed the point must
	 * be moving to activate a swipe. A higher value means the point must move 
	 * faster. Default value is 2 mm. If different units are used the sensitivity 
	 * must be set for swipes to work correctly. Sensitivity must be positive.
	 * 
	 * @param swipeSensitivity How fast the point/points must be moved to trigger a swipe. 
	 */
	public void setSwipeSensitivity(int swipeSensitivity)
	{
		this.swipeSensitivity = swipeSensitivity;
	}

	/**
	 * Sets the distance between points that indicates a pinch is about to 
	 * be triggered. Default value is 70 mm. If different units are used the 
	 * pinch threshold must be set for pinching to work correctly. Threshold
	 *  must be positive.
	 * @param pinchThreshold How close the points must be to trigger a pinch. 
	 */
	public void setPinchThreshold(int pinchThreshold)
	{
		if(pinchThreshold > 0)
			this.pinchThreshold = pinchThreshold;
	}

	/**
	 * These two methods are used to return static points so that the Wii remotes do not have to be connected.
	 * (Just for testing)
	 */
	public XYZ staticMote1Point(int pointIndex)
	{
		if( pointIndex == 0 )
		{
			return new XYZ( 327.0, 497.0, 0.0 );
		}
		return new XYZ( 138.0, 513.0, 0.0 );
	}
	
	public XYZ staticMote2Point(int pointIndex)
	{
		if( pointIndex == 0 )
		{
			return new XYZ( 882.0, 441.0, 0.0 );
		}
		return new XYZ( 692.0, 460.0, 0.0 );
	}
		
}
