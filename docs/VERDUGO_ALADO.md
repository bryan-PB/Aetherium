# Prototipo: Verdugo Alado (Winged Executioner)

## Decisión de diseño

Se reutiliza la entidad interna `aetherium:astral_devourer` para conservar los mundos guardados, pero su **identidad visual y nombre mostrado** pasan a Verdugo Alado. Este diseño sustituye totalmente el antiguo monstruo con ojo flotante y anillos cósmicos.

### Silueta y textura (Minecraft 1.16.5 / Forge)

- Modelo completamente nuevo `client/model/AstralDevourerModel.java`, con altura corporal cercana a 2,7 bloques, proporción aproximada a la hitbox de 2,8 bloques.
- Humanoide flotante de armadura oscura, máscara cerámica sin expresión, dos largos brazos con garras, protección de torso y faldones dañados.
- Dos alas enormes con articulaciones internas y externas, membranas irregulares, bordes resistentes y movimiento durante vuelo, carga y embestidas.
- Ojo incrustado **en el centro del pecho**: relieve con socket, dos párpados móviles, iris dorado en fases 1/2 e iris rojizo en fase 3, placas que se abren durante las cargas.
- Paleta propia **carbón, gris verdoso, marfil envejecido, latón y ámbar**; sin cambios de tono violeta sobre todo el cuerpo. Textura 128x128 nueva y original: `textures/entity/boss/winged_executioner.png`.
- No se agregan halos, ojos secundarios en alas, apéndices sin propósito ni armamento de dragón.

### Fases visibles

1. **I:** armadura intacta, alas recogidas, ojo parcialmente protegido.
2. **II:** se abren placas del pecho y costillas laterales; las alas amplían su extensión.
3. **III:** iris rojo, grietas en la coraza, puntas óseas desplegadas en extremos de las alas y postura más abierta.

### Lectura de ataques

- **Cristales:** comienza el estado 1 con 23 ticks. El jefe deja de desplazarse, levanta las alas y tres cristales aparecen junto al pecho. Primer disparo cuando quedan 11 ticks: **12 ticks (0,6 s) de aviso**. En fases II/III hace una segunda ráfaga con 4 ticks restantes. Ambas salvas conservan velocidad y dispersión previa.
- **Rayo:** procede de la altura del ojo en el pecho y empieza 0,38 bloques delante del cuerpo. El origen del rayo dibujado, las partículas y el cálculo de daño usan la misma función; carga 26 ticks antes de disparar.
- **Embestida:** alas plegadas hacia atrás y torso inclinado.
- **Explosión / atracción:** brazos abiertos y alas expandidas, sin superponerse con una salva de cristales.
- **Ilusión:** utiliza el **mismo modelo** renderizado en la posición del señuelo.

El rediseño **no cambia los registros** de las entidades, objetos, recetas o sonidos, ni renombra el repositorio. El nuevo nombre visible del mod sigue siendo Kryzorae.

### Ideas reservadas para criaturas futuras

No se descartan Ídolo de la Sima, Murciélago de Tórax Ocular, Carroñero del Umbral, Abominación Monástica y Quimera de Pecho Vigilante. Son conceptos, **no mobs ya implementados**.

### Pruebas pendientes en el cliente Minecraft

- [ ] Captura frontal y lateral: comprobar proporción cuerpo/alas y máscara a distintas distancias.
- [ ] Comprobar que el ojo sea visible sin taparse con brazos o placas en fases I, II y III.
- [ ] Comprobar la dirección aparente del rayo y su origen a la altura del ojo.
- [ ] Comprobar parada real y ventana de esquiva de cristales (12 ticks).
- [ ] Verificar animaciones de dash, transformación, muerte y el señuelo.
- [ ] Comprobar distancias de dibujo y posible culling de alas fuera de la hitbox.
- [ ] Comprobar que cargar un mundo creado con el jefe anterior no borre entidades ni inventario.

La compilación automatizada **no equivale a estas pruebas in-game**; la apariencia final debe evaluarse en Minecraft.
