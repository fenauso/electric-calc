package es.fenauso.calculadoraelectrica;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import es.fenauso.calculadoraelectrica.ProModule.Input;
import static es.fenauso.calculadoraelectrica.ProCalculations.*;

final class PvProject {
    static final String PANEL = "p voc vmp isc imp cvoc cvmp cisc reverse fuse";
    static final String INVERTER = "ac pmax phase iac max start low high imax scmax inputs trackers maxac integrated rcd feed";
    static final String BATTERY_REQUIRED = "daily days eta dod bank v ah ms mp min peak ids ics charge ec";
    static final String BATTERY_FIELDS = BATTERY_REQUIRED + " bmsd bmsc";
    static final String BATTERY_PROFILE = "v ah ms mp ids ics bmsd bmsc";

    private PvProject() {}

    record Plan(String text, Map<ProModule, Map<String, String>> details) {}

    static String required(Input x) {
        String goal = x.option("goal") == 0 ? "target" : "dailygen sun pr";
        if (x.option("design") == 1) return goal + " p ratio";
        return goal + " cold hot irr p voc vmp isc imp cvoc cvmp ac pmax max start low high"
                + " imax scmax inputs trackers ldc lac dropdc dropac"
                + (x.option("storage") == 1 ? " " + BATTERY_REQUIRED : "");
    }

