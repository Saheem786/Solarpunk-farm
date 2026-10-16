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
    buildRotationDeg,
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
  const skyMeshRef = useRef<THREE.Mesh | null>(null);
  const starsRef = useRef<THREE.Points | null>(null);
  const windmillBladesRef = useRef<THREE.Group[]>([]);
  const beeGroupsRef = useRef<THREE.Group[]>([]);
  const animalMeshesRef = useRef<Map<number, THREE.Group>>(new Map());
  const plotMeshesRef = useRef<Map<number, THREE.Group>>(new Map());
  const buildingMeshesRef = useRef<Map<number, THREE.Group>>(new Map());

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
    if (!containerRef.current) return;
    const container = containerRef.current;
    const width = container.clientWidth;
    const height = container.clientHeight;

    // 1. Scene
    const scene = new THREE.Scene();
    sceneRef.current = scene;
    scene.background = new THREE.Color(0x81d4fa);
    scene.fog = new THREE.FogExp2(0x81d4fa, 0.007);

    // 2. Camera
    const camera = new THREE.PerspectiveCamera(45, width / height, 0.5, 500);
    cameraRef.current = camera;

    // 3. Renderer
    const renderer = new THREE.WebGLRenderer({ antialias: true, powerPreference: 'high-performance' });
    renderer.setSize(width, height);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    container.innerHTML = '';
    container.appendChild(renderer.domElement);
    rendererRef.current = renderer;

    // 4. Lights
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.7);
    scene.add(ambientLight);
    ambientLightRef.current = ambientLight;

    const dirLight = new THREE.DirectionalLight(0xfffaed, 1.2);
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
    scene.add(dirLight);
    dirLightRef.current = dirLight;

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

    // 6. Terrain: Green Valley, Deep Forest, Wetland
    // Main terrain plane
    const terrainGeo = new THREE.PlaneGeometry(350, 350, 48, 48);
    terrainGeo.rotateX(-Math.PI / 2);
    // Vertex displacement for gentle medieval hills
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
      color: 0x4caf50,
      roughness: 0.85,
      metalness: 0.1,
    });
    const terrainMesh = new THREE.Mesh(terrainGeo, terrainMat);
    terrainMesh.receiveShadow = true;
    scene.add(terrainMesh);

    // River Water Plane
    const riverGeo = new THREE.PlaneGeometry(35, 160);
    riverGeo.rotateX(-Math.PI / 2);
    const riverMat = new THREE.MeshStandardMaterial({
      color: 0x00897b,
      roughness: 0.1,
      metalness: 0.8,
      transparent: true,
      opacity: 0.88,
    });
    const riverMesh = new THREE.Mesh(riverGeo, riverMat);
    riverMesh.position.set(-5, -0.6, -90);
    scene.add(riverMesh);

    // Farm Dirt Paths
    const pathGeo = new THREE.PlaneGeometry(6, 40);
    pathGeo.rotateX(-Math.PI / 2);
    const pathMat = new THREE.MeshStandardMaterial({ color: 0x8d6e63, roughness: 0.9 });
    const pathMesh = new THREE.Mesh(pathGeo, pathMat);
    pathMesh.position.set(0, 0.03, 0);
    pathMesh.receiveShadow = true;
    scene.add(pathMesh);

    const crossPathGeo = new THREE.PlaneGeometry(36, 5);
    crossPathGeo.rotateX(-Math.PI / 2);
    const crossPath = new THREE.Mesh(crossPathGeo, pathMat);
    crossPath.position.set(0, 0.02, 0);
    crossPath.receiveShadow = true;
    scene.add(crossPath);

    // 7. Static Medieval Buildings
    createStaticCitadel(scene);
    createStaticBarn(scene);
    createStaticWorkshop(scene);
    createStaticMarket(scene);
    createStaticWell(scene);
    createCampfire(scene);
    createTreesAndFlora(scene);
    createPoiLandmarks(scene);

    // 8. Player Character Group
    const playerGroup = new THREE.Group();
    playerGroupRef.current = playerGroup;
    createPlayerCharacter(playerGroup);
    playerGroup.position.copy(playerPosRef.current);
    scene.add(playerGroup);

    // 9. Ghost Building for Build Mode
    const ghostGroup = new THREE.Group();
    ghostGroupRef.current = ghostGroup;
    ghostGroup.visible = false;
    scene.add(ghostGroup);

    // Window Resize
    const handleResize = () => {
      if (!containerRef.current || !rendererRef.current || !cameraRef.current) return;
      const w = containerRef.current.clientWidth;
      const h = containerRef.current.clientHeight;
      cameraRef.current.aspect = w / h;
      cameraRef.current.updateProjectionMatrix();
      rendererRef.current.setSize(w, h);
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
          dirLightRef.current.color.setHex(0xff7043);
          dirLightRef.current.intensity = 0.85;
        } else {
          // Night
          dirLightRef.current.color.setHex(0x5c6bc0);
          dirLightRef.current.intensity = 0.25;
        }
      }

      if (skyMeshRef.current && ambientLightRef.current) {
        if (hour >= 6 && hour < 18) {
          skyMeshRef.current.material = new THREE.MeshBasicMaterial({ color: 0x81d4fa, side: THREE.BackSide });
          ambientLightRef.current.intensity = 0.65;
          if (starsRef.current) (starsRef.current.material as THREE.PointsMaterial).opacity = 0;
        } else if (hour >= 18 && hour < 20.5) {
          skyMeshRef.current.material = new THREE.MeshBasicMaterial({ color: 0x4a148c, side: THREE.BackSide });
          ambientLightRef.current.intensity = 0.45;
          if (starsRef.current) (starsRef.current.material as THREE.PointsMaterial).opacity = 0.4;
        } else {
          skyMeshRef.current.material = new THREE.MeshBasicMaterial({ color: 0x080d1a, side: THREE.BackSide });
          ambientLightRef.current.intensity = 0.25;
          if (starsRef.current) (starsRef.current.material as THREE.PointsMaterial).opacity = 0.95;
        }
      }

      // Rotate windmill blades
      windmillBladesRef.current.forEach((blades) => {
        blades.rotation.z += 0.035;
      });

      // Hovering bees bobbing
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

      setActivePrompt(closestPrompt);

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
        <div className="hud-overlay absolute top-20 right-4 z-20 flex flex-col gap-2">
          <div className="bg-[#1A0F0A]/90 border border-[#D4AF37] p-3 rounded-xl text-xs text-amber-200">
            <span className="font-bold text-[#D4AF37]">Build Mode</span>: {BUILDABLE_CATALOG[selectedBuildType].displayName}
            <div className="text-[10px] text-amber-400/80">Cost: {BUILDABLE_CATALOG[selectedBuildType].costCoins} Coins</div>
          </div>
          <button
            onClick={() => setBuildRotationDeg((r) => (r + 45) % 360)}
            className="px-4 py-2 bg-[#8D6E63] hover:bg-[#A1887F] text-amber-100 rounded-lg text-xs font-bold border border-amber-300/40 cursor-pointer"
          >
            Rotate (45°)
          </button>
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
            className="px-4 py-2 bg-[#00E676] hover:bg-emerald-400 text-slate-900 rounded-lg text-xs font-bold border border-white cursor-pointer shadow-lg"
          >
            Place Structure
          </button>
          <button
            onClick={() => setIsBuildMode(false)}
            className="px-4 py-2 bg-[#800020] hover:bg-red-800 text-amber-100 rounded-lg text-xs font-bold border border-red-400 cursor-pointer"
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
// 3D MODEL BUILDERS (Procedural Medieval Low-Poly)
// ==========================================

