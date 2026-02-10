# RogueLab

<div align="center">

**A Data-Driven Roguelike Engine**

*Demonstrating production-grade architecture, event-driven telemetry, and cross-language integration*

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![LibGDX](https://img.shields.io/badge/LibGDX-1.12-red?style=flat-square)](https://libgdx.com/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue?style=flat-square&logo=typescript)](https://www.typescriptlang.org/)
[![Python](https://img.shields.io/badge/Python-3.11-green?style=flat-square&logo=python)](https://python.org/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)

</div>

---

## Overview

RogueLab is a **multi-language portfolio project** that combines a fully playable roguelike game with professional-grade telemetry, analytics, and visualization systems. The project demonstrates:

- **Clean Object-Oriented Design** — Domain-driven architecture with clear separation of concerns
- **Event-Driven Telemetry** — Every game action emits structured, versioned events
- **Cross-Language Integration** — Java game engine, TypeScript dashboard, Python analytics
- **Production Patterns** — Testable code, schema contracts, and observability-first design

This is not a tutorial or toy project. It is designed to showcase real-world engineering competence across the full software stack.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              ROGUELAB SYSTEM                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐         │
│  │   GAME ENGINE   │───▶│   TELEMETRY     │───▶│   ANALYTICS     │         │
│  │     (Java)      │    │    SERVER       │    │    (Python)     │         │
│  └─────────────────┘    │     (Java)      │    └─────────────────┘         │
│          │              └─────────────────┘            │                   │
│          │                      │                      │                   │
│          ▼                      ▼                      ▼                   │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐         │
│  │   .jsonl files  │    │   REST API      │    │   Reports &     │         │
│  │   (Run Data)    │    │   Endpoints     │    │   Visualizations│         │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘         │
│                                 │                                          │
│                                 ▼                                          │
│                         ┌─────────────────┐                                │
│                         │   DASHBOARD     │                                │
│                         │  (TypeScript)   │                                │
│                         └─────────────────┘                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Design Principles

| Principle | Implementation |
|-----------|----------------|
| **Composition over Inheritance** | Entity-component design for Player, Enemy, Combat |
| **Immutable Events** | All telemetry events are versioned and serializable |
| **Interface Segregation** | Behavior contracts via interfaces, not data inheritance |
| **Dependency Injection** | GameSession receives configuration, listeners, random sources |
| **Testability** | Deterministic combat with seeded randomness |

---

## Features

### 🎮 Game Engine

- **Three Character Classes** — Warrior (tank), Rogue (balanced), Mage (glass cannon)
- **25 Enemy Types** — From rats and bats to dragons and demon lords
- **11 Special Abilities** — Poison, burn, stun, life drain, phase, corrode, and more
- **Procedural Dungeons** — Multi-floor exploration with varied room types
- **Turn-Based Combat** — Strategic encounters with damage calculation and status effects
- **Shop & Rest Systems** — Resource management and recovery options

### 🖼️ Daggerfall-Style UI

- **Procedural Graphics** — All sprites generated at runtime, no external assets
- **Viewport Scaling** — FitViewport system maintains layout at any window size
- **Animated Effects** — Damage numbers, screen shake, health bar interpolation
- **Retro Aesthetic** — Stone panels, gold accents, flickering torchlight

### 🔊 Procedural Audio

- **18 Sound Effects** — Generated via waveform synthesis (square, sawtooth, noise)
- **ADSR Envelopes** — Attack, decay, sustain, release shaping
- **No External Files** — All audio created programmatically at startup

### 📊 Telemetry System

Every meaningful game action emits a structured event:

```json
{
  "event_type": "DAMAGE_DEALT",
  "event_version": "1.0",
  "timestamp": "2025-02-10T14:32:15.123Z",
  "run_id": "run_abc123",
  "tick": 47,
  "payload": {
    "attacker_id": "player_1",
    "defender_id": "skeleton_3",
    "damage": 12,
    "damage_type": "PHYSICAL",
    "is_critical": false,
    "defender_health_after": 8
  }
}
```

**Event Types:**
- `RUN_STARTED`, `RUN_ENDED`
- `FLOOR_ENTERED`, `ROOM_ENTERED`, `ROOM_CLEARED`
- `COMBAT_STARTED`, `DAMAGE_DEALT`, `COMBAT_ENDED`
- `ITEM_PICKED`, `SHOP_PURCHASED`
- `PLAYER_RESTED`, `PLAYER_LEVEL_UP`, `PLAYER_DIED`

### 📈 Analytics Pipeline

Python scripts for offline analysis:

- **Win Rate Analysis** — Track success rates by class and difficulty
- **Enemy Lethality** — Identify which enemies cause the most deaths
- **Item Effectiveness** — Compare pick rates vs win rates
- **Balance Reports** — CSV summaries and matplotlib visualizations

---

## Project Structure

```
roguelab/
├── game-java/                    # Core game engine
│   ├── src/main/java/com/roguelab/
│   │   ├── domain/               # Player, Enemy, Item, EnemyType, etc.
│   │   ├── combat/               # CombatEngine, DamageCalculator
│   │   ├── dungeon/              # Floor, Room, DungeonGenerator
│   │   ├── game/                 # GameSession, GameState
│   │   ├── telemetry/            # Event emitters and writers
│   │   └── gdx/                  # LibGDX rendering and screens
│   │       ├── screen/           # MenuScreen, GameScreen, GameOverScreen
│   │       ├── audio/            # ProceduralSoundGenerator, SoundManager
│   │       └── effect/           # EffectsManager, damage numbers
│   └── build.gradle
│
├── telemetry-server/             # REST API for run data
│   ├── src/main/java/
│   └── build.gradle
│
├── dashboard-ts/                 # Web visualization (TypeScript)
│   ├── src/
│   └── package.json
│
├── analytics-py/                 # Offline analysis scripts
│   ├── analyze_runs.py
│   ├── balance_report.py
│   └── requirements.txt
│
├── docs/                         # Architecture documentation
│   ├── ARCHITECTURE.md
│   ├── EVENT_SCHEMA.md
│   └── DESIGN_DECISIONS.md
│
└── README.md
```

---

## Getting Started

### Prerequisites

- **Java 21+** (OpenJDK recommended)
- **Gradle 8+** (wrapper included)
- **Node.js 18+** (for dashboard)
- **Python 3.11+** (for analytics)

### Build & Run

```bash
# Clone the repository
git clone https://github.com/yourusername/roguelab.git
cd roguelab

# Build and run the game
cd game-java
./gradlew runGame

# Build and run the telemetry server
cd ../telemetry-server
./gradlew run

# Install and run the dashboard
cd ../dashboard-ts
npm install
npm run dev

# Run analytics
cd ../analytics-py
pip install -r requirements.txt
python analyze_runs.py ../runs/
```

### Controls

| Key | Action |
|-----|--------|
| `A` / `D` or `←` / `→` | Move between rooms |
| `SPACE` / `ENTER` | Attack / Interact / Confirm |
| `W` / `S` or `↑` / `↓` | Navigate menus |
| `1-9` | Purchase items in shop |
| `M` | Toggle sound |
| `ESC` | Quit to menu |

---

## Technical Highlights

### Domain Model

```java
public final class Player {
    private final EntityId id;
    private final PlayerClass playerClass;
    private final Health health;
    private final Combat combat;
    private final Inventory inventory;
    private final StatusEffects statuses;
    private int level;
    private int experience;
    
    public int getEffectiveAttack() {
        int base = combat.getTotalAttack();
        double modifier = statuses.getAttackModifier();
        return (int) Math.round(base * modifier);
    }
}
```

### Combat System

```java
public CombatResult runCombat(String runId, Player player, Room room, 
                               GameRandom random, int startTick) {
    CombatContext ctx = new CombatContext(runId, player, room, startTick);
    
    while (ctx.isCombatActive()) {
        ctx.nextTurn();
        processPlayerDoT(ctx);           // Poison, burn damage
        if (player.isDead()) break;
        
        processPlayerAttack(ctx);         // Player attacks first enemy
        processEnemyAttacks(ctx);         // All enemies counter-attack
        processSpecialAbilities(ctx);     // Trigger enemy abilities
        
        player.getStatuses().tickAll();   // Reduce effect durations
    }
    
    return ctx.buildResult();
}
```

### Viewport Scaling

```java
private static final float VIRTUAL_WIDTH = 1280;
private static final float VIRTUAL_HEIGHT = 720;

private final OrthographicCamera camera;
private final Viewport viewport;

public GameScreen() {
    this.camera = new OrthographicCamera();
    this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
}

@Override
public void resize(int width, int height) {
    viewport.update(width, height, true);
}
```

### Procedural Audio

```java
public Sound generateSound(Waveform wave, float frequency, float duration,
                           float frequencySlide, float vibratoDepth) {
    int sampleRate = 22050;
    int samples = (int)(sampleRate * duration);
    short[] pcm = new short[samples];
    
    for (int i = 0; i < samples; i++) {
        float t = (float) i / sampleRate;
        float freq = frequency + frequencySlide * t;
        float vibrato = (float) Math.sin(t * vibratoHz * 2 * Math.PI) * vibratoDepth;
        float sample = generateWaveform(wave, t, freq + vibrato);
        sample *= getEnvelope(t, duration);  // ADSR shaping
        pcm[i] = (short)(sample * 32767 * volume);
    }
    
    return createWavSound(pcm, sampleRate);
}
```

---

## Enemy Types

| Tier | Enemies | Special Abilities |
|------|---------|-------------------|
| **Floor 1** | Rat, Bat, Spider, Skeleton, Slime, Goblin | Poison, Corrode, Steal Gold |
| **Floor 2** | Zombie, Orc, Ghost, Wraith, Cultist | Life Drain, Phase, Curse |
| **Floor 3** | Troll, Elemental, Golem, Demon, Vampire, Minotaur | Burn, Stun, Charge |
| **Bosses** | Goblin King, Necromancer, Skeleton Lord, Orc Chieftain, Lich, Dragon, Demon Lord | Summon, Enrage, and tier abilities |

---

## Why This Project?

RogueLab was built to demonstrate:

1. **System Design** — Not just features, but coherent architecture
2. **Data Engineering** — Telemetry as a first-class citizen
3. **Cross-Language Competence** — Java, TypeScript, Python working together
4. **Production Mindset** — Testability, maintainability, observability
5. **Attention to Polish** — Procedural graphics, audio, and smooth UX

The codebase is intentionally readable and well-documented, suitable for review by engineering teams evaluating software craftsmanship.

---

## Roadmap

- [ ] TypeScript dashboard with run visualization
- [ ] Python balance analysis with matplotlib charts
- [ ] Additional character classes
- [ ] Item enchantment system
- [ ] Persistent high scores
- [ ] Run replay from telemetry

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---


</div>
