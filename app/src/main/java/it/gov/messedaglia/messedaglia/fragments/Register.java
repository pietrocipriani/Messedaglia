package it.gov.messedaglia.messedaglia.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import it.gov.messedaglia.messedaglia.R;
import it.gov.messedaglia.messedaglia.fragments.register.MarksFragment;
import it.gov.messedaglia.messedaglia.fragments.register.RegisterFragment;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi;
import it.gov.messedaglia.messedaglia.Utils;

public class Register extends Fragment {
    private TextView lastUpdateView;

    private RegisterFragment currentFragment;

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
        //prefs = context.getSharedPreferences("register", Context.MODE_PRIVATE);

        if (!RegisterApi.load(context)){
            if (!RegisterApi.logWithCredentials(() -> RegisterApi.updateAll(null))) {
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
            }
        } else RegisterApi.updateAll(null);

        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    private void setFragment (RegisterFragment fragment) {
        getChildFragmentManager().beginTransaction().replace(R.id.fragment2, fragment).commit();
        currentFragment = fragment;
        if (lastUpdateView == null) return;
        lastUpdateView.setText("Ultimo aggiornamento: ");
        lastUpdateView.append(Utils.intervalToString(currentFragment.getLastUpdate()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lastUpdateView = view.findViewById(R.id.last_update);

        setFragment(new MarksFragment());

        Handler handler = new Handler();

        Runnable run = new Runnable() {
            @Override
            public void run() {
                lastUpdateView.setText("Ultimo aggiornamento: ");
                lastUpdateView.append(Utils.intervalToString(currentFragment != null ? System.currentTimeMillis()-currentFragment.getLastUpdate() : -1));
                handler.postDelayed(this, 6000);
            }
        };

        handler.post(run);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RegisterApi.save(getContext());
    }
}
