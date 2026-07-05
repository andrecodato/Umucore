package br.com.umucraft.umucore.auth.data;

import br.com.umucraft.umucore.logger.UmuLogger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Dona da conexão SQLite única do plugin (um único arquivo, sem necessidade
 * de pool de conexões). Todo acesso via {@link AccountRepository} deve
 * acontecer fora da main thread do servidor.
 */
public class DatabaseManager {

    private final File arquivoBanco;
    private Connection conexao;

    public DatabaseManager(File pastaDeDados) {
        if (!pastaDeDados.exists()) {
            pastaDeDados.mkdirs();
        }
        this.arquivoBanco = new File(pastaDeDados, "dados.db");
    }

    /**
     * Abre a conexão com o SQLite. Chamado uma única vez, de forma síncrona,
     * durante o onEnable (antes do servidor aceitar jogadores).
     */
    public void conectar() {
        try {
            conexao = DriverManager.getConnection("jdbc:sqlite:" + arquivoBanco.getAbsolutePath());
            UmuLogger.sucesso("Database", "Conectado ao banco de dados SQLite.");
        } catch (SQLException e) {
            UmuLogger.erroCritico("Database", "Falha ao conectar ao banco de dados SQLite.", e);
        }
    }

    /**
     * Cria a tabela "contas" caso ainda não exista.
     */
    public void criarTabelas() {
        String ddl = """
                CREATE TABLE IF NOT EXISTS contas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome_minusculo TEXT NOT NULL UNIQUE,
                    senha_hash TEXT,
                    premium INTEGER NOT NULL DEFAULT 0,
                    ultimo_ip TEXT,
                    ultimo_login INTEGER,
                    criado_em INTEGER NOT NULL
                );
                """;
        String indice = "CREATE INDEX IF NOT EXISTS idx_contas_ip ON contas(ultimo_ip);";

        try (Statement statement = conexao.createStatement()) {
            statement.execute(ddl);
            statement.execute(indice);
        } catch (SQLException e) {
            UmuLogger.erroCritico("Database", "Falha ao criar as tabelas do banco de dados.", e);
        }
    }

    public Connection getConexao() {
        return conexao;
    }

    public void fechar() {
        if (conexao == null) {
            return;
        }
        try {
            conexao.close();
            UmuLogger.info("Database", "Conexão com o banco de dados encerrada.");
        } catch (SQLException e) {
            UmuLogger.erroCritico("Database", "Falha ao fechar a conexão com o banco de dados.", e);
        }
    }
}
