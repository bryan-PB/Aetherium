# Kryzorae — Documento maestro: Ciudadela de la Grieta
**Versión del diseño:** 1.0 · **Objetivo:** Minecraft Java 1.16.5 / Forge 36.2.42 / Java 8 · **ID heredado del mod:** `aetherium`

> **Estado:** **fase 1 implementada en código: registro nativo de estructura Forge 1.16.5, generación natural en chunks nuevos del Overworld, siete plantillas conectadas por jigsaw y tres tablas de botín**. Compila en CI; falta probar la aparición y el diseño en Minecraft. Los tres enemigos, minijefe y Custodio **aún no están implementados**. Los valores de combate del documento son objetivos de balance, no estadísticas registradas. Los valores de combate aquí son objetivos de balance, no estadísticas ya registradas.

## 1. Identidad y papel en el mod

Una fortaleza en ruinas de origen desconocido, erigida para contener una fisura subterránea. Presente **únicamente en el Overworld**, de aspecto antiguo, oscuro, con piedra ennegrecida, acentos violetas y lámparas de luz cian. No es una dimensión ni contenido del End.

**Curva de dificultad:** exploración con Brumacero/Umbracita; salas profundas requieren Aetherium y preparación. El Guardián de la Grieta del mod tiene **450 HP** y el Verdugo Alado **650 HP** en su versión actual. El Custodio proyectado tendrá **520 HP**: combate técnico intermedio, no un aumento arbitrario de daño.

**Principios innegociables:** siluetas y sprites reales en el JAR; ataques telegrafiados; recompensas no basadas solo en daño; no bloquear mundos antiguos; evitar cascadas de generación de chunks; sin partículas ni bloques que procesen constantemente; ninguna dependencia obligatoria con otro mod.

## 2. Plano narrativo y bucle jugable

```text
RUTA PRINCIPAL
PORTÓN RUINOSO (entrada) → PASILLO → PATIO → SEGUNDA ALA
                                       ├─ Cuartel / botín básico
                                       ├─ Archivo / pistas y botín opcional
                                       └─ Cripta / recurso ritual
SEGUNDA ALA → SALA DE VIGILANCIA → CENTINELA DEL UMBRAL
                      ↓                    │
            desvío opcional                 └─ Sello del Umbral (garantizado)
                                            ↓
                                   PUERTA DEL SANTUARIO
                                            ↓
                               ARENA DEL CUSTODIO DE LA GRIETA
                                            ↓
                                Cámara del tesoro y atajo de salida
```

**Flujo de una visita:** descubrir ruinas → inspeccionar para identificar el camino → enfrentar guardias y arqueros → investigar cuartos opcionales para materiales → luchar contra el Centinela → usar su sello en el santuario → enfrentar al Custodio → obtener reliquia y salida rápida.

No depender de encontrar al azar una llave: **el sello es un drop garantizado del minijefe**. La puerta no debe consumirlo para siempre; si el jugador muere tras usarlo, la partida debe poder continuar. Botín lateral no puede contener sellos ni materiales exclusivos de los dos jefes ya existentes.

## 3. Módulos espaciales

El generador incluye 7 piezas conectadas actualmente. Más adelante la ruta final usa hasta 12 tipos, con máximo **9 módulos físicos por ciudadela** de una selección ponderada; no se intenta colocar los 12 en una sola estructura.

| Identificador de plantilla | Dimensiones X×Y×Z | Función | Estado de implementación |
|---|---:|---|---|
| `citadel/gateway` | 19×11×15 | Portón, bienvenida y botín pequeño | **Generación natural** |
| `citadel/corridor` | 11×9×17 | Corredor y desvío oriental a cripta | **Generación natural** |
| `citadel/guardhouse` | 17×10×17 | Cuartel y desvío occidental a forja | **Generación natural** |
| `citadel/archive` | 17×11×17 | Libros, prismalita y pistas | **Generación natural** |
| `citadel/arena_shell` | 27×15×27 | Arena descubierta, aún sin minijefe ni boss | **Generación natural** |
| `citadel/courtyard` | 27×12×27 | Nodo de rutas, grupos pequeños | Fase 1 |
| `citadel/crypt` | 17×11×17 | Ala ritual conectada al corredor | **Generación natural** |
| `citadel/watchtower` | 11×24×11 | Visibilidad, arquero | Fase 1 |
| `citadel/forge` | 17×10×17 | Ala de herrería conectada al cuartel | **Generación natural** |
| `citadel/prison` | 17×10×17 | Enemigo Eco, exploración | Fase 2 |
| `citadel/threshold` | 23×15×23 | Minijefe Centinela | Fase 3 |
| `citadel/sanctum` | 31×19×31 | Arena del Custodio | Fase 4 |

