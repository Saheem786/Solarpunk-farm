import React, { useEffect } from 'react';
import { useGame } from '../../context/GameContext';
import confetti from 'canvas-confetti';
import { Sparkles, Trophy, Check } from 'lucide-react';
import { sounds } from '../../audio/soundSystem';

export const EndingSequenceModal: React.FC = () => {
  const { openModal } = useGame();

  useEffect(() => {
    sounds.playDiscoverySparkle();
    confetti({
      particleCount: 120,
      spread: 80,
      origin: { y: 0.6 },
      colors: ['#D4AF37', '#00E5FF', '#00E676', '#FFD700', '#800020'],
    });
  }, []);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/90 backdrop-blur-lg font-medieval animate-fade-in">
      <div className="w-full max-w-lg rounded-3xl bg-gradient-to-b from-[#2A160F] via-[#1A0F0A] to-[#0D0805] border-2 border-[#D4AF37] shadow-[0_0_50px_rgba(212,175,55,0.6)] p-8 text-center space-y-6">
        <div className="w-20 h-20 rounded-full bg-gradient-to-br from-[#D4AF37] to-[#FFA000] mx-auto flex items-center justify-center text-slate-950 text-4xl shadow-[0_0_30px_rgba(212,175,55,0.8)] animate-bounce">
          ✨
        </div>

        <div>
          <span className="text-xs text-cyan-300 font-mono tracking-widest uppercase">
            Story Epilogue Achieved
          </span>
          <h2 className="text-3xl font-bold text-[#D4AF37] font-cinzel tracking-wider mt-1">
            PROJECT NEW DAWN
          </h2>
          <h4 className="text-sm text-amber-200/90 font-cinzel mt-1">
            Harmony Restored to Medieval Island
          </h4>
        </div>

        <p className="text-xs text-stone-300 font-almendra leading-relaxed px-4">
          Through thy stewardship, the fertile groves of Green Valley, the ancient canopies of Deep Forest,
          and the sacred springs of Wetlands have been woven together in sustainable balance.
          Clean arcane mana courses freely through the crystal obelisks and aeolian spires,
          sheltering thy loyal citizens and beloved creatures forevermore.
        </p>

        <div className="p-4 rounded-2xl bg-black/50 border border-amber-900/60 text-xs text-amber-300 font-cinzel space-y-1">
          <div>Thy name is recorded in the Golden Chronicles of Solaria.</div>
          <div className="text-[11px] text-stone-400 font-almendra">
            Thou canst continue governing and cultivating thy realm indefinitely!
          </div>
        </div>

        <button
          onClick={() => openModal(null)}
          className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-[#D4AF37] via-[#FFA000] to-[#FFD700] hover:brightness-110 text-slate-950 font-bold text-sm shadow-[0_0_20px_rgba(212,175,55,0.5)] cursor-pointer"
        >
          Return to Thy Sovereign Realm
        </button>
      </div>
    </div>
  );
};
