import React, { useState } from 'react';
import { useGame } from '../context/GameContext';
import { CROP_CATALOG, CropTypeId, PlotEntity } from '../types/game';
import {
  Droplets,
  Sprout,
  Sparkles,
  Scissors,
  Plus,
  Info,
  Clock,
  Sun,
  Coins,
} from 'lucide-react';

export const AgricultureScreen: React.FC = () => {
  const { plots, waterPlot, waterAllPlots, fertilizePlot, harvestPlot, setSelectedPlotForModal, openModal } = useGame();
  const [selectedTab, setSelectedTab] = useState<'plots' | 'catalog'>('plots');

  return (
    <div className="w-full h-full pb-20 pt-16 px-4 overflow-y-auto bg-[#1A0F0A] text-[#F5EBE0] font-medieval">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-[#D4AF37]/30 pb-4">
          <div>
            <h1 className="text-2xl font-bold text-[#D4AF37] tracking-wider font-cinzel">
              SACRED GROVES & ROYAL GARDENS
            </h1>
            <p className="text-xs text-stone-400 font-almendra">
              Regenerative earth management, soil hydration, and ancient growth rituals
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setSelectedTab('plots')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                selectedTab === 'plots'
                  ? 'bg-[#800020] text-amber-100 border border-[#D4AF37]'
                  : 'bg-[#2A160F] text-stone-400 hover:text-amber-200'
              }`}
            >
              Garden Plots ({plots.length})
            </button>
            <button
              onClick={() => setSelectedTab('catalog')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                selectedTab === 'catalog'
                  ? 'bg-[#800020] text-amber-100 border border-[#D4AF37]'
                  : 'bg-[#2A160F] text-stone-400 hover:text-amber-200'
              }`}
            >
              Crop Codex
            </button>
            <button
              onClick={waterAllPlots}
              className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-cyan-800 hover:bg-cyan-700 text-cyan-100 text-xs font-bold border border-cyan-400 cursor-pointer shadow-md"
            >
              <Droplets className="w-3.5 h-3.5" />
              Hydrate All
            </button>
          </div>
        </div>

        {selectedTab === 'plots' ? (
          /* Plots Grid */
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {plots.map((plot) => {
              const crop = plot.cropType ? CROP_CATALOG[plot.cropType] : null;
              const isHarvestReady = plot.stage === 'HARVEST_READY';

              return (
                <div
                  key={plot.id}
                  className={`p-4 rounded-2xl bg-[#2A160F]/90 border transition-all ${
                    isHarvestReady
                      ? 'border-[#D4AF37] shadow-[0_0_15px_rgba(212,175,55,0.3)] bg-gradient-to-b from-[#2A160F] to-[#3B2014]'
                      : 'border-amber-900/40'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-xs text-amber-400/80 font-mono">Plot #{plot.id + 1}</span>
                      <h3 className="text-lg font-bold text-amber-100 font-cinzel">
                        {crop ? crop.displayName : 'Empty Soil Bed'}
                      </h3>
                    </div>
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold tracking-wider ${
                        isHarvestReady
                          ? 'bg-[#00E676] text-slate-950 animate-pulse'
                          : plot.stage === 'WITHERED'
                          ? 'bg-rose-900 text-rose-200'
                          : 'bg-[#8D6E63]/40 text-amber-200'
                      }`}
                    >
                      {plot.stage}
                    </span>
                  </div>

                  {/* Progress & Moisture */}
                  <div className="mt-4 space-y-2">
                    <div>
                      <div className="flex justify-between text-[11px] text-stone-300 mb-1">
                        <span>Growth Progress</span>
                        <span>{Math.round(plot.progress * 100)}%</span>
                      </div>
                      <div className="w-full h-2 rounded-full bg-black/60 overflow-hidden border border-stone-800">
                        <div
                          className="h-full bg-gradient-to-r from-amber-600 to-emerald-400 transition-all duration-300"
                          style={{ width: `${Math.min(100, plot.progress * 100)}%` }}
                        />
                      </div>
                    </div>

                    <div>
                      <div className="flex justify-between text-[11px] text-stone-300 mb-1">
                        <span className="flex items-center gap-1">
                          <Droplets className="w-3 h-3 text-cyan-400" /> Moisture
                        </span>
                        <span>{Math.round(plot.moisture * 100)}%</span>
                      </div>
                      <div className="w-full h-2 rounded-full bg-black/60 overflow-hidden border border-stone-800">
                        <div
                          className="h-full bg-cyan-500 transition-all duration-300"
                          style={{ width: `${Math.min(100, plot.moisture * 100)}%` }}
                        />
                      </div>
                    </div>
                  </div>

                  {/* Plot Actions */}
                  <div className="mt-4 pt-3 border-t border-amber-900/30 flex items-center justify-between gap-2">
                    {plot.cropType === null ? (
                      <button
                        onClick={() => {
                          setSelectedPlotForModal(plot);
                          openModal('plant_seed');
                        }}
                        className="flex-1 py-2 rounded-xl bg-[#800020] hover:bg-[#A31535] text-amber-100 text-xs font-bold border border-[#D4AF37]/50 flex items-center justify-center gap-1.5 cursor-pointer"
                      >
                        <Plus className="w-3.5 h-3.5" /> Sow Seed
                      </button>
                    ) : isHarvestReady ? (
                      <button
                        onClick={() => harvestPlot(plot.id)}
                        className="flex-1 py-2 rounded-xl bg-gradient-to-r from-[#D4AF37] to-[#FFA000] hover:from-amber-400 hover:to-amber-500 text-slate-950 text-xs font-bold shadow-lg flex items-center justify-center gap-1.5 cursor-pointer"
                      >
                        <Scissors className="w-3.5 h-3.5" /> Harvest Crop
                      </button>
                    ) : (
                      <>
                        <button
                          onClick={() => waterPlot(plot.id)}
                          className="flex-1 py-2 rounded-xl bg-cyan-900/60 hover:bg-cyan-800 text-cyan-200 text-xs font-bold border border-cyan-500/50 flex items-center justify-center gap-1 cursor-pointer"
                        >
                          <Droplets className="w-3.5 h-3.5" /> Hydrate
                        </button>
                        <button
                          onClick={() => fertilizePlot(plot.id)}
                          className="flex-1 py-2 rounded-xl bg-amber-900/40 hover:bg-amber-900/70 text-amber-200 text-xs font-bold border border-amber-600/40 flex items-center justify-center gap-1 cursor-pointer"
                        >
                          <Sparkles className="w-3.5 h-3.5" /> Enrich
                        </button>
                      </>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          /* Crop Catalog Codex */
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Object.values(CROP_CATALOG).map((crop) => (
              <div
                key={crop.id}
                className="p-4 rounded-2xl bg-[#2A160F]/90 border border-amber-900/40 flex items-start gap-3.5"
              >
                <div
                  className="w-12 h-12 rounded-xl flex items-center justify-center text-xl shadow-inner border border-white/10 shrink-0"
                  style={{ backgroundColor: crop.primaryColor + '33', color: crop.primaryColor }}
                >
                  🌾
                </div>
                <div className="flex-1">
                  <div className="flex items-center justify-between">
                    <h3 className="font-bold text-base text-amber-100 font-cinzel">{crop.displayName}</h3>
                    <div className="flex items-center gap-1 text-amber-300 font-bold text-xs">
                      <Coins className="w-3 h-3 text-[#D4AF37]" /> {crop.baseSellPrice}
                    </div>
                  </div>
                  <p className="text-xs text-stone-400 font-almendra mt-1">{crop.description}</p>
                  <div className="grid grid-cols-3 gap-2 mt-3 pt-2 border-t border-amber-900/20 text-[11px] text-stone-300">
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3 text-amber-400" /> {crop.growthDurationSec}s
                    </div>
                    <div className="flex items-center gap-1">
                      <Droplets className="w-3 h-3 text-cyan-400" /> {Math.round(crop.waterNeed * 100)}%
                    </div>
                    <div className="flex items-center gap-1">
                      <Sprout className="w-3 h-3 text-emerald-400" /> Yield: {crop.harvestYield}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
