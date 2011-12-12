package com.kissaki.client.MessengerGWTCore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageMasterHub;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEvent;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedHandler;
import com.kissaki.client.subFrame.debug.Debug;
import com.kissaki.client.uuidGenerator.UUID;


/**
 * MessengerGWTの実装
 * 
 * Obj-C版のGWT環境実装版
 * 	Obj-C版
 * 	http://gitorious.org/messengersystem-obj-c
 * 
 * 
 * -親子関係でのMessaging範囲制限と明示：子から親の限定は完了、親から子の限定は未実装
 * -任意時間での遅延実行：未作成
 * -同期：無し。未来永劫、yieldが万が一全ブラウザに積まれるまで無し
 * -クライアント間の通信：未作成
 * -クライアント-サーバ間の通信：未作成
 * 
 * @author ToruInoue, sassembla, krmd
 */
public class MessengerGWTImplement extends MessageReceivedHandler implements MessengerGWTInterface {
	
	static final String version = "0.9.0";//no static element.
//		"0.8.5";//アスペクト変化を認識/確認するために、 MasterHubへのアクセッサと、masterIDを追加 
//		"0.8.4";//テスト用にMessengerのAspectをリセットする機構を簡易化 setUpMessengerAspectForTesting() と tearDownMessengerAspectForTesting()
//		"0.8.3";//親子登録のTRIGGERがreceiveCenterまで貫通してしまうバグを解消
//		"0.8.2";//親が設定されたタイミングで、TRIGGERを発生させるよう調整
//		"0.8.1";//バグフィックス　テストが並列に行われていたのを解消。 
//		"0.8.0";//実働可能レベル 同期メソッドをテスト用として実装。テスト以外では使わない方がいい。 
//		"0.7.5";//親子関係設定、子からの親登録を実装。MIDでの関係しばり、子から親へのcallParentのみ完了。 callMyselfのID縛り完成。 
//		"0.7.4";//カテゴリ判別のルールを追加、callMyselfでの限定を実装。 
//		"0.7.3";//親子関係の設定/取得機能を追加、まだ制限は無し 
//		"0.7.2";///callMyself追加
//		"0.7.1";//バグフィックスとか調整中
//		"0.7.0";//11/01/18 17:50:30 Beta release
//		"0.5.2";//11/01/18 16:41:28 changed to EventBus from HasHandlers(Duplicated) 
//		"0.5.1";//11/01/09 20:55:55 String-Value-Bug fixed.
//		"0.5.0";//11/01/05 19:23:28 Alpha release


	
	
	Debug debug;
	
	public final String messengerName;
	public final String messengerID;
	public final Object invokeObject;
	
	public String parentName;
	public String parentID;
	
	public final String KEY_MESSAGE_CATEGOLY = "KEY_MESSAGE_CATEGOLY";
	public final int MS_CATEGOLY_LOCAL				= 0;
	public final int MS_CATEGOLY_CALLCHILD			= 1;
	public final int MS_CATEGOLY_CALLPARENT			= 2;
	public final int MS_CATEGOLY_PARENTSEARCH		= 3;
	public final int MS_CATEGOLY_PARENTSEARCH_S		= 4;
	public final int MS_CATEGOLY_PARENTSEARCH_RET	= 5;
	public final int MS_CATEGOLY_REMOVE_PARENT		= 6;
	public final int MS_CATEGOLY_REMOVE_CHILD		= 7;
	
	private final String KEY_MESSENGER_NAME	= "MESSENGER_messengerName";
	private final String KEY_MESSENGER_ID	= "MESSENGER_messengerID";
	private final String KEY_MESSAGE_ID	= "MESSENGER_messageID";
	
	public final String KEY_TO_NAME			= "MESSENGER_to";
	public final String KEY_TO_ID			= "MESSENGER_toID";
	private final String KEY_MESSENGER_EXEC	= "MESSENGER_exec";
	private final String KEY_MESSENGER_TAGVALUE_GROUP	= "MESSENGER_tagValue"; 
	private final String KEY_PARENT_NAME	= "MESSENGER_pName";
	private final String KEY_PARENT_ID		= "MESSENGER_pID"; 
	
	
	List <JSONObject> sendList = null;
	List <JSONObject> receiveList = null;
	
	public List <JSONObject> childList = null;
	public final String CHILDLIST_KEY_CHILD_NAME = "CHILDLIST_KEY_CHILD_NAME";//JSONObjectの中に、kvsで入れる時の名称　JSONObject A = {CHILDLIST_KEY_CHILD_NAME:b,CHILDLIST_KEY_CHILD_ID:c}
	public final String CHILDLIST_KEY_CHILD_ID = "CHILDLIST_KEY_CHILD_ID";
	
