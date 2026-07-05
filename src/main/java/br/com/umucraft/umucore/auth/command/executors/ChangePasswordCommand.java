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

public class ChangePasswordCommand implements CommandExecutor {

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final AuthConfig authConfig;

    public ChangePasswordCommand(Umucore plugin, AuthManager authManager, AccountRepository accountRepository, AuthConfig authConfig) {
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
            jogador.sendMessage(Component.text("❌ Você precisa estar logado para trocar sua senha.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 3) {
            jogador.sendMessage(Component.text("⚠ Uso correto: /changepassword <senhaAtual> <novaSenha> <confirmarNovaSenha>", NamedTextColor.GOLD));
            return true;
        }

        String senhaAtual = args[0];
        String novaSenha = args[1];
        String confirmarNovaSenha = args[2];

        if (!novaSenha.equals(confirmarNovaSenha)) {
            jogador.sendMessage(Component.text("❌ As novas senhas não coincidem!", NamedTextColor.RED));
            return true;
        }

        int senhaMinima = authConfig.senhaMinima();
        if (novaSenha.length() < senhaMinima) {
            jogador.sendMessage(Component.text("⚠ Sua nova senha deve ter pelo menos " + senhaMinima + " caracteres.", NamedTextColor.GOLD));
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

                Account conta = contaOpt.get();

                if (conta.senhaHash() == null && conta.premium()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            jogador.sendMessage(Component.text("❌ Contas Premium não possuem senha. Use /offline <senha> <confirmarSenha> para criar uma.", NamedTextColor.RED)));
                    return;
                }

                if (conta.senhaHash() == null) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            jogador.sendMessage(Component.text("❌ Sua conta ainda não tem uma senha definida! Use /register <senha> <confirmarSenha>", NamedTextColor.RED)));
                    return;
                }

                if (!BCrypt.checkpw(senhaAtual, conta.senhaHash())) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        UmuLogger.aviso("Auth", "O jogador " + jogador.getName() + " errou a senha atual em /changepassword.");
                        jogador.sendMessage(Component.text("❌ Senha atual incorreta!", NamedTextColor.RED));
                    });
                    return;
                }

                String novoHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
                accountRepository.atualizarSenha(nick, novoHash);

                Bukkit.getScheduler().runTask(plugin, () ->
                        jogador.sendMessage(Component.text("✔ Senha alterada com sucesso!", NamedTextColor.GREEN)));
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-ChangePassword", "Falha ao trocar senha de " + nick, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        jogador.sendMessage(Component.text("❌ Ocorreu um erro ao processar seu pedido. Tente novamente.", NamedTextColor.RED)));
            }
        });

        return true;
    }
}
