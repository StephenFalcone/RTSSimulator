
import java.util.HashMap;
import java.util.Scanner;
import java.util.Collection;
import java.io.PrintWriter;
import java.io.File;
import java.util.ArrayList;

public class IOUtil
{

   private static HashMap<String,BusStop> readBusStopInfo(Scanner input) throws Exception
   {
      String temp;
      int x, y;
      BusStop bs = null;
      HashMap<String,BusStop> busStops = new HashMap();
      
      temp = input.next();
      if (temp.compareTo("busstops") != 0) 
         throw new Exception(temp + " when busstops keyword is expected");
      while (!input.hasNext("line"))
      {   
         temp = input.next();
         x = input.nextInt();
         y = input.nextInt();
         if (temp.charAt(0) == '[' && temp.charAt(temp.length()-1) == ']')
         {
            temp = temp.substring(1,temp.length()-1);
            bs = new TimePoint(temp, x, y);
         }   
         else if (temp.charAt(0) != '[' && temp.charAt(temp.length()-1) != ']')
            bs = new BusStop(temp, x, y);
         else throw new Exception("Brackets do not match in " + temp);                           
         busStops.put(temp, bs);
      }  
      return busStops;
   }
   
   private static ArrayList<Line> readLineInfo(Scanner input, HashMap<String, BusStop> busStops) throws Exception
   {
        String temp, lineNo;
        int period;
        Time start, last;
        ArrayList<Line> lines = new ArrayList();
        
        input.useDelimiter("[:=<>,\\s]+");
        while (input.hasNext())
        {
            temp = input.next();
            if (temp.compareTo("line") != 0)
               throw new Exception(temp + " when keyword line is expected");
            lineNo = input.next();
            temp = input.next();
            if (temp.compareTo("route") != 0)
               throw new Exception(temp + " when keyword route is expected");
            ArrayList<BusStop> route = new ArrayList();
            ArrayList<Integer> timepoints = new ArrayList();
            while (!input.hasNext("start"))
            {
                  temp = input.next();
                  if (busStops.containsKey(temp))
                     route.add(busStops.get(temp)); 
                  else
                     throw new Exception("Busstop " + temp + " not defined!");
                  timepoints.add(input.nextInt());   
            }
            temp = input.next();      
            if (temp.compareTo("start") != 0)
               throw new Exception(temp + " when keyword start is expected");
            start = new Time(input.nextInt(), input.nextInt());
            temp = input.next();
            if (temp.compareTo("last") != 0)
               throw new Exception(temp + " when keyword last is expected");
            last = new Time(input.nextInt(), input.nextInt());
            temp = input.next();
            if (temp.compareTo("period") != 0)
               throw new Exception(temp + " when keyword period is expected");
            period = input.nextInt();           
            Line line = new Line(lineNo,route.get(0), route.get(route.size() - 1), start, last, period); 
            for(int i=0; i < route.size(); i++)
               line.addNextBusStop(route.get(i), timepoints.get(i));
            lines.add(line);    
        }        
        return lines;
   }


   /** Reads the bus stop and line information from file fileName 
     * @RETURNS an ArrayList of BusStop, TimePoint, and Line objects.
     * Text file fileName is expected to be in the following format:
     *   
     * busstops=<BusstopName1,x1,y1> <BusstopName2,x2,y2> ... <BusstopNameN,xN,yN>
     * line=lineNo1
     * route=<BusstopName1_1,duration1_1> <BusstopName1_2,duration1_2> ... <BusstopName1_M,duration1_M>
     * start=hoursOfStart1:minutesOfStart1
     * last=hoursOfLast1:minutesOfLast1
     * period=somePositiveInt
     * line=lineNo2
     * route=<BusstopName2_1,duration1_1> <BusstopName2_2,duration2_2> ... <BusstopName2_M,duration2_M>
     * start=hoursOfStart2:minutesOfStart2
     * last=hoursOfLast2:minutesOfLast2
     * period=somePositiveInt
     * ...
     * line=lineNoK
     * route=<BusstopNameK_1,durationK_1> <BusstopNameK_2,durationK_2> ... <BusstopNameK_M,durationK_M>
     * start=hoursOfStartK:minutesOfStartK
     * last=hoursOfLastK:minutesOfLastK
     * period=somePositiveInt
     *      
     * Note that for TimePoint type bus tops the BusstopName will be enclosed in square brackets. 
     * As an example in route=<[Reitz],0> <BS1,0> <[Macguire],7> ...
     * Reitz and Macguire are time points wheras BS1 is a regular bus stop.
     *
     * An Exception object is thrown if file fileName's format does not match the above format. 
     */
   public static ArrayList<Object> readBusstopAndLineInfo(String fileName) throws Exception
   {
         Scanner input = null;
         File inputFile = new File(fileName);       
         input = new Scanner(inputFile);
         input.useDelimiter("[<>=,\\s]+");
         HashMap<String,BusStop> busStops = readBusStopInfo(input);
         ArrayList<Line> lines = readLineInfo(input, busStops);
         ArrayList<Object> result = new ArrayList();
         Collection<BusStop> c = busStops.values();

         for(Object o: c)
            result.add(o);     
            
         for(int i=0; i < lines.size(); i++)
            result.add(lines.get(i));
            
         return result;   
    }

   public static void main(String[] args)
   {
       // The bus stops with names BSi are meant to be regular bus stops for 
       // which time information is not displayed in the schedule.
       // The others are meant to be time points for which time information 
       // is printed in the schedule.
       // Note: (x,y) coordinates are not used in this project but will be
       // useful once we start incorporating graphics.   
       
       if (args.length != 1) 
       {
           System.out.println("Inputfilename expected as the commandline parameter");
           System.exit(1);
       }     

       try {
         ArrayList<Object> list = readBusstopAndLineInfo(args[0]);
         for(int i=0; i < list.size(); i++)
         {
            Object o = list.get(i);
            if (o instanceof Line)
            {
                ((Line)o).printRoute(System.out);
                ((Line)o).printSchedule(System.out);
            }   
         } 
           
       }
       catch(Exception e) { System.out.println(e); }  
   }

}
