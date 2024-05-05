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

public class DragPreviewRobot extends Application {

    // list of nodes that are dragged. Can be modified at any time (on the FX Application Thread):
    private final List<Node> nodesToDrag = new ArrayList<>();

    private Rectangle previewRobot;
    private Line leftHalfFOV;
    private Line rightHalfFOV;

    @Override
    public void start( Stage primaryStage ) throws Exception {

        previewRobot = new Rectangle(200, 200, 100, 100);
        leftHalfFOV = new Line(250, 200, 150, 100);
        rightHalfFOV = new Line(250, 200, 350, 100);

        makeDraggable(previewRobot);
        makeDraggable(leftHalfFOV);    
        makeDraggable(rightHalfFOV);

        Group root = new Group();
        root.getChildren().addAll( previewRobot, leftHalfFOV, rightHalfFOV );
        primaryStage.setResizable( false );
        primaryStage.setScene( new Scene( root, 400, 400 ) );
        primaryStage.setTitle( DragPreviewRobot.class.getSimpleName() );
        primaryStage.show();
    }

    private void makeDraggable( Node pNode ) {
        MouseLocation lastMouseLocation = new MouseLocation();

        // --- remember initial coordinates of mouse cursor and node
        pNode.addEventFilter( MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent ) -> {
            lastMouseLocation.x = mouseEvent.getSceneX() ;
            lastMouseLocation.y = mouseEvent.getSceneY() ;
            nodesToDrag.add(pNode);            
        } );

        // --- Shift node calculated from mouse cursor movement
        pNode.addEventFilter( MouseEvent.MOUSE_DRAGGED, (
                final MouseEvent mouseEvent ) -> {
                    double deltaX = mouseEvent.getSceneX() - lastMouseLocation.x ;
                    double deltaY = mouseEvent.getSceneY() - lastMouseLocation.y ;

                    for (Node n : nodesToDrag) {
                        n.setTranslateX( n.getTranslateX() + deltaX  );
                        n.setTranslateX( n.getTranslateY() + deltaY );
                    }
                    
                    lastMouseLocation.x = mouseEvent.getSceneX();
                    lastMouseLocation.y = mouseEvent.getSceneY();
        } );

        pNode.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEvent -> nodesToDrag.clear());
    }

    private static final class MouseLocation {
        public double x, y;
    }
}