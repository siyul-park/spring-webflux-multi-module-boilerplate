import { Options } from 'k6/options';

function matrixScenarios(options: Options, thresholds: string[] = []): void {
  Array.from(Object.entries(options.scenarios ?? {})).forEach(([key]) => {
    const thresholdNames = [
      `http_req_duration{scenario:${key}}`,
      `http_req_failed{scenario:${key}}`,
      `http_req_receiving{scenario:${key}}`,
      `http_req_sending{scenario:${key}}`,
      `http_req_waiting{scenario:${key}}`,
      `http_reqs{scenario:${key}}`,
    ];
    thresholdNames.forEach((thresholdName) => {
      if (options.thresholds == null) {
        options.thresholds = {};
      }
      if (!options.thresholds[thresholdName]) {
        options.thresholds[thresholdName] = thresholds;
      }
    });
  });
}

export default matrixScenarios;