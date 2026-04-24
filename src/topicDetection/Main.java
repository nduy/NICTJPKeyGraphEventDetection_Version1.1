package topicDetection;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import histogramBuilder.KeywordHistogram;
import histogramBuilder.TopicHistogram;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import static sharedModule.Functions.*;
import tweetFilterandClustering.STWrapper;

/***
 * This is the main program of topic detection
 * @author Nguyen Duc-Duy
 */

public class Main {     
                
        public static List<TopicHistogram> histograms = new ArrayList(); // Histogram of topic freq by time windows
        public static List<KeywordHistogram> keyhistograms = new ArrayList(); // Histogram of keywords by time windows
        public static ArrayList<DocumentCluster> clusters;               // Cluster of documents and keygraph that was label according to their topic
        public static Graph g;                                           // A graph to visualize the Keygraph
        public static boolean traceGraph;                                // this avriable aims to enable step-by-step graph buiding tracing. It is temporary not applicable. PLEASE DISCARD IT! ;)
        public static boolean discardST=false;                           // If this variable is true, it means all spartial and temporal information will be discarded.
                                                                         //     Please note that this will also disable histogram drawing if its value is TRUE
        public static final List<STWrapper> clusterInput = new LinkedList<>();  // Arraylist to save all Spartial and temporal information of filtered tweets for clustering.
                                                                         //     This variable, acutally being fully computed from tokenize and filter step. An it is being READ only in this class.
                                                                         //     And it will be reuse to compute kml file for google earth dispalay
        public static Double[] max_tweet_scores= new Double[]{0.0,0.0,0.0,0.0};
        public static Set<String> inClustersIDs = new HashSet<>();
        public static boolean enable_ST_cluster_restrict = true;         // If true, then the result will be refined by the give ST info
        public static Integer refineOption = 0;                          // Refine options:
                                                                         //     0: CBTWScore*1/STScore
                                                                         //     1: CBTWScore+e^(1/STScore)
                                                                         //     2: αCBTWScore + (1-α)/STScore
        // Some parameters
        public static double DBSCAN_esp = 0.0015;                        // Epsilon of DBSCAN Clustering
        public static int DBSCAN_minPts =10;                             // Minimum number of item in cluster DBSCAN Clustering
        public static double Alpha = 0.5;                                // Alpha element for weighting the score
        public static double MIN_STSCORE = 0.51;                           // Minimum ST Score of cluster
        public static List<Cluster<STWrapper>> clusterResults = new ArrayList(); // To store DBSCAN clustering result
        public static boolean drawClusterAnchor = true;                 // To draw red anchor that connect all points in clusters
        
