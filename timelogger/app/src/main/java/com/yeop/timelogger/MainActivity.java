package com.yeop.timelogger;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout listContainer;
    private final SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void buildUi() {
        int pad = dp(18);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(0xFFFFFFFF);

        TextView title = new TextView(this);
        title.setText("시간 기록");
        title.setTextSize(28);
        title.setTextColor(0xFF111111);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setPadding(0, 0, 0, dp(6));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView date = new TextView(this);
        date.setText(dayFmt.format(new Date()));
        date.setTextSize(15);
        date.setTextColor(0xFF666666);
        date.setGravity(Gravity.CENTER_HORIZONTAL);
        date.setPadding(0, 0, 0, dp(16));
        root.addView(date, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);

        Button start = new Button(this);
        start.setText("시작 시간 기록");
        start.setTextSize(17);
        start.setOnClickListener(v -> {
            TimeLogStore.add(this, "START");
            vibrateButton(v);
            refreshWidget();
            refresh();
        });
        buttons.addView(start, new LinearLayout.LayoutParams(0, dp(64), 1));

        Button end = new Button(this);
        end.setText("끝 시간 기록");
        end.setTextSize(17);
        end.setOnClickListener(v -> {
            TimeLogStore.add(this, "END");
            vibrateButton(v);
            refreshWidget();
            refresh();
        });
        LinearLayout.LayoutParams endLp = new LinearLayout.LayoutParams(0, dp(64), 1);
        endLp.setMarginStart(dp(8));
        buttons.addView(end, endLp);
        root.addView(buttons, new LinearLayout.LayoutParams(-1, -2));

        TextView sub = new TextView(this);
        sub.setText("오늘 기록");
        sub.setTextSize(18);
        sub.setTextColor(0xFF222222);
        sub.setPadding(0, dp(20), 0, dp(8));
        root.addView(sub, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(listContainer, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
    }

    private void refresh() {
        if (listContainer == null) return;
        listContainer.removeAllViews();
        String today = dayFmt.format(new Date());
        List<TimeLogStore.Record> all = TimeLogStore.getAll(this);
        int count = 0;
        for (int i = all.size() - 1; i >= 0; i--) {
            TimeLogStore.Record r = all.get(i);
            Date d = new Date(r.time);
            if (!today.equals(dayFmt.format(d))) continue;
            count++;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(6), dp(5), 0, dp(5));

            TextView text = new TextView(this);
            String label = "START".equals(r.type) ? "시작" : "끝";
            text.setText(timeFmt.format(d) + "   " + label);
            text.setTextSize(18);
            text.setTextColor(0xFF222222);
            row.addView(text, new LinearLayout.LayoutParams(0, dp(48), 1));

            Button delete = new Button(this);
            delete.setText("삭제");
            delete.setTextSize(13);
            delete.setOnClickListener(v -> {
                TimeLogStore.delete(this, r.time, r.type);
                refreshWidget();
                refresh();
            });
            row.addView(delete, new LinearLayout.LayoutParams(dp(72), dp(48)));
            listContainer.addView(row, new LinearLayout.LayoutParams(-1, -2));
        }

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("아직 기록이 없습니다.");
            empty.setTextSize(16);
            empty.setTextColor(0xFF888888);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(30), 0, 0);
            listContainer.addView(empty, new LinearLayout.LayoutParams(-1, -2));
        }
    }

    private void refreshWidget() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        int[] ids = manager.getAppWidgetIds(new ComponentName(this, TimeWidget.class));
        if (ids.length > 0) new TimeWidget().onUpdate(this, manager, ids);
    }

    private void vibrateButton(View v) {
        v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