    static Plan calculate(Input x) {
        boolean energyMode = x.option("goal") == 1;
        double sun = energyMode ? x.positive("sun") : 0;
        double pr = energyMode ? x.fraction("pr", "Rendimiento global") : 0;
        double target = energyMode ? x.positive("dailygen") / (sun * pr) : x.positive("target");
        double panel = x.positive("p");
        if (x.option("design") == 1) {
            double count = Math.ceil(1000 * target / panel - 1e-10);
            double proposed = count * panel / 1000;
            double ratio = x.positive("ratio");
            return new Plan("# Estimación inicial FV"
                    + "\nPotencia pico objetivo: " + show(target) + " kWp"
                    + "\nMódulos estimados: " + (long) count + " unidades"
                    + "\nPotencia pico instalada: " + show(proposed) + " kWp"
                    + "\nPotencia nominal de inversor de referencia: " + show(proposed / ratio) + " kW"
                    + "\nRelación CC/CA supuesta: " + show(ratio)
                    + "\n~ Elige un modelo que admita la potencia FV, y completa sus fichas para distribuir los módulos y dimensionar los circuitos.",
                    new EnumMap<>(ProModule.class));
        }
        double rated = x.positive("ac"), maximum = x.positive("pmax");
        int trackers = x.integer("trackers");
        if (trackers > 32 || x.integer("inputs") > 32)
            throw FieldIssues.invalid(trackers > 32 ? "trackers" : "inputs", "Introduce hasta 32 MPPT y 32 entradas por MPPT.");
        SolarSizing.Limits limits = SolarSizing.limits(x);
        SolarSizing.Layout preferred = null;
        for (SolarSizing.Layout layout : SolarSizing.layouts(panel, target, maximum, limits.minimumModules(),
                limits.maxSeries(), limits.maxParallel(), trackers)) {
            if (layout == null) continue;
            if (preferred == null || layout.panels() < preferred.panels()
                    || (layout.panels() == preferred.panels() && layout.parallel() < preferred.parallel())) preferred = layout;
        }

        StringBuilder text = new StringBuilder("# Propuesta de instalación FV")
                .append("\nPotencia pico objetivo: ").append(show(target)).append(" kWp");
        Map<ProModule, Map<String, String>> details = new EnumMap<>(ProModule.class);
        if (preferred == null) {
            String keys = limits.minimumModules() > limits.maxSeries() ? "low high start max"
                    : limits.maxParallel() < 1 ? "imax scmax inputs"
                    : (energyMode ? "dailygen" : "target") + " pmax";
            text.append(FieldIssues.warning(keys, "No hay un reparto compatible que alcance el objetivo con este inversor."))
                    .append("\nIntervalo de módulos por string: ").append(limits.minimumModules()).append(" a ").append(limits.maxSeries())
                    .append("\nMáximo de strings por MPPT: ").append(limits.maxParallel())
                    .append("\n~ Revisa el objetivo, la ficha del inversor o el modelo de panel.");
            return new Plan(text.toString(), details);
        }

        int strings = preferred.parallel() * preferred.trackers();
        double kwp = preferred.panels() * panel / 1000;
        double voc = limits.vocWorst() * preferred.series(), vmp = limits.vmpLowest() * preferred.series();
        double dcLength = x.nonnegative("ldc"), acLength = x.nonnegative("lac");
        double dcDrop = x.positive("dropdc"), acDrop = x.positive("dropac");
        boolean three = x.option("phase") == 1;
        double acVoltage = three ? 400 : 230;
        double acFactor = three ? Math.sqrt(3) : 2;
        double current = x.has("iac") ? x.positive("iac") : rated * 1000 / (acVoltage * (three ? Math.sqrt(3) : 1));
        double sectionDc = minimumSection(vmp, dcLength, limits.operating(), dcDrop, 0.0225, 2, x.optionalPositive("tdc"));
        double sectionAc = minimumSection(acVoltage, acLength, current, acDrop, 0.0225, acFactor, x.optionalPositive("tac"));
        double izDc = x.optionalPositive("izdc"), izAc = x.optionalPositive("izac");
        double acDesign = 1.25 * current;

        text.append("\nMódulos FV: ").append(preferred.panels()).append(" unidades de ").append(show(panel)).append(" Wp")
                .append("\nInversor: 1 unidad de ").append(show(rated)).append(" kW CA")
                .append("\nPotencia pico instalada: ").append(show(kwp)).append(" kWp")
                .append("\nRelación CC/CA: ").append(show(kwp / rated))
                .append("\nStrings: ").append(strings)
                .append("\nMódulos en serie por string: ").append(preferred.series())
                .append("\nStrings en paralelo por MPPT: ").append(preferred.parallel())
                .append("\nMPPT utilizados: ").append(preferred.trackers())
                .append("\nVoc máxima por string: ").append(show(voc)).append(" V CC")
                .append("\nVmp mínima por string: ").append(show(vmp)).append(" V CC");
        if (energyMode) text.append("\nProducción diaria estimada de diseño: ").append(show(kwp * sun * pr)).append(" kWh")
                .append("\n~ La estimación de producción no determina la energía autoconsumida.");
        text.append("\n# Esquema")
                .append("\nRecorrido: módulos en serie → strings → MPPT → inversor → cuadro CA → interconexión")
                .append("\n~ Se supone el mismo reparto en MPPT idénticos. Confirma el límite por conector, la compatibilidad de los equipos y la disposición real.");
        if (!x.has("cisc")) text.append("\n~ Falta el coeficiente térmico de Isc. El cálculo de corriente usa la corrección por irradiancia.");

        text.append("\n# Cableado estimado de cobre")
                .append("\nCable CC de string: ").append(2 * strings).append(" recorridos unipolares de hasta ")
                .append(show(dcLength)).append(" m cada uno")
                .append("\nLongitud total estimada CC: ").append(show(2 * strings * dcLength)).append(" m sin reservas")
                .append("\nCable CA: 1 recorrido de ").append(show(acLength)).append(" m sin reservas")
                .append("\n~ Confirma conductores activos, neutro, PE y canalización según esquema y equipos.");
        appendCable(text, "CC", "ldc dropdc tdc", sectionDc, x.has("tdc"), limits.operating(), dcLength, vmp, 2);
        appendCable(text, "CA", "lac dropac tac", sectionAc, x.has("tac"), current, acLength, acVoltage, acFactor);
        text.append("\nIz de diseño CC por string: al menos ").append(show(limits.shortCircuit())).append(" A antes de otros márgenes aplicables")
                .append("\nIz de diseño CA: ").append(show(acDesign)).append(" A aplicando el 125 % a la salida de conexión");
        if (izDc > 0 && izDc < limits.shortCircuit()) text.append(FieldIssues.warning("izdc", "Iz CC declarada: insuficiente para la Isc calculada."));
        if (izAc > 0 && izAc < acDesign) text.append(FieldIssues.warning("izac", "Iz CA declarada: no alcanza el criterio de dimensionamiento adoptado."));

        appendProtection(text, x, preferred, limits.shortCircuit(), voc, current, izDc, izAc, acDesign);
        Map<String, String> solar = copy(x, PANEL + " " + INVERTER + " cold hot irr");
        put(solar, "target", target, "ns", preferred.series(), "np", preferred.parallel(), "mppt", preferred.trackers());
        details.put(ProModule.SOLAR, solar);

        Map<String, String> protection = copy(x, "reverse fuse feed mindc phase ac maxac fault rcd izdc izac");
        put(protection, "voc", voc, "isc", limits.shortCircuit(), "parallel", preferred.parallel(),
                "iac", current, "site", 0, "spd", x.option("spdcc") == x.option("spdca") ? x.option("spdcc") : 0);
        details.put(ProModule.PV_PROTECTION, protection);
        Map<String, String> cable = new HashMap<>();
        put(cable, "v", vmp, "i", limits.operating(), "ib", Math.max(limits.shortCircuit(), limits.operating()),
                "l", dcLength, "max", dcDrop, "metal", 0);
        if (sectionDc > 0) put(cable, "s", sectionDc);
        if (sectionDc > 0 && izDc > 0) put(cable, "iz", izDc);
        details.put(ProModule.DC, cable);

        if (x.option("storage") == 1) {
            text.append("\n# Almacenamiento opcional").append("\nRecorrido de batería: banco → entrada compatible del inversor o cargador.")
                    .append("\n~ Requiere un inversor compatible con batería y respaldo.");
            text.append("\n").append(SolarSizing.battery(x));
            text.append("\n~ Cableado, seccionamiento y protecciones del banco: verifica la solución del fabricante.");
            details.put(ProModule.BATTERY, copy(x, BATTERY_FIELDS));
        }
        return new Plan(text.toString(), details);
    }

