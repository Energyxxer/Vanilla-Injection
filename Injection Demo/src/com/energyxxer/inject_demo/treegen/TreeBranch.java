package com.energyxxer.inject_demo.treegen;

import static com.energyxxer.inject.InjectionBuffer.InjectionType.IMPULSE;

import de.adrodoc55.minecraft.coordinate.Vec3D;

/**
 * Created by User on 4/13/2017.
 */
public class TreeBranch {
    private final Tree tree;

    private final double angle;
    private final double length;

    private final double incline;

    private static double minLength = 0;
    private static double maxLength = 8;

    private static final String execute = "execute @e[type=armor_stand,name=$genTree] ~ ~ ~ ";

    private static double lastAngle = 0;

    TreeBranch(Tree tree) {
        this.tree = tree;
        this.angle = lastAngle + (tree.random.nextDouble() * Math.PI/2 - Math.PI/4) - (tree.random.nextBoolean() ? Math.PI/2 : - Math.PI/2);
        lastAngle = angle;

        double lengthFactor = getLengthFactor(tree.pos.y, tree.treeHeight);

        this.length = (tree.random.nextDouble()/2 + 0.5) * lengthFactor * (maxLength - minLength) + minLength;

        this.incline = tree.random.nextDouble() + 0.5;
    }

    void generate() {
        Vec3D bPos = tree.pos;

        for(int i = 0; i <= length; i++) {

            tree.connection.inject(IMPULSE, execute + "setblock ~" + (int) bPos.x + " ~" + bPos.y + " ~" + (int) bPos.z + " minecraft:log2 variant=dark_oak,axis=none");
            if(i / length < 0.2) { //Thicker
                tree.connection.inject(IMPULSE, execute + "fill ~" + ((int) bPos.x - 1) + " ~" + (bPos.y - 1) + " ~" + ((int) bPos.z) + " ~" + ((int) bPos.x + 1) + " ~" + (bPos.y) + " ~" + ((int) bPos.z) + " minecraft:leaves variant=" + tree.getRandomLeaf() + ",check_decay=false,decayable=false replace air *");
                tree.connection.inject(IMPULSE, execute + "fill ~" + ((int) bPos.x) + " ~" + (bPos.y - 1) + " ~" + ((int) bPos.z - 1) + " ~" + ((int) bPos.x) + " ~" + (bPos.y) + " ~" + ((int) bPos.z + 1) + " minecraft:leaves variant=" + tree.getRandomLeaf() + ",check_decay=false,decayable=false replace air *");
                tree.connection.inject(IMPULSE, execute + "fill ~" + ((int) bPos.x - 1) + " ~" + (bPos.y) + " ~" + ((int) bPos.z - 1) + " ~" + ((int) bPos.x + 1) + " ~" + (bPos.y) + " ~" + ((int) bPos.z + 1) + " minecraft:leaves variant=" + tree.getRandomLeaf() + ",check_decay=false,decayable=false replace air *");
            } else { //Not as thick
                tree.connection.inject(IMPULSE, execute + "fill ~" + ((int) bPos.x-1) + " ~" + (bPos.y) + " ~" + ((int) bPos.z) + " ~" + ((int) bPos.x+1) + " ~" + (bPos.y) + " ~" + ((int) bPos.z) + " minecraft:leaves variant=" + tree.getRandomLeaf() + ",check_decay=false,decayable=false replace air");
                tree.connection.inject(IMPULSE, execute + "fill ~" + ((int) bPos.x) + " ~" + (bPos.y-1) + " ~" + ((int) bPos.z) + " ~" + ((int) bPos.x) + " ~" + (bPos.y+1) + " ~" + ((int) bPos.z) + " minecraft:leaves variant=" + tree.getRandomLeaf() + ",check_decay=false,decayable=false replace air");
                tree.connection.inject(IMPULSE, execute + "fill ~" + ((int) bPos.x) + " ~" + (bPos.y) + " ~" + ((int) bPos.z-1) + " ~" + ((int) bPos.x) + " ~" + (bPos.y) + " ~" + ((int) bPos.z+1) + " minecraft:leaves variant=" + tree.getRandomLeaf() + ",check_decay=false,decayable=false replace air");
            }

            double deltaX = Math.cos(angle);
            double deltaY = (i / length) * -incline;
            double deltaZ = Math.sin(angle);
            bPos = bPos.plus(deltaX, deltaY, deltaZ);
        }
        tree.connection.inject(IMPULSE, execute + "setblock ~" + (int) bPos.x + " ~" + bPos.y + " ~" + (int) bPos.z + " minecraft:leaves variant=" + tree.getRandomLeaf() + ",check_decay=false,decayable=false keep");
    }

    private static double getLengthFactor(double y, double height) {
        return Math.sin(Math.PI * Math.pow(1 - (y/height), Math.PI)+0.1);
    }

    public static double getMinLength() {
        return minLength;
    }

    public static void setMinLength(double minLength) {
        TreeBranch.minLength = minLength;
    }

    public static double getMaxLength() {
        return maxLength;
    }

    public static void setMaxLength(double maxLength) {
        TreeBranch.maxLength = maxLength;
    }
}
