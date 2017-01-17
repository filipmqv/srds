package evac;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    public static final int ROW_COUNT = 600;
    public static final int COL_COUNT = 600;
    private static final int SCENE_ROW_COUNT = 700;
    private static final int SCENE_COL_COUNT = 1200;
    private static final double CANVAS_SCALE = 1;
    private static final double SCALE = 6;

    private static Map<Integer, Color> colorMap = new HashMap<>();

    private static Color getColor(int key) {
        return (key <= 0)
                ? colorMap.get(key)
                : Color.hsb(key%270, 0.45 + (0.07*key%0.55), 0.38 + (0.05*key%0.62));
    }

    private static void drawPixel(PixelWriter pw, int row, int col, int color) {
        pw.setColor(col, row, getColor(color));
    }

    private static void drawSquare(GraphicsContext gc, int row, int col, int color) {
            gc.setFill(getColor(color));
            gc.fillRect(SCALE * col, SCALE * row, SCALE, SCALE);
        }

    private static void draw(GraphicsContext gc, PixelWriter pw, UsersSession session) {
        int[][] map = session.selectAll();

        gc.clearRect(0, 0, Main.ROW_COUNT, Main.COL_COUNT);
        for (int i = 0; i < Main.ROW_COUNT; i++) {
            for (int j = 0; j < Main.COL_COUNT; j++) {
                if (map[i][j] != -10) {
                    if (SCALE == 1) {
                        drawPixel(pw, i, j, map[i][j]);
                    } else {
                        drawSquare(gc, i, j, map[i][j]);
                    }

                }

            }
        }
    }

    private static void initColorMap() {
        colorMap.put(0, Color.BLACK);
        colorMap.put(-1, Color.WHITE);
        colorMap.put(-2, Color.WHEAT);
        colorMap.put(-3, Color.BLACK);
        colorMap.put(-5, Color.CHOCOLATE);
        colorMap.put(-6, Color.RED);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initColorMap();

        StackPane drawingPane = new StackPane();
        drawingPane.setStyle("-fx-background-color: #3d3d3d");
        drawingPane.setPrefSize(ROW_COUNT * CANVAS_SCALE * SCALE, COL_COUNT * CANVAS_SCALE * SCALE);

        ScrollPane scrollPane = new ScrollPane(drawingPane);

        Canvas canvas = new Canvas(ROW_COUNT * SCALE, COL_COUNT * SCALE);
        canvas.setScaleX(CANVAS_SCALE);
        canvas.setScaleY(CANVAS_SCALE);
        PixelWriter pw = canvas.getGraphicsContext2D().getPixelWriter();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawingPane.getChildren().add(canvas);
        StackPane.setAlignment(canvas, Pos.CENTER);

        Scene scene = new Scene(scrollPane, SCENE_COL_COUNT, SCENE_ROW_COUNT, Color.RED);

        primaryStage.setTitle("Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        UsersSession session = new UsersSession("127.0.0.1");

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(0),
                        event -> draw(gc, pw, session)
                ),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();



        primaryStage.setOnCloseRequest(we -> {
            System.out.println("Stage is closing");
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
