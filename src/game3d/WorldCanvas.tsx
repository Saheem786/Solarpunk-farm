import React, { useEffect, useRef, useState, useCallback } from 'react';
import * as THREE from 'three';
import { useGame } from '../context/GameContext';
import {
  PlotEntity,
  LivestockEntity,
  PlacedBuildingEntity,
  BUILDABLE_CATALOG,
  CROP_CATALOG,
  POINTS_OF_INTEREST,
  FISHING_SPOTS,
} from '../types/game';

interface WorldCanvasProps {
  onPromptInteract?: (promptType: string, targetId: string | number) => void;
}

export const WorldCanvas: React.FC<WorldCanvasProps> = ({ onPromptInteract }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const {
    gameState,
    plots,
    livestock,
    placedBuildings,
    npcs,
    selectedTool,
    selectedSeedForPouch,
    isBuildMode,
    selectedBuildType,
    setSelectedBuildType,
    buildRotationDeg,
    setBuildRotationDeg,
    waterPlot,
    harvestPlot,
    fertilizePlot,
    openModal,
    setSelectedPlotForModal,
    setSelectedAnimalForModal,
    setSelectedNpcForDialogue,
    discoverPoiById,
    drinkWater,
    restAtCabin,
    placeBuilding,
    setIsBuildMode,
    addFloatingText,
  } = useGame();

  // Active interaction prompt overlay
  const [activePrompt, setActivePrompt] = useState<{
    text: string;
    subText?: string;
    icon: string;
    type: string;
    targetId: string | number;
  } | null>(null);

  // References for render loop & 3D objects
  const sceneRef = useRef<THREE.Scene | null>(null);
  const cameraRef = useRef<THREE.PerspectiveCamera | null>(null);
  const rendererRef = useRef<THREE.WebGLRenderer | null>(null);
  const playerGroupRef = useRef<THREE.Group | null>(null);
  const ghostGroupRef = useRef<THREE.Group | null>(null);
  const dirLightRef = useRef<THREE.DirectionalLight | null>(null);
  const ambientLightRef = useRef<THREE.AmbientLight | null>(null);
  const hemiLightRef = useRef<THREE.HemisphereLight | null>(null);
  const skyMeshRef = useRef<THREE.Mesh | null>(null);
  const starsRef = useRef<THREE.Points | null>(null);
  const windmillBladesRef = useRef<THREE.Group[]>([]);
  const beeGroupsRef = useRef<THREE.Group[]>([]);
  const animalMeshesRef = useRef<Map<number, THREE.Group>>(new Map());
  const plotMeshesRef = useRef<Map<number, THREE.Group>>(new Map());
  const buildingMeshesRef = useRef<Map<number, THREE.Group>>(new Map());

  // Solarpunk Kinetic & Animation Refs
  const dronesRef = useRef<
    {
      group: THREE.Group;
      rotors: THREE.Mesh[];
      speed: number;
      phase: number;
      radiusX: number;
      radiusZ: number;
      height: number;
      centerX: number;
      centerZ: number;
    }[]
  >([]);
  const companionDroneRef = useRef<{ group: THREE.Group; rotors: THREE.Mesh[] } | null>(null);
  const swayingFloraRef = useRef<{ group: THREE.Group; origRotZ: number; phase: number }[]>([]);
  const riverMeshRef = useRef<THREE.Mesh | null>(null);
  const riverPosOrigRef = useRef<Float32Array | null>(null);
  const solarTreesRef = useRef<THREE.Group[]>([]);
  const bioluminescentElementsRef = useRef<THREE.MeshStandardMaterial[]>([]);

  // Input states
  const keysRef = useRef<{ [key: string]: boolean }>({});
  const playerPosRef = useRef<THREE.Vector3>(new THREE.Vector3(gameState.playerX, 0, gameState.playerZ));
  const playerAngleRef = useRef<number>(gameState.playerAngle);
  const isWalkingRef = useRef<boolean>(false);
  const walkCycleRef = useRef<number>(0);
  const cameraAngleRef = useRef<{ yaw: number; pitch: number; dist: number }>({
    yaw: 0.8,
    pitch: 0.35,
    dist: 12.0,
  });
  const isDraggingRef = useRef<boolean>(false);
  const lastMousePosRef = useRef<{ x: number; y: number }>({ x: 0, y: 0 });

  // Joystick state
  const joystickCenterRef = useRef<{ x: number; y: number } | null>(null);
  const joystickVectorRef = useRef<{ x: number; y: number }>({ x: 0, y: 0 });
  const [joystickActive, setJoystickActive] = useState<boolean>(false);
  const [joystickThumb, setJoystickThumb] = useState<{ x: number; y: number }>({ x: 0, y: 0 });

  // Initialize Three.js scene
  useEffect(() => {
    if (!canvasRef.current) return;
    const canvas = canvasRef.current;
    const width = canvas.clientWidth || window.innerWidth;
    const height = canvas.clientHeight || window.innerHeight;

    // 1. Scene
    const scene = new THREE.Scene();
    sceneRef.current = scene;
    scene.background = new THREE.Color(0x81d4fa);
    scene.fog = new THREE.FogExp2(0x81d4fa, 0.007);

    // 2. Camera
    const camera = new THREE.PerspectiveCamera(45, width / height, 0.5, 500);
    cameraRef.current = camera;

    // 3. Renderer directly on React-managed canvas
    const renderer = new THREE.WebGLRenderer({
      canvas,
      antialias: true,
      powerPreference: 'high-performance',
    });
    renderer.setSize(width, height, false);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    rendererRef.current = renderer;

    // 4. Lights & Realistic Atmosphere
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.55);
    scene.add(ambientLight);
    ambientLightRef.current = ambientLight;

    // Hemisphere light: gentle sky blue dome with warm emerald grass bounce
    const hemiLight = new THREE.HemisphereLight(0xb2ebf2, 0x33691e, 0.55);
    scene.add(hemiLight);
    hemiLightRef.current = hemiLight;

    const dirLight = new THREE.DirectionalLight(0xfffaed, 1.35);
    dirLight.position.set(30, 60, 40);
    dirLight.castShadow = true;
    dirLight.shadow.mapSize.width = 2048;
    dirLight.shadow.mapSize.height = 2048;
    dirLight.shadow.camera.near = 10;
    dirLight.shadow.camera.far = 160;
    dirLight.shadow.camera.left = -60;
    dirLight.shadow.camera.right = 60;
    dirLight.shadow.camera.top = 60;
    dirLight.shadow.camera.bottom = -60;
    dirLight.shadow.bias = -0.0005;
    scene.add(dirLight);
    dirLightRef.current = dirLight;

    // Atmospheric Solarpunk Fog
    scene.fog = new THREE.FogExp2(0xd6f5ea, 0.005);

    // 5. Sky Dome & Stars
    const skyGeo = new THREE.SphereGeometry(300, 32, 16);
    const skyMat = new THREE.MeshBasicMaterial({ color: 0x81d4fa, side: THREE.BackSide });
    const skyMesh = new THREE.Mesh(skyGeo, skyMat);
    scene.add(skyMesh);
    skyMeshRef.current = skyMesh;

    // Stars particles
    const starGeo = new THREE.BufferGeometry();
    const starCount = 800;
    const starPos = new Float32Array(starCount * 3);
    for (let i = 0; i < starCount * 3; i += 3) {
      const u = Math.random();
      const v = Math.random();
      const theta = u * 2.0 * Math.PI;
      const phi = Math.acos(2.0 * v - 1.0);
      const r = 280;
      starPos[i] = r * Math.sin(phi) * Math.cos(theta);
      starPos[i + 1] = Math.abs(r * Math.cos(phi)) + 10;
      starPos[i + 2] = r * Math.sin(phi) * Math.sin(theta);
    }
    starGeo.setAttribute('position', new THREE.BufferAttribute(starPos, 3));
    const starMat = new THREE.PointsMaterial({ color: 0xffffff, size: 1.5, transparent: true, opacity: 0 });
    const stars = new THREE.Points(starGeo, starMat);
    scene.add(stars);
    starsRef.current = stars;

    // 6. Terrain: Green Valley, Forest, & Wetland
    const terrainGeo = new THREE.PlaneGeometry(350, 350, 48, 48);
    terrainGeo.rotateX(-Math.PI / 2);
    const posAttr = terrainGeo.attributes.position;
    for (let i = 0; i < posAttr.count; i++) {
      const x = posAttr.getX(i);
      const z = posAttr.getZ(i);
      let y = 0;
      // Flat farm zone in center
      if (Math.abs(x) > 25 || Math.abs(z) > 25) {
        y = Math.sin(x * 0.04) * Math.cos(z * 0.04) * 2.5 + Math.sin(x * 0.02) * 3.0;
      }
      // Low wetland river trench
      if (z < -30 && Math.abs(x + 5 + Math.sin(z * 0.05) * 15) < 14) {
        y = -1.2;
      }
      posAttr.setY(i, y);
    }
    terrainGeo.computeVertexNormals();

    const terrainMat = new THREE.MeshStandardMaterial({
      color: 0x43a047,
      roughness: 0.8,
      metalness: 0.05,
    });
    const terrainMesh = new THREE.Mesh(terrainGeo, terrainMat);
    terrainMesh.receiveShadow = true;
    scene.add(terrainMesh);

    // River Water Plane with fine resolution for animated surface waves
    const riverGeo = new THREE.PlaneGeometry(38, 170, 24, 48);
    riverGeo.rotateX(-Math.PI / 2);
    // Cache initial Y positions for ripple animation
    const riverPos = riverGeo.attributes.position;
    riverPosOrigRef.current = new Float32Array(riverPos.array);

    const riverMat = new THREE.MeshStandardMaterial({
      color: 0x00897b,
      roughness: 0.08,
      metalness: 0.85,
      transparent: true,
      opacity: 0.88,
    });
    const riverMesh = new THREE.Mesh(riverGeo, riverMat);
    riverMesh.position.set(-5, -0.55, -90);
    scene.add(riverMesh);
    riverMeshRef.current = riverMesh;

    // Solarpunk Porous Eco-Paths with glowing borders
    const pathMat = new THREE.MeshStandardMaterial({ color: 0xd7ccc8, roughness: 0.65, metalness: 0.1 });
    const pathGlowMat = new THREE.MeshStandardMaterial({
      color: 0x00e5ff,
      emissive: 0x00e5ff,
      emissiveIntensity: 0.35,
      roughness: 0.2,
    });
    bioluminescentElementsRef.current.push(pathGlowMat);

    // Main North-South Path
    const pathGeo = new THREE.PlaneGeometry(6, 40);
    pathGeo.rotateX(-Math.PI / 2);
    const pathMesh = new THREE.Mesh(pathGeo, pathMat);
    pathMesh.position.set(0, 0.03, 0);
    pathMesh.receiveShadow = true;
    scene.add(pathMesh);

    // Luminous borders for path
    const borderGeo = new THREE.PlaneGeometry(0.2, 40);
    borderGeo.rotateX(-Math.PI / 2);
    const leftBorder = new THREE.Mesh(borderGeo, pathGlowMat);
    leftBorder.position.set(-3.0, 0.035, 0);
    scene.add(leftBorder);
    const rightBorder = new THREE.Mesh(borderGeo, pathGlowMat);
    rightBorder.position.set(3.0, 0.035, 0);
    scene.add(rightBorder);

    // Cross East-West Path
    const crossPathGeo = new THREE.PlaneGeometry(38, 5);
    crossPathGeo.rotateX(-Math.PI / 2);
    const crossPath = new THREE.Mesh(crossPathGeo, pathMat);
    crossPath.position.set(0, 0.025, 0);
    crossPath.receiveShadow = true;
    scene.add(crossPath);

    // 7. Solarpunk Static Architecture & Eco-Sanctuary Elements
    createStaticCitadel(scene, bioluminescentElementsRef);
    createStaticBarn(scene, bioluminescentElementsRef);
    createStaticWorkshop(scene, bioluminescentElementsRef);
    createStaticMarket(scene, bioluminescentElementsRef);
    createStaticWell(scene, bioluminescentElementsRef);
    createCampfire(scene);
    createTreesAndFlora(scene, swayingFloraRef, bioluminescentElementsRef);
    createPoiLandmarks(scene);

    // 8. Autonomous Flying Eco-Drones & Companion Drone
    createAutonomousDrones(scene, dronesRef, bioluminescentElementsRef);
    createCompanionDrone(scene, companionDroneRef);

    // 9. Player Character Group
    const playerGroup = new THREE.Group();
    playerGroupRef.current = playerGroup;
    createPlayerCharacter(playerGroup);
    playerGroup.position.copy(playerPosRef.current);
    scene.add(playerGroup);

    // 10. Ghost Building for Build Mode
    const ghostGroup = new THREE.Group();
    ghostGroupRef.current = ghostGroup;
    ghostGroup.visible = false;
    scene.add(ghostGroup);

    // Window Resize
    const handleResize = () => {
      if (!canvasRef.current || !rendererRef.current || !cameraRef.current) return;
      const w = canvasRef.current.clientWidth || window.innerWidth;
      const h = canvasRef.current.clientHeight || window.innerHeight;
      cameraRef.current.aspect = w / h;
      cameraRef.current.updateProjectionMatrix();
      rendererRef.current.setSize(w, h, false);
    };
    window.addEventListener('resize', handleResize);

    // Cleanup
    return () => {
      window.removeEventListener('resize', handleResize);
      renderer.dispose();
    };
  }, []);

  // Update Ghost Building Mesh when in Build Mode
  useEffect(() => {
    if (!ghostGroupRef.current) return;
    const ghost = ghostGroupRef.current;
    ghost.clear();

    if (!isBuildMode) {
      ghost.visible = false;
      return;
    }

    ghost.visible = true;
    const def = BUILDABLE_CATALOG[selectedBuildType];
    if (!def) return;
    const ghostMat = new THREE.MeshStandardMaterial({
      color: new THREE.Color(def.previewColor),
      transparent: true,
      opacity: 0.55,
      roughness: 0.3,
    });

    const boxGeo = new THREE.BoxGeometry(def.size, def.size * 0.8, def.size);
    boxGeo.translate(0, (def.size * 0.8) / 2, 0);
    const box = new THREE.Mesh(boxGeo, ghostMat);
    ghost.add(box);

    // Wireframe highlight
    const wireGeo = new THREE.WireframeGeometry(boxGeo);
    const wireMat = new THREE.LineBasicMaterial({ color: 0x00e5ff });
    const wire = new THREE.LineSegments(wireGeo, wireMat);
    ghost.add(wire);
  }, [isBuildMode, selectedBuildType]);

  // Update Dynamic Crops in 3D Plots
  useEffect(() => {
    if (!sceneRef.current) return;
    const scene = sceneRef.current;

    plots.forEach((plot) => {
      let group = plotMeshesRef.current.get(plot.id);
      if (!group) {
        group = new THREE.Group();
        group.position.set(plot.posX, 0, plot.posZ);
        scene.add(group);
        plotMeshesRef.current.set(plot.id, group);
      }
      group.clear();

      // Soil Bed Frame
      const bedFrameGeo = new THREE.BoxGeometry(3.6, 0.3, 3.6);
      const bedFrameMat = new THREE.MeshStandardMaterial({ color: 0x5d4037, roughness: 0.9 });
      const bedFrame = new THREE.Mesh(bedFrameGeo, bedFrameMat);
      bedFrame.position.y = 0.15;
      bedFrame.castShadow = true;
      bedFrame.receiveShadow = true;
      group.add(bedFrame);

      // Soil Mound (moisture darkens soil)
      const soilMat = new THREE.MeshStandardMaterial({
        color: plot.moisture > 0.4 ? 0x3e2723 : 0x8d6e63,
        roughness: 0.95,
      });
      const soilGeo = new THREE.BoxGeometry(3.2, 0.32, 3.2);
      const soil = new THREE.Mesh(soilGeo, soilMat);
      soil.position.y = 0.17;
      soil.receiveShadow = true;
      group.add(soil);

      // Compost indicator sparkles
      if (plot.compostLevel > 0.5) {
        const compGeo = new THREE.SphereGeometry(0.1, 4, 4);
        const compMat = new THREE.MeshBasicMaterial({ color: 0xffd54f });
        for (let i = 0; i < 4; i++) {
          const compDot = new THREE.Mesh(compGeo, compMat);
          compDot.position.set((Math.random() - 0.5) * 2.5, 0.35, (Math.random() - 0.5) * 2.5);
          group.add(compDot);
        }
      }

      // Crop Stems & Models
      if (plot.cropType) {
        const def = CROP_CATALOG[plot.cropType];
        const stage = plot.stage;
        let cropHeight = 0.4;
        if (stage === 'SPROUT') cropHeight = 0.8;
        if (stage === 'VEGETATIVE') cropHeight = 1.3;
        if (stage === 'FLOWERING' || stage === 'HARVEST_READY') cropHeight = 1.9;

        const plantColor = new THREE.Color(def.primaryColor);
        const stemMat = new THREE.MeshStandardMaterial({ color: 0x388e3c, roughness: 0.8 });
        const fruitMat = new THREE.MeshStandardMaterial({ color: plantColor, roughness: 0.4 });

        // Multi-crop grid on the bed (4 plants)
        const offsets = [
          [-0.8, -0.8],
          [0.8, -0.8],
          [-0.8, 0.8],
          [0.8, 0.8],
        ];

        offsets.forEach(([ox, oz]) => {
          const stemGeo = new THREE.CylinderGeometry(0.04, 0.08, cropHeight, 6);
          stemGeo.translate(0, cropHeight / 2, 0);
          const stem = new THREE.Mesh(stemGeo, stemMat);
          stem.position.set(ox, 0.3, oz);
          stem.castShadow = true;
          group!.add(stem);

          if (stage === 'FLOWERING' || stage === 'HARVEST_READY') {
            const headGeo =
              plot.cropType === 'CORN' || plot.cropType === 'WHEAT'
                ? new THREE.CylinderGeometry(0.12, 0.15, 0.5, 6)
                : new THREE.SphereGeometry(plot.cropType === 'TOMATO' ? 0.22 : 0.3, 8, 8);
            const head = new THREE.Mesh(headGeo, fruitMat);
            head.position.set(ox, 0.3 + cropHeight, oz);
            head.castShadow = true;
            group!.add(head);
          }
        });
      }
    });
  }, [plots]);

  // Update Livestock in 3D
  useEffect(() => {
    if (!sceneRef.current) return;
    const scene = sceneRef.current;

    livestock.forEach((animal) => {
      let group = animalMeshesRef.current.get(animal.id);
      if (!group) {
        group = new THREE.Group();
        group.position.set(animal.posX, animal.posY, animal.posZ);
        scene.add(group);
        animalMeshesRef.current.set(animal.id, group);

        // Build Animal Visuals
        createAnimalModel(group, animal.type, animal.name);
        if (animal.type === 'ROBO_BEE_POLLINATOR') {
          beeGroupsRef.current.push(group);
        }
      }
    });
  }, [livestock]);

  // Update Placed Buildings in 3D
  useEffect(() => {
    if (!sceneRef.current) return;
    const scene = sceneRef.current;

    placedBuildings.forEach((b) => {
      let group = buildingMeshesRef.current.get(b.id);
      if (!group) {
        group = new THREE.Group();
        group.position.set(b.posX, b.posY, b.posZ);
        group.rotation.y = (b.rotationDeg * Math.PI) / 180;
        scene.add(group);
        buildingMeshesRef.current.set(b.id, group);

        createPlacedBuildingModel(group, b.buildingType, windmillBladesRef);
      }
    });
  }, [placedBuildings]);

  // Render & Physics Loop
  useEffect(() => {
    let animId: number;

    const animate = () => {
      animId = requestAnimationFrame(animate);

      // 1. Day / Night Sky & Lighting Update
      const hour = gameState.gameTimeHour;
      const isDay = hour >= 5.5 && hour <= 19.5;
      const sunRatio = (hour / 24) * Math.PI * 2 - Math.PI / 2;

      if (dirLightRef.current) {
        const sunX = Math.cos(sunRatio) * 90;
        const sunY = Math.max(-10, Math.sin(sunRatio) * 80);
        const sunZ = 50;
        dirLightRef.current.position.set(sunX, sunY, sunZ);

        // Sunlight color & intensity
        if (hour >= 6 && hour < 8) {
          // Dawn / Sunrise
          dirLightRef.current.color.setHex(0xffb347);
          dirLightRef.current.intensity = 0.9;
        } else if (hour >= 8 && hour < 17) {
          // Daytime
          dirLightRef.current.color.setHex(0xfffaed);
          dirLightRef.current.intensity = 1.3;
        } else if (hour >= 17 && hour < 20) {
          // Golden hour / Sunset
          dirLightRef.current.color.setHex(0xff8a65);
          dirLightRef.current.intensity = 1.05;
        } else {
          // Night - cool moonlight
          dirLightRef.current.color.setHex(0x5c6bc0);
          dirLightRef.current.intensity = 0.35;
        }
      }

      if (skyMeshRef.current && ambientLightRef.current) {
        const skyMat = skyMeshRef.current.material as THREE.MeshBasicMaterial;
        if (hour >= 6 && hour < 18) {
          skyMat.color.setHex(0x81d4fa);
          ambientLightRef.current.intensity = 0.65;
          if (hemiLightRef.current) hemiLightRef.current.intensity = 0.6;
          if (sceneRef.current?.fog) (sceneRef.current.fog as THREE.FogExp2).color.setHex(0xd6f5ea);
          if (starsRef.current) (starsRef.current.material as THREE.PointsMaterial).opacity = 0;
        } else if (hour >= 18 && hour < 20.5) {
          skyMat.color.setHex(0x311b92);
          ambientLightRef.current.intensity = 0.45;
          if (hemiLightRef.current) hemiLightRef.current.intensity = 0.35;
          if (sceneRef.current?.fog) (sceneRef.current.fog as THREE.FogExp2).color.setHex(0x281944);
          if (starsRef.current) (starsRef.current.material as THREE.PointsMaterial).opacity = 0.45;
        } else {
          skyMat.color.setHex(0x060914);
          ambientLightRef.current.intensity = 0.25;
          if (hemiLightRef.current) hemiLightRef.current.intensity = 0.2;
          if (sceneRef.current?.fog) (sceneRef.current.fog as THREE.FogExp2).color.setHex(0x0a1020);
          if (starsRef.current) (starsRef.current.material as THREE.PointsMaterial).opacity = 0.95;
        }
      }

      // Dynamic Bioluminescent Night Glow
      const isNightTime = hour >= 18.5 || hour < 6.0;
      const targetGlow = isNightTime ? 0.8 : 0.25;
      bioluminescentElementsRef.current.forEach((mat) => {
        mat.emissiveIntensity = THREE.MathUtils.lerp(mat.emissiveIntensity, targetGlow, 0.05);
      });

      // Flowing Water Waves on River Mesh
      if (riverMeshRef.current && riverPosOrigRef.current) {
        const geo = riverMeshRef.current.geometry;
        const pos = geo.attributes.position;
        const orig = riverPosOrigRef.current;
        const tWave = performance.now() * 0.0025;
        for (let i = 0; i < pos.count; i++) {
          const x = orig[i * 3];
          const z = orig[i * 3 + 2];
          const wave = Math.sin(z * 0.18 + tWave * 3.5) * 0.12 + Math.cos(x * 0.35 + tWave * 2.0) * 0.05;
          pos.setY(i, orig[i * 3 + 1] + wave);
        }
        pos.needsUpdate = true;
      }

      // Swaying Flora (bamboo, willow trees, sunflowers in wind)
      const tFlora = performance.now() * 0.0018;
      swayingFloraRef.current.forEach((item) => {
        item.group.rotation.z = item.origRotZ + Math.sin(tFlora * 2.2 + item.phase) * 0.045;
        item.group.rotation.x = Math.cos(tFlora * 1.6 + item.phase) * 0.025;
      });

      // Autonomous Flying Eco-Drones Animation
      dronesRef.current.forEach((drone) => {
        drone.phase += drone.speed;
        const px = drone.centerX + Math.sin(drone.phase) * drone.radiusX;
        const pz = drone.centerZ + Math.cos(drone.phase * 0.7) * drone.radiusZ;
        drone.group.position.x = px;
        drone.group.position.z = pz;
        drone.group.position.y = drone.height + Math.sin(drone.phase * 2.5) * 0.45;
        const dirX = Math.cos(drone.phase) * drone.radiusX;
        const dirZ = -Math.sin(drone.phase * 0.7) * drone.radiusZ * 0.7;
        drone.group.rotation.y = Math.atan2(dirX, dirZ);
        drone.rotors.forEach((r) => {
          r.rotation.y += 0.45;
        });
      });

      // Companion Eco-Drone hovering near player
      if (companionDroneRef.current) {
        const comp = companionDroneRef.current;
        const tComp = performance.now() * 0.002;
        const targetX = playerPosRef.current.x + Math.sin(playerAngleRef.current + 1.2) * 1.4;
        const targetZ = playerPosRef.current.z + Math.cos(playerAngleRef.current + 1.2) * 1.4;
        const targetY = 2.1 + Math.sin(tComp * 3.0) * 0.15;
        comp.group.position.lerp(new THREE.Vector3(targetX, targetY, targetZ), 0.08);
        comp.group.rotation.y = playerAngleRef.current;
        comp.rotors.forEach((r) => {
          r.rotation.y += 0.5;
        });
      }

      // Rotate windmill & aeroturbine blades
      windmillBladesRef.current.forEach((blades) => {
        blades.rotation.z += 0.045;
      });

      // Hovering robo-bees bobbing
      const time = performance.now() * 0.003;
      beeGroupsRef.current.forEach((bee, i) => {
        bee.position.y = 1.5 + Math.sin(time + i) * 0.3;
      });

      // 2. Player Movement Input
      let moveX = 0;
      let moveZ = 0;

      if (keysRef.current['KeyW'] || keysRef.current['ArrowUp']) moveZ -= 1;
      if (keysRef.current['KeyS'] || keysRef.current['ArrowDown']) moveZ += 1;
      if (keysRef.current['KeyA'] || keysRef.current['ArrowLeft']) moveX -= 1;
      if (keysRef.current['KeyD'] || keysRef.current['ArrowRight']) moveX += 1;

      // Add joystick vector
      if (joystickVectorRef.current.x !== 0 || joystickVectorRef.current.y !== 0) {
        moveX += joystickVectorRef.current.x;
        moveZ += joystickVectorRef.current.y;
      }

      const isSprinting = keysRef.current['ShiftLeft'] || keysRef.current['ShiftRight'];
      const speed = isSprinting ? 0.32 : 0.18;

      if (moveX !== 0 || moveZ !== 0) {
        isWalkingRef.current = true;
        walkCycleRef.current += 0.2;

        // Camera yaw relative movement
        const camYaw = cameraAngleRef.current.yaw;
        const forwardX = -Math.sin(camYaw);
        const forwardZ = -Math.cos(camYaw);
        const rightX = Math.cos(camYaw);
        const rightZ = -Math.sin(camYaw);

        const dirX = forwardX * -moveZ + rightX * moveX;
        const dirZ = forwardZ * -moveZ + rightZ * moveX;
        const len = Math.sqrt(dirX * dirX + dirZ * dirZ);

        if (len > 0) {
          const normX = dirX / len;
          const normZ = dirZ / len;
          playerPosRef.current.x += normX * speed;
          playerPosRef.current.z += normZ * speed;
          playerAngleRef.current = Math.atan2(normX, normZ);
        }
      } else {
        isWalkingRef.current = false;
      }

      // Update Player Group in 3D
      if (playerGroupRef.current) {
        playerGroupRef.current.position.set(playerPosRef.current.x, 0, playerPosRef.current.z);
        playerGroupRef.current.rotation.y = playerAngleRef.current;

        // Animate legs and arms while walking
        const leftLeg = playerGroupRef.current.getObjectByName('leftLeg');
        const rightLeg = playerGroupRef.current.getObjectByName('rightLeg');
        const leftArm = playerGroupRef.current.getObjectByName('leftArm');
        const rightArm = playerGroupRef.current.getObjectByName('rightArm');

        if (isWalkingRef.current && leftLeg && rightLeg && leftArm && rightArm) {
          leftLeg.rotation.x = Math.sin(walkCycleRef.current) * 0.6;
          rightLeg.rotation.x = -Math.sin(walkCycleRef.current) * 0.6;
          leftArm.rotation.x = -Math.sin(walkCycleRef.current) * 0.5;
          rightArm.rotation.x = Math.sin(walkCycleRef.current) * 0.5;
        } else if (leftLeg && rightLeg && leftArm && rightArm) {
          leftLeg.rotation.x = 0;
          rightLeg.rotation.x = 0;
          leftArm.rotation.x = 0;
          rightArm.rotation.x = 0;
        }
      }

      // 3. Ghost Building Follows in Front of Player
      if (ghostGroupRef.current && isBuildMode) {
        const offsetDist = 5.0;
        const ghostX = playerPosRef.current.x + Math.sin(playerAngleRef.current) * offsetDist;
        const ghostZ = playerPosRef.current.z + Math.cos(playerAngleRef.current) * offsetDist;
        ghostGroupRef.current.position.set(ghostX, 0, ghostZ);
        ghostGroupRef.current.rotation.y = (buildRotationDeg * Math.PI) / 180;
      }

      // 4. Third-person Camera Follow
      if (cameraRef.current) {
        const camYaw = cameraAngleRef.current.yaw;
        const camPitch = cameraAngleRef.current.pitch;
        const dist = cameraAngleRef.current.dist;

        const camX = playerPosRef.current.x + Math.sin(camYaw) * Math.cos(camPitch) * dist;
        const camY = playerPosRef.current.y + Math.sin(camPitch) * dist + 2.5;
        const camZ = playerPosRef.current.z + Math.cos(camYaw) * Math.cos(camPitch) * dist;

        cameraRef.current.position.set(camX, camY, camZ);
        cameraRef.current.lookAt(playerPosRef.current.x, playerPosRef.current.y + 1.6, playerPosRef.current.z);
      }

      // 5. Proximity Detection for Interaction Prompts
      const px = playerPosRef.current.x;
      const pz = playerPosRef.current.z;
      let closestPrompt: typeof activePrompt = null;
      let minDist = 3.5;

      // Check Plots
      plots.forEach((plot) => {
        const d = Math.hypot(px - plot.posX, pz - plot.posZ);
        if (d < minDist) {
          minDist = d;
          if (plot.stage === 'HARVEST_READY') {
            closestPrompt = {
              text: `Harvest ${CROP_CATALOG[plot.cropType!].displayName}`,
              subText: 'Press E / Tap Harvest',
              icon: 'Sprout',
              type: 'HARVEST_PLOT',
              targetId: plot.id,
            };
          } else if (plot.cropType === null) {
            closestPrompt = {
              text: 'Sow Sacred Seeds',
              subText: 'Press E / Tap Plant',
              icon: 'PlusCircle',
              type: 'PLANT_PLOT',
              targetId: plot.id,
            };
          } else {
            closestPrompt = {
              text: `Tend ${CROP_CATALOG[plot.cropType].displayName}`,
              subText: `Water: ${Math.round(plot.moisture * 100)}% | Stage: ${plot.stage}`,
              icon: 'Droplets',
              type: 'TEND_PLOT',
              targetId: plot.id,
            };
          }
        }
      });

      // Check Livestock
      livestock.forEach((animal) => {
        const d = Math.hypot(px - animal.posX, pz - animal.posZ);
        if (d < minDist) {
          minDist = d;
          closestPrompt = {
            text: animal.readyToHarvest ? `Collect ${animal.name} Produce` : `Pet ${animal.name}`,
            subText: `Happiness: ${Math.round(animal.happiness)}%`,
            icon: 'Heart',
            type: 'ANIMAL',
            targetId: animal.id,
          };
        }
      });

      // Check Sacred Well (8.0, -3.5)
      const distWell = Math.hypot(px - 8.0, pz - (-3.5));
      if (distWell < minDist) {
        minDist = distWell;
        closestPrompt = {
          text: 'Drink from Sacred Well',
          subText: 'Pure Spirit Water — Fully Hydrating',
          icon: 'Droplet',
          type: 'SACRED_WELL',
          targetId: 'well',
        };
      }

      // Check Campfire (0.0, 5.0)
      const distCamp = Math.hypot(px - 0.0, pz - 5.0);
      if (distCamp < minDist) {
        minDist = distCamp;
        closestPrompt = {
          text: 'Campfire Cooking & Hearth',
          subText: 'Boil water, roast fish, brew tea',
          icon: 'Flame',
          type: 'CAMPFIRE',
          targetId: 'campfire',
        };
      }

      // Check Fishing Spots
      Object.values(FISHING_SPOTS).forEach((spot) => {
        const d = Math.hypot(px - spot.worldX, pz - spot.worldZ);
        if (d < 5.0 && d < minDist) {
          minDist = d;
          closestPrompt = {
            text: `Cast Rod at ${spot.displayName}`,
            subText: 'Press E to Fish',
            icon: 'Fish',
            type: 'FISHING_SPOT',
            targetId: spot.id,
          };
        }
      });

      // Check POIs for Discovery
      POINTS_OF_INTEREST.forEach((poi) => {
        const d = Math.hypot(px - poi.worldX, pz - poi.worldZ);
        if (d < 6.0 && !gameState.discoveredPois.includes(poi.id)) {
          discoverPoiById(poi.id);
        }
      });

      // Check NPCs
      npcs.forEach((npc) => {
        const d = Math.hypot(px - npc.posX, pz - npc.posZ);
        if (d < minDist) {
          minDist = d;
          closestPrompt = {
            text: `Speak with ${npc.name}`,
            subText: `${npc.currentActivity} — Morale ${npc.morale}%`,
            icon: 'MessageSquare',
            type: 'NPC',
            targetId: npc.id,
          };
        }
      });

      setActivePrompt((prev) => {
        if (!closestPrompt && !prev) return null;
        if (!closestPrompt && prev) return null;
        if (closestPrompt && !prev) return closestPrompt;
        if (
          closestPrompt &&
          prev &&
          closestPrompt.type === prev.type &&
          closestPrompt.targetId === prev.targetId &&
          closestPrompt.text === prev.text
        ) {
          return prev;
        }
        return closestPrompt;
      });

      // Render Scene
      if (rendererRef.current && sceneRef.current && cameraRef.current) {
        rendererRef.current.render(sceneRef.current, cameraRef.current);
      }
    };

    animId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(animId);
  }, [gameState.gameTimeHour, gameState.discoveredPois, isBuildMode, buildRotationDeg, plots, livestock, npcs, discoverPoiById]);

  // Keyboard Event Listeners
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      keysRef.current[e.code] = true;

      // Interaction shortcut E or Space
      if (e.code === 'KeyE' || e.code === 'Space') {
        if (activePrompt) {
          handlePromptExecute(activePrompt);
        }
      }

      // Quick Build mode toggle B
      if (e.code === 'KeyB') {
        setIsBuildMode(!isBuildMode);
      }

      // Rotate build mode ghost R
      if (e.code === 'KeyR') {
        setBuildRotationDeg((prev) => (prev + 45) % 360);
      }

      // Tool shortcuts 1-7
      if (e.code === 'Digit1') setSelectedPlotForModal(null);
    };

    const handleKeyUp = (e: KeyboardEvent) => {
      keysRef.current[e.code] = false;
    };

    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
    };
  }, [activePrompt, isBuildMode]);

  // Mouse & Touch Camera Drag Orbit
  const handlePointerDown = (e: React.PointerEvent) => {
    if ((e.target as HTMLElement).closest('.hud-overlay')) return;
    isDraggingRef.current = true;
    lastMousePosRef.current = { x: e.clientX, y: e.clientY };
  };

  const handlePointerMove = (e: React.PointerEvent) => {
    if (!isDraggingRef.current) return;
    const dx = e.clientX - lastMousePosRef.current.x;
    const dy = e.clientY - lastMousePosRef.current.y;
    lastMousePosRef.current = { x: e.clientX, y: e.clientY };

    cameraAngleRef.current.yaw -= dx * 0.007;
    cameraAngleRef.current.pitch = Math.max(0.1, Math.min(1.2, cameraAngleRef.current.pitch + dy * 0.006));
  };

  const handlePointerUp = () => {
    isDraggingRef.current = false;
  };

  const handleWheel = (e: React.WheelEvent) => {
    cameraAngleRef.current.dist = Math.max(5.0, Math.min(25.0, cameraAngleRef.current.dist + e.deltaY * 0.01));
  };

  // Virtual Joystick handlers
  const handleJoystickStart = (e: React.PointerEvent<HTMLDivElement>) => {
    e.stopPropagation();
    const rect = e.currentTarget.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    joystickCenterRef.current = { x: centerX, y: centerY };
    setJoystickActive(true);
    handleJoystickMove(e);
  };

  const handleJoystickMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!joystickCenterRef.current) return;
    e.stopPropagation();
    const maxRadius = 45;
    const dx = e.clientX - joystickCenterRef.current.x;
    const dy = e.clientY - joystickCenterRef.current.y;
    const dist = Math.hypot(dx, dy);

    if (dist > maxRadius) {
      const angle = Math.atan2(dy, dx);
      const clampedX = Math.cos(angle) * maxRadius;
      const clampedY = Math.sin(angle) * maxRadius;
      setJoystickThumb({ x: clampedX, y: clampedY });
      joystickVectorRef.current = { x: clampedX / maxRadius, y: clampedY / maxRadius };
    } else {
      setJoystickThumb({ x: dx, y: dy });
      joystickVectorRef.current = { x: dx / maxRadius, y: dy / maxRadius };
    }
  };

  const handleJoystickEnd = (e: React.PointerEvent<HTMLDivElement>) => {
    e.stopPropagation();
    joystickCenterRef.current = null;
    joystickVectorRef.current = { x: 0, y: 0 };
    setJoystickActive(false);
    setJoystickThumb({ x: 0, y: 0 });
  };

  // Execute Prompt Action
  const handlePromptExecute = (prompt: NonNullable<typeof activePrompt>) => {
    if (prompt.type === 'HARVEST_PLOT') {
      harvestPlot(Number(prompt.targetId));
    } else if (prompt.type === 'PLANT_PLOT') {
      const plot = plots.find((p) => p.id === Number(prompt.targetId));
      if (plot) {
        setSelectedPlotForModal(plot);
        openModal('plant_seed');
      }
    } else if (prompt.type === 'TEND_PLOT') {
      if (selectedTool === 'WATER_CAN') {
        waterPlot(Number(prompt.targetId));
      } else if (selectedTool === 'FERTILIZER') {
        fertilizePlot(Number(prompt.targetId));
      } else {
        waterPlot(Number(prompt.targetId));
      }
    } else if (prompt.type === 'ANIMAL') {
      const animal = livestock.find((a) => a.id === Number(prompt.targetId));
      if (animal) {
        setSelectedAnimalForModal(animal);
        openModal('livestock');
      }
    } else if (prompt.type === 'SACRED_WELL') {
      drinkWater('CLEAN');
    } else if (prompt.type === 'CAMPFIRE') {
      openModal('campfire');
    } else if (prompt.type === 'FISHING_SPOT') {
      openModal('fishing');
    } else if (prompt.type === 'NPC') {
      const npc = npcs.find((n) => n.id === Number(prompt.targetId));
      if (npc) {
        setSelectedNpcForDialogue(npc);
        openModal('npc_dialogue');
      }
    }
  };

  return (
    <div
      ref={containerRef}
      className="relative w-full h-full overflow-hidden select-none touch-none bg-[#1A0F0A]"
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onWheel={handleWheel}
    >
      {/* Dedicated Three.js canvas element */}
      <canvas ref={canvasRef} className="absolute inset-0 w-full h-full block pointer-events-none" />

      {/* Dynamic Interaction Prompt Badge */}
      {activePrompt && (
        <div className="hud-overlay absolute bottom-28 left-1/2 -translate-x-1/2 z-20 pointer-events-auto">
          <button
            onClick={() => handlePromptExecute(activePrompt)}
            className="flex items-center gap-3 px-6 py-3.5 rounded-2xl bg-gradient-to-r from-[#1A0F0A]/95 to-[#2A160F]/95 border-2 border-[#D4AF37] shadow-[0_0_20px_rgba(212,175,55,0.4)] backdrop-blur-md active:scale-95 transition-transform cursor-pointer"
          >
            <div className="w-10 h-10 rounded-full bg-[#D4AF37]/20 border border-[#D4AF37] flex items-center justify-center text-[#D4AF37] font-bold text-lg">
              E
            </div>
            <div className="text-left">
              <div className="text-amber-100 font-bold text-base tracking-wide font-medieval">
                {activePrompt.text}
              </div>
              {activePrompt.subText && (
                <div className="text-amber-300/80 text-xs font-almendra">
                  {activePrompt.subText}
                </div>
              )}
            </div>
          </button>
        </div>
      )}

      {/* Build Mode Action Controls */}
      {isBuildMode && (
        <div className="hud-overlay absolute top-20 right-4 z-20 flex flex-col gap-2 max-w-xs">
          <div className="bg-[#1A0F0A]/95 border-2 border-emerald-400 p-3.5 rounded-2xl text-xs text-emerald-100 shadow-[0_0_20px_rgba(0,230,118,0.3)] backdrop-blur-md">
            <div className="flex items-center justify-between gap-2 mb-2">
              <span className="font-bold text-emerald-300 text-sm">Solarpunk Architect</span>
              <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 text-[10px] text-emerald-300 border border-emerald-400/40">
                Eco-Grid
              </span>
            </div>

            {/* Structure Switcher Carousel */}
            <div className="flex items-center justify-between gap-1 bg-black/40 p-1.5 rounded-xl border border-emerald-500/30 mb-2">
              <button
                onClick={() => {
                  const keys: (typeof selectedBuildType)[] = [
                    'HYDRO_DOME',
                    'SOLAR_TREE',
                    'BIOREACTOR',
                    'DRONE_DEPOT',
                    'WINDMILL',
                    'SOLAR_PANEL',
                    'ADVANCED_SOLAR',
                    'GREENHOUSE',
                    'WELL',
                    'WATER_PURIFIER',
                    'COMPOST_BIN',
                    'CABIN',
                    'STORAGE',
                  ];
                  const currentIdx = keys.indexOf(selectedBuildType);
                  const prevIdx = (currentIdx - 1 + keys.length) % keys.length;
                  setSelectedBuildType(keys[prevIdx]);
                }}
                className="w-7 h-7 rounded-lg bg-emerald-900/60 hover:bg-emerald-700/80 text-emerald-200 font-bold flex items-center justify-center cursor-pointer transition-colors"
                title="Previous Structure"
              >
                ◀
              </button>
              <div className="text-center flex-1 px-1">
                <div className="font-bold text-emerald-200 text-xs truncate">
                  {BUILDABLE_CATALOG[selectedBuildType]?.displayName || selectedBuildType}
                </div>
                <div className="text-[10px] text-amber-300 font-semibold">
                  {BUILDABLE_CATALOG[selectedBuildType]?.costCoins || 0} Sol Coins
                </div>
              </div>
              <button
                onClick={() => {
                  const keys: (typeof selectedBuildType)[] = [
                    'HYDRO_DOME',
                    'SOLAR_TREE',
                    'BIOREACTOR',
                    'DRONE_DEPOT',
                    'WINDMILL',
                    'SOLAR_PANEL',
                    'ADVANCED_SOLAR',
                    'GREENHOUSE',
                    'WELL',
                    'WATER_PURIFIER',
                    'COMPOST_BIN',
                    'CABIN',
                    'STORAGE',
                  ];
                  const currentIdx = keys.indexOf(selectedBuildType);
                  const nextIdx = (currentIdx + 1) % keys.length;
                  setSelectedBuildType(keys[nextIdx]);
                }}
                className="w-7 h-7 rounded-lg bg-emerald-900/60 hover:bg-emerald-700/80 text-emerald-200 font-bold flex items-center justify-center cursor-pointer transition-colors"
                title="Next Structure"
              >
                ▶
              </button>
            </div>

            <div className="text-[10px] text-emerald-200/80 mb-2 leading-tight">
              {BUILDABLE_CATALOG[selectedBuildType]?.gameplayEffect}
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => setBuildRotationDeg((r) => (r + 45) % 360)}
                className="flex-1 py-1.5 bg-slate-800/80 hover:bg-slate-700 text-emerald-200 rounded-lg text-xs font-medium border border-emerald-500/30 cursor-pointer"
              >
                Rotate ({buildRotationDeg}°)
              </button>
            </div>
          </div>

          <button
            onClick={() => {
              const offsetDist = 5.0;
              const gx = playerPosRef.current.x + Math.sin(playerAngleRef.current) * offsetDist;
              const gz = playerPosRef.current.z + Math.cos(playerAngleRef.current) * offsetDist;
              const placed = placeBuilding(selectedBuildType, gx, gz, buildRotationDeg);
              if (placed) {
                setIsBuildMode(false);
              }
            }}
            className="px-4 py-2.5 bg-gradient-to-r from-emerald-500 to-teal-400 hover:from-emerald-400 hover:to-teal-300 text-slate-950 rounded-xl text-xs font-bold border border-emerald-200 cursor-pointer shadow-[0_0_15px_rgba(0,230,118,0.4)] active:scale-95 transition-all text-center"
          >
            Deploy Structure
          </button>
          <button
            onClick={() => setIsBuildMode(false)}
            className="px-4 py-2 bg-red-950/80 hover:bg-red-900/90 text-red-200 rounded-xl text-xs font-bold border border-red-500/40 cursor-pointer text-center"
          >
            Cancel
          </button>
        </div>
      )}

      {/* Touch Virtual Joystick (Bottom Left) */}
      <div
        className="hud-overlay absolute bottom-8 left-8 w-28 h-28 rounded-full bg-black/40 border-2 border-[#D4AF37]/50 backdrop-blur-sm z-20 flex items-center justify-center touch-none pointer-events-auto select-none sm:flex"
        onPointerDown={handleJoystickStart}
        onPointerMove={handleJoystickMove}
        onPointerUp={handleJoystickEnd}
        onPointerCancel={handleJoystickEnd}
      >
        <div
          className="w-12 h-12 rounded-full bg-gradient-to-br from-[#D4AF37] to-[#CD7F32] shadow-[0_0_12px_rgba(212,175,55,0.6)] border border-amber-200 pointer-events-none transition-transform duration-75"
          style={{
            transform: `translate(${joystickThumb.x}px, ${joystickThumb.y}px)`,
          }}
        />
      </div>
    </div>
  );
};

