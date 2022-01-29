# BetterWhitelist
This is a spigot plugin for better whitelist
on a Minecraft server. If you want to enhance
your whitelist with a custom kick message or
want to use whitelist with offline insecure
mode, this is the plugin for you.

Current version is `v1.0-RC1`, keep in touch
if you want to be around when the first stable
version is released. If you find some bugs,
make sure to report them on GitHub so it can be
addressed before they make it into the production.
I am also testing it before the version is released,
but I mainly test the features I'm currently interested
in implementing. Once the first stable version is
released, it will also be released on
[SpigotMC.org](https://www.spigotmc.org/) websites.

## Table of contents:
1. [Installation and configuration](#installation-and-configuration)
2. [Importing vanilla whitelist](#importing-vanilla-whitelist)
3. [General usage](#general-usage)
4. [Important notice](#important-notice)

## Installation and configuration
To install the plugin, simply copy and paste it into
the `plugins` directory in your Spigot server and
restart the server. First time running this plugin
will generate its config file, and set of language
files in `languages` directory inside the plugin's
directory `BetterWhitelist`. It also generates
the whitelist file when you start using it. To enable
the whitelist simply call the command `/whitelist on`
and it will enable. To disable whitelist, call
`/whitelist off`. To set a specific language, call
`/whitelist lang <code>` where `<code>` refers to
the language locale ISO-1 or ISO-2 code. By default,
English language is selected (language locale code `en`).

## Importing vanilla whitelist
If you were using vanilla whitelist prior to this plugin,
you can import it with command `/whitelist import`.
That makes it extremely convenient if you're switching
to this plugin from a vanilla whitelist as you don't have
to manually type in each player's name again, it will
simply search through the vanilla whitelist and add every
entry in it. Don't forget to turn the vanilla whitelist off.
To do it after you add and activate this plugin, simply use
command `/minecraft:whitelist off`, which should run the
vanilla command instead of the plugin's one.

## General usage
The list of all the subcommands is as follow. You can also
refer to the [README.md](https://www.github.com/Polda18/BetterWhitelist#readme)
section on GitHub to display the usage.

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

Permissions are as follows. They all default to server operator (op).

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

## Important notice
Do not use this plugin with other plugins for whitelist
management as it may interfere with the plugin itself
and it may not work correctly.

Also do not use vanilla whitelist alongside with this
plugin, make sure to disable it, otherwise your players
may not join. This plugin doesn't completely hijack
the vanilla whitelist but instead is using its own
implementation. It only replaces the vanilla whitelist
command, which in turn can still be accessed using
`minecraft:` namespace.