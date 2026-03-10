import java.util.*;

public class UsernameChecker {
    private HashMap<String, Integer> usernameMap = new HashMap<>();
    private HashMap<String, Integer> attemptFrequency = new HashMap<>();
    public boolean checkAvailability(String username) {
        attemptFrequency.put(username,
                attemptFrequency.getOrDefault(username, 0) + 1);

        if(usernameMap.containsKey(username)) {
            return false;
        }
        return true;
    }

    public void registerUser(String username, int userId) {
        usernameMap.put(username, userId);
    }

    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();
        for(int i = 1; i <= 3; i++) {
            String suggestion = username + i;

            if(!usernameMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        String modified = username.replace("_", ".");
        if(!usernameMap.containsKey(modified)) {
            suggestions.add(modified);
        }

        return suggestions;
    }

    public String getMostAttempted() {

        String mostAttempted = "";
        int max = 0;

        for(String user : attemptFrequency.keySet()) {
            int count = attemptFrequency.get(user);

            if(count > max) {
                max = count;
                mostAttempted = user;
            }
        }

        return mostAttempted;
    }

    public static void main(String[] args) {

        UsernameChecker system = new UsernameChecker();

        system.registerUser("john_doe", 1);
        system.registerUser("admin", 2);

        System.out.println(system.checkAvailability("john_doe"));

        System.out.println(system.checkAvailability("jane_smith"));

        System.out.println(system.suggestAlternatives("john_doe"));

        System.out.println(system.getMostAttempted());
    }
}