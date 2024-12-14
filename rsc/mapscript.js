import * as THREE from 'three';
import {CSS2DObject, CSS2DRenderer} from 'CSS2D';
import {OrbitControls} from 'Orbit';

import Stats from 'Stats';

let iframe, map_spacer, map_container;
let camera, controls, scene, renderer, stats, raycaster;
let system_mesh, system_ids, systems, system_vertices;
let selection_mesh;
let jump_lines;
let labelRenderer,
    hover_label, hover_label_name, hover_label_security,
    select_label, select_label_name, select_label_security;

let lerp_target_origin = null, lerp_target_target = null,
    lerp_position_origin = null, lerp_position_target = null,
    lerp_azimuth_origin = null, lerp_azimuth_target = null,
    lerp_time = null, lerp_duration = null,
    lerp_oncomplete = null;

let selectables = {};
let selected_item = null;
let colour_mode = "security";
let system_info_jumps = {},
    system_info_jumps_max = null,
    system_info_ship_kills = {},
    system_info_ship_kills_max = null,
    system_info_npc_kills = {},
    system_info_npc_kills_max = null;

const NUMBER_FORMAT = new Intl.NumberFormat('en', {minimumFractionDigits: 1});
const SCALE = 1.0E18;
const SYSTEM_SIZE = 25 * 250 * 149_597_870_700;

function iframeUnloadHandler() {
    setTimeout(() => {
        const id = iframe.contentWindow.location.href
            .split("/")
            .pop()
            .split(".")[0];

        select_item(id);
    }, 0);
}

