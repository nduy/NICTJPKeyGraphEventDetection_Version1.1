package Visualization;

import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.scene.chart.StackedBarChart;
import topicDetection.Document;
import topicDetection.DocumentCluster;
import static topicDetection.Main.clusters;
 

public class DocChart extends Application {

    

         
         
         Map<String,List<String>> IndexTable = new LinkedHashMap<>(); // Inverted index [DocID, List of its topics]
        // Define axist 
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final StackedBarChart<String,Number> bc =
            new StackedBarChart<>(xAxis, yAxis);
        // Define series list
        final List< XYChart.Series<String,Number>> seriesList = new ArrayList();
     
    @Override public void start(Stage stage) {
                
        Map<String, String> lookUpTable = new HashMap<>(); 
        stage.setTitle("Document-Topic histogram");
        
        
        int i = 0;
         for (DocumentCluster dc : clusters) {
                for (Document d: dc.docs.values()){
                    if (!lookUpTable.containsKey(d.id)) lookUpTable.put(d.id, i+++""); else System.out.println("BUZZZZZZZZZ!");
                }
            //    if (i>100) break;    
        }
         
                  // Build the index table
        List<String> tempIDs = new LinkedList<>(); 

        tempIDs.addAll(lookUpTable.values());
    
        xAxis.setLabel("Document");
        xAxis.setCategories(FXCollections.<String>observableArrayList(
               tempIDs));

        yAxis.setLabel("Frq");
         
        bc.setTitle("Histogram of topic in documents");
        bc.setCategoryGap(0);
        
  /*     
       int i=0;
        
        for (Entry<String,List<String>> entry : IndexTable.entrySet()){
            if (i++==10) break;
              XYChart.Series<Number,String> seriesi = new XYChart.Series<>();
            // Set name of the serie
            seriesi.setName( i+"");
            for (String str : entry.getValue()){
                System.out.println(str);
                seriesi.getData().add(new XYChart.Data<>(1, str));
            seriesList.add(seriesi);
            
            }
                
        }
 */
        
        for (DocumentCluster cls: clusters){
             XYChart.Series<String,Number> seriesi = new XYChart.Series<>();
             seriesi.setName(cls.id + "");
             for (Document doc: cls.docs.values()){
                 if (lookUpTable.containsKey(doc.id))
                     seriesi.getData().add(new XYChart.Data<String, Number>(lookUpTable.get(doc.id), 1));
             }
             bc.getData().add(seriesi);
             
             
             
        }
//       for ( XYChart.Series<Number,String> se: seriesList) ;;

       Scene scene  = new Scene(bc,800,600); 
       stage.setScene(scene);
        stage.show();
    }
 
 
    public static void main(String[] args) {
        launch(args);
    
    }
    
    public static void drawChart(){
        // Copy all data to the internal variable
//        histos.addAll(hist);
        launch();
    }
}