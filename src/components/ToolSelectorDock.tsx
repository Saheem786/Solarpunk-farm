import React from 'react';
import { useGame } from '../context/GameContext';
import { PlayerTool, PLAYER_TOOLS } from '../types/game';
import {
  Hand,
  Droplets,
  Sparkles,
  Hammer,
  Scissors,
  Sprout,
  Fish,
} from 'lucide-react';

const TOOL_ICONS: Record<PlayerTool, React.ComponentType<{ className?: string }>> = {
  HAND: Hand,
  WATER_CAN: Droplets,
  FERTILIZER: Sparkles,
  SOLAR_WRENCH: Hammer,
  SHEARS: Scissors,
  SEED_POUCH: Sprout,
  FISHING_ROD: Fish,
};

export const ToolSelectorDock: React.FC = () => {
  const { selectedTool, setSelectedTool, isBuildMode, setIsBuildMode } = useGame();

  return (
    <div className="hud-overlay absolute bottom-20 left-1/2 -translate-x-1/2 z-20 flex items-center gap-1.5 p-1.5 rounded-2xl bg-[#1A0F0A]/90 border border-[#D4AF37]/50 shadow-[0_4px_20px_rgba(0,0,0,0.6)] backdrop-blur-md">
      {PLAYER_TOOLS.map((tool) => {
        const IconComponent = TOOL_ICONS[tool.id];
        const isSelected = selectedTool === tool.id;

        return (
          <button
            key={tool.id}
            onClick={() => {
              setSelectedTool(tool.id);
              if (tool.id === 'SOLAR_WRENCH') {
                setIsBuildMode(!isBuildMode);
              } else if (isBuildMode) {
                setIsBuildMode(false);
              }
            }}
            title={`${tool.displayName}: ${tool.description}`}
            className={`flex flex-col items-center justify-center w-11 h-11 sm:w-12 sm:h-12 rounded-xl transition-all cursor-pointer ${
              isSelected
                ? 'bg-gradient-to-t from-[#800020] to-[#A31535] text-amber-100 border-2 border-[#D4AF37] shadow-[0_0_12px_rgba(212,175,55,0.6)] scale-105'
                : 'bg-[#2A160F] text-amber-300/70 hover:text-amber-100 hover:bg-[#3E2723] border border-amber-900/50'
            }`}
          >
            <IconComponent className="w-5 h-5 sm:w-5.5 sm:h-5.5" />
            <span className="text-[8px] font-medieval mt-0.5 max-w-[42px] truncate leading-none">
              {tool.displayName}
            </span>
          </button>
        );
      })}
    </div>
  );
};
