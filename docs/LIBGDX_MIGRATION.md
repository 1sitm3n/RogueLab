# RogueLab LibGDX Migration Plan

## Overview

Migrate from Java2D to LibGDX while preserving all existing game logic, telemetry, and analytics.

## Architecture Changes

```
BEFORE (Java2D):
┌─────────────────────────────────────────────┐
│ GameWindow (JFrame)                         │
│   └── GameRenderer (JPanel + paintComponent)│
│         └── RenderColors                    │
│         └── KeyListener                     │
└─────────────────────────────────────────────┘

AFTER (LibGDX):
┌─────────────────────────────────────────────┐
│ RogueLabGame (Game)                         │
│   ├── GameScreen (Screen)                   │
│   │     ├── DungeonRenderer                 │
│   │     ├── CombatRenderer                  │
│   │     ├── UIRenderer                      │
│   │     └── EffectsManager                  │
│   ├── MenuScreen                            │
│   └── Assets (textures, sounds, fonts)      │
└─────────────────────────────────────────────┘
```

## New Package Structure

```
game-java/src/main/java/com/roguelab/
├── core/                    # Existing - unchanged
│   └── RogueLab.java
├── domain/                  # Existing - unchanged
│   ├── Player.java
│   ├── Enemy.java
│   ├── Item.java
│   └── ...
├── dungeon/                 # Existing - unchanged
│   ├── Dungeon.java
│   ├── Floor.java
│   └── ...
├── combat/                  # Existing - unchanged
│   └── CombatEngine.java
├── telemetry/              # Existing - unchanged
│   └── TelemetryEmitter.java
├── game/                   # Existing - unchanged
│   └── GameSession.java
└── gdx/                    # NEW - LibGDX layer
    ├── RogueLabGame.java       # Main game class
    ├── Assets.java             # Asset management
    ├── screen/
    │   ├── MenuScreen.java
    │   ├── GameScreen.java
    │   └── GameOverScreen.java
    ├── render/
    │   ├── DungeonRenderer.java
    │   ├── CombatRenderer.java
    │   ├── UIRenderer.java
    │   └── TileAtlas.java
    ├── effect/
    │   ├── EffectsManager.java
    │   ├── DamageNumber.java
    │   ├── ScreenShake.java
    │   └── ParticlePool.java
    ├── input/
    │   └── GameInputProcessor.java
    └── audio/
        └── SoundManager.java
```

## Dependencies (build.gradle)

```gradle
// Core
implementation "com.badlogicgames.gdx:gdx:1.12.1"

// Desktop launcher
implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.1"
implementation "com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop"

// Audio
implementation "com.badlogicgames.gdx:gdx-freetype:1.12.1"
implementation "com.badlogicgames.gdx:gdx-freetype-platform:1.12.1:natives-desktop"
```

## Visual Features

### 1. Tile-Based Dungeon View
- 32x32 pixel tiles
- Procedural dungeon map visualization
- Fog of war / visibility
- Animated torches and environmental effects

### 2. Combat Screen
- Player sprite with idle/attack animations
- Enemy sprites with variety per type
- Attack slash effects
- Damage numbers (floating, colored by type)
- Health bars (animated depletion)
- Status effect icons

### 3. UI Overlay
- Health/mana bars with smooth interpolation
- Item icons in inventory
- Gold counter with +/- animations
- Floor indicator
- Minimap

### 4. Effects System
- Screen shake on big hits
- Flash effects on damage
- Particle effects (blood, sparks, magic)
- Death animations
- Level up celebration

### 5. Transitions
- Fade between rooms
- Stair descent animation
- Combat enter/exit transitions

## Asset Requirements

```
assets/
├── sprites/
│   ├── player/
│   │   ├── warrior_idle.png
│   │   ├── warrior_attack.png
│   │   ├── rogue_idle.png
│   │   └── ...
│   ├── enemies/
│   │   ├── rat.png
│   │   ├── goblin.png
│   │   ├── skeleton.png
│   │   └── ...
│   └── items/
│       ├── sword.png
│       ├── potion.png
│       └── ...
├── tiles/
│   ├── floor.png
│   ├── wall.png
│   ├── door.png
│   └── stairs.png
├── ui/
│   ├── healthbar.png
│   ├── button.png
│   └── panel.png
├── effects/
│   ├── slash.png
│   ├── blood.png
│   └── magic.png
├── fonts/
│   └── pixel.ttf
└── audio/
    ├── attack.wav
    ├── hit.wav
    ├── death.wav
    └── music.ogg
```

## Implementation Phases

### Phase 11a: Project Setup (1-2 hours)
- Add LibGDX dependencies
- Create desktop launcher
- Basic game loop working

### Phase 11b: Core Rendering (2-3 hours)
- DungeonRenderer with placeholder tiles
- Player/enemy rendering
- Basic UI overlay

### Phase 11c: Combat Visuals (2-3 hours)
- Combat screen layout
- Attack animations
- Damage numbers
- Health bars

### Phase 11d: Effects & Polish (2-3 hours)
- Screen shake
- Particles
- Transitions
- Sound effects

### Phase 11e: Asset Integration (ongoing)
- Replace placeholder art
- Add more animations
- Polish UI

## Preserved Functionality

All existing systems remain unchanged:
- ✅ Domain model (Player, Enemy, Item, etc.)
- ✅ Combat engine with damage calculations
- ✅ Dungeon generation
- ✅ Telemetry emission
- ✅ Game session management
- ✅ Balance configuration

The LibGDX layer is purely presentational - it reads from and sends commands to the existing game logic.
