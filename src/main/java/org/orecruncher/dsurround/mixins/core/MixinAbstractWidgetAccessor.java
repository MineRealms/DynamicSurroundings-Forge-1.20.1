package org.orecruncher.dsurround.mixins.core;

import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractWidget.class)
public interface MixinAbstractWidgetAccessor {

    @Accessor("height")
    void dsurround_setHeight(int height);

    @Accessor("height")
    int dsurround_getHeight();
}
