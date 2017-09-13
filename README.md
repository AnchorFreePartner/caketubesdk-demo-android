[![](https://jitpack.io/v/AnchorFreePartner/cake-tube-sdk-android.svg)](https://jitpack.io/#AnchorFreePartner/cake-tube-sdk-android)

GitHub project: https://github.com/AnchorFreePartner/caketubesdk-demo-android

# Anchorfree CakeTube VPN SDK demo for Android

This is a demo application for Android with basic usage of CakeTube VPN SDK.

# Adding SDK to project

1. Add the JitPack repository to your build file

```groovy
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

2. Add the dependency, see badge on top SDK version

```groovy
    dependencies {
        compile 'com.github.AnchorFreePartner:cake-tube-sdk-android:{VERSION}'
    }
```

Note: In case of conflicts, use `transitive = false` feature

# Usage and core classes

SDK contains two core classes that must be implemented via Builder.

1. AFClientService
2. AFConnectionService

## AFClientService

Base class that must be implemented as singleton is AFClientService.  
This instance manages client user: authentication, vpn credentials retrieval, user licensing info, session management.  
Session will be saved after first successful sign in and destroyed and cleaned up after logout.  

Example usage:

```java
    AFClientService api = AFClientService.newBuilder(Application_Context)
        .setCarrierId(YourCarrierId)
        .setConnectionRetries(3)
        .setHostUrl(YourHostUrl)
        .build();
```

Login may require OAuth Access Token and Authentication Method.
This example uses GitHub and Anonymous for demonstration.

```java
    api.login(oauthAccessToken, authMethod, new ResponseCallback<LoginResponse>() {
        @Override
        public void success(LoginResponse loginResponse) {
            // Process Response;
        }
    
        @Override
        public void failure(ApiException e) {
            // Handle Exception;
        }
    });
```

Example with anonymous login:
```java
    api.login(null, "anonymous", new ResponseCallback<LoginResponse>() {
        @Override
        public void success(LoginResponse loginResponse) {
            // Process Response;
        }
    
        @Override
        public void failure(ApiException e) {
            // Handle Exception;
        }
    });
```

LoginResponse can return different results.  
In case it's "OK", you can proceed with enabling VPN.

```java
    "OK".equalsIgnoreCase(loginResponse.getResult())
```

Otherwise, handle Result or Exception and try to login again.

## AFConnectionService  
This service wraps ServiceConnection which is used to communicate with VPN client service and 
manage connection.  

Service implementation requires these base Activity callbacks for implementation:

```java

    private AFConnectionService connectionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        connectionService = AFConnectionService.newBuilder(Activity_Context)
            .addConnectionCallbacksListener(new ConnectionCallbacksListener)
            .addVPNConnectionStateListener(new VPNConnectionStateListener)
            .setName(Application_name)
            .build();
        ...
    }
    
    @Override
    protected void onStart() {
        connectionService.onStart(); // NOTE: this is a mandatory call
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        connectionService.onStop(); // NOTE: this is a mandatory call
        super.onStop();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // NOTE: service needs to know if VPN permission been granted by user
        connectionService.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
```

Note: all these methods are mandatory for proper SDK work.  

After this setup u can connect to VPN with connect() call:

```java
    Protocol protocol = Protocol.AUTO;
    void connectVpn() {
        AFClientService clientService = ...;
        clientService.getCredentials(protocol, new ResponseCallback<CredentialsResponse>() {
            @Override
            public void success(CredentialsResponse credentialsResponse) {
                connectionService.connect(credentialsResponse, Activity.this);
            }
        
            @Override
            public void failure(ApiException e) {
                // Handle Exception
            }
        });
    }
```

Disconnect from VPN with:

```java
    private void disconnectVPN() {
        connectionService.disconnect();
    }
```

# Change country
Getting server list from AFConnectionService instance:

```java
    api.getServers(new ResponseCallback<ServersResponse>() {
        @Override
        public void success(ServersResponse serversResponse) {
            //Process Response
        }
    
        @Override
        public void failure(ApiException e) {
            //Handle Exception
        }
    });
```

ServerResponse contains list of ServerItem. Response must also be checked for "OK" status.  
ServerItem contains available information(like Country and Servers count) and is used to change configuration:

```java
    api.setCountry(serverItem);
```

Note: changing connection configuration requires to restart VPN service.

# Check remaining traffic limit

User can check remaining traffic limit if it is set:

```java
    api.getRemainingTraffic(new ResponseCallback<RemainingTrafficResponse>() {
        @Override
        public void success(RemainingTrafficResponse remainingTrafficResponse) {
            Long trafficLimit = remainingTrafficResponse.getTrafficLimit();
        }
        
        @Override
        public void failure(ApiException e) {
            // Handle Exception
        }
    });
```

RemainingTrafficResponse contains information about:  
 - Traffic start - beggining session time.  
 - Traffic limit - limit for traffic usage in bytes.  
 - Traffic used - used traffic for subscriber.  
 - Traffic remaining - remaining traffic in bytes traffic

# OAuth or Anonymous authorization

This example application uses two types of client authorization: with OAuth token and Anonymous.  
Usage:

```java
    api.login(oauthAccessToken, authMethod, new ResponseCallback<LoginResponse>() {
        @Override
        public void success(LoginResponse loginResponse) {
            Process Response;
        }
    
        @Override
        public void failure(ApiException e) {
            Handle Exception;
        }
    });
```

oauthAccessToken - valid token from OAuth server or null for anonymous.  

authMethod - one of the valid authentication methods:  
- "github", “facebook”, "twitter", "firebase" - for public authentication servers,  
- "anonymous" - for anonymous authentication,  
- "oauth" - for custom authentication server.

Log out user with:

```java
    api.logout(new ResponseCallback<LogoutResponse>() {
        @Override
        public void success(LogoutResponse logoutResponse) {    
            // Process Response;
        }

        @Override
        public void failure(ApiException e) {
            // Handle Exception;
        }
    });
```
