package game;

import model.*;
import java.util.*;

public class DominoGame {
    private GameBoard board;
    private List<Tile> placedTiles = new ArrayList<>();
    private Tile leftTile;
    private Tile rightTile;
    private List<Tile> bazarTiles = new ArrayList<>();

    public static final int initialNumTiles = 6;

    public DominoGame(double width, double height, Position position) {
        this.board = new GameBoard(width, height, position);

        // here we need init all available dominos
        for (int i = 0; i <= 6; i ++)
            for (int j = 0; j <= i; j ++)
               bazarTiles.add(new Tile(i, j));

        // remove random n = 10 tiles to speedup game
        int n = 10;
        for (int i = 0; i < n; i ++) {
            getRandomTile();
        }
    }

    public GameResponse placeTile(Tile newTile) {
        if (!isMoveValid(newTile)) {
            return new GameResponse(ResponseType.PLACE_MOVE, Status.AGAIN);
        }

        // here tile has updated and correct coordinates of top left corner
        Tile tableTile = placeTileOnBoard(newTile);

        // System.out.println(" ---------------- Placing ----------------");
        // System.out.println("Right tile: " + rightTile.toString());
        // System.out.println("Left tile: " + leftTile.toString());
        // System.out.println("Placing tile: " + newTile.toString());
        // System.out.println("Tile on table: " + tableTile.toString());
        // System.out.println("\n\n\n");

        // this delta with respective to table tile left top corner coordinate
        int placeRight = rightTile == newTile ? 0 : 1;
        // System.out.println("Placing right: " + placeRight);
        double deltaX = getTileDeltaX(tableTile, newTile)[placeRight];
        double deltaY = getTileDeltaY(tableTile, newTile)[placeRight];

        // System.out.println("deltaX = " + deltaX);
        // System.out.println("deltaY = " + deltaY);

        return createPlaceMoveResponse(tableTile, newTile, deltaX, deltaY);
    }

    public GameResponse getRandomTile(List<Tile> userTiles) {
        if (bazarTiles.size() == 0)
            return new GameResponse(ResponseType.GET_TILE, Status.AGAIN);

        // just in case when user request tile from bazar,
        // but he has valid move to do
        System.out.println("Number tiles user has " + userTiles.size());
        System.out.println(userTiles);

        for (Tile tile : userTiles) {
            if (isMoveValid(tile)) {
                System.out.println("User has valid move!");
                return new GameResponse(ResponseType.GET_TILE, Status.AGAIN);
            }
        }

        Random getRandom = new Random();
        int randomIndex;
        randomIndex = getRandom.nextInt(bazarTiles.size());
        Tile newTile = bazarTiles.remove(randomIndex);

        GameResponse randomTile = new GameResponse(ResponseType.GET_TILE);
        randomTile.setTile(newTile);
        userTiles.add(newTile); // update number of user's tiles
        return randomTile;
    }

    public GameResponse getRandomTile() {
        if (bazarTiles.size() == 0)
            return new GameResponse(ResponseType.GET_TILE, Status.AGAIN);

        Random getRandom = new Random();
        int randomIndex;
        randomIndex = getRandom.nextInt(bazarTiles.size());
        Tile newTile = bazarTiles.remove(randomIndex);

        GameResponse randomTile = new GameResponse(ResponseType.GET_TILE);
        randomTile.setTile(newTile);
        return randomTile;
    }

    public boolean isMoveValid(Tile tile) {
        if (placedTiles.isEmpty())
            return tile.getHighVal() == tile.getLowVal(); // start game only with n | n tile
        return leftTile.isConnectable(tile) || rightTile.isConnectable(tile);
    }

    public int getBazarNum() {
        return bazarTiles.size();
    }

    private Tile placeTileOnBoard(Tile newTile) {
        if (placedTiles.isEmpty()) {
            setupFirstTile(newTile);
            return newTile;
        }

        // choose to which connect - right or left
        Tile tableTile = chooseConnectableTile(newTile);
        setupTileConnection(tableTile, newTile);
        setupTilePosition(tableTile, newTile);
        placedTiles.add(newTile);

        return tableTile;
    }

