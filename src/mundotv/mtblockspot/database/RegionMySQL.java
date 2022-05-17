package mundotv.mtblockspot.database;

import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mundotv.mtblockspot.config.Region;
import mundotv.mtblockspot.config.RegionOptions;

public class RegionMySQL extends RegionDatabase {

    private final String database, host, username, password;
    private Connection conn;

    public RegionMySQL(String database, String host, String username, String password) {
        this.database = database;
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public Connection getConn() throws SQLException {
        if (this.conn == null || this.conn.isClosed()) {
            this.conn = DriverManager.getConnection("jdbc:mysql://" + this.host + "/" + this.database + "?user=" + this.username + "&password=" + this.password);
        }
        return conn;
    }

    @Override
    public boolean addRegion(Region r) {
        try {
            PreparedStatement ps = this.getConn().prepareStatement("INSERT INTO `mtblockspot_regions` (`own`, `block`, `x`, `y`, `z`, `r`, `options`) VALUES (?, ?, ?, ?, ?, ?, ?)");
            Gson gson = new Gson();
            ps.setString(1, r.getOwn());
            ps.setString(2, r.getBlockName());
            ps.setInt(3, r.getX());
            ps.setInt(4, r.getY());
            ps.setInt(5, r.getZ());
            ps.setInt(6, r.getR());
            ps.setString(7, gson.toJson(r.getOptions()));
            ps.execute();
            this.regions.add(r);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeRegion(Region r) {
        try {
            PreparedStatement ps = this.getConn().prepareStatement("DELETE FROM `mtblockspot_regions` WHERE `x` = ? AND `y` = ? AND `z` = ?");
            ps.setInt(1, r.getX());
            ps.setInt(2, r.getY());
            ps.setInt(3, r.getZ());
            regions.remove(r);
            ps.execute();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void loadRegions() throws IOException {
        try {
            this.getConn().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `mtblockspot_regions` "
                    + "("
                    + "`own` VARCHAR(20) NOT NULL,"
                    + "`world` VARCHAR(64) NOT NULL,"
                    + "`block` VARCHAR(64) NOT NULL,"
                    + "`x` INT NOT NULL,"
                    + "`y` INT NOT NULL,"
                    + "`z` INT NOT NULL,"
                    + "`r` INT NOT NULL,"
                    + "`players` JSON NOT NULL DEFAULT '[]',"
                    + "`options` JSON NOT NULL DEFAULT '{}'"
                    + ")"
            ).execute();
            this.regions = new ArrayList();
            Gson gson = new Gson();
            PreparedStatement ps = getConn().prepareStatement("SELECT * FROM `mtblockspot_regions`");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<String> players = (List<String>) gson.fromJson(rs.getString("options"), List.class);
                RegionOptions options = gson.fromJson(rs.getString("options"), RegionOptions.class);
                regions.add(new Region(rs.getString("own"), rs.getString("block"), rs.getString("world"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("r"), players, options));
            }
        } catch (SQLException ex) {
            throw new IOException("MySQL error! " + ex.getMessage());
        }
    }

    @Override
    public boolean updateRegion(Region r) {
        try {
            PreparedStatement ps = this.getConn().prepareStatement("UPDATE `mtblockspot_regions` SET `players` = ?, `options` = ? WHERE `x` = ? AND `y` = ? AND `z` = ?");
            Gson gson = new Gson();
            ps.setString(1, gson.toJson(r.getPlayers()));
            ps.setString(2, gson.toJson(r.getOptions()));
            ps.setInt(3, r.getX());
            ps.setInt(4, r.getY());
            ps.setInt(5, r.getZ());
            ps.execute();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
