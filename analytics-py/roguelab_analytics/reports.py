"""
Report generation for RogueLab analytics.
"""

from pathlib import Path
from typing import Optional

import matplotlib
matplotlib.use('Agg')  # Non-interactive backend
import matplotlib.pyplot as plt
import seaborn as sns

from .models import RunData
from .analyzers import (
    analyze_run, analyze_combat_efficiency, analyze_enemy_lethality,
    analyze_item_effectiveness, analyze_death_causes, runs_to_dataframe,
    damage_events_to_dataframe
)


def generate_run_report(run: RunData, output_dir: Path | str) -> Path:
    """
    Generate a detailed report for a single run.
    
    Returns path to the generated report.
    """
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    analysis = analyze_run(run)
    
    # Generate markdown report
    report_path = output_dir / f"{run.run_id}_report.md"
    
    lines = [
        f"# Run Report: {run.run_id}",
        "",
        "## Summary",
        "",
        f"| Metric | Value |",
        f"|--------|-------|",
        f"| Outcome | **{analysis.outcome}** |",
        f"| Player | {run.player_name} ({run.player_class}) |",
        f"| Difficulty | {run.difficulty} |",
        f"| Seed | {run.seed} |",
        f"| Floors Reached | {analysis.floors_reached} |",
        f"| Rooms Visited | {run.rooms_visited} |",
        "",
        "## Combat Statistics",
        "",
        f"| Metric | Value |",
        f"|--------|-------|",
        f"| Total Combats | {analysis.total_combats} |",
        f"| Enemies Killed | {analysis.total_enemies_killed} |",
        f"| Damage Dealt | {analysis.damage_dealt} |",
        f"| Damage Taken | {analysis.damage_taken} |",
        f"| Damage Efficiency | {analysis.damage_efficiency:.2f}x |",
        f"| Avg Combat Turns | {analysis.avg_combat_turns:.1f} |",
        f"| Critical Hit Rate | {analysis.critical_hit_rate:.1%} |",
        "",
        "## Economy",
        "",
        f"| Metric | Value |",
        f"|--------|-------|",
        f"| Gold Earned | {analysis.gold_earned} |",
        f"| Items Collected | {analysis.items_collected} |",
        "",
    ]
    
    # Floor breakdown
    if run.floors:
        lines.extend([
            "## Floor Breakdown",
            "",
            "| Floor | Rooms | Combats | Enemies Killed | Items Found |",
            "|-------|-------|---------|----------------|-------------|",
        ])
        for floor in run.floors:
            combats = len(floor.combats)
            kills = sum(c.enemies_killed for c in floor.combats)
            items = len(floor.items_found)
            lines.append(
                f"| {floor.floor_number} | {floor.room_count} | {combats} | {kills} | {items} |"
            )
        lines.append("")
    
    # Items collected
    if run.all_items:
        lines.extend([
            "## Items Collected",
            "",
            "| Item | Type | Rarity |",
            "|------|------|--------|",
        ])
        for item in run.all_items:
            lines.append(f"| {item.name} | {item.item_type} | {item.rarity} |")
        lines.append("")
    
    # Death info
    if not run.player_alive:
        lines.extend([
            "## Death Analysis",
            "",
            f"- **Cause**: {analysis.death_cause or 'Unknown'}",
            f"- **Floor**: {analysis.death_floor or 'Unknown'}",
            "",
        ])
    
    report_path.write_text('\n'.join(lines), encoding='utf-8')
    return report_path


