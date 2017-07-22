# Vanilla-Injection
A library for the communication of Java programs and the Minecraft environment

# Examples
Examples shown in the video are available as source code (Injection Demo/src) and as ready-to-run executables (Compiled examples). Required functions and resource packs are also in the compiled examples.
When opening the example .jar files, you will be prompted a .minecraft directory path (It will try to guess the default path), and the name of the world folder to run the module in. Once you click "OK", the module should be running.
In order to set up the injection structure block, place a structure block in your world (Make sure there is enough space for the structure; some modules, like the tree generator, require a lot more space than others), set it to LOAD mode, and set it to load the following structures:

  * for Jarbot, load "inject/jarbot0"
  * for Physics Gun, load "inject/physicsgun0"
  * for Tree Generator, load "inject/treegen0"
  * for Warps, load "inject/warp0"
  * for WorldEdit, load "inject/worldedit0"

After that, make sure it is getting a redstone signal each tick. You may use any clock of your liking, but a setblock clock is recommended.

# Jarbot
There is no extra setup required for this module.
The way jarbot operates is rather simple. If the Jarbot module is running in your world, Jarbot will respond to anything you say in chat (That doesn't start with a period ".").
Jarbot uses the default chat memory log from Onnowhere's Albert AI.
Onnowhere: https://www.youtube.com/channel/UCtuEP-CE04rF2xjop9Pj0rQ
Albert AI: https://www.youtube.com/watch?v=IZJUtVh6ge4

# Physics Gun
Once you have the Physics Gun functions installed on your world and done /reload, run the following command:
/function physicsgun:init
This will set up all the scoreboard objectives and give you a Physics Gun item.
NOTE: Make sure that the function physicsgun:main is running every tick, may it be from the gameLoopFunction gamerule or a repeating command block.

This is based on the Physics Gun and Gravity Gun from Garry's Mod. To utilize, simply hold the physics gun (preferably in the offhand slot) and point it at an entity at max 10 blocks away.
Once you've done that, the entity will follow your crosshair wherever you look. At this point you may use the scrollwheel to increase or decrease the distance between you and the entity.
To free the controlled entity, right-click again with the physics gun to launch them in the direction you're looking, or shift-right-click to drop the entity.
This should work in multiplayer.

# Tree Generator
Once you have the Tree Generator functions installed on your world and done /reload, run the following command:
/function treegen:init
This will give you an armor stand used to generate a tree.
NOTE: Make sure that the function treegen:main is running every tick, may it be from the gameLoopFunction gamerule or a repeating command block.

This will generate a giant spruce tree wherever you place the armor stand. There are a few variables you can change by saying the following commands in chat:

  * .setMinTreeHeight \<integer : height>: Sets the minimum tree height.
  * .setMaxTreeHeight \<integer : height>: Sets the maximum tree height.
  * .setBranchChance \<double : chance>: Number between 0 and 1 that determines how likely it is for a branch to generate per y level. 0 is never, 1 is always.
  * .setMinBranchLength \<integer : length>: Sets the minimum branch length.
  * .setMaxBranchLength \<integer : length>: Sets the maximum branch length.
  * .setBaseHeight \<integer : height>: Sets the distance between the first branch and the ground.
Note that these commands were added in a hurry, just for fun and they have weak error handling.
Very high values may cause the command blocks to exceed the injection limit. If this happens, the tree generator will stop working until you re-open the program.

# Warps
There is no extra setup required for this module.
Once this is running in a world, you can say the following commands in chat:

  * .warp \<string : name>: Teleports to a warp of the given name, if it exists.
  * .warp set \<string : name>: Sets a warp at the current position, if a warp by the given name doesn't already exist.
  * .warp remove \<string : name>: Removes a warp by the given name, if it exists.
  * .warp list: Prints a clickable list of all the warps available.

All warps you set will be saved to a warps.txt file inside your world's folder so that they work even after restarting the warp system.

# WorldEdit
Once you have the WorldEdit functions installed on your world and done /reload, run the following command:
/function worldedit:init
This will set up all the scoreboard objectives and give you a WorldEdit Wand item.
NOTE: Make sure that the function worldedit:main is running every tick, may it be from the gameLoopFunction gamerule or a repeating command block.

With the wand in hand, right-click to set Position 1, and shift-right-click to set Position 2 to select a region.
NOTE: Because of how Minecraft saves the world, the block at which you point may not be the block that the injector sees. If the wand selects a block that isn't there, or doesn't select a block that is, pause and unpause the game to save the world and try again.
These are all the available commands:

  * ..set \<block>: Sets the selected region to the specified block.
  * ..replace \<block1> <block2>: Replaces any instances of block1 with block2 in the selected region.
  * ..walls \<block>: Sets the walls at the edges of the region to the specified block.
  * ..walls \<block1> <block2>: A combination of the previous two commands. Replaces block1 with block2 at the walls of the region.
  * ..center \<block>: Sets the center block(s) of the region to the specified block type.
  * ..up \<integer : amount>: Teleports you <amount> blocks up and places a glass block under your feet.
  * ..pos1: Sets Position 1 to the block at your feet.
  * ..pos2: Sets Position 2 to the block at your feet.
  * ..pos1 \<integer : x> \<integer : y> \<integer : z>: Sets Position 1 to the given coordinates.
  * ..pos2 \<integer : x> \<integer : y> \<integer : z>: Sets Position 2 to the given coordinates.
  
All of the block-related commands will place blocks past the limit of the /fill command.

BLOCK ARGUMENT:
There are 3 forms for the block argument:

  * \<block name>: In a setting context, it will place the given block in its default blockstate. In a replacing context, it will replace the given block, in any of its blockstates.
  * \<block name>:\<numerical data>: Refers to the block with its data value.
  * \<block name>#\<blockstate>: Refers to the block with its blockstate. There is little to no error handling of the blockstate, as most is done by Minecraft.
  
  Examples:
  * glass
  * wool:5
  * concrete#color=red
  * log#variant=spruce,axis=none
  
# Library
The injection library source is in 'Vanilla Injection/src'. All injection classes contain documentation for all fields and methods.
Classes of interest are 'com.energyxxer.inject.InjectionMaster', 'com.energyxxer.inject.Injector' and 'com.energyxxer.inject.level_utils.LevelReader'.
