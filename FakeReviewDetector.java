import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FakeReviewDetector {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> serveFile(exchange, "index.html", "text/html"));
        server.createContext("/style.css", exchange -> serveFile(exchange, "style.css", "text/css"));
        server.createContext("/script.js", exchange -> serveFile(exchange, "script.js", "application/javascript"));

        server.createContext("/analyze", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                send(exchange, "{\"error\":\"POST request required\"}", "application/json", 405);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseForm(body);
            String review = form.getOrDefault("review", "").trim();

            if (review.isEmpty()) {
                send(exchange, "{\"error\":\"Please enter a review.\"}", "application/json", 400);
                return;
            }

            Analysis result = analyzeReview(review);
            String json = String.format(
                Locale.US,
                "{\"score\":%.0f,\"label\":\"%s\",\"reason\":\"%s\"}",
                result.score, escapeJson(result.label), escapeJson(result.reason)
            );

            send(exchange, json, "application/json", 200);
        });

        server.start();
        System.out.println("Fake Review Detector is running at http://localhost:8080");
        System.out.println("Press Ctrl+C to stop the server.");
    }

    static Analysis analyzeReview(String review) {
        String lower = review.toLowerCase();
        int points = 0;
        List<String> reasons = new ArrayList<>();

        // Rule 1: suspicious promotional words
        String[] suspiciousWords = {
            "100% genuine", "best product ever", "must buy",
            "buy now", "guaranteed", "five star", "5 star",
            "amazing!!!", "perfect product", "life changing"
        };

        int suspiciousCount = 0;
        for (String word : suspiciousWords) {
            if (lower.contains(word)) suspiciousCount++;
        }

        if (suspiciousCount > 0) {
            points += Math.min(30, suspiciousCount * 10);
            reasons.add("Contains promotional or exaggerated phrases");
        }

        // Rule 2: too many exclamation marks
        long exclamations = review.chars().filter(c -> c == '!').count();
        if (exclamations >= 3) {
            points += 20;
            reasons.add("Uses too many exclamation marks");
        }

        // Rule 3: repeated characters such as "soooo" or "amazingggg"
        if (lower.matches(".*(.)\\1\\1+.*")) {
            points += 15;
            reasons.add("Contains repeated characters");
        }

        // Rule 4: very short review
        String[] words = review.trim().split("\\s+");
        if (words.length <= 4) {
            points += 20;
            reasons.add("Review is unusually short");
        }

        // Rule 5: excessive capital letters
        long letters = review.chars().filter(Character::isLetter).count();
        long capitals = review.chars().filter(Character::isUpperCase).count();
        if (letters >= 8 && capitals * 100 / letters >= 70) {
            points += 15;
            reasons.add("Uses an unusually high amount of capital letters");
        }

        // Rule 6: duplicate-looking words
        Set<String> unique = new HashSet<>();
        boolean duplicate = false;
        for (String word : words) {
            String clean = word.toLowerCase().replaceAll("[^a-z0-9]", "");
            if (!clean.isEmpty() && !unique.add(clean)) {
                duplicate = true;
                break;
            }
        }
        if (duplicate && words.length >= 6) {
            points += 10;
            reasons.add("Contains repeated words");
        }

        double score = Math.min(points, 100);
        String label;
        String reason;

        if (score >= 50) {
            label = "Likely Fake";
        } else if (score >= 25) {
            label = "Suspicious";
        } else {
            label = "Likely Genuine";
        }

        if (reasons.isEmpty()) {
            reason = "No major suspicious patterns were detected.";
        } else {
            reason = String.join("; ", reasons) + ".";
        }

        return new Analysis(score, label, reason);
    }

    static class Analysis {
        double score;
        String label;
        String reason;

        Analysis(double score, String label, String reason) {
            this.score = score;
            this.label = label;
            this.reason = reason;
        }
    }

    static Map<String, String> parseForm(String body) throws UnsupportedEncodingException {
        Map<String, String> result = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";
            result.put(key, value);
        }
        return result;
    }

    static void serveFile(HttpExchange exchange, String fileName, String contentType) throws IOException {
        Path path = Path.of(fileName);

        if (!Files.exists(path)) {
            send(exchange, "File not found: " + fileName, "text/plain", 404);
            return;
        }

        byte[] data = Files.readAllBytes(path);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    static void send(HttpExchange exchange, String response, String contentType, int status) throws IOException {
        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
}
