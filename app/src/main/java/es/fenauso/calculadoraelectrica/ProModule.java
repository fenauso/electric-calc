package es.fenauso.calculadoraelectrica;

import java.util.Map;

import static es.fenauso.calculadoraelectrica.ProCalculations.*;

enum ProModule {
    RESISTANCE("Resistencias", "Circuitos de CC y conductores. Introduce dos magnitudes para la ley de Ohm.",
            "mode:Ley de Ohm|Serie|Paralelo|Conductor",
            "v:Tensión (V)", "i:Intensidad (A)", "r:Resistencia (Ω)",
            "list:Resistencias (Ω), separadas por punto y coma",
            "metal:Cobre|Aluminio", "l:Longitud total del conductor (m)", "s:Sección (mm²)", "t:Temperatura del conductor (°C)"),
    REACTIVE("Compensación de reactiva", "Cálculo para una carga inductiva en régimen sinusoidal.",
            "p:Potencia activa (kW)", "a:Cos φ inicial", "b:Cos φ objetivo", "v:Tensión de línea (V)",
            "f:Frecuencia (Hz)=50", "mode:Monofásica|Trifásica en estrella|Trifásica en triángulo"),
    CONDUIT("Canalizaciones y tubos", "ITC-BT-21. Distingue diámetro exterior nominal y diámetro interior libre.",
            "mode:Superficie|Empotrado|Al aire|Enterrado",
            "rule:Ocupación geométrica|Consultar tabla de conductores iguales",
            "list:Diámetros exteriores reales de los cables (mm), separados por punto y coma",
            "s:Sección nominal de cada conductor igual (mm²)", "n:Número de conductores iguales",
            "d:Diámetro interior del tubo propuesto (mm, opcional)"),
    ENERGY("Consumo y costes", "Puedes calcular un periodo por tarifa. Incluye los términos fijos del periodo si quieres incorporarlos.",
            "p:Potencia media (kW)", "h:Horas de uso al día", "d:Días", "price:Precio de la energía (€/kWh)",
            "fixed:Coste fijo del periodo en euros=0", "tax:Impuesto sobre el subtotal (%)=0"),
    SOLAR("Fotovoltaica · strings", "Comprueba un grupo de MPPT idénticos y con el mismo reparto. Los valores térmicos son de célula.",
            "p:Potencia STC del módulo (Wp)", "voc:Voc STC (V)", "vmp:Vmp STC (V)",
            "isc:Isc STC (A)", "imp:Imp STC (A)", "cvoc:Coeficiente de Voc (%/°C)",
            "cvmp:Coeficiente de Vmp (%/°C)", "cisc:Coeficiente de Isc (%/°C, opcional)",
            "cold:Temperatura de célula mínima (°C)", "hot:Temperatura de célula máxima (°C)",
            "irr:Irradiancia máxima de diseño (W/m²)=1000",
            "max:Tensión CC máxima efectiva de equipos y módulos (V)",
            "start:Tensión de arranque del inversor (V)", "low:MPPT mínimo (V)", "high:MPPT máximo (V)",
            "imax:Intensidad operativa máxima por MPPT (A)", "scmax:Isc máxima por MPPT (A)",
            "inputs:Entradas físicas por MPPT", "mppt:MPPT utilizados",
            "ac:Potencia nominal CA (kW)", "pmax:Potencia FV máxima admitida (kWp, opcional)",
            "ns:Módulos en serie", "np:Strings por MPPT", "fuse:Fusible serie máximo del módulo (A, opcional)"),
    DC("Fotovoltaica · cable CC", "Calcula un tramo CC de dos conductores. Introduce la intensidad de diseño del tramo con el criterio aplicable.",
            "v:Tensión de trabajo del tramo (V)", "i:Intensidad de servicio (A)", "ib:Intensidad de diseño (A)",
            "l:Longitud de ida (m)", "max:Caída asignada al tramo (%)", "metal:Cobre|Aluminio",
            "s:Sección del cable a comprobar (mm², opcional)",
            "iz:Intensidad admisible corregida Iz de esa sección (A, opcional)",
            "method:Método y condiciones de instalación (texto, opcional)"),
    BATTERY("Fotovoltaica · baterías", "Dimensionamiento con módulos iguales. Usa los límites de conexiones en serie y paralelo de su fabricante.",
            "daily:Consumo diario en CA (kWh)", "days:Autonomía (días)", "eta:Rendimiento descarga a CA (0–1)",
            "dod:Fracción de capacidad utilizable (0–1)", "bank:Tensión nominal objetivo (V)",
            "v:Tensión nominal de un módulo (V)", "ah:Capacidad de un módulo (Ah)",
            "ms:Máximo de módulos en serie permitido", "mp:Máximo de ramas en paralelo permitido",
            "min:Tensión mínima de trabajo del banco (V)", "peak:Potencia punta CA de descarga (kW)",
            "ids:Corriente máxima de descarga por módulo (A)", "ics:Corriente máxima de carga por módulo (A)",
            "bmsd:Límite total de descarga de BMS e inversor (A, opcional)",
            "bmsc:Límite total de carga de BMS y cargador (A, opcional)",
            "charge:Potencia neta disponible para recarga (kW)", "ec:Rendimiento de carga (0–1)"),
    EV("Vehículo eléctrico · modo 3", "Un punto de recarga en CA. Indica las fases que utiliza el cargador.",
            "supply:Suministro monofásico 230 V|Suministro trifásico 400 V",
            "phase:Cargador monofásico 230 V|Cargador trifásico 400 V",
            "p:Potencia activa del punto (kW)", "i:Corriente máxima configurada (A, opcional)",
            "pf:Factor de potencia (0–1)=1", "l:Longitud de ida (m)", "drop:Caída asignada al circuito (%)=5",
            "site:Vivienda unifamiliar|Garaje comunitario residencial|Otro emplazamiento",
            "out:Interior|Exterior", "scheme:Esquema ITC-BT-52 (texto, opcional)",
            "metal:Cobre|Aluminio", "s:Sección a comprobar (mm², opcional)",
            "iz:Intensidad admisible corregida Iz de esa sección (A, opcional)",
            "method:Método y condiciones de instalación (texto, opcional)",
            "rdc:RDC-DD no verificado|RDC-DD IEC 62955 integrado|RDC-DD IEC 62955 externo|Sin RDC-DD",
            "spd:Protecciones aguas arriba sin verificar|Protección temporal y transitoria declarada",
            "distance:Distancia al protector transitorio aguas arriba (m, opcional)",
            "kwh:Capacidad útil del vehículo (kWh, opcional)", "from:Carga inicial (%)=20",
            "to:Carga objetivo (%)=80", "car:Límite del cargador de a bordo (kW, opcional)",
            "grid:Potencia disponible en la instalación (kW, opcional)", "eta:Rendimiento de carga (0–1)=0,9");

