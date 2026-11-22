package es.fenauso.calculadoraelectrica;

final class ProHelp {
    private ProHelp() {}

    static String overview(ProModule module) {
        return switch (module) {
            case RESISTANCE -> "Elige ley de Ohm para obtener la tercera magnitud y la potencia. Usa Serie o Paralelo para combinar resistencias, o Conductor para estimar su resistencia a una temperatura dada.";
            case REACTIVE -> "Introduce la potencia activa y los valores de cos φ de una carga inductiva. El resultado expresa los kvar totales y los µF de cada rama. Para elegir la batería comercial, comprueba armónicos y variación de carga.";
            case CONDUIT -> "La ITC-BT-21 distingue el diámetro exterior nominal de la tabla y el diámetro interior libre del cálculo de ocupación. Elige el criterio que corresponda y comprueba también las características mecánicas del tubo.";
            case ENERGY -> "Calcula energía y coste para un periodo con un precio por kWh. Si tienes varias tarifas, calcula cada periodo y suma los costes de energía; incorpora los términos fijos y los impuestos una sola vez.";
            case SOLAR -> "Copia los valores de las fichas del módulo y del inversor. El cálculo prueba los límites de tensión y corriente para MPPT idénticos con el mismo reparto. Repite por separado para grupos con características distintas y comprueba después los límites del inversor completo.";
            case DC -> "Calcula la sección mínima por caída para un tramo CC. Puedes comprobar también una sección e intensidad admisible conocidas. El cálculo mantiene separadas la caída, el criterio térmico y las protecciones pendientes.";
            case BATTERY -> "Empieza por el consumo en CA y la autonomía. La app estima la capacidad del banco, las ramas necesarias para atender el pico de descarga y las horas equivalentes de recarga. Contrasta tensiones y límites con los fabricantes de la batería y el inversor.";
            case EV -> "Dimensiona el circuito dedicado de un punto de recarga CA modo 3. Indica las fases que usa el punto, los datos de la línea y las protecciones documentadas. La estimación de tiempo usa la potencia disponible y la energía que falta en la batería.";
        };
    }

