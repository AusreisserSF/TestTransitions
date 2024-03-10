package sample;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
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
    public static final double APRIL_TAG_SIDE = 12.0;
    public static final double APRIL_TAG_OFFSET_TO_CENTER = APRIL_TAG_SIDE / 2;

    //**TODO Expand to include the serrations on the backdrop, support the
    // placement of a yellow pixel on the backdrop, and then show the
    // movements of the robot and pixel delivery.

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

    protected final GridPane field;
    protected final List<Shape> collidables = new ArrayList<>();

    public FieldFXCenterStageBackdrop(GridPane pField) {
        field = pField;
        initializeField();
    }

    public List<Shape> getFieldCollidables() {
        return collidables;
    }

    private void initializeField() {

        // Put down lines to mark the blue and red backdrops.
        // BLUE backdrop
        Rectangle blueBackdrop = new Rectangle(FieldFX.TILE_DIMENSIONS + 2, 0, FieldFX.TILE_DIMENSIONS - 4, FieldFX.TAPE_THICKNESS);
        blueBackdrop.setId(BLUE_BACKDROP_ID);
        blueBackdrop.setFill(Color.BLACK);
        field.getChildren().add(blueBackdrop);

        // Put down 3 rectangles to mark the BLUE side AprilTags.
        Rectangle aprilTag1Rect = new Rectangle(APRIL_TAG_SIDE, APRIL_TAG_SIDE);
        aprilTag1Rect.setFill(Color.WHITE);
        Text aprilTag1Text = new Text ("1");
        StackPane aprilTag1Stack = new StackPane();
        aprilTag1Stack.setLayoutX(FieldFX.TILE_DIMENSIONS + 10);
        aprilTag1Stack.setLayoutY(6);
        aprilTag1Stack.getChildren().addAll(aprilTag1Rect, aprilTag1Text);
        collidables.add(aprilTag1Rect);
        field.getChildren().add(aprilTag1Stack);

        Rectangle aprilTag2Rect = new Rectangle(APRIL_TAG_SIDE, APRIL_TAG_SIDE);
        aprilTag2Rect.setFill(Color.WHITE);
        Text aprilTag2Text = new Text ("2");
        StackPane aprilTag2Stack = new StackPane();
        aprilTag2Stack.setLayoutX(FieldFX.TILE_DIMENSIONS + (FieldFX.TILE_DIMENSIONS / 2) - 6);
        aprilTag2Stack.setLayoutY(6);
        aprilTag2Stack.getChildren().addAll(aprilTag2Rect, aprilTag2Text);
        collidables.add(aprilTag2Rect);
        field.getChildren().add(aprilTag2Stack);

        Rectangle aprilTag3Rect = new Rectangle(APRIL_TAG_SIDE, APRIL_TAG_SIDE);
        aprilTag3Rect.setFill(Color.WHITE);
        Text aprilTag3Text = new Text ("3");
        StackPane aprilTag3Stack = new StackPane();
        aprilTag3Stack.setLayoutX((FieldFX.TILE_DIMENSIONS * 2) - 22);
        aprilTag3Stack.setLayoutY(6);
        aprilTag3Stack.getChildren().addAll(aprilTag3Rect, aprilTag3Text);
        collidables.add(aprilTag3Rect);
        field.getChildren().add(aprilTag3Stack);

        // RED Backdrop
        Rectangle redBackdrop = new Rectangle((FieldFX.TILE_DIMENSIONS * 4) + 2, 0, FieldFX.TILE_DIMENSIONS - 4, FieldFX.TAPE_THICKNESS);
        redBackdrop.setId(RED_BACKDROP_ID);
        redBackdrop.setFill(Color.BLACK);
        field.getChildren().add(redBackdrop);

        Rectangle aprilTag4Rect = new Rectangle(APRIL_TAG_SIDE, APRIL_TAG_SIDE);
        aprilTag4Rect.setFill(Color.WHITE);
        Text aprilTag4Text = new Text ("4");
        StackPane aprilTag4Stack = new StackPane();
        aprilTag4Stack.setLayoutX((FieldFX.TILE_DIMENSIONS * 4) + 10);
        aprilTag4Stack.setLayoutY(6);
        aprilTag4Stack.getChildren().addAll(aprilTag4Rect, aprilTag4Text);
        collidables.add(aprilTag4Rect);
        field.getChildren().add(aprilTag4Stack);

        Rectangle aprilTag5Rect = new Rectangle(APRIL_TAG_SIDE, APRIL_TAG_SIDE);
        aprilTag5Rect.setFill(Color.WHITE);
        Text aprilTag5Text = new Text ("5");
        StackPane aprilTag5Stack = new StackPane();
        aprilTag5Stack.setLayoutX((FieldFX.TILE_DIMENSIONS * 4) + (FieldFX.TILE_DIMENSIONS / 2) - 6);
        aprilTag5Stack.setLayoutY(6);
        aprilTag5Stack.getChildren().addAll(aprilTag5Rect, aprilTag5Text);
        collidables.add(aprilTag5Rect);
        field.getChildren().add(aprilTag5Stack);

        Rectangle aprilTag6Rect = new Rectangle(APRIL_TAG_SIDE, APRIL_TAG_SIDE);
        aprilTag6Rect.setFill(Color.WHITE);
        Text aprilTag6Text = new Text ("6");
        StackPane aprilTag6Stack = new StackPane();
        aprilTag6Stack.setLayoutX((FieldFX.TILE_DIMENSIONS * 5) - 22);
        aprilTag6Stack.setLayoutY(6);
        aprilTag6Stack.getChildren().addAll(aprilTag6Rect, aprilTag6Text);
        collidables.add(aprilTag6Rect);
        field.getChildren().add(aprilTag6Stack);
    }

}
