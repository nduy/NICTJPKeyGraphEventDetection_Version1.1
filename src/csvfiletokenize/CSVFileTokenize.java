/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csvfiletokenize;

import cmu.arktweetnlp.Tagger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import tweetFilterandClustering.QueryContainer;
import tweetFilterandClustering.STWrapper;
import tweetFilterandClustering.TweetDataRecord;


/**
 *This program aims to tokenize words in tweets and append the tokens at the end of CSV file, with a new field called tokens
 * @author Nguyen Duc-Duy
 */
public class CSVFileTokenize {

    /**
     * @param args <input file>  <output file>  
     * The program aims to TOKENIZE a 
     *          specific TEXT FIELD whose index is found in header line key "observation.what.0.tweet.value"
     *          of the CSV file <input file>
     *          and write to <output file>
     *          The tokenized result are located as a NEW FIELD name TOKEN, added to the right most of each CSV record
     * In tokenized result, we just keep Noun, Proper noun, verb, adjective and adverb. It will also remove stopword, special characters (",',;,_,....) and all number characters
     * 
     * Important note: All record whose tokenized result is EMTY will NOT APPEAR in <output file>
     * 
     * Language supported: English (especially for social network) and Japanese
     */
    
    /** Variables: - stopword list (EN and JP) 
     *             - ASCII charset
     */
    //static final Set<String> stopWordsList = new HashSet<String>(Arrays.asList("これ","それ","あれ","この","その","あの","ここ","そこ","あそこ","こちら","どこ","だれ","なに","なん","何","私","貴方","貴方方","我々","私達","あの人","あのかた","彼女","彼","です","あります","おります","います","は","が","の","に","を","で","え","から","まで","より","も","どの","と","し","それで","しかし",                                           "a","a's","able","about","above","according","accordingly","across","actually","after","afterwards","again","against","ain't","all","allow","allows","almost","alone","along","already","also","although","always","am","among","amongst","an","and","another","any","anybody","anyhow","anyone","anything","anyway","anyways","anywhere","apart","appear","appreciate","appropriate","are","aren't","around","as","aside","ask","asking","associated","at","available","away","awfully","b","be","became","because","become","becomes","becoming","been","before","beforehand","behind","being","believe","below","beside","besides","best","better","between","beyond","both","brief","but","by","c","c'mon","c's","came","can","can't","cannot","cant","cause","causes","certain","certainly","changes","clearly","co","com","come","comes","concerning","consequently","consider","considering","contain","containing","contains","corresponding","could","couldn't","course","currently","d","definitely","described","despite","did","didn't","different","do","does","doesn't","doing","don't","done","down","downwards","during","e","each","edu","eg","eight","either","else","elsewhere","enough","entirely","especially","et","etc","even","ever","every","everybody","everyone","everything","everywhere","ex","exactly","example","except","f","far","few","fifth","first","five","followed","following","follows","for","former","formerly","forth","four","from","further","furthermore","g","get","gets","getting","given","gives","go","goes","going","gone","got","gotten","greetings","h","had","hadn't","happens","hardly","has","hasn't","have","haven't","having","he","he's","hello","help","hence","her","here","here's","hereafter","hereby","herein","hereupon","hers","herself","hi","him","himself","his","hither","hopefully","how","howbeit","however","i","i'd","i'll","i'm","i've","ie","if","ignored","immediate","in","inasmuch","inc","indeed","indicate","indicated","indicates","inner","insofar","instead","into","inward","is","isn't","it","it'd","it'll","it's","its","itself","j","just","k","keep","keeps","kept","know","knows","known","l","last","lately","later","latter","latterly","least","less","lest","let","let's","like","liked","likely","little","look","looking","looks","ltd","m","mainly","many","may","maybe","me","mean","meanwhile","merely","might","more","moreover","most","mostly","much","must","my","myself","n","name","namely","nd","near","nearly","necessary","need","needs","neither","never","nevertheless","new","next","nine","no","nobody","non","none","noone","nor","normally","not","nothing","novel","now","nowhere","o","obviously","of","off","often","oh","ok","okay","old","on","once","one","ones","only","onto","or","other","others","otherwise","ought","our","ours","ourselves","out","outside","over","overall","own","p","particular","particularly","per","perhaps","placed","please","plus","possible","presumably","probably","provides","q","que","quite","qv","r","rather","rd","re","really","reasonably","regarding","regardless","regards","relatively","respectively","right","s","said","same","saw","say","saying","says","second","secondly","see","seeing","seem","seemed","seeming","seems","seen","self","selves","sensible","sent","serious","seriously","seven","several","shall","she","should","shouldn't","since","six","so","some","somebody","somehow","someone","something","sometime","sometimes","somewhat","somewhere","soon","sorry","specified","specify","specifying","still","sub","such","sup","sure","t","t's","take","taken","tell","tends","th","than","thank","thanks","thanx","that","that's","thats","the","their","theirs","them","themselves","then","thence","there","there's","thereafter","thereby","therefore","therein","theres","thereupon","these","they","they'd","they'll","they're","they've","think","third","this","thorough","thoroughly","those","though","three","through","throughout","thru","thus","to","together","too","took","toward","towards","tried","tries","truly","try","trying","twice","two","u","un","under","unfortunately","unless","unlikely","until","unto","up","upon","us","use","used","useful","uses","using","usually","uucp","v","value","various","very","via","viz","vs","w","want","wants","was","wasn't","way","we","we'd","we'll","we're","we've","welcome","well","went","were","weren't","what","what's","whatever","when","whence","whenever","where","where's","whereafter","whereas","whereby","wherein","whereupon","wherever","whether","which","while","whither","who","who's","whoever","whole","whom","whose","why","will","willing","wish","with","within","without","won't","wonder","would","would","wouldn't","x","y","yes","yet","you","you'd","you'll","you're","you've","your","yours","yourself","yourselves","z","zero"));
    
