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

package org.orecruncher.dsurround.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.orecruncher.dsurround.lib.logging.IModLog;
import org.orecruncher.dsurround.lib.Library;
import org.orecruncher.dsurround.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages custom shader programs for Dynamic Surroundings.
 * Provides a simplified interface for loading and using GLSL shaders.
 */
@OnlyIn(Dist.CLIENT)
public class ShaderManager {

    private static final IModLog LOGGER = Library.LOGGER;
    private static final Map<String, ShaderProgram> SHADERS = new HashMap<>();

    /**
     * Aurora shader program
     */
    public static ShaderProgram AURORA;

    /**
     * Initialize all shader programs.
     * Should be called during client setup.
     */
    public static void initialize() {
        LOGGER.info("Initializing shader programs");

        try {
            AURORA = registerShader("aurora",
                new ResourceLocation(Constants.MOD_ID, "shaders/aurora.vert"),
                new ResourceLocation(Constants.MOD_ID, "shaders/aurora.frag"));

            LOGGER.info("Successfully initialized %d shader programs", SHADERS.size());
        } catch (Exception e) {
            LOGGER.error(e, "Failed to initialize shader programs");
        }
    }

    /**
     * Register a shader program.
     *
     * @param name The name of the shader
     * @param vertexShader The vertex shader resource location
     * @param fragmentShader The fragment shader resource location
     * @return The registered shader program, or null if registration failed
     */
    @Nullable
    private static ShaderProgram registerShader(String name, ResourceLocation vertexShader, ResourceLocation fragmentShader) {
        try {
            ShaderProgram program = new ShaderProgram(name, vertexShader, fragmentShader);
            SHADERS.put(name, program);
            LOGGER.info("Registered shader program: %s", name);
            return program;
        } catch (Exception e) {
            LOGGER.error(e, "Failed to register shader program: %s", name);
            return null;
        }
    }

    /**
     * Get a shader program by name.
     *
     * @param name The name of the shader
     * @return The shader program, or null if not found
     */
    @Nullable
    public static ShaderProgram getShader(String name) {
        return SHADERS.get(name);
    }

    /**
     * Check if shaders are supported on this system.
     *
     * @return true if shaders are supported
     */
    public static boolean areShadersSupported() {
        try {
            // Check if OpenGL 2.0+ is available (required for GLSL shaders)
            return RenderSystem.isOnRenderThread() &&
                   GlStateManager._getInteger(0x8B8D) > 0; // GL_SHADING_LANGUAGE_VERSION
        } catch (Exception e) {
            LOGGER.error(e, "Failed to check shader support");
            return false;
        }
    }

    /**
     * Clean up all shader programs.
     * Should be called during client shutdown.
     */
    public static void cleanup() {
        LOGGER.info("Cleaning up shader programs");

        for (ShaderProgram program : SHADERS.values()) {
            try {
                program.close();
            } catch (Exception e) {
                LOGGER.error(e, "Failed to cleanup shader: %s", program.getName());
            }
        }

        SHADERS.clear();
    }
}
