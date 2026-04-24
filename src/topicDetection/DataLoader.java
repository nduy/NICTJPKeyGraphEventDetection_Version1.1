package topicDetection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import dataset.twitter.StringDuplicate;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;

public class DataLoader {

	protected Constants constants;

	public DataLoader() {
	}

	public DataLoader(Constants constants) {
		this.constants = constants;
	}

	public InputStreamReader openDataInputStream(String fileName) throws Exception {
		return new InputStreamReader(new FileInputStream(fileName),"UTF-8");
	}

	public boolean exists(String f) throws Exception {
		return new File(f).exists();
	}

	public String[] list(String fname) throws Exception {
		return new File(fname).list();
	}

	public void loadDocumentsForSpinn3r(String[] files, HashMap<String, Document> docs, HashSet<String> stopwords, HashMap<String, Double> DF)
			throws Exception {
		for (int i = 0; i < files.length; i++) {
			if (i % 1000 == 0)
				System.out.println(i);
			// System.out.println(constants.DATA_KEY_NE_PATH + files[i]);
			String ff = (constants.DATA_KEYWORDS_1_PATH + files[i]);// +
			// ".txt");
			Document d = new Document(files[i]);
			loadDocumentKeyFile(openDataInputStream(ff), stopwords, d, constants.KEYWORDS_1_WEIGHT);
			if (constants.KEYWORDS_2_ENABLE) {
				ff = (constants.DATA_KEYWORDS_2_PATH + files[i]);// +
				// ".txt");
				loadDocumentKeyFile(openDataInputStream(ff), stopwords, d, constants.KEYWORDS_2_WEIGHT);
			}
			if (d.keywords.size() >= constants.DOC_KEYWORDS_SIZE_MIN) {
				docs.put(d.id, d);
				for (Keyword k : d.keywords.values()) {
					if (DF.containsKey(k.baseForm))
						DF.put(k.baseForm, DF.get(k.baseForm) + 1);
					else
						DF.put(k.baseForm, new Double(1));
				}
			}
		}
	}

	public void loadDocuments(String inputFileName, HashMap<String, Document> docs, HashSet<String> stopwords, HashMap<String, Double> DF,
			boolean removeDuplicates) throws Exception {
		File inputFile = new File(inputFileName);
		StringDuplicate sd = new StringDuplicate();
		if (inputFile.isDirectory()) {
			int i = 0;
			for (String file : inputFile.list())
				try {
					file = inputFileName + "/" + file;
					if (i++ % 1000 == 0)
						System.out.println(i + " documnets are loaded.");
					String id = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
					Document d = docs.containsKey(id) ? docs.get(id) : new Document(id);
					// d.setTitle(line.split(",")[0]);
					if (file.endsWith(".keywords"))
						loadDocumentKeyFile(openDataInputStream(file), stopwords, d, constants.KEYWORDS_1_WEIGHT);
					else
						// if (file.endsWith(".txt"))
						loadDocumentTextFile(openDataInputStream(file), stopwords, d, constants.TEXT_WEIGHT, removeDuplicates, sd);
					docs.put(id, d);
				} catch (Exception e) {
					e.printStackTrace();
				}
		} else {
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line = null;
			int i = 0;
			while ((line = in.readLine()) != null)
				try {
					String[] tokens = line.split("\t", 2);
					if (i++ % 1000 == 0)
						System.out.println(i + " documnets are loaded.");
					String id = tokens[0];
					// id = i + "";
					Document d = docs.containsKey(id) ? docs.get(id) : new Document(id);
					// d.setTitle(line.split(",")[0]);
					// if (file.endsWith(".keywords"))
					// loadDocumentKeyFile(openDataInputStream(file), stopwords,
					// porter, d, constants.KEYWORDS_1_WEIGHT);
					// if (file.endsWith(".txt"))
//					loadDocumentTextFile(new DataInputStream(new ByteArrayInputStream(tokens[1].getBytes("UTF-8"))), stopwords, d,
//							constants.TEXT_WEIGHT, removeDuplicates, sd);
                                        
                                        ByteArrayInputStream stream = new ByteArrayInputStream(tokens[1].getBytes(StandardCharsets.UTF_8));
                                        loadDocumentTextFile(new InputStreamReader(stream), stopwords, d, constants.TEXT_WEIGHT, removeDuplicates, sd);
                

					docs.put(id, d);
				} catch (Exception e) {
					e.printStackTrace();
				}
			in.close();
		}

		System.out.println(docs.size() + " documents are loaded.");
		ArrayList<String> toRemove = new ArrayList<String>();
		for (Document d : docs.values())
			if (d.keywords.size() >= constants.DOC_KEYWORDS_SIZE_MIN) {
				if (!removeDuplicates || !d.isDuplicate) {
					for (Keyword k : d.keywords.values())
						if (DF.containsKey(k.baseForm))
							DF.put(k.baseForm, DF.get(k.baseForm) + 1);
						else
							DF.put(k.baseForm, new Double(1));

				}
			} else
				toRemove.add(d.id);

		for (String id : toRemove)
			docs.remove(id);

		System.out.println(docs.size() + " documents remaind after filterig small documents (Documents that have less than " + constants.DOC_KEYWORDS_SIZE_MIN
				+ " keywords).");

	}
        
