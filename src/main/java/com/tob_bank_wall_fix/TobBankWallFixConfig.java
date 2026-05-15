package com.tob_bank_wall_fix;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tobBankWallFix")
public interface TobBankWallFixConfig extends Config {
    @ConfigItem(
            keyName = "hideUpperFloors",
            name = "Hide Upper Floors",
            description = "Hides the upper floors at ToB bank"
    )
    default boolean hideUpperFloors() {
        return false;
    }
}
