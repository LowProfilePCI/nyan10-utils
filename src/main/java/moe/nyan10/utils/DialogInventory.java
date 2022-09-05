package moe.nyan10.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import moe.nyan10.utils.internal.HookableInventory;

/**
 * アイテムにイベントをつけたインベントリを簡単に作れるにゃ！
 * 確認画面とかに使うと便利かもですにゃー！
 * インスタンス化には{@link Builder}を使ってにゃ！
 * @since 1.1
 */
public class DialogInventory extends HookableInventory {
	
	private DialogItem[] items;
	private String title;
	private int size;
	
	
	private DialogInventory(int size, String title, Player player, List<DialogItem> items) {
		super(player);
		
		this.size = size;
		this.title = title;
		this.items = new DialogItem[size];
		for(DialogItem item : items)
			this.items[item.slot] = item;
	}
	
	
	/**
	 * ダイアログにイベント無しアイテムを追加するにゃ！
	 * @param slot 追加するインデックス(0~)
	 * @param item 追加するアイテム
	 * @see #addItem(int, ItemStack, BiConsumer)
	 * @since 1.1
	 */
	public void addItem(int slot, ItemStack item) {
		addItem(slot, item, null);
	}
	
	
	/**
	 * ダイアログにイベント付きアイテムを追加するにゃ！
	 * @param slot 追加するインデックス(0~)
	 * @param item 追加するアイテム
	 * @param handler イベントハンドラ
	 * @see #addItem(int, ItemStack)
	 * @since 1.1
	 */
	public void addItem(int slot, ItemStack item, BiConsumer<DialogInventory, InventoryClickEvent> handler) {
		this.items[slot] = new DialogItem(slot, item, handler);
		this.inv.setItem(slot, item);
	}
	
	
	/**
	 * ダイアログのアイテムを削除するにゃ！
	 * @param slot 削除するインデックス(0~)
	 * @since 1.1
	 */
	public void removeItem(int slot) {
		this.items[slot] = null;
		this.inv.clear(slot);
	}
	
	
	@Override
	public void onClickInventory(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getSlotType() == SlotType.CONTAINER && e.getRawSlot() < size) {
			DialogItem item = items[e.getSlot()];
			if (item != null && item.handler != null)
				item.handler.accept(this, e);
		}
	}
	
	
	@Override
	protected Inventory createInventory() {
		Inventory inv = Bukkit.createInventory(player, size, title);
		for(DialogItem item : items)
			if (item != null)
				inv.setItem(item.slot, item.item);
		return inv;
	}
	
	
	/**
	 * ダイアログのタイトルを返すにゃ！
	 * @return ダイアログのタイトル
	 * @since 1.1
	 */
	public String getTitle() {
		return title;
	}
	
	
	
	
	/**
	 * ダイアログを構築するためのクラスにゃ！
	 * @since 1.1
	 */
	public static class Builder {
		
		private String title;
		private List<DialogItem> items = new ArrayList<>();
		private Player player;
		private int size;
		
		
		/**
		 * ダイアログを構築するビルダーを生成するにゃ！
		 * @param player ダイアログの所有者
		 * @param size ダイアログのサイズ(自動で9の倍数に整えられるにゃ！)
		 * @since 1,1
		 */
		public Builder(Player player, int size) {
			this.player = player;
			this.size = (size+8)/9*9;
		}
		
		
		/**
		 * インベントリにイベント無しのアイテムを追加するにゃ！
		 * @param slot 配置するスロット
		 * @param item 配置するアイテム
		 * @since 1.1
		 */
		public Builder addItem(int slot, ItemStack item) {
			if (slot < size)
				addItem(slot, item, null);
			return this;
		}
		
		
		/**
		 * インベントリにイベント付きアイテムを追加するにゃ！
		 * @param slot 配置するスロット
		 * @param item 配置するアイテム
		 * @param handler イベントハンドラー
		 * @since 1.1
		 */
		public Builder addItem(int slot, ItemStack item, BiConsumer<DialogInventory, InventoryClickEvent> handler) {
			items.add(new DialogItem(slot, item, handler));
			return this;
		}
		
		
		/**
		 * インベントリのタイトルを設定するにゃ！
		 * @param title タイトル
		 * @since 1.1
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}		
		
		
		/**
		 * DialogInventoryを生成するにゃ！
		 * @return DialogInventoryのインスタンス
		 * @since 1.1
		 */
		public DialogInventory build() {
			return new DialogInventory(size, (title==null)?"":title, player, items);
		}
		
	}
	
	
	
	
	/**
	 * アイテムとインデックスとイベントをまとめたクラスにゃ！
	 * @since 1.1
	 */
	private static class DialogItem {
		
		int slot;
		ItemStack item;
		BiConsumer<DialogInventory, InventoryClickEvent> handler;
		
		
		private DialogItem(int slot, ItemStack item, BiConsumer<DialogInventory, InventoryClickEvent> handler) {
			this.slot = slot;
			this.item = item;
			this.handler = handler;
		}
		
	}

}
