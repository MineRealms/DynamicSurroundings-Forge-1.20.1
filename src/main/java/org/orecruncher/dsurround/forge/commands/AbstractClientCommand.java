package org.orecruncher.dsurround.forge.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.orecruncher.dsurround.lib.Library;

import java.util.function.Supplier;

abstract class AbstractClientCommand {

    protected int execute(CommandContext<CommandSourceStack> ctx, Supplier<Component> handler) {
        try {
            var result = handler.get();
            ctx.getSource().sendSuccess(() -> result, false);
            return 1;
        } catch (final Throwable t) {
            Library.LOGGER.error(t, "Error executing command");
            ctx.getSource().sendFailure(Component.literal(t.getMessage()));
            return 0;
        }
    }
}
