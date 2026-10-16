import React, { useState } from 'react';
import { useGame } from '../context/GameContext';
import {
  BookOpen,
  Award,
  Users,
  Compass,
  Cpu,
  BookMarked,
  Scroll,
  CheckCircle2,
  Lock,
  ChevronRight,
  Sparkles,
} from 'lucide-react';

export const BusinessDashboardScreen: React.FC = () => {
  const {
    gameState,
    npcs,
    storyMissions,
    loreEntries,
    terminalLogs,
    ecosystemHealth,
    openModal,
    unlockResearch,
  } = useGame();

  const [activeTab, setActiveTab] = useState<'missions' | 'sanctuary' | 'lore' | 'terminals'>('missions');

  const totalCitizens = npcs.length;
  const avgMorale = totalCitizens > 0 ? Math.round(npcs.reduce((acc, n) => acc + n.morale, 0) / totalCitizens) : 100;

  return (
    <div className="w-full h-full pb-20 pt-16 px-4 overflow-y-auto bg-[#1A0F0A] text-[#F5EBE0] font-medieval">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="border-b border-[#D4AF37]/30 pb-4">
          <div className="flex items-center gap-2">
            <BookOpen className="w-6 h-6 text-[#D4AF37]" />
            <h1 className="text-2xl font-bold text-[#D4AF37] tracking-wider font-cinzel">
              KINGDOM CHRONICLES & SANCTUARY
            </h1>
          </div>
          <p className="text-xs text-stone-400 font-almendra mt-1">
            Story objectives, settlement citizen welfare, ancient encrypted logs, and historical lore
          </p>
        </div>

        {/* Quick Stats Banner */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="p-3.5 rounded-2xl bg-[#2A160F] border border-amber-900/40">
            <div className="text-xs text-stone-400">Sanctuary Citizens</div>
            <div className="text-xl font-bold font-cinzel text-amber-100 mt-1">{totalCitizens} Souls</div>
            <div className="text-[10px] text-emerald-400">Avg Morale: {avgMorale}%</div>
          </div>

          <div className="p-3.5 rounded-2xl bg-[#2A160F] border border-amber-900/40">
            <div className="text-xs text-stone-400">Ecosystem Vitality</div>
            <div className="text-xl font-bold font-cinzel text-emerald-400 mt-1">{ecosystemHealth} / 100</div>
            <div className="text-[10px] text-stone-400">Regenerative balance</div>
          </div>

          <div className="p-3.5 rounded-2xl bg-[#2A160F] border border-amber-900/40">
            <div className="text-xs text-stone-400">Research Points</div>
            <div className="text-xl font-bold font-cinzel text-cyan-300 mt-1">{gameState.researchPoints} RP</div>
            <div className="text-[10px] text-stone-400">Earned from exploration</div>
          </div>

          <div className="p-3.5 rounded-2xl bg-[#2A160F] border border-amber-900/40">
            <div className="text-xs text-stone-400">POIs Discovered</div>
            <div className="text-xl font-bold font-cinzel text-amber-300 mt-1">{gameState.discoveredPois.length} / 15</div>
            <div className="text-[10px] text-stone-400">Across 3 biomes</div>
          </div>
        </div>

        {/* Action Shortcuts */}
        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => openModal('research')}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#2A160F] hover:bg-[#3E2723] border border-cyan-500/50 text-cyan-300 text-xs font-bold cursor-pointer"
          >
            <Sparkles className="w-3.5 h-3.5" /> Research Tree
          </button>
          <button
            onClick={() => openModal('sanctuary')}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#2A160F] hover:bg-[#3E2723] border border-emerald-500/50 text-emerald-300 text-xs font-bold cursor-pointer"
          >
            <Users className="w-3.5 h-3.5" /> Citizens & Beds
          </button>
          <button
            onClick={() => openModal('journal')}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#2A160F] hover:bg-[#3E2723] border border-amber-500/50 text-amber-300 text-xs font-bold cursor-pointer"
          >
            <Scroll className="w-3.5 h-3.5" /> Lore Journal
          </button>
        </div>

        {/* Tabs */}
        <div className="flex items-center gap-2 border-b border-amber-900/30 pb-2">
          <button
            onClick={() => setActiveTab('missions')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === 'missions'
                ? 'bg-[#800020] text-amber-100 border border-[#D4AF37]'
                : 'bg-[#2A160F] text-stone-400 hover:text-amber-200'
            }`}
          >
            Story Missions ({storyMissions.filter((m) => m.isCompleted).length}/{storyMissions.length})
          </button>
          <button
            onClick={() => setActiveTab('sanctuary')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === 'sanctuary'
                ? 'bg-[#800020] text-amber-100 border border-[#D4AF37]'
                : 'bg-[#2A160F] text-stone-400 hover:text-amber-200'
            }`}
          >
            Settlement Citizens
          </button>
          <button
            onClick={() => setActiveTab('lore')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === 'lore'
                ? 'bg-[#800020] text-amber-100 border border-[#D4AF37]'
                : 'bg-[#2A160F] text-stone-400 hover:text-amber-200'
            }`}
          >
            Lore Entries
          </button>
          <button
            onClick={() => setActiveTab('terminals')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === 'terminals'
                ? 'bg-[#800020] text-amber-100 border border-[#D4AF37]'
                : 'bg-[#2A160F] text-stone-400 hover:text-amber-200'
            }`}
          >
            Ancient Terminals
          </button>
        </div>

        {activeTab === 'missions' ? (
          /* Story Missions */
          <div className="space-y-3">
            {storyMissions.map((mission) => (
              <div
                key={mission.id}
                className={`p-4 rounded-2xl border transition-all ${
                  mission.isCompleted
                    ? 'bg-[#2A160F]/60 border-stone-800 opacity-70'
                    : mission.isUnlocked
                    ? 'bg-[#2A160F] border-[#D4AF37]/50 shadow-[0_0_12px_rgba(212,175,55,0.2)]'
                    : 'bg-black/30 border-stone-900 opacity-40'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <span className="text-[10px] text-amber-400 font-mono">Mission #{mission.missionNumber}</span>
                    <h3 className="text-base font-bold text-amber-100 font-cinzel">{mission.title}</h3>
                    <p className="text-xs text-stone-300 font-almendra mt-1">{mission.description}</p>
                  </div>
                  {mission.isCompleted ? (
                    <span className="text-emerald-400 text-xs font-bold flex items-center gap-1">
                      <CheckCircle2 className="w-4 h-4" /> Completed
                    </span>
                  ) : !mission.isUnlocked ? (
                    <span className="text-stone-500 text-xs flex items-center gap-1">
                      <Lock className="w-4 h-4" /> Locked
                    </span>
                  ) : (
                    <span className="text-amber-300 text-xs font-bold">Active</span>
                  )}
                </div>

                <div className="mt-3 pt-2 border-t border-amber-900/20 flex flex-wrap items-center justify-between text-xs">
                  <div className="text-stone-300">
                    <span className="text-amber-300 font-bold">Objective:</span> {mission.objectiveDescription}
                  </div>
                  <div className="text-emerald-400 font-bold mt-1 sm:mt-0">
                    Reward: {mission.rewardSummary}
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : activeTab === 'sanctuary' ? (
          /* Citizens */
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {npcs.map((npc) => (
              <div key={npc.id} className="p-4 rounded-2xl bg-[#2A160F] border border-amber-900/40 space-y-2">
                <div className="flex justify-between items-start">
                  <div>
                    <h4 className="font-bold text-amber-100 text-base">{npc.name}</h4>
                    <span className="text-xs text-[#00E5FF] capitalize">{npc.role.replace('role_', '')}</span>
                  </div>
                  <span className="text-xs text-amber-300 font-bold">Morale: {npc.morale}%</span>
                </div>
                <div className="text-xs text-stone-400">
                  Trait: {npc.trait} • Activity: {npc.currentActivity}
                </div>
                <div className="text-[11px] text-stone-300 pt-2 border-t border-amber-900/20">
                  Friendship: {npc.relationshipToPlayer} / 100
                </div>
              </div>
            ))}
          </div>
        ) : activeTab === 'lore' ? (
          /* Lore Entries */
          <div className="space-y-3">
            {loreEntries.map((lore) => (
              <div key={lore.id} className="p-4 rounded-2xl bg-[#2A160F] border border-amber-900/40 space-y-2">
                <div className="flex justify-between items-start">
                  <div>
                    <h4 className="font-bold text-amber-100 text-base font-cinzel">{lore.title}</h4>
                    <span className="text-[10px] text-stone-400">By {lore.author}</span>
                  </div>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-black/40 text-amber-400 border border-amber-900">
                    {lore.category}
                  </span>
                </div>
                <p className="text-xs text-stone-300 font-almendra italic leading-relaxed pt-1">
                  "{lore.textContent}"
                </p>
              </div>
            ))}
          </div>
        ) : (
          /* Terminals */
          <div className="space-y-3">
            {terminalLogs.map((term) => (
              <div key={term.id} className="p-4 rounded-2xl bg-[#2A160F] border border-cyan-900/40 space-y-2 font-mono">
                <div className="flex justify-between items-start">
                  <div>
                    <h4 className="font-bold text-cyan-300 text-sm">{term.terminalName}</h4>
                    <div className="text-[10px] text-stone-400">{term.locationName}</div>
                  </div>
                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                      term.isHacked ? 'bg-emerald-950 text-emerald-400 border border-emerald-600' : 'bg-rose-950 text-rose-400 border border-rose-600'
                    }`}
                  >
                    {term.isHacked ? 'DECRYPTED' : 'ENCRYPTED'}
                  </span>
                </div>
                <p className="text-xs text-stone-300 bg-black/50 p-2.5 rounded-lg border border-cyan-950">
                  {term.isHacked ? term.logText : '*** ENCRYPTED DATA BLOCK *** (Password required: SOLARIS)'}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
