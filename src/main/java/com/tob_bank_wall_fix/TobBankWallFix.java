package com.tob_bank_wall_fix;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@PluginDescriptor(
        name = "ToB Bank Wall Fix",
        description = "Prevents clicking the upper floors at ToB bank to prevent unwanted pathing.",
        tags = {"tob", "wall", "fix", "hider", "bank"}
)
public class TobBankWallFix extends Plugin {

    public static final int[] TOB_TOP_LEFT = {3631, 3198};
    public static final int[] TOB_BOTTOM_RIGHT = {3698, 3240};

    private static final List<MenuAction> OBJECT_MENU_TYPES = ImmutableList.of(
            MenuAction.GAME_OBJECT_FIRST_OPTION,
            MenuAction.GAME_OBJECT_SECOND_OPTION,
            MenuAction.GAME_OBJECT_THIRD_OPTION,
            MenuAction.GAME_OBJECT_FOURTH_OPTION,
            MenuAction.GAME_OBJECT_FIFTH_OPTION
    );

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private RenderCallbackManager renderCallbackManager;

    @Inject
    private ObjectHider hider;

    private boolean loggingIn = false;

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
        if (event.getGameState() == GameState.LOGGING_IN) {
            loggingIn = true;
        }
    }

    // Forces a map load to show/hide objects
    private void reloadMap() {
        clientThread.invoke(() ->
        {
            if (client.getGameState() == GameState.LOGGED_IN) {
                client.setGameState(GameState.LOADING);
            }
        });
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (loggingIn) {
            loggingIn = false;
            reloadMap();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged e) {
        if (e.getGroup().equals("tobBankWallFix")) {
            reloadMap();
        }
    }

}
