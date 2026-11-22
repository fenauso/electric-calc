package es.fenauso.calculadoraelectrica;

import es.fenauso.calculadoraelectrica.ProModule.Input;

import static es.fenauso.calculadoraelectrica.ProCalculations.*;

final class SolarSizing {
    private SolarSizing() {}

    static String strings(Input x) {
        double cold = x.number("cold"), hot = x.number("hot");
        if (cold > hot || cold <= -273.15) throw new IllegalArgumentException("Revisa las temperaturas extremas de célula.");
        double voc = x.positive("voc"), vmp = x.positive("vmp");
        double isc = x.positive("isc"), imp = x.positive("imp");
        if (vmp >= voc || imp > isc) throw new IllegalArgumentException("Revisa Vmp, Voc, Imp e Isc de la ficha del módulo.");
        double vocCold = voltageAt(voc, x.number("cvoc"), cold);
        double vocHot = voltageAt(voc, x.number("cvoc"), hot);
        double vmpCold = voltageAt(vmp, x.number("cvmp"), cold);
        double vmpHot = voltageAt(vmp, x.number("cvmp"), hot);
        double maximum = x.positive("max");
        double low = x.positive("low"), high = x.positive("high"), start = x.positive("start");
        if (high < low || start > maximum) throw new IllegalArgumentException("Revisa la ventana MPPT y la tensión de arranque.");
        int minSeries = ceilCount(low / Math.min(vmpCold, vmpHot));
        int maxSeries = floorCount(Math.min(maximum / Math.max(vocCold, vocHot), high / Math.max(vmpCold, vmpHot)));
        int minStart = ceilCount(start / Math.min(vocCold, vocHot));
        int series = x.integer("ns"), parallel = x.integer("np"), trackers = x.integer("mppt");
        double irradiance = x.positive("irr") / 1000;
        double coefficient = x.has("cisc") ? x.number("cisc") : 0;
        double thermal = Math.max(1 + coefficient / 100 * (cold - 25), 1 + coefficient / 100 * (hot - 25));
        positive(thermal, "Factor térmico de Isc");
        double shortCircuit = isc * irradiance * thermal;
        double operating = imp * irradiance;
        int maxParallel = Math.min(x.integer("inputs"), floorCount(Math.min(
                x.positive("imax") / operating, x.positive("scmax") / shortCircuit)));
        double installed = x.positive("p") * series * parallel * trackers / 1000;
        double rated = x.positive("ac"), allowed = x.optionalPositive("pmax");
        double fuse = x.optionalPositive("fuse");
        double reverse = (parallel - 1) * shortCircuit;
        return (Math.max(minSeries, minStart) <= maxSeries
                ? "Intervalo compatible con las comprobaciones de tensión: "
                    + Math.max(minSeries, minStart) + "–" + maxSeries + " módulos por string"
                : "Sin configuración en serie que satisfaga las tensiones introducidas.")
                + "\nMínimo por MPPT: " + minSeries + " · mínimo estimado por arranque: " + minStart
                + "\nMáximo por tensión CC y MPPT: " + maxSeries
                + "\nMáximo orientativo de strings por MPPT por corriente y entradas: " + maxParallel
                + "\nConfiguración introducida: " + (series >= Math.max(minSeries, minStart) && series <= maxSeries
                    && parallel <= maxParallel && (allowed == 0 || installed <= allowed)
                    ? "dentro de los límites calculados." : "supera o incumple uno o más límites calculados.")
                + "\nVoc en frío del string: " + show(vocCold * series) + " V"
                + "\nVmp del string: " + show(vmpHot * series) + " V en calor · " + show(vmpCold * series) + " V en frío"
                + "\nImp / Isc estimadas por MPPT: " + show(operating * parallel) + " / " + show(shortCircuit * parallel) + " A"
                + "\nPotencia instalada: " + show(installed) + " kWp · Relación CC/CA: " + show(installed / rated)
                + (allowed > 0 ? "\nPotencia FV máxima declarada: " + show(allowed) + " kWp." : "")
                + "\nV(T) = Vₛₜ꜀ · [1 + β · (T − 25)/100]."
                + "\nAporte inverso estimado de las otras ramas del MPPT: " + show(reverse) + " A."
                + (fuse > 0 ? " Fusible serie máximo declarado: " + show(fuse) + " A." : "")
                + "\nPendiente: comprobar corriente inversa admisible del módulo, aportes del inversor, fusible gPV si procede, "
                + "seccionamiento, sobretensiones CC/CA, puesta a tierra y protecciones CA según esquema e ITC-BT-40."
                + (x.has("cisc") ? "" : "\nIsc estimada sin corrección térmica por falta de coeficiente.")
                + "\nConfirma límites por conector, MPPT y para el inversor completo con su fabricante.";
    }

