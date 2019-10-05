package it.gov.messedaglia.messedaglia.registerapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class RegisterApi {
    private final static String BASE_URL = "https://web.spaggiari.eu"; //   /rest/v1/auth/login"
    private final static String API_KEY = "Tg1NWEwNGIgIC0K";

    private static String username, password;
    private static String token;
    private static long tokenExpire = 0;

    private static Thread logThread = null;

    public static void updateCredentials (@NonNull String username, @NonNull String password, @Nullable Runnable then) {
        if (username.equals(RegisterApi.username) && password.equals(RegisterApi.password)) {
            if ((logThread != null && logThread.isAlive()) || tokenExpire >= System.currentTimeMillis()) return;
            logIn(then);
            return;
        }
        RegisterApi.username = username;
        RegisterApi.password = password;
        logIn(then);
    }

    private static JSONObject getJSONObject (String url, String method, String body, Map.Entry<String, String>... headers) throws IOException, JSONException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(body != null);
        for (Map.Entry<String, String> entry : headers) conn.setRequestProperty(entry.getKey(), entry.getValue());
        if (body != null) {
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(body);
            out.flush();
            out.close();
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder json = new StringBuilder();
        for (String str = in.readLine(); str != null; str = in.readLine()) json.append(str).append('\n');
        return new JSONObject(json.toString());
    }

    private static void logIn (Runnable then) {
        Thread t = new Thread(() -> {
            try {
                token = null;
                tokenExpire = 0;

                JSONObject obj = getJSONObject(
                        BASE_URL+"/rest/v1/auth/login",
                        "POST",
                        "{\"ident\":null,\"uid\":\""+username+"\",\"pass\":\""+password+"\"}",
                        new AbstractMap.SimpleEntry<>("Z-Dev-Apikey", API_KEY),
                        new AbstractMap.SimpleEntry<>("Content-Type", "application/json"),
                        new AbstractMap.SimpleEntry<>("User-Agent", "CVVS/std/1.7.9 Android/6.0")
                );

                token = obj.getString("token");
                tokenExpire = parseDate(obj.getString("expire"));
                Log.println(Log.ASSERT, "Register", String.valueOf(tokenExpire-System.currentTimeMillis()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (then != null) then.run();

        });
        if (logThread != null && logThread.isAlive()) logThread.interrupt();
        logThread = t;
        t.start();
    }

    private static long parseDate (String toParse) {
        String[] split = toParse.split("[-T:+]");
        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(split[0]), Integer.parseInt(split[1])-1, Integer.parseInt(split[2]),
                Integer.parseInt(split[3]), Integer.parseInt(split[4]), Integer.parseInt(split[5]));
        return c.getTimeInMillis();
    }

    public static void updateAll (Runnable then) {
        loadMarks(then);
    }

    public static void loadMarks (Runnable then) {
        new Thread(() -> {
            try {
                JSONArray array = getJSONObject(
                        BASE_URL+"/rest/v1/students/"+username.substring(1, username.length()-1)+"/grades2",
                        "GET",
                        null,
                        new AbstractMap.SimpleEntry<>("Z-Dev-Apikey", API_KEY),
                        new AbstractMap.SimpleEntry<>("Content-Type", "application/json"),
                        new AbstractMap.SimpleEntry<>("User-Agent", "CVVS/std/1.7.9 Android/6.0"),
                        new AbstractMap.SimpleEntry<>("Z-Auth-Token", token),
                        new AbstractMap.SimpleEntry<>("Z-If-None-Match", null)  // TODO: pass 'etag'
                ).getJSONArray("grades");

                for (int i=0; i<array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    MarksData.Subject subject = MarksData.data.get(obj.getInt("subjectId"));
                    if (subject == null) {
                        subject = new MarksData.Subject();
                        MarksData.data.put(obj.getInt("subjectId"), subject);
                    }
                    // TODO: complete mark parsing
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (then != null) then.run();

        }).start();
    }


    public static class MarksData {
        public static final HashMap<Integer, Subject> data = new HashMap<>();

        public static class Subject {
            public final TreeSet<Mark> marks = new TreeSet<>();
        }

        public static class Mark implements Comparable<Mark>{
            public double decimalValue;
            public String dislayValue;
            private int pos;

            @Override
            public int compareTo(Mark mark) {
                return Integer.compare(pos, mark.pos);
            }
        }
    }

}
