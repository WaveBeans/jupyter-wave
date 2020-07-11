(function() {
    window.WaveView = function (url, container) {
        const waveSurfer = WaveSurfer.create({
            container: "#" + container,
            scrollParent: true
        });

        function playPause() {
            waveSurfer.playPause()
        }

        function _init() {
            waveSurfer.load(url);
            waveSurfer.on('ready', function () {
                waveSurfer.play();
            });
            const playPauseButton = document.createElement("button")
            playPauseButton.appendChild(document.createTextNode("Play/Pause"))
            playPauseButton.addEventListener("click", playPause)
            document.getElementById(container).parentNode.appendChild(playPauseButton)
        }

        return {
            init: _init
        }
    };
})();