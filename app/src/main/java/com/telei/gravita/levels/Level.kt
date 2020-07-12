package com.telei.gravita.levels

import com.telei.gravita.game.*

data class Level(
    val aim: Aim,
    val point: Point,
    val attractors: List<Attractor> = emptyList(),
    val portals: List<Portal> = emptyList(),
    val chords: List<Chord> = emptyList()
) {
    fun clone(): Level =
        Level(
            aim = aim.copy(),
            point = point.copy(),
            attractors = attractors.map { it.copy().also { attractor -> attractor.f = it.f } },
            portals = portals.map { it.copy() },
            chords = chords.map { it.copy() }
        )
}