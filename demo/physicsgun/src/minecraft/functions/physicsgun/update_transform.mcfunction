summon area_effect_cloud ~ ~ ~ {CustomName:"$pgTransform",Duration:2}
tp @e[type=area_effect_cloud,name=$pgTransform,c=1] @s
gamerule logAdminCommands true
entitydata @e[type=area_effect_cloud,name=$pgTransform,c=1] {pg:transform}
gamerule logAdminCommands false
kill @e[type=area_effect_cloud,name=$pgTransform,c=1]