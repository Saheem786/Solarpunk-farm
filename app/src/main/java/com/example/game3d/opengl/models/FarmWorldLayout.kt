package com.example.game3d.opengl.models

data class StaticBuildingDef(
    val id: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val width: Float,
    val depth: Float,
    val height: Float,
    val isCircular: Boolean = false,
    val radius: Float = 0f
)

data class TreeDef(
    val x: Float,
    val z: Float,
    val type: Int
)

object FarmWorldLayout {
    val staticBuildings = listOf(
        StaticBuildingDef("farmhouse", 6.0f, 0.0f, 0.0f, 7.0f, 4.8f, 3.5f),
        StaticBuildingDef("barn", -12.0f, 0.0f, 10.0f, 7.2f, 5.6f, 3.8f),
        StaticBuildingDef("workshop", 0.0f, 0.0f, 14.0f, 5.8f, 4.6f, 3.5f),
        StaticBuildingDef("market", -14.0f, 0.0f, -14.0f, 0f, 0f, 2.5f, true, 1.8f),
        StaticBuildingDef("well", 8.0f, 0.0f, -3.5f, 0f, 0f, 2.0f, true, 1.0f)
    )

    val treePositions = listOf(
        // === Green Valley Trees ===
        TreeDef(-18.0f, -12.0f, 0), TreeDef(-22.0f, -6.0f, 1), TreeDef(-16.0f, -2.0f, 2),
        TreeDef(-22.0f, 4.0f, 0), TreeDef(-18.0f, 12.0f, 1), TreeDef(-22.0f, 18.0f, 0),
        TreeDef(-14.0f, 22.0f, 1), TreeDef(-6.0f, 22.0f, 0), TreeDef(2.0f, 22.0f, 2),
        TreeDef(10.0f, 22.0f, 1), TreeDef(18.0f, 22.0f, 0), TreeDef(10.0f, 6.0f, 2),
        TreeDef(12.0f, 14.0f, 1), TreeDef(-6.0f, -16.0f, 2), TreeDef(4.0f, -18.0f, 0),
        TreeDef(-2.0f, -22.0f, 1), TreeDef(12.0f, -14.5f, 3), TreeDef(6.0f, -11.0f, 3),
        TreeDef(16.0f, -4.0f, 3), TreeDef(18.0f, 4.0f, 3), TreeDef(18.0f, -18.0f, 0),

        // === Deep Forest Trees ===
        TreeDef(-35.0f, 50.0f, 5), TreeDef(-15.0f, 48.0f, 4), TreeDef(10.0f, 52.0f, 5), TreeDef(32.0f, 55.0f, 4),
        TreeDef(-45.0f, 70.0f, 4), TreeDef(-25.0f, 68.0f, 5), TreeDef(-8.0f, 72.0f, 4), TreeDef(15.0f, 70.0f, 5),
        TreeDef(38.0f, 75.0f, 4), TreeDef(55.0f, 80.0f, 5), TreeDef(-50.0f, 95.0f, 5), TreeDef(-28.0f, 98.0f, 4),
        TreeDef(-2.0f, 92.0f, 5), TreeDef(25.0f, 96.0f, 4), TreeDef(48.0f, 102.0f, 5), TreeDef(-42.0f, 118.0f, 4),
        TreeDef(-18.0f, 122.0f, 5), TreeDef(2.0f, 115.0f, 4), TreeDef(28.0f, 120.0f, 5), TreeDef(50.0f, 125.0f, 4),
        TreeDef(-55.0f, 140.0f, 5), TreeDef(-32.0f, 145.0f, 4), TreeDef(-5.0f, 138.0f, 5), TreeDef(20.0f, 142.0f, 4),
        TreeDef(42.0f, 148.0f, 5), TreeDef(-48.0f, 165.0f, 5), TreeDef(-22.0f, 168.0f, 4), TreeDef(8.0f, 162.0f, 5),
        TreeDef(32.0f, 166.0f, 4), TreeDef(58.0f, 172.0f, 5), TreeDef(-35.0f, 185.0f, 5), TreeDef(-10.0f, 188.0f, 4),
        TreeDef(15.0f, 182.0f, 5), TreeDef(40.0f, 186.0f, 4), TreeDef(-2.0f, 175.0f, 4),

        // === Wetland Trees ===
        TreeDef(-30.0f, -55.0f, 3), TreeDef(-10.0f, -50.0f, 3), TreeDef(15.0f, -52.0f, 3), TreeDef(35.0f, -58.0f, 3),
        TreeDef(-45.0f, -75.0f, 3), TreeDef(-18.0f, -70.0f, 1), TreeDef(8.0f, -72.0f, 3), TreeDef(28.0f, -78.0f, 3),
        TreeDef(-38.0f, -92.0f, 3), TreeDef(-12.0f, -88.0f, 3), TreeDef(18.0f, -90.0f, 3), TreeDef(42.0f, -94.0f, 3),
        TreeDef(-48.0f, -115.0f, 3), TreeDef(-20.0f, -112.0f, 1), TreeDef(10.0f, -118.0f, 3), TreeDef(32.0f, -122.0f, 3),
        TreeDef(-35.0f, -140.0f, 3), TreeDef(-8.0f, -145.0f, 3), TreeDef(22.0f, -138.0f, 3), TreeDef(45.0f, -142.0f, 3),
        TreeDef(-25.0f, -165.0f, 3), TreeDef(5.0f, -168.0f, 3), TreeDef(30.0f, -162.0f, 3)
    )
}
