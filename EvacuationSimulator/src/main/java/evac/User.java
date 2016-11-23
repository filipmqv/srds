package evac;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class User implements Runnable {
    private enum Dir {RIGHT(-6), DOWN(-2), LEFT(-4), UP(-8), RD(-3), RU(-9), LU(-7), LD(-1), START(-5), END(0);
        private final int id;
        Dir(int id) { this.id = id; }
        public int getValue() { return id; }
    };
    private Map<Integer, Dir> reversedDir = new HashMap<>();
    private Map<Dir, Integer> rowAddMap = new HashMap<>(); // direction; [row +-1?, col+-1?]
    private Map<Dir, Integer> colAddMap = new HashMap<>(); // direction; [row +-1?, col+-1?]

    private int id;
    private int row;
    private int col;
    private Dir currentPositionDirection = Dir.RD;
    private Dir futurePositionDirection = Dir.RD;
    private UsersSession session;

    public User(int id, int row, int col, UsersSession session) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.session = session;
        System.out.println(id + " created" + row +" " + col +" ");
        rowAddMap.put(Dir.DOWN, 1); colAddMap.put(Dir.DOWN, 0);
        rowAddMap.put(Dir.RIGHT, 0); colAddMap.put(Dir.RIGHT, 1);
        rowAddMap.put(Dir.LEFT, 0); colAddMap.put(Dir.LEFT, -1);
        rowAddMap.put(Dir.UP, -1); colAddMap.put(Dir.UP, 0);
        reversedDir.put(-1, Dir.LD);
        reversedDir.put(-2, Dir.DOWN);
        reversedDir.put(-3, Dir.RD);
        reversedDir.put(-4, Dir.LEFT);
        reversedDir.put(-5, Dir.START);
        reversedDir.put(-6, Dir.RIGHT);
        reversedDir.put(-7, Dir.LU);
        reversedDir.put(-8, Dir.UP);
        reversedDir.put(-9, Dir.RU);
        reversedDir.put(0, Dir.END);
    }

    private int rowPos(Dir d) {
        return row + rowAddMap.get(d);
    }

    private int colPos(Dir d) {
        return col + colAddMap.get(d);
    }

    private boolean isPositionFree(Map<Dir, Integer> valuesMap, Dir d) {
        return valuesMap.get(d) >= -9 && valuesMap.get(d) <=0;
    }

    /*private boolean isFinished(int right, int down) {
        return right == -2 || down == -2;
    }*/

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
        randomlySleepSafely(2000, 2000);


        while (running) {
            Map<Dir, Integer> valuesMap = new HashMap<>(); // direction, value from DB
            valuesMap.put(Dir.RIGHT, session.checkValue(Main.MAP_ID, rowPos(Dir.RIGHT), colPos(Dir.RIGHT)));
            valuesMap.put(Dir.DOWN, session.checkValue(Main.MAP_ID, rowPos(Dir.DOWN), colPos(Dir.DOWN)));
            valuesMap.put(Dir.LEFT, session.checkValue(Main.MAP_ID, rowPos(Dir.LEFT), colPos(Dir.LEFT)));
            valuesMap.put(Dir.UP, session.checkValue(Main.MAP_ID, rowPos(Dir.UP), colPos(Dir.UP)));


            /*if (isFinished(right, down)) {
                // finish - immediately empty position
                session.insertPosition(Main.MAP_ID, row, col, 0);
                running = false;
                System.out.println(id + " exiting");
                continue;
            }*/

            // TODO dodać sprawdzanie czy w losowych jest wolny - można po wylosowaniu; jeśi nie jest wolny to weź przeciwny, jeśli oba zajęte to nic
            Dir chosenDirection = null;
            switch (currentPositionDirection) {
                case UP:
                case DOWN:
                    if (isPositionFree(valuesMap, currentPositionDirection)) {
                        chosenDirection = currentPositionDirection;
                    } else if (isPositionFree(valuesMap, Dir.LEFT) && isPositionFree(valuesMap, Dir.RIGHT)) {
                        chosenDirection = (ThreadLocalRandom.current().nextInt(0, 2) == 0) ? Dir.LEFT : Dir.RIGHT;
                    } else if (isPositionFree(valuesMap, Dir.LEFT)) {
                        chosenDirection = Dir.LEFT;
                    } else if (isPositionFree(valuesMap, Dir.RIGHT)) {
                        chosenDirection = Dir.RIGHT;
                    } // else nothing
                    break;
                case LEFT:
                case RIGHT:
                    if (isPositionFree(valuesMap, currentPositionDirection)) {
                        chosenDirection = currentPositionDirection;
                    } else if (isPositionFree(valuesMap, Dir.DOWN) && isPositionFree(valuesMap, Dir.UP)) {
                        chosenDirection = (ThreadLocalRandom.current().nextInt(0, 2) == 0) ? Dir.DOWN : Dir.UP;
                    } else if (isPositionFree(valuesMap, Dir.DOWN)) {
                        chosenDirection = Dir.DOWN;
                    } else if (isPositionFree(valuesMap, Dir.UP)) {
                        chosenDirection = Dir.UP;
                    } // else nothing
                    break;
                case RD:
                    // TODO poprawić jeśli chosen jest zajęty to jeszcze sprawdzić drugi kierunek zamiast od razu null
                    chosenDirection = (ThreadLocalRandom.current().nextInt(0, 2) == 0) ? Dir.RIGHT : Dir.DOWN;
                    chosenDirection = (isPositionFree(valuesMap, chosenDirection)) ? chosenDirection : null;
                    break;
                case RU:
                    chosenDirection = (ThreadLocalRandom.current().nextInt(0, 2) == 0) ? Dir.RIGHT : Dir.UP;
                    chosenDirection = (isPositionFree(valuesMap, chosenDirection)) ? chosenDirection : null;
                    break;
                case LD:
                    chosenDirection = (ThreadLocalRandom.current().nextInt(0, 2) == 0) ? Dir.LEFT : Dir.DOWN;
                    chosenDirection = (isPositionFree(valuesMap, chosenDirection)) ? chosenDirection : null;
                    break;
                case LU:
                    chosenDirection = (ThreadLocalRandom.current().nextInt(0, 2) == 0) ? Dir.LEFT : Dir.UP;
                    chosenDirection = (isPositionFree(valuesMap, chosenDirection)) ? chosenDirection : null;
                    break;
                case START:
                case END:
                    chosenDirection = (ThreadLocalRandom.current().nextInt(0, 2) == 0) ? Dir.DOWN : Dir.RIGHT;
                    chosenDirection = (isPositionFree(valuesMap, chosenDirection)) ? chosenDirection : null;
                    break;
                default:
                    break;
            }

            if (chosenDirection == null) {
                randomlySleepSafely(10, 20);
                continue;
            }

            futurePositionDirection = reversedDir.get(valuesMap.get(chosenDirection));
            session.insertPosition(Main.MAP_ID, rowPos(chosenDirection), colPos(chosenDirection), id);
            //System.out.println("rezerw " + chosenDirection + " r" + row + " rp" + rowPos(chosenDirection) + " c" + col + " cp" + colPos(chosenDirection));

            /*Dir chosenDirection;
            if (right == 0 && down == 0) {
                //System.out.println(id + " BOTH "+ row +" " + col +" "+ right +" "+ down);
                if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
                    chosenDirection = Dir.RIGHT;
                    session.insertPosition(Main.MAP_ID, row, col+1, id);
                    //System.out.println(id + " BOTH RIGHT "+ row +" " + col +" "+ right +" "+ down);
                } else {
                    chosenDirection = Dir.DOWN;
                    //System.out.println(id + " BOTH DOWN "+ row +" " + col +" "+ right +" "+ down);
                    session.insertPosition(Main.MAP_ID, row+1, col, id);
                }
            } else if (right == 0) {
                //System.out.println(id + " RIGHT "+ row +" " + col +" "+ right +" "+ down);
                chosenDirection = Dir.RIGHT;
                session.insertPosition(Main.MAP_ID, row, col+1, id);
            } else if (down == 0) {
                chosenDirection = Dir.DOWN;
                //System.out.println(id + " DOWN "+ row +" " + col +" "+ right +" "+ down);
                session.insertPosition(Main.MAP_ID, row+1, col, id);
            } else { //no move down or right - try with possibility other dirctions

                randomlySleepSafely(50, 100);
                continue;
            }*/

            // TODO przed insertem należy do zmiennej zapisać jaki kierunek był na tym miejscu

            // wait to check if anyone changed our position
            randomlySleepSafely(10, 20);

            switch (chosenDirection) {
                case RIGHT:
                    if (session.checkValue(Main.MAP_ID, row, col+1) == id) {
                        //System.out.println(id + " REMOVE RIGHT "+ row +" " + col );
                        // TODO przywracanie do wartości previousPosDir i odpowiedni swap zmiennych
                        session.insertPosition(Main.MAP_ID, row, col, currentPositionDirection.getValue());
                        col++;
                        currentPositionDirection = futurePositionDirection;

                    }
                    break;
                case DOWN:
                    if (session.checkValue(Main.MAP_ID, row+1, col) == id) {
                        //System.out.println(id + " REMOVE DOWN "+ row +" " + col +" ");
                        session.insertPosition(Main.MAP_ID, row, col, currentPositionDirection.getValue());
                        row++;
                        currentPositionDirection = futurePositionDirection;
                        //randomlySleepSafely(10, 30);
                    }
                    break;
                case LEFT:
                    if (session.checkValue(Main.MAP_ID, row, col-1) == id) {
                        //System.out.println(id + " REMOVE LEFT "+ row +" " + col +" ");
                        session.insertPosition(Main.MAP_ID, row, col, currentPositionDirection.getValue());
                        col--;
                        currentPositionDirection = futurePositionDirection;
                        //randomlySleepSafely(10, 30);
                    }
                    break;
                case UP:
                    if (session.checkValue(Main.MAP_ID, row-1, col) == id) {
                        //System.out.println(id + " REMOVE UP "+ row +" " + col +" ");
                        session.insertPosition(Main.MAP_ID, row, col, currentPositionDirection.getValue());
                        row--;
                        currentPositionDirection = futurePositionDirection;
                        //randomlySleepSafely(10, 30);
                    }
                    break;
                default:
                    break;
            }
            randomlySleepSafely(10, 30);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (row != user.row) return false;
        return col == user.col;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + row;
        result = 31 * result + col;
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", row=" + row +
                ", col=" + col +
                '}';
    }
}
