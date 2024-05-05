package sample;
// https://stackoverflow.com/questions/28566860/javafx-how-to-group-shapes-for-dragging
// Answer from James D.

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

// Start with the drag-and-release code from here:
// http://java-buddy.blogspot.com/2013/07/javafx-drag-and-move-something.html#google_vignette
// but use Node instead of Circle.
public class DragPreviewRobot extends Application {

    // list of nodes that are dragged. Can be modified at any time (on the FX Application Thread):
    private final List<Node> nodesToDrag = new ArrayList<>();

    private Rectangle previewRobot;
    private Line leftHalfFOV;
    private Line rightHalfFOV;
    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    @Override
    public void start( Stage primaryStage ) throws Exception {

        previewRobot = new Rectangle(200, 200, 100, 100);
        leftHalfFOV = new Line(250, 200, 150, 100);
        rightHalfFOV = new Line(250, 200, 350, 100);

        makeDraggable(previewRobot);
        //makeDraggable(leftHalfFOV);
        //makeDraggable(rightHalfFOV);

        Group root = new Group();
        root.getChildren().addAll( previewRobot, leftHalfFOV, rightHalfFOV );
        primaryStage.setResizable( false );
        primaryStage.setScene( new Scene( root, 400, 400 ) );
        primaryStage.setTitle( DragPreviewRobot.class.getSimpleName() );
        primaryStage.show();
    }

    private void makeDraggable( Node pNode ) { //**TODO Node -> Rectangle

        // --- remember initial coordinates of mouse cursor and node
        pNode.addEventFilter( MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent ) -> {
            orgSceneX = mouseEvent.getSceneX();
            orgSceneY = mouseEvent.getSceneY();

            //**TODO These are different for each Shape ... generalize by
            // putting into an EnumMap??
            orgTranslateX = ((Node)(mouseEvent.getSource())).getTranslateX();
            orgTranslateY = ((Node)(mouseEvent.getSource())).getTranslateY();
            //nodesToDrag.add(previewRobot);
        } );

        // --- Shift node calculated from mouse cursor movement
        pNode.addEventFilter( MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent ) -> {
            double offsetX = mouseEvent.getSceneX() - orgSceneX;
            double offsetY = mouseEvent.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;

            ((Node)(mouseEvent.getSource())).setTranslateX(newTranslateX);
            ((Node)(mouseEvent.getSource())).setTranslateY(newTranslateY);
        } );

       // pNode.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEvent -> nodesToDrag.clear());
    }

}