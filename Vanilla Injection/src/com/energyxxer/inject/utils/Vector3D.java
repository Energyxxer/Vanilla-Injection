package com.energyxxer.inject.utils;

/**
 * Class containing integer values for x, y and z axes.
 * Used in most of the injection code to represent positions and, rarely, dimensions in 3D space.
 */
public class Vector3D {
    /**The x value associated with this vector.*/
    public int x,
    /**The y value associated with this vector.*/
            y,
    /**The z value associated with this vector.*/
            z;

    /**
     * Creates a vector at 0,0,0.
     * */
    public Vector3D() {
        this(0,0,0);
    }

    /**
     * Creates a vector from the given coordinates.
     *
     * @param x The position in the X axis.
     * @param y The position in the Y axis.
     * @param z The position in the Z axis.
     * */
    public Vector3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a vector from another vector's coordinates.
     *
     * @param vector The vector to clone.
     * */
    public Vector3D(Vector3D vector) {
        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector3D vector3D = (Vector3D) o;

        if (x != vector3D.x) return false;
        if (y != vector3D.y) return false;
        return z == vector3D.z;
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z;
    }

    /**
     * Displaces this vector by the given amounts.
     *
     * @param dx The distance to move in the X axis.
     * @param dy The distance to move in the Y axis.
     * @param dz The distance to move in the Z axis.
     * */
    public void translate(int dx, int dy, int dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    /**
     * Displaces this vector by adding the position of another vector.
     *
     * @param vector Another vector to add to this vector.
     * */
    public void translate(Vector3D vector) {
        this.translate(vector.x, vector.y, vector.z);
    }

    /**
     * Creates a vector by displacing this vector by the given amounts.
     *
     * @param dx The distance to move in the X axis.
     * @param dy The distance to move in the Y axis.
     * @param dz The distance to move in the Z axis.
     *
     * @return Another Vector3D object formed by adding the specified amounts to the original vector.
     * */
    public Vector3D translated(int dx, int dy, int dz) {
        return new Vector3D(this.x + dx, this.y + dy, this.z + dz);
    }

    /**
     * Creates a vector by adding the position of another vector.
     *
     * @param vector Another vector to add to this vector.
     *
     * @return Another Vector3D object formed by adding the specified vector vector to the original vector.
     * */
    public Vector3D translated(Vector3D vector) {
        return this.translated(vector.x,vector.y,vector.z);
    }

    /**
     * Class containing double values for x, y and z axes.
     * */
    public static class Double {
        /**The x value associated with this vector.*/
        public double x,
        /**The y value associated with this vector.*/
        y,
        /**The z value associated with this vector.*/
        z;

        /**
         * Creates a vector at 0,0,0.
         * */
        public Double() {
            this(0,0,0);
        }
        /**
         * Creates a vector from the given coordinates.
         *
         * @param x The position in the X axis.
         * @param y The position in the Y axis.
         * @param z The position in the Z axis.
         * */
        public Double(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Creates a vector from another vector's coordinates.
         *
         * @param vector The vector to clone.
         * */
        public Double(Double vector) {
            this.x = vector.x;
            this.y = vector.y;
            this.z = vector.z;
        }

        /**
         * Displaces this vector by the given amounts.
         *
         * @param dx The distance to move in the X axis.
         * @param dy The distance to move in the Y axis.
         * @param dz The distance to move in the Z axis.
         * */
        public void translate(double dx, double dy, double dz) {
            this.x += dx;
            this.y += dy;
            this.z += dz;
        }

        /**
         * Displaces this vector by adding the position of another vector.
         *
         * @param vector Another vector to add to this vector.
         * */
        public void translate(Double vector) {
            this.translate(vector.x, vector.y, vector.z);
        }

        /**
         * Creates a vector by displacing this vector by the given amounts.
         *
         * @param dx The distance to move in the X axis.
         * @param dy The distance to move in the Y axis.
         * @param dz The distance to move in the Z axis.
         *
         * @return Another Vector3D.Double object formed by adding the specified amounts to the original vector.
         * */
        public Double translated(double dx, double dy, double dz) {
            return new Double(this.x + dx, this.y + dy, this.z + dz);
        }

        /**
         * Creates a vector by adding the position of another vector.
         *
         * @param vector Another vector to add to this vector.
         *
         * @return Another Vector3D.Double object formed by adding the specified vector vector to the original vector.
         * */
        public Double translated(Double vector) {
            return translated(vector.x,vector.y,vector.z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Vector3D vector3D = (Vector3D) o;

            if (x != vector3D.x) return false;
            if (y != vector3D.y) return false;
            return z == vector3D.z;
        }

        @Override
        public String toString() {
            return x + " " + y + " " + z;
        }

        public Vector3D asIntVector() {
            return new Vector3D((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        }
    }
}
