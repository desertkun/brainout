# Brain / Out Content Repository

This repository contains all in-game content for the game <a href="https://brainout.org">Brain / Out</a>.

## Packages

The directory `packages` contains a list of the contnent "packages", each serving individual purpose. Each name of the package pretty much describes itself:

| Name | Description                                                         |
|------|---------------------------------------------------------------------|
| <a href="packages/base">base</a> | "root" package, where the most stings belong |
| <a href="packages/sounds">sounds</a> | A package for all in-game sounds and music |
| <a href="packages/mainmenu">mainmenu</a> | A package containing GUI graphics |
| <a href="packages/freeplay"> freeplay </a> | A package for the Free Play mode |
| `map_*` | A package for each individual map |

## Smart Folders

Each package may contain `smart` folder, which may contant in-game items. 

| Name | Description                                                         |
|------|---------------------------------------------------------------------|
| base / <a href="packages/base/smart">smart</a> | `smart` package for the `base` package |
| base / smart / <a href="packages/base/smart/Weapons">Weapons</a> | In-game Weapons |
| base / smart / <a href="packages/base/smart/Player Skins">Player Skins</a> | Player Outfits |
| base / smart / <a href="packages/base/smart/Containers">Containers</a> | In-game "containers" |
| freeplay / <a href="packages/freeplay/smart">smart</a> | `smart` package for the `freeplay` package |

* Each `smart` folder is parsed for the `*.txt` documents;
* The names of the documents is ignored, `AK74.txt` or `ak-74.txt` does not matter.
* The file format is <a href="https://hjson.org/try.html">very simplified user-friendly JSON</a>:

```
class = content.instrument.Weapon
components = [
   ...    
]
defaultSkin = skin-weapon-ar15-default
id = weapon-ar15
instrumentTags = primary
primary = {
    accuracy = 85
    aimDistance = 30
    allowedBullets = bullet-5.56x45
    clipSize = 20
    damage = 65
    fire-rate = 540
    recoil = 50
    reloadTime = 3
    shootModes = single
}
slot = slot-primary
```

For even more readability, `=` is allowed as key/value separator.

```
class = content.bullet.ShotBullet
components = [
    ...
]
id = bullet-5.56x45
mass = 0.05
name = ITEM_BULLET_556_45MM
power = 0.04
powerDistance = 160
speed = 240
good = 30
```

## Weapons

All weapons are stored in <a href="packages/base/smart/Weapons">Weapons</a> folder. Each weapon usually consists of several items:

* Spine animation;
* `Weapon.txt` – a document describing weapon's features;
* `Skins` – a folder for possible skins for the weapon;
* `Upgrades` – a folder for possible upgrades for the weapon;
* `Store Item.txt` – a document putting all of above together.

## Textures

Pretty much all textures are stored within `textures` folder within each package. 

Key textures are:

| Name | Description                                                         |
|------|---------------------------------------------------------------------|
| base / <a href="packages/base/textures/GAME">GAME</a> | In-game textures, such as weapon parts, player skins, in-game items |
| base / <a href="packages/base/textures/ICONS">ICONS</a> | If there's icon, its gonna be there |
| base / <a href="packages/base/textures/BLOCKS">BLOCKS</a>| 16x16 sprites for each tile for the "blocks" |
| base / <a href="packages/base/textures/CARDS">CARDS</a>| Achievement items, in-game store sprites |
| base / <a href="packages/base/textures/QUESTS">QUESTS</a>| Sprites for Free Play quests |
| freeplay / <a href="packages/freeplay/textures/FREEPLAY">FREEPLAY</a>| Textures for Free Play mode's environment |

## Sounds

Most of the sounds stored within the `sounds` package, <a href="packages/sounds/contents/content/sounds">here</a>.

## Attribution

Most sound effects may have been downloaded from [freesound](https://freesound.org/people/desertkun/downloaded_sounds/),
others have been bought from private sound collections using appropriate licenses.