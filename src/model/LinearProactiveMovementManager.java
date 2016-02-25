package model;

public class LinearProactiveMovementManager extends DinamicMovementManager {

    private MovementTypes movementTypes;

    public LinearProactiveMovementManager(final Position position, final Bounds bounds, final int speed,
            final boolean canFly, final MovementTypes movementTypes) {
        super(position, bounds, movementTypes == MovementTypes.HORIZONTAL_LINEAR ? Actions.MOVE : Actions.JUMP, speed,
                canFly);
        this.movementTypes = movementTypes;
    }

    @Override
    public Position getNextMove() {
        Position actualPosition = this.getPosition();
        if (!UtilityMovement.Move(actualPosition, this.getBounds(), getVolontarAction(this.getAction()), this.getSpeed()).isPresent()) {
            if (this.movementTypes == MovementTypes.HORIZONTAL_LINEAR) {
                if (actualPosition.getDirection() == Directions.LEFT) {
                    actualPosition.setDirection(Directions.RIGHT);
                } else {
                    actualPosition.setDirection(Directions.LEFT);
                }
            } else {
                if (UtilityMovement.splitActions(this.getAction()).contains(Actions.JUMP)) {
                    this.setAction(Actions.FALL);
                } else {
                    this.setAction(Actions.JUMP);
                }
            }
        }
        
        actualPosition = UtilityMovement.Move(actualPosition, this.getBounds(), getVolontarAction(this.getAction()), this.getSpeed())
                .orElseThrow(IllegalStateException::new);
        
        return super.applyGravity(actualPosition);

    }
    
    private Actions getVolontarAction(Actions action) {
        switch (action) {
        case MOVEONFALL:
            switch(this.movementTypes) {
            case HORIZONTAL_LINEAR:
                return Actions.MOVE;
            case VERTICAL_LINEAR:
                return Actions.JUMP;
            default:
                throw new IllegalStateException();
            }
        case MOVEONJUMP:
            switch(this.movementTypes) {
            case HORIZONTAL_LINEAR:
                return Actions.MOVE;
            case VERTICAL_LINEAR:
                return Actions.JUMP;
            default:
                throw new IllegalStateException();
            }
        default:
            return action;
        }
    }

    protected void setMovementTypes(final MovementTypes movementTypes) {
        this.movementTypes = movementTypes;
    }

    protected MovementTypes getMovementTypes() {
        return this.movementTypes;
    }

}
