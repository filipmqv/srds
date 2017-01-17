package evac;


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
		
		Cluster cluster = Cluster
				.builder()
				.addContactPoint(contactPoint)
				.withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.ONE))
				.build();
		session = cluster.connect("EvacSim");
		
		prepareStatements();
	}

	private static PreparedStatement INSERT_INTO_MAP;
	private static PreparedStatement CHECK_POSITION;

	private void prepareStatements() {
		INSERT_INTO_MAP = session.prepare("INSERT INTO Map (mapId, rowId, colId, value) VALUES (?, ?, ?, ?);");
		CHECK_POSITION = session.prepare("SELECT value FROM Map WHERE mapId=? AND rowId=? AND colId=?;");
		logger.info("Statements prepared");
	}

	public int checkValue(int mapId, int rowId, int colId) {
		BoundStatement bs = new BoundStatement(CHECK_POSITION);
		bs.bind(mapId, rowId, colId);
		ResultSet rs = session.execute(bs);
		for (Row row : rs) {
			return row.getInt("value");
		}
		return -10;
	}

	public void insertPosition(int mapId, int rowId, int colId, int value) {
		BoundStatement bs = new BoundStatement(INSERT_INTO_MAP);
		bs.bind(mapId, rowId, colId, value);
		session.execute(bs);
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