    static String field(ProModule module, String key) {
        return switch (module) {
            case RESISTANCE -> switch (key) {
                case "mode" -> "Cada opción muestra los campos que necesita. Para una red mixta, calcula primero sus grupos en serie o paralelo.";
                case "v" -> "Diferencia de potencial entre los bornes. Selecciona V o mV. Déjala vacía si conoces I y R.";
                case "i" -> "Corriente que atraviesa la resistencia. Selecciona A o mA. Déjala vacía si conoces V y R.";
                case "r" -> "Resistencia eléctrica. Selecciona Ω o mΩ. Déjala vacía si conoces V e I.";
                case "list" -> "Selecciona Ω o mΩ para toda la lista. Escribe un valor por resistencia separado por punto y coma. Ejemplo: 100; 220; 470.";
                case "metal" -> "El material determina la resistividad y cómo cambia con la temperatura.";
                case "l" -> "Longitud total del conductor que quieres calcular. Si buscas la resistencia de ida y vuelta, suma ambos recorridos.";
                case "s" -> "Área de la parte conductora en mm², según la ficha del cable.";
                case "t" -> "Temperatura de trabajo del conductor. A 20 °C se usa la resistividad de referencia.";
                default -> throw new IllegalArgumentException(key);
            };
            case REACTIVE -> switch (key) {
                case "p" -> "Potencia activa que consume la carga en funcionamiento. Selecciona kW o W.";
                case "a" -> "Cos φ medido antes de compensar, entre 0 y 1. En cargas con armónicos, distingue cos φ de factor de potencia total.";
                case "b" -> "Cos φ que quieres alcanzar. Debe ser igual o superior al inicial.";
                case "v" -> "Tensión entre fases en trifásica o entre fase y neutro en monofásica.";
                case "f" -> "Frecuencia de red, normalmente 50 Hz en España.";
                case "mode" -> "En estrella cada rama ve la tensión de línea dividida por √3. En triángulo recibe la tensión de línea completa.";
                default -> throw new IllegalArgumentException(key);
            };
            case CONDUIT -> switch (key) {
                case "mode" -> "Forma de instalación del tubo. Determina la tabla y el multiplicador de área aplicable.";
                case "rule" -> "Usa tablas para conductores iguales dentro de sus rangos. Usa ocupación para secciones diferentes o cuando se supere el número previsto por la instrucción.";
                case "list" -> "Introduce el diámetro exterior con aislamiento de cada cable, en mm. Ejemplo: 6,5; 6,5; 5. Repite el diámetro por cada cable instalado.";
                case "s" -> "Área nominal en mm² de cada conductor unipolar igual. Se usa para localizar la fila de la tabla.";
                case "n" -> "Cantidad de conductores iguales que comparten el tubo. Cuenta también el conductor de protección si va por ese tubo.";
                case "d" -> "Diámetro libre interior de la ficha del tubo. Úsalo para comprobar la ocupación del producto propuesto.";
                default -> throw new IllegalArgumentException(key);
            };
            case ENERGY -> switch (key) {
                case "p" -> "Potencia media durante las horas de uso. Selecciona kW o W y la app convierte el valor.";
                case "h" -> "Promedio de horas de uso al día, entre 0 y 24.";
                case "d" -> "Número de días del periodo de cálculo.";
                case "price" -> "Precio por kWh aplicable a las horas introducidas. Si añades impuestos aparte, usa el precio antes de ese impuesto.";
                case "fixed" -> "Importe fijo total del periodo, en euros. Introduce 0 para calcular solo el coste variable.";
                case "tax" -> "Porcentaje simplificado aplicado al subtotal de costes. Introduce 0 si ya está incluido o quieres el total antes de impuestos.";
                default -> throw new IllegalArgumentException(key);
            };
            case SOLAR -> switch (key) {
                case "p" -> "Potencia pico de un módulo en condiciones STC, en Wp.";
                case "voc" -> "Tensión de circuito abierto de un módulo en condiciones STC.";
                case "vmp" -> "Tensión de un módulo en su punto de máxima potencia en condiciones STC.";
                case "isc" -> "Corriente de cortocircuito de un módulo en condiciones STC.";
                case "imp" -> "Corriente de un módulo en el punto de máxima potencia en condiciones STC.";
                case "cvoc" -> "Variación porcentual de Voc por grado respecto a 25 °C. Conserva el signo de la ficha, por ejemplo −0,29 %/°C.";
                case "cvmp" -> "Variación porcentual de Vmp por grado. Usa el coeficiente de tensión en máxima potencia.";
                case "cisc" -> "Variación porcentual de Isc por grado. Si queda vacío, el cálculo indicará que omite esta corrección.";
                case "cold" -> "Temperatura mínima de célula prevista. Se usa, entre otras comprobaciones, para estimar Voc en frío.";
                case "hot" -> "Temperatura máxima de célula bajo irradiación. Si solo conoces la temperatura ambiente, estima antes la de célula.";
                case "irr" -> "Irradiancia máxima de diseño sobre el plano del módulo. El modelo aproxima la variación de corriente con este valor.";
                case "max" -> "Introduce el menor límite de tensión CC aplicable al inversor, módulos y demás equipos del tramo.";
                case "start" -> "Tensión mínima de arranque publicada por el inversor. Diferénciala del límite inferior de seguimiento MPPT.";
                case "low" -> "Extremo inferior de la ventana de seguimiento MPPT.";
                case "high" -> "Extremo superior de la ventana de seguimiento MPPT.";
                case "imax" -> "Corriente de funcionamiento máxima admitida por cada MPPT, en amperios.";
                case "scmax" -> "Límite de corriente de cortocircuito publicado por MPPT. Puede ser distinto del límite operativo.";
                case "inputs" -> "Número de conexiones físicas de string por cada MPPT según la ficha.";
                case "mppt" -> "Número de seguidores MPPT utilizados con este mismo reparto de módulos y strings.";
                case "ac" -> "Potencia activa nominal de salida del inversor en kW.";
                case "pmax" -> "Potencia pico FV máxima que admite el fabricante para la configuración indicada. Déjala vacía si ese dato no está disponible.";
                case "ns" -> "Número de módulos conectados en serie en cada string. En serie se suman las tensiones.";
                case "np" -> "Número de strings en paralelo por MPPT. En paralelo se suman las corrientes.";
                case "fuse" -> "Calibre máximo del fusible serie autorizado por el fabricante del módulo. Se muestra como dato de comprobación.";
                default -> throw new IllegalArgumentException(key);
            };
            case DC -> switch (key) {
                case "v" -> "Tensión CC de trabajo del tramo, por ejemplo Vmp del string en la condición de diseño.";
                case "i" -> "Corriente prevista en funcionamiento, usada para calcular la caída de tensión.";
                case "ib" -> "Corriente aplicable al dimensionamiento térmico. Debe recoger los márgenes que correspondan al tramo.";
                case "l" -> "Distancia de ida entre los dos extremos. La fórmula incluye el retorno.";
                case "max" -> "Porcentaje de caída de tensión asignado a este tramo dentro del presupuesto total de caída.";
                case "metal" -> "Material del conductor. Determina la resistividad empleada en la estimación de caída.";
                case "s" -> "Sección comercial del cable que quieres comprobar. Si queda vacía, se muestra la caída de la sección calculada.";
                case "iz" -> "Intensidad admisible corregida de la sección indicada, obtenida con el método, aislamiento, agrupamiento y temperatura reales.";
                case "method" -> "Anota el método de montaje y las condiciones que sustentan la Iz, por ejemplo tubo empotrado, agrupamiento y temperatura.";
                default -> throw new IllegalArgumentException(key);
            };
            case BATTERY -> switch (key) {
                case "daily" -> "Energía diaria de las cargas que debe alimentar la batería, en kWh medidos en CA.";
                case "days" -> "Días de autonomía antes de recargar. Se admiten fracciones, por ejemplo 1,5.";
                case "eta" -> "Rendimiento combinado desde la batería hasta las cargas en CA. Introduce 0,9 para el 90 %.";
                case "dod" -> "Fracción de la capacidad nominal que se puede utilizar, por ejemplo 0,8 para el 80 %.";
                case "bank" -> "Tensión nominal del banco requerida por el inversor o el sistema.";
                case "v" -> "Tensión nominal de una sola unidad de batería según su ficha.";
                case "ah" -> "Capacidad nominal en Ah de una sola unidad. En paralelo se suman los Ah.";
                case "ms" -> "Máximo de unidades en serie permitido por el fabricante. Usa 1 cuando no se permita conectarlas en serie.";
                case "mp" -> "Máximo de ramas en paralelo permitido por el fabricante.";
                case "min" -> "Tensión mínima del banco bajo carga. Se usa para estimar la mayor corriente demandada para una potencia dada.";
                case "peak" -> "Potencia máxima de las cargas en CA que pueden coincidir durante la descarga.";
                case "ids" -> "Corriente máxima de descarga por unidad. Usa el límite compatible con la duración del pico previsto.";
                case "ics" -> "Corriente máxima de carga por unidad según su ficha.";
                case "bmsd" -> "Menor límite total de descarga entre BMS e inversor, referido al banco.";
                case "bmsc" -> "Menor límite total de carga entre BMS y cargador, referido al banco.";
                case "charge" -> "Potencia disponible para recargar tras descontar consumos simultáneos y antes de aplicar el rendimiento de carga.";
                case "ec" -> "Fracción de la potencia de entrada que se almacena, por ejemplo 0,9 para el 90 %.";
                default -> throw new IllegalArgumentException(key);
            };
            case EV -> switch (key) {
                case "supply" -> "Tipo de suministro de la instalación. Una acometida trifásica puede alimentar un punto monofásico a 230 V.";
                case "phase" -> "Fases que utiliza el cargador en el punto de conexión. La selección determina la fórmula de intensidad.";
                case "p" -> "Potencia activa nominal o configurada del punto de recarga, en kW.";
                case "i" -> "Corriente máxima configurada por fase. Si la indicas, se usa para dimensionar la línea.";
                case "pf" -> "Relación entre potencia activa y aparente, entre 0 y 1. Usa el valor de ficha si está disponible.";
                case "l" -> "Longitud de ida desde el origen del circuito hasta el punto de recarga.";
                case "drop" -> "Caída de tensión asignada al circuito. La ITC-BT-52 fija un máximo del 5 % desde su origen hasta el punto.";
                case "site" -> "El tipo de emplazamiento condiciona, entre otras comprobaciones, el material de conductor admitido.";
                case "out" -> "Elige Exterior si la estación está a la intemperie. La app recuerda comprobar requisitos de protección ambiental y alimentación.";
                case "scheme" -> "Identificador del esquema de conexión de la ITC-BT-52, por ejemplo 2 o 3a. Anótalo según el origen y la medida del suministro.";
                case "metal" -> "Material del conductor. En viviendas y aparcamientos colectivos de edificios de viviendas el módulo exige cobre.";
                case "s" -> "Sección comercial que quieres comprobar. Si se deja vacía, se muestra la caída con la sección calculada.";
                case "iz" -> "Intensidad admisible ya corregida del cable de esa sección. Sirve para contrastar el calibre candidato.";
                case "method" -> "Anota montaje, aislamiento, agrupamiento, temperatura y conductores cargados que justifican el dato de Iz.";
                case "rdc" -> "Comprueba en la documentación si hay un RDC-DD conforme a IEC 62955, integrado o externo, antes de seleccionar diferencial tipo A.";
                case "spd" -> "Indica si existen protecciones documentadas contra sobretensiones temporales y transitorias aguas arriba.";
                case "distance" -> "Distancia desde el protector transitorio aguas arriba hasta la estación, útil para valorar una protección adicional cercana.";
                case "kwh" -> "Capacidad útil de la batería del vehículo, en kWh. Este campo activa la estimación de tiempo.";
                case "from" -> "Estado de carga al enchufar el vehículo, de 0 a 100 %.";
                case "to" -> "Estado de carga que quieres alcanzar, de 0 a 100 %. Debe ser igual o superior al inicial.";
                case "car" -> "Potencia CA máxima que acepta el cargador de a bordo con la conexión elegida.";
                case "grid" -> "Potencia disponible para el punto tras descontar otros consumos o aplicar la gestión de carga.";
                case "eta" -> "Fracción de la energía tomada de la red que llega a la batería, por ejemplo 0,9 para el 90 %.";
                default -> throw new IllegalArgumentException(key);
            };
        };
    }

    static String selectionTitle(ProModule module, String key) {
        return switch (key) {
            case "mode" -> switch (module) {
                case RESISTANCE -> "Tipo de cálculo";
                case REACTIVE -> "Conexión de los condensadores";
                case CONDUIT -> "Tipo de canalización";
                default -> throw new IllegalArgumentException(key);
            };
            case "metal" -> "Material del conductor";
            case "rule" -> "Criterio de dimensionamiento";
            case "supply" -> "Tipo de suministro";
            case "phase" -> "Fases del punto de recarga";
            case "site" -> "Emplazamiento";
            case "out" -> "Interior o exterior";
            case "rdc" -> "Detección de fuga en continua";
            case "spd" -> "Protecciones contra sobretensiones";
            default -> throw new IllegalArgumentException(key);
        };
    }
}
