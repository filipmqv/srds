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

    static final int MAP_ID = 1;
    // (-3)=entry_pont; (-2)=goal_-_user_disappears; (-1)=wall, 0=free, 1+=occupied_by_user(id)

	public static void main(String[] args) throws IOException {
		UsersSession session = new UsersSession("127.0.0.1");

        List<Thread> users = new ArrayList<>();

        //place all users on map; each on different spot
        // TODO place safely (with checking), randomly in (-3) area
        for (int i = 1; i < 30; i++) {
            for (int j = 1; j < 30; j++) {
                int userId = i*30+j;
                users.add(new Thread(new User(userId, i, j, session)));
                session.insertPosition(MAP_ID, i, j, userId);
            }
        }
        
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
