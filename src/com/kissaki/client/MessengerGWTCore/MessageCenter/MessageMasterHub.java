package com.kissaki.client.MessengerGWTCore.MessageCenter;


import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.subFrame.debug.Debug;
import com.kissaki.client.uuidGenerator.UUID;


/**
 * messageで行使されたメソッドから中継される、イベントハブ。
 * イベントを発行する手助けを行う、今回のMessengerのハブ部分。
 * 
 * staticなシングルトンオブジェクトとして所持される。
 * GINとか使って疎にしたいところだが。
 * 
 * @author ToruInoue
 */
public class MessageMasterHub implements MessengerGWTInterface {
	static Debug debug;
	
	int messageMasterStatus = MESSENGER_STATUS_NULL;
	
	private static MessageMasterHub hub;
	private static MessageReceivedEventBus eventBus;//付随するイベントオブジェクト、これもシングルトン。
	
	String m_masterID;
	/**
	 * シングルトン取得メソッド
	 * @return
	 */
	public static MessageMasterHub getMaster () {
		if (hub == null) {
			hub = new MessageMasterHub(UUID.uuid(8, 16));
			hub.setMessengerGlobalStatus(MESSENGER_STATUS_READY_FOR_INITIALIZE);
		}
		return hub;
	}
	
	/**
	 * コンストラクタ、シングルトンの為に秘匿
	 */
	private MessageMasterHub(String masterID) {
		debug = new Debug(this);
		m_masterID = masterID;
	}
	
	/**
	 * invoke対象の登録を行う
	 * 同じクラスに対して、二重登録を行わないようにチェックを行う
	 * (同じクラスを２つ登録すると、例えnameSpaceが別であっても、リスナのカウントが不自然に加算されるため。)
	 * e.g. Class Aのインスタンスを複数作り、messengerを持たせると、messengerの数だけ各クラスにメッセージが送られてしまう。
	 * 	Class A x 2 → 一つのメッセージが発生すると、インスタンス一つにつき2つのメッセージが送られてしまう。
	 * 	Eventの構造の問題だと思われる。登録数分だけ、同様のクラスに向けて発行されてしまう。
	 * 	
	 * 続き_11/01/13 20:29:49
	 * 通常のクラスについては、上記現象が発生しなかった。テストを書き換える必要がある。
	 * 
	 * @param messengerSelf
	 */
	public void setInvokeObject(MessageReceivedHandler messengerSelf) {
		
//		if (invocationClassNameList.contains(root.getClass().toString())) {//すでに同名のクラスが登録されていたら、登録しない。
//			debug.trace("already added_"+root.getClass());//JSの特例、同クラスの別インスタンスの所持するメソッドの区別が無い証、、、
//		} else {
//			invocationClassNameList.add(root.getClass().toString());
//			debug.trace("just added_"+root.getClass().toString());
//			checker.addMessageReceivedEventHandler((MessageReceivedEventHandler)messengerSelf);
//		}
		
		//一度設定すると、全体ごと一気にしか消せない！　この欠陥機構。 resetするにはより上位でアスペクトを切るしか無いが、それはもはや環境レベル。
		if (eventBus == null) {
			eventBus = new MessageReceivedEventBus();
		}
		eventBus.addHandler(MessageReceivedEvent.MESSAGE_RECEIVED_EVENT_TYPE, messengerSelf);
	}
	
	/**
	 * メッセージの行使を行う
	 * ここで、イベントが発行される
	 * @param message
	 */
	public static void messageReceived (String message) {
		eventBus.fireEvent(new MessageReceivedEvent(message));//ここを通過する過程で、staticが消える。
	}
	
	
	/**
	 * イベントでの同期メッセージ受け取り状態の強制作り出し
	 * @param message
	 */
	@Deprecated
	public void syncMessage (String message) {
		eventBus.fireEvent(new MessageReceivedEvent(message));
	}
	

	/**
	 * globalなインスタンスであるこのインスタンスが保持するMessengerSystemとしてのステータス
	 * @param setUp
	 */
	public void setMessengerGlobalStatus(int status) {
		messageMasterStatus = status;
	}

	public int getMessengerGlobalStatus() {
		return messageMasterStatus;
	}

	@Override
	public void receiveCenter(String message) {
		debug.assertTrue(false, "never call this method");
	}


	/**
	 * テスト用にMessengerのアスペクトをsetUpする
	 * 使用しないでテストを行う場合、eventBusに同名のレジスタが複数置かれてしまい、反応しなくなる。
	 * @return
	 */
	public static MessageMasterHub setUpMessengerAspectForTesting() {
		return getMaster();
	}

	/**
	 * テスト用にMessegnerのアスペクトをtearDownさせる
	 * 使用しないでテストを行う場合、eventBusに同名のレジスタが複数置かれてしまい、反応しなくなる。
	 */
	public void tearDownMessengerAspectForTesting() {
		eventBus = null;
	}

	/**
	 * 現在のアスペクトのID(master自身のID)を返す
	 * @return
	 */
	public String masterID() {
		return m_masterID;
	}


}