init();
async function init() {
    map_spacer = document.getElementById("map_spacer");
    map_container = document.getElementById("map_container");
    iframe = document.getElementById("map_frame");

    iframe.onload = () => {
        iframe.contentWindow.removeEventListener("unload", iframeUnloadHandler);
        iframe.contentWindow.addEventListener("unload", iframeUnloadHandler);
    }
    iframe.contentWindow.removeEventListener("unload", iframeUnloadHandler);
    iframe.contentWindow.addEventListener("unload", iframeUnloadHandler);

    {
        scene = new THREE.Scene();
        raycaster = new THREE.Raycaster();

        renderer = new THREE.WebGLRenderer({alpha: true});
        renderer.setSize(map_spacer.clientWidth, map_spacer.clientHeight, false);
        map_container.appendChild(renderer.domElement);

        camera = new THREE.PerspectiveCamera(35, map_spacer.clientWidth / map_spacer.clientHeight, 0.005, 5000 * (1.0E18 / SCALE));
        camera.position.y = 2.0E18 / SCALE;
        controls = new OrbitControls(camera, renderer.domElement);

        const light = new THREE.HemisphereLight(0xffffff, 0x888888, 3);
        light.position.set(0, 2.0E18 / SCALE);
        scene.add(light);

        const selection_geometry = new THREE.IcosahedronGeometry(1.5 * SYSTEM_SIZE / SCALE, 3);
        const selection_material = new THREE.MeshBasicMaterial({color: 0xffffff, side: THREE.BackSide});
        selection_mesh = new THREE.Mesh(selection_geometry, selection_material);
        selection_mesh.visible = false;
        scene.add(selection_mesh);

        stats = new Stats();
        stats.dom.style.position = "absolute";
        map_container.appendChild(stats.dom);
    }
    {
        labelRenderer = new CSS2DRenderer();
        labelRenderer.setSize(map_spacer.clientWidth, map_spacer.clientHeight, false);
        labelRenderer.domElement.style.position = 'absolute';
        labelRenderer.domElement.style.top = '0px';
        labelRenderer.domElement.style.pointerEvents = 'none'
        map_container.appendChild(labelRenderer.domElement);


        const hover_label_element = document.createElement("div");
        const hover_label_outer = document.createElement("div");
        hover_label_outer.classList.add("map_label_outer");
        hover_label_name = document.createElement("div");
        hover_label_name.classList.add("map_label_name", "font_header");
        hover_label_outer.appendChild(hover_label_name);
        hover_label_security = document.createElement("div");
        hover_label_security.classList.add("map_label_security", "font_header");
        hover_label_outer.appendChild(hover_label_security);
        hover_label_element.appendChild(hover_label_outer);

        hover_label = new CSS2DObject(hover_label_element);
        hover_label.position.set(0, 0, 0);
        hover_label.center.set(0, 1);
        hover_label.visible = false;
        scene.add(hover_label);


        const select_label_element = document.createElement("div");
        const select_label_outer = document.createElement("div");
        select_label_outer.classList.add("map_label_outer");

        select_label_name = document.createElement("div");
        select_label_name.classList.add("map_label_name", "font_header");
        select_label_outer.appendChild(select_label_name);
        select_label_security = document.createElement("div");
        select_label_security.classList.add("map_label_security", "font_header");
        select_label_outer.appendChild(select_label_security);
        select_label_element.appendChild(select_label_outer);

        select_label = new CSS2DObject(select_label_element);
        select_label.position.set(0, 0, 0);
        select_label.center.set(0, 1);
        select_label.visible = false;
        scene.add(select_label);
    }

    document.getElementById("map_reset_camera").onclick = () => {
        const dist = camera.position.distanceTo(controls.target);
        const new_target = (selected_item != null)
            ? select_label.position
            : new THREE.Vector3(0, 0, 0);

        lerp_to(
            250,
            null,
            new_target,
            new_target.clone().add(new THREE.Vector3(0, dist, 0)),
            0
        );
    }

    document.getElementById("map_select").onchange = event => {
        select_label.visible = false;

        let nav_id = null;
        switch (event.currentTarget.value) {
            case "nec":
                setiframe("../map/-1.html");
                load_map("./rsc/map/NEC.json", "./rsc/map/NEC_jumps.json");
                nav_id = -1;
                break;
            case "pochven":
                setiframe("../map/10000070.html");
                load_map("./rsc/map/pochven.json", "./rsc/map/pochven_jumps.json");
                nav_id = 10000070;
                break;
            case "anoikis":
                setiframe("../map/-2.html");
                load_map("./rsc/map/anoikis.json", null);
                nav_id = -2;
                break;
        }

        const selectable = selectables[nav_id];

        const position = new THREE.Vector3(
            selectable.x / SCALE,
            selectable.y / SCALE,
            -selectable.z / SCALE
        );

        lerp_to(
            250,
            () => { apply_camera_offset(false); },
            position,
            position.clone().add(new THREE.Vector3(0, selectable.distance / SCALE, 0)),
            0
        );
    }

    {
        window.addEventListener('resize', onWindowResize);
        renderer.setAnimationLoop(animate);

        fetch("./rsc/map/selection.json")
            .then(r => r.json())
            .then(s => {
                selectables = s;
            });

        await load_map("./rsc/map/NEC.json", "./rsc/map/NEC_jumps.json");
        renderer.domElement.addEventListener("mousemove", onMouseMove);
        renderer.domElement.addEventListener("mousedown", onMouseDown);

        const p1 = fetch("./rsc/map/system_jumps.json", {cache: "no-store"})
            .then(r => r.json())
            .then(system_jumps => {
                let map = {};
                for (let entry of system_jumps) {
                    map[entry.system_id.toString()] = entry.ship_jumps;
                }
                return map;
            });
        const p2 = fetch("./rsc/map/system_kills.json", {cache: "no-store"})
            .then(r => r.json())
            .then(system_kills => {
                let ship_map = {};
                let npc_map = {};
                for (const entry of system_kills) {
                    const id_str = entry.system_id.toString();
                    ship_map[id_str] = entry.ship_kills;
                    npc_map[id_str] = entry.npc_kills;
                }
                return [ship_map, npc_map];
            });


        [system_info_jumps, [system_info_ship_kills, system_info_npc_kills]] = await Promise.all([p1, p2]);
        system_info_jumps_max = Object.values(system_info_jumps).reduce((l, r) => Math.max(l, r), -Infinity);
        system_info_ship_kills_max = Object.values(system_info_ship_kills).reduce((l, r) => Math.max(l, r), -Infinity);
        system_info_npc_kills_max = Object.values(system_info_npc_kills).reduce((l, r) => Math.max(l, r), -Infinity);

        for (let radio of document.querySelectorAll("input[name=\"map_colour\"]")) {
            radio.disabled = false;
            radio.onchange = map_colour_update;
        }
    }
}

