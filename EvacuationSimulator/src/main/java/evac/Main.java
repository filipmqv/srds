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

	public static void main(String[] args) throws IOException {
		UsersSession session = new UsersSession("127.0.0.1");

        List<Thread> userThreads = new ArrayList<>();

        //place all users on map; each on different spot
        // TODO place safely (with checking), randomly in START area (to launch 1+ instances)
        int r=50, c=50;
        //int r=2, c=2;
        for (int i = 1; i < r; i++) {
            for (int j = 1; j < c; j++) {
                int userId = i*r+j;
                User u = new User(userId, i, j, session);
                userThreads.add(new Thread(u));
                session.insertPosition(MAP_ID, i, j, userId);
            }
        }
        
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
