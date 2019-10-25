package it.gov.messedaglia.messedaglia.registerapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.animation.Interpolator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import it.gov.messedaglia.messedaglia.SortedList;

public class RegisterApi {
    private final static String TAG = "RegisterApi";

    private final static String BASE_URL = "https://web.spaggiari.eu"; //   /rest/v1/auth/login"
    private final static String API_KEY = "Tg1NWEwNGIgIC0K";

    private static String username, password;
    private static String token;
    private static long tokenExpire = 0;

    private static Thread logThread = null;

    public static Runnable onMarksUpdate;

    public static boolean load (Context context) {
        File file = new File(context.getFilesDir(), "login.data");
        if (!file.exists()) return false;
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))){
            byte bytes[] = new byte[dis.readByte()];
            dis.readFully(bytes);
            username = new String(bytes);
            bytes = new byte[dis.readByte()];
            dis.readFully(bytes);
            password = new String(bytes);
            tokenExpire = dis.readLong();
            bytes = new byte[dis.readShort()];
            dis.readFully(bytes);
            token = new String(bytes);

            // TODO: load marks data
            loadMarks(context);
        } catch (IOException e){
            e.printStackTrace();
        }
        return token != null && tokenExpire > System.currentTimeMillis();
    }
    public static void save (Context context){
        File file = new File(context.getFilesDir(), "login.data");
        Log.println(Log.ASSERT, TAG, file.getAbsolutePath());
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
            byte bytes[] = username.getBytes();
            dos.writeByte(bytes.length);
            dos.write(bytes);
            bytes = password.getBytes();
            dos.writeByte(bytes.length);
            dos.write(bytes);
            dos.writeLong(tokenExpire);
            bytes = token == null ? new byte[0] : token.getBytes();
            dos.writeShort(bytes.length);
            dos.write(bytes);

            // TODO: save data
            saveMarks(context);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void saveMarks (Context context) throws IOException{
        File file = new File(context.getFilesDir(), "marks.data");

        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        byte bytes[];

        if (MarksData.etag == null) dos.writeByte(0);
        else {
            bytes = MarksData.etag.getBytes();
            dos.writeByte(bytes.length);
            dos.write(bytes);
        }   // print 'etag'

        dos.writeShort(MarksData.marks.size()); // print num of marks

        for (int i = 0; i < MarksData.marks.size(); i++) {
            MarksData.Mark mark = MarksData.marks.valueAt(i);

            dos.writeInt(MarksData.marks.keyAt(i)); // print id
            dos.writeInt(mark.subjectId);           // print subject id
            dos.writeLong(mark.date);               // print date
            dos.writeFloat(mark.decimalValue);      // print value

            bytes = mark.displayValue.getBytes();
            dos.writeByte(bytes.length);
            dos.write(bytes);                       // print value as string

            dos.writeByte(mark.pos);                // print position

            bytes = mark.info.getBytes();
            dos.writeShort(bytes.length);
            dos.write(bytes);                       // print info

            dos.writeByte(mark.period);             // print period
        }

        dos.flush();
        dos.close();

        saveSubjects(context);
    }
    private static void saveSubjects (Context context) throws IOException {
        File file = new File(context.getFilesDir(), "subjects.data");

        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        byte bytes[];

        dos.writeByte(MarksData.data.size()); // print num of subjects

        for (int i = 0; i < MarksData.data.size(); i++) {
            MarksData.Subject sbj = MarksData.data.valueAt(i);

            dos.writeInt(MarksData.data.keyAt(i)); // print id

            bytes = sbj.name.getBytes();
            dos.writeByte(bytes.length);
            dos.write(bytes);                       // print name
        }

        dos.flush();
        dos.close();
    }

    private static void loadMarks (Context context) throws IOException {
        loadSubjects (context);

        File file = new File(context.getFilesDir(), "marks.data");
        if (!file.exists()) return;

        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int n;
        byte bytes[];

        n = dis.readByte();
        if (n == 0) MarksData.etag = null;
        else {
            bytes = new byte[n];
            dis.readFully(bytes);
            MarksData.etag = new String(bytes);
        }

        n = dis.readShort();
        for (int i = 0; i < n; i++) {
            int m;

            int id = dis.readInt();
            int subject = dis.readInt();
            long date = dis.readLong();
            float decimalValue = dis.readFloat();

            m = dis.readByte();
            bytes = new byte[m];
            dis.readFully(bytes);
            String displayValue = new String(bytes);

            byte pos = dis.readByte();

            m = dis.readShort();
            bytes = new byte[m];
            dis.readFully(bytes);
            String info = new String(bytes);

            byte period = dis.readByte();

            new MarksData.Mark(id, subject, null, date, decimalValue, displayValue, pos, info, period, true, (byte) 0);
        }

        dis.close();
    }

    private static void loadSubjects (Context context) throws IOException {
        File file = new File(context.getFilesDir(), "subjects.data");
        if (!file.exists()) return;

        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int n;
        byte bytes[];

        n = dis.readByte();
        for (int i = 0; i < n; i++) {
            int id = dis.readInt();

            byte m = dis.readByte();
            bytes = new byte[m];
            dis.readFully(bytes);
            String name = new String(bytes);

            MarksData.data.put(id, new MarksData.Subject(name));

        }

        dis.close();
    }

    public static void updateCredentials (@NonNull String username, @NonNull String password, @Nullable Runnable then) {
        if (username.equals(RegisterApi.username) && password.equals(RegisterApi.password)) {
            if ((logThread != null && logThread.isAlive()) || tokenExpire >= System.currentTimeMillis()) return;
        } else {
            RegisterApi.username = username;
            RegisterApi.password = password;
        }
        logIn(then);
    }
    public static boolean logWithCredentials (@Nullable Runnable then) {
        if (username == null || password == null) return false;
        logIn(then);
        return true;
    }

    private static JSONObjectResponse getJSONObject (String url, String method, String body, String... headers) throws IOException, JSONException {
        if (headers.length %2 != 0) throw new IllegalArgumentException("headers must be in pairs (key-value).");
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(body != null);
        for (int i=0; i<headers.length; i+=2) conn.setRequestProperty(headers[i], headers[i+1]);
        if (body != null) {
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(body);
            out.flush();
            out.close();
        }

        if (conn.getResponseCode() >= 400) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

            StringBuilder json = new StringBuilder();
            for (String str = in.readLine(); str != null; str = in.readLine()) json.append(str).append('\n');
            Log.println(Log.ASSERT, TAG, new JSONObject(json.toString()).toString(4));
            return null;
        } else if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) return null;

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder json = new StringBuilder();
        for (String str = in.readLine(); str != null; str = in.readLine()) json.append(str).append('\n');
        return new JSONObjectResponse(json.toString(), conn.getHeaderFields());
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
                        "Z-Dev-Apikey", API_KEY,
                        "Content-Type", "application/json",
                        "User-Agent", "CVVS/std/1.7.9 Android/6.0"
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
        if (split.length == 3)
            c.set(Integer.parseInt(split[0]), Integer.parseInt(split[1])-1, Integer.parseInt(split[2]));
        else
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
                JSONObjectResponse response = getJSONObject(
                        BASE_URL+"/rest/v1/students/"+username.substring(1, username.length()-1)+"/grades2",
                        "GET",
                        null,
                        "Z-Dev-Apikey", API_KEY,
                        "Content-Type", "application/json",
                        "User-Agent", "CVVS/std/1.7.9 Android/6.0",
                        "Z-Auth-Token", token,
                        "Z-If-None-Match", MarksData.etag
                );
                if (response == null) return;

                JSONArray array = response.getJSONArray("grades");

                Log.println(Log.ASSERT, TAG, response.responseHeaders.toString());

                MarksData.etag = response.responseHeaders.get("Etag").get(0);

                Log.println(Log.ASSERT, TAG, array.toString(4));

                for (int i=0; i<array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    MarksData.Mark old = MarksData.marks.get(obj.getInt("evtId"));
                    if (old == null){
                        new MarksData.Mark(
                                obj.getInt("evtId"),
                                obj.getInt("subjectId"),
                                obj.getString("subjectDesc"),
                                parseDate(obj.getString("evtDate")),
                                (float) obj.optDouble("decimalValue", -1),
                                obj.getString("displayValue"),
                                (byte) obj.getInt("displaPos"),
                                obj.getString("notesForFamily"),
                                (byte) obj.getInt("periodPos"),
                                true,
                                (byte) -1
                        );
                    }
                }
                MarksData.lastUpdate = System.currentTimeMillis();

                Log.println(Log.ASSERT, TAG, MarksData.data.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (then != null) then.run();
            Log.println(Log.ASSERT, TAG, ""+onMarksUpdate);
            if (onMarksUpdate != null) onMarksUpdate.run();

        }).start();
    }


    public static class MarksData {
        public static final SparseArray<Subject> data = new SparseArray<>();
        public static final SparseArray<Mark> marks = new SparseArray<>();

        private static String etag = null;

        public static long lastUpdate = 0;

        public static class Subject {
            public final SortedList<Mark> marks = new SortedList<>();
            public final String name;
            private double average = 0;
            private byte newCount = 0;

            Subject (String name) {
                this.name = name;
            }

            void addMark (Mark mark){
                average = (average*marks.size()+mark.decimalValue)/(marks.size()+1);
                if (mark.hasNew < 0) newCount++;
                marks.add(mark);
            }

            public float getAverage () {
                return Math.round(average*10f)/10f;
            }
            public byte getNewCount () {
                return newCount;
            }

            @NonNull
            @Override
            public String toString (){
                return marks.toString();
            }
        }

        public static class Mark implements Comparable<Mark>{
            public final long date;
            public final int subjectId;
            public final float decimalValue;
            public final String displayValue;
            public final String info;
            private final byte pos;
            public final byte period;
            public final byte hasNew;

            public Mark (int id, int subjectId, String subjectName, long date, float decimalValue, String displayValue, byte pos, String info, byte period, boolean save, byte hasNew){
                this.date = date;
                this.decimalValue = decimalValue;
                this.displayValue = displayValue;
                this.pos = pos;
                this.info = info;
                this.period = period;
                this.hasNew = hasNew;
                this.subjectId = subjectId;

                if (save) {
                    MarksData.marks.put(id, this);
                    Subject sbj = MarksData.data.get(subjectId, new Subject(subjectName));
                    sbj.addMark(this);
                    MarksData.data.put(subjectId, sbj);
                }
            }

            public Mark (float decimalValue, byte hasNew) {
                this (0, 0, null, 0L, decimalValue, decimalValue == 10 ? "10" : String.valueOf(decimalValue), (byte) 0, null, (byte) 0x0, false, hasNew);
            }

            @NonNull
            @Override
            public String toString (){
                return displayValue;
            }

            @Override
            public int compareTo(Mark mark) {
                return Integer.compare(pos, mark.pos);
            }
        }
    }

}

class JSONObjectResponse extends JSONObject{
    Map<String, List<String>> responseHeaders;

    JSONObjectResponse (String json, Map<String, List<String>> responseHeaders) throws JSONException{
        super(json);
        this.responseHeaders = responseHeaders;
    }
}
