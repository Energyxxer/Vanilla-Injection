scoreboard players tag @s add pg_valid {SelectedItem:{id:"minecraft:carrot_on_a_stick",tag:{IsPhysicsGun:1b}}}
scoreboard players tag @s add pg_valid {Inventory:[{Slot:-106b,id:"minecraft:carrot_on_a_stick",tag:{IsPhysicsGun:1b}}]}
scoreboard players reset @s[tag=!pg_valid] pg_trigger
scoreboard players tag @s[tag=pg_valid] remove pg_valid