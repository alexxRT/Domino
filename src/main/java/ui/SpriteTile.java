package ui;

import model.*;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import connection.Connection;
import client.DominoClient;

public class SpriteTile extends ImageView {
    private Tile tile;

    // animatioins effects
    private PopUp slideTile = new PopUp();
    private DragMouse dragTile = new DragMouse(this);
    private MoveDesire moveTile = new MoveDesire();
    private PlaceHandler placeMove = new PlaceHandler(this);

    private boolean annimated = false;
    private int tileID = 0;

    public SpriteTile(Tile tile, boolean applyAnnimation) {
        this.tile = tile;
        annimated = applyAnnimation;
        try {
            String imagePath;
            if (this.tile.getLowVal() + this.tile.getHighVal() > 12)
                imagePath = getClass().getResource("/dominoDown.png").toExternalForm();
            else {
                var path = String.format("/dominoTile/%d-%d.png", tile.getLowVal(), tile.getHighVal());
                imagePath = getClass().getResource(path).toExternalForm();
            }
            setImage(new Image(imagePath));
            setFitHeight(tile.getLength());
            setFitWidth(tile.getWidth());
        } catch (Exception e) {
            System.out.println("Bad image init for tile!");
            e.printStackTrace();
        }

        if (annimated) {
            slideTile.apply(this);
            dragTile.apply(this);
            setOnMouseReleased(placeMove);
        }
    }

    public SpriteTile(Tile tile) {
        this.tile = tile;
        try {
            String imagePath;
            if (this.tile.getLowVal() + this.tile.getHighVal() > 12)
                imagePath = getClass().getResource("/dominoDown.png").toExternalForm();
            else {
                var path = String.format("/dominoTile/%d-%d.png", tile.getLowVal(), tile.getHighVal());
                imagePath = getClass().getResource(path).toExternalForm();
            }
            setImage(new Image(imagePath));
            setFitHeight(tile.getLength());
            setFitWidth(tile.getWidth());
        } catch (Exception e) {
            System.out.println("Bad image init for tile!");
            e.printStackTrace();
        }
    }

    public GameTable getTable() {
        return (GameTable)this.getParent();
    }

    final private class PlaceHandler implements EventHandler<MouseEvent> {

        private final SpriteTile toPlace;

        public PlaceHandler(SpriteTile toPlace) {
            this.toPlace = toPlace;
        }

        @Override
        public void handle(MouseEvent event) {
            GameTable table = getTable();
            Connection dominoServer = table.getConnection();
            int sessionID = table.getSessionID();

            GameResponse placeRequest = new GameResponse(ResponseType.PLACE_MOVE);
            placeRequest.setSessionID(sessionID);
            placeRequest.setTile(toPlace.getTile());

            try {
                // checks if currently my move to do
                // if not, return back in deck
                if (!DominoClient.dominoOn) {
                    table.removeTile(toPlace);
                    table.addPlayerDeck(toPlace.getTile());
                    return;
                }

                dominoServer.sendString(placeRequest.toString());
                System.out.println(placeRequest.toString());
                GameResponse placeResponse = table.getResponse(ResponseType.PLACE_MOVE);

                if (placeResponse.getStatus() == Status.OK) {
                    Tile responseTile = placeResponse.getTile();
                    toPlace.translateDesire(getLayoutX(), getLayoutY(), responseTile);

                    // turn off animation effects
                    dragTile.disable(toPlace);
                    slideTile.disable(toPlace);
                    setOnMouseReleased(null);

                    DominoClient.dominoOn = false; // end current move
                    table.updateStatusText();
                }
                else { // return tile back in deck
                    table.removeTile(toPlace);
                    table.addPlayerDeck(toPlace.getTile());
                }
            }
            catch (IOException ioExp) {
                System.out.println("Bad server response when placing new tile");
                table.removeTile(toPlace);
                table.addPlayerDeck(toPlace.getTile()); // even if server is unreachable -> keep UI consistent
            }
        }
    }

    public void translateDesire(double originX, double originY, Tile targetTile) {
        // optimization on no translate set
        if (areDoublesEqual(originX, targetTile.getX(), 1e-9) &&
            areDoublesEqual(originY, targetTile.getY(), 1e-9) &&
            areDoublesEqual(targetTile.getRotateDegree(), 0.0, 1e-9) &&
            !targetTile.getSwap())
            return;

        setLayoutX(originX);
        setLayoutY(originY);
        moveTile.setDesire(originX, originY, targetTile.getX(), targetTile.getY(),
                                    targetTile.getRotateDegree(), targetTile.getSwap());
        moveTile.apply(this);
    }

    public void translateDesire(double originX, double originY, double targetX, double targetY) {
        // optimization on no translate set
        if (areDoublesEqual(originX, targetX, 1e-9) &&
            areDoublesEqual(originY, targetY, 1e-9))
            return;

        setLayoutX(originX);
        setLayoutY(originY);

        moveTile.setDesire(originX, originY, targetX, targetY);
        moveTile.apply(this);
    }

    public Tile getTile() {
        return this.tile;
    }

    public int getID() { return tileID; }
    public void setID(int newID) { tileID = newID; }

    public void applyUpdate(Update upd) {
       System.out.println("Width: " + tile.getWidth());
       System.out.println("Lehgth: " + tile.getLength());

    //    setFitWidth(tile.getWidth() * upd.getResize());
    //    setFitHeight(tile.getLength() * upd.getResize());

    //    translateDesire(getLayoutX(), getLayoutY(),
    //                    getLayoutX() + upd.getDeltaX(), getLayoutY() + upd.getDeltaY());
    }
    // the problem is that tiles comparison happens implicitely and result is not controlled
    // need to properly override equals method

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null || obj.getClass() != this.getClass())
            return false;

        SpriteTile toCompare = (SpriteTile)obj;

        if (tile == toCompare.getTile() &&
            tileID == toCompare.getID())
            return true;
        return false;
    }

    private static boolean areDoublesEqual(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
}
}
