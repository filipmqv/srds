package evac;


import java.text.SimpleDateFormat;

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static PreparedStatement INSERT_INTO_MAP;
	private static PreparedStatement DELETE_ALL_VALUES;

	private static final SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private void prepareStatements() {
		INSERT_INTO_MAP = session.prepare("INSERT INTO Map (mapId, rowId, colId, value) VALUES (?, ?, ?, ?);");
		DELETE_ALL_VALUES = session.prepare("TRUNCATE Map;");
		logger.info("Statements prepared");
	}

	public void insertPosition(int mapId, int rowId, int colId, int value) {
		BoundStatement bs = new BoundStatement(INSERT_INTO_MAP);
		bs.bind(mapId, rowId, colId, value);
		session.execute(bs);
	}

	public void deleteAll() {
		BoundStatement bs = new BoundStatement(DELETE_ALL_VALUES);
		session.execute(bs);

		logger.info("All users deleted");
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
