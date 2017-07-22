execute @e[type=armor_stand,name=$genTree,tag=!seen] ~ ~ ~ gamerule logAdminCommands true
execute @e[type=armor_stand,name=$genTree,tag=!seen,c=1] ~ ~ ~ gamerule logAdminCommands false
scoreboard players tag @e[type=armor_stand,name=$genTree,tag=!seen] add seen