	public final String TRIGGER_PARENTCONNECTED = "TRIGGER_PARENTCONNECTED";
	
	
	
	/**
	 * コンストラクタ
	 * メッセージの受信ハンドラを設定する
	 * @param string 
	 */
	public MessengerGWTImplement (String messengerName, Object invokeObject) {
		debug = new Debug(this);

		this.messengerName = messengerName;
		this.messengerID = UUID.uuid(8,16);
		this.invokeObject = invokeObject;
		
		parentName = "";
		parentID = "";
		
		
		sendList = new ArrayList<JSONObject>();
		receiveList = new ArrayList<JSONObject>();

		childList = new ArrayList<JSONObject>(); 
		
		postMessageAPIMethod = get(this);
		
		if (MessageMasterHub.getMaster().getMessengerGlobalStatus() == MESSENGER_STATUS_READY_FOR_INITIALIZE) {
			int status = setUp(postMessageAPIMethod);//Java-method to JavaScriptObject(as function)
			
			debug.assertTrue(MessageMasterHub.getMaster().getMessengerGlobalStatus() == MESSENGER_STATUS_READY_FOR_INITIALIZE, "already initialized");
			MessageMasterHub.getMaster().setMessengerGlobalStatus(status);
		}
		
		MessageMasterHub.getMaster().setInvokeObject(this);
	}
	
	private JavaScriptObject postMessageAPIMethod;
	
	
	/**
	 * mtdメソッドをJSOとして値渡しするためのメソッド
	 * @param messengerGWTImplement 
	 * @return
	 */
	private native JavaScriptObject get (MessengerGWTImplement messengerGWTImplement) /*-{
		function f (e){messengerGWTImplement.@com.kissaki.client.MessengerGWTCore.MessengerGWTImplement::mtd(Ljava/lang/String;)(e.data)};
		return f;
	}-*/;
	
	
	/**
	 * window.postMessageの返り値を受け取るメソッド
	 * @param message
	 */
	private void mtd(String message) {
		MessageMasterHub.messageReceived(message);
	}
	
	
	/**
	 * setUp
	 * 
	 * Messengerの初期設定を行う
	 * Nativeのメッセージ受信部分
	 */
	private native int setUp(JavaScriptObject method) /*-{
		try {
			if (typeof window.postMessage === "undefined") { 
	    		alert("残念ですが、あなたのブラウザはpostMessage APIをサポートしていません。");
	    		return @com.kissaki.client.MessengerGWTCore.MessengerGWTInterface::MESSENGER_STATUS_NOT_SUPPORTED;
			} else {
				window.addEventListener('message',
					method,
					false);
			}
			
			return @com.kissaki.client.MessengerGWTCore.MessengerGWTInterface::MESSENGER_STATUS_OK;
		} catch (er) {
			alert("messenger_undefined_error_"+er);
			return @com.kissaki.client.MessengerGWTCore.MessengerGWTInterface::MESSENGER_STATUS_FAILURE;
		}
	}-*/;
	
	/**
	 * tearDown
	 * @param method
	 * @return
	 */
	private native void tearDown(JavaScriptObject method) /*-{
		window.removeEventListener('message',
					method,
					false);
	}-*/;
	

