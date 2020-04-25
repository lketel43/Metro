package fr.univparis.metro;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiPredicate;

public class WebserverLib {


  private static HashMap<String, String> statisticsHtml;


  public static String toOption() {
    String res = "";
    boolean first = true;
    for (String s : Configuration.getCitiesName()) {
      res += "<option " + ((first)? "selected": "") + " value = \"" + s + "\">" + s + "</option>\n";
    }
    return res;
  }



    public static String time(Double time) {
	if (time == Double.POSITIVE_INFINITY) throw new IllegalArgumentException();
	Double seconds = time % 60;
	Double minutesTmp = (time - seconds) / 60;
	Double minutes = minutesTmp % 60;
	Double hours = (minutesTmp - minutes) / 60;


        return "Average time to get to your destination : " + hours + " h, "  + minutes + " min, " + seconds + " s.";
    }


    public static String perturbationToHtml(String city) {
	Set<String> set = Trafics.getPerturbation(city);
	if (set.isEmpty()) return "";
	String res = "<h4>Here are the actual perturbation, select the one you want to remove</h4>\n" +
	    "<form action=\"" + city + "/removePerturbation\" method=\"post\">\n";
	for (String s : set) {
	    res += "<input type=\"checkbox\" id=\"" + s + "\" value=\"" + s + "\" name=\"removePerturbation\"><label for=\"" + s + "\">" + s + "</label><br>";
	}
	res += "<input type=\"submit\">\n" +
	    "</form>";
	return res;
    }

    public static String stat1(Pair<Pair<Station, Station>, Double> stat1, int stat2, String stat3, String stat4, int stat5, int stat6, Pair<String, Double> stat7, Pair<String, Double> stat8){
	Double seconds = stat1.getValue() % 60;
	Double minutesTmp = (stat1.getValue() - seconds) / 60;
	Double minutes = minutesTmp % 60;
	Double hours = (minutesTmp - minutes) / 60;
	int seconds5 = stat5 % 60;
	int minutesTmp5 = (stat5 - seconds5) / 60;
	int minutes5 = minutesTmp5 % 60;
	int hours5 = (minutesTmp5 - minutes5) / 60;
	Double seconds7 = stat7.getValue() % 60;
	Double minutesTmp7 = (stat7.getValue() - seconds7) / 60;
	Double minutes7 = minutesTmp7 % 60;
	Double hours7 = (minutesTmp7 - minutes7) / 60;
	Double seconds8 = stat8.getValue() % 60;
	Double minutesTmp8 = (stat8.getValue() - seconds8) / 60;
	Double minutes8 = minutesTmp8 % 60;
	Double hours8 = (minutesTmp8 - minutes8) / 60;
	return "<h3>The longest traject between two stations :</h3>Traject : " + stat1.getObj().getObj() + " to " + stat1.getObj().getValue()
	    + "<br>Time : " + hours + "h "+ minutes + "min " + seconds + "sec<br><h3>Number minimum of correspondence to do all the possible trajects on the network : " + stat2 +"</h3><br><h3> The average number of stations per line : " + stat6 + "</h3><br><h3>The line with the most stations : line " + stat3 + "</h3>"
	    + "<br><h3>The line with the least stations : line " + stat4 + "</h3><br><h3>The average time from one terminus of a line to the other : " + hours5 + "h "+ minutes5 + "min " + seconds5 + "sec</h3><br><h3>The longest(duration) line : line " + stat7.getObj() + " in " + hours7 + "h "+ minutes7 + "min " + seconds7 + "sec"
	    + "</h3><br><h3>The shortest(duration) line : line " + stat8.getObj() + " in "+ hours8 + "h "+ minutes8 + "min " + seconds8 + "sec</h3><br>" ;
    }

    /**
     * @param prev a HashMap acting like a tree. the root has null parent
     * @param target The targetted node
     * @return A list of nodes going from the root of prev to target
     */
    public static <T> LinkedList<T> buildPath(HashMap<T, T> prev, T target) {
	LinkedList<T> path = new LinkedList<T>();
	T current = target;
	while( current != null ) {
	    path.addFirst(current);
	    current = prev.get(current);
	}
	return path;
	}

