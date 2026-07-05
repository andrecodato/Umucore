package br.com.umucraft.umucore.auth.listener;

import br.com.umucraft.umucore.auth.AuthManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerGeneralListeners implements Listener {

    private final AuthManager authManager;

    public PlayerGeneralListeners(AuthManager authManager) {
        this.authManager = authManager;
    }

    @EventHandler
    public void aoSeMover(PlayerMoveEvent evento) {
        if (!authManager.isLogado(evento.getPlayer().getUniqueId())) {
            evento.setCancelled(true); // Trava o andar e o olhar
        }
    }

    @EventHandler
    public void aoQuebrarBloco(BlockBreakEvent evento) {
        if (!authManager.isLogado(evento.getPlayer().getUniqueId())) {
            evento.setCancelled(true);
        }
    }

    @EventHandler
    public void aoColocarBloco(BlockPlaceEvent evento) {
        if (!authManager.isLogado(evento.getPlayer().getUniqueId())) {
            evento.setCancelled(true);
        }
    }

    @EventHandler
    public void aoFalarNoChat(AsyncPlayerChatEvent evento) {
        if (!authManager.isLogado(evento.getPlayer().getUniqueId())) {
            evento.setCancelled(true);
            evento.getPlayer().sendMessage(Component.text("❌ Você não pode falar no chat antes de logar!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void aoSofrerDano(EntityDamageEvent evento) {
        if (evento.getEntity() instanceof Player jogador) {
            if (!authManager.isLogado(jogador.getUniqueId())) {
                evento.setCancelled(true); // Imunidade a dano enquanto não logar
            }
        }
    }

    @EventHandler
    public void aoDigitarComandos(PlayerCommandPreprocessEvent evento) {
        String mensagem = evento.getMessage().toLowerCase();
        // Se NÃO está logado e o comando NÃO for de login/registro, bloqueia!
        if (!authManager.isLogado(evento.getPlayer().getUniqueId())) {
            if (!mensagem.startsWith("/login") && !mensagem.startsWith("/register")) {
                evento.setCancelled(true);
                evento.getPlayer().sendMessage(Component.text("❌ Comando bloqueado! Autentique-se primeiro.", NamedTextColor.RED));
            }
        }
    }
}
