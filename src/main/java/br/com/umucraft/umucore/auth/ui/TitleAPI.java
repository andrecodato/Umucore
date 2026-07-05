package br.com.umucraft.umucore.auth.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

public class TitleAPI {

    private TitleAPI() {
    }

    public static void enviar(Player jogador, Component titulo, Component subtitulo) {
        jogador.showTitle(Title.title(titulo, subtitulo));
    }

    public static void enviarTituloLogin(Player jogador) {
        enviar(jogador,
                Component.text("UmuCraft", NamedTextColor.DARK_PURPLE),
                Component.text("Registre-se ou Faça Login para jogar!", NamedTextColor.YELLOW));
    }

    public static void enviarTituloAutoLogin(Player jogador) {
        enviar(jogador,
                Component.text("UmuCraft", NamedTextColor.DARK_PURPLE),
                Component.text("Login automático realizado!", NamedTextColor.GREEN));
    }
}
