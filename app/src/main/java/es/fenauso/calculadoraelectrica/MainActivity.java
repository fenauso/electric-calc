package es.fenauso.calculadoraelectrica;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;

import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout form;
    private Spinner mode, phase, loadType, material, insulation, breaker, section;
    private EditText load, voltage, powerFactor, length, temperature, maxDrop;
    private TextView result, equation, loadHint;

    private final String[] BREAKER_LABELS = {"2 A","4 A","6 A","10 A","16 A","20 A","25 A","32 A","40 A","50 A","63 A","80 A","100 A","125 A"};
    private final String[] SECTION_LABELS = {"1,5 mm²","2,5 mm²","4 mm²","6 mm²","10 mm²","16 mm²","25 mm²","35 mm²","50 mm²","70 mm²","95 mm²","120 mm²"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(22,50,79));
        buildUi();
        updateMode();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(28));
        root.setBackgroundColor(Color.rgb(247,249,252));
        scroll.addView(root);

        TextView title = text("⚡ Cálculo Eléctrico", 26, Color.rgb(22,50,79), true);
        root.addView(title);
        TextView subtitle = text("Carga · Magnetotérmico · Conductor", 15, Color.rgb(102,112,133), false);
        subtitle.setPadding(0,dp(3),0,dp(14)); root.addView(subtitle);

        equation = text("I CARGA ≤ In MAGNETOTÉRMICO ≤ Iz CABLE", 14, Color.WHITE, true);
        equation.setGravity(Gravity.CENTER); equation.setPadding(dp(10),dp(12),dp(10),dp(12));
        equation.setBackground(roundRect(Color.rgb(22,50,79), 14)); root.addView(equation);

        form = card(); form.setPadding(dp(14),dp(14),dp(14),dp(14));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1,-2); cardLp.setMargins(0,dp(14),0,0);
        root.addView(form, cardLp);

        mode = spinner(new String[]{"Calcular CONDUCTOR", "Calcular MAGNETOTÉRMICO", "Calcular CARGA máxima"});
        addField("¿Qué deseas calcular?", mode);
        mode.setOnItemSelectedListener(listener(this::updateMode));

        phase = spinner(new String[]{"Monofásica", "Trifásica"}); addField("Sistema", phase);
        phase.setOnItemSelectedListener(listener(() -> {
            if (phase.getSelectedItemPosition()==0 && voltage.getText().toString().equals("400")) voltage.setText("230");
            if (phase.getSelectedItemPosition()==1 && voltage.getText().toString().equals("230")) voltage.setText("400");
        }));

        loadType = spinner(new String[]{"Potencia (W)", "Intensidad (A)"}); addField("Dato de carga", loadType);
        loadType.setOnItemSelectedListener(listener(() -> loadHint.setText(loadType.getSelectedItemPosition()==0 ? "Potencia de la carga (W)" : "Intensidad de la carga (A)")));
        loadHint = label("Potencia de la carga (W)"); form.addView(loadHint);
        load = edit("3500"); form.addView(load);

        voltage = edit("230"); addField("Tensión (V)", voltage);
        powerFactor = edit("0.95"); addField("Factor de potencia (cos φ)", powerFactor);
        length = edit("20"); addField("Longitud de ida (m)", length);
        temperature = edit("30"); addField("Temperatura ambiente (°C)", temperature);
        maxDrop = edit("3"); addField("Caída de tensión máxima (%)", maxDrop);

        material = spinner(new String[]{"Cobre", "Aluminio"}); addField("Conductor", material);
        insulation = spinner(new String[]{"PVC 70 °C", "XLPE 90 °C"}); addField("Aislamiento", insulation);
        breaker = spinner(BREAKER_LABELS); breaker.setSelection(4); addField("Magnetotérmico conocido", breaker);
        section = spinner(SECTION_LABELS); section.setSelection(1); addField("Sección conocida", section);

        Button calculate = new Button(this);
        calculate.setText("CALCULAR"); calculate.setTextColor(Color.WHITE); calculate.setTextSize(16); calculate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        calculate.setBackground(roundRect(Color.rgb(37,99,235), 14));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1,dp(54)); btnLp.setMargins(0,dp(18),0,0);
        form.addView(calculate, btnLp); calculate.setOnClickListener(v -> calculate());

        result = text("Introduce los datos y pulsa CALCULAR", 16, Color.rgb(24,34,48), false);
        result.setPadding(dp(16),dp(16),dp(16),dp(16)); result.setBackground(roundRect(Color.WHITE, 14));
        LinearLayout.LayoutParams resultLp = new LinearLayout.LayoutParams(-1,-2); resultLp.setMargins(0,dp(14),0,0); root.addView(result,resultLp);

        TextView note = text("Cálculo orientativo. La intensidad admisible depende también del método de instalación, agrupamiento, canalización y normativa aplicable. Verifica el resultado con REBT/UNE antes de ejecutar una instalación.", 12, Color.rgb(102,112,133), false);
        note.setPadding(dp(4),dp(14),dp(4),0); root.addView(note);
        setContentView(scroll);
    }

    private void updateMode() {
        int m = mode.getSelectedItemPosition();
        boolean loadKnown = m != 2;
        loadType.setEnabled(loadKnown); load.setEnabled(loadKnown); load.setAlpha(loadKnown?1f:.45f); loadType.setAlpha(loadKnown?1f:.45f);
        breaker.setEnabled(m != 1); breaker.setAlpha(m!=1?1f:.45f);
        section.setEnabled(m != 0); section.setAlpha(m!=0?1f:.45f);
        if (m==0) result.setText("Se calculará la sección mínima que cumpla intensidad y caída de tensión.");
        if (m==1) result.setText("Se propondrá el calibre comercial que cumpla I ≤ In ≤ Iz.");
        if (m==2) result.setText("Se calculará la carga máxima permitida por la protección y el cable.");
    }

    private void calculate() {
        try {
            double v = num(voltage), pf = num(powerFactor), len = num(length), temp = num(temperature), dropMax = num(maxDrop);
            if (pf > 1) throw new IllegalArgumentException("El factor de potencia no puede ser mayor que 1");
            ElectricalCalculator.Phase ph = phase.getSelectedItemPosition()==0 ? ElectricalCalculator.Phase.MONOPHASE : ElectricalCalculator.Phase.THREE_PHASE;
            ElectricalCalculator.Material mat = material.getSelectedItemPosition()==0 ? ElectricalCalculator.Material.COPPER : ElectricalCalculator.Material.ALUMINIUM;
            ElectricalCalculator.Insulation ins = insulation.getSelectedItemPosition()==0 ? ElectricalCalculator.Insulation.PVC_70 : ElectricalCalculator.Insulation.XLPE_90;
            double in = ElectricalCalculator.BREAKERS[breaker.getSelectedItemPosition()];
            double sec = ElectricalCalculator.SECTIONS[section.getSelectedItemPosition()];
            int m = mode.getSelectedItemPosition();

            if (m == 2) {
                double iz = ElectricalCalculator.correctedAmpacity(sec,mat,ins,temp);
                double iMax = Math.min(in,iz);
                double pct = ElectricalCalculator.voltageDropPercent(iMax,v,len,sec,mat,ph);
                double pMax = ElectricalCalculator.powerFromCurrent(iMax,v,pf,ph);
                boolean valid = in <= iz;
                show(valid,
                        "CARGA MÁXIMA: " + whole(pMax) + " W  (" + f(iMax) + " A)\n\n" +
                        "In = " + f(in) + " A   ·   Iz corregida = " + f(iz) + " A\n" +
                        "Caída a carga máxima = " + f(pct) + " %\n\n" +
                        (valid ? "✓ Se cumple: I ≤ In ≤ Iz" : "✕ El magnetotérmico supera la capacidad del cable."));
                return;
            }

            double iLoad = loadType.getSelectedItemPosition()==0
                    ? ElectricalCalculator.currentFromPower(num(load),v,pf,ph) : num(load);

            if (m == 1) {
                double iz = ElectricalCalculator.correctedAmpacity(sec,mat,ins,temp);
                double rec = ElectricalCalculator.recommendBreaker(iLoad,iz);
                double pct = ElectricalCalculator.voltageDropPercent(iLoad,v,len,sec,mat,ph);
                boolean ok = rec > 0 && pct <= dropMax;
                String headline = rec > 0 ? "MAGNETOTÉRMICO: " + f(rec) + " A" : "SIN CALIBRE VÁLIDO";
                show(ok, headline + "\n\nI carga = " + f(iLoad) + " A   ·   Iz = " + f(iz) + " A\n"+
                        "Caída de tensión = " + f(pct) + " % (máx. " + f(dropMax) + " %)\n\n"+
                        (rec<0 ? "✕ La sección elegida no admite un calibre comercial adecuado." :
                                pct>dropMax ? "⚠ La protección es posible, pero la caída de tensión exige mayor sección." : "✓ Se cumple: I ≤ In ≤ Iz"));
            } else {
                double recSec = ElectricalCalculator.recommendSection(iLoad,in,v,len,dropMax,mat,ins,temp,ph);
                if (recSec < 0) {
                    show(false,"SIN SECCIÓN VÁLIDA EN LA TABLA\n\nComprueba que I carga ≤ In y aumenta el calibre o revisa los datos.");
                    return;
                }
                double iz = ElectricalCalculator.correctedAmpacity(recSec,mat,ins,temp);
                double pct = ElectricalCalculator.voltageDropPercent(iLoad,v,len,recSec,mat,ph);
                show(true,"CONDUCTOR MÍNIMO: " + sectionText(recSec) + "\n\nI carga = " + f(iLoad) + " A   ·   In = " + f(in) + " A\n"+
                        "Iz corregida = " + f(iz) + " A\nCaída de tensión = " + f(pct) + " %\n\n✓ Se cumple: I ≤ In ≤ Iz");
            }
        } catch (Exception e) {
            show(false,"Revisa los datos:\n" + e.getMessage());
        }
    }

    private void show(boolean good, String message) {
        result.setText(message); result.setTextColor(good?Color.rgb(19,122,75):Color.rgb(180,35,24));
        result.setBackground(roundRect(good?Color.rgb(236,253,243):Color.rgb(254,243,242),14));
    }

    private LinearLayout card() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setBackground(roundRect(Color.WHITE,16)); return l; }
    private void addField(String name, View view) { form.addView(label(name)); form.addView(view); }
    private TextView label(String s) { TextView t=text(s,13,Color.rgb(71,84,103),true); t.setPadding(dp(2),dp(12),0,dp(5)); return t; }
    private TextView text(String s,int sp,int color,boolean bold){ TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t; }
    private EditText edit(String initial){ EditText e=new EditText(this);e.setText(initial);e.setTextSize(16);e.setSingleLine(true);e.setPadding(dp(12),0,dp(12),0);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setBackground(roundRect(Color.rgb(242,244,247),10));e.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(48)));return e; }
    private Spinner spinner(String[] items){ Spinner s=new Spinner(this);ArrayAdapter<String>a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,items);s.setAdapter(a);s.setBackground(roundRect(Color.rgb(242,244,247),10));s.setPadding(dp(8),0,dp(8),0);s.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(50)));return s; }
    private AdapterView.OnItemSelectedListener listener(Runnable r){ return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int pos,long id){r.run();}public void onNothingSelected(AdapterView<?>p){}}; }
    private GradientDrawable roundRect(int color,int radius){ GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g; }
    private double num(EditText e){ String s=e.getText().toString().trim().replace(',','.'); if(s.isEmpty())throw new IllegalArgumentException("Falta un valor"); return Double.parseDouble(s); }
    private String f(double d){ return String.format(Locale.getDefault(),"%.2f",d); }
    private String whole(double d){ return String.format(Locale.getDefault(),"%.0f",d); }
    private String sectionText(double d){ return (d%1==0?String.format(Locale.getDefault(),"%.0f",d):String.format(Locale.getDefault(),"%.1f",d))+" mm²"; }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }
}
