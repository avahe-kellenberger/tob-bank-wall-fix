package com.tob_bank_wall_fix;

import net.runelite.api.Scene;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.RenderCallback;

import javax.inject.Inject;

import static com.tob_bank_wall_fix.TobBankWallFix.TOB_BOTTOM_RIGHT;
import static com.tob_bank_wall_fix.TobBankWallFix.TOB_TOP_LEFT;

public class ObjectHider implements RenderCallback {

    @Inject
    private TobBankWallFixConfig config;

    @Inject
    ObjectHider() {
    }

    @Override
    public boolean drawObject(Scene scene, TileObject object) {
        if (config.hideUpperFloors() && object.getPlane() > 0) {
            WorldPoint p = object.getWorldLocation();
            if (p.getX() >= TOB_TOP_LEFT[0] && p.getX() <= TOB_BOTTOM_RIGHT[0]) {
                if (p.getY() >= TOB_TOP_LEFT[1] && p.getY() <= TOB_BOTTOM_RIGHT[1]) {
                    return false;
                }
            }
        }
        return RenderCallback.super.drawObject(scene, object);
    }

}