	private static String pathFloyd(ArrayList<Pair<String,String>> l, String from, String to){
		String ret = "Departure" + from + "<br><br>";
		int i = 1;
		for(Pair<String, String> p : l){
			if(i == l.size() - 1){
				ret += "line : " + p.getValue() + " : " + p.getObj() + " -> " + l.get(i).getObj() + "<br><br>";
				ret += "Arrival : " + to;
				break; 
			}
			ret += "line : " + p.getValue() + " : " + p.getObj() + " -> " + l.get(i).getObj() + "<br>";
			i++;
		}
		return ret;
	}
	
	public static String limitedConnexionPathWithFloyd(WGraph<Station> g, Station from, Station to){
		String start = from.getName();
		String end = to.getName();
		String body = "";
        HashMap<String, MatriceWGraph> lines = MatriceWGraph.initializeAllLineGraphs(g);
        MatriceWGraph matriceGraph = new MatriceWGraph(g, lines);
        ArrayList<Pair<String, String>> l = LimitedConnectionSearch.getPath(matriceGraph, start, end);
        Collections.reverse(l);
        Double t = matriceGraph.getDirect()[matriceGraph.getSetOfVertices().get(start)][matriceGraph.getSetOfVertices().get(end)];
        t += (matriceGraph.getIntermediate()[matriceGraph.getSetOfVertices().get(start)][matriceGraph.getSetOfVertices().get(end)] - 1) * Parser.defaultChangeStationWeight;
		body = "<h2>Time</h2>\n" + WebserverLib.time(t) + "\n" + "<h2>Itinerary</h2>\n" + WebserverLib.pathFloyd(l, start, end);
		return body;
	}

    ///////////////////////////////////////////////////
    // limited connection path with BouarahAlgorithm //
    ///////////////////////////////////////////////////

    public static <T> boolean isThereAnyPath(HashMap<T, Double> dist, T to) {
	return dist.containsKey(to) && !dist.get(to).equals(Double.POSITIVE_INFINITY);
    }

    private static String path(HashMap<Pair<Station, Integer>,Pair<Station, Integer>> prev, Pair<Station, Integer> to) {
	LinkedList<Pair<Station, Integer>> path = buildPath(prev, to);

	path.removeFirst(); // on enlève la meta station start...
	path.removeLast();  // ...et la meta station end

	String from = path.getFirst().getObj().getName();
	String line = path.getFirst().getObj().getLine();
	String res = "Departure : " + from + "<br><br>"+"line " + line + " : " + from + " -> ";
	for (Pair<Station, Integer> st : path) {
	    if (!st.getObj().getLine().equals(line)) {
		res += st.getObj().getName() + "<br>" + "line " + st.getObj().getLine() + " : " + st.getObj().getName() + " -> ";
		line = st.getObj().getLine();
	    }
	}
	res += to.getObj().getName() + "<br><br>Arrival: " + to.getObj().getName();
	return res;
    }

    public static String limitedConnectionPath(WGraph<Station> g, Station start, Station to) {
	// on lance Dijkstra pour vérifier l'existence du chemin
	HashMap<Station, Station> prev = new HashMap<Station, Station>();
	HashMap<Station, Double> dist = new HashMap<Station, Double>();
	Dijkstra.shortestPath(g, start, prev, dist);
	if( !isThereAnyPath(dist,to) ) {
	    return "Due to actual trafics perturbation we couldn't find any path from " + start.getName() + " to " + to.getName();
	}

	// on lance ensuite BouarahAlgorithm pour trouver un chemin en un nombre minimal de correspondances
	HashMap<Pair<Station, Integer>, Pair<Station, Integer>> prevLimited = new HashMap<Pair<Station, Integer>, Pair<Station, Integer>>();
	HashMap<Pair<Station, Integer>, Double> distLimited = new HashMap<Pair<Station, Integer>, Double>();
	BiPredicate<Station, Station> sameLine = (Station s1, Station s2) -> s1.getLine().equals(s2.getLine()) || s1.getLine().startsWith("Meta Station") || s2.getLine().startsWith("Meta Station");
	int limit = 0;
	Pair<Station, Integer> toLimited = new Pair<Station, Integer>(to, limit);

	while( true ) { // le chemin existe
	    BouarahAlgorithm.shortestPath(g, start, limit, sameLine, prevLimited, distLimited);
	    if( WebserverLib.isThereAnyPath(distLimited, toLimited) )
		break;
	    limit += 1;
	    toLimited.setValue(limit);
	}
	String time = WebserverLib.time(distLimited.get(toLimited));
	String itinerary = WebserverLib.path(prevLimited, toLimited);
        return "<h2>Time</h2>\n" + time + "\n" + "<h2>Itinerary</h2>\n" + itinerary;
    }

