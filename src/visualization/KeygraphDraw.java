package Visualization;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import topicDetection.DocumentCluster;
import static topicDetection.Main.drawClusterAnchor;

/**
 * This program draw GrraphStream from clusters of keyGraph
 * How it works:
 * 1. Create graph, viewer and view (and extend of JFrame)
 * 2. Add nodes to the new graph as well as their class and other attributes
 *    There are 4 class of node, included:
 *         i. KeywordNode: node of key words in cluster. All KeywordNode connected to it cluster's ClusterAnchorNode
 *         ii. ClusterAnchorNode: node that connect to all member of its cluster.
 *         iii. RootNode: the root of all nodes, it connect to all ClusterAnchorNode
 */

/**
 *
 * @author Nguyen Duc-Duy
 */

public class KeygraphDraw extends Thread{
    
    
    
    public void run() {
        ArrayList<DocumentCluster> clusters = topicDetection.Main.clusters;
         final Graph graph = new SingleGraph("My graph");
         
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
//        graph.addAttribute("ui.stylesheet", styleSheet);
        graph.setAutoCreate(true);
        graph.setStrict(false);
        Viewer viewer;
        viewer =  graph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);
        final View view = viewer.getDefaultView();
       
       // jPanel3.add(view, BorderLayout.CENTER);
       
        view.setAutoscrolls(true);
        
//        double myX;
//        double myY;
        view.addMouseMotionListener(new MouseAdapter(){
            @Override
            public void mouseMoved(MouseEvent me) {
            
            double mX =  me.getPoint().getX();
            double mY =  me.getPoint().getY();
            //myX =  mX;
            }
        });
               