**Regla de conexión:** cada acceso principal libre tiene **3 bloques de ancho × 4 de alto**; enlazar por anclajes registrados y orientación, **nunca ensamblar plantillas aleatoriamente sobre chunks vecinos sin un plan de bounding boxes**. Comprobar colisiones, suelo y que exista una ruta continua hasta la salida. La arena nunca se monta sobre lava/agua profunda ni se abandona flotando en el aire.

**Paleta de bloques fase 0:** ladrillos de piedra, ladrillos agrietados, ladrillos de piedra cincelados, ladrillos de piedra negra pulida, obsidiana llorosa en puntos focales, vidrio morado, bibliotecas y luces de alma. Todo estático, sin tile entities persistentes que hagan tick. Solo los cofres conservan su bloque NBT de LootTable.

## 4. Aparición natural — fase posterior

- **Overworld** exclusivamente: sin Nether ni End; excluir océanos, ríos y bordes de bioma peligrosos inicialmente.
- Biomas candidatos: taiga, bosque, bosque oscuro, colinas templadas. Filtrado configurable, no invasivo.
- Frecuencia **implementada y configurable**: separación por cuadrícula de **26 chunks** (no 26 bloques), separación mínima de **10 chunks**; usa un `StructureSeparationSettings` nativo en Forge 1.16.5. Es un candidato por región, no garantiza una ciudadela en cada cuadrícula. Datos TOML: `citadelEnabled`, `citadelSpacingChunks`, `citadelMinSeparationChunks`.
- Altura: adaptar al terreno con soporte visible, sin destruir aldeas, portales ni estructuras de otros mods. Evitar cascadas al intentar colocar múltiples chunks fuera del área generada.
- **Implementado:** estructura auténtica de Forge 1.16.5 registrada en `ForgeRegistries.STRUCTURE_FEATURES`, `WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE`, `Structure.STRUCTURES_REGISTRY`, `FlatGenerationSettings.STRUCTURE_FEATURES` y `DimensionStructuresSettings`; ensamblaje con `JigsawManager`, siete plantillas NBT originales y siete pools JSON. Ningún `PlayerTickEvent`.
- Mundo existente: solo terreno **nuevo**; ningún escaneo retroactivo, inserción forzada o migración de chunks. Opción para desactivar la ciudadela sin alterar minerales existentes.

## 5. Enemigos, telegráficos y dificultad objetivo

| Mob | HP | Daño melee/proyectil base | Comportamiento único | Límites |
|---|---:|---:|---|---|
| Vigía de la Grieta | 32 | 6 | Golpe con escudo; 12–16 ticks de aviso y recuperación clara | máximo 3 activos por sala |
| Arquero Umbral | 24 | 4 | Disparo visible con preparación de 20 ticks; sin disparo continuo | máximo 2 por sala |
| Eco de la Grieta | 22 | 5 | Reposición corta con aviso, no teletransporte permanente | máximo 1 por sala |
| **Centinela del Umbral** (minijefe) | 170 | 8 | Barrido, golpe al suelo y embestida corta; ventanas 18–26 ticks | 1 por ciudadela |
| **Custodio de la Grieta** (jefe) | **520** | 10 | Espadón, corte energético y respuesta al posicionamiento | 1 por ciudadela |

Son valores de diseño sujetos a ajustes después de pruebas reales con el daño de las armas y las armaduras actuales. Mantener inmunidades y antiexploits comedidos: evitar el autobombo de la espada carmesí, **sin** hacer inmune al jefe al jugador.

