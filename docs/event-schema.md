# RogueLab Event Schema

This document defines the telemetry event schema used throughout RogueLab. All modules must conform to these definitions.

## Schema Version

**Current Version**: `1.0.0`

Schema versioning follows semantic versioning:
- **MAJOR**: Breaking changes (field removal, type changes)
- **MINOR**: Additive changes (new optional fields, new event types)
- **PATCH**: Documentation or validation fixes

## Base Event Structure

Every event MUST include these fields:

```json
{
  "eventType": "EVENT_TYPE_NAME",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:30:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 42,
  "payload": { }
}
```

### Field Definitions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `eventType` | string | Yes | Event type identifier (SCREAMING_SNAKE_CASE) |
| `eventVersion` | string | Yes | Semantic version of this event's schema |
| `timestamp` | string (ISO 8601) | Yes | When the event occurred |
| `runId` | string (UUID) | Yes | Unique identifier for this game run |
| `tick` | integer | Yes | Game tick/turn number when event occurred |
| `payload` | object | Yes | Event-specific data (may be empty `{}`) |

## Event Types

### RUN_STARTED

Emitted once at the beginning of each run.

```json
{
  "eventType": "RUN_STARTED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:30:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 0,
  "payload": {
    "seed": 12345678,
    "gameVersion": "0.1.0",
    "playerClass": "WARRIOR",
    "difficulty": "NORMAL",
    "playerName": "Hero"
  }
}
```

| Payload Field | Type | Description |
|---------------|------|-------------|
| `seed` | integer | Random seed for this run (enables replay) |
| `gameVersion` | string | Version of the game engine |
| `playerClass` | string | Player's chosen class |
| `difficulty` | string | Difficulty setting |
| `playerName` | string | Player-chosen name |

---

### RUN_ENDED

Emitted once when a run concludes (victory or defeat).

```json
{
  "eventType": "RUN_ENDED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T15:45:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 1847,
  "payload": {
    "outcome": "VICTORY",
    "finalFloor": 10,
    "finalScore": 12500,
    "totalGold": 847,
    "itemsCollected": 12,
    "enemiesDefeated": 45,
    "durationSeconds": 4500
  }
}
```

| Payload Field | Type | Description |
|---------------|------|-------------|
| `outcome` | string | `VICTORY`, `DEFEAT`, or `ABANDONED` |
| `finalFloor` | integer | Deepest floor reached |
| `finalScore` | integer | Final score |
| `totalGold` | integer | Total gold collected during run |
| `itemsCollected` | integer | Number of items picked up |
| `enemiesDefeated` | integer | Total enemies killed |
| `durationSeconds` | integer | Real-time duration of the run |

---

### ROOM_ENTERED

Emitted when the player enters a new room.

```json
{
  "eventType": "ROOM_ENTERED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:32:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 150,
  "payload": {
    "floor": 2,
    "roomId": "room_2_3",
    "roomType": "COMBAT",
    "enemyCount": 3,
    "hasChest": false,
    "playerHealthPercent": 0.85
  }
}
```

---

### COMBAT_STARTED

Emitted when combat begins in a room.

```json
{
  "eventType": "COMBAT_STARTED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:32:01.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 151,
  "payload": {
    "roomId": "room_2_3",
    "enemies": [
      {"enemyId": "goblin_1", "enemyType": "GOBLIN", "health": 20, "attack": 5},
      {"enemyId": "goblin_2", "enemyType": "GOBLIN", "health": 20, "attack": 5},
      {"enemyId": "orc_1", "enemyType": "ORC", "health": 40, "attack": 8}
    ],
    "playerHealth": 85,
    "playerMaxHealth": 100
  }
}
```

---

### COMBAT_ENDED

Emitted when combat concludes.

```json
{
  "eventType": "COMBAT_ENDED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:35:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 210,
  "payload": {
    "roomId": "room_2_3",
    "outcome": "VICTORY",
    "turnsElapsed": 12,
    "damageDealt": 120,
    "damageTaken": 25,
    "playerHealthRemaining": 60,
    "goldDropped": 15,
    "experienceGained": 30
  }
}
```

---

### DAMAGE_DEALT

Emitted for each instance of damage during combat.

```json
{
  "eventType": "DAMAGE_DEALT",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:33:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 165,
  "payload": {
    "sourceId": "player",
    "sourceType": "PLAYER",
    "targetId": "goblin_1",
    "targetType": "ENEMY",
    "baseDamage": 12,
    "finalDamage": 15,
    "damageType": "PHYSICAL",
    "isCritical": true,
    "targetHealthBefore": 20,
    "targetHealthAfter": 5,
    "targetKilled": false
  }
}
```

