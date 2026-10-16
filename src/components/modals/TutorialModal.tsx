import React, { useState } from 'react';
import { useGame } from '../../context/GameContext';
import { X, HelpCircle, ChevronRight, ChevronLeft, Check } from 'lucide-react';

const STEPS = [
  {
    title: 'Welcome to Medieval Island',
    content:
      'Thou art the architect of a sovereign medieval realm combining sustainable agriculture and clean arcane mana. Move freely using WASD keys or the virtual joystick on the lower left.',
    icon: '🏰',
  },
  {
    title: 'Agriculture & Soil Blessing',
    content:
      'Tend the 6 plots in Green Valley. Equip thy Blessing Vessel (Water Can) to hydrate dry soil, enrich with compost, and sow heritage seeds. Harvest golden grains when ripe!',
    icon: '🌾',
  },
  {
    title: 'Regenerative Menagerie',
    content:
      'Pet and feed thy animals (Golden Sheep, Royal Cows, Hens, Mystic Bees). When full and happy, they produce divine wool, sacred milk, eggs, and forest honey.',
    icon: '🐑',
  },
  {
    title: 'Ancient Mana & Crafting',
    content:
      'Construct mana stones and aeolian wind spires. Balance energy consumption between kingdom devices. Forge legendary fabrics, power stones, and tools at the Workshop.',
    icon: '⚡',
  },
  {
    title: 'Exploration & Project New Dawn',
    content:
      'Explore Deep Forest and Wetlands to discover all 15 POIs, catch wild fish, decrypt ancient terminals with password SOLARIS, and unlock the New Dawn epilogue!',
    icon: '✨',
  },
];

export const TutorialModal: React.FC = () => {
  const { openModal } = useGame();
  const [currentStep, setCurrentStep] = useState(0);

  const step = STEPS[currentStep];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md font-medieval">
      <div className="w-full max-w-md rounded-2xl bg-[#1A0F0A] border-2 border-[#D4AF37] shadow-[0_0_35px_rgba(212,175,55,0.4)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[#D4AF37]/30 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <HelpCircle className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-amber-100 font-cinzel text-lg">
              Guide to Thy Realm ({currentStep + 1}/{STEPS.length})
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
        <div className="p-6 text-center space-y-4">
          <div className="text-5xl">{step.icon}</div>
          <h4 className="text-xl font-bold text-amber-300 font-cinzel">{step.title}</h4>
          <p className="text-xs text-stone-300 font-almendra leading-relaxed px-2">
            {step.content}
          </p>

          {/* Dots */}
          <div className="flex justify-center gap-1.5 pt-2">
            {STEPS.map((_, idx) => (
              <div
                key={idx}
                className={`w-2.5 h-2.5 rounded-full transition-all ${
                  currentStep === idx ? 'bg-[#D4AF37] scale-125' : 'bg-stone-700'
                }`}
              />
            ))}
          </div>

          {/* Nav Buttons */}
          <div className="flex justify-between items-center pt-4 border-t border-amber-900/30">
            <button
              onClick={() => setCurrentStep((prev) => Math.max(0, prev - 1))}
              disabled={currentStep === 0}
              className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-[#2A160F] text-amber-200 text-xs disabled:opacity-30 cursor-pointer"
            >
              <ChevronLeft className="w-4 h-4" /> Previous
            </button>

            {currentStep < STEPS.length - 1 ? (
              <button
                onClick={() => setCurrentStep((prev) => prev + 1)}
                className="flex items-center gap-1 px-4 py-1.5 rounded-lg bg-gradient-to-r from-[#D4AF37] to-[#FFA000] text-slate-950 font-bold text-xs shadow-md cursor-pointer"
              >
                Next <ChevronRight className="w-4 h-4" />
              </button>
            ) : (
              <button
                onClick={() => openModal(null)}
                className="flex items-center gap-1 px-4 py-1.5 rounded-lg bg-[#00E676] text-slate-950 font-bold text-xs shadow-md cursor-pointer"
              >
                Begin Adventure <Check className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
