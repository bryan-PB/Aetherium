# Compatibilidad con Curios (Forge 1.16.5)

## Versiones objetivo

- Minecraft: `1.16.5`
- Forge: `36.2.42`
- Curios API: `1.16.5-4.1.0.0`
- Mod ID: `curios`

## Integración implementada en la Parte 3

- Curios se declara como dependencia **opcional** en `mods.toml`.
- La API de Curios se usa como dependencia `compileOnly`; nunca se empaqueta dentro del JAR de Aetherium.
- `AetheriumAccessoryItem` implementa `ICurioItem` con `@Optional.Interface`; los callbacks `canEquipFromUse` y `curioTick` están protegidos con `@Optional.Method`. Con Curios, el accesorio puede equiparse directamente desde la mano; sin Curios, el clic derecho activa su habilidad alternativa.
- Se solicitan las ranuras comunes `necklace` y `ring` durante `InterModEnqueueEvent`, solo si Curios está instalado. La petición se crea por reflexión para que la clase de integración no necesite enlazar clases de Curios al arrancar sin esa dependencia.
- Tags de objetos:
  - `data/curios/tags/items/necklace.json` → `aetherium_amulet`
  - `data/curios/tags/items/ring.json` → `aetherium_ring`
- El amuleto da Resistencia I mientras está equipado.
- El anillo da Velocidad I y Visión Nocturna mientras está equipado.
- Los efectos pasivos se actualizan cada 40 ticks y usan duraciones solapadas, para evitar cálculos y sincronización cada tick.
- Sin Curios, ambos accesorios siguen funcionando con clic derecho y un enfriamiento de 90 segundos. Los nombres de ranura se traducen al español e inglés.

## Dependencia de desarrollo

El `build.gradle` usa el artefacto API clasificado:

```gradle
compileOnly fg.deobf("top.theillusivec4.curios:curios-forge:${curios_version}:api")
```

La versión está centralizada en `gradle.properties`. Para probar las ranuras en el juego, instala Curios API `1.16.5-4.1.0.0` en la carpeta `mods` junto con Aetherium.

Curios es opcional: un usuario que no lo instale podrá iniciar Minecraft y seguir usando el amuleto y el anillo desde la mano.