    private static int ceilCount(double n) { return (int) Math.ceil(n - 1e-10); }
    private static int floorCount(double n) { return (int) Math.floor(n + 1e-10); }

    static String battery(Input x) {
        double daily = x.positive("daily"), days = x.positive("days"), efficiency = x.number("eta");
        double dod = fraction(x.number("dod"), "Fracción utilizable");
        double required = nominalBatteryKwh(daily, days, efficiency, dod);
        double bank = x.positive("bank"), moduleVoltage = x.positive("v"), moduleAh = x.positive("ah");
        double min = x.positive("min");
        if (min > bank) throw new IllegalArgumentException("La tensión mínima del banco no puede superar su tensión nominal.");
        int series = (int) Math.round(bank / moduleVoltage);
        double peakCurrent = x.positive("peak") * 1000 / (min * efficiency);
        double discharge = x.positive("ids"), charge = x.positive("ics");
        double bmsDischarge = x.optionalPositive("bmsd"), bmsCharge = x.optionalPositive("bmsc");
        double chargeEfficiency = fraction(x.number("ec"), "Rendimiento de carga");
        double sourcePower = x.positive("charge");
        double usefulRequired = required * dod;
        String capacity = "Requisito útil en CC: " + show(usefulRequired) + " kWh / " + show(usefulRequired * 1000 / bank)
                + " Ah\nRequisito nominal: " + show(required) + " kWh / " + show(required * 1000 / bank)
                + " Ah\nDemanda punta del banco: " + show(peakCurrent) + " A";
        if (series < 1 || series > x.integer("ms") || Math.abs(bank - series * moduleVoltage) > 0.01) {
            return capacity + "\nNo hay una combinación en serie para la tensión objetivo con los límites indicados.";
        }
        int parallel = ceilCount(Math.max(required * 1000 / (bank * moduleAh), peakCurrent / discharge));
        if (parallel > x.integer("mp")) return capacity + "\nLas ramas necesarias (" + parallel + ") superan el máximo permitido.";
        double totalAh = parallel * moduleAh, total = totalAh * bank / 1000;
        double dischargeLimit = bmsDischarge > 0 ? Math.min(discharge * parallel, bmsDischarge) : discharge * parallel;
        double chargeLimit = bmsCharge > 0 ? Math.min(charge * parallel, bmsCharge) : charge * parallel;
        double netCharge = Math.min(sourcePower * chargeEfficiency, chargeLimit * min / 1000);
        return capacity + "\nBanco por energía y demanda: " + series + " en serie × " + parallel + " ramas en paralelo"
                + "\nCapacidad del banco: " + show(total) + " kWh / " + show(totalAh) + " Ah nominales"
                + "\nCapacidad útil del banco: " + show(total * dod) + " kWh / " + show(totalAh * dod) + " Ah"
                + "\nLímite de descarga con los datos disponibles: " + show(dischargeLimit) + " A"
                + "\nLímite de carga con los datos disponibles: " + show(chargeLimit) + " A"
                + (peakCurrent > dischargeLimit ? "\nLa demanda supera el límite total de descarga declarado." : "")
                + "\nHoras equivalentes para reponer la energía de diseño: " + show(usefulRequired / netCharge)
                + " h a " + show(netCharge) + " kW netos estimados."
                + "\nE nominal = consumo diario × días / (rendimiento × fracción útil)."
                + "\nPendiente: confirmar compatibilidad de tensiones, BMS, conexiones, cableado y protecciones del fabricante."
                + (bmsDischarge == 0 || bmsCharge == 0 ? " Faltan límites totales del equipo." : "");
    }
}
