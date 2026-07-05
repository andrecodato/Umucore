package br.com.umucraft.umucore.logger;

/**
 * Banners ASCII (fonte figlet "standard") exibidos no console quando cada
 * módulo do UmuCore é inicializado. Módulos novos (UmuEconomy, etc.) devem
 * adicionar seu próprio array aqui.
 */
public final class Banners {

    private Banners() {
    }

    public static final String[] UMUCORE = {
            "  _   _ __  __ _   _  ____ ___  ____  _____ ",
            " | | | |  \\/  | | | |/ ___/ _ \\|  _ \\| ____|",
            " | | | | |\\/| | | | | |  | | | | |_) |  _|  ",
            " | |_| | |  | | |_| | |__| |_| |  _ <| |___ ",
            "  \\___/|_|  |_|\\___/ \\____\\___/|_| \\_\\_____|"
    };

    public static final String[] UMUAUTH = {
            "  _   _ __  __ _   _   _   _   _ _____ _   _ ",
            " | | | |  \\/  | | | | / \\ | | | |_   _| | | |",
            " | | | | |\\/| | | | |/ _ \\| | | | | | | |_| |",
            " | |_| | |  | | |_| / ___ \\ |_| | | | |  _  |",
            "  \\___/|_|  |_|\\___/_/   \\_\\___/  |_| |_| |_|"
    };
}
