package com.northghost.afvclient.core;

import com.northghost.afvclient.R;
import java.util.HashMap;

public class CodeStrings {
    public static final String DEVICE_EXCEED = "DEVICE_EXCEED";
    public static final String DEVICES_EXCEED = "DEVICES_EXCEED";
    public static final String SESSIONS_EXCEED = "SESSIONS_EXCEED";
    public static final String SESSION_EXCEED = "SESSION_EXCEED";
    public static final String OK = "OK";

    private static final HashMap<String, Integer> codes = new HashMap<String, Integer>() {{
        put(DEVICE_EXCEED, R.string.devices_exceed);
        put(DEVICES_EXCEED, R.string.devices_exceed);
        put(SESSIONS_EXCEED, R.string.sessions_exceed);
        put(SESSION_EXCEED, R.string.sessions_exceed);
    }};

    public static int codeForStatus(final String status) {
        Integer res = codes.get(status);
        if (res == null) { return R.string.app_error; }

        return res;
    }
}
