package bot.entities;

import com.google.gson.annotations.SerializedName;

public class MyWorm extends Worm {
    @SerializedName("weapon")
    public Weapon weapon;

    @SerializedName("snowballs")
    public Snowball snowballs;

    @SerializedName("bananaBombs")
    public BananaBomb bananaBombs;
}