	/**
	 * EventBus経由のメッセージ受取メソッド
	 * @param event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String rootMessage = event.getMessage();
		onMessagereceivedFromPostMessageAPI(rootMessage);
	}
	
	/**
	 * PostMessageAPI からダイレクトで複数のmessengerが各個に呼ばれる事を想定したメソッド
	 * @param rootMessage
	 */
	public void onMessagereceivedFromPostMessageAPI (String rootMessage) {
		JSONObject rootObject = null;
//		Window.alert("受け取った	"+getName());
		try {
			rootObject = JSONParser.parseStrict(rootMessage).isObject();
		} catch (Exception e) {
			debug.trace("receiveMessage_parseError_"+e);
			return;
		}
		
		
		if (rootObject == null) {
			debug.trace("rootObject = null");
			return;
		}
		
		
		String toName = null;
		{
			/*
			 * 宛先チェック
			 */
			debug.assertTrue(rootObject.get(KEY_TO_NAME).isString() != null, "invalid KEY_TO_NAME");
			toName = rootObject.get(KEY_TO_NAME).isString().stringValue();
			
			if (!toName.equals(getName())) {//送信者の指定した宛先が自分か
				//			NSLog(@"MS_CATEGOLY_CALLPARENT_宛先ではないMessnegerが受け取った");
				return;
			}
		}
		
		
		String fromName = null;
		String fromID = null;
		{
			/*
			 * 送付元名前チェック
			 */
			fromName = rootObject.get(KEY_MESSENGER_NAME).isString().stringValue();
			debug.assertTrue(fromName != null, "invalid KEY_MESSENGER_NAME");
			
			/*
			 * 送付元IDチェック
			 */
			
			fromID = rootObject.get(KEY_MESSENGER_ID).isString().stringValue();
			debug.assertTrue(fromID != null, "invalid KEY_MESSENGER_ID");
		}
		
		
		int categoly;
		{
			debug.assertTrue(rootObject.get(KEY_MESSAGE_CATEGOLY).isNumber() != null, "no KEY_MESSAGE_CATEGOLY");
			categoly = (int)rootObject.get(KEY_MESSAGE_CATEGOLY).isNumber().doubleValue();
		}
		
		
		
		/*
		 * コマンドチェック
		 */
		{
			debug.assertTrue(rootObject.get(KEY_MESSENGER_EXEC).isString() != null, "KEY_MESSENGER_EXEC = null");
		}
		
		/*
		 * tag-valueチェック
		 */
		{
			debug.assertTrue(rootObject.get(KEY_MESSENGER_TAGVALUE_GROUP).isObject() != null, "KEY_MESSENGER_TAGVALUE_GROUP = null");
		}
		
		/*
		 * 宛先存在チェック
		 */
		String toID = null;
		{
			debug.assertTrue(rootObject.get(KEY_TO_ID).isString() != null, "no KEY_TO_ID");
			toID = rootObject.get(KEY_TO_ID).isString().stringValue();
		}
//		Window.alert("カテゴリチェックまで	メッセージングを受け取りました");
		
		switch (categoly) {	
		case MS_CATEGOLY_LOCAL:
		{
			if (toID.equals(getID())) {
				addReceiveLog(rootObject);
				receiveCenter(rootMessage);
			}
		}
			return;
			
		case MS_CATEGOLY_CALLCHILD:
			if (toID.equals(getID())) {
				addReceiveLog(rootObject);
				receiveCenter(rootMessage);
			}
			return;
			
		case MS_CATEGOLY_CALLPARENT:
			//宛先MIDが自分のIDと一致するか
			if (toID.equals(getID())) {
//				Window.alert("親として呼ばれた	メッセージングを受け取りました	");
				addReceiveLog(rootObject);
				receiveCenter(rootMessage);
//				Window.alert("親として呼ばれた2	メッセージングを受け取りました");
			}			

			
			
			return;
			
		case MS_CATEGOLY_PARENTSEARCH:
			
			debug.assertTrue(rootObject.get(KEY_PARENT_NAME).isString() != null, "no KEY_PARENT_NAME");
			String childSearchingName = rootObject.get(KEY_PARENT_NAME).isString().stringValue();
			if (childSearchingName.equals(getName())) {
				
				JSONObject childInfo = new JSONObject();
				childInfo.put(CHILDLIST_KEY_CHILD_ID, new JSONString(fromID));
				childInfo.put(CHILDLIST_KEY_CHILD_NAME, new JSONString(fromName));
				
				childList.add(childInfo);
//				Window.alert("子供っす	"+childList+"	で、名前が	"+childInfo);
				JSONObject messageMap = getMessageStructure(MS_CATEGOLY_PARENTSEARCH_RET, UUID.uuid(8,16), getName(), getID(), fromName, fromID, TRIGGER_PARENTCONNECTED);
				sendAsyncMessage(messageMap);
				addReceiveLog(rootObject);
			}
			
			return;
			
		case MS_CATEGOLY_PARENTSEARCH_S:
			debug.assertTrue(rootObject.get(KEY_PARENT_NAME).isString() != null, "no KEY_PARENT_NAME");
			String childSearchingName2 = rootObject.get(KEY_PARENT_NAME).isString().stringValue();
			if (childSearchingName2.equals(getName())) {
				addReceiveLog(rootObject);
				JSONObject childInfo = new JSONObject();
				childInfo.put(CHILDLIST_KEY_CHILD_ID, new JSONString(fromID));
				childInfo.put(CHILDLIST_KEY_CHILD_NAME, new JSONString(fromName));
				
				childList.add(childInfo);
				
				JSONObject messageMap = getMessageStructure(MS_CATEGOLY_PARENTSEARCH_RET, UUID.uuid(8,16), getName(), getID(), fromName, fromID, TRIGGER_PARENTCONNECTED);
				MessageMasterHub.getMaster().syncMessage(messageMap.toString());
				addSendLog(messageMap);
			}
			break;
			
			
		case MS_CATEGOLY_PARENTSEARCH_RET:
			if (!parentName.equals(fromName)) {
				return;
			}
			
			if (toID.equals(getID())) {
				if (parentID.equals("")) {
					parentID = fromID;
					addReceiveLog(rootObject);
				} else {
//					debug.trace("もう別の親が居ます"+ "/fromID	"+fromID);
				}
				
			}
			
			return;	
			
		case MS_CATEGOLY_REMOVE_CHILD:
		case MS_CATEGOLY_REMOVE_PARENT:
		default:
			debug.assertTrue(false, "not ready yet or UNKNOWN CATEGOLY");
			return;
		}
	}
	
	





