(function () {

    function renderTime(t) {
        /** t is double value in seconds */
        let v;
        let m = Math.round(t / 60)
        let s = Math.round(t)
        let ms = Math.round(t * 1000) % 1000
        let us = Math.round(t * 1000000) % 1000
        let mstr = (m < 10 ? "0" : "") + m
        let sstr = (s < 10 ? "0" : "") + s
        let msstr = (ms < 100 ? "0" : "") + (ms < 10 ? "0" : "") + ms
        let usstr = "" + us
        return mstr + ":" + sstr + ":" + msstr + "." + usstr;
    }

    window.WaveView = function (url, container) {
        let waveSurfer = null
        let regionColors = ['#ffd97d55', '#ff9b8555', '#348aa755', '#52517455', '#d68c4555']

        function _init() {
            waveSurfer = WaveSurfer.create({
                container: document.querySelector("." + container + " .wave"),
                minimap: true,
                scrollParent: true,
                waveColor: '#6a994e',
                progressColor: '#386641',
                loopSelection: true,
                regionsMinLength: 2,
                plugins: [
                    WaveSurfer.regions.create({
                        color: "#ef233c33",
                        maxRegions: regionColors.length,
                        slop: 5,
                        dragSelection: {
                            slop: 5
                        }
                    }),
                    WaveSurfer.cursor.create({
                        showTime: true,
                        opacity: 1,
                        formatTimeCallback: (s) => renderTime(s),
                        customStyle: {
                            'height': '128px'
                        },
                        customShowTimeStyle: {
                            'background-color': '#132a13',
                            color: '#fff',
                            padding: '2px',
                            'font-size': '10pt',
                        }
                    }),
                    WaveSurfer.timeline.create({
                        container: document.querySelector("." + container + " .timeline"),
                        timeInterval: (pxPerSec) => {
                            if (pxPerSec < 25) {
                                return 0.01;
                            } else if (pxPerSec >= 25) {
                                return 0.05;
                            } else if (pxPerSec * 5 >= 25) {
                                return 0.25;
                            } else if (pxPerSec * 10 >= 25) {
                                return 1.0;
                            }
                        },
                        formatTimeCallback: (s) => renderTime(s)
                    }),
                    WaveSurfer.minimap.create({
                        height: 30,
                        waveColor: '#a7c957',
                        progressColor: '#6a994e',
                        cursorColor: '#132a13'
                    }),
                ]
            });


            // load
            waveSurfer.load(url);
            waveSurfer.on('ready', function () {
                waveSurfer.play();
            });

            // regions
            let regions = [];
            function redrawRegions() {
                let regionsEl = document.querySelector("." + container + " .regions")
                regionsEl.innerHTML = ""
                regions.forEach((region) => {
                    if (!region.colorValue) {
                        region.colorValue = regionColors.pop()
                        region.color = region.colorValue
                        region.updateRender()
                    }

                    let div = document.createElement("div")
                    div.style.float = 'left'
                    div.style.margin = '5px 10px 5px 10'

                    let removeBtn = document.createElement("button")
                    removeBtn.innerHTML = "<span class=\"material-icons\">remove_circle_outline</span></button>"
                    removeBtn.style.backgroundColor = region.colorValue
                    removeBtn.style.border = 'solid 1px black'
                    removeBtn.addEventListener('click', () => {
                        regions = regions.filter((r) => r !== region)
                        regionColors.push(region.colorValue)
                        region.remove()
                        redrawRegions()
                    })
                    let playButton = document.createElement("button")
                    playButton.innerHTML = "<span class=\"material-icons\">play_circle_outline\n</span></button>"
                    playButton.style.backgroundColor = region.colorValue
                    playButton.style.border = 'solid 1px black'
                    playButton.addEventListener('click', () => {
                        region.loop = false
                        region.play()
                    })

                    let loopButton = document.createElement("button")
                    loopButton.innerHTML = "<span class=\"material-icons\">loop</span></button>"
                    loopButton.style.backgroundColor = region.colorValue
                    loopButton.style.border = 'solid 1px black'
                    loopButton.addEventListener('click', () => {
                        region.loop = true
                        region.play()
                    })

                    let desc = document.createElement("span")
                    desc.style.marginLeft = '5px'
                    desc.innerText = renderTime(region.start) + " - " + renderTime(region.end)

                    div.appendChild(playButton)
                    div.appendChild(loopButton)
                    div.appendChild(removeBtn)
                    div.appendChild(desc)

                    region.uiElement = div
                    regionsEl.appendChild(div)
                })

            }

            waveSurfer.on('region-update-end', () => {
                console.log("region-update-end", arguments)
                regions = Object.values(waveSurfer.regions.list)
                setTimeout(redrawRegions, 0)
            })


            // controls
            // zoom slider
            var slider = document.querySelector("." + container + " [data-action=\"zoom\"]");
            slider.value = waveSurfer.params.minPxPerSec;
            slider.min = waveSurfer.params.minPxPerSec;
            slider.max = 10000;

            slider.addEventListener('input', function () {
                waveSurfer.zoom(Number(this.value));
            });

            // play-pause
            const playButton = document.querySelector('[data-action="play"]')
            playButton.addEventListener("click", playPause)
            const pauseButton = document.querySelector('[data-action="pause"]')
            pauseButton.addEventListener("click", playPause)
            playButton.style.display = 'inline-block'
            pauseButton.style.display = 'none'

            function playPause() {
                regions.forEach((r) => r.loop = false) // otherwise the region may loop in
                if (waveSurfer)
                    waveSurfer.playPause()
            }

            waveSurfer.on('play', () => {
                playButton.style.display = 'none'
                pauseButton.style.display = 'inline-block'
            })
            waveSurfer.on('pause', () => {
                playButton.style.display = 'inline-block'
                pauseButton.style.display = 'none'
            })


        }

        return {
            init: _init
        }
    };
})();