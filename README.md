**MessengerGWTJSNativeImpl**

**概要**
	GWTで構築したJSでのMessagingに対して、単純なJSから通信を行う試みです。
	下記のプロジェクトが元になっています。
	・MessengerSystem
		https://github.com/sassembla/MessengerSystem
		Objective-Cでのメッセージングをハンドルしやすいようにラップしたライブラリ
		
	・MessengerGWT
		https://github.com/sassembla/MessengerGWT
		Browser-Javascriptでのメッセージングをハンドルしやすいようにラップしたライブラリ

**目的**
	既存のJSライブラリから、Messagingでの他ライブラリとの通信を行う事。
	ライブラリ間の粗結合を実現するためのブリッジを、PureJSで作っている、という状態になります。


**現在の状態**
	試験中です。

	完成予定は未定。

	MessengerGWTからのメッセージを受け付けることは完了。
	MessengerGWTへとメッセージを送付することは完了。
	
