import java.util.*;

class DNSEntry {
    String domain;
    String ip;
    long expiryTime;

    DNSEntry(String domain, String ip, long ttl) {
        this.domain = domain;
        this.ip = ip;
        this.expiryTime = System.currentTimeMillis() + ttl * 1000;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

class DNSCache {
    private final int capacity;
    private final Map<String, DNSEntry> cache;
    private int hits = 0, misses = 0;

    public DNSCache(int capacity) {
        this.capacity = capacity;

        // LinkedHashMap for LRU
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        startCleanupThread();
    }

    public String resolve(String domain) {
        long start = System.nanoTime();

        if (cache.containsKey(domain)) {
            DNSEntry entry = cache.get(domain);

            if (!entry.isExpired()) {
                hits++;
                return entry.ip + " (HIT)";
            } else {
                cache.remove(domain);
            }
        }

        misses++;
        String ip = queryUpstreamDNS(domain);
        cache.put(domain, new DNSEntry(domain, ip, 5)); // TTL = 5 sec (example)

        return ip + " (MISS)";
    }

    private String queryUpstreamDNS(String domain) {
        // Simulated DNS lookup (random IP)
        return "172.217." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    public void getStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);

        System.out.println("Hits: " + hits);
        System.out.println("Misses: " + misses);
        System.out.println("Hit Rate: " + hitRate + "%");
    }

    private void startCleanupThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);

                    Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                    while (it.hasNext()) {
                        if (it.next().getValue().isExpired()) {
                            it.remove();
                        }
                    }

                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        DNSCache dns = new DNSCache(3);

        System.out.println(dns.resolve("google.com"));
        Thread.sleep(1000);

        System.out.println(dns.resolve("google.com"));
        Thread.sleep(6000);

        System.out.println(dns.resolve("google.com"));

        dns.getStats();
    }
}