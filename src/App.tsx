import React, { useState } from 'react';
import { GameProvider, useGame } from './context/GameContext';
import { WorldCanvas } from './game3d/WorldCanvas';
import { TopGameStatsBar } from './components/TopGameStatsBar';
import { ToolSelectorDock } from './components/ToolSelectorDock';
import { MainNavigationBar, ScreenTab } from './components/MainNavigationBar';

// Screens
import { AgricultureScreen } from './screens/AgricultureScreen';
import { ManaEnergyScreen } from './screens/ManaEnergyScreen';
import { LivestockScreen } from './screens/LivestockScreen';
import { WorkshopScreen } from './screens/WorkshopScreen';
import { MarketScreen } from './screens/MarketScreen';
import { BusinessDashboardScreen } from './screens/BusinessDashboardScreen';

// Modals
import { CropPlantModal } from './components/modals/CropPlantModal';
import { CampfireModal } from './components/modals/CampfireModal';
import { FishingMiniGameModal } from './components/modals/FishingMiniGameModal';
import { TerminalHackModal } from './components/modals/TerminalHackModal';
import { NpcDialogueModal } from './components/modals/NpcDialogueModal';
import { SanctuaryModal } from './components/modals/SanctuaryModal';
import { ResearchModal } from './components/modals/ResearchModal';
import { JournalModal } from './components/modals/JournalModal';
import { AchievementsModal } from './components/modals/AchievementsModal';
import { WorldMapModal } from './components/modals/WorldMapModal';
import { TutorialModal } from './components/modals/TutorialModal';
import { SettingsModal } from './components/modals/SettingsModal';
import { EndingSequenceModal } from './components/modals/EndingSequenceModal';

const AppInner: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<ScreenTab>('WORLD_3D');
  const {
    activeModal,
    openModal,
    floatingTexts,
    notifications,
  } = useGame();

  return (
    <div className="relative w-screen h-screen overflow-hidden bg-[#1A0F0A] select-none">
      {/* Top Universal Game Stats Bar */}
      <TopGameStatsBar
        onOpenMap={() => openModal('map')}
        onOpenSettings={() => openModal('settings')}
        onOpenTutorial={() => openModal('tutorial')}
        onOpenAchievements={() => openModal('achievements')}
      />

      {/* Main View Area */}
      <main className="w-full h-full">
        {/* Always keep 3D canvas mounted for seamless experience, toggle visibility */}
        <div className={`w-full h-full ${currentTab === 'WORLD_3D' ? 'block' : 'hidden'}`}>
          <WorldCanvas />
          <ToolSelectorDock />
        </div>

        {currentTab === 'CROPS' && <AgricultureScreen />}
        {currentTab === 'ENERGY' && <ManaEnergyScreen />}
        {currentTab === 'LIVESTOCK' && <LivestockScreen />}
        {currentTab === 'WORKSHOP' && <WorkshopScreen />}
        {currentTab === 'MARKET' && <MarketScreen />}
        {currentTab === 'LEDGER' && <BusinessDashboardScreen />}
      </main>

      {/* Floating Damage/Reward Text Overlay */}
      <div className="pointer-events-none fixed inset-0 z-40 overflow-hidden">
        {floatingTexts.map((ft) => (
          <div
            key={ft.id}
            className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 font-bold font-cinzel text-lg tracking-wide drop-shadow-[0_2px_8px_rgba(0,0,0,0.9)] animate-float-up"
            style={{ color: ft.color }}
          >
            {ft.text}
          </div>
        ))}
      </div>

      {/* Toast Notification Banners */}
      <div className="pointer-events-none fixed top-16 right-4 z-40 flex flex-col gap-2 max-w-sm">
        {notifications.map((n) => (
          <div
            key={n.id}
            className="p-3.5 rounded-2xl bg-[#1A0F0A]/95 border border-[#D4AF37] shadow-[0_4px_20px_rgba(0,0,0,0.7)] backdrop-blur-md animate-slide-in pointer-events-auto"
          >
            <div className="text-xs font-bold text-amber-300 font-cinzel">{n.title}</div>
            <div className="text-[11px] text-stone-200 font-almendra mt-0.5">{n.description}</div>
          </div>
        ))}
      </div>

      {/* Active Modals */}
      {activeModal === 'plant_seed' && <CropPlantModal />}
      {activeModal === 'campfire' && <CampfireModal />}
      {activeModal === 'fishing' && <FishingMiniGameModal />}
      {activeModal === 'terminal' && <TerminalHackModal />}
      {activeModal === 'npc_dialogue' && <NpcDialogueModal />}
      {activeModal === 'sanctuary' && <SanctuaryModal />}
      {activeModal === 'research' && <ResearchModal />}
      {activeModal === 'journal' && <JournalModal />}
      {activeModal === 'achievements' && <AchievementsModal />}
      {activeModal === 'map' && <WorldMapModal />}
      {activeModal === 'tutorial' && <TutorialModal />}
      {activeModal === 'settings' && <SettingsModal />}
      {activeModal === 'ending' && <EndingSequenceModal />}

      {/* Bottom Medieval Navigation Bar */}
      <MainNavigationBar currentTab={currentTab} onTabChange={setCurrentTab} />
    </div>
  );
};

export default function App() {
  return (
    <GameProvider>
      <AppInner />
    </GameProvider>
  );
}
