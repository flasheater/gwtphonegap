/*
 * Copyright 2010 Daniel Kurka Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.gwtphonegap.client.geolocation.browser;

import com.google.gwt.core.client.Callback;
import com.google.gwt.geolocation.client.Geolocation.PositionOptions;
import com.google.gwt.user.client.Timer;
import com.googlecode.gwtphonegap.client.geolocation.Geolocation;
import com.googlecode.gwtphonegap.client.geolocation.GeolocationCallback;
import com.googlecode.gwtphonegap.client.geolocation.GeolocationOptions;
import com.googlecode.gwtphonegap.client.geolocation.GeolocationWatcher;
import com.googlecode.gwtphonegap.client.geolocation.PositionError;

public class GeolocationBrowserEmptyImpl implements Geolocation
{
  private final com.google.gwt.geolocation.client.Geolocation gwtGeoLocation;

  public GeolocationBrowserEmptyImpl()
  {
    gwtGeoLocation = com.google.gwt.geolocation.client.Geolocation.getIfSupported();
  }

  @Override
  public void getCurrentPosition( final GeolocationCallback callback )
  {
    getCurrentPosition( callback, null );
  }

  @Override
  public void getCurrentPosition( final GeolocationCallback callback, final GeolocationOptions options )
  {
    if ( gwtGeoLocation == null )
      callback.onFailure( new PostionErrorJavaImpl( PositionError.PERMISSION_DENIED, "" ) );
    else
      gwtGeoLocation
          .getCurrentPosition(
              new Callback<com.google.gwt.geolocation.client.Position, com.google.gwt.geolocation.client.PositionError>()
              {
                @Override
                public void onSuccess( final com.google.gwt.geolocation.client.Position result )
                {
                  final PositionBrowserImpl positionBrowserImpl = createPosition( result );
                  callback.onSuccess( positionBrowserImpl );
                }

                @Override
                public void onFailure( final com.google.gwt.geolocation.client.PositionError reason )
                {
                  callback.onFailure( new PostionErrorJavaImpl( reason.getCode(), reason.getMessage() ) );
                }
              }, convertToGwtOptions( options ) );
  }

  /**
   * Converts the {@link GeolocationOptions} to {@link PositionOptions}.
   * 
   * @param options gwt phonegap geolocation options
   * @return gwt geolocation options
   */
  private PositionOptions convertToGwtOptions( final GeolocationOptions options )
  {
    final PositionOptions gwtOptions = new PositionOptions();
    gwtOptions.setHighAccuracyEnabled( options.isEnableHighAccuracy() );
    gwtOptions.setMaximumAge( options.getMaximumAge() );
    gwtOptions.setTimeout( options.getTimeout() );
    return gwtOptions;
  }

  @Override
  public GeolocationWatcher watchPosition( final GeolocationOptions options,
                                           final GeolocationCallback callback )
  {
    if ( gwtGeoLocation == null )
      return new GeolocationWatcherGwtTimerImpl( options, callback );
    else
    {
      final com.google.gwt.geolocation.client.Geolocation.PositionOptions opt = new PositionOptions();
      opt.setHighAccuracyEnabled( true );
      opt.setMaximumAge( options.getMaximumAge() );
      opt.setTimeout( options.getTimeout() );
      final int watchPosition =
          fixGwtGeoLocation(
              new Callback<com.google.gwt.geolocation.client.Position, com.google.gwt.geolocation.client.PositionError>()
              {
                @Override
                public void onSuccess( final com.google.gwt.geolocation.client.Position result )
                {
                  final PositionBrowserImpl positionBrowserImpl = createPosition( result );
                  callback.onSuccess( positionBrowserImpl );
                }

                @Override
                public void onFailure( final com.google.gwt.geolocation.client.PositionError reason )
                {
                  callback.onFailure( new PostionErrorJavaImpl( reason.getCode(), reason.getMessage() ) );
                }
              }, opt );
      return new GwtLocationWatcher( watchPosition );
    }
  }

  /**
   * See issue http://code.google.com/p/google-web-toolkit/issues/detail?id=6834
   */
  // TODO remove this once gwt fixes the bug
  private native int fixGwtGeoLocation( Callback<com.google.gwt.geolocation.client.Position, com.google.gwt.geolocation.client.PositionError> callback,
                                        PositionOptions options ) /*-{
                                                                  var opt = @com.google.gwt.geolocation.client.Geolocation::toJso(*)(options);

                                                                  var success = $entry(function(pos) {
                                                                  @com.google.gwt.geolocation.client.Geolocation::handleSuccess(*)(callback, pos);
                                                                  });

                                                                  var failure = $entry(function(err) {
                                                                  @com.google.gwt.geolocation.client.Geolocation::handleFailure(*)(callback, err.code, err.message);
                                                                  });

                                                                  var id = -1;
                                                                  if (@com.google.gwt.geolocation.client.Geolocation::isSupported()) {
                                                                  id = $wnd.navigator.geolocation
                                                                  .watchPosition(success, failure, opt);
                                                                  }
                                                                  return id;
                                                                  }-*/;

  @Override
  public void clearWatch( final GeolocationWatcher watcher )
  {
    if ( watcher instanceof GeolocationWatcherGwtTimerImpl )
    {
      final GeolocationWatcherGwtTimerImpl timerImpl = (GeolocationWatcherGwtTimerImpl) watcher;
      timerImpl.cancel();
    }
    else
      if ( watcher instanceof GwtLocationWatcher )
      {
        final GwtLocationWatcher gwtLocationWatcher = (GwtLocationWatcher) watcher;
        gwtGeoLocation.clearWatch( gwtLocationWatcher.getId() );
      }
      else
        throw new IllegalArgumentException();
  }

  /**
   * @param result
   * @return
   */
  private PositionBrowserImpl createPosition( final com.google.gwt.geolocation.client.Position result )
  {
    final CoordinatesBrowserImpl co = new CoordinatesBrowserImpl();
    co.setAltitude( result.getCoordinates().getAltitude() != null ? result.getCoordinates().getAltitude() : 0 );
    co.setAltitudeAccuracy( result.getCoordinates().getAltitudeAccuracy() != null ? result.getCoordinates()
        .getAltitudeAccuracy() : 0 );
    co.setHeading( result.getCoordinates().getHeading() != null ? result.getCoordinates().getHeading() : 0 );
    co.setAccuracy( result.getCoordinates().getAccuracy() );
    co.setLatidue( result.getCoordinates().getLatitude() );
    co.setLongitude( result.getCoordinates().getLongitude() );
    co.setSpeed( result.getCoordinates().getSpeed() != null ? result.getCoordinates().getSpeed() : 0 );
    final PositionBrowserImpl positionBrowserImpl =
        new PositionBrowserImpl( co, Math.round( result.getTimestamp() ) );
    return positionBrowserImpl;
  }
  private class GwtLocationWatcher implements GeolocationWatcher
  {
    private final int id;

    public GwtLocationWatcher( final int id )
    {
      this.id = id;
    }

    public int getId()
    {
      return id;
    }
  }
  private class GeolocationWatcherGwtTimerImpl extends Timer implements GeolocationWatcher
  {
    private final GeolocationCallback callback;
    private final GeolocationOptions  options;

    public GeolocationWatcherGwtTimerImpl( final GeolocationOptions options,
                                           final GeolocationCallback callback )
    {
      this.callback = callback;
      this.options = options;
      schedule( options.getFrequency() );
    }

    @Override
    public void run()
    {
      schedule( options.getFrequency() );
      callback.onFailure( new PostionErrorJavaImpl( PositionError.PERMISSION_DENIED, "" ) );
    }
  }
}
