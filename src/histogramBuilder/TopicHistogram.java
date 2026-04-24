/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package histogramBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import static sharedModule.Functions.*;

/**
 *
 * @author Nguyen Duc Duy
 * @organization: NICT- JP
 * 
 * 
 * Instruction:
 * Class name: TopicHistogram
 * Use to save necessary information about a topic, included:
 *  - topicName: (String) name of the topic (if applicable)
 *  - topicID: (String) unique ID of the topic
 *  - Keywords: (List<String>) keywords related to the topic
 *  - histogram: (Histogram) the histogram was made base on the original structure of Robert Sedgewick and Kevin Wayne. It represent the frequency of
 * the topic in a time series. It is controlled by a couple of additional variables with will be explained bellow.
 *  - tStart: (DateTime) the starting point of the time series
 *  - tEnd: (Datetime) the ending point of the time series
 *  - lookupTable: (Map<DateTime,Integer>) save the label of time checkpoint in histogram (reference to indeces of histogram)
 */

public class TopicHistogram {
    public String topicName= "";
    public int ID = -1;
    public List<String> keywords = new LinkedList();
    public Histogram histogram;
    public Map<DateTime,Integer> lookupTable = new LinkedHashMap();
    DateTime tStart;
    DateTime tEnd;
    
    public TopicHistogram(DateTime timeStart, DateTime timeEnd, int timeSlotSize){
        tStart = timeStart;
        tEnd = timeEnd;
        
        DateTime t= timeStart;
        int count=0;
        while (t.isBefore(timeEnd) ){
            lookupTable.put(t,count);
            t = t.plusMinutes(timeSlotSize);
            count++;
        }
        histogram =  new Histogram(count);
    }
    public TopicHistogram(TopicHistogram his){
        histogram = his.histogram;
        lookupTable = his.lookupTable;
        tStart = his.tStart;
        tEnd = his.tEnd;
    }
 
    public void AddItem(DateTime time){
        if (time.isAfter( this.tEnd) || time.isBefore( this.tStart)) {
            System.out.println("Warning! unable to add data point: time is out of range " + time);
            return;
        }
        
        int index=-1;
        for (Map.Entry<DateTime,Integer> entry:  this.lookupTable.entrySet()){
            if (time.isAfter(entry.getKey()) || time.equals(entry.getKey())) {
                index++;
            }
                else break;
        }
        this.histogram.addDataPoint(index);
    }    
    
    public void PrintHistogram(){
        System.out.println("\nStart printing histogram: " );
        for (Map.Entry<DateTime,Integer> entry: lookupTable.entrySet()){ 
            System.out.println(" -* "
            + entry.getKey().toString("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'") + "+ ------> " + histogram.getItem(entry.getValue())
            + "\n    Tweet IDs: " + histogram.getRecords(entry.getValue())
            );
        }
    }
    
    public void WriteHistogramToFile(File outFile) throws IOException{
        FileWriter fileWriter = new FileWriter(outFile);
        if (outFile.exists()== false) {
                    outFile.createNewFile();
        }
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.append("time.stamp,frequency");
        for (Map.Entry<DateTime,Integer> entry: lookupTable.entrySet()){ 
  //          DateTimeFormatter ft = DateTimeFormat.forPattern("yyyy-m-d'T'HH:mm:ss.SSS'Z'"); 
            bufferedWriter.append("\n" + entry.getKey().toString("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'") + "," + histogram.getItem(entry.getValue()));
        }       
        bufferedWriter.close();
        fileWriter.close();
    }
    
    public void WriteListToFile(File outFile) throws IOException{
        FileWriter fileWriter = new FileWriter(outFile);
        if (outFile.exists()== false) {
                    outFile.createNewFile();
        }
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.append("time.stamp,frequency");

        for (Map.Entry<DateTime,Integer> entry: lookupTable.entrySet()){ 
  //          DateTimeFormatter ft = DateTimeFormat.forPattern("yyyy-m-d'T'HH:mm:ss.SSS'Z'"); 
            bufferedWriter.append("\n" + entry.getKey().toString("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'") + "," + histogram.lsa.toString() );
        }
        
        bufferedWriter.close();
        fileWriter.close();
    }
    
    
    // Utest
    
    public static void main(String[] args) {
        DateTime sta = ParseDateTimeUnified("2013-07-17T06:30:00.000Z");
        DateTime end = ParseDateTimeUnified("2013-07-17T7:50:50.000Z");
        TopicHistogram myHis = new TopicHistogram(sta, end, 30);

        
        
        myHis.PrintHistogram();
    }
}
