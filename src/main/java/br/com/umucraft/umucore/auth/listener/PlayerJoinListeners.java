package br.com.umucraft.umucore.auth.listener;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.PreAuthDecision;
import br.com.umucraft.umucore.auth.data.AccountRepository;
import br.com.umucraft.umucore.auth.task.LoginTimeoutTask;
import br.com.umucraft.umucore.auth.ui.TitleAPI;
import br.com.umucraft.umucore.config.ConfigManager;
import br.com.umucraft.umucore.logger.UmuLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerJoinListeners implements Listener {

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final ConfigManager configManager;

    public PlayerJoinListeners(Umucore plugin, AuthManager authManager, AccountRepository accountRepository, ConfigManager configManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.accountRepository = accountRepository;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void aoEntrar(PlayerJoinEvent evento) {
        Player jogador = evento.getPlayer();
        UUID uuid = jogador.getUniqueId();

        if (jogador.hasPermission("umucore.auth.bypass")) {
            authManager.consumirPreAuth(uuid);
            authManager.logar(uuid, jogador.getName());
            return;
        }

        PreAuthDecision decisao = authManager.consumirPreAuth(uuid).orElse(PreAuthDecision.PADRAO_PRECISA_LOGIN);

        if (decisao.autoLogin()) {
            autenticarAutomaticamente(jogador, uuid);
            return;
        }

        authManager.deslogar(uuid);
        TitleAPI.enviarTituloLogin(jogador);
        jogador.sendMessage(Component.text(
                decisao.contaExiste()
                        ? "➡ Use /login <senha> para entrar."
                        : "➡ Use /register <senha> <confirmarSenha> para criar sua conta.",
                NamedTextColor.LIGHT_PURPLE));

        LoginTimeoutTask task = new LoginTimeoutTask(jogador, authManager, configManager.timeoutLoginSegundos());
        BukkitTask bukkitTask = task.runTaskTimer(plugin, 0L, 20L);
        authManager.registrarTaskAtiva(uuid, bukkitTask);
    }

    private void autenticarAutomaticamente(Player jogador, UUID uuid) {
        authManager.logar(uuid, jogador.getName());
        TitleAPI.enviarTituloAutoLogin(jogador);
        jogador.sendMessage(Component.text("✔ Você foi autenticado automaticamente.", NamedTextColor.GREEN));

        String nick = jogador.getName().toLowerCase();
        String ip = jogador.getAddress() != null ? jogador.getAddress().getAddress().getHostAddress() : null;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                accountRepository.atualizarUltimoLogin(nick, ip, System.currentTimeMillis());
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Join", "Falha ao atualizar último login de " + nick, e);
            }
        });
    }
}
