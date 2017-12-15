scoreboard players tag @a[tag=we_sneaking] remove we_sneaking
scoreboard players tag @a[score_we_sneak_min=1] add we_sneaking
scoreboard players set @a[score_we_sneak_min=1] we_sneak 0

execute @a[score_we_wand_min=1] ~ ~ ~ function worldedit:filter_trigger

execute @a[score_we_wand_min=1,score_we_wand=1] ~ ~ ~ function worldedit:update_transform
execute @a[score_we_wand_min=1,score_we_wand=1] ~ ~ ~ function worldedit:send_action
scoreboard players reset @a[score_we_wand_min=1] we_wand
tp @e[type=shulker,name=wePosMarker,score_we_timer_min=20] ~ -512 ~
scoreboard players add @e[type=shulker,name=wePosMarker] we_timer 1
execute @a ~ ~ ~ scoreboard players set @e[type=shulker,name=wePosMarker,r=1] we_timer 20