	/**
	 * 内部から外部への行使
	 */
	public void receiveCenter(String rootMessage) {
//		Window.alert("getInvokeObject()	"+getInvokeObject());
		((MessengerGWTInterface) getInvokeObject()).receiveCenter(rootMessage);
	}


		

	/**
	 * 送付前のメッセージのプレビューを取得するメソッド
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 * @return
	 */
	public JSONObject getMessageObjectPreview (int messageCategoly, String receiverName, String receiverID, String command, JSONObject ... tagValue) {
		return getMessageStructure(messageCategoly, UUID.uuid(8,16), getName(), getID(), receiverName, receiverID, command, tagValue);
	}
	
	
	
	/**
	 * 非同期メッセージ送信メソッド
	 * @param message
	 */
	public void sendAsyncMessage(JSONObject message) {
		
		String href = Window.Location.getHref();
		postMessage(message.toString(), href);
		
		addSendLog(message);
	}
	
	
	
	/**
	 * 非同期メッセージを子供へと送信するメソッド
	 * 子供へのメッセージング
	 * @param toName
	 * @param command
	 * @param tagValue
	 */
	public void call(String toName, String command, JSONObject ... tagValue) {
//		Window.alert("送る前までは来てる	"+childList);
		for (JSONObject currentChild : childList) {
//			Window.alert("currentChild	"+currentChild);
			if (currentChild.get(CHILDLIST_KEY_CHILD_NAME).isString().stringValue().equals(toName)) {
				String toID = currentChild.get(CHILDLIST_KEY_CHILD_ID).isString().stringValue();
				JSONObject messageMap = getMessageStructure(MS_CATEGOLY_CALLCHILD, UUID.uuid(8,16), getName(), getID(), toName, toID, command, tagValue);
				sendAsyncMessage(messageMap);
//				Window.alert("送った");
			}
		}
	}

	/**
	 * 非同期メッセージを自分へと送信するメソッド
	 * 自分へのメッセージング
	 * @param command
	 * @param tagValue
	 */
	public String callMyself(String command, JSONObject ... tagValue) {
		String messageID = UUID.uuid(8,16);
		JSONObject messageMap = getMessageStructure(MS_CATEGOLY_LOCAL, messageID, getName(), getID(), getName(), getID(), command, tagValue);
		sendAsyncMessage(messageMap);
		return messageID;
	}
	
	/**
	 * 非同期メッセージを親へと送信するメソッド
	 * 親へのメッセージング
	 * @param command
	 * @param tagValue
	 */
	public String callParent(String command, JSONObject ... tagValue) {
		debug.assertTrue(parentName != "", "ASYNC parentName not applied yet");
		debug.assertTrue(parentID != "", "ASYNC	parentID not applied yet");
		
		String messageID = UUID.uuid(8,16);
		JSONObject messageMap = getMessageStructure(MS_CATEGOLY_CALLPARENT, messageID, getName(), getID(), getParentName(), getParentID(), command, tagValue);
		debug.trace("true	messageMap	"+messageMap.toString());
		sendAsyncMessage(messageMap);
		return messageID;
	}
	

	
	/**
	 * EventBus実装での同期メッセージング
	 * イベントでの実装が、他と同レベルなコンテキストに書かれてしまう事が、どうしても欠点として映る。
	 * 
	 * @param toName
	 * @param command
	 * @param tagValue
	 */
	public void sCall (String toName, String command, JSONObject ... tagValue) {
		for (JSONObject currentChild : childList) {
			if (currentChild.get(CHILDLIST_KEY_CHILD_NAME).isString().stringValue().equals(toName)) {
				String toID = currentChild.get(CHILDLIST_KEY_CHILD_ID).isString().stringValue();
				JSONObject messageMap = getMessageStructure(MS_CATEGOLY_CALLCHILD, UUID.uuid(8,16), getName(), getID(), toName, toID, command, tagValue);
				MessageMasterHub.getMaster().syncMessage(messageMap.toString());
				addSendLog(messageMap);
			}
		}
	}
	
