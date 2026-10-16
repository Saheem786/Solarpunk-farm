import React from 'react';
import { useGame } from '../../context/GameContext';
import { CROP_CATALOG, CropTypeId } from '../../types/game';
import { X, Sprout, Clock, Droplets, Coins } from 'lucide-react';

export const CropPlantModal: React.FC = () => {
  const { selectedPlotForModal, inventory, plantPlot, openModal } = useGame();

  if (!selectedPlotForModal) return null;

  const seedItems = inventory.filter((i) => i.category === 'SEEDS' && i.quantity > 0);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm font-medieval">
      <div className="w-full max-w-md rounded-2xl bg-[#1A0F0A] border-2 border-[#D4AF37] shadow-[0_0_30px_rgba(212,175,55,0.3)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[#D4AF37]/30 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <Sprout className="w-5 h-5 text-emerald-400" />
            <h3 className="font-bold text-amber-100 font-cinzel text-lg">
              Sow Sacred Seeds (Plot #{selectedPlotForModal.id + 1})
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
        <div className="p-4 space-y-3 max-h-[60vh] overflow-y-auto">
          {seedItems.length === 0 ? (
            <div className="text-center py-8 text-stone-400 font-almendra space-y-2">
              <p>Thou hast no seeds remaining in thy satchel.</p>
              <p className="text-xs text-amber-300">Visit the Bazaar to purchase seeds from the Merchant Guild.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-2.5">
              {seedItems.map((item) => {
                // Find matching crop
                const cropEntry = Object.values(CROP_CATALOG).find(
                  (c) => item.itemId.includes(c.id.toLowerCase()) || item.name.toLowerCase().includes(c.displayName.toLowerCase())
                );
                const cropId = (cropEntry ? cropEntry.id : 'WHEAT') as CropTypeId;
                const def = CROP_CATALOG[cropId];

                return (
                  <div
                    key={item.itemId}
                    className="p-3 rounded-xl bg-[#2A160F] border border-amber-900/40 hover:border-[#D4AF37] transition-all flex items-center justify-between gap-3"
                  >
                    <div>
                      <h4 className="font-bold text-amber-100">{item.name}</h4>
                      <div className="flex items-center gap-3 text-[11px] text-stone-300 mt-1">
                        <span className="flex items-center gap-1 text-amber-400">
                          <Clock className="w-3 h-3" /> {def.growthDurationSec}s
                        </span>
                        <span className="flex items-center gap-1 text-cyan-400">
                          <Droplets className="w-3 h-3" /> {Math.round(def.waterNeed * 100)}%
                        </span>
                        <span className="text-emerald-400 font-bold">Qty: {item.quantity}</span>
                      </div>
                    </div>

                    <button
                      onClick={() => plantPlot(selectedPlotForModal.id, cropId)}
                      className="px-4 py-2 rounded-xl bg-gradient-to-r from-[#D4AF37] to-[#FFA000] hover:brightness-110 text-slate-950 font-bold text-xs shadow-md cursor-pointer"
                    >
                      Sow
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
