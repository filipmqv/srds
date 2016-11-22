package evac;


import java.text.SimpleDateFormat;

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

	private static PreparedStatement INSERT_INTO_MAP;
	private static PreparedStatement CHECK_POSITION;

	private static final String USER_FORMAT = "- %-10s  %-16s %-10s %-10s\n";
	private static final SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

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
			int value = row.getInt("value");
			return value != -3 ? value : 0;
		}
		return -1;
	}

	public void insertPosition(int mapId, int rowId, int colId, int value) {
		BoundStatement bs = new BoundStatement(INSERT_INTO_MAP);
		bs.bind(mapId, rowId, colId, value);
		session.execute(bs);
		//logger.info("User " + user + " inserted position: row=" + rowId + " col=" + colId);
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
