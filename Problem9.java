import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long time; // epoch seconds

    Transaction(int id, int amount, String merchant, String account, long time) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.time = time;
    }
}

class TransactionAnalyzer {

    List<Transaction> transactions;

    public TransactionAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // 1. Classic Two-Sum
    public List<String> findTwoSum(int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                Transaction prev = map.get(complement);
                result.add("(" + prev.id + ", " + t.id + ")");
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // 2. Two-Sum with 1-hour window
    public List<String> findTwoSumWithTime(int target, long windowSeconds) {
        List<String> result = new ArrayList<>();
        Map<Integer, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                for (Transaction prev : map.get(complement)) {
                    if (Math.abs(t.time - prev.time) <= windowSeconds) {
                        result.add("(" + prev.id + ", " + t.id + ")");
                    }
                }
            }

            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }

        return result;
    }

    // 3. K-Sum (recursive)
    public List<List<Integer>> findKSum(int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        kSumHelper(0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void kSumHelper(int start, int k, int target,
                            List<Integer> path, List<List<Integer>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(path));
            return;
        }

        if (k == 0 || start >= transactions.size()) return;

        for (int i = start; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);

            path.add(t.id);
            kSumHelper(i + 1, k - 1, target - t.amount, path, result);
            path.remove(path.size() - 1);
        }
    }

    // 4. Duplicate Detection
    public List<String> detectDuplicates() {
        Map<String, Set<String>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;

            map.computeIfAbsent(key, k -> new HashSet<>()).add(t.account);
        }

        for (String key : map.keySet()) {
            Set<String> accounts = map.get(key);

            if (accounts.size() > 1) {
                result.add(key + " → accounts: " + accounts);
            }
        }

        return result;
    }
}

public class Main {
    public static void main(String[] args) {

        List<Transaction> txns = Arrays.asList(
                new Transaction(1, 500, "StoreA", "acc1", 1000),
                new Transaction(2, 300, "StoreB", "acc2", 1100),
                new Transaction(3, 200, "StoreC", "acc3", 1200),
                new Transaction(4, 500, "StoreA", "acc2", 1300)
        );

        TransactionAnalyzer ta = new TransactionAnalyzer(txns);

        System.out.println("Two Sum: " + ta.findTwoSum(500));
        System.out.println("Two Sum (Time): " + ta.findTwoSumWithTime(500, 3600));
        System.out.println("K Sum: " + ta.findKSum(3, 1000));
        System.out.println("Duplicates: " + ta.detectDuplicates());
    }
}