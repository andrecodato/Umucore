package br.com.umucraft.umucore.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Base para o arquivo de configuração de um módulo (ex: modules/auth.yml).
 * Cada módulo do UmuCore (auth, economy, etc.) deve ter sua própria classe
 * estendendo esta, mantendo a raiz da pasta de dados só com o config.yml
 * central do core.
 */
public abstract class ModuleConfig {

    private final JavaPlugin plugin;
    private final String caminhoRelativo;
    protected FileConfiguration config;

    protected ModuleConfig(JavaPlugin plugin, String caminhoRelativo) {
        this.plugin = plugin;
        this.caminhoRelativo = caminhoRelativo;
    }

    public void carregar() {
        File arquivo = new File(plugin.getDataFolder(), caminhoRelativo);
        if (!arquivo.exists()) {
            plugin.saveResource(caminhoRelativo, false);
        }
        this.config = YamlConfiguration.loadConfiguration(arquivo);
    }

    public void recarregar() {
        carregar();
    }
}
