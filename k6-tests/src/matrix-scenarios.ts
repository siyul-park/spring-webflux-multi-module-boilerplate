import { Options } from 'k6/options';

function matrixScenarios(options: Options): void {
  Array.from(Object.entries(options.scenarios ?? {})).forEach(([key]) => {
    let thresholdName = `http_req_duration{scenario:${key}}`;
    if (options.thresholds == null) {
      options.thresholds = {};
    }
    if (!options.thresholds[thresholdName]) {
      options.thresholds[thresholdName] = [];
    }
    options.thresholds[thresholdName].push('max>=0');
  });
}

export default matrixScenarios;