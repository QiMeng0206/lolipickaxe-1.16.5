# lolipickaxe-1.16.5
適用於`MinecraftForge1.16.5-36.2.39--1.16.5-36.2.42`的Lolipickaxe-1.16.5

英文:[English](https://github.com/QiMeng0206/lolipickaxe-1.16.5/blob/main/README.md)簡體中文:[简体中文](https://github.com/QiMeng0206/lolipickaxe-1.16.5/blob/main/README.md)繁體中文:本頁面

本倉庫fork自https://github.com/gjdbg148525/lolipickaxe (原倉庫已停止維護)

### 計劃

1.~~將lolipickaxe繼續遷移至1.18.2以及更高版本且持續維護~~（長期計劃）

2.修復lolipickaxe1.16.5的bug

### 使用方法

前往[Release](https://github.com/QiMeng0206/lolipickaxe-1.16.5/releases/latest)下載最新正式版Lolipickaxe

將Lolipickaxe-1.16.5-1.0.jar放入mods文件夾中

啓動遊戲

### 從源碼構建

前往[Downloads for Minecraft Forge for Minecraft 1.16.5](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html)下載最新版1.16.5forgemdk

克隆本倉庫

```shell
git clone https://github.com/QiMeng0206/lolipickaxe-1.16.5.git
```

將lolipickaxe-1.16.5中的main複製到mdk的src文件夾中

用Eclipse IDE，IntelliJ IDEA或vscode打開1.16.5forgemdk

稍等一會後使用gradle/task/forgegradle runs/genIntellijRuns(以IDEA爲例)

執行成功後使用gradle/task/forgegradle runs/runClient
