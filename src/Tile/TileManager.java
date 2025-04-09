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
import java.util.*;
import java.util.List;

public class TileManager {
    public final GamePanel gamePanel;
    public final Map<Integer, Tile> tiles; // store tiles by GID
    public final List<int[][]> mapLayers; // Store each layer's tile data
    public final List<Rectangle> collisionBoxes = new ArrayList<>(); // Store rectangle collisions
    public int mapWidth;
    public int mapHeight;


    public TileManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.tiles = new HashMap<>();
        this.mapLayers = new ArrayList<>();
        loadTileSet("/maps/World Map 1.tmj");
        loadMap("/maps/World Map 1.tmj");
    }

    public void loadTileSet(String mapPath) {
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
        }catch (Exception _){
        }

    }

    public void loadMap(String mapPath) {
        try (InputStream is = getClass().getResourceAsStream(mapPath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            Gson gson = new Gson();
            JsonObject mapData = gson.fromJson(br, JsonObject.class);
            mapWidth = mapData.get("width").getAsInt();
            mapHeight = mapData.get("height").getAsInt();
            JsonArray layers = mapData.getAsJsonArray("layers");

            for (JsonElement layerElement : layers) {
                JsonObject layer = layerElement.getAsJsonObject();
                String type = layer.get("type").getAsString();

                if (type.equals("tilelayer")) {
                    int[][] layerData = new int[mapWidth][mapHeight];
                    JsonArray data = layer.getAsJsonArray("data");
                    int index = 0;
                    for (int y = 0; y < mapHeight; y++) {
                        for (int x = 0; x < mapWidth; x++) {
                            layerData[x][y] = data.get(index++).getAsInt();
                        }
                    }
                    mapLayers.add(layerData);
                } else if (type.equals("objectgroup")) {
                    JsonArray objects = layer.getAsJsonArray("objects");
                    for (JsonElement objectElement : objects) {
                        JsonObject object = objectElement.getAsJsonObject();
                        int x = object.get("x").getAsInt();
                        int y = object.get("y").getAsInt();
                        int width = object.get("width").getAsInt();
                        int height = object.get("height").getAsInt();

                        collisionBoxes.add(new Rectangle(x, y, width, height));
                    }
                }
            }
        } catch (Exception _) {
        }
    }

    public void draw(Graphics2D g2) {
        // Set rendering hints once (already set in GamePanel)
        int tileSize = gamePanel.getTileSize();

        // Calculate visible tile indices (columns/rows)
        int leftCol = (gamePanel.player.worldX - gamePanel.player.screenX) / tileSize;
        int rightCol = (gamePanel.player.worldX + gamePanel.player.screenX) / tileSize + 1;
        int topRow = (gamePanel.player.worldY - gamePanel.player.screenY) / tileSize;
        int bottomRow = (gamePanel.player.worldY + gamePanel.player.screenY) / tileSize + 1;

        leftCol -= 1;
        rightCol += 1;
        topRow -= 1;
        bottomRow += 1;

        // Clamp indices to world bounds to avoid out-of-bounds
        leftCol = Math.max(0, leftCol);
        rightCol = Math.min(mapWidth, rightCol);
        topRow = Math.max(0, topRow);
        bottomRow = Math.min(mapHeight, bottomRow);

        for (int[][] layerData : mapLayers) {
            // Iterate ONLY over visible tiles
            for (int row = topRow; row < bottomRow; row++) {
                for (int col = leftCol; col < rightCol; col++) {
                    int gid = layerData[col][row];
                    if (gid == 0) continue;

                    Tile tile = tiles.get(gid);
                    if (tile != null) {
                        // Calculate screen coordinates
                        int screenX = (col * tileSize - gamePanel.player.worldX + gamePanel.player.screenX);
                        int screenY = row * tileSize - gamePanel.player.worldY + gamePanel.player.screenY;


                        // Draw the tile
                        g2.drawImage(tile.image, screenX, screenY, tileSize, tileSize, null);
                    }
                }
            }
        }
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getMapWidth() {
        return mapWidth;
    }
}

