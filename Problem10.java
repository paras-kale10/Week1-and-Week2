import java.util.*;

class Video {
    String id;
    String data;

    Video(String id, String data) {
        this.id = id;
        this.data = data;
    }
}

class MultiLevelCache {

    // L1 Cache (Memory) - LRU
    private LinkedHashMap<String, Video> L1;

    // L2 Cache (SSD simulated)
    private Map<String, Video> L2;

    // L3 (Database simulation)
    private Map<String, Video> L3;

    // Access frequency
    private Map<String, Integer> accessCount;

    // Stats
    private int l1Hits = 0, l2Hits = 0, l3Hits = 0;

    private final int L1_CAP = 3;     // small for demo (use 10,000 real)
    private final int L2_CAP = 5;     // small for demo (use 100,000 real)

    public MultiLevelCache() {

        L1 = new LinkedHashMap<>(L1_CAP, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Video> e) {
                return size() > L1_CAP;
            }
        };

        L2 = new LinkedHashMap<>(L2_CAP, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Video> e) {
                return size() > L2_CAP;
            }
        };

        L3 = new HashMap<>();
        accessCount = new HashMap<>();

        // preload DB
        for (int i = 1; i <= 10; i++) {
            L3.put("video_" + i, new Video("video_" + i, "DATA_" + i));
        }
    }

    public Video getVideo(String id) {

        // L1
        if (L1.containsKey(id)) {
            l1Hits++;
            updateAccess(id);
            System.out.println("L1 HIT");
            return L1.get(id);
        }

        // L2
        if (L2.containsKey(id)) {
            l2Hits++;
            System.out.println("L2 HIT → Promoting to L1");

            Video v = L2.get(id);
            L1.put(id, v);
            updateAccess(id);

            return v;
        }

        // L3
        if (L3.containsKey(id)) {
            l3Hits++;
            System.out.println("L3 HIT → Adding to L2");

            Video v = L3.get(id);
            L2.put(id, v);
            updateAccess(id);

            return v;
        }

        return null;
    }

    private void updateAccess(String id) {
        int count = accessCount.getOrDefault(id, 0) + 1;
        accessCount.put(id, count);

        // Promote to L1 if frequently accessed
        if (count > 2 && L2.containsKey(id)) {
            System.out.println("Promoting " + id + " to L1 (hot content)");
            L1.put(id, L2.get(id));
        }
    }

    // Cache invalidation
    public void invalidate(String id) {
        L1.remove(id);
        L2.remove(id);
        L3.remove(id);
        accessCount.remove(id);
    }

    public void printStats() {
        int total = l1Hits + l2Hits + l3Hits;

        System.out.println("\n=== CACHE STATS ===");
        System.out.println("L1 Hits: " + l1Hits);
        System.out.println("L2 Hits: " + l2Hits);
        System.out.println("L3 Hits: " + l3Hits);

        System.out.println("Hit Rate: " + (total == 0 ? 0 : (l1Hits + l2Hits) * 100.0 / total) + "%");
    }
}

public class Main {
    public static void main(String[] args) {

        MultiLevelCache cache = new MultiLevelCache();

        cache.getVideo("video_1"); // L3 → L2
        cache.getVideo("video_1"); // L2 → L1
        cache.getVideo("video_1"); // L1

        cache.getVideo("video_2"); // L3
        cache.getVideo("video_2"); // L2

        cache.getVideo("video_3"); // L3

        cache.printStats();
    }
}