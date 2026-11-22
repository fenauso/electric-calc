package es.fenauso.calculadoraelectrica;

import es.fenauso.calculadoraelectrica.ProModule.Input;

import static es.fenauso.calculadoraelectrica.ProCalculations.*;

final class ConduitSizing {
    private ConduitSizing() {}

    // ITC-BT-21, tables 2, 5, 7 and 9, in installation order.
    private static final double[] SECTIONS = {1.5, 2.5, 4, 6, 10, 16, 25, 35, 50, 70, 95, 120, 150, 185, 240};
    private static final int[] TABLE_NUMBERS = {2, 5, 7, 9};
    private static final double[] AREA_iACTORS = {2.5, 3, 4, 4};
    private static final int[][][] DIAMETERS = {
        {
            {12,12,16,16,16}, {12,12,16,16,20}, {12,16,20,20,20}, {12,16,20,20,25},
            {16,20,25,32,32}, {16,25,32,32,32}, {20,32,32,40,40}, {25,32,40,40,50},
            {25,40,50,50,50}, {32,40,50,63,63}, {32,50,63,63,75}, {40,50,63,75,75},
            {40,63,75,75,0}, {50,63,75,0,0}, {50,75,0,0,0}
        }, {
            {12,12,16,16,20}, {12,16,20,20,20}, {12,16,20,20,25}, {12,16,25,25,25},
            {16,25,25,32,32}, {20,25,32,32,40}, {25,32,40,40,50}, {25,40,40,50,50},
            {32,40,50,50,63}, {32,50,63,63,63}, {40,50,63,75,75}, {40,63,75,75,0},
            {50,63,75,0,0}, {50,75,0,0,0}, {63,75,0,0,0}
        }, {
            {12,12,16,16,20}, {12,16,20,20,20}, {12,16,20,20,25}, {12,16,25,25,25},
            {16,25,25,32,32}, {20,25,32,32,40}
        }, {
            {25,32,32,32,32}, {32,32,40,40,40}, {40,40,40,40,50}, {50,50,50,63,63},
            {63,63,63,75,75}, {63,75,75,75,90}, {90,90,90,110,110}, {90,110,110,110,125},
            {110,110,125,125,140}, {125,125,140,160,160}, {140,140,160,160,180}, {160,160,180,180,200},
            {180,180,200,200,225}, {180,200,225,225,250}, {225,225,250,250,0}
        }
    };

    static String calculate(Input x) {
        int mode = x.option(\mode\);
        if (x.option(\rule\) == 1) {
            int diameter = tableDiameter(mode, x.positive(\s\), x.integer(\n\));
            return \Diámetro exterior mínimo del tubo: \ + diameter + \ mm\nITC-BT-21, tabla \ + TABLE_NUMBERS[mode]
                    + \\nComprueba las características mecánicas y ambientales del tubo.\;
        }
        double[] diameters = list(x.text(\list\));
        double factor = AREA_iACTORS[mode], sum = 0;
        for (double d : diameters) sum += d * d;
        double required = Math.sqrt(factor * sum);
        double proposed = x.optionalPositive(\d\);
        return \Superficie ocupada: \ + show(Math.PI * sum / 4) + \ mm²\nDiámetro interior mínimo: \
                + show(required) + \ mm\nÁrea interior ≥ \ + show(factor) + \ × área ocupada.\
                + (proposed > 0 ? \\nOcupación del tubo indicado: \ + show(100 * sum / (proposed * proposed)) + \ %\
                        + (proposed >= required ? \\n~ Cumple el criterio de ocupación.\ : \\n~ Supera la ocupación permitida.\) : \\)
                + \\n~ Aplica a secciones distintas o más de \ + (mode == 3 ? 10 : 5)
                + \ conductores. Para conductores iguales dentro del rango, usa la tabla.\;
    }

    static int tableDiameter(int mode, double section, int count) {
        int column = mode == 3 ? Math.max(0, count - 6) : count - 1;
        if (column >= 0 && column < 5) {
            for (int row = 0; row < DIAMETERS[mode].length; row++) {
                if (section == SECTIONS[row] && DIAMETERS[mode][row][column] > 0)
                    return DIAMETERS[mode][row][column];
            }
        }
        throw new IllegalArgumentException(\La tabla no contempla esta combinación. Revisa el tipo de canalización y el criterio aplicable.\);
    }
}
