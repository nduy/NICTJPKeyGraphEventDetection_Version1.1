/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sharedModule;

import histogramBuilder.KeywordHistogram;
import histogramBuilder.TopicHistogram;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.mahout.math.Vector;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import static org.apache.mahout.clustering.ClusteringUtils.daviesBouldinIndex;
import static org.apache.mahout.clustering.ClusteringUtils.dunnIndex;
import static org.apache.mahout.clustering.ClusteringUtils.summarizeClusterDistances;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.stats.OnlineSummarizer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import topicDetection.Document;
import topicDetection.DocumentCluster;
import topicDetection.Node;
import tweetFilterandClustering.STWrapper;

/**
 *
 * @author isp
 */
public class Functions {
    
    
    /**
     * 
     * @param str
     * @return the DateTime form (JodaTime) of the string
     */
    public static DateTime ParseDateTimeUnified(String str){
        DateTime result = new DateTime();
        DateTimeFormatter ft = DateTimeFormat.forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'"); 
        
            // Parse from date
        try { 
                result = ft.parseDateTime(str); 
            } catch (Exception e) { 
              System.out.println("Unparseable using " + ft); 
              // return default: CURRENT TIME
              result = DateTime.now();
              e.printStackTrace();
        }
        return result;
        }
    
    /**
     * Build Topic Histogram from a Document Cluster
     * @param: 
     *   - cluster a  Document Cluster form KeyGraph FrameWork (see https://keygraph.codeplex.com/)
     *   - timeRecords a map <ObjectID,itsDateTime>
     *   - timeStart, timeEnd,timeSlotSize: time information
     * @return the histogram of the cluster
     */
    public static TopicHistogram DocClustertoTopicHistogram(DocumentCluster cluster, Map<String,DateTime> timeRecords, DateTime timeStart, DateTime timeEnd, int timeSlotSize){
        TopicHistogram rs = new TopicHistogram(timeStart,timeEnd, timeSlotSize);
        if (cluster.MatchingEvent!= null) rs.topicName = cluster.MatchingEvent.id;
        rs.ID = cluster.id;
        for (Node node: cluster.keyGraph.values()) rs.keywords.add(node.keyword.baseForm);
        // Now add times to the histogram
        for (Document doc : cluster.docs.values()){
            rs.AddItem(timeRecords.get(doc.id));
        }
        return rs;
    }
    
    
    /**
     * 
     * @param clusters an array list of DocumentClusters
     * @param timeRecords timeRecords a map <ObjectID,itsDateTime>
     * @param timeStart start ti,e of histograms
     * @param timeEnd end time of histograms
     * @param timeSlotSize size of time slot (IN MINUTE)
     * @return 
     */
    public static List<TopicHistogram>  DocClustertoTopicHistograms(ArrayList<DocumentCluster> clusters, Map<String,DateTime> timeRecords, DateTime timeStart, DateTime timeEnd, int timeSlotSize){
        List<TopicHistogram> rs = new ArrayList();
        for (DocumentCluster clus: clusters) rs.add(DocClustertoTopicHistogram(clus,timeRecords,timeStart,timeEnd,timeSlotSize));
        return rs;
    }
    
