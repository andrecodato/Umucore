package br.com.umucraft.umucore.auth.command.executors;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.config.AuthConfig;
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

public class UnregisterCommand implements CommandExecutor {

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final AuthConfig authConfig;

    public UnregisterCommand(Umucore plugin, AuthManager authManager, AccountRepository accountRepository, AuthConfig authConfig) {
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

        if (!authConfig.permitirUnregister()) {
            jogador.sendMessage(Component.text("❌ Este comando está desabilitado neste servidor.", NamedTextColor.RED));
            return true;
        }

        UUID uuid = jogador.getUniqueId();
        String nick = jogador.getName().toLowerCase();

        if (!authManager.isLogado(uuid)) {
            jogador.sendMessage(Component.text("❌ Você precisa estar logado para apagar sua conta.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            jogador.sendMessage(Component.text("⚠ Uso correto: /unregister <suaSenhaAtual>", NamedTextColor.GOLD));
            return true;
        }

        String confirmacao = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Optional<Account> contaOpt = accountRepository.buscarPorNome(nick);
                if (contaOpt.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            jogador.sendMessage(Component.text("❌ Conta não encontrada.", NamedTextColor.RED)));
                    return;
                }

                Account conta = contaOpt.get();
                boolean confirmado;

                if (conta.senhaHash() == null) {
                    // Conta puramente premium sem senha: confirma redigitando o próprio nick.
                    confirmado = confirmacao.equalsIgnoreCase(jogador.getName());
                } else {
                    confirmado = BCrypt.checkpw(confirmacao, conta.senhaHash());
                }

                if (!confirmado) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        UmuLogger.aviso("Auth", "O jogador " + jogador.getName() + " errou a confirmação de /unregister.");
                        jogador.sendMessage(Component.text("❌ Confirmação incorreta! Sua conta não foi apagada.", NamedTextColor.RED));
                    });
                    return;
                }

                accountRepository.deletarConta(nick);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    authManager.deslogar(uuid);
                    jogador.kick(Component.text("✔ Conta apagada com sucesso. Registre-se novamente para jogar.", NamedTextColor.GREEN));
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Unregister", "Falha ao apagar conta de " + nick, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        jogador.sendMessage(Component.text("❌ Ocorreu um erro ao processar seu pedido. Tente novamente.", NamedTextColor.RED)));
            }
        });

        return true;
    }
}
