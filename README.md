# Arena Checkpoint

NeoForge mod for Minecraft 1.21.1. When you die during a boss fight, your dropped items land where you entered the fight instead of inside the boss.

The fight entry is the spot you were standing when you came within range of a living boss. Walk out of that range and back in, and the spot updates. The rule is server-side. The mod does not add weapons, change loot tables, or depend on any other gameplay mod.

## Install

1. Install [NeoForge for Minecraft 1.21.1](https://neoforged.net/) (this build uses NeoForge 21.1.265).
2. Put `arenacheckpoint-1.0.0.jar` in the `mods` folder.
3. Launch the game. Arena Checkpoint loads on its own. The Corpse mod is optional.

The radius is a server config, created after the first launch:

- Single player: `saves/<world>/serverconfig/arenacheckpoint-server.toml`
- Dedicated server: `<world>/serverconfig/arenacheckpoint-server.toml`

```toml
entryRadius = 48.0
```

That is the distance, in blocks, from a living boss. The default is 48. Allowed range is 1 to 512.

## How a death is moved

The saved entry is used when any of these is true:

- the killing blow came from a boss (including a projectile or breath cloud the boss owns)
- a boss is your current combat target (you hit one, or one hit you, within the last 10 seconds)
- a living boss is still within the radius

Items are placed at the saved coordinates in the same dimension. If [Corpse](https://www.curseforge.com/minecraft/mc-mods/corpse) is installed, it stores your death position while you die and spawns its corpse there, so the corpse ends up on the entry spot too. Nothing in this mod references Corpse. Leave it out and vanilla drops are moved instead.

## Try it

Summon a boss from outside the radius, walk in, then die.

Wither:

1. Note `entryRadius` (48 unless you changed it).
2. Stand farther away than that and summon the wither, or walk away after it appears.
3. Walk toward it until you cross the radius. The server log records `Saved boss-fight entry`.
4. Let the wither kill you.
5. Your items (or your Corpse) are at the crossing point, not under the wither.

If you summon the wither on top of yourself, you are already inside the radius, so the first saved spot is your feet. Walk out past the radius and back in before you die. That updates the entry.

Ender dragon:

1. Enter the End.
2. Walk toward the dragon until the entry is saved. The dragon's body is much smaller than 48 blocks, so the crossing is outside the hitbox.
3. Die to the dragon. Your items are back at that entry.

You can confirm the tags the game loaded with:

```
/execute if entity @e[type=#c:bosses]
/execute if entity @e[type=#arenacheckpoint:bosses]
```

## Add a modded boss

Bosses are entity-type tags, not hardcoded mod classes. NeoForge's conventional tag is `#c:bosses` (`data/c/tags/entity_type/bosses.json`). This mod also ships `#arenacheckpoint:bosses`, which contains the ender dragon, the wither, and `#c:bosses` when that tag exists.

Pack makers add bosses by filling:

`data/c/tags/entity_type/bosses.json`

Example datapack (pack format 48 for 1.21.1):

```
more-bosses/
  pack.mcmeta
  data/c/tags/entity_type/bosses.json
```

`pack.mcmeta`:

```json
{
  "pack": {
    "description": "More bosses for Arena Checkpoint",
    "pack_format": 48
  }
}
```

`data/c/tags/entity_type/bosses.json`:

```json
{
  "values": [
    "yourmod:boss_entity"
  ]
}
```

Use the entity id from the other mod. Do not add Java class names. Put the datapack in `saves/<world>/datapacks/` or the server `datapacks` folder, then run `/reload`. Appending to `#arenacheckpoint:bosses` works too, at `data/arenacheckpoint/tags/entity_type/bosses.json`, if the boss should apply only to this mod.

## Build

Java 21.

```
bash gradlew build
```

`gradlew` restores `gradle/wrapper/gradle-wrapper.jar` from `gradle/wrapper/gradle-wrapper.jar.base64` if the jar is not already there. The mod jar is `build/libs/arenacheckpoint-1.0.0.jar`.
