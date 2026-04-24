/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetFilterandClustering;



import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengts.util.GeoPoint;


/**
 *
 * @author Nguyen Duc DUy
 * This structure contain information related to a tweet, included
 * - OpjectID
 * - Time
 * - Coordinates
 * - Text value
 */
public class TweetDataRecord {
    public static String _id="";
    public static DateTime observation_when_time= new DateTime();
//    Location observation_where_coordinates;
    public static GeoPoint observation_where_coordinates = new GeoPoint();
    public static String observation_what_0_tweet_value= "";
    public static long observation_what_1_id_str_value = 0; 
    
    public TweetDataRecord(){
        
    }
            
    public boolean ImportFromCSV(CSVRecord inp){
        boolean result = true;
        if (inp.size() != 5) { // id, date, location, val0. val1
            System.out.println("Invalid csv data size (it shold be 5, included id, date, location, val0. val1)");
            System.out.println(inp.size() + "++++" +inp.toString());
            return false;
        }
        _id = inp.get(0);
 //     observation_when_time = Date.parse(inp.get("observation.where.coordinates"));
         DateTimeFormatter ft = DateTimeFormat.forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'"); 
        
        // Parse from date
        try { 
            observation_when_time = ft.parseDateTime(inp.get(1)); 
        } catch (Exception e) { 
          System.out.println("Unparseable using " + ft); 
          e.printStackTrace();
        }
        
        //Parse from coodinate string
        String str = inp.get(2);
        String coors = str.substring(1, str.length()-2);
        String[] splitCoors = coors.split(",");
        if (splitCoors.length!=2) {
            System.out.println("Invalid coorditions: " + coors );
            return false;
        }
//        observation_where_coordinates.setLongtitudeNLatitude(Double.parseDouble(splitCoors[0]), Double.parseDouble(splitCoors[1]));
//        System.out.println(splitCoors[0]+ "____" + splitCoors[1]);
        this.observation_where_coordinates.setLongitude(Double.parseDouble(splitCoors[0]));
        this.observation_where_coordinates.setLatitude(Double.parseDouble(splitCoors[1]));
        
        this.observation_what_0_tweet_value = inp.get(3);
        this.observation_what_1_id_str_value = Long.parseLong(inp.get(4)); 
        
        return result;
    }
    
    
}
