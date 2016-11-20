package evac;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class Painter implements Runnable {

    private GraphicsContext gc;
    private UsersSession session;
    private boolean running = true;
    private static Map<Integer, Color> colorMap = new HashMap<Integer, Color>();

    public Painter(GraphicsContext gc, UsersSession session) {
        this.gc = gc;
        this.session = session;
        colorMap.put(0, Color.AQUA);
        colorMap.put(-1, Color.BLACK);
        colorMap.put(1, Color.RED);
    }

    public void stopMe() {
        running = false;
    }

    private void drawSquare(GraphicsContext gc, int row, int col, int color) {
        gc.setFill(colorMap.get(color));
        int x = 20;
        gc.fillRect(x * row, x * col, x, x);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int[][] map = session.selectAll();

            gc.clearRect(0, 0, 300, 300);
            for (int i = 0; i < Main.ROW_COUNT; i++) {
                for (int j = 0; j < Main.COL_COUNT; j++) {
                    drawSquare(gc, i, j, map[i][j]);
                }
            }
        }
    }
}
