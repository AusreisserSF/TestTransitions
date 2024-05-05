package sample;
// https://stackoverflow.com/questions/28566860/javafx-how-to-group-shapes-for-dragging
// Answer from James D.

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class DragGroupCircles extends Application {

    // list of nodes that are dragged. Can be modified at any time (on the FX Application Thread):
    private final List<Circle> nodesToDrag = new ArrayList<>();

    private final Circle[] circles = new Circle[3] ;

    @Override
    public void start( Stage primaryStage ) throws Exception {

        circles[0] = new Circle( 30.0, 30.0, 30.0, Color.RED );
        circles[1] = new Circle( 45.0, 45.0, 30.0, Color.GREEN );
        circles[2] = new Circle( 60.0, 60.0, 30.0, Color.BLUE );

        for ( Circle circle : circles ) {
            makeDraggable( circle );
        }

        Group root = new Group();
        root.getChildren().addAll( circles[0], circles[1], circles[2] );
        primaryStage.setResizable( false );
        primaryStage.setScene( new Scene( root, 400, 350 ) );
        primaryStage.setTitle( DragGroupCircles.class.getSimpleName() );
        primaryStage.show();
    }

    private void makeDraggable( Circle circle ) {
        MouseLocation lastMouseLocation = new MouseLocation();

        // --- remember initial coordinates of mouse cursor and node
        circle.addEventFilter( MouseEvent.MOUSE_PRESSED, (
                final MouseEvent mouseEvent ) -> {
            lastMouseLocation.x = mouseEvent.getSceneX() ;
            lastMouseLocation.y = mouseEvent.getSceneY() ;

            // just some example logic to modify the list of dragged nodes:
            boolean found = false ;
            for (Circle c : circles) {
                if (c == circle) found = true ;
                if (found) nodesToDrag.add(c);
            }
        } );

        // --- Shift node calculated from mouse cursor movement
        circle.addEventFilter( MouseEvent.MOUSE_DRAGGED, (
                final MouseEvent mouseEvent ) -> {
                    double deltaX = mouseEvent.getSceneX() - lastMouseLocation.x ;
                    double deltaY = mouseEvent.getSceneY() - lastMouseLocation.y ;
                    for (Circle c : nodesToDrag) {
                        c.setCenterX( c.getCenterX() + deltaX  );
                        c.setCenterY( c.getCenterY() + deltaY );
                    }
                    lastMouseLocation.x = mouseEvent.getSceneX();
                    lastMouseLocation.y = mouseEvent.getSceneY();
        } );

        circle.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEvent -> nodesToDrag.clear());

    }

    private static final class MouseLocation {
        public double x, y;
    }
}