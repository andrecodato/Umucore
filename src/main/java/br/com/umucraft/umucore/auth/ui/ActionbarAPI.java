package br.com.umucraft.umucore.auth.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class ActionbarAPI {

    private ActionbarAPI() {
    }

    public static void enviar(Player jogador, Component mensagem) {
        jogador.sendActionBar(mensagem);
    }

    public static void enviarContagemRegressiva(Player jogador, int segundosRestantes) {
        enviar(jogador, Component.text("⏳ Autentique-se! Tempo restante: " + segundosRestantes + "s", NamedTextColor.YELLOW));
    }
}
