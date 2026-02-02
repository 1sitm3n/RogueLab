# RogueLab - LibGDX Edition

A data-driven roguelike game built with LibGDX for rich 2D graphics and effects.

## Quick Start

### Prerequisites
- Java 17+
- Gradle 8+ (or use the included wrapper)

### Running the Game

```bash
cd game-java
gradle runGame
```

Or build and run the JAR:
```bash
gradle jar
java -jar build/libs/game-java-0.5.0.jar
```

## Controls

### Menu
- **UP/DOWN** or **W/S**: Select class
- **ENTER/SPACE**: Start game
- **ESC**: Quit

### Dungeon Exploration
- **A/D** or **LEFT/RIGHT**: Move between rooms
- **SPACE/ENTER**: Interact with room (open chests, rest, shop, descend stairs)
- **ESC**: Return to menu

### Combat
- **SPACE/ENTER**: Attack
- Combat is turn-based: you attack, then the enemy attacks

## Game Flow

1. **Select a class**: Warrior (balanced), Rogue (high damage), or Mage (highest damage, lowest HP)
2. **Explore the dungeon**: Move through rooms on each floor
3. **Fight enemies**: Enter combat rooms to battle monsters
4. **Collect loot**: Open treasure chests for gold
5. **Rest**: Recover 30% HP at campfires
6. **Shop**: Buy weapon upgrades for 50 gold
7. **Defeat the boss**: Each floor ends with a boss battle
8. **Descend**: After defeating the boss, use the stairs to go deeper
9. **Win**: Clear all 3 floors to achieve victory!

## Features

- **Visual Effects**: Screen shake, floating damage numbers, combat animations
- **Procedural Dungeons**: Each run generates different room layouts
- **Class System**: Three distinct player classes with different stats
- **Boss Battles**: Challenging encounters at the end of each floor
- **Progression**: Collect gold, buy upgrades, get stronger

## Project Structure

```
game-java/
├── build.gradle                 # Gradle build configuration
└── src/main/java/com/roguelab/
    ├── gdx/
    │   ├── DesktopLauncher.java    # Main entry point
    │   ├── RogueLabGame.java       # Game class managing screens
    │   ├── Assets.java             # Asset management
    │   ├── screen/
    │   │   ├── MenuScreen.java     # Main menu
    │   │   ├── GameScreen.java     # Gameplay screen
    │   │   └── GameOverScreen.java # Victory/defeat screen
    │   ├── render/
    │   │   ├── DungeonRenderer.java
    │   │   ├── CombatRenderer.java
    │   │   └── UIRenderer.java
    │   └── effect/
    │       ├── EffectsManager.java
    │       └── DamageNumber.java
    └── (existing domain classes)
```

## Integration with Existing Code

This LibGDX layer is designed to work with the existing RogueLab domain model:

1. **GameScreen.GameState** is a simplified demo state
2. Replace it with the real domain classes:
   - `Player` from `com.roguelab.domain`
   - `Enemy` from `com.roguelab.domain`
   - `Dungeon` from `com.roguelab.dungeon`
   - `CombatEngine` from `com.roguelab.combat`
   - `GameSession` from `com.roguelab.game`

3. The renderers read from game state - they don't modify it
4. Telemetry continues to work through `GameSession`

## Next Steps

- [ ] Integrate with existing domain model
- [ ] Add sprite sheets for real graphics
- [ ] Implement full item system UI
- [ ] Add sound effects and music
- [ ] Add particle effects (blood, magic, etc.)
- [ ] Screen transitions (fade between rooms)
- [ ] Minimap

## Version

0.5.0 - LibGDX Edition (Standalone Demo)
