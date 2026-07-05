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

import java.sql.SQLException;

public class AuthAdminCommand implements CommandExecutor {

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final ConfigManager configManager;

    public AuthAdminCommand(Umucore plugin, AuthManager authManager, AccountRepository accountRepository, ConfigManager configManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.accountRepository = accountRepository;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este comando!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("=== Menu de Administração Auth ===", NamedTextColor.DARK_PURPLE));
            sender.sendMessage(Component.text("➡ /umuauth forcelogin <jogador> - Força o login de alguém.", NamedTextColor.LIGHT_PURPLE));
            sender.sendMessage(Component.text("➡ /umuauth forceunregister <jogador> - Apaga a conta de alguém à força.", NamedTextColor.LIGHT_PURPLE));
            sender.sendMessage(Component.text("➡ /umuauth reload - Recarrega as configurações do UmuCore.", NamedTextColor.LIGHT_PURPLE));
            return true;
        }

        String subComando = args[0].toLowerCase();

        switch (subComando) {
            case "forcelogin" -> forceLogin(sender, args);
            case "forceunregister" -> forceUnregister(sender, args);
            case "reload" -> reload(sender);
            default -> sender.sendMessage(Component.text("❌ Subcomando desconhecido!", NamedTextColor.RED));
        }

        return true;
    }

    private void forceLogin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth.forcelogin")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("⚠ Uso correto: /umuauth forcelogin <jogador>", NamedTextColor.GOLD));
            return;
        }

        String nomeAlvo = args[1];
        Player jogadorAlvo = Bukkit.getPlayer(nomeAlvo);

        if (jogadorAlvo == null) {
            sender.sendMessage(Component.text("❌ O jogador '" + nomeAlvo + "' não está online!", NamedTextColor.RED));
            return;
        }

        if (authManager.isLogado(jogadorAlvo.getUniqueId())) {
            sender.sendMessage(Component.text("❌ O jogador " + jogadorAlvo.getName() + " já está autenticado.", NamedTextColor.RED));
            return;
        }

        authManager.logar(jogadorAlvo.getUniqueId(), jogadorAlvo.getName());
        authManager.cancelarTaskAtiva(jogadorAlvo.getUniqueId());

        jogadorAlvo.sendMessage(Component.text("⚡ Um administrador forçou o seu login!", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("✔ Você forçou o login de " + jogadorAlvo.getName() + " com sucesso.", NamedTextColor.GREEN));

        UmuLogger.aviso("Auth-Admin", "O administrador " + sender.getName() + " forçou o login de " + jogadorAlvo.getName());
    }

    private void forceUnregister(CommandSender sender, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth.forceunregister")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("⚠ Uso correto: /umuauth forceunregister <jogador>", NamedTextColor.GOLD));
            return;
        }

        String nomeAlvo = args[1];
        String nickMinusculo = nomeAlvo.toLowerCase();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!accountRepository.existeConta(nickMinusculo)) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(Component.text("❌ Nenhuma conta encontrada com o nick '" + nomeAlvo + "'.", NamedTextColor.RED)));
                    return;
                }

                accountRepository.deletarConta(nickMinusculo);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player jogadorAlvo = Bukkit.getPlayer(nomeAlvo);
                    if (jogadorAlvo != null) {
                        authManager.deslogar(jogadorAlvo.getUniqueId());
                        jogadorAlvo.kick(Component.text("❌ Um administrador apagou sua conta.", NamedTextColor.RED));
                    }
                    sender.sendMessage(Component.text("✔ Conta de '" + nomeAlvo + "' apagada com sucesso.", NamedTextColor.GREEN));
                    UmuLogger.aviso("Auth-Admin", "O administrador " + sender.getName() + " apagou a conta de " + nomeAlvo);
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Admin", "Falha ao apagar conta de " + nickMinusculo, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(Component.text("❌ Ocorreu um erro ao processar o pedido.", NamedTextColor.RED)));
            }
        });
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("umucore.admin.auth.reload")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        configManager.recarregar();
        sender.sendMessage(Component.text("✔ Configurações recarregadas.", NamedTextColor.GREEN));
    }
}
