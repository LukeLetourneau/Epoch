package com.ender.games.epoch.ship

import com.badlogic.ashley.core.Entity
import com.ender.games.epoch.Ships
import com.ender.games.epoch.entities.Player
import com.ender.games.epoch.ship.weapons.InvalidMunitionType
import com.ender.games.epoch.ship.weapons.Munition
import com.ender.games.epoch.ship.weapons.Weapon
import com.ender.games.epoch.util.InvalidAffixationException

class Ship(val baseStats: Ships, val entity: Entity) {
    val ar
        get() = baseStats.atlasRegion
    val name
        get() = baseStats.shipName
    val company
        get() = baseStats.company
    val type
        get() = baseStats.type

    var health= baseStats.health
    var armor = object: Armor(0f, 0, this) {}
    var cargoSpace = baseStats.cargoSpace
    var speed = baseStats.speed

    private val hardpoints = 2
    private val hardpointArray = Array(hardpoints) { Hardpoint(3, null) }

    private val maxAffixLvl = 3

    fun affixArmor(armorPiece: Armor) {
        if(armorPiece.level <= maxAffixLvl) {
            armor = armorPiece
        } else {
            throw InvalidAffixationException("Armor level too high")
        }
    }

    fun affixWeapon(weapon: Weapon, slot: Int) {
        if(weapon.level <= hardpointArray[slot].level) {
            if (hardpointArray[slot].weapon == null) {
                hardpointArray[slot].weapon = weapon
            }
        } else {
            throw InvalidAffixationException("Weapon level too high")
        }
    }

    fun fire() {
        for(weapon in hardpointArray.mapNotNull { it.weapon }) {
            weapon.fireIfAble()
        }
    }
}