### Centinela — máquina de estados
- **Patrulla / guardia:** aggro dentro del área del encuentro, sin persecución interminable.
- **Barrido:** anuncio visual 18 ticks → ataque frontal de arco acotado → recuperación 18 ticks.
- **Mazazo:** anuncio 26 ticks → pulso de suelo corto → recuperación 24 ticks.
- **Embestida:** desplazamiento corto con velocidad limitada, sin aceleración acumulativa; respetar paredes y agua.
- **Recompensa garantizada:** `aetherium:citadel_threshold_seal` (ítem **planificado, no registrado todavía**).

### Custodio — fases propuestas
1. **100–60 % HP:** cuerpo a cuerpo, cortes direccionales, una onda frontal anunciada.
2. **60–25 % HP:** combina un corte energético lento y una carrera corta; deja al menos 18 ticks de recuperación después de ataques grandes.
3. **25–0 % HP:** mejora el orden/velocidad de ataques existentes, sin spam de ilusiones o mobs. Máximo 2 réplicas temporales si más adelante se implementan; no pueden sobrevivir indefinidamente.

El boss nunca dispara proyectiles invisibles: la malla/textura o entidad renderizada debe verificarse en cámara, con hitbox coincidente. No reutilizar efectos visuales que no representen la mecánica (ej. cristales por defecto). Los ataques deben ser aprendibles antes de aumentar el daño.

## 6. Loot y objetos

**Tres cofres de prototipo integrados en las plantillas fase 0:**
- `aetherium:chests/citadel_entrance`: comida, libros, hierro, pequeña posibilidad de Brumacero bruto.
- `aetherium:chests/citadel_guardhouse`: lingotes Brumacero, Umbracita bruta y material vanilla utilitario.
- `aetherium:chests/citadel_archive`: papel/libros, polvo etéreo, algunos fragmentos de Prismalita.

Prohibido introducir mediante cofres comunes `rift_core`, `astral_heart`, `astral_ingot` o sellos invocadores. Esa progresión ya corresponde a los jefes actuales.

**Materiales futuros (sin duplicar recursos existentes):**
- `citadel_threshold_seal`: una llave **persistente, no consumible**, obtenida del Centinela; no confundir con el `rift_sigil` ya existente.
- `citadel_shard`: fragmento de runa propio de la Ciudadela, usado en mejoras, no como moneda sin utilidad.
- `warden_heart`: núcleo de miniboss para receta auxiliar, no fuente de minerales infinitos.

**Arma especial proyectada — Hoja del Umbral:** en vez de aumentar daño bruto, defensa temporal de ciertos proyectiles mediante uso con enfriamiento. Duración máxima de ventana **6 ticks**, cooldown inicial propuesto **100 ticks**, lista explícita de proyectiles que sí pueden desviarse, sin reflejar fuego amigo o explosiones. Validación exclusivamente en servidor, con aviso audiovisual breve.

**Recompensas del Custodio:** núcleo de ciudadela único, mejora cosmética funcional, 1 arma/reliquia; nunca reemplazar automáticamente los dos tiers endgame actuales.

## 7. Rendimiento: presupuestos de ingeniería (criterios de diseño)

| Elemento | Presupuesto |
|---|---|
| Generación | máximo **9** módulos de entre 12 tipos por ciudadela; trabajo limitado a worldgen de nuevos chunks |
| Dimensiones | cada NBT ≤31×24×31; total final sujeto a bounding box y prueba de colisiones |
| Entidades activas | 6 enemigos normales por encuentro como máximo, 1 miniboss o boss; no spawning por tick en chunks inactivos |
| Proyectiles | máximo 12 simultáneos **dentro de la ciudadela**; cada uno con vida acotada y estado de muerte limpio |
| Ilusiones | máximo 2 por jefe, TTL explícito; no bucles de búsqueda continua |
| Partículas | ráfagas acotadas al lanzamiento/impacto; cero emisiones decorativas permanentes |
| IA | pathfinding estándar limitado; selección de ataques por cooldown y evento; no escaneos cúbicos del mundo |
| Texturas | bloques estáticos 16–32px, ítems 32–64px si la legibilidad lo exige; sin shaders obligatorios |
| Guardado/red | sin datos persistentes por bloque normal; sincronizar solo estado de combate/puerta relevante |
| Objetivo de benchmark | comparar MSPT, memoria y tiempos de generación con/sin mod en **misma semilla, misma distancia y mismo hardware**; sin prometer números de FPS antes de medir |

