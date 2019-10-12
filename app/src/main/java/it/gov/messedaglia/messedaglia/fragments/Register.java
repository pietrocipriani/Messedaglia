package it.gov.messedaglia.messedaglia.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import it.gov.messedaglia.messedaglia.R;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi;
import it.gov.messedaglia.messedaglia.Utils;

public class Register extends Fragment {

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    static boolean logged = false;
    static String token = null;

    public Register() {}

    public static Register newInstance() {
        return new Register();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context context = inflater.getContext();
        prefs = context.getSharedPreferences("register", Context.MODE_PRIVATE);

        if (!RegisterApi.load(getContext())){
            // TODO: maybe the token expired, try to do a new login with saved data

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setTitle("Login");
            dialogBuilder.setMessage("Esegui il login per accedere alla tua area personale");

            LinearLayout root = new LinearLayout(context);
            EditText username = new EditText(context);
            EditText password = new EditText(context);

            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(Utils.dpToPx(22), 0, Utils.dpToPx(22), 0);

            username.setHint("username");
            username.setInputType(InputType.TYPE_CLASS_TEXT);

            password.setHint("password");
            password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);

            dialogBuilder.setPositiveButton("ACCEDI", (DialogInterface dialogInterface, int i) ->
                    RegisterApi.updateCredentials(username.getText().toString(), password.getText().toString(), () -> RegisterApi.updateAll(null))
            );

            root.addView(username, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            root.addView(password, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            dialogBuilder.setView(root);
            dialogBuilder.create().show();
        } else RegisterApi.updateAll(null);

        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.println(Log.ASSERT, "Register", "onDestroy");
        RegisterApi.save(getContext());
    }
}
