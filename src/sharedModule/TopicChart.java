package sharedModule;

import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import histogramBuilder.TopicHistogram;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import topicDetection.Main;
 

public class TopicChart extends Application {

     public static List<TopicHistogram> histos = new ArrayList();
     
    @Override public void start(Stage stage) {
        histos.addAll(Main.histograms);
        
        stage.setTitle("Topic histogram");
        //defining the axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        //creating the chart
        final LineChart<String,Number> lineChart = 
                new LineChart<String,Number>(xAxis,yAxis);
                
        lineChart.setTitle("Histogram of topics");
        
        // Add topics 
        
         Collection<XYChart.Series> series = new LinkedList();
        for (TopicHistogram histogram: histos ){
            XYChart.Series seriesi = new XYChart.Series();
            
            // Set name of the serie
            if (!histogram.topicName.isEmpty()) seriesi.setName(histogram.topicName);
            else {
                // Get 5 first keywords
                String label="";
                int i=0;
                while (i<10 && i<histogram.keywords.size()){
                    label=label.concat(histogram.keywords.get(i)+",");
                    i++;
                }
                
                seriesi.setName(label);
            }
            
            // Add points
            for (Entry<DateTime,Integer> item : histogram.lookupTable.entrySet()){
                seriesi.getData().add(new XYChart.Data(item.getKey().toString("MM'-'dd' 'HH':'mm'"), histogram.histogram.getItem(item.getValue())));
            }
            
            // add to series
            series.add(seriesi);
            System.out.println("A chart element added.");
            
        }
       Scene scene  = new Scene(lineChart,800,600); 
       stage.setScene(scene);
        stage.show();
        for (XYChart.Series serie : series) {
                         
            lineChart.getData().add(serie);
           
        }
        
    }
 
 
    public static void main(String[] args) {
        launch(args);
    
    }
    
    public static void drawChart(List<TopicHistogram> hist){
        // Copy all data to the internal variable
//        histos.addAll(hist);
        launch();
    }
}