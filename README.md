# nyan10-utils
nyan10で使う機能や便利な機能を詰め込んだSpigot用のライブラリ兼プラグインにゃ！
## 導入方法
サーバーのpluginsにダウンロードしたjarファイルを入れてサーバー再起動/リロードにゃ！
## 開発猫や開発者へ！
#### mavenで依存関係に追加したいにゃ！ってねこさんへ！
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
