# Penguin Client
 
<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20.1-green?logo=minecraft" alt="Minecraft 1.20.1">
  <img src="https://img.shields.io/badge/Mod%20Loader-Fabric-dbd0b4?logo=curseforge" alt="Fabric">
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="MIT License">
  </a>
  <a href="https://github.com/JasonVinion/Penguin-Client-MC/releases">
  </br>
  <a href="[https://github.com/JasonVinion/Whisper-Studio](https://github.com/JasonVinion/Penguin-Client-MC)">
  <img src="https://img.shields.io/endpoint?url=https://ghloc.vercel.app/api/JasonVinion/Penguin-Client-MC/badge&label=lines%20of%20code" alt="GitHub lines of code"/>
</p>

<p align="center">
  A feature-rich utility client for Minecraft 1.20.1 built with Fabric
</p>

---

## **About**

Penguin Client started as a personal project to sharpen my modding and software development skills. It was never meant to be a serious competitive client, but has snowballed a bit to have some more niche features.
Feel free to explore the codebase and use any code for your own projects, or contribute if you'd like. 

**Note:** This client is for educational purposes and personal use.

---

Penguin Client includes **100+ modules** across multiple categories:

### Combat
Auto Crystal, Kill Aura, Aim Assist, Auto Totem, Criticals, Velocity, Trigger Bot, etc.

### Movement
Flight, Speed, Jesus (Water Walk), No Fall, Elytra Tweaks, Boat Fly, Phase, Spider, etc.

### Player
Auto Armor, Auto Eat, Auto Tool, Inventory Cleaner, Fast EXP, Anti AFK, etc.

### Render
ESP, Chest ESP, Block ESP, Tracers, Freecam, X-Ray, Fullbright, Trajectories, etc.

### World
Auto Mine, Nuker, Scaffold, Fast Mine, Auto Farm, Ghost Hand, Hole Filler, etc.

### Misc
Anti Crash, Fast Place, Chat Suffix, Fancy Chat, Spammer, etc.

<details>
<summary><b>View Full Feature List</b></summary>

For a comprehensive list of all 100+ modules and their descriptions, see the [feature documentation](docs/FEATURES.md) or check the source code.

</details>

---

## UI

Penguin Client features two UI modes:

- **List UI**: Modeled after GTA V mod menus with a clean, vertical list layout
- **Click GUI**: Standard client interface familiar to most Minecraft utility clients

Switch between UIs anytime in the Settings category.

---

## Installation

### Prerequisites
1. **Fabric Loader** for Minecraft 1.20.1
   - Download from the [official Fabric website](https://fabricmc.net/use/installer/)
   - Run the installer and select Minecraft version 1.20.1
   
2. **Fabric API** for 1.20.1
   - Download from [Fabric API on Modrinth](https://modrinth.com/mod/fabric-api) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

### Installing Penguin Client

1. Download the latest `Penguin-Client-X.X.X.jar` from the [Releases](https://github.com/JasonVinion/Penguin-Client-MC/releases) page
2. Locate your Minecraft mods folder:
   - **Windows**: `%appdata%\.minecraft\mods`
   - **macOS**: `~/Library/Application Support/minecraft/mods`
   - **Linux**: `~/.minecraft/mods`
3. Place both the **Fabric API jar** and **Penguin Client jar** into the `mods` folder
4. Launch Minecraft using the Fabric profile
5. Press `Insert` to open the client menu in-game

---

## Default Keybinds

- Press **`Insert`** to open the menu
- **Navigation**:
  - `Up`/`Down`: Select category or module
  - `Right` (or `Enter`): Enter category or toggle module
  - `Left` (or `Backspace`): Go back

All keybinds are fully configurable in the Client Settings menu.

---

## Building from Source

### Requirements
- Java Development Kit (JDK) 17 or higher
- Git

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/JasonVinion/Penguin-Client-MC.git
   cd Penguin-Client-MC
   ```

2. Build the project:
   ```bash
   # On Windows
   gradlew build
   
   # On macOS/Linux
   ./gradlew build
   ```

3. The compiled JAR will be located in:
   ```
   build/libs/Penguin-Client-X.X.X.jar
   ```

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/JasonVinion/Penguin-Client-MC/issues).

---

## Disclaimer

This client is intended for **educational purposes and single-player or private server use only**. Using utility clients on public servers may violate their terms of service. Always respect server rules and play fairly. The author is not responsible for any bans or consequences resulting from the use of this client.

---

<p align="center">
</p>
