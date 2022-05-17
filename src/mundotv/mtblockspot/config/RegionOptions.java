package mundotv.mtblockspot.config;

public class RegionOptions {
    private boolean pvp = false, farm = true;

    public void setFarm(boolean farm) {
        this.farm = farm;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }
    
    public boolean isFarm() {
        return farm;
    }

    public boolean isPvp() {
        return pvp;
    }
}