function createPlayerCharacter(group: THREE.Group) {
  // Torso / Tunic
  const torsoGeo = new THREE.BoxGeometry(0.7, 0.9, 0.45);
  const torsoMat = new THREE.MeshStandardMaterial({ color: 0x800020, roughness: 0.7 }); // Crimson Tunic
  const torso = new THREE.Mesh(torsoGeo, torsoMat);
  torso.position.y = 1.35;
  torso.castShadow = true;
  group.add(torso);

  // Leather Belt & Buckle
  const beltGeo = new THREE.BoxGeometry(0.72, 0.14, 0.47);
  const beltMat = new THREE.MeshStandardMaterial({ color: 0x4e342e });
  const belt = new THREE.Mesh(beltGeo, beltMat);
  belt.position.y = 1.05;
  group.add(belt);

  // Head
  const headGeo = new THREE.BoxGeometry(0.45, 0.45, 0.45);
  const headMat = new THREE.MeshStandardMaterial({ color: 0xffcc80, roughness: 0.8 });
  const head = new THREE.Mesh(headGeo, headMat);
  head.position.y = 2.05;
  head.castShadow = true;
  group.add(head);

  // Medieval Hat with Feather
  const hatGeo = new THREE.ConeGeometry(0.38, 0.35, 6);
  const hatMat = new THREE.MeshStandardMaterial({ color: 0x2e7d32 });
  const hat = new THREE.Mesh(hatGeo, hatMat);
  hat.position.y = 2.38;
  group.add(hat);

  // Backpack
  const packGeo = new THREE.BoxGeometry(0.5, 0.6, 0.28);
  const packMat = new THREE.MeshStandardMaterial({ color: 0x5d4037 });
  const pack = new THREE.Mesh(packGeo, packMat);
  pack.position.set(0, 1.35, -0.32);
  pack.castShadow = true;
  group.add(pack);

  // Left Leg
  const legGeo = new THREE.BoxGeometry(0.24, 0.85, 0.24);
  legGeo.translate(0, -0.42, 0);
  const legMat = new THREE.MeshStandardMaterial({ color: 0x3e2723 });
  const leftLeg = new THREE.Mesh(legGeo, legMat);
  leftLeg.name = 'leftLeg';
  leftLeg.position.set(-0.2, 0.9, 0);
  leftLeg.castShadow = true;
  group.add(leftLeg);

  // Right Leg
  const rightLeg = new THREE.Mesh(legGeo, legMat);
  rightLeg.name = 'rightLeg';
  rightLeg.position.set(0.2, 0.9, 0);
  rightLeg.castShadow = true;
  group.add(rightLeg);

  // Arms
  const armGeo = new THREE.BoxGeometry(0.2, 0.75, 0.2);
  armGeo.translate(0, -0.37, 0);
  const armMat = new THREE.MeshStandardMaterial({ color: 0x800020 });
  const leftArm = new THREE.Mesh(armGeo, armMat);
  leftArm.name = 'leftArm';
  leftArm.position.set(-0.46, 1.7, 0);
  leftArm.castShadow = true;
  group.add(leftArm);

  const rightArm = new THREE.Mesh(armGeo, armMat);
  rightArm.name = 'rightArm';
  rightArm.position.set(0.46, 1.7, 0);
  rightArm.castShadow = true;
  group.add(rightArm);
}

