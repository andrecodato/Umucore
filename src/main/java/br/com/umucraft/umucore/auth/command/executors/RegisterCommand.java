package br.com.umucraft.umucore.auth.command.executors;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.data.AccountRepository;
import br.com.umucraft.umucore.config.ConfigManager;
import br.com.umucraft.umucore.logger.UmuLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.UUID;

public class RegisterCommand implements CommandExecutor {

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final ConfigManager configManager;

    public RegisterCommand(Umucore plugin, AuthManager authManager, AccountRepository accountRepository, ConfigManager configManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.accountRepository = accountRepository;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player jogador)) {
            sender.sendMessage("Apenas jogadores usam este comando.");
            return true;
        }

        UUID uuid = jogador.getUniqueId();
        String nick = jogador.getName().toLowerCase();

        if (authManager.isLogado(uuid)) {
            jogador.sendMessage(Component.text("❌ Você já está autenticado!", NamedTextColor.RED));
            return true;
        }

        if (args.length != 2) {
            jogador.sendMessage(Component.text("⚠ Uso correto: /register <senha> <confirmarSenha>", NamedTextColor.GOLD));
            return true;
        }

        String senha = args[0];
        String confirmarSenha = args[1];

        if (!senha.equals(confirmarSenha)) {
            jogador.sendMessage(Component.text("❌ As senhas não coincidem!", NamedTextColor.RED));
            return true;
        }

        int senhaMinima = configManager.senhaMinima();
        if (senha.length() < senhaMinima) {
            jogador.sendMessage(Component.text("⚠ Sua senha deve ter pelo menos " + senhaMinima + " caracteres.", NamedTextColor.GOLD));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (accountRepository.existeConta(nick)) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            jogador.sendMessage(Component.text("❌ Esta conta já está registrada. Use /login <senha>", NamedTextColor.RED)));
                    return;
                }

                String hash = BCrypt.hashpw(senha, BCrypt.gensalt());
                accountRepository.criarConta(nick, hash, false, System.currentTimeMillis());

                String ip = jogador.getAddress() != null ? jogador.getAddress().getAddress().getHostAddress() : null;
                accountRepository.atualizarUltimoLogin(nick, ip, System.currentTimeMillis());

                Bukkit.getScheduler().runTask(plugin, () -> {
                    authManager.logar(uuid, jogador.getName());
                    authManager.cancelarTaskAtiva(uuid);
                    jogador.sendMessage(Component.text("✔ Registrado e logado com sucesso! Divirta-se.", NamedTextColor.GREEN));
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Register", "Falha ao registrar conta de " + nick, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        jogador.sendMessage(Component.text("❌ Ocorreu um erro ao processar seu registro. Tente novamente.", NamedTextColor.RED)));
            }
        });

        return true;
    }
}
