package es.fenauso.calculadoraelectrica;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProActivity extends Activity {
    static final String MODULE = "module";
    private static final int NAVY = 0xff16324f;
    private final Map<String, View> inputs = new LinkedHashMap<>();
    private final Map<String, View> rows = new LinkedHashMap<>();
    private ProModule module;
    private ScrollView scroll;
    private TextView result;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        module = ProModule.valueOf(getIntent().getStringExtra(MODULE));
        getWindow().setStatusBarColor(NAVY);

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xfff7f9fc);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        LinearLayout body = column();
        body.setPadding(dp(20), dp(12), dp(20), dp(24));
        scroll.addView(body);
        Button back = new Button(this);
        back.setText("Volver a módulos");
        back.setOnClickListener(v -> finish());
        body.addView(back);
        TextView heading = text(module.title, 24);
        body.addView(heading);
        body.addView(text(module.help, 15));
        Button help = new Button(this);
        help.setText("ⓘ Ayuda del módulo");
        help.setOnClickListener(v -> showHelp(module.title, ProHelp.overview(module)));
        body.addView(help);
        for (String field : module.fields) addField(body, field, state);

        Button calculate = new Button(this);
        calculate.setText("Calcular");
        calculate.setOnClickListener(v -> calculate());
        body.addView(calculate);
        result = text("", 16);
        result.setTextIsSelectable(true);
        result.setPadding(dp(12), dp(12), dp(12), dp(12));
        result.setBackgroundColor(Color.WHITE);
        result.setVisibility(View.GONE);
        body.addView(result);
        updateVisibleFields();
        setContentView(scroll);
    }

    private void addField(LinearLayout body, String definition, Bundle state) {
        String[] parts = definition.split(":", 2);
        String key = parts[0];
        String[] options = parts[1].split("\\|");
        LinearLayout row = column();
        if (options.length > 1) {
            Spinner spinner = new Spinner(this);
            spinner.setId(View.generateViewId());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(state == null ? 0 : state.getInt(key));
            spinner.setMinimumHeight(dp(48));
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                    clearResult();
                    updateVisibleFields();
                }

                @Override public void onNothingSelected(AdapterView<?> p) {}
            });
            inputs.put(key, spinner);
            addFieldLabel(row, spinner, ProHelp.selectionTitle(module, key), key);
            row.addView(spinner);
        } else {
            String[] field = options[0].split("=", 2);

            EditText input = new EditText(this);
            input.setId(View.generateViewId());
            input.setSingleLine(true);

            boolean freeText = key.equals("list") || key.equals("method") || key.equals("scheme");
            input.setInputType(freeText ? InputType.TYPE_CLASS_TEXT
                    : InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setText(state == null ? (field.length == 2 ? field[1] : "") : state.getString(key, ""));
            input.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { clearResult(); }
                @Override public void afterTextChanged(Editable s) {}
            });
            inputs.put(key, input);
            addFieldLabel(row, input, field[0], key);
            row.addView(input);
        }
        rows.put(key, row);
        body.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addFieldLabel(LinearLayout row, View control, String title, String key) {
        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView label = text(title, 15);
        label.setLabelFor(control.getId());
        heading.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button info = new Button(this);
        info.setText("ⓘ");
        info.setContentDescription("Ayuda sobre " + title);
        info.setOnClickListener(v -> showHelp(title, ProHelp.field(module, key)));
        heading.addView(info, new LinearLayout.LayoutParams(dp(48), dp(48)));
        row.addView(heading);
    }

    private void showHelp(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void updateVisibleFields() {
        if (module == ProModule.RESISTANCE && rows.containsKey("t")) {
            int mode = ((Spinner) inputs.get("mode")).getSelectedItemPosition();
            visible(mode == 0, "v", "i", "r");
            visible(mode == 1 || mode == 2, "list");
            visible(mode == 3, "metal", "l", "s", "t");
        }
        if (module == ProModule.CONDUIT && rows.containsKey("d")) {
            boolean table = ((Spinner) inputs.get("rule")).getSelectedItemPosition() == 1;
            visible(table, "s", "n");
            visible(!table, "list", "d");
        }
    }

    private void visible(boolean visible, String... keys) {
        for (String key : keys) rows.get(key).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void calculate() {
        Map<String, String> values = new LinkedHashMap<>();
        inputs.forEach((key, view) -> values.put(key, view instanceof Spinner
                ? Integer.toString(((Spinner) view).getSelectedItemPosition()) : ((EditText) view).getText().toString()));
        try {
            result.setText(module.calculate(values));
            result.setTextColor(NAVY);
        } catch (NumberFormatException e) {
            result.setText("Revisa el formato numérico. Usa coma o punto decimal y separa los valores de una lista con punto y coma.");
            result.setTextColor(0xffa42318);
        } catch (IllegalArgumentException e) {
            result.setText(e.getMessage());
            result.setTextColor(0xffa42318);
        }
        result.setVisibility(View.VISIBLE);
        scroll.post(() -> scroll.smoothScrollTo(0, result.getTop()));
    }

    private void clearResult() {
        if (result != null) result.setVisibility(View.GONE);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        inputs.forEach((key, view) -> {
            if (view instanceof Spinner) state.putInt(key, ((Spinner) view).getSelectedItemPosition());
            else state.putString(key, ((EditText) view).getText().toString());
        });
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(String content, int size) {
        TextView view = new TextView(this);
        view.setText(content);
        view.setTextSize(size);
        view.setTextColor(NAVY);
        view.setPadding(0, dp(6), 0, dp(6));
        return view;
    }

    private int dp(int size) {
        return Math.round(size * getResources().getDisplayMetrics().density);
    }
}
