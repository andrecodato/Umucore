package br.com.umucraft.umucore.auth;

import br.com.umucraft.umucore.logger.UmuLogger;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dono do estado de SESSÃO (em memória, por instância) — não confundir com
 * dados persistidos de conta, que ficam em AccountRepository. Aqui só
 * respondemos "esse jogador está autenticado agora?".
 */
public class AuthManager {

    private final Set<UUID> jogadoresLogados = ConcurrentHashMap.newKeySet();
    private final Map<UUID, PreAuthDecision> decisoesPreAuth = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> tarefasAtivas = new ConcurrentHashMap<>();

    /**
     * Define que o jogador passou na autenticação.
     */
    public void logar(UUID uuid, String nome) {
        jogadoresLogados.add(uuid);
        UmuLogger.info("Auth", "O jogador " + nome + " autenticou com sucesso.");
    }

    /**
     * Remove o jogador da lista de logados (quando ele desloga do servidor).
     */
    public void deslogar(UUID uuid) {
        jogadoresLogados.remove(uuid);
    }

    /**
     * Verifica se o jogador já está logado.
     */
    public boolean isLogado(UUID uuid) {
        return jogadoresLogados.contains(uuid);
    }

    public void guardarPreAuth(UUID uuid, PreAuthDecision decisao) {
        decisoesPreAuth.put(uuid, decisao);
    }

    public Optional<PreAuthDecision> consumirPreAuth(UUID uuid) {
        return Optional.ofNullable(decisoesPreAuth.remove(uuid));
    }

    public void limparPreAuth(UUID uuid) {
        decisoesPreAuth.remove(uuid);
    }

    public void registrarTaskAtiva(UUID uuid, BukkitTask task) {
        tarefasAtivas.put(uuid, task);
    }

    public void cancelarTaskAtiva(UUID uuid) {
        BukkitTask task = tarefasAtivas.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
}
