package sample;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FieldFXCenterStageBackdrop {

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
    public static final double FIELD_DIMENSIONS_MM = 3600;
    public static final double FIELD_WIDTH = FIELD_DIMENSIONS_MM / 6;
    public static final double TILE_DIMENSIONS = FIELD_WIDTH / 6;
    public static final double FIELD_HEIGHT = FIELD_DIMENSIONS_MM / 12; // partial field

    // Identifiers for field objects.
    // Identifiers are used during animation to get a specific object via Pane.lookup().
    // Reference: https://stackoverflow.com/questions/34861690/javafx-scene-lookup-returning-null
    public static final String BLUE_BACKDROP_ID = "blueBackdropId";
    public static final String APRIL_TAG_1_ID = "aprilTag1Id";
    public static final String APRIL_TAG_2_ID = "aprilTag2Id";
    public static final String APRIL_TAG_3_ID = "aprilTag3Id";
    public static final String RED_BACKDROP_ID = "redBackdropId";
    public static final String APRIL_TAG_4_ID = "aprilTag4Id";
    public static final String APRIL_TAG_5_ID = "aprilTag5Id";
    public static final String APRIL_TAG_6_ID = "aprilTag6Id";

    protected final Pane field;
    protected final List<Shape> collidables = new ArrayList<>();

    public FieldFXCenterStageBackdrop() {
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
        }

        // Center Stage: partial field: three tiles
        for (int i = 1; i < 3; i++) {
            // horizontal
            hLine = new Line(0, TILE_DIMENSIONS * i, FIELD_WIDTH - 3, TILE_DIMENSIONS * i);
            hLine.setStroke(Color.DIMGRAY);
            hLine.setStrokeWidth(3.0);
            field.getChildren().add(hLine);
        }

        //**TODO Place the two backdrops and the six AprilTags with labels.

        // Put down lines to mark the blue and red backdrops.
        // BLUE backdrop
        Rectangle blueBackdrop = new Rectangle(FieldFX.TILE_DIMENSIONS + 2, 0, FieldFX.TILE_DIMENSIONS - 4, FieldFX.TAPE_THICKNESS);
        blueBackdrop.setId(BLUE_BACKDROP_ID);
        blueBackdrop.setFill(Color.BLACK);
        field.getChildren().add(blueBackdrop);

        //**TODO Change triangles to rectangles
        // Put down 3 rectangles to mark the BLUE side AprilTags.
        Rectangle aprilTag1Rect = new Rectangle(FieldFX.TILE_DIMENSIONS, 0, 10, 10);
        aprilTag1Rect.setFill(Color.WHITE);
        //Text aprilTag1Text = new Text ("1");
        //StackPane aprilTag1Stack = new StackPane();
        //aprilTag1Stack.getChildren().addAll(aprilTag1Rect, aprilTag1Text);
        field.getChildren().add(aprilTag1Rect);

        Polygon aprilTag1 = new Polygon();
        aprilTag1.setId(APRIL_TAG_1_ID);
        aprilTag1.getPoints().addAll((FieldFX.TILE_DIMENSIONS * 2) + 20, 0.0,
                (FieldFX.TILE_DIMENSIONS * 2) + 30.0, 0.0,
                (FieldFX.TILE_DIMENSIONS * 2) + 25.0, 10.0);
        field.getChildren().add(aprilTag1);

        Polygon aprilTag2 = new Polygon();
        aprilTag2.setId(APRIL_TAG_2_ID);
        aprilTag2.getPoints().addAll((FieldFX.TILE_DIMENSIONS * 2) + 50, 0.0,
                (FieldFX.TILE_DIMENSIONS * 2) + 60.0, 0.0,
                (FieldFX.TILE_DIMENSIONS * 2) + 55.0, 10.0);
        field.getChildren().add(aprilTag2);

        Polygon aprilTag3 = new Polygon();
        aprilTag3.setId(APRIL_TAG_3_ID);
        aprilTag3.getPoints().addAll((FieldFX.TILE_DIMENSIONS * 2) + 80, 0.0,
                (FieldFX.TILE_DIMENSIONS * 2) + 90.0, 0.0,
                (FieldFX.TILE_DIMENSIONS * 2) + 85.0, 10.0);
        field.getChildren().add(aprilTag3);

        // RED Backdrop
        Rectangle redBackdrop = new Rectangle((FieldFX.TILE_DIMENSIONS * 4) + 2, 0, FieldFX.TILE_DIMENSIONS - 4, FieldFX.TAPE_THICKNESS);
        redBackdrop.setId(RED_BACKDROP_ID);
        redBackdrop.setFill(Color.BLACK);
        field.getChildren().add(redBackdrop);

        // Put down 3 triangles to mark the RED side AprilTags.
        Polygon aprilTag4 = new Polygon();
        aprilTag4.setId(APRIL_TAG_4_ID);
        aprilTag4.getPoints().addAll((FieldFX.TILE_DIMENSIONS * 3) + 10, 0.0,
                (FieldFX.TILE_DIMENSIONS * 3) + 20.0, 0.0,
                (FieldFX.TILE_DIMENSIONS * 3) + 15.0, 10.0);
        field.getChildren().add(aprilTag4);

        Polygon aprilTag5 = new Polygon();
        aprilTag5.setId(APRIL_TAG_5_ID);
        aprilTag5.getPoints().addAll((FieldFX.TILE_DIMENSIONS * 3) + 40, 0.0,
                (FieldFX.TILE_DIMENSIONS * 3) + 50.0, 0.0,
                (FieldFX.TILE_DIMENSIONS * 3) + 45.0, 10.0);
        field.getChildren().add(aprilTag5);

        Polygon aprilTag6 = new Polygon();
        aprilTag6.setId(APRIL_TAG_6_ID);
        aprilTag6.getPoints().addAll((FieldFX.TILE_DIMENSIONS * 3) + 70, 0.0,
                (FieldFX.TILE_DIMENSIONS * 3) + 80.0, 0.0,
                (FieldFX.TILE_DIMENSIONS * 3) + 75.0, 10.0);
        field.getChildren().add(aprilTag6);

        //**TODO TEST text in rectangle (AprilTag)
        final Rectangle aprilTag = new Rectangle(100, 150, 10, 10);
        aprilTag.setFill(Color.WHITE);
        final Text text = new Text ("1");
        final StackPane stack = new StackPane();
        stack.getChildren().addAll(aprilTag, text);
        field.getChildren().add(stack);

        //stack.setLayoutX(30);
        //stack.setLayoutY(30);
    }

}
