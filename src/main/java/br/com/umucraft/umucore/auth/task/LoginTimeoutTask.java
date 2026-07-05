package br.com.umucraft.umucore.auth.task;

import br.com.umucraft.umucore.auth.AuthManager;
import br.com.umucraft.umucore.auth.ui.ActionbarAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LoginTimeoutTask extends BukkitRunnable {

    private final Player jogador;
    private final AuthManager authManager;
    private int tempoRestante;

    public LoginTimeoutTask(Player jogador, AuthManager authManager, int timeoutSegundos) {
        this.jogador = jogador;
        this.authManager = authManager;
        this.tempoRestante = timeoutSegundos;
    }

    @Override
    public void run() {
        // 1. Se o jogador deslogou voluntariamente antes do tempo acabar, para o cronômetro
        if (!jogador.isOnline()) {
            this.cancel();
            return;
        }

        // 2. Se o jogador conseguiu fazer o login/registro, para o cronômetro
        if (authManager.isLogado(jogador.getUniqueId())) {
            this.cancel();
            return;
        }

        // 3. Se o tempo zerar e ele ainda não estiver logado, expulsa o jogador do servidor
        if (tempoRestante <= 0) {
            jogador.kick(Component.text("❌ Seu tempo para fazer login expirou!", NamedTextColor.RED));
            this.cancel();
            return;
        }

        // 4. Avisa o jogador a cada 10 segundos para ele não esquecer
        if (tempoRestante % 10 == 0) {
            jogador.sendMessage(Component.text("⚠ Você tem mais " + tempoRestante + " segundos para se autenticar!", NamedTextColor.GOLD));
        }

        // 5. Contagem regressiva contínua na actionbar
        ActionbarAPI.enviarContagemRegressiva(jogador, tempoRestante);

        tempoRestante--;
    }
}
