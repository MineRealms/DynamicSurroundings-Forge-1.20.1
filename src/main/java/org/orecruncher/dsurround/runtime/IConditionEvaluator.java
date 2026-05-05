package org.orecruncher.dsurround.runtime;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.lib.scripting.Script;

@OnlyIn(Dist.CLIENT)
public interface IConditionEvaluator {
    boolean check(final Script conditions);

    Object eval(final Script conditions);
}
