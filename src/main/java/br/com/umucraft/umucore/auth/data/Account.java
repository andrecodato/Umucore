package br.com.umucraft.umucore.auth.data;

/**
 * Representa uma linha da tabela "contas". senhaHash é nulo para contas
 * puramente premium que nunca definiram uma senha (modo /offline).
 */
public record Account(
        long id,
        String nomeMinusculo,
        String senhaHash,
        boolean premium,
        String ultimoIp,
        long ultimoLoginEpochMillis,
        long criadoEmEpochMillis
) {
}
