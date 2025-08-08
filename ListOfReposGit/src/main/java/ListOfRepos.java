import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ListOfRepos {

    // Finals modifiers to avoid reference changes, but allow methods
    private final HttpClient client;
    private final String token;
    private final ObjectMapper objectMapper;

    public ListOfRepos() { // Object building
        this.client = HttpClient.newHttpClient();
        this.token = loadToken();
        this.objectMapper = new ObjectMapper();
    }
    // Main method. Returning int's left for easier debbuging
    public int getRepos(String username) throws IOException, InterruptedException {
        JSONArray repos = fetchUserRepositories(username);
        if (repos == null) return -1;
        if (repos.length() == 0) {
            System.out.println("No repositories found for user: " + username);
            return 0;
        }

        printOwnerInfo(repos);
        int count = printRepositoriesAndBranches(username, repos);
        return count >= 0 ? 0 : -1;
    }
    // Checks if fetching data is possible and does it. If not possible throws errors.
    private JSONArray fetchUserRepositories(String username) throws IOException, InterruptedException {
        String safeUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String url = "https://api.github.com/users/" + safeUsername + "/repos";

        HttpRequest request;
        try {
            request = buildGetRequest(url);
        } catch (IllegalArgumentException e) {
            printError(400, "Invalid username: " + username);
            return null;
        }

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();

        if (code == 404) {
            printError(404, "GitHub user not found");
            return null;
        } else if (code != 200) {
            printError(code, "Failed to fetch user repositories");
            return null;
        }

        return new JSONArray(response.body());
    }
    // Printing login of the owner of repositories
    private void printOwnerInfo(JSONArray repos) {
        String ownerLogin = repos.getJSONObject(0).getJSONObject("owner").getString("login");
        System.out.println("Owner: " + ownerLogin);
        System.out.println("-----------------------------------");
    }
    // Connecting branches to repositories
    private int printRepositoriesAndBranches(String username, JSONArray repos) throws IOException, InterruptedException {
        int repoCount = 0;
        for (int i = 0; i < repos.length(); i++) {
            JSONObject repo = repos.getJSONObject(i);
            if (repo.getBoolean("fork")) continue;

            repoCount++;
            String repoName = repo.getString("name");
            System.out.println("Repository #" + repoCount + ": " + repoName);

            printBranches(username, repoName);
            System.out.println("-----------------------------------");
        }
        return repoCount;
    }
    // Building the branches from repositories and printing them
    private void printBranches(String username, String repoName) throws IOException, InterruptedException {
        String safeUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String safeRepoName = URLEncoder.encode(repoName, StandardCharsets.UTF_8);
        String url = "https://api.github.com/repos/" + safeUsername + "/" + safeRepoName + "/branches";

        HttpRequest request;
        try {
            request = buildGetRequest(url);
        } catch (IllegalArgumentException e) {
            printError(400, "Invalid repository name: " + repoName);
            return;
        }

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONArray branches = new JSONArray(response.body());
            for (int j = 0; j < branches.length(); j++) {
                JSONObject branch = branches.getJSONObject(j);
                String branchName = branch.getString("name");
                String sha = branch.getJSONObject("commit").getString("sha");
                System.out.println("  - Branch: " + branchName + ", SHA: " + sha);
            }
        } else {
            System.out.println("  Failed to fetch branches. Status: " + response.statusCode());
        }
    }

    private HttpRequest buildGetRequest(String url) { // Builds request for the API server
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .build();
    }

    private String loadToken() { // Loads GitHub REST API token from the .env file
        Dotenv dotenv = Dotenv.load();
        String t = dotenv.get("GITHUB_TOKEN");
        if (t == null || t.isBlank()) {
            throw new RuntimeException("GITHUB_TOKEN not found in .env");
        }
        return t;
    }

    private void printError(int status, String message) { // Builds an error
        try {
            String json = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(new ResponseError(status, message));
            System.out.println(json);
        } catch (IOException e) {
            System.err.println("Error creating error JSON: " + e.getMessage());
        }
    }

    @JsonPropertyOrder({"status", "message"}) // Enforces right order of the error messages
    static class ResponseError {
        private final int status;
        private final String message;

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

