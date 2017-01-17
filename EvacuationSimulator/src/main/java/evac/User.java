package evac;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class User implements Runnable {
    private enum Dir implements EnumConverter {
        RIGHT(-6), DOWN(-2), LEFT(-4), UP(-8), RD(-3), RU(-9), LU(-7), LD(-1), START(-5), END(0);

        private final byte value;

        Dir(int value) {
            this.value = (byte) value;
        }

        public int convert() {
            return value;
        }
    }
    private final ReverseEnumMap<Dir> reversedDir = new ReverseEnumMap<>(Dir.class);
    private Map<Dir, Integer> rowAddMap = new HashMap<>(); // direction; row +-1?
    private Map<Dir, Integer> colAddMap = new HashMap<>(); // direction; col+-1?

    private int id;
    private int row;
    private int col;
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
    }

    private int rowPos(Dir d) {
        return row + rowAddMap.get(d);
    }

    private int colPos(Dir d) {
        return col + colAddMap.get(d);
    }

    private void randomlySleepSafely(int min, int max) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(min, max+1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Map<Dir, Integer> getValuesMap() {
        Map<Dir, Integer> valuesMap = new HashMap<>();
        valuesMap.put(Dir.RIGHT, session.checkValue(Main.MAP_ID, rowPos(Dir.RIGHT), colPos(Dir.RIGHT)));
        valuesMap.put(Dir.DOWN, session.checkValue(Main.MAP_ID, rowPos(Dir.DOWN), colPos(Dir.DOWN)));
        valuesMap.put(Dir.LEFT, session.checkValue(Main.MAP_ID, rowPos(Dir.LEFT), colPos(Dir.LEFT)));
        valuesMap.put(Dir.UP, session.checkValue(Main.MAP_ID, rowPos(Dir.UP), colPos(Dir.UP)));
        return valuesMap;
    }

    private boolean isFinished(Map<Dir, Integer> valuesMap) {
        return valuesMap.get(Dir.DOWN) == 0 || valuesMap.get(Dir.RIGHT) == 0 || valuesMap.get(Dir.LEFT) == 0;
    }

    private Dir chooseDirection(Map<Dir, Integer> valuesMap, Dir currentPositionDirection) {
        switch (currentPositionDirection) {
            case UP:
            case DOWN:
                return chooseDirectionIfFree(valuesMap, currentPositionDirection, Dir.LEFT, Dir.RIGHT);
            case LEFT:
            case RIGHT:
                return chooseDirectionIfFree(valuesMap, currentPositionDirection, Dir.DOWN, Dir.UP);
            case RD:
                return chooseDirectionIfFree(valuesMap, null, Dir.RIGHT, Dir.DOWN);
            case RU:
                return chooseDirectionIfFree(valuesMap, null, Dir.RIGHT, Dir.UP);
            case LD:
                return chooseDirectionIfFree(valuesMap, null, Dir.LEFT, Dir.DOWN);
            case LU:
                return chooseDirectionIfFree(valuesMap, null, Dir.LEFT, Dir.UP);
            case START:
            case END:
                return chooseDirectionIfFree(valuesMap, null, Dir.DOWN, Dir.RIGHT);
            default:
                return null;
        }
    }

    private Dir chooseDirectionIfFree(Map<Dir, Integer> valuesMap, Dir currentPositionDirection, Dir option1, Dir option2) {
        // decide to choose option if possible (position free)
        if (currentPositionDirection != null && isPositionFree(valuesMap, currentPositionDirection)) {
            return currentPositionDirection;
        } else if (isPositionFree(valuesMap, option1) && isPositionFree(valuesMap, option2)) {
            return (ThreadLocalRandom.current().nextInt(0, 2) == 0) ? option1 : option2;
        } else if (isPositionFree(valuesMap, option1)) {
            return option1;
        } else if (isPositionFree(valuesMap, option2)) {
            return option2;
        }
        return null;
    }

    private boolean isPositionFree(Map<Dir, Integer> valuesMap, Dir d) {
        return valuesMap.get(d) >= -9 && valuesMap.get(d) <=0;
    }

    @Override
    public void run() {
        // check all 4 directions in DB (wall or empty or occupied position)
        // if empty -> insert -> wait -> check if still mine -> free previous position
        boolean running = true;
        randomlySleepSafely(5000, 5000);

        boolean fishedAnnouncedBefore = false;
        Dir currentPositionDirection = Dir.RD;

        while (running) {
            // check values of surrounding places from DB (can be empty-with next step direction, wall or other user)
            Map<Dir, Integer> valuesMap = getValuesMap();

            if (isFinished(valuesMap)) {
                if (!fishedAnnouncedBefore) {
                    fishedAnnouncedBefore = true;
                    System.out.println(id + " finished");
                }
            }

            Dir chosenDirection = chooseDirection(valuesMap, currentPositionDirection);

            // if direction is not chosen, skip round
            if (chosenDirection == null) {
                randomlySleepSafely(40, 60);
                continue;
            }

            // insert reservation
            session.insertPosition(Main.MAP_ID, rowPos(chosenDirection), colPos(chosenDirection), id);

            // wait to check if anyone changed our position reservation
            randomlySleepSafely(40, 60);

            // if reservation valid, accept change, update current position and remove previous from DB
            if (session.checkValue(Main.MAP_ID, rowPos(chosenDirection), colPos(chosenDirection)) == id) {
                session.insertPosition(Main.MAP_ID, row, col, currentPositionDirection.convert());
                row = rowPos(chosenDirection);
                col = colPos(chosenDirection);
                currentPositionDirection = reversedDir.get(valuesMap.get(chosenDirection));
            }

            randomlySleepSafely(40, 60);
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