// ==========================================
// 3D MODEL BUILDERS (Realistic Solarpunk Eco-Futuristic)
// ==========================================

function createPlayerCharacter(group: THREE.Group) {
  // Solarpunk Eco-Suit / Cyber-Linen Tunic
  const torsoGeo = new THREE.BoxGeometry(0.72, 0.95, 0.46);
  const torsoMat = new THREE.MeshStandardMaterial({
    color: 0x004d40, // Deep bio-teal
    roughness: 0.6,
    metalness: 0.2,
  });
  const torso = new THREE.Mesh(torsoGeo, torsoMat);
  torso.position.y = 1.38;
  torso.castShadow = true;
  group.add(torso);

  // Solar Thread Filigree on Chest
  const vestGeo = new THREE.BoxGeometry(0.74, 0.45, 0.48);
  const vestMat = new THREE.MeshStandardMaterial({
    color: 0xffd54f,
    roughness: 0.3,
    metalness: 0.8,
  });
  const vest = new THREE.Mesh(vestGeo, vestMat);
  vest.position.set(0, 1.55, 0);
  group.add(vest);

  // Tech Utility Belt with Solar Power Cell
  const beltGeo = new THREE.BoxGeometry(0.76, 0.14, 0.5);
  const beltMat = new THREE.MeshStandardMaterial({ color: 0x37474f, roughness: 0.4, metalness: 0.7 });
  const belt = new THREE.Mesh(beltGeo, beltMat);
  belt.position.y = 1.05;
  group.add(belt);

  // Glowing Holo-Communicator buckle
  const buckleGeo = new THREE.BoxGeometry(0.18, 0.12, 0.54);
  const buckleMat = new THREE.MeshStandardMaterial({
    color: 0x00e5ff,
    emissive: 0x00e5ff,
    emissiveIntensity: 0.6,
  });
  const buckle = new THREE.Mesh(buckleGeo, buckleMat);
  buckle.position.y = 1.05;
  group.add(buckle);

  // Head
  const headGeo = new THREE.BoxGeometry(0.44, 0.44, 0.44);
  const headMat = new THREE.MeshStandardMaterial({ color: 0xffcc80, roughness: 0.8 });
  const head = new THREE.Mesh(headGeo, headMat);
  head.position.y = 2.05;
  head.castShadow = true;
  group.add(head);

  // Lightweight AR Sun-Visor
  const visorGeo = new THREE.BoxGeometry(0.48, 0.12, 0.32);
  const visorMat = new THREE.MeshStandardMaterial({
    color: 0x00e5ff,
    transparent: true,
    opacity: 0.75,
    roughness: 0.1,
    metalness: 0.9,
    emissive: 0x00e5ff,
    emissiveIntensity: 0.4,
  });
  const visor = new THREE.Mesh(visorGeo, visorMat);
  visor.position.set(0, 2.08, 0.15);
  group.add(visor);

  // Ergonomic Solar Backpack
  const packGeo = new THREE.BoxGeometry(0.5, 0.65, 0.28);
  const packMat = new THREE.MeshStandardMaterial({ color: 0x263238, roughness: 0.5, metalness: 0.3 });
  const pack = new THREE.Mesh(packGeo, packMat);
  pack.position.set(0, 1.4, -0.32);
  pack.castShadow = true;
  group.add(pack);

  // Miniature curved solar panel on top of pack
  const packSolarGeo = new THREE.BoxGeometry(0.46, 0.04, 0.26);
  const packSolarMat = new THREE.MeshStandardMaterial({
    color: 0x00bcd4,
    roughness: 0.2,
    metalness: 0.85,
  });
  const packSolar = new THREE.Mesh(packSolarGeo, packSolarMat);
  packSolar.position.set(0, 1.74, -0.32);
  group.add(packSolar);

  // Legs with lightweight composite exoskeleton
  const legGeo = new THREE.BoxGeometry(0.22, 0.88, 0.22);
  legGeo.translate(0, -0.44, 0);
  const legMat = new THREE.MeshStandardMaterial({ color: 0x2e7d32, roughness: 0.7 });
  const leftLeg = new THREE.Mesh(legGeo, legMat);
  leftLeg.name = 'leftLeg';
  leftLeg.position.set(-0.2, 0.9, 0);
  leftLeg.castShadow = true;
  group.add(leftLeg);

  const rightLeg = new THREE.Mesh(legGeo, legMat);
  rightLeg.name = 'rightLeg';
  rightLeg.position.set(0.2, 0.9, 0);
  rightLeg.castShadow = true;
  group.add(rightLeg);

  // Arms with bio-cuffs
  const armGeo = new THREE.BoxGeometry(0.2, 0.78, 0.2);
  armGeo.translate(0, -0.39, 0);
  const armMat = new THREE.MeshStandardMaterial({ color: 0x004d40, roughness: 0.6 });
  const leftArm = new THREE.Mesh(armGeo, armMat);
  leftArm.name = 'leftArm';
  leftArm.position.set(-0.46, 1.72, 0);
  leftArm.castShadow = true;
  group.add(leftArm);

  const rightArm = new THREE.Mesh(armGeo, armMat);
  rightArm.name = 'rightArm';
  rightArm.position.set(0.46, 1.72, 0);
  rightArm.castShadow = true;
  group.add(rightArm);
}