const security_colours = [
    [112.0 / 255.0, 33.0 / 255.0, 30.0 / 255.0],    // 0.0 -> 1.0, <=0.0 itself is special-cased
    [112.0 / 255.0, 33.0 / 255.0, 30.0 / 255.0],    // 1.0
    [188.0 / 255.0, 18.0 / 255.0, 18.0 / 255.0],    // 2.0
    [202.0 / 255.0, 72.0 / 255.0, 18.0 / 255.0],    // 3.0
    [220.0 / 255.0, 108.0 / 255.0, 9.0 / 255.0],    // 4.0
    [240.0 / 255.0, 255.0 / 255.0, 133.0 / 255.0],  // 5.0
    [115.0 / 255.0, 227.0 / 255.0, 82.0 / 255.0],   // 6.0
    [93.0 / 255.0, 220.0 / 255.0, 166.0 / 255.0],   // 7.0
    [72.0 / 255.0, 208.0 / 255.0, 242.0 / 255.0],   // 8.0
    [58.0 / 255.0, 156.0 / 255.0, 241.0 / 255.0],   // 9.0
    [46.0 / 255.0, 116.0 / 255.0, 222.0 / 255.0]    // 1.0
]

const class_colours = [ // Colours stolen off anoik.is 😇
    [82.0/255.0, 82.0/255.0, 255.0/255.0],  // C1
    [0.0/255.0, 168.0/255.0, 168.0/255.0],  // C2
    [0.0/255.0, 168.0/255.0, 0.0/255.0],    // C3
    [168.0/255.0, 168.0/255.0, 0.0/255.0],  // C4
    [168.0/255.0, 84.0/255.0, 0.0/255.0],   // C5
    [168.0/255.0, 0.0/255.0, 0.0/255.0],    // C6
];

function set_colour_security(system_id, colour) {
    let security = systems[system_id].security;
    let whclass = Number((systems[system_id].whclass ?? "").substring(1,2));    // A bit janky; we don't push the whClassID number to HTML
    if (whclass >= 1 && whclass <= 6) {
        colour.set(...class_colours[whclass - 1]);
    } else if (security > 0) {
        colour.set(...security_colours[Math.round(security * 10)]);
    } else {
        colour.set(142.0 / 255.0, 49.0 / 255.0, 99.0 / 255.0);
    }
}
function set_colour_jumps(system_id, colour) {
    let jumps = system_info_jumps[system_id] ?? 0;
    const channel = (jumps / system_info_jumps_max);
    colour.set(channel, channel, channel);
}
function set_colour_ship_kills(system_id, colour) {
    let jumps = system_info_ship_kills[system_id] ?? 0;
    const channel = (jumps / system_info_ship_kills_max);
    colour.set(channel, channel, channel);
}
function set_colour_npc_kills(system_id, colour) {
    let jumps = system_info_npc_kills[system_id] ?? 0;
    const channel = (jumps / system_info_npc_kills_max);
    colour.set(channel, channel, channel);
}

let set_colour = set_colour_security;

function map_colour_update(event) {
    colour_mode = event.currentTarget.value;
    switch (colour_mode) {
        case "security":
            set_colour = set_colour_security;
            break;
        case "jumps":
            set_colour = set_colour_jumps;
            break;
        case "ship_kills":
            set_colour = set_colour_ship_kills;
            break;
        case "npc_kills":
            set_colour = set_colour_npc_kills;
            break;
    }

    (async () => {
        const colour = new THREE.Color(1, 1, 1);
        for (let i = 0; i < system_mesh.count; i++) {
            set_colour(system_ids[i], colour);
            system_mesh.setColorAt(i, colour);
        }
        system_mesh.instanceColor.needsUpdate = true;
    })();

    const system = systems[selected_item];
    if (system != null) {
        if (system.whclass == null) {
            switch (colour_mode) {
                case "security":
                    if (system.security != null) {
                        select_label_security.textContent = NUMBER_FORMAT.format(Math.round(system.security * 10) / 10);
                    } else {
                        select_label_security.textContent = "???";
                    }
                    break;
                case "jumps":
                    select_label_security.textContent = "🚀" + (system_info_jumps[selected_item] ?? 0);
                    break;
                case "ship_kills":
                    select_label_security.textContent = "☠️" + (system_info_ship_kills[selected_item] ?? 0);
                    break;
                case "npc_kills":
                    select_label_security.textContent = "☠️" + (system_info_npc_kills[selected_item] ?? 0);
                    break;
            }
        }
    }
}