function createStaticCitadel(scene: THREE.Scene) {
  const citadel = new THREE.Group();
  citadel.position.set(6.0, 0, 0.0);

  // Stone Base
  const baseGeo = new THREE.BoxGeometry(7.0, 3.5, 5.0);
  const baseMat = new THREE.MeshStandardMaterial({ color: 0x78909c, roughness: 0.9 });
  const base = new THREE.Mesh(baseGeo, baseMat);
  base.position.y = 1.75;
  base.castShadow = true;
  base.receiveShadow = true;
  citadel.add(base);

  // Timber Upper Frame & Gable Roof
  const roofGeo = new THREE.ConeGeometry(5.2, 2.5, 4);
  roofGeo.rotateY(Math.PI / 4);
  const roofMat = new THREE.MeshStandardMaterial({ color: 0x4e342e, roughness: 0.8 });
  const roof = new THREE.Mesh(roofGeo, roofMat);
  roof.position.y = 4.6;
  roof.castShadow = true;
  citadel.add(roof);

  // Chimney
  const chimGeo = new THREE.BoxGeometry(0.8, 2.8, 0.8);
  const chimMat = new THREE.MeshStandardMaterial({ color: 0x37474f, roughness: 0.95 });
  const chim = new THREE.Mesh(chimGeo, chimMat);
  chim.position.set(2.2, 4.2, 1.2);
  chim.castShadow = true;
  citadel.add(chim);

  scene.add(citadel);
}

