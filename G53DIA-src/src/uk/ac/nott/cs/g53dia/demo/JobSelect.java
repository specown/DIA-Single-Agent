package uk.ac.nott.cs.g53dia.demo;
import java.util.Iterator;
import java.util.HashMap;

import uk.ac.nott.cs.g53dia.demo.Point;
import uk.ac.nott.cs.g53dia.library.*;

import java.util.Map.Entry;

public class JobSelect 
{
	
	//private static Point closestStation = null;
	
	//Find the closest station to the Tanker
	public static Point findClosestStation (DemoTanker tanker)
	{	
		HashMap<Task, Point> jobs = tanker.jobs;
			
		Point closestStation = null;
		Point tankerCords = tanker.tankerCords;
		
		int i =0, minDistance = 0;
	
		//Initialize iterator
 		Iterator<Entry<Task, Point>> iterator = jobs.entrySet().iterator();
		
		//Scan through jobs looking for potential closest job
		while(iterator.hasNext())
		{
			Entry<Task, Point> keys = iterator.next();
			Task job = keys.getKey();
			Point jobPosition = jobs.get(job);
			
			//Check path cost Tanker->Station->FuelPump and ensure that we will have enough fuel with advance in case of FalibleAction
			int pathCost = tanker.MAX_FUEL - Logic.halfPathCost(tanker, jobPosition);
			
			//Check if job is not complete
			if(job.isComplete() == false && job.getWasteRemaining()>0 && pathCost > 2)
			{
				//If there is more than 1 job
				if(i != 0 && Logic.distanceComputation(tankerCords, jobPosition) < minDistance)
				{
						minDistance = Logic.distanceComputation(tankerCords, jobPosition);;
						
						//Get that job position
						closestStation = jobPosition;
				}
				//If there is only 1 job
				else if (i == 0)
				{
					minDistance = Logic.distanceComputation(tankerCords, jobPosition);
					closestStation = jobPosition;
				}
				
			}
			//If first statement is false -> remove element
			else
			{
				iterator.remove();
			}
			
			//Used to keep track that there is more than 1 job available
			i++;
		}


		return closestStation;
	}
}