// 1. Solarpunk Central Eco-Spire (Curved biophilic architecture, geodesic solar dome, vertical terrace gardens)
function createStaticCitadel(scene: THREE.Scene, bioluminescentRef: React.MutableRefObject<THREE.MeshStandardMaterial[]>) {
  const citadel = new THREE.Group();
  citadel.position.set(6.0, 0, 0.0);

  // Curved biophilic white composite podium
  const podiumGeo = new THREE.CylinderGeometry(5.2, 6.2, 2.8, 16);
  const whiteMat = new THREE.MeshStandardMaterial({
    color: 0xf5f5f0,
    roughness: 0.25,
    metalness: 0.1,
  });
  const podium = new THREE.Mesh(podiumGeo, whiteMat);
  podium.position.y = 1.4;
  podium.castShadow = true;
  podium.receiveShadow = true;
  citadel.add(podium);

  // Circular Tiered Garden Ring (Cascading Greenery)
  const gardenRingGeo = new THREE.TorusGeometry(5.4, 0.45, 8, 24);
  gardenRingGeo.rotateX(Math.PI / 2);
  const gardenMat = new THREE.MeshStandardMaterial({ color: 0x2e7d32, roughness: 0.75 });
  const gardenRing = new THREE.Mesh(gardenRingGeo, gardenMat);
  gardenRing.position.y = 2.6;
  citadel.add(gardenRing);

  // Middle Glass Atrium with Spiral Vines
  const atriumGeo = new THREE.CylinderGeometry(3.6, 4.4, 3.8, 16);
  const glassMat = new THREE.MeshStandardMaterial({
    color: 0x80deea,
    roughness: 0.1,
    metalness: 0.4,
    transparent: true,
    opacity: 0.65,
  });
  const atrium = new THREE.Mesh(atriumGeo, glassMat);
  atrium.position.y = 4.7;
  atrium.castShadow = true;
  citadel.add(atrium);

  // Geodesic Photovoltaic Crown Dome
  const domeGeo = new THREE.SphereGeometry(3.8, 14, 10, 0, Math.PI * 2, 0, Math.PI / 2);
  const domeMat = new THREE.MeshStandardMaterial({
    color: 0x00bcd4,
    roughness: 0.15,
    metalness: 0.7,
    transparent: true,
    opacity: 0.75,
  });
  const dome = new THREE.Mesh(domeGeo, domeMat);
  dome.position.y = 6.6;
  citadel.add(dome);

  // Solarpunk Crown Spire & Energy Ring
  const spireGeo = new THREE.CylinderGeometry(0.12, 0.35, 4.5, 8);
  const spireMat = new THREE.MeshStandardMaterial({ color: 0xffd54f, roughness: 0.3, metalness: 0.85 });
  const spire = new THREE.Mesh(spireGeo, spireMat);
  spire.position.y = 10.5;
  citadel.add(spire);

  // Glowing Cyan Bioluminescent Ring
  const haloGeo = new THREE.TorusGeometry(1.4, 0.12, 8, 24);
  haloGeo.rotateX(Math.PI / 2);
  const haloMat = new THREE.MeshStandardMaterial({
    color: 0x00e5ff,
    emissive: 0x00e5ff,
    emissiveIntensity: 0.6,
    roughness: 0.2,
  });
  bioluminescentRef.current.push(haloMat);
  const halo = new THREE.Mesh(haloGeo, haloMat);
  halo.position.y = 10.2;
  citadel.add(halo);

  scene.add(citadel);
}

