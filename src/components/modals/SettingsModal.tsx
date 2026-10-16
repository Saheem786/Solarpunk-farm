import React from 'react';
import { useGame } from '../../context/GameContext';
import { X, Settings, Volume2, FastForward, RotateCcw } from 'lucide-react';

export const SettingsModal: React.FC = () => {
  const {
    soundEnabled,
    setSoundEnabled,
    masterVolume,
    setMasterVolume,
    timeSpeedMultiplier,
    setTimeSpeedMultiplier,
    resetGameData,
    openModal,
  } = useGame();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md font-medieval">
      <div className="w-full max-w-md rounded-2xl bg-[#1A0F0A] border-2 border-[#D4AF37] shadow-[0_0_35px_rgba(212,175,55,0.4)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[#D4AF37]/30 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <Settings className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-amber-100 font-cinzel text-lg">Realm Settings</h3>
          </div>
          <button
            onClick={() => openModal(null)}
            className="p-1 rounded-lg text-stone-400 hover:text-amber-200 hover:bg-stone-800 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 space-y-4 text-xs">
          {/* Master Volume */}
          <div className="space-y-1.5 bg-[#2A160F] p-3 rounded-xl border border-amber-900/40">
            <div className="flex justify-between items-center text-stone-300">
              <span className="flex items-center gap-1.5">
                <Volume2 className="w-4 h-4 text-amber-400" /> Audio Volume
              </span>
              <span className="font-mono text-amber-300">{Math.round(masterVolume * 100)}%</span>
            </div>
            <input
              type="range"
              min="0"
              max="1"
              step="0.05"
              value={masterVolume}
              onChange={(e) => setMasterVolume(parseFloat(e.target.value))}
              className="w-full accent-[#D4AF37] cursor-pointer"
            />
          </div>

          {/* Sound Toggle */}
          <div className="flex justify-between items-center bg-[#2A160F] p-3 rounded-xl border border-amber-900/40">
            <span className="text-stone-300">Synthesized Sound Effects</span>
            <button
              onClick={() => setSoundEnabled(!soundEnabled)}
              className={`px-3 py-1 rounded-lg font-bold text-xs cursor-pointer ${
                soundEnabled ? 'bg-[#00E676] text-slate-950' : 'bg-stone-800 text-stone-400'
              }`}
            >
              {soundEnabled ? 'ENABLED' : 'MUTED'}
            </button>
          </div>

          {/* Time Speed */}
          <div className="space-y-1.5 bg-[#2A160F] p-3 rounded-xl border border-amber-900/40">
            <div className="flex justify-between items-center text-stone-300">
              <span className="flex items-center gap-1.5">
                <FastForward className="w-4 h-4 text-cyan-400" /> Simulation Speed
              </span>
              <span className="font-mono text-cyan-300">{timeSpeedMultiplier}x</span>
            </div>
            <div className="grid grid-cols-3 gap-2 pt-1">
              {[1, 2, 4].map((speed) => (
                <button
                  key={speed}
                  onClick={() => setTimeSpeedMultiplier(speed)}
                  className={`py-1.5 rounded-lg font-bold text-xs cursor-pointer border ${
                    timeSpeedMultiplier === speed
                      ? 'bg-[#800020] text-amber-100 border-[#D4AF37]'
                      : 'bg-black/40 text-stone-400 border-stone-800 hover:bg-[#3E2723]'
                  }`}
                >
                  {speed}x Realtime
                </button>
              ))}
            </div>
          </div>

          {/* Reset Save Data */}
          <div className="pt-3 border-t border-amber-900/30">
            <button
              onClick={() => {
                if (window.confirm('Reset all farm progress and restore initial island state?')) {
                  resetGameData();
                  openModal(null);
                }
              }}
              className="w-full py-2.5 rounded-xl bg-rose-950/80 hover:bg-rose-900 text-rose-300 border border-rose-700/50 font-bold text-xs flex items-center justify-center gap-1.5 cursor-pointer"
            >
              <RotateCcw className="w-3.5 h-3.5" /> Reset Island Realm Data
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
