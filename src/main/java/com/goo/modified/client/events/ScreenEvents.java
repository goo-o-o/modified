package com.goo.modified.client.events;

import com.goo.modified.client.gui.reforge.ReforgingScreen;
import com.goo.modified.common.Modified;
import com.goo.modified.common.registry.ModifiedMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Modified.MOD_ID, value = Dist.CLIENT)
public class ScreenEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                ModifiedMenuTypes.REFORGING.get(),
                ReforgingScreen::new
        );
    }
}