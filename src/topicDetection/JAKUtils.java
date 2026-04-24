package topicDetection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;

public class JAKUtils {

  final static String GOOGLE_CHART_API_URL = "http://chart.apis.google.com/chart?";

  /**
   * genereate a 3D chart from google chart api the first element is normal 3D chart the second element is a preview
   * icon chart without labels and title
   * 
   * @param width
   *          of the chart
   * @param heigh
   *          of the chart
   * @param data
   *          for the chart
   * @param labels
   *          for each data element, if labels is null the data values will be used
   * @param title
   *          for the chart
   * @return google api chart url
   */
  public static String[] generate3dPieChart(int width, int height, int data[], String labels[], String title) {
    String icon;
    // set size and encoding type
    String result = "chs=" + width + "x" + height + "&chd=t:";
    int sum = 0;
    // set data values
    for (int val : data) {
      result += val + ",";
      sum += val;
    }
    result += (100 - sum);
    // icon has a normal pie chart, the descripton has a 3D pie chart
    icon = result;
    result += "&cht=p3";
    icon += "&cht=p";
    // use labels if not null
    if (labels != null) {
      result += "&chl=" + join(labels, "|");
    } else {
      // otherwise use data values as labels
      labels = new String[data.length];
      for (int i = 0; i < data.length; i++) {
        labels[i] = data[i] + "%";
      }
      result += "&chl=" + join(labels, "|");
    }
    if (title != null)
      result += "&chtt=" + title;
    return new String[] { GOOGLE_CHART_API_URL + result, GOOGLE_CHART_API_URL + icon + "&chf=bg,s,ffffff00" };
  }

  /**
   * @see Example1#join(Collection, String)
   * @param labels
   * @param delimiter
   * @return
   */
  public static String join(int[] labels, String delimiter) {
    String[] str = new String[labels.length];
    for (int i = 0; i < labels.length; i++) {
      str[i] = Integer.valueOf(labels[i]).toString();
    }
    return join(Arrays.asList(str), delimiter);
  }

  /**
   * @see Example1#join(Collection, String)
   * @param labels
   * @param delimiter
   * @return
   */
  public static String join(String[] labels, String delimiter) {
    return join(Arrays.asList(labels), delimiter);
  }

  /**
   * Join a collection of strings with a delimiter into a string
   * 
   * @param s
   * @param delimiter
   * @return
   */
  public static String join(Collection<String> s, String delimiter) {
    StringBuffer buffer = new StringBuffer();
    Iterator<String> iter = s.iterator();
    while (iter.hasNext()) {
      buffer.append(iter.next());
      if (iter.hasNext()) {
        buffer.append(delimiter);
      }
    }
    return buffer.toString();
  }

  /**
   * Generate a random color in the hex format
   * 
   * @return random color
   */
  public static String getRandomColor() {
    Random aRandom = new Random(System.nanoTime());
    String color = "";
    for (int j = 0; j < 3; j++) {
      int z = aRandom.nextInt(256);
      String val = Integer.toHexString(z).toUpperCase();
      color += val.length() == 1 ? "0" + val : val; // add leading zero
    }
    return color;
  }

  /**
   * Convert three int values into a hex string
   * 
   * @param r
   *          red
   * @param g
   *          green
   * @param b
   *          blue
   * @param inserverOrder
   *          set true to use inverse order of RGB => BGR
   * @return RGB string in hex format
   */
  public static String getHexColor(int r, int g, int b, boolean inverseOrder) {
    String red, green, blue;
    String val = Integer.toHexString(r).toUpperCase();
    red = val.length() == 1 ? "0" + val : val; // add leading zero
    val = Integer.toHexString(g).toUpperCase();
    green = val.length() == 1 ? "0" + val : val; // add leading zero
    val = Integer.toHexString(b).toUpperCase();
    blue = val.length() == 1 ? "0" + val : val; // add leading zero
    if (!inverseOrder) {
      return blue + green + red;
    } else {
      return red + green + blue;
    }
  }

