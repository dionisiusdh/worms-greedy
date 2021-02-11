package bot;

import bot.command.*;
import bot.entities.*;
import bot.enums.CellType;
import bot.enums.Direction;
import bot.enums.Profession;

// import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class Bot {
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;

    /**
     * Metode constructor bot
     * @param random ???
     * @param gameState ???
     */
    public Bot(GameState gameState) {
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
    }

    /**
     * Metode untuk mendapatkan worm bot bot
     * @param gameState gameState pada suatu ronde
     * @return array of worms milik bot
     */
    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                //.filter(myWorm -> myWorm.id == 1)
                .findFirst()
                .get();
    }

    /**
     * Metode untuk mendapatkan worm pertama yang dapat ditembak
     * @return worm musuh pertama yang dapat ditembak
     */
    private Worm getFirstWormInRange() {
        /// semua sel yang dalam line of fire
        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            if (enemyWorm.health <= 0) continue;

            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition)) {
                return enemyWorm;
            }
        }

        return null;
    }

    /**
     * ???
     * @param range ???
     * @return ???
     */
    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    /**
     * Metode untuk menentukan sel di sekitar suatu titik
     * @param x absis titik
     * @param y ordinat titik
     * @return List dari sel
     */
    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }

        return cells;
    }

    /**
     * Metode untuk mendapatkan sel di sekitar musuh yang terdekat dengan salah
     * satu worm milik bot
     * @param currentWorm array list berisi worm milik bot
     * @param enemyWorm array list berisi worm milik musuh
     * @return sel terdekat antara salah satu worm milik musuh dengan salah satu
     * worm milik bot
     */
    private Cell getCellNearestToEnemyWorm(Worm currentWorm, Worm enemyWorm){
        List<Cell> surroundingCells = getSurroundingCells(currentWorm.position.x,
                                                    currentWorm.position.y);
        int minDistance = 9999999;
        int currDistance;
        Cell nearestCell = surroundingCells.get(0);

        for (Cell cell : surroundingCells){
            currDistance = euclideanDistance(enemyWorm.position.x,
                                                enemyWorm.position.y,
                                                cell.x, cell.y);
            if (currDistance <= minDistance){
                minDistance = currDistance;
                nearestCell = cell;
            }
        }

        return nearestCell;
    }

    /**
     * Metode untuk menghitung jarak euclidean dari dua titik, a dan b
     * @param aX absis titik a
     * @param aY ordinat titik a
     * @param bX absis titik b
     * @param bY ordinat titik b
     * @return Jarak titik a dan b
     */
    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    /**
     * Metode untuk menentukan apakah koordinat valid
     * @param x absis dari titik
     * @param y ordinat dari titik
     * @return true jika koordinat valik, false jika tidak
     */
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    /**
     * Metode untuk menentukan arah dari dua posisi
     * @param a posisi a
     * @param b posisi b
     * @return arah untuk kedua posisi
     */
    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }

    /**
     * Fungsi untuk mendapatkan posisi terbaik to move to sesuai dengan greedy
     * @param currentWorm worm yang di-select
     * @return posisi terbaik
     */
    private Cell moveEarlyGame() {
        Worm targetWorm;
        Cell targetCell;

        // TODO: Mungkin bisa dibikin lebih mangkus
        if (isExistGoodWorm()){
            targetWorm = getLowestHPWorm();
        } else {
            targetWorm = getNearestWorm();
        }

        targetCell = getCellNearestToEnemyWorm(currentWorm, targetWorm);

        return targetCell;
    }

    /**
     * Metode untuk mendapatkan bot dengan HP terendah
     * @param enemyWorms ArrayList of worms milik musuh
     * @return list worm dengan HP terendah
     */
    private Worm getLowestHPWorm() {
        int leastWormHp = opponent.worms[0].health;
        Worm selected = opponent.worms[0];

        for (Worm worm : opponent.worms){
            if (worm.health < leastWormHp) {
                selected = worm;
            }
        }

        return selected;
    }

    /**
     * Metode untuk mendapatkan bot terdekat
     * @param friendlyWorms ArrayList of worms milik bot
     * @param enemyWorms ArrayList of worms milik musuh
     * @return worm dengan jarak terdekat
     */
    private Worm getNearestWorm() {
        int enemyX, enemyY;
        Worm nearestEnemyWorm = opponent.worms[0];
        int minDistance = 9999999;
        int currDistance;

        for (Worm enemyWorm : opponent.worms){
            enemyX = enemyWorm.position.x;
            enemyY = enemyWorm.position.y;
            currDistance = euclideanDistance(currentWorm.position.x,
                                                currentWorm.position.y,
                                                enemyX, enemyY);

            if (currDistance < minDistance){
                minDistance = currDistance;
                nearestEnemyWorm = enemyWorm;
            }
        }

        return nearestEnemyWorm;
    }

    /**
     * Metode untuk menentukan keberadaan worm dengan selisih HP lebih besar
     * dari batas
     * @param enemyWorms ArrayList berisi worm milik musuh
     * @return true jika ada worm yang memiliki HP dengan selisih lebih besar
     * dari batas
     */
    private boolean isExistGoodWorm() {
        int lowestHp = 99999999;
        for (Worm worm: opponent.worms) {
            if (lowestHp > worm.health && worm.health > 0) {
                lowestHp = worm.health;
            }
        }

        for (Worm worm: opponent.worms) {
            if (worm.health - lowestHp >= 20) {
                return true;
            }
        }

        return false;
    }

    private boolean isBananaBombable(Worm enemyWorm) {
        if (currentWorm.profession != Profession.AGENT) return false;

        Position target = new Position(enemyWorm.position.x,
                                        enemyWorm.position.y);
        int distance = euclideanDistance(currentWorm.position.x,
                                    currentWorm.position.y, target.x, target.y);

        return currentWorm.bananaBombs.count > 0
            && distance <= currentWorm.bananaBombs.range
            && distance > currentWorm.bananaBombs.damageRadius * 0.75;
    }

    private boolean isSnowballable(Worm enemyWorm) {
        if (currentWorm.profession != Profession.TECHNOLOGIST) return false;

        Position target = new Position(enemyWorm.position.x,
                                        enemyWorm.position.y);
        int distance = euclideanDistance(currentWorm.position.x,
                                    currentWorm.position.y, target.x, target.y);
        return currentWorm.snowballs.count > 0
            && distance <= currentWorm.snowballs.range
            && distance > currentWorm.snowballs.freezeRadius * 0.75
            && enemyWorm.roundsUntilUnfrozen == 0;
    }

    /**
     * Metode untuk menjalankan bot
     * @return command yang ingin dijalankan
     */
    public Command run() {
        Worm enemyWorm = getFirstWormInRange();
        if (enemyWorm != null) {
            if (isBananaBombable(enemyWorm))
                return new BananaCommand(enemyWorm.position.x,
                                            enemyWorm.position.y);
            if (isSnowballable(enemyWorm))
                return new SnowballCommand(enemyWorm.position.x,
                                            enemyWorm.position.y);

            Direction direction = resolveDirection(currentWorm.position,
                                                    enemyWorm.position);
            return new ShootCommand(direction);
        }

        Cell targetCell = moveEarlyGame();
        if (targetCell.type == CellType.AIR) {
            return new MoveCommand(targetCell.x, targetCell.y);
        } else if (targetCell.type == CellType.DIRT) {
            return new DigCommand(targetCell.x, targetCell.y);
        }

        return new DoNothingCommand();
    }
}
