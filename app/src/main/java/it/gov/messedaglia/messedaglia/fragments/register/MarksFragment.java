package it.gov.messedaglia.messedaglia.fragments.register;


import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.gov.messedaglia.messedaglia.R;
import it.gov.messedaglia.messedaglia.fragments.Register;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi.MarksData.Subject;
import it.gov.messedaglia.messedaglia.views.Chart;
import it.gov.messedaglia.messedaglia.views.MarkView;

public class MarksFragment extends RegisterFragment {
    private LinearLayout root;


    public MarksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RegisterApi.onMarksUpdate = () -> getActivity().runOnUiThread(this::loadMarks);

        return inflater.inflate(R.layout.fragment_marks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        root = view.findViewById(R.id.marks_list);
        loadMarks();

    }

    public void loadMarks () {
        root.removeAllViews();
        for (int i = 0; i< RegisterApi.MarksData.data.size(); i++) {
            Subject s = RegisterApi.MarksData.data.valueAt(i);
            View subject = LayoutInflater.from(getContext()).inflate(R.layout.subject_item, root, false);
            subject.setOnClickListener((sbj) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCustomTitle(new TitleMarkView(getContext(), s.getAverage()/10, s.name));

                View view = View.inflate(getContext(), R.layout.marks_subject_details, null);
                builder.setView(view);

                Chart v = view.findViewById(R.id.chart);
                v.setSubject(s);

                builder.create().show();
            });
            root.addView(subject);
            ((TextView) subject.findViewById(R.id.textView)).setText(s.name);
            ((MarkView) subject.findViewById(R.id.markView)).setMark(new RegisterApi.MarksData.Mark(s.getAverage(), s.getNewCount()));
        }
    }

    @Override
    public long getLastUpdate() {
        return RegisterApi.MarksData.lastUpdate;
    }

    /*private class RegisterAdapter extends RecyclerView.Adapter<RegisterViewHolder> {

        @NonNull
        @Override
        public RegisterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = ViewGroup.inflate(viewGroup.getContext(), R.layout.subject_item, null);
            return new RegisterViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RegisterViewHolder registerViewHolder, int i) {
            for (Subject s : RegisterApi.MarksData.data.values())
                if (i < s.marks.size()) {
                    registerViewHolder.bind(s.marks.get(i));
                } else i -= s.marks.size();
        }

        @Override
        public int getItemCount() {
            int c = 0;
            for (Subject s : RegisterApi.MarksData.data.values()) c += s.marks.size();
            return c;
        }
    }
    private class RegisterViewHolder extends RecyclerView.ViewHolder {

        public RegisterViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind (RegisterApi.MarksData.Mark m){
            ((MarkView) itemView.findViewById(R.id.markView)).setMark(m);
        }
    }*/
}
