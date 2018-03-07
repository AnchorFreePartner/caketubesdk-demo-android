[![](https://jitpack.io/v/AnchorFreePartner/cake-tube-sdk-android.svg)](https://jitpack.io/#AnchorFreePartner/cake-tube-sdk-android)

# Get API Key

In order to be able to use the SDK the following steps have to be done:

1. Register an account at [developer.anchorfree.com](https://developer.anchorfree.com)
2. Create a project
3. Use SDK with a **carrier_id** equals to the given *PublicKey*.

## Changelog

- [HydraSDK for Android changelog](https://github.com/AnchorFreePartner/caketubesdk-demo-android/blob/master/CHANGES.md)

# Prerequisites

- Android 4+ (API 14+).
- armeabi or x86 compatible device
- VpnService support (most of Android versions)

# Installing

## Gradle

Add jitpack repo to root **build.gradle**:

```groovy
repositories {
    maven {
        url "https://jitpack.io"        
    }
}
```

Add sdk dependency to project **build.gradle**. Version name is available on top of this document.

```groovy
    compile 'com.github.AnchorFreePartner:cake-tube-sdk-android:{VERSION_NAME}'
```

Setup Android Advertising ID by including the Google Play Services 4.0+ into your project and add the following line to your project's AndroidManifest.xml as a child of the <application> tag:

```xml
<application>
    <meta-data
        android:name="com.google.android.gms.version"
        android:value="@integer/google_play_services_version" />
</application>
```

Add following permissions to your manifest:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

Minimum permissions required (INTERNET, ACCESS_NETWORK_STATE) are automatically added to your manifest.

## Preparing your app for release
If you are using proguard, add these rules to your proguard config (usually proguard-rules.pro file):

```
-dontwarn android.security.**
-dontwarn com.quantcast.**
-dontwarn com.squareup.okhttp.**
-dontwarn javax.mail.**
-dontwarn okio.**
-dontwarn retrofit.**
-keep class ch.qos.** { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context); }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { java.lang.String getId(); boolean isLimitAdTrackingEnabled(); }
-keep class com.google.gson.** { *; }
-keep class com.google.inject.* { *; }
-keep class com.northghost.** { *; }
-keep class com.quantcast.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep class de.blinkt.openvpn.** { *; }
-keep class javax.inject.* { *; }
-keep class org.apache.http.* { *; }
-keep class org.apache.http.**
-keep class org.apache.james.mime4j.* { *; }
-keep class retrofit.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep interface com.northghost.**
-keep interface com.squareup.okhttp.** { *; }
-keep public class com.google.android.gms.ads.identifier.** { *; }
-keepattributes *
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * { @retrofit.http.* <methods>; }
-keep class com.anchorfree.hydrasdk.api.** {*;}
```

# Android library API

There are two main services created for User Management and VPN connections. 

- AFClientService – general user management, sign in / sign out, credentials management
- AFConnectionService – manage VPN

## Authentication

Anchorfree Partner VPN Backend supports different authentication methods:
- Anonymous
- OAuth 2. Partner Backend should be pre-configured. See information below.
- Firebase. Partner Backend should be pre-configured with application ID.
- Social networks (Facebook, Google+, Twitter, GitHub). Partner Backend should be pre-configured with application ID.

To be able to use your own OAuth provider, you will need:

- Deploy and configure OAuth service. Service should be publicly available in Internet.
- Configure Partner Backend to use OAuth service.
- Implement OAuth client for your application.
- Retrieve access token in your app, this token will be used to initialize and sign in Android Partner SDK (see login).

# API reference

## Managing user, configuring application

### AFClientService.Builder

```java
/*

AFClientServcie configuration.

*/

// Create new builder instance within android application Context.
public Builder(Application application);

// Mandatory. Backend API host. See Prerequisites step.
public Builder setHostUrl(String url);

// Mandatory. Unique carrier identifier. See Prerequisites step.
public Builder setCarrierId(String carrierId);

// Maximum connection retries to one backend host. Default value is 2.
public Builder setConnectionRetries(int count);

// Maximum timeout for API calls, in seconds. Default is 15.
public Builder setTimeout(int timeout);

//Enabled sdk logging with Timber.
public Builder setDebugLogging(boolean val);

// Notification provider / delegate. Used from VPN process to notify users about VPN status.
public Builder setVPNNotificationProvider(VPNNotificationProvider provider);

// Creates AFClientService
public AFClientService build();

```

### AFClientService

```java
// Creates AFClientService.Builder configurator instance to setup AFClientService.
public static Builder newBuilder(Application application);

// User sign in with OAuth access token.
// oauthAccessToken - access token for selected auth method or `null` if authentication is of "anonymous" method.
// authMethod - "anonymous", "oauth", "firebase", "facebook", "google", "twitter", "github"
public void login(final String oauthAccessToken, final String authMethod, final ResponseCallback<LoginResponse> cb);

// Returns authentication status. `true` if logged in, `false` otherwise. If user is authenticated it’s OK to use rest of the functions.
public boolean isLoggedIn();

// Sets the SDK to pause VPN connection when device screen is off.
public void setPauseVPNWhenScreenOff(boolean pause);

// Disconnects VPN when the network is changed. Default value: false.
public void setDisconnectWhenNetworkChanged(boolean disconnect);

// Retrieve VPN credentials object. Credentials are cached on client-side for 24 hours. If they are expired - credential request will be sent to backend. User needs to be authorized. 
public void getCredentials(final ResponseCallback<CredentialsResponse> cb);

// Get remaining traffic information. Use if there is a traffic limit for a user.
public void getRemainingTraffic(final ResponseCallback<RemainingTrafficResponse> callback);

// Retrieve User's profile/subscriber information including current plan.
public void getSubscriber(final ResponseCallback<SubscriberResponse> callback);

// Log out and destroy session information. User will be deauthorized and session will be destroyed after invocation.
public void logout(final ResponseCallback<LogoutResponse> cb);

// Retrieve list of available servers.
public void getServers(final ResponseCallback<ServersResponse> callback);

// Retrieve list of available servers for protocol
public void getServers(final Protocol protocol, final ResponseCallback<ServersResponse> callback);

```

## Managing VPN connections

### AFConnectionService.Builder

```java
/*
Configures and connects remote VPN service to your application.
 */

// Sets connection callbacks listener. Connection callbacks are called while service is connected 
// or disconnected to your activity.
public Builder addConnectionCallbacksListener(ServiceConnectionCallbacks listener);
 
// Sets Android’s status bar notification icon. Only if VPNNotification provider is not set.
public Builder setNotificationIcon(int resId);

// Sets VPN connection name in status bar notifications. Only if VPNNotification provider is not set.
public Builder setName(String vpnName);

// Sets connection state listener. Connection state listener will update VPN connection state info.
public Builder addVPNConnectionStateListener(VPNConnectionStateListener listener);

// Creates AFConnectionService instance based on your parameters.
public AFConnectionService build();

```

### AFConnectionService

```java
/*
Manages device's VPN connection. To establish VPN connection user should manually add client 
certificate to their device: will be asked once with service configuration.

This service wraps ServiceConnection which is used to communicate with VPN client service and 
manage connection. You should call onStart and onStop services’ methods in activity. To embed 
Android VPN confirmation dialog you should also call onActivityResult method in your activity. 
Service configured with a builder (See AFConnectionService.Builder).
NOTE: to handle VPN connection state you must implement proper notification handling. 
See VPNConnectionState.

*/

// Creates Builder instance to configure and create AFConnectionService. 
// Context - is your activity context.
public static Builder newBuilder(Context context);

// Called on Activity onStart. Required.
public void onStart();

// Called on Activity onStop. Required.
public void onStop();

// Called on Activity onDestroy. This is recommended call.
public void onDestroy();

// Called on Activity onActivity result. Required.
public void onActivityResult(int requestCode, int resultCode, Intent data);


/*
Primary connection method. This method will configure and connect application's VPN (Android asks 
user to confirm VPN Permission on first run). Activity is required argument, if there were no VPN 
Permission request before. When SDK requires VPN Permission from user, you will receive 
onVPNPermissionRequested callback. If activity was null, you have to call this function once again,
but providing it correct activity. Your activity method onActivityResult 
SHOULD call AFConnectionService onActivityResult instance method. 
For more info see VPNConnectionStateListener. 
*/
public void connect(CredentialsResponse credentials, Activity activity)

// Disconnects from current connection.
public void disconnect();

// Receives VPN traffic stats object. If connection is inactive, traffic data will be zeroes. 
// See class TrafficStats.
public TrafficStats getTrafficStats(); 

```

### VPNConnectionState

```java
// Enum containing basic VPN connection states. To receive and handle notification you should be subscribed to service connection callbacks (see ServiceConnectionCallbacks).

public enum VPNConnectionState {NOT_CONNECTED, CONNECTING, CONNECTED}
```

### TrafficStats

```java

// Returns in and out bytes for current session (for the last hour). Will be zero, if connection is not active.
public long getBytesIn();
public long getBytesOut();

// Returns VPN start time (UNIX) in milliseconds.
public long getStartTime();

```

### ServiceConnectionCallbacks

```java
// Called when AFConnectionService is connected to activity. After this callback service 
//is ready to manage VPN connection.
public void onConnected()

// Called when AFConnectionService is disconnected from activity. cause - is a code identifying 
//reason service is disconnected.
public void onConnectionSuspended(int cause)

```

### VPNConnectionStateListener

```java
// Called when AFConnectionService VPN connection state changes. state - is a new VPN connection state.
public void onVPNConnectionStateChanged(VPNConnectionState state)

// Called when SDK requires explicit on-demand VPN Permission from user.
void onVPNPermissionRequested();

// Called when SDK received permission or deny from user.
void onVPNPermissionGranted(boolean granted);
```

## VPNNotificationProvider
```
// Overrides default notification with your own, message contains VPN connection status strings if VPN is connecting, or traffic counters string if VPN is already connected
Notification createVPNNotification(String message, String ticker, boolean lowPriority, long when, VpnStatus.ConnectionStatus status);
```

## Handling errors

All methods have two callback functions: onSuccess, onFailure. Failure callback comes with 
Throwable object which is a good start to check what went wrong. There are several types of 
exceptions which may be generated during API methods request.

- **InternalErrorException** - Something went wrong on the server side. General error.
- **NetworkRelatedException** - There was a network error during request: timeout, no connection, etc.
- **ServerUnavailableException** - Happens when client requested credentials from VPN node which is 
offline or unavailable.
- **TrafficExceedException** - User’s traffic limit is exceeded.
- **SessionsExceedException** - Too many sessions from one client.
- **UnauthorizedException** - User is unauthorized for that request or not logged in.




