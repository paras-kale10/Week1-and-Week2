import java.util.*;
import java.util.concurrent.*;

class Event {
    String url;
    String userId;
    String source;

    Event(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class AnalyticsDashboard {

    // Page → total visits
    private Map<String, Integer> pageViews = new ConcurrentHashMap<>();

    // Page → unique users
    private Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // Source → count
    private Map<String, Integer> sourceCount = new ConcurrentHashMap<>();

    // Process incoming event
    public void processEvent(Event e) {

        // Count page views
        pageViews.put(e.url, pageViews.getOrDefault(e.url, 0) + 1);

        // Track unique visitors
        uniqueVisitors
                .computeIfAbsent(e.url, k -> ConcurrentHashMap.newKeySet())
                .add(e.userId);

        // Track traffic source
        sourceCount.put(e.source, sourceCount.getOrDefault(e.source, 0) + 1);
    }

    // Get Top 10 pages
    public List<String> getTopPages() {
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            pq.offer(entry);
            if (pq.size() > 10) pq.poll();
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            Map.Entry<String, Integer> e = pq.poll();
            String page = e.getKey();
            int views = e.getValue();
            int unique = uniqueVisitors.get(page).size();

            result.add(page + " - " + views + " views (" + unique + " unique)");
        }

        Collections.reverse(result);
        return result;
    }

    // Print Dashboard
    public void getDashboard() {
        System.out.println("\n===== DASHBOARD =====");

        System.out.println("Top Pages:");
        int rank = 1;
        for (String page : getTopPages()) {
            System.out.println(rank++ + ". " + page);
        }

        System.out.println("\nTraffic Sources:");
        for (String src : sourceCount.keySet()) {
            System.out.println(src + ": " + sourceCount.get(src));
        }
    }

    // Auto refresh every 5 seconds
    public void startDashboard() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    getDashboard();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {

        AnalyticsDashboard dashboard = new AnalyticsDashboard();
        dashboard.startDashboard();

        String[] pages = {"/news", "/sports", "/tech", "/breaking"};
        String[] sources = {"google", "facebook", "direct"};

        Random rand = new Random();

        // Simulate real-time traffic
        for (int i = 0; i < 1000; i++) {
            String url = pages[rand.nextInt(pages.length)];
            String user = "user_" + rand.nextInt(100);
            String source = sources[rand.nextInt(sources.length)];

            dashboard.processEvent(new Event(url, user, source));

            Thread.sleep(10); // simulate stream
        }
    }
}