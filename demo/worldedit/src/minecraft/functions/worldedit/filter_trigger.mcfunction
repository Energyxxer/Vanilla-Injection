scoreboard players tag @s add we_valid {SelectedItem:{id:"minecraft:carrot_on_a_stick",tag:{IsWorldEditWand:1b}}}
scoreboard players tag @s add we_valid {Inventory:[{Slot:-106b,id:"minecraft:carrot_on_a_stick",tag:{IsWorldEditWand:1b}}]}
scoreboard players reset @s[tag=!we_valid] we_wand
scoreboard players tag @s[tag=we_valid] remove we_valid