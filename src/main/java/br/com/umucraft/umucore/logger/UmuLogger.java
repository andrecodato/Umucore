package br.com.umucraft.umucore.logger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class UmuLogger {

    private static final Component PREFIXO_GERAL = Component.text("[UmuCore] ", NamedTextColor.DARK_PURPLE);

    // Controle global do modo debug (futuramente lido do config.yml)
    private static boolean debugAtivado = false;

    /**
     * Permite ligar ou desligar as mensagens de debug em tempo de execução.
     */
    public static void setDebug(boolean ativado) {
        debugAtivado = ativado;
    }

    /**
     * Envia uma mensagem de Debug. Só aparece no console se o modo debug estiver ativo.
     * Excelente para rastrear variáveis e fluxos de dados internos.
     */
    public static void debug(String modulo, String mensagem) {
        if (debugAtivado) {
            Component mensagemCompleta = PREFIXO_GERAL
                    .append(Component.text("[DEBUG] [" + modulo + "] ", NamedTextColor.GRAY))
                    .append(Component.text(mensagem, NamedTextColor.DARK_GRAY));

            Bukkit.getConsoleSender().sendMessage(mensagemCompleta);
        }
    }

    /**
     * Captura um erro do Java (Exception), exibe de forma estilizada e mostra a causa raiz.
     * Evita que o console exploda com centenas de linhas vermelhas genéricas.
     */
    public static void erroCritico(String modulo, String descricaoDoProblema, Throwable erro) {
        // Mensagem de cabeçalho personalizada
        Component mensagemCompleta = PREFIXO_GERAL
                .append(Component.text("[ERRO CRÍTICO] [" + modulo + "] ❌ ", NamedTextColor.RED))
                .append(Component.text(descricaoDoProblema, NamedTextColor.WHITE))
                .append(Component.text(" (Causa: " + erro.getMessage() + ")", NamedTextColor.YELLOW));

        Bukkit.getConsoleSender().sendMessage(mensagemCompleta);

        // Mostra o rastreamento do erro (Stacktrace) no console para o desenvolvedor saber a linha exata
        erro.printStackTrace();
    }

    public static void info(String modulo, String mensagem) {
        Component mensagemCompleta = PREFIXO_GERAL
                .append(Component.text("[" + modulo + "] ", NamedTextColor.BLUE))
                .append(Component.text(mensagem, NamedTextColor.WHITE));
        Bukkit.getConsoleSender().sendMessage(mensagemCompleta);
    }

    public static void sucesso(String modulo, String mensagem) {
        Component mensagemCompleta = PREFIXO_GERAL
                .append(Component.text("[" + modulo + "] ✔ ", NamedTextColor.BLUE))
                .append(Component.text(mensagem, NamedTextColor.GREEN));
        Bukkit.getConsoleSender().sendMessage(mensagemCompleta);
    }

    public static void aviso(String modulo, String mensagem) {
        Component mensagemCompleta = PREFIXO_GERAL
                .append(Component.text("[AVISO] [" + modulo + "] ⚠ ", NamedTextColor.GOLD))
                .append(Component.text(mensagem, NamedTextColor.GOLD));
        Bukkit.getConsoleSender().sendMessage(mensagemCompleta);
    }
}