import java.util.ArrayList;
import java.util.Stack;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Arrays;
import java.text.DecimalFormat;

public class Driver
{
  public static void main(String[] args)
  {
    if(args.length != 1)
    {
      System.out.println("Incorrect arguments. Please run by \"java " +
                         "Driver <numPoints>\"");
      System.exit(-1);
    }

    int numPoints = Integer.parseInt(args[0]);

    int iterations = 0;

    Point[] p = new Point[0];
    
    PointSet ps = PointSet.createRandomPointSet(numPoints);
    
    Stack<Point> hull;
    ArrayList<Point> points;

    while(ps.size() != 0)
    {
      iterations++;

      hull = PointSet.convexHull(ps);
      
      points = new ArrayList<Point>(Arrays.asList(ps.getPointSet()));

      Iterator<Point> it = hull.iterator();

      while(it.hasNext())
        points.remove(it.next());

      ps = new PointSet(points.toArray(p));
    }

    System.out.println(iterations);
  }
}


class PointSet
{
  private Point[] pointset;
   
  public PointSet(int n)
  {
    pointset = new Point[n];
  }

  public PointSet(Point[] p)
  {
    pointset = p;
  }

  public int size()
  {
    return pointset.length;
  }

  public Point[] getPointSet()
  {
    return pointset;
  }

  public String toString()
  {
    int i = 0;
    String s = "[";
    for(i = 0; i < pointset.length-1; i++)
    {
      s += pointset[i];
      s = s.substring(0, s.length()-1);
      s += ", ";
    }

    s += pointset[i];
    s = s.substring(0, s.length()-1);
    s += "]\n";
    
    return s;
  }

  public static PointSet createRandomPointSet(int n)
  {
    PointSet ps = new PointSet(n);
    
    double r,t;

    for(int i = 0; i < n; i++)
    {
      t = 2 * Math.PI * Math.random();
      r = Math.random() + Math.random();

      r = r > 1 ? (2-r) : r;

      ps.pointset[i] = new Point(r * Math.cos(t), r * Math.sin(t));
    }

    return ps;
  }

  public static Stack<Point> convexHull(PointSet ps)
  {
    Point temp = ps.pointset[0];
    int tempIndex = 0;

    ArrayList<Point> hull = new ArrayList<Point>();
    Stack<Point> stack = new Stack<Point>();
    Stack<Integer> index = new Stack<Integer>();

    for(int i = 0; i < ps.pointset.length; i++)
    {
      if(temp.getY() > ps.pointset[i].getY() || 
        (temp.getY() == ps.pointset[i].getY() &&
         temp.getX() > ps.pointset[i].getX()))
      {
        temp = ps.pointset[i];
        tempIndex = i;
      }
    }


    temp = ps.pointset[0];
    ps.pointset[0] = ps.pointset[tempIndex];
    ps.pointset[tempIndex] = temp;

    stack.push(ps.pointset[0]);
    index.push(0);
    
    if(ps.pointset.length == 1)
      return stack;
    
    if(ps.pointset.length == 2)
    {
      stack.push(ps.pointset[1]);
      return stack;
    }

    for(int i = 1; i < ps.pointset.length; i++)
    {
      ps.pointset[i].setAngleWith(ps.pointset[0]);
    }
    
    Arrays.sort(ps.pointset, 1, ps.pointset.length, Point.compareAngle);

    tempIndex = 0;

    for(int i = 1; i < ps.pointset.length-1; i++)
    {
      if(ps.pointset[i].turn(ps.pointset[index.peek()], ps.pointset[i+1]) > 0)
      {
        stack.push(ps.pointset[i]);
        index.push(i);
      }
      else
      {
        int j = index.pop();
        while(ps.pointset[j].turn(ps.pointset[index.peek()], ps.pointset[i+1]) <= 0)
        {
          stack.pop();
          j = index.pop();
        }
        index.push(j);
      }
    }
    
    stack.push(ps.pointset[ps.pointset.length-1]);
    return stack;
  }
}


class Point implements Comparable<Point>
{
  private double x;
  private double y;
  private double angle;

  public static Comparator<Point> compareAngle = new Comparator<Point>()
  {
    public int compare(Point p1, Point p2)
    {
      return p1.compareTo(p2);
    }
  };

  public Point()
  {
    x = 0.0;
    y = 0.0;
  }

  public Point(double x, double y)
  {
    this.x = x;
    this.y = y;
  }

  public double getX()
  {
    return x;
  }

  public double getY()
  {
    return y;
  }

  public void setX(double x)
  {
    this.x = x;
  }

  public void setY(double y)
  {
    this.y = y;
  }

  public double distance(Point p)
  {
    return distance(p.x, p.y);
  }

  public double distance(double x, double y)
  {
    return Math.sqrt(Math.pow(this.x-x, 2) +
                     Math.pow(this.y-y, 2));
  }

  public double setAngleWith(Point p)
  {
    return setAngleWith(p.x, p.y);
  }

  public double setAngleWith(double x, double y)
  {
    if(x == this.x && y != this.y)
      return Math.PI/2;

    angle = Math.atan((this.y-y)/(this.x-x));
    angle = (angle < 0) ? Math.PI + angle : angle;

    return angle;
  
  }

  double turn(Point p1, Point p3)
  {
    return ((this.x-p1.x)*(p3.y-p1.y) - (this.y-p1.y)*(p3.x-p1.x));
  }

  public int compareTo(Point p)
  {
    if(this.angle < p.angle)
      return -1;
    else if(this.angle  > p.angle)
      return 1;
    else
      return 0;
  }

  public String toString()
  {
    String s = "(";
    DecimalFormat d = new DecimalFormat("#.000");
    
    s += d.format(getX()) + ", " + d.format(getY()) + ")";
    return s;
  }
}  
