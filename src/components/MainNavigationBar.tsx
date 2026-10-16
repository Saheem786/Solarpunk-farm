import React from 'react';
import {
  Mountain,
  Flower2,
  Sparkles,
  PawPrint,
  Hammer,
  Store,
  BookOpen,
} from 'lucide-react';

export type ScreenTab =
  | 'WORLD_3D'
  | 'CROPS'
  | 'ENERGY'
  | 'LIVESTOCK'
  | 'WORKSHOP'
  | 'MARKET'
  | 'LEDGER';

interface TabItem {
  id: ScreenTab;
  title: string;
  icon: React.ComponentType<{ className?: string }>;
}

const TABS: TabItem[] = [
  { id: 'WORLD_3D', title: 'Realm', icon: Mountain },
  { id: 'CROPS', title: 'Fields', icon: Flower2 },
  { id: 'ENERGY', title: 'Mana', icon: Sparkles },
  { id: 'LIVESTOCK', title: 'Menagerie', icon: PawPrint },
  { id: 'WORKSHOP', title: 'Forge', icon: Hammer },
  { id: 'MARKET', title: 'Bazaar', icon: Store },
  { id: 'LEDGER', title: 'Chronicles', icon: BookOpen },
];

interface MainNavigationBarProps {
  currentTab: ScreenTab;
  onTabChange: (tab: ScreenTab) => void;
}

export const MainNavigationBar: React.FC<MainNavigationBarProps> = ({ currentTab, onTabChange }) => {
  return (
    <nav className="hud-overlay absolute bottom-0 left-0 right-0 z-30 h-16 bg-[#1A0F0A] border-t-2 border-[#D4AF37]/40 flex items-center justify-around px-2 shadow-[0_-4px_20px_rgba(0,0,0,0.8)]">
      {TABS.map((tab) => {
        const Icon = tab.icon;
        const isSelected = currentTab === tab.id;

        return (
          <button
            key={tab.id}
            onClick={() => onTabChange(tab.id)}
            className={`flex flex-col items-center justify-center flex-1 h-full py-1 transition-all cursor-pointer ${
              isSelected
                ? 'text-[#D4AF37] font-bold'
                : 'text-[#CD7F32]/70 hover:text-amber-200'
            }`}
          >
            <div
              className={`p-1 rounded-xl transition-all ${
                isSelected
                  ? 'bg-[#800020] text-amber-100 shadow-[0_0_10px_rgba(128,0,32,0.8)] scale-110'
                  : ''
              }`}
            >
              <Icon className="w-5 h-5" />
            </div>
            <span
              className={`text-[10px] mt-0.5 font-medieval tracking-wide ${
                isSelected ? 'text-[#D4AF37]' : 'text-stone-400'
              }`}
            >
              {tab.title}
            </span>
          </button>
        );
      })}
    </nav>
  );
};
