package me.mundotv.mtblockspot.storange;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RegionsSQL extends Regions {

    private final String username, host, password, database;
    private final String tb_prefix = "mtspotblocks";
    private Connection conn;
    private List<Region> regions;

    public RegionsSQL(String username, String host, String password, String database) {
        this.username = username;
        this.host = host;
        this.password = password;
        this.database = database;
    }

    public Connection getConn() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection("jdbc:mysql://" + this.host + "/" + this.database + "?user=" + this.username + "&password=" + this.password);
        }
        return conn;
    }

    @Override
    public Region getRegionByRadiuns(int x, int z) {
        for (Region r : regions) {
            if (r.inRadiuns(x, z)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public Region getRegionByRadiuns(int x, int z, int r) {
        for (Region rs : regions) {
            if (rs.inRadiuns(x, z, r)) {
                return rs;
            }
        }
        return null;
    }
    
    @Override
    public boolean addRegion(Region r) {
        try {
            PreparedStatement ps = getConn().prepareStatement("INSERT INTO `"+tb_prefix+"_regions` (`own`, `pos_x`, `pos_y`, `pos_z`, `pos_r`, `options`) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.getOwn());
            ps.setInt(2, r.getPosX());
            ps.setInt(3, r.getPosY());
            ps.setInt(4, r.getPosZ());
            ps.setInt(5, r.getPosR());
            ps.setString(6, new Gson().toJson(r.getOptions()));
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                r.setId(rs.getInt(1));
                regions.add(r);
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeRegion(Region r) {
        regions.remove(r);
        if (r.getId() == 0) {
            return true;
        }
        try {
            PreparedStatement ps = getConn().prepareStatement("DELETE FROM `"+tb_prefix+"_regions` WHERE `id` = ?");
            ps.setInt(1, r.getId());
            ps.execute();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateRegion(Region r) {
        try {
            Gson gson = new Gson();
            PreparedStatement ps = getConn().prepareStatement("UPDATE `"+tb_prefix+"_regions` SET `own` = ?, `pos_x` = ?, `pos_y` = ?, `pos_z` = ?, `pos_r` = ?, `players` = ?, `options` = ? WHERE `id` = ?");
            ps.setString(1, r.getOwn());
            ps.setInt(2, r.getPosX());
            ps.setInt(3, r.getPosY());
            ps.setInt(4, r.getPosZ());
            ps.setInt(5, r.getPosR());
            ps.setString(6, gson.toJson(r.getPlayers()));
            ps.setString(7, gson.toJson(r.getOptions()));
            ps.setInt(8, r.getId());
            ps.execute();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean loadRegions() {
        try {
            getConn().prepareStatement("CREATE TABLE IF NOT EXISTS `"+tb_prefix+"_regions` ("
                    + "`id` INT NOT NULL AUTO_INCREMENT,"
                    + "`own` VARCHAR(16) NOT NULL,"
                    + "`pos_x` INT NOT NULL,"
                    + "`pos_y` INT NOT NULL,"
                    + "`pos_z` INT NOT NULL,"
                    + "`pos_r` INT NOT NULL,"
                    + "`options` JSON NOT NULL DEFAULT '{}',"
                    + "`players` JSON NOT NULL DEFAULT '[]',"
                    + "PRIMARY KEY(`id`))"
            ).execute();
            regions = new ArrayList();
            ResultSet rs = getConn().prepareStatement("SELECT * FROM `"+tb_prefix+"_regions`").executeQuery();
            while (rs.next()) {
                Gson gson = new Gson();
                regions.add(new Region(rs.getInt("id"), rs.getInt("pos_x"), rs.getInt("pos_y"), rs.getInt("pos_z"), rs.getInt("pos_r"), rs.getString("own"), gson.fromJson(rs.getString("players"), List.class), gson.fromJson(rs.getString("options"), Region.Options.class)));
            }
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Region> getRegions(String own) {
        List<Region> rs = new ArrayList();
        regions.stream().filter(r -> (r.getOwn().equals(own))).forEachOrdered(r -> {
            rs.add(r);
        });
        return rs;
    }
}
