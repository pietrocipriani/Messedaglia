package it.gov.messedaglia.messedaglia;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import it.gov.messedaglia.messedaglia.fragments.BlankFragment;
import it.gov.messedaglia.messedaglia.fragments.DeskEditor;
import it.gov.messedaglia.messedaglia.fragments.Register;
import it.gov.messedaglia.messedaglia.fragments.TimeTable;

public class MainActivity extends AppCompatActivity {
    private Register register = new Register();
    private TimeTable timeTable = new TimeTable();
    private DeskEditor deskEditor = new DeskEditor();

    private static final boolean RESET_DATA = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            if (RESET_DATA) ifNewVersionDeleteData(versionName);

            Http.get("https://api.github.com/repos/pietrocipriani/Messedaglia/releases").async(
                    r -> {
                        JSONObject obj = new JSONArray(r.body).getJSONObject(0);
                        if (!obj.getString("tag_name").equals("v"+versionName)) {
                            String url = obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");

                            Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator), "Nuovo aggiornamento!", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("AGGIORNA!", v -> {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(url));
                                        startActivity(intent);
                                    });
                            snackbar.setActionTextColor(0xFFFFFFFF);
                            snackbar.show();
                        }
                    }
            );
        } catch (IOException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        BottomNavigationView bnv = findViewById(R.id.navigation_bar);
        bnv.setOnNavigationItemSelectedListener(menuItem -> {
            changeFragment(menuItem.getItemId());
            return true;
        });

        changeFragment(bnv.getSelectedItemId());

    }

    private void changeFragment (int id) {
        Fragment frg;
        switch (id) {
            case R.id.main_navigation_bar_menu_register:
                frg = register;
                break;
            case R.id.main_navigation_bar_menu_calendar:
                frg = new BlankFragment();
                break;
            case R.id.main_navigation_bar_menu_timetable:
                frg = timeTable;
                break;
            case R.id.main_navigation_bar_menu_desk_editor:
                frg = deskEditor;
                break;
            default: throw new IllegalArgumentException(id+" is not a valid id!");
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, frg).commit();
    }

    private void ifNewVersionDeleteData (String versionName) throws IOException {
        File file = new File(getFilesDir(), "lastVersion.data");
        String oldVersionName;
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            oldVersionName = reader.readLine();
            reader.close();
        } else oldVersionName = "null";

        if (versionName.equals(oldVersionName)) return;

        File[] files = getFilesDir().listFiles();
        for (File f : files) f.delete();
        file.createNewFile();
        PrintWriter writer = new PrintWriter(file);
        writer.println(versionName);
        writer.flush();
        writer.close();
    }
}