async function load_map(mapFile, jumpsFile) {
    if (system_mesh != null) {
        system_mesh.geometry.dispose();
        system_mesh.material.dispose();
        scene.remove(system_mesh);
    }
    if (jump_lines != null) {
        jump_lines.geometry.dispose();
        jump_lines.material.dispose();
        scene.remove(jump_lines);
    }

    {
        systems = await (await fetch(mapFile)).json();

        system_ids = [];
        system_vertices = [];

        const sphere_geometry = new THREE.IcosahedronGeometry(SYSTEM_SIZE / SCALE, 3);
        const sphere_material = new THREE.MeshPhongMaterial({color: 0xffffff});
        system_mesh = new THREE.InstancedMesh(sphere_geometry, sphere_material, Object.keys(systems).length);


        const matrix = new THREE.Matrix4();
        const colour = new THREE.Color(1, 1, 1);
        for (const systemID in systems) {
            const system = systems[systemID];
            let idx = system_ids.length;
            system["index"] = idx;
            system_ids.push(systemID);

            system_mesh.setMatrixAt(idx, matrix.makeTranslation(system.x / SCALE, system.y / SCALE, -system.z / SCALE))
            system_vertices.push(system.x / SCALE, system.y / SCALE, -system.z / SCALE);
            set_colour(systemID, colour);
            system_mesh.setColorAt(idx, colour);
        }
        system_mesh.instanceColor.needsUpdate = true;

        scene.add(system_mesh);
    }
    let line_vertices = [];
    if (jumpsFile != null) {
        const jumps = await (await fetch(jumpsFile)).json();
        for (let jump of jumps) {
            for (const systemID of jump) {
                const system = systems[systemID];

                line_vertices.push(new THREE.Vector3(system.x / SCALE, system.y / SCALE, -system.z / SCALE));
            }
        }
    }

    const line_material = new THREE.LineBasicMaterial({
        color: 0x0000ff,
        linewidth: 1
    });
    const geometry = new THREE.BufferGeometry().setFromPoints(line_vertices);
    jump_lines = new THREE.LineSegments(geometry, line_material);
    scene.add(jump_lines);

    jump_lines.visible = document.getElementById("map_show_jumps").checked;

    document.getElementById("map_show_jumps").onchange = event => {
        jump_lines.visible = event.currentTarget.checked === true;
    }
}

function setiframe(src) {
    // Avoid refreshing the iframe if it's the same src
    if (iframe.src !== src) {
        iframe.contentWindow.removeEventListener("unload", iframeUnloadHandler);
        iframe.src = src;
    }
}

