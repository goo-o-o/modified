package com.goo.modified.client.events;

import com.goo.modified.client.registry.ModifiedKeybinds;
import com.goo.modified.common.Modified;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Modified.MOD_ID, value = Dist.CLIENT)
public class KeybindEvents {
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ModifiedKeybinds.SHOW_RARITY);
        event.register(ModifiedKeybinds.SHOW_STATS);
    }
}
