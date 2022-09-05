package moe.nyan10.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * プラグイン間でデータベース接続を共有することで管理とかリソース面で改善しちゃおう大作戦なのにゃ！！
 * あと、接続が切れちゃっても{@link #createStatement()}}とかを使ったときに自動で再接続するにゃ！
 * @since 1.1
 */
public class Database implements AutoCloseable {

	private static final int TIMEOUT_PERIOD = 3;//sec
	private static Map<String, Database> databases = new HashMap<>();
	private static FileConfiguration config = Nyan10Utils.getInstance().getConfig();

	private String url;
	private String user;
	private String password;
	private Connection conn;


	static {
		ConfigurationSection dbList = config.getConfigurationSection("database.list");
		for(String name : dbList.getKeys(false)) {
			ConfigurationSection dbConf = dbList.getConfigurationSection(name);
			databases.put(name, new Database(dbConf.getString("url"), dbConf.getString("user"), dbConf.getString("password")));
		}

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	/**
	 * データベースに接続するにゃ
	 * @param url データベースのURL。
	 * @param userName ユーザー名
	 * @param password パスワード
	 * @since 1.1
	 * @throws SQLException
	 */
	public Database(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}


	/**
	 * データベース接続を返すにゃ！
	 * 接続が確立してないときは接続をするにゃ！
	 * @return データベースへの接続
	 * @since 1.1
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		if (conn != null && conn.isValid(TIMEOUT_PERIOD)) {
			return conn;
		} else {
			conn = DriverManager.getConnection(url, user, password);
			return conn;
		}
	}


	/**
	 * {@link #getConnection()}で取得した接続でStatementを生成して返すにゃ！
	 * ちゃんと{@link Statement#close()}をするのにゃ！try-with-resources文使うのおすすめなのにゃ！°˖✧◝(⁰▿⁰)◜✧˖°
	 * @return 生成したStatement
	 * @since 1.1
	 * @throws SQLException
	 */
	public Statement createStatement() throws SQLException {
		return getConnection().createStatement();
	}


	/**
	 * 設定されたデータベースのURLを取得するにゃ！
	 * @return データベースのURL
	 * @since 1.1
	 */
	public String getUrl() {
		return url;
	}


	/**
	 * 設定されたユーザー名を返すにゃ！
	 * @return データベースのユーザー名
	 * @since 1.1
	 */
	public String getUsername() {
		return user;
	}


	/**
	 * 設定されたパスワードを返すにゃ！
	 * @return データベースのパスワード
	 * @since 1.1
	 */
	public String getPassword() {
		return password;
	}


	/**
	 * このデータベースをcloseするにゃー
	 * @since 1.1
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (conn != null)
			conn.close();
	}


	/**
	 * データベースを登録するにゃ！
	 * @param name データベースの登録名
	 * @param db 登録するデータベース
	 * @since 1.1
	 */
	public static void register(String name, Database db) {
		databases.put(name, db);
		ConfigurationSection dbconf = config.getConfigurationSection("database.list").createSection(name);
		dbconf.set("url", db.getUrl());
		dbconf.set("user", db.getUsername());
		dbconf.set("password", db.getPassword());
		Nyan10Utils.getInstance().saveConfig();
	}
	
	
	/**
	 * データベースの登録を解除するにゃ！
	 * @param name データベースの登録名
	 * @return 解除したデータベース。未定義の場合null。
	 * @since 1.1
	 */
	public static Database unregister(String name) {
		config.set("database.list."+name, null);
		Nyan10Utils.getInstance().saveConfig();
		return databases.remove(name);
	}


	/**
	 * データベースを返すにゃ！
	 * @param データベースの登録名
	 * @return リモートデータベース。未設定時はnull。
	 * @since 1.1
	 */
	public static Database get(String name) {
		return databases.get(name);
	}
	
	
	/**
	 * 登録されたすべてのデータベースを返すにゃ！
	 * @return 全てのデータベース
	 * @since 1.1
	 */
	public static Map<String, Database> getAll() {
		return databases;
	}


	/**
	 * 指定の名前のデータベースが登録されてるか返すにゃ！
	 * @param データベースの登録名
	 * @return 設定されている場合true
	 * @since 1.1
	 */
	public static boolean has(String name) {
		return databases.containsKey(name);
	}
	
}