    public static Map<String,KeywordHistogram> ComputeKeywordHistogram(List<String> listOfWords,Map<String,DateTime> timeRecords, DateTime timeStart, DateTime timeEnd, int timeSlotSize, String pathToTokenFile) throws FileNotFoundException, UnsupportedEncodingException, IOException{
        Map<String,KeywordHistogram> res = new HashMap<>();
        for (String key: listOfWords){
            res.put(key,  new KeywordHistogram(timeStart,timeEnd,timeSlotSize));
        }
        
        // Open the token file 
        try{
            BufferedReader bufferedReader = new   BufferedReader(new InputStreamReader(new FileInputStream(pathToTokenFile), "UTF-8"));
            String rline;
            int line_no =0;
            while ((rline = bufferedReader.readLine()) != null) {
                // skip the header line    
                if (line_no == 0) {
                    line_no++;
                    continue;
           //        rline="ObjectID(5),2014-08-20T00:50:00.000Z,\"[ 139.6917,35.6895 ]\",\"雨\",292796271142526976";
                }    
                Reader in = new StringReader(rline);
                CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT);
                List<CSVRecord> list = parser.getRecords();  

                List<String> tokens = Arrays.asList(list.get(0).get(5).split(" ")); 

    // Now to the checks
                //System.out.println(rline);
                for (String key : listOfWords){
                    if (tokens.contains(key)){
                        res.get(key).AddItem(ParseDateTimeUnified(list.get(0).get(1)));
                    }
                }

                line_no++;
                if (line_no %100000 == 0) System.out.println("Processed "+ line_no+ " lines");
            }

            System.out.println("Finished Processing "+ line_no+ " lines");
            } catch (IOException e) {
                    e.printStackTrace();
            } 
        return res;
    }
    
    public static void writeKeywordHistogram(Map<String,KeywordHistogram> histos, String PathToSaveFile) throws IOException{
        File outFile = new File(PathToSaveFile);
        FileWriter fileWriter = new FileWriter(outFile);

        if (outFile.exists()== false) {
                outFile.createNewFile();
        }
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.append("time.stamp");
       
        String lastKey = ""; // The key to get datetime. It could be anykey
        for (String key: histos.keySet()) {
           bufferedWriter.append(","+ key);
           lastKey = key;
        }

        if (!histos.isEmpty()){
           for (Map.Entry<DateTime,Integer> entry: histos.get(lastKey).lookupTable.entrySet()){ 
                       bufferedWriter.append("\n" + entry.getKey().toString("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'"));
                       for (Map.Entry<String,KeywordHistogram> kwHis : histos.entrySet()){
                          bufferedWriter.append("," + kwHis.getValue().histogram.getItem(entry.getValue()));
                       }
                   }      
        }
           
       
       bufferedWriter.close();
       fileWriter.close();
       }
    
    public static Map<String,STWrapper> STWrapperToHashMap(List<STWrapper> stlist){
        Map<String,STWrapper> result = new HashMap<>();
        for (STWrapper item: stlist){
            if (item.tweetID!=null)
            result.put(item.tweetID, item);
        }
        return result;
    }

    //We compute density. Desity is (2* sum all weight)/(V.(V-1)) where V is number of Vertecs
    // cite: Takeshi Tokuyama (Ed.): Algorithms and Computation, 18th International Symposium, ISAAC 2007, Sendai, Japan, December 17-19, 2007, Proceedings. Springer 2007, ISBN 978-3-540-77118-0
    public static void ComputeDensity(List<DocumentCluster> cls) throws IOException{
        BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("ClusterDensities.csv")));
        bwr.append("cluster_nom,keys,density");
        int count = 0;
        for (DocumentCluster cl : cls){
            Double v = cl.keyGraph.size()*1.1;
            Double sum_weight = 0.0;

            for (topicDetection.Node n : cl.keyGraph.values()){
                for (topicDetection.Edge e : n.edges.values()){
                    if (e.n1.equals(n)){
                      Double MI = e.df / (e.n1.keyword.df + e.n2.keyword.df - e.df);
                      MI =Math.round( MI * 10000.0 ) / 10000.0;
                      sum_weight = sum_weight+ MI;
                    }
                }
            }
        bwr.append("\n" + count++ + ",\"" + cl.keyGraph.keySet().toString() + "\","+ 2*sum_weight/(v*(v-1)));
        }
        bwr.close();
    }
    
    // Internal Evaluation clusters using Davies-Bouldin Index and Dunn Index 
    
    public static void ResultInternalEvatuation(List<DocumentCluster> cls, Map<String,STWrapper> tempLookupdatable) throws IOException{
        //Get centroids and data points
        List<Vector> centroids = new LinkedList<>();
        List<Vector> datapoints = new LinkedList<>();
        for (DocumentCluster cl: cls){
            double averagex = 0;
            double averagey = 0;
            double averagez = 0;
            for (Document d : cl.docs.values()){
                if (tempLookupdatable.containsKey(d.id)){
                    double[] point = tempLookupdatable.get(d.id).getPoint();
                    datapoints.add(new DenseVector(point));
                    averagex = averagex + point[0];
                    averagey = averagey + point[1];
                    averagez = averagez + point[2];
                }
            }
            double ndoc = cl.docs.size()*1.0;
            double[] centroid = {averagex/ndoc,averagey/ndoc,averagez/ndoc};
            centroids.add(new DenseVector(centroid));
        }
        
        // Compue summary
        List<OnlineSummarizer> summary = summarizeClusterDistances(centroids,datapoints,new  EuclideanDistanceMeasure());
        BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("InternalEvaluation.csv")));
        bwr.append("measure,value");
        bwr.append("\n\"DaviesBouldinIndex\"," + daviesBouldinIndex(centroids,new  EuclideanDistanceMeasure(),summary ));
        bwr.append("\n\"DunnIndex\"," + dunnIndex(centroids,new  EuclideanDistanceMeasure(),summary ));
        bwr.close();
    }
    
    
    public static void writeEvaluationData(List<DocumentCluster> cls, Map<String,STWrapper> tempLookupdatable) throws IOException{
        BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("EvaluationData.csv")));
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("EvaluationData_orig.csv")));
        bwr.append("long_score,lat_score,time_score,cluster");
        bw.append("long_score,lat_score,time_score,cluster");
        int cluster_count=1;
        for (DocumentCluster cl: cls){
            for (Document d : cl.docs.values()){
                if (tempLookupdatable.containsKey(d.id)){
                    double[] point = tempLookupdatable.get(d.id).getPoint();
                    double[] opoint = tempLookupdatable.get(d.id).original_points;
                    bwr.append("\n" + point[0]+ "," + point[1] + "," + point[2] + "," + cluster_count);
                    bw.append("\n" + opoint[0]+ "," + opoint[1] + "," + opoint[2] + "," + cluster_count);
                    
                }
            }
            cluster_count++;
        }
        bwr.close();
        bw.close();
    }
}

    

    

