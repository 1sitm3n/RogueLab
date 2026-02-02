# RogueLab

A data-driven roguelike demonstrating professional-grade Java architecture, telemetry systems, and cross-language analytics.

## Overview

RogueLab is a portfolio project showcasing:
- **Clean OOP Design**: Domain-driven architecture with clear separation of concerns
- **Event-Driven Telemetry**: Every game action emits structured JSON events
- **Cross-Language Integration**: Java game engine, Python analytics pipeline
- **Classic Dungeon Crawler UI**: 90s-inspired first-person perspective with stone-frame aesthetics

## Quick Start

### Prerequisites
- Java 21+
- Gradle 8+ (or use included wrapper)
- Python 3.10+ (for analytics)

### Running the Game

```bash
cd game-java
gradle runGame
```

## Screenshots

The game features a classic dungeon crawler aesthetic:
- Stone-framed UI panels
- First-person corridor view
- Character portrait with vertical HP bar
- Gothic color palette (dark stone, aged gold, blood red)

## Controls

| Screen | Key | Action |
|--------|-----|--------|
| Menu | W/S, UP/DOWN | Select class |
| Menu | ENTER, SPACE | Start game |
| Dungeon | A/D, LEFT/RIGHT | Move between rooms |
| Dungeon | SPACE | Interact (rest, shop, descend) |
| Combat | SPACE | Attack |
| Any | ESC | Menu / Quit |

## Game Features

### Classes
| Class | HP | ATK | DEF | Special |
|-------|----|----|-----|---------|
| Warrior | 120 | 12 | 8 | Balanced stats |
| Rogue | 80 | 15 | 4 | 15% crit chance, 2x crit damage |
| Mage | 70 | 18 | 3 | Magic damage type |

### Dungeon Structure
- **3 Floors** with 6 rooms each
- **Room Types**: Combat, Boss, Treasure, Shop, Rest
- **Progression**: Clear all combat rooms → Defeat boss → Descend

### Combat
- Turn-based battles against procedurally scaled enemies
- Damage calculation: `ATK - DEF` with variance
- Visual feedback: Screen shake, floating damage numbers

## Project Structure

```
roguelab/
├── game-java/                    # Java game engine
│   └── src/main/java/com/roguelab/
│       ├── domain/               # Core entities (Player, Enemy, Item)
│       ├── combat/               # Combat engine and damage calculation
│       ├── dungeon/              # Dungeon and floor generation
│       ├── game/                 # GameSession orchestration
│       ├── event/                # Telemetry event definitions
│       ├── telemetry/            # JSON event emission
│       └── gdx/                  # LibGDX presentation layer
│           ├── screen/           # Menu, Game, GameOver screens
│           ├── render/           # Dungeon, Combat, UI renderers
│           └── effect/           # Visual effects (damage numbers, etc.)
├── analytics-py/                 # Python analytics pipeline
│   ├── analyze.py                # Main analysis script
│   ├── reports/                  # Generated reports
│   └── requirements.txt
├── docs/                         # Documentation
└── runs/                         # Telemetry output (*.jsonl)
```

## Architecture

### Domain Model
```
Player ─── Health, Combat, Inventory, StatusEffects
Enemy ─── EnemyType, Health, Combat
Dungeon ─── Floor[] ─── Room[] ─── Enemy[], Item[]
GameSession ─── orchestrates Player, Dungeon, CombatEngine
```

### Telemetry Events
Every game action emits a structured event:
```json
{
  "event_type": "DAMAGE_DEALT",
  "event_version": "1",
  "timestamp": "2025-02-02T14:30:00Z",
  "run_id": "run_1738505400000",
  "tick": 42,
  "payload": {
    "attacker": "player",
    "defender": "SKELETON",
    "damage": 12,
    "critical": false
  }
}
```

**Event Types**: `RUN_STARTED`, `ROOM_ENTERED`, `COMBAT_STARTED`, `DAMAGE_DEALT`, `ITEM_PICKED`, `SHOP_PURCHASED`, `PLAYER_HEALED`, `PLAYER_DIED`, `RUN_ENDED`

### Analytics Pipeline

```bash
cd analytics-py
pip install -r requirements.txt
python analyze.py
```

Generates reports on:
- Win/loss rates by class
- Item effectiveness
- Enemy lethality rankings
- Death cause analysis
- Floor difficulty curves

## Development Phases

| Phase | Description | Status |
|-------|-------------|--------|
| 1-4 | Domain model, combat, items | ✅ Complete |
| 5-6 | Dungeon generation | ✅ Complete |
| 7 | Telemetry system | ✅ Complete |
| 8 | Python analytics | ✅ Complete |
| 9 | Java2D renderer | ✅ Complete |
| 10 | Balance tuning (35% win rate) | ✅ Complete |
| 11 | LibGDX visual upgrade | ✅ Complete |
| 12 | Classic dungeon UI | ✅ Complete |

## Technical Highlights

- **Deterministic Gameplay**: Seeded random for reproducible runs
- **Component Architecture**: Player stats via Health, Combat, Inventory components
- **Listener Pattern**: Decoupled telemetry via GameSessionListener, CombatEventListener
- **Procedural Assets**: All sprites and textures generated at runtime (no external files)
- **Balance Iteration**: 10 tuning iterations using analytics data

## Configuration

Game balance is controlled in:
- `PlayerClass.java` - Starting stats per class
- `EnemyType.java` - Enemy stats and scaling
- `DungeonConfig.java` - Floor count, room counts, difficulty curve

## Building

```bash
# Build JAR
cd game-java
gradle jar

# Run JAR
java -jar build/libs/game-java-0.5.0.jar

# Run tests
gradle test
```

## Future Roadmap

- [ ] Sound effects and music
- [ ] Real sprite assets (replace procedural)
- [ ] Status effect visuals
- [ ] Save/load system
- [ ] TypeScript telemetry dashboard
- [ ] More enemy types and boss mechanics

## Version History

- **0.5.1** - Classic Dungeon UI (stone frames, portraits, corridor view)
- **0.5.0** - LibGDX integration with domain model
- **0.4.3** - Balance tuning complete (35% win rate)
- **0.4.0** - Python analytics pipeline
- **0.3.0** - Telemetry system
- **0.2.0** - Dungeon generation
- **0.1.0** - Core domain model

## License

Portfolio project - MIT License

## Author

Built as a demonstration of production-grade game architecture and data-driven development.
