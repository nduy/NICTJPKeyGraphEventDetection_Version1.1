package Visualization;


import sharedModule.TopicChart;
import static topicDetection.Main.histograms;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nguyen Duc-Duy
 */
public class GraphDraw extends Thread{
 public void run() {
        TopicChart.drawChart(histograms);
    }

    public static void main(String args[]) {
        (new GraphDraw()).start();
    }    
}
