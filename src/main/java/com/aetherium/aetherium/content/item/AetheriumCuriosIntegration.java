package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.AetheriumMod;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Requests Curios' common preset slots only when Curios is installed.
 * Reflection keeps this setup class loadable in installations without Curios.
 */
public final class AetheriumCuriosIntegration {
    private AetheriumCuriosIntegration() {
    }

    public static void onInterModEnqueue(InterModEnqueueEvent event) {
        if (!ModList.get().isLoaded("curios")) {
            return;
        }

        sendSlotRequest("necklace");
        sendSlotRequest("ring");
    }

    private static void sendSlotRequest(String slotId) {
        InterModComms.sendTo("curios", "register_type", () -> createSlotMessage(slotId));
    }

    private static Object createSlotMessage(String slotId) {
        try {
            Class<?> builderClass = Class.forName(
                    "top.theillusivec4.curios.api.SlotTypeMessage$Builder", true,
                    AetheriumCuriosIntegration.class.getClassLoader());
            Constructor<?> constructor = builderClass.getConstructor(String.class);
            Object builder = constructor.newInstance(slotId);
            Method sizeMethod = builderClass.getMethod("size", int.class);
            sizeMethod.invoke(builder, 1);
            return builderClass.getMethod("build").invoke(builder);
        } catch (ReflectiveOperationException exception) {
            AetheriumMod.LOGGER.warn("Could not request the Curios '{}' slot. Accessories will remain usable via right-click.",
                    slotId, exception);
            return null;
        }
    }
}
