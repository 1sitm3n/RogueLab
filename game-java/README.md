# RogueLab Game Engine (Java)

The core game engine for RogueLab, implementing the domain model, simulation, and telemetry emission.

## Prerequisites

- **Java 21+** (required)
- **Gradle 8.7+** (or use included wrapper)

## Quick Start

```bash
# Generate Gradle wrapper (one-time setup)
gradle wrapper

# Build the project
./gradlew build

# Run the game
./gradlew run

# Run tests
./gradlew test
```

## Project Structure

```
src/main/java/com/roguelab/
├── core/           # Application entry point, game bootstrap
├── domain/         # Domain model (entities, value objects, enums)
│   └── component/  # Reusable components (Health, Combat, StatusEffects)
├── event/          # Event types and event bus (coming soon)
├── simulation/     # Game logic, combat resolution (coming soon)
├── telemetry/      # Event emission, JSON serialization (coming soon)
├── render/         # Java2D rendering (coming soon)
└── util/           # Utilities (GameRandom, etc.)

src/test/java/      # Unit tests mirroring main structure
```

## Architecture

### Composition Over Inheritance

Entities use composition rather than deep inheritance hierarchies:

```java
// Player is composed of reusable components
public final class Player {
    private final Health health;
    private final Combat combat;
    private final StatusEffects statuses;
    private final Inventory inventory;
}
```

### Deterministic Simulation

All randomness goes through `GameRandom`, seeded at run start:

```java
GameRandom rng = new GameRandom(seed);
int damage = rng.nextIntInRange(minDamage, maxDamage);
```

Same seed = same run = same telemetry output.

### Immutable Value Objects

Domain primitives are immutable records:

```java
public record EntityId(String value) { }
public record Position(int x, int y) { }
```

## Domain Model

### Core Entities

| Entity | Description |
|--------|-------------|
| `Player` | The player character with health, combat stats, inventory |
| `Enemy` | Hostile entities with type-based stats |
| `Item` | Weapons, armor, consumables, relics |
| `Room` | Dungeon rooms containing enemies, items, events |

### Components

| Component | Description |
|-----------|-------------|
| `Health` | Current/max HP, damage/heal logic |
| `Combat` | Attack, defense, crit chance |
| `StatusEffects` | Active buffs/debuffs with duration |
| `Inventory` | Item storage, equipment, gold |

### Enums

| Enum | Values |
|------|--------|
| `PlayerClass` | WARRIOR, ROGUE, MAGE |
| `EnemyType` | GOBLIN, ORC, DRAGON, etc. |
| `ItemType` | WEAPON, ARMOR, CONSUMABLE, etc. |
| `Rarity` | COMMON through LEGENDARY |
| `DamageType` | PHYSICAL, FIRE, ICE, POISON, MAGIC |
| `StatusType` | POISON, BURNING, FROZEN, etc. |
| `RoomType` | COMBAT, TREASURE, SHOP, REST, BOSS, EVENT |
| `Difficulty` | EASY, NORMAL, HARD, NIGHTMARE |

## Testing

Tests use JUnit 5 and AssertJ:

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.roguelab.domain.PlayerTest"

# Run with verbose output
./gradlew test --info
```

## Next Steps

The following subsystems will be implemented in future iterations:

1. **Event System** - Immutable, versioned events for all game actions
2. **Telemetry Emitter** - JSON Lines output for run analysis
3. **Combat Resolution** - Turn-based combat with damage calculation
4. **Room Generation** - Procedural dungeon floors
5. **Game Loop** - Tick-based simulation
6. **Renderer** - Simple Java2D visualization
