package movement;

import core.Coord;
import core.Settings;

public class RandomGridMovement extends MovementModel {
    private static final String GRID_COUNT_X = "gridCountX";
    private static final String GRID_COUNT_Y = "gridCountY";
    private static final String HOME_X_SETTING = "homeX";
    private static final String HOME_Y_SETTING = "homeY";
    private static final String MOVEMENT_NS = "RandomGridMovement";

    private Coord lastWaypoint;
    private int currentGridX, currentGridY;
    private int gridCountX, gridCountY;
    private int homeX, homeY;

    public RandomGridMovement(Settings settings) {

        super(settings);

        // 2. Buat objek Settings khusus yang merujuk pada namespace global
        Settings globalSettings = new Settings(MOVEMENT_NS);

        // 3. Baca jumlah Grid dari namespace global (RandomGridMovement.gridCountX)
        this.gridCountX = globalSettings.contains(GRID_COUNT_X) ? globalSettings.getInt(GRID_COUNT_X) : 1;
        this.gridCountY = globalSettings.contains(GRID_COUNT_Y) ? globalSettings.getInt(GRID_COUNT_Y) : 1;

        // 4. Baca titik spawn dari namespace grup spesifik (misal: Group1.homeX)
        this.homeX = settings.contains(HOME_X_SETTING) ? settings.getInt(HOME_X_SETTING) : 0;
        this.homeY = settings.contains(HOME_Y_SETTING) ? settings.getInt(HOME_Y_SETTING) : 0;
    }

    protected RandomGridMovement(RandomGridMovement grp) {
        super(grp);
        this.gridCountX = grp.gridCountX;
        this.gridCountY = grp.gridCountY;
        this.homeX = grp.homeX;
        this.homeY = grp.homeY;
    }

    @Override
    public Coord getInitialLocation() {
        assert rng != null : "MovementModel not initialized!";

        currentGridX = homeX;
        currentGridY = homeY;

        Coord c = randomCoordInGrid(currentGridX, currentGridY);
        this.lastWaypoint = c;
        return c;
    }

    @Override
    public Path getPath() {
        Path p = new Path(generateSpeed());
        p.addWaypoint(lastWaypoint.clone());

        int nextGridX, nextGridY;
        do {
            nextGridX = rng.nextInt(gridCountX);
            nextGridY = rng.nextInt(gridCountY);
        } while (nextGridX == currentGridX && nextGridY == currentGridY);

        currentGridX = nextGridX;
        currentGridY = nextGridY;

        Coord c = randomCoordInGrid(currentGridX, currentGridY);
        p.addWaypoint(c);
        this.lastWaypoint = c;
        return p;
    }

    @Override
    public RandomGridMovement replicate() {
        return new RandomGridMovement(this);
    }

    protected Coord randomCoordInGrid(int gridX, int gridY) {
        return new Coord((rng.nextDouble() * getCoordX()) + (gridX * getCoordX()),
                (rng.nextDouble() * getCoordY()) + (gridY * getCoordY()));
    }

    private double getCoordX() {
        return getMaxX() / (double) gridCountX;
    }

    private double getCoordY() {
        return getMaxY() / (double) gridCountY;
    }
}