  /**
   * Read a CSV file and put each line into the list. Each item have a arrays of strings (for each column)
   * 
   * @param fileWithPath
   *          path to the CSV file
   * @param delimiter
   *          , usually comma
   * @return list of arrays and array of strings
   */
  public static List<String[]> loadCsvFile(String fileWithPath, String delimiter) {
    // read CSV file
    Vector<String[]> list = new Vector<String[]>();
    if (delimiter == null || delimiter.equals("")) {
      delimiter = ",";
    }
    BufferedReader reader = null;
    String readString;
    try {
      // open file
      reader = new BufferedReader(new FileReader(new File(fileWithPath)));
      while ((readString = reader.readLine()) != null) {
        list.add(readString.split(delimiter));
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * Convert the data of a CVS file into a Hashmap hashmap keys => codeColumn hashmap values => dataColumn Example:
   * hashmap.get("AFG") returns the data of Afghanistan
   * 
   * @param fileWithPath
   *          path to the CSV file
   * @param codeColumn
   *          index of the column for the area or country code
   * @param dataColumn
   *          index of the column for the data (double value)
   * @return Hashmap filled with data from the CSV
   */
  public static HashMap<String, Double> readCSVDoubleData(String fileWithPath, int codeColumn, int dataColumn) {

    List<String[]> stringVector = loadCsvFile(fileWithPath, ",\"");
    int size = stringVector.size(); // size of rows in the CSV file
    HashMap<String, Double> dataMap = new HashMap<String, Double>();
    double max = Double.NEGATIVE_INFINITY;
    double min = Double.POSITIVE_INFINITY;
    String regex = "\"";
    String replacement = "";
    String code, data;

    // ignore header, start with index 1
    for (int i = 1; i < size; i++) {
      String[] row = stringVector.get(i); // get row with index i: array of strings (for each column)
      code = row[codeColumn].replaceAll(regex, replacement).trim();
      data = row[dataColumn].replaceAll(regex, replacement).trim();
      // save minimum and maximum
      max = Math.max(new Double(data), max);
      min = Math.min(new Double(data), min);
      dataMap.put(code, new Double(data));
    }
    // save min and max to HashMap
    dataMap.put("minimum", min);
    dataMap.put("maximum", max);
    return dataMap;
  }

  /**
   * Convert the data of a CVS file into a nested Hashmap hashmap keys => codeColumn hashmap values => nested Hashmap
   * nested keys => => multiColumn nested values => dataColumn Example: hashmap.get("AFG").get("2009") returns the data
   * of Afghanistan in year 2009
   * 
   * @param fileWithPath
   *          path to the CSV file
   * @param codeColumn
   *          index of the column for the area or country code
   * @param dataColumn
   *          index of the column for the data (double value)
   * @param multiColumn
   *          index of the column for the year
   * @return Hashmap filled with data from the CSV
   * 
   *         Hashmap contains keys from the dataColum, values as (nested Hashmap) nested Hashmap contains keys from the
   *         multiColumn and values from the dataColumn
   */
  public static HashMap<String, HashMap<String, Double>> readMultiDataFromCVS(String fileWithPath, int codeColumn,
      int dataColumn, int multiColumn) {

    // read CSV file
    List<String[]> stringVector = loadCsvFile(fileWithPath, ",\"");
    int size = stringVector.size(); // size of rows in the CSV file
    HashMap<String, HashMap<String, Double>> dataMap = new HashMap<String, HashMap<String, Double>>();
    double max = Double.NEGATIVE_INFINITY;
    double min = Double.POSITIVE_INFINITY;
    String regex = "\"";
    String replacement = "";
    String code, data, multiData;

    // ignore header, start with index 1
    for (int i = 1; i < size; i++) {
      String[] row = stringVector.get(i); // get row with index i: array of strings (for each column)
      code = row[codeColumn].replaceAll(regex, replacement).trim();
      data = row[dataColumn].replaceAll(regex, replacement).trim();
      multiData = row[multiColumn].replaceAll(regex, replacement).trim();

      if (!dataMap.containsKey(code)) {
        HashMap<String, Double> tmp = new HashMap<String, Double>();
        dataMap.put(code, tmp);
      }
      HashMap<String, Double> nested = dataMap.get(code);
      double value = new Double(data);
      nested.put(multiData, new Double(value));

      // save minimum and maximum
      max = Math.max(value, max);
      min = Math.min(value, min);
    }

    // save min and max to HashMap
    HashMap<String, Double> minmax = new HashMap<String, Double>();
    minmax.put("minimum", min);
    minmax.put("maximum", max);
    dataMap.put("info", minmax);
    return dataMap;
  }

  /**
   * @param placemark
   *          change the Style of this placemark
   * @param color
   *          color format: hex value, default: white
   * @param polyMode
   *          colormode of the polygon
   * @param width
   *          width of the LineStyle, default: 0
   * @param lineColor
   *          alpha and color value in hex format, default: black
   * @param lineMode
   *          colormode of the line/border
   */
  public static void setPolyStyleAndLineStyle(Placemark placemark, String color, ColorMode polyMode, double width,
      String lineColor, ColorMode lineMode) {
    if (color == null || color.length() != 8) {
      color = "FFFFFFFF";
      polyMode = ColorMode.NORMAL;
    }

    if (width <= 0) {
      width = 0;
    }
    if (lineColor == null || lineColor.length() != 8) {
      lineColor = "FF000000";
      lineMode = ColorMode.NORMAL;
    }
    List<StyleSelector> styleSelector = placemark.getStyleSelector();
    if (styleSelector.isEmpty()) {
      Style style = new Style();
      style.createAndSetPolyStyle();
      styleSelector.add(style);
    }
    Iterator<StyleSelector> iterator = styleSelector.iterator();
    Style style = null;
    while (iterator.hasNext()) {
      StyleSelector tmp = iterator.next();
      if (tmp instanceof Style) {
        style = (Style) tmp;
        style.getPolyStyle().withColor(color).withColorMode(polyMode);
        style.createAndSetLineStyle().withWidth(width).withColor(lineColor).withColorMode(lineMode);
      }
    }
  }

  public static void setLineStyle(Placemark placemark, String color, ColorMode polyMode, double width,
      String lineColor, ColorMode lineMode) {
   if (color == null || color.length() != 8) {
      color = "FF000001";

    }

    if (width <= 0) {
      width = 0;
    }
    if (lineColor == null || lineColor.length() != 8) {
      lineColor = "FF000000";
      lineMode = ColorMode.RANDOM;
    }
    List<StyleSelector> styleSelector = placemark.getStyleSelector();
    if (styleSelector.isEmpty()) {
      Style style = new Style();
      styleSelector.add(style);
    }
    Iterator<StyleSelector> iterator = styleSelector.iterator();
    Style style = null;
    while (iterator.hasNext()) {
      StyleSelector tmp = iterator.next();
      if (tmp instanceof Style) {
        style = (Style) tmp;
        style.createAndSetLineStyle().withWidth(width).withColor("FF00FF").withColorMode(lineMode);
      }
    }
  }
  
  /**
   * set the PolyStyle of the placemark with a color the color range is a gradient from yellow to red the color
   * calculate with the value and maximum
   * 
   * @param placemark
   *          change Style of this placemark
   * @param max
   *          maximum value of the data
   * @param value
   *          current value for the placemark
   * @param width
   *          width of the line (LineStyle)
   * @param transparency
   *          alpha value for the color
   */
  public static void setDataValueColor(Placemark placemark, Double max, double value, String transparency,
      double width, String lineColor, ColorMode lineMode) {
    if (transparency == null || transparency.length() != 2) {
      transparency = "FF";
    }
    if (lineColor == null || lineColor.length() != 8) {
      lineColor = "FF000000"; // black
      lineMode = ColorMode.NORMAL;
    }

    int colorValue = (int) ((value / max) * 255);
    /*
     * value <-> color conversion calculate percental value (with the max value) and use it for the hex color value 0 %
     * => yellow 50 % => orange 100 % => red
     */
    String color = getHexColor(0, (255 - colorValue), 255, true); // KML color format: inverse order of RGB
    setPolyStyleAndLineStyle(placemark, transparency + color, ColorMode.NORMAL, width, lineColor, lineMode);
  }

  /**
   * Clone the Coordinate objects and all depending objects in the object strucutre Clone the attributes id, name and
   * the objects of the classes Placemark, MultiGeometry, Polygon, Boundary, LinearRing and Coordinate
   * 
   * @param pm
   *          object to clone
   * @return cloned object of pm
   */
  public static Placemark clonePlacemark(Placemark pm) {
    /*
     * original objects have short names like pm, mg new (cloned) object have long names like placemark, multiGeometry
     */
    Placemark placemark = new Placemark().withId(pm.getId()).withName(pm.getName())
        .withDescription(pm.getDescription());

    MultiGeometry multiGeometry = new MultiGeometry();
    MultiGeometry mg = ((MultiGeometry) pm.getGeometry());
    for (int i = 0; i < mg.getGeometry().size(); i++) {

      Polygon p = (Polygon) mg.getGeometry().get(i);
      Polygon polygon = new Polygon();
      polygon.withAltitudeMode(p.getAltitudeMode()).withExtrude(p.isExtrude());

      Boundary outerBoundaryIs = new Boundary();
      List<Coordinate> coordinates = outerBoundaryIs.createAndSetLinearRing().createAndSetCoordinates();

      // set the altitude of all vertices (height of the polygon)
      for (int j = 0; j < p.getOuterBoundaryIs().getLinearRing().getCoordinates().size(); j++) {
        Coordinate c = p.getOuterBoundaryIs().getLinearRing().getCoordinates().get(j);
        coordinates.add(j, new Coordinate(c.getLongitude(), c.getLatitude(), c.getAltitude()));
      }
      if (!p.getInnerBoundaryIs().isEmpty()) {
        for (int j = 0; j < p.getInnerBoundaryIs().size(); j++) {
          Boundary innerBoundary = new Boundary();
          List<Coordinate> coordinatesInner = innerBoundary.createAndSetLinearRing().createAndSetCoordinates();
          for (int k = 0; k < p.getInnerBoundaryIs().get(j).getLinearRing().getCoordinates().size(); k++) {
            Coordinate c = p.getInnerBoundaryIs().get(j).getLinearRing().getCoordinates().get(k);
            coordinatesInner.add(k, new Coordinate(c.getLongitude(), c.getLatitude(), c.getAltitude()));
          }
          polygon.addToInnerBoundaryIs(innerBoundary);
        }
      }
      polygon.setOuterBoundaryIs(outerBoundaryIs);
      multiGeometry.addToGeometry(polygon);
    }
    placemark.setGeometry(multiGeometry);
    return placemark;
  }
}
