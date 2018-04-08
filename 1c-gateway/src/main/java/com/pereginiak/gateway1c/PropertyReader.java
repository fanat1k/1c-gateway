package com.pereginiak.gateway1c;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private Properties properties = new Properties();

    public PropertyReader(Context context) {
        initProperties(context);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void initProperties(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
