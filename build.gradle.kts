plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.4.3"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation("org.xerial:sqlite-jdbc:3.53.2.0")
    implementation("org.mindrot:jbcrypt:0.4")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.sqlite", "br.com.umucraft.umucore.libs.sqlite")
        relocate("org.mindrot.jbcrypt", "br.com.umucraft.umucore.libs.jbcrypt")
        // Necessário para reescrever META-INF/services/java.sql.Driver com o
        // nome relocado da classe (senão o driver SQLite não se autorregistra).
        mergeServiceFiles()
    }
}

tasks.register<Copy>("copiarPlugin") {
    // Diz para rodar o shadowJar antes de copiar (o jar precisa conter sqlite-jdbc/jbcrypt)
    dependsOn("shadowJar")

    // Pega o jar sombreado (com as dependências embutidas) na pasta build/libs
    from(tasks.named("shadowJar"))

    // Alvo: A pasta plugins do seu servidor de testes (Ajuste o caminho se necessário)
    into("C:/ServidorTeste/plugins")
}
