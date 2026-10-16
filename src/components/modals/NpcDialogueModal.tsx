import React from 'react';
import { useGame } from '../../context/GameContext';
import { NPC_ROLES, NpcRoleId } from '../../types/game';
import { X, MessageSquare, Heart, Briefcase, Award } from 'lucide-react';

export const NpcDialogueModal: React.FC = () => {
  const { selectedNpcForDialogue, interactWithNpc, assignNpcRole, openModal } = useGame();

  if (!selectedNpcForDialogue) return null;
  const npc = selectedNpcForDialogue;
  const roleDef = NPC_ROLES[npc.role];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm font-medieval">
      <div className="w-full max-w-md rounded-2xl bg-[#1A0F0A] border-2 border-[#D4AF37] shadow-[0_0_30px_rgba(212,175,55,0.3)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[#D4AF37]/30 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <MessageSquare className="w-5 h-5 text-amber-400" />
            <div>
              <h3 className="font-bold text-amber-100 font-cinzel text-lg">{npc.name}</h3>
              <span className="text-[11px] text-cyan-300 font-mono">{roleDef.title}</span>
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
        <div className="p-5 space-y-4 text-xs">
          {/* Quote bubble */}
          <div className="p-3.5 rounded-xl bg-[#2A160F] border border-amber-900/40 text-stone-200 font-almendra italic text-sm">
            "{npc.speechBubble || 'Greetings, sire! The harvest blooms in thy realm and the mana stones hum with power.'}"
          </div>

          {/* Stats */}
          <div className="grid grid-cols-2 gap-2 bg-[#1A0F0A] p-3 rounded-xl border border-amber-950 text-[11px]">
            <div>
              <span className="text-stone-400">Morale:</span>{' '}
              <span className="text-emerald-400 font-bold">{npc.morale}%</span>
            </div>
            <div>
              <span className="text-stone-400">Personality:</span>{' '}
              <span className="text-amber-200 font-bold">{npc.trait}</span>
            </div>
            <div>
              <span className="text-stone-400">Friendship:</span>{' '}
              <span className="text-rose-400 font-bold">{npc.relationshipToPlayer}/100</span>
            </div>
            <div>
              <span className="text-stone-400">Current Duty:</span>{' '}
              <span className="text-cyan-300">{npc.currentActivity}</span>
            </div>
          </div>

          {/* Assign Role */}
          <div className="space-y-2">
            <h4 className="font-bold text-amber-100 font-cinzel flex items-center gap-1.5">
              <Briefcase className="w-4 h-4 text-amber-400" /> Assign Kingdom Duty:
            </h4>
            <div className="grid grid-cols-2 gap-2">
              {Object.values(NPC_ROLES).map((r) => (
                <button
                  key={r.id}
                  onClick={() => assignNpcRole(npc.id, r.id)}
                  className={`p-2 rounded-xl text-left border transition-all cursor-pointer ${
                    npc.role === r.id
                      ? 'bg-[#800020] text-amber-100 border-[#D4AF37] font-bold'
                      : 'bg-[#2A160F] text-stone-300 border-amber-900/30 hover:bg-[#3E2723]'
                  }`}
                >
                  <div className="text-xs">{r.displayName}</div>
                  <div className="text-[9px] text-stone-400 mt-0.5 truncate">{r.description}</div>
                </button>
              ))}
            </div>
          </div>

          {/* Actions */}
          <div className="flex gap-2 pt-2 border-t border-amber-900/30">
            <button
              onClick={() => interactWithNpc(npc.id, 'chat')}
              className="flex-1 py-2 rounded-xl bg-gradient-to-r from-[#D4AF37] to-[#FFA000] text-slate-950 font-bold text-xs shadow-md cursor-pointer"
            >
              Converse & Encourage
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
