package bot.entities;

import com.google.gson.annotations.SerializedName;
import bot.enums.PowerUpType;

public class PowerUp {
    @SerializedName("type")
    public PowerUpType type;

    @SerializedName("value")
    public int value;
}
