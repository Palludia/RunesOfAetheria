package Tile;

import Main.GamePanel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileManager {
    private final GamePanel gamePanel;
    private final Map<Integer, Tile> tiles; // store tiles by GID
    private List<int[][]> mapLayers; // Store each layer's tile data


    public TileManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.tiles = new HashMap<>();
        this.mapLayers = new ArrayList<>();
        loadTileSet("/maps/WorldMapProto.tmj");
        loadMap("/maps/worldMapProto.tmj");
    }

    private void loadTileSet(String mapPath) {
        try(InputStream is = getClass().getResourceAsStream(mapPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            Gson gson = new Gson();
            JsonObject mapData = gson.fromJson(br,JsonObject.class);

            // parse tilesets
            JsonArray tilesets = mapData.getAsJsonArray("tilesets");
            for(JsonElement tilesetElement : tilesets) {
                JsonObject tileset = tilesetElement.getAsJsonObject();

                // Load tileset Image
                String tilesetImagePath = tileset.get("image").getAsString().replace("../","/");
                int tileWidth = tileset.get("tilewidth").getAsInt();
                int tileHeight = tileset.get("tileheight").getAsInt();
                int tilesetColumns = tileset.get("columns").getAsInt();
                int firstGID = tileset.get("firstgid").getAsInt(); // FIRST GID in this tileset

                BufferedImage tilesetImage = ImageIO.read(getClass().getResourceAsStream(tilesetImagePath));

                // Slicing the tileset image into individual tiles
                int tileCount = tileset.get("tilecount").getAsInt();
                for(int i = 0; i<tileCount; i++) {
                    int tileX = (i % tilesetColumns) * tileWidth;
                    int tileY = (i / tilesetColumns) * tileHeight;

                    BufferedImage tileImage = tilesetImage.getSubimage(tileX,tileY,tileWidth,tileHeight);
                    Tile tile = new Tile();
                    tile.image = tileImage;
                    tiles.put(firstGID + i, tile); // Store tile by GID
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadMap(String mapPath) {
        try(InputStream is = getClass().getResourceAsStream(mapPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is))){

            Gson gson = new Gson();
            JsonObject mapData = gson.fromJson(br, JsonObject.class);

            int mapWidth = mapData.get("width").getAsInt();
            int mapHeight = mapData.get("height").getAsInt();

            JsonArray layers = mapData.getAsJsonArray("layers");
            for(JsonElement layerElement : layers) {
                JsonObject layer = layerElement.getAsJsonObject();
                if(!layer.get("type").getAsString().equals("tilelayer")) continue;

                int[][] layerData = new int[mapWidth][mapHeight];
                JsonArray data = layer.getAsJsonArray("data");
                int col = 0, row = 0;

                for(int i = 0; i < data.size(); i++) {
                    int gid = data.get(i).getAsInt();
                    layerData[col][row] = gid; // Store raw GID directly

                    if(++col == mapWidth) {
                        col = 0;
                        row++;
                    }
                }
                mapLayers.add(layerData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        // Draw all layers in order
        for(int[][] layerData: mapLayers) {
            for(int worldRow = 0; worldRow < gamePanel.getMaxScreenRow(); worldRow++) {
                for(int worldCol = 0; worldCol < gamePanel.getMaxScreenCol(); worldCol++) {
                    int gid = layerData[worldCol][worldRow];
                    if(gid == 0) continue; // skip empty tiles

                    Tile tile = tiles.get(gid);
                    if(tile != null) {
                        int worldX = worldCol * gamePanel.getTileSize();
                        int worldY = worldRow * gamePanel.getTileSize();
                        int screenX = worldX - gamePanel.player.worldX + gamePanel.player.screenX;
                        int screenY = worldY - gamePanel.player.worldY + gamePanel.player.screenY;

                        if (worldX + gamePanel.getTileSize() > gamePanel.player.worldX - gamePanel.player.screenX &&
                            worldX  - gamePanel.getTileSize() < gamePanel.player.worldX + gamePanel.player.screenX &&
                            worldY + gamePanel.getTileSize() > gamePanel.player.worldY - gamePanel.player.screenY &&
                            worldY - gamePanel.getTileSize() < gamePanel.player.worldY + gamePanel.player.screenY) {
                            g2.drawImage(tile.image,screenX,screenY,gamePanel.getTileSize(),gamePanel.getTileSize(),null);
                        }
                    }
                }
            }
        }
    }
}