// 2. Solarpunk Bio-Barn & Agritech Hub (Curved aeroponic greenhouse, solar glass panels, dual vertical spirals)
function createStaticBarn(scene: THREE.Scene, bioluminescentRef: React.MutableRefObject<THREE.MeshStandardMaterial[]>) {
  const barn = new THREE.Group();
  barn.position.set(-12.0, 0, 10.0);

  // Curved Aerodynamic Shell
  const hullGeo = new THREE.CylinderGeometry(3.6, 4.2, 5.8, 16, 1, false, 0, Math.PI);
  hullGeo.rotateZ(Math.PI / 2);
  const hullMat = new THREE.MeshStandardMaterial({ color: 0xfafafa, roughness: 0.3, metalness: 0.15 });
  const hull = new THREE.Mesh(hullGeo, hullMat);
  hull.position.set(0, 2.6, 0);
  hull.castShadow = true;
  barn.add(hull);

  // Translucent Solar Glass Skylight Arches
  const glassGeo = new THREE.CylinderGeometry(3.65, 4.25, 4.2, 12, 1, false, 0.2, Math.PI - 0.4);
  glassGeo.rotateZ(Math.PI / 2);
  const glassMat = new THREE.MeshStandardMaterial({
    color: 0x80cbc4,
    roughness: 0.1,
    metalness: 0.6,
    transparent: true,
    opacity: 0.7,
  });
  const glass = new THREE.Mesh(glassGeo, glassMat);
  glass.position.set(0, 2.65, 0);
  barn.add(glass);

  // Vertical Aeroponic Spiral Towers on Left & Right
  [-3.8, 3.8].forEach((xOffset) => {
    const spiralGeo = new THREE.CylinderGeometry(0.65, 0.75, 4.8, 10);
    const spiralMat = new THREE.MeshStandardMaterial({ color: 0x43a047, roughness: 0.7 });
    const tower = new THREE.Mesh(spiralGeo, spiralMat);
    tower.position.set(xOffset, 2.4, 2.0);
    tower.castShadow = true;
    barn.add(tower);

    // Bamboo framing rings
    const ringGeo = new THREE.TorusGeometry(0.8, 0.08, 6, 16);
    ringGeo.rotateX(Math.PI / 2);
    const ringMat = new THREE.MeshStandardMaterial({ color: 0x8d6e63 });
    [1.2, 2.4, 3.6].forEach((y) => {
      const ring = new THREE.Mesh(ringGeo, ringMat);
      ring.position.set(xOffset, y, 2.0);
      barn.add(ring);
    });
  });

  // Glowing status band
  const bandGeo = new THREE.BoxGeometry(6.0, 0.1, 0.1);
  const bandMat = new THREE.MeshStandardMaterial({
    color: 0x00e676,
    emissive: 0x00e676,
    emissiveIntensity: 0.5,
  });
  bioluminescentRef.current.push(bandMat);
  const band = new THREE.Mesh(bandGeo, bandMat);
  band.position.set(0, 1.2, 2.85);
  barn.add(band);

  scene.add(barn);
}