        view.addMouseWheelListener(new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
          String message;
          String newline= "\n";
            
            double  notches = e.getPreciseWheelRotation();
            double curretpercent = view.getCamera().getViewPercent();
            //System.out.println("curretpercent " + curretpercent);
            //System.out.println("notches" + notches);
            double crollpercen = curretpercent+notches/40;
            //System.out.println("crollpercen " + crollpercen);
            view.getCamera().setViewPercent(crollpercen);
            viewer.enableAutoLayout();
            
            if (notches < 0) {
                message = "Mouse wheel moved UP "
                             + -notches + " notch(es)" + newline;
            } else {
                message = "Mouse wheel moved DOWN "
                             + notches + " notch(es)" + newline;
            }
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                message += "    Scroll type: WHEEL_UNIT_SCROLL" + newline;
                message += "    Scroll amount: " + e.getScrollAmount()
                        + " unit increments per notch" + newline;
                message += "    Units to scroll: " + e.getUnitsToScroll()
                        + " unit increments" + newline;
                message += "    Vertical unit increment: "
                   
                    + " pixels" + newline;
            } else { //scroll type == MouseWheelEvent.WHEEL_BLOCK_SCROLL
                message += "    Scroll type: WHEEL_BLOCK_SCROLL" + newline;
                message += "    Vertical block increment: "
                   
                    + " pixels" + newline;
            }
            //System.out.println(message);
        }
     });   
    /*    
        view.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
            view.setSize(e.getComponent().getHeight(),e.getComponent().getWidth());
            
            System.out.println("WTF");
            //myX =  mX;
            }
        });
    */    
       // DocumentCluster cluster = clusters.get(7);
        if (clusters != null){
                // now add the nodes and edgles 
                int count = 0;
                for (DocumentCluster cluster: clusters){
                    try {
                        
                        for (topicDetection.Node n : cluster.keyGraph.values()){
                            // add node to graph
                            String nodeName= n.getID() + "\0" + n.keyword.baseForm;
                            Node newNode= graph.addNode(nodeName);
                            newNode.addAttribute("class","KeywordNode" );
                            newNode.addAttribute("ui.style", keywordNodeStyle2);
                            newNode.addAttribute("ui.label", n.keyword.baseForm);
                            
                        }
                        System.out.println("_______________ " + count++);
                        view.updateUI();
                        
                        view.getCamera().resetView();
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KeygraphDraw.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            // Set label to nodes
 /*           for (Node node : graph) {
                if (node.getAttribute("class").equals("KeywordNode"))
                     node.addAttribute("ui.label", node.getId().split("\0")[1]); // Get only keyword to display
                     ;
                     //node.addAttribute("ui.color", Color.RED);
            } 
  */          
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                Logger.getLogger(KeygraphDraw.class.getName()).log(Level.SEVERE, null, ex);
            }
              
            // Add edges clusters internal
            for (DocumentCluster cluster: clusters){
            {
                // Add anchor node for a cluster
                String anchorName = "";
                Node newNode;
                if (drawClusterAnchor) {
                    anchorName = "anchor\0"+ cluster.id;
                    newNode = graph.addNode(anchorName);
                    newNode.addAttribute("ui.style", ClusterAnchorNode);
                }
                
                int i=0;
                for (topicDetection.Node n : cluster.keyGraph.values()) {

                    
                    int valid_edge = 0;
                    for (topicDetection.Edge e : n.edges.values()){
                        if (e.n1.equals(n)){
                           Double MI = e.df / (e.n1.keyword.df + e.n2.keyword.df - e.df);
                           Double MI2 = (e.df_incluster*1.0) / e.df ;
                           //if (MI2==0.0) {
                           //     continue;
                           //}
                          // if (MI!=MI2) System.out.println("!?!?!?!??!?!??!"); 
                           MI =Math.round( MI * 10000.0 ) / 10000.0;
                           MI2 =Math.round( MI2 * 10000.0 ) / 10000.0;
                           // semilar to nood, we add edgle weight next to him
                           Edge ed = graph.addEdge(e.n1.getID() + "\0" + MI , e.n1.getID() +"\0" + e.n1.keyword.baseForm, e.n2.getID() +"\0" + e.n2.keyword.baseForm);
                           if (ed!= null) {
                               if (topicDetection.Main.enable_ST_cluster_restrict)
                                ed.addAttribute("ui.label", MI + "<>" + MI2);
                               else ed.addAttribute("ui.label", MI);
                               ed.addAttribute("ui.style", KeywordEdge);
                               valid_edge++;
                           }
                           //Thread.sleep(10);
                       }
                    }

                       
                    
                    // add an edge to Anchor
                    Edge newEdge = null;
                    if (drawClusterAnchor && valid_edge!=0) newEdge= graph.addEdge(anchorName+ i++ +"\0anchor", n.getID() + "\0" + n.keyword.baseForm,anchorName,true);
                    if (newEdge!=null) newEdge.addAttribute("ui.style", ClusterAnchorEdge);
   //                    view.updateUI();
    //                   view.getCamera().resetView();

                    }   
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KeygraphDraw.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            }     
            
        } 
        
    }
            
    /*  
        
        for (Edge ed : graph.getEdgeSet()) {
           
           ed.addAttribute("ui.label", ed.getId().split("\0")[1]);
        }

      
     
       
        
       
         
        //setSize(800, 800);
    
    }
  */  
    public static void main(String args[]) {
        (new KeygraphDraw()).start();
    }  
    
    protected String styleSheet =
        "node {" +
        "   fill-color: #3399FF;" +
        "   stroke-color: #000000;" +
        "   stroke-width: 5px;" +
        "   shadow-mode: gradient-radial;" +
        "   shadow-width: 5px; " +
        "   text-size: 20px; " +
        "   size: 20px;" +
        "   z-index: 0;" +
        "}" +
        "node.marked {" +
         "   text-size: 55px; " +
        "   fill-color: red;" +
        "}" +
        "edge {"+
        "shape: line;"+
        "fill-color: #222;"+
        "text-color: #009900;"+
        "arrow-size: 3px, 2px;"+
        "}";
        
    protected String keywordNodeStyle =
        
        "   fill-color: #3399FF;" +
        "   stroke-color: #000000;" +
        "   stroke-width: 5px;" +
        "   shadow-mode: gradient-radial;" +
        "   shadow-width: 5px; " +
        "   text-size: 20px; " +
        "   size: 20px;" +
        "   z-index: 0;";
     protected String keywordNodeStyle2 =
            "   shape: circle;"+
            "   size-mode: dyn-size;"+
            "   size: 15px;"+
            "   fill-mode: gradient-radial;"+
            "   fill-color: #3399FF, #3300FF;"+
            "   stroke-mode: none;"+
            "   shadow-mode: gradient-radial;"+
            "   shadow-color: #FFF5, #FFF0;"+
            "   shadow-width: 5px;"+
            "   shadow-offset: 0px, 0px;"+
             "   text-size: 20px; "
             ;
     
     protected String ClusterAnchorNode =
        "   shape: text-rounded-box;" +
        "   fill-color: red;" +
        "   stroke-color: #000000;" +
        "   stroke-width: 5px;" +
        "   shadow-mode: gradient-radial;" +
        "   shadow-width: 5px; " +
        "   size: 15px;" +
        "   z-index: 0;";
     
     protected String ClusterAnchorEdge =
        "   shape: L-square-line;" +
        "   size: 1px; " +
        "   fill-color: #FFF3;  " +
        "   fill-mode: plain; " +
        "   text-color: green;" +
        "   arrow-shape: none;";
     
      protected String KeywordEdge =
        "   text-color: grey; " +
        "   text-size: 15px ;";

}
