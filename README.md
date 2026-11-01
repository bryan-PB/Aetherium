# Aetherium

Proyecto de mod de contenido para Minecraft **1.16.5** con **Minecraft Forge 36.2.42**.

## Código fuente

El paquete fuente completo de las seis entregas está almacenado en este repositorio como nueve partes binarias:

- `Aetherium-source.tar.xz.part001` a `Aetherium-source.tar.xz.part009`

Descarga las nueve partes (puedes usar **Code → Download ZIP** para descargar todo el repositorio) y colócalas en una misma carpeta.

### Reconstruir el archivo

Linux/macOS:

```bash
cat Aetherium-source.tar.xz.part* > Aetherium-source.tar.xz
tar -xf Aetherium-source.tar.xz
cd Aetherium
```

Windows (CMD):

```bat
copy /b Aetherium-source.tar.xz.part001+Aetherium-source.tar.xz.part002+Aetherium-source.tar.xz.part003+Aetherium-source.tar.xz.part004+Aetherium-source.tar.xz.part005+Aetherium-source.tar.xz.part006+Aetherium-source.tar.xz.part007+Aetherium-source.tar.xz.part008+Aetherium-source.tar.xz.part009 Aetherium-source.tar.xz
```

Después extrae `Aetherium-source.tar.xz` con 7-Zip. Se creará la carpeta `Aetherium/` con los archivos Gradle y `src/main/`.

## Compilar

Requisito previsto: **JDK 8 de 64 bits**. El proyecto todavía necesita una compilación real en ese entorno; el paquete fuente no es un JAR ya compilado.

Windows:

```bat
gradlew.bat build
```

Linux/macOS:

```bash
chmod +x gradlew
./gradlew build
```

Si la compilación termina correctamente, el JAR estará en `Aetherium/build/libs/`.

## Compatibilidad

- Minecraft 1.16.5 / Forge 36.2.42.
- Java 8.
- Curios API es opcional para los accesorios.
- Rubidium y FerriteCore son optimizaciones opcionales, no dependencias obligatorias.

## Rendimiento

El proyecto apunta a limitar IA, partículas y búsquedas periódicas para equipos modestos. La experiencia real debe validarse dentro del juego.

## Estado

- [x] Paquete de código fuente y recursos de las seis entregas añadido al repositorio en nueve partes.
- [ ] Reconstrucción local del archivo y compilación Gradle con JDK 8.
- [ ] Pruebas dentro de Minecraft 1.16.5.
- [ ] Publicación de un JAR compilado y verificado.

No se debe considerar estable ni listo para jugar hasta completar esas pruebas.