    ///////////////////
    // multiple path //
    ///////////////////

    private static String path(HashMap<Station, Station> prev, Station to, ArrayList<Pair<Station, Station>> changingStation) {
	changingStation.clear();

	LinkedList<Station> path = buildPath(prev, to);

	path.removeFirst(); // on enlève la meta station start...
	path.removeLast();  // ...et la meta station end

	String from = path.getFirst().getName();
	String line = path.getFirst().getLine();
	String res = "Departure : " + from + "<br><br>"+"line " + line + " : " + from + " -> ";
	Station prec = null;
	for (Station st : path) {
	    if (!st.getLine().equals(line)) {
		changingStation.add(new Pair<Station, Station>(prec, st));
		res += st.getName() + "<br>" + "line " + st.getLine() + " : " + st.getName() + " -> ";
		line = st.getLine();
	    }
	    prec = st;
	}
	res += to.getName() + "<br><br>Arrival: " + to.getName();
	return res;
    }

    public static String multiplePath(WGraph<Station> g, Station start, Station to) {
	TreeSet<Pair<String, Double>> resAux = new TreeSet<Pair<String, Double>>((p1, p2) -> {
		if (p1.getValue() < p2.getValue()) return -1;
		if (p1.getValue() > p2.getValue()) return 1;
		else {
		    return p1.getObj().hashCode() - p2.getObj().hashCode();
		}
	});
	double THRESHOLD = 1.2;
	HashMap<Station, Station> prev = new HashMap<Station, Station>();
	HashMap<Station, Double> dist = new HashMap<Station, Double>();
	ArrayList<Pair<Station, Station>> changingStation  = new ArrayList<Pair<Station, Station>>();
	Dijkstra.shortestPath(g, start, prev, dist);
	try {
	    resAux.add(new Pair<String, Double>(
						"<h2>Time</h2>\n" +
						time(dist.get(to)) +
						"<h2>Itinerary</h2>" +
						path(prev, to, changingStation),
						dist.get(to)
						));

	} catch(RuntimeException e) {
	    return "Due to actual trafics perturbation we couldn't find any path from " + start.getName() + " to " + to.getName();
	}

	multiplePathAux(g, start, to, changingStation, resAux, THRESHOLD * dist.get(to), 0);

	String res = "";
	for (Pair<String, Double> p : resAux) {
	    res += p.getObj();
	}
	return res;
    }

    private static void multiplePathAux(WGraph<Station> g, Station start, Station to, ArrayList<Pair<Station, Station>> changingStation, TreeSet<Pair<String, Double>> resAux, Double threshold, int depth) {
	int MAX_CORRESPONDANCES = 3;
	int MAX_DEPTH = 5;
	if (depth >= MAX_DEPTH) return;

	HashMap<Station, Station> prev = new HashMap<Station, Station>();
	HashMap<Station, Double> dist = new HashMap<Station, Double>();
	for (Pair<Station, Station> p : changingStation) {
	    WGraph<Station> revert = new WGraph<Station>();
	    Station st = p.getObj();
	    revert.addVertex(st);
	    for (Station n : g.neighbors(st)) {
		if (n.getName().equals(st.getName()) && ! n.getLine().startsWith("Meta Station")) {
		    // Changin are on both sides
		    revert.addVertex(n);
		    revert.addEdge(st, n, g.weight(st, n));
		    g.setWeight(st, n, Double.POSITIVE_INFINITY);
		}
	    }
	    ArrayList<Pair<Station, Station>> pChangingStation = new ArrayList<Pair<Station, Station>>();
	    Dijkstra.shortestPath(g, start, prev, dist);
	    if (dist.get(to) <= threshold) {
		String itinerary = path(prev, to, pChangingStation);
		if (pChangingStation.size() <= MAX_CORRESPONDANCES) {
		    resAux.add(new Pair<String, Double>(
							"<h2>Time</h2>\n" +
							time(dist.get(to)) +
							"<h2>Itinerary</h2>" +
							itinerary,
							dist.get(to)
							));
		    multiplePathAux(g, start, to, pChangingStation, resAux, threshold, depth + 1);
		}

	    }
	    g.apply(revert);
	}
    }

