#!/usr/bin/env python3
"""
Analyze a single RogueLab run.

Usage:
    python analyze_run.py <path_to_jsonl> [--output <output_dir>]
"""

import argparse
import sys
from pathlib import Path

# Add parent to path for imports
sys.path.insert(0, str(Path(__file__).parent.parent))

from roguelab_analytics import load_run, analyze_run, generate_run_report


def main():
    parser = argparse.ArgumentParser(description="Analyze a single RogueLab run")
    parser.add_argument("filepath", type=Path, help="Path to .jsonl telemetry file")
    parser.add_argument(
        "--output", "-o", 
        type=Path, 
        default=Path("output"),
        help="Output directory for report"
    )
    args = parser.parse_args()
    
    if not args.filepath.exists():
        print(f"Error: File not found: {args.filepath}")
        sys.exit(1)
    
    print(f"Loading run from: {args.filepath}")
    run = load_run(args.filepath)
    
    print(f"Run ID: {run.run_id}")
    print(f"Player: {run.player_name} ({run.player_class})")
    print(f"Difficulty: {run.difficulty}")
    print()
    
    # Analyze
    analysis = analyze_run(run)
    
    print("=== Run Summary ===")
    print(f"Outcome: {analysis.outcome}")
    print(f"Floors Reached: {analysis.floors_reached}")
    print(f"Total Combats: {analysis.total_combats}")
    print(f"Enemies Killed: {analysis.total_enemies_killed}")
    print()
    
    print("=== Combat Stats ===")
    print(f"Damage Dealt: {analysis.damage_dealt}")
    print(f"Damage Taken: {analysis.damage_taken}")
    print(f"Damage Efficiency: {analysis.damage_efficiency:.2f}x")
    print(f"Avg Combat Turns: {analysis.avg_combat_turns:.1f}")
    print(f"Critical Hit Rate: {analysis.critical_hit_rate:.1%}")
    print()
    
    print("=== Economy ===")
    print(f"Gold Earned: {analysis.gold_earned}")
    print(f"Items Collected: {analysis.items_collected}")
    print()
    
    if not run.player_alive:
        print("=== Death ===")
        print(f"Killed by: {analysis.death_cause}")
        print(f"On floor: {analysis.death_floor}")
        print()
    
    # Generate report
    report_path = generate_run_report(run, args.output)
    print(f"Report saved to: {report_path}")


if __name__ == "__main__":
    main()
