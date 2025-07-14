import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TestWebServer {
    private static HttpServer server;
    
    public static void main(String[] args) {
        try {
            int port = 8081;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // Serve static files (HTML, CSS, JS, etc.)
            server.createContext("/", new StaticFileHandler());
            
            // API endpoints
            server.createContext("/api/moderation/config/", new ConfigHandler());
            server.createContext("/api/moderation/filters/", new FiltersHandler());
            server.createContext("/api/moderation/actions/", new ActionsHandler());
            server.createContext("/api/moderation/templates/", new TemplatesHandler());
            
            server.setExecutor(null);
            server.start();
            
            System.out.println("Test web server started on port " + port);
            System.out.println("Dashboard: http://localhost:" + port + "/moderation-dashboard.html");
            System.out.println("Press Ctrl+C to stop");
            
            // Keep running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                server.stop(0);
            }));
            
            Thread.currentThread().join();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static class StaticFileHandler implements HttpHandler {
        private static final Map<String, String> MIME_TYPES = new HashMap<>();
        
        static {
            MIME_TYPES.put(".html", "text/html");
            MIME_TYPES.put(".css", "text/css");
            MIME_TYPES.put(".js", "application/javascript");
            MIME_TYPES.put(".json", "application/json");
            MIME_TYPES.put(".svg", "image/svg+xml");
            MIME_TYPES.put(".png", "image/png");
            MIME_TYPES.put(".jpg", "image/jpeg");
            MIME_TYPES.put(".jpeg", "image/jpeg");
            MIME_TYPES.put(".ico", "image/x-icon");
        }
        
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String requestPath = exchange.getRequestURI().getPath();
                
                // Skip API endpoints
                if (requestPath.startsWith("/api/")) {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().close();
                    return;
                }
                
                // Default to index.html for root path
                if ("/".equals(requestPath)) {
                    requestPath = "/index.html";
                }
                
                // Try website directory first, then static directory
                String filePath = "website" + requestPath;
                if (!Files.exists(Paths.get(filePath))) {
                    filePath = "src/main/resources/static" + requestPath;
                }
                
                if (Files.exists(Paths.get(filePath))) {
                    byte[] response = Files.readAllBytes(Paths.get(filePath));
                    
                    // Set appropriate content type
                    String contentType = getContentType(filePath);
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    
                    // Add CORS headers for API-like requests
                    if (filePath.endsWith(".js") || filePath.endsWith(".css")) {
                        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    }
                    
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                } else {
                    String notFound = "<h1>File not found</h1><p>Looking for: " + filePath + "</p>";
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(404, notFound.length());
                    exchange.getResponseBody().write(notFound.getBytes());
                }
            } catch (Exception e) {
                String error = "Error: " + e.getMessage();
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
            }
            exchange.getResponseBody().close();
        }
        
        private String getContentType(String filePath) {
            String extension = "";
            int lastDot = filePath.lastIndexOf('.');
            if (lastDot > 0) {
                extension = filePath.substring(lastDot).toLowerCase();
            }
            return MIME_TYPES.getOrDefault(extension, "text/plain");
        }
    }
    
    static class ConfigHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
                return;
            }
            
            String response;
            if ("GET".equals(method)) {
                // Return sample configuration
                response = "{\"success\": true, \"data\": {\"guildId\": \"123456789\", \"spamFilter\": {\"enabled\": true, \"threshold\": 5}, \"toxicityFilter\": {\"enabled\": true, \"threshold\": 0.7}, \"linkFilter\": {\"enabled\": false}, \"wordFilter\": {\"enabled\": true, \"words\": [\"badword1\", \"badword2\"]}, \"actions\": {\"warn\": true, \"mute\": true, \"kick\": false, \"ban\": false}, \"thresholds\": {\"warnThreshold\": 3, \"muteThreshold\": 5, \"kickThreshold\": 7, \"banThreshold\": 10}}}";
            } else if ("PUT".equals(method)) {
                // Read the request body
                BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
                
                System.out.println("Received configuration update: " + requestBody.toString());
                response = "{\"success\": true, \"message\": \"Configuration saved successfully\"}";
            } else {
                response = "{\"success\": false, \"error\": \"Method not allowed\"}";
            }
            
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
    
    static class FiltersHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            String response = "{\"success\": true, \"data\": {\"spam\": [\"basic\", \"advanced\", \"strict\"], \"toxicity\": [\"low\", \"medium\", \"high\"], \"link\": [\"whitelist\", \"blacklist\", \"disabled\"], \"word\": [\"custom\", \"preset\", \"disabled\"]}}";
            
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
    
    static class ActionsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            String response = "{\"success\": true, \"data\": [\"warn\", \"mute\", \"kick\", \"ban\", \"delete_message\", \"timeout\"]}";
            
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
    
    static class TemplatesHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            String response = "{\"success\": true, \"data\": [{\"id\": \"strict\", \"name\": \"Strict Moderation\", \"description\": \"High security settings\"}, {\"id\": \"community\", \"name\": \"Community Friendly\", \"description\": \"Balanced moderation\"}, {\"id\": \"gaming\", \"name\": \"Gaming Server\", \"description\": \"Relaxed settings for gaming\"}]}";
            
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
}