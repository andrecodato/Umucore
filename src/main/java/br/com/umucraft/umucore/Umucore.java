package br.com.umucraft.umucore;

import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.command.CommandManagement;
import br.com.umucraft.umucore.auth.config.AuthConfig;
import br.com.umucraft.umucore.auth.data.AccountRepository;
import br.com.umucraft.umucore.auth.data.DatabaseManager;
import br.com.umucraft.umucore.auth.data.MojangApiClient;
import br.com.umucraft.umucore.auth.listener.PlayerAuthenticateListener;
import br.com.umucraft.umucore.auth.listener.PlayerGeneralListeners;
import br.com.umucraft.umucore.auth.listener.PlayerJoinListeners;
import br.com.umucraft.umucore.auth.listener.PlayerKickListeners;
import br.com.umucraft.umucore.config.ConfigManager;
import br.com.umucraft.umucore.logger.Banners;
import br.com.umucraft.umucore.logger.UmuLogger;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Umucore extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        configManager.carregar();

        if (configManager.mostrarBanner()) {
            UmuLogger.banner(Banners.UMUCORE, NamedTextColor.DARK_PURPLE, "Core v" + getPluginMeta().getVersion());
        }
        UmuLogger.sucesso("Core", "Sistemas centrais inicializados com sucesso!");

        AuthConfig authConfig = new AuthConfig(this);
        authConfig.carregar();

        databaseManager = new DatabaseManager(getDataFolder());
        databaseManager.conectar();
        databaseManager.criarTabelas();

        AccountRepository accountRepository = new AccountRepository(databaseManager);
        MojangApiClient mojangApiClient = new MojangApiClient();
        AuthManager authManager = new AuthManager();

        if (configManager.mostrarBanner()) {
            UmuLogger.banner(Banners.UMUAUTH, NamedTextColor.LIGHT_PURPLE, "Módulo de Autenticação");
        }

        // REGISTRO DE EVENTOS (LISTENERS)
        getServer().getPluginManager().registerEvents(
                new PlayerAuthenticateListener(authManager, accountRepository, authConfig, mojangApiClient), this);
        getServer().getPluginManager().registerEvents(
                new PlayerJoinListeners(this, authManager, accountRepository, authConfig), this);
        getServer().getPluginManager().registerEvents(
                new PlayerGeneralListeners(authManager), this);
        getServer().getPluginManager().registerEvents(
                new PlayerKickListeners(authManager), this);

        // REGISTRO DOS COMANDOS DE AUTH
        new CommandManagement(this, authManager, accountRepository, configManager, authConfig, mojangApiClient).registrar();

        UmuLogger.info("Core", "Sistema de autenticação totalmente carregado.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.fechar();
        }
    }
}
