# Step 10: Balance Tuning

## New Files
- `game/BalanceConfig.java` - Centralized balance configuration
- `domain/EnemyFactory.java` - Updated to use BalanceConfig
- `domain/EnemyType.java` - Updated with all enemy types including bosses
- `domain/PlayerFactory.java` - Updated to use BalanceConfig
- `combat/CombatEngine.java` - Updated to use BalanceConfig

## Balance Changes

### Problem Analysis (from telemetry data)
- GOBLIN_KING had 100% lethality (too hard)
- Regular enemies had 0% lethality (too easy)
- Damage ratio 25:10 showed player was overpowered

### Key Balance Adjustments

1. **GOBLIN_KING nerfed**: Attack reduced from ~15 to 12
2. **Regular enemies buffed slightly**: More threatening early game
3. **Floor scaling added**: Enemies get stronger each floor
4. **New enemy types**: RAT_KING (floor 1 boss), DRAGON (floor 3 boss)

### How to Apply

1. Extract archive to project
2. These files REPLACE existing versions:
   - `EnemyFactory.java`
   - `EnemyType.java`
   - `CombatEngine.java`

3. Add this line to `GameSession` constructor (after difficulty is set):
   ```java
   BalanceConfig.applyDifficulty(difficulty);
   ```

4. Update `rest()` method in GameSession:
   ```java
   int healAmount = (int)(player.getHealth().getMaximum() * BalanceConfig.REST_HEAL_PERCENT);
   ```

5. Rebuild and test!

## Target Metrics
- Win rate: ~50-60% on NORMAL
- Combat win rate: ~85-90%  
- Boss lethality: ~20-30% (challenging but beatable)
- Regular enemy lethality: ~5-10% (occasional deaths)
