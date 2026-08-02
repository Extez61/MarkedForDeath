# 🎯 MarkedForDeath with Imposter

![icon](https://cdn.modrinth.com/data/5JiIhUJr/03f25ab51aa4012a107fc1b9040ac7b3c2248d25.png)

A fast-paced Minecraft minigame where a **Runner** must complete a deadly task while **Guardians** try to stop them — but one Guardian is secretly an **Imposter** working against the team!

> 💡 **Inspiration:** This plugin's core concept was inspired by [this YouTube video by @goktugv](https://www.youtube.com/watch?v=pu6y3JsuD1w).

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
2. Roles are assigned — Imposter is revealed privately via title screen (and via chat if enabled, as a backup in case the title is missed while AFK)
3. Runner punches a player to start the countdown
4. Runner tries to die in the specific way shown on their action bar
5. Guardians (and secretly the Imposter) react accordingly
6. Game ends when: Runner completes the task / Runner dies the wrong way / 
   A Guardian kills the Runner / Time runs out
```

Until the Runner punches someone, no player can place blocks or cast a fishing rod (or use any other right-click item interaction — pearls, splash potions, buckets, food, etc. — or throw a projectile), preventing pre-round trap setups. This is on by default and can be turned off via `restrict-actions-before-touch`.

### Winning Conditions
| Condition | Winner |
|-----------|--------|
| Runner dies the correct way | ✅ Runner + Imposter |
| A Guardian kills the Runner | ✅ Runner + Imposter |
| Runner dies the wrong way | ✅ Guardians |
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
| `/mfd kiteditgui` | Open the kit editor GUI |

### Kit Editing — Permission: `markedfordeath.kitedit`

| Command | Description |
|---------|-------------|
| `/kitedit <runner\|guardians\|imposter>` | Enter kit edit mode (text-based) |
| `/kitedit leave` | Save and exit edit mode |
| `/kiteditgui` | Open the kit editor GUI |

> Two players can edit different kits simultaneously. The same kit cannot be edited by two people at once.

> If you disconnect while editing, your kit is saved automatically.

> Kit definitions live in `kits.yml` and are updated live whenever a kit is saved from `/kitedit` or the GUI — no restart required.

---

## 🔧 Requirements

- **Minecraft**: 1.21.11+ (Spigot/Paper/Purpur)
- **Java**: 21+
- **Dependencies**: None

---

## 🔔 Update Checker
Version check feature notifies every admin/OPerator player on join. Can be disabled from `update-checker.enabled` in `config.yml`.

## 🔄 Config Auto-Update
Plugin updates never wipe your `config.yml` — old settings are kept automatically.

---

## 💬 Support & Feedback

For any questions, suggestions, or bug reports, feel free to reach out on Discord:
[@extez610](https://discord.com/users/1348670674574512160)

## 🇹🇷 Türkçe Özet

### 🎭 Roller

| Rol | Amaç | Takım |
|------|------|------|
| 🏃 **Runner** | Verilen ölüm görevini tamamlamak | Runner + Hain |
| 🛡️ **Koruyucu** | Runner'ın görevi tamamlamasını engellemek | Koruyucular |
| 🕵️ **Hain** | Runner'a gizlice yardım etmek | Runner + Hain |

> Sadece 2 oyuncu çevrimiçiyse oyun Hain olmadan oynanır (Runner vs Koruyucu).

### 🎮 Oyun Akışı

```
1. Yetkili /mfd start komutunu çalıştırır
2. Roller dağıtılır — Hain, title ekranıyla gizlice kendisine bildirilir (AFK olup title'ı kaçırma ihtimaline karşı, ayarlıysa chat üzerinden de bildirilir)
3. Runner, oyunu başlatmak için bir oyuncuya vurur
4. Runner, action bar'da gösterilen şekilde ölmeye çalışır
5. Koruyucular (ve gizlice Hain) buna göre hareket eder
6. Oyun şu durumlarda biter: Runner görevi tamamlar / Runner yanlış şekilde ölür /
   Bir Koruyucu Runner'ı öldürür / Süre dolar
```

Runner birine vurana kadar hiçbir oyuncu blok koyamaz, olta atamaz (ya da pearl, iksir, kova, yemek gibi başka bir sağ tık eşya etkileşimi yapamaz) veya mermi/varlık fırlatamaz; bu, önceden tuzak kurulmasını engeller. Varsayılan olarak açıktır, `restrict-actions-before-touch` ile kapatılabilir.

**Kazanma Koşulları**

| Durum | Kazanan |
|-----------|--------|
| Runner doğru şekilde ölür | ✅ Runner + Hain |
| Bir Koruyucu Runner'ı öldürür | ✅ Runner + Hain |
| Runner yanlış şekilde ölür | ✅ Koruyucular |
| Süre dolar | ✅ Koruyucular |

### 📋 Görevler

| 🔥 | 💧 | 🟠 | 💥 | 🪨 |
|---|---|---|---|---|
| Lavda Ölüm | Boğulma | Magma Bloğu | Creeper | Sıkışma |
| ✅ | ✅ | ✅ | ✅ | ✅ |

| 🏹 | 🧟 | 🕷️ | ⚙️ | 🪂 |
|---|---|---|---|---|
| İskelet | Zombi | Örümcek | Demir Golem | Düşme Hasarı |
| ✅ | ✅ | ✅ | ✅ | ✅ |

| 🌵 | 🍓 | ⚒️ | ⚡ | 💀 |
|---|---|---|---|---|
| Kaktüs | Meyve Çalısı | Örs | Yıldırım | Solma Etkisi |
| ✅ | ✅ | ❌ | ❌ | ❌ |

---

### 🔔 Güncelleme Kontrolü
Versiyon kontrol işlevi her yetkili birisinin girişinde bilgilendirir. `config.yml`'de `update-checker.enabled` kısmından kapatılabilir.

### 🔄 Config Otomatik Güncelleme
Eklenti güncellemelerinde `config.yml`'iniz silinmez — eski ayarlarınız otomatik korunur.

---
## 💬 Destek & Geri Bildirim

Herhangi bir soru, öneri veya hata bildirimi için Discord üzerinden ulaşabilirsiniz:
[@extez610](https://discord.com/users/1348670674574512160)