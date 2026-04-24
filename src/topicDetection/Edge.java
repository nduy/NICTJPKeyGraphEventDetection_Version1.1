package topicDetection;

import static java.lang.Math.exp;
import static topicDetection.Main.Alpha;

public class Edge {
	public Node n1, n2;
	public String id;
	public int df;
        public int df_incluster=0;
	public double cp1, cp2, cpc1, cpc2;;
	public double betweennessScore;
        public double stscore;

	public Edge(Node n1, Node n2, String id) {
		this.n1 = n1;
		this.n2 = n2;
		this.id = id;
	}

	public Edge(Node n1, Node n2) {
		this(n1, n2, getId(n1, n2));
	}

	public static String getId(Node n1, Node n2) {
		if (n1.keyword.baseForm.compareTo(n2.keyword.baseForm) < 1)
			return n1.keyword.baseForm + "_" + n2.keyword.baseForm;
		else
			return n2.keyword.baseForm + "_" + n1.keyword.baseForm;
	}

	public void computeCPs() {
		cp1 = 1.0 * df / n1.keyword.tf;
		cp2 = 1.0 * df / n2.keyword.tf;
                cpc1 = 1.0 * df_incluster / n1.keyword.tf;
		cpc2 = 1.0 * df_incluster / n2.keyword.tf;
	}

        public void computeSTscore(){
            stscore = df_incluster*1.0/df;
        }
	public Node opposit(Node n) {
		if (n1.keyword.baseForm.equals(n.keyword.baseForm))
			return n2;
		if (n2.keyword.baseForm.equals(n.keyword.baseForm))
			return n1;
		return null;
	}
/*original function
	public int compareBetweenness(Edge e) {
		if (n1.edges.size() < 2 || n2.edges.size() < 2 || betweennessScore < e.betweennessScore )
			return -1;
		if (betweennessScore > e.betweennessScore)
			return 1;
		if (betweennessScore == e.betweennessScore)
			if (df > e.df)
				return -1;
		if (df < e.df)
			return 1;
		return 0;
	}
*/
	public int compareBetweenness(Edge e) {
		
                if (Main.enable_ST_cluster_restrict){
                    if (e.stscore<0){
                        System.out.println(e.stscore + "BUZZZZZZZZZZZZZZZZZZ");
                    }
                    switch (Main.refineOption){
                        case 0:  // CBTWScore*1/STScore
                        //    System.out.println("xx");
                        //    if (stscore == 0.0 && df_incluster!=0 && df!=0) computeSTscore();
                        //    if (e.stscore == 0.0 && e.df_incluster!=0 && e.df!=0) e.computeSTscore();
                        //    System.out.println(df_incluster + " " +  e.df_incluster + " " + stscore + " " + e.stscore);
                            //if (stscore==0) return 1;
                        //    System.out.println("betweennessScore="+ betweennessScore + " after modify= " + betweennessScore*1/stscore);
                        //    System.out.println("e.betweennessScore="+ betweennessScore + " after modify= " + e.betweennessScore*1/stscore);
                            if (n1.edges.size() < 2 || n2.edges.size() < 2 || betweennessScore*1/stscore < e.betweennessScore*1/e.stscore )
                                return -1;
                            if (betweennessScore*1/stscore > e.betweennessScore*1/e.stscore)
                                    return 1;
                            if (betweennessScore*1/stscore == e.betweennessScore*1/e.stscore)
                                    if (df_incluster > e.df_incluster)
                                            return -1;
                            if (df_incluster < e.df_incluster)
                                    return 1;
                            
                            break;
                            
                        case 1: //CBTWScore+e^(1/STScore)
                            System.out.println("yy");
                            if (n1.edges.size() < 2 || n2.edges.size() < 2 || (betweennessScore + exp(1/stscore)) < (e.betweennessScore+ exp(1/e.stscore )))
                                return -1;
                            if ((betweennessScore + exp(1/stscore)) > (e.betweennessScore+ exp(1/e.stscore )))
                                    return 1;
                            if ((betweennessScore + exp(1/stscore)) == (e.betweennessScore+ exp(1/e.stscore )))
                                    if (df_incluster > e.df_incluster)
                                            return -1;
                            if (df_incluster < e.df_incluster)
                                    return 1;
                            
                            break;
                            
                        case 2: // αCBTWScore + (1-α)/STScore
                            System.out.println("zz");
                            if (n1.edges.size() < 2 || n2.edges.size() < 2 || (Alpha*betweennessScore + (1-Alpha)*stscore) < (Alpha*e.betweennessScore + (1-Alpha)*e.stscore))
                                return -1;
                            if ((Alpha*betweennessScore + (1-Alpha)*stscore) > (Alpha*e.betweennessScore + (1-Alpha)*e.stscore))
                                    return 1;
                            if ((Alpha*betweennessScore + (1-Alpha)*stscore) == (Alpha*e.betweennessScore + (1-Alpha)*e.stscore))
                                    if (df_incluster > e.df_incluster)
                                            return -1;
                            if (df_incluster < e.df_incluster)
                                    return 1;
                            
                            break;
                    }
                } else{ // Normal case. with no refining
                    //System.out.println("tt");
                    if (n1.edges.size() < 2 || n2.edges.size() < 2 || betweennessScore < e.betweennessScore )
			return -1;
                    if (betweennessScore > e.betweennessScore)
                            return 1;
                    if (betweennessScore == e.betweennessScore)
                            if (df > e.df)
                                    return -1;
                    if (df < e.df)
                            return 1;
                }
                
                
		return 0;
	}        
        
       
	public int compareBetweenness2(Edge e) {
		double ecp = Math.max(cp1, cp2);
		double cp = Math.max(e.cp1, e.cp2);
                
                double ecpc = Math.max(cpc1, cpc2);
		double cpc = Math.max(e.cpc1, e.cpc2);
                
                if (Main.enable_ST_cluster_restrict){
                   if (cpc < ecpc)
			return -1;
                    if (cpc > ecpc)
                            return 1;
                    if (cpc == ecpc)
                            if (df_incluster > e.df_incluster)
                                    return -1;
                    if (df_incluster < e.df_incluster)
                            return 1; 
                }
		if (cp < ecp)
			return -1;
		if (cp > ecp)
			return 1;
		if (cp == ecp)
			if (df > e.df)
				return -1;
		if (df < e.df)
			return 1;
		return 0;
	}
        
        public String GetID(){
            return id;
        }
}
