package com.kissaki.client.subFrame.debug;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;


/**
 * デバッグ設定を行うクラス。
 * スクリーンに表示するために、要素の保持と出力機構を持つ。
 * 描画機能は持たないので、気をつける事。
 * 
 * traceが呼ばれるたび、通知が行くと良いのだけれど。
 * イベント発行の必要がある。
 * 
 * デバッグ文字表示が発生
 * →文字列を保存
 * →変更があった事をイベントで外部に通知
 * →外部で受け取ったスクリーンがイベントを受け取り、デバッグクラスへと文言取得に来る（もしくはイベントに乗せておく？　最近の数行分の方が良いな。）
 * →スクリーンによって文字列が表示される
 * 
 * @author sassembla
 *
 */
public class Debug {
	String VERSION = "0.7.3";//timeAssertについて、Illigalなフォーマットに対してエラーをはく処理を追記
//			"0.7.2";// assertJSONNotNull Bug Fix　null値を放り込んだ際にブロックをnoErrorで抜けるバグを修正
//			"0.7.1";//JSONObjectの対象Key有無をチェックするassertJSONNotNullを追加 
//			"0.7.0";//サーバサイドでのAssertの効果を変更、エラーを出すのではなく、エラーを伝えるように変更。 プロジェクトとして独立
//			"0.6.6";//assertNotNullメソッドを追加、エラー出力をJava assertに変更　timeAssertの内容をassertに変更 
//			"0.6.5";//timeAssertの表示を見やすく調整 BOMBを行頭に表示
//			"0.6.4";//TimeAssertの管理フラッグ追加 
//			"0.6.3_11/05/10 21:42:04";////TimeAssertの文言の調整、BOMBのメッセージをBOMB直前に表示するように
//			"0.6.2_11/05/06 18:22:16";//TimeAssertの文言の調整 
//			"0.6.1_11/05/05 9:02:09";
//			"0.6.0_11/05/04 10:07:24";
	
	/*
	 * デバッグ状態　enumで持ちたい。
	 */
	public static final int DEBUG_TRUE = 0;
	public static final int DEBUG_FALSE = 1;
	
	
	private int debug = DEBUG_TRUE;//デバッグモード getter,setterあり。
	
	private String attr = null;//追跡用、作成者クラスの表示用文字列
	private int traceSetting;//追跡用のセッティング
	
	
	private ArrayList<String> assertCollectionBuffer;//アサートのコレクションを格納するバッファ
	
	
	/*
	 * traceSetting用セッティングステータス
	 */
	public final static int DEBUG_OUTPUT_OFF	= 0x0000;//デバッグの文字列に関連する標準出力以外の出力を行わない
	public final static int DEBUG_EVENT_ON		= 0x0001;//デバッグの文字列をイベントとして出力する	
	public final static int DEBUG_ALERT_ON		= 0x0010;//デバッグの文字列をアラートとして出力する
	public final static int DEBUG_TIMEASSERT_ON	= 0x0100;//デバッグのタイムアサート(時限付きToDo表記)を発生させる
	public final static int DEBUG_ASSERT_COLLECTION_ON = 0x1000;//デバッグのアサートコレクション設定をONにし、アサートを文字として回収する。
	
	
	final String TRACE_MESSAGE = ":";//トレース文字列
	final String ASSERT_MESSAGE = "_assert:";//アサート文字列
	
	
	/**
	 * コンストラクタ
	 * @param obj
	 */
	public Debug (Object obj) {
		setDebug(debug);//デバッグモードのセット
		initialize(obj);	
	}
	
	
	
