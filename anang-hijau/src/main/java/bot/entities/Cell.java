package bot.entities;

import com.google.gson.annotations.SerializedName;
import bot.enums.CellType;

public class Cell {
    @SerializedName("x")
    public int x;

    @SerializedName("y")
    public int y;

    @SerializedName("type")
    public CellType type;

    @SerializedName("powerup")
    public PowerUp powerUp;

    @SerializedName("occupier")
    public Worm occupier;
}
