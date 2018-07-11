package uk.ac.nott.cs.g53dia.demo;
import java.util.HashMap;


import uk.ac.nott.cs.g53dia.demo.Point;
import uk.ac.nott.cs.g53dia.library.*;

public class Logic 
{
	 public static Point coordinatesOnMap( int x_cord, int y_cord, Point tankerCords )
	 {
	        return new Point( tankerCords.row + x_cord, tankerCords.column + y_cord );
	 }
	 
	 
	 /*Distance computation - Max(Delta X/Y) 
	  *Where Delta X/Y - absolute value of the difference in the X & Y coordinates
	  */
	 public static int distanceComputation(Point first, Point second)
	 {
		 return Math.max(Math.abs(first.row-second.row), Math.abs(first.column-second.column));
	 }
	 
	 //Find the closest well 
	 public static Well dropPoint(HashMap<Well, Point> wells, Point coordinates)
	 {
		 int max = Integer.MAX_VALUE;
		 
		 //Assign to Null
		 Well dropOffPosition = null;
		 
		 //Scan trough HashMap
		 for(Well well:wells.keySet())
		 {
			 Point point = wells.get(well);
			
			 int distance = distanceComputation(coordinates, point);
			 
			 if(distance<max)
			 {
				 max = distance;
				 dropOffPosition = well;
			 }
		 }
		 return dropOffPosition;
	 }
	 
	 public static FuelPump closestRefill(HashMap<FuelPump, Point> refillStations, Point coordinates)
	 {
		 int max = Integer.MAX_VALUE;
		 
		 //Assign to Null
		 FuelPump closestFuelStation = null;
		 
		 //Scan trough HashMap
		 for(FuelPump fuelPump:refillStations.keySet())
		 {
			 Point point = refillStations.get(fuelPump);
			
			 int distance = distanceComputation(coordinates, point);
			 
			 if(distance<max)
			 {
				 max = distance;
				 closestFuelStation = fuelPump;
			 }
		 }
		 return closestFuelStation;
	 }
	 
	 public static int directTarget(Point tankerCords, Point target)
	    {
		 //Get X/Y to the target
	        int targetX  =target.row - tankerCords.row;
	        int targetY =  target.column - tankerCords.column;
	        return moveNavigation(targetX, targetY);
	    }
	 
	 //Moves the tanker based on the direction
	 private static int moveNavigation(int targetX, int targetY)
	    {
	        int moveTowards = 8;
	        
	        if( targetY == 0 ){
	        	if( targetX == 0 )
	        	{
		            /*Arrived at the location
		             * X && Y == 0
		             */
		            	moveTowards = 8;
		        }
	        	
	        	else if( targetX > 0 ){
	            	//East
	            	moveTowards = 2;
	            }
	        	
	            else if( targetX < 0 ){
	            	//West
	            	moveTowards = 3;
	            }
	            
	        }
	        
	        else if( targetY > 0 )
	        {
	        	if( targetX == 0 ){
	            	//North
	            	moveTowards = 0;
	            }
	        	
	        	else if( targetX > 0 ){
	            	//NorthEast
	            	moveTowards = 4;
	            }
	            
	            else if( targetX < 0 ){
	            	//NorthWest
	            	moveTowards = 5;
	            }
	        }
	        
	         
	        else if( targetY < 0 ){
	            
	            if( targetX == 0 ){
	            	//South
	            	moveTowards = 1; 
	            }
	            
	            else if( targetX > 0 ){
	            	//SouthEast
	            	moveTowards = 6;
	            }
	            
	            else if( targetX < 0 ){
	            	//SouthWest
	            	moveTowards = 7;
	            }
	        }
	        
	      /* Debug purposes only
	       * System.out.println("X:" + targetX + " Y:" + targetY);
	       */

	        return moveTowards;
	    }
	 
	 public static boolean onLocation(Point tankerCords, Point targetCords)
	 {
		 //Check if tanker arrived at location
		 if(tankerCords.comparePoints(targetCords))
		 {
			 return true;
		 }
		 return false;
	 }
	 
	 //Update tankers coordinates based on movement
	 public static void tankerMovementUpdate( Point tankerCords, int moveTowards )
	 {
	        switch( moveTowards )
	        {
	            case 0:
	            	tankerCords.column++;
	            	break;

	            case 1:
	            	tankerCords.column--;
	            	break;

	            case 2:
	            	tankerCords.row++;
	            	break;

	            case 3:
	            	tankerCords.row--;
	            	break;

	            case 4:
	            	tankerCords.column++;
	            	tankerCords.row++;
	            	break;

	            case 5:
	            	tankerCords.column++;
	            	tankerCords.row--;
	            	break;

	            case 6:
	            	tankerCords.row++;
	            	tankerCords.column--;
	            	break;

	            case 7:
	            	tankerCords.column--;
	            	tankerCords.row--;
	            	break;
	        }
	 }
	 
	 //Used to calculate path cost
	 public static int halfPathCost(DemoTanker tanker, Point stationCoordinates)
	 {
		 HashMap<FuelPump, Point> refillStations = tanker.refillStations;
		 FuelPump closestRefillStation = closestRefill(refillStations, stationCoordinates);
		 
		 //Closest FuelPump, Station -> FuelPump
		 Point refillPosition = refillStations.get(closestRefillStation);
	 
		 Point tankerCords = tanker.tankerCords;
		 
		 //Get distance from Tanker->Coordinates->Pump
		 int distance = distanceComputation(tankerCords, stationCoordinates) + distanceComputation(refillPosition, stationCoordinates);
		 
		 return distance;
	 }
	 
	 public static Action retrieveActions(DemoTanker tanker, Point coordinates, String pathChose)
	 {	

	     Action action = null;
		 Point tankerCords = tanker.tankerCords;
		 
		 //Direct target to FuelPump
		 int moveTowards = directTarget(tankerCords, coordinates);
		 action = new MoveAction(moveTowards);
		 
		 switch (pathChose)
		 {
		 	case "toStation":
		 		 if(onLocation(tankerCords, coordinates) == true)
		 		 {
						//Gets the task from the Station that the Tanker is standing on and passes it to LoadWaste
			    		Station station = (Station) tanker.getCurrentCell(tanker.cellInside);
			    		Task job = station.getTask();
			    		
			    		//Load waste from Job
			    		action = new LoadWasteAction(job);
		 		 } 
		 		 break;
		 	case "toFuelPump":
				if(Logic.onLocation(tankerCords, coordinates) == true)
				{
						action = new RefuelAction();
				}
				break;
				
		 	case "toWell":
		 		if(Logic.onLocation(tankerCords, coordinates) == true)
        		{
    			action = new DisposeWasteAction();
        		}
				break;
		 }
	
		//Update tanker coordinates
		tankerMovementUpdate( tankerCords, moveTowards );
		 
		 return action;
	 }
	 
}

