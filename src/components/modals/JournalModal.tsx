import React from 'react';
import { useGame } from '../../context/GameContext';
import { X, Scroll, BookMarked, User } from 'lucide-react';

export const JournalModal: React.FC = () => {
  const { loreEntries, openModal } = useGame();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md font-medieval">
      <div className="w-full max-w-2xl rounded-2xl bg-[#1A0F0A] border-2 border-[#D4AF37] shadow-[0_0_35px_rgba(212,175,55,0.4)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[#D4AF37]/30 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <Scroll className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-amber-100 font-cinzel text-lg">Lore Journal & Settler Diaries</h3>
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
          {loreEntries.map((lore) => (
            <div
              key={lore.id}
              className="p-4 rounded-xl bg-[#2A160F] border border-amber-900/40 space-y-2"
            >
              <div className="flex justify-between items-start">
                <div>
                  <h4 className="font-bold text-amber-100 text-base font-cinzel">{lore.title}</h4>
                  <span className="text-xs text-stone-400 flex items-center gap-1 mt-0.5">
                    <User className="w-3 h-3 text-amber-400" /> {lore.author}
                  </span>
                </div>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-black/40 text-amber-300 border border-amber-900">
                  {lore.category}
                </span>
              </div>
              <p className="text-xs text-stone-300 font-almendra italic leading-relaxed pt-1 border-t border-amber-900/20">
                "{lore.textContent}"
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
