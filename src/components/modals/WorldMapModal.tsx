import React from 'react';
import { useGame } from '../../context/GameContext';
import { POINTS_OF_INTEREST, BIOMES } from '../../types/game';
import { X, Compass, MapPin, CheckCircle2, Lock } from 'lucide-react';

export const WorldMapModal: React.FC = () => {
  const { gameState, openModal } = useGame();

  // Normalize 350x350 world space (-175 to 175) to 100% canvas coordinates
  const worldToMap = (x: number, z: number) => {
    const left = ((x + 70) / 140) * 100;
    const top = ((z + 160) / 340) * 100;
    return { left: `${Math.max(5, Math.min(95, left))}%`, top: `${Math.max(5, Math.min(95, top))}%` };
  };

  const playerPos = worldToMap(gameState.playerX, gameState.playerZ);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md font-medieval">
      <div className="w-full max-w-2xl rounded-2xl bg-[#1A0F0A] border-2 border-[#D4AF37] shadow-[0_0_40px_rgba(212,175,55,0.4)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[#D4AF37]/30 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <Compass className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-amber-100 font-cinzel text-lg">Realm Cartography & Biomes</h3>
          </div>
          <button
            onClick={() => openModal(null)}
            className="p-1 rounded-lg text-stone-400 hover:text-amber-200 hover:bg-stone-800 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-4 space-y-4">
          {/* Map Canvas Visualizer */}
          <div className="relative w-full h-80 rounded-2xl bg-[#091512] border-2 border-amber-950 overflow-hidden shadow-inner">
            {/* Biome Region Bands */}
            {/* Deep Forest (Top) */}
            <div className="absolute top-0 left-0 right-0 h-[38%] bg-gradient-to-b from-[#1b5e20]/60 to-[#2e7d32]/40 border-b border-emerald-800/40 flex items-start p-2">
              <span className="text-[10px] text-emerald-300 font-bold uppercase tracking-widest bg-black/40 px-2 py-0.5 rounded">
                Deep Forest (Northern Canopies)
              </span>
            </div>

            {/* Green Valley (Center) */}
            <div className="absolute top-[38%] left-0 right-0 h-[28%] bg-gradient-to-b from-[#4caf50]/40 to-[#388e3c]/50 border-b border-emerald-900/40 flex items-center p-2">
              <span className="text-[10px] text-amber-200 font-bold uppercase tracking-widest bg-black/40 px-2 py-0.5 rounded">
                Green Valley (Citadel & Fields)
              </span>
            </div>

            {/* Wetland (Bottom) */}
            <div className="absolute bottom-0 left-0 right-0 h-[34%] bg-gradient-to-b from-[#00897b]/40 to-[#004d40]/60 flex items-end p-2">
              <span className="text-[10px] text-cyan-300 font-bold uppercase tracking-widest bg-black/40 px-2 py-0.5 rounded">
                Wetlands & River Delta (Southern Springs)
              </span>
            </div>

            {/* River Ribbon */}
            <div className="absolute left-[35%] top-[55%] bottom-0 w-8 bg-cyan-700/60 rounded-full blur-[1px] -rotate-12" />

            {/* Discovered POI Markers */}
            {POINTS_OF_INTEREST.map((poi) => {
              const isDiscovered = gameState.discoveredPois.includes(poi.id);
              const pos = worldToMap(poi.worldX, poi.worldZ);

              return (
                <div
                  key={poi.id}
                  title={`${poi.displayName} (${isDiscovered ? 'Discovered' : 'Unexplored'})`}
                  className="absolute -translate-x-1/2 -translate-y-1/2 group cursor-pointer"
                  style={pos}
                >
                  <div
                    className={`w-6 h-6 rounded-full flex items-center justify-center text-xs shadow-md border ${
                      isDiscovered
                        ? 'bg-[#D4AF37] text-slate-950 border-white'
                        : 'bg-black/60 text-stone-500 border-stone-700'
                    }`}
                  >
                    {isDiscovered ? '★' : '?'}
                  </div>
                  <div className="hidden group-hover:block absolute bottom-7 left-1/2 -translate-x-1/2 whitespace-nowrap px-2 py-1 bg-black/90 text-amber-100 text-[10px] rounded border border-[#D4AF37] z-30 pointer-events-none">
                    {isDiscovered ? poi.displayName : 'Unknown Landmark'}
                  </div>
                </div>
              );
            })}

            {/* Player Location Marker */}
            <div
              className="absolute -translate-x-1/2 -translate-y-1/2 z-20 flex items-center justify-center"
              style={playerPos}
            >
              <div className="w-4 h-4 rounded-full bg-rose-500 border-2 border-white shadow-[0_0_12px_rgba(244,63,94,0.9)] animate-pulse" />
              <span className="absolute -top-5 text-[9px] font-bold text-rose-300 bg-black/80 px-1 rounded">
                Thy Position
              </span>
            </div>
          </div>

          {/* Footer stats */}
          <div className="flex justify-between items-center text-xs text-stone-400 pt-2 border-t border-amber-900/30">
            <span>
              Discovered Landmarks: <strong className="text-amber-300">{gameState.discoveredPois.length}</strong> / 15
            </span>
            <span>
              Player Coordinates: ({Math.round(gameState.playerX)}, {Math.round(gameState.playerZ)})
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
