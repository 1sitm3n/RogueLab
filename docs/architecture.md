# RogueLab Architecture

## Overview

RogueLab follows a modular, event-driven architecture where the game engine emits telemetry that flows through a server to visualization and analysis tools.

## System Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              RogueLab System                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                                GAME LAYER                                   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         game-java                                    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │   │
│  │  │  Game Loop   │  │ Domain Model │  │   Renderer   │              │   │
│  │  │  (Tick/Turn) │  │  (Entities)  │  │   (Java2D)   │              │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────────────┘              │   │
│  │         │                 │                                         │   │
│  │         ▼                 ▼                                         │   │
│  │  ┌─────────────────────────────────┐                               │   │
│  │  │        Simulation Engine        │                               │   │
│  │  │  (Combat, Movement, Items, AI)  │                               │   │
│  │  └──────────────┬──────────────────┘                               │   │
│  │                 │                                                   │   │
│  │                 ▼                                                   │   │
│  │  ┌─────────────────────────────────┐     ┌────────────────────┐   │   │
│  │  │         Event System            │────▶│ Telemetry Emitter  │   │   │
│  │  │  (Immutable, Versioned Events)  │     │  (JSON Lines)      │   │   │
│  │  └─────────────────────────────────┘     └─────────┬──────────┘   │   │
│  └─────────────────────────────────────────────────────│──────────────┘   │
└────────────────────────────────────────────────────────│──────────────────┘
                                                         │
                                                    .jsonl files
                                                         │
┌────────────────────────────────────────────────────────│──────────────────┐
│                             DATA LAYER                 │                   │
│  ┌─────────────────────────────────────────────────────▼──────────────┐   │
│  │                      telemetry-server                              │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐ │   │
│  │  │ File Watcher │  │  Run Store   │  │      REST Endpoints      │ │   │
│  │  │  (Ingest)    │  │  (In-Memory) │  │  /runs, /stats/*         │ │   │
│  │  └──────────────┘  └──────────────┘  └────────────┬─────────────┘ │   │
│  └───────────────────────────────────────────────────│────────────────┘   │
└──────────────────────────────────────────────────────│────────────────────┘
                                                       │
                                           HTTP/JSON   │
                       ┌───────────────────────────────┴───────────────────┐
                       │                                                   │
┌──────────────────────▼─────────────────┐    ┌───────────────────────────▼──┐
│          VISUALIZATION LAYER           │    │        ANALYSIS LAYER        │
│  ┌─────────────────────────────────┐  │    │  ┌─────────────────────────┐ │
│  │         dashboard-ts            │  │    │  │      analytics-py       │ │
│  │  ┌───────────┐ ┌─────────────┐  │  │    │  │  ┌─────────────────┐   │ │
│  │  │ Run List  │ │ Run Detail  │  │  │    │  │  │  Data Loaders   │   │ │
│  │  └───────────┘ └─────────────┘  │  │    │  │  └─────────────────┘   │ │
│  │  ┌───────────┐ ┌─────────────┐  │  │    │  │  ┌─────────────────┐   │ │
│  │  │  Balance  │ │ Statistics  │  │  │    │  │  │   Aggregators   │   │ │
│  │  └───────────┘ └─────────────┘  │  │    │  │  └─────────────────┘   │ │
│  └─────────────────────────────────┘  │    │  │  ┌─────────────────┐   │ │
└───────────────────────────────────────┘    │  │  │ Report Writers  │   │ │
                                             │  │  └─────────────────┘   │ │
                                             │  └─────────────────────────┘ │
                                             └──────────────────────────────┘
```

## Module Responsibilities

### game-java

**Purpose**: Core game engine—owns all game logic, domain model, and telemetry emission.

**Key Responsibilities**:
- Deterministic game loop (tick/turn-based progression)
- Domain entities (Player, Enemy, Item, Room, StatusEffect)
- Combat resolution and damage calculation
- AI behavior for enemies
- Event emission for all meaningful game actions
- Simple rendering via Java2D

**Does NOT**:
- Store historical run data
- Perform analytics
- Make network requests (telemetry is file-based)

### telemetry-server

**Purpose**: REST API that ingests and serves telemetry data.

**Key Responsibilities**:
- Watch for and ingest `.jsonl` telemetry files
- Store run data in memory (or simple file persistence)
- Expose read-only REST endpoints for runs and statistics
- Validate event schemas on ingestion

**Does NOT**:
- Contain any game logic
- Modify or generate game events
- Render UI

### dashboard-ts

**Purpose**: Browser-based visualization of telemetry data.

**Key Responsibilities**:
- Fetch data from telemetry-server REST API
- Display run lists with filtering
- Visualize individual run timelines
- Show aggregate statistics and charts
- Highlight balance issues (win rates, lethality)

**Does NOT**:
- Duplicate game logic
- Store data (stateless UI)
- Process raw `.jsonl` files directly

### analytics-py

**Purpose**: Offline batch analysis and report generation.

**Key Responsibilities**:
- Load and validate `.jsonl` files
- Compute aggregate statistics across many runs
- Generate CSV exports for further analysis
- Create visualizations (matplotlib plots)
- Produce Markdown/HTML reports

**Does NOT**:
- Run in real-time
- Serve HTTP endpoints
- Modify telemetry data

## Design Principles

### Separation of Concerns
Each module has a single, well-defined responsibility. The game engine doesn't know about the dashboard; the dashboard doesn't know about Python analytics. They communicate only through well-defined interfaces (files, REST APIs).

### Event-Driven Architecture
The game emits events for every meaningful action. This creates a complete, replayable record of what happened. Events are:
- **Immutable**: Once emitted, never modified
- **Versioned**: Schema changes get new version numbers
- **Self-contained**: Each event has all context needed to understand it

### Schema as Contract
The event schema is the contract between all modules. It lives in `/docs/event-schema.md` and is the source of truth. All modules must:
- Emit events matching the schema (game-java)
- Validate incoming events against the schema (telemetry-server)
- Type their data models to match the schema (dashboard-ts, analytics-py)

### Testability
The game simulation is decoupled from rendering. You can:
- Run thousands of simulated games without a window
- Unit test combat, items, and AI deterministically
- Replay runs from telemetry for debugging

## Technology Choices

| Component | Technology | Rationale |
|-----------|------------|-----------|
| Build (Java) | Gradle | Modern, flexible, good multi-module support |
| HTTP Server | Javalin | Lightweight, simple, no framework magic |
| JSON (Java) | Jackson | Industry standard, excellent performance |
| Frontend | React + Vite | Fast dev experience, component model |
| Charts | Recharts | React-native, declarative |
| Analysis | pandas + matplotlib | De facto standard for data analysis |