---

### STATUS_APPLIED

Emitted when a status effect is applied to any entity.

```json
{
  "eventType": "STATUS_APPLIED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:34:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 180,
  "payload": {
    "targetId": "player",
    "targetType": "PLAYER",
    "statusType": "POISON",
    "duration": 3,
    "stacks": 2,
    "sourceId": "goblin_2",
    "sourceType": "ENEMY"
  }
}
```

---

### ITEM_PICKED

Emitted when the player picks up an item.

```json
{
  "eventType": "ITEM_PICKED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:36:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 220,
  "payload": {
    "itemId": "sword_of_fire_001",
    "itemType": "WEAPON",
    "itemName": "Sword of Fire",
    "rarity": "RARE",
    "floor": 2,
    "source": "CHEST",
    "stats": {
      "attack": 8,
      "bonusDamageType": "FIRE",
      "bonusDamage": 3
    }
  }
}
```

---

### SHOP_PURCHASED

Emitted when player buys something from a shop.

```json
{
  "eventType": "SHOP_PURCHASED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:40:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 300,
  "payload": {
    "itemId": "health_potion_002",
    "itemName": "Health Potion",
    "itemType": "CONSUMABLE",
    "price": 50,
    "playerGoldBefore": 120,
    "playerGoldAfter": 70,
    "floor": 3
  }
}
```

---

### BOSS_DEFEATED

Emitted when a boss enemy is killed.

```json
{
  "eventType": "BOSS_DEFEATED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T15:30:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 1500,
  "payload": {
    "bossId": "dragon_boss",
    "bossName": "Ancient Dragon",
    "floor": 5,
    "combatTurns": 25,
    "damageDealt": 500,
    "damageTaken": 60,
    "playerHealthRemaining": 40,
    "rewardGold": 200,
    "rewardItem": {
      "itemId": "dragon_scale_armor",
      "itemName": "Dragon Scale Armor",
      "rarity": "LEGENDARY"
    }
  }
}
```

---

### PLAYER_DIED

Emitted when the player's health reaches zero.

```json
{
  "eventType": "PLAYER_DIED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T15:45:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 1847,
  "payload": {
    "causeType": "ENEMY",
    "causeId": "demon_lord",
    "causeName": "Demon Lord",
    "damageType": "FIRE",
    "finalBlow": 35,
    "floor": 8,
    "itemsHeld": ["sword_of_ice", "shield_of_valor", "health_ring"],
    "gold": 450,
    "enemiesKilledThisRun": 42
  }
}
```

---

### PLAYER_HEALED

Emitted when player health is restored.

```json
{
  "eventType": "PLAYER_HEALED",
  "eventVersion": "1.0.0",
  "timestamp": "2025-01-15T14:50:00.000Z",
  "runId": "550e8400-e29b-41d4-a716-446655440000",
  "tick": 450,
  "payload": {
    "source": "CONSUMABLE",
    "sourceId": "health_potion_002",
    "amount": 30,
    "healthBefore": 45,
    "healthAfter": 75,
    "maxHealth": 100,
    "overheal": 0
  }
}
```

---

## File Format

Telemetry is written as JSON Lines (`.jsonl`), one event per line:

```
{"eventType":"RUN_STARTED","eventVersion":"1.0.0","timestamp":"2025-01-15T14:30:00.000Z",...}
{"eventType":"ROOM_ENTERED","eventVersion":"1.0.0","timestamp":"2025-01-15T14:32:00.000Z",...}
{"eventType":"COMBAT_STARTED","eventVersion":"1.0.0","timestamp":"2025-01-15T14:32:01.000Z",...}
```

### File Naming Convention

```
run_{runId}_{timestamp}.jsonl
```

Example: `run_550e8400-e29b-41d4-a716-446655440000_2025-01-15T14-30-00.jsonl`

---

## Schema Evolution Rules

1. **Never remove fields** - mark as deprecated, keep for backward compatibility
2. **Never change field types** - create a new field instead
3. **New fields must be optional** - provide sensible defaults for old consumers
4. **Bump `eventVersion`** when event structure changes
5. **Document all changes** in a changelog section below

---

## Changelog

### 1.0.0 (Initial Release)
- Defined all core event types
- Established base event structure
