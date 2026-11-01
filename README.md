# Aetherium

Mod de contenido para Minecraft **1.16.5** usando **Minecraft Forge 36.2.42**.

## Estado del proyecto

Esta instantánea contiene el código fuente, recursos y documentación reunidos durante las seis entregas. **No es un JAR compilado**: la compilación real debe comprobarse en un equipo con JDK 8 de 64 bits y Gradle descargado por el wrapper.

## Obtener el código fuente

Descarga `Aetherium-source.tar.xz` desde este repositorio y extrae el archivo. En Windows puedes usar 7-Zip; en Linux:

```bash
tar -xf Aetherium-source.tar.xz
cd Aetherium
```

La extracción crea la carpeta `Aetherium/` con `build.gradle`, `gradlew` y `src/`.

## Compilar

Requisito: **JDK 8 de 64 bits**.

Windows:

```bat
gradlew.bat build
```

Linux:

```bash
chmod +x gradlew
./gradlew build
```

El artefacto esperado estará en `build/libs/`.

## Compatibilidad

- Minecraft 1.16.5 / Forge 36.2.42.
- Java 8.
- Curios API es opcional para los accesorios; el mod debe poder funcionar sin Curios.
- Rubidium y FerriteCore son optimizaciones opcionales, no dependencias obligatorias.

## Rendimiento

Consulta [OPTIMIZATION.md](Aetherium/OPTIMIZATION.md) después de extraer el archivo fuente.

## Licencia

Consulta el archivo `LICENSE` incluido en el paquete fuente.
