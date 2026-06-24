package com.goo.modified.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;

public class ClientProxy {
    public static RegistryAccess getClientRegistryAccess() {
        if (Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.registryAccess();
        }
        return null;
    }
}