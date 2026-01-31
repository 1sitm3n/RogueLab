#!/usr/bin/env python3
"""
Generate a batch analysis report from multiple RogueLab runs.

Usage:
    python batch_report.py <runs_directory> [--output <output_dir>] [--no-plots]
"""

import argparse
import sys
from pathlib import Path

# Add parent to path for imports
sys.path.insert(0, str(Path(__file__).parent.parent))

from roguelab_analytics import (
    load_runs_from_directory, 
    generate_batch_report,
    analyze_combat_efficiency,
    analyze_enemy_lethality,
    analyze_death_causes
)


def main():
    parser = argparse.ArgumentParser(
        description="Generate batch analysis report for RogueLab runs"
    )
    parser.add_argument(
        "directory", 
        type=Path, 
        help="Directory containing .jsonl telemetry files"
    )
    parser.add_argument(
        "--output", "-o",
        type=Path,
        default=Path("output"),
        help="Output directory for reports"
    )
    parser.add_argument(
        "--no-plots",
        action="store_true",
        help="Skip generating plots"
    )
    args = parser.parse_args()
    
    if not args.directory.exists():
        print(f"Error: Directory not found: {args.directory}")
        sys.exit(1)
    
    print(f"Loading runs from: {args.directory}")
    runs = load_runs_from_directory(args.directory)
    
    if not runs:
        print("No runs found!")
        sys.exit(1)
    
    print(f"Loaded {len(runs)} runs")
    print()
    
    # Quick summary
    victories = sum(1 for r in runs if r.is_victory)
    print("=== Quick Summary ===")
    print(f"Total Runs: {len(runs)}")
    print(f"Victories: {victories} ({victories/len(runs):.1%})")
    print(f"Defeats: {len(runs) - victories}")
    print()
    
    # Combat analysis
    combat = analyze_combat_efficiency(runs)
    print("=== Combat Analysis ===")
    print(f"Total Combats: {combat.total_combats}")
    print(f"Combat Win Rate: {combat.win_rate:.1%}")
    print(f"Avg Turns/Combat: {combat.avg_turns_per_combat:.1f}")
    print(f"Critical Hit Rate: {combat.critical_hit_rate:.1%}")
    if combat.most_dangerous_enemy:
        print(f"Most Dangerous Enemy: {combat.most_dangerous_enemy}")
    print()
    
    # Enemy lethality
    enemies = analyze_enemy_lethality(runs)
    print("=== Enemy Lethality (Top 5) ===")
    for enemy in enemies[:5]:
        print(f"  {enemy.enemy_type}: {enemy.lethality_score:.2%} "
              f"({enemy.times_killed_player} player deaths)")
    print()
    
    # Death causes
    deaths = analyze_death_causes(runs)
    if deaths:
        print("=== Top Death Causes ===")
        for enemy, count in list(deaths.items())[:5]:
            print(f"  {enemy}: {count} deaths")
        print()
    
    # Generate full report
    print("Generating report...")
    report_path = generate_batch_report(
        runs, 
        args.output,
        generate_plots=not args.no_plots
    )
    print(f"Report saved to: {report_path}")
    print(f"CSV saved to: {args.output / 'runs_summary.csv'}")
    
    if not args.no_plots:
        print(f"Plots saved to: {args.output}")


if __name__ == "__main__":
    main()