function select_item(item_id) {
    if (selected_item === item_id) return;
    const prev_item = selected_item;
    selected_item = item_id;

    const new_src = "../map/" + item_id + ".html";
    setiframe(new_src);

    hover_label.visible = false;

    const system = systems[item_id];
    const selectable = selectables[item_id];
    if (system != null) {
        select_label.position.set(
            system_vertices[system.index * 3],
            system_vertices[system.index * 3 + 1],
            system_vertices[system.index * 3 + 2]
        )
            .add(system_mesh.position);
        select_label_name.textContent = system.solarSystemName;
        if (system.whclass != null) {
            select_label_security.textContent = system.whclass;
        } else {
            switch (colour_mode) {
                case "security":
                    if (system.security != null) {
                        select_label_security.textContent = NUMBER_FORMAT.format(Math.round(system.security * 10) / 10);
                    } else {
                        select_label_security.textContent = "???";
                    }
                    break;
                case "jumps":
                    select_label_security.textContent = "🚀" + system_info_jumps[item_id] ?? 0;
                    break;
                case "ship_kills":
                    select_label_security.textContent = "☠️" + system_info_ship_kills[item_id] ?? 0;
                    break;
                case "npc_kills":
                    select_label_security.textContent = "☠️" + system_info_npc_kills[item_id] ?? 0;
                    break;
            }
        }
        select_label.visible = true;

        lerp_to(
            250,
            () => { apply_camera_offset(true); },
            select_label.position,
            camera.position.clone()
                .sub(controls.target)
                .add(select_label.position),
            null
        );

        if (systems[prev_item] == null && prev_item !== "-1" && prev_item !== "-2") {
            (async () => {
                const colour = new THREE.Color(0.1, 0.1, 0.1);
                for (let i = 0; i < system_mesh.count; i++) {
                    set_colour(system_ids[i], colour);
                    system_mesh.setColorAt(i, colour);
                }
                system_mesh.instanceColor.needsUpdate = true;
            })();
        }

        // TODO: load in-system celestials
    } else if (selectable != null) {
        select_label.position.set(
            selectable.x / SCALE,
            selectable.y / SCALE,
            -selectable.z / SCALE
        )
            .add(system_mesh.position);

        if (selectable.name != null) {
            select_label_name.textContent = selectable.name;
            if (selectable.whclass != null) {
                select_label_security.textContent = selectable.whclass;
            } else if (selectable.security != null) {
                select_label_security.textContent = NUMBER_FORMAT.format(Math.round(selectable.security * 10) / 10);
            } else {
                select_label_security.textContent = "";
            }
            select_label.visible = true;
        } else {
            select_label.visible = false;
        }

        lerp_to(
            250,
            () => { apply_camera_offset(false); },
            select_label.position,
            select_label.position.clone().add(new THREE.Vector3(0, selectable.distance / SCALE, 0)),
            0
        );

        (async () => {
            if (selectable.systems != null) {
                const colour = new THREE.Color(0.05, 0.05, 0.05);
                for (let i = 0; i < system_mesh.count; i++) {
                    system_mesh.setColorAt(i, colour);
                }

                colour.set(1, 1, 1);
                for (const system_id of selectable.systems) {
                    set_colour(system_id, colour);
                    system_mesh.setColorAt(systems[system_id].index, colour);
                }
                system_mesh.instanceColor.needsUpdate = true;
            } else {
                if (systems[prev_item] == null && prev_item !== "-1" && prev_item !== "-2") {
                    const colour = new THREE.Color(0.1, 0.1, 0.1);
                    for (let i = 0; i < system_mesh.count; i++) {
                        set_colour(system_ids[i], colour);
                        system_mesh.setColorAt(i, colour);
                    }
                    system_mesh.instanceColor.needsUpdate = true;
                }
            }
        })();
    } else {
        console.log("Unknown selection! ", item_id);
    }
}

function apply_camera_offset(show_selection_mesh) {
    system_mesh.position.sub(controls.target);
    jump_lines.position.sub(controls.target);
    camera.position.sub(controls.target);
    hover_label.position.sub(controls.target);
    select_label.position.sub(controls.target);
    controls.target.set(0, 0, 0);

    selection_mesh.visible = show_selection_mesh;
}

function lerp_to(
    duration,
    oncomplete,
    toTarget,
    toPosition,
    toAzimuth
) {
    lerp(
        duration,
        oncomplete,
        toTarget != null ? controls.target.clone() : null,
        toTarget,
        toPosition != null ? camera.position.clone() : null,
        toPosition,
        toAzimuth != null ? camera.rotation.z : null,
        toAzimuth
    )
}

function lerp(
    duration,
    oncomplete,
    fromTarget, toTarget,
    fromPosition, toPosition,
    fromAzimuth, toAzimuth
) {
    lerp_target_origin = fromTarget ?? null;
    lerp_target_target = toTarget ?? null;
    console.assert((lerp_target_origin == null) === (lerp_target_target == null));
    lerp_position_origin = fromPosition ?? null;
    lerp_position_target = toPosition ?? null;
    console.assert((lerp_position_origin == null) === (lerp_position_target == null));
    lerp_azimuth_origin = fromAzimuth ?? null;
    lerp_azimuth_target = toAzimuth ?? null;
    console.assert((lerp_azimuth_origin == null) === (lerp_azimuth_target == null));
    lerp_oncomplete = oncomplete ?? null;
    console.assert(duration != null);
    lerp_duration = duration;
    lerp_time = 0;
    controls.enablePan = false;
    controls.enableRotate = false;
}