	/**
	 * 初期化を行う。
	 * @param obj 
	 */
	private void initialize(Object obj) {
		addTraceSetting(DEBUG_EVENT_ON);//クライアント内のデバッグ表示用
		addTraceSetting(DEBUG_TIMEASSERT_ON);
//		addTraceSetting(DEBUG_ALERT_ON);//JavaScriptでのどうしようも無い時のデバッグ用
		
		attr = ""+obj.getClass();
	}

	

	
	/**
	 * トレーサー
	 * オプションとして、このクラスで保持しているHTMLの内容に次々追記する。
	 */
	public void trace (String s) {
		
		if (!isDebug()) {
			return;//何もしない
		}
		
		if (isDebug()) {
			procDebugTrace(s);//出力を行う
		}
		
		try {
			if (isTraceSet(DEBUG_ALERT_ON)) {//アラートスイッチがあればアラートを出す
				Window.alert(attr + TRACE_MESSAGE + s);
			}
		} catch (Throwable t) {
			selfDebugTrace("trace_error_caused by DEBUG_ALERT_ON_"+t);
		}
		
		try {//サーバサイドから使用する際は、この機能は使えない。。。
			if (isTraceSet(DEBUG_EVENT_ON)) {//トレースイベントを発行する設定の場合、イベントを送付する。
//				ToDoAppDelegate.getDelegate().fireEvent(new DebugEvent(attr + TRACE_MESSAGE + s));
			}
		} catch (Throwable t) {
			selfDebugTrace("trace_error_caused by DEBUG_EVENT_ON_"+t);
		}
	}
	
	/**
	 * timeAssert
	 * 
	 * yy/MM/dd H:mm:ss　フォーマットで日時をセット、余暇時間をsec単位で入力、コメントを入れておくと、
	 * その時刻+sec後にこのメソッドが起動したタイミングでassert、Exceptionを発生させる
	 * sample: 11/08/07 14:34:43
	 * 
	 * @param timeString
	 * @param timeLimit
	 * @param comment
	 */
	public void timeAssert(String timeString, int timeLimit, String comment) {
		//11/08/07 14:34:43
		
		DateTimeFormat dForm = DateTimeFormat.getFormat("yy/MM/dd H:mm:ss");
		Date date1 = null;
		try {
			date1 = dForm.parseStrict(timeString);
		} catch (Exception e) {
			assertTrue(false, "not correct format for timeAssert! ex:"+"11/08/07 14:34:43");
		}
		assert date1 != null: assertTrue(false, "unknown error. maybe caused by not correct format for timeAssert! ex:"+"11/08/07 14:34:43");
		
		
		long timeMine = date1.getTime()+timeLimit*1000;
		
		Date dateDefault = new Date();
		long now = dateDefault.getTime();
		if (isTraceSet(DEBUG_TIMEASSERT_ON)) { 
			if (now < timeMine) {
				
				assertDebugTrace("", comment+"	/left: "+(timeMine - now)+"msec");
			} else {
				assertDebugTrace("*BOMB*",attr + ASSERT_MESSAGE + comment+"	/expired:	+"+(now - timeMine)+"msec before");
				if (isTraceSet(DEBUG_ASSERT_COLLECTION_ON)) {
					appendAssertCollectionBufferLine("*BOMB*"+	attr + ASSERT_MESSAGE + comment+"	/expired:	+"+(now - timeMine)+"msec before");
				} else {
					assertion("*BOMB*"+attr + ASSERT_MESSAGE + comment+"	/expired:	+"+(now - timeMine)+"msec before");
				}
					
			}
		}
		
	}
	
	
	
	
	
	
	private void appendAssertCollectionBufferLine(String string) {
		assertCollectionBuffer.add(assertCollectionBuffer.size()+" "+string);
	}



	/**
	 * アサーション内容
	 * @param string
	 */
	private void assertion(String string) {
		assert false : string;
		throw new RuntimeException(string);
	}



	/**
	 * デバッグモードを取得する
	 * @return the debug
	 */
	public boolean isDebug() {
		if (debug == DEBUG_TRUE) return true;
		else if (debug == DEBUG_FALSE) return false;
		
		selfDebugTrace("isDebug_ no reason through here");
		
		assert (false);
		return true;//到達する場合何かヤバいコード
	}

