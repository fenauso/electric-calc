package es.fenauso.calculadoraelectrica;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ElectricalCalculatorTest {
    @Test
    public void calculatesSinglePhaseCurrent() {
        double current = ElectricalCalculator.currentFromPower(
                3500, 230, 0.95, ElectricalCalculator.Phase.MONOPHASE);
        assertEquals(16.02, current, 0.02);
    }

    @Test
    public void recommendsProtectedSection() {
        double section = ElectricalCalculator.recommendSection(
                16.02,
                20,
                230,
                20,
                3,
                ElectricalCalculator.Material.COPPER,
                ElectricalCalculator.Insulation.PVC_70,
                30,
                ElectricalCalculator.Phase.MONOPHASE);
        assertEquals(2.5, section, 0.001);
    }

    @Test
    public void validatesProtectionChain() {
        assertTrue(ElectricalCalculator.validProtection(12, 16, 20));
    }
}
