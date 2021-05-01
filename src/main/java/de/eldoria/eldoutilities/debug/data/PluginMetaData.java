package de.eldoria.eldoutilities.debug.data;

public class PluginMetaData {
    protected String name;
    protected String version;
    protected boolean enabled;
    protected String main;
    protected String[] authors;
    protected String[] loadBefore;
    protected String[] dependencies;
    protected String[] softDependencies;
    protected String[] provides;

    public PluginMetaData(String name, String version, boolean enabled, String main, String[] authors, String[] loadBefore, String[] dependencies, String[] softDependencies, String[] provides) {
        this.name = name;
        this.version = version;
        this.enabled = enabled;
        this.main = main;
        this.authors = authors;
        this.loadBefore = loadBefore;
        this.dependencies = dependencies;
        this.softDependencies = softDependencies;
        this.provides = provides;
    }
}
