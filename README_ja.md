# 説明

このプラグインはFuelPHPフレームワークをサポートします。

## 機能

- バッジアイコン表示 (完了)
- プロジェクトの新規作成からFuelPHPプロジェクトを作成(完了)
- 自動補完用のファイル作成(完了)
- アクション : ビューとアクションへの移動 (完了)

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

## 重要!
プロジェクトの新規作成からSDカードポートへのクローンは行わないでください!(ハングアップします...)

##ライセンス

CDDL-GPLv2 (see license file)