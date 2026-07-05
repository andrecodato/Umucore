package br.com.umucraft.umucore.auth.listener;

import br.com.umucraft.umucore.auth.AuthManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerKickListeners implements Listener {

    private final AuthManager authManager;

    public PlayerKickListeners(AuthManager authManager) {
        this.authManager = authManager;
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent evento) {
        UUID uuid = evento.getPlayer().getUniqueId();
        authManager.deslogar(uuid);
        authManager.cancelarTaskAtiva(uuid);
        authManager.limparPreAuth(uuid);
    }
}
