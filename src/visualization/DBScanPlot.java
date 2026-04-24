/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visualization;

import java.util.Random;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import tweetFilterandClustering.STWrapper;

/**
 *
 * @author isp
 */

public class DBScanPlot extends AbstractAnalysis {
    public static void run() throws Exception {
		AnalysisLauncher.open(new DBScanPlot());
	}
		
    @Override
    public void init(){

        int size = 0;
// compute the size
        for (Cluster<STWrapper> clus: topicDetection.Main.clusterResults) size = size + clus.getPoints().size();
        

        Coord3d[] points = new Coord3d[size];
        Color[]   colors = new Color[size];
        
        int i=0;
        // Now color the graph

        for (Cluster<STWrapper> clus: topicDetection.Main.clusterResults){
             Random rand = new Random();
            float b = rand.nextFloat();//- 0.5f;
            float g = rand.nextFloat();//- 0.5f;
            float r = rand.nextFloat();//- 0.5f;
            for(STWrapper wr:  clus.getPoints()){
                double x = wr.getOriginalLonScore();
                double y = wr.getOriginalLatScore();
                double z = wr.getOriginalDatTimScore();
                points[i] = new Coord3d(x, y, z);
                colors[i] = new Color(r,g,b,1.0f);
                i++;
            }
        }
        
       
        Scatter scatter = new Scatter(points, colors);
        scatter.setWidth(2);
        scatter.setBoundingBoxColor(Color.BLACK);
        chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
        chart.getScene().add(scatter);
        chart.getView().setBackgroundColor(Color.WHITE);
        chart.getAxeLayout().setXAxeLabel("Longitude");
        chart.getAxeLayout().setYAxeLabel("Latitude");
        chart.getAxeLayout().setZAxeLabel("Time.stamp");
        }
}