function onMouseMove(event) {
    event.preventDefault();
    var mouse = new THREE.Vector2();
    var box = renderer.domElement.getBoundingClientRect();
    mouse.x = ((event.clientX - box.left) / (box.right - box.left)) * 2.0 - 1.0;
    mouse.y = ((event.clientY - box.bottom) / (box.top - box.bottom)) * 2.0 - 1.0;
    raycaster.setFromCamera(mouse, camera);
    var intersects = raycaster.intersectObject(system_mesh);
    if (intersects.length > 0 && selected_item !== system_ids[intersects[0].instanceId]) {
        hover_label.position.set(
            system_vertices[intersects[0].instanceId * 3],
            system_vertices[intersects[0].instanceId * 3 + 1],
            system_vertices[intersects[0].instanceId * 3 + 2]
        )
            .add(system_mesh.position);
        hover_label.visible = true;
        const system_id = system_ids[intersects[0].instanceId];
        const system = systems[system_id];
        hover_label_name.textContent = system.solarSystemName;
        if (system.whclass != null) {
            hover_label_security.textContent = system.whclass;
        } else {
            switch (colour_mode) {
                case "security":
                    if (system.security != null) {
                        hover_label_security.textContent = NUMBER_FORMAT.format(Math.round(system.security * 10) / 10);
                    } else {
                        hover_label_security.textContent = "???";
                    }
                    break;
                case "jumps":
                    hover_label_security.textContent = "🚀" + (system_info_jumps[system_id] ?? 0);
                    break;
                case "ship_kills":
                    hover_label_security.textContent = "☠️" + (system_info_ship_kills[system_id] ?? 0);
                    break;
                case "npc_kills":
                    hover_label_security.textContent = "☠️" + (system_info_npc_kills[system_id] ?? 0);
                    break;
            }
        }
    } else {
        hover_label.visible = false;
    }
}


function onMouseDown(event) {
    if (event.button === 0) {
        event.preventDefault();
        var mouse = new THREE.Vector2();
        var box = renderer.domElement.getBoundingClientRect();
        mouse.x = ((event.clientX - box.left) / (box.right - box.left)) * 2.0 - 1.0;
        mouse.y = ((event.clientY - box.bottom) / (box.top - box.bottom)) * 2.0 - 1.0;
        raycaster.setFromCamera(mouse, camera);
        var intersects = raycaster.intersectObject(system_mesh);
        if (intersects.length > 0) {
            select_item(system_ids[intersects[0].instanceId]);
        }
    }
}

function onWindowResize() {
    camera.aspect = map_spacer.clientWidth / map_spacer.clientHeight
    renderer.setSize(map_spacer.clientWidth, map_spacer.clientHeight, false);
    camera.updateProjectionMatrix();
    labelRenderer.setSize(map_spacer.clientWidth, map_spacer.clientHeight, false);
}

let prev_frame_time = 0;

function animate(time) {
    const dt = time - prev_frame_time;
    prev_frame_time = time;

    if (lerp_time != null) {
        lerp_time += dt;
        const alpha = Math.min(lerp_time / lerp_duration, 1);
        if (lerp_position_target != null) {
            camera.position.lerpVectors(lerp_position_origin, lerp_position_target, alpha);
        }
        if (lerp_azimuth_target != null) {
            controls.maxAzimuthAngle = THREE.MathUtils.lerp(lerp_azimuth_origin, lerp_azimuth_target, alpha);
            controls.minAzimuthAngle = THREE.MathUtils.lerp(lerp_azimuth_origin, lerp_azimuth_target, alpha);
        }
        if (lerp_target_target != null) {
            controls.target.lerpVectors(lerp_target_origin, lerp_target_target, alpha);
        }

        if (alpha === 1) {
            controls.update();  // Update controls to reset any gimbal lock

            lerp_target_origin = null;
            lerp_target_target = null;
            lerp_time = null;
            lerp_duration = null;

            controls.minAzimuthAngle = -Infinity;
            controls.maxAzimuthAngle = +Infinity;
            controls.enablePan = true;
            controls.enableRotate = true;

            if (lerp_oncomplete != null) {
                lerp_oncomplete();
            }
        }
    }

    render();
    stats.update();
}

function render() {
    controls.update();
    renderer.render(scene, camera);
    labelRenderer.render(scene, camera);
}