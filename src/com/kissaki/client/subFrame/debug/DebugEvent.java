package com.kissaki.client.subFrame.debug;

import com.google.gwt.event.shared.GwtEvent;




/**
 * イベント、デバッグ情報をスクリーンに出す
 * @author sassembla
 *
 */
public class DebugEvent extends GwtEvent<DebugHandler> {
	public static final Type<DebugHandler> TYPE = new Type<DebugHandler>();//ハンドラを特定するタイプ表記

	Debug debug = null;
	String debugMessage;
	
	
	/**
	 * コンストラクタ
	 * @param s 
	 */
	public DebugEvent (String s) {
		debug = new Debug(this);
//		debug.trace("DebugEvent_コンストラクタ");
		
		setDebugMessage(s);
	}
	
	
	
	/**
	 * 所持しているデバッグメッセージを返す
	 * @return the debugMessage
	 */
	public String getDebugMessage() {
		return debugMessage;
	}



	/**
	 * デバッグメッセージをセットする
	 * @param debugMessage the debugMessage to set
	 */
	public void setDebugMessage(String debugMessage) {
		this.debugMessage = debugMessage;
	}



	/**
	 * 実装、ハンドラの特定メソッドを起動する。
	 */
	@Override
	protected void dispatch(DebugHandler handler) {
		handler.doProcess(this);
	}

	
	
	/**
	 * タイプを返す
	 */
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<DebugHandler> getAssociatedType() {
		return TYPE;
	}



	
}