	/**
	 * EventBus実装での同期メッセージング
	 * 同期メッセージを自分へと送信するメソッド
	 * 自分へのメッセージング
	 * @param command
	 * @param tagValue
	 */
	public void sCallMyself(String command, JSONObject ... tagValue) {
		JSONObject messageMap = getMessageStructure(MS_CATEGOLY_LOCAL, UUID.uuid(8,16), getName(), getID(), getName(), getID(), command, tagValue);
		MessageMasterHub.getMaster().syncMessage(messageMap.toString());
		addSendLog(messageMap);
	}
	
	/**
	 * EventBus実装での同期メッセージング
	 * 同期メッセージを親へと送信するメソッド
	 * 親へのメッセージング
	 * @param command
	 * @param tagValue
	 */
	public void sCallParent(String command, JSONObject ... tagValue) {
		debug.assertTrue(parentName != "", "SYNC	parentName not applied yet");
		debug.assertTrue(parentID != "", "SYNC	parentID not applied yet");
		
		JSONObject messageMap = getMessageStructure(MS_CATEGOLY_CALLPARENT, UUID.uuid(8,16), getName(), getID(), getParentName(), getParentID(), command, tagValue);
		MessageMasterHub.getMaster().syncMessage(messageMap.toString());
		addSendLog(messageMap);
	}
	
	
	
	private native void yield() /*-{
//		function y() {
//		  var i = 0, j = 1;
//		  while (true) {
//		    yield i;//無い
//		    var t = i;
//		    i = j;
//		    j += t;
//		  }
//		}
//		var g = fib();
//		var i = 0;
//		
//		
//		while (true) {
//			yield;
//			
//			
//		}
//		var result = yield setTimeout(function () {request("hoge");}, 2000);
	}-*/;


