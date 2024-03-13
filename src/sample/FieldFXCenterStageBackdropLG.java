package sample;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FieldFXCenterStageBackdropLG {

    //## Note: in JavaFX pixel coordinates are of type double.

    // From the FTC Field Setup Manual: "The field perimeter should measure
    // approximately 141 inches from inside wall to inside wall." The tiles
    // are 24" square but the Field Setup Guide includes a "Critical
    // Mandatory Step: Trim all outer tabs from the 20 soft tiles on the
    // outside edges of the field."
    // But for the simulation, instead of 141" or 3581.4mm per side we'll
    // just use 141.7" per side or 3600mm.
    //**TODO Then scale down to 1/6 for pixels per side.
    // By convention the width is the distance across the wall facing the audience.

    //**TODO For the CenterStage backdrop show a closeup view of 3 x 3 tiles
    public static final double PIXEL_SCALE = 6;
    public static final double VIEW_SCALE = 2;

    //**TODO Why not define all dimensions in terms of PX_PER_INCH or PX_PER_MM?

    public static final double FIELD_DIMENSIONS_IN = 141.7;
    public static final double FIELD_DIMENSIONS_MM = 3600;
    public static final double FIELD_DIMENSIONS_PX = FIELD_DIMENSIONS_MM / PIXEL_SCALE; // pixels
    public static final double PX_PER_INCH = (FIELD_DIMENSIONS_PX / FIELD_DIMENSIONS_IN) * VIEW_SCALE;
    public static final double FIELD_OUTSIDE_BORDER_WIDTH = PX_PER_INCH * 1;
    public static final double TILE_DIMENSIONS = FIELD_DIMENSIONS_PX / (PIXEL_SCALE / VIEW_SCALE);
    public static final double VIEW_WIDTH = TILE_DIMENSIONS * 3; // number of tiles to show horizontally
    public static final double VIEW_HEIGHT = TILE_DIMENSIONS * 3; // nuber of tiles to show vertically

    public static final double TAPE_WIDTH = PX_PER_INCH * 1;
    public static final double BACKDROP_HEIGHT = PX_PER_INCH * 10.75;
    public static final double APRIL_TAG_WIDTH = PX_PER_INCH * 2.5;
    public static final double APRIL_TAG_HEIGHT = PX_PER_INCH * 3.0;
    public static final double APRIL_TAG_OFFSET = APRIL_TAG_HEIGHT + (PX_PER_INCH * 1.0); // from the bottom of the backdrop

    //**TODO These should be in a Group with the backstop ...
    public final double APRIL_TAG_LEFT = PX_PER_INCH * 2.0;
    public final double APRIL_TAG_CENTER = (TILE_DIMENSIONS / 2) - (APRIL_TAG_WIDTH /2);
    public final double APRIL_TAG_RIGHT = TILE_DIMENSIONS - (APRIL_TAG_WIDTH + (PX_PER_INCH * 2.0));
    public static final double BACKSTAGE_BOUNDARY_TO_ANGLE = PX_PER_INCH + (PX_PER_INCH * 10.75);

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

    private final RobotConstants.Alliance alliance;
    private final Pane field;
    private final List<Shape> collidables = new ArrayList<>();

    public FieldFXCenterStageBackdropLG(RobotConstants.Alliance pAlliance,  Pane pField) {
        alliance = pAlliance;
        field = pField;
        initializeField();
    }

    public List<Shape> getFieldCollidables() {
        return collidables;
    }

    private void initializeField() {

        field.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(PX_PER_INCH, PX_PER_INCH, PX_PER_INCH, PX_PER_INCH))));

        // Place horizontal and vertical lines on the field.
        // The lines represent the edges of the interlocking tiles.
        // See jewelsea's answer in https://stackoverflow.com/questions/11881834/what-are-a-lines-exact-dimensions-in-javafx-2
        Line vLine;
        Line hLine;
        for (int i = 1; i < 4; i++) {
            // vertical
            vLine = new Line(TILE_DIMENSIONS * i, PX_PER_INCH,
                    TILE_DIMENSIONS * i, VIEW_HEIGHT - (PX_PER_INCH * 2));
            vLine.setStroke(Color.DIMGRAY);
            vLine.setStrokeWidth(3.0); //**TODO Do not hardcode
            field.getChildren().add(vLine);
        }

        // Center Stage: partial field: three tiles
        for (int i = 1; i < 3; i++) {
            // horizontal
            hLine = new Line(PX_PER_INCH, (TILE_DIMENSIONS * i) + PX_PER_INCH, VIEW_WIDTH - (PX_PER_INCH * 2), (TILE_DIMENSIONS * i) + PX_PER_INCH);
            hLine.setStroke(Color.DIMGRAY);
            hLine.setStrokeWidth(3.0);
            field.getChildren().add(hLine);
        }

        // Put down lines to mark the blue and red backdrops.
        // BLUE backdrop
        if (alliance == RobotConstants.Alliance.BLUE) {
            Rectangle blueBackdrop = new Rectangle(TILE_DIMENSIONS, 0, TILE_DIMENSIONS, BACKDROP_HEIGHT);
            blueBackdrop.setId(BLUE_BACKDROP_ID);
            blueBackdrop.setFill(Color.BLACK);
            field.getChildren().add(blueBackdrop);

            // Put down 3 rectangles to mark the BLUE side AprilTags.
            Rectangle aprilTag1Rect = new Rectangle(APRIL_TAG_WIDTH, APRIL_TAG_HEIGHT);
            aprilTag1Rect.setFill(Color.WHITE);
            Text aprilTag1Text = new Text("1");
            StackPane aprilTag1Stack = new StackPane();
            aprilTag1Stack.setLayoutX(TILE_DIMENSIONS + APRIL_TAG_LEFT);
            aprilTag1Stack.setLayoutY(BACKDROP_HEIGHT - APRIL_TAG_OFFSET);
            aprilTag1Stack.getChildren().addAll(aprilTag1Rect, aprilTag1Text);
            collidables.add(aprilTag1Rect);
            field.getChildren().add(aprilTag1Stack);

            Rectangle aprilTag2Rect = new Rectangle(APRIL_TAG_WIDTH, APRIL_TAG_HEIGHT);
            aprilTag2Rect.setFill(Color.WHITE);
            Text aprilTag2Text = new Text("2");
            StackPane aprilTag2Stack = new StackPane();
            aprilTag2Stack.setLayoutX(TILE_DIMENSIONS + APRIL_TAG_CENTER);
            aprilTag2Stack.setLayoutY(BACKDROP_HEIGHT - APRIL_TAG_OFFSET);
            aprilTag2Stack.getChildren().addAll(aprilTag2Rect, aprilTag2Text);
            collidables.add(aprilTag2Rect);
            field.getChildren().add(aprilTag2Stack);

            Rectangle aprilTag3Rect = new Rectangle(APRIL_TAG_WIDTH, APRIL_TAG_HEIGHT);
            aprilTag3Rect.setFill(Color.WHITE);
            Text aprilTag3Text = new Text("3");
            StackPane aprilTag3Stack = new StackPane();
            aprilTag3Stack.setLayoutX(TILE_DIMENSIONS + APRIL_TAG_RIGHT);
            aprilTag3Stack.setLayoutY(BACKDROP_HEIGHT - APRIL_TAG_OFFSET);
            aprilTag3Stack.getChildren().addAll(aprilTag3Rect, aprilTag3Text);
            collidables.add(aprilTag3Rect);
            field.getChildren().add(aprilTag3Stack);

            // Place the Backstage tape lines according to the field assembly guide.
            Line backstageBoundaryBlue = new Line(0, TILE_DIMENSIONS - TAPE_WIDTH,
                    TILE_DIMENSIONS * 2 + BACKSTAGE_BOUNDARY_TO_ANGLE, TILE_DIMENSIONS - TAPE_WIDTH);
            backstageBoundaryBlue.setStroke(Color.BLUE);
            backstageBoundaryBlue.setStrokeWidth(TAPE_WIDTH);
            backstageBoundaryBlue.setStrokeLineJoin(StrokeLineJoin.MITER);
            field.getChildren().add(backstageBoundaryBlue);

            Line backstageAngledLineBlue = new Line(TILE_DIMENSIONS * 2 + BACKSTAGE_BOUNDARY_TO_ANGLE,
                    TILE_DIMENSIONS - TAPE_WIDTH, TILE_DIMENSIONS * 3, 0);
            backstageAngledLineBlue.setStroke(Color.BLUE);
            backstageAngledLineBlue.setStrokeWidth(TAPE_WIDTH);
            backstageAngledLineBlue.setStrokeLineJoin(StrokeLineJoin.MITER);
            field.getChildren().add(backstageAngledLineBlue);
        }

        // RED Backdrop
        if (alliance == RobotConstants.Alliance.RED) {
        Rectangle redBackdrop = new Rectangle(TILE_DIMENSIONS, 0, TILE_DIMENSIONS, BACKDROP_HEIGHT);
        redBackdrop.setId(RED_BACKDROP_ID);
        redBackdrop.setFill(Color.BLACK);
        field.getChildren().add(redBackdrop);

        Rectangle aprilTag4Rect = new Rectangle(APRIL_TAG_WIDTH, APRIL_TAG_HEIGHT);
        aprilTag4Rect.setFill(Color.WHITE);
        Text aprilTag4Text = new Text ("4");
        StackPane aprilTag4Stack = new StackPane();
        aprilTag4Stack.setLayoutX(TILE_DIMENSIONS + APRIL_TAG_LEFT);
        aprilTag4Stack.setLayoutY(BACKDROP_HEIGHT - APRIL_TAG_OFFSET);
        aprilTag4Stack.getChildren().addAll(aprilTag4Rect, aprilTag4Text);
        collidables.add(aprilTag4Rect);
        field.getChildren().add(aprilTag4Stack);

        Rectangle aprilTag5Rect = new Rectangle(APRIL_TAG_WIDTH, APRIL_TAG_HEIGHT);
        aprilTag5Rect.setFill(Color.WHITE);
        Text aprilTag5Text = new Text ("5");
        StackPane aprilTag5Stack = new StackPane();
        aprilTag5Stack.setLayoutX(TILE_DIMENSIONS + APRIL_TAG_CENTER);
        aprilTag5Stack.setLayoutY(BACKDROP_HEIGHT - APRIL_TAG_OFFSET);
        aprilTag5Stack.getChildren().addAll(aprilTag5Rect, aprilTag5Text);
        collidables.add(aprilTag5Rect);
        field.getChildren().add(aprilTag5Stack);

        Rectangle aprilTag6Rect = new Rectangle(APRIL_TAG_WIDTH, APRIL_TAG_HEIGHT);
        aprilTag6Rect.setFill(Color.WHITE);
        Text aprilTag6Text = new Text ("6");
        StackPane aprilTag6Stack = new StackPane();
        aprilTag6Stack.setLayoutX(TILE_DIMENSIONS + APRIL_TAG_RIGHT);
        aprilTag6Stack.setLayoutY(BACKDROP_HEIGHT - APRIL_TAG_OFFSET);
        aprilTag6Stack.getChildren().addAll(aprilTag6Rect, aprilTag6Text);
        collidables.add(aprilTag6Rect);
        field.getChildren().add(aprilTag6Stack);

        Line backstageAngledLineRed = new Line(0,
                0, TILE_DIMENSIONS - BACKSTAGE_BOUNDARY_TO_ANGLE, TILE_DIMENSIONS - TAPE_WIDTH);
        backstageAngledLineRed.setStroke(Color.RED);
        backstageAngledLineRed.setStrokeWidth(TAPE_WIDTH);
        backstageAngledLineRed.setStrokeLineJoin(StrokeLineJoin.MITER);
        field.getChildren().add(backstageAngledLineRed);

        Line backstageBoundaryRed = new Line(TILE_DIMENSIONS - BACKSTAGE_BOUNDARY_TO_ANGLE, TILE_DIMENSIONS - TAPE_WIDTH,
                TILE_DIMENSIONS * 2, TILE_DIMENSIONS - TAPE_WIDTH);
        backstageBoundaryRed.setStroke(Color.RED);
        backstageBoundaryRed.setStrokeWidth(TAPE_WIDTH);
        backstageBoundaryRed.setStrokeLineJoin(StrokeLineJoin.MITER);
        field.getChildren().add(backstageBoundaryRed);
        }
    }

}
