package es.fenauso.calculadoraelectrica;

import es.fenauso.calculadoraelectrica.ProModule.Input;

import static es.fenauso.calculadoraelectrica.ProCalculations.*;

final class CableSizing {
    private CableSizing() {}

    static String result(Input x, double voltage, double current, double requiredIz, double length,
                         double rho, double factor, double limit, double minimumSection, double regulatoryMinimum) {
        double section = x.optionalPositive("s");
        double iz = x.optionalPositive("iz");
        if (section == 0 && iz > 0) throw new IllegalArgumentException("Indica a qué sección corresponde la Iz introducida.");
        String result = minimumSection > 0
                ? "Sección comercial mínima por caída" + (regulatoryMinimum > 0 ? " y mínimo reglamentario" : "")
                        + ": " + show(minimumSection) + " mm²"
                : "Ninguna sección del catálogo cubre la caída o el mínimo aplicable.";
        result += "\nIz requerida: " + show(requiredIz) + " A";
        boolean specifiedSection = section > 0;
        if (!specifiedSection) section = minimumSection;
        if (section > 0) {
            double volts = factor * rho * length * current / section;
            double percent = 100 * volts / voltage;
            result += "\nCaída estimada con " + show(section) + " mm²: " + show(volts) + " V ("
                    + show(percent) + " %). " + (percent <= limit ? "Dentro del límite asignado." : "Supera el límite asignado.");
            if (specifiedSection && section < regulatoryMinimum) result += "\nLa sección indicada es inferior al mínimo reglamentario.";
            if (iz > 0) result += "\nIz declarada: " + show(iz) + " A. "
                    + (iz >= requiredIz ? "Alcanza la Iz requerida." : "Insuficiente para la Iz requerida.");
        }
        if (iz == 0 || !x.has("method")) {
            result += "\nPendiente: aportar método, condiciones e Iz corregida por aislamiento, agrupamiento, temperatura y conductores cargados.";
        } else {
            result += "\nCondiciones declaradas: " + x.text("method") + ". Verifica la fuente de Iz.";
        }
        return result;
    }
}
