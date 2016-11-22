/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private static final int ROW_COUNT = 600;
    private static final int COL_COUNT = 600;
    private static final int SCENE_ROW_COUNT = 700;
    private static final int SCENE_COL_COUNT = 1200;
    private static final double SCALE = 3;
    private static Map<Integer, Color> colorMap = new HashMap<>();

    private static Color getColor(int key) {
        return (key <= 0)
                ? colorMap.get(key)
                : Color.hsb((key*key*40)%270, 0.5 + (0.1*key%0.5), 0.5 + (0.1*key%0.5));
    }

    private static void drawPixel(PixelWriter pw, int row, int col, int color) {
        pw.setColor(col, row, getColor(color));
    }

    private static void draw(GraphicsContext gc, PixelWriter pw, UsersSession session) {
        int[][] map = session.selectAll();

        gc.clearRect(0, 0, Main.ROW_COUNT, Main.COL_COUNT);
        for (int i = 0; i < Main.ROW_COUNT; i++) {
            for (int j = 0; j < Main.COL_COUNT; j++) {
                if (map[i][j] != -1)
                    drawPixel(pw, i, j, map[i][j]);
            }
        }
    }

    private static void initColorMap() {
        colorMap.put(0, Color.BLACK);
        colorMap.put(-1, Color.WHITE);
        colorMap.put(-2, Color.WHEAT);
        colorMap.put(-3, Color.BLACK);
        colorMap.put(-4, Color.CHOCOLATE);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initColorMap();

        StackPane drawingPane = new StackPane();
        drawingPane.setStyle("-fx-background-color: #3d3d3d");
        drawingPane.setPrefSize(ROW_COUNT * SCALE, COL_COUNT * SCALE);

        ScrollPane scrollPane = new ScrollPane(drawingPane);

        Canvas canvas = new Canvas(ROW_COUNT, COL_COUNT);
        canvas.setScaleX(SCALE);
        canvas.setScaleY(SCALE);
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
                new KeyFrame(Duration.millis(600))
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
