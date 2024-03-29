package it.gov.messedaglia.messedaglia.registerapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

import it.gov.messedaglia.messedaglia.Http;
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
    public static Runnable onNoticesUpdate;

    public static boolean load (Context context) {
        File file = new File(context.getFilesDir(), "login.data");
        if (!file.exists()) return false;
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))){
            byte[] bytes = new byte[dis.readByte()];
            dis.readFully(bytes);
            username = new String(bytes);
            bytes = new byte[dis.readByte()];
            dis.readFully(bytes);
            password = new String(bytes);
            tokenExpire = dis.readLong();
            bytes = new byte[dis.readShort()];
            dis.readFully(bytes);
            token = new String(bytes);

            loadMarks(context);
        } catch (IOException e){
            e.printStackTrace();
        }
        return token != null && tokenExpire > System.currentTimeMillis();
    }
    public static void save (Context context){
        File file = new File(context.getFilesDir(), "login.data");
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
            byte[] bytes = username.getBytes();
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
        byte[] bytes;

        dos.writeLong(MarksData.lastUpdate);

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
        byte[] bytes;

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
        byte[] bytes;

        MarksData.lastUpdate = dis.readLong();

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
        byte[] bytes;

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

    public static void updateCredentials (@NonNull String username, @NonNull String password, @Nullable OnLogin then) {
        if (username.equals(RegisterApi.username) && password.equals(RegisterApi.password)) {
            if ((logThread != null && logThread.isAlive()) || tokenExpire >= System.currentTimeMillis()) return;
        } else {
            RegisterApi.username = username;
            RegisterApi.password = password;
        }
        logIn(then);
    }
    public static boolean logWithCredentials (@Nullable OnLogin then) {
        if (username == null || password == null) return false;
        logIn(then);
        return true;
    }

    private static void logIn (OnLogin then) {
        try {
            token = null;
            tokenExpire = 0;

            if (logThread != null && logThread.isAlive()) logThread.interrupt();
            logThread = Http.post(
                    BASE_URL+"/rest/v1/auth/login",
                    "{\"ident\":null,\"uid\":\""+username+"\",\"pass\":\""+password+"\"}",
                    "Z-Dev-Apikey", API_KEY,
                    "Content-Type", "application/json",
                    "User-Agent", "CVVS/std/1.7.9 Android/6.0"
            ).async(r -> {
                if (r.responseCode != HttpURLConnection.HTTP_OK) {
                    then.then(false);
                    return;
                }

                JSONObject obj = new JSONObject(r.body);

                token = obj.getString("token");
                tokenExpire = parseDate(obj.getString("expire"));

                if (then != null) then.then(true);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public static void updateAll (@Nullable Runnable then) {
        loadMarks(then);
        loadNoticeBoard(then);
    }

    public static void loadMarks (@Nullable Runnable then) {

        try {
            Http.get(
                    BASE_URL+"/rest/v1/students/"+username.substring(1, username.length()-1)+"/grades2",
                    "Z-Dev-Apikey", API_KEY,
                    "Content-Type", "application/json",
                    "User-Agent", "CVVS/std/1.7.9 Android/6.0",
                    "Z-Auth-Token", token,
                    "Z-If-None-Match", MarksData.etag
            ).async(r -> {
                if (r.responseCode >= 400)  return;
                MarksData.lastUpdate = System.currentTimeMillis();
                if (r.responseCode == HttpURLConnection.HTTP_NOT_MODIFIED){
                    if (then != null) then.run();
                    return;
                }
                JSONArray array = new JSONObject(r.body).getJSONArray("grades");
                MarksData.etag = Objects.requireNonNull(r.headers.get("Etag")).get(0);

                for (int i=0; i<array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    MarksData.Mark old = MarksData.marks.get(obj.getInt("evtId"));
                    if (old == null)
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

                if (then != null) then.run();
                if (onMarksUpdate != null) onMarksUpdate.run();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void loadNoticeBoard (@Nullable Runnable then ) {
        try {
            Http.get(
                    BASE_URL+"/rest/v1/students/"+username.substring(1, username.length()-1)+"/noticeboard",
                    "Z-Dev-Apikey", API_KEY,
                    "Content-Type", "application/json",
                    "User-Agent", "CVVS/std/1.7.9 Android/6.0",
                    "Z-Auth-Token", token,
                    "Z-If-None-Match", Notices.etag
            ).async(r -> {
                if (r.responseCode >= 400) return;
                Notices.lastUpdate = System.currentTimeMillis();
                if (r.responseCode == HttpURLConnection.HTTP_NOT_MODIFIED){
                    if (then != null) then.run();
                    return;
                }

                JSONArray array = new JSONObject(r.body).getJSONArray("items");
                Notices.etag = Objects.requireNonNull(r.headers.get("Etag")).get(0);

                for (int i=0; i<array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Notices.Notice old = Notices.notices.get(obj.getInt("pubId"));
                    if (old == null) {
                        JSONArray attachmentsJSON = obj.getJSONArray("attachments");
                        int[] attachments = new int[attachmentsJSON.length()];
                        for (int j = 0; j < attachments.length; j++) attachments[j] = attachmentsJSON.getJSONObject(j).getInt("attachNum");
                        new Notices.Notice(
                                obj.getInt("pubId"),
                                parseDate(obj.getString("pubDT")),
                                obj.getBoolean("readStatus"),
                                obj.getString("evtCode"),
                                obj.getBoolean("cntValidInRange") && obj.getString("cntStatus").equals("active"),
                                obj.getString("cntTitle"),
                                attachments
                        );
                    }
                }

                if (then != null) then.run();
                if (onNoticesUpdate != null) onNoticesUpdate.run();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class MarksData {
        public static final SparseArray<Subject> data = new SparseArray<>();
        public static final SparseArray<Mark> marks = new SparseArray<>();

        private static String etag = null;

        public static long lastUpdate = 0;

        public static class Subject {
            public final SortedList<Mark> marks = new SortedList<>();
            public final SortedList<Mark> nonBlueMarks = new SortedList<>();
            public final String name;
            private double average = 0;
            private byte newCount = 0;

            Subject (String name) {
                this.name = name;
            }

            void addMark (Mark mark){
                if (!mark.isBlue()) {
                    average += mark.decimalValue;
                    nonBlueMarks.add(mark);
                }

                if (mark.hasNew < 0) newCount++;
                marks.add(mark);
            }

            public float getAverage () {
                return Math.round(average/nonBlueMarks.size()*10f)/10f;
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
            final long date;
            final int subjectId;
            public final float decimalValue;
            public final String displayValue;
            final String info;
            private final byte pos;
            final byte period;
            public final byte hasNew;

            Mark (int id, int subjectId, String subjectName, long date, float decimalValue, String displayValue, byte pos, String info, byte period, boolean save, byte hasNew){
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

            public boolean isBlue () {
                return decimalValue < 0;
            }
        }
    }
    public static class Notices {
        public static final SparseArray<Notice> notices = new SparseArray<>();

        private static String etag = null;

        public static long lastUpdate = 0;

        public static class Notice {
            public final long date;
            public final boolean read, valid;
            public final String evt, title;
            public final int[] attachments;

            Notice (int id, long date, boolean read, String evt, boolean valid, String title, int... attachments) {
                this.date = date;
                this.read = read;
                this.evt = evt;
                this.valid = valid;
                this.title = title;
                this.attachments = attachments;

                notices.put(id, this);
            }

            @NonNull
            @Override
            public String toString() {
                return "{\n\tdate: "+date+"\n\tread: "+read+"\n\t"+"valid: "+valid+"\n\tevt: "+evt+"\n\ttitle: "+title+"\n\tattachments: "+ Arrays.toString(attachments) +"\n}";
            }
        }
    }

    public interface OnLogin {
        void then (boolean logged);
    }

}
