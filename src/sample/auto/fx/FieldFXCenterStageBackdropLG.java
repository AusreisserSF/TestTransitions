package sample.auto.fx;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import sample.auto.RobotConstants;

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
    // just use 141.7" per side or 3600mm. Then scale down to 1/6 for pixels
    // per side, i.e. 600, as defined in the fxml. For the CenterStage backdrop
    // show a closeup view of 3 x 3 double-sized tiles.
    public static final double PIXEL_SCALE = 6; // 6 tiles in 600 pixels
    public static final double VIEW_SCALE = 2; // We'll use double-scale or 200px/tile

    // By convention the width is the distance across the wall facing the audience.
    public static final double FIELD_DIMENSIONS_IN = 141.7;
    public static final double FIELD_DIMENSIONS_MM = 3600;
    public static final double FIELD_DIMENSIONS_PX = FIELD_DIMENSIONS_MM / PIXEL_SCALE; // pixels
    public static final double PX_PER_INCH = (FIELD_DIMENSIONS_PX / FIELD_DIMENSIONS_IN) * VIEW_SCALE;
    public static final double FIELD_OUTSIDE_BORDER_SIZE = PX_PER_INCH * 1;
    public static final double TILE_DIMENSIONS = FIELD_DIMENSIONS_PX / (PIXEL_SCALE / VIEW_SCALE);
    public static final double TILE_BOUNDARY_WIDTH = 3.0;
    public static final double VIEW_WIDTH = TILE_DIMENSIONS * 3; // number of tiles to show horizontally
    public static final double VIEW_HEIGHT = TILE_DIMENSIONS * 3; // number of tiles to show vertically

    public static final double TAPE_WIDTH = PX_PER_INCH * 1; // looks better at 1"
    public static final double BACKDROP_HEIGHT = PX_PER_INCH * 10.75;
    public static final double APRIL_TAG_WIDTH = PX_PER_INCH * 2.5;
    public static final double APRIL_TAG_HEIGHT = PX_PER_INCH * 3.0;
    public static final double APRIL_TAG_OFFSET = APRIL_TAG_HEIGHT + (PX_PER_INCH * 1.0); // from the bottom of the backdrop

    public final double APRIL_TAG_LEFT = PX_PER_INCH * 2.0;
    public final double APRIL_TAG_CENTER = (TILE_DIMENSIONS / 2) - (APRIL_TAG_WIDTH / 2);
    public final double APRIL_TAG_RIGHT = TILE_DIMENSIONS - (APRIL_TAG_WIDTH + (PX_PER_INCH * 2.0));
    public static final double BACKSTAGE_BOUNDARY_TO_ANGLE = PX_PER_INCH + (PX_PER_INCH * 10.75);

    // Identifiers for field objects.
    // Identifiers are used during animation to get a specific object via Pane.lookup().
    // Reference: https://stackoverflow.com/questions/34861690/javafx-scene-lookup-returning-null
    public static final String BACKDROP_ID = "backdropId";
    public static final String APRIL_TAG_ID = "aprilTagId";
    public static final String APRIL_TAG_1_ID = "aprilTagId1";
    public static final String APRIL_TAG_2_ID = "aprilTagId2";
    public static final String APRIL_TAG_3_ID = "aprilTagId3";
    public static final String APRIL_TAG_4_ID = "aprilTagId4";
    public static final String APRIL_TAG_5_ID = "aprilTagId5";
    public static final String APRIL_TAG_6_ID = "aprilTagId6";

    private final RobotConstants.Alliance alliance;
    private final Pane field;
    private final List<Shape> collidables = new ArrayList<>();

    public FieldFXCenterStageBackdropLG(RobotConstants.Alliance pAlliance, Pane pField) {
        alliance = pAlliance;
        field = pField;
        initializeField();
    }

    public List<Shape> getFieldCollidables() {
        return collidables;
    }

    private void initializeField() {
        // Place horizontal and vertical lines on the field.
        // The lines represent the edges of the interlocking tiles.
        // See jewelsea's answer in https://stackoverflow.com/questions/11881834/what-are-a-lines-exact-dimensions-in-javafx-2
        Line vLine;
        Line hLine;
        for (int i = 1; i < 4; i++) {
            // vertical
            vLine = new Line(TILE_DIMENSIONS * i, 0, //FIELD_OUTSIDE_BORDER_SIZE,
                    TILE_DIMENSIONS * i, VIEW_HEIGHT); // - (FIELD_OUTSIDE_BORDER_SIZE * 2));
            vLine.setStroke(Color.DIMGRAY);
            vLine.setStrokeWidth(TILE_BOUNDARY_WIDTH);
            field.getChildren().add(vLine);
        }

        // Center Stage: partial field: three tiles
        for (int i = 1; i < 3; i++) {
            // horizontal
            //hLine = new Line(FIELD_OUTSIDE_BORDER_SIZE, (TILE_DIMENSIONS * i) + FIELD_OUTSIDE_BORDER_SIZE, VIEW_WIDTH - (FIELD_OUTSIDE_BORDER_SIZE * 2), (TILE_DIMENSIONS * i) + FIELD_OUTSIDE_BORDER_SIZE);
            hLine = new Line(0, TILE_DIMENSIONS * i, VIEW_WIDTH, TILE_DIMENSIONS * i);
            hLine.setStroke(Color.DIMGRAY);
            hLine.setStrokeWidth(3.0);
            field.getChildren().add(hLine);
        }

        // Draw the parts of the field, the backdrop and the AprilTag outlines,
        // that are common to both alliances.
        Rectangle backdrop = new Rectangle(TILE_DIMENSIONS, 0, TILE_DIMENSIONS, BACKDROP_HEIGHT);
        backdrop.setId(BACKDROP_ID);
        backdrop.setFill(Color.BLACK);
        field.getChildren().add(backdrop);

        // Put down 3 rectangles for the AprilTags.
        Rectangle aprilTagLeftRect = new Rectangle(APRIL_TAG_WIDTH, APRIL_TAG_HEIGHT);
        aprilTagLeftRect.setFill(Color.WHITE);

        StackPane aprilTagLeftStack = new StackPane();
        aprilTagLeftStack.setLayoutX(TILE_DIMENSIONS + APRIL_TAG_LEFT);
        aprilTagLeftStack.setLayoutY(BACKDROP_HEIGHT - APRIL_TAG_OFFSET);
        aprilTagLeftStack.getChildren().add(aprilTagLeftRect);
        collidables.add(aprilTagLeftRect);
        field.getChildren().add(aprilTagLeftStack);

        Rectangle aprilTagCenterRect = new Rectangle(APRIL_TAG_WIDTH, APRIL_TAG_HEIGHT);
        aprilTagCenterRect.setFill(Color.WHITE);
        StackPane aprilTagCenterStack = new StackPane();
        aprilTagCenterStack.setLayoutX(TILE_DIMENSIONS + APRIL_TAG_CENTER);
        aprilTagCenterStack.setLayoutY(BACKDROP_HEIGHT - APRIL_TAG_OFFSET);
        aprilTagCenterStack.getChildren().addAll(aprilTagCenterRect);
        collidables.add(aprilTagCenterRect);
        field.getChildren().add(aprilTagCenterStack);

        Rectangle aprilTagRightRect = new Rectangle(APRIL_TAG_WIDTH, APRIL_TAG_HEIGHT);
        aprilTagRightRect.setFill(Color.WHITE);
        StackPane aprilTagRightStack = new StackPane();
        aprilTagRightStack.setLayoutX(TILE_DIMENSIONS + APRIL_TAG_RIGHT);
        aprilTagRightStack.setLayoutY(BACKDROP_HEIGHT - APRIL_TAG_OFFSET);
        aprilTagRightStack.getChildren().addAll(aprilTagRightRect);
        collidables.add(aprilTagRightRect);
        field.getChildren().add(aprilTagRightStack);

        // Put down lines to mark the blue and red backdrops.
        // BLUE backdrop
        if (alliance == RobotConstants.Alliance.BLUE) {
            aprilTagLeftRect.setId(APRIL_TAG_1_ID);
            aprilTagLeftStack.getChildren().add(new Text("1"));
            aprilTagCenterRect.setId(APRIL_TAG_2_ID);
            aprilTagCenterStack.getChildren().add(new Text("2"));
            aprilTagRightRect.setId(APRIL_TAG_3_ID);
            aprilTagRightStack.getChildren().add(new Text("3"));

            // Place the Backstage tape lines according to the field assembly guide.
            Line backstageBoundaryBlue = new Line(FIELD_OUTSIDE_BORDER_SIZE, TILE_DIMENSIONS - TAPE_WIDTH,
                    FIELD_OUTSIDE_BORDER_SIZE + (TILE_DIMENSIONS * 2) + BACKSTAGE_BOUNDARY_TO_ANGLE, TILE_DIMENSIONS - TAPE_WIDTH);
            backstageBoundaryBlue.setStroke(Color.BLUE);
            backstageBoundaryBlue.setStrokeWidth(TAPE_WIDTH);
            backstageBoundaryBlue.setStrokeLineJoin(StrokeLineJoin.MITER);
            field.getChildren().add(backstageBoundaryBlue);

            Line backstageAngledLineBlue = new Line(FIELD_OUTSIDE_BORDER_SIZE + (TILE_DIMENSIONS * 2) + BACKSTAGE_BOUNDARY_TO_ANGLE,
                    TILE_DIMENSIONS - TAPE_WIDTH, (TILE_DIMENSIONS * 3) - FIELD_OUTSIDE_BORDER_SIZE, FIELD_OUTSIDE_BORDER_SIZE);
            backstageAngledLineBlue.setStroke(Color.BLUE);
            backstageAngledLineBlue.setStrokeWidth(TAPE_WIDTH);
            backstageAngledLineBlue.setStrokeLineJoin(StrokeLineJoin.MITER);
            field.getChildren().add(backstageAngledLineBlue);
        }

        // RED Backdrop
        if (alliance == RobotConstants.Alliance.RED) {
            aprilTagLeftRect.setId(APRIL_TAG_4_ID);
            aprilTagLeftStack.getChildren().add(new Text("4"));
            aprilTagCenterRect.setId(APRIL_TAG_5_ID);
            aprilTagCenterStack.getChildren().add(new Text("5"));
            aprilTagRightRect.setId(APRIL_TAG_6_ID);
            aprilTagRightStack.getChildren().add(new Text("6"));

            Line backstageAngledLineRed = new Line(FIELD_OUTSIDE_BORDER_SIZE,
                    FIELD_OUTSIDE_BORDER_SIZE, TILE_DIMENSIONS - BACKSTAGE_BOUNDARY_TO_ANGLE, TILE_DIMENSIONS - TAPE_WIDTH);
            backstageAngledLineRed.setStroke(Color.RED);
            backstageAngledLineRed.setStrokeWidth(TAPE_WIDTH);
            backstageAngledLineRed.setStrokeLineJoin(StrokeLineJoin.MITER);
            field.getChildren().add(backstageAngledLineRed);

            Line backstageBoundaryRed = new Line(TILE_DIMENSIONS - BACKSTAGE_BOUNDARY_TO_ANGLE, TILE_DIMENSIONS - TAPE_WIDTH,
                    (TILE_DIMENSIONS * 3) - FIELD_OUTSIDE_BORDER_SIZE, TILE_DIMENSIONS - TAPE_WIDTH);
            backstageBoundaryRed.setStroke(Color.RED);
            backstageBoundaryRed.setStrokeWidth(TAPE_WIDTH);
            backstageBoundaryRed.setStrokeLineJoin(StrokeLineJoin.MITER);
            field.getChildren().add(backstageBoundaryRed);
        }

        // Draw the border on top of the field.
        Rectangle border = new Rectangle(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        border.setStroke(Color.BLACK);
        border.setStrokeWidth(FIELD_OUTSIDE_BORDER_SIZE);
        border.setFill(Color.TRANSPARENT);
        field.getChildren().add(border);
    }

}
