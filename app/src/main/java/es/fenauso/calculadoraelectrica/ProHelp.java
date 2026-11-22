package es.fenauso.calculadoraelectrica;

final class ProHelp {
    private ProHelp() {}

    static String overview(ProModule module) {
        return switch (module) {
            case RESISTANCE -> "Elige ley de Ohm para obtener la tercera magnitud y la potencia. Usa Serie o Paralelo para combinar resistencias, o Conductor para estimar su resistencia a una temperatura dada.";
            case REACTIVE -> "Introduce la potencia activa y los valores de cos φ de una carga inductiva. El resultado expresa los kvar totales y los µF de cada rama. Para elegir la batería comercial, comprueba armónicos y variación de carga.";
            case CONDUIT -> "La ITC-BT-21 distingue el diámetro exterior nominal de la tabla y el diámetro interior libre del cálculo de ocupación. Elige el criterio que corresponda y comprueba también las características mecánicas del tubo.";
            case ENERGY -> "Calcula energía y coste para un periodo con un precio por kWh. Si tienes varias tarifas, calcula cada periodo y suma los costes de energía; incorpora los términos fijos y los impuestos una sola vez.";
            case PV_PROJECT -> "Elige un objetivo, revisa las fichas del panel y el inversor y añade el recorrido de cable. La propuesta genera cantidades y permite abrir el detalle con los mismos datos. Puedes guardar fichas de equipos en el dispositivo.";
            case SOLAR -> "Copia los valores de las fichas del módulo y del inversor. El cálculo prueba los límites de tensión y corriente para MPPT idénticos con el mismo reparto. Repite por separado para grupos con características distintas y comprueba después los límites del inversor completo.";
            case PV_PROTECTION -> "Evalúa la corriente inversa y compara el fusible del string. En CA, contrasta el magnetotérmico, el diferencial y las comprobaciones pendientes. Para sobretensiones introduce el resultado del estudio del emplazamiento.";
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
            case PV_PROJECT -> switch (key) {
                case "design" -> "La estimación inicial entrega una referencia de paneles e inversor. Con fichas de equipos se pueden dimensionar strings, cableado y protecciones.";
                case "goal" -> "Elige una potencia pico o estima la potencia a partir de la energía diaria y las horas solares de diseño.";
                case "ratio" -> "Relación CC/CA supuesta para estimar la potencia nominal del inversor. Contrasta después con su ficha.";
                case "target" -> "Potencia pico mínima del generador.";
                case "dailygen" -> "Parte de la demanda diaria que se quiere producir. La coincidencia horaria requiere un estudio separado.";
                case "sun" -> "Usa las horas solares pico en el plano del generador para el emplazamiento, orientación y periodo de diseño.";
                case "pr" -> "Fracción que queda tras las pérdidas estimadas. Evita contabilizar dos veces las pérdidas ya incluidas en las horas solares.";
                case "cold" -> "Temperatura de célula en el extremo frío de diseño.";
                case "hot" -> "Temperatura de célula en el extremo caliente de diseño.";
                case "irr" -> "Valor máximo previsto sobre el plano de los módulos para estimar las corrientes.";
                case "p" -> "Potencia pico STC de la ficha del módulo.";
                case "voc" -> "Tensión en circuito abierto en STC.";
                case "vmp" -> "Tensión en máxima potencia en STC.";
                case "isc" -> "Corriente de cortocircuito en STC.";
                case "imp" -> "Corriente en máxima potencia en STC.";
                case "cvoc" -> "Coeficiente térmico con el signo de la ficha.";
                case "cvmp" -> "Coeficiente térmico de tensión en máxima potencia.";
                case "cisc" -> "Si lo omites, la propuesta de intensidad presenta la corrección térmica pendiente.";
                case "reverse" -> "Límite de corriente inversa de la ficha. Es distinto del calibre máximo de fusible.";
                case "fuse" -> "Calibre máximo autorizado por el fabricante del módulo.";
                case "ac" -> "Potencia activa nominal de salida. El proyecto usa un inversor.";
                case "pmax" -> "Límite de potencia pico total autorizado por la ficha.";
                case "phase" -> "Fases y tensión de la salida del inversor.";
                case "iac" -> "Tiene prioridad sobre la corriente estimada a partir de la potencia.";
                case "max" -> "Usa el menor de los límites CC del inversor, los módulos y demás equipos.";
                case "start" -> "Tensión de arranque de la ficha.";
                case "low" -> "Extremo inferior de la ventana de seguimiento.";
                case "high" -> "Extremo superior de la ventana de seguimiento.";
                case "imax" -> "Introduce el límite operativo por MPPT.";
                case "scmax" -> "Introduce el límite de cortocircuito por MPPT.";
                case "inputs" -> "Número de strings físicamente conectables por cada MPPT.";
                case "trackers" -> "La propuesta reparte strings iguales entre MPPT de características idénticas.";
                case "maxac" -> "Máximo de protección CA permitido por el fabricante.";
                case "ldc" -> "Se aplica la longitud del string más largo para calcular la caída y estimar la compra de cable.";
                case "lac" -> "Longitud del tramo de salida analizado.";
                case "dropdc" -> "Límite de diseño del tramo CC.";
                case "dropac" -> "Presupuesto de caída del tramo CA. Verifica el alcance del circuito y el límite aplicable.";
                case "tdc" -> "Si la conoces, indica una sección comercial que cumpla intensidad admisible con el método, temperatura, cable y agrupamiento reales.";
                case "tac" -> "Si la conoces, indica la sección mínima que ya cumple térmicamente para esta línea.";
                case "izdc" -> "Introduce la Iz corregida de la sección que finalmente se propone, para contrastar el gPV.";
                case "izac" -> "Introduce la Iz corregida de la sección que finalmente se propone para contrastar el magnetotérmico.";
                case "feed" -> "Incluye otros aportes documentados. Introduce 0 solo si se ha verificado su ausencia.";
                case "mindc" -> "Límite inferior definido por el método aplicable y por el fabricante del dispositivo.";
                case "integrated" -> "Indica si el inversor incorpora el seccionamiento apropiado para todas las ramas.";
                case "rcd" -> "Contrasta la solución del circuito residencial con el fabricante del inversor.";
                case "spdcc" -> "Resultado del estudio y de la coordinación en el lado CC.";
                case "spdca" -> "Resultado del estudio y de la coordinación en el lado CA.";
                case "fault" -> "Permite calcular el mínimo poder de corte que debe contrastarse.";
                case "storage" -> "Activa el dimensionamiento del banco. El inversor y las baterías deben admitir conexión y comunicación compatibles.";
                case "daily" -> "Consumo de las cargas que quieres alimentar desde el banco.";
                case "days" -> "Días de autonomía deseada con la demanda de respaldo indicada.";
                case "eta" -> "Rendimiento desde la batería hasta las cargas.";
                case "dod" -> "Fracción utilizable según el fabricante y el régimen de servicio.";
                case "bank" -> "Tensión nominal que requiere el inversor compatible.";
                case "v" -> "Copia el valor de la ficha de una unidad.";
                case "ah" -> "Capacidad nominal de cada unidad.";
                case "ms" -> "Límite de unidades en serie del fabricante.";
                case "mp" -> "Límite de ramas en paralelo de la ficha.";
                case "min" -> "Tensión mínima admisible usada para calcular la corriente de pico.";
                case "peak" -> "Suma de las cargas que pueden demandar energía a la vez.";
                case "ids" -> "Límite por unidad para la duración de la punta.";
                case "ics" -> "Límite de carga por unidad.";
                case "bmsd" -> "Menor límite total de batería, BMS e inversor.";
                case "bmsc" -> "Menor límite total de banco, BMS y cargador.";
                case "charge" -> "Potencia neta disponible antes de aplicar el rendimiento de carga.";
                case "ec" -> "Fracción de energía que queda almacenada.";
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
                case "trackers" -> "Número de MPPT idénticos disponibles para explorar alternativas. Si queda vacío, se usa el número utilizado.";
                case "target" -> "Potencia pico mínima deseada en kWp. Si queda vacía se toma como referencia la potencia nominal CA del inversor.";
                case "mppt" -> "Número de seguidores MPPT utilizados con este mismo reparto de módulos y strings.";
                case "ac" -> "Potencia activa nominal de salida del inversor en kW.";
                case "pmax" -> "Potencia pico FV máxima que admite el fabricante para la configuración indicada. Déjala vacía si ese dato no está disponible.";
                case "ns" -> "Número de módulos conectados en serie en cada string. En serie se suman las tensiones.";
                case "np" -> "Número de strings en paralelo por MPPT. En paralelo se suman las corrientes.";
                case "fuse" -> "Calibre máximo del fusible serie autorizado por el fabricante del módulo. Se muestra como dato de comprobación.";
                default -> throw new IllegalArgumentException(key);
            };
            case PV_PROTECTION -> switch (key) {
                case "voc" -> "Copia la Voc máxima del string calculada a la temperatura más desfavorable.";
                case "isc" -> "Usa la corriente de cortocircuito de diseño de un solo string, con las correcciones de irradiancia y temperatura aplicables.";
                case "parallel" -> "Cuenta las ramas con conexión eléctrica común que pueden alimentar el defecto, sin sumar MPPT independientes.";
                case "feed" -> "Introduce el aporte de otras fuentes en CC en el escenario de fallo. Usa cero solo si está verificado.";
                case "reverse" -> "Comprueba el límite de corriente inversa en la documentación. Es un dato distinto del fusible máximo.";
                case "fuse" -> "Límite del fabricante para el fusible serie del módulo.";
                case "fuseselect" -> "Calibre del fusible que se quiere contrastar con el intervalo obtenido.";
                case "mindc" -> "Usa el mínimo definido por el método aplicable y por el fabricante, incluidas las correcciones del dispositivo.";
                case "maxdc" -> "Introduce el calibre máximo documentado para un automático FV si se admite como alternativa al fusible. Déjalo vacío si no está verificado.";
                case "izdc" -> "Intensidad admisible corregida del conductor de la rama protegida.";
                case "phase" -> "Fases de salida del inversor. Si falta la intensidad máxima de ficha, permite estimar la nominal con la potencia.";
                case "ac" -> "Potencia activa nominal del inversor. El cálculo alternativo supone un factor de potencia de 1.";
                case "iac" -> "Intensidad máxima de salida según el fabricante. Tiene prioridad sobre la estimación por potencia.";
                case "izac" -> "Intensidad admisible del conductor de salida, con el montaje y las correcciones aplicables.";
                case "maxac" -> "Límite de protección CA de la ficha o del manual del inversor.";
                case "fault" -> "Valor calculado o declarado para comprobar el poder de corte mínimo del automático CA.";
                case "rcd" -> "Confirma el tipo de diferencial con el fabricante. El apartado 4.3 de la ITC-BT-40 prevé tipo A para instalaciones próximas al consumo.";
                case "site" -> "En zona residencial o análoga, la ITC-BT-40 establece sensibilidad de 30 mA para circuitos de generación.";
                case "spd" -> "Selecciona el resultado de la evaluación de sobretensiones y de la coordinación con dispositivos instalados. Verifica ambos lados por separado.";
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
        if (module == ProModule.PV_PROJECT) {
            return switch (key) {
                case "design" -> "Nivel de dimensionamiento";
                case "goal" -> "Tipo de objetivo";
                case "phase" -> "Salida CA del inversor";
                case "integrated" -> "Seccionador CC";
                case "rcd" -> "Diferencial según fabricante";
                case "spdcc" -> "Sobretensiones CC";
                case "spdca" -> "Sobretensiones CA";
                case "storage" -> "Batería de respaldo";
                default -> throw new IllegalArgumentException(key);
            };
        }
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
            case "rcd" -> "Diferencial según fabricante";
            case "spd" -> "Protecciones contra sobretensiones";
            default -> throw new IllegalArgumentException(key);
        };
    }
}