        /**
         * Primary function to call topic detection
         * @param args, included:
         *      - InputFile
         *      - Constants file
         *      - Output debug file
         *      - StartTime
         *      - EndTime
         *      - Delta t
         * @throws Exception 
         */
	public static void main(String[] args) throws Exception {
		Constants constants = new Constants(args[2]);
		PrintStream out = new PrintStream(args[1], "UTF-8");

                // Parse Datime
                DateTime sta = ParseDateTimeUnified(args[3]);
                DateTime end = ParseDateTimeUnified(args[4]);
                int slotSize = Integer.parseInt(args[5]);
                
		double toMins = 1000 * 60;
		HashSet<String> stopwords = Utils.importStopwords();

		HashMap<String, Double> DF = new HashMap<String, Double>();
		HashMap<String, Document> docs = new HashMap<String, Document>();
		long time1 = System.currentTimeMillis();
		
//                List<STWrapper> TemClusterInput = new ArrayList<>(); 
//                for (STWrapper wrp: clusterInput){
//                    TemClusterInput.add(new STWrapper(wrp));
//                }
                
                // Rescale tweet scores and prepare for clustering
                for (int i=0; i<clusterInput.size(); i++){
 //                   STWrapper item = new STWrapper(clusterInput.get(i));
                    STWrapper item = new STWrapper();
                    item.points = clusterInput.get(i).points;
                    item.tweetID = clusterInput.get(i).tweetID;
                    item.setLonScore(item.getLonScore()/max_tweet_scores[0]);
                    item.setLatScore(item.getLatScore()/max_tweet_scores[1]);
                    item.setDatTimScore(item.getDatTimScore()/max_tweet_scores[2]);
                }

                // Wrte vectors to file
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File("ClusteringData.csv")));
                bw.append("time,long,lat");
                for (STWrapper wr: clusterInput){
                    bw.append("\n"+wr.getDatTimScore()+ "," + wr.getLonScore() + "," + wr.getLatScore());
                }
                bw.close();
                // Start clustering
                
                if(enable_ST_cluster_restrict){
                    System.out.println("Entering clustering");
                    DBSCANClusterer<STWrapper> clusterer = new DBSCANClusterer<>(DBSCAN_esp, DBSCAN_minPts);
                    clusterResults = clusterer.cluster(clusterInput);
                   
                     System.out.println(clusterResults.size());
                    // output the clusters
                    for (int i=0; i<clusterResults.size(); i++) {
                        System.out.println("Cluster " + i);

                        for (STWrapper stWrapper : clusterResults.get(i).getPoints()){
                            inClustersIDs.add(stWrapper.tweetID);
                            //System.out.println(stWrapper.tweetID);
                            //System.out.println();
                        }

                    } 
                }

                ///////////////////////////////////////////////////////////////
                try {
                    int countf=0;
                    DateTimeFormatter ft = DateTimeFormat.forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");
                    for (Cluster<STWrapper> cl: clusterResults){
                        // Open a writer
                        BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("ClusteredData" + countf++ + ".csv")));
                        bwr.append("_id,observation.when.time,observation.where.coordinates,observation.what.0.tweet.value,observation.what.1.id_str.value");
//                        for (STWrapper wr: clusterInput){
//                            bwr.append("\n"+wr.getDatTimScore()+ "," + wr.getLonScore() + "," + wr.getLatScore());
//                        }
//                        bwr.close();
                        for (STWrapper wr: cl.getPoints()){
                            String obID = wr.tweetID;
                            String tweet = "\"" + wr.tweet.replace("\"","") + "\"";
                            String location = "\"[ " + wr.getOriginalLonScore() + ", " + wr.getOriginalLatScore() + " ]\"";
                            String obvrWhatID = "\"0\"";
                            int plusminute =  wr.getOriginalDatTimScore().intValue();

                            double plusseconds = (wr.getOriginalDatTimScore() - plusminute)*60;
                            System.out.println((int)plusseconds);
                            DateTime time = csvfiletokenize.CSVFileTokenize.query.startDate.plusMinutes(plusminute) ;
                            //DateTime time = csvfiletokenize.CSVFileTokenize.query.startDate.plusMinutes(1500) ;
                            time.plusSeconds((int)plusseconds);

                            String datetime = time.toString(ft);
                            bwr.append("\n" + obID + "," + datetime + "," + location + "," + tweet + "," + obvrWhatID );
                        }
                        bwr.close();
                    }
                } catch (Exception e){};
                
                
                ///////////////////////////////////////////////////////////////
		System.out.println("Loading Documents...");
		new DataLoader(constants).loadDocumentsCSVfile(args[0], docs, stopwords, DF, constants.REMOVE_DUPLICATES);
		long time2 = System.currentTimeMillis();
		System.out.println(docs.size() + " documents are loaded (after filtering)!");


               // String[] args = {};
                // Sided Graph stream
                g = new MultiGraph("G");
                 // init the sender
                 // - the sender
                
