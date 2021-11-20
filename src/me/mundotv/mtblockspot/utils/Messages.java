package me.mundotv.mtblockspot.utils;

import org.bukkit.configuration.file.FileConfiguration;

public enum Messages {
    /* errors */
    ERROR("messages.errors.internal", ""),
    WRONG_COMMAND("messages.errors.wrong_command", ""),
    UNREMOVED("messages.errors.only_own", ""),
    RADIUNS_LIMIT("messages.errors.radiuns_limit", ""),
    OUT_REGION("messages.errors.out_region", ""),
    NOT_PERMISSION("message.erros.not_permission", ""),
    /* general */
    PROTECT_ON("messages.protect_on", ""),
    PROTECT_OFF("messages.protect_off", ""),
    UNCLAIM("messages.block_removed", ""),
    ;
    
    public static FileConfiguration config;
    private final String path, message;

    private Messages(String path, String message) {
        this.path = path;
        this.message = message;
    }

    public String getMessage() {
        String msg = config.getString(path);
        return (msg == null ? message: msg).replace("&", "§").replace("\\n", "\n");
    }
}
