import React from 'react';
import { useGame } from '../../context/GameContext';
import { X, Users, Bed, Utensils, Droplets, Heart, Sparkles } from 'lucide-react';

export const SanctuaryModal: React.FC = () => {
  const { npcs, placedBuildings, openModal } = useGame();

  const totalCitizens = npcs.length;
  // Calculate total beds from placed cabins and bunkhouses
  let totalBeds = 4; // base farmhouse beds
  placedBuildings.forEach((b) => {
    if (b.buildingType === 'CABIN' || b.buildingType === 'NPC_CABIN') totalBeds += 2;
    if (b.buildingType === 'BUNKHOUSE') totalBeds += 6;
  });

  const avgMorale = totalCitizens > 0 ? Math.round(npcs.reduce((a, n) => a + n.morale, 0) / totalCitizens) : 100;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md font-medieval">
      <div className="w-full max-w-xl rounded-2xl bg-[#1A0F0A] border-2 border-emerald-500 shadow-[0_0_35px_rgba(0,230,118,0.3)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-emerald-900/50 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <Users className="w-5 h-5 text-emerald-400" />
            <h3 className="font-bold text-amber-100 font-cinzel text-lg">Settlement Sanctuary & Citizens</h3>
          </div>
          <button
            onClick={() => openModal(null)}
            className="p-1 rounded-lg text-stone-400 hover:text-amber-200 hover:bg-stone-800 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 space-y-4 max-h-[65vh] overflow-y-auto">
          {/* Quick Metrics */}
          <div className="grid grid-cols-3 gap-3 text-center">
            <div className="p-3 rounded-xl bg-[#2A160F] border border-emerald-900/40">
              <div className="text-[10px] text-stone-400">Sheltered Beds</div>
              <div className="text-lg font-bold font-cinzel text-emerald-300 mt-1">
                {totalCitizens} / {totalBeds}
              </div>
              <div className="text-[9px] text-stone-400">
                {totalBeds >= totalCitizens ? 'All Housed' : 'Deficit!'}
              </div>
            </div>

            <div className="p-3 rounded-xl bg-[#2A160F] border border-amber-900/40">
              <div className="text-[10px] text-stone-400">Overall Morale</div>
              <div className="text-lg font-bold font-cinzel text-amber-300 mt-1">{avgMorale}%</div>
              <div className="text-[9px] text-emerald-400">High Harmony</div>
            </div>

            <div className="p-3 rounded-xl bg-[#2A160F] border border-cyan-900/40">
              <div className="text-[10px] text-stone-400">Daily Demand</div>
              <div className="text-lg font-bold font-cinzel text-cyan-300 mt-1">
                {totalCitizens * 2} Meals
              </div>
              <div className="text-[9px] text-stone-400">From Royal Hearth</div>
            </div>
          </div>

          {/* Citizen Roster */}
          <div className="space-y-2 pt-2">
            <h4 className="font-bold text-amber-100 font-cinzel text-sm">Loyal Subjects ({npcs.length})</h4>
            <div className="space-y-2">
              {npcs.map((npc) => (
                <div
                  key={npc.id}
                  className="p-3 rounded-xl bg-[#2A160F] border border-amber-900/30 flex items-center justify-between"
                >
                  <div>
                    <h5 className="font-bold text-amber-100">{npc.name}</h5>
                    <div className="text-[11px] text-stone-400">
                      Trait: {npc.trait} • Activity: {npc.currentActivity}
                    </div>
                  </div>

                  <div className="text-right">
                    <span className="text-xs text-emerald-400 font-bold block">Morale: {npc.morale}%</span>
                    <span className="text-[10px] text-cyan-300 uppercase font-mono">
                      {npc.role.replace('role_', '')}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
