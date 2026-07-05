package br.com.umucraft.umucore.auth.command.executors;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.data.AccountRepository;
import br.com.umucraft.umucore.auth.data.MojangApiClient;
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
import java.util.UUID;

public class PremiumCommand implements CommandExecutor {

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final ConfigManager configManager;
    private final MojangApiClient mojangApiClient;

    public PremiumCommand(Umucore plugin, AuthManager authManager, AccountRepository accountRepository,
                           ConfigManager configManager, MojangApiClient mojangApiClient) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.accountRepository = accountRepository;
        this.configManager = configManager;
        this.mojangApiClient = mojangApiClient;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player jogador)) {
            sender.sendMessage("Apenas jogadores usam este comando.");
            return true;
        }

        if (!configManager.premiumHabilitado()) {
            jogador.sendMessage(Component.text("❌ O modo Premium está desativado neste servidor.", NamedTextColor.RED));
            return true;
        }

        UUID uuid = jogador.getUniqueId();
        String nick = jogador.getName().toLowerCase();
        String nomeAtual = jogador.getName();

        if (!authManager.isLogado(uuid)) {
            jogador.sendMessage(Component.text("❌ Você precisa estar logado para ativar o modo Premium.", NamedTextColor.RED));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean premium = mojangApiClient.nomeEhPremium(nomeAtual);

            if (!premium) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        jogador.sendMessage(Component.text("❌ Nenhuma conta Premium encontrada com este nickname.", NamedTextColor.RED)));
                return;
            }

            try {
                accountRepository.atualizarPremium(nick, true);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    jogador.sendMessage(Component.text("✔ Modo Premium ativado! Da próxima vez que você conectar com uma conta Premium/Microsoft com este nickname, o login será automático.", NamedTextColor.GREEN));
                    UmuLogger.info("Auth", "O jogador " + nomeAtual + " ativou o modo Premium.");
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Premium", "Falha ao ativar modo Premium para " + nick, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        jogador.sendMessage(Component.text("❌ Ocorreu um erro ao processar seu pedido. Tente novamente.", NamedTextColor.RED)));
            }
        });

        return true;
    }
}
