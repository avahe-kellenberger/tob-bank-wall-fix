package com.tob_bank_wall_fix;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
        name = "ToB Bank Wall Fix",
        description = "Prevents clicking the upper floors at ToB bank to prevent unwanted pathing.",
        tags = {"tob", "wall", "fix", "hider", "bank"}
)
public class TobBankWallFix extends Plugin {

    public static final int[] TOB_TOP_LEFT = {3631, 3198};
    public static final int[] TOB_BOTTOM_RIGHT = {3698, 3240};

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private RenderCallbackManager renderCallbackManager;

    @Inject
    private ObjectHider hider;

    private boolean loggingIn = false;
    private boolean deferredReload = false;

    @Provides
    TobBankWallFixConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TobBankWallFixConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        renderCallbackManager.register(hider);
        reloadMap();
    }

    @Override
    protected void shutDown() throws Exception {
        renderCallbackManager.unregister(hider);
        reloadMap();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        switch (event.getGameState()) {
            case LOGGING_IN:
                loggingIn = true;
                break;
            case LOGGED_IN:
                removeUpperFloors();
                break;
            case LOADING:
                final WorldPoint loc = client.getLocalPlayer().getWorldLocation();
                if (loc.getX() >= TOB_TOP_LEFT[0] && loc.getX() <= TOB_BOTTOM_RIGHT[0]) {
                    if (loc.getY() >= TOB_TOP_LEFT[1] && loc.getY() <= TOB_BOTTOM_RIGHT[1]) {
                        // Player is in the ToB area, need to reload
                        deferredReload = true;
                    }
                }
                break;
        }
    }

    // Forces a map load to show/hide objects
    private void reloadMap() {
        clientThread.invoke(() ->
        {
            if (client.getGameState() == GameState.LOGGED_IN) {
                client.setGameState(GameState.LOADING);
                deferredReload = false;
            }
        });
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (loggingIn) {
            loggingIn = false;
            reloadMap();
        }
        if (deferredReload) {
            reloadMap();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged e) {
        if (e.getGroup().equals("tobBankWallFix")) {
            reloadMap();
        }
    }

    private void removeUpperFloors() {
        Scene scene = client.getTopLevelWorldView().getScene();
        Tile[][][] tiles = scene.getTiles();

        for (int x = TOB_TOP_LEFT[0]; x < TOB_BOTTOM_RIGHT[0]; x++) {
            for (int y = TOB_TOP_LEFT[1]; y < TOB_BOTTOM_RIGHT[1]; y++) {
                LocalPoint p = LocalPoint.fromWorld(scene, x, y);
                if (p == null) continue;
                final int px = p.getSceneX();
                final int py = p.getSceneY();
                Tile tile1 = tiles[1][px][py];
                Tile tile2 = tiles[2][px][py];
                Tile tile3 = tiles[3][px][py];

                if (tile1 != null)
                    scene.removeTile(tile1);

                if (tile2 != null)
                    scene.removeTile(tile2);

                if (tile3 != null)
                    scene.removeTile(tile3);

            }
        }
    }

}
