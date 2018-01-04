scoreboard players set @s pg_slot 0 {SelectedItemSlot:0}
scoreboard players set @s pg_slot 1 {SelectedItemSlot:1}
scoreboard players set @s pg_slot 2 {SelectedItemSlot:2}
scoreboard players set @s pg_slot 3 {SelectedItemSlot:3}
scoreboard players set @s pg_slot 4 {SelectedItemSlot:4}
scoreboard players set @s pg_slot 5 {SelectedItemSlot:5}
scoreboard players set @s pg_slot 6 {SelectedItemSlot:6}
scoreboard players set @s pg_slot 7 {SelectedItemSlot:7}
scoreboard players set @s pg_slot 8 {SelectedItemSlot:8}
scoreboard players operation @s pg_distChange = @s pg_previousSlot
scoreboard players operation @s pg_distChange -= @s pg_slot
scoreboard players operation @s pg_previousSlot = @s pg_slot
scoreboard players remove @s[score_pg_distChange_min=5] pg_distChange 9
scoreboard players add @s[score_pg_distChange=-5] pg_distChange 9
execute @s[score_pg_distChange_min=1] ~ ~ ~ function physicsgun:on_slot_changed
execute @s[score_pg_distChange=-1] ~ ~ ~ function physicsgun:on_slot_changed