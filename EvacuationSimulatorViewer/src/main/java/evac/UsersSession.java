package evac;


import java.text.SimpleDateFormat;
import java.util.Arrays;

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsersSession {

	private static final Logger logger = LoggerFactory
			.getLogger(UsersSession.class);

	public static final String DEFAULT_CONTACT_POINT = "127.0.0.1";
	public static UsersSession instance = null;
	
	private Session session;

	
	public static UsersSession getSession() {
		if (instance != null)
			return instance;
		
		synchronized (UsersSession.class)
		{
			if (instance == null)
				instance = new UsersSession(null);
		}
		
		return instance;
	}
	
	public UsersSession(String contactPoint) {
		if (contactPoint == null || contactPoint.isEmpty())
			contactPoint = DEFAULT_CONTACT_POINT;
		
		Cluster cluster = Cluster.builder()
				.addContactPoint(contactPoint).build();
		session = cluster.connect("EvacSim");
		
		prepareStatements();
	}

	private static PreparedStatement SELECT_ALL_MAP;

	private static final String USER_FORMAT = "- %-10s  %-16s %-10s %-10s\n";
	private static final SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private void prepareStatements() {
        SELECT_ALL_MAP = session.prepare("SELECT * FROM Map;");
		logger.info("Statements prepared");
	}

	public int[][] selectAll() {
		BoundStatement bs = new BoundStatement(SELECT_ALL_MAP);
		ResultSet rs = session.execute(bs);
        int[][] map = new int[600][600];
		for (int i = 0; i < 600; i++) {
			for (int j = 0; j < 600; j++) {
				map[i][j] = -1;
			}
		}
		int users = 0;
		for (Row row : rs) {
            map[row.getInt("rowId")][row.getInt("colId")] = row.getInt("value");
			if (row.getInt("value") > 0)
				users++;
		}
		System.out.println(users);

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
