package com.kissaki.client;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageMasterHub;
import com.kissaki.client.subFrame.debug.Debug;

public class TestMessengerGWTWithJS extends GWTTestCase implements MessengerGWTInterface {
	Debug debug;

	MessengerGWTWIthHype hype;
	MessengerGWTImplement messenger;
	MessageMasterHub currentAspectMaster;

	ReceiverClass rec;

	int TIME_LIMIT = 1000;
	int TIME_INTERVAL = 10;

	String TEST_MESSAGE_ID = "0";// 固定値

	String TEST_MYNAME = "hype";
	String TEST_COMMAND = "TEST_COMMAND";
	String TEST_RECEIVER_ID = "TEST_RECEIVER_ID";

	String TEST_TAG = "TEST_TAG";
	String TEST_VALUE = "TEST_VALUE";
	String TEST_RECEIVER = "TEST_RECEIVER";

	/**
	 * コンストラクタ
	 */
	public TestMessengerGWTWithJS() {
		debug = new Debug(this);
		debug.trace("constructed");
	}

	/**
	 * Must refer to a valid module that sources this class.
	 */
	public String getModuleName() {
		return "com.kissaki.MessengerGWTWithHype";// パッケージの中で、クライアント/サーバの前+プロジェクトプロジェクト名称(xmlでの読み出しが行われている箇所)
	}

	/**
	 * セットアップ
	 */
	public void gwtSetUp() {
		debug.trace("setup_" + this);
		hype = new MessengerGWTWIthHype();
		// hype.onModuleLoad();

		currentAspectMaster = MessageMasterHub.setUpMessengerAspectForTesting();

		rec = new ReceiverClass(TEST_RECEIVER);

		messenger = new MessengerGWTImplement(TEST_MYNAME, this);
	}

	/**
	 * ティアダウン
	 */
	public void gwtTearDown() {
		hype = null;

		currentAspectMaster.tearDownMessengerAspectForTesting();

		// レシーバ、必要であれば。
		rec.receiver.removeFromCurrentMessageAspect();
		rec.receiver = null;
		rec = null;

		messenger.removeFromCurrentMessageAspect();
		messenger = null;
		debug.trace("teardown");
	}

	/**
	 * JSNIで親への認定設定を送信するポスト機構のテスト messageMapで制作できる内容を模倣していく。
	 * 
	 * postMessageでメッセージを送る(最初は、inputParentの内容)
	 */
	public void testJSNIMessageInput() {

		// inputParentを模倣する。
		String messageMap = getInputParentContainor(TEST_MYNAME, messenger.getID(),
				TEST_RECEIVER);

		// オリジナル２　（詳細比較用）
		// getMessageStructure(MS_CATEGOLY_PARENTSEARCH, messageID,
		// getName(),getID(), inputName, "", "");
		String actual_messageMap = messenger.getMessageStructure(3,
				messenger.getID(), TEST_MYNAME, messenger.getID(), TEST_RECEIVER,
				"", "").toString();
		// assertEquals(actual_messageMap, messageMap);

		// オリジナル(比較用)
		JSONObject messageMap_org = messenger.getMessageStructure(3,
				"01234567", messenger.getName(), messenger.getID(),
				TEST_RECEIVER, "", "");
		String messageMap_org_str = messageMap_org.toString();

		// messenger.sendAsyncMessage(messageMap_org);
		sendMessageAsPostMessage(messageMap, Window.Location.getHref());

		// messenger.inputParent(TEST_RECEIVER);

		delayTestFinish(TIME_LIMIT);
		Timer timer = new Timer() {

			@Override
			public void run() {
				// 定期的に到着の判断を行う。レシーバーへの返信で良いと思う。
				if (rec.getMessengerForTesting().getReceiveLogSize() == 1) {
					cancel();
					finishTest();
				}
			}
		};
		timer.scheduleRepeating(TIME_INTERVAL);
	}

