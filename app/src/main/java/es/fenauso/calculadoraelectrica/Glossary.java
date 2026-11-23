package es.fenauso.calculadoraelectrica;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class Glossary {
    private static final int NAVY = 0xff16324f;
    private static final int[] COLUMN_WIDTHS = {155, 150, 175, 85, 320};

    private final Activity activity;
    private final HorizontalScrollView tableScroll;
    private final ScrollView verticalScroll;
    private final LinearLayout table;
    private final List<Button> tabs = new ArrayList<>();
    private final JSONArray headings;
    private final JSONArray categories;

    static void show(Activity activity) {
        try (BufferedReader source = new BufferedReader(new InputStreamReader(
                activity.getAssets().open("glosario-fv.json"), StandardCharsets.UTF_8))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = source.readLine()) != null) json.append(line);
            new Glossary(activity, new JSONObject(json.toString())).open();
        } catch (IOException | JSONException e) {
            Toast.makeText(activity, "No se pudo abrir el glosario.", Toast.LENGTH_LONG).show();
        }
    }

    private Glossary(Activity activity, JSONObject glossary) throws JSONException {
        this.activity = activity;
        headings = glossary.getJSONArray("encabezados");
        categories = glossary.getJSONArray("categorias");
        tableScroll = new HorizontalScrollView(activity);
        verticalScroll = new ScrollView(activity);
        table = new LinearLayout(activity);
        table.setOrientation(LinearLayout.VERTICAL);
        tableScroll.setScrollbarFadingEnabled(false);
        verticalScroll.addView(table);
        tableScroll.addView(verticalScroll, new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.WRAP_CONTENT, HorizontalScrollView.LayoutParams.MATCH_PARENT));
    }

    private void open() throws JSONException {
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        HorizontalScrollView tabScroll = new HorizontalScrollView(activity);
        tabScroll.setScrollbarFadingEnabled(false);
        tabScroll.setBackgroundColor(0xfff7f9fc);
        LinearLayout tabRow = new LinearLayout(activity);
        tabRow.setPadding(dp(12), dp(6), dp(12), dp(9));
        tabRow.setClipToPadding(false);
        tabScroll.addView(tabRow);
        content.addView(tabScroll);
        for (int index = 0; index < categories.length(); index++) {
            JSONArray category = categories.getJSONArray(index);
            JSONArray rows = category.getJSONArray(1);
            Button tab = new Button(activity);
            tab.setAllCaps(false);
            tab.setText(category.getString(0));
            tab.setSingleLine(true);
            tab.setPadding(dp(14), 0, dp(14), 0);
            tab.setStateListAnimator(null);
            tab.setElevation(dp(2));
            tab.setOnClickListener(view -> select(tab, rows));
            tabs.add(tab);
            LinearLayout.LayoutParams tabLayout = new LinearLayout.LayoutParams(-2, dp(48));
            if (index > 0) tabLayout.setMarginStart(dp(8));
            tabRow.addView(tab, tabLayout);
        }
        TextView hint = new TextView(activity);
        hint.setText("Desliza la tabla hacia los lados para ver todas las columnas.");
        hint.setPadding(dp(12), dp(6), dp(12), dp(6));
        content.addView(hint);
        content.addView(tableScroll, new LinearLayout.LayoutParams(-1, 0, 1));
        select(tabs.get(0), categories.getJSONArray(0).getJSONArray(1));
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Glosario fotovoltaico")
                .setView(content)
                .setPositiveButton("Cerrar", null)
                .create();
        dialog.show();
        dialog.getWindow().setLayout(-1, Math.round(activity.getResources().getDisplayMetrics().heightPixels * 0.85f));
    }

    private void select(Button selected, JSONArray rows) {
        for (Button tab : tabs) {
            boolean active = tab == selected;
            tab.setSelected(active);
            tab.setTextColor(active ? Color.WHITE : NAVY);
            tab.setTypeface(Typeface.DEFAULT, active ? Typeface.BOLD : Typeface.NORMAL);
            GradientDrawable surface = new GradientDrawable();
            surface.setColor(active ? NAVY : Color.WHITE);
            surface.setCornerRadius(dp(10));
            surface.setStroke(dp(1), active ? NAVY : 0xffcdd8e4);
            tab.setBackground(new RippleDrawable(
                    ColorStateList.valueOf(active ? 0x33ffffff : 0x2416324f), surface, null));
        }
        table.removeAllViews();
        addRow(headings, NAVY, Color.WHITE, true);
        for (int index = 0; index < rows.length(); index++) {
            addRow(rows.optJSONArray(index), index % 2 == 0 ? Color.WHITE : 0xffeef3f9, NAVY, false);
        }
        verticalScroll.scrollTo(0, 0);
        tableScroll.scrollTo(0, 0);
    }

    private void addRow(JSONArray values, int background, int foreground, boolean header) {
        LinearLayout row = new LinearLayout(activity);
        row.setBackgroundColor(0xffcdd8e4);
        row.setPadding(0, 0, 0, dp(1));
        for (int column = 0; column < COLUMN_WIDTHS.length; column++) {
            TextView cell = new TextView(activity);
            cell.setText(values.optString(column));
            cell.setTextColor(foreground);
            cell.setBackgroundColor(background);
            cell.setGravity(Gravity.TOP);
            cell.setPadding(dp(8), dp(9), dp(8), dp(9));
            if (header || column == 1) cell.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(dp(COLUMN_WIDTHS[column]), -1);
            layout.setMarginEnd(dp(1));
            row.addView(cell, layout);
        }
        table.addView(row, new LinearLayout.LayoutParams(-2, -2));
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
