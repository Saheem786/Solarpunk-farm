import React from 'react';
import { useGame } from '../../context/GameContext';
import { X, Flame, Droplets, Fish, Sparkles } from 'lucide-react';

export const CampfireModal: React.FC = () => {
  const { gameState, inventory, boilWater, cookFish, brewTea, openModal } = useGame();

  const fishList = inventory.filter((i) => i.itemId.startsWith('fish_') && i.quantity > 0);
  const herbItem = inventory.find((i) => i.itemId.includes('herb') && i.quantity > 0);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm font-medieval">
      <div className="w-full max-w-md rounded-2xl bg-[#1A0F0A] border-2 border-[#FFA000] shadow-[0_0_30px_rgba(255,160,0,0.3)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-amber-900/50 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <Flame className="w-5 h-5 text-orange-400" />
            <h3 className="font-bold text-amber-100 font-cinzel text-lg">
              Campfire Cooking & Hearth
            </h3>
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
          {/* Boil Water */}
          <div className="p-3.5 rounded-xl bg-[#2A160F] border border-amber-900/40 flex items-center justify-between gap-3">
            <div>
              <h4 className="font-bold text-cyan-300 flex items-center gap-1.5 text-sm">
                <Droplets className="w-4 h-4 text-cyan-400" /> Boil & Purify Stream Water
              </h4>
              <p className="text-xs text-stone-400 font-almendra mt-1">
                Converts raw river water into safe, hydrating Sacred Water.
              </p>
              <div className="text-[11px] text-stone-300 mt-1">
                Raw Water Available: <span className="text-cyan-400 font-bold">{gameState.rawWaterCarried}</span> units
              </div>
            </div>

            <button
              onClick={boilWater}
              disabled={gameState.rawWaterCarried <= 0}
              className="px-3.5 py-2 rounded-xl bg-cyan-800 hover:bg-cyan-700 disabled:opacity-40 text-cyan-100 text-xs font-bold cursor-pointer shrink-0"
            >
              Boil Vessel
            </button>
          </div>

          {/* Roast Fish */}
          <div className="p-3.5 rounded-xl bg-[#2A160F] border border-amber-900/40 space-y-2">
            <h4 className="font-bold text-amber-200 flex items-center gap-1.5 text-sm">
              <Fish className="w-4 h-4 text-amber-400" /> Roast Caught Fish
            </h4>
            <p className="text-xs text-stone-400 font-almendra">
              Cook fresh fish over oak embers into high-nutrition meals (+35 Hunger).
            </p>

            {fishList.length === 0 ? (
              <div className="text-xs text-stone-500 italic py-1">No raw fish in thy satchel.</div>
            ) : (
              <div className="space-y-1.5 pt-1">
                {fishList.map((fish) => (
                  <div key={fish.itemId} className="flex items-center justify-between text-xs bg-[#1A0F0A] p-2 rounded-lg">
                    <span className="text-amber-100">{fish.name} ({fish.quantity})</span>
                    <button
                      onClick={() => cookFish(fish.itemId)}
                      className="px-3 py-1 bg-amber-800 hover:bg-amber-700 text-amber-100 font-bold rounded-lg cursor-pointer"
                    >
                      Roast
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Brew Vitality Herbal Tea */}
          <div className="p-3.5 rounded-xl bg-[#2A160F] border border-amber-900/40 flex items-center justify-between gap-3">
            <div>
              <h4 className="font-bold text-emerald-300 flex items-center gap-1.5 text-sm">
                <Sparkles className="w-4 h-4 text-emerald-400" /> Brew Vitality Tea
              </h4>
              <p className="text-xs text-stone-400 font-almendra mt-1">
                Restores stamina and cures water-borne illness.
              </p>
              <div className="text-[11px] text-stone-300 mt-1">
                Requires: 1x Aromatic Herbs & 1x Sacred Water
              </div>
            </div>

            <button
              onClick={brewTea}
              disabled={!herbItem || gameState.cleanWaterCarried <= 0}
              className="px-3.5 py-2 rounded-xl bg-emerald-800 hover:bg-emerald-700 disabled:opacity-40 text-emerald-100 text-xs font-bold cursor-pointer shrink-0"
            >
              Brew Tea
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
