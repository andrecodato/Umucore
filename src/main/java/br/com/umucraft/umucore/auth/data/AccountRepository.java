package br.com.umucraft.umucore.auth.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CRUD de contas via JDBC puro (sem ORM/pool — é um único arquivo SQLite).
 *
 * IMPORTANTE: nenhum método aqui agenda trabalho assíncrono sozinho. Quem
 * chamar é responsável por rodar fora da main thread do servidor
 * (Bukkit.getScheduler().runTaskAsynchronously), exceto dentro de um
 * AsyncPlayerPreLoginEvent, que já roda assíncrono por contrato do Bukkit.
 */
public class AccountRepository {

    private final DatabaseManager databaseManager;

    public AccountRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<Account> buscarPorNome(String nomeMinusculo) throws SQLException {
        String sql = "SELECT * FROM contas WHERE nome_minusculo = ?";
        Connection conexao = databaseManager.getConexao();

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, nomeMinusculo);

            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearConta(resultado));
            }
        }
    }

    public boolean existeConta(String nomeMinusculo) throws SQLException {
        return buscarPorNome(nomeMinusculo).isPresent();
    }

    public void criarConta(String nomeMinusculo, String senhaHash, boolean premium, long criadoEm) throws SQLException {
        String sql = "INSERT INTO contas (nome_minusculo, senha_hash, premium, criado_em) VALUES (?, ?, ?, ?)";
        Connection conexao = databaseManager.getConexao();

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, nomeMinusculo);
            statement.setString(2, senhaHash);
            statement.setInt(3, premium ? 1 : 0);
            statement.setLong(4, criadoEm);
            statement.executeUpdate();
        }
    }

    public void atualizarSenha(String nomeMinusculo, String novoHash) throws SQLException {
        String sql = "UPDATE contas SET senha_hash = ? WHERE nome_minusculo = ?";
        Connection conexao = databaseManager.getConexao();

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, novoHash);
            statement.setString(2, nomeMinusculo);
            statement.executeUpdate();
        }
    }

    public void deletarConta(String nomeMinusculo) throws SQLException {
        String sql = "DELETE FROM contas WHERE nome_minusculo = ?";
        Connection conexao = databaseManager.getConexao();

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, nomeMinusculo);
            statement.executeUpdate();
        }
    }

    public void atualizarPremium(String nomeMinusculo, boolean premium) throws SQLException {
        String sql = "UPDATE contas SET premium = ? WHERE nome_minusculo = ?";
        Connection conexao = databaseManager.getConexao();

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setInt(1, premium ? 1 : 0);
            statement.setString(2, nomeMinusculo);
            statement.executeUpdate();
        }
    }

    /**
     * Remove apenas a senha da conta (mantém a linha, premium, IP e datas
     * intactos). Usado pelo /umuauth unregister — diferente de
     * {@link #deletarConta(String)}, que apaga a conta inteira.
     */
    public void limparSenha(String nomeMinusculo) throws SQLException {
        String sql = "UPDATE contas SET senha_hash = NULL WHERE nome_minusculo = ?";
        Connection conexao = databaseManager.getConexao();

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, nomeMinusculo);
            statement.executeUpdate();
        }
    }

    public List<Account> listarContasPorIp(String ip) throws SQLException {
        String sql = "SELECT * FROM contas WHERE ultimo_ip = ? ORDER BY nome_minusculo";
        Connection conexao = databaseManager.getConexao();
        List<Account> contas = new ArrayList<>();

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, ip);

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    contas.add(mapearConta(resultado));
                }
            }
        }

        return contas;
    }

    public void atualizarUltimoLogin(String nomeMinusculo, String ip, long timestamp) throws SQLException {
        String sql = "UPDATE contas SET ultimo_ip = ?, ultimo_login = ? WHERE nome_minusculo = ?";
        Connection conexao = databaseManager.getConexao();

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, ip);
            statement.setLong(2, timestamp);
            statement.setString(3, nomeMinusculo);
            statement.executeUpdate();
        }
    }

    public List<Account> listarContas(int offset, int limite) throws SQLException {
        String sql = "SELECT * FROM contas ORDER BY nome_minusculo LIMIT ? OFFSET ?";
        Connection conexao = databaseManager.getConexao();
        List<Account> contas = new ArrayList<>();

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setInt(1, limite);
            statement.setInt(2, offset);

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    contas.add(mapearConta(resultado));
                }
            }
        }

        return contas;
    }

    public int contarContas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM contas";
        Connection conexao = databaseManager.getConexao();

        try (PreparedStatement statement = conexao.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {
            resultado.next();
            return resultado.getInt(1);
        }
    }

    private Account mapearConta(ResultSet resultado) throws SQLException {
        return new Account(
                resultado.getLong("id"),
                resultado.getString("nome_minusculo"),
                resultado.getString("senha_hash"),
                resultado.getInt("premium") == 1,
                resultado.getString("ultimo_ip"),
                resultado.getLong("ultimo_login"),
                resultado.getLong("criado_em")
        );
    }
}
