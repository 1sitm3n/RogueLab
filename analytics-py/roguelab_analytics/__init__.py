"""
RogueLab Analytics - Telemetry analysis for RogueLab game runs.
"""

__version__ = "0.1.0"

from .loader import load_run, load_runs_from_directory
from .models import RunData, CombatEvent, DamageEvent
from .analyzers import (
    analyze_run,
    analyze_combat_efficiency,
    analyze_item_effectiveness,
    analyze_enemy_lethality,
    analyze_death_causes,
)
from .reports import generate_run_report, generate_batch_report
