package com.tob_bank_wall_fix;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
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

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    private boolean loggingIn = false;

    @Override
    protected void startUp() throws Exception {
        reloadMap();
    }

    @Override
    protected void shutDown() throws Exception {
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

    private static final List<MenuAction> OBJECT_MENU_TYPES = ImmutableList.of(
            MenuAction.GAME_OBJECT_FIRST_OPTION,
            MenuAction.GAME_OBJECT_SECOND_OPTION,
            MenuAction.GAME_OBJECT_THIRD_OPTION,
            MenuAction.GAME_OBJECT_FOURTH_OPTION,
            MenuAction.GAME_OBJECT_FIFTH_OPTION
    );

    @Subscribe
    public void onGameTick(GameTick event) {
        if (loggingIn) {
            loggingIn = false;
            reloadMap();
        }
    }

    @Subscribe
    public void onClientTick(ClientTick tick) {
        if (client.isMenuOpen()) {
            return;
        }

        for (MenuEntry menuEntry : client.getMenu().getMenuEntries()) {
            MenuAction type = menuEntry.getType();

            // Check if this is an object interaction (including walls)
            if (OBJECT_MENU_TYPES.contains(type)) {
                int x = menuEntry.getParam0();
                int y = menuEntry.getParam1();
                int id = menuEntry.getIdentifier();

                // Adjust coordinates for extended scene
                x += (Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2;
                y += (Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2;

                WorldView view = client.getTopLevelWorldView();
                Scene scene = view.getScene();
                Tile[][][] tiles = scene.getExtendedTiles();

                for (int plane = 0; plane < 4; plane++) {
                    Tile tile = tiles[plane][x][y];
                    if (tile == null) continue;
                    WallObject wallObject = tile.getWallObject();
                    if (wallObject != null && wallObject.getId() == id) {
                        if (wallObject.getPlane() > view.getPlane()) {
                            menuEntry.setDeprioritized(true);
                        }
                        break;
                    }
                }
            }
        }
    }

}
