package br.com.umucraft.umucore.auth.config;

import br.com.umucraft.umucore.Umucore;
import br.com.umucraft.umucore.config.ModuleConfig;

/**
 * Configurações do módulo de autenticação (UmuAuth), lidas de
 * modules/auth.yml na pasta de dados do plugin.
 */
public class AuthConfig extends ModuleConfig {

    public AuthConfig(Umucore plugin) {
        super(plugin, "modules/auth.yml");
    }

    public int timeoutLoginSegundos() {
        return config.getInt("timeout-login-segundos", 30);
    }

    public boolean ipAutoLoginHabilitado() {
        return config.getBoolean("ip-auto-login.habilitado", true);
    }

    public int ipAutoLoginJanelaMinutos() {
        return config.getInt("ip-auto-login.janela-minutos", 15);
    }

    public boolean premiumHabilitado() {
        return config.getBoolean("premium.habilitado", true);
    }

    public int senhaMinima() {
        return config.getInt("registro.senha-minima", 4);
    }

    public boolean permitirUnregister() {
        return config.getBoolean("registro.permitir-unregister", false);
    }
}
