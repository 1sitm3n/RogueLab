"""
Analysis functions for RogueLab telemetry data.
"""

from collections import defaultdict
from dataclasses import dataclass
from typing import Optional

import pandas as pd

from .models import RunData, CombatEvent, DamageEvent


@dataclass
class RunAnalysis:
    """Analysis results for a single run."""
    run_id: str
    outcome: str
    floors_reached: int
    total_combats: int
    total_enemies_killed: int
    damage_dealt: int
    damage_taken: int
    damage_efficiency: float
    gold_earned: int
    items_collected: int
    avg_combat_turns: float
    critical_hit_rate: float
    death_cause: Optional[str] = None
    death_floor: Optional[int] = None


@dataclass
class EnemyStats:
    """Statistics for an enemy type."""
    enemy_type: str
    encounters: int
    total_kills: int
    times_killed_player: int
    total_damage_dealt_to_player: int
    total_damage_taken: int
    avg_damage_per_encounter: float
    lethality_score: float  # How often this enemy kills the player


@dataclass
class ItemStats:
    """Statistics for an item."""
    item_name: str
    item_type: str
    rarity: str
    times_picked: int
    pick_rate: float  # Across all runs
    win_rate_with_item: float  # Win rate when picked


@dataclass
class CombatAnalysis:
    """Analysis of combat efficiency."""
    total_combats: int
    victories: int
    defeats: int
    win_rate: float
    avg_turns_per_combat: float
    avg_damage_dealt: float
    avg_damage_taken: float
    total_critical_hits: int
    critical_hit_rate: float
    most_dangerous_enemy: Optional[str] = None


def analyze_run(run: RunData) -> RunAnalysis:
    """
    Analyze a single run and return summary statistics.
    """
    # Calculate critical hit rate
    total_player_attacks = sum(
        1 for d in run.all_damage_events 
        if d.source_type == "PLAYER"
    )
    critical_hits = sum(
        1 for d in run.all_damage_events 
        if d.source_type == "PLAYER" and d.critical
    )
    crit_rate = critical_hits / total_player_attacks if total_player_attacks > 0 else 0.0
    
    # Calculate average combat turns
    total_turns = sum(c.turns_elapsed for c in run.all_combats)
    avg_turns = total_turns / len(run.all_combats) if run.all_combats else 0.0
    
    # Determine death cause if applicable
    death_cause = None
    death_floor = None
    if not run.player_alive:
        # Find the last enemy that dealt damage before death
        player_damage = [d for d in run.all_damage_events if d.target_type == "PLAYER"]
        if player_damage:
            last_hit = player_damage[-1]
            death_cause = last_hit.source_type
            death_floor = run.final_floor
    
    return RunAnalysis(
        run_id=run.run_id,
        outcome=run.end_reason or "UNKNOWN",
        floors_reached=run.final_floor,
        total_combats=len(run.all_combats),
        total_enemies_killed=run.total_enemies_killed,
        damage_dealt=run.total_damage_dealt,
        damage_taken=run.total_damage_taken,
        damage_efficiency=run.damage_efficiency,
        gold_earned=run.total_gold_earned,
        items_collected=run.total_items_collected,
        avg_combat_turns=avg_turns,
        critical_hit_rate=crit_rate,
        death_cause=death_cause,
        death_floor=death_floor
    )


def analyze_combat_efficiency(runs: list[RunData]) -> CombatAnalysis:
    """
    Analyze combat efficiency across multiple runs.
    """
    all_combats = []
    all_damage = []
    
    for run in runs:
        all_combats.extend(run.all_combats)
        all_damage.extend(run.all_damage_events)
    
    if not all_combats:
        return CombatAnalysis(
            total_combats=0, victories=0, defeats=0, win_rate=0.0,
            avg_turns_per_combat=0.0, avg_damage_dealt=0.0, avg_damage_taken=0.0,
            total_critical_hits=0, critical_hit_rate=0.0
        )
    
    victories = sum(1 for c in all_combats if c.outcome == "VICTORY")
    defeats = sum(1 for c in all_combats if c.outcome == "DEFEAT")
    
    # Damage stats
    player_attacks = [d for d in all_damage if d.source_type == "PLAYER"]
    critical_hits = sum(1 for d in player_attacks if d.critical)
    
    # Find most dangerous enemy
    enemy_damage = defaultdict(int)
    for d in all_damage:
        if d.target_type == "PLAYER":
            enemy_damage[d.source_type] += d.final_damage
    
    most_dangerous = max(enemy_damage.items(), key=lambda x: x[1])[0] if enemy_damage else None
    
    return CombatAnalysis(
        total_combats=len(all_combats),
        victories=victories,
        defeats=defeats,
        win_rate=victories / len(all_combats),
        avg_turns_per_combat=sum(c.turns_elapsed for c in all_combats) / len(all_combats),
        avg_damage_dealt=sum(c.total_damage_dealt for c in all_combats) / len(all_combats),
        avg_damage_taken=sum(c.total_damage_taken for c in all_combats) / len(all_combats),
        total_critical_hits=critical_hits,
        critical_hit_rate=critical_hits / len(player_attacks) if player_attacks else 0.0,
        most_dangerous_enemy=most_dangerous
    )