        public void loadDocumentsCSVfile(String inputFileName, HashMap<String, Document> docs, HashSet<String> stopwords, HashMap<String, Double> DF, 
                boolean removeDuplicates) throws Exception {
            	//Random ID set
                long id_new = 0;
		File inputFile = new File(inputFileName);
		StringDuplicate sd = new StringDuplicate();
                BufferedReader in = new   BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
                String line = null;
                int i = 0;
                int idIndex=0;
                int tokensIndex = 4;
                while ((line = in.readLine()) != null)
                    if (i==0) //First line
                    {
                       idIndex = line.contains("_id") ? StringUtils.countMatches(line.substring(0, line.indexOf("_id")),",") : 0;
                       tokensIndex = line.contains("tokens") ? StringUtils.countMatches(line.substring(0, line.indexOf("tokens")),",") : 4;
                       i++;
                    }
                else
                try {
                    Reader inp = new StringReader(line);
                    CSVParser parser = new CSVParser(inp, CSVFormat.DEFAULT);
                    List<CSVRecord> list = parser.getRecords(); 
                    String[] tokens = { list.get(0).get(idIndex) , list.get(0).get(tokensIndex)};
                    if (i++ % 1000 == 0)
                            System.out.println(i + " documnets are loaded.");
                    String id = tokens[0];
                    if (id.equals("")) id = id_new+++"";
                    // id = i + "";
                    Document d = docs.containsKey(id) ? docs.get(id) : new Document(id);
                    // d.setTitle(line.split(",")[0]);
                    // if (file.endsWith(".keywords"))
                    // loadDocumentKeyFile(openDataInputStream(file), stopwords,
                    // porter, d, constants.KEYWORDS_1_WEIGHT);
                    // if (file.endsWith(".txt"))
/*                    loadDocumentTextFile(new DataInputStream(new ByteArrayInputStream(tokens[1].getBytes("UTF-8"))), stopwords, porter, d,
                                    constants.TEXT_WEIGHT, removeDuplicates, sd);
 */
                    
  //                  InputStream bais = new ByteArrayInputStream(tokens[1].getBytes("UTF-8"));
                    
                    ByteArrayInputStream stream = new ByteArrayInputStream(tokens[1].getBytes(StandardCharsets.UTF_8));
 //                   InputStream inps = IOUtils.toInputStream(tokens[1], "UTF-8");
                    
          //          DataInputStream dis =  new DataInputStream(stream);
                     loadDocumentTextFile(new InputStreamReader(stream), stopwords, d, constants.TEXT_WEIGHT, removeDuplicates, sd);
                
                        docs.put(id, d);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                in.close();
		
                
		System.out.println(docs.size() + " documents are loaded.");
		ArrayList<String> toRemove = new ArrayList<String>();
		for (Document d : docs.values())
                {
//                    System.out.println(d.keywords);
                    
                    if (d.keywords.size() >= constants.DOC_KEYWORDS_SIZE_MIN) {
                       
                        if (!removeDuplicates || !d.isDuplicate) {
                            for (Keyword k : d.keywords.values()){
                                if (DF.containsKey(k.baseForm))
                                            DF.put(k.baseForm, DF.get(k.baseForm) + 1);
                                    else
                                            DF.put(k.baseForm, new Double(1));
                            }
                        }   
                        
                        
				
			} else{
    //                    System.out.println(d.id + "  " + d.keywords.size());
                          //  boolean privilege = false; // bias to keep tweets contain prefered words
                          //  for (String str: GraphAnalyze.preferredWords ) 
                         //       if (d.keywords.keySet().contains(str)) { privilege = true; break;} 
                        //    if (!privilege) 
                                toRemove.add(d.id);
                            }
                        //   
				
                }
			

		for (String id : toRemove)
			docs.remove(id);

		System.out.println(docs.size() + " documents remaind after filterig small documents (Documents that have less than " + constants.DOC_KEYWORDS_SIZE_MIN
				+ " keywords).");

	}

/*	public void loadDocumentsOLD(String inputFileName, HashMap<String, Document> docs, HashSet<String> stopwords, HashMap<String, Double> DF, Porter porter,
			boolean removeDuplicates) throws Exception {
		File inputFile = new File(inputFileName);
		StringDuplicate sd = new StringDuplicate();
		if (inputFile.isDirectory()) {
			int i = 0;
			for (String file : inputFile.list())
				try {
					file = inputFileName + "/" + file;
					if (i++ % 1000 == 0)
						System.out.println(i + " documents are loaded.");
					String id = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
					Document d = docs.containsKey(id) ? docs.get(id) : new Document(id);
					// d.setTitle(line.split(",")[0]);
					if (file.endsWith(".keywords"))
						loadDocumentKeyFile(openDataInputStream(file), stopwords,  d, constants.KEYWORDS_1_WEIGHT);
					else
						// if (file.endsWith(".txt"))
						loadDocumentTextFile(openDataInputStream(file), stopwords, d, constants.TEXT_WEIGHT, removeDuplicates, sd);
					docs.put(id, d);
				} catch (Exception e) {
					e.printStackTrace();
				}
		} else {
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line = null;
			int i = 0;
			while ((line = in.readLine()) != null)
				try {
					String file = line.split(",")[1];
					if (i++ % 1000 == 0)
						System.out.println(i + " documents are loaded.");
					String id = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
					Document d = docs.containsKey(id) ? docs.get(id) : new Document(id);
					// d.setTitle(line.split(",")[0]);
					if (file.endsWith(".keywords"))
						loadDocumentKeyFile(openDataInputStream(file), stopwords, d, constants.KEYWORDS_1_WEIGHT);

					if (file.endsWith(".txt"))
						loadDocumentTextFile(openDataInputStream(file), stopwords, d, constants.TEXT_WEIGHT, removeDuplicates, sd);

					docs.put(id, d);
				} catch (Exception e) {
					e.printStackTrace();
				}
			in.close();
		}

		System.out.println(docs.size() + " documents are loaded.");
		ArrayList<String> toRemove = new ArrayList<String>();
		for (Document d : docs.values())
			if (d.keywords.size() >= constants.DOC_KEYWORDS_SIZE_MIN) {
				if (!removeDuplicates || !d.isDuplicate) {
					for (Keyword k : d.keywords.values())
						if (DF.containsKey(k.baseForm))
							DF.put(k.baseForm, DF.get(k.baseForm) + 1);
						else
							DF.put(k.baseForm, new Double(1));
				}
			} else
				toRemove.add(d.id);

		for (String id : toRemove)
			docs.remove(id);

		System.out.println(docs.size() + "documents remaind after filterig small documents (Documents that have less than " + constants.DOC_KEYWORDS_SIZE_MIN
				+ " keywords).");

	}
*/
	public void loadDocumentKeyFile(InputStreamReader in, HashSet<String> stopwords, Document d, double BoostRate) throws UnsupportedEncodingException {
		// if (Constants.BREAK_NP)
		// fetchDocumentNEAndNPFileWithBreaking(f, stopwords, porter, d);
		// System.out.println("injaaaaaaaaaaaaaaaaa:"+d.id);
		BufferedReader bf = new   BufferedReader(in);
                 try {
			// DataInputStream in = openDataInputStream(f);
			String line = null;
			while ((line = bf.readLine()) != null && line.length() > 2) {
				// System.out.println(line);
				int index = line.lastIndexOf("==");
				String word = line.substring(0, index);
				double tf = Integer.parseInt(line.substring(index + 2)) * BoostRate;
				String base = getBaseForm(stopwords, word);
				if (base.length() > 2)
					if (!d.keywords.containsKey(base))
						d.keywords.put(base, new Keyword(base, word, tf, 1, 0));
					else
						d.keywords.get(base).tf += tf;

			}
			bf.close();
			// System.out.println("done::"+d.keywords.size());
		} catch (Exception e) {
			// System.out.println(f.getName());
			e.printStackTrace();
		}

	}

	public void loadDocumentKeyFile(ArrayList<String> words, HashSet<String> stopwords, Porter porter, Document d, double BoostRate) {
		// if (Constants.BREAK_NP)
		// fetchDocumentNEAndNPFileWithBreaking(f, stopwords, porter, d);
		// System.out.println("injaaaaaaaaaaaaaaaaa:"+d.id);
		try {
			// DataInputStream in = openDataInputStream(f);
			for (String word : words)
				if (!stopwords.contains(word) && word.length() > 2) {
					String base = getBaseForm(stopwords, word);
					if (base.length() > 2)
						if (!d.keywords.containsKey(base))
							d.keywords.put(base, new Keyword(base, word, 1, 1, 0));
						else
							d.keywords.get(base).tf += 1;

				}
			// System.out.println("done::"+d.keywords.size());
		} catch (Exception e) {
			// System.out.println(f.getName());
			e.printStackTrace();
		}

	}

	public static String getBaseForm(HashSet<String> stopwords, String word) {
		String base = "";
		StringTokenizer stt = new StringTokenizer(word, "!' -_@0123456789.");
		// System.out.println(stt.countTokens()+"::"+stopwords+"::"+porter);
		while (stt.hasMoreTokens()) {
			String token = stt.nextToken().toLowerCase();
			if ((token.indexOf("?") == -1 && token.length() > 2 && !stopwords.contains(token)))
				base += token + " ";
		}
		return base.trim();
	}

	public void loadDocumentTextFile(InputStreamReader ist, HashSet<String> stopwords,  Document d, double BoostRate, boolean removeDuplicates,
			StringDuplicate sd) {
		try {
                         BufferedReader br = new BufferedReader(ist);
			String content = "";
			String line = "";
			while ((line = br.readLine()) != null)
				content += line;

			if (removeDuplicates)
				d.isDuplicate = sd.isDuplicate(content);

//			StringTokenizer st = new StringTokenizer(content, "!?|\"' -_@0123456789.,;#$&%/\\*()<>\t");
                        String[] words = content.split(" ");
			//while (st.hasMoreTokens()) {
                        for (String word: words){
//				String word = st.nextToken();
				//String token = word;
				double tf = 1 * BoostRate;
				String base = "";
				if ((word.indexOf("?") == -1 && word.length() > 0 && !stopwords.contains(word)))
					base = word;

				if (base.length() > 0)
					if (!d.keywords.containsKey(base))
						d.keywords.put(base, new Keyword(base, word, tf, 1, 0));
					else
						d.keywords.get(base).tf += tf;

			}
			br.close();
			// System.out.println("done::"+d.keywords.size());
		} catch (Exception e) {
			// System.out.println(f.getName());
			e.printStackTrace();
		}

	}

	public boolean hasDigit(String in) {
		for (int i = 0; i < in.length(); i++)
			if (Character.isDigit(in.charAt(i)))
				return true;
		return false;
	}
}
