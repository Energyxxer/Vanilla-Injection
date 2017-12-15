package com.energyxxer.inject_demo.treegen;

import static com.energyxxer.inject.InjectionBuffer.InjectionType.IMPULSE;

import java.util.Random;

import com.energyxxer.inject.InjectionConnection;

import de.adrodoc55.minecraft.coordinate.Vec3D;
import de.adrodoc55.minecraft.coordinate.Vec3I;

/**
 * Created by User on 4/12/2017.
 */
public class Tree {
    public final InjectionConnection connection;
    public final Random random = new Random();

    private static int minTreeHeight = 20;
    private static int maxTreeHeight = 30;

    private static double branchChance = 0.7;
    private static int baseHeight = 6;

    private static final int BRANCH_COOLDOWN = 3;

    private static final String execute = "execute @e[type=armor_stand,name=$genTree] ~ ~ ~ ";

    int treeHeight;
    Vec3D pos;
    Vec3D incline;
    double inclineAngle;
    double globalInclineFactor;
    double mcInclineAngle;

    public Tree(InjectionConnection connection) {
        this.connection = connection;
    }

    void generate() {
        treeHeight = random.nextInt(maxTreeHeight - minTreeHeight + 1) + minTreeHeight;

        pos = new Vec3D();

        double minOffset = 0.1;
        double maxOffset = 0.6;

        inclineAngle = random.nextDouble() * 2 * Math.PI;
        globalInclineFactor = random.nextDouble() * (maxOffset - minOffset) + minOffset;

        double inclineX = Math.cos(inclineAngle) * globalInclineFactor;
        double inclineZ = Math.sin(inclineAngle) * globalInclineFactor;
        incline = new Vec3D(inclineX, 0, inclineZ);

        mcInclineAngle = inclineAngle - 90;

        double rootHeight = treeHeight / 8d;

        int N = 0, E = 1, S = 2, W = 3, NE = 4, SE = 5, SW = 6, NW = 7;

        double[] rootHeights = new double[] {
            getRootAscent(Math.PI - mcInclineAngle) * rootHeight,
            getRootAscent((-Math.PI/2) - mcInclineAngle) * rootHeight,
            getRootAscent(-mcInclineAngle) * rootHeight,
            getRootAscent((Math.PI/2) - mcInclineAngle) * rootHeight,
            0,
            0,
            0,
            0
        };

        Vec3I[] rootOffsets = new Vec3I[] {
                new Vec3I(0, 0, -1),
                new Vec3I(1, 0, 0),
                new Vec3I(0, 0, 1),
                new Vec3I(1, 0, 0),
                null, null, null, null
        };
        rootOffsets[NE] = rootOffsets[N].plus(rootOffsets[E]);
        rootOffsets[SE] = rootOffsets[S].plus(rootOffsets[E]);
        rootOffsets[SW] = rootOffsets[S].plus(rootOffsets[W]);
        rootOffsets[NW] = rootOffsets[N].plus(rootOffsets[W]);


        double avgRootHeight = (rootHeights[N] + rootHeights[S] + rootHeights[E] + rootHeights[W]) / 4;
        for(int d = 0; d < 4; d++) rootHeights[d] = (rootHeights[d] - avgRootHeight) * globalInclineFactor + avgRootHeight;

        rootHeights[NE] = Math.min(rootHeights[N], rootHeights[E]) / 2 - (random.nextInt(2)+1);
        rootHeights[SE] = Math.min(rootHeights[S], rootHeights[E]) / 2 - (random.nextInt(2)+1);
        rootHeights[SW] = Math.min(rootHeights[S], rootHeights[W]) / 2 - (random.nextInt(2)+1);
        rootHeights[NW] = Math.min(rootHeights[N], rootHeights[W]) / 2 - (random.nextInt(2)+1);

        int branchCooldown = baseHeight;

        for(int y = 0; y < treeHeight; y++) {
            double heightFactor = (double) y / treeHeight;

            connection.inject(IMPULSE, execute + "setblock ~" + (int) pos.x + " ~" + pos.y + " ~" + (int) pos.z + " minecraft:log2 variant=dark_oak,axis=none");
            for(int d = 0; d < 8; d++) {
                if(y <= rootHeights[d]) {
                    Vec3I offset = rootOffsets[d];
                    connection.inject(IMPULSE, execute + "setblock ~" + ((int) pos.x + offset.x) + " ~" + pos.y + " ~" + ((int) pos.z + offset.z) + " minecraft:log2 variant=dark_oak,axis=none");
                }
            }

            if(branchCooldown <= 0) {
                for(int i = 0; i < (treeHeight-y)/2; i++) {
                    if(random.nextDouble() <= branchChance) {
                        new TreeBranch(this).generate();
                        branchCooldown = BRANCH_COOLDOWN;
                    }
                }
            }

            if(y > 6) {
                connection.inject(IMPULSE, execute + "fill ~" + ((int) pos.x-1) + " ~" + (pos.y) + " ~" + ((int) pos.z) + " ~" + ((int) pos.x+1) + " ~" + (pos.y) + " ~" + ((int) pos.z) + " minecraft:leaves variant=" + getRandomLeaf() + ",check_decay=false,decayable=false replace air");
                connection.inject(IMPULSE, execute + "fill ~" + ((int) pos.x) + " ~" + (pos.y-1) + " ~" + ((int) pos.z) + " ~" + ((int) pos.x) + " ~" + (pos.y+1) + " ~" + ((int) pos.z) + " minecraft:leaves variant=" + getRandomLeaf() + ",check_decay=false,decayable=false replace air");
                connection.inject(IMPULSE, execute + "fill ~" + ((int) pos.x) + " ~" + (pos.y) + " ~" + ((int) pos.z-1) + " ~" + ((int) pos.x) + " ~" + (pos.y) + " ~" + ((int) pos.z+1) + " minecraft:leaves variant=" + getRandomLeaf() + ",check_decay=false,decayable=false replace air");
            }

            pos = pos.plus(incline.x * heightFactor, 1, incline.z * heightFactor);

            if(branchCooldown > 0) branchCooldown--;
        }
    }

    private double getRootAscent(double angrad) {
        return (Math.cos(angrad + Math.PI)+1);
    }

    String getRandomLeaf() {
        return random.nextBoolean() ? "spruce" : "oak";
    }

    public static int getMinTreeHeight() {
        return minTreeHeight;
    }

    public static void setMinTreeHeight(int minTreeHeight) {
        Tree.minTreeHeight = minTreeHeight;
    }

    public static int getMaxTreeHeight() {
        return maxTreeHeight;
    }

    public static void setMaxTreeHeight(int maxTreeHeight) {
        Tree.maxTreeHeight = maxTreeHeight;
    }

    public static double getBranchChance() {
        return branchChance;
    }

    public static void setBranchChance(double branchChance) {
        Tree.branchChance = branchChance;
    }

    public static int getBaseHeight() {
        return baseHeight;
    }

    public static void setBaseHeight(int baseHeight) {
        Tree.baseHeight = baseHeight;
    }
}