def analyze_enemy_lethality(runs: list[RunData]) -> list[EnemyStats]:
    """
    Analyze enemy lethality across multiple runs.
    
    Returns list of EnemyStats sorted by lethality score (descending).
    """
    enemy_data = defaultdict(lambda: {
        'encounters': 0,
        'kills': 0,
        'player_kills': 0,
        'damage_to_player': 0,
        'damage_taken': 0
    })
    
    total_runs = len(runs)
    deaths_by_enemy = defaultdict(int)
    
    for run in runs:
        # Track which enemies were encountered
        seen_enemies = set()
        
        for combat in run.all_combats:
            for enemy in combat.enemies:
                enemy_type = enemy.type
                if enemy_type not in seen_enemies:
                    enemy_data[enemy_type]['encounters'] += 1
                    seen_enemies.add(enemy_type)
        
        # Count kills from damage events
        for damage in run.all_damage_events:
            if damage.source_type == "PLAYER" and damage.killed:
                enemy_data[damage.target_type]['kills'] += 1
                enemy_data[damage.target_type]['damage_taken'] += damage.final_damage
            elif damage.target_type == "PLAYER":
                enemy_data[damage.source_type]['damage_to_player'] += damage.final_damage
                if damage.killed:
                    enemy_data[damage.source_type]['player_kills'] += 1
                    deaths_by_enemy[damage.source_type] += 1
    
    # Build stats
    results = []
    for enemy_type, data in enemy_data.items():
        encounters = data['encounters']
        lethality = data['player_kills'] / encounters if encounters > 0 else 0.0
        
        results.append(EnemyStats(
            enemy_type=enemy_type,
            encounters=encounters,
            total_kills=data['kills'],
            times_killed_player=data['player_kills'],
            total_damage_dealt_to_player=data['damage_to_player'],
            total_damage_taken=data['damage_taken'],
            avg_damage_per_encounter=data['damage_to_player'] / encounters if encounters > 0 else 0.0,
            lethality_score=lethality
        ))
    
    # Sort by lethality
    results.sort(key=lambda x: x.lethality_score, reverse=True)
    return results


def analyze_item_effectiveness(runs: list[RunData]) -> list[ItemStats]:
    """
    Analyze item pick rates and effectiveness.
    
    Returns list of ItemStats sorted by pick rate (descending).
    """
    item_data = defaultdict(lambda: {
        'type': '',
        'rarity': '',
        'picks': 0,
        'runs_with_item': set(),
        'wins_with_item': 0
    })
    
    total_runs = len(runs)
    
    for run in runs:
        items_in_run = set()
        
        for item in run.all_items:
            key = item.name
            item_data[key]['type'] = item.item_type
            item_data[key]['rarity'] = item.rarity
            item_data[key]['picks'] += 1
            items_in_run.add(key)
        
        # Track wins with each item
        for item_name in items_in_run:
            item_data[item_name]['runs_with_item'].add(run.run_id)
            if run.is_victory:
                item_data[item_name]['wins_with_item'] += 1
    
    # Build stats
    results = []
    for item_name, data in item_data.items():
        runs_with = len(data['runs_with_item'])
        pick_rate = runs_with / total_runs if total_runs > 0 else 0.0
        win_rate = data['wins_with_item'] / runs_with if runs_with > 0 else 0.0
        
        results.append(ItemStats(
            item_name=item_name,
            item_type=data['type'],
            rarity=data['rarity'],
            times_picked=data['picks'],
            pick_rate=pick_rate,
            win_rate_with_item=win_rate
        ))
    
    # Sort by pick rate
    results.sort(key=lambda x: x.times_picked, reverse=True)
    return results


def analyze_death_causes(runs: list[RunData]) -> dict[str, int]:
    """
    Analyze causes of death across runs.
    
    Returns dict mapping enemy type to death count.
    """
    deaths = defaultdict(int)
    
    for run in runs:
        if run.player_alive:
            continue
        
        # Find last enemy that dealt damage
        player_damage = [d for d in run.all_damage_events if d.target_type == "PLAYER"]
        if player_damage:
            killer = player_damage[-1].source_type
            deaths[killer] += 1
        else:
            deaths["UNKNOWN"] += 1
    
    return dict(sorted(deaths.items(), key=lambda x: x[1], reverse=True))


def runs_to_dataframe(runs: list[RunData]) -> pd.DataFrame:
    """
    Convert runs to a pandas DataFrame for analysis.
    """
    data = []
    for run in runs:
        analysis = analyze_run(run)
        data.append({
            'run_id': run.run_id,
            'seed': run.seed,
            'difficulty': run.difficulty,
            'player_class': run.player_class,
            'outcome': run.end_reason,
            'is_victory': run.is_victory,
            'floors_reached': run.final_floor,
            'rooms_visited': run.rooms_visited,
            'rooms_cleared': run.rooms_cleared,
            'enemies_killed': run.total_enemies_killed,
            'damage_dealt': run.total_damage_dealt,
            'damage_taken': run.total_damage_taken,
            'damage_efficiency': run.damage_efficiency,
            'gold_earned': run.total_gold_earned,
            'gold_spent': run.total_gold_spent,
            'items_collected': run.total_items_collected,
            'combats': len(run.all_combats),
            'avg_combat_turns': analysis.avg_combat_turns,
            'critical_hit_rate': analysis.critical_hit_rate,
        })
    
    return pd.DataFrame(data)


def damage_events_to_dataframe(runs: list[RunData]) -> pd.DataFrame:
    """
    Convert all damage events to a DataFrame.
    """
    data = []
    for run in runs:
        for d in run.all_damage_events:
            data.append({
                'run_id': run.run_id,
                'tick': d.tick,
                'source_id': d.source_id,
                'source_type': d.source_type,
                'target_id': d.target_id,
                'target_type': d.target_type,
                'base_damage': d.base_damage,
                'final_damage': d.final_damage,
                'damage_type': d.damage_type,
                'critical': d.critical,
                'killed': d.killed,
            })
    
    return pd.DataFrame(data)
