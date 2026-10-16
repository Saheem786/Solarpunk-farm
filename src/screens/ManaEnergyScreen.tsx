import React from 'react';
import { useGame } from '../context/GameContext';
import {
  Zap,
  BatteryCharging,
  Sun,
  Wind,
  Flame,
  ShieldAlert,
  Power,
  ChevronDown,
  Info,
} from 'lucide-react';
import {
  DEVICE_CATALOG,
  DeviceTypeId,
  POWER_PRIORITIES,
  PowerPriorityLevel,
} from '../types/game';

export const ManaEnergyScreen: React.FC = () => {
  const { energySummary, devicePriorities, setDevicePriority, energyNodes, placedBuildings } = useGame();

  const batteryPct = Math.round(energySummary.batteryPercent * 100);

  return (
    <div className="w-full h-full pb-20 pt-16 px-4 overflow-y-auto bg-[#1A0F0A] text-[#F5EBE0] font-medieval">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="border-b border-[#D4AF37]/30 pb-4">
          <div className="flex items-center gap-2">
            <Zap className="w-6 h-6 text-cyan-400" />
            <h1 className="text-2xl font-bold text-[#D4AF37] tracking-wider font-cinzel">
              ANCIENT MANA & MYSTIC CIRCLES
            </h1>
          </div>
          <p className="text-xs text-stone-400 font-almendra mt-1">
            Renewable arcane energy gathering, runic storage vessels, and selective device load management
          </p>
        </div>

        {/* Battery & Grid Overview Banner */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Main Vault Charge */}
          <div className="p-4 rounded-2xl bg-[#2A160F] border border-cyan-500/40 relative overflow-hidden">
            <div className="flex justify-between items-center mb-2">
              <span className="text-xs text-cyan-300 font-bold flex items-center gap-1.5">
                <BatteryCharging className="w-4 h-4 text-cyan-400" /> Mana Storage
              </span>
              <span className="text-lg font-bold font-cinzel text-cyan-300">{batteryPct}%</span>
            </div>
            <div className="w-full h-3 rounded-full bg-black/60 overflow-hidden border border-cyan-900/80 mb-2">
              <div
                className="h-full bg-gradient-to-r from-cyan-600 to-sky-300 transition-all duration-300"
                style={{ width: `${Math.min(100, batteryPct)}%` }}
              />
            </div>
            <div className="flex justify-between text-[11px] text-stone-300">
              <span>{Math.round(energySummary.currentStoredKwh)} kWh</span>
              <span>Capacity: {Math.round(energySummary.totalStorageCapacityKwh)} kWh</span>
            </div>
          </div>

          {/* Current Generation Rate */}
          <div className="p-4 rounded-2xl bg-[#2A160F] border border-amber-500/40">
            <span className="text-xs text-amber-300 font-bold flex items-center gap-1.5 mb-2">
              <Sun className="w-4 h-4 text-amber-400" /> Current Generation
            </span>
            <div className="text-2xl font-bold font-cinzel text-amber-100">
              +{energySummary.currentGenerationKwhPerHour.toFixed(1)} <span className="text-sm font-normal text-stone-400">kWh/h</span>
            </div>
            <div className="text-[11px] text-stone-400 mt-2">
              Daily Output: ~{Math.round(energySummary.totalGenerationKwhPerDay)} kWh/day
            </div>
          </div>

          {/* Net Flow & Consumption */}
          <div className="p-4 rounded-2xl bg-[#2A160F] border border-stone-700">
            <span className="text-xs text-stone-300 font-bold flex items-center gap-1.5 mb-2">
              <Power className="w-4 h-4 text-rose-400" /> Net Grid Rate
            </span>
            <div
              className={`text-2xl font-bold font-cinzel ${
                energySummary.isCharging ? 'text-emerald-400' : 'text-rose-400'
              }`}
            >
              {energySummary.netHourlyKwh >= 0 ? '+' : ''}
              {energySummary.netHourlyKwh.toFixed(1)}{' '}
              <span className="text-sm font-normal text-stone-400">kWh/h</span>
            </div>
            <div className="text-[11px] text-stone-400 mt-2">
              {energySummary.isCharging
                ? 'Surplus mana charging vessels'
                : `Discharging (~${Math.round(energySummary.hoursUntilEmpty)}h remaining)`}
            </div>
          </div>
        </div>

        {/* Device Power Priority Management */}
        <div className="bg-[#2A160F]/90 rounded-2xl border border-amber-900/40 p-5 space-y-4">
          <div className="flex items-center justify-between border-b border-amber-900/30 pb-3">
            <div>
              <h2 className="text-lg font-bold text-amber-100 font-cinzel">Device Power Priorities</h2>
              <p className="text-xs text-stone-400 font-almendra">
                During low battery reserve, lower tier devices shed load to protect critical kingdom systems.
              </p>
            </div>
            <span className="text-xs font-mono px-2.5 py-1 rounded-lg bg-black/40 text-cyan-300 border border-cyan-800">
              {energySummary.activeDevicesCount}/{energySummary.totalDevicesCount} Online
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {Object.values(DEVICE_CATALOG).map((dev) => {
              const priority = devicePriorities[dev.id] || dev.defaultPriority;
              const threshold = POWER_PRIORITIES[priority].thresholdPercent;
              const isPowered = energySummary.batteryPercent >= threshold && energySummary.currentStoredKwh > 0.1;

              return (
                <div
                  key={dev.id}
                  className={`p-3.5 rounded-xl border flex items-center justify-between gap-3 ${
                    isPowered
                      ? 'bg-[#1A0F0A] border-amber-900/40'
                      : 'bg-black/40 border-stone-800 opacity-60'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-3 h-3 rounded-full shrink-0 ${
                        isPowered ? 'bg-emerald-400 shadow-[0_0_8px_rgba(0,230,118,0.6)]' : 'bg-rose-500'
                      }`}
                    />
                    <div>
                      <h4 className="text-sm font-bold text-amber-100">{dev.displayName}</h4>
                      <div className="text-[10px] text-stone-400">
                        {dev.category} • {dev.consumptionKwhPerHour} kWh/h
                      </div>
                    </div>
                  </div>

                  <select
                    value={priority}
                    onChange={(e) => setDevicePriority(dev.id, e.target.value as PowerPriorityLevel)}
                    className="bg-[#2A160F] text-amber-200 border border-amber-900/60 rounded-lg text-xs px-2 py-1 outline-none cursor-pointer"
                  >
                    {Object.keys(POWER_PRIORITIES).map((lvl) => (
                      <option key={lvl} value={lvl}>
                        {POWER_PRIORITIES[lvl as PowerPriorityLevel].displayName} (&gt;
                        {Math.round(POWER_PRIORITIES[lvl as PowerPriorityLevel].thresholdPercent * 100)}%)
                      </option>
                    ))}
                  </select>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
