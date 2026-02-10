package api.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();


    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties", e);
        }
    }

    public static String getProperty(String key) {
        //Приоритет 1 - переменная система baseApiUrl =.. - если есть, то обращаемся к ней
        String systemValue = System.getProperty(key);

        if (systemValue != null) {
            return systemValue;
        }

        //Если переменной системы нет, то
        //Приоритет 2 = переменная окружения baseApiUrl - BASEAPIURL
        //admin.username -> ADMIN_USERNAME
        String envKey = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            return envValue;
        }

        String ciOverride = getCiUiOverride(key);
        if (ciOverride != null) {
            return ciOverride;
        }

        //Приоритет 3 - это config.properties

        return INSTANCE.properties.getProperty(key);
    }

    private static String getCiUiOverride(String key) {
        if (!isCi()) {
            return null;
        }

        if ("uiBaseUrl".equals(key)) {
            return "http://frontend";
        }
        if ("uiRemote".equals(key)) {
            return "http://selenoid:4444/wd/hub";
        }

        return null;
    }

    private static boolean isCi() {
        return System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;
    }
}
