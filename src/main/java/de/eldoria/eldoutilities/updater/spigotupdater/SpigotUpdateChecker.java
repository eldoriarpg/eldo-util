package de.eldoria.eldoutilities.updater.spigotupdater;

import de.eldoria.eldoutilities.updater.Updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Updater implementation for spigot update check.
 *
 * @since 1.0.0
 */
public final class SpigotUpdateChecker extends Updater<SpigotUpdateData> {
    public SpigotUpdateChecker(SpigotUpdateData data) {
        super(data);
    }

    @Override
    protected Optional<String> getLatestVersion(SpigotUpdateData data) {
        HttpURLConnection con;
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + data.getSpigotId());
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
        } catch (IOException e) {
            data.plugin().getLogger().log(Level.WARNING, "Request to spigotmc.org failed.", e);
            return Optional.empty();
        }

        StringBuilder newestVersionRequest = new StringBuilder();
        try (InputStream stream = con.getInputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
            String inputLine = in.readLine();
            while (inputLine != null) {
                newestVersionRequest.append(inputLine);
                inputLine = in.readLine();
            }
        } catch (IOException e) {
            data.plugin().getLogger().log(Level.WARNING, "Could not read response from spigotmc.org", e);
            return Optional.empty();
        }

        return Optional.of(newestVersionRequest.toString());
    }
}