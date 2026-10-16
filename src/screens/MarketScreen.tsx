import React, { useState } from 'react';
import { useGame } from '../context/GameContext';
import { CROP_CATALOG, CropTypeId } from '../types/game';
import {
  Store,
  Coins,
  Shield,
  Clock,
  CheckCircle,
  AlertTriangle,
  ArrowRight,
  Package,
} from 'lucide-react';

export const MarketScreen: React.FC = () => {
  const { contracts, inventory, gameState, claimContract, buySeed, sellInventoryItem } = useGame();
  const [activeTab, setActiveTab] = useState<'contracts' | 'seeds' | 'sell'>('contracts');

  const hour = gameState.gameTimeHour;
  const isOpen = hour >= 8.0 && hour < 18.0;

  return (
    <div className="w-full h-full pb-20 pt-16 px-4 overflow-y-auto bg-[#1A0F0A] text-[#F5EBE0] font-medieval">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-[#D4AF37]/30 pb-4">
          <div>
            <div className="flex items-center gap-2">
              <Store className="w-6 h-6 text-[#D4AF37]" />
              <h1 className="text-2xl font-bold text-[#D4AF37] tracking-wider font-cinzel">
                TRADING POST & MERCHANT GUILD
              </h1>
            </div>
            <p className="text-xs text-stone-400 font-almendra mt-1">
              Fulfill royal commissions, purchase sacred seeds, and barter island goods
            </p>
          </div>

          {/* Operating hours status */}
          <div
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold border ${
              isOpen
                ? 'bg-emerald-950/60 border-emerald-500/50 text-emerald-300'
                : 'bg-rose-950/60 border-rose-500/50 text-rose-300'
            }`}
          >
            <Clock className="w-4 h-4" />
            {isOpen ? 'Guild Open (8am - 6pm)' : 'Guild Closed (Opens 8am)'}
          </div>
        </div>

        {/* Closed Banner Warning */}
        {!isOpen && (
          <div className="p-4 rounded-2xl bg-[#3E1F1F] border-2 border-[#800020] text-center space-y-1">
            <h3 className="text-base font-bold text-rose-300 font-cinzel flex items-center justify-center gap-2">
              <AlertTriangle className="w-4 h-4 text-rose-400" /> BAZAAR CLOSED UNTIL DAWN
            </h3>
            <p className="text-xs text-amber-200/80 font-almendra">
              The merchant caravans have rested for the night. Return between 8:00 AM and 6:00 PM to trade.
            </p>
          </div>
        )}

        {/* Sub-Tabs */}
        <div className="flex items-center gap-2 border-b border-amber-900/30 pb-2">
          <button
            onClick={() => setActiveTab('contracts')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === 'contracts'
                ? 'bg-[#800020] text-amber-100 border border-[#D4AF37]'
                : 'bg-[#2A160F] text-stone-400 hover:text-amber-200'
            }`}
          >
            King's Commissions ({contracts.filter((c) => !c.isClaimed).length})
          </button>
          <button
            onClick={() => setActiveTab('seeds')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === 'seeds'
                ? 'bg-[#800020] text-amber-100 border border-[#D4AF37]'
                : 'bg-[#2A160F] text-stone-400 hover:text-amber-200'
            }`}
          >
            Sacred Seeds
          </button>
          <button
            onClick={() => setActiveTab('sell')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === 'sell'
                ? 'bg-[#800020] text-amber-100 border border-[#D4AF37]'
                : 'bg-[#2A160F] text-stone-400 hover:text-amber-200'
            }`}
          >
            Sell Goods
          </button>
        </div>

        {activeTab === 'contracts' ? (
          /* Royal Commissions / Contracts */
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {contracts.map((contract) => {
              const inv = inventory.find((i) => i.itemId === contract.requiredItemId);
              const available = inv ? inv.quantity : 0;
              const canClaim = available >= contract.requiredQty && !contract.isClaimed && isOpen;

              return (
                <div
                  key={contract.id}
                  className={`p-4 rounded-2xl bg-[#2A160F]/90 border space-y-3 ${
                    contract.isClaimed
                      ? 'border-stone-800 opacity-60'
                      : canClaim
                      ? 'border-emerald-500 shadow-[0_0_15px_rgba(0,230,118,0.2)]'
                      : 'border-amber-900/40'
                  }`}
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <span className="text-[10px] text-amber-400 font-mono">{contract.buyerName}</span>
                      <h3 className="font-bold text-amber-100 font-cinzel">{contract.title}</h3>
                    </div>
                    {contract.isClaimed && (
                      <span className="text-emerald-400 font-bold text-xs flex items-center gap-1">
                        <CheckCircle className="w-3.5 h-3.5" /> Claimed
                      </span>
                    )}
                  </div>

                  <div className="flex justify-between text-xs bg-[#1A0F0A] p-2.5 rounded-xl border border-amber-950">
                    <span className="text-stone-300">Required: {contract.requiredItemName}</span>
                    <span className={available >= contract.requiredQty ? 'text-emerald-400 font-bold' : 'text-rose-400'}>
                      {available} / {contract.requiredQty}
                    </span>
                  </div>

                  <div className="flex items-center justify-between pt-1">
                    <div className="flex items-center gap-3 text-xs font-bold">
                      <span className="flex items-center gap-1 text-amber-300">
                        <Coins className="w-3.5 h-3.5 text-[#D4AF37]" /> +{contract.rewardCoins}
                      </span>
                      <span className="flex items-center gap-1 text-emerald-300">
                        <Shield className="w-3.5 h-3.5 text-emerald-400" /> +{contract.rewardEco}
                      </span>
                    </div>

                    {!contract.isClaimed && (
                      <button
                        onClick={() => claimContract(contract.id)}
                        disabled={!canClaim}
                        className={`px-4 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                          canClaim
                            ? 'bg-[#00E676] hover:bg-emerald-400 text-slate-950 shadow-md active:scale-95'
                            : 'bg-stone-800 text-stone-500 cursor-not-allowed'
                        }`}
                      >
                        Deliver Goods
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        ) : activeTab === 'seeds' ? (
          /* Buy Seeds */
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
            {Object.values(CROP_CATALOG).map((crop) => (
              <div
                key={crop.id}
                className="p-3.5 rounded-xl bg-[#2A160F] border border-amber-900/40 flex flex-col justify-between space-y-2"
              >
                <div>
                  <h4 className="font-bold text-amber-100 font-cinzel">{crop.displayName}</h4>
                  <div className="text-[11px] text-stone-400 font-almendra">{crop.description}</div>
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-amber-900/20">
                  <span className="text-xs text-amber-300 font-bold flex items-center gap-1">
                    <Coins className="w-3 h-3 text-[#D4AF37]" /> {crop.seedCost}
                  </span>
                  <button
                    onClick={() => buySeed(crop.id, 1)}
                    disabled={!isOpen || gameState.solCoins < crop.seedCost}
                    className="px-3 py-1 rounded-lg bg-[#800020] hover:bg-[#A31535] text-amber-100 text-xs font-bold border border-[#D4AF37]/50 disabled:opacity-40 cursor-pointer"
                  >
                    Buy Seed
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          /* Sell Goods */
          <div className="space-y-3">
            {inventory.filter((i) => i.quantity > 0 && i.category !== 'TOOL').length === 0 ? (
              <div className="text-center py-10 text-stone-500 font-almendra">
                Thy satchel holds no trade goods to sell.
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {inventory
                  .filter((i) => i.quantity > 0 && i.category !== 'TOOL')
                  .map((item) => (
                    <div
                      key={item.itemId}
                      className="p-3.5 rounded-xl bg-[#2A160F] border border-amber-900/40 flex items-center justify-between"
                    >
                      <div>
                        <h4 className="font-bold text-amber-100">{item.name}</h4>
                        <div className="text-[10px] text-stone-400">
                          Qty: {item.quantity} • Value: {item.sellValue} coins each
                        </div>
                      </div>

                      <div className="flex gap-1.5">
                        <button
                          onClick={() => sellInventoryItem(item.itemId, 1)}
                          disabled={!isOpen}
                          className="px-2.5 py-1 rounded-lg bg-amber-900/40 hover:bg-amber-900 text-amber-200 text-xs font-bold border border-amber-700/40 cursor-pointer disabled:opacity-40"
                        >
                          Sell 1
                        </button>
                        <button
                          onClick={() => sellInventoryItem(item.itemId, item.quantity)}
                          disabled={!isOpen}
                          className="px-2.5 py-1 rounded-lg bg-[#800020] hover:bg-[#A31535] text-amber-100 text-xs font-bold border border-[#D4AF37]/40 cursor-pointer disabled:opacity-40"
                        >
                          Sell All
                        </button>
                      </div>
                    </div>
                  ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
