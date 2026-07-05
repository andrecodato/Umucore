package br.com.umucraft.umucore;

import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.command.CommandManagement;
import br.com.umucraft.umucore.auth.data.AccountRepository;
import br.com.umucraft.umucore.auth.data.DatabaseManager;
import br.com.umucraft.umucore.auth.data.MojangApiClient;
import br.com.umucraft.umucore.auth.listener.PlayerAuthenticateListener;
import br.com.umucraft.umucore.auth.listener.PlayerGeneralListeners;
import br.com.umucraft.umucore.auth.listener.PlayerJoinListeners;
import br.com.umucraft.umucore.auth.listener.PlayerKickListeners;
import br.com.umucraft.umucore.config.ConfigManager;
import br.com.umucraft.umucore.logger.UmuLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class Umucore extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        UmuLogger.sucesso("Core", "Sistemas centrais inicializados com sucesso!");

        ConfigManager configManager = new ConfigManager(this);
        configManager.carregar();

        databaseManager = new DatabaseManager(getDataFolder());
        databaseManager.conectar();
        databaseManager.criarTabelas();

        AccountRepository accountRepository = new AccountRepository(databaseManager);
        MojangApiClient mojangApiClient = new MojangApiClient();
        AuthManager authManager = new AuthManager();

        // REGISTRO DE EVENTOS (LISTENERS)
        getServer().getPluginManager().registerEvents(
                new PlayerAuthenticateListener(authManager, accountRepository, configManager, mojangApiClient), this);
        getServer().getPluginManager().registerEvents(
                new PlayerJoinListeners(this, authManager, accountRepository, configManager), this);
        getServer().getPluginManager().registerEvents(
                new PlayerGeneralListeners(authManager), this);
        getServer().getPluginManager().registerEvents(
                new PlayerKickListeners(authManager), this);

        // REGISTRO DOS COMANDOS DE AUTH
        new CommandManagement(this, authManager, accountRepository, configManager, mojangApiClient).registrar();

        UmuLogger.info("Core", "Sistema de autenticação totalmente carregado.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.fechar();
        }
    }
}
