package it.gov.messedaglia.messedaglia.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.gov.messedaglia.messedaglia.R;

public class DeskEditor extends Fragment {


    public DeskEditor() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_desk_editor, container, false);
    }

}