    final String title;
    final String help;
    final String[] fields;

    ProModule(String title, String help, String... fields) {
        this.title = title;
        this.help = help;
        this.fields = fields;
    }

    String calculate(Map<String, String> values) {
        Input x = new Input(values);
        return switch (this) {
            case RESISTANCE -> resistance(x);
            case REACTIVE -> reactive(x);
            case CONDUIT -> ConduitSizing.calculate(x);
            case ENERGY -> energy(x);
            case SOLAR -> SolarSizing.strings(x);
            case DC -> dc(x);
            case BATTERY -> SolarSizing.battery(x);
            case EV -> EvSizing.calculate(x);
        };
    }

    private static String resistance(Input x) {
        int mode = x.option("mode");
        if (mode == 1 || mode == 2) {
            return "Resistencia equivalente: " + show(equivalent(list(x.text("list")), mode == 2))
                    + " Ω\n" + (mode == 1 ? "R = Σ Rᵢ" : "1/R = Σ (1/Rᵢ)");
        }
        if (mode == 3) {
            boolean copper = x.option("metal") == 0;
            double rho = copper ? 0.01724 : 0.02826;
            double alpha = copper ? 0.00393 : 0.00403;
            double corrected = positive(rho * (1 + alpha * (x.number("t") - 20)), "Resistividad a esa temperatura");
            return "Resistencia estimada: " + show(corrected * x.positive("l") / x.positive("s"))
                    + " Ω\nR = ρ(T) · L / S\nL es la longitud total del conductor considerado.";
        }
        if ((x.has("v") ? 1 : 0) + (x.has("i") ? 1 : 0) + (x.has("r") ? 1 : 0) != 2)
            throw new IllegalArgumentException("Introduce exactamente dos valores: tensión, intensidad o resistencia.");
        double v = x.optionalPositive("v"), i = x.optionalPositive("i"), r = x.optionalPositive("r");
        if (!x.has("v")) v = i * r;
        if (!x.has("i")) i = v / r;
        if (!x.has("r")) r = v / i;
        return show(v) + " V · " + show(i) + " A · " + show(r) + " Ω\nPotencia disipada: "
                + show(v * i) + " W\nV = I · R; P = V · I";
    }