    private static void appendCable(StringBuilder text, String name, String keys, double section, boolean thermal,
                                    double amps, double length, double volts, double factor) {
        if (section <= 0) {
            text.append(FieldIssues.warning(keys, "Sección " + name + ": ninguna del catálogo alcanza los límites."));
            return;
        }
        double drop = 100 * factor * 0.0225 * length * amps / (section * volts);
        text.append("\nSección propuesta ").append(name).append(": ").append(show(section)).append(" mm²")
                .append("\nCaída estimada ").append(name).append(": ").append(show(drop)).append(" %");
        if (!thermal) text.append("\n~ Sección ").append(name).append(" por caída. Falta verificar intensidad admisible.");
    }

    private static void appendProtection(StringBuilder text, Input x, SolarSizing.Layout layout,
                                         double isc, double voc, double current, double izDc, double izAc, double design) {
        text.append("\n# Lista de protecciones");
        if (x.option("integrated") == 1) text.append("\nSeccionamiento CC: integrado según la ficha indicada.");
        else text.append("\nSeccionamiento CC: ").append(x.option("integrated") == 2
                ? "prever seccionador externo. Verifica cantidad de polos, entradas y corte en carga."
                : "comprueba el dispositivo integrado antes de comprar uno externo.");
        text.append("\nTensión de referencia CC para los equipos: ").append(show(voc)).append(" V");
        double reverseLimit = x.optionalPositive("reverse");
        double possibleReverse = (layout.parallel() - 1) * isc + (x.has("feed") ? x.nonnegative("feed") : 0);
        if (reverseLimit == 0 || !x.has("feed")) {
            text.append("\nFusibles de string: pendientes del límite inverso del módulo y de los aportes de fallo.");
        } else if (possibleReverse > reverseLimit) {
            double maxFuse = x.optionalPositive("fuse"), minFuse = x.optionalPositive("mindc");
            int candidate = minFuse > 0 && izDc > 0 && maxFuse > 0
                    ? PvProtection.fuseCandidate(minFuse, Math.min(izDc, maxFuse)) : 0;
            text.append("\nRamas a proteger por corriente inversa: ").append(layout.parallel() * layout.trackers())
                    .append("\nFusible gPV por polo protegido: ").append(candidate > 0 ? candidate + " A como candidato."
                            : "pendiente de calibre. Verifica mínimo de diseño, máximo de ficha e Iz.");
        } else {
            text.append("\nFusibles de string: la corriente inversa calculada no supera el límite declarado. Revisa requisitos del fabricante.");
        }
        double maxAc = x.optionalPositive("maxac");
        double upper = Math.min(izAc > 0 ? izAc : 125, maxAc > 0 ? maxAc : 125);
        double breaker = ElectricalCalculator.recommendBreaker(current, upper);
        if (breaker > 0) {
            text.append("\nMagnetotérmico CA: 1 candidato de ").append(show(breaker)).append(" A");
            if (izAc < design || maxAc == 0) text.append("\n~ Pendiente confirmar Iz de diseño o calibre máximo de ficha.");
        } else text.append(FieldIssues.warning("iac izac maxac", "Magnetotérmico CA: sin calibre compatible en el catálogo."));
        if (x.has("fault")) text.append("\nPoder de corte mínimo por cortocircuito previsto: ").append(show(x.positive("fault"))).append(" kA");
        else text.append("\n~ Falta la corriente de cortocircuito para seleccionar poder de corte.");
        text.append("\nDiferencial CA residencial: prever 1 tipo A de 30 mA conforme a ITC-BT-40.");
        if (x.option("rcd") != 1) text.append("\n~ Confirma la compatibilidad del diferencial con el inversor antes de seleccionarlo.");
        appendSpd(text, "CC", x.option("spdcc"), voc);
        appendSpd(text, "CA", x.option("spdca"), 0);
        text.append("\n~ Cantidades preliminares. Comprueba polos, puesta a tierra, protecciones existentes y coordinación de dispositivos.");
    }

    private static void appendSpd(StringBuilder text, String side, int selection, double voc) {
        text.append("\nSobretensiones ").append(side).append(": ");
        switch (selection) {
            case 1, 2 -> {
                text.append("prever equipo ").append(selection == 1 ? "tipo 2" : "tipo 1 o combinado 1+2");
                if (voc > 0) text.append(" específico FV con Ucpv de al menos ").append(show(voc)).append(" V");
                text.append(". Verifica cantidad, ubicación y coordinación.");
            }
            case 3 -> text.append("sin protector adicional según el estudio declarado.");
            default -> text.append("pendiente de evaluación del emplazamiento.");
        }
    }

    static Map<String, String> copy(Input x, String keys) {
        Map<String, String> values = new HashMap<>();
        for (String key : keys.split(" ")) {
            if (x.has(key)) values.put(key, x.text(key));
        }
        return values;
    }

    private static void put(Map<String, String> values, Object... pairs) {
        for (int i = 0; i < pairs.length; i += 2) values.put((String) pairs[i], pairs[i + 1].toString());
    }
}
