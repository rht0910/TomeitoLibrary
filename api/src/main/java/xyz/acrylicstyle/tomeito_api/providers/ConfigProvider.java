package xyz.acrylicstyle.tomeito_api.providers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.SneakyThrow;
import xyz.acrylicstyle.tomeito_api.utils.Log;

@SuppressWarnings("unused")
public class ConfigProvider extends YamlConfiguration {
    @NotNull
    public final File file;
    @NotNull
    public final String path;

    public ConfigProvider(@NotNull File file) {
        this(file.getAbsolutePath());
    }

    public ConfigProvider(@NotNull String path) {
        this(path, false);
    }

    /**
     * @param path the path to the configuration file
     * @param disableConstructor whether disable the constructor or not. mainly for classes that extends this class.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigProvider(@NotNull String path, boolean disableConstructor) {
        if (disableConstructor) throw new UnsupportedOperationException();
        this.path = path;
        this.file = new File(this.path);
        // avoid some dangerous situation (e.g. there is a file or directory and accidentally deletes it)
        if (!this.file.exists()) {
            this.file.mkdirs(); // creates directory(ies) including file name
            this.file.delete(); // deletes file/directory but not parent directory
            try {
                this.file.createNewFile(); // then finally create a file
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.load(this.file);
    }

    /**
     * @param path relative or absolute path from the spigot.jar
     * @return ConfigProvider
     */
    @Nullable
    public static ConfigProvider initWithoutException(@NotNull String path) {
        try {
            return new ConfigProvider(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param file An configuration file
     * @return ConfigProvider
     */
    @Nullable
    public static ConfigProvider initWithoutException(@NotNull File file) {
        try {
            return new ConfigProvider(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    public static ConfigProvider getConfig(Plugin plugin, String file) {
        return getConfig(new File(plugin.getDataFolder(), file));
    }

    @NotNull
    public static ConfigProvider getConfig(File file) {
        ConfigProvider c = initWithoutException(file);
        if (c == null) throw new NullPointerException();
        return c;
    }

    @NotNull
    public static ConfigProvider getConfig(String path) {
        ConfigProvider c = initWithoutException(path);
        if (c == null) throw new NullPointerException();
        return c;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getConfigSectionValue(@Nullable Object o, boolean deep) {
        if (o == null) return null;
        if (o instanceof ConfigurationSection) {
            return ((ConfigurationSection) o).getValues(deep);
        } else if (o instanceof Map) {
            return (Map<String, Object>) o;
        }
        return null;
    }

    public Map<String, Object> getConfigSectionValue(@NotNull String path, boolean deep) {
        Object o = this.get(path, new HashMap<String, Object>());
        return getConfigSectionValue(o, deep);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void reload() {
        try {
            if (!this.file.exists()) this.file.createNewFile();
            this.load(this.file);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void save() {
        try {
            this.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setThenSave(@NotNull String path, @Nullable Object value) {
        this.set(path, value);
        this.save();
    }

    @Deprecated
    public void setThenSaveWithoutException(String path, Object value) {
        setThenSave(path, value);
    }

    @Override
    public void load(File file) {
        try {
            super.load(file);
        } catch (IOException | InvalidConfigurationException | RuntimeException e) {
            Log.error("An error occurred while loading a config file: " + file.getAbsolutePath());
            SneakyThrow.sneaky(e);
        }
    }

    public static void setThenSave(String path, Object value, File file) {
        try {
            YamlConfiguration config = new YamlConfiguration();
            if (!file.exists()) config.save(file);
            config.load(file);
            config.set(path, value);
            config.save(file);
        } catch (IOException | InvalidConfigurationException e) {
            Log.error("An error occurred while saving a config file: " + file.getAbsolutePath());
            SneakyThrow.sneaky(e);
        }
    }

    public static Boolean getBoolean(String path, Boolean def, String pluginName) {
        return getBoolean(path, def, new File("./plugins/" + pluginName + "/config.yml"));
    }

    public static Boolean getBoolean(String path, Boolean def, File file) {
        return getConfig(file).getBoolean(path, def);
    }

    public static String getString(String path, String def, String pluginName) {
        return getString(path, def, new File("./plugins/" + pluginName + "/config.yml"));
    }

    public static String getString(String path, String def, File file) {
        return getConfig(file).getString(path, def);
    }

    public static void setThenSave(String path, Object value, String pluginName) {
        setThenSave(path, value, new File("./plugins/" + pluginName + "/config.yml"));
    }
}
