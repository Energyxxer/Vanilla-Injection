summon area_effect_cloud ~ ~ ~ {CustomName:"$weTransform",Duration:2}
tp @e[type=area_effect_cloud,name=$weTransform,c=1] @s
gamerule logAdminCommands true
entitydata @e[type=area_effect_cloud,name=$weTransform,c=1] {we:transform}
gamerule logAdminCommands false
kill @e[type=area_effect_cloud,name=$weTransform,c=1]