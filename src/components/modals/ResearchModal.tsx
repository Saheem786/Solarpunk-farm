import React from 'react';
import { useGame } from '../../context/GameContext';
import { X, Sparkles, CheckCircle2, Lock, ArrowDown } from 'lucide-react';

export const ResearchModal: React.FC = () => {
  const { researchTechs, gameState, unlockResearch, openModal } = useGame();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md font-medieval">
      <div className="w-full max-w-2xl rounded-2xl bg-[#1A0F0A] border-2 border-cyan-500 shadow-[0_0_35px_rgba(0,229,255,0.4)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-cyan-900/50 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-cyan-400" />
            <div>
              <h3 className="font-bold text-amber-100 font-cinzel text-lg">Arcane Research Tree</h3>
              <span className="text-xs text-cyan-300">
                Available Research Points: <strong>{gameState.researchPoints} RP</strong>
              </span>
            </div>
          </div>
          <button
            onClick={() => openModal(null)}
            className="p-1 rounded-lg text-stone-400 hover:text-amber-200 hover:bg-stone-800 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 space-y-3 max-h-[65vh] overflow-y-auto">
          {researchTechs.map((tech) => {
            const hasPrereqs =
              tech.prerequisiteTechIds.length === 0 ||
              tech.prerequisiteTechIds.every((reqId) =>
                researchTechs.some((t) => t.id === reqId && t.isUnlocked)
              );
            const canUnlock = hasPrereqs && !tech.isUnlocked && gameState.researchPoints >= tech.rpCost;

            return (
              <div
                key={tech.id}
                className={`p-4 rounded-xl border transition-all ${
                  tech.isUnlocked
                    ? 'bg-emerald-950/40 border-emerald-500/50 text-emerald-200'
                    : canUnlock
                    ? 'bg-[#2A160F] border-cyan-400 shadow-[0_0_12px_rgba(0,229,255,0.2)]'
                    : 'bg-black/40 border-stone-800 opacity-60 text-stone-400'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <span className="text-[10px] text-cyan-400 font-mono">Tier {tech.tier} Tech</span>
                    <h4 className="text-base font-bold text-amber-100 font-cinzel">{tech.title}</h4>
                    <p className="text-xs text-stone-300 font-almendra mt-1">{tech.description}</p>
                  </div>

                  {tech.isUnlocked ? (
                    <span className="text-emerald-400 font-bold text-xs flex items-center gap-1 shrink-0">
                      <CheckCircle2 className="w-4 h-4" /> Unlocked
                    </span>
                  ) : !hasPrereqs ? (
                    <span className="text-stone-500 text-xs flex items-center gap-1 shrink-0">
                      <Lock className="w-4 h-4" /> Prerequisite Required
                    </span>
                  ) : (
                    <button
                      onClick={() => unlockResearch(tech.id)}
                      disabled={!canUnlock}
                      className="px-3.5 py-1.5 rounded-lg bg-gradient-to-r from-cyan-600 to-emerald-500 hover:brightness-110 disabled:opacity-40 text-slate-950 text-xs font-bold shadow-md cursor-pointer shrink-0"
                    >
                      Unlock ({tech.rpCost} RP)
                    </button>
                  )}
                </div>

                <div className="text-[11px] text-cyan-300 font-mono mt-2 pt-1 border-t border-cyan-900/30">
                  Bonus Effect: {tech.unlockFeature}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
