# 説明

このプラグインはFuelPHPフレームワークをサポートします。

## 機能

- バッジアイコン表示 (完了)
- プロジェクトの新規作成からFuelPHPプロジェクトを作成(完了)
- 自動補完用のファイル作成(完了)
- アクション : ビューとアクションへの移動 (完了)
- カスタムfuelディレクトリ名
- Viewパスの補完
- MVCノード

## インストール

次のどちらかで行なって下さい。
- このプロジェクトをクローンして手動でビルド＆インストール
- NBMファイル(https://github.com/junichi11/fuelphp-netbeans/downloads)をダウンロードして、インストール

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

## MVCノード
プロジェクトペインにMVC(model, views, controller)ノードを作成します。

```
myproject
├─controller (fuel/app/classes/controller)
├─model (fuel/app/classes/model)
├─views (fuel/app/views)
├─source files
├─test files
├─important files
├─include path
```
ソース・ファイルでMVCディレクトリを非表示にしたい場合は、
`プロジェクトプロパティ > フレームワーク > FuelPHP`に移動して"ソース・ファイルの中でMVCを無視する"にチェックを入れてください。

もし無視するにチェックを入れた場合は、MVCノードで新規ファイルを作成したときにソース・ファイルのノードに移動することはありません。
チェックを入れてない場合は、ソース・ファイルに移動します。

## 重要!
プロジェクトの新規作成からSDカードポートへのクローンは行わないでください!(ハングアップします...)

##ライセンス

CDDL-GPLv2 (see license file)