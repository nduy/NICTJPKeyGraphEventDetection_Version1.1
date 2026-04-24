/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetFilterandClustering;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.joda.time.DateTime;

/**
 *
 * @author Nguyen Duc Duy
 */
public class STWrapper implements Clusterable {
    public double[] points;
    public double[] original_points;
    public String tweet;
    
    public String tweetID= "";
    public STWrapper(TweetDataRecord rec) {
        this.points = fetchTweetMetadata(rec);
        tweetID = rec._id;
        original_points = new double[points.length];
        tweet = rec.observation_what_0_tweet_value;
        for (int i=0; i< points.length; i++ ) original_points[i]= points[i];
       // this.datapoints = new double[] { location.getX(), location.getY() }
    }
    
    public STWrapper(STWrapper wrap) {
        this.points = wrap.points;
        this.tweetID = wrap.tweetID;
       // this.datapoints = new double[] { location.getX(), location.getY() }
    }
    
     public STWrapper() {
      
       // this.datapoints = new double[] { location.getX(), location.getY() }
    }

    @Override
    public double[] getPoint() {
        return points;
    }
    
    // Fetch location and time information into doubles for clustering
    protected double[] fetchTweetMetadata(TweetDataRecord dataRecord){
        double longtitude = dataRecord.observation_where_coordinates.getLongitude();
        double latitude = dataRecord.observation_where_coordinates.getLatitude();
        double datetimescore;
        //double datescore;
        //double timescore;
        DateTime datetime = dataRecord.observation_when_time;
        DateTime queryStartDate = csvfiletokenize.CSVFileTokenize.query.startDate;
        
        if (datetime.isBefore(queryStartDate) || datetime.isAfter(csvfiletokenize.CSVFileTokenize.query.endDate)){
            System.out.println("DateTime out of range.");
            return null;
        }
//        System.out.println(datetime.toString() +"||"+ datetime.getYear() +"||"+ datetime.getMonthOfYear() +"||"+ datetime.getDayOfMonth());
//        System.out.println(datetime.toString() +"::"+ datetime.getHourOfDay() +"::"+ datetime.getMinuteOfHour() +"::"+ datetime.getSecondOfMinute() + "::"+ datetime.getMillisOfSecond()); 
        
        // We process date first. The date is yyyy-mm-dd. We assum that all months have 30.41666667 days. then we use MONTH as mark (before decimal points)
        datetime = datetime.minusYears(queryStartDate.getYear()); // Minust the year that the time window started
        datetime = datetime.minusMonths(queryStartDate.getMonthOfYear()-1);
        datetime = datetime.minusDays(queryStartDate.getDayOfMonth()-1);
/*        // The datescore is: total of days (after January 1 of the year of staring of time windows and + current day of 
        datescore = datetime.getYear()*12 
                + datetime.getMonthOfYear() -1 // -1 since current month is not finish yet
                + datetime.getDayOfMonth()/34.4166666666667;
        
        //Sencondly, we process time. The time format is HH:MM:ss,zzzz. Then we use MINUTE as mark
        timescore = datetime.getHourOfDay()*60.0
                + datetime.getMinuteOfHour()*1.0      // Since an hour have 60 minutes
                + datetime.getSecondOfMinute()*1.0 / (60) // since an hour have 3600 second
                + datetime.getMillisOfSecond()*1.0 / (60*1000);

        if (longtitude> topicDetection.Main.max_tweet_scores[0])  topicDetection.Main.max_tweet_scores[0] = longtitude;
        if (latitude> topicDetection.Main.max_tweet_scores[1])  topicDetection.Main.max_tweet_scores[1] = latitude;
        if (datescore> topicDetection.Main.max_tweet_scores[2])  topicDetection.Main.max_tweet_scores[2] = datescore;
        if (timescore> topicDetection.Main.max_tweet_scores[3])  topicDetection.Main.max_tweet_scores[3] = timescore;
        
        //System.out.println(longtitude + " __ " + latitude + " __ " + datescore + " __ " + timescore);       

        return (new double[] { longtitude, latitude, datescore, timescore});
*/
        datetimescore = datetime.getYear()*12 *30.46666*24*60
                + (datetime.getMonthOfYear()-1) *30.46666*24*60 // -1 since current month is not finish yet
                + (datetime.getDayOfMonth()-1) *24*60
                + (datetime.getHourOfDay())*60
                + datetime.getMinuteOfHour()      // Since an hour have 60 minutes
                + datetime.getSecondOfMinute()*1.0 / (60) // since an hour have 3600 second
                + datetime.getMillisOfSecond()*1.0 / (60*1000);
        
        if (longtitude> topicDetection.Main.max_tweet_scores[0])  topicDetection.Main.max_tweet_scores[0] = longtitude;
        if (latitude> topicDetection.Main.max_tweet_scores[1])  topicDetection.Main.max_tweet_scores[1] = latitude;
        if (datetimescore> topicDetection.Main.max_tweet_scores[2])  topicDetection.Main.max_tweet_scores[2] = datetimescore;
        
        return (new double[] { longtitude, latitude, datetimescore});
    }
    
    public void setLonScore(Double d){
        this.points[0] = d;
    }
    
    public void setLatScore(Double d){
        this.points[1] = d;
    }
    
    public void setDatScore(Double d){
        this.points[2] = d;
    }

    public void setDatTimScore(Double d){
        this.points[2] = d;
    }
    
    public void setTimScore(Double d){
        this.points[3] = d;
    }
        
    public Double getLonScore(){
        return this.points[0];
    }
    
    public Double getLatScore(){
        return this.points[1];
    }
    
    public Double getDatTimScore(){
        return this.points[2];
    }
    
    public Double getOriginalLonScore(){
        return this.original_points[0];
    }
    
    public Double getOriginalLatScore(){
        return this.original_points[1];
    }
    
    public Double getOriginalDatTimScore(){
        return this.original_points[2];
    }
    
    
}
