package promo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Test {

    private static final String BASE_URL = "http://localhost:8000/";

    public static void main(String[] args) throws IOException {
        testPostShortLink();
        testGetLongLink();
        testGetNonExistentLink();
        testUnsupportedMethod();
    }

    private static void testPostShortLink() throws IOException {
        String longUrl = "https://promo-z.ru/";
        String jsonInputString = "{\"longUrl\":\"" + longUrl + "\"}";

        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonInputString.getBytes());
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("POST Response Code: " + responseCode);
    }

    private static void testGet
