package model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import utility.Pair;

public class ModelImpl implements Model {

    private static final int DEFAULT_OFFSET_X = 500;
    private static final ModelImpl singleton = new ModelImpl();
    private Arena arena;
    private ArenaManager arenaManager;

    private ModelImpl() {
    }

    public static ModelImpl getModel() {
        return singleton;
    }

    @Override
    public void notifyEvent(final Directions direction) {
        this.arena.getHero().setPosition(arena.getHero().getPosition().getPoint(), direction);
    }

    @Override
    public void notifyEvent(final Actions action) {
        this.arena.getHero().setAction(action);
    }

    public List<EntitiesInfo> updateArena() {

        int size = this.arena.getEntities().size();
        for (int i = 0; i < size; i++) {
            if (this.arena.getEntities().get(i).getLifeManager().getLife() == 0) {
                this.arena.getEntities().remove(i);
                i--;
                size--;
            }
        }

        size = this.arena.getBullets().size();
        for (int i = 0; i < size; i++) {
            if (this.arena.getBullets().get(i).getLifeManager().getLife() == 0) {
                this.arena.getBullets().remove(i);
                i--;
                size--;
            }
        }

        this.arenaManager.MoveEntities();

        List<EntitiesInfo> bullets = new LinkedList<>();

        this.arena.getEntities().stream().forEach(t -> {
            if (t.getShootManager().isPresent()) {
                if (t.getPosition().getPoint().getX() > this.arena.getHero().getPosition().getPoint().getX()
                        - DEFAULT_OFFSET_X
                        && t.getPosition().getPoint().getX() < this.arena.getHero().getPosition().getPoint().getX()
                                + DEFAULT_OFFSET_X) {
                    Optional<EntitiesInfo> bullet = t.getShootManager().get().getBullet(t.getCode(), t.getPosition());
                    if (bullet.isPresent()) {
                        bullets.add(bullet.get());
                    }
                }
            }

        });

        return bullets;
    }

    @Override
    public List<EntitiesInfoToControl> getState() {
        final List<EntitiesInfoToControl> result = new LinkedList<>();

        if (this.arenaManager.isGameWon()) {
            Entities t = this.arena.getGoal();
            result.add(new EntitiesInfoToControlImpl(t.getCode(), t.getLifeManager().getLife(), t.getPosition(),
                    t.getAction(), Optional.empty()));

        } else if (this.arena.getHero().getLifeManager().getLife() != 0) {

            Stream.concat(this.arena.getEntities().stream(), this.arena.getBullets().stream()).forEach(t -> {
                Optional<Integer> speed = t.getMovementManager().isPresent()
                        ? Optional.of(t.getMovementManager().get().getSpeed()) : Optional.empty();
                result.add(new EntitiesInfoToControlImpl(t.getCode(), t.getLifeManager().getLife(), t.getPosition(),
                        t.getAction(), speed));
            });
        } else {
            Entities t = this.arena.getHero();
            result.add(new EntitiesInfoToControlImpl(t.getCode(), t.getLifeManager().getLife(), t.getPosition(),
                    t.getAction(), Optional.of(t.getMovementManager().get().getSpeed())));
        }

        return result;
    }

    @Override
    public void createArena(final List<EntitiesInfo> entitiesInfo) {

        this.arena = new ArenaImpl();
        this.arenaManager = new ArenaManagerImpl(this.arena);

        entitiesInfo.stream().forEach(t -> {

            Pair<Optional<Position>, Optional<MovementManager>> pair = MovementManagerFactory
                    .getMovementManager(t.getPosition(), t.getMovementInfo());
            Optional<ShootManager> shootManager = ShootManagerFactory.getShootManager(t.getShootInfo());

            this.arena.add(new Entities.Builder()
                                        .code(t.getCode())
                                        .lifeManager(new LifeManager(t.getLife(), t.getLifePattern()))
                                        .position(pair.getX().isPresent() ? pair.getX().get() : null)
                                        .movementManager(pair.getY().isPresent() ? pair.getY().get() : null)
                                        .shootManager(shootManager.isPresent() ? shootManager.get() : null)
                                        .contactDamage(t.getContactDamage().isPresent() ? t.getContactDamage().get() : null)
                                        .build());
        });

    }

    @Override
    public void putBullet(final List<EntitiesInfo> entitiesInfo) {
        entitiesInfo.stream().forEach(t -> {
            MovementInfo movementInfo = t.getMovementInfo().get();
            this.arena.add(new Entities.Builder()
                                        .code(t.getCode())
                                        .movementManager(new LinearProactiveMovementManager(t.getPosition(), movementInfo.getBounds(),
                                                           movementInfo.getSpeed(), movementInfo.isCanFly(), movementInfo.getMovementTypes()))
                                         .contactDamage(t.getContactDamage().get())
                                         .build());
        });
    }

}
