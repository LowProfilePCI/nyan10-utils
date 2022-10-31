package moe.nyan10.utils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import moe.nyan10.utils.internal.EventListener;

/**
 * nyan10の便利機能や共有機能を詰め込んだクラスにゃ！
 * @since 1.0
 */
public class Nyan10Utils extends JavaPlugin {

    static final String PREFIX = "§7§l[§dNyan10§aUtils§7§l]§r ";
    private static Nyan10Utils instance;
    private EventListener listener = new EventListener();

    private FileConfiguration config;
    private boolean showPassword;
    private boolean enableExecCmd;

    //private static PermissionManager permMgr = PermissionsEx.getPermissionManager();


    @Override
    public void onEnable() {
        instance = this;
        
        System.out.println("利用可能なJDBCドライバ:");
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while(drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			System.out.println("  "+driver.getClass().getCanonicalName()+" / "+driver.getMajorVersion()+"."+driver.getMinorVersion());
		}

        getServer().getPluginManager().registerEvents(listener, this);

        loadConfig();
    }


    @Override
    public void onDisable() {
    	
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nyan10utils")) {
            if (args.length == 0) {
                sender.sendMessage(
                		PREFIX+"§6/nyan10utils version: バージョンを表示するにゃ！\n"+
                		PREFIX+"§6/nyan10utils reload: 設定を再読み込みするにゃ！\n"+
                		PREFIX+"§6/nyan10utils show-pw [on|off]: DBのパスワード表示設定をするにゃ！\n"+
                		PREFIX+"§6/nyan10utils get-db [DB名]: データベースの情報を表示するにゃ！\n"+
    					PREFIX+"§6/nyan10utils register-db <DB名> <URL> [USER] [PW]: データベースを登録するにゃ！\n"+
                		PREFIX+"§6/nyan10utils remove-db <DB名>: データベースの登録を解除するにゃ！\n"+
                		PREFIX+"§6/nyan10utils rename-db <DB名> <新DB名>: データベースの登録名を変更するにゃ！\n"+
                		PREFIX+"§6/nyan10utils ping-db <DB名>: データベースの応答速度を測定するにゃ！\n"+
    					(enableExecCmd ? PREFIX+"§6/nyan10utils exec-db <DB名> <SQL>: ローカルデータベースにSQLコマンドを送信するにゃ！":"")
                );
            } else {

                if (args[0].equalsIgnoreCase("version")) {
                    sender.sendMessage(PREFIX+"バージョンは§e"+getDescription().getVersion()+"§rにゃ！");

                } else if (args[0].equalsIgnoreCase("show-pw") && Nyan10Utils.pCheck(sender, "nyan10utils.showpwpolicy")) {
                	if (args.length < 2) {
                		sender.sendMessage(PREFIX+"現在の設定：§e"+(showPassword?"on§7(表示)":"off§7(非表示)"));
                	} else {
                		if (sender instanceof ConsoleCommandSender) {
	                		showPassword = args[1].equals("on");
	                		sender.sendMessage(PREFIX+"パスワード表示を§e"+(showPassword?"on§7(表示)":"off§7(非表示)")+"§fに設定したにゃ！");
	                		config.set("database.show-password", showPassword);
	                		saveConfig();
                		} else {
                			sender.sendMessage(PREFIX+"§cこの設定はコンソールからしか変更できないにゃ(*- -)(*_ _)ペコリ");
                		}
                	}
                	
                } else if (args[0].equalsIgnoreCase("reload") && Nyan10Utils.pCheck(sender, "nyan10utils.reload")) {
                	loadConfig();
                	sender.sendMessage(PREFIX+"§a設定ファイルを再読み込みしたにゃ！");

                } else if (args[0].equalsIgnoreCase("register-db") && Nyan10Utils.pCheck(sender, "nyan10utils.regdb")) {
                	if (args.length < 3) {
                		sender.sendMessage(PREFIX+"§c/nyan10utils register-db <登録名> <URL> [USER] [PW]");
                		return true;
                	}
                	Database db = new Database(args[2], args.length>3?args[3]:"", args.length>4?args[4]:"");
                	try(Connection conn = db.getConnection()) {
                		Database.register(args[1], db);
                		sender.sendMessage(PREFIX+"§aデータベースを登録したにゃ！(  ´∀｀)bｸﾞｯ!");
                	} catch(SQLException e) {
                		sender.sendMessage(PREFIX+"§cデータベースに接続できなかったのにゃ( ﾉД`)ｼｸｼｸ…");
                		e.printStackTrace();
                	}

                } else if (args[0].equalsIgnoreCase("get-db") && Nyan10Utils.pCheck(sender, "nyan10utils.getdb")) {
            		if (args.length < 2) {
            			StringBuilder builder = new StringBuilder(PREFIX+"登録されているデータベース一覧にゃ！");
                    	for(Entry<String, Database> db : Database.getAll().entrySet()) {
                    		builder.append("\n§e").append(db.getKey()).append("§f: §a").append(db.getValue().getUrl());
                    		if (db.getValue().getAliveMonitoring() > 0)
                    			builder.append("§f(").append(db.getValue().getLastResponseTime()).append("ms)");
                    	}
                    	sender.sendMessage(builder.toString());
            			return true;
            		}
                	Database db = Database.get(args[1]);
                	if (db != null) {
	                	sender.sendMessage(
	                			PREFIX+"データベース§e"+args[1]+"§fの設定にゃ！\n"+
			                	PREFIX+"§bURL: §f"+db.getUrl()+"\n"+
			                	PREFIX+"§bUser: §f"+db.getUsername()+"\n"+
			                	PREFIX+"§bPassword: §f"+(showPassword ? db.getPassword() : "********")+"\n"+
			                	PREFIX+"§b死活監視: "+(db.getAliveMonitoring()>0 ? "§a"+db.getAliveMonitoring()+"秒ごと" : "§c無効")+"\n"+
			                	PREFIX+"§b応答速度: "+(db.getLastPingedTime()==-1 ? "§c利用不可" : "§a"+db.getLastResponseTime()+"ms"+"§7("+LocalDateTime.ofEpochSecond(db.getLastPingedTime()/1000, 0, OffsetDateTime.now().getOffset())+")")
	                	);
                	} else {
                		sender.sendMessage(PREFIX+"§cデータベース§e"+args[1]+"§cが見つからなかったにゃ(´・ω・`)");
                	}

                } else if (args[0].equalsIgnoreCase("exec-db") && Nyan10Utils.pCheck(sender, "nyan10utils.execdb")) {
                	if (args.length < 3) {
            			sender.sendMessage(PREFIX+"§c/nyan10utils exec-db <登録名> <SQL文>");
            			return true;
            		}
                	if (enableExecCmd) {
                		if (Database.has(args[1])) {
                			StringBuilder builder = new StringBuilder();
                			for(int i=2; i<args.length; i++)
                				builder.append(args[i]).append(" ");
                			try(Statement stmt = Database.get(args[1]).createStatement()) {
                				String sql = builder.toString();
                				sender.sendMessage(PREFIX+sql);
                				if (stmt.execute(sql))
                					sender.sendMessage(resultSetToTable(stmt.getResultSet()));
                				else
                					sender.sendMessage(PREFIX+"更新行数: "+stmt.getUpdateCount());
                			} catch(Exception e) {
                				sender.sendMessage(PREFIX+"§c"+e.getClass().getSimpleName()+": "+e.getMessage());
                				e.printStackTrace();
                			}
                		} else {
                			sender.sendMessage(PREFIX+"§cデータベース§e"+args[1]+"§cが見つからなかったにゃ...");
                		}
                	} else {
                		sender.sendMessage(PREFIX+"§cこのコマンドは設定で無効にされてるにゃ！config.ymlで変更できるにゃ！");
                	}

                } else if (args[0].equalsIgnoreCase("rename-db") && Nyan10Utils.pCheck(sender, "nyan10utils.rendb")) {
                	if (args.length < 3) {
                		sender.sendMessage(PREFIX+"§c/nyan10utils rename-db <登録名> <新しい登録名>");
                		return true;
                	}
                	Database db = Database.get(args[1]);
                	if (db == null) {
                		sender.sendMessage(PREFIX+"§cデータベース§e"+args[1]+"§cが見つからなかったにゃ(´・ω・`)");
                	} else {
                		Database.unregister(args[1]);
                		Database.register(args[2], db);
                		sender.sendMessage(PREFIX+"§a登録名を変更したにゃ！§e("+args[1]+"->"+args[2]+")");
                	}
                	
                } else if (args[0].equalsIgnoreCase("remove-db") && Nyan10Utils.pCheck(sender, "nyan10utils.rmdb")) {
                	if (args.length < 2) {
                		sender.sendMessage(PREFIX+"§c/nyan10utils remove-db <登録名>");
                		return true;
                	}
                	if (Database.unregister(args[1]) != null)
                		sender.sendMessage(PREFIX+"§e"+args[1]+"§aの登録を解除したにゃ！");
                	else
                		sender.sendMessage(PREFIX+"§e"+args[1]+"§cが見つからなかったにゃ(´・ω・`)");
                	
                } else if (args[0].equalsIgnoreCase("ping-db") && Nyan10Utils.pCheck(sender, "nyan10utils.pingdb")) {
                	if (args.length < 2) {
                		sender.sendMessage(PREFIX+"§c/nyan10utils ping-db <DB名>");
                		return true;
                	}
                	Database db = Database.get(args[1]);
                	if (db == null) {
                		sender.sendMessage(PREFIX+"§cデータベース§e"+args[1]+"§cが見つからなかったにゃ(´・ω・`)");
                	} else {
                		new Thread(() -> {
                			db.ping();
                			if (db.getLastResponseTime() == -1)
                				sender.sendMessage(PREFIX+"§cデータベース§e"+args[1]+"§cに接続できなかったにゃ...");
                			else
                				sender.sendMessage(PREFIX+"§aデータベース§e"+args[1]+"§aの応答速度: §e"+db.getLastResponseTime()+"§ams");
                		}).start();
                	}
                	
                	
                } else {
                	sender.sendMessage(PREFIX+"§c使い方が間違ってるみたいにゃ.../nyan10utilsで使い方表示にゃ！");
                }

            }
        }
        return true;
    }
    
    
    /**
     * Nyan10Utilsのコンフィグを再読み込みするにゃ！
     * @since 1.2
     */
    public void loadConfig() {
    	saveDefaultConfig();
    	reloadConfig();
        config = getConfig();
        showPassword = config.getBoolean("database.show-password", false);
        enableExecCmd = config.getBoolean("database.enable-exec-cmd", false);
        Database.load();
    }


    private static String resultSetToTable(ResultSet result) throws SQLException {
    	ResultSetMetaData meta = result.getMetaData();
    	int x=meta.getColumnCount();

    	String[] columnNames = new String[x];
    	int[] width = new int[x];
    	for(int i=0; i<x; i++) {
    		columnNames[i] = meta.getColumnName(i+1)+" "+meta.getColumnTypeName(i+1);
    		width[i] = columnNames[i].length();
    	}

    	List<String[]> rows = resultSetToRows(result);
    	int y=rows.size();

    	for(String[] row : rows) {
    		for(int i=0; i<x; i++) {
    			if (row[i] != null && row[i].length() > width[i])
    				width[i] = row[i].length();
    		}
    	}

    	String[] format = new String[x];
    	for(int i=0; i<x; i++)
    		format[i] = "%-"+width[i]+"s|";

    	StringBuilder table = new StringBuilder("§d");
    	for(int i=0; i<x; i++)
    		table.append(String.format(format[i], columnNames[i]));
    	table.append("\n");
    	for(int i=0; i<y; i++) {
    		String[] row = rows.get(i);
    		table.append(i%2==0 ? "§a" : "§b");
    		for(int j=0; j<x; j++)
    			table.append(String.format(format[j], row[j]));
    		table.append("\n");
    	}
    	return table.toString();
    }


    /**
     * Nyan10Utilsのイベントリスナを返すにゃー。
     * 内部で使ってるだけにゃので使う場面は少ないはずにゃ...
     * @return Nyan10Utilsのイベントリスナ
     * @since 1.0
     */
    public EventListener getEventListener() {
        return listener;
    }


    /**
     * 権限のチェックをして、権限がなかった場合はメッセージを送信するにゃ！
     * OP権限を持っている場合、常にtrueが返ってくるにゃ！
     * @param target 対象のプレーヤー
     * @param permission 権限名
     * @return 権限を持っていた場合true, 持っていなかった場合false
     * @since 1.0
     * @see #pCheck(CommandSender, String)
     */
    public static boolean pCheck(Player target, String permission) {
        if (target.hasPermission(permission)) return true;
        target.sendMessage(PREFIX+"§cけっ、権限が足りないにこっ(´・ω・`)§f ("+permission+")");
        return false;
    }


    /**
     * 権限のチェックをして、権限がなかった場合はメッセージを送信するにゃ！
     * OP権限を持っているかコンソールからの場合、常にtrueが返ってくるにゃ！
     * @param target 対象のコマンド送信者
     * @param permission 権限名
     * @return 権限を持っているかコンソールの場合true, 持っていなかった場合false
     * @since 1.1
     * @see #pCheck(Player, String)
     */
    public static boolean pCheck(CommandSender target, String permission) {
    	if (target instanceof ConsoleCommandSender) return true;
    	return pCheck((Player)target, permission);
    }



    /**
     * 権限のチェックをして、権限がなかった場合はメッセージを送信するにゃ！
     * その際、OP権限は無視されるにゃ！
     * @param target 対象のプレーヤー
     * @param permission 権限名
     * @return 権限を持っていた場合true, 持っていなかった場合false
     * @since 1.0
     * @see #pCheck(Player, String)
     */
    /*
    public static boolean pCheckIgnoreOp(Player target, String permission) {
        if (permMgr.has(target, permission)) return true;
        target.sendMessage(PREFIX+"§cけっ、権限が足りないにこっ(´・ω・`)§6 ("+permission+")");
        return false;
    }
    */


    /**
     * ResultSetを行ごとのリストに変換するにゃ！
     * メソッド名が分かりづらかったので変更したにゃ！後方互換性のために前のもあるだけにゃので、
     * {@link #resultSetToRows(ResultSet)}を使って欲しいにゃー...
     * @param result 変換するResultSet
     * @return 行ごとのリスト
     * @throws SQLException
     * @since 1.0
     */
    @Deprecated
    public static List<String[]> linesFromResult(ResultSet result) throws SQLException {
        return resultSetToRows(result);
    }


    /**
     * ResultSetを行ごとのリストに変換するにゃ！
     * {@link #linesFromResult(ResultSet)}と同じにゃ...
     * @param result 変換するResultSet
     * @return 行ごとのリスト
     * @throws SQLException
     * @since 1.1
     */
    public static List<String[]> resultSetToRows(ResultSet result) throws SQLException {
    	List<String[]> list = new ArrayList<String[]>();
        int nColumns = result.getMetaData().getColumnCount();
        while(result.next()) {
            String[] line = new String[nColumns];
            for(int cnt=0; cnt<nColumns; cnt++)
                line[cnt] = result.getString(cnt+1);
            list.add(line);
        }
        return list;
    }


    /**
     * サーバーにいるOP権限を持っているプレーヤー全員にメッセージを送信するにゃ！
     * @param msg メッセージ
     * @since 1.0
     */
    public static void broadcastOp(String msg) {
        for(Player player : Bukkit.getOnlinePlayers())
            if (player.isOp()) player.sendMessage(msg);
    }


    /**
     * 表示名もしくはアイテム名を返すにゃー。
     * @param item
     * @return 表示名もしくはアイテム名
     * @since 1.0
     */
    public static String getItemName(ItemStack item) {
        String itemName = item.getItemMeta().getDisplayName();
        if (itemName==null)
            itemName = item.getType().name();
        return itemName;
    }


    /**
     * 設定されたDisplayName、Loreでアイテムを作成するにゃ！
     * @param material アイテムの素材
     * @param name アイテムの表示名
     * @param lore Lore(可変長)
     * @return 作成したアイテム
     * @since 1.0
     * @see #createItem(Material, int, String, String...)
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }


    /**
     * 設定されたダメージ値、DisplayName、Loreでアイテムを作成するにゃ！
     * @param material アイテムの素材
     * @param damage ダメージ値
     * @param name アイテムの表示名
     * @param lore Lore(可変長)
     * @return 作成したアイテム
     * @since 1.0
     * @see #createItem(Material, String, String...)
     */
    public static ItemStack createItem(Material material, int damage, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        item.setDurability((short) damage);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }


    /**
     * アイテムにLoreを追加するにゃ！
     * @param item 追加するアイテム
     * @param lore 追加する文字列(可変長)
     * @since 1.0
     */
    public static void addLore(ItemStack item, String... newLore) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
        lore.addAll(Arrays.asList(newLore));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }


    /**
     * アイテムの名前を変更するにゃ。
     * @return 変更されたアイテム
     * @since 1.0
     */
    public static ItemStack rename(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }


    /**
     * 表示されないエンチャントを付与するにゃ。
     * キラキラさせるのにどぞ！( ^^) _旦~~
     * エンチャントがすでに付与されているアイテムに使用すると既存のエンチャントが消える可能性があります。
     * @param item きらきらさせるアイテム
     * @since 1.0
     * @see #clearEnchants(ItemStack)
     */
    public static void makeShine(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }


    /**
     * アイテムに付与されているすべてのエンチャントを削除するにゃ！
     * @param item アイテム
     * @since 1.0
     * @see #makeShine(ItemStack)
     */
    public static void clearEnchants(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchants()) return;
        for(Enchantment tmp : meta.getEnchants().keySet())
            meta.removeEnchant(tmp);
        item.setItemMeta(meta);
    }


    /**
     * プレーヤーにitemを与えるにゃ！
     * でもでもインベントリに空きがなかったら、メッセージとともに地面にドロップするにゃー。
     * スレッドセーフ...であってほしいにゃw
     * @param player プレーヤー
     * @param item 与えるアイテム
     * @since 1.0
     */
    public static void giveItem(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            Bukkit.getScheduler().runTask(instance, () -> {
                player.getWorld().dropItem(player.getLocation(), item);
                player.sendMessage(PREFIX+"§c§lインベントリに空きがないので、地面にドロップしたのにゃ！");
            });
        } else {
            player.getInventory().addItem(item);
        }
    }


    /**
     * ２進接頭辞(無印, Ki, Mi, Gi, Ti, Pi, Ei, Zi, Yi)を付けた表現にして
     * 小数第２位までを返すにゃ！概数を出力する際などにどうぞにゃ！
     * @param d ２進接頭辞で表現する数値
     * @return dを２進接頭辞で表現した文字列
     * @since 1.0
     * @see #siPrefix(double)
     */
    public static String binaryPrefix(double d) {
        if (d>=1024.0)
            if (d>=1048576.0)
                if (d>=1073741824.0)
                    if (d>=1099511627776.0)
                        if (d>=1125899906842624.0)
                            if (d>=1152921504606846976.0)
                                if (d>=1180591620717411303424.0)
                                    if (d>=1208925819614629174706176.0)
                                        return String.format("%.2fYi", d/1208925819614629174706176.0);
                                    else
                                        return String.format("%.2fZi", d/1180591620717411303424.0);
                                else
                                    return String.format("%.2fEi", d/1152921504606846976.0);
                            else
                                return String.format("%.2fPi", d/1125899906842624.0);
                        else
                            return String.format("%.2fTi", d/1099511627776.0);
                    else
                        return String.format("%.2fGi", d/1073741824.0);
                else
                    return String.format("%.2fMi", d/1048576.0);
            else
                return String.format("%.2fKi", d/1024);
        else
            return String.format("%.2f", d);
    }


    /**
     * SI接頭辞(無印, K, M, G, T, P, E, Z, Y, Q, R)を付けた表現にして
     * 少数第２位までを返すにゃ！概数を出力する際などにどうぞにゃ！
     * @param d SI接頭辞で表現する値
     * @return dをSI接頭辞で表現した文字列
     * @since 1.0
     * @see #binaryPrefix(double)
     */
    public static String siPrefix(double d) {
        if (d>=1000.0)
            if (d>=1000000.0)
                if (d>=1000000000.0)
                    if (d>=1000000000000.0)
                        if (d>=1000000000000000.0)
                            if (d>=1000000000000000000.0)
                                if (d>=1000000000000000000000.0)
                                    if (d>=1000000000000000000000000.0)
                                        if (d>=1000000000000000000000000000.0)
                                            if (d>=1000000000000000000000000000000.0)
                                                return String.format("%.2fR", d/1000000000000000000000000000000.0);
                                            else
                                                return String.format("%.2fQ", d/1000000000000000000000000000.0);
                                        else
                                            return String.format("%.2fY", d/1000000000000000000000000.0);
                                    else
                                        return String.format("%.2fZ", d/1000000000000000000000.0);
                                else
                                    return String.format("%.2fE", d/1000000000000000000.0);
                            else
                                return String.format("%.2fP", d/1000000000000000.0);
                        else
                            return String.format("%.2fT", d/1000000000000.0);
                    else
                        return String.format("%.2fG", d/1000000000.0);
                else
                    return String.format("%.2fM", d/1000000.0);
            else
                return String.format("%.2fK", d/1000.0);
        else
            return String.format("%.2f", d);
    }


    public static Nyan10Utils getInstance() {
        return instance;
    }

}
