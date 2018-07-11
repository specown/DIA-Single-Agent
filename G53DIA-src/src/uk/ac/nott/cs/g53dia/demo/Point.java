package uk.ac.nott.cs.g53dia.demo;


//This Class was created due to the fact that we are not allowed to edit Library
//Functions and X|Y variables were not set to public.

public class Point 
{
	public int row, column;
	
	public Point(int row, int column)
	{
		this.row = row;
		this.column = column;
	}
	
    public Object clone() 
    {
        return new Point(row, column);
    }
    
    public boolean comparePoints( Point point )
    {
        return (this.row == point.row && this.column == point.column);
    }
}
