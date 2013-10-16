package com.wattzap.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DataStore {
	private String framework = "embedded";
	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:";
	Connection conn = null;

	private Logger logger = LogManager.getLogger(DataStore.class.getName());

	public static void main(String[] args) {
		DataStore ds = new DataStore(".");
		String user = System.getProperty("user.name");
		System.out.println(ds.getProp(user, "weight"));
		ds.insertProp(user, "weight", "69.0");
		System.out.println(ds.getProp(user, "weight"));

		ds.close();
		System.out.println("SimpleApp finished");
	}

	public DataStore(String wd) {
		try {
			Class.forName(driver).newInstance();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}

		Statement s = null;
		try {

			Properties props = new Properties();
			String dbName = wd + "/prefs"; // the name of the database
			try {
				conn = DriverManager.getConnection(protocol + dbName + "",
						props);
				logger.info("Connected to " + dbName);
			} catch (SQLException e) {
				logger.info("Creating Data Store");
				conn = DriverManager.getConnection(protocol + dbName
						+ ";create=true", props);
				// Setup the database
				s = conn.createStatement();
				s.execute("create table props(username varchar(64), k varchar(128), v varchar(128), primary key (username, k))");
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			printSQLException(sqle);
		} finally {
			try {
				if (s != null) {
					s.close();
				}
			} catch (SQLException sqle) {
				// printSQLException(sqle);
				logger.error(sqle.getLocalizedMessage());
			}
		}
	}

	public void insertProp(String user, String k, String v) {
		PreparedStatement psInsert = null;
		try {
			int count = this.getRows(user, k);
			if (count == 0) {
				psInsert = conn
						.prepareStatement("INSERT INTO props (v, username, k) values(  ?, ?, ?)");

			} else {
				psInsert = conn
						.prepareStatement("UPDATE props SET v=? WHERE username=? and k=?");

			}
			psInsert.setString(1, v);
			psInsert.setString(2, user);
			psInsert.setString(3, k);
			int i = psInsert.executeUpdate();

			conn.commit();
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage());
		} finally {
			try {
				if (psInsert != null) {
					psInsert.close();
				}
			} catch (SQLException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
	}

	public String getProp(String user, String k) {
		PreparedStatement s = null;
		ResultSet rs = null;
		String value = null;

		try {
			s = conn.prepareStatement("SELECT v FROM props WHERE username = ? and k = ?");
			s.setString(1, user);
			s.setString(2, k);
			rs = s.executeQuery();

			if (rs.next()) {
				value = rs.getString(1);
			}
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage());
		} finally {
			try {
				if (s != null) {
					s.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return value;
	}

	public int getRows(String user, String k) {
		PreparedStatement s = null;
		ResultSet rs = null;
		int count = -1;

		try {
			s = conn.prepareStatement(" SELECT COUNT(*) FROM props WHERE username = ? and k = ?");
			// s =
			// conn.prepareStatement(" SELECT v FROM props WHERE username = ? and k = ?");
			s.setString(1, user);
			s.setString(2, k);
			rs = s.executeQuery();

			if (rs.next()) {
				count = rs.getInt(1);
				// String v = rs.getString(1);
				// count = 1;
			}
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage());
		} finally {
			try {
				if (s != null) {
					s.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return count;
	}

	public void close() {
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException sqle) {
			// printSQLException(sqle);
			logger.error(sqle.getLocalizedMessage());
		}
		if (framework.equals("embedded")) {
			try {
				// the shutdown=true attribute shuts down Derby
				DriverManager.getConnection("jdbc:derby:;shutdown=true");

				// To shut down a specific database only, but keep the
				// engine running (for example for connecting to other
				// databases), specify a database in the connection URL:
				// DriverManager.getConnection("jdbc:derby:" + dbName +
				// ";shutdown=true");
			} catch (SQLException se) {
				if (((se.getErrorCode() == 50000) && ("XJ015".equals(se
						.getSQLState())))) {
					// we got the expected exception
					logger.info("Data Store shut down normally");
					// Note that for single database shutdown, the expected
					// SQL state is "08006", and the error code is 45000.
				} else {
					// if the error code or SQLState is different, we have
					// an unexpected exception (shutdown failed)
					logger.error("Error shutting down Data Store "
							+ se.getLocalizedMessage());
				}
			}
		}
	}

	/**
	 * Prints details of an SQLException chain to <code>System.err</code>.
	 * Details included are SQL State, Error code, Exception message.
	 * 
	 * @param e
	 *            the SQLException from which to print details.
	 */
	public static void printSQLException(SQLException e) {
		// Unwraps the entire exception chain to unveil the real cause of the
		// Exception.
		while (e != null) {
			System.err.println("\n----- SQLException -----");
			System.err.println("  SQL State:  " + e.getSQLState());
			System.err.println("  Error Code: " + e.getErrorCode());
			System.err.println("  Message:    " + e.getMessage());
			// for stack traces, refer to derby.log or uncomment this:
			// e.printStackTrace(System.err);
			e = e.getNextException();
		}
	}
}
