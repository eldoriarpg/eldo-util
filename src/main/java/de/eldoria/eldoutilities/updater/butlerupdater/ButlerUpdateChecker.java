package de.eldoria.eldoutilities.updater.butlerupdater;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import de.eldoria.eldoutilities.updater.Updater;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Updater implementation for butler application.
 *
 * @since 1.1.0
 */
public class ButlerUpdateChecker extends Updater<ButlerUpdateData> {
    private UpdateCheckResponse response;

    public ButlerUpdateChecker(ButlerUpdateData data) {
        super(data);
    }

    @Override
    protected Optional<String> getLatestVersion(ButlerUpdateData data) {
        Plugin plugin = data.plugin();

        HttpURLConnection con;
        try {
            URL url = new URL(data.host() + "/check?version=" + plugin.getDescription().getVersion() + "&id=" + data.butlerId() + "&devbuild=" + false);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
        } catch (IOException e) {
            plugin.getLogger().log(Level.FINEST, "Could not open connection.", e);
            return Optional.empty();
        }

        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try {
            if (con.getResponseCode() != 200) {
                plugin.getLogger().log(Level.FINEST, "Received non 200 request.");

                return Optional.empty();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.INFO, "Could not read response.", e);

            return Optional.empty();
        }


        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                builder.append(responseLine.trim());
            }
            response = new Gson().fromJson(builder.toString(), UpdateCheckResponse.class);
        } catch (IOException e) {
            plugin.getLogger().log(Level.FINEST, "Could not read response.", e);

            return Optional.empty();
        }

        return Optional.ofNullable(response.latestVersion());
    }

    @Override
    protected boolean update() {
        if (response == null) return false;
        Plugin plugin = getData().plugin();
        plugin.getLogger().info("§2>------------------------<");
        plugin.getLogger().info("§2> Performing auto update <");
        plugin.getLogger().info("§2>------------------------<");

        plugin.getLogger().info("§2Performing auto update.");
        URL url;
        try {
            url = new URL(getData().host() + "/download?id=" + getData().butlerId() + "&version=" + response.latestVersion());
        } catch (MalformedURLException e) {
            plugin.getLogger().log(Level.CONFIG, "Could not create download url.", e);
            plugin.getLogger().warning("§cAborting Update.");
            return false;
        }

        plugin.getLogger().info("§2Downloaded new file.");

        String plugins = plugin.getDataFolder().getParent();
        File updateDirectory = new File(Paths.get(plugins, "update").toString());
        if (!updateDirectory.exists()) {
            if (!updateDirectory.mkdirs()) {
                plugin.getLogger().warning("§cCould not create update directory.");
                plugin.getLogger().warning("§cAborting Update.");
                return false;
            }
        }

        // Get File of plugin
        Field fileField;
        try {
            fileField = JavaPlugin.class.getDeclaredField("file");
        } catch (NoSuchFieldException e) {
            plugin.getLogger().log(Level.WARNING, "§cCould not find field file in plugin.", e);
            plugin.getLogger().warning("§cAborting Update.");
            return false;
        }
        fileField.setAccessible(true);
        File pluginFile;
        try {
            pluginFile = (File) fileField.get(plugin);
        } catch (IllegalAccessException e) {
            plugin.getLogger().log(Level.WARNING, "Could not retrieve file of plugin.", e);
            return false;
        }

        File updateFile = Paths.get(updateDirectory.getAbsolutePath(), pluginFile.getName()).toFile();
        try (InputStream input = url.openStream()) {
            Files.copy(input, updateFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not create update file.", e);
            plugin.getLogger().warning("§cAborting Update.");
            return false;
        }

        String hash;
        plugin.getLogger().info("§2Calculate checksum.");
        try {
            hash = Hashing.sha256().hashBytes(Files.readAllBytes(updateFile.toPath())).toString();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create hash from update file.", e);
            plugin.getLogger().warning("§cAborting Update.");
            updateFile.delete();
            return false;
        }

        if (!hash.equals(response.hash())) {
            plugin.getLogger().warning("§cChecksums of update file is not as expected.");
            plugin.getLogger().warning("§cAborting Update.");
            updateFile.delete();
            return false;
        }
        plugin.getLogger().info("§2Checksums of update file is correct.");

        plugin.getLogger().info("§2File " + pluginFile.getName() + " will be replaced with the new version on next startup.");

        plugin.getLogger().info("§2>----------------------------------------------------<");
        plugin.getLogger().info("§2> Update downloaded. Please restart to apply update. <");
        plugin.getLogger().info("§2>----------------------------------------------------<");
        return true;
    }
}
