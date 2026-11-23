package es.fenauso.calculadoraelectrica;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final int BLUE = Color.rgb(37,99,235);
    private static final int NAVY = Color.rgb(22,50,79);
    private static final int TEXT = Color.rgb(24,34,48);
    private static final int MUTED = Color.rgb(102,112,133);
    private static final int GREEN = Color.rgb(19,122,75);
    private static final int RED = Color.rgb(180,35,24);
    private static final int AMBER = Color.rgb(181,105,0);

    private LinearLayout form, advancedBox;
    private Spinner mode, phase, loadType, material, insulation, breaker, section;
    private EditText load, voltage, powerFactor, length, temperature, maxDrop;
    private TextView result, equation, loadHint, loadConversion, advancedButton, whyButton;

    private boolean advancedVisible = false;
    private boolean hasCalculationResult;
    private String lastExplanation = "";
    private SharedPreferences prefs;

    private final String[] BREAKER_LABELS = {"2 A","4 A","6 A","10 A","16 A","20 A","25 A","32 A","40 A","50 A","63 A","80 A","100 A","125 A"};
    private final String[] SECTION_LABELS = {"1,5 mm²","2,5 mm²","4 mm²","6 mm²","10 mm²","16 mm²","25 mm²","35 mm²","50 mm²","70 mm²","95 mm²","120 mm²"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(NAVY);
        prefs = getSharedPreferences("electric_calc", MODE_PRIVATE);
        buildUi();
        restoreValues();
        updateMode();
        updateLoadConversion();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(28));
        root.setBackgroundColor(Color.rgb(247,249,252));
        scroll.addView(root);

        TextView title = text("⚡ Cálculo Eléctrico PRO", 26, NAVY, true);
        root.addView(title);

        TextView subtitle = text("Carga · Magnetotérmico · Conductor", 15, MUTED, false);
        subtitle.setPadding(0,dp(3),0,dp(10));
        root.addView(subtitle);

        equation = text("I CARGA ≤ In MAGNETOTÉRMICO ≤ Iz CABLE", 14, Color.WHITE, true);
        equation.setGravity(Gravity.CENTER);
        equation.setPadding(dp(10),dp(12),dp(10),dp(12));
        equation.setBackground(roundRect(NAVY, 14));
        root.addView(equation);

        root.addView(CalculatorTabs.tools(this, this::showHelp));

        form = card();
        form.setPadding(dp(14),dp(14),dp(14),dp(14));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1,-2);
        cardLp.setMargins(0,dp(10),0,0);
        root.addView(form, cardLp);

        mode = spinner(new String[]{"Calcular CONDUCTOR", "Calcular MAGNETOTÉRMICO", "Calcular CARGA máxima"});
        addField("¿Qué deseas calcular?", mode);
        mode.setOnItemSelectedListener(listener(this::updateMode));

        sectionHeader("Sistema y carga");

        phase = spinner(new String[]{"Monofásica", "Trifásica"});
        loadType = spinner(new String[]{"Potencia (W)", "Intensidad (A)"});
        addFieldPair("Sistema", phase, "Dato que conoces", loadType);

        LinearLayout loadRow = new LinearLayout(this);
        loadRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams loadRowLp = new LinearLayout.LayoutParams(-1,-2);
        loadRowLp.setMargins(0,0,0,dp(12));
        load = edit("3500");
        loadHint = label("");
        FieldHelp.bind(loadHint, load, "Potencia de la carga (W)", () ->
                loadType.getSelectedItemPosition() == 0
                        ? "Introduce la potencia total de los aparatos que alimentará el circuito. La app calcula los amperios equivalentes."
                        : "Introduce en amperios la corriente que consume la carga. La app calcula la potencia equivalente.");
        form.addView(loadHint);
        form.addView(loadRow, loadRowLp);
        LinearLayout.LayoutParams loadLp = new LinearLayout.LayoutParams(0,dp(48),1.55f);
        loadLp.setMarginEnd(dp(6));
        loadRow.addView(load, loadLp);

        loadConversion = text("≈ 16,01 A", 14, NAVY, true);
        loadConversion.setGravity(Gravity.CENTER);
        loadConversion.setPadding(dp(8),0,dp(8),0);
        loadConversion.setBackground(roundRect(Color.rgb(235,242,255),10));
        LinearLayout.LayoutParams convLp = new LinearLayout.LayoutParams(0,dp(48),1f);
        convLp.setMarginStart(dp(6));
        loadRow.addView(loadConversion, convLp);

        voltage = edit("230");
        powerFactor = edit("0.95");
        addFieldPair("Tensión (V)", voltage, "Factor potencia", powerFactor);

        phase.setOnItemSelectedListener(listener(() -> {
            invalidateCalculation();
            if (phase.getSelectedItemPosition()==0 && clean(voltage).equals("400")) voltage.setText("230");
            if (phase.getSelectedItemPosition()==1 && clean(voltage).equals("230")) voltage.setText("400");
            updateLoadConversion();
        }));

        loadType.setOnItemSelectedListener(listener(() -> {
            invalidateCalculation();
            boolean watts = loadType.getSelectedItemPosition()==0;
            FieldHelp.setTitle(loadHint, watts ? "Potencia de la carga (W)" : "Intensidad de la carga (A)");
            updateLoadConversion();
        }));

        addWatcher(load);
        addWatcher(voltage);
        addWatcher(powerFactor);

        advancedButton = text("▾ MOSTRAR OPCIONES AVANZADAS", 13, BLUE, true);
        advancedButton.setGravity(Gravity.CENTER);
        advancedButton.setPadding(dp(8),dp(13),dp(8),dp(13));
        advancedButton.setBackground(roundRect(Color.rgb(245,248,255),10));
        LinearLayout.LayoutParams advancedBtnLp = new LinearLayout.LayoutParams(-1,-2);
        advancedBtnLp.setMargins(0,dp(4),0,dp(6));
        form.addView(advancedButton, advancedBtnLp);
        advancedButton.setOnClickListener(v -> toggleAdvanced());

        advancedBox = new LinearLayout(this);
        advancedBox.setOrientation(LinearLayout.VERTICAL);
        advancedBox.setVisibility(View.GONE);
        form.addView(advancedBox);

        buildAdvancedFields();

        sectionHeader("Protección y conductor");

        breaker = spinner(BREAKER_LABELS);
        breaker.setSelection(4);
        section = spinner(SECTION_LABELS);
        section.setSelection(1);
        addFieldPair("Magnetotérmico", breaker, "Sección cable", section);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonRowLp = new LinearLayout.LayoutParams(-1,-2);
        buttonRowLp.setMargins(0,dp(14),0,0);
        form.addView(buttonRow, buttonRowLp);

        Button clear = new Button(this);
        clear.setText("Vaciar campos");
        clear.setTextColor(BLUE);
        clear.setBackground(roundRect(Color.rgb(245,248,255), 10));
        clear.setOnClickListener(v -> clearFields());
        TextView example = smallAction("▶ EJEMPLO");
        example.setOnClickListener(v -> loadExample());
        LinearLayout clearRow = new LinearLayout(this);
        LinearLayout.LayoutParams clearLp = new LinearLayout.LayoutParams(-1, dp(48));
        clearLp.topMargin = dp(8);
        clearRow.addView(clear, weightedEnd(dp(5), dp(48), 1f));
        clearRow.addView(example, weightedStart(dp(5), dp(48), 1f));
        form.addView(clearRow, clearLp);

        Button calculate = new Button(this);
        calculate.setText("CALCULAR");
        calculate.setTextColor(Color.WHITE);
        calculate.setTextSize(16);
        calculate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        calculate.setBackground(roundRect(BLUE, 14));
        buttonRow.addView(calculate, weightedEnd(dp(5), dp(54), 2f));
        calculate.setOnClickListener(v -> calculate());

        Button reset = new Button(this);
        reset.setText("NUEVO");
        reset.setTextColor(NAVY);
        reset.setTextSize(14);
        reset.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        reset.setBackground(roundRect(Color.rgb(238,242,247),14));
        buttonRow.addView(reset, weightedStart(dp(5), dp(54), 1f));
        reset.setOnClickListener(v -> resetForm());

        result = text("Introduce los datos y pulsa CALCULAR", 16, TEXT, false);
        result.setPadding(dp(16),dp(16),dp(16),dp(16));
        result.setBackground(roundRect(Color.WHITE, 14));
        LinearLayout.LayoutParams resultLp = new LinearLayout.LayoutParams(-1,-2);
        resultLp.setMargins(0,dp(14),0,0);
        root.addView(result,resultLp);

        whyButton = text("ⓘ ¿POR QUÉ ESTE RESULTADO?", 13, BLUE, true);
        whyButton.setGravity(Gravity.CENTER);
        whyButton.setPadding(dp(10),dp(12),dp(10),dp(12));
        whyButton.setVisibility(View.GONE);
        root.addView(whyButton);
        whyButton.setOnClickListener(v -> showWhy());

        addWatcher(length);
        addWatcher(temperature);
        addWatcher(maxDrop);
        for (Spinner input : new Spinner[]{material, insulation, breaker, section}) {
            input.setOnItemSelectedListener(listener(this::invalidateCalculation));
        }

        TextView note = text("Cálculo orientativo. La intensidad admisible depende también del método de instalación, agrupamiento, canalización y normativa aplicable. Verifica el resultado con REBT/UNE antes de ejecutar una instalación.", 12, MUTED, false);
        note.setPadding(dp(4),dp(14),dp(4),0);
        root.addView(note);

        setContentView(CalculatorTabs.surround(this, scroll, null, this::openProModule));
    }

    private void openProModule(ProModule module) {
        Intent intent = new Intent(this, ProActivity.class);
        intent.putExtra(ProActivity.MODULE, module.name());
        startActivity(intent);
    }

    private void buildAdvancedFields() {
        TextView intro = text("Ajustes que afinan el cálculo. Si no los modificas, se usan valores habituales de partida.",12,MUTED,false);
        intro.setPadding(dp(2),dp(4),dp(2),dp(8));
        advancedBox.addView(intro);

        length = edit("20");
        temperature = edit("30");
        addFieldPairTo(advancedBox,"Longitud ida (m)", length, "Temperatura (°C)", temperature);

        maxDrop = edit("3");
        addFieldTo(advancedBox,"Caída máxima (%)", maxDrop);

        material = spinner(new String[]{"Cobre", "Aluminio"});
        insulation = spinner(new String[]{"PVC 70 °C", "XLPE 90 °C"});
        addFieldPairTo(advancedBox,"Conductor", material, "Aislamiento", insulation);
    }

    private void updateLoadConversion() {
        if (loadConversion == null || load == null || voltage == null || powerFactor == null || phase == null || loadType == null) return;
        try {
            double value = num(load);
            double v = num(voltage);
            double pf = num(powerFactor);
            if (value < 0 || v <= 0 || pf <= 0 || pf > 1) throw new Exception();
            ElectricalCalculator.Phase ph = phase.getSelectedItemPosition()==0 ? ElectricalCalculator.Phase.MONOPHASE : ElectricalCalculator.Phase.THREE_PHASE;
            if (loadType.getSelectedItemPosition()==0) {
                double amps = ElectricalCalculator.currentFromPower(value,v,pf,ph);
                loadConversion.setText("≈ " + f(amps) + " A");
            } else {
                double watts = ElectricalCalculator.powerFromCurrent(value,v,pf,ph);
                loadConversion.setText("≈ " + whole(watts) + " W");
            }
        } catch (Exception e) {
            loadConversion.setText("—");
        }
    }

    private void toggleAdvanced() {
        advancedVisible = !advancedVisible;
        advancedBox.setVisibility(advancedVisible ? View.VISIBLE : View.GONE);
        advancedButton.setText(advancedVisible ? "▴ OCULTAR OPCIONES AVANZADAS" : "▾ MOSTRAR OPCIONES AVANZADAS");
    }

    private void showHelp() {
        ScrollView scroll = new ScrollView(this);
        TextView body = text(
                "GUÍA RÁPIDA\n\n" +
                "1. Elige qué quieres calcular: cable, magnetotérmico o carga máxima.\n" +
                "2. Introduce los datos que conoces.\n" +
                "3. La app traduce automáticamente W ↔ A.\n" +
                "4. Pulsa CALCULAR. Verás un resultado técnico y una explicación sencilla.\n\n" +
                "LA REGLA FUNDAMENTAL\n\n" +
                "I carga ≤ In magnetotérmico ≤ Iz cable\n\n" +
                "• I CARGA: corriente que necesita el aparato o conjunto de aparatos.\n\n" +
                "• In MAGNETOTÉRMICO: amperios nominales del interruptor automático que protege el circuito.\n\n" +
                "• Iz CABLE: corriente que puede soportar el conductor en las condiciones consideradas.\n\n" +
                "• TENSIÓN: normalmente 230 V en monofásica y 400 V en trifásica.\n\n" +
                "• FACTOR DE POTENCIA: en una carga resistiva suele estar cerca de 1; motores y otras cargas pueden tener valores menores.\n\n" +
                "• LONGITUD: distancia de ida desde el origen hasta la carga. La caída de tensión tiene en cuenta el recorrido eléctrico correspondiente.\n\n" +
                "• CAÍDA DE TENSIÓN: pérdida de tensión producida en el conductor. Una línea larga puede necesitar más sección aunque soporte la corriente.\n\n" +
                "MODO BÁSICO / AVANZADO\n\n" +
                "Para empezar puedes dejar ocultas las opciones avanzadas. La app usa 20 m, 30 °C, 3 %, cobre y PVC como valores iniciales. Si conoces tus condiciones reales, abre OPCIONES AVANZADAS y modifícalas. Pulsa VACIAR CAMPOS para borrar las cifras o EJEMPLO para cargar un caso de prueba.\n\n" +
                "IMPORTANTE\n\n" +
                "El resultado es orientativo. Una instalación real debe comprobarse según método de instalación, agrupamiento, canalización y normativa REBT/UNE aplicable.",
                15, TEXT, false);
        body.setPadding(dp(20),dp(12),dp(20),dp(20));
        scroll.addView(body);

        new AlertDialog.Builder(this)
                .setTitle("Ayuda de Cálculo Eléctrico")
                .setView(scroll)
                .setPositiveButton("ENTENDIDO", null)
                .show();
    }

    private void showWhy() {
        if (lastExplanation == null || lastExplanation.trim().isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle("¿Por qué este resultado?")
                .setMessage(lastExplanation)
                .setPositiveButton("ENTENDIDO", null)
                .show();
    }

    private void loadExample() {
        clearFieldMarks();
        invalidateCalculation();
        mode.setSelection(0);
        phase.setSelection(0);
        loadType.setSelection(0);
        load.setText("3500");
        voltage.setText("230");
        powerFactor.setText("0.95");
        length.setText("20");
        temperature.setText("30");
        maxDrop.setText("3");
        material.setSelection(0);
        insulation.setSelection(0);
        breaker.setSelection(5); // 20 A
        section.setSelection(1);
        updateLoadConversion();
        showStatus(Status.INFO,
                "EJEMPLO CARGADO\n\nCarga: 3.500 W  ≈ " + safeCurrentText() + "\nMagnetotérmico conocido: 20 A\n\nPulsa CALCULAR para buscar el conductor mínimo.");
    }

    private void updateMode() {
        if (mode == null || result == null) return;
        int m = mode.getSelectedItemPosition();
        boolean loadKnown = m != 2;
        loadType.setEnabled(loadKnown);
        load.setEnabled(loadKnown);
        load.setAlpha(loadKnown?1f:.45f);
        loadType.setAlpha(loadKnown?1f:.45f);
        loadConversion.setAlpha(loadKnown?1f:.45f);
        breaker.setEnabled(m != 1);
        breaker.setAlpha(m!=1?1f:.45f);
        section.setEnabled(m != 0);
        section.setAlpha(m!=0?1f:.45f);

        clearCalculation();
        if (m==0) showStatus(Status.INFO,"Vamos a calcular la sección mínima del cable. Introduce la carga y el magnetotérmico que ya conoces.");
        if (m==1) showStatus(Status.INFO,"Vamos a calcular un magnetotérmico adecuado a la carga y a la sección de cable conocida.");
        if (m==2) showStatus(Status.INFO,"Vamos a calcular la carga máxima permitida por el magnetotérmico y el cable seleccionados.");
    }

    private void calculate() {
        clearFieldMarks();
        hasCalculationResult = true;
        if (showMissingFields()) return;
        try {
            double v = num(voltage);
            double pf = num(powerFactor);
            double len = num(length);
            double temp = num(temperature);
            double dropMax = num(maxDrop);
            validateInputs(v,pf,len,temp,dropMax);

            ElectricalCalculator.Phase ph = phase.getSelectedItemPosition()==0 ? ElectricalCalculator.Phase.MONOPHASE : ElectricalCalculator.Phase.THREE_PHASE;
            ElectricalCalculator.Material mat = material.getSelectedItemPosition()==0 ? ElectricalCalculator.Material.COPPER : ElectricalCalculator.Material.ALUMINIUM;
            ElectricalCalculator.Insulation ins = insulation.getSelectedItemPosition()==0 ? ElectricalCalculator.Insulation.PVC_70 : ElectricalCalculator.Insulation.XLPE_90;
            double in = ElectricalCalculator.BREAKERS[breaker.getSelectedItemPosition()];
            double sec = ElectricalCalculator.SECTIONS[section.getSelectedItemPosition()];
            int m = mode.getSelectedItemPosition();

            saveValues();

            if (m == 2) {
                double iz = ElectricalCalculator.correctedAmpacity(sec,mat,ins,temp);
                if (iz <= 0) {
                    throw new IllegalArgumentException("La tabla no ofrece intensidad admisible para esa temperatura y aislamiento.");
                }
                double iMax = ElectricalCalculator.maximumLoadCurrent(in,iz,v,len,dropMax,sec,mat,ph);
                double pct = ElectricalCalculator.voltageDropPercent(iMax,v,len,sec,mat,ph);
                double pMax = ElectricalCalculator.powerFromCurrent(iMax,v,pf,ph);
                boolean protectionValid = in <= iz;
                if (!protectionValid) {
                    markChoice(breaker);
                    markChoice(section);
                }
                Status status = protectionValid ? Status.GOOD : Status.WARNING;

                String simple = protectionValid
                        ? "El magnetotérmico no supera la capacidad corregida del cable."
                        : "El magnetotérmico es demasiado grande para la capacidad calculada del cable.";
                String comparison = f(iMax) + " A ≤ " + f(in) + " A" +
                        (protectionValid ? " ≤ " + f(iz) + " A"
                                : "\n" + f(in) + " A > " + f(iz) + " A");

                showStatus(status,
                        (status==Status.GOOD ? "# Combinación coherente" : "# Revisar combinación") +
                        (protectionValid ? "\n\nCarga máxima: " : "\n\nLímite teórico de carga: ") +
                        whole(pMax) + " W\nIntensidad máxima: " + f(iMax) + " A" +
                        "\nMagnetotérmico: " + f(in) + " A" +
                        "\nCable:          " + sectionText(sec) + "\nIz del cable: " + f(iz) + " A" +
                        "\nCaída tensión:  " + f(pct) + " %  (máx. " + f(dropMax) + " %)" +
                        "\n\n" + comparison +
                        (protectionValid ? "\n~ " + simple : FieldIssues.warning("", simple)));

                lastExplanation = "El cálculo toma el menor límite de corriente entre el magnetotérmico, la capacidad corregida del cable y la caída de tensión máxima. " +
                        "Con los datos introducidos, la app obtiene aproximadamente " + f(iMax) + " A, equivalentes a " + whole(pMax) + " W. " +
                        simple;
                whyButton.setVisibility(View.VISIBLE);
                return;
            }

            double enteredLoad = num(load);
            if (enteredLoad < 0) throw new IllegalArgumentException("La carga no puede ser negativa.");
            double iLoad = loadType.getSelectedItemPosition()==0
                    ? ElectricalCalculator.currentFromPower(enteredLoad,v,pf,ph) : enteredLoad;
            double pLoad = ElectricalCalculator.powerFromCurrent(iLoad,v,pf,ph);

            if (m == 1) {
                double iz = ElectricalCalculator.correctedAmpacity(sec,mat,ins,temp);
                double rec = ElectricalCalculator.recommendBreaker(iLoad,iz);
                double pct = ElectricalCalculator.voltageDropPercent(iLoad,v,len,sec,mat,ph);
                boolean dropOk = pct <= dropMax;
                boolean breakerOk = rec > 0;
                Status status = breakerOk && dropOk ? Status.GOOD : (breakerOk ? Status.WARNING : Status.BAD);
                if (!breakerOk || !dropOk) markChoice(section);

                String headline = rec > 0 ? "# Dimensionamiento de la protección\nMagnetotérmico recomendado: " + f(rec) + " A" : "✕ SIN CALIBRE VÁLIDO";
                String translation = "Carga: " + f(iLoad) + " A\nPotencia equivalente: " + whole(pLoad) + " W";
                String plain;
                if (!breakerOk) plain = "La sección elegida no permite un calibre comercial adecuado para esa carga.";
                else if (!dropOk) plain = "La protección es posible, pero la caída de tensión aconseja aumentar la sección del cable.";
                else plain = "La carga queda por debajo del magnetotérmico propuesto y éste queda por debajo de la capacidad del cable.";

                showStatus(status, headline + "\n\n" + translation +
                        "\nIz cable: " + f(iz) + " A" +
                        "\nCaída tensión: " + f(pct) + " %  (máx. " + f(dropMax) + " %)" +
                        (rec > 0 ? "\n\n" + f(iLoad) + " A ≤ " + f(rec) + " A ≤ " + f(iz) + " A" : "") +
                        (breakerOk && dropOk ? "\n~ " + plain : FieldIssues.warning("", plain)));

                lastExplanation = "La app busca un calibre comercial que sea igual o superior a la corriente de carga (" + f(iLoad) + " A) y que, al mismo tiempo, no supere la capacidad corregida del cable (" + f(iz) + " A). " +
                        (dropOk ? "Además, la caída de tensión cumple el límite introducido." : "La caída de tensión no cumple el límite introducido, por eso aparece una advertencia.");
                whyButton.setVisibility(View.VISIBLE);
            } else {
                double recSec = ElectricalCalculator.recommendSection(iLoad,in,v,len,dropMax,mat,ins,temp,ph);
                if (recSec < 0) {
                    if (iLoad > in) {
                        load.setError("La intensidad de carga supera el calibre seleccionado.");
                        markChoice(breaker);
                    } else {
                        double largest = ElectricalCalculator.SECTIONS[ElectricalCalculator.SECTIONS.length - 1];
                        double largestIz = ElectricalCalculator.correctedAmpacity(largest, mat, ins, temp);
                        if (in > largestIz) markChoice(breaker);
                        if (ElectricalCalculator.voltageDropPercent(iLoad, v, len, largest, mat, ph) > dropMax) {
                            if (!advancedVisible) toggleAdvanced();
                            maxDrop.setError("Ninguna sección del catálogo satisface esta caída.");
                        }
                    }
                    showStatus(Status.BAD,
                            "✕ SIN SECCIÓN VÁLIDA EN LA TABLA\n\nCarga: " + f(iLoad) + " A\nPotencia equivalente: " + whole(pLoad) + " W\nMagnetotérmico: " + f(in) + " A\n\nComprueba que la carga no supera el magnetotérmico o revisa los datos.");
                    lastExplanation = "Para poder proteger correctamente el circuito debe cumplirse primero que la corriente de carga no sea mayor que la corriente nominal del magnetotérmico. Después, el cable debe soportar al menos esa protección y cumplir la caída de tensión indicada.";
                    whyButton.setVisibility(View.VISIBLE);
                    return;
                }

                double iz = ElectricalCalculator.correctedAmpacity(recSec,mat,ins,temp);
                double pct = ElectricalCalculator.voltageDropPercent(iLoad,v,len,recSec,mat,ph);

                showStatus(Status.GOOD,
                        "# Dimensionamiento del conductor\nSección recomendada: " + sectionText(recSec) +
                        "\n\nCarga:          " + f(iLoad) + " A\nPotencia equivalente: " + whole(pLoad) + " W" +
                        "\nMagnetotérmico: " + f(in) + " A" +
                        "\nIz cable:       " + f(iz) + " A" +
                        "\nCaída tensión:  " + f(pct) + " %" +
                        "\n\n" + f(iLoad) + " A ≤ " + f(in) + " A ≤ " + f(iz) + " A" +
                        "\n\nLa sección propuesta soporta la protección seleccionada y mantiene la caída de tensión dentro del límite indicado.");

                lastExplanation = "La sección " + sectionText(recSec) + " es la primera sección comercial de la tabla que cumple simultáneamente dos condiciones: " +
                        "su capacidad corregida (Iz ≈ " + f(iz) + " A) es igual o superior al magnetotérmico de " + f(in) + " A, y la caída de tensión queda en " + f(pct) + " %, por debajo del máximo de " + f(dropMax) + " %.";
                whyButton.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            showStatus(Status.BAD,"✕ REVISA LOS DATOS\n\n" + (e.getMessage()==null ? "Hay un valor no válido." : e.getMessage()));
            lastExplanation = "";
            whyButton.setVisibility(View.GONE);
        }
    }

    private boolean showMissingFields() {
        EditText[] fields = {load, voltage, powerFactor, length, temperature, maxDrop};
        String[] names = {loadType.getSelectedItemPosition() == 0 ? "Potencia de la carga" : "Intensidad de la carga",
                "Tensión", "Factor de potencia", "Longitud de ida", "Temperatura ambiente", "Caída máxima"};
        StringBuilder missing = new StringBuilder();
        boolean advancedMissing = false;
        for (int index = mode.getSelectedItemPosition() == 2 ? 1 : 0; index < fields.length; index++) {
            if (!clean(fields[index]).isEmpty()) continue;
            fields[index].setError("Campo requerido");
            missing.append("\n• ").append(names[index]);
            if (index >= 3) advancedMissing = true;
        }
        if (missing.length() == 0) return false;
        if (advancedMissing && !advancedVisible) toggleAdvanced();
        showStatus(Status.BAD, "Completa los campos necesarios para este cálculo." + missing);
        return true;
    }

    private void markChoice(Spinner spinner) {
        spinner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(255, 222, 220)));
        spinner.setContentDescription(((String) spinner.getSelectedItem()) + ". Revisar");
    }

    private void clearFieldMarks() {
        for (EditText field : new EditText[]{load, voltage, powerFactor, length, temperature, maxDrop}) {
            field.setError(null);
        }
        for (Spinner spinner : new Spinner[]{breaker, section, insulation}) {
            spinner.setBackgroundTintList(null);
            spinner.setContentDescription(null);
        }
    }

    private void validateInputs(double v, double pf, double len, double temp, double dropMax) {
        if (v <= 0 || v > 1000) { voltage.setError("Revisa la tensión."); throw new IllegalArgumentException("La tensión debe ser mayor que 0 V y razonable para baja tensión."); }
        if (pf <= 0 || pf > 1) { powerFactor.setError("Revisa el factor de potencia."); throw new IllegalArgumentException("El factor de potencia debe estar entre 0 y 1."); }
        if (len < 0 || len > 10000) {
            if (!advancedVisible) toggleAdvanced();
            length.setError("Revisa la longitud.");
            throw new IllegalArgumentException("Revisa la longitud del conductor.");
        }
        if (temp < -50 || temp > 120) {
            if (!advancedVisible) toggleAdvanced();
            temperature.setError("Revisa la temperatura.");
            throw new IllegalArgumentException("Revisa la temperatura ambiente.");
        }
        if (dropMax <= 0 || dropMax > 20) {
            if (!advancedVisible) toggleAdvanced();
            maxDrop.setError("Revisa la caída máxima.");
            throw new IllegalArgumentException("La caída de tensión máxima debe ser mayor que 0 % y razonable.");
        }
    }

    private void clearFields() {
        clearFieldMarks();
        for (EditText field : new EditText[]{load, voltage, powerFactor, length, temperature, maxDrop}) {
            field.setText("");
        }
        updateMode();
        updateLoadConversion();
        saveValues();
    }

    private void resetForm() {
        clearFieldMarks();
        mode.setSelection(0);
        phase.setSelection(0);
        loadType.setSelection(0);
        load.setText("3500");
        voltage.setText("230");
        powerFactor.setText("0.95");
        length.setText("20");
        temperature.setText("30");
        maxDrop.setText("3");
        material.setSelection(0);
        insulation.setSelection(0);
        breaker.setSelection(4);
        section.setSelection(1);
        advancedVisible = false;
        advancedBox.setVisibility(View.GONE);
        advancedButton.setText("▾ MOSTRAR OPCIONES AVANZADAS");
        lastExplanation = "";
        whyButton.setVisibility(View.GONE);
        updateLoadConversion();
        updateMode();
    }

    private void saveValues() {
        prefs.edit()
                .putInt("mode", mode.getSelectedItemPosition())
                .putInt("phase", phase.getSelectedItemPosition())
                .putInt("loadType", loadType.getSelectedItemPosition())
                .putString("load", clean(load))
                .putString("voltage", clean(voltage))
                .putString("pf", clean(powerFactor))
                .putString("length", clean(length))
                .putString("temperature", clean(temperature))
                .putString("drop", clean(maxDrop))
                .putInt("material", material.getSelectedItemPosition())
                .putInt("insulation", insulation.getSelectedItemPosition())
                .putInt("breaker", breaker.getSelectedItemPosition())
                .putInt("section", section.getSelectedItemPosition())
                .apply();
    }

    private void restoreValues() {
        try {
            mode.setSelection(prefs.getInt("mode",0));
            phase.setSelection(prefs.getInt("phase",0));
            loadType.setSelection(prefs.getInt("loadType",0));
            load.setText(prefs.getString("load","3500"));
            voltage.setText(prefs.getString("voltage","230"));
            powerFactor.setText(prefs.getString("pf","0.95"));
            length.setText(prefs.getString("length","20"));
            temperature.setText(prefs.getString("temperature","30"));
            maxDrop.setText(prefs.getString("drop","3"));
            material.setSelection(prefs.getInt("material",0));
            insulation.setSelection(prefs.getInt("insulation",0));
            breaker.setSelection(prefs.getInt("breaker",4));
            section.setSelection(prefs.getInt("section",1));
        } catch (Exception ignored) { }
    }

    private String safeCurrentText() {
        try {
            double p = num(load), v = num(voltage), pf = num(powerFactor);
            ElectricalCalculator.Phase ph = phase.getSelectedItemPosition()==0 ? ElectricalCalculator.Phase.MONOPHASE : ElectricalCalculator.Phase.THREE_PHASE;
            return f(ElectricalCalculator.currentFromPower(p,v,pf,ph)) + " A";
        } catch (Exception e) {
            return "—";
        }
    }

    private enum Status { GOOD, WARNING, BAD, INFO }

    private void showStatus(Status status, String message) {
        int fg;
        int bg;
        if (status == Status.GOOD) { fg = GREEN; bg = Color.rgb(236,253,243); }
        else if (status == Status.WARNING) { fg = AMBER; bg = Color.rgb(255,249,235); }
        else if (status == Status.BAD) { fg = RED; bg = Color.rgb(254,243,242); }
        else { fg = NAVY; bg = Color.WHITE; }
        result.setText(CalculationResult.format(message));
        result.setTextColor(fg);
        result.setBackground(roundRect(bg,14));
    }

    private void sectionHeader(String title) {
        TextView header = text(title, 12, MUTED, true);
        header.setAllCaps(true);
        header.setLetterSpacing(0.05f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0, dp(18), 0, dp(8));
        header.setLayoutParams(lp);
        form.addView(header);
    }

    private void addFieldPair(String label1, View field1, String label2, View field2) {
        addFieldPairTo(form,label1,field1,label2,field2);
    }

    private void addFieldPairTo(LinearLayout parent, String label1, View field1, String label2, View field2) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1,-2);
        rowLp.setMargins(0,0,0,dp(12));

        LinearLayout col1 = new LinearLayout(this);
        col1.setOrientation(LinearLayout.VERTICAL);
        col1.addView(fieldLabel(label1, field1));
        col1.addView(field1);
        LinearLayout.LayoutParams col1Lp = new LinearLayout.LayoutParams(0,-2,1f);
        col1Lp.setMarginEnd(dp(6));

        LinearLayout col2 = new LinearLayout(this);
        col2.setOrientation(LinearLayout.VERTICAL);
        col2.addView(fieldLabel(label2, field2));
        col2.addView(field2);
        LinearLayout.LayoutParams col2Lp = new LinearLayout.LayoutParams(0,-2,1f);
        col2Lp.setMarginStart(dp(6));

        row.addView(col1, col1Lp);
        row.addView(col2, col2Lp);
        parent.addView(row, rowLp);
    }

    private TextView fieldLabel(String title, View control) {
        String message = switch (title) {
            case "¿Qué deseas calcular?" -> "Elige si quieres obtener la sección del conductor, el calibre del magnetotérmico o la carga máxima. Completa los datos que pide cada opción.";
            case "Sistema" -> "Selecciona monofásica o trifásica según la alimentación de la carga. En trifásica, introduce la tensión entre fases.";
            case "Dato que conoces" -> "Selecciona potencia si tienes un valor en vatios, o intensidad si conoces la corriente en amperios.";
            case "Tensión (V)" -> "Introduce la tensión de alimentación. En monofásica se mide entre fase y neutro y en trifásica, entre fases.";
            case "Factor potencia" -> "Relaciona la potencia activa con la aparente. Suele aproximarse a 1 en cargas resistivas. Usa el dato del equipo si lo conoces.";
            case "Longitud ida (m)" -> "Introduce la distancia de ida desde el origen del circuito hasta la carga.";
            case "Temperatura (°C)" -> "Introduce la temperatura ambiente del lugar por donde discurre el cable. Afecta a su intensidad admisible.";
            case "Caída máxima (%)" -> "Introduce el porcentaje máximo de pérdida de tensión previsto para este tramo.";
            case "Conductor" -> "Selecciona el material del conductor. Afecta a la resistencia eléctrica y a la caída de tensión.";
            case "Aislamiento" -> "Selecciona el aislamiento indicado en la ficha del cable. Su límite térmico influye en la intensidad admisible.";
            case "Magnetotérmico" -> "Selecciona la intensidad nominal del interruptor que protege el circuito frente a sobrecargas y cortocircuitos.";
            case "Sección cable" -> "Selecciona el área nominal del conductor en mm² según la ficha del cable.";
            default -> throw new IllegalArgumentException(title);
        };
        TextView label = label(title);
        FieldHelp.bind(label, control, title, () -> message);
        return label;
    }

    private void addField(String name, View view) { addFieldTo(form, name, view); }

    private void addFieldTo(LinearLayout parent, String name, View view) {
        parent.addView(fieldLabel(name, view));
        parent.addView(view);
    }

    private TextView smallAction(String caption) {
        TextView t = text(caption,13,BLUE,true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8),dp(11),dp(8),dp(11));
        t.setBackground(roundRect(Color.rgb(245,248,255),10));
        return t;
    }

    private LinearLayout.LayoutParams weightedStart(int marginStart) { return weightedStart(marginStart,-2,1f); }
    private LinearLayout.LayoutParams weightedEnd(int marginEnd,int height,float weight){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,height,weight);lp.setMarginEnd(marginEnd);return lp; }
    private LinearLayout.LayoutParams weightedStart(int marginStart,int height,float weight){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,height,weight);lp.setMarginStart(marginStart);return lp; }

    private void addWatcher(EditText e) {
        e.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) { }
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                invalidateCalculation();
                if (e == load || e == voltage || e == powerFactor) updateLoadConversion();
            }
            public void afterTextChanged(Editable s) { }
        });
    }

    private void invalidateCalculation() {
        if (!hasCalculationResult) return;
        clearCalculation();
        showStatus(Status.INFO, "Datos modificados. Pulsa CALCULAR para actualizar el resultado.");
    }

    private void clearCalculation() {
        hasCalculationResult = false;
        lastExplanation = "";
        whyButton.setVisibility(View.GONE);
    }

    private LinearLayout card() {
        LinearLayout l=new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackground(roundRect(Color.WHITE,16));
        return l;
    }

    private TextView label(String s) {
        TextView t=text(s,13,Color.rgb(71,84,103),true);
        t.setPadding(dp(2),dp(12),0,dp(5));
        return t;
    }

    private TextView text(String s,int sp,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }

    private EditText edit(String initial){
        EditText e=new EditText(this);
        e.setText(initial);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setPadding(dp(12),0,dp(12),0);
        DecimalInput.configure(e);
        e.setBackground(roundRect(Color.rgb(242,244,247),10));
        e.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(48)));
        return e;
    }

    private Spinner spinner(String[] items){
        Spinner s=new Spinner(this);
        ArrayAdapter<String>a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,items);
        s.setAdapter(a);
        s.setBackground(roundRect(Color.rgb(242,244,247),10));
        s.setPadding(dp(8),0,dp(8),0);
        s.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(50)));
        return s;
    }

    private AdapterView.OnItemSelectedListener listener(Runnable r){
        return new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?>p,View v,int pos,long id){r.run();}
            public void onNothingSelected(AdapterView<?>p){}
        };
    }

    private GradientDrawable roundRect(int color,int radius){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private String clean(EditText e){ return e.getText().toString().trim(); }
    private double num(EditText e){
        String s=clean(e).replace(',','.');
        if(s.isEmpty())throw new IllegalArgumentException("Falta un valor.");
        return Double.parseDouble(s);
    }
    private String f(double d){ return String.format(Locale.getDefault(),"%.2f",d); }
    private String whole(double d){ return String.format(Locale.getDefault(),"%.2f",d); }
    private String sectionText(double d){ return (d%1==0?String.format(Locale.getDefault(),"%.2f",d):String.format(Locale.getDefault(),"%.1f",d))+" mm²"; }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }
}
