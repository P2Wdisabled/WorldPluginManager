# WorldPluginManager

WorldPluginManager is a Minecraft plugin that allows server administrators to manage the activation and deactivation of plugins on a per-world basis. This plugin also supports customizable messages and permission-based command execution.

## Features

- Enable or disable plugins for specific worlds.
- Focus a plugin to only be enabled in a specific world.
- View the status of plugins in different worlds.
- Customizable messages through `messages.yml` with support for placeholders.
- Permission-based command execution.

## Compilation

WorldPluginManager is built with **Maven** and requires **JDK 8** or later.

```bash
git clone https://github.com/P2Wdisabled/WorldPluginManager.git
cd WorldPluginManager
mvn package
```

After the build, the plugin JAR is located in `target/WorldPluginManager-1.0-SNAPSHOT.jar`.

## Installation

1. **Download or Compile**: Use the JAR from the [releases](https://www.spigotmc.org/resources/world-plugin-manager.118225/) page or compile it yourself as described above.

2. **Add to Server**: Copy the `.jar` file into the `plugins` folder of your Minecraft server.

3. **Start Server**: Launch your server to generate the default configuration files.

## Configuration

### `config.yml`

Stores the activation status of plugins for each world. This file is created automatically and normally does not need manual editing.

### `messages.yml`

Customize the messages that the plugin sends. You can use placeholders like `{plugin}` and `{world}` in your messages.

```yaml
plugin_disabled: "&cThe plugin {plugin} is disabled in the world {world}."
plugin_enabled: "&aThe plugin {plugin} is enabled in the world {world}."
plugin_not_found: "&cPlugin {plugin} not found."
world_not_found: "&cThe world {world} does not exist."
cannot_disable_main_plugin: "&cYou cannot disable the main plugin {plugin} in any world."
no_permission: "&cYou do not have permission to use this command."
```
### `plugin.yml`

Reference file listing all commands and their permissions. You normally do not need to modify it.

```yaml
name: WorldPluginManager
main: fr.P2W.wplmanager.WorldPluginManager
version: 2.0
commands:
  displ:
    description: Disables a plugin in a specified world.
    usage: /displ <plugin> <world>
    permission: worldpluginmanager.displ
  enpl:
    description: Enables a plugin in a specified world.
    usage: /enpl <plugin> <world>
    permission: worldpluginmanager.enpl
  flags:
    description: Displays the status of plugins in a specified world.
    usage: /flags <world> [plugin]
    permission: worldpluginmanager.flags
  focus:
    description: Sets a plugin to be enabled only in a specified world.
    usage: /focus <plugin> <world>
    permission: worldpluginmanager.focus
permissions:
  worldpluginmanager.displ:
    description: Allows the use of the /displ command.
    default: op
  worldpluginmanager.enpl:
    description: Allows the use of the /enpl command.
    default: op
  worldpluginmanager.flags:
    description: Allows the use of the /flags command.
    default: op
  worldpluginmanager.focus:
    description: Allows the use of the /focus command.
    default: op
```

## Usage

### Commands

```
/displ <plugin> <world>   - Disable a plugin in a world
/enpl <plugin> <world>    - Enable a plugin in a world
/flags <world> [plugin]   - Show plugin status
/focus <plugin> <world>   - Enable a plugin only in one world
```

### Permissions

- `worldpluginmanager.displ` – use the `/displ` command
- `worldpluginmanager.enpl` – use the `/enpl` command
- `worldpluginmanager.flags` – use the `/flags` command
- `worldpluginmanager.focus` – use the `/focus` command


### License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License. See the [LICENSE](https://github.com/P2Wdisabled/WorldPluginManager/blob/main/LICENSE) file for details.