    // this works fine - no adjustemnts //
    private void setupFirstTile(Tile tile) {
        leftTile = tile;
        rightTile = tile;

        if (!(tile.getHighVal() == tile.getLowVal()))
            throw new RuntimeException("First tile can be (n | n) double only!");

        tile.setVertical(true);

        // centric tile more precisely
        tile.setX(board.getWidth() / 2 - tile.getWidth() / 2);
        tile.setY(board.getHeight() / 2 - tile.getLength() / 2);

        placedTiles.add(tile);
    }

    private Tile chooseConnectableTile(Tile newTile) {
        if (leftTile.isConnectable(newTile)) return leftTile;
        if (rightTile.isConnectable(newTile)) return rightTile;
        throw new RuntimeException("No mutable tile found");
    }

    private Tile setupTileConnection(Tile tableTile, Tile newTile) {
        int connectVal = tableTile == leftTile ? leftTile.getConnectVal() :
                                                 rightTile.getConnectVal();

        newTile.setVertical(newTile.getHighVal() == newTile.getLowVal());

        if (!newTile.isVertical()) {
            newTile.setRotate(tableTile == leftTile ? 90 : -90);
            newTile.setSwap(connectVal == newTile.getLowVal());
        }

        return newTile;
    }

    private double[] placeHoriontalX (Tile tableTile, Tile placeTile) {
                            // place right    // place left
        return new double[]{tableTile.getLength(), 0};
    }

    private double[] placeHoriontalY (Tile tableTile, Tile placeTile) {
                            // place right  // place left
        return new double[]{tableTile.getWidth(), 0};
    }

    private double[] placeVerticalX(Tile tableTile, Tile placeTile) {
        if (tableTile.isVertical())
            return new double[]{tableTile.getWidth(), 0};

        return new double[]{tableTile.getLength(), -placeTile.getWidth()};
    }

    private double[] placeVerticalY(Tile tableTile, Tile placeTile) {
        if (tableTile.isVertical()) {
            System.out.println("Place VerticatY table: Vertical");
            return new double[]{tableTile.getLength() / 2 + placeTile.getWidth() / 2,
                                placeTile.getLength() / 2 - tableTile.getWidth() / 2};
        }
        System.out.println("Place VerticatY table: Horizontal");
        return new double[]{tableTile.getWidth() / 2 - placeTile.getLength() / 2,
                            tableTile.getWidth() / 2 - placeTile.getLength() / 2};
    }

    private double[] getTileDeltaX(Tile tableTile, Tile newTile) {
        // in case when first tile placed -> no delta needed
        if (tableTile == newTile)
            return new double[] {0, 0};

        if (newTile.isVertical() && tableTile.isVertical())
            throw new RuntimeException("Placing two tiles vertical to each other!");

        if (!(newTile.isVertical() || tableTile.isVertical()))
            return placeHoriontalX(tableTile, newTile);

        return placeVerticalX(tableTile, newTile);
    }

    private double[] getTileDeltaY(Tile tableTile, Tile newTile) {
        // in case when first tile placed -> no delta needed
        if (tableTile == newTile)
            return new double[] {0, 0};

        if (newTile.isVertical() && tableTile.isVertical())
            throw new RuntimeException("Placing two tiles vertical to each other!");

        if (!(newTile.isVertical() || tableTile.isVertical()))
            return placeHoriontalY(tableTile, newTile);

        return placeVerticalY(tableTile, newTile);
    }

