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

    public SpriteTile(Tile tile) {
        this.tile = tile;

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

        // temperary DURTY hack to destinguish player and enemies tiles
        if (this.tile.getLeftVal() + this.tile.getRightVal() >= 0) {
            slideTile.apply(this);
            dragTile.apply(this);

            setOnMouseReleased(placeMove);
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
                    table.addTileInDeck(tile);
                    return;
                }

                dominoServer.sendString(placeRequest.toString());
                System.out.println(placeRequest.toString());
                GameResponse placeResponse = table.getResponse(ResponseType.PLACE_MOVE);

                if (placeResponse.getStatus() == Status.OK) {
                    table.placeTile(toPlace);

                    Tile responseTile = placeResponse.getTile();
                    // actions on success server request
                    dragTile.disable(toPlace);
                    slideTile.disable(toPlace);
                    moveTile.setDesire(event.getSceneX(), event.getSceneY(), responseTile.getX(),
                    responseTile.getY(), responseTile.getRotateDegree());
                    moveTile.apply(toPlace);
                    setOnMouseReleased(null);

                    table.removeTileFromDeck(toPlace);
                    DominoGame.dominoOn = false; // end current move
                }
                else { // return tile back in deck
                    table.getChildren().remove(toPlace);
                    table.addTileInDeck(tile);
                }
            }
            catch (IOException ioExp) {
                System.out.println("Bad server response when placing new tile");
                table.getChildren().remove(toPlace);
                table.addTileInDeck(tile); // even if server is unreachable -> keep UI consistent
            }
        }
    }

    public Tile getTile() {
        return this.tile;
    }

    public void applyUpdate(Update upd) {
       setFitWidth(tile.getWidth() * upd.getResize());
       setFitHeight(tile.getLength() * upd.getResize());

       setTranslateX(upd.getDeltaX());
       setTranslateY(upd.getDeltaY());
    }
}
