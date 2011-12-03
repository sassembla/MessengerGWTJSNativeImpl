package com.kissaki.client;

import com.kissaki.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MessengerGWTWIthHype implements EntryPoint {
	

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		Timer timer = new Timer() {
			
			@Override
			public void run() {
				for (int i = 0; i < 8; i++) {
					runH(i);
				}
				cancel();
			}
		};
		timer.schedule(1000);
		
	}
	
	
	
	/**
	 * 実行
	 */
	public native void runH(int index) /*-{
		var names = $wnd.HYPE.documents["rotating"].sceneNames();
    	$wnd.HYPE.documents["rotating"].showSceneNamed(names[index]);
	}-*/;
}

