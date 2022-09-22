package moe.nyan10.utils.internal;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import moe.nyan10.utils.Nyan10Utils;

/**
 * @since 1.1
 */
public abstract class HookableInventory {
	
	boolean isReplacing;
	protected Player player;
	protected Inventory inv;
	
	
	/**
	 * @param player インベントリの所有者
	 * @since 1.1
	 */
	public HookableInventory(Player player) {
		this.player = player;
	}
	
	
	/**
	 * インベントリクリック時のイベントハンドラにゃ！
	 * @param e イベントの情報
	 * @since 1.1
	 */
	public abstract void onClickInventory(InventoryClickEvent e);
	
	
	/**
     * インベントリを開くにゃ！
     * @since 1.1
     */
	public final void open() {
		player.closeInventory();
		this.inv = createInventory();
		if (inv == null) return;
		Nyan10Utils.getInstance().getEventListener().hook(player, this);
		player.openInventory(inv);
	}
	
	
	/**
	 * {@link #open()}が呼ばれたときにインベントリを作成するために呼ばれるにゃ！
	 * @return 作成したインベントリ
	 * @since 1.1
	 */
	protected abstract Inventory createInventory();
	
	
	public final void close() {
		if (inv.getViewers().contains(player))
			player.closeInventory();
	}
	
	
	/**
	 * フックを解除しないまま、インベントリを切り替えるにゃ！
	 * @param inv 新しく開くインベントリ
	 * @since 1.1
	 */
	protected final void replace(Inventory inv) {
		isReplacing = true;
		this.inv = inv;
		player.openInventory(inv);
		isReplacing = false;
	}
	
	
	/**
	 * インベントリを所有してるプレーヤーを返すにゃ！
	 * @return 所有者
	 * @since 1.1
	 */
	public final Player getPlayer() {
		return player;
	}
	
	
	/**
	 * 現在開いているInventoryを返すにゃ！
	 * @return 現在のInventory
	 * @since 1.1
	 */
	public final Inventory getInventory() {
		return inv;
	}
	
}
