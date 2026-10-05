package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    // Game State
    @Query("SELECT * FROM game_state WHERE id = 1 LIMIT 1")
    fun getGameState(): Flow<GameStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(state: GameStateEntity)

    // Farm Plots
    @Query("SELECT * FROM farm_plots ORDER BY id ASC")
    fun getAllPlots(): Flow<List<PlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlots(plots: List<PlotEntity>)

    @Update
    suspend fun updatePlot(plot: PlotEntity)

    @Query("UPDATE farm_plots SET moisture = :moisture WHERE id = :plotId")
    suspend fun updatePlotMoisture(plotId: Int, moisture: Float)

    // Energy Nodes
    @Query("SELECT * FROM energy_nodes ORDER BY id ASC")
    fun getAllEnergyNodes(): Flow<List<EnergyNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnergyNodes(nodes: List<EnergyNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnergyNode(node: EnergyNodeEntity)

    @Update
    suspend fun updateEnergyNode(node: EnergyNodeEntity)

    @Query("DELETE FROM energy_nodes WHERE id = :id")
    suspend fun deleteEnergyNode(id: Int)

    // Livestock
    @Query("SELECT * FROM livestock ORDER BY id ASC")
    fun getAllLivestock(): Flow<List<LivestockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLivestockList(animals: List<LivestockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLivestock(animal: LivestockEntity)

    @Update
    suspend fun updateLivestock(animal: LivestockEntity)

    @Query("DELETE FROM livestock WHERE id = :id")
    suspend fun deleteLivestock(id: Int)

    // Inventory
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE itemId = :itemId LIMIT 1")
    suspend fun getInventoryItem(itemId: String): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: InventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryList(items: List<InventoryEntity>)

    @Query("DELETE FROM inventory WHERE itemId = :itemId")
    suspend fun deleteInventory(itemId: String)

    // Contracts
    @Query("SELECT * FROM contracts ORDER BY isClaimed ASC")
    fun getAllContracts(): Flow<List<ContractEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContracts(contracts: List<ContractEntity>)

    @Update
    suspend fun updateContract(contract: ContractEntity)

    // Placed Buildings
    @Query("SELECT * FROM placed_buildings ORDER BY id ASC")
    fun getAllPlacedBuildings(): Flow<List<PlacedBuildingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacedBuilding(building: PlacedBuildingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacedBuildings(buildings: List<PlacedBuildingEntity>)

    @Update
    suspend fun updatePlacedBuilding(building: PlacedBuildingEntity)

    @Query("DELETE FROM placed_buildings WHERE id = :id")
    suspend fun deletePlacedBuilding(id: Int)

    // NPCs
    @Query("SELECT * FROM npcs ORDER BY id ASC")
    fun getAllNpcs(): Flow<List<NpcEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNpc(npc: NpcEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNpcs(npcs: List<NpcEntity>)

    @Update
    suspend fun updateNpc(npc: NpcEntity)

    @Query("DELETE FROM npcs WHERE id = :id")
    suspend fun deleteNpc(id: Int)

    @Query("DELETE FROM npcs")
    suspend fun deleteAllNpcs()

    // NPC Arrivals
    @Query("SELECT * FROM npc_arrivals ORDER BY id ASC")
    fun getAllArrivals(): Flow<List<NpcArrivalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArrival(arrival: NpcArrivalEntity): Long

    @Update
    suspend fun updateArrival(arrival: NpcArrivalEntity)

    @Query("DELETE FROM npc_arrivals")
    suspend fun deleteAllArrivals()

    // Research Techs
    @Query("SELECT * FROM research_techs ORDER BY tier ASC, id ASC")
    fun getAllResearchTechs(): Flow<List<ResearchTechEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResearchTechs(techs: List<ResearchTechEntity>)

    @Update
    suspend fun updateResearchTech(tech: ResearchTechEntity)

    // Story Missions
    @Query("SELECT * FROM story_missions ORDER BY missionNumber ASC")
    fun getAllStoryMissions(): Flow<List<StoryMissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryMissions(missions: List<StoryMissionEntity>)

    @Update
    suspend fun updateStoryMission(mission: StoryMissionEntity)

    // Points of Interest
    @Query("SELECT * FROM points_of_interest ORDER BY name ASC")
    fun getAllPois(): Flow<List<PoiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPois(pois: List<PoiEntity>)

    @Update
    suspend fun updatePoi(poi: PoiEntity)

    // Lore Entries
    @Query("SELECT * FROM lore_entries ORDER BY id ASC")
    fun getAllLoreEntries(): Flow<List<LoreEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoreEntries(entries: List<LoreEntryEntity>)

    @Update
    suspend fun updateLoreEntry(entry: LoreEntryEntity)

    // Terminal Logs
    @Query("SELECT * FROM terminal_logs ORDER BY id ASC")
    fun getAllTerminalLogs(): Flow<List<TerminalLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerminalLogs(logs: List<TerminalLogEntity>)

    @Update
    suspend fun updateTerminalLog(log: TerminalLogEntity)

    // Clear Tables for Loading Overwrite
    @Query("DELETE FROM farm_plots")
    suspend fun deleteAllPlots()

    @Query("DELETE FROM inventory")
    suspend fun deleteAllInventory()

    @Query("DELETE FROM placed_buildings")
    suspend fun deleteAllPlacedBuildings()

    @Query("DELETE FROM energy_nodes")
    suspend fun deleteAllEnergyNodes()

    @Query("DELETE FROM livestock")
    suspend fun deleteAllLivestock()
}
