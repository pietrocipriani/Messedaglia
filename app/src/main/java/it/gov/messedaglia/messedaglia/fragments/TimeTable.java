package it.gov.messedaglia.messedaglia.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import it.gov.messedaglia.messedaglia.R;

public class TimeTable extends Fragment {


    public TimeTable() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imgView = view.findViewById(R.id.image);
        new Thread(() -> {
            try {
                Bitmap btm = BitmapFactory.decodeStream(new URL("https://www.messedaglia.edu.it/images/stories/2019-20/orario/classi/edc0002591p00001s3fffffffffffffff_4g_ac.png").openStream());
                getActivity().runOnUiThread(() -> imgView.setImageBitmap(btm));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