def generate_batch_report(
    runs: list[RunData], 
    output_dir: Path | str,
    generate_plots: bool = True
) -> Path:
    """
    Generate a comprehensive report for multiple runs.
    
    Returns path to the generated report.
    """
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    if not runs:
        raise ValueError("No runs to analyze")
    
    # Analyze
    combat_stats = analyze_combat_efficiency(runs)
    enemy_stats = analyze_enemy_lethality(runs)
    item_stats = analyze_item_effectiveness(runs)
    death_causes = analyze_death_causes(runs)
    
    # Create DataFrames
    runs_df = runs_to_dataframe(runs)
    
    # Save CSV
    runs_df.to_csv(output_dir / "runs_summary.csv", index=False)
    
    # Generate plots
    if generate_plots:
        _generate_plots(runs, runs_df, enemy_stats, item_stats, output_dir)
    
    # Generate markdown report
    report_path = output_dir / "batch_report.md"
    
    total_runs = len(runs)
    victories = sum(1 for r in runs if r.is_victory)
    
    lines = [
        "# RogueLab Batch Analysis Report",
        "",
        "## Overview",
        "",
        f"| Metric | Value |",
        f"|--------|-------|",
        f"| Total Runs | {total_runs} |",
        f"| Victories | {victories} ({victories/total_runs:.1%}) |",
        f"| Defeats | {total_runs - victories} ({(total_runs-victories)/total_runs:.1%}) |",
        "",
        "## Combat Statistics",
        "",
        f"| Metric | Value |",
        f"|--------|-------|",
        f"| Total Combats | {combat_stats.total_combats} |",
        f"| Combat Win Rate | {combat_stats.win_rate:.1%} |",
        f"| Avg Turns/Combat | {combat_stats.avg_turns_per_combat:.1f} |",
        f"| Avg Damage Dealt | {combat_stats.avg_damage_dealt:.1f} |",
        f"| Avg Damage Taken | {combat_stats.avg_damage_taken:.1f} |",
        f"| Critical Hit Rate | {combat_stats.critical_hit_rate:.1%} |",
        f"| Most Dangerous Enemy | {combat_stats.most_dangerous_enemy or 'N/A'} |",
        "",
        "## Enemy Lethality Ranking",
        "",
        "| Enemy | Encounters | Player Deaths | Lethality |",
        "|-------|------------|---------------|-----------|",
    ]
    
    for enemy in enemy_stats[:10]:  # Top 10
        lines.append(
            f"| {enemy.enemy_type} | {enemy.encounters} | "
            f"{enemy.times_killed_player} | {enemy.lethality_score:.2%} |"
        )
    
    lines.extend([
        "",
        "## Death Causes",
        "",
        "| Enemy | Deaths |",
        "|-------|--------|",
    ])
    
    for enemy, count in list(death_causes.items())[:10]:
        lines.append(f"| {enemy} | {count} |")
    
    lines.extend([
        "",
        "## Item Statistics",
        "",
        "| Item | Rarity | Times Picked | Win Rate |",
        "|------|--------|--------------|----------|",
    ])
    
    for item in item_stats[:15]:  # Top 15
        lines.append(
            f"| {item.item_name} | {item.rarity} | "
            f"{item.times_picked} | {item.win_rate_with_item:.1%} |"
        )
    
    lines.extend([
        "",
        "## Run Statistics",
        "",
        f"| Metric | Mean | Min | Max |",
        f"|--------|------|-----|-----|",
        f"| Floors Reached | {runs_df['floors_reached'].mean():.1f} | "
        f"{runs_df['floors_reached'].min()} | {runs_df['floors_reached'].max()} |",
        f"| Enemies Killed | {runs_df['enemies_killed'].mean():.1f} | "
        f"{runs_df['enemies_killed'].min()} | {runs_df['enemies_killed'].max()} |",
        f"| Damage Dealt | {runs_df['damage_dealt'].mean():.0f} | "
        f"{runs_df['damage_dealt'].min()} | {runs_df['damage_dealt'].max()} |",
        f"| Gold Earned | {runs_df['gold_earned'].mean():.0f} | "
        f"{runs_df['gold_earned'].min()} | {runs_df['gold_earned'].max()} |",
        "",
    ])
    
    if generate_plots:
        lines.extend([
            "## Visualizations",
            "",
            "![Damage Distribution](damage_distribution.png)",
            "",
            "![Enemy Damage](enemy_damage.png)",
            "",
            "![Items by Rarity](items_by_rarity.png)",
            "",
        ])
    
    report_path.write_text('\n'.join(lines), encoding='utf-8')
    return report_path


def _generate_plots(
    runs: list[RunData],
    runs_df,
    enemy_stats,
    item_stats,
    output_dir: Path
):
    """Generate visualization plots."""
    sns.set_theme(style="whitegrid")
    
    # 1. Damage distribution
    fig, ax = plt.subplots(figsize=(10, 6))
    ax.hist(runs_df['damage_dealt'], bins=20, alpha=0.7, label='Dealt', color='green')
    ax.hist(runs_df['damage_taken'], bins=20, alpha=0.7, label='Taken', color='red')
    ax.set_xlabel('Damage')
    ax.set_ylabel('Frequency')
    ax.set_title('Damage Distribution Across Runs')
    ax.legend()
    fig.savefig(output_dir / 'damage_distribution.png', dpi=150, bbox_inches='tight')
    plt.close(fig)
    
    # 2. Enemy damage to player
    if enemy_stats:
        fig, ax = plt.subplots(figsize=(10, 6))
        enemies = [e.enemy_type for e in enemy_stats[:10]]
        damage = [e.total_damage_dealt_to_player for e in enemy_stats[:10]]
        ax.barh(enemies, damage, color='crimson')
        ax.set_xlabel('Total Damage to Player')
        ax.set_title('Enemy Damage Ranking')
        ax.invert_yaxis()
        fig.savefig(output_dir / 'enemy_damage.png', dpi=150, bbox_inches='tight')
        plt.close(fig)
    
    # 3. Items by rarity
    if item_stats:
        fig, ax = plt.subplots(figsize=(8, 6))
        rarity_counts = {}
        for item in item_stats:
            rarity_counts[item.rarity] = rarity_counts.get(item.rarity, 0) + item.times_picked
        
        rarities = list(rarity_counts.keys())
        counts = list(rarity_counts.values())
        colors = {
            'COMMON': '#808080',
            'UNCOMMON': '#00ff00', 
            'RARE': '#0080ff',
            'EPIC': '#8000ff',
            'LEGENDARY': '#ff8000'
        }
        bar_colors = [colors.get(r, '#808080') for r in rarities]
        
        ax.bar(rarities, counts, color=bar_colors)
        ax.set_xlabel('Rarity')
        ax.set_ylabel('Times Picked')
        ax.set_title('Items Collected by Rarity')
        fig.savefig(output_dir / 'items_by_rarity.png', dpi=150, bbox_inches='tight')
        plt.close(fig)
