package br.com.umucraft.umucore.auth.command;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.command.executors.AuthAdminCommand;
import br.com.umucraft.umucore.auth.command.executors.ChangePasswordCommand;
import br.com.umucraft.umucore.auth.command.executors.LoginCommand;
import br.com.umucraft.umucore.auth.command.executors.OfflineCommand;
import br.com.umucraft.umucore.auth.command.executors.PremiumCommand;
import br.com.umucraft.umucore.auth.command.executors.RegisterCommand;
import br.com.umucraft.umucore.auth.command.executors.UnregisterCommand;
import br.com.umucraft.umucore.auth.config.AuthConfig;
import br.com.umucraft.umucore.auth.data.AccountRepository;
import br.com.umucraft.umucore.auth.data.MojangApiClient;
import br.com.umucraft.umucore.config.ConfigManager;
import br.com.umucraft.umucore.logger.UmuLogger;

/**
 * Registra todos os comandos do módulo de autenticação nos executors da
 * nova arquitetura, ligando-os aos comandos declarados no plugin.yml.
 */
public class CommandManagement {

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final ConfigManager configManager;
    private final AuthConfig authConfig;
    private final MojangApiClient mojangApiClient;

    public CommandManagement(Umucore plugin, AuthManager authManager, AccountRepository accountRepository,
                              ConfigManager configManager, AuthConfig authConfig, MojangApiClient mojangApiClient) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.accountRepository = accountRepository;
        this.configManager = configManager;
        this.authConfig = authConfig;
        this.mojangApiClient = mojangApiClient;
    }

    public void registrar() {
        plugin.getCommand("login").setExecutor(new LoginCommand(plugin, authManager, accountRepository));
        plugin.getCommand("register").setExecutor(new RegisterCommand(plugin, authManager, accountRepository, authConfig));
        plugin.getCommand("unregister").setExecutor(new UnregisterCommand(plugin, authManager, accountRepository, authConfig));
        plugin.getCommand("changepassword").setExecutor(new ChangePasswordCommand(plugin, authManager, accountRepository, authConfig));
        plugin.getCommand("premium").setExecutor(new PremiumCommand(plugin, authManager, accountRepository, authConfig, mojangApiClient));
        plugin.getCommand("offline").setExecutor(new OfflineCommand(plugin, authManager, accountRepository, authConfig));
        plugin.getCommand("umuauth").setExecutor(new AuthAdminCommand(plugin, authManager, accountRepository, configManager, authConfig));

        UmuLogger.info("Comandos", "Todos os comandos de autenticação foram registrados.");
    }
}
