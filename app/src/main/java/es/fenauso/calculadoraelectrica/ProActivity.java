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
    private final Map<String, TextView> labels = new LinkedHashMap<>();
    private final Map<String, Spinner> units = new LinkedHashMap<>();
    private static final String DRAFTS = "drafts";
    private final Bundle drafts = new Bundle();
    private ProModule module;
    private ScrollView scroll;
    private TextView result;
    private LinearLayout projectDetails;
    private final java.util.List<View> detailedProjectHeadings = new java.util.ArrayList<>();

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
        labels.clear();
        units.clear();
        result = null;
        projectDetails = null;
        detailedProjectHeadings.clear();
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
        body.addView(CalculatorTabs.tools(this, () -> showHelp(module.title, ProHelp.overview(module)
                + "\n\nPulsa Ejemplo para recuperar los valores iniciales o Vaciar campos para introducir tus propios datos."
                + "\nUn resultado en verde indica que el cálculo se ejecutó. Los avisos en rojo indican límites incumplidos y señalan los campos que conviene revisar.")));
        Button clear = action("Vaciar campos", false);
        clear.setOnClickListener(v -> clearFields());
        Button example = action("Ejemplo", false);
        example.setOnClickListener(v -> {
            drafts.remove(module.name());
            showModule();
        });
        LinearLayout actions = new LinearLayout(this);
        LinearLayout.LayoutParams clearLayout = new LinearLayout.LayoutParams(0, dp(48), 1);
        clearLayout.setMarginEnd(dp(6));
        actions.addView(clear, clearLayout);
        actions.addView(example, new LinearLayout.LayoutParams(0, dp(48), 1));
        body.addView(actions);
        body.addView(text("Valores iniciales de ejemplo. Para una instalación real, usa sus datos.", 13));
        for (String field : module.fields) {
            if (module == ProModule.PV_PROJECT) addProjectHeading(body, field.substring(0, field.indexOf(':')));
            addField(body, field, state);
        }

        Button calculate = action("Calcular", true);
        calculate.setOnClickListener(v -> calculate());
        body.addView(calculate);
        result = text("", 16);
        result.setTextIsSelectable(true);
        result.setPadding(dp(12), dp(12), dp(12), dp(12));
        result.setBackgroundColor(Color.WHITE);
        result.setVisibility(View.GONE);
        body.addView(result);
        if (module == ProModule.PV_PROJECT) {
            projectDetails = column();
            body.addView(projectDetails);
        }
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
            row.addView(fieldLabel(spinner, ProHelp.selectionTitle(module, key), key));
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
            addInput(row, fieldLabel(input, title, key), input, key, state);
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

    private void addInput(LinearLayout row, TextView label, EditText input, String key, Bundle state) {
        String[] choices = unitChoices(key);
        if (choices == null) {
            row.addView(label);
            row.addView(input);
            return;
        }
        LinearLayout line = new LinearLayout(this);
        LinearLayout valueField = column();
        valueField.addView(label);
        valueField.addView(input, new LinearLayout.LayoutParams(-1, dp(48)));
        line.addView(valueField, new LinearLayout.LayoutParams(0, -2, 1));
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(state == null ? 0 : state.getInt("unit." + key));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { clearResult(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        units.put(key, spinner);
        LinearLayout unitField = column();
        TextView unitLabel = text("Unidad", 15);
        FieldHelp.bind(unitLabel, spinner, "Unidad", () ->
                "Selecciona " + choices[0] + " o " + choices[1] + " según la unidad del valor introducido.");
        unitField.addView(unitLabel);
        unitField.addView(spinner, new LinearLayout.LayoutParams(-1, dp(48)));
        line.addView(unitField, new LinearLayout.LayoutParams(dp(108), -2));
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
            try {
                values.put(key, ProCalculations.fromSmallerUnit(values.get(key), key.equals("list")));
            } catch (IllegalArgumentException e) {
                throw FieldIssues.invalid(key, "Revisa el formato numérico del campo señalado.");
            }
        }
    }

    private TextView fieldLabel(View control, String title, String key) {
        TextView label = text(title, 15);
        FieldHelp.bind(label, control, title, () -> ProHelp.field(module, key));
        labels.put(key, label);
        return label;
    }

    private void showHelp(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void updateVisibleFields() {
        if (module == ProModule.PV_PROJECT && rows.containsKey("ec")) {
            boolean detailed = ((Spinner) inputs.get("design")).getSelectedItemPosition() == 0;
            for (View heading : detailedProjectHeadings) heading.setVisibility(detailed ? View.VISIBLE : View.GONE);
            for (Map.Entry<String, View> entry : rows.entrySet()) {
                boolean always = (" design goal target ratio dailygen sun pr p ").contains(" " + entry.getKey() + " ");
                entry.getValue().setVisibility((detailed || always) ? View.VISIBLE : View.GONE);
            }
            visible(!detailed, "ratio");
            boolean byEnergy = ((Spinner) inputs.get("goal")).getSelectedItemPosition() == 1;
            visible(!byEnergy, "target");
            visible(byEnergy, "dailygen", "sun", "pr");
            boolean storage = detailed && ((Spinner) inputs.get("storage")).getSelectedItemPosition() == 1;
            visible(storage, PvProject.BATTERY_FIELDS.split(" "));
        }
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
        clearFieldMarks();
        if (projectDetails != null) projectDetails.removeAllViews();
        View firstMissing = null;
        Map<String, String> values = new LinkedHashMap<>();
        inputs.forEach((key, view) -> values.put(key, view instanceof Spinner
                ? Integer.toString(((Spinner) view).getSelectedItemPosition()) : ((EditText) view).getText().toString()));
        try {
            String[] missing = module.requiredFields(values);
            if (missing.length > 0) {
                firstMissing = rows.get(missing[0]);
                throw FieldIssues.invalid(String.join(" ", missing),
                        "Completa los campos necesarios para este cálculo.");
            }
            normalizeUnits(values);
            String answer;
            if (module == ProModule.PV_PROJECT) {
                PvProject.Plan project = PvProject.calculate(new ProModule.Input(values));
                answer = project.text();
                showProjectDetails(project);
            } else {
                answer = module.calculate(values);
            }
            for (String key : FieldIssues.keys(answer)) markField(key);
            result.setText(CalculationResult.format(answer.startsWith("# ") ? answer : "# Resultado\n" + answer));
            result.setTextColor(GREEN);
        } catch (FieldIssues.Invalid e) {
            StringBuilder message = new StringBuilder(e.getMessage());
            for (String key : e.keys) {
                markField(key);
                TextView label = labels.get(key);
                if (label != null) message.append("\n• ").append(label.getTag());
            }
            result.setText(message);
            result.setTextColor(CalculationResult.ALERT);
            if (firstMissing == null && e.keys.length > 0) firstMissing = rows.get(e.keys[0]);
        } catch (NumberFormatException e) {
            result.setText("Revisa el formato numérico. Usa coma o punto decimal y separa los valores de una lista con punto y coma.");
            result.setTextColor(0xffa42318);
        } catch (IllegalArgumentException e) {
            result.setText(e.getMessage());
            result.setTextColor(0xffa42318);
        }
        result.setVisibility(View.VISIBLE);
        View destination = firstMissing == null ? result : firstMissing;
        scroll.post(() -> scroll.smoothScrollTo(0, destination.getTop()));
    }

    private void addProjectHeading(LinearLayout body, String key) {
        String title = switch (key) {
            case "design" -> "Objetivo y condiciones";
            case "p" -> "Módulo fotovoltaico";
            case "ac" -> "Inversor";
            case "ldc" -> "Canalización y cableado";
            case "feed" -> "Protecciones";
            case "storage" -> "Almacenamiento";
            default -> null;
        };
        if (title == null) return;
        TextView heading = text(title, 19);
        heading.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        body.addView(heading);
        if (!key.equals("design") && !key.equals("p")) detailedProjectHeadings.add(heading);
        switch (key) {
            case "p" -> addEquipmentButton(body, "panel", PvProject.PANEL);
            case "ac" -> addEquipmentButton(body, "inversor", PvProject.INVERTER);
            case "storage" -> addEquipmentButton(body, "batería", PvProject.BATTERY_PROFILE);
            default -> { }
        }
    }

    private void addEquipmentButton(LinearLayout body, String family, String keys) {
        Button button = action("Fichas de " + family, false);
        button.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Fichas de " + family)
                .setItems(new String[]{"Cargar ficha", "Guardar ficha con los valores actuales", "Borrar una ficha"}, (dialog, option) -> {
                    switch (option) {
                        case 0 -> selectEquipment(family, keys, false);
                        case 1 -> saveEquipment(family, keys);
                        case 2 -> selectEquipment(family, keys, true);
                    }
                })
                .show());
        body.addView(button);
        if (!family.equals("panel")) detailedProjectHeadings.add(button);
    }

    private void saveEquipment(String family, String keys) {
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setHint("Fabricante y modelo");
        LinearLayout form = column();
        form.setPadding(dp(20), 0, dp(20), 0);
        TextView nameLabel = text("Fabricante y modelo", 15);
        FieldHelp.bind(nameLabel, name, "Fabricante y modelo", () ->
                "Escribe el nombre con el que quieres identificar esta ficha de " + family + " al cargarla o borrarla.");
        form.addView(nameLabel);
        form.addView(name);
        new AlertDialog.Builder(this).setTitle("Guardar ficha de " + family).setView(form)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String model = name.getText().toString().trim();
                    if (model.isEmpty()) {
                        showHelp("Nombre de la ficha", "Introduce un nombre para guardar y recuperar el equipo.");
                        return;
                    }
                    Map<String, String> values = new LinkedHashMap<>();
                    for (String key : keys.split(" ")) {
                        View field = inputs.get(key);
                        values.put(key, field instanceof Spinner
                                ? Integer.toString(((Spinner) field).getSelectedItemPosition())
                                : ((EditText) field).getText().toString());
                    }
                    getSharedPreferences("pv_equipment", MODE_PRIVATE).edit()
                            .putString(family + ":" + model, new org.json.JSONObject(values).toString()).apply();
                }).setNegativeButton("Cancelar", null).show();
    }

    private void selectEquipment(String family, String keys, boolean delete) {
        android.content.SharedPreferences equipment = getSharedPreferences("pv_equipment", MODE_PRIVATE);
        String prefix = family + ":";
        String[] models = equipment.getAll().keySet().stream().filter(key -> key.startsWith(prefix))
                .map(key -> key.substring(prefix.length())).sorted().toArray(String[]::new);
        if (models.length == 0) {
            showHelp("Fichas de " + family, "Todavía no hay fichas guardadas para este equipo.");
            return;
        }
        new AlertDialog.Builder(this).setTitle((delete ? "Borrar ficha de " : "Cargar ficha de ") + family)
                .setItems(models, (dialog, which) -> {
                    String model = models[which];
                    if (delete) {
                        new AlertDialog.Builder(this).setTitle("Borrar ficha de " + family)
                                .setMessage("¿Quieres borrar la ficha «" + model + "»?")
                                .setNegativeButton("Cancelar", null)
                                .setPositiveButton("Borrar", (confirmation, button) ->
                                        equipment.edit().remove(prefix + model).apply())
                                .show();
                        return;
                    }
                    try {
                        org.json.JSONObject values = new org.json.JSONObject(equipment.getString(prefix + model, "{}"));
                        for (String key : keys.split(" ")) {
                            if (!values.has(key)) continue;
                            View field = inputs.get(key);
                            if (field instanceof Spinner) ((Spinner) field).setSelection(values.optInt(key));
                            else ((EditText) field).setText(values.optString(key));
                        }
                    } catch (org.json.JSONException e) {
                        showHelp("Ficha de equipo", "La ficha guardada tiene un formato incorrecto.");
                    }
                }).show();
    }

    private void showProjectDetails(PvProject.Plan project) {
        for (Map.Entry<ProModule, Map<String, String>> detail : project.details().entrySet()) {
            ProModule destination = detail.getKey();
            Button open = action("Abrir " + destination.title + " con estos datos", false);
            open.setOnClickListener(v -> {
                Bundle data = new Bundle();
                for (String definition : destination.fields) {
                    String key = definition.substring(0, definition.indexOf(':'));
                    String value = detail.getValue().get(key);
                    if (definition.contains("|")) data.putInt(key, value == null ? 0 : Integer.parseInt(value));
                    else data.putString(key, value == null ? "" : value);
                }
                drafts.putBundle(destination.name(), data);
                selectModule(destination);
            });
            projectDetails.addView(open);
        }
    }

    private void markField(String key) {
        TextView label = labels.get(key);
        if (label == null || rows.get(key).getVisibility() != View.VISIBLE) return;
        label.setTextColor(CalculationResult.ALERT);
        FieldHelp.setCaption(label, label.getTag() + " · Revisar");
    }

    private void clearFieldMarks() {
        for (TextView label : labels.values()) {
            label.setTextColor(NAVY);
            FieldHelp.setCaption(label, (String) label.getTag());
        }
    }

    private void clearResult() {
        clearFieldMarks();
        if (result != null) result.setVisibility(View.GONE);
        if (projectDetails != null) projectDetails.removeAllViews();
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
