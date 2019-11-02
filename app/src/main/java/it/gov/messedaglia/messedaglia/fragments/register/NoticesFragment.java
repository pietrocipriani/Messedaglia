package it.gov.messedaglia.messedaglia.fragments.register;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Objects;

import it.gov.messedaglia.messedaglia.R;
import it.gov.messedaglia.messedaglia.Utils;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi;

public class NoticesFragment extends RegisterFragment {
    private LinearLayout root;


    public NoticesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RegisterApi.onNoticesUpdate = () -> {
            if (isVisible()) Objects.requireNonNull(getActivity()).runOnUiThread(this::loadNotices);
        };

        return inflater.inflate(R.layout.fragment_notices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        root = view.findViewById(R.id.notices_list);
        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.notices_container);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            RegisterApi.loadNoticeBoard(() -> {
                if (isVisible()) Objects.requireNonNull(getActivity()).runOnUiThread(() -> refreshLayout.setRefreshing(false));
            });
        });
        loadNotices();

    }

    public void loadNotices () {
        root.removeAllViews();
        for (int i = RegisterApi.Notices.notices.size()-1; i >= 0; i--) {
            RegisterApi.Notices.Notice n = RegisterApi.Notices.notices.valueAt(i);
            if (!n.valid) continue;
            View notice = LayoutInflater.from(getContext()).inflate(R.layout.notice_item, root, false);
            /*notice.setOnClickListener((sbj) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCustomTitle(new TitleMarkView(getContext(), s.getAverage()/10, s.name));

                View view = View.inflate(getContext(), R.layout.marks_subject_details, null);
                builder.setView(view);

                Chart v = view.findViewById(R.id.chart);
                v.setSubject(s);

                builder.create().show();
            });*/
            root.addView(notice);
            ((TextView) notice.findViewById(R.id.textView)).setText(n.title);
            if (n.attachments.length > 0) notice.findViewById(R.id.downloadButton).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public long getLastUpdate() {
        return RegisterApi.Notices.lastUpdate;
    }

}