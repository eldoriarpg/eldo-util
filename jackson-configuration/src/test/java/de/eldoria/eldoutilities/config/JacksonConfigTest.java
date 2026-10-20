/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config;

import dev.chojo.ocular.components.Wrapper;
import dev.chojo.ocular.key.Key;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

class JacksonConfigTest extends JavaPlugin {

    @Override
    public void onEnable(){
        Key<ConfigFile> defKey = Key.builder(Path.of("config.yml"), ConfigFile::new).build();
        var config = new JacksonConfig<>(this, defKey);
        ConfigFile general = config.main();

        Key<Database> databaseConfig = Key.builder(Path.of("database.yml"), Database::new).name("Database Config").build();
        Database database = config.secondary(databaseConfig);
        Wrapper<Database> databaseWrapped = config.secondaryWrapped(databaseConfig);

        config.reload(databaseConfig);
        config.reload();

        config.save(databaseConfig);
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
