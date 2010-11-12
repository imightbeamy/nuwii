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
* File:    MultiMoteTest.java
*/
package util;

import java.util.ArrayList;
import java.util.List;

import motej.Mote;
import motej.MoteFinder;
import motej.MoteFinderListener;
import motej.event.MoteDisconnectedEvent;
import motej.event.MoteDisconnectedListener;

/**
 * @author Amy Ciavolino
 */
public class MultiMoteFinder implements MoteFinderListener
{

	private List<Mote> motes;
	private int num_of_motes;
	private Object lock = new Object();
	private MoteFinder finder;
	
	/**
	 * Returns a list of Wii Remotes found and connected to via Bluetooth. This
	 * works best hen the red "hard sync" button is pressed on the Wii Remotes.
	 * Continue to press buttons on the remotes to keep the the LEDs flashing
	 * until the remotes connect.
	 * 
	 * You can write code like this to add a listener to each mote:
	 * <blockquote><pre>
	 * 		for(Mote m: motes)
	 * 		{
	 * 			m.addIrCameraListener(new IrCameraListener());
	 * 			m.enableIrCamera();
	 * 			m.setReportMode(ReportModeRequest.DATA_REPORT_0x36);	
	 * 			//Do what ever you want to each Mote
	 * 		}
	 * </pre></blockquote>
	 * <p>
	 * @param motesToFind The number of Wii Remotes to connect to.
	 * @return a list of the Wii Remotes that were connected to. 
	 */
	public static List<Mote> getMotes(int motesToFind)
	{
		MultiMoteFinder finder = new MultiMoteFinder();
		
		if(motesToFind  > 0)
			finder.num_of_motes = motesToFind;
		else
			return null;
		
		boolean done = false;
		while(!done)
		{
			try
			{
				finder.findMotes();
				done = true;
			} 
			catch (InterruptedException e) 
			{
				System.out.print("Searching intrrupted. Searching again...");
			}
			catch (Exception e)
			{
				System.out.println("There was an error. Bluetooth maybe turned off.");
				return null;
			}
		}
		return finder.motes;
	}

	
	private void findMotes() throws InterruptedException
	{
		motes = new ArrayList<Mote>();
		finder = MoteFinder.getMoteFinder();
		finder.addMoteFinderListener(this);
		System.out.println("Starting discovery..");
		
		while(motes.size() < num_of_motes)
		{
			finder.startDiscovery();
			
			System.out.println("Looking for " + num_of_motes + " wiimotes..");
			System.out.println("Please wait...");
			
			synchronized(lock) {
				lock.wait();
			}
			
			//wait for a bit to let motes finish connecting if they need to 
			Thread.sleep(2000l);
			
			finder.stopDiscovery();
		}
		
		System.out.println("Motes found!");
	}
	
	/*
	 * Implementation of method in MoteFinderListener. 
	 * This is runs when a Wii Remote is found.
	 */
	public void moteFound(final Mote mote) 
	{
		System.out.println("Found mote: " + mote.getBluetoothAddress());
		mote.rumble(2000l);
		
		//as the motes are found the LED lights up
		mote.setPlayerLeds(new boolean[] { true, false, false, true });
		
		//I don't think this helps anything...
		mote.addMoteDisconnectedListener(new MoteDisconnectedListener<Mote>(){
			public void moteDisconnected(MoteDisconnectedEvent<Mote> evt) {
				motes.remove(mote);
			}
		});
		
		motes.add(mote);
		
		if(motes.size() >= num_of_motes)
			synchronized(lock) {
				lock.notifyAll();
			}
	}
}
