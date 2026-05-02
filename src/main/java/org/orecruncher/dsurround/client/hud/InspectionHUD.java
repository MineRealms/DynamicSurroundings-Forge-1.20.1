/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays debug information about the block or entity the player is looking at.
 * This is a simplified version that will be expanded as more systems are implemented.
 */
public class InspectionHUD implements IGuiOverlay {

    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BACKGROUND_COLOR = 0x80000000;

    private final List<String> gatherText = new ArrayList<>();
    private final Minecraft minecraft = Minecraft.getInstance();

    @Override
    public void doRender(final RenderGuiOverlayEvent.Post event) {
        // Only render if F3 debug screen is not active
        if (minecraft.options.renderDebug) {
            return;
        }

        final Player player = minecraft.player;
        if (player == null) {
            return;
        }

        // Check if player is looking at a block
        final HitResult hitResult = minecraft.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        final BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        final BlockPos pos = blockHitResult.getBlockPos();
        final Level level = player.level();
        final BlockState state = level.getBlockState(pos);

        // Gather information
        gatherText.clear();
        gatherText.add("Block: " + state.getBlock().getName().getString());
        gatherText.add("Position: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());

        // TODO: Add more information as systems are implemented:
        // - Biome information (requires BiomeInfo registry)
        // - Block effects (requires BlockInfo registry)
        // - Sound information (requires SoundEngine)
        // - Season information (already available via Capabilities)

        // Render the text
        renderText(event.getGuiGraphics());
    }

    private void renderText(final net.minecraft.client.gui.GuiGraphics guiGraphics) {
        if (gatherText.isEmpty()) {
            return;
        }

        final Font font = minecraft.font;
        final int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        final int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Position in the top-right corner
        final int margin = 5;
        int y = margin;

        // Find the longest line for background width
        int maxWidth = 0;
        for (String line : gatherText) {
            int width = font.width(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        final int boxWidth = maxWidth + 4;
        final int boxHeight = gatherText.size() * (font.lineHeight + 1) + 2;
        final int x = screenWidth - boxWidth - margin;

        // Draw background
        guiGraphics.fill(x - 2, y - 2, x + boxWidth, y + boxHeight, BACKGROUND_COLOR);

        // Draw text
        for (String line : gatherText) {
            guiGraphics.drawString(font, line, x, y, TEXT_COLOR);
            y += font.lineHeight + 1;
        }
    }
}
