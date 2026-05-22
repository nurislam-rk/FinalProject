package com.railshooter.collision;

/**
 * PATTERN: Chain of Responsibility — базовый обработчик коллизий.
 */
public abstract class CollisionHandler {
    protected CollisionHandler next;

    public CollisionHandler setNext(CollisionHandler handler) {
        this.next = handler;
        return handler;
    }

    public void handle(CollisionContext ctx) {
        doHandle(ctx);
        if (next != null) next.handle(ctx);
    }

    protected abstract void doHandle(CollisionContext ctx);
}
