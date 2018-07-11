package uk.ac.nott.cs.g53dia.demo;
import uk.ac.nott.cs.g53dia.library.*;
import uk.ac.nott.cs.g53dia.demo.Point;
import java.util.HashMap;
import java.util.Random;


/**
 * A simple example Tanker
 * 
 * @author Julian Zappala
 */
/*
 * Copyright (c) 2011 Julian Zappala
 * 
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class DemoTanker extends Tanker 
{
	//HashMaps based on data
	HashMap<FuelPump, Point> refillStations;
	HashMap<Well, Point> wells;
	HashMap<Station, Point> stations;
	HashMap<Task, Point> jobs;

	//Initialize cells
	Cell[][] cellInside;
	
	//Points of Cells
	Point tankerCords;
	Point closestStation;
	Point secondStation;
	int moveTowards = 8;
	
	//These strings are used for Switch in Logic function
	String refuelAction = "toFuelPump";
	String stationAction = "toStation";
	String wellAction = "toWell";
	
	public DemoTanker()
	{
	  this(new Random());
	  
	  //Initialize HashMaps
	  refillStations  = new HashMap<FuelPump, Point>();
      wells  = new HashMap<Well, Point>();
      stations  = new HashMap<Station, Point>();
      jobs   = new HashMap<Task, Point>();
      
      //Initial coordinates of tanker is 0,0 
      tankerCords = new Point(0,0);

	}

    public DemoTanker(Random r) 
    {
	this.r = r;
    }
    
	//Gets environment data and stores keys into lists
    public void environmentData()
    {
    	int row, column, x_cord, y_cord;
		
    	//Map is a square meaning row/column are equal to cells
    	for(row=0; row<cellInside.length; row++)
    	{
    		for(column=0; column<cellInside.length; column++)
    		{
    			
    			//Put Row/Column into Cell
    			Cell currentCell = cellInside[row][column];
    			
    			x_cord = row - Tanker.VIEW_RANGE;
    			y_cord = Tanker.VIEW_RANGE - column;
    			Point point = Logic.coordinatesOnMap(x_cord, y_cord, tankerCords);
    			
    			//Check if point is not null
    			if(point != null)
    			{
	    			//Check the type of the cell in [row/column] and put the position into list
	    			if(currentCell instanceof FuelPump)
	    			{
	    				FuelPump refillStation = (FuelPump) currentCell;
	    				
	    				//Check if key already exists
	    				if(refillStations.containsKey(point) == false)
	    				{
	    					//If not, put into HashMap
	    					refillStations.put(refillStation, point);
	    				}

	    			}
	    			
	    			else if(currentCell instanceof Well)
	    			{
	    				Well well = (Well) currentCell;
	    				
	    				if(wells.containsKey(point) == false)
	    				{
	    					wells.put(well, point);
	    				}
	    			}
	    			
	    			else if(currentCell instanceof Station)
	    			{
	    				Station station = (Station) currentCell;
	    			
	    				if(stations.containsKey(point) == false)
	    				{
	    					stations.put(station, point);
	    					
	    					//Get task from station
	    					Task job = station.getTask();
	    					
	    					//Check if job is not null + not completed
		    				if(job != null && job.isComplete() == false)
							{
		    					jobs.put(job, point);
							}
	    				}
	    				
	    			}
    			}
    		}
    	}
    }
    
    public Action performAction()
    {
    	Action action = null;
    	
		Well dropOffPoint = Logic.dropPoint(wells, tankerCords);
    	Point wellPosition = wells.get(dropOffPoint);
    	
    	FuelPump closestRefillStation = Logic.closestRefill(refillStations, tankerCords);
		Point refillPosition = refillStations.get(closestRefillStation);
    	
    	//There are no jobs in the HashMap
    	if(jobs.isEmpty() == true)
    	{

			int distance = this.getFuelLevel() - Logic.distanceComputation(tankerCords, refillPosition);

			if(distance <= 2)
			{
				action = Logic.retrieveActions(this, refillPosition, refuelAction);
			}
			
			//REIKIA IDIEGTI
			else
			{
				moveTowards=r.nextInt(1);
				action = new MoveAction(moveTowards);
				Logic.tankerMovementUpdate( tankerCords, moveTowards ); 
			}	
			
			
			
    	}
    	
    	//We have a job in the HashMap
    	else
    	{
    		//Check if we got any waste
    		if(this.getWasteLevel() == 0)
    		{	    			
    			closestStation = JobSelect.findClosestStation(this);

    			//If Closest Station is Null -> search for more tasks
    			if(closestStation == null)
    			{
    				moveTowards=r.nextInt(1);
    				action = new MoveAction(moveTowards);
    				Logic.tankerMovementUpdate( tankerCords, moveTowards );
    			}
    			
    			else
    			{
    				//Cost to go from Tanker -> Closest station -> FuelPump
        			int halfPathCost = this.getFuelLevel() - Logic.halfPathCost(this, closestStation);
        			
        			//Check if we can go from Tanker->Closest Station->FuelPump
    				if(halfPathCost > 2)
    				{
    					action = Logic.retrieveActions(this, closestStation, stationAction);
    				}
    					
    				//If not, then move Tanker->FuelPump to do a refill
    				else
    				{
    					action = Logic.retrieveActions(this, refillPosition, refuelAction);
    					closestStation = null;
        			}

    			}
    			
    		}
    		
    		//Tanker has collected some waste
    		else
    		{
    				//Path cost from Tanker->Well->FuelPump
            		int halfPathCost = this.getFuelLevel() - Logic.halfPathCost(this, wellPosition);
            		
            		//Retrieve second station with a task from the map
            		secondStation = JobSelect.findClosestStation(this);
            		
            		//This integer is used to calculate distance from Tanker->Second station with a task
            		int costToSecondStation = Integer.MIN_VALUE;
            		
            		//Check if Second Station has a task and we can carry more waste, calculate cost Tanker->Second Station->FuelPump
            		if( secondStation != null && (this.getWasteLevel() != this.MAX_WASTE) )
            		{
            			costToSecondStation = this.getFuelLevel() - Logic.halfPathCost(this, secondStation);
            		}
            		
            		//Check if tanker will have enough fuel Tanker->Second Station->FuelPump will have enough fuel to pick up task &  go to FuelPump
            		if( (costToSecondStation != Integer.MAX_VALUE) && (this.getWasteLevel() != this.MAX_WASTE) && (costToSecondStation > 2) )
            		{
            				//If he does then move to the second station
             				action = Logic.retrieveActions(this, secondStation, stationAction);
            		}
            		//If he cannot pick up another task then move either to refuel station or well
            		else
            		{
            			//If we haven't got enough fuel for Tanker->Well->FuelPump then move Tanker->FuelPump
            			if(halfPathCost <= 2)
                        {
                			action = Logic.retrieveActions(this, refillPosition, refuelAction); 
                        }
            			//Else move Tanker->Well
                       	else
                       	{
                       		action = Logic.retrieveActions(this, wellPosition, wellAction);
                       	}
            		}	
    		}
    	}
    	
    	//Return what the tanker should do
    	return action;
    }
 
    public Action senseAndAct(Cell[][] view, long timestep) 
    {
        
    	cellInside = view;
    	Action action = null;
    	
    	//Scans the environment
    	environmentData();
    	
    	//Retrieve what action tanker should do
    	action = performAction();	
    	
    	//There is no action -> something is wrong in the code. This should never be true
    	if(action == null)
    	{
    		System.err.println("Action assigned to null");
    	}

    		return action;
    }
}
