package es.fenauso.calculadoraelectrica;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

final class ProExamples {
    private static final Map<ProModule, Map<String, String>> VALUES = new EnumMap<>(ProModule.class);
    static {
        add(ProModule.RESISTANCE,
                "v=12",
                "i=0,25",
                "list=100; 220; 470",
                "l=20",
                "s=2,5",
                "t=20");
        add(ProModule.REACTIVE,
                "p=10",
                "a=0,8",
                "b=0,95",
                "v=230");
        add(ProModule.CONDUIT,
                "list=6,5; 6,5; 5; 4",
                "d=25",
                "s=2,5",
                "n=3");
        add(ProModule.ENERGY,
                "p=1,5",
                "h=2",
                "d=30",
                "price=0,18");
        add(ProModule.SOLAR,
                "p=440",
                "voc=40,6",
                "vmp=34",
                "isc=13,8",
                "imp=12,94",
                "cvoc=-0,25",
                "cvmp=-0,30",
                "cisc=0,045",
                "cold=-5",
                "hot=70",
                "irr=1050",
                "max=600",
                "start=120",
                "low=100",
                "high=550",
                "imax=16",
                "scmax=20",
                "inputs=1",
                "mppt=2",
                "ac=5",
                "pmax=7,5",
                "ns=8",
                "np=1",
                "fuse=25");
        add(ProModule.DC,
                "v=235",
                "i=13",
                "ib=18,5",
                "l=25",
                "max=1,5",
                "s=6",
                "iz=32",
                "method=Ejemplo: cobre, dos conductores cargados; Iz ilustrativa");
        add(ProModule.BATTERY,
                "daily=4,5",
                "days=1",
                "eta=0,92",
                "dod=0,8",
                "bank=51,2",
                "v=51,2",
                "ah=100",
                "ms=1",
                "mp=4",
                "min=44,8",
                "peak=3",
                "ids=80",
                "ics=50",
                "bmsd=100",
                "bmsc=80",
                "charge=2",
                "ec=0,95");
        add(ProModule.EV,
                "p=3,68",
                "i=16",
                "l=25",
                "scheme=3a",
                "s=2,5",
                "iz=20",
                "method=Ejemplo: cobre en tubo empotrado; Iz ilustrativa",
                "kwh=60",
                "car=7,4",
                "grid=3,68");
    }

    private ProExamples() {}

    static String value(ProModule module, String key, String fallback) {
        return VALUES.get(module).getOrDefault(key, fallback);
    }

    private static void add(ProModule module, String... settings) {
        Map<String, String> values = new HashMap<>();
        for (String setting : settings) {
            String[] pair = setting.split("=", 2);
            values.put(pair[0], pair[1]);
        }
        VALUES.put(module, values);
    }
}
