package com.railshooter.command;

import com.railshooter.entities.Player;

/**
 * PATTERN: Command — команда выстрела
 */
public class ShootCommand implements Command {
    private final Player player;
    private final float targetX, targetY;

    public ShootCommand(Player player, float targetX, float targetY) {
        this.player = player;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public void execute() {
        player.shoot(targetX, targetY);
    }
}
