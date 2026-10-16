import React from 'react';
import { useGame } from '../context/GameContext';
import { CRAFTING_RECIPES } from '../types/game';
import { Hammer, Zap, Check, X, Coins, Sparkles } from 'lucide-react';
import { sounds } from '../audio/soundSystem';

export const WorkshopScreen: React.FC = () => {
  const { inventory, gameState, addFloatingText, showNotification } = useGame();

  const handleCraft = (recipe: typeof CRAFTING_RECIPES[0]) => {
    // Check energy
    if (gameState.batteryChargeKwh < recipe.energyCostKwh) {
      sounds.playErrorSound();
      showNotification('Low Mana', `Requires ${recipe.energyCostKwh} kWh of stored mana.`, 'warning');
      return;
    }

    // Check ingredients
    const missing: string[] = [];
    Object.entries(recipe.ingredients).forEach(([itemId, req]) => {
      const inv = inventory.find((i) => i.itemId === itemId);
      if (!inv || inv.quantity < req.requiredQty) {
        missing.push(`${req.requiredQty}x ${req.itemName}`);
      }
    });

    if (missing.length > 0) {
      sounds.playErrorSound();
      showNotification('Missing Ingredients', `Missing: ${missing.join(', ')}`, 'warning');
      return;
    }

    sounds.playCraftSuccess();
    addFloatingText(`Crafted ${recipe.name}!`, '#FFD700');
    showNotification('Item Forged!', `Crafted ${recipe.name} at the Master's Forge.`, 'success');
  };

  return (
    <div className="w-full h-full pb-20 pt-16 px-4 overflow-y-auto bg-[#1A0F0A] text-[#F5EBE0] font-medieval">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="border-b border-[#D4AF37]/30 pb-4">
          <div className="flex items-center gap-2">
            <Hammer className="w-6 h-6 text-[#D4AF37]" />
            <h1 className="text-2xl font-bold text-[#D4AF37] tracking-wider font-cinzel">
              MASTER'S FORGE & ALCHEMY LAB
            </h1>
          </div>
          <p className="text-xs text-stone-400 font-almendra mt-1">
            Craft legendary artifacts, sacred fabrics & power stones using ancient mana and harvest offerings
          </p>
        </div>

        {/* Recipes Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {CRAFTING_RECIPES.map((recipe) => {
            const hasEnergy = gameState.batteryChargeKwh >= recipe.energyCostKwh;
            const canCraft =
              hasEnergy &&
              Object.entries(recipe.ingredients).every(([itemId, req]) => {
                const inv = inventory.find((i) => i.itemId === itemId);
                return inv && inv.quantity >= req.requiredQty;
              });

            return (
              <div
                key={recipe.id}
                className="p-5 rounded-2xl bg-[#2A160F]/90 border border-amber-900/40 flex flex-col justify-between space-y-4"
              >
                <div>
                  <div className="flex justify-between items-start">
                    <h3 className="text-lg font-bold text-amber-100 font-cinzel">{recipe.name}</h3>
                    <span className="text-xs text-amber-300 font-bold flex items-center gap-1">
                      <Coins className="w-3 h-3 text-[#D4AF37]" /> {recipe.sellValue}
                    </span>
                  </div>
                  <p className="text-xs text-stone-400 font-almendra mt-1">{recipe.description}</p>
                </div>

                {/* Ingredients & Mana cost */}
                <div className="space-y-2 bg-[#1A0F0A]/60 p-3 rounded-xl border border-amber-950">
                  <div className="flex justify-between text-xs text-stone-300 border-b border-amber-900/20 pb-1.5">
                    <span className="flex items-center gap-1 text-cyan-300">
                      <Zap className="w-3.5 h-3.5" /> Mana Energy Required:
                    </span>
                    <span className={hasEnergy ? 'text-cyan-400 font-bold' : 'text-rose-400 font-bold'}>
                      {recipe.energyCostKwh} kWh
                    </span>
                  </div>

                  <div className="space-y-1 pt-1">
                    <div className="text-[11px] text-stone-400">Required Offerings:</div>
                    {Object.entries(recipe.ingredients).map(([itemId, req]) => {
                      const inv = inventory.find((i) => i.itemId === itemId);
                      const current = inv ? inv.quantity : 0;
                      const hasEnough = current >= req.requiredQty;

                      return (
                        <div key={itemId} className="flex justify-between text-xs">
                          <span className={hasEnough ? 'text-amber-200' : 'text-rose-400'}>
                            {req.itemName}
                          </span>
                          <span className={hasEnough ? 'text-emerald-400' : 'text-rose-400'}>
                            {current} / {req.requiredQty}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Craft Button */}
                <button
                  onClick={() => handleCraft(recipe)}
                  disabled={!canCraft}
                  className={`w-full py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
                    canCraft
                      ? 'bg-gradient-to-r from-[#D4AF37] to-[#FFA000] text-slate-950 shadow-lg hover:brightness-110 active:scale-98'
                      : 'bg-stone-800 text-stone-500 cursor-not-allowed border border-stone-700'
                  }`}
                >
                  <Sparkles className="w-4 h-4" /> Forge Artifact
                </button>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
