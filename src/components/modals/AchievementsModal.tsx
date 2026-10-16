import React from 'react';
import { useGame } from '../../context/GameContext';
import { X, Trophy, Award, CheckCircle2, Lock } from 'lucide-react';

interface AchievementDef {
  id: string;
  title: string;
  description: string;
  icon: string;
}

const ALL_ACHIEVEMENTS: AchievementDef[] = [
  { id: 'ach_first_steps', title: 'First Steps', description: 'Arrive at the medieval island and inspect thy citadel.', icon: '🏰' },
  { id: 'ach_first_harvest', title: 'Bountiful Harvest', description: 'Harvest thy first golden grain or sun-ripened crop.', icon: '🌾' },
  { id: 'ach_animal_friend', title: 'Kind Spirit', description: 'Befriend and pet a sanctuary animal.', icon: '🐑' },
  { id: 'ach_first_catch', title: 'Master of the Rod', description: 'Land a fresh fish from island streams or lakes.', icon: '🎣' },
  { id: 'ach_first_building', title: 'Royal Architect', description: 'Construct a new building structure in Build Mode.', icon: '🔨' },
  { id: 'ach_explorer', title: 'Realm Explorer', description: 'Discover an ancient landmark or point of interest.', icon: '🧭' },
  { id: 'ach_cipher_breaker', title: 'Cipher Breaker', description: 'Decrypt an ancient encrypted terminal log.', icon: '💻' },
  { id: 'ach_new_dawn', title: 'Project New Dawn', description: 'Restore eternal harmony to Medieval Island.', icon: '✨' },
];

export const AchievementsModal: React.FC = () => {
  const { unlockedAchievements, openModal } = useGame();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md font-medieval">
      <div className="w-full max-w-lg rounded-2xl bg-[#1A0F0A] border-2 border-[#D4AF37] shadow-[0_0_35px_rgba(212,175,55,0.4)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[#D4AF37]/30 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <Trophy className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-amber-100 font-cinzel text-lg">
              Royal Achievements ({unlockedAchievements.size}/{ALL_ACHIEVEMENTS.length})
            </h3>
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
          {ALL_ACHIEVEMENTS.map((ach) => {
            const isUnlocked = unlockedAchievements.has(ach.id);

            return (
              <div
                key={ach.id}
                className={`p-3.5 rounded-xl border flex items-center justify-between gap-3 ${
                  isUnlocked
                    ? 'bg-[#2A160F] border-[#D4AF37] shadow-[0_0_10px_rgba(212,175,55,0.2)]'
                    : 'bg-black/30 border-stone-800 opacity-50'
                }`}
              >
                <div className="flex items-center gap-3">
                  <div className="text-2xl">{ach.icon}</div>
                  <div>
                    <h4 className="font-bold text-amber-100">{ach.title}</h4>
                    <p className="text-xs text-stone-400 font-almendra">{ach.description}</p>
                  </div>
                </div>

                {isUnlocked ? (
                  <span className="text-emerald-400 font-bold text-xs flex items-center gap-1 shrink-0">
                    <CheckCircle2 className="w-4 h-4" /> Unlocked
                  </span>
                ) : (
                  <span className="text-stone-500 text-xs flex items-center gap-1 shrink-0">
                    <Lock className="w-4 h-4" /> Locked
                  </span>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
