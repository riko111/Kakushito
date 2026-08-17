# かくしーと

PDFにマーカーを引いて、タップするとマーカー部分を隠せる**学習支援アプリ**です。

PDF教材にマーカーを引き、マーカー部分を隠して暗記・復習できる「自分で作る穴埋め問題」のような使い方を想定しています。

## Features

* PDFファイルの閲覧
* PDF上へのマーカー描画
* マーカー部分のマスク表示
* タップによるマーカーの表示・非表示
* PDFそのものを変更せず、オーバーレイとしてマーカーを管理
* マーカー情報のローカル保存
* 複数端末での利用・クラウド同期（Premium向け、予定）

## Project

「かくしーと」は Kotlin Multiplatform（KMP）を利用し、複数プラットフォームで共通のロジックを共有する構成を目指しています。

```text
kakushito/
├── README.md
├── docs/
│   └── coordinate-system.md
├── shared/
├── androidApp/
└── iosApp/
```

## Architecture

PDFの表示とマーカー情報を分離して管理します。

```text
PDF
 │
 │ 表示
 ↓
PDF Viewer
 │
 ├──────────────┐
 │              │
 ↓              ↓
PDF表示       Marker Overlay
               │
               ↓
          Marker Data
```

マーカーはPDFファイルそのものに書き込まず、PDFの上に重ねる透明なレイヤーとして管理します。

これにより、印刷禁止・読み取り専用など、PDF自体を編集できない場合でも、アプリ側でマーカーを付けられる設計とします。

### Coordinate System

マーカーの座標は**PDF座標を唯一の正規座標系**として扱います。

画面サイズ、ズーム倍率、スクロール位置などの表示状態は保存データに含めず、表示時にPDF座標から画面座標へ変換します。

詳細は以下を参照してください。

* [PDF座標系・画面座標系変換仕様](docs/coordinate-system.md)

基本的なデータフローは以下のとおりです。

```text
ユーザー操作
    ↓
画面座標
    ↓
Screen → PDF変換
    ↓
PDF座標
    ↓
保存
```

表示時は逆方向に変換します。

```text
保存されたマーカー
    ↓
PDF座標
    ↓
PDF → Screen変換
    ↓
画面上に描画
```

この方式により、ズームや画面回転、端末サイズの違いがあっても、マーカーをPDF上の同じ位置に表示できます。

## Data

マーカーなどのアプリ固有データは、PDFファイルとは独立して管理します。

概念的には以下のような構造を想定しています。

```text
Document
├── PDF
└── Annotations
    ├── Marker
    ├── Mask
    └── ...
```

PDFファイルを変更するのではなく、PDFを参照する形でアノテーション情報を管理します。

## Development

### Requirements

開発環境の詳細は、各プラットフォームおよびKotlin Multiplatformの構成に応じて整備していきます。

主な開発環境：

* Kotlin
* Kotlin Multiplatform
* Android
* iOS

## Documentation

詳細な設計・仕様は `docs/` 以下にまとめます。

* [PDF座標系・画面座標系変換仕様](docs/coordinate-system.md)

今後、以下のような仕様を追加予定です。

* PDF表示仕様
* マーカー仕様
* マスク仕様
* データモデル仕様
* ファイル保存仕様
* 同期仕様
* 課金・Premium機能仕様

## License

TBD
