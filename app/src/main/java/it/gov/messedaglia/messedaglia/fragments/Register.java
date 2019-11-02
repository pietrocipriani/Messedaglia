package it.gov.messedaglia.messedaglia.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Objects;

import it.gov.messedaglia.messedaglia.R;
import it.gov.messedaglia.messedaglia.fragments.register.MarksFragment;
import it.gov.messedaglia.messedaglia.fragments.register.NoticesFragment;
import it.gov.messedaglia.messedaglia.fragments.register.RegisterFragment;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi;
import it.gov.messedaglia.messedaglia.Utils;

public class Register extends Fragment {
    private TextView lastUpdateView;

    private final MarksFragment marksFragment = new MarksFragment();
    private final NoticesFragment noticesFragment = new NoticesFragment();

    private RegisterFragment currentFragment;

    public Register() {}

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
            if (!RegisterApi.logWithCredentials(logged -> {
                if (logged) RegisterApi.updateAll(null);
                else showLoginDialog(context);
            })) showLoginDialog(context);
        } else RegisterApi.updateAll(null);

        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    private void showLoginDialog (Context context) {
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
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);

        dialogBuilder.setPositiveButton("ACCEDI", (DialogInterface dialogInterface, int i) ->
                RegisterApi.updateCredentials(username.getText().toString(), password.getText().toString(), logged -> {
                    if (logged) RegisterApi.updateAll(null);
                    else showLoginDialog(context);
                })
        );
        root.addView(username, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        root.addView(password, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        dialogBuilder.setView(root);
        ((Activity) context).runOnUiThread(() -> dialogBuilder.create().show());
    }

    private Fragment getFragment (int index) {
        switch (index) {
            case 0: return marksFragment;
            case 1: case 2: return new BlankFragment();
            case 3: return noticesFragment;
            default: throw new IllegalArgumentException(index+" is not a valid index");
        }
    }

    private void setFragment (Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(R.id.fragment2, fragment).commit();
        currentFragment = fragment instanceof RegisterFragment ? (RegisterFragment) fragment : null;
        if (lastUpdateView == null) return;
        lastUpdateView.setText("Ultimo aggiornamento: ");
        lastUpdateView.append(Utils.intervalToString(currentFragment != null ? System.currentTimeMillis()-currentFragment.getLastUpdate() : -1));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lastUpdateView = view.findViewById(R.id.last_update);
        TabLayout tab = view.findViewById(R.id.register_tabs);

        setFragment(getFragment(tab.getSelectedTabPosition()));

        tab.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setFragment(getFragment(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

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
        RegisterApi.save(Objects.requireNonNull(getContext()));
    }

    @Override
    public void onStop() {
        super.onStop();
        RegisterApi.save(Objects.requireNonNull(getContext()));
    }

    @Override
    public void onPause() {
        super.onPause();
        RegisterApi.save(Objects.requireNonNull(getContext()));
    }
}
