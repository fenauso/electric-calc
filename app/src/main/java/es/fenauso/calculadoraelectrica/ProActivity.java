package es.fenauso.calculadoraelectrica;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
    private static final int BLUE = 0xff2563eb;
    private static final int GREEN = 0xff137a4b;
    private final Map<String, View> inputs = new LinkedHashMap<>();
    private final Map<String, View> rows = new LinkedHashMap<>();
    private final Map<String, Spinner> units = new LinkedHashMap<>();
    private static final String DRAFTS = "drafts";
    private final Bundle drafts = new Bundle();
    private ProModule module;
    private ScrollView scroll;
    private TextView result;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Bundle savedDrafts = state == null ? null : state.getBundle(DRAFTS);
        if (savedDrafts != null) drafts.putAll(savedDrafts);
        String active = state == null ? getIntent().getStringExtra(MODULE) : state.getString(MODULE);
        module = ProModule.valueOf(active);
        getWindow().setStatusBarColor(NAVY);
        showModule();
    }

    private void showModule() {
        inputs.clear();
        rows.clear();
        units.clear();
        result = null;
        Bundle state = drafts.getBundle(module.name());

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xfff7f9fc);
        LinearLayout body = column();
        body.setPadding(dp(20), dp(12), dp(20), dp(24));
        scroll.addView(body);
        TextView heading = text(module.title, 24);
        body.addView(heading);
        body.addView(text(module.help, 15));
        Button help = action("ⓘ Ayuda del módulo", false);
        help.setOnClickListener(v -> showHelp(module.title, ProHelp.overview(module)
                + "\n\nLos valores iniciales son un ejemplo. Pulsa Vaciar campos para introducir tus propios datos."
                + "\nUn resultado en verde indica que el cálculo se ejecutó. Lee las comprobaciones pendientes del resultado."));
        body.addView(help);
        Button clear = action("Vaciar campos", false);
        clear.setOnClickListener(v -> clearFields());
        body.addView(clear);
        body.addView(text("Valores iniciales de ejemplo. Para una instalación real, usa sus datos.", 13));
        for (String field : module.fields) addField(body, field, state);

        Button calculate = action("Calcular", true);
        calculate.setOnClickListener(v -> calculate());
        body.addView(calculate);
        result = text("", 16);
        result.setTextIsSelectable(true);
        result.setPadding(dp(12), dp(12), dp(12), dp(12));
        result.setBackgroundColor(Color.WHITE);
        result.setVisibility(View.GONE);
        body.addView(result);
        updateVisibleFields();
        setContentView(CalculatorTabs.surround(this, scroll, module, this::selectModule));
    }

    private void selectModule(ProModule selected) {
        if (selected == null) {
            finish();
            return;
        }
        rememberInputs();
        module = selected;
        showModule();
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
            if (freeText) {
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                if (key.equals("list")) DecimalInput.addLeadingZero(input);
            } else {
                DecimalInput.configure(input);
            }
            String example = ProExamples.value(module, key, field.length == 2 ? field[1] : "");
            input.setText(state == null ? example : state.getString(key, ""));
            input.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { clearResult(); }
                @Override public void afterTextChanged(Editable s) {}
            });
            inputs.put(key, input);
            String title = unitChoices(key) == null ? field[0] : field[0].replaceFirst(" \\((V|A|Ω|kW)\\)", "");
            addFieldLabel(row, input, title, key);
            addInput(row, input, key, state);
        }
        rows.put(key, row);
        body.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private Button action(String label, boolean primary) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(primary ? Color.WHITE : BLUE);
        GradientDrawable background = new GradientDrawable();
        background.setColor(primary ? BLUE : 0xfff5f8ff);
        background.setCornerRadius(dp(primary ? 14 : 10));
        button.setBackground(background);
        button.setMinimumHeight(dp(48));
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.bottomMargin = dp(6);
        button.setLayoutParams(layout);
        return button;
    }

    private void clearFields() {
        for (View field : inputs.values()) {
            if (field instanceof EditText) ((EditText) field).setText("");
        }
        clearResult();
    }

    private void addInput(LinearLayout row, EditText input, String key, Bundle state) {
        String[] choices = unitChoices(key);
        if (choices == null) {
            row.addView(input);
            return;
        }
        LinearLayout line = new LinearLayout(this);
        line.setGravity(android.view.Gravity.CENTER_VERTICAL);
        line.addView(input, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Spinner spinner = new Spinner(this);
        spinner.setContentDescription("Unidad de " + choices[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(state == null ? 0 : state.getInt("unit." + key));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { clearResult(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        units.put(key, spinner);
        line.addView(spinner, new LinearLayout.LayoutParams(dp(108), dp(48)));
        row.addView(line);
    }

    private String[] unitChoices(String key) {
        if (module == ProModule.RESISTANCE) {
            return switch (key) {
                case "v" -> new String[]{"V", "mV"};
                case "i" -> new String[]{"A", "mA"};
                case "r", "list" -> new String[]{"Ω", "mΩ"};
                default -> null;
            };
        }
        return (module == ProModule.REACTIVE || module == ProModule.ENERGY) && key.equals("p")
                ? new String[]{"kW", "W"} : null;
    }

    private void normalizeUnits(Map<String, String> values) {
        for (Map.Entry<String, Spinner> unit : units.entrySet()) {
            String key = unit.getKey();
            if (unit.getValue().getSelectedItemPosition() != 1
                    || rows.get(key).getVisibility() != View.VISIBLE || values.get(key).trim().isEmpty()) continue;
            values.put(key, ProCalculations.fromSmallerUnit(values.get(key), key.equals("list")));
        }
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
            normalizeUnits(values);
            String answer = module.calculate(values);
            result.setText(CalculationResult.format(answer.startsWith("# ") ? answer : "# Resultado\n" + answer));
            result.setTextColor(GREEN);
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
        rememberInputs();
        state.putString(MODULE, module.name());
        state.putBundle(DRAFTS, drafts);
    }

    private void rememberInputs() {
        Bundle state = new Bundle();
        inputs.forEach((key, view) -> {
            if (view instanceof Spinner) state.putInt(key, ((Spinner) view).getSelectedItemPosition());
            else state.putString(key, ((EditText) view).getText().toString());
        });
        units.forEach((key, spinner) -> state.putInt("unit." + key, spinner.getSelectedItemPosition()));
        drafts.putBundle(module.name(), state);
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
