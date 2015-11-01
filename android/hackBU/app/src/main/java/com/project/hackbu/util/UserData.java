package com.project.hackbu.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alqiluo on 2015-10-31.
 */
public class UserData {

    private static UserData userData;

    private Map<String, Object> data;

    private UserData() {
        data = new HashMap<>();
    }

    public static UserData getInstance() {
        if(userData == null)
            userData = new UserData();
        return userData;
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public void setData(String key, Object object) {
        data.put(key, object);
    }

}
