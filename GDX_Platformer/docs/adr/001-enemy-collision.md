# ADR-001: Enemy Collision Implementation

## Status
✅ Fixed - Syntax errors resolved + Kill logic improved

## Context
The `GameScreen.java` implementation had syntax errors that prevented compilation, and the enemy kill mechanics were too restrictive.

### Errors Found in GameScreen.java

| Line | Issue | Description |
|------|-------|-------------|
| 395 | **Extra closing brace `}`** | Unclosed a block that didn't exist, breaking Java parsing |
| 542-557 | **Overly complex kill conditions** | Required specific falling state and dual checks making stomp detection difficult |

### Code Structure Before Fix (lines 375-398)

```java
for (Enemy e : enemies) {
    if (player.overlaps(e.bounds)) {
        int pushPower = 20;
        if (e.bounds.x < player.x) {
            xVelocity = pushPower; 
            player.setX(player.getX() + 5); 
        } else {
            xVelocity = (-pushPower);
            player.setX(player.getX() - 10);
            e.movingRight = true;
        }
        
        float prevBottom = prevPlayerBounds.y + prevPlayerBounds.height;
        boolean isStomp = prevBottom >= e.bounds.y && yVelocity < 0;

        if (isStomp || player.y < e.bounds.y) {
            yVelocity = (-2);
        }
        // ERROR: Extra closing brace closed non-existent block!
        }  
    }
}
```

### Syntax Fix Applied

**Before (broken):**
```java
if (isStomp || player.y < e.bounds.y) {
    yVelocity = (-2);
}
}  // <-- EXTRA BRACE: closes a block that doesn't exist!
```

**After (fixed):**
```java
if (isStomp || player.y < e.bounds.y) {
    yVelocity = (-2);
}
// Now properly closes the if statement at line 391, then the else at 382 
// then the main if at 377 - no extra braces needed
```

### Issue: Unable to Kill Enemies

**Original Problem:**
The kill logic required `!wasFalling && yVelocity < 0` for stomp detection. This failed in common scenarios:
- If player was falling onto an enemy after landing (wasFalling=true but still jumping)
- The dual-condition check (`if/else if`) made testing difficult

**Solution Implemented:**
```java
boolean isStomp = false;

// Kill if player overlaps enemy top half AND:
float prevBottom = prevPlayerBounds.y + prevPlayerBounds.height;
isStomp = prevBottom >= e.bounds.y && yVelocity < 0;

// Also kill if feet are within 20px of enemy top (more forgiving)
float playerBottomY = player.y + player.height;
if (playerBottomY >= enemyTopY - 20 && Math.abs(playerBottomY - enemyTopY) < 30) {
    isStomp = true;
}

// On kill: push enemy down, create dust effect, remove from array
if (isStomp) {
    e.velocity.y = -10;
    enemies.removeIndex(i);
    createDust(e.bounds.x + e.bounds.width/2, e.bounds.y + e.bounds.height/2, 8);
}
```

**Key Changes:**
1. **Simplified condition**: Just check `prevPlayerBounds` position from previous frame before physics
2. **Expanded tolerance**: Feet within 20px of enemy top counts as stomp (was: 15px tolerance + 8px overlap)
3. **Visual feedback**: Add dust particles on kill for better game feel
4. **Removed unnecessary conditions**: No more `wasFalling` or dual-branch logic

## Related
- [[Wall Grab]] - Player wall-grab interaction with enemy hitboxes  
- [[Enemy.spawn]] - Enemy initialization and respawn logic  

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
