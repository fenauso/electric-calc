package es.fenauso.calculadoraelectrica;

import java.util.Locale;

final class ProCalculations {
    private ProCalculations() {}

    static double positive(double x, String label) {
        if (!Double.isFinite(x) || x <= 0) throw new IllegalArgumentException(label + " debe ser mayor que cero.");
        return x;
    }

    static double fraction(double x, String label) {
        if (positive(x, label) > 1) throw new IllegalArgumentException(label + " debe estar entre 0 y 1.");
        return x;
    }

    static double nonnegative(double x, String label) {
        if (!Double.isFinite(x) || x < 0) throw new IllegalArgumentException(label + " no puede ser negativo.");
        return x;
    }

    static int count(double x, String label) {
        if (positive(x, label) > 100000 || x != Math.rint(x))
            throw new IllegalArgumentException(label + " debe ser un entero entre 1 y 100 000.");
        return (int) x;
    }

    static String show(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("El resultado está fuera del rango de cálculo.");
        return String.format(Locale.getDefault(), "%,.2f", value);
    }

    static double decimal(String value) {
        double number = Double.parseDouble(value.trim().replace(',', '.'));
        if (!Double.isFinite(number)) throw new IllegalArgumentException("Introduce valores numéricos finitos.");
        return number;
    }

    static String fromSmallerUnit(String input, boolean multiple) {
        if (!multiple) return Double.toString(decimal(input) / 1000);
        String[] parts = input.split(";", -1);
        for (int i = 0; i < parts.length; i++) parts[i] = Double.toString(decimal(parts[i]) / 1000);
        return String.join(";", parts);
    }

    static double[] list(String input) {
        String[] items = input.split(";", -1);
        double[] values = new double[items.length];
        for (int i = 0; i < items.length; i++) values[i] = positive(decimal(items[i]), "Cada valor de la lista");
        return values;
    }

    static double equivalent(double[] resistances, boolean parallel) {
        double sum = 0;
        for (double r : resistances) sum += parallel ? 1 / r : r;
        return parallel ? 1 / sum : sum;
    }

    static double compensation(double kw, double initial, double target) {
        positive(kw, "Potencia activa");
        fraction(initial, "Cos φ inicial");
        fraction(target, "Cos φ objetivo");
        if (target < initial) throw new IllegalArgumentException("El cos φ objetivo debe ser igual o superior al inicial.");
        return kw * (Math.tan(Math.acos(initial)) - Math.tan(Math.acos(target)));
    }

    static double capacitorMicrofarads(double kvar, double voltage, double hz, int connection) {
        positive(voltage, "Tensión");
        positive(hz, "Frecuencia");
        int branches = connection == 0 ? 1 : 3;
        double branchVoltage = connection == 1 ? voltage / Math.sqrt(3) : voltage;
        return kvar * 1e9 / (branches * 2 * Math.PI * hz * branchVoltage * branchVoltage);
    }

    static double voltageAt(double stc, double coefficient, double temperature) {
        return positive(stc * (1 + coefficient / 100 * (temperature - 25)), "Tensión corregida");
    }

    static double minimumSection(double voltage, double length, double current, double dropPct,
                                 double rho, double loopFactor, double minimum) {
        positive(voltage, "Tensión");
        nonnegative(length, "Longitud");
        positive(current, "Intensidad");
        positive(dropPct, "Caída admisible");
        double required = 100 * loopFactor * rho * length * current / (voltage * dropPct);
        for (double section : ElectricalCalculator.SECTIONS) {
            if (section >= minimum && section >= required) return section;
        }
        return -1;
    }

    static double nominalBatteryKwh(double dailyAc, double days, double efficiency, double usable) {
        return positive(dailyAc, "Consumo diario") * positive(days, "Autonomía")
                / fraction(efficiency, "Rendimiento de descarga") / fraction(usable, "Fracción utilizable");
    }
}