	/**
	 * デバッグモードのセットを行う
	 * @param debug the debug to set
	 */
	public void setDebug (int debug) {
		this.debug = debug;
	}
	
	
	/**
	 * デバッグモードを取得する
	 * @return int デバッグモード
	 */
	public int getDebug () {
		return this.debug;
	}

	
	
	
	
	
	
	
	/**
	 * トレースセッティングに条件を追加する
	 * @param debugSettingOn
	 */
	public void addTraceSetting(int debugSettingOn) {
		if (isTraceSet(debugSettingOn)) {
			//なにもしない
			selfDebugTrace("既に加算済み");
		} else {
			traceSetting |= debugSettingOn;
		}
		
		switch (debugSettingOn) {
		case DEBUG_ASSERT_COLLECTION_ON:
			assertCollectionBuffer = new ArrayList<String>();
			break;

		default:
			break;
		}
	}

	/**
	 * トレースセッティングから条件を削除する
	 * @param debugSettingOff
	 */
	public void removeTraceSetting (int debugSettingOff) {
		if (isTraceSet(debugSettingOff)) {
			traceSetting -= debugSettingOff;
		} else {
			//なにもしない
		}
	}
	
	/**
	 * トレースセッティングに該当のフラグがあるか無いかの確認結果を返す
	 * @param debugEventOn
	 * @return 指定フラグがセットされている場合True そうでなければFalse
	 */
	private boolean isTraceSet(int debugEventOn) {
		return (traceSetting & debugEventOn) != 0;
	}

	/**
	 * トレース設定を取得する
	 * @return the traceSetting
	 */
	public int getTraceSetting() {
		return traceSetting;
	}





	
	
	
	

	
	
	
	


	
	

	/**
	 * アサート
	 * 
	 * @param b
	 * @param string
	 * @return 問題無ければ
	 */
	public boolean assertTrue(boolean b, String string) {
		if (!b) {
			selfDebugTrace(attr + ASSERT_MESSAGE + string);
			if (isTraceSet(DEBUG_ASSERT_COLLECTION_ON)) {
				appendAssertCollectionBufferLine(attr + ASSERT_MESSAGE + string);
			} else {
				assertion(attr + ASSERT_MESSAGE + string);
			}
			
		}
		
		return true;
	}

	
	/**
	 * notNullをチェックする
	 * @param obj
	 * @param string
	 */
	public void assertNotNull(Object obj, String string) {
		if (obj != null) return;
		assertTrue(false, string);
	}

	
	
	/**
	 * デバッグ文字列の出力を行う。
	 * @param s
	 */
	private void procDebugTrace (String s) {
		if (isDebug()) System.out.println(attr + TRACE_MESSAGE + s);//デバッグがonなら、標準出力を行う
	}
	

	/**
	 * Debugクラス自体が発行する標準出力
	 * System.out.println();
	 * @param string 文字列
	 */
	private void selfDebugTrace(String string) {
		if (isDebug()) System.out.println("*seldDebug* "+attr + TRACE_MESSAGE + string);
	}
	
	/**
	 * timeAssertから発行する標準出力
	 * @param string
	 */
	private void assertDebugTrace (String head, String string) {
		if (isDebug()) System.out.println(head+"	*timeAssert* "+attr + TRACE_MESSAGE + string);
	}



	/**
	 * 特定の範囲のAssert文言を取得する
	 * @return
	 */
	public String assertCollection() {
		if (assertCollectionBuffer != null) {
			StringBuffer answer = new StringBuffer();
			for (String str : assertCollectionBuffer) {
				answer.append(str+"\n");
			}
			assertCollectionBuffer = null;
			return answer.toString();
		}
		return null;
	}


	/**
	 * 特定のJSONObjectの直下に、指定したKeyが存在している場合、Trueを返す。存在していない場合、Assertを発生させる。
	 * @param key
	 * @param root
	 * @return
	 */
	public boolean assertJSONNotNull(String key, JSONObject root) {
		assertNotNull(root, root+"	is null, no key	"+key);

		if (root.keySet().size() == 0) {
			assertTrue(false, root+"	is contains no key	"+key);
			return false;
		}
		if (root.containsKey(key)) {
			return true;
		}
		
		assertTrue(false, key+"	is not contained in	"+root);
		return false;
	}




	
}
