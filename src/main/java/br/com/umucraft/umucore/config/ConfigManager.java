package br.com.umucraft.umucore.config;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.logger.UmuLogger;
import org.bukkit.configuration.file.FileConfiguration;

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

    public int timeoutLoginSegundos() {
        return config.getInt("auth.timeout-login-segundos", 30);
    }

    public boolean ipAutoLoginHabilitado() {
        return config.getBoolean("auth.ip-auto-login.habilitado", true);
    }

    public int ipAutoLoginJanelaMinutos() {
        return config.getInt("auth.ip-auto-login.janela-minutos", 15);
    }

    public boolean premiumHabilitado() {
        return config.getBoolean("auth.premium.habilitado", true);
    }

    public int senhaMinima() {
        return config.getInt("auth.registro.senha-minima", 4);
    }
}
