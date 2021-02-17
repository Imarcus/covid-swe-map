const CovidSweMap = {
  data() {
    return {
      regionData: {},
      casesResponse: {},
      sliderElement: {},
      outputElement: {},
      thresholds: [10000, 5000, 2500, 1000, 500, 100, 10, 1, 0],
      layerGroup: {},
      sliderValue: 0,
      map: {},
      playInterval: {},
      isPlaying: false
    }
  },
  watch: {
    sliderValue: function (value) {
      this.sliderValue = parseInt(value)
      this.loadFeatureCollection(this.sliderValue)
    }
  },
  mounted() {
    this.sliderElement = document.getElementById("date-slider");
    this.outputElement = document.getElementById("date-label");
    this.start();
  },
  methods: {
    start() {
      fetch('/cases')
        .then(response => response.json())
        .then(data => {
            this.casesResponse = data;
            this.sliderElement.min = this.casesResponse.startWeek;
            this.sliderElement.max = this.casesResponse.endWeek;
            fetch('sweden-counties.geojson')
              .then(file => file.json())
              .then(regionData => {
                  this.regionData = regionData;
                  this.sliderValue = this.casesResponse.startWeek;
              });
        });
      this.loadMap();
      this.loadMapLegend();
    },
    loadFeatureCollection(weekNumber) {
      this.regionData.features.forEach(feat => {
          region = this.casesResponse.regionCases.find(regionCase => regionCase.regionName === feat.properties.name);
          feat.properties.cases = region.casesPerWeek[weekNumber]
      });
      this.outputElement.innerHTML = this.getWeekLabel(weekNumber);
      this.layerGroup.clearLayers();
      this.layerGroup.addLayer(L.geoJSON(this.regionData, {
          style: this.getStyle
      }));
    },
    loadMap() {
      this.map = L.map('mapid').setView([63.02152819100765, 15.029296875000002], 4);
      L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoicmJxODgzIiwiYSI6ImNrZzQ3NDR6cjBob2cycHBhbnBiaGszaXMifQ.xXgPAHbxCAYXTJOUudXqcQ', {
          maxZoom: 5,
          minZoom: 5,
          attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
          id: 'mapbox/streets-v11',
          tileSize: 512,
          zoomOffset: -1,
          id: 'mapbox/light-v9',
      }).addTo(this.map);
      this.map.dragging.disable();
      this.layerGroup = new L.LayerGroup();
      this.layerGroup.addTo(this.map);
      this.map.on('click', this.onMapClick);
    },
    loadMapLegend() {
      let legend = L.control({position: 'bottomright'});
      legend.onAdd = map => {
          const div = L.DomUtil.create('div', 'info legend');
          const rev = this.thresholds.slice().reverse();

          for (let i = 0; i < this.thresholds.length; i++) {
              let label;
              if(i === 0) {
                label = '0<br>';
              } else {
                label = rev[i] + (rev[i + 1] ? '&ndash;' + rev[i + 1] + '<br>' : '+');
              }
              div.innerHTML +=
                  '<i style="background:' + this.getColor(rev[i]) + '"></i> ' + label;
          }

          return div;
      };
      legend.addTo(this.map);
    },
    getWeekLabel(weekNr) {
      return weekNr <= 53 ? `Week ${weekNr} - 2020` : `Week ${weekNr - 53} - 2021`;
    },
    onMapClick(e) {
      console.log("You clicked the map at " + e.latlng.lat + ", " + e.latlng.lng);
    },
    getStyle(feature) {
      const color = this.getColor(feature.properties.cases);
      return {
          "color": 'white',
          "fillColor": color,
          "weight": 1,
          "opacity": 1,
          "fillOpacity": 0.8
      };
    },
    getColor(cases) {
      return cases >= this.thresholds[0] ? '#662506' :
            cases >= this.thresholds[1] ? '#993404' :
            cases >= this.thresholds[2] ? '#cc4c02' :
            cases >= this.thresholds[3] ? '#ec7014' :
            cases >= this.thresholds[4] ? '#fe9929' :
            cases >= this.thresholds[5] ? '#fec44f' :
            cases >= this.thresholds[6] ? '#fee391' :
            cases >= this.thresholds[7] ? '#fff7bc' :
                    '#c2c2c2';
    },
    incWeek() {
      const isLastWeek = this.casesResponse.endWeek <= this.sliderValue;
      this.sliderValue = !isLastWeek ? this.sliderValue + 1 : this.sliderValue;
      if (isLastWeek && this.isPlaying) {
        this.stopSlider();
      }
    },
    decWeek() {
      this.sliderValue = this.casesResponse.startWeek < this.sliderValue ? this.sliderValue - 1 : this.sliderValue;
    },
    playSlider() {
      if(!this.isPlaying) {
        this.startSlider();
      } else {
        this.stopSlider();
      }
    },
    startSlider() {
      this.playInterval = window.setInterval(this.incWeek, 500)
      this.isPlaying = true;
    },
    stopSlider() {
      window.clearInterval(this.playInterval);
      this.isPlaying = false;
    }
  },
  computed: {
    playButtonText() {
      return this.isPlaying ? 'Stop' : 'Play';
    }
  }
}

Vue.createApp(CovidSweMap).mount('#app')