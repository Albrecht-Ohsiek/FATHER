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

    private final int HISTORY_SIZE = 5;
    private final Map<Integer, Deque<Point>> enemyPositions = new HashMap<>();

    public static void main(String[] args) {
        new Father().start();
    }

    @Override
    public void run() {
        setColors();

        while (isRunning()) {
            spinStrategy();
        }
    }

    // Spin and move with full speed
    private void spinStrategy() {
        setTurnRight(10_000);
        setMaxSpeed(5);
        forward(10_000);
    }

    // YOU HIT ME, YOU DIE NOW!
    @Override
    public void onHitBot(HitBotEvent e) {
        turnToFaceTarget(e.getX(), e.getY());

        double distance = distanceTo(e.getX(), e.getY());
        smartFire(distance);

        if (e.isRammed()){
            turnRight(10);
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        updateEnemyPosition(e);
        
        // Should still get a count of all stationary bots so ignores eratic bots until no more stationary bots
        double distance = distanceTo(e.getX(), e.getY());
        if (isStationary(enemyPositions.get(e.getScannedBotId()))) {
            turnToFaceTarget(e.getX(), e.getY());
            fire(3);
        } else {
            smartFire(distance);
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

    private void smartFire(double distance) {
        if (getEnergy() < 20 || distance > 200) {
            fire(1);
        } else if (distance > 50) {
            fire(2);
        } else {
            fire(3);
        }
    }

    private void turnToFaceTarget(double x, double y) {
        double bearing = bearingTo(x, y);
        turnRight(bearing);
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
}
