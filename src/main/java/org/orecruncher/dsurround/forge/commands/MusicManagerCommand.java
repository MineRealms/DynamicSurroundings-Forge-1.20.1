package org.orecruncher.dsurround.forge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.orecruncher.dsurround.commands.MusicManagerCommandHandler;

class MusicManagerCommand extends AbstractClientCommand {

    private static final String COMMAND = "dsmusic";

    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal(COMMAND)
                .then(Commands.literal("reset").executes(ctx -> this.execute(ctx, MusicManagerCommandHandler::reset))));
    }
}
