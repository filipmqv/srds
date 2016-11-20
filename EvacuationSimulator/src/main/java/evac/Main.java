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
import java.util.List;

public class Main {

    public static final int ROW_COUNT = 15;
    public static final int COL_COUNT = 15;
    public static final int MAP_ID = 1;
    // (-3)=entry_pont; (-2)=goal_-_user_disappears; (-1)=wall, 0=free, 1+=occupied_by_user(id)
    // [row][col]
    public static final int[][] MAP = {
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,},
            {-1,-3,-3,-3,-3,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,},
            {-1,-3,-3,-3,-3,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,},
            {-1,-3,-3,-3,-3, 0, 0, 0,-1,-1,-1,-1,-1,-1,-1,},
            {-1,-3,-3,-3,-3, 0, 0, 0,-1,-1,-1,-1,-1,-1,-1,},
            {-1,-1,-1,-1,-1,-1, 0, 0,-1,-1,-1,-1,-1,-1,-1,},
            {-1,-1,-1,-1,-1,-1, 0, 0,-1,-1,-1,-1,-1,-1,-1,},
            {-1,-1,-1,-1,-1,-1, 0, 0, 0, 0, 0, 0,-1,-1,-1,},
            {-1,-1,-1,-1,-1,-1, 0, 0, 0, 0, 0, 0,-1,-1,-1,},
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 0, 0,-1,-1,-1,},
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 0, 0, 0, 0,-1,},
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 0, 0, 0, 0,-1,},
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 0, 0, 0, 0,-1,},
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 0, 0, 0,-2,-1,},
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,},
    };
    
    private static void initMap(UsersSession session) {
        int mapIndex = 0;
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COL_COUNT; j++) {
                session.insertPosition(MAP_ID, i, j, MAP[i][j]);
            }
        }
    }

    private static void clearEntryPoints(UsersSession session) {
        int mapIndex = 0;
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COL_COUNT; j++) {
                if (MAP[i][j] == -3) {
                    MAP[i][j] = 0;
                    session.insertPosition(MAP_ID, i, j, MAP[i][j]);
                }
                mapIndex++;
            }
        }
    }

	public static void main(String[] args) throws IOException {
		UsersSession session = new UsersSession("127.0.0.1");

        initMap(session);

        List<Thread> users = new ArrayList<>();

        //place all users on map; each on different spot
        users.add(new Thread(new User(1, 1, 1, session)));
        session.insertPosition(MAP_ID, 1, 1, 1);

        clearEntryPoints(session);
        
        users.forEach(Thread::start);

        // working

        users.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

		System.exit(0);
		
	}
}
