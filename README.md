# BetterWhitelist
A Minecraft Spigot plugin for better whitelist

## Introduction
This plugin is fairly simple. It implements its own whitelist as opposed
to vanilla one. The usage of this plugin is aimed for offline mode servers
that for some reason want to use whitelist feature to allow only certain
players to be able to join the server. First version of this plugin are out,
but keep in mind that these versions are still in ALPHA testing phase. If you
want to participate in testing out this plugin, download from releases.
Otherwise wait for more stable releases. If you find a bug, fill in bug report
in GitHub [issues page](https://www.github.com/Polda18/BetterWhitelist/issues).

## How it works and issue with vanilla whitelist
Pretty much how you would expect the whitelist to work. It checks if the player
is on the whitelist and if not, it kicks them out of the server with appropriate
message. Managing the whitelist is you add a player to the whitelist and turn
whitelist on. That player is then allowed to play on the server. And that is
how the vanilla whitelist should work. Where is the problem? In offline mode.
Server in offline mode still treats whitelist in the same way. When whitelist
is off, anybody can join the server and adding an online player to a disabled
whitelist takes their IGN and their current UUID and adds them to the whitelist.
That's fully expected. But when the player is *NOT* online, then whitelist checks
Mojang database to find that player in list of registered Minecraft players.
If that player cannot be found, whitelist refuses to add that player to whitelist.

That is no issue on online mode servers as this is fully expected and there's no
need to check if the player online does indeed exist or not, because otherwise server
wouldn't allow that player to join in the first place. But offline mode server allows
anybody join, regardless of paid account or not, which is an issue because not only
whitelist adds anybody online without checking an existing profile, existing players
cannot join the server because their UUID reported to server mismatches the one on
whitelist. That is because whitelist doesn't actually check against player names,
but rather checks against UUID of the player joining. Since UUID of players added
while being offline is pulled from Mojang database, the UUID the server generates for
them when joined mismatches and server refuses them to join. This is what this plugin
is fixing. It allows offline mode servers manage their whitelist while whitelist is on
and new players added to that whitelist are offline (in other words have never been
on the server yet).

How is it achieved? First of all, it doesn't use vanilla whitelist,
it uses its own whitelist file, native to Spigot plugin configuration format.
It allows the plugin to easily manage the whitelist without relying on JSON translation
from the vanilla whitelist (apart from importing). This allows the plugin to manage
pair of UUIDs for each player. An online UUID, which matches the one on Mojang database,
and offline UUID, which is generated from the player's IGN. Plugin then checks the player's
UUID against the correct UUID according to whether the server runs in online or offline mode.

### Command
The whitelist command for this plugin is `/whitelist`, or `/wl`, it replaces the vanilla whitelist.
You can still access the vanilla whitelist using `/minecraft:whitelist` command if you need to
use it. These are the subcommands:

- `status` - Checks the status of whitelist (enabled or disabled), requires `betterwhitelist.admin`
  permission to run
- `on` - Turns the whitelist on, requires `betterwhitelist.admin` permission to run
- `off` - Turns the whitelist off, requires `betterwhitelist.admin` permission to run
- `list` - Lists all whitelisted players, requires `betterwhitelist.list` permission to run
- `add <playername>` - Adds specified player to the whitelist, requires `betterwhitelist.add`
  permission to run
- `remove <playername>` - Removes specified player from the whitelist, requires `betterwhitelist.remove` 
  permission to run
- `import` - Imports players from vanilla whitelist to be used with this plugin, requires
  `betterwhitelist.admin` permission to run
- `lang <language code>` - Checks or changes language of the plugin messages, requires
  `betterwhitelist.admin` permission to run
- `reload` - Reloads the whitelist and configuration from the plugin files (used mainly if you do
  manual edits - not recommended), requires `betterwhitelist.admin` permission to run
- `version` - Displays current version of the plugin, requires `betterwhitelist.admin` permission to run

### Permissions
If you're using permissions plugin, this is a list of available permissions you'll need to use.
All permissions default to server operators (op).

- `betterwhitelist.*` - This is a wildcard permission, which is equivalent for `betterwhitelist.admin`.
  It's recommended to use the named permission.
- `betterwhitelist.admin` - This is a super permission that allows you to access all subcommands,
  including `add`, `remove` and `list`.
- `betterwhitelist.add` - This permission allows you to add players into whitelist.
  Does not grant you removal and list permissions.
- `betterwhitelist.remove` - This permission allows you to remove players from whitelist.
  Does not grant you add and list permissions.
- `betterwhitelist.list` - This permission allows you to list whitelisted players.
  Does not grant you add and remove permissions.

It is recommended to assign add, remove and list permissions simultaneously for player
that is in charge of managing whitelist, but cannot be trusted with full access to all
the commands. Each individual permission only grants an access to the single command
it is assigned to. If you want some player to be able just list the whitelisted players,
assign only list permission to them.

### Installation and configuration
If you want to use this plugin with your server, simply add it to the `plugins` directory
and restart your server for the plugin to be enabled. It will generate few files in its
own `BetterWhitelist` subdirectory, namely `config.yml`, `whitelist.yml` and directory
`languages` with few other `.yml` files in it. This is the structure:

- `config.yml` - This file contains basic configuration (enabled/disabled, language, etc.)
- `whitelist.yml` - This is the whitelist itself. It is empty by default, but is created
  in order to maintain the whitelist content. You can see the whitelist file example
  in [`whitelist.example.yml`](src/main/resources/whitelist.example.yml) file.
- `languages/*.yml` - This directory contains files that are used for plugin translations.
  By default, English language is selected and used (`en.yml`). There are additional
  languages available, which can be selected by executing `/wl lang <code>` where `<code>`
  refers to the language ISO 639-1 or ISO 639-2 code (which is also the file name).
  If you want to contribute, feel free to fork this repository, add your language file(s)
  (or fix spelling/grammar issues in existing ones), and create a pull request to be
  added in this original repository. Don't forget to keep your fork up to date!

If you had your players in vanilla whitelist previously, you can import them
using this command: `/wl import`. No additional parameters are required to run this
command, no additional parameters are also taken into account. Running this
command silently adds all players found in vanilla whitelist to whitelist of this
plugin. You can verify content by executing `/wl list` command. To add a new player
to the whitelist simply run `/wl add <playername>` where `<playername>` refers to
the player IGN, and to remove a player from the whitelist run `/wl remove <playername>`
where `<playername>` again refers to the player IGN.

### Disclaimer
This plugin isn't intended to support criminal behavior. It is intended to aid
servers running in offline mode from legitimate reasons in managing their whitelist.

Reasons to run a server in offline mode can be testing, running server in a closed
environment without an Internet access (home LAN server), running server for closed
range of friends who want to play together. In some countries, owning a copy of the
game may be prohibited, which leaves residents of these countries with no option
but to play without paying. This massively reduces their choices of servers down
to those that run in an offline insecure mode. In some countries, children may or
may not be allowed to spend money for games. In Czech Republic for example, most
children who play Minecraft do not legally own a copy of the game and play on
a cracked version. They eighter cannot afford the game, cannot spend money on games
or their parents refuse to buy them games. Nobody has rights to decide for parents,
but it's also in nobody's rights to refuse somebody to play games. I'm not sceptical
towards children playing without paid Minecraft account, they simply might have no
other choice. I am however sceptical towards adults playing Minecraft without paying
unless they come from a country with restricted access or want to try the game out
before buying. I am in no position to require payment though, that's fully in Mojang's
competence.

## Recommendations
I'd suggest you to disable the vanilla whitelist in case you want to give
this plugin a shot. Otherwise your players might not be able to join.
I also recommend you to use this plugin in conjunction with an auth plugin
to ensure your players will have their server accounts at least somehow
protected, and optionally also use a skin restorer plugin if your auth
plugin doesn't have an option for premium access (original game buyers).
