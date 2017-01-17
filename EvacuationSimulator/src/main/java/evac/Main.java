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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    static final int MAP_ID = 1;
    static int PROC_ID;
    static int NUMBER_OF_USERS = 1;//500;
    static int START_ROW = 1;
    static int START_COL = 1;
    static int START_ROWS = 80; // max 99
    static int START_COLS = 80; // max 99


    private static void parseArgs(String[] args) {
        if (args.length == 0) {
            System.out.println("Put proc_id as first arg!");
            System.exit(0);
        }
        PROC_ID = Integer.parseInt(args[0]);
        if (args.length > 1) {
            NUMBER_OF_USERS = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            START_ROW = Integer.parseInt(args[2]);
            START_COL = Integer.parseInt(args[3]);
            START_ROWS = Integer.parseInt(args[4]);
            START_COLS = Integer.parseInt(args[5]);
        }
    }

	public static void main(String[] args) throws IOException, InterruptedException {
	    parseArgs(args);

		UsersSession session = new UsersSession("127.0.0.1");

        List<Thread> userThreads = new ArrayList<>();

        // reserve N random places in start area by putting PROC_ID as value
        Set<Reservation> reservations = new HashSet<>();
        while (reservations.size() < NUMBER_OF_USERS) {
            int row = ThreadLocalRandom.current().nextInt(START_ROW, START_ROW + START_ROWS);
            int col = ThreadLocalRandom.current().nextInt(START_COL, START_COL + START_COLS);
            if (session.checkValue(MAP_ID, row, col) == -5) {
                if (reservations.add(new Reservation(row, col))) {
                    session.insertPosition(MAP_ID, row, col, PROC_ID);
                }
            }
        }

        Thread.sleep(2000);

        // place users on reserved spots if they still belong to PROC_ID
        for (Reservation r : reservations) {
            if (session.checkValue(MAP_ID, r.getRow(), r.getCol()) == PROC_ID) {
                int userId = 10000 * PROC_ID + r.getRow() * START_COLS + r.getCol();
                User u = new User(userId, r.getRow(), r.getCol(), session);
                userThreads.add(new Thread(u));
                session.insertPosition(MAP_ID, r.getRow(), r.getCol(), userId);
            }
        }

        System.out.println("Press enter to start");
        System.in.read();
        
        userThreads.forEach(Thread::start);

        // working

        System.out.println("users size: " + userThreads.size());

        userThreads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

		System.exit(0);
		
	}
}
