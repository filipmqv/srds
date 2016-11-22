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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static int rowCount = 0;
    private static int colCount = 0;
    private static final int MAP_ID = 1;
    private static int[][] map;
    // (-3)=entry_pont; (-2)=goal_-_user_disappears; (-1)=wall, 0=free, 1+=occupied_by_user(id)
    // [row][col]
    
    private static void initMap(UsersSession session) {
        session.deleteAll();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (map[i][j] != -1) {
                    session.insertPosition(MAP_ID, i, j, map[i][j]);
                }
            }
            System.out.println("finished " + i);
        }
    }

    private static void readMapFile() throws FileNotFoundException {
        Scanner sc=new Scanner(new FileReader("/home/filipmqv/srds/srds/mapHelpers/map.txt"));
        String[] firstLine = sc.nextLine().split("\\t");
        rowCount = Integer.parseInt(firstLine[0]);
        colCount = Integer.parseInt(firstLine[1]);
        map = new int[rowCount][colCount];
        int i = 0;
        while (sc.hasNextLine()) {
            String[] elements = sc.nextLine().split("\\t");
            for (int j = 0; j < colCount; j++) {
                map[i][j] = Integer.parseInt(elements[j]);
            }
            i++;
        }
    }

	public static void main(String[] args) throws IOException {
        readMapFile();
        UsersSession session = new UsersSession("127.0.0.1");
        initMap(session);
        System.out.println("finished");
        System.exit(0);
    }
}