	/**
	 * 親設定が終わったあと、連絡を貰う
	 */
	public void testReceiveInput() {
		// inputParentを模倣する。
		String messageMap = getInputParentContainor(TEST_MYNAME, "01234567",
				TEST_RECEIVER);
		sendMessageAsPostMessage(messageMap, Window.Location.getHref());
		setReceiver(TEST_MYNAME, TEST_RECEIVER);
		
		
		// messenger.inputParent(TEST_RECEIVER);
		
		delayTestFinish(TIME_LIMIT);
		Timer timer = new Timer() {
			int i = 0;

			@Override
			public void run() {
				// 定期的に到着の判断を行う。レシーバーへの返信で良いと思う。
				switch (i) {
				case 0:
					if (rec.getMessengerForTesting().getReceiveLogSize() == 1) {

						// if (messenger.isReadyAsChild()) {//
						// 本物の解析、無事に受け取れている場合の確認
						// messenger.callParent("command",
						// messenger.tagValue("key", "value"));
						// }

						if (isReceived(TEST_MYNAME)) {
							cancel();
							finishTest();
						}
					}

					break;
				}

			}

		};
		timer.scheduleRepeating(TIME_INTERVAL);
	}

	
	/**
	 * 親子系の形成が完了していれば
	 * @param propertyName
	 * @return
	 */
	private native boolean isReceived(String propertyName) /*-{
		if (!window[propertyName]) return false;
		if (window[propertyName].slice(0,1) == "_")
			return true;
		return false;
	}-*/;
	
	/**
	 * 親からのメッセージを受け取っていたら、受け取ったメッセージを返す
	 * @param propertyName
	 * @return
	 */
	private native String isReceivedMessageFromParent (String propertyName) /*-{
		//さて、何を持って返答が来たと見なすか
		
		return null;
	}-*/;
	
	/**
	 * レシーバをセットする(特定の関数をセットし、値を保持する)
	 * 
	 * @param myself
	 * @param parent
	 */
	private native void setReceiver(String myself, String parent) /*-{
		
		window[myself] = myself;//create property
		window[myself+"_"+parent] = parent;
		
		//レシーバ
		var method = function(e) {
			var rootObject = JSON.parse(e.data);
			
			switch (rootObject.KEY_MESSAGE_CATEGOLY) {
				case 0://MS_CATEGOLY_LOCAL
				case 3://MS_CATEGOLY_PARENTSEARCH
				case 4:
					alert("何か来た");
					//何もしない
					break;
					
				case 1://MS_CATEGOLY_CALLCHILD
					alert("親からなんか来た");
					//自分の親だったら、受け取って何かする
					switch (rootObject.MESSENGER_messengerName) {
						
						case window[myself+"_"+parent]:
						if (window[myself+"_parentId"] == rootObject.MESSENGER_messengerID) {
							alert("何か届いた	"+rootObject.KEY_MESSENGER_EXEC);
						}
						break;
					}
					
					break;
				case 2://MS_CATEGOLY_CALLPARENT
					//何もしない
					break;
					
				case 5://MS_CATEGOLY_PARENTSEARCH_RET
					
					switch (rootObject.MESSENGER_messengerName) {
						case window[myself+"_"+parent]:
						if (window[myself].slice(0, 1) != "_") {
							alert("親から認定");
							window[myself] = "_" + window[myself]+"_"+e.data.MESSENGER_messengerID;
							window[myself+"_parentId"] = rootObject.MESSENGER_messengerID;//parentIdをセット(今後通信で使用する)
						}
						break;
					}
					break;
				default:
					alert("何か来ました	"+rootObject.KEY_MESSAGE_CATEGOLY);
					break;
			}
			
			
			
		}

		window.addEventListener('message', method, false);
	}-*/;

	
	