  public static void initStatistics() {
    statisticsHtml = new HashMap<String, String>();
    for (String city : Configuration.getCitiesName()) {
      WGraph<Station> g = Trafics.getInitialGraph(city);
      Pair<Pair<Station, Station>, Double> stat1 = Statistics.mostDistantStations(g, (s -> !s.getLine().equals("Meta Station Start")), (t -> t.getLine().equals("Meta Station End")));
      int stat2 = Statistics.minimumCorrespondence(g, (s -> s.getLine().equals("Meta Station Start")), s -> s.getLine().equals("Meta Station End") , (Station s1, Station s2) -> s1.getLine().equals(s2.getLine()) || s1.getLine().startsWith("Meta Station") || s2.getLine().startsWith("Meta Station"));
      String stat3 = Statistics.extremumLine(g, true);
      String stat4 = Statistics.extremumLine(g, false);
      HashMap<String, Double> res = new HashMap<String, Double>();
      int stat5 = Statistics.averageTimeOnEachLine(g, res);
      int stat6 = Statistics.averageNbOfStationPerLine(g);
      Pair<String, Double> stat7 = Statistics.longestTimeTravelLine(g);
      Pair<String, Double> stat8 = Statistics.shortestTimeTravelLine(g);
      statisticsHtml.put(city, statsToString(stat1, stat2, stat3, stat4, stat5, stat6, stat7, stat8));
    }
  }


  private static String statsToString(Pair<Pair<Station, Station>, Double> stat1, int stat2, String stat3, String stat4, int stat5, int stat6, Pair<String, Double> stat7, Pair<String, Double> stat8) {
    Double[] time1 = doubleToTime(stat1.getValue());
    Double[] time5 = doubleToTime((double) stat5);
    Double[] time7 = doubleToTime(stat7.getValue());
    Double[] time8 = doubleToTime(stat8.getValue());
    return "<h3>The longest traject between two stations :</h3>Traject : " + stat1.getObj().getObj() + " to " + stat1.getObj().getValue()
    + "<br>Time : " + time1[0] + "h "+ time1[1] + "min " + time1[2] + "sec<br>" +
    "<h3>Number minimum of correspondence to do all the possible trajects on the network : " + stat2 +"</h3><br>" +
    "<h3> The average number of stations per line : " + stat6 + "</h3><br><h3>The line with the most stations : line " + stat3 + "</h3><br>" +
    "<h3>The line with the least stations : line " + stat4 + "</h3><br>" +
    "<h3>The average time from one terminus of a line to the other : " + time5[0] + "h "+ time5[1] + "min " + time5[2] + "sec</h3><br>" +
    "<h3>The longest(duration) line : line " + stat7.getObj() + " in " + time7[0] + "h "+ time7[1] + "min " + time7[2] + "sec</h3><br>" +
    "<h3>The shortest(duration) line : line " + stat8.getObj() + " in "+ time8[0] + "h "+ time8[1] + "min " + time8[2] + "sec</h3><br>" ;

  }
 /**
  * Return a String containing information about the network of city
  * @param city the city in which we want statistics
  * @return a String containing information about the network of city
  */
  public static String getStringStatistics(String city) {
    return statisticsHtml.get(city);
  }

  public static Double[] doubleToTime(Double time) {
    Double seconds = time % 60;
    Double minutesTmp = (time - seconds) / 60;
    Double minutes = minutesTmp % 60;
    Double hours = (minutesTmp - minutes) / 60;
    Double[] res = {hours, minutes, seconds};
    return res;
  }


}
