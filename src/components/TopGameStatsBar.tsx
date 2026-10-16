import React from 'react';
import { useGame } from '../context/GameContext';
import {
  Sun,
  Moon,
  CloudRain,
  Zap,
  Coins,
  Shield,
  Heart,
  Utensils,
  Droplet,
  Compass,
  Settings,
  HelpCircle,
  FastForward,
  Trophy,
} from 'lucide-react';
import { WEATHER_CATALOG } from '../types/game';

interface TopGameStatsBarProps {
  onOpenMap: () => void;
  onOpenSettings: () => void;
  onOpenTutorial: () => void;
  onOpenAchievements: () => void;
}

export const TopGameStatsBar: React.FC<TopGameStatsBarProps> = ({
  onOpenMap,
  onOpenSettings,
  onOpenTutorial,
  onOpenAchievements,
}) => {
  const { gameState, energySummary, advanceTime } = useGame();

  const hourInt = Math.floor(gameState.gameTimeHour);
  const minInt = Math.floor((gameState.gameTimeHour % 1) * 60);
  const timeString = `${hourInt.toString().padStart(2, '0')}:${minInt.toString().padStart(2, '0')}`;
  const isDay = gameState.gameTimeHour >= 6.0 && gameState.gameTimeHour <= 19.0;
  const weather = WEATHER_CATALOG[gameState.currentWeather];

  return (
    <header className="hud-overlay absolute top-0 left-0 right-0 z-20 px-3 py-2 bg-gradient-to-b from-[#1A0F0A]/95 via-[#1A0F0A]/80 to-transparent flex flex-wrap items-center justify-between gap-2 border-b border-[#D4AF37]/30 text-xs select-none">
      {/* Left: Time & Weather */}
      <div className="flex items-center gap-2">
        <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#2A160F] border border-[#D4AF37]/50 shadow-inner">
          {isDay ? (
            <Sun className="w-4 h-4 text-amber-400 animate-spin-slow" />
          ) : (
            <Moon className="w-4 h-4 text-indigo-300" />
          )}
          <span className="font-bold text-amber-100 font-cinzel text-sm">{timeString}</span>
          <span className="text-[10px] text-amber-300/70 ml-1">Day {gameState.gameTimeDay}</span>
          <button
            onClick={() => advanceTime(2)}
            title="Advance 2 Hours"
            className="ml-1 p-0.5 hover:bg-amber-900/40 rounded text-amber-300 hover:text-amber-100 cursor-pointer"
          >
            <FastForward className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Weather Pill */}
        <div
          title={weather.description}
          className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl bg-[#2A160F]/80 border border-amber-900/60 text-amber-200"
        >
          <CloudRain className="w-3.5 h-3.5 text-sky-400" />
          <span className="font-medieval hidden sm:inline">{weather.displayName}</span>
        </div>
      </div>

      {/* Center: Vitals (Health, Hunger, Thirst, Stamina) */}
      <div className="flex items-center gap-2 bg-[#2A160F]/80 px-3 py-1 rounded-xl border border-amber-900/40">
        {/* Health */}
        <div className="flex items-center gap-1" title={`Health: ${Math.round(gameState.health)}%`}>
          <Heart className="w-3.5 h-3.5 text-rose-500 fill-rose-500" />
          <div className="w-12 h-2 rounded-full bg-black/60 overflow-hidden border border-rose-950">
            <div
              className="h-full bg-gradient-to-r from-red-600 to-rose-400 transition-all duration-300"
              style={{ width: `${Math.max(0, Math.min(100, gameState.health))}%` }}
            />
          </div>
        </div>

        {/* Hunger */}
        <div className="flex items-center gap-1" title={`Hunger: ${Math.round(gameState.hunger)}%`}>
          <Utensils className="w-3.5 h-3.5 text-amber-500" />
          <div className="w-12 h-2 rounded-full bg-black/60 overflow-hidden border border-amber-950">
            <div
              className="h-full bg-gradient-to-r from-amber-600 to-yellow-400 transition-all duration-300"
              style={{ width: `${Math.max(0, Math.min(100, gameState.hunger))}%` }}
            />
          </div>
        </div>

        {/* Thirst */}
        <div className="flex items-center gap-1" title={`Thirst: ${Math.round(gameState.thirst)}%`}>
          <Droplet className="w-3.5 h-3.5 text-cyan-400 fill-cyan-400" />
          <div className="w-12 h-2 rounded-full bg-black/60 overflow-hidden border border-cyan-950">
            <div
              className="h-full bg-gradient-to-r from-cyan-600 to-sky-300 transition-all duration-300"
              style={{ width: `${Math.max(0, Math.min(100, gameState.thirst))}%` }}
            />
          </div>
        </div>

        {/* Water carried */}
        <div className="text-[11px] font-mono text-cyan-200 ml-1" title="Sacred Water Carried">
          💧 {gameState.cleanWaterCarried}/{gameState.maxWaterCarried}
        </div>
      </div>

      {/* Right: Currency & Arcane Mana & Fast Actions */}
      <div className="flex items-center gap-2">
        {/* Sol Coins */}
        <div className="flex items-center gap-1 px-2.5 py-1 rounded-xl bg-[#2A160F] border border-[#D4AF37]/50 text-amber-300">
          <Coins className="w-3.5 h-3.5 text-[#D4AF37]" />
          <span className="font-bold font-cinzel text-xs">{gameState.solCoins}</span>
        </div>

        {/* Eco Prestige */}
        <div className="flex items-center gap-1 px-2.5 py-1 rounded-xl bg-[#2A160F] border border-emerald-500/40 text-emerald-300">
          <Shield className="w-3.5 h-3.5 text-emerald-400" />
          <span className="font-bold text-xs">{gameState.ecoPrestige}</span>
        </div>

        {/* Mana Battery */}
        <div
          title={`Mana: ${Math.round(gameState.batteryChargeKwh)} / ${Math.round(energySummary.totalStorageCapacityKwh)} kWh (${energySummary.isCharging ? '+charging' : '-discharging'})`}
          className="flex items-center gap-1 px-2.5 py-1 rounded-xl bg-[#2A160F] border border-cyan-500/40 text-cyan-300"
        >
          <Zap className="w-3.5 h-3.5 text-cyan-400" />
          <span className="font-bold text-xs">{Math.round(gameState.batteryChargeKwh)} kWh</span>
        </div>

        {/* Action icons */}
        <div className="flex items-center gap-1 ml-1">
          <button
            onClick={onOpenAchievements}
            title="Achievements"
            className="p-1.5 rounded-lg bg-[#2A160F] border border-[#D4AF37]/30 text-amber-300 hover:text-amber-100 hover:border-[#D4AF37] cursor-pointer"
          >
            <Trophy className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={onOpenMap}
            title="World Map"
            className="p-1.5 rounded-lg bg-[#2A160F] border border-[#D4AF37]/30 text-amber-300 hover:text-amber-100 hover:border-[#D4AF37] cursor-pointer"
          >
            <Compass className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={onOpenTutorial}
            title="Tutorial"
            className="p-1.5 rounded-lg bg-[#2A160F] border border-[#D4AF37]/30 text-amber-300 hover:text-amber-100 hover:border-[#D4AF37] cursor-pointer"
          >
            <HelpCircle className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={onOpenSettings}
            title="Settings"
            className="p-1.5 rounded-lg bg-[#2A160F] border border-[#D4AF37]/30 text-amber-300 hover:text-amber-100 hover:border-[#D4AF37] cursor-pointer"
          >
            <Settings className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </header>
  );
};