    // keep top left corner consistency
    private void setupTilePosition(Tile tableTile, Tile newTile) {
        double[] deltaX;
        double[] deltaY;

         if (!(newTile.isVertical() || tableTile.isVertical())) {
            deltaX = new double[]{tableTile.getLength(), -tableTile.getLength()};
            deltaY = new double[]{0, 0};
         } else {
            deltaX = tableTile.isVertical() ?
            new double[]{tableTile.getWidth(), -newTile.getLength()} :
            new double[]{tableTile.getLength(), -newTile.getWidth()};


            deltaY = tableTile.isVertical() ?
            new double[]{tableTile.getLength() / 2 - newTile.getWidth() / 2,
                         tableTile.getLength() / 2 - newTile.getWidth() / 2} :
            new double[]{tableTile.getWidth() / 2 - newTile.getLength() / 2,
                         tableTile.getWidth() / 2 - newTile.getLength() / 2};
         }

        // case rightTile == leftTile == (n | n) at the beginning of the game
        if (rightTile == leftTile) { // this if happens only once, maybe rewrite logic? ....
            leftTile = newTile.getRotateDegree() == 90 ? newTile : leftTile;
            rightTile = newTile.getRotateDegree() == -90 ? newTile : rightTile;
        } else {
            rightTile = tableTile == rightTile ? newTile : rightTile;
            leftTile = tableTile == leftTile ? newTile : leftTile;
        }

        // set new domino on table logic
        newTile.setX(tableTile.getX() + deltaX[newTile == rightTile ? 0 : 1]);
        newTile.setY(tableTile.getY() + deltaY[newTile == rightTile ? 0 : 1]);
    }

    private GameResponse createPlaceMoveResponse(Tile tableTile, Tile placedTile, double deltaX, double deltaY) {
        GameResponse response = new GameResponse(ResponseType.PLACE_MOVE);
        Tile responseTile = new Tile(placedTile.getLowVal(), placedTile.getHighVal());

        responseTile.setX(tableTile.getX() + deltaX);
        responseTile.setY(tableTile.getY() + deltaY);
        responseTile.setRotate(placedTile.getRotateDegree());
        responseTile.setSwap(placedTile.getSwap());

        response.setTile(responseTile);
        return response;
    }

    public GameResponse updatePos() {
        GameResponse response = new GameResponse(ResponseType.UPDATE_POS);

        double resizeCoeff = calculateResizeCoefficient();
        double deltaX = calculateTranslation();

        // applyTranslation(deltaX);
        // applyResize(resizeCoeff);

        response.setUpdate(resizeCoeff, deltaX, 0);
        return response;
    }

    public List<Tile> getPlacedTiles() {
        return placedTiles;
    }

    // Private helper methods
    private double calculateResizeCoefficient() {
        if (placedTiles.isEmpty())
            return 1;

        double minDivergency = 2 * placedTiles.get(0).getLength();
        double chainLength = 0;

        for (Tile tile : placedTiles) {
            if (tile.getLowVal() == tile.getHighVal()) {
                chainLength += tile.getWidth();
            } else {
                chainLength += tile.getLength();
            }
        }

        return board.getWidth() - chainLength < minDivergency ?
            (board.getWidth() - minDivergency) / chainLength : 1;
    }

    private double calculateTranslation() {
        if (placedTiles.isEmpty()) {
            return 0;
        }

        double maxDivergency = 2 * placedTiles.get(0).getLength();
        double centerTableX = board.getPosition().getX() + board.getWidth() / 2;
        double centerChainX = calculateChainCenter();

        return Math.abs(centerTableX - centerChainX) > maxDivergency ?
               centerTableX - centerChainX : 0;
    }

    private double calculateChainCenter() {
        return placedTiles.stream()
                         .mapToDouble(Tile::getX)
                         .average()
                         .orElse(0);
    }

    private void applyTranslation(double deltaX) {
        placedTiles.forEach(tile -> tile.setX(tile.getX() + deltaX));
    }

    private void applyResize(double resizeCoeff) {
        placedTiles.forEach(tile -> {
            tile.setLength(resizeCoeff * tile.getLength());
            tile.setWidth(resizeCoeff * tile.getWidth());
        });
    }
}
