package fca.io.db.reader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleConnection {
	
	private String serverUrl = "";
	private String userName = "";
	private String password = "";
	
	public OracleConnection() {
		
	}
	
	public OracleConnection(String serverUrl, String username, String password) {
		this.serverUrl = serverUrl;
		this.userName = username;
		this.password = password;
	}
	
	public ResultSet ExecuteQuery (String query) throws ClassNotFoundException, SQLException {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		String url = "jdbc:oracle:thin:@//" + this.serverUrl;
		Connection conn = DriverManager.getConnection(url,this.userName, this.password);
		conn.setAutoCommit(false);
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);
		
		stmt.close();
		
		return rset;
	}
}