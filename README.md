# 🎯 MarkedForDeath with Imposter

![icon](https://cdn.modrinth.com/data/5JiIhUJr/03f25ab51aa4012a107fc1b9040ac7b3c2248d25.png)

A fast-paced Minecraft minigame where a **Runner** must complete a deadly task while **Guardians** try to stop them — but one Guardian is secretly an **Imposter** working against the team!

---

## 🎭 Roles

| Role | Goal | Team |
|------|------|------|
| 🏃 **Runner** | Complete the assigned death task | Runner + Imposter |
| 🛡️ **Guardian** | Prevent the Runner from completing the task | Guardians |
| 🕵️ **Imposter** | Secretly help the Runner succeed | Runner + Imposter |

> With only 2 players online, the game runs without an Imposter (Runner vs Guardian).

---

## 🎮 Game Flow

```
1. Admin runs /mfd start
2. Roles are assigned — Imposter is revealed privately via title screen
3. Runner punches a player to start the countdown
4. Runner tries to die in the specific way shown on their action bar
5. Guardians (and secretly the Imposter) react accordingly
6. Game ends when: Runner completes the task / Runner dies the wrong way / Time runs out
```

### Winning Conditions

| Condition | Winner |
|-----------|--------|
| Runner dies the correct way | ✅ Runner + Imposter |
| Runner dies the wrong way OR a Guardian kills the Runner | ✅ Guardians |
| Timer runs out | ✅ Guardians |

---

## 📋 Tasks

| Task | Default |
|------|---------|
| 🔥 Lava Death | ✅ |
| 💧 Drowning | ✅ |
| 🟠 Magma Block | ✅ |
| 💥 Creeper | ✅ |
| 🪨 Suffocation | ✅ |
| 🏹 Skeleton | ✅ |
| 🧟 Zombie | ✅ |
| 🕷️ Spider | ✅ |
| ⚙️ Iron Golem | ✅ |
| 🪂 Fall Damage | ✅ |
| 🌵 Cactus | ✅ |
| 🍓 Berry Bush | ✅ |
| ⚒️ Anvil | ❌ |
| ⚡ Lightning | ❌ |
| 💀 Wither Effect | ❌ |

---

## 🎮 Commands

### `/mfd` (alias: `/markedfordeath`) — Permission: `markedfordeath.admin`

| Command | Description |
|---------|-------------|
| `/mfd start` | Start a new game |
| `/mfd stop` | Force stop the current game |
| `/mfd reload` | Reload config and language files |
| `/mfd selectrunner <player>` | Manually assign the Runner |
| `/mfd help` | Show all commands |

### Kit Editing — Permission: `markedfordeath.kitedit`

| Command | Description |
|---------|-------------|
| `/kitedit <runner\|guardians\|imposter>` | Enter kit edit mode (text-based) |
| `/kitedit leave` | Save and exit edit mode |
| `/kiteditgui` | Open the kit editor GUI |

> Two players can edit different kits simultaneously. The same kit cannot be edited by two people at once.

> If you disconnect while editing, your kit is saved automatically.

---

## 🔧 Requirements

- **Minecraft**: 1.15+
- **Server**: Spigot, Paper, or Purpur
- **Dependencies**: None