//                System.out.println("Connected");
                // - plug the graph to the sender so that graph events can be
                // sent automatically
                
               
                String style = "node{fill-mode:plain;fill-color:#567;size:6px;}";
                g.addAttribute("stylesheet", style);
                g.addAttribute("ui.antialias", true);
                g.addAttribute("layout.stabilization-limit", 0);
                /// End init              
                
		clusters = new DocumentAnalyze(constants).clusterbyKeyGraph(docs, DF);
		DocumentAnalyze.printTopics(clusters, out);
		out.close();
                
                //Get list of event-related object IDs
                Set<String> objectIDs = new HashSet();
                for (DocumentCluster cluster: clusters)
                    for (Document doc: cluster.docs.values())
                        objectIDs.add(doc.id);
                    
                // Read CSV file again to get timestamp, whose file is args[0]
                Map<String,DateTime> timeRecords = new HashMap();
                File inputFile = new File(args[0]);
		BufferedReader in = new   BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
                String line = null;
                int i = 0;
                int idIndex=0;
                int timeIndex = 0;
                while ((line = in.readLine()) != null)
                    if (i==0) //First line
                    {
                       String[] strs = line.split(",");
                       for (int j = 0; j< strs.length; j++){
                           if (strs[j].endsWith("_id")) idIndex=j;
                           else if (strs[j].endsWith("observation.when.time")) timeIndex = j;
                       }
                       i++;
                    }
                else
                try {
                    Reader inp = new StringReader(line);
                    CSVParser parser = new CSVParser(inp, CSVFormat.DEFAULT);
                    List<CSVRecord> list = parser.getRecords(); 
                    timeRecords.put(list.get(0).get(idIndex), ParseDateTimeUnified(list.get(0).get(timeIndex)));
                } catch (Exception e) {
                        e.printStackTrace();
                }
                in.close();
                
                if (discardST == false){
                    // Build Topic Histograms
                    histograms = DocClustertoTopicHistograms(clusters,timeRecords,sta,end,slotSize);
                   // Write Topic Histogram
                    /// Write histograms to file
                   File outFile = new File("TopicHistogram.csv");
                   FileWriter fileWriter = new FileWriter(outFile);
                   int numTopics = histograms.size();

                   if (outFile.exists()== false) {
                           outFile.createNewFile();
                   }
                   BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                   bufferedWriter.append("time.stamp");
                   for (int topicInx= 0 ; topicInx< numTopics; topicInx++) bufferedWriter.append(",freq_topic"+ topicInx);


                   if (!histograms.isEmpty())
                   for (Map.Entry<DateTime,Integer> entry: histograms.get(0).lookupTable.entrySet()){ 
                       bufferedWriter.append("\n" + entry.getKey().toString("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'"));
                       for (int j = 0; j < numTopics; j++){
                          bufferedWriter.append("," + histograms.get(j).histogram.getItem(entry.getValue()));
                       }
                   }       
                   bufferedWriter.close();
                   fileWriter.close();
                   
                   
                    // Compute keyword Histogram
                        // Fisrt get list of keywords
                   Set<String> keywordSet= new HashSet<>(); 
                   for (DocumentCluster cl : clusters){
                       for (String str : cl.keyGraph.keySet()){
                           for (String key: csvfiletokenize.CSVFileTokenize.query.keyword)
                           if (str.contains(key)) {
                               keywordSet.addAll(cl.keyGraph.keySet());
                               System.out.println("ASSSSSSSSSSSSSSSSSSSSS");
                               break;
                           }
                       }
                        
                    }
                        //Then write to file
                    Map<String,KeywordHistogram> keyHistogram = ComputeKeywordHistogram(Arrays.asList(keywordSet.toArray(new String[keywordSet.size()])),timeRecords,sta,end,slotSize,"tokenize_result.csv");
                    writeKeywordHistogram(keyHistogram,"KeywordHistogram"+ sta.toString("yyyy'.'MM'.'dd'")+ "_"+ end.toString("yyyy'.'MM'.'dd'")+ ".csv");
                    

                    
                    
                   // Build KML file for google overlay
                    final Kml kml = new Kml();
                    // First read the tokenize for Spartial info
                    // Try to display all points
                    // Document to store all tweets locations in map

                    Folder rootFolder = new Folder().withVisibility(true).withName("STKegraph markers" + (Main.enable_ST_cluster_restrict? " (With STScore)" : " (without STScore)")).withOpen(false); 
                    
                   // Original data points display
                    Folder oriFolder  = new Folder().withVisibility(true).withName("Original data points").withOpen(false); // A folder to hold all original points
                    rootFolder.addToFeature(oriFolder);
                    for (STWrapper wrap : clusterInput){
                        oriFolder.createAndAddPlacemark().withDescription(wrap.tweetID + "\n At: \n - Time: " + wrap.getOriginalDatTimScore() + "\n Tweet: " + wrap.tweet)
                                .withStyleSelector(null)
                                .withOpen(Boolean.FALSE).withStyleUrl("styles.kml#jugh_style") .withVisibility(true)
                                .createAndSetPoint().addToCoordinates(wrap.getOriginalLonScore(),wrap.getOriginalLatScore(),wrap.getOriginalDatTimScore()/2)
                                .withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND).withExtrude(Boolean.TRUE);                           
                    }
                     
                    // DBScan clustering result display
                    Folder dbsclusterFolder  = new Folder().withVisibility(true).withName("DBScan clusters data points").withOpen(false); // A folder to hold all original points
                    rootFolder.addToFeature(dbsclusterFolder);

                    for (Cluster<STWrapper> clus: clusterResults) {
                        int clusternum = 0;

                        de.micromata.opengis.kml.v_2_2_0.Document doc = new de.micromata.opengis.kml.v_2_2_0.Document().withName("Cluster "+ clusternum++);
                        Random rand = new Random();
                        int a = rand.nextInt(20)+235;
                        int b = rand.nextInt(255);
                        int g = rand.nextInt(255);
                        int r = rand.nextInt(255);
                        
                        for (STWrapper wrap : clus.getPoints()){
                            doc.createAndAddStyle().withId("img").createAndSetIconStyle().withScale(1).withColor(Integer.toHexString(255)+Integer.toHexString(b)+Integer.toHexString(g)+Integer.toHexString(r))
                                                 .withColorMode(ColorMode.NORMAL).createAndSetIcon().setHref("http://maps.google.com/mapfiles/kml/shapes/target.png");
                             doc.createAndAddPlacemark().withName(" ")
                                     .withVisibility(true)
                                     .withOpen(false)
                                     .withStyleUrl("#img")
                                     .createAndSetPoint()
                                     .withExtrude(true)
                                     .withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND)
                                     .addToCoordinates(wrap.getOriginalLonScore()+ "," +wrap.getOriginalLatScore()+ "," + wrap.getOriginalDatTimScore()/2);
                        }
                        dbsclusterFolder.addToFeature(doc);
                        clusternum++;
                    } 
                    
                    
                    // STGraph result display
                      Folder STKeyGraph  = new Folder().withVisibility(true).withName("Keygraph points").withOpen(false); // A folder to hold all original points
                    rootFolder.addToFeature(STKeyGraph);

                    Map<String,STWrapper> tempLookupdatable = STWrapperToHashMap(clusterInput); // A lookup tabel [tweetID, STWrapper]
                    int clusternum = 0;
                    for(DocumentCluster cl : clusters){
                        de.micromata.opengis.kml.v_2_2_0.Document doc = new de.micromata.opengis.kml.v_2_2_0.Document().withName("Cluster "+ clusternum++)
                                                                                                                       .withDescription("Keys:" + cl.keyGraph.values().toString() + "\nDocs" + cl.docs.keySet().toString());
                        Random rand = new Random();
                        int a = rand.nextInt(20)+235;
                        int b = rand.nextInt(255);
                        int g = rand.nextInt(255);
                        int r = rand.nextInt(255);
                        int point = 0;
                        for (String docid : cl.docs.keySet()){
                            STWrapper wrap = tempLookupdatable.get(docid);
                            doc.withDescription(docid).createAndAddStyle().withId("img").createAndSetIconStyle().withScale(1)
                                                 .withColor(Integer.toHexString(255)+Integer.toHexString(b)+Integer.toHexString(g)+Integer.toHexString(r))
                                                 .withColorMode(ColorMode.NORMAL).createAndSetIcon().setHref("http://maps.google.com/mapfiles/kml/shapes/star.png"); 
                            doc.createAndAddPlacemark().withName(" ")
                                     .withVisibility(true)
                                     .withOpen(true)
                                     .withName((clusternum-1)+"_"+point++)
                                     .withDescription("Cluster: " + (clusternum-1) + "."
                                             + "\n\nCluster Keys: " + cl.keyGraph.keySet().toString()
                                             + "\n\nNom: " + (point-1) + "."
                                             + "\n\nDocID: " + docid.replace("ObjectID(","").replace(")","")
                                             + ".\n\nTweet: " + wrap.tweet )
                                     .withStyleUrl("#img")
                                     .createAndSetPoint()
                                     .withExtrude(true)
                                     .withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND)
                                     .addToCoordinates(wrap.getOriginalLonScore()+ "," +wrap.getOriginalLatScore()+ "," + wrap.getOriginalDatTimScore()/2);
                        }
                        STKeyGraph.addToFeature(doc);
                    }

                    // STGraph result display by key
                    Folder STKeyGraph_withwords  = new Folder().withVisibility(true).withName("Keygraph points with keywords").withOpen(false); // A folder to hold all original points
                    rootFolder.addToFeature(STKeyGraph_withwords);
                    clusternum = 0;
                    for(DocumentCluster cl : clusters){
                        Folder keyClus = new Folder().withName("Cluster "+ clusternum++).withDescription("Keys:" + cl.keyGraph.values().toString() + "\nDocs" + cl.docs.keySet().toString());
                        // Sub category by keywords
                        Set<String> keywords = cl.keyGraph.keySet();
                        // create inverted index
                        Map<String,List<String>> invertedIndex = new HashMap<>(); 
                        for (String key: keywords){
                            List<String> doclist = new LinkedList<>();
                            for (Document doc: cl.docs.values()){
                                if (doc.keywords.containsKey(key)) doclist.add(doc.id);
                            }
                            invertedIndex.put(key, doclist);
                        }
                        
                         int point = 0;
                        for (String kw :keywords){
                            // File re;ated documents:
                             de.micromata.opengis.kml.v_2_2_0.Document doc = new de.micromata.opengis.kml.v_2_2_0.Document().withName(kw)
                                                                                                                       .withDescription("Keys:" + cl.keyGraph.values().toString() + "\nDocs" + cl.docs.keySet().toString());
                            Random rand = new Random();
                                 int a = rand.nextInt(20)+235;
                                int b = rand.nextInt(255);
                                int g = rand.nextInt(255);
                                int r = rand.nextInt(255);
                            doc.createAndAddStyle().withId("img3").createAndSetIconStyle().withScale(1)
                                                 .withColor(Integer.toHexString(255)+Integer.toHexString(b)+Integer.toHexString(g)+Integer.toHexString(r))
                                                 .withColorMode(ColorMode.NORMAL).createAndSetIcon().setHref("http://maps.google.com/mapfiles/kml/shapes/open-diamond.png");
                           
                            for (String docid: invertedIndex.get(kw)){
                               STWrapper wrap = tempLookupdatable.get(docid);
                                
                               Placemark mark = new Placemark()
                                     .withName(" ")
                                     .withVisibility(true)
                                     .withOpen(true)
                                     .withDescription("Cluster: " + (clusternum-1) + "."
                                             + "\n\nCluster Keys: " + keywords.toString()
                                             + "\n\nNom: " + (point-1) + "."
                                             + "\n\nDocID: " + docid.replace("ObjectID(","").replace(")","")
                                             + ".\n\nTweet: " + wrap.tweet )
                                     .withName((clusternum-1)+"_"+point++);
                                 mark.setStyleUrl("#img3");
                                 mark.createAndSetPoint()
                                     .withExtrude(true)
                                     .withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND)
                                     .addToCoordinates(wrap.getOriginalLonScore()+ "," +wrap.getOriginalLatScore()+ "," + wrap.getOriginalDatTimScore()*2);
                                 doc.addToFeature(mark);
                            }
                            keyClus.addToFeature(doc);
                        }
                        STKeyGraph_withwords.addToFeature(keyClus);
                    }
                     
                    kml.setFeature(rootFolder);
                    kml.marshal(new File("Mappoints.kml"));
                     // The all encapsulating kml element     
                    
                    
                    
                    //Compute densities
                    ComputeDensity(clusters);
                    // Cluster evaluation
              //      ResultInternalEvatuation(clusters,tempLookupdatable);
                    // Write Evaluation data
                    writeEvaluationData(clusters,tempLookupdatable);
                   }
                // Compute running time
		long time3 = System.currentTimeMillis();
		System.out.println(docs.size() + "\t" + (time3 - time1) / toMins + "\t" + (time2 - time1) / toMins + "\t" + (time3 - time2)
			/ toMins + "\n");
		System.out.println("DONE!");

	}
}
