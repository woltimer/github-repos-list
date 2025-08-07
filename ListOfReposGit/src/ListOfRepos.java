import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import io.github.cdimascio.dotenv.Dotenv;

public class ListOfRepos {

    public int getRepos(String username) throws IOException, InterruptedException {
    	Dotenv dotenv = Dotenv.load(); // Automatically loads from .env in project root
    	String token = dotenv.get("GITHUB_TOKEN");
        HttpClient client = HttpClient.newHttpClient();

        // Step 1: Get user's repositories
        HttpRequest repoRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/users/" + username + "/repos"))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        HttpResponse<String> repoResponse = client.send(repoRequest, HttpResponse.BodyHandlers.ofString());

        // === ERROR HANDLING FOR NON-EXISTING USER ===
        if (repoResponse.statusCode() == 404) {
            printError(404, "GitHub user not found");
            return -1;
        } else if (repoResponse.statusCode() != 200) {
            printError(repoResponse.statusCode(), "Failed to fetch user repositories");
            return -1;
        }

        JSONArray repos = new JSONArray(repoResponse.body());

        if (repos.length() == 0) {
            System.out.println("No repositories found for user: " + username);
            return 0;
        }

        // Print owner login ONCE at the top
        String ownerLogin = repos.getJSONObject(0).getJSONObject("owner").getString("login");
        System.out.println("Owner: " + ownerLogin);
        System.out.println("-----------------------------------");

        int repoCount = 0;

        for (int i = 0; i < repos.length(); i++) {
            JSONObject repo = repos.getJSONObject(i);
            if (repo.getBoolean("fork")) continue;

            repoCount++;
            String repoName = repo.getString("name");
            System.out.println("Repository #" + repoCount + ": " + repoName);

            // Step 2: Get branches and last commit SHA
            String branchesUrl = "https://api.github.com/repos/" + username + "/" + repoName + "/branches";
            HttpRequest branchRequest = HttpRequest.newBuilder()
                    .uri(URI.create(branchesUrl))
                    .header("Authorization", "token " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            HttpResponse<String> branchResponse = client.send(branchRequest, HttpResponse.BodyHandlers.ofString());

            if (branchResponse.statusCode() == 200) {
                JSONArray branches = new JSONArray(branchResponse.body());

                for (int j = 0; j < branches.length(); j++) {
                    JSONObject branch = branches.getJSONObject(j);
                    String branchName = branch.getString("name");
                    String sha = branch.getJSONObject("commit").getString("sha");

                    System.out.println("  - Branch: " + branchName + ", SHA: " + sha);
                }
            } else {
                System.out.println("  Failed to fetch branches. Status: " + branchResponse.statusCode());
            }

            System.out.println("-----------------------------------");
        }

        return 0;
    }

    private void printError(int status, String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(new ResponseError(status, message));
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @JsonPropertyOrder({ "status", "message" })  // Enforce order
    static class ResponseError {
        private int status;
        private String message;

        public ResponseError(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