	/**
	 * 親設定が終わったあとに、POSTする
	 */
	public void testJSNIMessagePost() {
		// inputParentを模倣する。
		String messageMap = getInputParentContainor(TEST_MYNAME, "01234567",
				TEST_RECEIVER);
		sendMessageAsPostMessage(messageMap, Window.Location.getHref());
		setReceiver(TEST_MYNAME, TEST_RECEIVER);

//		messenger.inputParent(TEST_RECEIVER);

		delayTestFinish(TIME_LIMIT);
		Timer timer = new Timer() {
			int i = 0;

			@Override
			public void run() {
				// 定期的に到着の判断を行う。レシーバーへの返信で良いと思う。
				switch (i) {
				case 0:
					if (rec.getMessengerForTesting().getReceiveLogSize() == 1) {
						
						if (messenger.isReadyAsChild()) {// 本物が、無事に受け取れている場合
							messenger.callParent("command",
									messenger.tagValue("key", "value"));
							i = 1;
						}

						if (isReceived(TEST_MYNAME)) {
							i = 1;

							// 宛先指定
							String messageMap = getPostToParentContainor(
									TEST_MYNAME, "01234567",
									TEST_RECEIVER,
									myParentId(TEST_MYNAME), "testExec");
							
//							String real_message = messenger.getMessageStructure(2, "01234567", TEST_MYNAME, TEST_MYNAME, TEST_RECEIVER, "parentId", "testExec").toString();
//							assertEquals(real_message, messageMap);
							sendMessageAsPostMessage(messageMap, Window.Location.getHref());
							
							
						}
					}

					break;

				case 1:
					if (rec.getMessengerForTesting().getReceiveLogSize() == 2) {
						debug.trace("finish!!");
						cancel();
						finishTest();
					}
					break;

				default:
					break;
				}

			}
		};
		timer.scheduleRepeating(TIME_INTERVAL);
	}

	
	/**
	 * 親設定が終わったあとに、POST、親がそれに反応する
	 */
	public void testJSNIMessagePostAndReplied() {
		// inputParentを模倣する。
		String messageMap = getInputParentContainor(TEST_MYNAME, "01234567",
				TEST_RECEIVER);
		if (false) {
		sendMessageAsPostMessage(messageMap, Window.Location.getHref());
		setReceiver(TEST_MYNAME, TEST_RECEIVER);
		} else {
			messenger.inputParent(TEST_RECEIVER);
		}
		
		delayTestFinish(TIME_LIMIT);
		Timer timer = new Timer() {
			int i = 0;

			@Override
			public void run() {
				// 定期的に到着の判断を行う。レシーバーへの返信で良いと思う。
				switch (i) {
				case 0:
					if (rec.getMessengerForTesting().getReceiveLogSize() == 1) {
						/*
						 * 本物として送り込んで、リアクションを見てみる
						 */
						if (messenger.isReadyAsChild()) {// 本物が、無事に受け取れている場合
							Window.alert("通常のmessengerでの送り込みを行う");
							messenger.callParent("testExec",
									messenger.tagValue("key", "value"));
							i = 1;
						}

						//受け取ったらPost
						if (isReceived(TEST_MYNAME)) {
							i = 1;

							// 宛先指定
							String messageMap = getPostToParentContainor(
									TEST_MYNAME, "01234567",
									TEST_RECEIVER,
									myParentId(TEST_MYNAME), "testExec");//2通目
							Window.alert("immitate	messageMap	"+messageMap);
//							String real_message = messenger.getMessageStructure(2, "01234567", TEST_MYNAME, TEST_MYNAME, TEST_RECEIVER, "parentId", "testExec").toString();
//							assertEquals(real_message, messageMap);
							sendMessageAsPostMessage(messageMap, Window.Location.getHref());
							
						}
					}

					break;

				case 1:
					/*
					 * 現象としては、受け取っているのに発動しない、というケースか。
					 * たぶんコマンドが悪いんだと思う。どう違うのか。
					 * 
					 * 違うポイントは、
					 * ・受け取ったのにReceiveCenterが発動しない
					 * なので、仮に、普通に送り込んだ場合のものとメッセージを見比べる処理が必要。
					 * →編集してたのは別プロジェクトのメッセンジャーだった。、、、、orz
					 */
					if (rec.getMessengerForTesting().getReceiveLogSize() == 2) {
						Window.alert("レシーバが受け取ったので、返答をする	はず。"+100);
						
						//返答が発生しない場合
//						rec.getMessengerForTesting().call(TEST_MYNAME, "somethingReply");
						i = 2;
					}
					break;


				case 2://この分解能の書き方はハマる。
//					//自分がこれを受け取れたらOK
//					if (isReceivedMessageFromParent(TEST_MYNAME) != null) {
//						cancel();
//						finishTest();
//					}
				default:
					break;
				}

			}
		};
		timer.scheduleRepeating(TIME_INTERVAL);
	}
	
	/**
	 * ちょっと問題を整理する為に、
	 * messengerが子、receiverが親になり、
	 * 
	 * messengerが申し込む
	 * →receiverが親になる
	 * →messengerからreceiverにメッセージを送る
	 * →receiverが答える
	 * →messengerに届く
	 * 
	 * というシナリオをやってみる。
	 */
	public void testPrimitiveMessaging() {
		messenger.inputParent(TEST_RECEIVER);
		
		delayTestFinish(TIME_LIMIT);
		Timer timer = new Timer() {
			int step = 0;
				
			@Override
			public void run() {
				switch (step) {
				case 0:
					if (messenger.getReceiveLogSize() == 1) {//messengerが認められた
						
						messenger.callParent("testExec");
						//debug.trace("step0 ended");
						step = 1;
					}
					break;
					
				case 1:
					if (messenger.getReceiveLogSize() == 2) {//messengerにメッセージが届いた
						step = 2;
						cancel();
						finishTest();
					}
					break;
					
				default:
					break;
				}
			}
		};
		timer.scheduleRepeating(TIME_INTERVAL);
	}

	
	
