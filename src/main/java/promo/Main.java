package promo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShortLinkService {

    // Модель
    static class UrlLink {
        private String longUrl;
        private String shortUrl;

        public UrlLink(String longUrl, String shortUrl) {
            this.longUrl = longUrl;
            this.shortUrl = shortUrl;
        }

        public String getLongUrl() {
            return longUrl;
        }

        public String getShortUrl() {
            return shortUrl;
        }
    }

    // Репозиторий
    static class LinkRepository {
        private final Map<String, UrlLink> links = new ConcurrentHashMap<>();
        private int counter = 0;

        public String save(String longUrl) {
            String shortUrl = "https://promo-z.ru/" + (++counter);
            links.put(shortUrl, new UrlLink(longUrl, shortUrl));
            return shortUrl;
        }

        public String getLongUrl(String shortUrl) {
            UrlLink link = links.get(shortUrl);
            return link != null ? link.getLongUrl() : null;
        }
    }

    // Контроллер
    static class LinkController implements HttpHandler {
        private final LinkRepository linkRepository = new LinkRepository();
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;
            int statusCode;

            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    String longUrl = objectMapper.readTree(requestBody).get("longUrl").asText();
                    String shortUrl = linkRepository.save(longUrl);
                    response = objectMapper.writeValueAsString(Map.of("shortUrl", shortUrl));
                    statusCode = 200;
                } else if ("GET".equals(exchange.getRequestMethod())) {
                    String shortUrl = "https://promo-z.ru" + exchange.getRequestURI().getPath();
                    String longUrl = linkRepository.getLongUrl(shortUrl);
                    response = longUrl != null ? longUrl : "Не найдено";
                    statusCode = longUrl != null ? 200 : 404;
                } else {
                    response = "Неподдерживаемый метод";
                    statusCode = 405; // Method Not Allowed
                }
            } catch (Exception e) {
                response = "Ошибка обработки запроса: " + e.getMessage();
                statusCode = 500; // Internal Server Error
            }

            exchange.sendResponseHeaders(statusCode, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new LinkController());
        server.setExecutor(null); // создает стандартный исполнитель
        server.start();
        System.out.println("Сервер запущен на порту 8000");
    }
}