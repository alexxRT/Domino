package server;

import java.util.*;

public class domino {
    private double hight;
    private double width;
    // table borders left top corner coordinate
    private double x;
    private double y;

    //           width
    // (x, y) ----------------   h
    //    |                   |  i
    //    |    -- (tile)      |  g
    //    |                   |  h
    //     -------------------   t

    private class Tile {
        public int leftVal;
        public int rightVal;

        // states domino tile position on table
        private double x_pos; // center_x
        private double y_pos; // center_y

        private double length;
        private double width;

        private double rotateDegree;

        private boolean isVertical = false;
        private boolean mutedRight = false;
        private boolean mutedLeft  = false;

        public Tile(int left, int right) {
            leftVal = left;
            rightVal = right;
        }

        @Override
        public String toString() {
            return "Tile"
                    + "[l:" + leftVal + ";r:" + rightVal + "]"
                    + "[x:" + x_pos + ";y:" + y_pos + "]"
                    + "[rotate:" + rotateDegree + "]";
        }

        public boolean areMutable(Tile nextTile) {
            if ((rightVal == nextTile.getRightVal() || rightVal == nextTile.getLeftVal())
                 && !mutedRight)
                return true;

            if ((leftVal == nextTile.getRightVal() || leftVal == nextTile.getLeftVal())
                 && !mutedLeft)
                return true;

            return false;
        }

        @Override
        public boolean equals(Object x) {
            if (x instanceof Tile == false)
                return false;

            Tile tile = (Tile)x;
            if (leftVal != tile.getLeftVal() || rightVal != tile.getRightVal())
                return false;

            return true;
        }

        public int getLeftVal() {return leftVal;};
        public int getRightVal() {return rightVal;};
        public double getX() {return x_pos;};
        public double getY() {return y_pos;};
        public double getWidth() {return width;};
        public double getLength() {return length;};
        public double getSize() {return width * length;};
        public boolean getMutedRight() {return mutedRight;};
        public boolean getMutedLeft() {return mutedLeft;};
        public boolean isVertical(){return isVertical;};

        public void setX(double setX) {x_pos = setX;};
        public void setY(double setY) {y_pos = setY;};
        public void setRotate(double setDegree) {rotateDegree = setDegree;};
        public void setMutedRight() {mutedRight = true;};
        public void setMutedLeft() {mutedLeft = true;};
        public void setWidth(double setWidth) {width = setWidth;};
        public void setLength(double setLength) {length = setLength;};
        public void setVertical(boolean setVertical){isVertical = setVertical;};

    }

    // responsible to form text String message
    private class response {
        final static int BAD_MOVE    = 0;
        final static int PLACE_MOVE  = 1;
        final static int UPDATE_MOVE = 2;
        final static int RESIZE      = 3; // when too many tiles on board -> decrease tiles sizes

        private int move;
        public List<String> updateTiles = new ArrayList<>();

        public response(int move) {
            this.move = move;
        }

        public response(int move, Tile tile) {
            this.move = move;
            updateTiles.add(tile.toString());
        }

        public void addUpdateTile(Tile tile) {
            updateTiles.add(tile.toString());
        }

        @Override
        public String toString() {
            String response = new String();
            response += "Move [f:";
            switch (move) {
                case (BAD_MOVE):
                    response += "BAD_MOVE](";
                    break;
                case (PLACE_MOVE):
                    response += "PLACE_MOVE](";
                    break;
                case (UPDATE_MOVE):
                    response += "UPDATE_MOVE](";
                    break;
                default:
                    response += "UNKNOWN]()";
            }
            for (String tileInfo : updateTiles)
                response += tileInfo;
            return response;
        }
    }

    private Tile leftTile;
    private Tile rightTile;
    private List<Tile> placedTiles = new ArrayList<>();

