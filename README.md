# CloneBang

CloneBang is a Minecraft Fabric mod that makes the `/clone` command easier to use. It provides a dedicated blaze rod selection tool, lets you mark two corners of an area, and helps you copy, run, save, and reload clone selections without manually typing coordinates.

## Features

- Get the dedicated clone tool with the `/CloneTool` command
- Select two positions with left click, Shift+left click, or right click
- Generate, copy, and run `/clone` commands from the selected area
- Render selections with particles or the mod's native line renderer
- Save selected structures by name and load recent saved structures
- Preview placement before running the clone command
- Configure selection behavior, render mode, excluded blocks, and local data from an in-game settings screen
- Includes multilingual resources, including English and Korean
- Includes a reserved network bridge for future Paper plugin integration

## Requirements

| Item | Version |
| --- | --- |
| Minecraft | `26.1.2` |
| Fabric Loader | `0.19.2` or newer |
| Fabric API | `0.147.0+26.1.2` |
| Java | `25` or newer |
| Mod ID | `clonebang` |
| Current Version | `1.0.0` |

## Installation

1. Install Fabric Loader and Fabric API.
2. Download `clonebang-1.0.0.jar` from the release files.
3. Place the jar file in your Minecraft `mods` folder.
4. Launch the game and run `/CloneTool` to receive the clone tool.

The built mod jar is generated at `build/libs/clonebang-1.0.0.jar`.

## Usage

1. Run `/CloneTool` to receive the clone tool.
2. Hold the tool and left click the first position.
3. Set the second position with Shift+left click or right click.
4. Press `Ctrl + C` to copy the generated `/clone` command, or press `Ctrl + V` to run it at the block you are looking at.

## Default Shortcuts

| Shortcut | Action |
| --- | --- |
| `Shift + Alt` | Open settings |
| `Shift + H` | Open help |
| `Shift + S` | Save the current selection |
| `Shift + E` | Load a saved structure |
| `Alt + E` | Preview placement |
| `Ctrl + C` | Copy the `/clone` command |
| `Ctrl + V` | Run the `/clone` command |
| `Alt + D` | Clear the selection |
| `Ctrl + Z` / `Ctrl + Y` | Reserved undo/redo hooks for placement history |

## Local Data

CloneBang stores settings and saved structure metadata under the Minecraft game directory.

```text
moddata/clonebang/
moddata/clonebang/config.properties
moddata/clonebang/structures/*.properties
```

The settings screen can open the data folder, reset the config, and clear saved structure data.

## Building

Windows:

```powershell
.\gradlew.bat build
```

macOS/Linux:

```bash
./gradlew build
```

After a successful build, the distributable jar and sources jar are created in `build/libs/`.

```text
build/libs/clonebang-1.0.0.jar
build/libs/clonebang-1.0.0-sources.jar
```

## CI

This repository includes a GitHub Actions workflow that runs on pushes and pull requests. It builds the project with Java 25 and uploads the generated files from `build/libs/` as workflow artifacts.

## Paper Plugin Bridge

CloneBang includes a reserved protocol for future Paper server plugin integration. The mod does not require the plugin; when no compatible plugin is detected, it continues to use its local/client-side fallback behavior.

See [docs/plugin-bridge.md](docs/plugin-bridge.md) for protocol details.

## License

This project is licensed under `CC0-1.0`.
