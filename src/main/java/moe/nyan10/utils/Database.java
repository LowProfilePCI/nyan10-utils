package moe.nyan10.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.configuration.ConfigurationSection;

/**
 * プラグイン間でデータベース接続を共有することで管理とかリソース面で改善しちゃおう大作戦なのにゃ！！
 * あと、接続が切れちゃっても{@link #createStatement()}}とかを使ったときに自動で再接続するにゃ！
 * @since 1.1
 */
public class Database implements AutoCloseable {

	private static Map<String, Database> databases = new HashMap<>();
	private static int defaultTimeout = 3;//sec
	private static Timer timer = new Timer();
	private static boolean printExceptionByPing;
	private String url;
	private String user;
	private String password;
	private Connection conn;
	private int timeout = defaultTimeout;
	private long lastPing = -1;
	private int responseTime = -1;
	private int aliveMonitoring = -1;
	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			ping();
		}
	};


	static {
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
	 */
	public Database(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}
	
	
	/**
	 * データベースに接続するにゃ
	 * @param url データベースのURL。
	 * @param userName ユーザー名
	 * @param password パスワード
	 * @param timeoutTime タイムアウト時間(0以下でデフォルト値)
	 * @param aliveMonitoring 死活監視間隔(0以下で無効)
	 * @since 1.2
	 */
	public Database(String url, String user, String password, int timeoutTime, int aliveMonitoring) {
		this(url, user, password);
		if (timeoutTime > 0)
			this.timeout = timeoutTime;
		this.aliveMonitoring = aliveMonitoring;
		if (aliveMonitoring > 0)
			timer.schedule(task, 0, aliveMonitoring*1000);
	}


	/**
	 * データベース接続を返すにゃ！
	 * 接続が確立してないときは接続をするにゃ！
	 * @return データベースへの接続
	 * @since 1.1
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		if (conn != null && conn.isValid(timeout)) {
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
	 * タイムアウト時間を秒単位で返すにゃ！
	 * @return タイムアウト時間(秒)
	 * @since 1.2
	 */
	public int getTimeoutTime() {
		return timeout;
	}
	
	
	/**
	 * 死活監視の間隔を返すにゃ！
	 * 死活監視が無効の場合は0以下の数値を返すにゃ！
	 * @return 死活監視の間隔(秒)
	 * @since 1.2
	 */
	public int getAliveMonitoring() {
		return aliveMonitoring;
	}
	
	
	/**
	 * 最後の死活監視の応答速度を返すにゃ！
	 * データベースに接続できなかった場合やデータがない場合は-1を返すにゃ！
	 * @return 応答速度(ms)
	 * @since 1.2
	 */
	public int getLastResponseTime() {
		return responseTime;
	}
	
	
	/**
	 * 最後に{@link #ping()}をした時間を返すにゃ！
	 * 時間は{@link System#currentTimeMillis()}に依存するにゃ！
	 * まだ一度もpingを実行していない場合は-1を返すにゃ！
	 * @return 最後にpingを実行した時間
	 * @since 1.2
	 */
	public long getLastPingedTime() {
		return lastPing;
	}
	
	
	/**
	 * 死活監視をするためにpingを投げるにゃ！
	 * 応答速度を取得するには{@link #getLastResponseTime()}を使ってにゃ！
	 * @see #getLastResponseTime()
	 * @since 1.2
	 */
	public void ping() {
		this.lastPing = System.currentTimeMillis();
		try(Statement stmt = createStatement()) {
			long start = System.nanoTime();
			stmt.execute("SELECT 1");
			responseTime = (int)(System.nanoTime()-start)/1000000;
		} catch (SQLException e) {
			responseTime = -1;
			if (printExceptionByPing)
				e.printStackTrace();
			else
				Nyan10Utils.getInstance().getLogger().severe(url+"に接続できなかったにゃ！");
		}
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
		ConfigurationSection dbconf = Nyan10Utils.getInstance().getConfig().getConfigurationSection("database.list").createSection(name);
		dbconf.set("url", db.getUrl());
		dbconf.set("user", db.getUsername());
		dbconf.set("password", db.getPassword());
		dbconf.set("timeout-time", db.getTimeoutTime());
		dbconf.set("alive-monitoring", db.getAliveMonitoring());
		Nyan10Utils.getInstance().saveConfig();
	}
	
	
	/**
	 * データベースの登録を解除するにゃ！
	 * @param name データベースの登録名
	 * @return 解除したデータベース。未定義の場合null。
	 * @since 1.1
	 */
	public static Database unregister(String name) {
		Nyan10Utils.getInstance().getConfig().set("database.list."+name, null);
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
	
	
	static void load() {
		databases.clear();
		timer.purge();
		printExceptionByPing = Nyan10Utils.getInstance().getConfig().getBoolean("database.print-exception-by-ping", true);
		defaultTimeout = Nyan10Utils.getInstance().getConfig().getInt("database.default-timeout-time", 3);
		ConfigurationSection dbList = Nyan10Utils.getInstance().getConfig().getConfigurationSection("database.list");
		for(String name : dbList.getKeys(false)) {
			ConfigurationSection dbConf = dbList.getConfigurationSection(name);
			databases.put(name, new Database(dbConf.getString("url"), dbConf.getString("user"), dbConf.getString("password"), dbConf.getInt("timeout-time", -1), dbConf.getInt("alive-monitoring", -1)));
		}
	}
	
}
