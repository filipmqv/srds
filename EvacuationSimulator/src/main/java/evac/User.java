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
        System.out.println(id + " created");
        System.out.println(id +" "+ row +" " + col +" ");
    }

    private boolean isFinished(int right, int down) {
        return right == -2 || down == -2;
    }

    private void randomlySleepSafely(int min, int max) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(min, max+1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // sprawdź w mapie możliwe kroki (prawo/dół?) czy ściana czy korytarz
        // sprawdź w bazie sprawdź czy wolne -> zajmij -> poczekaj -> sprawdź czy faktycznie jest zajęte ->
        // jeśli jest to przejdź na to pole i zwolnij poprzednie, a jak nie to próbuj od nowa zajmować
        boolean running = true;
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 2300));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (running) {
            int right = session.checkValue(Main.MAP_ID, row, col+1);
            int down = session.checkValue(Main.MAP_ID, row+1, col);
            if (isFinished(right, down)) {
                // finish - immediately empty position
                session.insertPosition(Main.MAP_ID, row, col, 0);
                running = false;
                System.out.println(id + " exiting");
                continue;
            }

            boolean chosenRight = true; // false = down chosen
            if (right == 0 && down == 0) {
                //System.out.println(id + " BOTH "+ row +" " + col +" "+ right +" "+ down);
                if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
                    session.insertPosition(Main.MAP_ID, row, col+1, id);
                    //System.out.println(id + " BOTH RIGHT "+ row +" " + col +" "+ right +" "+ down);
                } else {
                    chosenRight = false;
                    //System.out.println(id + " BOTH DOWN "+ row +" " + col +" "+ right +" "+ down);
                    session.insertPosition(Main.MAP_ID, row+1, col, id);
                }
            } else if (right == 0) {
                //System.out.println(id + " RIGHT "+ row +" " + col +" "+ right +" "+ down);
                session.insertPosition(Main.MAP_ID, row, col+1, id);
            } else if (down == 0) {
                chosenRight = false;
                //System.out.println(id + " DOWN "+ row +" " + col +" "+ right +" "+ down);
                session.insertPosition(Main.MAP_ID, row+1, col, id);
            } else {
                randomlySleepSafely(50, 100);
                continue;
            }

            // wait to check if anyone changed our position
            randomlySleepSafely(10, 30);

            if (chosenRight) {
                if (session.checkValue(Main.MAP_ID, row, col+1) == id) {
                    //System.out.println(id + " REMOVE RIGHT "+ row +" " + col +" "+ right +" "+ down);
                    session.insertPosition(Main.MAP_ID, row, col, 0);
                    col++;
                    //randomlySleepSafely(10, 30);
                }
            } else { // chosen down
                if (session.checkValue(Main.MAP_ID, row+1, col) == id) {
                    //System.out.println(id + " REMOVE DOWN "+ row +" " + col +" "+ right +" "+ down);
                    session.insertPosition(Main.MAP_ID, row, col, 0);
                    row++;
                    //randomlySleepSafely(10, 30);
                }
            }
        }
    }
}
