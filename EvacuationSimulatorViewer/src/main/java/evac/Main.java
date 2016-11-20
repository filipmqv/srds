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

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Application {

    public static final int ROW_COUNT = 15;
    public static final int COL_COUNT = 15;


    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = new Pane();

        Canvas canvas = new Canvas(300, 300);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        root.getChildren().add(canvas);

        Scene scene = new Scene(root, 300, 300, Color.WHITESMOKE);

        primaryStage.setTitle("Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        UsersSession session = new UsersSession("127.0.0.1");

        Painter painter = new Painter(gc, session);
        Thread t = new Thread(painter);
        t.start();

        primaryStage.setOnCloseRequest(we -> {
            painter.stopMe();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Stage is closing");
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
