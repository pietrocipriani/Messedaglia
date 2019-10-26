package it.gov.messedaglia.messedaglia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import it.gov.messedaglia.messedaglia.fragments.Register;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            Log.println(Log.ASSERT, "MainActivity", packageInfo.versionName);

            Http.get("https://api.github.com/repos/pietrocipriani/Messedaglia/releases").async(
                    r -> {
                        JSONObject obj = new JSONArray(r.body).getJSONObject(0);
                        if (!obj.getString("tag_name").equals("v"+packageInfo.versionName)) {
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new Register()).commit();
    }
}
