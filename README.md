# Block Overlay Minecraft Mod (1.21.11) - Fabric

Customizable block overlay rendering compatible with shaders, spectator mode, and adventure mode

## Features

TODO

## Licence

This mod is available under the GPLv3 licence. Refer the `LICENSE` and `THIRD_PARTY_LICENSES` files at the project root
for more details.

## Issues

1. Outline of cauldrons and anvils are funky
2. Dark overlays work worse than light ones due to a compromise made to add support to shaders

## References/Credits

1. [Block Overlay Mod 4.0.3 (1.8.9)](https://hypixel.net/threads/forge-1-8-9-block-overlay-v4-0-3.1417995/) (Heavy
   inspiration)
2. [Block Outline Customizer](https://www.curseforge.com/minecraft/mc-mods/block-outline-customizer)
   By [An0mz](https://github.com/An0mz/) - https://github.com/An0mz/BlockOutlineCustomizer
3. https://linkie.shedaniel.dev/mappings
3. https://docs.fabricmc.net/develop/rendering/world
4. https://www.answeroverflow.com/m/1193300740911923240
4. https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/index-files/index-1.html
6. https://piston-data.mojang.com/v1/objects/031a68bebf55d824f66d6573d8c752f0e1bf232a/client.txt
7. https://gist.github.com/gigaherz/b8756ff463541f07a644ef8f14cb10f5
8. https://github.com/FabricMC/yarn/blob/3ce892a659aa977aa50d1cb90a96dfcf270d4ba0/filament/src/test/resources/projects/sharedData/yarn-mappings-v2.tiny
9. https://raw.githubusercontent.com/FabricMC/intermediary/refs/heads/master/mappings/1.21.11.tiny

## Knowledge used

### Mojang to Yarn Mappings

1. Go to https://piston-data.mojang.com/v1/objects/031a68bebf55d824f66d6573d8c752f0e1bf232a/client.txt
2. Cntrl+F search for "<mapping_path> ->"
3. Notice what it says after the `->`, this will be `A`
4. Go to https://raw.githubusercontent.com/FabricMC/intermediary/refs/heads/master/mappings/1.21.11.tiny
5. Cntrl+F search for `A`, and make sure the first column says class or method or field or what you want exactly. You
   will have to scroll down
6. Notice the method number in that row, this will be `B`
7. Go
   to https://github.com/FabricMC/yarn/blob/3ce892a659aa977aa50d1cb90a96dfcf270d4ba0/filament/src/test/resources/projects/sharedData/yarn-mappings-v2.tiny
8. Cntrl+F search for `B`, which will be the yarn mapping. The first column will show `c` for class, second column `m`
   for method, etc. So make sure you find what you want

### Yarn to Mojang Mappings

1. Go to https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/index-files/index-1.html
2. Search for the path you want
3. A table will contain the official namespace (usually 3 letters), this will be A
4. Go to https://piston-data.mojang.com/v1/objects/031a68bebf55d824f66d6573d8c752f0e1bf232a/client.txt
5. Search for " -> <A>"
6. The path on the left of it will be the Mojang mapping

### The easier way

1. Use https://linkie.shedaniel.dev/mappings, which allows you to search by the intermediary, official, yarn or mojang
   name
2. Make sure you choose which namespace you want (intermediary, yarn, etc.)
3. Once you have the intermediary path you can easily search for it in the same link but choose the mojang (via
   intermediary) or yarn namespace
