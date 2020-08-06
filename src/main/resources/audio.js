(function() {
    window.WaveView = function (url, container) {
        let waveSurfer = null

        function playPause() {
            if (waveSurfer)
                waveSurfer.playPause()
        }

        function _init() {
            waveSurfer = WaveSurfer.create({
                container: "#" + container,
                scrollParent: true
            });

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