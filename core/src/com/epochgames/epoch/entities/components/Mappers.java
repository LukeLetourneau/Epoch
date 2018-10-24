package com.epochgames.epoch.entities.components;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

/**
 * Provides mappers for quick access to an {@link Entity}'s
 * components
 */
public class Mappers {
    public static final ComponentMapper<ActionCompletenessComponent> actionCompleteness = ComponentMapper.getFor(ActionCompletenessComponent.class);
    public static final ComponentMapper<ActionQueueComponent> actionQueue = ComponentMapper.getFor(ActionQueueComponent.class);
    public static final ComponentMapper<HealthComponent> health = ComponentMapper.getFor(HealthComponent.class);
    public static final ComponentMapper<IconComponent> icon = ComponentMapper.getFor(IconComponent.class);
    public static final ComponentMapper<MoveComponent> move = ComponentMapper.getFor(MoveComponent.class);
    public static final ComponentMapper<StorageComponent> storage = ComponentMapper.getFor(StorageComponent.class);
    public static final ComponentMapper<TransformComponent> transform = ComponentMapper.getFor(TransformComponent.class);
    public static final ComponentMapper<TurnComponent> turn = ComponentMapper.getFor(TurnComponent.class);
    public static final ComponentMapper<TypeComponent> type = ComponentMapper.getFor(TypeComponent.class);
}
