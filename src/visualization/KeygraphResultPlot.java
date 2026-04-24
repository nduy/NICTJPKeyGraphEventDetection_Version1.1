/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visualization;

/**
 *
 * @author isp
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import topicDetection.Document;
import topicDetection.DocumentCluster;
import tweetFilterandClustering.STWrapper;


public class KeygraphResultPlot extends AbstractAnalysis{ 
    public static void run() throws Exception {
		AnalysisLauncher.open(new KeygraphResultPlot());
	}
		
    @Override
    public void init(){

        int size = topicDetection.Main.clusterInput.size();
        double x;
        double y;
        double z;

        Coord3d[] points = new Coord3d[size];
        Color[]   colors = new Color[size];
        
        int i=0;
        Map<String,Integer> indexMapTable = new HashMap<>(); // A map to save [DocID,index_in_points]
        for (STWrapper wr: topicDetection.Main.clusterInput){
            
            x = wr.getOriginalLonScore();
            y = wr.getOriginalLatScore();
            z = wr.getOriginalDatTimScore();
            points[i] = new Coord3d(x, y, z);
            colors[i] = new Color(0,0,0,0);
            indexMapTable.put(wr.tweetID,i);
            i++;
        }
       
        // intitial description

         KeygraphResultPlotDecriptions.comboList = new String[topicDetection.Main.clusters.size()];
         KeygraphResultPlotDecriptions.comboColors = new java.awt.Color[topicDetection.Main.clusters.size()];
         KeygraphResultPlotDecriptions.KeyList = new String[topicDetection.Main.clusters.size()];
        // Now color the graph
        i=0;
        for (DocumentCluster cl : topicDetection.Main.clusters){
            // creatnew color
           
            Random rand = new Random();
            float b = rand.nextFloat();//- 0.5f;
            float g = rand.nextFloat();//- 0.5f;
            float r = rand.nextFloat();//- 0.5f;
            for (Document doc: cl.docs.values()){
                colors[indexMapTable.get(doc.id)] = new Color(r,g,b,1.0f); // r,g,b,a
            }
            KeygraphResultPlotDecriptions.comboList[i] = "Cluster " + i;
            KeygraphResultPlotDecriptions.comboColors[i] = new java.awt.Color(r,g,b);
            KeygraphResultPlotDecriptions.KeyList[i] = cl.keyGraph.keySet().toString();
            i++;
            
        }
        Scatter scatter = new Scatter(points, colors);
        scatter.setWidth(5);
        scatter.setBoundingBoxColor(Color.BLACK);
        
        chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
        chart.getScene().add(scatter);
        chart.getView().setBackgroundColor(Color.WHITE);
        chart.getAxeLayout().setXAxeLabel("Longitude");
        chart.getAxeLayout().setYAxeLabel("Latitude");
        chart.getAxeLayout().setZAxeLabel("Time.stamp");  
        String args[] = null;
        KeygraphResultPlotDecriptions item = new KeygraphResultPlotDecriptions();
        item.main(args);

        
        }
    
        
    
}