**Riesgos identificados y mitigación:**
1. Cascadas de generación: no colocar manualmente habitaciones en chunks aún no generados durante `BiomeLoadingEvent`.
2. Cofres sin LootTable o crashes de NBT: tests para carga, tamaño, links y paletas.
3. Atascos de enemigos en agua: comportamiento de escape, no congelación de IA.
4. Pérdida de sello y puerta cerrada: flags de progreso o sello retornable y posibilidad de reintentar el minijefe.
5. Copias de jefes infinitas o ataques invisibles: proyectiles visibles, TTL y hitbox test.
6. Incompatibilidad con otros mods: biomas configurables, no reemplazar registros ni forzar cambios de mapa.

## 8. Entregas y criterios de aceptación

| Hito | Entregable mínimo | Cómo se prueba | Estado |
|---|---|---|---|
| **0. Documento y piezas NBT** | Documento, 7 plantillas reales en JAR y 3 loot tables; generación automática y checks CI | CI comprobó NBT, archivos JSON, cofres, firma y espacio disponible | **Implementado** |
| **1. Generación natural** | Estructura registrable `/locate aetherium:citadel`, conectores, rareza configurable, 7 salas | CI #254 compiló y verificó pools y NBT; faltan semillas, terrenos y prueba real con mods | **Código completo, falta prueba in-game** |
| **2. Primeros enemigos** | Vigía, Arquero, Eco, iconos y render visible | Arena creativa, pruebas de agua, tiro y límite de proyectiles | Pendiente |
| **3. Puerta y minijefe** | Centinela, sello garantizado, estado de apertura | Morir/reentrar, multijugador, reinicio del servidor | Pendiente |
| **4. Custodio** | 2–3 fases, animaciones, sonido y drops | 20 enfrentamientos, balance con tres tiers y telegráficos | Pendiente |
| **5. Recompensas** | Hoja, reliquias y recetas sin exploits | Validación de NBT/recetas, supervivencia real | Pendiente |
| **6. Pulido** | Ajustes visuales, audio, traducciones y perfilado | Comparar FPS/MSPT y arte in-game | Pendiente |

**Criterio global de terminado:** un mundo creado con el JAR anterior debe seguir abriendo; todos los recursos y modelos apuntan a rutas existentes; no se tocan los minerales ni bosses actuales salvo correcciones explícitas; arte mostrado = PNG y modelo que usa el JAR; todas las compilaciones deben pasar CI y el usuario verifica el diseño en Minecraft.

## 9. Referencias técnicas para Forge 1.16.5

- [Primer técnico de NeoForged para 1.16.5](https://github.com/neoforged/.github/blob/main/primers/1.16.5/index.md) — precauciones de registro de estructuras.
- Las plantillas reutilizables siguen el formato de NBT de bloques de estructura de Java Edition 1.16.5. **No** emplear el formato `mcstructure` de Bedrock ni registros de estructuras 1.19+.
- La configuración de biomas del mod reside en `src/main/java/com/aetherium/aetherium/world/AetheriumWorldGen.java` y las opciones comunes en `AetheriumConfig.java`. No se extenderán con funciones de estructuras hasta que las piezas sean verificables y se planifique el registro apropiado.

## 10. Estado de implementación del sistema de estructuras

Los 7 módulos comparten un camino principal de 5 habitaciones, con dos desvíos: cripta al este del corredor y forja al oeste del cuartel. Dos conectores (entrada/salida) se sustituyen por aire durante la generación. La entrada usa torres de piedra negra y almenas; el interior tiene detalles estáticos. La estructura se genera únicamente en bosques, llanuras, taigas y colinas terrestres de nuevas regiones del Overworld, sin modificar chunks previos. La dificultad final (enemigos, puertas selladas y Custodio) pertenece a los siguientes hitos, no a la versión actual.
