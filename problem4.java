import java.util.*;

class PlagiarismDetector {

    // n-gram index: ngram -> set of document IDs
    private Map<String, Set<String>> index = new HashMap<>();

    // store document n-grams
    private Map<String, List<String>> docNgrams = new HashMap<>();

    private int N = 5; // 5-gram

    // Add document to system
    public void addDocument(String docId, String text) {
        List<String> ngrams = generateNgrams(text);
        docNgrams.put(docId, ngrams);

        for (String ng : ngrams) {
            index.computeIfAbsent(ng, k -> new HashSet<>()).add(docId);
        }
    }

    // Analyze a document
    public void analyzeDocument(String docId) {
        List<String> ngrams = docNgrams.get(docId);
        Map<String, Integer> matchCount = new HashMap<>();

        for (String ng : ngrams) {
            Set<String> docs = index.getOrDefault(ng, new HashSet<>());

            for (String d : docs) {
                if (!d.equals(docId)) {
                    matchCount.put(d, matchCount.getOrDefault(d, 0) + 1);
                }
            }
        }

        // Calculate similarity
        for (String otherDoc : matchCount.keySet()) {
            int matches = matchCount.get(otherDoc);
            int total = ngrams.size();

            double similarity = (matches * 100.0) / total;

            System.out.println("Compared with: " + otherDoc);
            System.out.println("Matching n-grams: " + matches);
            System.out.println("Similarity: " + similarity + "%");

            if (similarity > 60) {
                System.out.println("⚠️ PLAGIARISM DETECTED\n");
            } else if (similarity > 15) {
                System.out.println("⚠️ Suspicious\n");
            } else {
                System.out.println("✅ Safe\n");
            }
        }
    }

    // Generate n-grams
    private List<String> generateNgrams(String text) {
        List<String> result = new ArrayList<>();

        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < N; j++) {
                sb.append(words[i + j]).append(" ");
            }

            result.add(sb.toString().trim());
        }

        return result;
    }
}

public class Main {
    public static void main(String[] args) {

        PlagiarismDetector pd = new PlagiarismDetector();

        String doc1 = "data structures and algorithms are important for coding interviews";
        String doc2 = "data structures and algorithms are important for problem solving";
        String doc3 = "machine learning and artificial intelligence are future technologies";

        pd.addDocument("essay_089", doc1);
        pd.addDocument("essay_092", doc2);
        pd.addDocument("essay_123", doc3);

        pd.analyzeDocument("essay_092");
    }
}