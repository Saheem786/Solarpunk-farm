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

    @Query("DELETE FROM placed_buildings WHERE id = :id")
    suspend fun deletePlacedBuilding(id: Int)
}