function createStaticBarn(scene: THREE.Scene) {
  const barn = new THREE.Group();
  barn.position.set(-12.0, 0, 10.0);

  // Red Timber Body
  const bodyGeo = new THREE.BoxGeometry(7.2, 3.8, 5.6);
  const bodyMat = new THREE.MeshStandardMaterial({ color: 0x8b0000, roughness: 0.8 });
  const body = new THREE.Mesh(bodyGeo, bodyMat);
  body.position.y = 1.9;
  body.castShadow = true;
  barn.add(body);

  // Barn Gambrel Roof
  const roofGeo = new THREE.ConeGeometry(5.4, 2.2, 4);
  roofGeo.rotateY(Math.PI / 4);
  const roofMat = new THREE.MeshStandardMaterial({ color: 0xd7ccc8, roughness: 0.6 });
  const roof = new THREE.Mesh(roofGeo, roofMat);
  roof.position.y = 4.8;
  roof.castShadow = true;
  barn.add(roof);

  scene.add(barn);
}

function createStaticWorkshop(scene: THREE.Scene) {
  const forge = new THREE.Group();
  forge.position.set(0.0, 0, 14.0);

  const baseGeo = new THREE.BoxGeometry(5.8, 3.5, 4.6);
  const baseMat = new THREE.MeshStandardMaterial({ color: 0x546e7a, roughness: 0.9 });
  const base = new THREE.Mesh(baseGeo, baseMat);
  base.position.y = 1.75;
  base.castShadow = true;
  forge.add(base);

  const roofGeo = new THREE.ConeGeometry(4.4, 2.0, 4);
  roofGeo.rotateY(Math.PI / 4);
  const roofMat = new THREE.MeshStandardMaterial({ color: 0x3e2723, roughness: 0.85 });
  const roof = new THREE.Mesh(roofGeo, roofMat);
  roof.position.y = 4.4;
  roof.castShadow = true;
  forge.add(roof);

  scene.add(forge);
}

function createStaticMarket(scene: THREE.Scene) {
  const market = new THREE.Group();
  market.position.set(-14.0, 0, -14.0);

  // Market Counter
  const counterGeo = new THREE.BoxGeometry(3.6, 1.2, 2.2);
  const counterMat = new THREE.MeshStandardMaterial({ color: 0x6d4c41, roughness: 0.85 });
  const counter = new THREE.Mesh(counterGeo, counterMat);
  counter.position.y = 0.6;
  counter.castShadow = true;
  market.add(counter);

  // Canopy
  const canopyGeo = new THREE.ConeGeometry(2.6, 1.2, 6);
  const canopyMat = new THREE.MeshStandardMaterial({ color: 0xffd54f, roughness: 0.5 });
  const canopy = new THREE.Mesh(canopyGeo, canopyMat);
  canopy.position.y = 2.8;
  canopy.castShadow = true;
  market.add(canopy);

  scene.add(market);
}

function createStaticWell(scene: THREE.Scene) {
  const well = new THREE.Group();
  well.position.set(8.0, 0, -3.5);

  // Circular Stone Base
  const baseGeo = new THREE.CylinderGeometry(1.2, 1.3, 1.1, 12);
  const baseMat = new THREE.MeshStandardMaterial({ color: 0x90a4ae, roughness: 0.9 });
  const base = new THREE.Mesh(baseGeo, baseMat);
  base.position.y = 0.55;
  base.castShadow = true;
  well.add(base);

  // Wooden Roof
  const roofGeo = new THREE.ConeGeometry(1.6, 1.1, 6);
  const roofMat = new THREE.MeshStandardMaterial({ color: 0x4e342e });
  const roof = new THREE.Mesh(roofGeo, roofMat);
  roof.position.y = 2.4;
  roof.castShadow = true;
  well.add(roof);

  scene.add(well);
}

