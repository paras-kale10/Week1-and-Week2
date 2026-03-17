import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEnd = false;

    // store top queries for this prefix (optimization)
    PriorityQueue<Query> topQueries = new PriorityQueue<>(
            Comparator.comparingInt(q -> q.freq)
    );
}

class Query {
    String text;
    int freq;

    Query(String text, int freq) {
        this.text = text;
        this.freq = freq;
    }
}

class AutocompleteSystem {

    private TrieNode root = new TrieNode();

    // Global frequency map
    private Map<String, Integer> freqMap = new HashMap<>();

    private int K = 10;

    // Insert / Update query
    public void insert(String query) {
        int freq = freqMap.getOrDefault(query, 0) + 1;
        freqMap.put(query, freq);

        TrieNode node = root;

        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            updateTopK(node, query, freq);
        }

        node.isEnd = true;
    }

    // Maintain top K at each node
    private void updateTopK(TrieNode node, String query, int freq) {
        node.topQueries.removeIf(q -> q.text.equals(query));
        node.topQueries.offer(new Query(query, freq));

        if (node.topQueries.size() > K) {
            node.topQueries.poll(); // remove smallest
        }
    }

    // Search suggestions
    public List<String> search(String prefix) {
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }
            node = node.children.get(c);
        }

        List<Query> list = new ArrayList<>(node.topQueries);
        list.sort((a, b) -> b.freq - a.freq);

        List<String> result = new ArrayList<>();
        for (Query q : list) {
            result.add(q.text + " (" + q.freq + ")");
        }

        return result;
    }
}

public class Main {
    public static void main(String[] args) {

        AutocompleteSystem ac = new AutocompleteSystem();

        ac.insert("java tutorial");
        ac.insert("javascript");
        ac.insert("java download");
        ac.insert("java tutorial");
        ac.insert("java tutorial");

        System.out.println(ac.search("jav"));
    }
}