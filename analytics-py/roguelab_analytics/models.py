"""
Data models for RogueLab telemetry events.
"""

from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional
from enum import Enum


class EventType(Enum):
    RUN_STARTED = "RUN_STARTED"
    RUN_ENDED = "RUN_ENDED"
    FLOOR_ENTERED = "FLOOR_ENTERED"
    ROOM_ENTERED = "ROOM_ENTERED"
    ROOM_CLEARED = "ROOM_CLEARED"
    COMBAT_STARTED = "COMBAT_STARTED"
    COMBAT_ENDED = "COMBAT_ENDED"
    DAMAGE_DEALT = "DAMAGE_DEALT"
    ITEM_PICKED = "ITEM_PICKED"
    SHOP_PURCHASED = "SHOP_PURCHASED"
    PLAYER_RESTED = "PLAYER_RESTED"
    PLAYER_LEVEL_UP = "PLAYER_LEVEL_UP"
    PLAYER_DIED = "PLAYER_DIED"


@dataclass
class GameEvent:
    """Base class for all game events."""
    event_type: str
    event_version: str
    timestamp: datetime
    run_id: str
    tick: int
    payload: dict


@dataclass
class EnemySnapshot:
    """Snapshot of enemy state."""
    id: str
    type: str
    health: int
    max_health: int
    attack: int = 0
    defense: int = 0


@dataclass
class ItemSnapshot:
    """Snapshot of item."""
    id: str
    name: str
    item_type: str
    rarity: str
    value: int = 0
    stats: dict = field(default_factory=dict)


@dataclass
class DamageEvent:
    """Represents a single damage instance."""
    tick: int
    source_id: str
    source_type: str
    target_id: str
    target_type: str
    base_damage: int
    final_damage: int
    damage_type: str
    critical: bool
    health_before: int
    health_after: int
    killed: bool


@dataclass
class CombatEvent:
    """Represents a complete combat encounter."""
    room_id: str
    floor_number: int
    enemies: list[EnemySnapshot]
    outcome: str
    turns_elapsed: int
    total_damage_dealt: int
    total_damage_taken: int
    enemies_killed: int
    gold_earned: int
    experience_gained: int
    damage_events: list[DamageEvent] = field(default_factory=list)


@dataclass
class FloorData:
    """Data for a single floor."""
    floor_number: int
    room_count: int
    is_boss_floor: bool
    room_types: list[str]
    combats: list[CombatEvent] = field(default_factory=list)
    items_found: list[ItemSnapshot] = field(default_factory=list)


@dataclass
class RunData:
    """Complete data for a single run."""
    run_id: str
    seed: int
    version: str
    difficulty: str
    player_name: str
    player_class: str
    starting_health: int
    
    # Outcome
    end_reason: Optional[str] = None
    final_floor: int = 0
    player_alive: bool = True
    
    # Aggregated stats
    total_enemies_killed: int = 0
    total_bosses_killed: int = 0
    total_damage_dealt: int = 0
    total_damage_taken: int = 0
    total_gold_earned: int = 0
    total_gold_spent: int = 0
    total_items_collected: int = 0
    rooms_visited: int = 0
    rooms_cleared: int = 0
    
    # Detailed data
    floors: list[FloorData] = field(default_factory=list)
    all_combats: list[CombatEvent] = field(default_factory=list)
    all_damage_events: list[DamageEvent] = field(default_factory=list)
    all_items: list[ItemSnapshot] = field(default_factory=list)
    events: list[GameEvent] = field(default_factory=list)
    
    @property
    def is_victory(self) -> bool:
        return self.end_reason == "VICTORY"
    
    @property
    def damage_efficiency(self) -> float:
        """Ratio of damage dealt to damage taken."""
        if self.total_damage_taken == 0:
            return float('inf') if self.total_damage_dealt > 0 else 0.0
        return self.total_damage_dealt / self.total_damage_taken
    
    @property
    def kill_rate(self) -> float:
        """Enemies killed per room visited."""
        if self.rooms_visited == 0:
            return 0.0
        return self.total_enemies_killed / self.rooms_visited
