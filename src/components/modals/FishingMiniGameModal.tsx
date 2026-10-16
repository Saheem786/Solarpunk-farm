import React, { useState, useEffect, useRef } from 'react';
import { useGame } from '../../context/GameContext';
import { FishTypeId, FISH_CATALOG } from '../../types/game';
import { X, Fish, AlertCircle, Sparkles, CheckCircle2 } from 'lucide-react';
import { sounds } from '../../audio/soundSystem';

export const FishingMiniGameModal: React.FC = () => {
  const { gameState, catchFish, openModal, addFloatingText } = useGame();

  const [stage, setStage] = useState<'CAST' | 'WAITING' | 'BITE' | 'REELING' | 'CAUGHT' | 'ESCAPED'>('CAST');
  const [biteTimer, setBiteTimer] = useState<number>(0);
  const [tension, setTension] = useState<number>(50); // 0 to 100
  const [sweetSpot, setSweetSpot] = useState<number>(50); // target center
  const [reelProgress, setReelProgress] = useState<number>(0); // 0 to 100
  const [caughtFishType, setCaughtFishType] = useState<FishTypeId>('TROUT');
  const [isPressingReel, setIsPressingReel] = useState<boolean>(false);

  // Cast rod
  const startFishing = () => {
    setStage('WAITING');
    sounds.playButtonTap();
    // Bite occurs in 2-4 seconds
    const waitMs = 2000 + Math.random() * 2000;
    setTimeout(() => {
      setStage('BITE');
      sounds.playFishingBite();
    }, waitMs);
  };

  // Hook when fish bites
  const hookFish = () => {
    if (stage !== 'BITE') return;
    sounds.playButtonTap();
    setStage('REELING');
    setTension(50);
    setReelProgress(10);

    // Pick fish type based on weights
    const roll = Math.random() * 100;
    let picked: FishTypeId = 'TROUT';
    if (roll < 40) picked = 'TROUT';
    else if (roll < 70) picked = 'BASS';
    else if (roll < 88) picked = 'CATFISH';
    else if (roll < 96) picked = 'CARP';
    else picked = 'GOLDEN_FISH';
    setCaughtFishType(picked);
  };

  // Reeling game ticker
  useEffect(() => {
    if (stage !== 'REELING') return;

    const interval = setInterval(() => {
      // Sweet spot wanders slowly
      setSweetSpot((prev) => Math.max(25, Math.min(75, prev + (Math.random() - 0.5) * 8)));

      // Tension goes up when pressing reel, drops when letting go
      setTension((prev) => {
        const delta = isPressingReel ? 4.5 : -3.5;
        const next = Math.max(0, Math.min(100, prev + delta));
        return next;
      });

      // Reel progress increases when tension is near sweet spot
      setReelProgress((prev) => {
        const inZone = Math.abs(tension - sweetSpot) < 18;
        const delta = inZone ? 2.5 : -1.5;
        const next = Math.max(0, Math.min(100, prev + delta));

        if (next >= 100) {
          setStage('CAUGHT');
          catchFish(caughtFishType, gameState.fishingRodTier);
        } else if (tension <= 5 || tension >= 95) {
          setStage('ESCAPED');
          sounds.playErrorSound();
        }

        return next;
      });
    }, 50);

    return () => clearInterval(interval);
  }, [stage, isPressingReel, tension, sweetSpot, caughtFishType, catchFish, gameState.fishingRodTier]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md font-medieval">
      <div className="w-full max-w-md rounded-2xl bg-[#1A0F0A] border-2 border-cyan-500 shadow-[0_0_35px_rgba(0,229,255,0.3)] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-cyan-900/50 bg-[#2A160F]">
          <div className="flex items-center gap-2">
            <Fish className="w-5 h-5 text-cyan-400" />
            <h3 className="font-bold text-amber-100 font-cinzel text-lg">Fishing at Island Waters</h3>
          </div>
          <button
            onClick={() => openModal(null)}
            className="p-1 rounded-lg text-stone-400 hover:text-amber-200 hover:bg-stone-800 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 text-center space-y-6">
          {stage === 'CAST' && (
            <div className="space-y-4">
              <div className="w-20 h-20 rounded-full bg-cyan-950/60 border-2 border-cyan-400 mx-auto flex items-center justify-center text-cyan-300 text-3xl shadow-inner">
                🎣
              </div>
              <div>
                <h4 className="text-lg font-bold text-amber-100 font-cinzel">Cast Thy Line</h4>
                <p className="text-xs text-stone-400 font-almendra mt-1">
                  Patience brings the bounty of the mountain rivers and deep ponds.
                </p>
              </div>
              <button
                onClick={startFishing}
                className="w-full py-3 rounded-xl bg-gradient-to-r from-cyan-600 to-emerald-500 hover:brightness-110 text-slate-950 font-bold text-sm shadow-lg cursor-pointer"
              >
                Cast Line into Waters
              </button>
            </div>
          )}

          {stage === 'WAITING' && (
            <div className="space-y-4 py-6">
              <div className="relative w-16 h-16 mx-auto flex items-center justify-center">
                <div className="absolute inset-0 rounded-full border-2 border-cyan-400/40 animate-ping" />
                <div className="w-10 h-10 rounded-full bg-red-600 border-2 border-white shadow-md animate-bounce" />
              </div>
              <p className="text-sm text-cyan-300 font-bold animate-pulse">Bobber floating... Watching for bites...</p>
            </div>
          )}

          {stage === 'BITE' && (
            <div className="space-y-4 py-4 animate-bounce">
              <div className="text-4xl">⚡</div>
              <h4 className="text-xl font-bold text-amber-300 font-cinzel">A FISH BITES!</h4>
              <button
                onClick={hookFish}
                className="w-full py-3.5 rounded-xl bg-gradient-to-r from-amber-500 to-rose-500 text-slate-950 font-bold text-base shadow-[0_0_20px_rgba(255,160,0,0.6)] animate-pulse cursor-pointer"
              >
                HOOK FISH NOW!
              </button>
            </div>
          )}

          {stage === 'REELING' && (
            <div className="space-y-4 select-none">
              <div className="flex justify-between text-xs text-stone-300">
                <span>Tension Sweet-Spot</span>
                <span className="text-cyan-400 font-bold">Progress: {Math.round(reelProgress)}%</span>
              </div>

              {/* Progress Bar */}
              <div className="w-full h-3 rounded-full bg-black/60 overflow-hidden border border-cyan-900">
                <div
                  className="h-full bg-gradient-to-r from-cyan-500 to-emerald-400 transition-all duration-75"
                  style={{ width: `${reelProgress}%` }}
                />
              </div>

              {/* Tension Gauge Track */}
              <div className="relative w-full h-12 rounded-2xl bg-black/70 border-2 border-stone-800 overflow-hidden">
                {/* Sweet Spot Target Zone */}
                <div
                  className="absolute top-0 bottom-0 bg-emerald-500/40 border-x-2 border-emerald-400"
                  style={{
                    left: `${Math.max(0, sweetSpot - 15)}%`,
                    width: '30%',
                  }}
                />

                {/* Current Tension Indicator */}
                <div
                  className="absolute top-1 bottom-1 w-4 rounded-lg bg-amber-400 border border-white shadow-[0_0_10px_rgba(255,215,0,0.8)] -translate-x-1/2 transition-all duration-75"
                  style={{ left: `${tension}%` }}
                />
              </div>

              <div className="text-[11px] text-stone-400">
                Press & Hold the Reel button to maintain tension inside the green zone!
              </div>

              <button
                onPointerDown={() => setIsPressingReel(true)}
                onPointerUp={() => setIsPressingReel(false)}
                onPointerLeave={() => setIsPressingReel(false)}
                className="w-full py-4 rounded-xl bg-gradient-to-r from-cyan-600 to-emerald-500 active:scale-95 text-slate-950 font-bold text-base shadow-lg cursor-pointer"
              >
                HOLD TO REEL IN
              </button>
            </div>
          )}

          {stage === 'CAUGHT' && (
            <div className="space-y-4 py-4">
              <div className="text-4xl animate-bounce">🎉</div>
              <h4 className="text-xl font-bold text-amber-300 font-cinzel">
                Landed {FISH_CATALOG[caughtFishType].displayName}!
              </h4>
              <p className="text-xs text-stone-300 font-almendra">
                Rarity: {FISH_CATALOG[caughtFishType].rarity} • Value: {FISH_CATALOG[caughtFishType].sellPrice} Coins
              </p>
              <div className="flex gap-2">
                <button
                  onClick={startFishing}
                  className="flex-1 py-2.5 rounded-xl bg-[#800020] text-amber-100 font-bold text-xs border border-[#D4AF37] cursor-pointer"
                >
                  Cast Again
                </button>
                <button
                  onClick={() => openModal(null)}
                  className="flex-1 py-2.5 rounded-xl bg-stone-800 text-stone-300 font-bold text-xs cursor-pointer"
                >
                  Close
                </button>
              </div>
            </div>
          )}

          {stage === 'ESCAPED' && (
            <div className="space-y-4 py-4">
              <div className="text-3xl text-rose-500">💔</div>
              <h4 className="text-lg font-bold text-rose-400 font-cinzel">The Fish Slipped Away!</h4>
              <p className="text-xs text-stone-400 font-almendra">
                The tension grew too erratic and snapped the line.
              </p>
              <button
                onClick={startFishing}
                className="w-full py-2.5 rounded-xl bg-cyan-800 text-cyan-100 font-bold text-xs cursor-pointer"
              >
                Try Again
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
