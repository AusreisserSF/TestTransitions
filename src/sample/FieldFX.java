package sample;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class FieldFX {

    //## Note: in JavaFX pixel coordinates are of type double.

    // From the FTC Field Setup Manual: "The field perimeter should measure
    // approximately 141 inches from inside wall to inside wall." The tiles
    // are 24" square but the Field Setup Guide includes a "Critical
    // Mandatory Step: Trim all outer tabs from the 20 soft tiles on the
    // outside edges of the field."
    // But for the simulation, instead of 141" or 3581.4mm per side we'll
    // just use 141.7" per side or 3600mm. Then scale down to 1/6 for
    // pixels per side.
    // By convention the width is the distance across the wall facing the audience.
    public static final double MM_PER_INCH = 25.4;
    public static final double TAPE_THICKNESS = Math.floor((2 * MM_PER_INCH) / 6);
    public static final double FIELD_DIMENSIONS_MM = 3600;
    public static final double FIELD_WIDTH = FIELD_DIMENSIONS_MM / 6;
    public static final double TILE_DIMENSIONS = FIELD_WIDTH / 6;
    public static final double FIELD_HEIGHT = FIELD_DIMENSIONS_MM / 6;

    public static final String THUMBS_DOWN_IMAGE = "/resources/Thumbs down (Microsoft) 30x30.png";

    protected final Pane field;
    protected final List<Shape> collidables = new ArrayList<>();

    public FieldFX() {
        field = new Pane();
        initializeField();
    }

    public Pane getField() {
        return field;
    }

    public List<Shape> getCollidables() {
        return collidables;
    }

    private void initializeField() {

        field.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        // Place invisible lines around the outside borders of the field.
        // These will be used for collision detection.
        Line northBoundary = new Line(0, 0, FIELD_WIDTH, 0);
        northBoundary.setStrokeWidth(2.0);
        northBoundary.setVisible(false);
        field.getChildren().add(northBoundary);
        collidables.add(northBoundary);

        Line southBoundary = new Line(0, FIELD_HEIGHT, FIELD_WIDTH, FIELD_HEIGHT);
        southBoundary.setStrokeWidth(2.0);
        southBoundary.setVisible(false);
        field.getChildren().add(southBoundary);
        collidables.add(southBoundary);

        Line westBoundary = new Line(0, 0, 0, FIELD_HEIGHT);
        westBoundary.setStrokeWidth(2.0);
        westBoundary.setVisible(false);
        field.getChildren().add(westBoundary);
        collidables.add(westBoundary);

        Line eastBoundary = new Line(FIELD_WIDTH, 0, FIELD_WIDTH, FIELD_HEIGHT);
        eastBoundary.setStrokeWidth(2.0);
        eastBoundary.setVisible(false);
        field.getChildren().add(eastBoundary);
        collidables.add(eastBoundary);

        // Place horizontal and vertical lines on the field.
        // The lines represent the edges of the interlocking tiles.
        // See jewelsea's answer in https://stackoverflow.com/questions/11881834/what-are-a-lines-exact-dimensions-in-javafx-2
        Line vLine;
        Line hLine;
        for (int i = 1; i < 6; i++) {
            // vertical
            vLine = new Line(TILE_DIMENSIONS * i, 0, TILE_DIMENSIONS * i, FIELD_HEIGHT - 2);
            vLine.setStroke(Color.DIMGRAY);
            vLine.setStrokeWidth(2.0);
            field.getChildren().add(vLine);

            // horizontal
            hLine = new Line(0, TILE_DIMENSIONS * i, FIELD_WIDTH - 3, TILE_DIMENSIONS * i);
            hLine.setStroke(Color.DIMGRAY);
            hLine.setStrokeWidth(3.0);
            field.getChildren().add(hLine);
        }
    }
}
