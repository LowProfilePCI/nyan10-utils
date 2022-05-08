package moe.nyan10.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

    private static final String PREFIX = "§7§l[§dNyan10§aUtils§7§l]§r ";
    private static Nyan10Utils instance;
    private EventListener listener = new EventListener();
    //private static PermissionManager permMgr = PermissionsEx.getPermissionManager();


    @Override
    public void onEnable() {
        instance = this;

        getServer().getPluginManager().registerEvents(listener, this);
    }


    @Override
    public void onDisable() {

    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nyan10utils")) {
            if (args.length == 0) {
                sender.sendMessage(PREFIX+"§6/nyan10utils version: バージョンを表示するにゃ！");

            } else {

                if (args[0].equalsIgnoreCase("version")) {
                    sender.sendMessage(PREFIX+"バージョンは§e"+getDescription().getVersion()+"§rにゃ！");

                }

            }
        }
        return true;
    }


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
     * @see Nyan10Utils#pCheck(Player, String)
     */
    public static boolean pCheck(Player target, String permission) {
        if (target.hasPermission(permission)) return true;
        target.sendMessage(PREFIX+"§cけっ、権限が足りないにこっ(´・ω・`)§f ("+permission+")");
        return false;
    }



    /**
     * 権限のチェックをして、権限がなかった場合はメッセージを送信するにゃ！
     * その際、OP権限は無視されるにゃ！
     * @param target 対象のプレーヤー
     * @param permission 権限名
     * @return 権限を持っていた場合true, 持っていなかった場合false
     * @since 1.0
     * @see Nyan10Utils#pCheck(Player, String)
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
     * @param result 変換するResultSet
     * @return 行ごとのリスト
     * @throws SQLException
     * @since 1.0
     */
    public static List<String[]> linesFromResult(ResultSet result) throws SQLException {
        List<String[]> list = new ArrayList<String[]>();
        int nColumns = result.getMetaData().getColumnCount();
        while(result.next()) {
            String[] line = new String[nColumns];
            for(int cnt=1; cnt<=nColumns; cnt++)
                line[cnt] = result.getString(cnt);
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
     * @see Nyan10Utils#createItem(Material, int, String, String...)
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
     * @see Nyan10Utils#createItem(Material, String, String...)
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
     * @see Nyan10Utils#clearEnchants(ItemStack)
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
     * @see Nyan10Utils#makeShine(ItemStack)
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
     * @see Nyan10Utils#siPrefix(double)
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
     * @see Nyan10Utils#binaryPrefix(double)
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


    /*
    public static void main(String[] args) {
        System.out.println(binaryPrefix(1));
        System.out.println(binaryPrefix(1024.0));
        System.out.println(binaryPrefix(1048576.0));
        System.out.println(binaryPrefix(1073741824.0));
        System.out.println(binaryPrefix(1099511627776.0));
        System.out.println(binaryPrefix(1125899906842624.0));
        System.out.println(binaryPrefix(1152921504606846976.0));
        System.out.println(binaryPrefix(1180591620717411303424.0));
        System.out.println(binaryPrefix(1208925819614629174706176.0));
        System.out.println(siPrefix(1.0));
        System.out.println(siPrefix(1000.0));
        System.out.println(siPrefix(1000000.0));
        System.out.println(siPrefix(1000000000.0));
        System.out.println(siPrefix(1000000000000.0));
        System.out.println(siPrefix(1000000000000000.0));
        System.out.println(siPrefix(1000000000000000000.0));
        System.out.println(siPrefix(1000000000000000000000.0));
        System.out.println(siPrefix(1000000000000000000000000.0));
        System.out.println(siPrefix(1000000000000000000000000000.0));
        System.out.println(siPrefix(1000000000000000000000000000000.0));
    }
    */

}
