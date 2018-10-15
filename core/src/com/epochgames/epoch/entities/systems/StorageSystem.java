package com.epochgames.epoch.entities.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.epochgames.epoch.Epoch;
import com.epochgames.epoch.entities.components.Mappers;
import com.epochgames.epoch.entities.components.MoveComponent;
import com.epochgames.epoch.entities.components.TransformComponent;
import com.epochgames.epoch.entities.components.TypeComponent;
import com.epochgames.epoch.util.HexagonGrid;
import com.epochgames.epoch.util.hexlib.HexSatelliteData;
import org.codetome.hexameter.core.api.CubeCoordinate;
import org.codetome.hexameter.core.api.Hexagon;

public class StorageSystem extends IteratingSystem {

    private HexagonGrid hexagonGrid;
    private Epoch game;
    private ComponentMapper<TransformComponent> transform = Mappers.transform;

    public StorageSystem(Epoch game, HexagonGrid hexagonGrid) {
        super(Family.all(MoveComponent.class).get());
        this.hexagonGrid = hexagonGrid;
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CubeCoordinate entityPos = hexagonGrid.hexGrid.getByPixelCoordinate(transform.get(entity).position.x, transform.get(entity).position.y).get().getCubeCoordinate();
        Hexagon<HexSatelliteData> hexagon = hexagonGrid.hexGrid.getByCubeCoordinate(entityPos).get();
        hexagon.setSatelliteData(new HexSatelliteData(entity, entityPos));
        if(Mappers.type.get(entity) != null && Mappers.type.get(entity).type == TypeComponent.PLAYER) {
            game.inGameScreen.playerPos = entityPos;
        }
    }
}