	/**
	 * ネイティブで親のIDを返すメソッド
	 * @param string
	 * @return
	 */
	protected native String myParentId(String string) /*-{
		return window[string+"_"+"parentId"];
	}-*/;

	/**
	 * JSONObjectを作り出す、ひな形比較用のメソッド
	 * 
	 * @param name
	 * @param id8
	 * @param receiver
	 * @return
	 */
	private String getMessageStructure(String name, String id8, String receiver) {
		// JSONを合成する。　あとでJSNI化する。

		JSONObject rootJson = new JSONObject();
		rootJson.put("MESSENGER_messengerName", new JSONString(name));
		rootJson.put("MESSENGER_messengerID", new JSONString(name));
		rootJson.put("MESSENGER_messageID", new JSONString(id8));
		rootJson.put("KEY_MESSAGE_CATEGOLY", new JSONNumber(3));
		rootJson.put("MESSENGER_to", new JSONString(receiver));
		rootJson.put("MESSENGER_toID", new JSONString(""));
		rootJson.put("MESSENGER_exec", new JSONString(""));
		rootJson.put("MESSENGER_pName", new JSONString(receiver));
		rootJson.put("MESSENGER_tagValue", new JSONObject());

		// String result = rootJson.toString();
		String result = getInputParentContainor(name, "01234567", receiver);
		return result;
	}

	/**
	 * 親申し込み用のJSNIコンテナ
	 * 
	 * @param name
	 * @param receiver
	 * @return
	 */
	public native String getInputParentContainor(String name, String id8,
			String receiver) /*-{
				
		var result = new Object();

		result.MESSENGER_messengerName = name;
		result.MESSENGER_messengerID = name;
		result.MESSENGER_messageID = id8;
		result.KEY_MESSAGE_CATEGOLY = 3;//inputParent
		result.MESSENGER_to = receiver;
		result.MESSENGER_toID = "";
		result.MESSENGER_exec = "";
		result.MESSENGER_pName = receiver;
		result.MESSENGER_tagValue = new Object();
			
		return JSON.stringify(result);
	}-*/;

	/**
	 * JSONObjectを作り出す、ひな形比較用のメソッド
	 * 
	 * @param name
	 * @param receiver
	 * @return
	 */
	private String getMessageStructureForPost(String name, String receiver) {
		// JSONを合成する。　あとでJSNI化する。

		JSONObject rootJson = new JSONObject();
		rootJson.put("MESSENGER_messengerName", new JSONString(name));
		rootJson.put("MESSENGER_messengerID", new JSONString(name));
		rootJson.put("MESSENGER_messageID", new JSONString(""));
		rootJson.put("KEY_MESSAGE_CATEGOLY", new JSONNumber(3));
		rootJson.put("MESSENGER_to", new JSONString(receiver));
		rootJson.put("MESSENGER_toID", new JSONString(""));
		rootJson.put("MESSENGER_exec", new JSONString(""));
		rootJson.put("MESSENGER_pName", new JSONString(receiver));
		rootJson.put("MESSENGER_tagValue", new JSONObject());

		// String result = rootJson.toString();
		String result = getInputParentContainor(name, "01234567", receiver);
		return result;
	}

	/**
	 * 親へのPost用のJSNIコンテナ
	 * 
	 * @param name
	 * @param id8
	 * @param receiverName
	 * @param receiverId
	 * @param exec
	 * @return
	 */
	public native String getPostToParentContainor(String name, String id8,
			String receiverName, String receiverId, String exec) /*-{
		
		//もう、直書き。
		var kv = new Object();
		kv.key0 = "val0";
		kv.key1 = "val1";
		
		
		var result = new Object();
		result.MESSENGER_messengerName = name;
		result.MESSENGER_messengerID = name;
		result.MESSENGER_messageID = id8;
		result.KEY_MESSAGE_CATEGOLY = 2;//callParent
		result.MESSENGER_to = receiverName;
		result.MESSENGER_toID = receiverId;
		result.MESSENGER_exec = exec;
		result.MESSENGER_pName = "";
		result.MESSENGER_tagValue = kv;

//		alert("result_str	" + JSON.stringify(result));

		return JSON.stringify(result);
	}-*/;

	/**
	 * PureJSでJSON化されたDataをPostする
	 * 
	 * @param message
	 * @return
	 */
	public native boolean sendMessageAsPostMessage(String message, String href) /*-{
																				window.postMessage(message, href);
																				return true;
																				}-*/;

	@Override
	public void receiveCenter(String message) {
		debug.trace("メッセージを受けとった	"+messenger.getReceiveLogSize());
	}

}
