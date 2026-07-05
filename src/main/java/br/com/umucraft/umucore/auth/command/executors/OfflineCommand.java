package br.com.umucraft.umucore.auth.command.executors;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.data.Account;
import br.com.umucraft.umucore.auth.data.AccountRepository;
import br.com.umucraft.umucore.auth.config.AuthConfig;
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
import java.util.Optional;
import java.util.UUID;

public class OfflineCommand implements CommandExecutor {

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final AuthConfig authConfig;

    public OfflineCommand(Umucore plugin, AuthManager authManager, AccountRepository accountRepository, AuthConfig authConfig) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.accountRepository = accountRepository;
        this.authConfig = authConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player jogador)) {
            sender.sendMessage("Apenas jogadores usam este comando.");
            return true;
        }

        UUID uuid = jogador.getUniqueId();
        String nick = jogador.getName().toLowerCase();

        if (!authManager.isLogado(uuid)) {
            jogador.sendMessage(Component.text("❌ Você precisa estar logado para desativar o modo Premium.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 2) {
            jogador.sendMessage(Component.text("⚠ Uso correto: /offline <senha> <confirmarSenha>", NamedTextColor.GOLD));
            return true;
        }

        String senha = args[0];
        String confirmarSenha = args[1];

        if (!senha.equals(confirmarSenha)) {
            jogador.sendMessage(Component.text("❌ As senhas não coincidem!", NamedTextColor.RED));
            return true;
        }

        int senhaMinima = authConfig.senhaMinima();
        if (senha.length() < senhaMinima) {
            jogador.sendMessage(Component.text("⚠ Sua senha deve ter pelo menos " + senhaMinima + " caracteres.", NamedTextColor.GOLD));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Optional<Account> contaOpt = accountRepository.buscarPorNome(nick);
                if (contaOpt.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            jogador.sendMessage(Component.text("❌ Conta não encontrada.", NamedTextColor.RED)));
                    return;
                }

                if (!contaOpt.get().premium()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            jogador.sendMessage(Component.text("❌ Sua conta já está no modo offline (senha).", NamedTextColor.RED)));
                    return;
                }

                String hash = BCrypt.hashpw(senha, BCrypt.gensalt());
                accountRepository.atualizarSenha(nick, hash);
                accountRepository.atualizarPremium(nick, false);

                Bukkit.getScheduler().runTask(plugin, () ->
                        jogador.sendMessage(Component.text("✔ Modo offline ativado. Use /login <senha> normalmente a partir de agora.", NamedTextColor.GREEN)));
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Offline", "Falha ao desativar modo Premium para " + nick, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        jogador.sendMessage(Component.text("❌ Ocorreu um erro ao processar seu pedido. Tente novamente.", NamedTextColor.RED)));
            }
        });

        return true;
    }
}
