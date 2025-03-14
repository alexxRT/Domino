package Tile;

public class Tile {
    private int left;
    private int right;

    public Tile(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public int getLeft() {
        return this.left;
    }

    public int getRight() {
        return this.right;
    }

    @Override
    public boolean equals(Object obj) {
        Tile compTile = (Tile)obj;
        if (compTile.left == left && compTile.right == right)
            return true;
        return false;
    }
}
