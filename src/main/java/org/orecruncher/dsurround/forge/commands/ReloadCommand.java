package org.orecruncher.dsurround.forge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.orecruncher.dsurround.commands.ReloadCommandHandler;

class ReloadCommand extends AbstractClientCommand {

    private static final String COMMAND = "dsreload";

    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal(COMMAND).executes(this::execute));
    }

    public int execute(CommandContext<CommandSourceStack> ctx) {
        return this.execute(ctx, ReloadCommandHandler::execute);
    }
}
