package HttpRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        //Task1
        sendPOST();

        System.out.print("Write ID to update info about user: ");
        String PUTId = scanner.nextLine();
        String jsonPath = "updateuser.json";
        sendPUT(PUTId, jsonPath);

        sendGET();

        System.out.print("Write ID to delete info about user: ");
        String deleteID = scanner.nextLine();
        sendDELETE(deleteID);

        System.out.print("Write ID to get info: ");
        String userID = scanner.nextLine();
        sendGETId(userID);

        System.out.print("Write username to get info: ");
        String userName = scanner.nextLine();
        sendGETUserName(userName);

        //Task2

        System.out.print("Write the user ID for comments on the last post: ");
        String commentsToFileId = scanner.nextLine();
        getCommentsToFile(commentsToFileId, getPosts(commentsToFileId));

        //Task3
        System.out.print("Write ID to take all open tasks: ");
        String idTask = scanner.nextLine();
        getAllOpenTasks(idTask);
    }

    //Task1
    private static void sendPOST() throws IOException, InterruptedException {
        String filePath = "user.json";

        String json = new String(Files.readAllBytes(new File(filePath).toPath()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response status code on adding an object: " + response.statusCode());
        System.out.println(response.body());
    }

    private static void sendPUT(String id, String jsonPath) throws IOException, InterruptedException {
        String json = new String(Files.readAllBytes(new File(jsonPath).toPath()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response status code on object update: " +response.statusCode());
        System.out.println(response.body());
    }

    private static void sendDELETE (String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users/" + id))
                .DELETE()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response status code on object delete: " + response.statusCode());
    }

    private static void sendGET() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        System.out.println("Response status code on get all objects: " + response.statusCode());
    }

    private static void sendGETId(String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users/" + id))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        System.out.println("Response status code on get object by ID: " + response.statusCode());
    }

    private static void sendGETUserName (String userName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users?username=" + userName))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Info about User " + userName + ":");
        System.out.println(response.body());
        System.out.println("Response status code on get info by username: " + response.statusCode());
    }

    //Task2

    private static TreeMap<Integer, JsonObject> getPosts(String userPostId) throws IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users/" + userPostId + "/posts"))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        JsonArray posts = JsonParser.parseString(postResponse.body()).getAsJsonArray();
        TreeMap<Integer, JsonObject> sortedPosts = new TreeMap<>(Collections.reverseOrder());

        for (JsonElement postElement : posts) {
            JsonObject post = postElement.getAsJsonObject();
            sortedPosts.put(post.get("id").getAsInt(), post);
        }

        return sortedPosts;
    }

    private static void getCommentsToFile(String userPostId, TreeMap<Integer, JsonObject> sortedPosts) throws IOException, InterruptedException {
        JsonObject lastPost = sortedPosts.firstEntry().getValue();

        HttpRequest commentsRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts/" + lastPost.get("id").getAsInt() + "/comments"))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> commentsResponse = client.send(commentsRequest, HttpResponse.BodyHandlers.ofString());

        String filename = "user-" + userPostId + "-post-" + lastPost.get("id").getAsString() + "-comments.json";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(commentsResponse.body());
        }

        System.out.println("Comments for user " + userPostId + ", post " + lastPost.get("id") + " written to file " + filename);
    }
    //Task3
    private static void getAllOpenTasks (String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users/" + id + "/todos?completed=false"))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        System.out.println("Response status code: " + response.statusCode());
    }
}
