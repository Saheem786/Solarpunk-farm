# Implementation Plan: Solarpunk Realistic Graphics & Animated Eco-Sanctuary

Transform the 3D environment into a full eco-futuristic Solarpunk world featuring realistic materials, lifelike animations (wind turbines, water ripples, swaying foliage, and hovering eco-drones), and authentic Solarpunk technology structures.

---

## 1. Visual Realism & Solarpunk Atmosphere Overhaul

- **Realistic PBR Lighting & Materials**:
  - Warm sun directional lighting with ambient occlusion, soft hemisphere illumination, and golden-hour tone mapping.
  - Realistic materials for architectural glass (specular highlights, refraction, transparency), metallic accents (brushed brass, white coated titanium, recycled alloy), and organic wood/bamboo finishes.
  - Realistic river water shader/material with animated uv displacement waves, foam edges, and realistic surface reflectance.
  - Atmospheric dynamic sky gradient with moving volumetric cloud wisps and night bioluminescence.

- **Solarpunk Eco-Futuristic Architecture & Village Elements**:
  - Replace medieval castle/citadel with a **Solarpunk Central Eco-Hub**: curved biophilic architecture, glass terraces with cascading hanging gardens, solar canopy, and vertical wind vanes.
  - Replace rustic barn and timber sheds with **Solarpunk Bio-Barn & Agritech Workshop**: curved white composite walls, solar glass skylights, vertical hydroponic spirals, and brass energy conduits.
  - Natural pathways with porous eco-paving, integrated solar ground tiles with soft glowing pathways at dusk.

---

## 2. Dynamic 3D Animations & Kinetic Systems

- **Kinetic Wind Generators & Aeroturbines**:
  - Smooth 360-degree blade rotation on vertical-axis helical wind spires and modern three-blade aerodynamic turbines, responsive to island breeze speed.
- **Organic Flora Swaying Animation**:
  - Procedural vertex swaying and gentle oscillation on bamboo stalks, solar sunflowers, willow trees, and vertical planters to simulate natural wind.
- **Flowing Water & Reflection Ripples**:
  - Animated multi-layer river surface with drifting surface caustics and particle foam near riverbanks.
- **Autonomous Flying Eco-Drones**:
  - 3D robotic quadcopter / bio-glider drones hovering, carrying small harvest containers, emitting soft thruster particle trails, and patrolling the farm plots along dynamic flight paths.
- **Player Character & Living Wildlife Animation**:
  - Smooth character movement with natural head-look, subtle breathing idle cycle, animated robotic companion bot hovering beside player, and dynamic footstep dust/pollen particle burst.

---

## 3. New Solarpunk Structures & Interactive Gameplay

- **Geodesic Hydroponic Dome**:
  - Multilateral glass dome with automated misting irrigation, increasing nearby crop growth speed and protecting plants from extreme weather.
- **Kinetic Solar-Tree Collector**:
  - Photovoltaic leaf canopy that dynamically tracks the sun during daytime, collecting clean kilowatt energy and storing power in integrated solid-state batteries.
- **Algae Spirulina Bioreactor**:
  - Glowing glass cylinders circulating nutrient-rich green spirulina and oxygen bubbles, producing bio-fertilizer and high-value superfoods.
- **Autonomous Eco-Drone Delivery Depot**:
  - Landing pad with robotic drone recharging port that automatically collects ripe crops or delivers trade resources to the central eco-market.

---

## 4. Execution Steps & Verification

1. **Step 1: 3D Scene Assets & Shader Enhancements** (`src/game3d/WorldCanvas.tsx`)
   - Update 3D builder functions to construct high-fidelity Solarpunk structures with curved geometries, solar glass, metal framing, and bioluminescent rings.
   - Implement animation loop hooks for turbine rotation, water UV panning, foliage swaying, and flying drones.
2. **Step 2: Catalog & Types Alignment** (`src/types/game.ts` & `src/context/GameContext.tsx`)
   - Add new Solarpunk buildables and functional perks (Solar Tree, Hydroponic Dome, Algae Bioreactor, Eco-Drone Depot).
   - Ensure clean integration with clean energy and ecological balance metrics.
3. **Step 3: Verification & Polish**
   - Run compilation and TypeScript validation (`compile_applet`).
   - Verify performance (steady 60 FPS), ensure zero DOM errors, and test all interactive prompts.
