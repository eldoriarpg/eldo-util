/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

class JacksonConfigTest extends JavaPlugin {

    @Override
    public void onEnable(){
        // Defining the key for the config.yml
        ConfigKey<ConfigFile> defKey = ConfigKey.defaultConfig(ConfigFile.class, ConfigFile::new);
        var config = new JacksonConfig<>(this, defKey);
        // Getting the config.yml data
        ConfigFile general = config.main();

        // Defining a second config key for database.yml
        ConfigKey<Database> databaseConfig = ConfigKey.of("Database Config", Path.of("database.yml"), Database.class, Database::new);
        // Loading the file and creating it, if it doesn't exist yet
        Database database = config.secondary(databaseConfig);
        // Retrieving a wrapped instance which will be updated during config reloads
        Wrapper<Database> databaseWrapped = config.secondaryWrapped(databaseConfig);

        // Reloading the config with the key
        config.reload(databaseConfig);
        // Reloading all configuration files
        config.reload();

        // Saving a specific file
        config.save(databaseConfig);
        // Saving all files
        config.save();
    }

public class ConfigFile {
    private Database database = new Database();
    private General general = new General();

    public Database database() {
        return database;
    }

    public General general() {
        return general;
    }
}

public class Database {
    private String host = "localhost";
    private int port = 3306;
    private String user = "root";
    private String password = "root";

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }
}

public class General{
    private String language = "en_US";
    private String prefix = "[Plugin]";

    public String language() {
        return language;
    }

    public String prefix() {
        return prefix;
    }
}
}
