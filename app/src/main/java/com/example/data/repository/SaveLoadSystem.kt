package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object SaveLoadSystem {
    private const val SAVE_FILE_NAME = "savegame.json"

    fun hasSave(context: Context): Boolean {
        val file = File(context.filesDir, SAVE_FILE_NAME)
        return file.exists() && file.length() > 0
    }

    fun deleteSave(context: Context): Boolean {
        val file = File(context.filesDir, SAVE_FILE_NAME)
        return if (file.exists()) file.delete() else false
    }

    suspend fun saveGame(context: Context, dao: FarmDao): Boolean {
        return try {
            val state = dao.getGameState().firstOrNull() ?: return false
            val plots = dao.getAllPlots().firstOrNull() ?: emptyList()
            val inventory = dao.getAllInventory().firstOrNull() ?: emptyList()
            val buildings = dao.getAllPlacedBuildings().firstOrNull() ?: emptyList()

            val json = JSONObject()
            json.put("version", 1)
            json.put("timestamp", System.currentTimeMillis() / 1000L)
            
            // World
            val hourInt = state.gameTimeHour.toInt()
            val minuteInt = ((state.gameTimeHour - hourInt) * 60).toInt()
            json.put("day", state.gameTimeDay)
            json.put("hour", hourInt)
            json.put("minute", minuteInt)
            json.put("weather", state.currentWeather.name.lowercase())
            json.put("weatherTimeLeft", state.weatherChangeCountdownHours.toInt())

            // Player position
            val playerObj = JSONObject()
            playerObj.put("x", state.playerX.toDouble())
            playerObj.put("y", state.playerY.toDouble())
            playerObj.put("z", state.playerZ.toDouble())
            playerObj.put("rotY", state.playerAngle.toDouble())
            json.put("player", playerObj)

            // Survival Stats
            val statsObj = JSONObject()
            statsObj.put("health", state.health.toDouble())
            statsObj.put("hunger", state.hunger.toDouble())
            statsObj.put("thirst", state.thirst.toDouble())
            statsObj.put("stamina", state.stamina.toDouble())
            json.put("stats", statsObj)

            // Economy
            val ecoObj = JSONObject()
            ecoObj.put("money", state.solCoins)
            ecoObj.put("energy", state.batteryChargeKwh.toDouble())
            ecoObj.put("greenPoints", state.ecoPrestige.toDouble())
            json.put("economy", ecoObj)

            // Crops (6 plots)
            val cropsArray = JSONArray()
            for (plot in plots) {
                val pObj = JSONObject()
                pObj.put("plotIndex", plot.id)
                pObj.put("type", plot.cropType?.name?.lowercase() ?: "empty")
                val stageVal = when (plot.stage) {
                    CropStage.SEEDLING -> 0
                    CropStage.SPROUT -> 1
                    CropStage.VEGETATIVE -> 2
                    CropStage.HARVEST_READY -> 3
                    else -> 0
                }
                pObj.put("stage", stageVal)
                pObj.put("watered", plot.moisture > 0.4f)
                pObj.put("daysGrowing", plot.progress.toInt())
                cropsArray.put(pObj)
            }
            json.put("crops", cropsArray)

            // Inventory
            val invObj = JSONObject()
            val cropsInv = JSONObject()
            val seedsInv = JSONObject()
            for (item in inventory) {
                val baseId = item.itemId
                if (baseId.startsWith("harvest_")) {
                    val cropName = baseId.removePrefix("harvest_")
                    cropsInv.put(cropName, item.quantity)
                } else if (baseId.startsWith("seed_")) {
                    val cropName = baseId.removePrefix("seed_")
                    seedsInv.put(cropName, item.quantity)
                }
            }
            invObj.put("crops", cropsInv)
            invObj.put("seeds", seedsInv)
            json.put("inventory", invObj)

            // Buildings
            val bArray = JSONArray()
            for (b in buildings) {
                val bObj = JSONObject()
                bObj.put("type", b.buildingType.name.lowercase())
                bObj.put("x", b.posX.toDouble())
                bObj.put("y", b.posY.toDouble())
                bObj.put("z", b.posZ.toDouble())
                bObj.put("rotY", b.rotationDeg.toDouble())
                bArray.put(bObj)
            }
            json.put("buildings", bArray)

            // Write to file
            val file = File(context.filesDir, SAVE_FILE_NAME)
            file.writeText(json.toString(2))
            Log.d("SaveLoadSystem", "Saved game state successfully to private storage")
            true
        } catch (e: Exception) {
            Log.e("SaveLoadSystem", "Save failed", e)
            false
        }
    }

    suspend fun loadGame(context: Context, dao: FarmDao): Boolean {
        return try {
            val file = File(context.filesDir, SAVE_FILE_NAME)
            if (!file.exists()) return false

            val jsonStr = file.readText()
            val json = JSONObject(jsonStr)

            // Parse version & timestamp
            val version = json.optInt("version", 1)

            // Clear database tables to ensure clean reload
            dao.deleteAllPlots()
            dao.deleteAllInventory()
            dao.deleteAllPlacedBuildings()

            // Economy & Stats
            val ecoObj = json.optJSONObject("economy")
            val money = ecoObj?.optInt("money", 450) ?: 450
            val energy = ecoObj?.optDouble("energy", 100.0)?.toFloat() ?: 100.0f
            val greenPoints = ecoObj?.optDouble("greenPoints", 0.0)?.toFloat() ?: 0.0f

            val statsObj = json.optJSONObject("stats")
            val health = statsObj?.optDouble("health", 100.0)?.toFloat() ?: 100.0f
            val hunger = statsObj?.optDouble("hunger", 100.0)?.toFloat() ?: 100.0f
            val thirst = statsObj?.optDouble("thirst", 100.0)?.toFloat() ?: 100.0f
            val stamina = statsObj?.optDouble("stamina", 100.0)?.toFloat() ?: 100.0f

            val playerObj = json.optJSONObject("player")
            val px = playerObj?.optDouble("x", 0.0)?.toFloat() ?: 0.0f
            val py = playerObj?.optDouble("y", 0.0)?.toFloat() ?: 0.0f
            val pz = playerObj?.optDouble("z", 0.0)?.toFloat() ?: 0.0f
            val rotY = playerObj?.optDouble("rotY", 0.0)?.toFloat() ?: 0.0f

            val weatherStr = json.optString("weather", "sunny_clear").uppercase()
            val weatherVal = try {
                WeatherType.valueOf(weatherStr)
            } catch (e: Exception) {
                WeatherType.SUNNY_CLEAR
            }
            val weatherTimeLeft = json.optInt("weatherTimeLeft", 6)

            val day = json.optInt("day", 1)
            val hour = json.optInt("hour", 6)
            val minute = json.optInt("minute", 0)
            val computedHour = hour.toFloat() + (minute.toFloat() / 60.0f)

            // Restore GameStateEntity
            val state = GameStateEntity(
                id = 1,
                gameTimeDay = day,
                gameTimeHour = computedHour,
                currentWeather = weatherVal,
                weatherChangeCountdownHours = weatherTimeLeft.toFloat(),
                solCoins = money,
                batteryChargeKwh = energy,
                ecoPrestige = greenPoints.toInt(),
                hunger = hunger,
                thirst = thirst,
                stamina = stamina,
                health = health,
                playerX = px,
                playerY = py,
                playerZ = pz,
                playerAngle = rotY
            )
            dao.saveGameState(state)

            // Crops
            val cropsArray = json.optJSONArray("crops")
            val plotsList = mutableListOf<PlotEntity>()
            if (cropsArray != null) {
                for (i in 0 until cropsArray.length()) {
                    val pObj = cropsArray.getJSONObject(i)
                    val pIdx = pObj.optInt("plotIndex", i)
                    val typeStr = pObj.optString("type", "empty").uppercase()
                    val cropType = if (typeStr == "EMPTY") null else {
                        try { CropType.valueOf(typeStr) } catch (e: Exception) { null }
                    }
                    val stageInt = pObj.optInt("stage", 0)
                    val watered = pObj.optBoolean("watered", false)
                    val daysGrowing = pObj.optInt("daysGrowing", 0)

                    val stageVal = when (stageInt) {
                        0 -> CropStage.SEEDLING
                        1 -> CropStage.SPROUT
                        2 -> CropStage.VEGETATIVE
                        3 -> CropStage.HARVEST_READY
                        else -> CropStage.SEEDLING
                    }

                    // Plot Position
                    val row = pIdx / 3
                    val col = pIdx % 3
                    val posX = -4.0f + (col - 1.0f) * 4.5f
                    val posZ = -6.0f + (row - 0.5f) * 4.5f

                    plotsList.add(
                        PlotEntity(
                            id = pIdx,
                            plotType = PlotType.PERMACULTURE_BED,
                            cropType = cropType,
                            stage = if (cropType == null) CropStage.EMPTY else stageVal,
                            progress = daysGrowing.toFloat(),
                            moisture = if (watered) 1.0f else 0.0f,
                            posX = posX,
                            posY = 0.0f,
                            posZ = posZ
                        )
                    )
                }
            }
            dao.insertPlots(plotsList)

            // Inventory
            val invList = mutableListOf<InventoryEntity>()
            val invObj = json.optJSONObject("inventory")
            if (invObj != null) {
                val cropsInv = invObj.optJSONObject("crops")
                if (cropsInv != null) {
                    val keys = cropsInv.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val qty = cropsInv.optInt(key, 0)
                        if (qty > 0) {
                            val cropTypeEnum = try { CropType.valueOf(key.uppercase()) } catch (e: Exception) { null }
                            val dispName = cropTypeEnum?.displayName ?: key.substring(0, 1).uppercase() + key.substring(1)
                            invList.add(
                                InventoryEntity(
                                    itemId = "harvest_${key.lowercase()}",
                                    name = dispName,
                                    category = ItemCategory.HARVEST,
                                    quantity = qty,
                                    sellValue = cropTypeEnum?.baseSellPrice ?: 20
                                )
                            )
                        }
                    }
                }
                val seedsInv = invObj.optJSONObject("seeds")
                if (seedsInv != null) {
                    val keys = seedsInv.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val qty = seedsInv.optInt(key, 0)
                        if (qty > 0) {
                            val cropTypeEnum = try { CropType.valueOf(key.uppercase()) } catch (e: Exception) { null }
                            val dispName = "${cropTypeEnum?.displayName ?: key.substring(0, 1).uppercase() + key.substring(1)} Seeds"
                            invList.add(
                                InventoryEntity(
                                    itemId = "seed_${key.lowercase()}",
                                    name = dispName,
                                    category = ItemCategory.SEEDS,
                                    quantity = qty,
                                    sellValue = (cropTypeEnum?.seedCost ?: 5) / 2
                                )
                            )
                        }
                    }
                }
            }
            dao.insertInventoryList(invList)

            // Buildings
            val bArray = json.optJSONArray("buildings")
            val bList = mutableListOf<PlacedBuildingEntity>()
            if (bArray != null) {
                for (i in 0 until bArray.length()) {
                    val bObj = bArray.getJSONObject(i)
                    val bTypeStr = bObj.optString("type", "solar_panel").uppercase()
                    val bType = try { BuildableType.valueOf(bTypeStr) } catch(e: Exception) { BuildableType.SOLAR_PANEL }
                    val bx = bObj.optDouble("x", 0.0).toFloat()
                    val by = bObj.optDouble("y", 0.0).toFloat()
                    val bz = bObj.optDouble("z", 0.0).toFloat()
                    val brotY = bObj.optDouble("rotY", 0.0).toFloat()

                    bList.add(
                        PlacedBuildingEntity(
                            id = i + 1,
                            buildingType = bType,
                            posX = bx,
                            posY = by,
                            posZ = bz,
                            rotationDeg = brotY,
                            level = 1
                        )
                    )
                }
            }
            dao.insertPlacedBuildings(bList)

            Log.d("SaveLoadSystem", "Loaded game state successfully from private storage")
            true
        } catch (e: Exception) {
            Log.e("SaveLoadSystem", "Load failed", e)
            false
        }
    }
}
