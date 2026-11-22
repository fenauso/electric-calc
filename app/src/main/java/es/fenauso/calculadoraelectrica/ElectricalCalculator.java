package es.fenauso.calculadoraelectrica;

import java.util.Locale;

public final class ElectricalCalculator {
    private ElectricalCalculator() {}

    public enum Phase { MONOPHASE, THREE_PHASE }
    public enum Material { COPPER, ALUMINIUM }
    public enum Insulation { PVC_70, XLPE_90 }

    public static final double[] BREAKERS = {2,4,6,10,16,20,25,32,40,50,63,80,100,125};
    public static final double[] SECTIONS = {1.5,2.5,4,6,10,16,25,35,50,70,95,120};

    // Valores orientativos para conductores unipolares/multipolares habituales en canalización.
    // Deben contrastarse con REBT/UNE y el método real de instalación.
    private static final double[] CU_PVC = {14.5,20,26,34,46,61,80,99,119,151,182,210};
    private static final double[] CU_XLPE = {18,25,34,44,60,80,105,130,157,200,242,278};
    private static final double[] AL_PVC = {11,15,20,26,36,47,62,77,93,118,142,164};
    private static final double[] AL_XLPE = {14,19,26,34,46,62,81,101,122,155,188,216};

    public static double currentFromPower(double powerW, double voltage, double pf, Phase phase) {
        requirePositive(powerW, voltage, pf);
        return phase == Phase.THREE_PHASE
                ? powerW / (Math.sqrt(3.0) * voltage * pf)
                : powerW / (voltage * pf);
    }

    public static double powerFromCurrent(double current, double voltage, double pf, Phase phase) {
        requirePositive(current, voltage, pf);
        return phase == Phase.THREE_PHASE
                ? Math.sqrt(3.0) * voltage * current * pf
                : voltage * current * pf;
    }

    public static double baseAmpacity(double section, Material material, Insulation insulation) {
        int index = sectionIndex(section);
        double[] table;
        if (material == Material.COPPER) table = insulation == Insulation.PVC_70 ? CU_PVC : CU_XLPE;
        else table = insulation == Insulation.PVC_70 ? AL_PVC : AL_XLPE;
        return table[index];
    }

    public static double temperatureFactor(double temperatureC, Insulation insulation) {
        if (temperatureC <= 30) return 1.00;
        if (insulation == Insulation.PVC_70) {
            if (temperatureC <= 35) return 0.94;
            if (temperatureC <= 40) return 0.87;
            if (temperatureC <= 45) return 0.79;
            if (temperatureC <= 50) return 0.71;
            if (temperatureC <= 55) return 0.61;
            if (temperatureC <= 60) return 0.50;
            return 0.0;
        }
        if (temperatureC <= 35) return 0.96;
        if (temperatureC <= 40) return 0.91;
        if (temperatureC <= 45) return 0.87;
        if (temperatureC <= 50) return 0.82;
        if (temperatureC <= 55) return 0.76;
        if (temperatureC <= 60) return 0.71;
        if (temperatureC <= 65) return 0.65;
        if (temperatureC <= 70) return 0.58;
        return 0.0;
    }

    public static double correctedAmpacity(double section, Material material, Insulation insulation, double temperatureC) {
        return baseAmpacity(section, material, insulation) * temperatureFactor(temperatureC, insulation);
    }

    public static double voltageDropPercent(double current, double voltage, double lengthM, double section,
                                            Material material, Phase phase) {
        requirePositive(current, voltage, section);
        if (lengthM < 0) throw new IllegalArgumentException("La longitud no puede ser negativa");
        double resistivity = material == Material.COPPER ? 0.0225 : 0.0360; // Ω·mm²/m, conductor caliente
        double drop = phase == Phase.THREE_PHASE
                ? Math.sqrt(3.0) * current * lengthM * resistivity / section
                : 2.0 * current * lengthM * resistivity / section;
        return 100.0 * drop / voltage;
    }

    public static double maximumLoadCurrent(double breaker, double cableAmpacity, double voltage,
                                            double lengthM, double maxDropPct, double section,
                                            Material material, Phase phase) {
        requirePositive(breaker, maxDropPct);
        double dropPerAmp = voltageDropPercent(1.0, voltage, lengthM, section, material, phase);
        double equipmentLimit = Math.min(breaker, cableAmpacity);
        return dropPerAmp == 0.0 ? equipmentLimit : Math.min(equipmentLimit, maxDropPct / dropPerAmp);
    }

    public static double recommendBreaker(double loadCurrent, double cableAmpacity) {
        requirePositive(loadCurrent, cableAmpacity);
        for (double breaker : BREAKERS) {
            if (breaker >= loadCurrent && breaker <= cableAmpacity) return breaker;
        }
        return -1;
    }

    public static double recommendSection(double loadCurrent, double breaker, double voltage, double lengthM,
                                          double maxDropPct, Material material, Insulation insulation,
                                          double temperatureC, Phase phase) {
        requirePositive(loadCurrent, breaker, voltage, maxDropPct);
        for (double section : SECTIONS) {
            double iz = correctedAmpacity(section, material, insulation, temperatureC);
            double drop = voltageDropPercent(loadCurrent, voltage, lengthM, section, material, phase);
            if (loadCurrent <= breaker && breaker <= iz && drop <= maxDropPct) return section;
        }
        return -1;
    }

    public static boolean validProtection(double loadCurrent, double breaker, double cableAmpacity) {
        return loadCurrent <= breaker && breaker <= cableAmpacity;
    }

    public static String fmt(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    private static int sectionIndex(double section) {
        for (int i=0; i<SECTIONS.length; i++) if (Math.abs(SECTIONS[i]-section) < 0.001) return i;
        throw new IllegalArgumentException("Sección no contemplada");
    }

    private static void requirePositive(double... values) {
        for (double value : values) if (!(value > 0)) throw new IllegalArgumentException("Todos los valores deben ser mayores que cero");
    }
}
