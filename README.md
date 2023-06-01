# MCTRPGManager
### マインクラフトでクトゥルフ神話TRPGのオンラインセッションを行うためのプラグイン
### ※Bedrock Editionでプレイする場合はコマンド補完が出来ません
### ※コマンドはCCBルールになっています

<br>

## インストール方法
### 1. Spigot(Paper)サーバーを用意
### 2. MCTRPGManagerをPluginsフォルダに配置
### 3. [PlayerNPC](https://www.spigotmc.org/resources/%E2%9C%85-api-player-npc-%E2%9C%85-1-17-1-19-4.93625/?__cf_chl_tk=pqgn_8Dr5oVtuqKJ5SmJThT9CD2nOn_EIwtTbUahuvk-1684402964-0-gaNycGzNEXs)プラグインをPluginsフォルダに配置

<br>

## 利用方法
### 1. キャラクターシートを作成
- [いあきゃら](https://iachara.com/)でキャラクターシートを作成
- キャラ出力 → すべての技能を出力・CCB を選択 → ココフォリア駒出力

### 2. キャラクターシートを登録する
- プラグインをインストール後、サーバーを起動して、起動完了したら停止する
- plugins/MCTRPGManager/pieces.jsonが作成されているのを確認
- pieces.jsonの最外の[　]内に先程コピーしたココフォリア駒を貼付
  - ※必ず「 , 」で区切って下さい

### 3. NPCを登録する
- NPCがいる場合も、全てキャラクターシートで登録する
  - PCと同様にpieces.jsonに貼り付ける
  - 同じステータスのNPCを複数使いたい場合でも、nameのみ変更してNPCの数だけ登録する
  - pieces.jsonにはPCを含めた登場人物の数だけ駒データが登録されることになる

### 4. セッションを開始する
- サーバーを起動し、/session start でセッションを開始する
  - セッションを開始するとcharacter_stat.jsonが作成されます
  - セッションを終了するとcharacter_stat.jsonは削除されます

### 5. キャラクターを設定する
- /registchar [プレイヤー名] [キャラクター名] でマイクラのプレイヤーとキャラクターを紐づけする
  - ※サーバーに参加しているオンラインプレイヤーでないと紐づけ出来ません
- /tnpc [ID] [キャラクター名] でNPCを適当なIDを決めて登録します
  - 登録後は、/tnpc summon ID で召喚、/tnpc remove ID で消去が出来ます

### （オプション）NPCのスキンを変更する
- character_stat.jsonを開く
- スキンを変更したいキャラクターのtex_valとtex_sigを変更します
  - [Mineskin](https://mineskin.org/)にアクセスして適用したいスキンのTexture ValueとTexture Signatureをそれぞれコピー&ペーストします
- サーバーにアクセスしてNPCを召喚し、スキンが変更されているか確認します

<br>

## コマンドリスト
### ＜ プレイヤー用コマンド ＞
### /dice [ (0-999)(d or D)(0-999) ]
- ダイスを振ります（例：/dice 1d10）

### /commanddice [ 技能名 ]
- 技能ダイスを振ります（例：/commanddice 目星）

### /hp [ 値 ]
- 自分と紐づけされたキャラクターのHPを変更します

### /mp [ 値 ]
- 自分と紐づけされたキャラクターのMPを変更します

### /san [ 値 ]
- 自分と紐づけされたキャラクターのSAN値を変更、もしくはSAN値チェックをします
- /san のみならSAN値チェック、後ろに数字を入力すればその値にSAN値を変更できます

### /giveskillbook
- 自分の技能と値の一覧の記載された本を獲得します


<br>

### ＜ OP専用コマンド ＞
### /session [ start or end ]
- セッションを開始、または終了します

### /registchar [ プレイヤー名 ] [ キャラクター名 ]
- プレイヤーとキャラクターの紐づけをします

### /removechar [ キャラクター名 ]
- プレイヤーとキャラクターの紐づけを解除します

### /updatestat [ キャラクター名 ] [ ステータス名 ] [ 技能名 もしくは 値(0-999) ] [ 値(0-999) ]
- キャラクターの技能やステータスの値を変更します

### /tnpc [ モード ] [ キャラクターID ] *[ キャラクター名 ]
- NPCを召喚します
- モード
  - register
    - IDを決めてNPCを登録します
  - summon
    - IDを指定して、登録済のNPCを自分と同じ位置に召喚します
  - remove
    - IDを指定して、召喚済のNPCを消去します

### /secretdice [ (0-999)(d or D)(0-999) ]
- シークレットダイスを振ります
- 他のプレイヤーには結果が表示されません