    // Encoder to identify ASCII/Non ASCII words.
    static final CharsetEncoder asciiEncoder =     Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1
    
    /// TWEET FILTERING DECLARATIONS
    public static boolean enable_location_restriction = true;
    public static boolean enable_time_restriction = true;
    public static boolean enable_keyword_restriction = false;
    
    /// TWEET TOKENIZING DECLARATIONS
    //Variable to Enable/diable toikenzing  Japanese/English.
    // For example, if tokenize_Japanese==false then all tweet contains non-ASCII characters will be ignored
    public static boolean tokenize_Japanese = true;
    public static boolean tokenize_English = true;
    public static boolean keep_Noun = true;
    public static boolean keep_Verb = false;
    public static boolean keep_ProperNoun= false;
    public static QueryContainer query = new QueryContainer();
    
    public static void main(String[] args) throws UnsupportedEncodingException, IOException {
        // TODO code application logic here
        if (args.length!= 2){
            System.out.println("Invalid parameters");
        }
        
        int text_index = 3; // Default value
        
        File fIn = new File(args[0]);
        if (!fIn.exists()) {
            System.out.println("Input file does not exist!");
        }
        
        File fOut = new File(args[1]);
        
        BufferedReader bufferedReader = new   BufferedReader(new InputStreamReader(new FileInputStream(fIn), "UTF-8"));
        BufferedWriter bufferedWriter = new   BufferedWriter(new OutputStreamWriter(new FileOutputStream(fOut), "UTF-8"));
        String rline;
        int line_no = 0;
        while ((rline = bufferedReader.readLine()) != null){
            
            // skip the header line    
            if (line_no == 0) {
                bufferedWriter.append(rline + ",tokens\n");
               // find  text_index
               text_index = rline.contains("observation.what.0.tweet.value") ? StringUtils.countMatches(rline.substring(0, rline.indexOf("observation.what.0.tweet.value")),",") : 3;
               line_no++;
                continue;
            } 
            
             if (line_no%200000 == 0) {
                System.out.println("Proccessed " + line_no + " line(s).");
            }
            line_no++;
            
             Reader in = new StringReader(rline);
            CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT); 
            List<CSVRecord> list=null;
            try {
           // List<CSVRecord> list = parser.getRecords(); 
                list = parser.getRecords(); 
            }
            catch (Exception e){
                System.out.println(rline);
            }
            
            
            // Creat a structure to save the tweet content
            TweetDataRecord rec = new TweetDataRecord();
            // Check Time and Location information. If the tweet was upload inside the polygon and insize time window, then we go to next steps. Otherwise, discard it.
            if (!topicDetection.Main.discardST){
                
                rec.ImportFromCSV(list.get(0));
                 if (InformationIntegrityCheck(rec)) {}  else continue;                                              // All the information is available check
                 
                 if (enable_time_restriction && !InDateRangeCheck(rec,query)) continue;                              // Time is within the interested time range check
                 
                 if (enable_location_restriction && !query.observedArea.isPointInside(rec.observation_where_coordinates)) continue;   // Inside the interested region check
                 
                // !!!  Keyword restriction will be in tokenization step 
                
            }
                        
            // Start Tokinization
            String str = list.get(0).get(text_index);
            str = str.replace("("," ");
            str = str.replace(")"," ");
            str= removeUrl(str);
            str = str.replaceAll("(?!\"\')\\p{Punct}", "");
            str = str.replaceAll("[0-9]", "");
            if (str.replace(" ","").equals("")) continue;
            
            // A list to store all tokens
            List<String> words = new LinkedList<>();
                       
            if (isPureAscii(str)){ // Use TweetNLP
                if (!tokenize_English) continue; // skip if we don't care English
                Tagger tagger = new Tagger();
                tagger.loadModel("model.20120919");
//                System.out.println(str);
                if (str.equals("")) continue;
                List<Tagger.TaggedToken> taggedTokens;
                taggedTokens= tagger.tokenizeAndTag(str);
                for (Tagger.TaggedToken token : taggedTokens) {        
//                         if (isStopWord(token.token)) continue; // stopword checking will be in KeygraphBuildingStep
                    if (token.tag.equals("N") && keep_Noun) { // Noun
                        words.add(token.token.replace(" ", ""));
                        continue;
                    }
                    
                    if (token.tag.equals("V") && keep_Verb) { // Verb
                        words.add(token.token.replace(" ", ""));
                        continue;
                    }
                    
                    if (token.tag.equals("^") && keep_ProperNoun) { // ProperNoun
                        words.add(token.token.replace(" ", ""));
                    }
                }
           
            }
            else { // Use Japanese tagger
                if (!tokenize_Japanese) continue; // skip if we don't care Japanese
                Tokenizer tokenizer = Tokenizer.builder().build();
                for (Token token : tokenizer.tokenize(str)) {
    //                System.out.println(token.getSurfaceForm() + "\t" + token.getAllFeatures());
                    String baseForm = token.getBaseForm();
                    if (baseForm == null || isPureAscii(baseForm)) continue; // Skip the pure English words
                    String pos = token.getPartOfSpeech();
    //                if (isStopWord(surfaceForm) || isStopWord(baseForm)) continue; // Skip punctuations and stopwords
                    if (( pos.contains("名詞") && keep_Noun)
                            || (pos.contains("動詞") && keep_Verb)
                            || (pos.contains("固有名詞") && keep_ProperNoun)
                        ){
                        if (!keep_ProperNoun){
                            if ( pos.contains("地域") // Tag related to Proper Noun. Regularly overlaped in 名詞
                                || pos.contains("人名") || pos.contains("接尾")
                                || pos.contains("助動詞")|| pos.contains("接頭詞")
                                || pos.contains("名詞接続")|| pos.contains("数")
                                || pos.contains("副詞可能")|| pos.contains("自立")
                            )  continue;
                        }
                        words.add(baseForm);
                        
                    }
                }
            }  
            
            // Now check if the tweets contain interested words -->> Keyword Restriction here
                if (enable_keyword_restriction){
                    boolean containKeyWord = false;
                    for (String keyword : query.getKeywords()){
                        if (words.contains(keyword)) {
                            containKeyWord= true;
                            break;
                        }
                    }
                    if (!containKeyWord) continue; 
                }
            
            // Skip records that have no token
            if (words.isEmpty()) continue;
            
            // Append to the end of CSV line, and wrtie to file
            String tmp ="";
            for (String w: words){
                tmp = tmp + w+ " ";
            }
            if (tmp.endsWith(" ")) tmp = tmp.substring(0,tmp.length()-1);   
            if (tmp.equals("")) continue;
            bufferedWriter.append(rline + ",\"" + tmp + "\"\n");
            
            // Get temporal and spartial info fo future clustering
            topicDetection.Main.clusterInput.add(new STWrapper(rec));
            
           
        }
        System.out.println("Proccessed " + line_no + " line(s).");
        bufferedWriter.close();
        
    }
    
    ///////////////////////////////////////////////////////
    /**
     * Check if a string is a stopword
     * @param target string
     * @return if the string is stopword or not
     * 
     */
    /*    public static boolean isStopWord(String str){
        if (stopWordsList.contains(str)) return true;
        return false;
    }
    */
    
    ///////////////////////////////////////////////////////
    /**
     * Check if a string contain ASCII characters only
     * @param target string
     * @return true if the string is purely ASCII
     * 
     */
         public static boolean isPureAscii(String v) {
        return asciiEncoder.canEncode(v);
    }
         
    ///////////////////////////////////////////////////////
    /**
     * Return a string after cleaning URLS
     * @param target string
     * @return cleaned string
     * 
     */     
         public static  String removeUrl(String commentstr)
    {
        try{
            if (commentstr == null) return "";
            String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
            Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(commentstr);
            int i = 0;
            while (m.find()) {
                commentstr = commentstr.replaceAll(m.group(i),"").trim();
                i++;
            }
            } catch (Exception e){ e.printStackTrace();}
        
        return commentstr;
    }
    
        /////////////////////////////////////////////////////////////////////////////////////
        
    public static boolean InformationIntegrityCheck(TweetDataRecord tw){
        if (    tw._id.isEmpty()
                || tw.observation_when_time== null
                || tw.observation_where_coordinates== null
                || tw.observation_what_0_tweet_value.isEmpty()
            ) 
            {
                System.out.println("\n Unable to analyze a tweet due to its intergrity.");
                return false;
            }
        else return true;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////
        
    public static boolean InDateRangeCheck(TweetDataRecord tw, QueryContainer qu){
        if (tw.observation_when_time.isBefore(qu.startDate) || tw.observation_when_time.isAfter(qu.endDate)) {
        //    System.out.println("\n Unable to analyze a tweet due out of time range.");
            return false;
        }
        return true;
    }

}
