# 説明

このプラグインはFuelPHPフレームワークをサポートします。

## 機能

- バッジアイコン表示
- プロジェクトの新規作成からFuelPHPプロジェクトを作成
- 自動補完用のファイル作成
- アクション : ビューとアクションへの移動
- カスタムfuelディレクトリ名
- Viewパスの補完
- MVCノード
- PHPUnitを実行するためのサポート
- ハイパーリンクナビゲーション
- コマンドを実行
- Generateアクション

## インストール

次のどちらかで行なって下さい。
- このプロジェクトをクローンして手動でビルド＆インストール
- NBMファイル(http://plugins.netbeans.org/plugin/44665/php-fuelphp-framework)をダウンロードして、インストール

## 自動コード補完

プロジェクトメニュー > FuelPHP > 自動補完用のファイルの作成 から自動補完用のファイルを作成します。
nbprojectフォルダに追加されます。

c.f.
- https://gist.github.com/2364280 (kenjis)
- https://gist.github.com/4094832 (wate)

新規プロジェクトからプロジェクトを作成した場合、自動的に追加します。

## Go to view / action Action

### Go to view

- Controllerファイルのアクションメソッドの中で右クリック
- ナビゲート
- ビューへ移動

### Go to action

- ViewファイルもしくはViewModelファイルのなかで右クリック
- ナビゲート
- アクションへ移動

## カスタムfuelディレクトリ名

"fuel"の代わりに別のディレクトリ名"myfuel"を使う場合は次のように設定してください。

1. プロジェクトプロパティ > フレームワーク > FuelPHP
2. fuel name : fuelディレクトリ名 (e.g. myfuel)

## Viewパスの補完
Viewパスの補完をサポートします
e.g.

```php
View::forge('[Ctrl+Space]');
View::forge('welcome/[Ctrl+Space]');
ViewModel::forge('[Ctrl+Space]');
// popup directory and file names
```
もし、ディレクトリ名でエンターキーを押した場合は、もう一度[Ctrl+Space]（補完）を実行してください。そうすると、ディレクトリの子要素が表示されます。

## MVC,modulesノード
プロジェクトペインにMVC(model, views, controller),modulesノードを作成します。

```
myproject
├─source files
├─test files
├─important files
├─include path
├─controller (fuel/app/classes/controller)
├─model (fuel/app/classes/model)
├─views (fuel/app/views)
├─modules (fuel/app/modules)
```
ソース・ファイルでMVCディレクトリを非表示にしたい場合は、
`プロジェクトプロパティ > フレームワーク > FuelPHP`に移動して"ソース・ファイルの中でMVCを無視する"にチェックを入れてください。

もし無視するにチェックを入れた場合は、MVCノードで新規ファイルを作成したときにソース・ファイルのノードに移動することはありません。
チェックを入れてない場合は、ソース・ファイルに移動します。

**注意：保存時にリモートアップロードを行う場合は、これらのノードを無視する設定はできません**

## PHPUnit Test Init Action
Project右クリック > FuelPHP > PHPUnit Test Init

このアクションはNetBeansでPHPUnitを実行するのに必要なファイルを作成し、それらを設定します。

## Create Test Action
ファイルノードもしくはエディタ上のコンテキストメニューからテストケースファイルを作成することができます。

## ハイパーリンクナビゲーション
この機能はGo To Action と似ています。

1. Ctrlキーを押したまま
2. テキストの色が青に変わるまで待つ
3. クリック
4. ファイルに移動

e.g.
```php
// 'bootstrap.css'の上にカーソルを移動
// 1. ... 4.
Asset::css('bootstrap.css');
```
これは次のメソッドで有効です。

### Asset Class
- img
- css
- js

### View, ViewModel Class
- forge

### 自動的にファイルを作成するためのオプション
自動ファイル作成のための設定も使用することができます。
`プロジェクトプロパティ > フレームワーク > FuelPHP > create a file ...`　にチェックを入れて下さい。

このオプションが有効であれば、ターゲットファイルが存在しない場合に空のファイルを作成することができます。

## コマンドを実行
IDEでoilコマンドを実行することができます。

プロジェクトを右クリック > FuelPHP > コマンドを実行 または、ショートカットキー `Alt + Shift + X`

## Generate
GUIで`oil generate`コマンドを実行することができます。

プロジェクトを右クリック > FuelPHP > Generate

## 重要!
プロジェクトの新規作成からSDカードポートへのクローンは行わないでください!(ハングアップします...)

##ライセンス

CDDL-GPLv2 (see license file)