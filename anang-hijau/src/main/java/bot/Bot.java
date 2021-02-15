package bot;

import bot.command.*;
import bot.entities.*;
import bot.enums.CellType;
import bot.enums.Direction;
import bot.enums.PowerUpType;

// (༼ つ ◕_◕ ༽つ ( •_•)>⌐■-■ ( •_•)>⌐■-■ ( •_•)>⌐■-■－O－
// nice

import java.util.*;
import java.util.stream.Collectors;

public class Bot {
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;
    private static ArrayList<Position> hpLoc;

    /**
     * Metode constructor bot
     * @param gameState Keadaan game di ronde
     */
    public Bot(GameState gameState) {
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);

        if (gameState.currentRound == 1) {
            // hpLoc hanya perlu diset pada round pertama
            hpLoc = getHealthPackLoc();
        }
    }

    /**
     * Metode untuk mendapatkan list posisi dari health pack
     * @return list posisi heealth pack
     */
    private ArrayList<Position> getHealthPackLoc() {
        /// array list untuk menyimpan posisi-posisi sel yang ada health pack
        ArrayList<Position> hpLoc = new ArrayList<Position>();
        for (Cell[] cells : gameState.map) {
            for (Cell cell: cells) {
                // kalo ga ada powerup di sel, di-skip aja
                if (cell.powerUp == null) continue;

                // kalo di selnya ada health pack, tambahin posisi sel ke hpLoc
                if (cell.powerUp.type == PowerUpType.HEALTH_PACK) {
                    hpLoc.add(new Position(cell.x, cell.y));
                }
            }
        }

        return hpLoc;
    }

    /**
     * Metode untuk mendapatkan worm bot bot
     * @param gameState gameState pada suatu ronde
     * @return array of worms milik bot
     */
    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
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
            // kalo musuh udah mati, skip
            if (enemyWorm.health <= 0) continue;

            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition)) {
                return enemyWorm;
            }
        }

        return null;
    }

    /**
     * Metode untuk mendapatkan worm pertama yang dapat ditembak
     * @return worm musuh pertama yang dapat ditembak
     */
    private Worm getFirstWormInRangeBanana() {
        for (Worm enemyWorm : opponent.worms) {
            if (enemyWorm.health <= 0) continue;

            int enemyX = enemyWorm.position.x,
                enemyY = enemyWorm.position.y,
                curX = currentWorm.position.x,
                curY = currentWorm.position.y,
                distance = euclideanDistance(curX, curY, enemyX, enemyY);

            if (distance < currentWorm.bananaBombs.range) {
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

                // Friendly fire checker
                if (cell.occupier != null && cell.occupier.playerId ==
                    gameState.myPlayer.id) {
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
     * Metode untuk mendapatkan sel di sekitar worm yang terdekat dengan salah
     * satu worm musuh dan belum ditempati
     * @param enemyWorm worm milik musuh
     * @return sel terdekat antara salah satu worm milik musuh dengan salah satu
     * worm milik bot
     */
    private Cell getSurroundCellNearestToTarget(Cell target){
        List<Cell> surroundingCells = getSurroundingCells(currentWorm.position.x,
                                                    currentWorm.position.y);
        int minDistance = 9999999;
        int currDistance;
        Cell nearestCell = surroundingCells.get(0);

        for (Cell cell : surroundingCells){
            if (cell.occupier == null) {
                currDistance = euclideanDistance(target.x, target.y,
                                                cell.x, cell.y);
                if (currDistance <= minDistance){
                    minDistance = currDistance;
                    nearestCell = cell;
                }
            }
        }

        return nearestCell;
    }

    /**
     * Fungsi untuk mendapatkan posisi terbaik to move to sesuai dengan greedy
     * @param currentWorm worm yang di-select
     * @return posisi terbaik
     */
    private Cell getLowetOrNearest() {
        Worm targetWorm;

        // good worm adalah worm yang memiliki health minimum dan selisihnya
        // dengan health minimum kedua lebih besar dari threshold
        // Jika ada good worm, kejar good worm, jika tidak ke yg terdekat
        // TODO: Mungkin bisa dibikin lebih mangkus
        if (isExistGoodWorm()) {
            targetWorm = getLowestHPWorm();
        } else {
            targetWorm = getNearestWorm();
        }

        return getCell(targetWorm.position.x, targetWorm.position.y);
    }

    /**
     * Metode untuk memeriksa keadaan healthpack dan membuangnya dari list jika
     * sudah tidak ada
     */
    private void checkHealthPack() {
        for (Iterator<Position> it = hpLoc.iterator(); it.hasNext();) { 
            Position pos = it.next();
            Cell curCel = getCell(pos.x, pos.y);

            if (curCel.powerUp == null) {
                it.remove();
            }
        }
    }

    /**
     * Metode untuk mendapatkan health pack yang dekat dengan pemain
     * @return sel yang memiliki health pack dan dekat dengan pemain
     */
    private Cell getCloseHealthPack() {
        if (hpLoc.size() != 0) {
            for (Position hp : hpLoc) {
                int curX = currentWorm.position.x;
                int curY = currentWorm.position.y;
                int distance = euclideanDistance(curX, curY, hp.x, hp.y);
                final int MAX_RANGE_TO_HP = 100;

                if (distance <= MAX_RANGE_TO_HP
                    && getCell(hp.x, hp.y).powerUp != null) {
                    return getCell(hp.x, hp.y);
                }
            }
        }
        
        return null;
    }

    /**
     * Mendapatkan sel di peta pada map
     * @param x absis
     * @param y ordinat
     * @return sel peta di titik (x,y)
     */
    private Cell getCell(int x, int y) {
        return gameState.map[y][x];
    }

    /**
     * Metode untuk mendapatkan bot dengan HP terendah
     * @return worm dengan HP terendah
     */
    private Worm getLowestHPWorm() {
        int leastWormHp = opponent.worms[0].health;
        Worm selected = opponent.worms[0];

        for (Worm worm : opponent.worms){
            if (worm.health < leastWormHp && worm.health > 0) {
                selected = worm;
            }
        }

        return selected;
    }

    /**
     * Metode untuk mendapatkan bot terdekat
     * @return worm dengan jarak terdekat
     */
    private Worm getNearestWorm() {
        int enemyX, enemyY;
        Worm nearestEnemyWorm = opponent.worms[0];
        int minDistance = 9999999;
        int currDistance;

        for (Worm enemyWorm : opponent.worms){
            if (enemyWorm.health <= 0) continue;

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
     * dari batas (good worm)
     * @return true jika ada worm yang memiliki HP dengan selisih lebih besar
     * dari batas
     */
    private boolean isExistGoodWorm() {
        /// darah minimum
        int lowestHp = 99999999;
        /// threshold
        final int threshold = 20;

        // Mencari darah terkecil worm
        for (Worm worm: opponent.worms) {
            if (lowestHp > worm.health && worm.health > 0) {
                lowestHp = worm.health;
            }
        }

        // Bandingin selisih hp dengan threshold
        for (Worm worm: opponent.worms) {
            if (worm.health - lowestHp >= threshold) {
                return true;
            }
        }

        return false;
    }

    /**
     * Metode untuk mengecek apakah musuh dapat di-banana bomb atau tidak
     * @param enemyWorm musuh yang ingin dicek kebisaannya di-banana bomb
     * @return true jika bisa, false jika tidak
     */
    private boolean isBananaBombable(Worm enemyWorm) {
        if (currentWorm.bananaBombs == null) return false;

        Position target = new Position(enemyWorm.position.x,
                                        enemyWorm.position.y);
        int distance = euclideanDistance(currentWorm.position.x,
                                    currentWorm.position.y, target.x, target.y);

        /* Syarat:
         * - Banana bomb masih tersisa
         * - Jarak ke musuh (target) tidak melebihi jarak lempar banana bomb
         * - Jarak worm ke musuh (target) melebihi radius damage banana bomb
         */
        return currentWorm.bananaBombs.count > 0
            && distance <= currentWorm.bananaBombs.range
            && (distance > currentWorm.bananaBombs.damageRadius
                || currentWorm.health < 50);
    }

    /**
     * Metode untuk mengecek apakah musuh dapat di-snowball atau tidak
     * @param enemyWorm musuh yang ingin dicek kebisaannya di-snowball
     * @return true jika bisa, false jika tidak
     */
    private boolean isSnowballable(Worm enemyWorm) {
        if (currentWorm.snowballs == null) return false;

        Position target = new Position(enemyWorm.position.x,
                                        enemyWorm.position.y);
        int distance = euclideanDistance(currentWorm.position.x,
                                    currentWorm.position.y, target.x, target.y);
        /* Syarat:
         * - Banana bomb masih tersisa
         * - Jarak ke musuh (target) tidak melebihi jarak lempar snowball
         * - Jarak worm ke musuh (target) melebihi radius damage snowball
         * - Musuh yang ingin dilempari snowball (target) tidak frozen
         */
        return currentWorm.snowballs.count > 0
            && distance <= currentWorm.snowballs.range
            && (distance > currentWorm.snowballs.freezeRadius
                || currentWorm.health < 50)
            && enemyWorm.roundsUntilUnfrozen == 0;
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
     * Metode untuk menjalankan bot
     * @return command yang ingin dijalankan
     */
    public Command run() {
        if (hpLoc.size() > 0) { 
            // jika sebelumnya masih ada healthpack, dicek di round ini masih
            // ada nggak
            checkHealthPack();
        }

        Worm enemyWorm;

        // Usaha untuk mem-banana bomb musuh
        if (currentWorm.bananaBombs != null) {
            enemyWorm = getFirstWormInRangeBanana();
            if (enemyWorm != null && isBananaBombable(enemyWorm)) { 
                return new BananaCommand(enemyWorm.position.x,
                                            enemyWorm.position.y);
            }
        }

        // Usaha untuk melempar musuh dengan snowball atau menembak musuh
        enemyWorm = getFirstWormInRange();
        if (enemyWorm != null) {

            int enX = enemyWorm.position.x;
            int enY = enemyWorm.position.y;

            // Snowball musuh kalo bisa
            if (isSnowballable(enemyWorm))
                return new SnowballCommand(enX, enY);

            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
            return new ShootCommand(direction);
        }

        /// Sel di sekitar worm yang akan jadi tempat move/dig selanjutnya
        Cell targetCell, enemyCell, hpCell;

        // Nyari health pack terdekat
        hpCell = getCloseHealthPack();
        if (hpCell != null){
            // Kalo ada healthpack yang deket, nentuin move ke sel mana
            int x = currentWorm.position.x,
                y = currentWorm.position.y;

            // Kalo udah di sebelah health pack, lgsg jalan ke health pack aja
            if (euclideanDistance(x, y, hpCell.x, hpCell.y) <= 1) {
                targetCell = hpCell;
            } else {
                targetCell = getSurroundCellNearestToTarget(hpCell);
            }
        } else {
            // Tentuin mendingan ke musuh terdekat atau tersekarat
            enemyCell = getLowetOrNearest();

            // Nentuin move ke sel mana
            targetCell = getSurroundCellNearestToTarget(enemyCell);

        }

        // Cek tipe sel yang dituju
        if (targetCell.type == CellType.AIR || targetCell.type == CellType.LAVA) {
            return new MoveCommand(targetCell.x, targetCell.y);
        } else if (targetCell.type == CellType.DIRT) {
            return new DigCommand(targetCell.x, targetCell.y);
        }

        return new DoNothingCommand();
    }
}