package es.fenauso.calculadoraelectrica;

import es.fenauso.calculadoraelectrica.ProModule.Input;

import static es.fenauso.calculadoraelectrica.ProCalculations.*;

final class EvSizing {
    private EvSizing() {}

    static String calculate(Input x) {
        boolean three = x.option("phase") == 1;
        if (three && x.option("supply") == 0) throw FieldIssues.invalid("phase supply", "Un cargador trifásico requiere suministro trifásico.");
        double voltage = three ? 400 : 230;
        double kw = x.positive("p"), pf = x.fraction("pf", "Factor de potencia");
        double configured = x.optionalPositive("i");
        double current = configured > 0 ? configured : kw * 1000 / (voltage * pf * (three ? Math.sqrt(3) : 1));
        double limit = x.positive("drop");
        if (limit > 5) throw FieldIssues.invalid("drop", "La ITC-BT-52 limita la caída del circuito de recarga al 5 %.");
        boolean aluminum = x.option("metal") == 1;
        if (aluminum && x.option("site") < 2)
            throw FieldIssues.invalid("metal", "Para vivienda o aparcamiento colectivo residencial selecciona cobre.");
        double length = x.nonnegative("l"), rho = aluminum ? 0.036 : 0.0225;
        double factor = three ? Math.sqrt(3) : 2;
        double regulatory = aluminum ? 4 : 2.5;
        double minimum = minimumSection(voltage, length, current, limit, rho, factor, regulatory);
        double breaker = -1;
        for (double rating : ElectricalCalculator.BREAKERS) {
            if (rating >= current) {
                breaker = rating;
                break;
            }
        }
        double iz = x.optionalPositive("iz");
        boolean rdcVerified = x.option("rdc") == 1 || x.option("rdc") == 2;
        String result = "# Línea de recarga\nIntensidad de diseño: " + show(current) + " A"
                + (configured > 0 ? "\n~ Se usa la corriente configurada." : three ? "\n~ I = P / (√3 · V · FP)." : "\n~ I = P / (V · FP).")
                + "\n" + CableSizing.result(x, voltage, current, breaker > 0 ? breaker : current,
                        length, rho, factor, limit, minimum, regulatory)
                + "\nCaída aproximada resistiva. Si la reactancia es relevante, verifica con los datos R/X del cable."
                + "\n\n# Protecciones (ITC-BT-52, apartados 6.1, 6.3 y 6.4)"
                + (breaker > 0 ? "\nMagnetotérmico candidato: curva C, corte omnipolar, " + show(breaker) + " A."
                    : FieldIssues.warning(configured > 0 ? "i" : "p", "Magnetotérmico: no hay calibre suficiente en el catálogo."))
                + (breaker > 0 && iz > 0 && iz < breaker ? FieldIssues.warning("iz", "Iz: insuficiente para ese calibre.") : "")
                + "\nPendiente: poder de corte, corriente mínima de defecto y coordinación con la línea."
                + "\nDiferencial individual ≤ 30 mA: "
                + (rdcVerified ? "tipo A con el RDC-DD declarado, o tipo B compatible con el equipo."
                    : "tipo B, o tipo A solo si se verifica un RDC-DD conforme a IEC 62955.")
                + "\nProtección contra sobretensiones temporales y transitorias en el circuito."
                + (x.option("spd") == 0 ? " Pendiente: verificar dispositivos aguas arriba y su coordinación."
                    : " Verifica coordinación y cobertura de los dispositivos declarados.")
                + (x.has("distance") && x.nonnegative("distance") >= 10
                    ? "\n~ A 10 m o más, la guía recomienda valorar protección transitoria tipo 2 cerca del punto." : "")
                + (x.option("out") == 1 ? "\nExterior: comprobar envolvente, canalización y cable de alimentación 0,6/1 kV." : "")
                + "\nEsquema: " + (x.has("scheme") ? x.text("scheme") : "pendiente de indicar según ITC-BT-52.");
        if (!x.has("kwh")) return result + "\n\nTiempo de carga pendiente: introduce la capacidad útil del vehículo.";
        double from = x.nonnegative("from"), to = x.nonnegative("to");
        if (to > 100 || from > to) throw FieldIssues.invalid(to > 100 ? "to" : "from to", "Revisa los porcentajes inicial y objetivo (0–100 %).");
        double energy = x.positive("kwh") * (to - from) / 100;
        double available = Math.min(kw, current * voltage * pf * (three ? Math.sqrt(3) : 1) / 1000);
        if (x.has("car")) available = Math.min(available, x.positive("car"));
        if (x.has("grid")) available = Math.min(available, x.positive("grid"));
        return result + "\n# Tiempo de recarga\nEnergía a reponer: " + show(energy) + " kWh"
                + "\nTiempo estimado: " + show(energy / (available * x.fraction("eta", "Rendimiento de carga"))) + " h"
                + "\n~ t = energía / (potencia disponible × rendimiento)."
                + (!x.has("car") || !x.has("grid") ? " Estimación parcial: falta límite del vehículo o potencia disponible." : "");
    }
}
