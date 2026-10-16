import React, { useState } from 'react';
import { useGame } from '../../context/GameContext';
import { X, Terminal, Lock, KeyRound, CheckCircle } from 'lucide-react';

export const TerminalHackModal: React.FC = () => {
  const { terminalLogs, hackTerminal, openModal } = useGame();
  const [passwordInput, setPasswordInput] = useState('');
  const [selectedTermId, setSelectedTermId] = useState<string>(terminalLogs[0]?.id || 'term_1');

  const selectedTerm = terminalLogs.find((t) => t.id === selectedTermId) || terminalLogs[0];

  const handleAttempt = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTerm) return;
    const ok = hackTerminal(selectedTerm.id, passwordInput);
    if (ok) {
      setPasswordInput('');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md font-mono">
      <div className="w-full max-w-lg rounded-2xl bg-[#0a1118] border-2 border-cyan-500 shadow-[0_0_35px_rgba(0,229,255,0.4)] overflow-hidden text-cyan-400">
        {/* Terminal Header */}
        <div className="flex items-center justify-between p-3.5 border-b border-cyan-900/60 bg-[#061017]">
          <div className="flex items-center gap-2">
            <Terminal className="w-4 h-4 text-cyan-400" />
            <span className="font-bold text-xs tracking-wider text-cyan-300">
              ARCHIVE_TERMINAL_INTERFACE // SDZ_SOLARIA
            </span>
          </div>
          <button
            onClick={() => openModal(null)}
            className="p-1 rounded text-stone-400 hover:text-cyan-200 cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 space-y-4 text-xs">
          {/* Selector */}
          <div className="flex gap-2 overflow-x-auto pb-2 border-b border-cyan-950">
            {terminalLogs.map((t) => (
              <button
                key={t.id}
                onClick={() => setSelectedTermId(t.id)}
                className={`px-3 py-1.5 rounded-lg text-[11px] whitespace-nowrap cursor-pointer transition-all ${
                  selectedTermId === t.id
                    ? 'bg-cyan-950 text-cyan-200 border border-cyan-400 font-bold'
                    : 'bg-black/40 text-stone-500 hover:text-cyan-400 border border-stone-800'
                }`}
              >
                {t.terminalName}
              </button>
            ))}
          </div>

          {selectedTerm && (
            <div className="space-y-3">
              <div className="flex justify-between items-center text-[11px] text-stone-400">
                <span>Location: {selectedTerm.locationName}</span>
                <span className={selectedTerm.isHacked ? 'text-emerald-400 font-bold' : 'text-rose-400 font-bold'}>
                  STATUS: {selectedTerm.isHacked ? 'DECRYPTED' : 'LOCKED'}
                </span>
              </div>

              {selectedTerm.isHacked ? (
                <div className="p-4 rounded-xl bg-black/60 border border-cyan-900 text-stone-200 space-y-2">
                  <div className="text-emerald-400 text-xs font-bold flex items-center gap-1.5">
                    <CheckCircle className="w-4 h-4" /> CIPHER DECRYPTED (+25 RP GRANTED)
                  </div>
                  <p className="text-xs leading-relaxed font-mono">{selectedTerm.logText}</p>
                </div>
              ) : (
                <form onSubmit={handleAttempt} className="space-y-3">
                  <div className="p-4 rounded-xl bg-black/70 border border-rose-950 text-stone-400 space-y-2">
                    <div className="text-rose-400 font-bold flex items-center gap-1.5">
                      <Lock className="w-4 h-4" /> ENCRYPTED MASTER ARCHIVE
                    </div>
                    <p className="text-[11px]">
                      Enter security override cipher to decrypt this sector's research database.
                    </p>
                    <div className="text-[10px] text-amber-400 italic">
                      HINT: Discovered in Facility Core Log: SOLARIS
                    </div>
                  </div>

                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={passwordInput}
                      onChange={(e) => setPasswordInput(e.target.value)}
                      placeholder="ENTER CIPHER..."
                      className="flex-1 px-3 py-2 rounded-xl bg-black/80 border border-cyan-800 text-cyan-200 uppercase text-xs outline-none focus:border-cyan-400"
                    />
                    <button
                      type="submit"
                      className="px-4 py-2 rounded-xl bg-cyan-700 hover:bg-cyan-600 text-slate-950 font-bold text-xs flex items-center gap-1 cursor-pointer"
                    >
                      <KeyRound className="w-3.5 h-3.5" /> DECRYPT
                    </button>
                  </div>
                </form>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
