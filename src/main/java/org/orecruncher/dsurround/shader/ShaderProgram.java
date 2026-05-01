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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.lib.logging.IModLog;
import org.orecruncher.dsurround.lib.Library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a compiled GLSL shader program.
 * Provides methods for setting uniforms and using the shader.
 */
@OnlyIn(Dist.CLIENT)
public class ShaderProgram implements AutoCloseable {

    private static final IModLog LOGGER = Library.LOGGER;

    private final String name;
    private final ResourceLocation vertexShaderLocation;
    private final ResourceLocation fragmentShaderLocation;

    private int programId = -1;
    private int vertexShaderId = -1;
    private int fragmentShaderId = -1;

    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private boolean compiled = false;

    /**
     * Create a new shader program.
     *
     * @param name The name of the shader
     * @param vertexShader The vertex shader resource location
     * @param fragmentShader The fragment shader resource location
     */
    public ShaderProgram(String name, ResourceLocation vertexShader, ResourceLocation fragmentShader) {
        this.name = name;
        this.vertexShaderLocation = vertexShader;
        this.fragmentShaderLocation = fragmentShader;
    }

    /**
     * Get the name of this shader program.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Compile and link the shader program.
     * This is done lazily on first use.
     */
    private void compile() throws IOException {
        if (this.compiled) {
            return;
        }

        LOGGER.debug("Compiling shader program: %s", this.name);

        // Create program
        this.programId = GL20.glCreateProgram();
        if (this.programId == 0) {
            throw new IOException("Failed to create shader program");
        }

        // Load and compile vertex shader
        String vertexSource = loadShaderSource(this.vertexShaderLocation);
        this.vertexShaderId = compileShader(GL20.GL_VERTEX_SHADER, vertexSource);
        GL20.glAttachShader(this.programId, this.vertexShaderId);

        // Load and compile fragment shader
        String fragmentSource = loadShaderSource(this.fragmentShaderLocation);
        this.fragmentShaderId = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource);
        GL20.glAttachShader(this.programId, this.fragmentShaderId);

        // Link program
        GL20.glLinkProgram(this.programId);

        // Check link status
        int linkStatus = GL20.glGetProgrami(this.programId, GL20.GL_LINK_STATUS);
        if (linkStatus == 0) {
            String log = GL20.glGetProgramInfoLog(this.programId);
            throw new IOException("Failed to link shader program: " + log);
        }

        // Validate program
        GL20.glValidateProgram(this.programId);
        int validateStatus = GL20.glGetProgrami(this.programId, GL20.GL_VALIDATE_STATUS);
        if (validateStatus == 0) {
            String log = GL20.glGetProgramInfoLog(this.programId);
            LOGGER.warn("Shader program validation warning: %s", log);
        }

        this.compiled = true;
        LOGGER.debug("Successfully compiled shader program: %s (id=%d)", this.name, this.programId);
    }

    /**
     * Load shader source code from a resource location.
     */
    private String loadShaderSource(ResourceLocation location) throws IOException {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(location).orElseThrow();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new IOException("Failed to load shader source: " + location, e);
        }
    }

    /**
     * Compile a shader from source code.
     */
    private int compileShader(int type, String source) throws IOException {
        int shaderId = GL20.glCreateShader(type);
        if (shaderId == 0) {
            throw new IOException("Failed to create shader");
        }

        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);

        // Check compile status
        int compileStatus = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
        if (compileStatus == 0) {
            String log = GL20.glGetShaderInfoLog(shaderId);
            GL20.glDeleteShader(shaderId);
            throw new IOException("Failed to compile shader: " + log);
        }

        return shaderId;
    }

    /**
     * Get the location of a uniform variable.
     */
    private int getUniformLocation(String name) {
        if (!this.compiled) {
            return -1;
        }

        return this.uniformLocations.computeIfAbsent(name, n -> {
            int location = GL20.glGetUniformLocation(this.programId, n);
            if (location == -1) {
                LOGGER.debug("Uniform '%s' not found in shader '%s'", n, this.name);
            }
            return location;
        });
    }

    /**
     * Use this shader program for rendering.
     */
    public void use() {
        if (!this.compiled) {
            try {
                compile();
            } catch (IOException e) {
                LOGGER.error(e, "Failed to compile shader program: %s", this.name);
                return;
            }
        }

        RenderSystem.assertOnRenderThread();
        GL20.glUseProgram(this.programId);
    }

    /**
     * Use this shader program with a callback for setting uniforms.
     */
    public void use(IShaderUseCallback callback) {
        use();
        if (callback != null) {
            callback.onShaderUse(this);
        }
    }

    /**
     * Stop using this shader program.
     */
    public void unUse() {
        RenderSystem.assertOnRenderThread();
        GL20.glUseProgram(0);
    }

    /**
     * Set a float uniform.
     */
    public void setUniform(String name, float value) {
        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform1f(location, value);
        }
    }

    /**
     * Set a vec2 uniform.
     */
    public void setUniform(String name, float x, float y) {
        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform2f(location, x, y);
        }
    }

    /**
     * Set a vec2 uniform.
     */
    public void setUniform(String name, Vector2f value) {
        setUniform(name, value.x, value.y);
    }

    /**
     * Set a vec4 uniform.
     */
    public void setUniform(String name, float x, float y, float z, float w) {
        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform4f(location, x, y, z, w);
        }
    }

    /**
     * Set a vec4 uniform.
     */
    public void setUniform(String name, Vector4f value) {
        setUniform(name, value.x, value.y, value.z, value.w);
    }

    /**
     * Set an int uniform.
     */
    public void setUniform(String name, int value) {
        int location = getUniformLocation(name);
        if (location != -1) {
            GL20.glUniform1i(location, value);
        }
    }

    /**
     * Check if this shader program is compiled and ready to use.
     */
    public boolean isCompiled() {
        return this.compiled;
    }

    @Override
    public void close() {
        if (this.programId != -1) {
            GL20.glDeleteProgram(this.programId);
            this.programId = -1;
        }

        if (this.vertexShaderId != -1) {
            GL20.glDeleteShader(this.vertexShaderId);
            this.vertexShaderId = -1;
        }

        if (this.fragmentShaderId != -1) {
            GL20.glDeleteShader(this.fragmentShaderId);
            this.fragmentShaderId = -1;
        }

        this.uniformLocations.clear();
        this.compiled = false;
    }

    /**
     * Callback interface for setting shader uniforms.
     */
    @FunctionalInterface
    public interface IShaderUseCallback {
        void onShaderUse(ShaderProgram shader);
    }
}
