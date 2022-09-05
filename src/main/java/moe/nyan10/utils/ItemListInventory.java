package moe.nyan10.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import moe.nyan10.utils.internal.HookableInventory;

/**
 * アイテム一覧インベントリを表示して、アイテム選択時に特定の処理を実行する便利なやつにゃ！！
 * @since 1.0
 */
public class ItemListInventory extends HookableInventory {

    private List<ItemStack> items = new ArrayList<>();
    private int page;
    private int nPages;
    private String title;
    private Consumer<ItemSelectEvent> handler = item -> {};


    /**
     *
     * @param title インベントリのタイトル
     * @param player インベントリを開くプレーヤー
     * @param origList アイテムリスト
     * @since 1.1
     */
    public ItemListInventory(String title, Player player, Collection<ItemStack> origList) {
    	super(player);
    	this.title = title;

        items.addAll(origList);
        int nItems = items.size();
        nPages = nItems/45 + ((nItems%45==0) ? 0 : 1);
    }


    /**
     * アイテム選択時の処理を設定するにゃ！
     * @param handler 選択時に実行する処理
     * @since 1.0
     */
    public void setOnSelect(Consumer<ItemSelectEvent> handler) {
        this.handler = handler;
    }


    /**
     * 次のページを開くにゃ！
     * ページ範囲外の場合は最後のページにクランプされるにゃ！
     * @see ItemListInventory#openPrevPage()
     * @see ItemListInventory#openAt(int)
     * @since 1.0
     */
    public void openNextPage() {
        page = Math.min(page+1, nPages-1);
        this.replace(createInventory());
    }


    /**
     * 前のページを開くにゃ！
     * ページ範囲外の場合は最初のページにクランプされるにゃ！
     * @see ItemListInventory#openNextPage()
     * @see ItemListInventory#openAt(int)
     * @since 1.0
     */
    public void openPrevPage() {
        page = Math.max(page-1, 0);
        this.replace(createInventory());
    }


    /**
     * 指定したページを開くにゃ！
     * ページ範囲外の場合はクランプされるにゃ！
     * @param page ページ
     * @see ItemListInventory#openNextPage()
     * @see ItemListInventory#openPrevPage()
     * @since 1.0
     */
    public void openAt(int page) {
        this.page = Math.min(Math.max(page, 0), nPages-1);
        this.replace(createInventory());
    }


    /**
     * 現在のページのインベントリを生成するにゃ！
     * @since 1.1
     */
    protected Inventory createInventory() {
        if (page < 0 || page >= nPages) return null;
        String pagePrefix = "§8(§e§l"+(page+1)+"§8/"+nPages+") §r";
        Inventory inv = Bukkit.createInventory(null, 54, pagePrefix+title);

        final int OFFSET = page*45;
        final int LIMIT = Math.min(45-((OFFSET+45)-items.size()), 45);
        for(int i=0; i<LIMIT; i++)
            inv.setItem(i, items.get(i+OFFSET));

        if (page > 0)
            inv.setItem(45, Nyan10Utils.createItem(Material.STAINED_GLASS_PANE, 6, "§c前のページ", "§e"+(page)+"§7/"+nPages));
        int min = 0;
        for(int i=0; i<7; i++) {
            int max = (int)(((nPages-1)/6.0)*(i+1));
            inv.setItem(i+46, Nyan10Utils.createItem(Material.STAINED_GLASS_PANE, (min <= page && max > page) ? 3 : 7, "§e"+(min+1)+(i==6?"":"§7-§e"+(max+1))+"§7ページ", "§e"+(min+1)+"§7/"+nPages));
            min = max;
        }

        if (page < nPages-1)
            inv.setItem(53, Nyan10Utils.createItem(Material.STAINED_GLASS_PANE, 5, "§c次のページ", "§e"+(page+2)+"§7/"+nPages));
        
        return inv;
    }


    /**
     * 非推奨にゃ。今開いてるページの特定のスロットのアイテムを
     * 選択して選択時の処理を実行するにゃ！
     * @param slot スロット
     * @param type クリックタイプ
     * @since 1.0
     */
    @Deprecated
    public void select(int slot, ClickType type) {
        int index = (page*45)+slot;
        if (index<0 || slot>items.size()) return;
        ItemStack item = items.get(index);
        ItemSelectEvent event = new ItemSelectEvent(item, index, type);
        handler.accept(event);
        close();
    }


    /**
     * リストにアイテムを追加するにゃ！
     * @param item 追加するアイテム
     * @since 1.0
     */
    public void addItem(ItemStack item) {
        items.add(item);
        nPages = page/45 +((page%45==0) ? 0 : 1);
    }


    /**
     * リストからアイテムを削除するにゃ！
     * @param item 削除するアイテム
     * @see ItemListInventory#removeItem(int)
     * @since 1.0
     */
    public void removeItem(ItemStack item) {
        items.remove(item);
    }


    /**
     * リストからアイテムをインデックスで指定して閉じるにゃ！
     * @param index 削除するアイテムのインデックス
     * @see ItemListInventory#removeItem(ItemStack)
     * @since 1.0
     */
    public void removeItem(int index) {
        items.remove(index);
    }


    /**
     * 現在のアイテムリストを返すにゃ～！
     * @return アイテムリスト
     * @since 1.0
     */
    public List<ItemStack> getItemList() {
        return items;
    }


    /**
     * 現在のページ番号を返すにゃ！
     * @return 0を含むページ番号
     * @since 1.0
     */
    public int getPage() {
        return page;
    }


    /**
     * 総ページ数を返すにゃ！
     * @return 総ページ数
     * @since 1.0
     */
    public int getNPages() {
        return nPages;
    }
    
    
    /**
     * @since 1.1
     */
    @Override
    public void onClickInventory(InventoryClickEvent e) {
    	e.setCancelled(true);
    	
    	if (e.getSlotType() == SlotType.CONTAINER && e.getCurrentItem().getType() != Material.AIR) {
	    	int slot = e.getRawSlot();
	    	
	        if (slot >= 0 && slot < 45) {
	            select(slot, e.getClick());
	        } else if (slot == 45) {
	            openPrevPage();
	        } else if (slot > 45 && slot < 53) {
	            openAt((int)(((getNPages()-1)/6.0)*(slot-46)));
	        } else if (slot == 53) {
	            openNextPage();
	        }
    	}
	}




    /**
     * アイテムが選択されたときのデータを入れるクラスにゃ！
     * @since 1.0
     */
    public static class ItemSelectEvent {

        private ItemStack item;
        private int index;
        private ClickType type;


        private ItemSelectEvent(ItemStack item, int index, ClickType type) {
            this.item = item;
            this.type = type;
            this.index = index;
        }


        /**
         * 選択されたアイテムを返すにゃ！
         * @return 選択されたアイテム
         * @since 1.0
         */
        public ItemStack getItem() {
            return item;
        }


        /**
         * 選択されたアイテムのインデックスを返すにゃ！
         * @return 選択されたアイテムのインデックス
         * @since 1.0
         */
        public int getIndex() {
            return index;
        }


        /**
         * 選択時のクリックの種類を返すにゃ！
         * @return クリックの種類
         * @since 1.0
         */
        public ClickType getType() {
            return type;
        }

    }

}
