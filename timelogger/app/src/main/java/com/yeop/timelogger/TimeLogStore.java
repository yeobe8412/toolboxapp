package com.yeop.timelogger;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class TimeLogStore {
    private static final String PREFS = "time_logs";
    private static final String KEY = "records";

    public static final class Record {
        public final long time;
        public final String type;
        public Record(long time, String type) {
            this.time = time;
            this.type = type;
        }
    }

    private TimeLogStore() {}

    public static synchronized void add(Context context, String type) {
        long now = System.currentTimeMillis();
        List<Record> records = getAll(context);
        records.add(new Record(now, type));
        save(context, records);
    }

    public static synchronized void delete(Context context, long time, String type) {
        List<Record> records = getAll(context);
        for (int i = 0; i < records.size(); i++) {
            Record r = records.get(i);
            if (r.time == time && r.type.equals(type)) {
                records.remove(i);
                break;
            }
        }
        save(context, records);
    }

    public static synchronized List<Record> getAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY, "");
        List<Record> list = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return list;
        String[] lines = raw.split("\\n");
        for (String line : lines) {
            String[] p = line.split("\\|", 2);
            if (p.length != 2) continue;
            try {
                list.add(new Record(Long.parseLong(p[0]), p[1]));
            } catch (NumberFormatException ignored) {}
        }
        Collections.sort(list, Comparator.comparingLong(r -> r.time));
        return list;
    }

    private static void save(Context context, List<Record> records) {
        StringBuilder sb = new StringBuilder();
        for (Record r : records) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(r.time).append('|').append(r.type);
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, sb.toString()).apply();
    }
}
