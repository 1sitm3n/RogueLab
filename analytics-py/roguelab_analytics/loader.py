"""
Loader for RogueLab telemetry files.
"""

import json
from datetime import datetime
from pathlib import Path
from typing import Iterator

from .models import (
    GameEvent, RunData, FloorData, CombatEvent, DamageEvent,
    EnemySnapshot, ItemSnapshot
)


def parse_timestamp(ts: str) -> datetime:
    """Parse ISO timestamp string."""
    # Handle various timestamp formats
    ts = ts.rstrip('Z')
    if '.' in ts:
        # Truncate nanoseconds to microseconds
        parts = ts.split('.')
        micros = parts[1][:6]
        ts = f"{parts[0]}.{micros}"
        return datetime.fromisoformat(ts)
    return datetime.fromisoformat(ts)


def load_events(filepath: Path) -> Iterator[GameEvent]:
    """Load events from a JSONL file."""
    with open(filepath, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            data = json.loads(line)
            yield GameEvent(
                event_type=data['event_type'],
                event_version=data['event_version'],
                timestamp=parse_timestamp(data['timestamp']),
                run_id=data['run_id'],
                tick=data['tick'],
                payload=data.get('payload', {})
            )


def load_run(filepath: Path | str) -> RunData:
    """
    Load and parse a complete run from a JSONL file.
    
    Args:
        filepath: Path to the .jsonl telemetry file
        
    Returns:
        RunData object with all parsed information
    """
    filepath = Path(filepath)
    events = list(load_events(filepath))
    
    if not events:
        raise ValueError(f"No events found in {filepath}")
    
    # Initialize from RUN_STARTED
    run_started = next((e for e in events if e.event_type == "RUN_STARTED"), None)
    if not run_started:
        raise ValueError(f"No RUN_STARTED event found in {filepath}")
    
    payload = run_started.payload
    run = RunData(
        run_id=run_started.run_id,
        seed=payload.get('seed', 0),
        version=payload.get('version', 'unknown'),
        difficulty=payload.get('difficulty', 'NORMAL'),
        player_name=payload.get('player_name', 'Unknown'),
        player_class=payload.get('player_class', 'UNKNOWN'),
        starting_health=payload.get('starting_health', 100),
        events=events
    )
    
    # Process events in order
    current_floor: FloorData | None = None
    current_combat: CombatEvent | None = None
    current_combat_floor = 0
    
    for event in events:
        p = event.payload
        
        if event.event_type == "FLOOR_ENTERED":
            current_floor = FloorData(
                floor_number=p.get('floor_number', 1),
                room_count=p.get('room_count', 0),
                is_boss_floor=p.get('is_boss_floor', False),
                room_types=p.get('room_types', [])
            )
            run.floors.append(current_floor)
            
        elif event.event_type == "COMBAT_STARTED":
            enemies = [
                EnemySnapshot(
                    id=e.get('id', ''),
                    type=e.get('type', ''),
                    health=e.get('health', 0),
                    max_health=e.get('max_health', 0),
                    attack=e.get('attack', 0),
                    defense=e.get('defense', 0)
                )
                for e in p.get('enemies', [])
            ]
            current_combat = CombatEvent(
                room_id=p.get('room_id', ''),
                floor_number=current_floor.floor_number if current_floor else 1,
                enemies=enemies,
                outcome='',
                turns_elapsed=0,
                total_damage_dealt=0,
                total_damage_taken=0,
                enemies_killed=0,
                gold_earned=0,
                experience_gained=0
            )
            current_combat_floor = current_floor.floor_number if current_floor else 1
            
        elif event.event_type == "DAMAGE_DEALT":
            damage = DamageEvent(
                tick=event.tick,
                source_id=p.get('source_id', ''),
                source_type=p.get('source_type', ''),
                target_id=p.get('target_id', ''),
                target_type=p.get('target_type', ''),
                base_damage=p.get('base_damage', 0),
                final_damage=p.get('final_damage', 0),
                damage_type=p.get('damage_type', 'PHYSICAL'),
                critical=p.get('critical', False),
                health_before=p.get('health_before', 0),
                health_after=p.get('health_after', 0),
                killed=p.get('killed', False)
            )
            run.all_damage_events.append(damage)
            if current_combat:
                current_combat.damage_events.append(damage)
                
        elif event.event_type == "COMBAT_ENDED":
            if current_combat:
                current_combat.outcome = p.get('outcome', 'UNKNOWN')
                current_combat.turns_elapsed = p.get('turns_elapsed', 0)
                current_combat.total_damage_dealt = p.get('total_damage_dealt', 0)
                current_combat.total_damage_taken = p.get('total_damage_taken', 0)
                current_combat.enemies_killed = p.get('enemies_killed', 0)
                current_combat.gold_earned = p.get('gold_earned', 0)
                current_combat.experience_gained = p.get('experience_gained', 0)
                
                run.all_combats.append(current_combat)
                if current_floor:
                    current_floor.combats.append(current_combat)
                current_combat = None
                
        elif event.event_type == "ITEM_PICKED":
            item = ItemSnapshot(
                id=p.get('item_id', ''),
                name=p.get('item_name', ''),
                item_type=p.get('item_type', ''),
                rarity=p.get('rarity', 'COMMON'),
                value=p.get('value', 0),
                stats=p.get('stats', {})
            )
            run.all_items.append(item)
            if current_floor:
                current_floor.items_found.append(item)
                
        elif event.event_type == "RUN_ENDED":
            run.end_reason = p.get('end_reason', 'UNKNOWN')
            run.final_floor = p.get('final_floor', 0)
            run.total_enemies_killed = p.get('enemies_killed', 0)
            run.total_bosses_killed = p.get('bosses_killed', 0)
            run.total_damage_dealt = p.get('damage_dealt', 0)
            run.total_damage_taken = p.get('damage_taken', 0)
            run.total_gold_earned = p.get('gold_earned', 0)
            run.total_gold_spent = p.get('gold_spent', 0)
            run.total_items_collected = p.get('items_collected', 0)
            run.rooms_visited = p.get('rooms_visited', 0)
            run.rooms_cleared = p.get('rooms_cleared', 0)
            run.player_alive = p.get('player_alive', True)
    
    return run


def load_runs_from_directory(directory: Path | str) -> list[RunData]:
    """
    Load all runs from a directory of JSONL files.
    
    Args:
        directory: Path to directory containing .jsonl files
        
    Returns:
        List of RunData objects
    """
    directory = Path(directory)
    runs = []
    
    for filepath in sorted(directory.glob("*.jsonl")):
        try:
            run = load_run(filepath)
            runs.append(run)
        except Exception as e:
            print(f"Warning: Failed to load {filepath}: {e}")
    
    return runs
