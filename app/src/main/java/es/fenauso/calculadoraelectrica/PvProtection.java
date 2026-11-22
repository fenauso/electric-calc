package es.fenauso.calculadoraelectrica;

import es.fenauso.calculadoraelectrica.ProModule.Input;

import static es.fenauso.calculadoraelectrica.ProCalculations.*;

final class PvProtection {
    private static final int[] GPV = {1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 16, 20, 25, 30, 32, 40, 50, 63};

    private PvProtection() {}

    static int fuseCandidate(double minimum, double maximum) {
        for (int caliber : GPV) {
            if (caliber >= minimum && caliber <= maximum) return caliber;
        }
        return 0;
    }

    static String calculate(Input x) {
        double voc = x.positive("voc");
        double isc = x.positive("isc");
        int parallel = x.integer("parallel");
        double reverse = isc * (parallel - 1) + (x.has("feed") ? x.nonnegative("feed") : 0);
        double permitted = x.optionalPositive("reverse"), declared = x.optionalPositive("fuse");
        double minimum = x.optionalPositive("mindc"), iz = x.optionalPositive("izdc");
        double proposed = x.optionalPositive("fuseselect");

        StringBuilder result = new StringBuilder("# Entrada CC · Protección de strings")
                .append("\nTensión máxima de cálculo: ").append(show(voc)).append(" V CC")
                .append("\nCorriente inversa estimada por string: ").append(show(reverse)).append(" A")
                .append("\nCorriente de cortocircuito de las ramas en paralelo: ").append(show(isc * parallel)).append(" A");
        if (permitted > 0) result.append("\nCorriente inversa admisible declarada: ").append(show(permitted)).append(" A");
        if (declared > 0) result.append("\nFusible máximo declarado: ").append(show(declared)).append(" A");
        if (permitted == 0 || !x.has("feed")) {
            result.append("\n~ Pendiente determinar la necesidad de protección de string. Verifica el límite inverso y los aportes de otras fuentes.");
        } else if (reverse > permitted) {
            result.append("\nProtección de string: necesaria por el criterio de corriente inversa.");
        } else {
            result.append("\nProtección de string: la corriente inversa calculada no supera el límite declarado.")
                    .append("\n~ Comprueba requisitos adicionales del fabricante, del cable y del esquema.");
        }
        if (minimum > 0 && iz > 0 && declared > 0) {
            double upper = Math.min(iz, declared);
            int candidate = fuseCandidate(minimum, upper);
            result.append("\nCalibre mínimo de diseño: ").append(show(minimum)).append(" A")
                    .append("\nLímite superior por cable y módulo: ").append(show(upper)).append(" A");
            result.append(candidate == 0 ? FieldIssues.warning("mindc fuse izdc", "Fusible gPV: sin calibre en el catálogo para ese intervalo.")
                    : "\nFusible gPV candidato si procede: " + candidate + " A");
            if (proposed > 0) {
                result.append("\nFusible previsto: ").append(show(proposed)).append(" A")

                        .append(proposed >= minimum && proposed <= upper ? "\nComparación del calibre previsto: dentro del intervalo calculado." : FieldIssues.warning("fuseselect", "Comparación del calibre previsto: fuera del intervalo calculado."));
            }
        } else {
            result.append("\n~ Para comparar calibres, indica el mínimo de diseño, el fusible máximo de ficha y la Iz del string.");
        }
        if (x.has("maxdc") && minimum > 0 && iz > 0) {
            double maximumDc = Math.min(iz, x.positive("maxdc"));
            if (declared > 0) maximumDc = Math.min(maximumDc, declared);
            double automatic = ElectricalCalculator.recommendBreaker(minimum, maximumDc);
            result.append("\nMagnetotérmico específico FV por intensidad: ")
                    .append(automatic > 0 ? show(automatic) + " A como candidato." : "sin calibre en el catálogo.");
            result.append("\n~ Solo como alternativa si el fabricante la admite y la curva protege frente a la corriente inversa. ")
                    .append("Comprueba tensión CC, polos, sentido de corriente y poder de corte.");
        } else {
            result.append("\n~ Para evaluar un magnetotérmico FV, indica el máximo autorizado, el mínimo de diseño y la Iz del string.");
        }
        result.append("\n~ Verifica tensión CC asignada, poder de corte, curvas y condiciones del gPV o del automático específico FV.")
                .append("\nSeccionamiento CC: verificar capacidad de corte DC-PV, polos y límites de tensión y corriente de cada tramo.")
                .append("\nProtección contra contactos en CC: comprobar aislamiento, puesta a tierra y vigilancia de aislamiento según esquema.");

        boolean three = x.option("phase") == 1;
        double voltage = three ? 400 : 230;
        double iac = x.has("iac") ? x.positive("iac") : x.positive("ac") * 1000 / (voltage * (three ? Math.sqrt(3) : 1));
        double izac = x.optionalPositive("izac"), maximum = x.optionalPositive("maxac");
        double breaker = -1;
        if (izac > 0) breaker = ElectricalCalculator.recommendBreaker(iac, maximum > 0 ? Math.min(maximum, izac) : izac);
        result.append("\n# Salida CA · Protección del inversor")
                .append("\nIntensidad de salida adoptada: ").append(show(iac)).append(" A")
                .append(x.has("iac") ? "\n~ Se usa la intensidad máxima de ficha." : "\n~ Intensidad estimada por potencia a factor de potencia 1.")
                .append("\nIz mínima del cable de conexión si aplica ITC-BT-40, apartado 5: ").append(show(iac * 1.25)).append(" A")
                .append("\nMagnetotérmico CA: ");
        if (izac == 0) result.append("pendiente de Iz corregida.");
        else if (breaker < 0) result.append("sin calibre compatible.").append(FieldIssues.warning("izac maxac", "Magnetotérmico CA: revisa los límites de calibre."));
        else result.append(show(breaker)).append(" A como candidato por intensidad.");
        if (izac > 0 && izac < iac * 1.25) result.append(FieldIssues.warning("izac", "Iz declarada: por debajo del criterio del 125 % cuando sea aplicable."));
        if (x.has("fault")) result.append("\nPoder de corte mínimo por cortocircuito previsto: ").append(show(x.positive("fault"))).append(" kA");
        else result.append("\n~ Falta la corriente de cortocircuito prevista para comprobar poder de corte.");
        result.append("\n~ Verifica curva, polos, tensión asignada y desconexión por defecto. ")
                .append("Confirma también la caída de tensión del tramo de interconexión.")
                .append("\nDiferencial en instalaciones próximas a consumo: tipo A según ITC-BT-40, apartado 4.3.");
        if (x.option("site") == 0) result.append("\nSensibilidad en zona residencial o análoga: 30 mA.");
        if (x.option("rcd") != 1) result.append("\n~ Confirma la compatibilidad con el fabricante. Si exige otra solución, resuelve la coordinación antes de seleccionar.");
        result.append("\n# Sobretensiones · CC y CA");
        switch (x.option("spd")) {
            case 1 -> result.append("\nTipo de protector según estudio declarado: tipo 2.");
            case 2 -> result.append("\nTipo de protector según estudio declarado: tipo 1 o combinado 1+2.");
            case 3 -> result.append("\n~ El estudio declarado no exige un protector adicional. Verifica el alcance en cada lado.");
            default -> result.append("\n~ Pendiente el tipo de protector. Revisa riesgo de rayo, pararrayos y protecciones existentes por cada lado.");
        }
        return result.append("\nProtector CC si procede: específico FV, con Ucpv al menos igual a ")
                .append(show(voc)).append(" V CC según la tensión máxima calculada.")
                .append("\nProtector CA si procede: selección de Uc y conexión según tensiones y esquema de tierra.")
                .append("\n~ Verifica Up, corriente asignada, protección de respaldo y coordinación entre dispositivos.").toString();
    }
}
