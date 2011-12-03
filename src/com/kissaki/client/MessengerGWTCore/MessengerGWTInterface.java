package com.kissaki.client.MessengerGWTCore;

/**
 * 実装すべきメソッドについて記述されたインターフェース
 * 
 * MessengerGWTIplementsで実装すべきメソッドを実装している。
 * Messengerを使うクラスは、このインターフェースを実装する必要があるようにしたい。
 * TODO その縛り
 * @author ToruInoue
 *
 */
public interface MessengerGWTInterface {
	final int MESSENGER_STATUS_NULL = 0;
	final int MESSENGER_STATUS_READY_FOR_INITIALIZE = 1;
	final int MESSENGER_STATUS_OK = 2;
	final int MESSENGER_STATUS_REMOVED = 3;
	
	final int MESSENGER_STATUS_NOT_SUPPORTED = -1;
	final int MESSENGER_STATUS_FAILURE = -2;
	
	abstract void receiveCenter(String message);
}
