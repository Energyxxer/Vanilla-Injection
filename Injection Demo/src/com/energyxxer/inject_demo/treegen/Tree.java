package com.energyxxer.inject_demo.treegen;

import static com.energyxxer.inject.InjectionBuffer.InjectionType.IMPULSE;

import java.util.Random;

import com.energyxxer.inject.InjectionConnection;
import com.energyxxer.inject.utils.Vector3D;

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
    Vector3D.Double pos;
    Vector3D.Double incline;
    double inclineAngle;
    double globalInclineFactor;
    double mcInclineAngle;

    public Tree(InjectionConnection connection) {
        this.connection = connection;
    }

    void generate() {
        treeHeight = random.nextInt(maxTreeHeight - minTreeHeight + 1) + minTreeHeight;

        pos = new Vector3D.Double();
        incline = new Vector3D.Double();

        double minOffset = 0.1;
        double maxOffset = 0.6;

        inclineAngle = random.nextDouble() * 2 * Math.PI;
        globalInclineFactor = random.nextDouble() * (maxOffset - minOffset) + minOffset;

        incline.x = Math.cos(inclineAngle) * globalInclineFactor;
        incline.z = Math.sin(inclineAngle) * globalInclineFactor;

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

        Vector3D[] rootOffsets = new Vector3D[] {
                new Vector3D(0,0,-1),
                new Vector3D(1, 0, 0),
                new Vector3D(0, 0, 1),
                new Vector3D(1, 0, 0),
                null, null, null, null
        };
        rootOffsets[NE] = rootOffsets[N].translated(rootOffsets[E]);
        rootOffsets[SE] = rootOffsets[S].translated(rootOffsets[E]);
        rootOffsets[SW] = rootOffsets[S].translated(rootOffsets[W]);
        rootOffsets[NW] = rootOffsets[N].translated(rootOffsets[W]);


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
                    Vector3D offset = rootOffsets[d];
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

            pos.y++;

            pos.x += incline.x * heightFactor;
            pos.z += incline.z * heightFactor;

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
