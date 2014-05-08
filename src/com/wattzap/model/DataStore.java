package com.wattzap.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.dto.WorkoutData;

/**
 * (c) 2013 David George / Wattzap.com
 * 
 * @author David George
 * @date 11 June 2013
 */
public class DataStore {
	private String framework = "embedded";
	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:";
	Connection conn = null;
	private static Cipher cipher = null; // in javax.crypto
	SecretKey secretKey = null;

	private Logger logger = LogManager.getLogger("DataStore");

	public DataStore(String wd, String key) {
		SecretKeyFactory keyGenerator;
		try {
			keyGenerator = SecretKeyFactory.getInstance("DES");

			// keyGenerator.init(168);
			DESKeySpec keySpec = new DESKeySpec(key.getBytes("UTF8"));
			secretKey = keyGenerator.generateSecret(keySpec);

			cipher = Cipher.getInstance("DES");
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}

		try {
			Class.forName(driver).newInstance();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}

		Statement s1 = null;
		Statement s2 = null;
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
				s1 = conn.createStatement();
				s1.execute("create table props(username varchar(64), k varchar(128), v varchar(128), primary key (username, k))");
			}

			String dbVersion = getProp("", "dbVersion");
			if (dbVersion == null) {
				s2 = conn.createStatement();
				s2.execute(WorkoutData.dbTable12());

				insertProp("", "dbVersion", "1.2");
			} else {
				s2 = conn.createStatement();


				// s2.execute("DROP TABLE workouts");
				//System.out.println("adding column");
				//s2.execute("ALTER TABLE workouts ADD COLUMN description varchar(256)");
				// insertProp("", "dbVersion", null);
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			printSQLException(sqle);
		} finally {
			try {
				if (s1 != null) {
					s1.close();
				}
			} catch (SQLException sqle) {
				logger.error(sqle.getLocalizedMessage());
			}
			try {
				if (s2 != null) {
					s2.close();
				}
			} catch (SQLException sqle) {
				logger.error(sqle.getLocalizedMessage());
			}
		}
	}

	/*
	 * create table metrics (id integer primary key autoincrement, "
	 * "filename varchar," "ride_date date," "ride_time double, "
	 * "average_cad double," "workout_time double, " "total_distance double,"
	 * "x_power double," "average_speed double," "total_work double,"
	 * "average_power double," "average_hr double," "relative_intensity double,"
	 * "bike_score double)");
	 */
	public void saveWorkOut(String user, WorkoutData data) {
		PreparedStatement psInsert = null;
		try {

			psInsert = conn.prepareStatement(WorkoutData.insert());

			psInsert.setString(1, user);
			psInsert.setString(2, data.getTcxFile());
			// Power
			psInsert.setInt(3, data.getFiveSecondPwr());
			psInsert.setInt(4, data.getOneMinutePwr());
			psInsert.setInt(5, data.getFiveMinutePwr());
			psInsert.setInt(6, data.getTwentyMinutePwr());

			psInsert.setInt(7, data.getQuadraticPower());
			psInsert.setInt(8, data.getTotalPower());
			psInsert.setInt(9, data.getMaxPower());
			psInsert.setInt(10, data.getAvePower());
			psInsert.setInt(11, data.getFtp());

			psInsert.setInt(12, data.getMaxHR());
			psInsert.setInt(13, data.getAveHR());
			psInsert.setInt(14, data.getMinHR());
			psInsert.setInt(15, data.getFtHR());

			psInsert.setInt(16, data.getMaxCadence());
			psInsert.setInt(17, data.getAveCadence());

			psInsert.setDouble(18, data.getDistance());
			psInsert.setDouble(19, data.getWeight());

			psInsert.setTime(20, new java.sql.Time(data.getTime()));
			psInsert.setDate(21, new java.sql.Date(data.getDate()));
			
			psInsert.setString(22, data.getDescription());
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

	public WorkoutData getWorkout(String user, String name) {
		PreparedStatement s = null;
		ResultSet rs = null;

		WorkoutData data = null;

		try {
			s = conn.prepareStatement(WorkoutData.selectWorkout());
			s.setString(1, user);
			s.setString(2, name);
			rs = s.executeQuery();

			while (rs.next()) {
				data = new WorkoutData();
				data.setTcxFile(rs.getString(2));
				data.setFiveSecondPwr(rs.getInt(3));
				data.setOneMinutePwr(rs.getInt(4));
				data.setFiveMinutePwr(rs.getInt(5));
				data.setTwentyMinutePwr(rs.getInt(6));
				data.setQuadraticPower(rs.getInt(7));
				data.setTotalPower(rs.getInt(8));
				data.setMaxPower(rs.getInt(9));
				data.setAvePower(rs.getInt(10));
				data.setFtp(rs.getInt(11));

				data.setMaxHR(rs.getInt(12));
				data.setAveHR(rs.getInt(13));
				data.setMinHR(rs.getInt(14));
				data.setFtHR(rs.getInt(15));

				data.setMaxCadence(rs.getInt(16));
				data.setAveCadence(rs.getInt(17));

				data.setDistance(rs.getDouble(18));
				data.setWeight(rs.getDouble(19));
				data.setTime(rs.getTime(20).getTime());
				data.setDate(rs.getDate(21).getTime());
				
				data.setDescription(rs.getString(22));
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

		return data;
	}
	
	public WorkoutData deleteWorkout(String user, String name) {
		PreparedStatement s = null;
		ResultSet rs = null;

		WorkoutData data = null;

		try {
			s = conn.prepareStatement(WorkoutData.delete());
			s.setString(1, user);
			s.setString(2, name);
			s.executeUpdate();
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

		return data;
	}
	
	public List<WorkoutData> listWorkouts(String user) {
		PreparedStatement s = null;
		ResultSet rs = null;

		List<WorkoutData> workouts = null;

		try {
			s = conn.prepareStatement(WorkoutData.select());
			s.setString(1, user);
			// s.setString(2, k);
			rs = s.executeQuery();

			// username, filename, fivesecp, oneminp, fiveminp, twentyminp, qp ,
			// totalp,
			// maxp, avep, ftp, maxhr, avehr, minhr, fthr, maxcad, avecad,
			// distance, weight, ridetime,
			// ridedate, primary key (username, filename))";

			workouts = new ArrayList<WorkoutData>();
			while (rs.next()) {
				WorkoutData data = new WorkoutData();
				data.setTcxFile(rs.getString(2));
				data.setFiveSecondPwr(rs.getInt(3));
				data.setOneMinutePwr(rs.getInt(4));
				data.setFiveMinutePwr(rs.getInt(5));
				data.setTwentyMinutePwr(rs.getInt(6));
				data.setQuadraticPower(rs.getInt(7));
				data.setTotalPower(rs.getInt(8));
				data.setMaxPower(rs.getInt(9));
				data.setAvePower(rs.getInt(10));
				data.setFtp(rs.getInt(11));

				data.setMaxHR(rs.getInt(12));
				data.setAveHR(rs.getInt(13));
				data.setMinHR(rs.getInt(14));
				data.setFtHR(rs.getInt(15));

				data.setMaxCadence(rs.getInt(16));
				data.setAveCadence(rs.getInt(17));

				data.setDistance(rs.getDouble(18));
				data.setWeight(rs.getDouble(19));
				data.setTime(rs.getTime(20).getTime());
				data.setDate(rs.getDate(21).getTime());
				
				data.setDescription(rs.getString(22));

				workouts.add(data);
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

		return workouts;
	}

	/**
	 * Encrypts values before writing to database using DES. If there is an
	 * exception nothing is written.
	 * 
	 * @param user
	 * @param k
	 * @param v
	 */
	public void insertPropCrypt(String user, String k, String v) {
		byte[] clearTextBytes;
		try {
			clearTextBytes = v.getBytes("UTF8");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] cipherBytes = cipher.doFinal(clearTextBytes);
			v = toHexString(cipherBytes);
			insertProp(user, k, v);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
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

	public String getPropCrypt(String user, String k) {
		String v = getProp(user, k);
		try {
			byte[] cipherBytes = toByteArray(v);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] decryptedBytes = cipher.doFinal(cipherBytes);
			v = new String(decryptedBytes, "UTF8");

		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
		return v;
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

	private static String toHexString(byte[] array) {
		return DatatypeConverter.printHexBinary(array);
	}

	private static byte[] toByteArray(String s) {
		return DatatypeConverter.parseHexBinary(s);
	}
}
