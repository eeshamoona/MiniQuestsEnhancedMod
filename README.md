# 🌟 Eesha's Mini Quests

*A fast-paced, gacha-style delivery quest mod for Minecraft NeoForge 1.21.4!*

## 📦 What is this mod?

Tired of complex, heavy questing modpacks with hundred-page quest books? **Eesha's Mini Quests** injects a fast-paced, incredibly satisfying, and lightweight questing loop directly into your everyday survival world. 

Receive randomized "delivery" requests for common everyday items (like logs, cobblestone, string, or seeds), submit them through a sleek custom UI, and immediately get rewarded with a prize roll! Will you get a handful of XP bottles, or perhaps hit the jackpot with a Diamond Block or Totem of Undying? Who knows!

## ✨ Features & What to Expect

- **Instant Tiered Rewards:** Submit your items and instantly receive a randomized reward! The more complex the quest (up to 3 distinct item types requested), the higher your chance at rolling Epic tier rewards.
- **Fast-Paced Core Gameplay:** Quests intentionally ask for readily available early-game materials. The fun comes from the fast turnarounds and managing multiple deliveries, rather than endlessly grinding for a 1% rare mob drop.
- **Sleek Vanilla-Style UI:** No clunky chat menus or awkward command typing. Press `G` to open a custom, beautifully designed quest screen where you can easily drag and drop your items to submit them just like inserting fuel into a furnace.
- **Dynamic Tiered Loot:** Roll for Common, Uncommon, and Rare/Epic rewards. Prizes range from basic food, resources, and fireworks, up to Netherite Ingots, Shulker Boxes, and custom **Bronze, Silver, and Gold Tokens**.
- **Zero Bloat:** Quests are randomly generated on the fly. There are no massive data files, saving system bloat, or heavy loads to weigh down your server.

## 🚀 How It Works (Getting Started)

Jumping into the game? Here is how to complete your first quest:

1. **Get a Quest:** Type `/quest start` to assign yourself your first quest.
2. **Open the UI:** Press the **`G` key** (customizable in your controls) at any time to open your Mini Quests Dashboard.
3. **Check the Request:** Look at the left panel. Here you will see a checklist of what items and exactly how many your current quest requires.
4. **Gather & Submit:** Collect the items in the world! Open the UI `G`, drop them into the **Input Slot**, and hit **Submit**. 
5. **Claim Your Prize:** Once all the required items are submitted, your randomized gacha reward appears instantly in the **Output Slot**.
6. **Keep Going:** Take your reward into your inventory. A 5-second countdown will begin before your *next* quest starts automatically!

## ⚙️ How to Install (For Players)

### Prerequisites
- **Minecraft 1.21.4**.
- **NeoForge** mod loader installed (version `21.4.156` or higher).

### Installation Steps
1. Download the latest `.jar` file for **Eesha's Mini Quests** from the releases page (or build it yourself!).
2. Navigate to your Minecraft installation folder:
   - **Windows:** `%appdata%\.minecraft\mods`
   - **Mac:** `~/Library/Application Support/minecraft/mods`
   - **Linux:** `~/.minecraft/mods`
3. Drop the downloaded `.jar` file directly into the `mods` folder.
4. Launch the game using your NeoForge profile and jump into a world!

## 🛠️ For Developers / Compiling from Source

Want to poke around the code or build the mod yourself?

1. Clone this repository:
   ```bash
   git clone <repo-url>
   ```
2. Navigate into the folder and run the Gradle wrapper to build the jar:
   ```bash
   # Windows
   gradlew build
   
   # Mac/Linux
   ./gradlew build
   ```
3. Your newly compiled mod `.jar` will be generated in `build/libs/`.

*Curious about the internal design or want to modify the drop tables? Check the `docs/` folder in this repository for the full source of truth on Mod Design and Quest/Reward rules!*

---

**Happy Questing! May the odds be ever in your favor.**
