package ui;

import model.*;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import connection.Connection;
import game.DominoGame;

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

        //String imagePath = "sprite/" + left + "_" + right + ".png";
        try {
            String imagePath;
            if (this.tile.getLeftVal() < 0 || this.tile.getRightVal() < 0)
                imagePath = getClass().getResource("/dominoDown.png").toExternalForm();
            else
                imagePath = getClass().getResource("/dominoTile.png").toExternalForm();
            setImage(new Image(imagePath));
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
        //String imagePath = "sprite/" + left + "_" + right + ".png";
        try {
            String imagePath;
            if (this.tile.getLeftVal() + this.tile.getRightVal() > 12)
                imagePath = getClass().getResource("/dominoDown.png").toExternalForm();
            else
                imagePath = getClass().getResource("/dominoTile.png").toExternalForm();
            setImage(new Image(imagePath));
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

            GameResponse placeRequest = new GameResponse(ResponseType.PLACE_MOVE);
            placeRequest.setTile(tile);

            try {
                // checks if currently my move to do
                // if not, return back in deck
                if (!DominoGame.dominoOn) {
                    table.getChildren().remove(toPlace);
                    table.addPlayerDeck(tile);
                    return;
                }

                dominoServer.sendString(placeRequest.toString());
                System.out.println(placeRequest.toString());
                GameResponse placeResponse = table.getResponse(ResponseType.PLACE_MOVE);

                if (placeResponse.getStatus() == Status.OK) {
                    table.placeTile(toPlace);
                    Tile responseTile = placeResponse.getTile();
                    toPlace.translateDesire(event.getSceneX(), event.getSceneY(), responseTile.getX(),
                    responseTile.getY(), responseTile.getRotateDegree());

                    // actions on success server request
                    table.removeTileFromDeck(toPlace);

                    // turn off animation effects
                    dragTile.disable(toPlace);
                    slideTile.disable(toPlace);
                    setOnMouseReleased(null);

                    DominoGame.dominoOn = false; // end current move
                }
                else { // return tile back in deck
                    table.getChildren().remove(toPlace);
                    table.addPlayerDeck(tile);
                }
            }
            catch (IOException ioExp) {
                System.out.println("Bad server response when placing new tile");
                table.getChildren().remove(toPlace);
                table.addPlayerDeck(tile); // even if server is unreachable -> keep UI consistent
            }
        }
    }

    public void translateDesire(double originX, double originY, double targetX, double targetY, double rotateDegree) {
        setLayoutX(originX);
        setLayoutY(originY);
        moveTile.setDesire(originX, originY, targetX, targetY, rotateDegree);
        moveTile.apply(this);
    }

    public Tile getTile() {
        return this.tile;
    }

    public int getID() { return tileID; }
    public void setID(int newID) { tileID = newID; }

    public void applyUpdate(Update upd) {
       setFitWidth(tile.getWidth() * upd.getResize());
       setFitHeight(tile.getLength() * upd.getResize());

       setTranslateX(upd.getDeltaX());
       setTranslateY(upd.getDeltaY());
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
}
