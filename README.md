# nyan10-utils
nyan10で使う機能や便利な機能を詰め込んだSpigot1.12.2用のライブラリ兼プラグインにゃ！
# 導入方法
サーバーのpluginsにダウンロードしたjarファイルを入れてサーバー再起動/リロードにゃ！
# 開発猫や開発者へ！
## ライブラリの追加方法
### mavenで依存関係に追加したいにゃ！ってねこさんへ！
```xml
  <repositories>
    <repository>
        <id>github</id>
        <url>https://raw.github.com/LowProfilePCI/nyan10-repo/main/</url>
    </repository>
  </repositories>
```
```xml
  <dependencies>
    <dependency>
      <groupId>moe.nyan10</groupId>
      <artifactId>nyan10-utils</artifactId>
      <version>1.0</version>
    </dependency>
  </dependencies>
```
#### ビルドツールを使いにゃ！ってねこさんへ！
クラスパスにjarファイルを追加して、開発するにゃ！

## すっごく適当に使い方を教えてあげるにゃ(⋈◍＞◡＜◍)。✧♡
#### たとえば、こんなアイテムを作る時はこうするにゃ！  
![image](https://user-images.githubusercontent.com/87465192/154571536-4aa5cdba-5e4e-4b20-9c83-4a78d34c0ce1.png)
```java
ItemStack item = Nyan10Utils.createItem(Material.RAW_FISH, "§b§lろーのごはん", "明日のお昼ご飯にゃ...", "近くのスーパーで198円だったにゃー
```

#### 他にも撮るの忘れちゃったので写真はにゃいですけど、アイテム一覧のインベントリを作ったりできるにゃ！
```java
Player player = (ほにゃらら);
List<ItemStack> items = new ArrayList<>();
items.add(Nyan10Utils.createItem(Material.RAW_FISH, "ろーの朝ごはん"));
items.add(Nyan10Utils.createItem(Material.RAW_FISH, "ろーの昼ごはん"));
items.add(Nyan10Utils.createItem(Material.RAW_FISH, "たくにゃんからもらったおやつ", "ハート型のチョコにゃ"));
items.add(Nyan10Utils.createItem(Material.RAW_FISH, "ろーの夜ごはん"));

ItemListInventory listinv = new ItemListInventory("ろーのご飯リスト", player, items);
listinv.setOnSelect(e -> {
  player.sendMessage("いただきますにゃ！！")
  Nyan10Utils.giveItem(player, e.getItem());
});
```
こんな感じで...動くはずにゃ...動いてにゃ...(人>ω•*)ｵﾈｶﾞｲ
