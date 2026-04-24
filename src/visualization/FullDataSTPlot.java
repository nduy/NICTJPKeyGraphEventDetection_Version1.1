/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visualization;

import java.util.Map;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import static sharedModule.Functions.STWrapperToHashMap;
import tweetFilterandClustering.STWrapper;

/**
 *
 * @author isp
 */

public class FullDataSTPlot extends AbstractAnalysis {
    public static void run() throws Exception {
		AnalysisLauncher.open(new FullDataSTPlot());
	}
		
    @Override
    public void init(){
        Map<String,STWrapper> tempLookupdatable = STWrapperToHashMap(topicDetection.Main.clusterInput);

        
        
        int size = tempLookupdatable.size();

        Coord3d[] points = new Coord3d[size];
        Color[]   colors = new Color[size];
        int i=0;

        for (STWrapper wr: topicDetection.Main.clusterInput){
            points[i] = new Coord3d(wr.getOriginalLonScore(), wr.getOriginalLatScore(), wr.getOriginalDatTimScore());
            colors[i] = new Color(1.0f,0,0,1.0f);
            i++;
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
