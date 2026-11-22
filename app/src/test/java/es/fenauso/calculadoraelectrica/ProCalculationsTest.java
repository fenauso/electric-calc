package es.fenauso.calculadoraelectrica;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProCalculationsTest {
    @Test
    public void calculatesResistanceAndThreePhaseCompensation() {
        assertEquals(300, ProCalculations.equivalent(ProCalculations.list("100;200"), false), 1e-6);
        assertEquals(66.666667, ProCalculations.equivalent(ProCalculations.list("100;200"), true), 1e-6);
        double kvar = ProCalculations.compensation(10, 0.8, 1);
        assertEquals(7.5, kvar, 1e-6);
        double star = ProCalculations.capacitorMicrofarads(kvar, 400, 50, 1);
        double delta = ProCalculations.capacitorMicrofarads(kvar, 400, 50, 2);
        assertEquals(star, 3 * delta, 1e-6);
    }

    @Test
    public void usesDifferentConduitColumnsForBuriedTubes() {
        assertEquals(20, ConduitSizing.tableDiameter(1, 2.5, 3));
        assertEquals(32, ConduitSizing.tableDiameter(3, 2.5, 6));
        assertEquals(40, ConduitSizing.tableDiameter(3, 2.5, 8));
    }

    @Test
    public void sizesCableByVoltageDropAndBatteryByUsableCapacity() {
        assertEquals(6, ProCalculations.minimumSection(230, 50, 16, 3, 0.0225, 2, 2.5), 1e-6);
        assertEquals(12.5, ProCalculations.nominalBatteryKwh(4.5, 2, 0.9, 0.8), 1e-6);
        assertEquals(55.25, ProCalculations.voltageAt(50, -0.3, -10), 1e-6);
    }

    @Test
    public void checksSolarSeriesRangeAndDcAcRatio() {
        Map<String, String> values = values("p=400 voc=50 vmp=40 isc=11 imp=10 cvoc=-0.3 cvmp=-0.4"
                + " cold=-10 hot=70 irr=1000 max=600 start=140 low=160 high=550 imax=25 scmax=30"
                + " inputs=2 mppt=2 ac=5 ns=8 np=1");
        String result = ProModule.SOLAR.calculate(values);
        assertTrue(result.contains("5–10 módulos por string"));
        assertTrue(result.contains("dentro de los límites calculados"));
        assertTrue(result.contains(ProCalculations.show(1.28)));
    }

    @Test
    public void usesChargerPhasesAndSeparatesProtectionChecks() {
        Map<String, String> values = values("supply=1 phase=0 p=3.68 pf=1 l=20 drop=5 site=1 out=0"
                + " metal=0 rdc=1 spd=0 kwh=60 from=20 to=80 eta=0.9");
        String result = ProModule.EV.calculate(values);
        assertTrue(result.contains(ProCalculations.show(16) + " A"));
        assertTrue(result.contains("tipo A con el RDC-DD"));
        assertTrue(result.contains("Tiempo estimado"));
        assertTrue(result.contains("Pendiente: aportar método"));
    }

    @Test
    public void sizesBatteryBranchesForPeakCurrentAsWellAsEnergy() {
        Map<String, String> values = values("daily=4.5 days=2 eta=0.9 dod=0.8 bank=48 v=12 ah=100"
                + " ms=4 mp=8 min=44 peak=5 ids=30 ics=20 charge=2 ec=0.9");
        String result = ProModule.BATTERY.calculate(values);
        assertTrue(result.contains("4 en serie × 5 ramas en paralelo"));
        assertTrue(result.contains("Horas equivalentes"));
    }

    private static Map<String, String> values(String assignments) {
        Map<String, String> values = new HashMap<>();
        for (String assignment : assignments.split(" ")) {
            String[] pair = assignment.split("=", 2);
            values.put(pair[0], pair[1]);
        }
        return values;
    }
}
