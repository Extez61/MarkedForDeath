# 🎯 MarkedForDeath with Imposter

![MarkedForDeath](https://cdn.modrinth.com/data/5JiIhUJr/03f25ab51aa4012a107fc1b9040ac7b3c2248d25.png)

**A thrilling cat-and-mouse minigame for Minecraft!** MarkedForDeath with Imposter is a fast-paced multiplayer minigame where a Runner must complete a deadly task while Guardians try to stop them — but one Guardian is secretly an Imposter working against the team!

---

## ✨ Features

### 🎮 Core Gameplay

- **Three Unique Roles**: Runner, Guardian, and Imposter — each with different goals
- **Random Tasks**: Runner must die in a specific way (lava, drowning, creeper, and more!)
- **Hidden Imposter**: One Guardian secretly helps the Runner succeed
- **Touch to Start**: Game begins when a player physically hits the Runner
- **Action Bar HUD**: Real-time task and timer display for all players
- **Game Summary**: Full round recap shown to every player after the game ends

### 🧰 Kit System

- **Per-Role Kits**: Fully customizable item sets for Runner, Guardian, and Imposter
- **In-Game Kit Editor**: Edit kits live with /kitedit without touching config files
- **Potion Support**: Splash potions with type, extended, and upgraded settings
- **Unbreakable Items**: Mark tools as unbreakable directly from config

### 🌍 Multi-Language Support

- **Turkish** and **English** built-in
- Easily switch language in config.yml
- All messages, titles, and action bars are fully localized

### 🔧 Admin Tools

- **Force Start / Stop**: Full control over game state at any time
- **Runner Selection**: Manually pick the Runner with a command
- **Live Reload**: Reload config and language files without restarting
- **Spectator Handling**: Late-joining players automatically become spectators

---

## 🎭 Roles

Runner — Goal: Complete the assigned death task — Team: Runner + Imposter

Guardian — Goal: Prevent the Runner from completing the task — Team: Guardians

Imposter — Goal: Secretly help the Runner succeed — Team: Runner + Imposter

---

## 📋 Tasks

The Runner is assigned a random task they must fulfill to win. Guardians must prevent it!

- 🔥 Lava Death — Die by standing in lava
- 💧 Drowning — Die underwater
- 🟠 Magma Block — Die on a magma block
- 💥 Creeper — Die to a creeper explosion
- 🪨 Suffocation — Die by being crushed inside a block
- 🏹 Skeleton — Die to a skeleton's arrow
- 🧟 Zombie — Die to a zombie
- 🕷️ Spider — Die to a spider
- ⚙️ Iron Golem — Die to an iron golem
- 🪂 Fall Damage — Die from falling
- 🌵 Cactus — Die touching a cactus
- 🍓 Berry Bush — Die in a sweet berry bush
- ⚒️ Anvil — Die from a falling anvil (disabled by default)
- ⚡ Lightning — Die from a lightning strike (disabled by default)
- 💀 Wither Effect — Die from the wither status effect (disabled by default)

Enable or disable any task in config.yml under the tasks section.

---

## 🎮 Commands

### Main Command /mfd (alias: /markedfordeath)

/mfd start — Start a new game — Permission: markedfordeath.admin

/mfd stop — Force stop the current game — Permission: markedfordeath.admin

/mfd reload — Reload config and language files — Permission: markedfordeath.admin

/mfd selectrunner player — Manually assign the Runner role — Permission: markedfordeath.admin

/mfd help — Show all available commands — Permission: markedfordeath.admin

### Kit Editor /kitedit

/kitedit runner — Edit the Runner's kit — Permission: markedfordeath.kitedit

/kitedit guardians — Edit the Guardian's kit — Permission: markedfordeath.kitedit

/kitedit imposter — Edit the Imposter's kit — Permission: markedfordeath.kitedit

/kitedit leave — Save changes and exit edit mode — Permission: markedfordeath.kitedit

---

## 🔐 Permissions

markedfordeath.admin — Access to all /mfd commands — Default: op

markedfordeath.kitedit — Access to /kitedit commands — Default: op

---

## 🎯 How to Play

### Starting a Game

Run /mfd start. Requires at least 3 online players (configurable). The Runner is chosen randomly unless manually set with /mfd selectrunner.

### Game Flow

1. Admin runs /mfd start
2. Roles are assigned: Runner, Imposter, Guardians
3. Imposter sees a private title revealing their role
4. The Runner must PUNCH a player to begin the countdown
5. Guardians chase and disrupt the Runner
6. Runner tries to die in the specific way shown on their action bar
7. Game ends when: Runner completes the task / Runner dies the wrong way / Time runs out

### Winning Conditions

- Runner dies the correct way → Runner + Imposter win
- Runner dies the wrong way → Guardians win
- Timer runs out → Guardians win

---

## 🧰 Kit Editing

Enter kit edit mode to customize items for each role by running /kitedit runner, /kitedit guardians, or /kitedit imposter. You will be placed into Creative mode with the current kit loaded. Arrange items freely in your hotbar and inventory (slots 0 to 35). When finished, run /kitedit leave. Changes are instantly saved to config.yml and your original inventory is restored automatically.

If you disconnect while editing, your kit is saved and your inventory is restored on reconnect.

---

## ⚙️ Configuration

language: en or tr

game.duration: Round length in seconds, default is 600 (10 minutes)

game.min-players: Minimum players required to start, default is 3

game.show-task-to-all: Show the task on everyone's action bar, default false

game.show-imposter-to-runner-in-chat: Reveal Imposter to Runner in chat, default false

tasks: Enable or disable individual tasks such as lava-death, drowning, creeper, fall-damage, anvil, lightning, wither-effect and more

---

## 🌍 Language Configuration

Switch between English and Turkish by editing language in config.yml to en or tr, then reload with /mfd reload. All messages, titles, action bars, and task names update automatically.

---

## 🔧 Technical Details

### Requirements

- Minecraft: 1.13 or higher
- Server: Spigot, Paper, or Purpur
- Dependencies: None — pure Spigot API

### Performance

- Lightweight BukkitRunnable-based timer system
- Action bar updates every second only for active players
- Smart task cancellation on game end
- Inventory and gamemode saved and restored safely for kit editors

---

## 📝 Notes

- Players who join mid-game are automatically set to Spectator mode
- The Runner cannot be damaged by other players, only by the environment
- The touch-to-start mechanic gives everyone time to get into position before the clock begins
- Potion effects are cleared for all players at game start and end

---

More plugins on Modrinth: https://modrinth.com/user/Extez0612
