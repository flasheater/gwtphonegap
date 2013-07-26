/*
 * Copyright 2010 Daniel Kurka
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.gwtphonegap.client.notification;

/**
 * Visual, audible, and tactile device notifications.
 * 
 * @author Daniel Kurka
 * 
 */
public class NotificationMobileImpl implements Notification {

  private static final AlertCallback emptyCallback = new AlertCallback() {

    @Override
    public void onOkButtonClicked() {

    }
  };
  private static final String[] defaultLabels = new String[] { "Ok", "Cancel" };

  public NotificationMobileImpl() {

  }

  @Override
  public native void beep(int count)/*-{
		$wnd.navigator.notification.beep(count);
	}-*/;

  @Override
  public native void vibrate(int milliseconds)/*-{
		$wnd.navigator.notification.vibrate(milliseconds);
	}-*/;

  @Override
  public void alert(final String message) {
    alert(message, emptyCallback);

  }

  @Override
  public void alert(final String message, final AlertCallback callback) {
    alert(message, callback, "Alert");

  }

  @Override
  public void alert(final String message, final AlertCallback callback, final String title) {
    alert(message, callback, title, "Ok");

  }

  @Override
  public native void alert(String message, AlertCallback callback, String title, String buttonName) /*-{

		var cal = function() {
			callback.@com.googlecode.gwtphonegap.client.notification.AlertCallback::onOkButtonClicked()();
		};

		$wnd.navigator.notification.alert(message, $entry(cal), title,
				buttonName);

	}-*/;

  @Override
  public void confirm(final String message, final ConfirmCallback callback) {
    confirm(message, callback, "Title");

  }

  @Override
  public void confirm(final String message, final ConfirmCallback callback, final String title) {

    confirm(message, callback, title, defaultLabels);

  }

  @Override
  public void confirm(final String message, final ConfirmCallback callback, final String title, final String[] buttonLabels) {
    String[] labels = buttonLabels;
    if ( labels == null )
    {
      labels = defaultLabels;
    }

    final StringBuffer buffer = new StringBuffer();

    for ( int i = 0; i < labels.length; i++ )
    {
      buffer.append( labels[ i ] );

      if ( i != labels.length - 1 )
      {
        buffer.append(",");
      }

    }

    final String flatLabels = buffer.toString();
    confirm0( message, callback, title, flatLabels );

  }

  private native void confirm0(String message, ConfirmCallback callback, String title, String buttonLabels) /*-{

		var cal = function(button) {
			callback.@com.googlecode.gwtphonegap.client.notification.ConfirmCallback::onConfirm(I)(button);
		};

		$wnd.navigator.notification.confirm(message, $entry(cal), title,
				buttonLabels);
	}-*/;
}
