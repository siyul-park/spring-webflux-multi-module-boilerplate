import { Options } from 'k6/options';

function matrixType(options: Options, types: string[]): void {
  types.forEach((type) => {
    const thresholdNames = [
      `http_req_duration{scenario:${type}}`,
      `http_req_failed{scenario:${type}}`,
      `http_req_receiving{scenario:${type}}`,
      `http_req_sending{scenario:${type}}`,
      `http_req_waiting{scenario:${type}}`,
      `http_reqs{scenario:${type}}`,
    ];
    thresholdNames.forEach((thresholdName) => {
      if (options.thresholds == null) {
        options.thresholds = {};
      }
      if (!options.thresholds[thresholdName]) {
        options.thresholds[thresholdName] = [];
      }
    });
  });
}

export default matrixType;