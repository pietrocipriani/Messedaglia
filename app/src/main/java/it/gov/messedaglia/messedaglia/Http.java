package it.gov.messedaglia.messedaglia;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Http {

    public static Request get (String url, String... headers) throws IOException {
        if (headers.length %2 != 0) throw new IllegalArgumentException("headers must be in pair key-value, passed an odd number of strings");

        Map<String, List<String>> headersMap = new HashMap<>();

        for (int i = 0; i < headers.length; i+=2) {
            String key = headers[i], value = headers[i+1];
            if (!headersMap.containsKey(key)) {
                ArrayList<String> list = new ArrayList<>();
                list.add(value);
                headersMap.put(key, list);
            } else headersMap.get(key).add(value);
        }

        return new Request("GET", new URL(url), null, headersMap);
    }

    public static Request post (String url, String body, String... headers) throws IOException {
        if (headers.length %2 != 0) throw new IllegalArgumentException("headers must be in pair key-value, passed an odd number of strings");

        Map<String, List<String>> headersMap = new HashMap<>();

        for (int i = 0; i < headers.length; i+=2) {
            String key = headers[i], value = headers[i+1];
            if (!headersMap.containsKey(key)) {
                ArrayList<String> list = new ArrayList<>();
                list.add(value);
                headersMap.put(key, list);
            } else headersMap.get(key).add(value);
        }

        return new Request("POST", new URL(url), body, headersMap);
    }

    public static class Request {
        public final String method;
        public final String body;
        public final URL url;
        public final Map<String, List<String>> headers;

        Request (String method, URL url, String body, Map<String, List<String>> headers) {
            this.method = method;
            this.body = body;
            this.url = url;
            this.headers = headers;
        }

        public Thread async (Then then) {
            Thread t = new Thread(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(method);
                    for (Map.Entry<String, List<String>> entry : headers.entrySet())
                        for (String value : entry.getValue()) connection.setRequestProperty(entry.getKey(), value);
                    if (body != null) {
                        connection.setDoOutput(true);
                        PrintWriter writer = new PrintWriter(connection.getOutputStream());
                        writer.write(body);
                        writer.flush();
                        writer.close();
                    }
                    InputStream stream = connection.getResponseCode() < 400 ? connection.getInputStream() : connection.getErrorStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder body = new StringBuilder();
                    for (String str = reader.readLine(); str != null; str = reader.readLine()) body.append(str).append('\n');

                    reader.close();

                    then.then(new Response(body.toString(), connection.getResponseCode(), connection.getHeaderFields(), this));
                } catch (IOException e){
                    e.printStackTrace();
                } catch (JSONException e){
                    e.printStackTrace();
                }
            });
            t.start();
            return t;
        }

        public Response sync () throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            for (Map.Entry<String, List<String>> entry : headers.entrySet())
                for (String value : entry.getValue()) connection.setRequestProperty(entry.getKey(), value);
            if (body != null) {
                connection.setDoOutput(true);
                PrintWriter writer = new PrintWriter(connection.getOutputStream());
                writer.write(body);
                writer.flush();
                writer.close();
            }
            InputStream stream = connection.getResponseCode() < 400 ? connection.getInputStream() : connection.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder body = new StringBuilder();
            for (String str = reader.readLine(); str != null; str = reader.readLine()) body.append(str).append('\n');

            reader.close();

            return new Response(body.toString(), connection.getResponseCode(), connection.getHeaderFields(), this);
        }

        public interface Then {
            void then (Response r) throws JSONException;
        }

    }


    public static class Response {
        public final String body;
        public final int responseCode;
        public final Map<String, List<String>> headers;
        public final Request request;

        Response (String body, int responseCode, Map<String, List<String>> headers, Request request) {
            this.body = body;
            this.responseCode = responseCode;
            this.headers = headers;
            this.request = request;
        }
    }
}
