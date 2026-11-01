# Aetherium

Proyecto de mod de contenido para Minecraft **1.16.5** con **Minecraft Forge 36.2.42**.

## Estado del repositorio

Este repositorio está creado como **público**. Por ahora contiene esta documentación inicial; el paquete completo de código fuente y recursos todavía no se ha subido. No descargues ni intentes compilar este repositorio esperando encontrar ya todos los archivos del mod.

El proyecto preparado durante las seis entregas está disponible como paquete ZIP en la conversación de ChatGPT, pero aún debe transferirse a este repositorio.

## Requisitos previstos

- Minecraft 1.16.5.
- Minecraft Forge 36.2.42.
- JDK 8 de 64 bits.
- Curios API opcional para accesorios.
- Rubidium y FerriteCore opcionales para optimización.

## Compilación

Cuando el código fuente esté cargado, desde la carpeta raíz del proyecto:

Windows:

```bat
gradlew.bat build
```

Linux:

```bash
chmod +x gradlew
./gradlew build
```

El JAR, si la compilación termina correctamente, aparecerá en `build/libs/`.

## Rendimiento

El objetivo es mantener bajo el consumo de CPU, memoria y partículas para equipos modestos. La configuración definitiva debe probarse dentro del juego.

## Aviso

No existe todavía un JAR compilado y probado publicado en este repositorio.
