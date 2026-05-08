# DynamicSurroundings-Forge

[English](#english) | [中文](#中文)

---

## English

### Overview

DynamicSurroundings-Forge is a port of the original DynamicSurroundings mod (originally for Minecraft 1.12) to Minecraft 1.20.1 using Forge. This mod enhances the game's environmental experience through a variety of sound and visual effects.

### Features

#### 1. Footsteps System (`footsteps`)
- Block-based footstep sound effects
- Acoustic material detection
- Customizable footstep sounds through JSON configuration

#### 2. Biome Sound Effects
- Ambient sounds tailored to each biome type
- Dynamic sound layering based on player location
- Support for custom biome configurations

#### 3. Enhanced Weather System
- Real-time weather tracking and synchronization
- Customizable rain and storm effects
- Thunder simulation with realistic timing
- Weather-based fog effects

#### 4. Aurora Borealis System
- Procedural aurora rendering
- Dynamic colors and movement
- Configurable display based on biome and conditions

#### 5. Block Effects
- Water ripple effects when entering water
- Heat producer effects (lava, fire)
- Steam effects on cold biome liquid interactions
- Firefly particles in appropriate biomes
- Various footstep accent sounds

#### 6. Entity Effects
- Entity-specific visual and sound effects
- Bow pull animations
- Frost breath from certain mobs
- Item swing effects

#### 7. Fog System
- Enhanced fog rendering with biome-specific colors
- Multiple calculation modes (Vanilla, Fixed, Biome-based, Bedrock)
- Dynamic fog based on weather conditions

#### 8. Particle System
- Custom particle sheets
- Particle suppression for potions
- Enhanced environmental particles

#### 9. HUD Elements
- Customizable compass display
- In-game clock
- Inspection mode for block/entity information

#### 10. Sound System
- Individual sound volume controls
- Sound fade based on distance
- Background sound loops

### Mod Compatibility

This mod includes built-in support for the following mods:

- **Biomes O' Plenty** - Extended biome sound and effect configurations
- **Promenade** - Additional biome support
- **Profundis** - Deep biome configurations
- **Nature's Spirit** - Custom biome tags and configurations

### Configuration

Configuration files are located in `config/dsurround/`:
- `biomes.json` - Biome-specific settings
- `blocks.json` - Block effect configurations
- `dimensions.json` - Dimension-specific settings
- `sound_factories.json` - Sound factory configurations

The mod also provides in-game configuration through the mod menu.

### Commands

- `/dsurround` - Main mod command for various operations

### Requirements

- Minecraft 1.20.1
- Forge 47.1.3+
- Java 17+

### Building

```bash
./gradlew build
```

---

## 中文

### 概述

DynamicSurroundings-Forge 是原版 DynamicSurroundings 模组（最初为 Minecraft 1.12 开发）的 Forge 1.20.1 移植版本。该模组通过各种声音和视觉效果增强游戏的沉浸式环境体验。

### 功能特性

#### 1. 脚步声系统 (`footsteps`)
- 基于方块的脚步声效
- 声学材质检测
- 通过 JSON 配置自定义脚步声

#### 2. 生物群系音效
- 针对每个生物群系类型定制的环境音效
- 基于玩家位置的动态音效分层
- 支持自定义生物群系配置

#### 3. 增强天气系统
- 实时天气追踪与同步
- 可自定义的雨和风暴效果
- 真实的雷电模拟计时
- 基于天气的雾效

#### 4. 极光系统
- 程序化极光渲染
- 动态颜色和运动
- 基于生物群系和条件的可配置显示

#### 5. 方块效果
- 进入水中的水波纹效果
- 热源效果（熔岩、火）
- 寒冷生物群系液体交互时的蒸汽效果
- 适当生物群系中的萤火虫粒子
- 各种脚步声强调音效

#### 6. 实体效果
- 特定实体的视觉和声音效果
- 弓箭拉弓动画
- 某些生物的寒霜吐息
- 物品挥动效果

#### 7. 雾系统
- 增强的雾渲染，具有生物群系特定颜色
- 多种计算模式（原版、固定、生物群系、地狱）
- 基于天气条件的动态雾

#### 8. 粒子系统
- 自定义粒子表
- 药水粒子抑制
- 增强的环境粒子

#### 9. HUD 元素
- 可自定义的罗盘显示
- 游戏中时钟
- 方块/实体信息的查看模式

#### 10. 声音系统
- 单独的声音音量控制
- 基于距离的声音衰减
- 背景声音循环

### 模组兼容性

本模组内置支持以下模组：

- **Biomes O Plenty** - 扩展的生物群系声音和效果配置
- **Promenade** - 额外的生物群系支持
- **Profundis** - 深海生物群系配置
- **Nature's Spirit** - 自定义生物群系标签和配置

### 配置

配置文件位于 `config/dsurround/`:
- `biomes.json` - 生物群系特定设置
- `blocks.json` - 方块效果配置
- `dimensions.json` - 维度特定设置
- `sound_factories.json` - 声音工厂配置

该模组还通过模组菜单提供游戏内配置。

### 命令

- `/dsurround` - 主模组命令，用于各种操作

### 需求

- Minecraft 1.20.1
- Forge 47.1.3+
- Java 17+

### 构建

```bash
./gradlew build
```

### 许可证

MIT License

### 作者

OreCruncher (Original Author)
Forge Port Contributors