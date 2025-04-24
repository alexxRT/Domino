package game;

import model.*;
import java.util.*;

public class DominoGame {
    private GameBoard board;
    private List<Tile> placedTiles;
    private Tile leftTile;
    private Tile rightTile;

    public DominoGame(double width, double height, Position position) {
        this.board = new GameBoard(width, height, position);
        this.placedTiles = new ArrayList<>();
    }

    public GameResponse placeTile(int left, int right) {
        Tile newTile = new Tile(left, right);

        if (!isMoveValid(newTile)) {
            return new GameResponse(ResponseType.BAD_MOVE);
        }

        placeTileOnBoard(newTile);
        return createPlaceMoveResponse(newTile);
    }

    private boolean isMoveValid(Tile tile) {
        if (placedTiles.isEmpty()) return true;
        return leftTile.areMutable(tile) || rightTile.areMutable(tile);
    }

    private void placeTileOnBoard(Tile newTile) {
        if (placedTiles.isEmpty()) {
            setupFirstTile(newTile);
            return;
        }

        Tile muteTile = findMutableTile(newTile);
        setupTileMutation(newTile, muteTile);
        setupTilePosition(newTile, muteTile);
        placedTiles.add(newTile);
    }

    private void setupFirstTile(Tile tile) {
        leftTile = tile;
        rightTile = tile;
        boolean isDouble = tile.getRightVal() == tile.getLeftVal();
        tile.setRotate(isDouble ? 0 : -90);
        tile.setX(board.getPosition().getX() + board.getWidth() / 2);
        tile.setY(board.getPosition().getY() + board.getHeight() / 2);
        placedTiles.add(tile);
    }

    private Tile findMutableTile(Tile newTile) {
        if (leftTile.areMutable(newTile)) return leftTile;
        if (rightTile.areMutable(newTile)) return rightTile;
        throw new RuntimeException("No mutable tile found");
    }

    private void setupTileMutation(Tile newTile, Tile muteTile) {
        int muteVal = muteTile == leftTile ? leftTile.getLeftVal() : rightTile.getRightVal();
        boolean needSwap = false;

        if (muteTile.getMutedRight()) {
            needSwap = newTile.getRightVal() != muteVal;
            muteTile.setMutedLeft();
            newTile.setMutedRight();
        } else {
            needSwap = newTile.getLeftVal() != muteVal;
            newTile.setMutedLeft();
            muteTile.setMutedRight();
        }

        double rotateDegree = -90;
        if (newTile.getLeftVal() == newTile.getRightVal()) {
            rotateDegree += 90;
            newTile.setVertical(true);
        } else {
            rotateDegree += needSwap ? 180 : 0;
        }
        newTile.setRotate(rotateDegree);
    }

    private void setupTilePosition(Tile newTile, Tile muteTile) {
        newTile.setY(muteTile.getY());
        double deltaX = newTile.getRotateDegree() == 0 ?
            newTile.getWidth() / 2 : newTile.getLength() / 2;
        deltaX += muteTile.isVertical() ?
            muteTile.getWidth() / 2 : muteTile.getLength() / 2;

        if (muteTile == rightTile) {
            newTile.setX(muteTile.getX() + deltaX);
            rightTile = newTile;
        } else {
            newTile.setX(muteTile.getX() - deltaX);
            leftTile = newTile;
        }
    }

    private GameResponse createPlaceMoveResponse(Tile tile) {
        GameResponse response = new GameResponse(ResponseType.PLACE_MOVE);
        response.addUpdateTile(tile);
        return response;
    }

    public boolean resizeTileChain() {
        double resizeCoeff = calculateResizeCoefficient();
        if (resizeCoeff == 0) {
            return false;
        }

        applyResize(resizeCoeff);
        return true;
    }

    public GameResponse translateTileChain() {
        double deltaX = calculateTranslation();
        GameResponse response = new GameResponse(ResponseType.UPDATE_MOVE);

        if (deltaX == 0) {
            return response; // Returns empty UPDATE_MOVE response
        }

        applyTranslation(deltaX);

        // Add all updated tiles to response
        for (Tile tile : placedTiles) {
            response.addUpdateTile(tile);
        }

        return response;
    }

    public List<Tile> getPlacedTiles() {
        return placedTiles;
    }

    // Private helper methods
    private double calculateResizeCoefficient() {
        if (placedTiles.isEmpty()) {
            return 0;
        }

        double minDivergency = 2 * placedTiles.get(0).getLength();
        double chainLength = 0;

        for (Tile tile : placedTiles) {
            if (tile.getLeftVal() == tile.getRightVal()) {
                chainLength += tile.getLength();
            } else {
                chainLength += tile.getWidth();
            }
        }

        return board.getWidth() - chainLength < minDivergency ?
            (board.getWidth() - minDivergency) / chainLength : 1;
    }

    private void applyResize(double resizeCoeff) {
        placedTiles.forEach(tile -> {
            tile.setLength(resizeCoeff * tile.getLength());
            tile.setWidth(resizeCoeff * tile.getWidth());
        });
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
}