    public String placeTile(int left, int right) throws RuntimeException  {
        Tile newTile = new Tile(left, right);

        if (!isMoveValid(newTile))
            return new response(response.BAD_MOVE).toString();

        // case of empty board
        if (placedTiles.size() == 0) {
            leftTile  = newTile;
            rightTile = newTile;
            boolean isDouble = newTile.getRightVal() == newTile.getLeftVal();
            newTile.setRotate(isDouble? 0: -90);
            // place on board center
            newTile.setX(x + width / 2);
            newTile.setY(y + hight / 2);
        }

        Tile muteTile;
        int muteVal;
        if (leftTile.areMutable(newTile)) {
            muteTile = leftTile;
            muteVal = leftTile.getLeftVal();
        }
        else if (rightTile.areMutable(newTile)){
            muteTile  = rightTile;
            muteVal = rightTile.getRightVal();
        }
        else {
            String errorMsg = "Tile immutable, but assumed valid move";
            throw new RuntimeException(errorMsg);
        }
        // finish place tile and track relative orientation
        boolean needSwope = false;
        if (muteTile.getMutedRight()) {
            needSwope = newTile.getRightVal() != muteVal;
            muteTile.setMutedLeft();
            newTile.setMutedRight();
        }
        else {
            needSwope = newTile.getLeftVal() != muteVal;
            newTile.setMutedLeft();
            muteTile.setMutedRight();
        }
        // originaly all tiles oriented verticaly, rotate clockwise to formate a chain
        double rotateDegree = -90;
        // there are some cases, when we need adjust rotation degree
        if (newTile.getLeftVal() == newTile.getRightVal()) {
            rotateDegree += 90;
            newTile.setVertical(true);
        }
        else
            rotateDegree += needSwope? 180 : 0;
        newTile.setRotate(rotateDegree);

        boolean needResize = muteTile.getSize() != newTile.getSize();
        if (needResize) {
            newTile.setWidth(muteTile.getWidth());
            newTile.setLength(muteTile.getLength());
        }

        // finish placing tile and set x, y
        // placing to right tail
        newTile.setY(muteTile.getY());
        double deltaX = rotateDegree == 0? newTile.getWidth() / 2: newTile.getLength() / 2;
        deltaX += muteTile.isVertical()? muteTile.getWidth() / 2: muteTile.getLength() / 2;
        if (muteTile == rightTile) {
            newTile.setX(newTile.getX() + deltaX);
            rightTile = newTile;
        }
        else {
            newTile.setX(newTile.getX() - deltaX);
            leftTile = newTile;
        }

    }
    public boolean resizeTileChain() {
        double resizeCoeff = getResize();
        // TODO:
        if (resizeCoeff == 0)
            return false;

        for (Tile tile: placedTiles) {
            tile.setLength(resizeCoeff * tile.getLength());
            tile.setWidth(resizeCoeff * tile.getWidth());
        }
        return true;
    }

    private double getResize() {
        if (placedTiles.size() == 0)
            return 0;

        double minDivergency = 2 * placedTiles.get(0).getLength();
        double chainLength = 0;

        for (Tile tile : placedTiles) {
            if (tile.getLeftVal() == tile.getRightVal())
                chainLength += tile.getWidth();
            else
                chainLength += tile.getLength();
        }
        return chainLength > width - minDivergency? (width - minDivergency) / chainLength: 0;
    }


    public boolean translateTileChain() {
        double deltaX = getTranslate();
        // TODO:
        if (deltaX == 0) // hope zero-double comparison works in java :)
            return false;

        for (Tile tile : placedTiles) {
            tile.setX(tile.getX() + deltaX);
        }
        return true;
    }

    private double getTranslate() {
        if (placedTiles.size() == 0)
            return 0;

        double maxDivergency = 2 * placedTiles.get(0).getLength();

        double centerTableX = x + width / 2;
        double centerChainX;
        for (Tile tile : placedTiles)
            centerChainX += tile.getX();
        centerChainX /= placedTiles.size();

        return Math.abs(centerTableX - centerChainX) > maxDivergency? centerTableX - centerChainX: 0;
    }

    private boolean isMoveValid(Tile tile) {
        if (!leftTile.areMutable(tile) && !rightTile.areMutable(tile))
            return false;

        return true;
    }
}
