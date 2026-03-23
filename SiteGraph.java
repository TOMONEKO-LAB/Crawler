import java.util.*; // Collections, HashMap, HashSet, Map, Set

public class SiteGraph {

  private final Map<String, Set<String>> graph;

  public SiteGraph() {
    this.graph = new HashMap<>();
  }

  // from -> to のリンクを追加
  public synchronized void addEdge(String from, String to) {
    Set<String> links = graph.get(from);
    if (links == null) {
      links = new HashSet<>();
      graph.put(from, links);
    }
    links.add(to);
  }

  // URLに対するリンクを取得
  public synchronized Set<String> getLinks(String url) {
    return graph.getOrDefault(url, Collections.emptySet());
  }

  // 訪問済みか
  public synchronized boolean isVisit(String url) {
    return graph.containsKey(url);
  }

  public static void main(String[] args) {
    SiteGraph g = new SiteGraph();
    g.addEdge("https://example.com", "https://example.com/a");
    g.addEdge("https://example.com", "https://example.com/b");
    System.out.println("visitedRoot=" + g.isVisit("https://example.com"));
    System.out.println("links=" + g.getLinks("https://example.com").size());
  }
}