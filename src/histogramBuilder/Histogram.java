/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package histogramBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert Sedgewick and Kevin Wayne
 * Modified by Nguyen Duc Duy - NICT
 */
/*************************************************************************
 *  Compilation:  javac Histogram.java
 *
 *  This data type supports simple client code to create dynamic
 *  histograms of the frequency of occurrence of values in [0, N).
 *  The frequencies are kept in an instance-variable array, and
 *  an instance variable max tracks the maximum frequency (for scaling).
 *
 *  % java Histogram 50 1000000 
 * 
 *
 *************************************************************************/

public class Histogram {
    private final double[] freq;   // freq[i] = # occurences of value i
    public static List<List<String>> lsa;
    private double max;            // max frequency of any value

    // Create a new histogram. 
    public Histogram(int N) {
        freq = new double[N];
        lsa = new ArrayList<List<String>>(N);
        for (int i= 0; i< N; i++){
            lsa.add(new LinkedList<String>());
        }
    }

    // Add one occurrence of the value i. 
    public void addDataPoint(int i) {
        freq[i]++; 
//        lsa.add(i, null);
        if (freq[i] > max) max = freq[i]; 
    } 
    // Add one tweet identity 
    public void addRecordPoint(int i, String tweetIdentity) {
        List<String> currentData = lsa.get(i);
        lsa.remove(i);
        currentData.add(tweetIdentity);
        lsa.add(i,currentData);
    } 
    // draw (and scale) the histogram.
    public void draw() {
        StdDraw.setYscale(-1, max + 1);  // to leave a little border
        StdStats.plotBars(freq);
    }
 
    public double getItem(int i){
        return freq[i];
    }
    public String getRecords(int i){
        return lsa.get(i).toString();
    }
    
    // See Program 2.2.6.
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);   // number of coins
        int T = Integer.parseInt(args[1]);   // number of trials

        // create the histogram
        Histogram histogram = new Histogram(N+1); 
        for (int t = 0; t < T; t++) {
            histogram.addDataPoint(Bernoulli.binomial(N));
        }

        // display using standard draw
        StdDraw.setCanvasSize(500, 100);
        histogram.draw();
    } 
    
    
} 