// 3. Solarpunk Clean Fab-Lab & Workshop (Circular open pavilion with 3D printer hologram pedestal)
function createStaticWorkshop(scene: THREE.Scene, bioluminescentRef: React.MutableRefObject<THREE.MeshStandardMaterial[]>) {
  const forge = new THREE.Group();
  forge.position.set(0.0, 0, 14.0);

  // Circular white composite platform
  const baseGeo = new THREE.CylinderGeometry(4.0, 4.4, 0.8, 16);
  const baseMat = new THREE.MeshStandardMaterial({ color: 0x37474f, roughness: 0.4, metalness: 0.4 });
  const base = new THREE.Mesh(baseGeo, baseMat);
  base.position.y = 0.4;
  base.castShadow = true;
  forge.add(base);

  // 4 Curved Bamboo/Alloy Support Columns
  const colGeo = new THREE.CylinderGeometry(0.18, 0.22, 3.6, 8);
  const colMat = new THREE.MeshStandardMaterial({ color: 0xd7ccc8, roughness: 0.5, metalness: 0.3 });
  for (let i = 0; i < 4; i++) {
    const angle = (i * Math.PI) / 2 + Math.PI / 4;
    const col = new THREE.Mesh(colGeo, colMat);
    col.position.set(Math.cos(angle) * 3.2, 2.2, Math.sin(angle) * 3.2);
    col.castShadow = true;
    forge.add(col);
  }

  // Hexagonal Photovoltaic Canopy
  const canopyGeo = new THREE.CylinderGeometry(3.8, 4.2, 0.3, 6);
  const canopyMat = new THREE.MeshStandardMaterial({
    color: 0x0288d1,
    roughness: 0.2,
    metalness: 0.8,
  });
  const canopy = new THREE.Mesh(canopyGeo, canopyMat);
  canopy.position.y = 4.1;
  canopy.castShadow = true;
  forge.add(canopy);

  // Central Hologram Fabricator Pedestal
  const pedGeo = new THREE.CylinderGeometry(0.8, 0.9, 1.2, 10);
  const pedMat = new THREE.MeshStandardMaterial({ color: 0x455a64, roughness: 0.3, metalness: 0.6 });
  const ped = new THREE.Mesh(pedGeo, pedMat);
  ped.position.y = 1.0;
  forge.add(ped);

  // Glowing Holographic Energy Ring
  const holoGeo = new THREE.TorusGeometry(0.55, 0.05, 8, 20);
  holoGeo.rotateX(Math.PI / 2);
  const holoMat = new THREE.MeshStandardMaterial({
    color: 0x00e5ff,
    emissive: 0x00e5ff,
    emissiveIntensity: 0.7,
  });
  bioluminescentRef.current.push(holoMat);
  const holo = new THREE.Mesh(holoGeo, holoMat);
  holo.position.y = 1.7;
  forge.add(holo);

  scene.add(forge);
}

