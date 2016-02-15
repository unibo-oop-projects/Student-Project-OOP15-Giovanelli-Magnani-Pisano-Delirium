package model;


import control.Point;

public enum Actions {
    MOVE((point, speed, direction) -> (direction == Directions.RIGHT ? new Point(point.getX() + speed, point.getY()) : new Point(point.getX() - speed, point.getY()))),
    JUMP((point, speed, direction) -> (new Point(point.getX(), point.getY() + speed))),
    FALL((point, speed, direction) -> (new Point(point.getX(), point.getY() + speed))),
    STOP((point, speed, direction) -> (point));

    
    private final DeterminateNewPoint function;
    
    Actions(final DeterminateNewPoint function) {
        this.function = function;
    }
    
    public DeterminateNewPoint getFunction() {
        return this.function;
    }
}
