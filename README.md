# RogueLab

A production-grade roguelike game engine with integrated telemetry, analytics, and visualization—designed to demonstrate professional software engineering across multiple languages.

## What This Project Is

RogueLab is a small roguelike-style game where each playthrough ("run") generates structured telemetry events. These events are logged and analyzed to understand player behavior, balance issues, and systemic weaknesses.

The project is intentionally designed so it can be:
- **Played** by a human
- **Simulated** programmatically
- **Analyzed** statistically
- **Improved** using data

This is not a tutorial, toy project, or coursework demo. It is a portfolio-quality demonstration of:
- Clean object-oriented design
- Event-driven architecture
- Schema-based data contracts
- Cross-language integration for legitimate engineering reasons
- Observability and data-driven development

## Repository Structure

```
/roguelab
├── game-java/        # Core game engine (Java 21+)
├── telemetry-server/ # REST API for telemetry data (Java)
├── dashboard-ts/     # Data visualization UI (TypeScript/React)
├── analytics-py/     # Offline analysis & reporting (Python)
├── docs/             # Architecture, schemas, design decisions
└── README.md
```

## Why Multiple Languages?

Each language serves a distinct, justified purpose:

| Module | Language | Rationale |
|--------|----------|-----------|
| `game-java` | Java | Strongly-typed OOP, clean domain modeling, deterministic simulation |
| `telemetry-server` | Java | Shared domain knowledge with game, type-safe event handling |
| `dashboard-ts` | TypeScript | Rich UI ecosystem, strong typing for API contracts |
| `analytics-py` | Python | Data science libraries (pandas, matplotlib), rapid analysis iteration |

## Data Flow

```
┌─────────────┐     .jsonl      ┌───────────────────┐
│  game-java  │ ───────────────▶│ telemetry-server  │
│  (engine)   │   telemetry     │     (REST API)    │
└─────────────┘    events       └─────────┬─────────┘
                                          │ HTTP/JSON
                    ┌─────────────────────┼─────────────────────┐
                    ▼                                           ▼
          ┌─────────────────┐                        ┌──────────────────┐
          │  dashboard-ts   │                        │   analytics-py   │
          │ (visualization) │                        │ (batch analysis) │
          └─────────────────┘                        └──────────────────┘
```

## Building Each Module

### game-java
```bash
cd game-java
./gradlew build      # Compile and test
./gradlew run        # Play the game
./gradlew test       # Run unit tests
```

### telemetry-server
```bash
cd telemetry-server
./gradlew build
./gradlew run        # Start server on http://localhost:8080
```

### dashboard-ts
```bash
cd dashboard-ts
npm install
npm run dev          # Start dev server on http://localhost:5173
npm run build        # Production build
```

### analytics-py
```bash
cd analytics-py
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python -m roguelab_analytics.cli --help
```

## Documentation

See the `/docs` directory for:
- [Architecture Overview](docs/architecture.md)
- [Event Schema](docs/event-schema.md)
- [Design Decisions](docs/design-decisions.md)

## License

MIT License - See [LICENSE](LICENSE) for details.
