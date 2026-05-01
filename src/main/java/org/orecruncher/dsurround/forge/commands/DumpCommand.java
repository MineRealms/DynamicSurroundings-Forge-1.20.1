package org.orecruncher.dsurround.forge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.orecruncher.dsurround.commands.DumpCommandHandler;

class DumpCommand extends AbstractClientCommand {

    private static final String COMMAND = "dsdump";

    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal(COMMAND)
                .then(Commands.literal("biomes").executes(ctx -> this.execute(ctx, DumpCommandHandler::dumpBiomes)))
                .then(Commands.literal("sounds").executes(ctx -> this.execute(ctx, DumpCommandHandler::dumpSounds)))
                .then(Commands.literal("dimensions").executes(ctx -> this.execute(ctx, DumpCommandHandler::dumpDimensions)))
                .then(Commands.literal("blocks").executes(ctx -> this.execute(ctx, () -> DumpCommandHandler.dumpBlocks(false))))
                .then(Commands.literal("blockstates").executes(ctx -> this.execute(ctx, DumpCommandHandler::dumpBlockState)))
                .then(Commands.literal("items").executes(ctx -> this.execute(ctx, DumpCommandHandler::dumpItems)))
                .then(Commands.literal("tags").executes(ctx -> this.execute(ctx, DumpCommandHandler::dumpTags)))
                .then(Commands.literal("di").executes(ctx -> this.execute(ctx, DumpCommandHandler::dumpDIRegistrations))));
    }
}