	/**
	 * 入力されたメッセージを元に、宛先とコマンドを変更したものを返す
	 * 
	 * @deprecated 0.7.4 親子関係を組み込むと、callの部分がmanualに成る為、使用不可とする。
	 * 
	 * @param receiverName
	 * @param command
	 * @param eventString
	 * @return
	 */
	public String copyOut(int messageCategoly, String newReceiverName, String newCommand, String eventString) {
		//内容チェックを行い、receiverとcommandを書き換える
		debug.assertTrue(newReceiverName != null, "newReceiverName = null");
		debug.assertTrue(newCommand != null, "newCommand = null");
		debug.assertTrue(eventString != null, "eventString = null");
		
		JSONObject eventObj = JSONParser.parseStrict(eventString).isObject();
		debug.assertTrue(eventObj.containsKey(KEY_MESSAGE_CATEGOLY), "not contain KEY_MESSAGE_CATEGOLY");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_NAME), "not contain KEY_MESSENGER_NAME");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_ID), "not contain KEY_MESSENGER_ID");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_EXEC), "not contain KEY_MESSENGER_EXEC");
		debug.assertTrue(eventObj.containsKey(KEY_TO_NAME), "not contain KEY_TO_NAME");
		debug.assertTrue(eventObj.containsKey(KEY_TO_ID), "not contain KEY_TO_ID");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_TAGVALUE_GROUP), "not contain KEY_MESSENGER_TAGVALUE_GROUP");
		
		//categolyの書き換えを行う
		
		return replaceSenderInformation(messageCategoly, getName(), getID(), newReceiverName, newCommand, eventObj).toString();
	}
	


	/**
	 * 送信者情報を特定のものに変更する
	 * @param name
	 * @param id
	 * @param newCommand 
	 * @param newReceiverName 
	 * @param eventObj
	 */
	private JSONObject replaceSenderInformation(int messageCategoly, String name, String id,
			String newReceiverName, String newCommand, JSONObject eventObj) {
		JSONObject newObject = new JSONObject();
		newObject.put(KEY_MESSAGE_CATEGOLY, new JSONNumber(messageCategoly));
		newObject.put(KEY_MESSENGER_NAME, new JSONString(name));
		newObject.put(KEY_MESSENGER_ID, new JSONString(id));
		newObject.put(KEY_TO_NAME, new JSONString(newReceiverName));
		newObject.put(KEY_MESSENGER_EXEC, new JSONString(newCommand));
		newObject.put(KEY_MESSENGER_TAGVALUE_GROUP, eventObj.get(KEY_MESSENGER_TAGVALUE_GROUP));
		
		return newObject;
	}



	/**
	 * 秘匿されるべき関数
	 * 
	 * 特定の宛先に向けて、メッセージを送付する
	 * @param message
	 * @param uri
	 */
	private native void post (String message, String uri) /*-{
		window.postMessage(message, uri);
	}-*/;

	
	/**
	 * 送信メソッド
	 * @param message
	 * @param href
	 */
	private void postMessage (String message, String href) {
		post(message, href);
	}
	
	
	
	
	
	/**
	 * 送信メッセージ構造を構築する
	 * 
	 * KEY_MESSENGER_NAME:送信者名
	 * KEY_MESSENGER_ID:送信者ID
	 * KEY_TO_NAME:送信先
	 * KEY_MESSENGER_EXEC:実行コマンド
	 * KEY_MESSENGER_TAGVALUE_GROUP:タグとバリューのグループ
	 * 
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 * @return
	 */
	public JSONObject getMessageStructure(
			int messageCategoly,
			String messageID,
			String senderName,
			String senderID,
			String receiverName,
			String receiverID,
			String command,
			JSONObject ... tagValue) {//JSONObject[] tagValue
		JSONObject messageMap = new JSONObject();
		
		messageMap.put(KEY_MESSENGER_NAME, new JSONString(senderName));
		messageMap.put(KEY_MESSENGER_ID, new JSONString(senderID));
		messageMap.put(KEY_MESSAGE_ID, new JSONString(messageID));
		messageMap.put(KEY_MESSAGE_CATEGOLY, new JSONNumber(messageCategoly));
		
		
		switch (messageCategoly) {
		case MS_CATEGOLY_LOCAL:
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			messageMap.put(KEY_TO_ID, new JSONString(senderID));
			
			break;
			
		case MS_CATEGOLY_CALLCHILD:
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_TO_ID, new JSONString(receiverID));

			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			break;
			
		case MS_CATEGOLY_CALLPARENT:
			
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_TO_ID, new JSONString(receiverID));
			
			
			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			
			break;
			
		case MS_CATEGOLY_PARENTSEARCH:
		case MS_CATEGOLY_PARENTSEARCH_S:
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_TO_ID, new JSONString(receiverID));
			
			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			
			messageMap.put(KEY_PARENT_NAME, new JSONString(receiverName));
			break;
			
		case MS_CATEGOLY_PARENTSEARCH_RET:
			messageMap.put(KEY_PARENT_NAME, new JSONString(getName()));
			messageMap.put(KEY_PARENT_ID, new JSONString(getID()));
			
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_TO_ID, new JSONString(receiverID));

			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			break;
			
		case MS_CATEGOLY_REMOVE_CHILD:
		case MS_CATEGOLY_REMOVE_PARENT:
		default:
			debug.assertTrue(false, "not ready yet");
			break;
		}

		
		JSONObject tagValueGroup = new JSONObject();
		
		for (JSONObject currentObject:tagValue) {
			for (Iterator<String> currentItel = currentObject.keySet().iterator(); currentItel.hasNext();) {
				String currentKey = currentItel.next();
				tagValueGroup.put(currentKey, currentObject.get(currentKey));//オブジェクトの移し替え
			}
		}
		
		messageMap.put(KEY_MESSENGER_TAGVALUE_GROUP, tagValueGroup);
