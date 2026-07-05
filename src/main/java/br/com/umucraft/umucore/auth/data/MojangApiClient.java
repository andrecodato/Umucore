package br.com.umucraft.umucore.auth.data;

import br.com.umucraft.umucore.logger.UmuLogger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Consulta a API pública da Mojang para detectar se existe uma conta
 * Premium com um determinado nickname. Isso é uma heurística por nome, não
 * uma verificação criptográfica de propriedade da conta.
 *
 * Todos os métodos fazem uma chamada HTTP bloqueante — só devem ser
 * chamados fora da main thread do servidor.
 */
public class MojangApiClient {

    private static final String ENDPOINT = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    public boolean nomeEhPremium(String nome) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT + nome))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            UmuLogger.erroCritico("Premium", "Falha ao consultar a API da Mojang para " + nome, e);
            return false;
        }
    }
}
