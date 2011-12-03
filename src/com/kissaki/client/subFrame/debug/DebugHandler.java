package com.kissaki.client.subFrame.debug;

import com.google.gwt.event.shared.EventHandler;


/**
 * デバッグクラスのハンドラ
 * @author sassembla
 *
 */
public class DebugHandler implements EventHandler {
	Debug debug = null;
	
	/**
	 * コンストラクタ
	 */
	public DebugHandler() {
		debug = new Debug(this);
//		debug.trace("DebugHandler_コンストラクタ");
	}

	
	/**
	 * イベントが発生した際に行う挙動
	 * ココに記述せず、イベントリスナ設定側でオーバーライドする対象
	 */
	public void doProcess(DebugEvent e) {
		debug.trace("doProcess");
	}
}
