package com.kissaki.client.MessengerGWTCore.MessageCenter;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 受信したメッセージについて、実行内容自体が記述してあるイベントオブジェクト
 * @author ToruInoue
 */
public class MessageReceivedEvent extends GwtEvent<MessageReceivedHandler> {

    public static Type<MessageReceivedHandler> MESSAGE_RECEIVED_EVENT_TYPE = new Type<MessageReceivedHandler>();

    private final String message;

    public MessageReceivedEvent(String message) {
        this.message = message;
    }


    /**
     * メッセージのゲッター
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * イベント発行時の処理を行う
     */
	@Override
	protected void dispatch(MessageReceivedHandler handler) {
		handler.onMessageReceived(this);
	}


	/**
	 * イベント固有の型情報を返す
	 */
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<MessageReceivedHandler> getAssociatedType() {
		return MESSAGE_RECEIVED_EVENT_TYPE;
	}
}