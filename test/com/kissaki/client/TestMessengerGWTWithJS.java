package com.kissaki.client;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageMasterHub;
import com.kissaki.client.subFrame.debug.Debug;



public class TestMessengerGWTWithJS extends GWTTestCase {
	Debug debug;
	
	MessengerGWTWIthHype hype;
	MessengerGWTImplement messenger;
	MessageMasterHub currentAspectMaster;
	
	ReceiverClass rec;
	
	
	int TIME_LIMIT = 10000;
	int TIME_INTERVAL = 10;
	
	String TEST_MESSAGE_ID = "0";//固定値 
	
	String TEST_MYNAME = "TEST_MYNAME";
	String TEST_COMMAND = "TEST_COMMAND";
	String TEST_RECEIVER_ID = "TEST_RECEIVER_ID";
	
	String TEST_TAG = "TEST_TAG";
	String TEST_VALUE = "TEST_VALUE";
	String TEST_RECEIVER = "TEST_RECEIVER";
	
	
	/**
	 * コンストラクタ
	 */
	public TestMessengerGWTWithJS () {
		debug = new Debug(this);
		debug.trace("constructed");
	}
	
	
	/**
	 * Must refer to a valid module that sources this class.
	 */
	public String getModuleName() {
		return "com.kissaki.MessengerGWTWithHype";//パッケージの中で、クライアント/サーバの前+プロジェクトプロジェクト名称(xmlでの読み出しが行われている箇所)
	}
	
	/**
	 * セットアップ
	 */
	public void gwtSetUp () {
		debug.trace("setup_"+this);
		hype = new MessengerGWTWIthHype();
//		hype.onModuleLoad();
		
		currentAspectMaster = MessageMasterHub.setUpMessengerAspectForTesting();
		
		rec = new ReceiverClass(TEST_RECEIVER);
		
		messenger = new MessengerGWTImplement(TEST_MYNAME, this);
	}
	
	
	/**
	 * ティアダウン
	 */
	public void gwtTearDown () {
		hype = null;
		
		currentAspectMaster.tearDownMessengerAspectForTesting();
		
		//レシーバ、必要であれば。
		rec.receiver.removeFromCurrentMessageAspect();
		rec.receiver = null;
		rec = null;
		
		messenger.removeFromCurrentMessageAspect();
		messenger = null;
		debug.trace("teardown");
	}
	
	
	
	
	/**
	 * JSNIで親への認定設定を送信するポスト機構のテスト
	 * messageMapで制作できる内容を模倣していく。
	 * 
	 * postMessageでメッセージを送る(最初は、inputParentの内容)
	 */
	public void testJSNIMessageInput() {
		
		//inputParentを模倣する。
		String messageMap = getInputParentContainor("hype", TEST_RECEIVER);
		String actual_messageMap = messenger.getMessageStructure(3, "", "hype", "hype", TEST_RECEIVER, "", "").toString();
//		assertEquals(actual_messageMap, messageMap);
		
		boolean isSended = sendMessageAsPostMessage(messageMap, Window.Location.getHref());
		debug.trace("isSended	"+isSended);
		
		delayTestFinish(TIME_LIMIT);
		Timer timer = new Timer() {
			
			@Override
			public void run() {
				//定期的に到着の判断を行う。レシーバーへの返信で良いと思う。
				if (rec.getMessengerForTesting().getReceiveLogSize() == 1) {
					cancel();
					finishTest();
				}
			}
		};
		timer.scheduleRepeating(TIME_INTERVAL);
	}
	
	
	/**
	 * 親設定が終わったあとに、POSTする
	 */
	public void testJSNIMessagePost() {
		
		//inputParentを模倣する。
		String messageMap = getInputParentContainor("hype", TEST_RECEIVER);
		sendMessageAsPostMessage(messageMap, Window.Location.getHref());
		
		
		
		delayTestFinish(TIME_LIMIT);
		Timer timer = new Timer() {
			int i = 0;
			@Override
			public void run() {
				//定期的に到着の判断を行う。レシーバーへの返信で良いと思う。
				switch (i) {
				case 0:
									
					if (rec.getMessengerForTesting().getReceiveLogSize() == 1) {
						String messageMap = getMessageStructureForPost("hype", TEST_RECEIVER);
						//sendMessageAsPostMessage(messageMap, Window.Location.getHref());
						
						messenger.callParent("command", messenger.tagValue("key", "value"));
						
						i = 1;
					}

					break;
				case 1:
					
					if (rec.getMessengerForTesting().getReceiveLogSize() == 2) {
						
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
	 * 親設定が終わったあと、連絡を貰う
	 */
	public void testReceiveInput() {
		
	}
	
	
	
	

	/**
	 * JSONObjectを作り出す、ひな形比較用のメソッド
	 * 
	 * @param name
	 * @param receiver
	 * @return
	 */
	private String getMessageStructure(String name, String receiver) {
		//JSONを合成する。　あとでJSNI化する。
		
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
		
		
//		String result = rootJson.toString();
		String result = getInputParentContainor(name, receiver);
		return result;
	}

	
	
	
	/**
	 * 親申し込み用のJSNIコンテナ
	 * @param name
	 * @param receiver
	 * @return
	 */
	public native String getInputParentContainor (String name, String receiver) /*-{
		var result = {
			"MESSENGER_messengerName":name,
			"MESSENGER_messengerID":name,
			"MESSENGER_messageID":"",
			"KEY_MESSAGE_CATEGOLY":3,//inputParent
			"MESSENGER_to":receiver,
			"MESSENGER_toID":"",
			"MESSENGER_exec":"",
			"MESSENGER_pName":receiver,
			"MESSENGER_tagValue":{}
		};
		
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
		//JSONを合成する。　あとでJSNI化する。
		
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
		
		
//		String result = rootJson.toString();
		String result = getInputParentContainor(name, receiver);
		return result;
	}

	
	
	/**
	 * 親へのPost用のJSNIコンテナ
	 * @param name
	 * @param receiver
	 * @return
	 */
	public native String getPostToParentContainor (String name, String receiver) /*-{
		var result = {
			"MESSENGER_messengerName":name,
			"MESSENGER_messengerID":name,
			"MESSENGER_messageID":"",
			"KEY_MESSAGE_CATEGOLY":2,//callParent
			"MESSENGER_to":receiver,
			"MESSENGER_toID":"",
			"MESSENGER_exec":"",
			"MESSENGER_pName":receiver,
			"MESSENGER_tagValue":{}
		};
		
		return JSON.stringify(result);
	}-*/;
	
	
	

	/**
	 * PureJSでJSON化されたDataをPostする
	 * @param message
	 * @return
	 */
	public native boolean sendMessageAsPostMessage (String message, String href) /*-{
		//http://192.168.1.100:54501/com.kissaki.MessengerGWTWithHype.JUnit/hosted.html?com_kissaki_MessengerGWTWithHype_JUnit
		//http://127.0.0.1:54680/com.kissaki.MessengerGWTWithHype.JUnit/junit.html?gwt.codesvr=127.0.0.1:54679
//		var href = $wnd.location.href;//window.location;
		$wnd.postMessage(message, href);
		return true;
	}-*/;
	

}