//		debug.trace("messageMap_"+messageMap);//しばらくin-outのテスト用にとっておこう。
		return messageMap;
	}
	
	

	

	/**
	 * 名称取得
	 * @return
	 */
	public String getName () {
		return messengerName;
	}
	
	
	/**
	 * ID取得
	 * @return
	 */
	public String getID () {
		return messengerID;
	}
	
	
	
	/**
	 * invocation元、実行者の取得
	 * @return
	 */
	private Object getInvokeObject() {
//		debug.trace("invokator_"+invokeObject);
		return invokeObject;
	}
	
	



	/**
	 * integer
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, int value) {
		JSONObject intObj = new JSONObject();
		intObj.put(key, new JSONNumber(value));
		return intObj;
	}



	/**
	 * double
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, double value) {
		JSONObject doubleObj = new JSONObject();
		doubleObj.put(key, new JSONNumber(value));
		return doubleObj;
	}



	/**
	 * String
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, String value) {
		JSONObject stringObj = new JSONObject();
		stringObj.put(key, new JSONString(value));
		return stringObj;
	}
	
	


	/**
	 * JSONObject-Array
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, JSONObject [] value) {
		JSONObject arrayObj = new JSONObject();
		JSONArray array = new JSONArray();
		
		int i = 0;
		for (JSONObject currentValue:value) {
			array.set(i++, currentValue);
		}
		arrayObj.put(key, array);
		
		return arrayObj;
	}
	


	/**
	 * JSONObject
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, JSONObject value) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(key, value);
		return jsonObj;
	}
	
	/**
	 * JSONArray
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, JSONArray value) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(key, value);
		return jsonObj;
	}


	
	//Lock系の実装

//	public JSONObject withLockBefore(String ... key_lockValues) {
//		JSONObject singleLockObject = new JSONObject();
//		
//		
//		
//		int i = 0;
//		for (String currentObject:key_lockValues) {
//			
//		}
//		singleLockObject.put(keyName, new JSONString(lockValue));
//		
//		//[NSDictionary dictionaryWithObject:multiLockArray forKey:MS_LOCK_AFTER];
//		return tagValue(KEY_LOCK_BEFORE, singleLockObject);
//	}
//
//	public JSONObject withLockAfter(String lockValue, String keyName) {
//		JSONObject singleLockObject = new JSONObject();
//		singleLockObject.put(keyName, new JSONString(lockValue));
//		
//		return tagValue(KEY_LOCK_AFTER, singleLockObject);
//	}
	
	
	



	/**
	 * 送信ログをセットする
	 * 
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 */
	public void addSendLog(JSONObject logMap) {
		sendList.add(logMap);		
	}
	

	/**
	 * 送信ログを差し出す
	 * @return
	 */
	public String getSendLog(int i) {
		debug.assertTrue(sendList != null, "sendList == null");
		debug.assertTrue(i < sendList.size(), "oversize of sendList");
		return sendList.get(i).toString();
	}
	
	
	/**
	 * 送信ログのサイズを取得する
	 * @return
	 */
	public int getSendLogSize() {
		return sendList.size();
	}

	
	

	/**
	 * 受け取りログに内容を加える
	 * @param receiverName
	 * @param command
	 */
	private void addReceiveLog(JSONObject logMap) {
		receiveList.add(logMap);
	}

	
	/**
	 * 受け取りログを差し出す
	 * @param i
	 * @return
	 */
	public String getReceiveLog(int i) {
		debug.assertTrue(receiveList != null, "receiveList not yet initialize");
		return receiveList.get(i).toString();
	}
	
	
	/**
	 * 受け取りログのサイズ取得
	 * @return
	 */
	public int getReceiveLogSize() {
		return receiveList.size();
	}
	
	
	/**
	 * メッセージから、メッセージの送信者名を取得する
	 * @param message
	 * @return
	 */
	public String getSenderName(String message) {
		return JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_NAME).isString().stringValue();
	}

	/**
	 * メッセージから、メッセージの送信者のIDを取得する
	 * @param message
	 * @return
	 */
	public String getSenderID(String message) {
		return JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_ID).isString().stringValue();
	}
	
	/**
	 * メッセージから、メッセージコマンドを取得する
	 * @param message
	 * @return
	 */
	public String getCommand(String message) {
		return JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_EXEC).isString().stringValue();
	}

	/**
	 * messageから、TagValue部分をJSONObjectに変換して返す
	 * @param message
	 * @return
	 */
	public JSONObject getJSONObjetFromMessage(String message) {
		return JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_TAGVALUE_GROUP).isObject();
	}
	
	/**
	 * バリューをタグから取得する
	 * 存在しない場合アサーションエラー
	 * @param message
	 * @param tag
	 * @return
	 */
	public JSONValue getValueForTag(String tag, String message) {
		JSONObject obj = getJSONObjetFromMessage(message);
		debug.assertTrue(obj.containsKey(tag), "no-	" + tag + "	-contains");
		
		return obj.get(tag);
	}
	
	
	
	/**
	 * tagValueグループに含まれるtagをリストとして取得する
	 * @param message
	 * @return
	 */
	public ArrayList<String> getTags(String message) {
		JSONObject obj = JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_TAGVALUE_GROUP).isObject();
		
		ArrayList<String> tags = new ArrayList<String>();
		
		Set<String> currentSet = obj.keySet();
		
		for (Iterator<String> currentSetItel = currentSet.iterator(); currentSetItel.hasNext();) {
			tags.add(currentSetItel.next());
		}
		
		return tags;
	}

	/**
	 * tagValueグループに含まれるvalueをリストとして取得する
	 * @param message
	 * @return
	 */
	public ArrayList<JSONValue> getValues(String message) {
		JSONObject obj = JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_TAGVALUE_GROUP).isObject();
		
		ArrayList<JSONValue> values = new ArrayList<JSONValue>();
		
		Set<String> currentSet = obj.keySet();
		
		for (Iterator<String> currentSetItel = currentSet.iterator(); currentSetItel.hasNext();) {
			String currentKey = currentSetItel.next();
			values.add(obj.get(currentKey));
		}
		
		return values;
	}
	
	
	
	
	
	/**
	 * @return the messengerStatus
	 */
	public int getMessengerStatus() {
		return MessageMasterHub.getMaster().getMessengerGlobalStatus();
	}



	/**
	 * 親子関係の構築を行う
	 * @param input
	 */
	public void inputParent(String inputName) {
		debug.assertTrue(parentName.equals(""), "already have parentName	すでに先約があるようです");
		debug.assertTrue(parentID.equals(""), "already have parentID	すでに先約があるようです");
		
		debug.assertTrue(!inputName.equals(""), "空文字は親の名称として指定できません");
		
		parentName = inputName;
		
		String messageID = UUID.uuid(8,16);
		JSONObject messageMap = getMessageStructure(MS_CATEGOLY_PARENTSEARCH, messageID, getName(), getID(), inputName, "", "");
		sendAsyncMessage(messageMap);
	}
	
	public String testbed(String inputName) {
		debug.assertTrue(parentName.equals(""), "already have parentName	すでに先約があるようです");
		debug.assertTrue(parentID.equals(""), "already have parentID	すでに先約があるようです");
		
		debug.assertTrue(!inputName.equals(""), "空文字は親の名称として指定できません");
		
		parentName = inputName;
		
		String messageID = UUID.uuid(8,16);
		JSONObject messageMap = getMessageStructure(MS_CATEGOLY_PARENTSEARCH, messageID, getName(), getID(), inputName, "", "");
		sendAsyncMessage(messageMap);
		return messageMap.toString();
	}

	/**
	 * 同期版
	 * @param name
	 */
	public void sInputParent(String inputName) {
		debug.assertTrue(parentName.equals(""), "already have parentName	すでに先約があるようです");
		debug.assertTrue(parentID.equals(""), "already have parentID	すでに先約があるようです");
		
		debug.assertTrue(!inputName.equals(""), "空文字は親の名称として指定できません");
		
		parentName = inputName;
		
		String messageID = UUID.uuid(8,16);
		JSONObject messageMap = getMessageStructure(MS_CATEGOLY_PARENTSEARCH_S, messageID, getName(), getID(), inputName, "", "");
		MessageMasterHub.getMaster().syncMessage(messageMap.toString());
		addSendLog(messageMap);
	}


	/**
	 * この名称のMessengerのIDを探し、取得する
	 * @param input
	 * @return
	 */
	private void getParentID(String input) {
		
	}


	/**
	 * 入力されている親の名前を取得する
	 * @return
	 */
	public String getParentName() {
		debug.assertTrue(!parentName.equals(""), "まだ親のNameのinputが行われていません");
		return parentName;
	}
	
	/**
	 * 入力されている親のIDを取得する
	 * @return
	 */
	public String getParentID () {
		debug.assertTrue(!parentName.equals(""), "まだ親のIDのinputが行われていません");
		return parentID;
	}
	
	
	
	/**
	 * 同期メッセージング(試作)
	 */
	private void sendSyncMessage(JSONObject message) {
		debug.timeAssert("11/07/23 8:40:07", 0, "yieldが正式に全ブラウザに実装されるまでは使えない。");
		
		String href = Window.Location.getHref();
		postMessage(message.toString(), href);
		
		//この辺にyieldでメッセージ受信の受付を行えば良い
		
		addSendLog(message);
	}



	/**
	 * 現在のEventBusからこのMessengerのレジスタだけを削除する
	 */
	public void removeFromCurrentMessageAspect() {
//		debug.timeAssert("11/09/29 17:17:21", 3600, "未完成の切断");//24F8A5DC-A35B-4981-A206-B5AB86690992
		tearDown(postMessageAPIMethod);
		debug.trace("this	"+this+"/postMessageAPIMethod	tearDown	"+postMessageAPIMethod);
	}



	/**
	 * masterHubを返す
	 * @return
	 */
	public MessageMasterHub masterHub() {
		return MessageMasterHub.getMaster();
	}


	/**
	 * 親設定が完了したかどうか
	 * @return
	 */
	public boolean isReadyAsChild() {
		if (parentID.equals("")) return false;
		if (parentName.equals("")) return false;
		
		return true;
	}


}
