import java.util.concurrent.*;
import java.util.*;

class TokenBucket {
    private int tokens;
    private final int maxTokens;
    private final double refillRate; // tokens per second
    private long lastRefillTime;

    public TokenBucket(int maxTokens, double refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    // synchronized for thread safety
    public synchronized boolean allowRequest() {
        refill();

        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        double seconds = (now - lastRefillTime) / 1000.0;

        int refillTokens = (int)(seconds * refillRate);

        if (refillTokens > 0) {
            tokens = Math.min(maxTokens, tokens + refillTokens);
            lastRefillTime = now;
        }
    }

    public synchronized int getRemainingTokens() {
        refill();
        return tokens;
    }

    public synchronized long getRetryAfterSeconds() {
        if (tokens > 0) return 0;

        double timeForNextToken = 1.0 / refillRate;
        return (long)Math.ceil(timeForNextToken);
    }
}

class RateLimiter {

    private final Map<String, TokenBucket> clients = new ConcurrentHashMap<>();

    private final int MAX_TOKENS = 1000;
    private final double REFILL_RATE = 1000.0 / 3600; // per second

    public String checkRateLimit(String clientId) {

        clients.putIfAbsent(clientId,
                new TokenBucket(MAX_TOKENS, REFILL_RATE));

        TokenBucket bucket = clients.get(clientId);

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after "
                    + bucket.getRetryAfterSeconds() + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clients.get(clientId);

        if (bucket == null) {
            return "No data for client";
        }

        int remaining = bucket.getRemainingTokens();
        int used = MAX_TOKENS - remaining;

        return "{used: " + used +
                ", limit: " + MAX_TOKENS +
                ", remaining: " + remaining + "}";
    }
}

public class Main {
    public static void main(String[] args) {

        RateLimiter rl = new RateLimiter();

        String client = "abc123";

        for (int i = 0; i < 1005; i++) {
            System.out.println(rl.checkRateLimit(client));
        }

        System.out.println(rl.getRateLimitStatus(client));
    }
}