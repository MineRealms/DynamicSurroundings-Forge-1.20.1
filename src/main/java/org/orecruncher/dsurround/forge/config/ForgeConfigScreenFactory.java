package org.orecruncher.dsurround.forge.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.orecruncher.dsurround.gui.sound.IndividualSoundControlScreen;
import org.orecruncher.dsurround.lib.config.IScreenFactory;

public class ForgeConfigScreenFactory implements IScreenFactory<Screen> {

    @Override
    public Screen create(Minecraft client, Screen parent) {
        return new IndividualSoundControlScreen(parent, true);
    }
}
