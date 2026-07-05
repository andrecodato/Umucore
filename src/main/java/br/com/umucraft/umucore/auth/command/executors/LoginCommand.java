package br.com.umucraft.umucore.auth.command.executors;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.data.Account;
import br.com.umucraft.umucore.auth.data.AccountRepository;
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

public class LoginCommand implements CommandExecutor {

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;

    public LoginCommand(Umucore plugin, AuthManager authManager, AccountRepository accountRepository) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.accountRepository = accountRepository;
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
            jogador.sendMessage(Component.text("❌ Você já está logado!", NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            jogador.sendMessage(Component.text("⚠ Uso correto: /login <suaSenha>", NamedTextColor.GOLD));
            return true;
        }

        String senhaDigitada = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Optional<Account> contaOpt = accountRepository.buscarPorNome(nick);

                if (contaOpt.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            jogador.sendMessage(Component.text("❌ Você ainda não tem registro! Use /register <senha> <confirmarSenha>", NamedTextColor.RED)));
                    return;
                }

                Account conta = contaOpt.get();

                if (conta.premium()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            jogador.sendMessage(Component.text("❌ Sua conta é Premium e não usa senha. Reconecte para entrar automaticamente ou use /offline para migrar.", NamedTextColor.RED)));
                    return;
                }

                boolean senhaCorreta = BCrypt.checkpw(senhaDigitada, conta.senhaHash());

                if (senhaCorreta) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        authManager.logar(uuid, jogador.getName());
                        authManager.cancelarTaskAtiva(uuid);
                        jogador.sendMessage(Component.text("✔ Autenticado com sucesso! Bem-vindo de volta.", NamedTextColor.GREEN));
                    });

                    String ip = jogador.getAddress() != null ? jogador.getAddress().getAddress().getHostAddress() : null;
                    accountRepository.atualizarUltimoLogin(nick, ip, System.currentTimeMillis());
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        UmuLogger.aviso("Auth", "O jogador " + jogador.getName() + " errou a senha.");
                        jogador.sendMessage(Component.text("❌ Senha incorreta! Tente novamente.", NamedTextColor.RED));
                    });
                }
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Login", "Falha ao consultar conta de " + nick, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        jogador.sendMessage(Component.text("❌ Ocorreu um erro ao processar seu login. Tente novamente.", NamedTextColor.RED)));
            }
        });

        return true;
    }
}
