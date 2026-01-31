# RogueLab Analytics

Python analytics pipeline for analyzing RogueLab game telemetry.

## Installation

```bash
cd analytics-py
pip install -r requirements.txt
```

## Usage

### Analyze a Single Run

```bash
python scripts/analyze_run.py ../game-java/runs/run_123456.jsonl
```

Output:
- Console summary
- Markdown report in `output/`

### Batch Analysis

```bash
python scripts/batch_report.py ../game-java/runs/
```

Output:
- Console summary
- `output/batch_report.md` - Full markdown report
- `output/runs_summary.csv` - CSV with all run data
- `output/*.png` - Visualization plots

### Options

```bash
# Custom output directory
python scripts/batch_report.py ../game-java/runs/ --output ./reports

# Skip plot generation
python scripts/batch_report.py ../game-java/runs/ --no-plots
```

## Package API

```python
from roguelab_analytics import (
    load_run,
    load_runs_from_directory,
    analyze_run,
    analyze_combat_efficiency,
    analyze_enemy_lethality,
    analyze_item_effectiveness,
    analyze_death_causes,
    generate_run_report,
    generate_batch_report,
)

# Load a single run
run = load_run("path/to/run.jsonl")

# Load all runs from directory
runs = load_runs_from_directory("path/to/runs/")

# Analyze
analysis = analyze_run(run)
print(f"Damage efficiency: {analysis.damage_efficiency}x")

# Generate reports
generate_run_report(run, "output/")
generate_batch_report(runs, "output/")
```

## Analysis Capabilities

### Run Analysis
- Outcome (victory/defeat)
- Floors reached
- Combat statistics (damage dealt/taken, efficiency)
- Critical hit rate
- Death cause analysis

### Enemy Lethality
- Encounter frequency
- Player death count per enemy
- Damage dealt to player
- Lethality score (deaths per encounter)

### Item Effectiveness
- Pick rate across runs
- Win rate correlation
- Rarity distribution

### Combat Efficiency
- Win rate
- Average turns per combat
- Damage statistics
- Critical hit analysis

## Output Formats

- **Markdown** - Human-readable reports
- **CSV** - Data for spreadsheet analysis
- **PNG** - Visualization plots
