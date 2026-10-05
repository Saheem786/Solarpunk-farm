package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.BuildableType
import com.example.data.model.CropStage
import com.example.data.model.CropType
import com.example.data.model.EnergyNodeType
import com.example.data.model.ItemCategory
import com.example.data.model.LivestockType
import com.example.data.model.PlotType
import com.example.data.model.WeatherType

class Converters {
    @TypeConverter
    fun fromCropType(value: CropType?): String? = value?.name

    @TypeConverter
    fun toCropType(value: String?): CropType? = value?.let { enumValueOf<CropType>(it) }

    @TypeConverter
    fun fromCropStage(value: CropStage): String = value.name

    @TypeConverter
    fun toCropStage(value: String): CropStage = enumValueOf(value)

    @TypeConverter
    fun fromPlotType(value: PlotType): String = value.name

    @TypeConverter
    fun toPlotType(value: String): PlotType = enumValueOf(value)

    @TypeConverter
    fun fromEnergyNodeType(value: EnergyNodeType): String = value.name

    @TypeConverter
    fun toEnergyNodeType(value: String): EnergyNodeType = enumValueOf(value)

    @TypeConverter
    fun fromLivestockType(value: LivestockType): String = value.name

    @TypeConverter
    fun toLivestockType(value: String): LivestockType = enumValueOf(value)

    @TypeConverter
    fun fromWeatherType(value: WeatherType): String = value.name

    @TypeConverter
    fun toWeatherType(value: String): WeatherType = enumValueOf(value)

    @TypeConverter
    fun fromItemCategory(value: ItemCategory): String = value.name

    @TypeConverter
    fun toItemCategory(value: String): ItemCategory = enumValueOf(value)

    @TypeConverter
    fun fromBuildableType(value: BuildableType): String = value.name

    @TypeConverter
    fun toBuildableType(value: String): BuildableType = enumValueOf(value)

    @TypeConverter
    fun fromNpcRole(value: com.example.data.model.NpcRole): String = value.name

    @TypeConverter
    fun toNpcRole(value: String): com.example.data.model.NpcRole = enumValueOf(value)

    @TypeConverter
    fun fromNpcPersonalityTrait(value: com.example.data.model.NpcPersonalityTrait): String = value.name

    @TypeConverter
    fun toNpcPersonalityTrait(value: String): com.example.data.model.NpcPersonalityTrait = enumValueOf(value)

    @TypeConverter
    fun fromNpcActivity(value: com.example.data.model.NpcActivity): String = value.name

    @TypeConverter
    fun toNpcActivity(value: String): com.example.data.model.NpcActivity = enumValueOf(value)
}
