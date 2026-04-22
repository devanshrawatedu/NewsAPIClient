import com.sun.net.httpserver.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.awt.Desktop;
import org.json.*;

public class NewsAPIClient {
    static String API_KEY;
    static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        loadEnv(); 
        
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("❌ ERROR: API key not found in .env (NEWSAPI_KEY=...) or system environment.");
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", exchange -> {
            sendResponse(exchange, getFullUI(), "text/html");
        });

        server.createContext("/api/news", exchange -> {
            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            String query = params.getOrDefault("q", "");
            String category = params.getOrDefault("c", "technology");

            try {
                String responseBody = fetchFromNewsAPI(query, category);
                sendResponse(exchange, responseBody, "application/json");
            } catch (Exception e) {
                sendResponse(exchange, "{\"error\":\"" + e.getMessage() + "\"}", "application/json");
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("🚀 Dashboard running at http://localhost:" + PORT);
        openBrowser(new URI("http://localhost:" + PORT));
    }

    private static void loadEnv() {
        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    if (line.startsWith("NEWSAPI_KEY=")) {
                        API_KEY = line.split("=", 2)[1].trim();
                    }
                }
            }
        } catch (Exception e) { /* fallback to system env */ }
        
        if (API_KEY == null) API_KEY = System.getenv("NEWSAPI_KEY");
    }

    private static String fetchFromNewsAPI(String q, String c) throws Exception {
        String url = q.isEmpty() 
            ? "https://newsapi.org" + c + "&apiKey=" + API_KEY
            : "https://newsapi.org" + URLEncoder.encode(q, "UTF-8") + "&apiKey=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static void sendResponse(HttpExchange exchange, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    static void openBrowser(URI uri) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
            } else {
                new ProcessBuilder("xdg-open", uri.toString()).start();
            }
        } catch (Exception e) { System.out.println("Open: " + uri); }
    }

    static String getFullUI() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>News Search</title>" +
            "<style>" +
            "body{font-family:sans-serif; background:#202124; color:#e8eaed; margin:0; display:flex; flex-direction:column; align-items:center;}" +
            ".search-container{margin-top:80px; text-align:center; width:90%; max-width:600px;}" +
            "h1{font-size:80px; margin-bottom:24px; background: linear-gradient(to right, #4285f4, #ea4335, #fbbc05, #34a853); -webkit-background-clip: text; -webkit-text-fill-color: transparent;}" +
            ".box{display:flex; background:#303134; border:1px solid #5f6368; border-radius:24px; padding:12px 20px; align-items:center;}" +
            "input{background:none; border:none; color:#e8eaed; flex:1; font-size:16px; outline:none;}" +
            "select{background:#303134; color:#9aa0a6; border:none; border-left:1px solid #5f6368; margin-left:10px; padding-left:10px; outline:none;}" +
            ".results{margin-top:40px; width:95%; max-width:850px; display:grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap:20px; padding:20px;}" +
            ".card{background:#303134; border-radius:12px; overflow:hidden; border:1px solid #3c4043; transition: 0.2s;}" +
            ".card:hover{transform: translateY(-5px); background:#3c4043;}" +
            ".card img{width:100%; height:160px; object-fit:cover;}" +
            ".card-content{padding:15px;}" +
            "h3{margin:0; font-size:18px;} h3 a{color:#8ab4f8; text-decoration:none;} p{font-size:14px; color:#bdc1c6; line-height:1.4;}" +
            "</style></head><body>" +
            "<div class='search-container'><h1>News</h1><div class='box'>" +
            "<input type='text' id='q' placeholder='Search news...' onkeypress='if(event.key===\"Enter\")search()'>" +
            "<select id='c' onchange='search()'>" +
                "<option value='technology'>Tech</option><option value='business'>Business</option>" +
                "<option value='science'>Science</option><option value='sports'>Sports</option>" +
            "</select></div></div>" +
            "<div id='results' class='results'></div>" +
            "<script>" +
            "async function search() {" +
            "  const q = encodeURIComponent(document.getElementById('q').value);" +
            "  const c = document.getElementById('c').value;" +
            "  const resDiv = document.getElementById('results');" +
            "  resDiv.innerHTML = '<div style=\"grid-column:1/-1;text-align:center\">Loading...</div>';" +
            "  const response = await fetch(`/api/news?q=${q}&c=${c}`);" +
            "  const data = await response.json();" +
            "  if (data.status === 'error') { resDiv.innerHTML = 'Error: ' + data.message; return; }" +
            "  resDiv.innerHTML = data.articles.map(a => `" +
            "    <div class='card'>" +
            "      ${a.urlToImage ? `<img src='${a.urlToImage}' onerror=\"this.style.display='none'\">` : ''}" +
            "      <div class='card-content'>" +
            "        <h3><a href='${a.url}' target='_blank'>${a.title}</a></h3>" +
            "        <p>${a.description || ''}</p>" +
            "      </div>" +
            "    </div>`).join('');" +
            "}" +
            "window.onload = search;" +
            "</script></body></html>";
    }
}

