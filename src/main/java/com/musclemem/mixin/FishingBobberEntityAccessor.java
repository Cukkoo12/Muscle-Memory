package com.musclemem.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingHook.class)
public interface FishingBobberEntityAccessor {
    @Accessor("nibble")
    int getHookCountdown();

    @Accessor("nibble")
    void setHookCountdown(int value);

    @Accessor("timeUntilLured")
    int getWaitCountdown();

    @Accessor("timeUntilLured")
    void setWaitCountdown(int value);
}