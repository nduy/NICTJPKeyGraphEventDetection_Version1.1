/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetFilterandClustering;

import java.util.LinkedList;
import java.util.List;
import org.joda.time.DateTime;
import org.opengts.util.GeoPoint;
import org.opengts.util.GeoPolygon;
import static sharedModule.Functions.ParseDateTimeUnified;

/**
 *
 * @author isp
 */
public class QueryContainer {
    public static GeoPolygon observedArea = new GeoPolygon();
    public static DateTime startDate = new DateTime();
    public static DateTime endDate = new DateTime();
    public static List<String> keyword = new LinkedList<>();
    public static int delta=0;
    
    public QueryContainer(){
        
    }
    
    public void SetGeopPolygon(List<GeoPoint> ls){
        observedArea = new GeoPolygon(ls);
    }
    
    public void SetGeopPolygon(GeoPolygon obsArea){
        observedArea = obsArea;
    }
    
    public void AddNewGeoPoint(GeoPoint pt){
        observedArea.addGeoPoint(pt);
        
    }
    
        
    public void SetStartDate(DateTime d){
        startDate = d;
    }
    
    public void AddKeyword(String str){
        keyword.add(str);
    }
    
    public void SetDelta(int del){
        delta= del;
    }
    public void SetStartDate(String str){
//         SimpleDateFormat ft = new SimpleDateFormat ("yyyy-mm-dd'T'HH:mm:ss.SS'Z'"); 
//        // Parse from date
//        try { 
//            startDate = ft.parse(str); 
//        } catch (ParseException e) { 
//          System.out.println("Unparseable using " + ft); 
//          e.printStackTrace();
//        }
        startDate = ParseDateTimeUnified(str);
    }
    
    public void SetEndDate(DateTime d) {
        endDate = d;
    }
    
    public void SetEndDate(String str) {
        // Parse from date
//        try { 
//            endDate = ft.parse(str); 
//        } catch (ParseException e) { 
//          System.out.println("Unparseable using " + ft); 
//          e.printStackTrace();
//        }
        endDate = ParseDateTimeUnified(str);
    }
    
    public void SetKeywords(List<String>  keys) {
        keyword = keys;
    }
    
    public List<String> getKeywords(){
        return keyword;
    }
    
}