function createCampfire(scene: THREE.Scene) {
  const campfire = new THREE.Group();
  campfire.position.set(0.0, 0, 5.0);

  // Ring of stones
  for (let i = 0; i < 8; i++) {
    const angle = (i / 8) * Math.PI * 2;
    const stoneGeo = new THREE.DodecahedronGeometry(0.25);
    const stoneMat = new THREE.MeshStandardMaterial({ color: 0x424242, roughness: 0.9 });
    const stone = new THREE.Mesh(stoneGeo, stoneMat);
    stone.position.set(Math.cos(angle) * 0.9, 0.15, Math.sin(angle) * 0.9);
    campfire.add(stone);
  }

  // Wood logs
  const logGeo = new THREE.CylinderGeometry(0.1, 0.1, 1.1, 6);
  const logMat = new THREE.MeshStandardMaterial({ color: 0x3e2723 });
  for (let i = 0; i < 3; i++) {
    const log = new THREE.Mesh(logGeo, logMat);
    log.position.y = 0.2;
    log.rotation.z = Math.PI / 4;
    log.rotation.y = (i * Math.PI) / 3;
    campfire.add(log);
  }

  // Glowing Fire Core
  const fireGeo = new THREE.ConeGeometry(0.4, 0.8, 6);
  const fireMat = new THREE.MeshBasicMaterial({ color: 0xff7043 });
  const fire = new THREE.Mesh(fireGeo, fireMat);
  fire.position.y = 0.45;
  campfire.add(fire);

  const fireLight = new THREE.PointLight(0xffa726, 1.5, 12);
  fireLight.position.y = 1.0;
  campfire.add(fireLight);

  scene.add(campfire);
}

function createTreesAndFlora(scene: THREE.Scene) {
  const treePositions = [
    [-18, -12], [-22, -6], [-16, -2], [-22, 4], [-18, 12],
    [10, 6], [12, 14], [-6, -16], [4, -18], [-2, -22],
    // Deep forest dense trees
    [-35, 50], [-15, 48], [10, 52], [32, 55], [-45, 70],
    [-25, 68], [-8, 72], [15, 70], [38, 75], [55, 80],
    [-50, 95], [-28, 98], [-2, 92], [25, 96], [48, 102],
    // Wetland reeds and willows
    [-30, -55], [-10, -50], [15, -52], [35, -58], [-45, -75]
  ];

  treePositions.forEach(([x, z]) => {
    const tree = new THREE.Group();
    tree.position.set(x, 0, z);

    // Trunk
    const trunkGeo = new THREE.CylinderGeometry(0.3, 0.45, 2.5, 6);
    const trunkMat = new THREE.MeshStandardMaterial({ color: 0x4e342e, roughness: 0.9 });
    const trunk = new THREE.Mesh(trunkGeo, trunkMat);
    trunk.position.y = 1.25;
    trunk.castShadow = true;
    tree.add(trunk);

    // Canopy (3 stacked cones)
    const leavesMat = new THREE.MeshStandardMaterial({
      color: z > 30 ? 0x1b5e20 : z < -30 ? 0x00796b : 0x2e7d32,
      roughness: 0.7,
    });
    [2.8, 3.8, 4.8].forEach((y, idx) => {
      const radius = 2.2 - idx * 0.5;
      const coneGeo = new THREE.ConeGeometry(radius, 1.6, 6);
      const cone = new THREE.Mesh(coneGeo, leavesMat);
      cone.position.y = y;
      cone.castShadow = true;
      tree.add(cone);
    });

    scene.add(tree);
  });
}

