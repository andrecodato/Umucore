package br.com.umucraft.umucore.auth;

/**
 * Resultado calculado de forma assíncrona pelo PlayerAuthenticateListener
 * (AsyncPlayerPreLoginEvent) e consumido pelo PlayerJoinListeners no join.
 */
public record PreAuthDecision(
        boolean contaExiste,
        boolean autoLoginPorIp,
        boolean premium
) {
    public static final PreAuthDecision PADRAO_PRECISA_LOGIN = new PreAuthDecision(true, false, false);

    public boolean autoLogin() {
        return autoLoginPorIp || premium;
    }
}
