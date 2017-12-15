scoreboard players add @a pg_state 0

execute @a[score_pg_trigger_min=1] ~ ~ ~ function physicsgun:filter_trigger

scoreboard players tag @a[tag=pg_sneaking] remove pg_sneaking
scoreboard players tag @a[score_pg_sneak_min=1] add pg_sneaking
scoreboard players set @a[score_pg_sneak_min=1] pg_sneak 0
execute @a[score_pg_trigger_min=1] ~ ~ ~ function physicsgun:update_transform
execute @a[score_pg_trigger_min=1] ~ ~ ~ function physicsgun:send_trigger

scoreboard players set @a[score_pg_state_min=3] pg_state 0

execute @a[score_pg_state_min=2,score_pg_state=2] ~ ~ ~ function physicsgun:update_transform
execute @a ~ ~ ~ function physicsgun:update_slot

scoreboard players set @a[score_pg_state=0] pg_timeout 0
scoreboard players set @a[score_pg_state_min=2] pg_timeout 0
scoreboard players add @a[score_pg_state_min=1,score_pg_state=1] pg_timeout 1
scoreboard players set @a[score_pg_timeout_min=60] pg_state 0
tellraw @a[score_pg_timeout_min=60] {"text":"The injection master for the Physics Gun module appears to be down. Try again later","color":"red"}