function createPoiLandmarks(scene: THREE.Scene) {
  // Ancient Radio Tower (45, 0, 120)
  const tower = new THREE.Group();
  tower.position.set(45, 0, 120);
  const towerGeo = new THREE.CylinderGeometry(0.4, 2.2, 18, 4);
  const towerMat = new THREE.MeshStandardMaterial({ color: 0xb0bec5, wireframe: true });
  const towerMesh = new THREE.Mesh(towerGeo, towerMat);
  towerMesh.position.y = 9;
  tower.add(towerMesh);
  scene.add(tower);

  // Ancient Banyan Tree (-15, 0, -140)
  const banyan = new THREE.Group();
  banyan.position.set(-15, 0, -140);
  const banyanTrunkGeo = new THREE.CylinderGeometry(2.0, 3.5, 8, 8);
  const banyanTrunkMat = new THREE.MeshStandardMaterial({ color: 0x3e2723, roughness: 0.9 });
  const banyanTrunk = new THREE.Mesh(banyanTrunkGeo, banyanTrunkMat);
  banyanTrunk.position.y = 4;
  banyan.add(banyanTrunk);
  const banyanCrownGeo = new THREE.SphereGeometry(7, 8, 8);
  const banyanCrownMat = new THREE.MeshStandardMaterial({ color: 0x004d40, roughness: 0.8 });
  const crown = new THREE.Mesh(banyanCrownGeo, banyanCrownMat);
  crown.position.y = 11;
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
    const bodyMat = new THREE.MeshStandardMaterial({ color: 0xeeeeee, roughness: 0.8 });
    const body = new THREE.Mesh(bodyGeo, bodyMat);
    body.position.y = 1.3;
    body.castShadow = true;
    group.add(body);

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
    const beeMat = new THREE.MeshStandardMaterial({ color: 0xffd700 });
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

function createPlacedBuildingModel(
  group: THREE.Group,
  type: string,
  bladesRef: React.MutableRefObject<THREE.Group[]>
) {
  if (type === 'WINDMILL') {
    // Aeolian Spire tower
    const towerGeo = new THREE.CylinderGeometry(0.8, 1.8, 8, 8);
    const towerMat = new THREE.MeshStandardMaterial({ color: 0xd7ccc8, roughness: 0.8 });
    const tower = new THREE.Mesh(towerGeo, towerMat);
    tower.position.y = 4.0;
    tower.castShadow = true;
    group.add(tower);

    // Rotating Sails
    const rotorGroup = new THREE.Group();
    rotorGroup.position.set(0, 7.8, 0.9);
    group.add(rotorGroup);
    bladesRef.current.push(rotorGroup);

    for (let i = 0; i < 4; i++) {
      const sailGeo = new THREE.BoxGeometry(0.3, 3.2, 0.05);
      sailGeo.translate(0, 1.6, 0);
      const sailMat = new THREE.MeshStandardMaterial({ color: 0xffffff });
      const sail = new THREE.Mesh(sailGeo, sailMat);
      sail.rotation.z = (i * Math.PI) / 2;
      rotorGroup.add(sail);
    }
  } else if (type === 'SOLAR_PANEL' || type === 'ADVANCED_SOLAR') {
    const standGeo = new THREE.CylinderGeometry(0.1, 0.1, 1.0);
    const stand = new THREE.Mesh(standGeo, new THREE.MeshStandardMaterial({ color: 0x546e7a }));
    stand.position.y = 0.5;
    group.add(stand);

    const panelGeo = new THREE.BoxGeometry(1.8, 0.1, 1.8);
    const panelMat = new THREE.MeshStandardMaterial({ color: 0xffd54f, roughness: 0.3, metalness: 0.7 });
    const panel = new THREE.Mesh(panelGeo, panelMat);
    panel.position.set(0, 1.0, 0);
    panel.rotation.x = Math.PI / 6;
    panel.castShadow = true;
    group.add(panel);
  } else {
    // Default placed cabin / building
    const boxGeo = new THREE.BoxGeometry(3.5, 2.5, 3.5);
    const boxMat = new THREE.MeshStandardMaterial({ color: 0x8d6e63, roughness: 0.85 });
    const box = new THREE.Mesh(boxGeo, boxMat);
    box.position.y = 1.25;
    box.castShadow = true;
    group.add(box);

    const roofGeo = new THREE.ConeGeometry(2.8, 1.8, 4);
    roofGeo.rotateY(Math.PI / 4);
    const roofMat = new THREE.MeshStandardMaterial({ color: 0x4e342e });
    const roof = new THREE.Mesh(roofGeo, roofMat);
    roof.position.y = 3.4;
    group.add(roof);
  }
}
