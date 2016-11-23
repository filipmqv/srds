package evac;


import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class UsersSession {

	private static final Logger logger = LoggerFactory
			.getLogger(UsersSession.class);

	public static final String DEFAULT_CONTACT_POINT = "127.0.0.1";

	private Session session;
	
	public UsersSession(String contactPoint) {
		if (contactPoint == null || contactPoint.isEmpty())
			contactPoint = DEFAULT_CONTACT_POINT;
		
		Cluster cluster = Cluster.builder()
				.addContactPoint(contactPoint).build();
		session = cluster.connect("EvacSim");
		
		prepareStatements();
	}

	private static PreparedStatement SELECT_ALL_MAP;

	private void prepareStatements() {
        SELECT_ALL_MAP = session.prepare("SELECT * FROM Map;");
		logger.info("Statements prepared");
	}

	public int[][] selectAll() {
		BoundStatement bs = new BoundStatement(SELECT_ALL_MAP);
		ResultSet rs = session.execute(bs);
        int[][] map = new int[Main.ROW_COUNT][Main.COL_COUNT];
		for (int i = 0; i < Main.ROW_COUNT; i++) {
			for (int j = 0; j < Main.COL_COUNT; j++) {
				map[i][j] = -10;
			}
		}
		int users = 0;
		Set<Integer> valueSet = new HashSet<>();
		for (Row row : rs) {
            map[row.getInt("rowId")][row.getInt("colId")] = row.getInt("value");

			if (row.getInt("value") > 0) { // statistics about users
				users++;
				valueSet.add(row.getInt("value")); // can be less than actual no of users due to different moments of insert/delete
			}
		}
		System.out.println("count: " + users + " set:" + valueSet.size());

		return map;
	}

	protected void finalize() {
		try {
			if (session != null) {
				session.getCluster().close();
			}
		} catch (Exception e) {
			logger.error("Could not close existing cluster", e);
		}
	}
	
}