// 4. Solarpunk Eco-Market & Harvest Depot (Tensile solar sail canopy and bamboo trade counter)
function createStaticMarket(scene: THREE.Scene, bioluminescentRef: React.MutableRefObject<THREE.MeshStandardMaterial[]>) {
  const market = new THREE.Group();
  market.position.set(-14.0, 0, -14.0);

  // Organic curved bamboo counter
  const counterGeo = new THREE.BoxGeometry(4.2, 1.1, 2.2);
  const counterMat = new THREE.MeshStandardMaterial({ color: 0x8d6e63, roughness: 0.7 });
  const counter = new THREE.Mesh(counterGeo, counterMat);
  counter.position.y = 0.55;
  counter.castShadow = true;
  market.add(counter);

  // Wooden produce crates
  const crateGeo = new THREE.BoxGeometry(0.9, 0.45, 0.7);
  const crateMat = new THREE.MeshStandardMaterial({ color: 0xa1887f });
  [-1.2, 0, 1.2].forEach((x, i) => {
    const crate = new THREE.Mesh(crateGeo, crateMat);
    crate.position.set(x, 1.3, (i % 2 === 0 ? 0.2 : -0.2));
    market.add(crate);

    // Fresh produce spheres in crate
    const produceGeo = new THREE.SphereGeometry(0.12, 6, 6);
    const produceMat = new THREE.MeshStandardMaterial({
      color: i === 0 ? 0xff5252 : i === 1 ? 0xffd54f : 0x69f0ae,
    });
    for (let p = 0; p < 3; p++) {
      const prod = new THREE.Mesh(produceGeo, produceMat);
      prod.position.set(x + (p - 1) * 0.22, 1.6, (i % 2 === 0 ? 0.2 : -0.2));
      market.add(prod);
    }
  });

  // Tensile Solar Sail Canopy (Golden yellow flexible thin-film solar)
  const canopyGeo = new THREE.ConeGeometry(3.6, 1.4, 5);
  const canopyMat = new THREE.MeshStandardMaterial({
    color: 0xffb300,
    roughness: 0.35,
    metalness: 0.5,
  });
  const canopy = new THREE.Mesh(canopyGeo, canopyMat);
  canopy.position.y = 3.6;
  canopy.castShadow = true;
  market.add(canopy);

  // Holographic pricing badge
  const badgeGeo = new THREE.PlaneGeometry(0.8, 0.4);
  const badgeMat = new THREE.MeshStandardMaterial({
    color: 0x00e5ff,
    emissive: 0x00e5ff,
    emissiveIntensity: 0.5,
    transparent: true,
    opacity: 0.85,
  });
  bioluminescentRef.current.push(badgeMat);
  const badge = new THREE.Mesh(badgeGeo, badgeMat);
  badge.position.set(0, 2.4, 1.15);
  market.add(badge);

  scene.add(market);
}

// 5. Atmospheric Moisture Condensation Spire & Well (Sleek vertical helix tower with pure water pool)
function createStaticWell(scene: THREE.Scene, bioluminescentRef: React.MutableRefObject<THREE.MeshStandardMaterial[]>) {
  const well = new THREE.Group();
  well.position.set(8.0, 0, -3.5);

  // White quartz catchment basin
  const baseGeo = new THREE.CylinderGeometry(1.6, 1.8, 0.9, 16);
  const baseMat = new THREE.MeshStandardMaterial({ color: 0xe0e0e0, roughness: 0.3, metalness: 0.1 });
  const base = new THREE.Mesh(baseGeo, baseMat);
  base.position.y = 0.45;
  base.castShadow = true;
  well.add(base);

  // Pure Crystalline Water Pool
  const waterGeo = new THREE.CylinderGeometry(1.4, 1.4, 0.1, 16);
  const waterMat = new THREE.MeshStandardMaterial({
    color: 0x00e5ff,
    roughness: 0.05,
    metalness: 0.9,
    transparent: true,
    opacity: 0.85,
  });
  const water = new THREE.Mesh(waterGeo, waterMat);
  water.position.y = 0.88;
  well.add(water);

  // Central Vertical Condensation Core (glass tube with internal spiral)
  const coreGeo = new THREE.CylinderGeometry(0.4, 0.4, 3.8, 12);
  const coreMat = new THREE.MeshStandardMaterial({
    color: 0xb2ebf2,
    roughness: 0.1,
    metalness: 0.3,
    transparent: true,
    opacity: 0.6,
  });
  const core = new THREE.Mesh(coreGeo, coreMat);
  core.position.y = 2.6;
  well.add(core);

  // Atmospheric Catchment Rings
  const ringGeo = new THREE.TorusGeometry(0.9, 0.08, 6, 16);
  ringGeo.rotateX(Math.PI / 2);
  const ringMat = new THREE.MeshStandardMaterial({ color: 0x00bcd4, roughness: 0.2, metalness: 0.8 });
  [1.8, 2.8, 3.8].forEach((y) => {
    const ring = new THREE.Mesh(ringGeo, ringMat);
    ring.position.y = y;
    well.add(ring);
  });

  // Aqua bioluminescent base light
  const glowGeo = new THREE.TorusGeometry(1.7, 0.06, 6, 20);
  glowGeo.rotateX(Math.PI / 2);
  const glowMat = new THREE.MeshStandardMaterial({
    color: 0x00e5ff,
    emissive: 0x00e5ff,
    emissiveIntensity: 0.6,
  });
  bioluminescentRef.current.push(glowMat);
  const glow = new THREE.Mesh(glowGeo, glowMat);
  glow.position.y = 0.1;
  well.add(glow);

  scene.add(well);
}

// 6. Solar Bio-Plasma Hearth (Clean catalytic plasma bowl with warm golden glow)
function createCampfire(scene: THREE.Scene) {
  const campfire = new THREE.Group();
  campfire.position.set(0.0, 0, 5.0);

  // Sculptural brass and stone bowl
  const bowlGeo = new THREE.CylinderGeometry(1.2, 0.7, 0.5, 12);
  const bowlMat = new THREE.MeshStandardMaterial({ color: 0x37474f, roughness: 0.4, metalness: 0.6 });
  const bowl = new THREE.Mesh(bowlGeo, bowlMat);
  bowl.position.y = 0.25;
  bowl.castShadow = true;
  campfire.add(bowl);

  // Polished Copper Inset Ring
  const ringGeo = new THREE.TorusGeometry(1.05, 0.08, 6, 16);
  ringGeo.rotateX(Math.PI / 2);
  const ringMat = new THREE.MeshStandardMaterial({ color: 0xb87333, metalness: 0.85, roughness: 0.2 });
  const ring = new THREE.Mesh(ringGeo, ringMat);
  ring.position.y = 0.5;
  campfire.add(ring);

  // Levitating Bio-Plasma Core (polyhedral crystal flame)
  const coreGeo = new THREE.OctahedronGeometry(0.38, 1);
  const coreMat = new THREE.MeshBasicMaterial({ color: 0xffa726 });
  const core = new THREE.Mesh(coreGeo, coreMat);
  core.position.y = 0.75;
  campfire.add(core);

  // Soft warm ambient firelight
  const fireLight = new THREE.PointLight(0xffb74d, 2.0, 14);
  fireLight.position.y = 1.0;
  campfire.add(fireLight);

  scene.add(campfire);
}

// 7. Solarpunk Trees & Swaying Flora (Willow trees, tall bamboo, solar sunflowers, and night-glowing mushrooms)
function createTreesAndFlora(
  scene: THREE.Scene,
  swayingFloraRef: React.MutableRefObject<{ group: THREE.Group; origRotZ: number; phase: number }[]>,
  bioluminescentRef: React.MutableRefObject<THREE.MeshStandardMaterial[]>
) {
  const treePositions = [
    [-18, -12], [-22, -6], [-16, -2], [-22, 4], [-18, 12],
    [10, 6], [12, 14], [-6, -16], [4, -18], [-2, -22],
    [-35, 50], [-15, 48], [10, 52], [32, 55], [-45, 70],
    [-25, 68], [-8, 72], [15, 70], [38, 75], [55, 80],
    [-50, 95], [-28, 98], [-2, 92], [25, 96], [48, 102],
    [-30, -55], [-10, -50], [15, -52], [35, -58], [-45, -75]
  ];

  treePositions.forEach(([x, z], idx) => {
    const tree = new THREE.Group();
    tree.position.set(x, 0, z);

    // Natural curved trunk
    const trunkGeo = new THREE.CylinderGeometry(0.28, 0.5, 2.8, 8);
    const trunkMat = new THREE.MeshStandardMaterial({ color: 0x5d4037, roughness: 0.85 });
    const trunk = new THREE.Mesh(trunkGeo, trunkMat);
    trunk.position.y = 1.4;
    trunk.castShadow = true;
    tree.add(trunk);

    // Lush Layered Solarpunk Foliage (spherical bio-canopy clusters)
    const leafColor = z > 30 ? 0x1b5e20 : z < -30 ? 0x00695c : (idx % 2 === 0 ? 0x388e3c : 0x43a047);
    const leavesMat = new THREE.MeshStandardMaterial({
      color: leafColor,
      roughness: 0.65,
    });

    const crownGroup = new THREE.Group();
    crownGroup.position.y = 2.8;

    // Multi-sphere bio-canopy
    const offsets = [
      [0, 0.9, 0, 1.8],
      [-0.8, 0.4, 0.4, 1.3],
      [0.8, 0.5, -0.4, 1.4],
      [0.2, 0.3, 0.8, 1.2],
      [-0.4, 0.6, -0.7, 1.3],
    ];
    offsets.forEach(([ox, oy, oz, r]) => {
      const sphereGeo = new THREE.DodecahedronGeometry(r, 1);
      const sphere = new THREE.Mesh(sphereGeo, leavesMat);
      sphere.position.set(ox, oy, oz);
      sphere.castShadow = true;
      crownGroup.add(sphere);
    });
    tree.add(crownGroup);

    scene.add(tree);
    swayingFloraRef.current.push({
      group: tree,
      origRotZ: 0,
      phase: idx * 0.4,
    });
  });

  // Radiant Solar Sunflowers
  const sunflowerPositions = [
    [-6, 3], [-8, 2], [-5, 6], [14, 2], [15, 5], [14, -6]
  ];
  sunflowerPositions.forEach(([sx, sz], sIdx) => {
    const flower = new THREE.Group();
    flower.position.set(sx, 0, sz);

    // Stalk
    const stalkGeo = new THREE.CylinderGeometry(0.05, 0.08, 1.8, 6);
    const stalkMat = new THREE.MeshStandardMaterial({ color: 0x2e7d32, roughness: 0.7 });
    const stalk = new THREE.Mesh(stalkGeo, stalkMat);
    stalk.position.y = 0.9;
    flower.add(stalk);

    // Golden Photovoltaic Petals
    const headGeo = new THREE.CylinderGeometry(0.42, 0.42, 0.1, 10);
    headGeo.rotateX(Math.PI / 4);
    const headMat = new THREE.MeshStandardMaterial({ color: 0xffc107, roughness: 0.3 });
    const head = new THREE.Mesh(headGeo, headMat);
    head.position.set(0, 1.8, 0);
    flower.add(head);

    // Concentrator Center (glowing core)
    const centerGeo = new THREE.SphereGeometry(0.2, 8, 8);
    const centerMat = new THREE.MeshStandardMaterial({
      color: 0xff6f00,
      emissive: 0xff6f00,
      emissiveIntensity: 0.4,
    });
    bioluminescentRef.current.push(centerMat);
    const center = new THREE.Mesh(centerGeo, centerMat);
    center.position.set(0, 1.82, 0.05);
    flower.add(center);

    scene.add(flower);
    swayingFloraRef.current.push({
      group: flower,
      origRotZ: 0,
      phase: sIdx * 0.7,
    });
  });

  // Bioluminescent Night Mushrooms near river & shaded trees
  const shroomPositions = [
    [-4, -26], [-8, -32], [2, -28], [-14, -8], [-12, -4], [8, -14]
  ];
  shroomPositions.forEach(([mx, mz]) => {
    const shroom = new THREE.Group();
    shroom.position.set(mx, 0, mz);

    const stemGeo = new THREE.CylinderGeometry(0.06, 0.08, 0.45, 6);
    const stemMat = new THREE.MeshStandardMaterial({ color: 0xe0f7fa });
    const stem = new THREE.Mesh(stemGeo, stemMat);
    stem.position.y = 0.22;
    shroom.add(stem);

    // Translucent Glowing Cap
    const capGeo = new THREE.SphereGeometry(0.22, 8, 8, 0, Math.PI * 2, 0, Math.PI / 2);
    const capMat = new THREE.MeshStandardMaterial({
      color: 0x00e5ff,
      emissive: 0x00e5ff,
      emissiveIntensity: 0.6,
      transparent: true,
      opacity: 0.85,
    });
    bioluminescentRef.current.push(capMat);
    const cap = new THREE.Mesh(capGeo, capMat);
    cap.position.y = 0.45;
    shroom.add(cap);

    scene.add(shroom);
  });
}

