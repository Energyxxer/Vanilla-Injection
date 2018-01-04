gamerule logAdminCommands true
scoreboard players set @s[score_pg_state_min=0,score_pg_state=0] pg_state 1
scoreboard players set @s[score_pg_state_min=2,score_pg_state=2] pg_state 3
gamerule logAdminCommands false
scoreboard players reset @s pg_trigger