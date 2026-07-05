package br.com.umucraft.umucore.config;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.logger.UmuLogger;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Configurações centrais do UmuCore (config.yml na raiz da pasta de
 * dados). Configurações específicas de cada módulo (auth, economy, etc.)
 * ficam em suas próprias classes que estendem ModuleConfig.
 */
public class ConfigManager {

    private final Umucore plugin;
    private FileConfiguration config;

    public ConfigManager(Umucore plugin) {
        this.plugin = plugin;
    }

    /**
     * Carrega (ou recarrega) o config.yml do disco.
     */
    public void carregar() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        UmuLogger.setDebug(config.getBoolean("debug", false));
    }

    public void recarregar() {
        carregar();
    }

    public boolean mostrarBanner() {
        return config.getBoolean("mostrar-banner", true);
    }

    public List<String> contatosSuporte() {
        return config.getStringList("suporte.contatos");
    }
}
