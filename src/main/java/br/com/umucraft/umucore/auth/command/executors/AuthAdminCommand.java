package br.com.umucraft.umucore.auth.command.executors;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.config.AuthConfig;
import br.com.umucraft.umucore.auth.data.Account;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthAdminCommand implements CommandExecutor {

    private static final int CONTAS_POR_PAGINA = 10;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final Pattern PADRAO_IP = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    private final Umucore plugin;
    private final AuthManager authManager;
    private final AccountRepository accountRepository;
    private final ConfigManager configManager;
    private final AuthConfig authConfig;

    public AuthAdminCommand(Umucore plugin, AuthManager authManager, AccountRepository accountRepository,
                             ConfigManager configManager, AuthConfig authConfig) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.accountRepository = accountRepository;
        this.configManager = configManager;
        this.authConfig = authConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este comando!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            enviarMenu(sender);
            return true;
        }

        String subComando = args[0].toLowerCase();

        switch (subComando) {
            case "forcelogin", "forcarlogin" -> forceLogin(sender, args);
            case "delete", "apagar" -> delete(sender, args);
            case "unregister", "desregistrar" -> unregister(sender, args);
            case "listaccounts", "listarcontas" -> listAccounts(sender, args);
            case "verify", "verificar" -> verify(sender, args);
            case "changepass", "mudarsenha" -> changePass(sender, args);
            case "setpremium", "definirpremium" -> setPremium(sender, args);
            case "dupeip", "ipduplicado" -> dupeIp(sender, args);
            case "kickunauth", "expulsarnaologados" -> kickUnauth(sender);
            case "support", "suporte" -> support(sender);
            case "version", "versao" -> version(sender);
            case "reload", "recarregar" -> reload(sender);
            default -> sender.sendMessage(Component.text("❌ Subcomando desconhecido!", NamedTextColor.RED));
        }

        return true;
    }

    private void enviarMenu(CommandSender sender) {
        sender.sendMessage(Component.text("=== Menu de Administração Auth ===", NamedTextColor.DARK_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth forcelogin|forcarlogin <jogador> - Força o login de alguém.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth delete|apagar <jogador> - Apaga a conta de alguém à força (irreversível).", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth unregister|desregistrar <jogador> - Remove só a senha de alguém, mantém a conta.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth listaccounts|listarcontas [página] - Lista as contas cadastradas.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth verify|verificar <jogador> - Mostra detalhes da conta de alguém.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth changepass|mudarsenha <jogador> <novaSenha> - Redefine a senha de alguém.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth setpremium|definirpremium <jogador> <true|false> - Ativa/desativa o modo Premium de alguém.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth dupeip|ipduplicado <jogador|ip> - Lista contas que compartilham o mesmo IP.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth kickunauth|expulsarnaologados - Remove todos os jogadores não autenticados.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth support|suporte - Lista os contatos de suporte.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth version|versao - Mostra a versão do plugin.", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("➡ /umuauth reload|recarregar - Recarrega as configurações do UmuCore.", NamedTextColor.LIGHT_PURPLE));
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

    private void delete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth.delete")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("⚠ Uso correto: /umuauth delete <jogador>", NamedTextColor.GOLD));
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
                    sender.sendMessage(Component.text("🚨 Isso apagou a conta inteira (dados de IP/premium/etc. são perdidos). Para apenas remover a senha, use /umuauth unregister.", NamedTextColor.GOLD));
                    UmuLogger.aviso("Auth-Admin", "O administrador " + sender.getName() + " apagou a conta de " + nomeAlvo);
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Admin", "Falha ao apagar conta de " + nickMinusculo, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(Component.text("❌ Ocorreu um erro ao processar o pedido.", NamedTextColor.RED)));
            }
        });
    }

    private void unregister(CommandSender sender, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth.unregister")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("⚠ Uso correto: /umuauth unregister <jogador>", NamedTextColor.GOLD));
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

                accountRepository.limparSenha(nickMinusculo);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player jogadorAlvo = Bukkit.getPlayer(nomeAlvo);
                    if (jogadorAlvo != null) {
                        authManager.deslogar(jogadorAlvo.getUniqueId());
                        jogadorAlvo.kick(Component.text("❌ Um administrador removeu a senha da sua conta. Use /register para definir uma nova.", NamedTextColor.GOLD));
                    }
                    sender.sendMessage(Component.text("✔ Senha de '" + nomeAlvo + "' removida. A conta continua existindo (IP, premium e histórico preservados).", NamedTextColor.GREEN));
                    UmuLogger.aviso("Auth-Admin", "O administrador " + sender.getName() + " removeu a senha de " + nomeAlvo);
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Admin", "Falha ao remover senha de " + nickMinusculo, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(Component.text("❌ Ocorreu um erro ao processar o pedido.", NamedTextColor.RED)));
            }
        });
    }

    private void listAccounts(CommandSender sender, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth.listaccounts")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        int pagina = 1;
        if (args.length >= 2) {
            try {
                pagina = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("⚠ A página deve ser um número. Uso: /umuauth listaccounts [página]", NamedTextColor.GOLD));
                return;
            }
        }

        int paginaFinal = pagina;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int total = accountRepository.contarContas();
                int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) CONTAS_POR_PAGINA));
                int paginaValida = Math.min(paginaFinal, totalPaginas);
                int offset = (paginaValida - 1) * CONTAS_POR_PAGINA;

                List<Account> contas = accountRepository.listarContas(offset, CONTAS_POR_PAGINA);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(Component.text("=== Contas (página " + paginaValida + "/" + totalPaginas + ", total: " + total + ") ===", NamedTextColor.DARK_PURPLE));
                    for (Account conta : contas) {
                        String sufixo = conta.premium() ? " (premium)" : "";
                        sender.sendMessage(Component.text("- " + conta.nomeMinusculo() + sufixo, NamedTextColor.LIGHT_PURPLE));
                    }
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Admin", "Falha ao listar contas.", e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(Component.text("❌ Ocorreu um erro ao listar as contas.", NamedTextColor.RED)));
            }
        });
    }

    private void verify(CommandSender sender, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth.verify")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("⚠ Uso correto: /umuauth verify <jogador>", NamedTextColor.GOLD));
            return;
        }

        String nomeAlvo = args[1];
        String nickMinusculo = nomeAlvo.toLowerCase();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Optional<Account> contaOpt = accountRepository.buscarPorNome(nickMinusculo);

                if (contaOpt.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(Component.text("❌ Nenhuma conta encontrada com o nick '" + nomeAlvo + "'.", NamedTextColor.RED)));
                    return;
                }

                Account conta = contaOpt.get();
                String ultimoIp = conta.ultimoIp() != null ? conta.ultimoIp() : "nunca logou";
                String ultimoLogin = conta.ultimoLoginEpochMillis() > 0
                        ? FORMATO_DATA.format(Instant.ofEpochMilli(conta.ultimoLoginEpochMillis()))
                        : "nunca logou";
                String criadoEm = FORMATO_DATA.format(Instant.ofEpochMilli(conta.criadoEmEpochMillis()));
                String temSenha = conta.senhaHash() != null ? "sim" : "não";

                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(Component.text("=== Conta: " + conta.nomeMinusculo() + " ===", NamedTextColor.DARK_PURPLE));
                    sender.sendMessage(Component.text("Premium: " + (conta.premium() ? "sim" : "não"), NamedTextColor.LIGHT_PURPLE));
                    sender.sendMessage(Component.text("Possui senha: " + temSenha, NamedTextColor.LIGHT_PURPLE));
                    sender.sendMessage(Component.text("Último IP: " + ultimoIp, NamedTextColor.LIGHT_PURPLE));
                    sender.sendMessage(Component.text("Último login: " + ultimoLogin, NamedTextColor.LIGHT_PURPLE));
                    sender.sendMessage(Component.text("Criado em: " + criadoEm, NamedTextColor.LIGHT_PURPLE));
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Admin", "Falha ao consultar conta de " + nickMinusculo, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(Component.text("❌ Ocorreu um erro ao consultar a conta.", NamedTextColor.RED)));
            }
        });
    }

    private void changePass(CommandSender sender, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth.changepass")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(Component.text("⚠ Uso correto: /umuauth changepass <jogador> <novaSenha>", NamedTextColor.GOLD));
            return;
        }

        String nomeAlvo = args[1];
        String nickMinusculo = nomeAlvo.toLowerCase();
        String novaSenha = args[2];

        int senhaMinima = authConfig.senhaMinima();
        if (novaSenha.length() < senhaMinima) {
            sender.sendMessage(Component.text("⚠ A nova senha deve ter pelo menos " + senhaMinima + " caracteres.", NamedTextColor.GOLD));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!accountRepository.existeConta(nickMinusculo)) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(Component.text("❌ Nenhuma conta encontrada com o nick '" + nomeAlvo + "'.", NamedTextColor.RED)));
                    return;
                }

                String novoHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
                accountRepository.atualizarSenha(nickMinusculo, novoHash);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player jogadorAlvo = Bukkit.getPlayer(nomeAlvo);
                    if (jogadorAlvo != null) {
                        jogadorAlvo.sendMessage(Component.text("⚡ Um administrador redefiniu sua senha.", NamedTextColor.GOLD));
                    }
                    sender.sendMessage(Component.text("✔ Senha de '" + nomeAlvo + "' redefinida com sucesso.", NamedTextColor.GREEN));
                    UmuLogger.aviso("Auth-Admin", "O administrador " + sender.getName() + " redefiniu a senha de " + nomeAlvo);
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Admin", "Falha ao redefinir senha de " + nickMinusculo, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(Component.text("❌ Ocorreu um erro ao processar o pedido.", NamedTextColor.RED)));
            }
        });
    }

    private void setPremium(CommandSender sender, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth.setpremium")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        if (args.length < 3 || !(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))) {
            sender.sendMessage(Component.text("⚠ Uso correto: /umuauth setpremium <jogador> <true|false>", NamedTextColor.GOLD));
            return;
        }

        String nomeAlvo = args[1];
        String nickMinusculo = nomeAlvo.toLowerCase();
        boolean premium = Boolean.parseBoolean(args[2]);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!accountRepository.existeConta(nickMinusculo)) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(Component.text("❌ Nenhuma conta encontrada com o nick '" + nomeAlvo + "'. O jogador precisa se registrar antes.", NamedTextColor.RED)));
                    return;
                }

                accountRepository.atualizarPremium(nickMinusculo, premium);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player jogadorAlvo = Bukkit.getPlayer(nomeAlvo);
                    if (jogadorAlvo != null) {
                        jogadorAlvo.sendMessage(Component.text("⚡ Um administrador " + (premium ? "ativou" : "desativou") + " o modo Premium da sua conta.", NamedTextColor.GOLD));
                    }
                    sender.sendMessage(Component.text("✔ Modo Premium de '" + nomeAlvo + "' " + (premium ? "ativado" : "desativado") + " com sucesso.", NamedTextColor.GREEN));
                    UmuLogger.aviso("Auth-Admin", "O administrador " + sender.getName() + " " + (premium ? "ativou" : "desativou") + " o modo Premium de " + nomeAlvo);
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Admin", "Falha ao alterar modo Premium de " + nickMinusculo, e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(Component.text("❌ Ocorreu um erro ao processar o pedido.", NamedTextColor.RED)));
            }
        });
    }

    private void dupeIp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("umucore.admin.auth.dupeip")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("⚠ Uso correto: /umuauth dupeip <jogador|ip>", NamedTextColor.GOLD));
            return;
        }

        String argumento = args[1];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String ip;

                if (PADRAO_IP.matcher(argumento).matches()) {
                    ip = argumento;
                } else {
                    Optional<Account> contaOpt = accountRepository.buscarPorNome(argumento.toLowerCase());
                    if (contaOpt.isEmpty()) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                                sender.sendMessage(Component.text("❌ Nenhuma conta encontrada com o nick '" + argumento + "'.", NamedTextColor.RED)));
                        return;
                    }
                    if (contaOpt.get().ultimoIp() == null) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                                sender.sendMessage(Component.text("❌ Essa conta nunca fez login, sem IP registrado.", NamedTextColor.RED)));
                        return;
                    }
                    ip = contaOpt.get().ultimoIp();
                }

                List<Account> contas = accountRepository.listarContasPorIp(ip);
                String ipFinal = ip;

                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(Component.text("=== Contas com o IP " + ipFinal + " (" + contas.size() + ") ===", NamedTextColor.DARK_PURPLE));
                    for (Account conta : contas) {
                        sender.sendMessage(Component.text("- " + conta.nomeMinusculo(), NamedTextColor.LIGHT_PURPLE));
                    }
                });
            } catch (SQLException e) {
                UmuLogger.erroCritico("Auth-Admin", "Falha ao consultar contas por IP para '" + argumento + "'", e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(Component.text("❌ Ocorreu um erro ao processar o pedido.", NamedTextColor.RED)));
            }
        });
    }

    private void kickUnauth(CommandSender sender) {
        if (!sender.hasPermission("umucore.admin.auth.kickunauth")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        int removidos = 0;
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            if (!authManager.isLogado(jogador.getUniqueId())) {
                jogador.kick(Component.text("❌ O servidor está em manutenção. Faça login novamente em instantes.", NamedTextColor.RED));
                removidos++;
            }
        }

        sender.sendMessage(Component.text("✔ " + removidos + " jogador(es) não autenticado(s) foram removidos.", NamedTextColor.GREEN));
        UmuLogger.aviso("Auth-Admin", "O administrador " + sender.getName() + " removeu " + removidos + " jogador(es) não autenticado(s).");
    }

    private void support(CommandSender sender) {
        sender.sendMessage(Component.text("=== Suporte ===", NamedTextColor.DARK_PURPLE));
        List<String> contatos = configManager.contatosSuporte();
        if (contatos.isEmpty()) {
            sender.sendMessage(Component.text("Nenhum contato de suporte configurado.", NamedTextColor.LIGHT_PURPLE));
            return;
        }
        for (String contato : contatos) {
            sender.sendMessage(Component.text("➡ " + contato, NamedTextColor.LIGHT_PURPLE));
        }
    }

    private void version(CommandSender sender) {
        sender.sendMessage(Component.text("=== UmuCore ===", NamedTextColor.DARK_PURPLE));
        sender.sendMessage(Component.text("Versão: " + plugin.getPluginMeta().getVersion(), NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("API: " + plugin.getPluginMeta().getAPIVersion(), NamedTextColor.LIGHT_PURPLE));
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("umucore.admin.auth.reload")) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este subcomando!", NamedTextColor.RED));
            return;
        }

        configManager.recarregar();
        authConfig.recarregar();
        sender.sendMessage(Component.text("✔ Configurações recarregadas.", NamedTextColor.GREEN));
    }
}
