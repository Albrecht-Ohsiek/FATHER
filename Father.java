import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import dev.robocode.tankroyale.botapi.graphics.Point;

import java.util.*;

// ------------------------------------------------------------------
// Father
// ------------------------------------------------------------------
public class Father extends Bot {
    private final Color PRIMARY_COLOR = Color.WHITE;
    private final Color SECONDARY_COLOR = Color.BLACK;

    private final int HISTORY_SIZE = 2;
    private final Map<Integer, Deque<Point>> enemyPositions = new HashMap<>();

    public static void main(String[] args) {
        new Father().start();
    }

    @Override
    public void run() {
        setColors();

        while (isRunning()) {
            setTurnRight(10_000);
            setMaxSpeed(5);
            forward(10_000);
        }
    }

    @Override
    public void onHitBot(HitBotEvent e) {
        double direction = directionTo(e.getX(), e.getY());
        double bearing = calcBearing(direction);
        if (bearing > -10 && bearing < 10) {
            fire(3);
        }
        if (e.isRammed()) {
            turnRight(10);
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        updateEnemyPosition(e);
        
        int botId = e.getScannedBotId();
        List<Integer> stationaryBots = getStationaryBots();

        if (stationaryBots.isEmpty()) {
            fire(3);
        } else {
            if (stationaryBots.contains(botId)) {
                fire(3);
            }
        }
    }

    private void setColors() {
        setBodyColor(PRIMARY_COLOR);
        setTracksColor(SECONDARY_COLOR);
        setTurretColor(PRIMARY_COLOR);
        setGunColor(SECONDARY_COLOR);
        setRadarColor(PRIMARY_COLOR);
        setBulletColor(PRIMARY_COLOR);
        setScanColor(PRIMARY_COLOR);
    }

    private void updateEnemyPosition(ScannedBotEvent e) {
        int botId = e.getScannedBotId();
        Point position = new Point(e.getX(), e.getY());

        enemyPositions.putIfAbsent(botId, new ArrayDeque<>());
        Deque<Point> positions = enemyPositions.get(botId);

        if (positions.size() >= HISTORY_SIZE) {
            positions.removeFirst();
        }
        positions.addLast(position);
    }

    private boolean isStationary(Deque<Point> positions) {
        if (positions.size() < HISTORY_SIZE) return false;

        Point first = positions.getFirst();
        boolean stationary = positions.stream().allMatch(p -> p.equals(first));

        return stationary;
    }

    private List<Integer> getStationaryBots() {
        List<Integer> stationaryBots = new ArrayList<>();
        for (var entry : enemyPositions.entrySet()) {
            if (isStationary(entry.getValue())) {
                stationaryBots.add(entry.getKey());
            }
        }
        return stationaryBots;
    }
}
