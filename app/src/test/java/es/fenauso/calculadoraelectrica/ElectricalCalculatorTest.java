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
    public void limitsMaximumLoadByVoltageDrop() {
        double current = ElectricalCalculator.maximumLoadCurrent(
                16, 20, 230, 50, 3, 2.5,
                ElectricalCalculator.Material.COPPER, ElectricalCalculator.Phase.MONOPHASE);
        assertEquals(7.6666666667, current, 0.000001);
    }

    @Test
    public void limitsMaximumLoadByEquipmentWhenLengthIsZero() {
        double current = ElectricalCalculator.maximumLoadCurrent(
                25, 20, 230, 0, 3, 2.5,
                ElectricalCalculator.Material.COPPER, ElectricalCalculator.Phase.MONOPHASE);
        assertEquals(20, current, 0.000001);
    }


    @Test
    public void validatesProtectionChain() {
        assertTrue(ElectricalCalculator.validProtection(12, 16, 20));
    }
}