// 8. Autonomous Flying Eco-Drones (Patrol quadcopters with spinning rotors & headlights)
function createAutonomousDrones(
  scene: THREE.Scene,
  dronesRef: React.MutableRefObject<
    {
      group: THREE.Group;
      rotors: THREE.Mesh[];
      speed: number;
      phase: number;
      radiusX: number;
      radiusZ: number;
      height: number;
      centerX: number;
      centerZ: number;
    }[]
  >,
  bioluminescentRef: React.MutableRefObject<THREE.MeshStandardMaterial[]>
) {
  const droneConfigs = [
    { speed: 0.014, phase: 0, radiusX: 24, radiusZ: 18, height: 7.5, centerX: 0, centerZ: 0 },
    { speed: 0.011, phase: Math.PI, radiusX: 32, radiusZ: 26, height: 9.0, centerX: -6, centerZ: -10 },
  ];

  droneConfigs.forEach((cfg) => {
    const droneGroup = new THREE.Group();
    const rotors: THREE.Mesh[] = [];

    // Aerodynamic Central Pod Body
    const podGeo = new THREE.SphereGeometry(0.45, 10, 8);
    podGeo.scale(1.2, 0.5, 1.4);
    const podMat = new THREE.MeshStandardMaterial({
      color: 0xf5f5f5,
      roughness: 0.2,
      metalness: 0.6,
    });
    const pod = new THREE.Mesh(podGeo, podMat);
    pod.castShadow = true;
    droneGroup.add(pod);

    // Solar Wing Trim on top
    const solarWingGeo = new THREE.BoxGeometry(0.7, 0.04, 0.9);
    const solarWingMat = new THREE.MeshStandardMaterial({
      color: 0x00bcd4,
      roughness: 0.1,
      metalness: 0.85,
    });
    const solarWing = new THREE.Mesh(solarWingGeo, solarWingMat);
    solarWing.position.y = 0.22;
    droneGroup.add(solarWing);

    // 4 Carbon-Alloy Rotor Arms
    const armPositions = [
      [0.65, 0.65],
      [-0.65, 0.65],
      [0.65, -0.65],
      [-0.65, -0.65],
    ];
    armPositions.forEach(([ax, az]) => {
      const armGeo = new THREE.CylinderGeometry(0.03, 0.03, 0.9, 6);
      armGeo.rotateZ(Math.PI / 2);
      const armMat = new THREE.MeshStandardMaterial({ color: 0x37474f, metalness: 0.8 });
      const arm = new THREE.Mesh(armGeo, armMat);
      arm.position.set(ax / 2, 0.05, az / 2);
      arm.lookAt(new THREE.Vector3(ax, 0.05, az));
      droneGroup.add(arm);

      // Motor Hub
      const hubGeo = new THREE.CylinderGeometry(0.08, 0.08, 0.12, 8);
      const hubMat = new THREE.MeshStandardMaterial({ color: 0x455a64 });
      const hub = new THREE.Mesh(hubGeo, hubMat);
      hub.position.set(ax, 0.1, az);
      droneGroup.add(hub);

      // Spinning 2-Blade Propeller
      const propGeo = new THREE.BoxGeometry(0.65, 0.02, 0.08);
      const propMat = new THREE.MeshStandardMaterial({ color: 0x00e5ff, transparent: true, opacity: 0.85 });
      const prop = new THREE.Mesh(propGeo, propMat);
      prop.position.set(ax, 0.18, az);
      droneGroup.add(prop);
      rotors.push(prop);
    });

    // Glowing Cyan Headlight
    const lightGeo = new THREE.SphereGeometry(0.12, 6, 6);
    const lightMat = new THREE.MeshStandardMaterial({
      color: 0x00e5ff,
      emissive: 0x00e5ff,
      emissiveIntensity: 0.9,
    });
    bioluminescentRef.current.push(lightMat);
    const light = new THREE.Mesh(lightGeo, lightMat);
    light.position.set(0, -0.1, 0.6);
    droneGroup.add(light);

    // Soft downward spot lighting
    const downLight = new THREE.PointLight(0x00e5ff, 0.8, 8);
    downLight.position.set(0, -0.2, 0.3);
    droneGroup.add(downLight);

    droneGroup.position.set(cfg.centerX, cfg.height, cfg.centerZ);
    scene.add(droneGroup);

    dronesRef.current.push({
      group: droneGroup,
      rotors,
      speed: cfg.speed,
      phase: cfg.phase,
      radiusX: cfg.radiusX,
      radiusZ: cfg.radiusZ,
      height: cfg.height,
      centerX: cfg.centerX,
      centerZ: cfg.centerZ,
    });
  });
}

// 9. Player's Personal Companion Drone
function createCompanionDrone(
  scene: THREE.Scene,
  companionRef: React.MutableRefObject<{ group: THREE.Group; rotors: THREE.Mesh[] } | null>
) {
  const group = new THREE.Group();
  const rotors: THREE.Mesh[] = [];

  // Orb body
  const orbGeo = new THREE.SphereGeometry(0.24, 10, 8);
  const orbMat = new THREE.MeshStandardMaterial({
    color: 0xffffff,
    roughness: 0.2,
    metalness: 0.5,
  });
  const orb = new THREE.Mesh(orbGeo, orbMat);
  group.add(orb);

  // Cyan Eye Lens
  const eyeGeo = new THREE.CylinderGeometry(0.08, 0.08, 0.06, 10);
  eyeGeo.rotateX(Math.PI / 2);
  const eyeMat = new THREE.MeshStandardMaterial({
    color: 0x00e5ff,
    emissive: 0x00e5ff,
    emissiveIntensity: 0.8,
  });
  const eye = new THREE.Mesh(eyeGeo, eyeMat);
  eye.position.set(0, 0.02, 0.22);
  group.add(eye);

  // Top Aerofoil Rotor
  const rotorGeo = new THREE.BoxGeometry(0.42, 0.02, 0.06);
  const rotorMat = new THREE.MeshStandardMaterial({ color: 0x00e5ff, transparent: true, opacity: 0.75 });
  const rotor = new THREE.Mesh(rotorGeo, rotorMat);
  rotor.position.y = 0.3;
  group.add(rotor);
  rotors.push(rotor);

  scene.add(group);
  companionRef.current = { group, rotors };
}

function createPoiLandmarks(scene: THREE.Scene) {
  // Ancient Solar Communication Spire (45, 0, 120)
  const tower = new THREE.Group();
  tower.position.set(45, 0, 120);
  const towerGeo = new THREE.CylinderGeometry(0.35, 2.0, 22, 6);
  const towerMat = new THREE.MeshStandardMaterial({ color: 0xb0bec5, wireframe: true });
  const towerMesh = new THREE.Mesh(towerGeo, towerMat);
  towerMesh.position.y = 11;
  tower.add(towerMesh);

  // Solar dishes on spire
  const dishGeo = new THREE.SphereGeometry(1.6, 10, 8, 0, Math.PI * 2, 0, Math.PI / 3);
  const dishMat = new THREE.MeshStandardMaterial({ color: 0x00bcd4, roughness: 0.2, metalness: 0.8 });
  const dish = new THREE.Mesh(dishGeo, dishMat);
  dish.position.set(0, 18, 0);
  dish.rotation.x = Math.PI / 4;
  tower.add(dish);
  scene.add(tower);

  // Ancient Sacred Bio-Banyan Tree (-15, 0, -140)
  const banyan = new THREE.Group();
  banyan.position.set(-15, 0, -140);
  const banyanTrunkGeo = new THREE.CylinderGeometry(2.0, 3.8, 9, 8);
  const banyanTrunkMat = new THREE.MeshStandardMaterial({ color: 0x3e2723, roughness: 0.9 });
  const banyanTrunk = new THREE.Mesh(banyanTrunkGeo, banyanTrunkMat);
  banyanTrunk.position.y = 4.5;
  banyan.add(banyanTrunk);
  const banyanCrownGeo = new THREE.SphereGeometry(8, 12, 10);
  const banyanCrownMat = new THREE.MeshStandardMaterial({ color: 0x004d40, roughness: 0.75 });
  const crown = new THREE.Mesh(banyanCrownGeo, banyanCrownMat);
  crown.position.y = 12;
  banyan.add(crown);
  scene.add(banyan);
}

function createAnimalModel(group: THREE.Group, type: string, name: string) {
  if (type === 'CHICKEN') {
    const bodyGeo = new THREE.SphereGeometry(0.35, 8, 8);
    const bodyMat = new THREE.MeshStandardMaterial({ color: 0xffffff });
    const body = new THREE.Mesh(bodyGeo, bodyMat);
    body.position.y = 0.4;
    body.castShadow = true;
    group.add(body);

    const combGeo = new THREE.ConeGeometry(0.08, 0.2, 4);
    const combMat = new THREE.MeshStandardMaterial({ color: 0xd32f2f });
    const comb = new THREE.Mesh(combGeo, combMat);
    comb.position.set(0.15, 0.75, 0);
    group.add(comb);
  } else if (type === 'CYBER_BOVINE') {
    const bodyGeo = new THREE.BoxGeometry(1.6, 1.2, 2.4);
    const bodyMat = new THREE.MeshStandardMaterial({ color: 0xeeeeee, roughness: 0.7 });
    const body = new THREE.Mesh(bodyGeo, bodyMat);
    body.position.y = 1.3;
    body.castShadow = true;
    group.add(body);

    // Glowing cyber harness
    const harnessGeo = new THREE.BoxGeometry(1.65, 0.14, 1.8);
    const harnessMat = new THREE.MeshStandardMaterial({
      color: 0x00e676,
      emissive: 0x00e676,
      emissiveIntensity: 0.5,
    });
    const harness = new THREE.Mesh(harnessGeo, harnessMat);
    harness.position.y = 1.4;
    group.add(harness);

    const headGeo = new THREE.BoxGeometry(0.8, 0.8, 0.9);
    const head = new THREE.Mesh(headGeo, bodyMat);
    head.position.set(0, 1.6, 1.5);
    head.castShadow = true;
    group.add(head);
  } else if (type === 'SOLAR_SHEEP') {
    const fleeceGeo = new THREE.DodecahedronGeometry(0.9, 1);
    const fleeceMat = new THREE.MeshStandardMaterial({ color: 0xfff9c4, roughness: 0.9 });
    const fleece = new THREE.Mesh(fleeceGeo, fleeceMat);
    fleece.position.y = 0.9;
    fleece.castShadow = true;
    group.add(fleece);
  } else if (type === 'ROBO_BEE_POLLINATOR') {
    const beeGeo = new THREE.SphereGeometry(0.2, 6, 6);
    const beeMat = new THREE.MeshStandardMaterial({ color: 0xffd700, emissive: 0xffa000, emissiveIntensity: 0.4 });
    const bee = new THREE.Mesh(beeGeo, beeMat);
    group.add(bee);
  } else {
    const alpacaGeo = new THREE.BoxGeometry(0.9, 1.0, 1.6);
    const alpacaMat = new THREE.MeshStandardMaterial({ color: 0xd7ccc8 });
    const alpaca = new THREE.Mesh(alpacaGeo, alpacaMat);
    alpaca.position.y = 1.0;
    group.add(alpaca);
  }
}

