import { Options } from 'k6/options';

function matrixType(options: Options, types: string[], thresholds: string[] = []): void {
  types.forEach((type) => {
    const thresholdNames = [
      `http_req_duration{type:${type}}`,
      `http_req_failed{type:${type}}`,
      `http_req_receiving{type:${type}}`,
      `http_req_sending{type:${type}}`,
      `http_req_waiting{type:${type}}`,
      `http_reqs{type:${type}}`,
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

export default matrixType;