window.onload = main;

let app;
let mapContainer;

let scale = 3.0;
let offsetX = 0;
let offsetY = 0;

let oldX = 0;
let oldY = 0;
let newX = 0;
let newY = 0;
let movementInProgress = false;

let mapWidth = 800;
let mapHeight = 800;

let neighboursMap = new Map();
let polygonsMap = new Map();

function initPixiApp() {
    app = new PIXI.Application({
        width: window.innerWidth,
        height: window.innerHeight
    });
    app.renderer.backgroundColor = 0xF1F1FF;
    app.renderer.view.style.position = "absolute";
    app.renderer.view.style.display = "block";
    app.renderer.autoDensity = true;
    app.renderer.resize(mapWidth, mapHeight);
    let div = document.getElementById("main");
    div.appendChild(app.view);

    mapContainer = new PIXI.Container();
    app.stage.addChild(mapContainer);

    let playerCircle = new PIXI.Graphics();
    playerCircle.beginFill(0xFFFFFF);
    playerCircle.drawCircle(mapWidth/2, mapHeight/2, 10 * scale);
    app.stage.addChild(playerCircle);

    return app;
}

function initFileReader() {
    document.getElementById('file-input').addEventListener('change', readSingleFile, false);
}

function main() {
    initPixiApp();
    initFileReader();
}

function displayWorld(allText) {
    let worldObject = JSON.parse(allText);
    let cities = worldObject.cities;

    for (let i = 0; i < cities.length; i++) {
        displayCity(cities[i]);
    }
}

function highlightNeighbours(id) {
    let neighbourIds = neighboursMap.get(id);
    for (let i = 0; i < neighbourIds.length; i++) {
        let polygon = polygonsMap.get(neighbourIds[i]);
        if (polygon != null) {
            polygon.tint = 0xFF0000;
        }
    }
}

function resetNeighbours(id) {
    let neighbourIds = neighboursMap.get(id);
    for (let i = 0; i < neighbourIds.length; i++) {
        let polygon = polygonsMap.get(neighbourIds[i]);
        if (polygon != null) {
            polygon.tint = polygon.defaultColor;
        }
    }
}

function updateMainContainer() {
    newX = -(offsetX - mapWidth / 2);
    newY = -(offsetY - mapHeight / 2);
    oldX = mapContainer.x;
    oldY = mapContainer.y;
    animate({
        duration: 1000,
        timing: inOutQuad,
        callback: fromOldToNew
    });
}

function fromOldToNew(progress) {
    let currentX = oldX + (newX - oldX) * progress;
    let currentY = oldY + (newY - oldY) * progress;
    mapContainer.x = currentX;
    mapContainer.y = currentY;
}

function displayCity(city) {
    neighboursMap.set(city.siteId, city.neighbours);

    const pointsX = city.polygon.pointsX;
    const pointsY = city.polygon.pointsY;

    let maxX = -99999;
    let maxY = -99999;
    let minX = 99999;
    let minY = 99999;

    let points = [];
    for (let i = 0; i < pointsX.length; i++) {
        let x = pointsX[i] * scale;
        let y = pointsY[i] * scale;
        if (x > maxX) maxX = x;
        if (x < minX) minX = x;
        if (y > maxY) maxY = y;
        if (y < minY) minY = y;
        points.push(x);
        points.push(y);
    }

    let midX = (maxX + minX) / 2;
    let midY = (maxY + minY) / 2;

    let polygon = new PIXI.Graphics();
    polygon.id = city.siteId;
    polygon.midX = midX;
    polygon.midY = midY;
    polygon.interactive = true;
    polygon.mouseover = function (mouseEvent) {
        this.tint = 0xFFFFFF;
        highlightNeighbours(this.id);
    };
    polygon.mouseout = function (mouseEvent) {
        this.tint = this.defaultColor;
        resetNeighbours(this.id);
    };
    polygon.click = function (mouseEvent) {
        alert(city.population);
        if (!movementInProgress) {
            offsetX = this.midX;
            offsetY = this.midY;
            updateMainContainer();
        }
    };
    let color = city.color;
    polygon.beginFill(0xFFFFFF, 1.0);
    polygon.tint = color;
    polygon.defaultColor = color;
    polygon.lineStyle(1, 0x110000);
    polygon.drawPolygon(points);
    polygon.endFill();
    polygon.x = offsetX;
    polygon.y = offsetY;
    polygonsMap.set(city.siteId, polygon);
    mapContainer.addChild(polygon);

    let text = new PIXI.Text();
    text.text = city.name;
    text.x = offsetX + midX - 20;
    text.y = offsetY + midY - 3;
    text.style.fontSize = 14;
    mapContainer.addChild(text);
}

function readSingleFile(e) {
    let file = e.target.files[0];
    if (!file) {
        return;
    }
    let reader = new FileReader();
    reader.onload = function (e) {
        let contents = e.target.result;
        displayWorld(contents);
    };
    reader.readAsText(file);
}

function getRandomNumber(max) {
    return Math.random() * Math.floor(max);
}

function animate({timing, callback, duration}) {
    movementInProgress = true;
    let start = performance.now();

    requestAnimationFrame(function animate(time) {
        // timeFraction изменяется от 0 до 1
        let timeFraction = (time - start) / duration;
        if (timeFraction > 1) timeFraction = 1;

        // вычисление текущего состояния анимации
        let progress = timing(timeFraction);

        callback(progress); // отрисовать её

        if (timeFraction < 1) {
            requestAnimationFrame(animate);
        } else {
            movementInProgress = false;
        }
    });
}

function linear(t) {
    return t;
}

function inOutQuad(t) {
    t *= 2;
    if (t < 1) return 0.5 * t * t;
    return -0.5 * (--t * (t - 2) - 1);
}
