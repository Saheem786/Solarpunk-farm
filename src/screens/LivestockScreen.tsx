import React from 'react';
import { useGame } from '../context/GameContext';
import { LIVESTOCK_CATALOG, LivestockTypeId } from '../types/game';
import {
  PawPrint,
  Heart,
  Utensils,
  Scissors,
  Plus,
  Coins,
  Sparkles,
  Info,
} from 'lucide-react';

export const LivestockScreen: React.FC = () => {
  const { livestock, feedAnimal, petAnimal, harvestAnimalProduce, buyLivestock, gameState } = useGame();

  return (
    <div className="w-full h-full pb-20 pt-16 px-4 overflow-y-auto bg-[#1A0F0A] text-[#F5EBE0] font-medieval">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-[#D4AF37]/30 pb-4">
          <div>
            <div className="flex items-center gap-2">
              <PawPrint className="w-6 h-6 text-amber-400" />
              <h1 className="text-2xl font-bold text-[#D4AF37] tracking-wider font-cinzel">
                REGENERATIVE MENAGERIE
              </h1>
            </div>
            <p className="text-xs text-stone-400 font-almendra mt-1">
              Sanctuary animals producing divine fleece, sacred milk, farm eggs, and wild forest honey
            </p>
          </div>
          <div className="text-xs text-amber-300 font-bold bg-[#2A160F] px-3 py-1.5 rounded-xl border border-amber-900/40">
            Total Creatures: {livestock.length}
          </div>
        </div>

        {/* Animals Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {livestock.map((animal) => {
            const def = LIVESTOCK_CATALOG[animal.type];

            return (
              <div
                key={animal.id}
                className="p-4 rounded-2xl bg-[#2A160F]/90 border border-amber-900/40 space-y-3 relative overflow-hidden"
              >
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-lg font-bold text-amber-100 font-cinzel">{animal.name}</h3>
                    <span className="text-xs text-[#D4AF37]">{def.displayName}</span>
                  </div>

                  {animal.readyToHarvest && (
                    <span className="px-2.5 py-1 rounded-full bg-[#00E676] text-slate-950 text-[10px] font-bold shadow-md animate-pulse">
                      Ready to Collect!
                    </span>
                  )}
                </div>

                {/* Vitals */}
                <div className="grid grid-cols-2 gap-3 text-xs">
                  <div>
                    <div className="flex justify-between text-stone-300 mb-1">
                      <span className="flex items-center gap-1">
                        <Heart className="w-3 h-3 text-rose-400" /> Happiness
                      </span>
                      <span>{Math.round(animal.happiness)}%</span>
                    </div>
                    <div className="w-full h-1.5 rounded-full bg-black/60 overflow-hidden">
                      <div
                        className="h-full bg-rose-500"
                        style={{ width: `${Math.min(100, animal.happiness)}%` }}
                      />
                    </div>
                  </div>

                  <div>
                    <div className="flex justify-between text-stone-300 mb-1">
                      <span className="flex items-center gap-1">
                        <Utensils className="w-3 h-3 text-amber-400" /> Hunger
                      </span>
                      <span>{Math.round(animal.hunger)}%</span>
                    </div>
                    <div className="w-full h-1.5 rounded-full bg-black/60 overflow-hidden">
                      <div
                        className="h-full bg-amber-500"
                        style={{ width: `${Math.min(100, animal.hunger)}%` }}
                      />
                    </div>
                  </div>
                </div>

                {/* Produce Timer */}
                <div>
                  <div className="flex justify-between text-[11px] text-stone-400 mb-1">
                    <span>Producing: {def.productProduced}</span>
                    <span>{Math.round(animal.produceProgress * 100)}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-black/60 overflow-hidden border border-amber-950">
                    <div
                      className="h-full bg-gradient-to-r from-amber-600 to-yellow-300 transition-all duration-300"
                      style={{ width: `${Math.min(100, animal.produceProgress * 100)}%` }}
                    />
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="flex gap-2 pt-2 border-t border-amber-900/30">
                  <button
                    onClick={() => petAnimal(animal.id)}
                    className="flex-1 py-1.5 rounded-xl bg-[#800020]/80 hover:bg-[#800020] text-amber-100 text-xs font-bold border border-[#D4AF37]/30 flex items-center justify-center gap-1 cursor-pointer"
                  >
                    <Heart className="w-3 h-3 text-rose-400" /> Pet
                  </button>
                  <button
                    onClick={() => feedAnimal(animal.id)}
                    className="flex-1 py-1.5 rounded-xl bg-amber-900/50 hover:bg-amber-900 text-amber-100 text-xs font-bold border border-amber-700/40 flex items-center justify-center gap-1 cursor-pointer"
                  >
                    <Utensils className="w-3 h-3 text-amber-400" /> Feed
                  </button>
                  {animal.readyToHarvest && (
                    <button
                      onClick={() => harvestAnimalProduce(animal.id)}
                      className="flex-1 py-1.5 rounded-xl bg-[#00E676] hover:bg-emerald-400 text-slate-950 text-xs font-bold shadow-md flex items-center justify-center gap-1 cursor-pointer"
                    >
                      <Scissors className="w-3 h-3" /> Collect
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {/* Adopt New Animals Section */}
        <div className="p-5 rounded-2xl bg-[#2A160F] border border-amber-900/40 space-y-4">
          <h2 className="text-lg font-bold text-amber-100 font-cinzel">Adopt Sanctuary Animals</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {Object.values(LIVESTOCK_CATALOG).map((animalDef) => (
              <div
                key={animalDef.id}
                className="p-3.5 rounded-xl bg-[#1A0F0A] border border-amber-900/30 flex flex-col justify-between"
              >
                <div>
                  <h4 className="font-bold text-amber-100">{animalDef.displayName}</h4>
                  <p className="text-[11px] text-stone-400 font-almendra mt-1">{animalDef.description}</p>
                </div>
                <div className="mt-3 pt-2 border-t border-amber-900/20 flex items-center justify-between">
                  <div className="flex items-center gap-1 text-xs text-amber-300 font-bold">
                    <Coins className="w-3 h-3 text-[#D4AF37]" /> {animalDef.purchaseCostCoins}
                  </div>
                  <button
                    onClick={() => buyLivestock(animalDef.id)}
                    className="px-3 py-1 rounded-lg bg-[#800020] hover:bg-[#A31535] text-amber-100 text-xs font-bold border border-[#D4AF37]/50 cursor-pointer"
                  >
                    Adopt
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
