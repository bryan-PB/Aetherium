# Audio de Aetherium (Forge 1.16.5)

## Identidad sonora

Aetherium usa sonidos creados mediante síntesis procedural original en lugar de copiar
sonidos de Enderman, Ravager, gólem de hierro, zorro u otras criaturas de Minecraft.
La biblioteca contiene 35 eventos: 6 eventos históricos reconstruidos y otros 29
con funciones específicas (ambiente, transformación, herida, muerte, preparación,
impacto, láser, embestida, ilusión, voces de las criaturas, armaduras y utilidades).

Los efectos tienen envolventes de entrada y salida para evitar clics y cortes bruscos.
Los avisos de ataques y sus impactos utilizan muestras diferentes: volver a iniciar
el mismo pulso varias veces en un combo podía enmascarar el resultado sonoro.

## Compilación y recursos

Los 6 archivos OGG antiguos del repositorio incluyen al menos un archivo
`aetherium_blade_hit.ogg` con CRC inválido. En la compilación automática se regeneran
los 35 sonidos de forma determinista y se sobrescriben los antiguos **antes** de que
Gradle empaquete el mod. El flujo valida con `ffprobe` que todos los eventos de
`sounds.json` apunten a un audio decodificable y luego comprueba el contenido del JAR.

Para una compilación manual, después de preparar y extraer las fuentes del proyecto,
se necesitan **Python 3 y FFmpeg con codificador libvorbis**:

```bash
python3 tools/generate_aetherium_sounds.py
gradle build --no-daemon
```

No empaquetes directamente los seis OGG antiguos sin regenerarlos; no son una
referencia fiable. El archivo `tools/generate_aetherium_sounds.py` contiene las
fórmulas y parámetros de todas las pistas; no requiere bibliotecas de Python extra.

## Revisión dentro de Minecraft (pendiente)

- Escuchar la carga del láser (~1,3 segundos) y el disparo (~1 segundo)
  sin interrupciones artificiales ni sonidos de otros mobs.
- Escuchar una transformación de fase de cada jefe hasta su final natural.
- Confirmar que la espada del Guardián distingue preparación, carga y barrido,
  sin sonidos de jugador, Ravager o gólem.
- Probar varios golpes simultáneos en multijugador: los proyectiles solo hacen
  ruido de impacto cuando el objetivo recibe daño.
- Escuchar por separado voces de Devorador, Guardián, Cazador de la Brecha y
  Lumivelo; comprobar equilibrio de volumen con música y sonidos del juego.
- Comprobar que las ilusiones se manifiestan y se disipan con efectos reconocibles.
- Equipar armaduras, activar accesorios, bengalas e imanes e invocar jefes: cada
  acción debe utilizar un efecto de Aetherium, sin el de Netherite, beacon o portal.
- Probar las opciones de subtítulos con los idiomas español de Chile, España,
  México y Argentina.

Compilar correctamente certifica recursos y código, **no** sustituye la
verificación auditiva en el juego real.
