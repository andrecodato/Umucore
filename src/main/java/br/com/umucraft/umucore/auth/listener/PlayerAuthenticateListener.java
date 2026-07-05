package br.com.umucraft.umucore.auth.listener;

import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.PreAuthDecision;
import br.com.umucraft.umucore.auth.data.Account;
import br.com.umucraft.umucore.auth.data.AccountRepository;
import br.com.umucraft.umucore.auth.data.MojangApiClient;
import br.com.umucraft.umucore.auth.config.AuthConfig;
import br.com.umucraft.umucore.logger.UmuLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Faz a checagem assíncrona (fora da main thread, por contrato do
 * AsyncPlayerPreLoginEvent) de existência da conta, auto-login por IP e
 * modo Premium, guardando o resultado no AuthManager para ser consumido
 * pelo PlayerJoinListeners quando o jogador efetivamente entrar.
 */
public class PlayerAuthenticateListener implements Listener {

    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final AuthConfig authConfig;
    private final MojangApiClient mojangApiClient;

    public PlayerAuthenticateListener(AuthManager authManager, AccountRepository accountRepository,
                                       AuthConfig authConfig, MojangApiClient mojangApiClient) {
        this.authManager = authManager;
        this.accountRepository = accountRepository;
        this.authConfig = authConfig;
        this.mojangApiClient = mojangApiClient;
    }

    @EventHandler
    public void aoPreLogin(AsyncPlayerPreLoginEvent evento) {
        String nick = evento.getName().toLowerCase();
        String ip = evento.getAddress().getHostAddress();

        try {
            Optional<Account> contaOpt = accountRepository.buscarPorNome(nick);

            if (contaOpt.isEmpty()) {
                authManager.guardarPreAuth(evento.getUniqueId(), new PreAuthDecision(false, false, false));
                return;
            }

            Account conta = contaOpt.get();

            boolean autoLoginPorIp = authConfig.ipAutoLoginHabilitado()
                    && ip.equals(conta.ultimoIp())
                    && (System.currentTimeMillis() - conta.ultimoLoginEpochMillis()) <= authConfig.ipAutoLoginJanelaMinutos() * 60_000L;

            boolean premiumAutoLogin = conta.premium()
                    && authConfig.premiumHabilitado()
                    && mojangApiClient.nomeEhPremium(evento.getName());

            PreAuthDecision decisao = new PreAuthDecision(true, autoLoginPorIp, premiumAutoLogin);
            authManager.guardarPreAuth(evento.getUniqueId(), decisao);

            UmuLogger.debug("Auth-PreLogin", "Decisão para " + nick + ": " + decisao);
        } catch (SQLException e) {
            UmuLogger.erroCritico("Auth-PreLogin", "Falha ao consultar conta de " + nick, e);
            authManager.guardarPreAuth(evento.getUniqueId(), new PreAuthDecision(true, false, false));
        }
    }
}