// 10. Placed Solarpunk Buildings (Windmills, Solar Trees, Hydroponic Domes, Bioreactors, Drone Depots)
function createPlacedBuildingModel(
  group: THREE.Group,
  type: string,
  bladesRef: React.MutableRefObject<THREE.Group[]>
) {
  if (type === 'WINDMILL') {
    // Sleek Aerodynamic Wind Turbine
    const towerGeo = new THREE.CylinderGeometry(0.35, 0.85, 9.5, 12);
    const towerMat = new THREE.MeshStandardMaterial({ color: 0xf5f5f5, roughness: 0.25, metalness: 0.4 });
    const tower = new THREE.Mesh(towerGeo, towerMat);
    tower.position.y = 4.75;
    tower.castShadow = true;
    group.add(tower);

    // Nacelle (aerodynamic generator head)
    const nacelleGeo = new THREE.BoxGeometry(0.9, 0.7, 1.8);
    const nacelleMat = new THREE.MeshStandardMaterial({ color: 0x37474f, roughness: 0.3, metalness: 0.6 });
    const nacelle = new THREE.Mesh(nacelleGeo, nacelleMat);
    nacelle.position.set(0, 9.6, 0.2);
    group.add(nacelle);

    // 3 Aerodynamic Twisted Blades
    const rotorGroup = new THREE.Group();
    rotorGroup.position.set(0, 9.6, 1.15);
    group.add(rotorGroup);
    bladesRef.current.push(rotorGroup);

    // Nose Cone
    const coneGeo = new THREE.ConeGeometry(0.35, 0.6, 10);
    coneGeo.rotateX(Math.PI / 2);
    const cone = new THREE.Mesh(coneGeo, nacelleMat);
    cone.position.set(0, 0, 0.3);
    rotorGroup.add(cone);

    for (let i = 0; i < 3; i++) {
      const angle = (i * Math.PI * 2) / 3;
      const bladeGeo = new THREE.BoxGeometry(0.24, 4.4, 0.05);
      bladeGeo.translate(0, 2.2, 0);
      const bladeMat = new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.2 });
      const blade = new THREE.Mesh(bladeGeo, bladeMat);
      blade.rotation.z = angle;
      rotorGroup.add(blade);
    }
  } else if (type === 'SOLAR_TREE') {
    // Sculptural Biomimetic Solar Tree
    const trunkGeo = new THREE.CylinderGeometry(0.35, 0.65, 4.2, 8);
    const trunkMat = new THREE.MeshStandardMaterial({ color: 0x455a64, roughness: 0.4, metalness: 0.6 });
    const trunk = new THREE.Mesh(trunkGeo, trunkMat);
    trunk.position.y = 2.1;
    trunk.castShadow = true;
    group.add(trunk);

    // Branching Photovoltaic Hexagonal Petals
    const canopy = new THREE.Group();
    canopy.position.y = 4.2;
    group.add(canopy);

    const petalAngles = [0, (Math.PI * 2) / 5, (Math.PI * 4) / 5, (Math.PI * 6) / 5, (Math.PI * 8) / 5];
    petalAngles.forEach((ang) => {
      const branchGeo = new THREE.CylinderGeometry(0.08, 0.12, 1.8, 6);
      branchGeo.rotateZ(Math.PI / 3);
      const branchMat = new THREE.MeshStandardMaterial({ color: 0x37474f, metalness: 0.7 });
      const branch = new THREE.Mesh(branchGeo, branchMat);
      branch.rotation.y = ang;
      canopy.add(branch);

      // Hexagonal Solar Leaf
      const leafGeo = new THREE.CylinderGeometry(0.85, 0.85, 0.06, 6);
      const leafMat = new THREE.MeshStandardMaterial({
        color: 0x00bcd4,
        roughness: 0.15,
        metalness: 0.85,
        emissive: 0x006064,
        emissiveIntensity: 0.2,
      });
      const leaf = new THREE.Mesh(leafGeo, leafMat);
      leaf.position.set(Math.cos(ang) * 1.5, 0.8, Math.sin(ang) * 1.5);
      leaf.rotation.x = Math.PI / 6;
      canopy.add(leaf);
    });
  } else if (type === 'HYDRO_DOME') {
    // Geodesic Hydroponic Dome
    const baseGeo = new THREE.CylinderGeometry(3.6, 3.8, 0.6, 16);
    const baseMat = new THREE.MeshStandardMaterial({ color: 0xf5f5f5, roughness: 0.3 });
    const base = new THREE.Mesh(baseGeo, baseMat);
    base.position.y = 0.3;
    group.add(base);

    // Faceted Geodesic Translucent Dome
    const domeGeo = new THREE.SphereGeometry(3.4, 14, 10, 0, Math.PI * 2, 0, Math.PI / 2);
    const domeMat = new THREE.MeshStandardMaterial({
      color: 0x80deea,
      roughness: 0.1,
      metalness: 0.4,
      transparent: true,
      opacity: 0.65,
    });
    const dome = new THREE.Mesh(domeGeo, domeMat);
    dome.position.y = 0.6;
    group.add(dome);

    // Internal Concentric Hydroponic Spiral with Green Plants
    const spiralGeo = new THREE.CylinderGeometry(0.5, 0.7, 2.6, 8);
    const spiralMat = new THREE.MeshStandardMaterial({ color: 0x00e676 });
    const spiral = new THREE.Mesh(spiralGeo, spiralMat);
    spiral.position.y = 1.6;
    group.add(spiral);
  } else if (type === 'BIOREACTOR') {
    // Algae Spirulina Bioreactor (Twin glass tubes with glowing green spirulina fluid)
    const baseGeo = new THREE.BoxGeometry(2.6, 0.5, 1.4);
    const baseMat = new THREE.MeshStandardMaterial({ color: 0x263238, roughness: 0.3, metalness: 0.8 });
    const base = new THREE.Mesh(baseGeo, baseMat);
    base.position.y = 0.25;
    group.add(base);

    // Twin Cylindrical Glass Tubes
    [-0.7, 0.7].forEach((x) => {
      const tubeGeo = new THREE.CylinderGeometry(0.48, 0.48, 3.2, 16);
      const tubeMat = new THREE.MeshStandardMaterial({
        color: 0x00e676,
        roughness: 0.1,
        metalness: 0.2,
        transparent: true,
        opacity: 0.85,
        emissive: 0x00e676,
        emissiveIntensity: 0.55,
      });
      const tube = new THREE.Mesh(tubeGeo, tubeMat);
      tube.position.set(x, 1.9, 0);
      group.add(tube);

      // Brass End Caps
      const capGeo = new THREE.CylinderGeometry(0.52, 0.52, 0.2, 16);
      const capMat = new THREE.MeshStandardMaterial({ color: 0xb87333, metalness: 0.9, roughness: 0.2 });
      const topCap = new THREE.Mesh(capGeo, capMat);
      topCap.position.set(x, 3.55, 0);
      group.add(topCap);
    });
  } else if (type === 'DRONE_DEPOT') {
    // Autonomous Eco-Drone Delivery Depot
    const padGeo = new THREE.CylinderGeometry(3.0, 3.2, 0.4, 8);
    const padMat = new THREE.MeshStandardMaterial({ color: 0x37474f, roughness: 0.3, metalness: 0.6 });
    const pad = new THREE.Mesh(padGeo, padMat);
    pad.position.y = 0.2;
    group.add(pad);

    // Illuminated Holographic Landing Ring
    const ringGeo = new THREE.TorusGeometry(2.4, 0.08, 6, 24);
    ringGeo.rotateX(Math.PI / 2);
    const ringMat = new THREE.MeshStandardMaterial({
      color: 0x7c4dff,
      emissive: 0x7c4dff,
      emissiveIntensity: 0.8,
    });
    const ring = new THREE.Mesh(ringGeo, ringMat);
    ring.position.y = 0.42;
    group.add(ring);

    // Docked Mini Patrol Drone on pad
    const miniDroneGeo = new THREE.BoxGeometry(0.8, 0.22, 0.8);
    const miniDroneMat = new THREE.MeshStandardMaterial({ color: 0xffffff });
    const miniDrone = new THREE.Mesh(miniDroneGeo, miniDroneMat);
    miniDrone.position.set(0, 0.55, 0);
    group.add(miniDrone);
  } else if (type === 'SOLAR_PANEL' || type === 'ADVANCED_SOLAR') {
    // High-Efficiency Photovoltaic Tracker Array
    const standGeo = new THREE.CylinderGeometry(0.12, 0.16, 1.2, 8);
    const stand = new THREE.Mesh(standGeo, new THREE.MeshStandardMaterial({ color: 0x455a64, metalness: 0.7 }));
    stand.position.y = 0.6;
    group.add(stand);

    const panelGeo = new THREE.BoxGeometry(2.4, 0.08, 1.8);
    const panelMat = new THREE.MeshStandardMaterial({
      color: 0x00bcd4,
      roughness: 0.15,
      metalness: 0.85,
    });
    const panel = new THREE.Mesh(panelGeo, panelMat);
    panel.position.set(0, 1.25, 0);
    panel.rotation.x = Math.PI / 6;
    panel.castShadow = true;
    group.add(panel);
  } else if (type === 'GREENHOUSE') {
    // Curved Bioclimatic Glass Conservatory
    const baseGeo = new THREE.BoxGeometry(4.8, 0.4, 4.0);
    const base = new THREE.Mesh(baseGeo, new THREE.MeshStandardMaterial({ color: 0xf5f5f5 }));
    base.position.y = 0.2;
    group.add(base);

    const archGeo = new THREE.CylinderGeometry(2.2, 2.2, 4.4, 12, 1, false, 0, Math.PI);
    archGeo.rotateZ(Math.PI / 2);
    const archMat = new THREE.MeshStandardMaterial({
      color: 0x80deea,
      roughness: 0.1,
      metalness: 0.3,
      transparent: true,
      opacity: 0.7,
    });
    const arch = new THREE.Mesh(archGeo, archMat);
    arch.position.set(0, 2.2, 0);
    group.add(arch);
  } else {
    // Solarpunk Eco-Cabin with Living Green Roof
    const boxGeo = new THREE.BoxGeometry(3.6, 2.4, 3.6);
    const boxMat = new THREE.MeshStandardMaterial({ color: 0xfafafa, roughness: 0.35, metalness: 0.1 });
    const box = new THREE.Mesh(boxGeo, boxMat);
    box.position.y = 1.2;
    box.castShadow = true;
    group.add(box);

    // Curved Living Moss & Solar Roof
    const roofGeo = new THREE.CylinderGeometry(2.2, 2.2, 3.8, 10, 1, false, 0, Math.PI);
    roofGeo.rotateZ(Math.PI / 2);
    const roofMat = new THREE.MeshStandardMaterial({ color: 0x388e3c, roughness: 0.8 });
    const roof = new THREE.Mesh(roofGeo, roofMat);
    roof.position.y = 2.4;
    group.add(roof);

    // Solar Skylight
    const skylightGeo = new THREE.BoxGeometry(2.6, 0.08, 1.2);
    const skylightMat = new THREE.MeshStandardMaterial({ color: 0x00bcd4, metalness: 0.8 });
    const skylight = new THREE.Mesh(skylightGeo, skylightMat);
    skylight.position.set(0, 3.52, 0);
    group.add(skylight);
  }
}
