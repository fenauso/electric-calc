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
    PV_PROJECT("Proyecto fotovoltaico", "Calcula una propuesta de autoconsumo residencial conectado a red a partir de un objetivo y las fichas de los equipos.",
            "design:Con fichas de equipos|Estimación inicial sin ficha de inversor",
            "goal:Objetivo en kWp|Objetivo por energía diaria",
            "ratio:Relación CC/CA supuesta=1,2",
            "target:Potencia pico objetivo (kWp)=5,5",
            "dailygen:Energía diaria que se desea cubrir (kWh)=16",
            "sun:Horas solares pico del periodo de diseño=4,5",
            "pr:Rendimiento global de generación (0–1)=0,8",
            "cold:Temperatura de célula mínima (°C)=-5",
            "hot:Temperatura de célula máxima (°C)=70",
            "irr:Irradiancia máxima de diseño (W/m²)=1050",
            "p:Potencia por módulo (Wp)=440",
            "voc:Voc STC del módulo (V)=40,6",
            "vmp:Vmp STC del módulo (V)=34",
            "isc:Isc STC del módulo (A)=13,8",
            "imp:Imp STC del módulo (A)=12,94",
            "cvoc:Coeficiente de Voc (%/°C)=-0,25",
            "cvmp:Coeficiente de Vmp (%/°C)=-0,30",
            "cisc:Coeficiente de Isc (%/°C, opcional)=0,045",
            "reverse:Corriente inversa máxima del módulo (A, opcional)",
            "fuse:Fusible serie máximo del módulo (A, opcional)=25",
            "ac:Potencia nominal del inversor (kW)=5",
            "pmax:Potencia FV máxima admitida (kWp)=7,5",
            "phase:Salida monofásica 230 V|Salida trifásica 400 V",
            "iac:Corriente máxima CA de ficha (A, opcional)=22",
            "max:Tensión CC máxima efectiva (V)=600",
            "start:Tensión de arranque del inversor (V)=120",
            "low:MPPT mínimo (V)=100",
            "high:MPPT máximo (V)=550",
            "imax:Corriente operativa máxima por MPPT (A)=16",
            "scmax:Isc máxima por MPPT (A)=20",
            "inputs:Entradas físicas por MPPT=1",
            "trackers:MPPT idénticos disponibles=2",
            "maxac:Calibre máximo CA de ficha (A, opcional)=32",
            "ldc:Longitud de ida máxima por string (m)=25",
            "lac:Longitud CA de ida hasta la interconexión (m)=15",
            "dropdc:Caída asignada al tramo CC (%)=1",
            "dropac:Caída asignada al tramo CA (%)=1,5",
            "tdc:Sección mínima térmica CC verificada (mm², opcional)",
            "tac:Sección mínima térmica CA verificada (mm², opcional)",
            "izdc:Iz CC para la sección propuesta (A, opcional)",
            "izac:Iz CA para la sección propuesta (A, opcional)",
            "feed:Aporte inverso adicional CC (A, opcional)",
            "mindc:Calibre mínimo de diseño gPV (A, opcional)",
            "integrated:Seccionamiento CC por verificar|Seccionador CC integrado y verificado|Seccionador CC externo requerido",
            "rcd:Diferencial según fabricante sin verificar|Fabricante admite tipo A|Fabricante exige otra solución",
            "spdcc:Sobretensiones CC sin evaluar|CC requiere tipo 2|CC requiere tipo 1 o 1+2|CC sin protector adicional según estudio",
            "spdca:Sobretensiones CA sin evaluar|CA requiere tipo 2|CA requiere tipo 1 o 1+2|CA sin protector adicional según estudio",
            "fault:Cortocircuito previsto máximo en CA (kA, opcional)=4",
            "storage:Sin batería|Con batería opcional",
            "daily:Consumo diario de respaldo en CA (kWh)=4,5",
            "days:Autonomía de respaldo (días)=1",
            "eta:Rendimiento de descarga a CA (0–1)=0,92",
            "dod:Fracción utilizable (0–1)=0,8",
            "bank:Tensión nominal del banco (V)=51,2",
            "v:Tensión nominal de una batería (V)=51,2",
            "ah:Capacidad de una batería (Ah)=100",
            "ms:Máximo en serie autorizado=1",
            "mp:Máximo de ramas en paralelo autorizado=4",
            "min:Tensión mínima de trabajo del banco (V)=44,8",
            "peak:Potencia punta de respaldo (kW)=3",
            "ids:Descarga máxima por batería (A)=80",
            "ics:Carga máxima por batería (A)=50",
            "bmsd:Límite total de descarga (A, opcional)=100",
            "bmsc:Límite total de carga (A, opcional)=80",
            "charge:Potencia disponible para recarga (kW)=2",
            "ec:Rendimiento de carga (0–1)=0,95"),
    SOLAR("Fotovoltaica · strings", "Comprueba un grupo de MPPT idénticos y con el mismo reparto. Los valores térmicos son de célula.",
            "p:Potencia STC del módulo (Wp)", "voc:Voc STC (V)", "vmp:Vmp STC (V)",
            "isc:Isc STC (A)", "imp:Imp STC (A)", "cvoc:Coeficiente de Voc (%/°C)",
            "cvmp:Coeficiente de Vmp (%/°C)", "cisc:Coeficiente de Isc (%/°C, opcional)",
            "cold:Temperatura de célula mínima (°C)", "hot:Temperatura de célula máxima (°C)",
            "irr:Irradiancia máxima de diseño (W/m²)=1000",
            "max:Tensión CC máxima efectiva de equipos y módulos (V)",
            "start:Tensión de arranque del inversor (V)", "low:MPPT mínimo (V)", "high:MPPT máximo (V)",
            "imax:Intensidad operativa máxima por MPPT (A)", "scmax:Isc máxima por MPPT (A)",
            "inputs:Entradas físicas por MPPT", "mppt:MPPT utilizados", "trackers:MPPT disponibles (opcional)",
            "target:Potencia FV objetivo (kWp, opcional)",
            "ac:Potencia nominal CA (kW)", "pmax:Potencia FV máxima admitida (kWp, opcional)",
            "ns:Módulos en serie", "np:Strings por MPPT", "fuse:Fusible serie máximo del módulo (A, opcional)"),
    PV_PROTECTION("Fotovoltaica · protecciones", "Usa los valores de diseño del string y la ficha del inversor. Las cifras iniciales son ilustrativas.",
            "voc:Voc máxima del string corregida (V)=358",
            "isc:Isc máxima de un string (A)=14,8",
            "parallel:Strings eléctricamente en paralelo=2",
            "feed:Aporte inverso adicional (A, opcional)=0",
            "reverse:Corriente inversa admisible del módulo (A, opcional)=12",
            "fuse:Fusible serie máximo declarado (A, opcional)=25",
            "fuseselect:Fusible gPV instalado o previsto (A, opcional)=25",
            "mindc:Calibre mínimo de diseño por string (A, opcional)=22,2",
            "izdc:Iz corregida del cable CC por string (A, opcional)=28",
            "maxdc:Máximo del automático CC si el fabricante lo admite (A, opcional)",
            "phase:Salida monofásica 230 V|Salida trifásica 400 V",
            "ac:Potencia nominal CA (kW)=5",
            "iac:Corriente máxima CA de ficha (A, opcional)=22",
            "izac:Iz corregida del cable CA (A, opcional)=34",
            "maxac:Calibre máximo permitido por el fabricante (A, opcional)=32",
            "fault:Cortocircuito máximo previsto en origen CA (kA, opcional)=4",
            "rcd:Diferencial según fabricante sin verificar|Fabricante admite tipo A|Fabricante exige otra solución",
            "site:En zona residencial o análoga|En otro emplazamiento",
            "spd:Riesgo de sobretensiones sin evaluar|El estudio exige tipo 2|El estudio exige tipo 1 o combinado 1+2|El estudio no exige protector adicional"),
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

    String[] requiredFields(Map<String, String> values) {
        Input x = new Input(values);
        String keys = switch (this) {
            case RESISTANCE -> switch (x.option("mode")) {
                case 1, 2 -> "list";
                case 3 -> "l s t";
                default -> {
                    int present = (x.has("v") ? 1 : 0) + (x.has("i") ? 1 : 0) + (x.has("r") ? 1 : 0);
                    yield present < 2 ? "v i r" : "";
                }
            };
            case REACTIVE -> "p a b v f";
            case CONDUIT -> x.option("rule") == 1 ? "s n" : "list";
            case ENERGY -> "p h d price fixed tax";
            case PV_PROJECT -> PvProject.required(x);
            case SOLAR -> "p voc vmp isc imp cvoc cvmp cold hot irr max start low high imax scmax inputs mppt ac ns np";
            case PV_PROTECTION -> "voc isc parallel" + (x.has("iac") ? "" : " ac");
            case DC -> "v i ib l max" + (x.has("iz") ? " s" : "");
            case BATTERY -> "daily days eta dod bank v ah ms mp min peak ids ics charge ec";
            case EV -> "p pf l drop" + (x.has("iz") ? " s" : "") + (x.has("kwh") ? " from to eta" : "");
        };
        return keys.isEmpty() ? new String[0] : java.util.Arrays.stream(keys.split(" "))
                .filter(key -> !x.has(key)).toArray(String[]::new);
    }

    String calculate(Map<String, String> values) {
        Input x = new Input(values);
        return switch (this) {
            case RESISTANCE -> resistance(x);
            case REACTIVE -> reactive(x);
            case CONDUIT -> ConduitSizing.calculate(x);
            case ENERGY -> energy(x);
            case PV_PROJECT -> PvProject.calculate(x).text();
            case SOLAR -> SolarSizing.strings(x);
            case PV_PROTECTION -> PvProtection.calculate(x);
            case DC -> dc(x);
            case BATTERY -> SolarSizing.battery(x);
            case EV -> EvSizing.calculate(x);
        };
    }

    private static String resistance(Input x) {
        int mode = x.option("mode");
        if (mode == 1 || mode == 2) {
            return "Resistencia equivalente: " + show(equivalent(x.list("list"), mode == 2))
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
            throw FieldIssues.invalid("v i r", "Introduce exactamente dos valores: tensión, intensidad o resistencia.");
        double v = x.optionalPositive("v"), i = x.optionalPositive("i"), r = x.optionalPositive("r");
        if (!x.has("v")) v = i * r;
        if (!x.has("i")) i = v / r;
        if (!x.has("r")) r = v / i;
        return "# Ley de Ohm\nTensión: " + show(v) + " V\nIntensidad: " + show(i) + " A\nResistencia: " + show(r) + " Ω\nPotencia disipada: "
                + show(v * i) + " W\nV = I · R; P = V · I";
    }

    private static String reactive(Input x) {
        double initial = x.number("a"), target = x.number("b");
        if (initial <= 0 || initial > 1) throw FieldIssues.invalid("a", "El cos φ inicial debe estar entre 0 y 1.");
        if (target <= 0 || target > 1) throw FieldIssues.invalid("b", "El cos φ objetivo debe estar entre 0 y 1.");
        if (target < initial) throw FieldIssues.invalid("a b", "El cos φ objetivo debe ser igual o superior al inicial.");
        double kvar = compensation(x.positive("p"), initial, target);
        double c = capacitorMicrofarads(kvar, x.positive("v"), x.positive("f"), x.option("mode"));
        return "Potencia de compensación: " + show(kvar) + " kvar\nCapacidad por rama: " + show(c)
                + " µF\nQc = P · (tan φ inicial − tan φ objetivo)\n"
                + "Pendiente para seleccionar la batería: perfil de carga, armónicos y características del equipo.";
    }

    private static String energy(Input x) {
        double h = x.nonnegative("h");
        if (h > 24) throw FieldIssues.invalid("h", "Las horas diarias no pueden superar 24.");
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
        if (design < current) throw FieldIssues.invalid("i ib", "La intensidad de diseño debe ser al menos la de servicio.");
        double length = x.nonnegative("l"), drop = x.positive("max"), rho = x.option("metal") == 0 ? 0.0225 : 0.036;
        double minimum = minimumSection(voltage, length, current, drop, rho, 2, 0);
        return CableSizing.result(x, voltage, current, design, length, rho, 2, drop, minimum, 0)
                + "\n~ ΔV = 2 · ρ · L · I / S. Caída a corriente de servicio.\n"
                + "Pendiente: verificar tensión asignada, cortocircuito y protecciones CC del tramo. "
                + "Si aplica ITC-BT-40, apartado 5, verifica el 125 % de intensidad y el 1,5 % hasta el punto de interconexión.";
    }

    static final class Input {
        private final Map<String, String> data;
        Input(Map<String, String> data) { this.data = data; }
        Map<String, String> values() { return data; }
        String text(String key) { return data.getOrDefault(key, "").trim(); }
        boolean has(String key) { return !text(key).isEmpty(); }
        double number(String key) {
            if (!has(key)) throw FieldIssues.invalid(key, "Completa los campos necesarios para este cálculo.");
            try {
                return decimal(text(key));
            } catch (IllegalArgumentException e) {
                throw FieldIssues.invalid(key, "Revisa el formato numérico de los campos señalados.");
            }
        }
        double positive(String key) {
            double value = number(key);
            if (value <= 0) throw FieldIssues.invalid(key, "El valor señalado debe ser mayor que cero.");
            return value;
        }
        double nonnegative(String key) {
            double value = number(key);
            if (value < 0) throw FieldIssues.invalid(key, "El valor señalado no puede ser negativo.");
            return value;
        }
        double optionalPositive(String key) { return has(key) ? positive(key) : 0; }
        int integer(String key) {
            double value = number(key);
            try {
                return count(value, "El número de unidades");
            } catch (IllegalArgumentException e) {
                throw FieldIssues.invalid(key, e.getMessage());
            }
        }

        double fraction(String key, String label) {
            double value = number(key);
            try {
                return ProCalculations.fraction(value, label);
            } catch (IllegalArgumentException e) {
                throw FieldIssues.invalid(key, e.getMessage());
            }
        }

        double[] list(String key) {
            try {
                return ProCalculations.list(text(key));
            } catch (IllegalArgumentException e) {
                throw FieldIssues.invalid(key, "Escribe valores positivos separados por punto y coma.");
            }
        }
        int option(String key) { return Integer.parseInt(text(key)); }
    }
}
