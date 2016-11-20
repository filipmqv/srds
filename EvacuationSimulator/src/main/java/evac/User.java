package evac;

import java.util.concurrent.ThreadLocalRandom;

public class User implements Runnable {
    private int id;
    private int row;
    private int col;
    private UsersSession session;

    public User(int id, int row, int col, UsersSession session) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.session = session;
    }

    private boolean isFinished(int right, int down) {
        return right == -2 || down == -2;
    }

    @Override
    public void run() {
        // sprawdź w mapie możliwe kroki (prawo/dół?) czy ściana czy korytarz
        // sprawdź w bazie sprawdź czy wolne -> zajmij -> poczekaj -> sprawdź czy faktycznie jest zajęte ->
        // jeśli jest to przejdź na to pole i zwolnij poprzednie, a jak nie to próbuj od nowa zajmować
        boolean running = true;
        while (running) {
            int right = session.checkPosition(id, row, col+1);
            int down = session.checkPosition(id, row+1, col);
            if (isFinished(right, down)) {
                // finish - immediately empty position
                session.insertPosition(Main.MAP_ID, row, col, 0);
                running = false;
                continue;
            }

            boolean chosenRight = true; // false = down chosen
            if (right == 0 && down == 0) {
                if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
                    session.insertPosition(Main.MAP_ID, row, col+1, id);
                } else {
                    chosenRight = false;
                    session.insertPosition(Main.MAP_ID, row+1, col, id);
                }
            } else if (right == 0) {
                session.insertPosition(Main.MAP_ID, row, col+1, id);
            } else  if (down == 0) {
                chosenRight = false;
                session.insertPosition(Main.MAP_ID, row+1, col, id);
            } else {
                continue;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (chosenRight) {
                if (session.checkPosition(id, row, col+1) == id) {
                    session.insertPosition(Main.MAP_ID, row, col, 0);
                    col++;
                }
            } else { // chosen down
                if (session.checkPosition(id, row+1, col) == id) {
                    session.insertPosition(Main.MAP_ID, row, col, 0);
                    row++;
                }
            }


        }
    }
}
