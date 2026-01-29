# RogueLab Design Decisions

This document explains the reasoning behind key architectural and implementation decisions.

---

## Decision: Event-First Telemetry Architecture

### Context
We needed a way to understand game balance, player behavior, and failure modes without instrumenting every analysis at development time.

### Decision
Every meaningful game action emits an immutable, versioned event. Events are the source of truth for what happened during a run.

### Rationale
- **Decoupling**: The game doesn't need to know how data will be analyzed
- **Flexibility**: New analyses can be created without changing game code
- **Debuggability**: Any run can be replayed from its event log
- **Testing**: Deterministic simulation can be validated against expected event sequences

### Trade-offs
- **Storage**: Event logs can grow large (mitigated by .jsonl compression)
- **Performance**: Serialization overhead per event (negligible for turn-based game)

---

## Decision: JSON Lines (.jsonl) for Telemetry

### Context
We needed a file format for storing telemetry events that balances human readability, tooling compatibility, and processing efficiency.

### Decision
Use JSON Lines format: one JSON object per line, newline-delimited.

### Rationale
- **Streaming**: Files can be read line-by-line without loading entirely into memory
- **Appendable**: New events append without rewriting the file
- **Human Readable**: Easy to inspect with standard tools (`head`, `grep`, `jq`)
- **Universal**: Every language has JSON support
- **Recoverable**: Corrupt line doesn't invalidate entire file

### Alternatives Considered
| Format | Rejected Because |
|--------|------------------|
| Single JSON array | Can't append; must rewrite entire file |
| Protobuf | Less readable; overkill for this scale |
| CSV | Nested structures awkward; schema evolution painful |

---

## Decision: Separate Telemetry Server

### Context
The game emits events to files. The dashboard needs to read events. We could have the dashboard read files directly.

### Decision
Interpose a telemetry server between files and consumers.

### Rationale
- **Abstraction**: Dashboard doesn't care about file locations/formats
- **Aggregation**: Server pre-computes statistics, reducing dashboard complexity
- **Validation**: Server validates events on ingestion, catching schema violations early
- **Future flexibility**: Could switch to real-time ingestion without changing dashboard

### Trade-offs
- **Complexity**: One more service to run
- **Latency**: Files must be ingested before they appear in dashboard

---

## Decision: Java for Game Engine

### Context
Choosing a primary language for the core game engine.

### Decision
Java 21+ is the core language.

### Rationale
- **Type Safety**: Strong typing catches errors at compile time
- **OOP Expressiveness**: Clean domain modeling with interfaces and composition
- **Ecosystem**: Excellent testing (JUnit), build tools (Gradle), JSON handling (Jackson)
- **Performance**: More than sufficient for a roguelike
- **Portfolio Value**: Demonstrates enterprise-relevant skills

### Alternatives Considered
| Language | Rejected Because |
|----------|------------------|
| Kotlin | Smaller ecosystem; less universal recognition |
| C# | Excellent choice but less common in target job markets |
| Rust | Steeper learning curve; overkill for this domain |
| Python | Weaker type safety; not ideal for complex domain models |

---

## Decision: TypeScript for Dashboard (Not Java)

### Context
We need a data visualization UI. Java has options (JavaFX, Swing) but they're unusual for this use case.

### Decision
TypeScript with React for the dashboard.

### Rationale
- **Right Tool**: Web tech is the standard for dashboards/visualization
- **Type Safety**: TypeScript provides static typing for API contracts
- **Ecosystem**: React, Recharts, Vite provide excellent DX
- **Portfolio Value**: Demonstrates cross-language competence
- **Legitimate Reason**: This isn't "language padding"—it's using appropriate tools

---

## Decision: Python for Offline Analytics (Not TypeScript)

### Context
We need to perform batch analysis: loading many runs, computing statistics, generating reports.

### Decision
Python with pandas/matplotlib for analytics.

### Rationale
- **Data Science Standard**: pandas is the de facto tool for data analysis
- **Visualization**: matplotlib, seaborn provide publication-quality charts
- **Rapid Iteration**: Script-based analysis is faster to iterate than compiled code
- **Portfolio Value**: Shows comfort in data engineering domain

---

## Decision: Composition Over Inheritance for Entities

### Context
Game entities (Player, enemies) share some behaviors but vary significantly.

### Decision
Use composition: entities are containers for components/behaviors, not deep class hierarchies.

### Example
```java
// NOT this:
class Goblin extends Enemy extends Entity extends GameObject { }

// THIS:
class GameEntity {
    private final EntityId id;
    private final HealthComponent health;
    private final CombatComponent combat;
    private final BehaviorComponent behavior;  // AI or player input
}
```

### Rationale
- **Flexibility**: Can mix and match behaviors without multiple inheritance
- **Testability**: Components can be tested in isolation
- **Maintainability**: Changes to one behavior don't ripple through hierarchy

---

## Decision: Deterministic Game Loop

### Context
The game needs to be testable and its events need to be reproducible.

### Decision
The game simulation is fully deterministic given a seed. No reliance on wall-clock time, system state, or random() without explicit seeding.

### Implementation
- Single `GameRandom` instance per run, seeded from `RUN_STARTED.seed`
- Game loop advances in discrete ticks, not real time
- Rendering is separate from simulation—can run "headless"

### Benefits
- **Reproducibility**: Same seed → same run → same events
- **Testing**: Assertions on specific game states at specific ticks
- **Debugging**: Replay problematic runs from their seed

---

## Decision: Immutable Events

### Context
Events are emitted during gameplay and consumed by multiple systems.

### Decision
Events are immutable after creation. Never modified, only created.

### Implementation
```java
public record DamageDealtEvent(
    Instant timestamp,
    UUID runId,
    int tick,
    DamagePayload payload
) implements GameEvent { }
```

Using Java records makes immutability enforced by the compiler.

### Rationale
- **Thread Safety**: No synchronization needed
- **Debuggability**: Event state never changes after emission
- **Trust**: Consumers know events represent truth at emission time

---

## Decision: Versioned Event Schemas

### Context
The event schema will evolve as we add features. Consumers must handle old and new events.

### Decision
Every event carries its schema version. Breaking changes require major version bumps.

### Rules
1. Never remove fields
2. Never change field types
3. New fields must be optional with defaults
4. Document all changes

### Benefits
- **Forward Compatibility**: Old consumers ignore new fields
- **Backward Compatibility**: New consumers handle old events via version-aware parsing
- **Auditability**: Can trace when schema changes occurred

---

## Decision: No ORM, Simple Persistence

### Context
The telemetry server needs to store run data.

### Decision
In-memory storage with optional file persistence. No database, no ORM.

### Rationale
- **Simplicity**: This is a portfolio project, not a production system at scale
- **Focus**: The interesting architecture is in event design, not CRUD operations
- **Sufficiency**: Hundreds of runs fit easily in memory

### Future Evolution
If needed, could add SQLite or PostgreSQL without changing the API contract.

---

## Decision: Minimal Graphics

### Context
Roguelikes can range from ASCII to elaborate pixel art.

### Decision
Simple Java2D rendering. ASCII-style or basic tile sprites.

### Rationale
- **Focus**: This project demonstrates engineering, not art
- **Velocity**: Time spent on graphics is time not spent on architecture
- **Sufficiency**: Classic roguelikes prove visual simplicity doesn't limit gameplay depth

---

## Open Questions

These decisions haven't been finalized:

1. **Real-time vs. polling telemetry ingestion**: File watching or HTTP push from game?
2. **Persistence format**: Keep JSON files or migrate to SQLite for server?
3. **Authentication**: Add API keys for telemetry server in future?