    private static String reactive(Input x) {
        double kvar = compensation(x.positive("p"), x.number("a"), x.number("b"));
        double c = capacitorMicrofarads(kvar, x.positive("v"), x.positive("f"), x.option("mode"));
        return "Potencia de compensación: " + show(kvar) + " kvar\nCapacidad por rama: " + show(c)
                + " µF\nQc = P · (tan φ inicial − tan φ objetivo)\n"
                + "Pendiente para seleccionar la batería: perfil de carga, armónicos y características del equipo.";
    }

    private static String energy(Input x) {
        double h = x.nonnegative("h");
        if (h > 24) throw new IllegalArgumentException("Las horas diarias no pueden superar 24.");
        double kwh = x.nonnegative("p") * h * x.positive("d");
        double variable = kwh * x.nonnegative("price");
        double fixed = x.nonnegative("fixed");
        double tax = (variable + fixed) * x.nonnegative("tax") / 100;
        return "Energía: " + show(kwh) + " kWh\nE = P · horas/día · días\nCoste variable: " + show(variable)
                + " €\nCoste fijo: " + show(fixed) + " €\nImpuesto estimado: " + show(tax)
                + " €\nTotal: " + show(variable + fixed + tax) + " €";
    }

    private static String dc(Input x) {
        double voltage = x.positive("v"), current = x.positive("i"), design = x.positive("ib");
        if (design < current) throw new IllegalArgumentException("La intensidad de diseño debe ser al menos la de servicio.");
        double length = x.nonnegative("l"), drop = x.positive("max"), rho = x.option("metal") == 0 ? 0.0225 : 0.036;
        double minimum = minimumSection(voltage, length, current, drop, rho, 2, 0);
        return CableSizing.result(x, voltage, current, design, length, rho, 2, drop, minimum, 0)
                + "\nΔV = 2 · ρ · L · I / S. Caída a corriente de servicio.\n"
                + "Pendiente: verificar tensión asignada, cortocircuito y protecciones CC del tramo. "
                + "Si aplica ITC-BT-40 §5, verifica el 125 % de intensidad y el 1,5 % hasta el punto de interconexión.";
    }

    static final class Input {
        private final Map<String, String> data;
        Input(Map<String, String> data) { this.data = data; }
        String text(String key) { return data.getOrDefault(key, "").trim(); }
        boolean has(String key) { return !text(key).isEmpty(); }
        double number(String key) {
            if (!has(key)) throw new IllegalArgumentException("Completa los campos necesarios para este cálculo.");
            return decimal(text(key));
        }
        double positive(String key) { return ProCalculations.positive(number(key), "El valor"); }
        double nonnegative(String key) { return ProCalculations.nonnegative(number(key), "El valor"); }
        double optionalPositive(String key) { return has(key) ? positive(key) : 0; }
        int integer(String key) { return count(number(key), "El número de unidades"); }
        int option(String key) { return Integer.parseInt(text(key)); }
    }
}
