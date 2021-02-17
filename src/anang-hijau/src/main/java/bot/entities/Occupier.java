package bot.entities;

import com.google.gson.annotations.SerializedName;

public class Occupier extends Worm  {
    @SerializedName("playerId")
    public int playerId;
}
