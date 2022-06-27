// @ts-ignore
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

const output = __ENV.OUTPUT;

function handleSummary(data: Record<string, unknown>): Record<string, unknown> {
  const result: Record<string, unknown> = {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
  };
  if (output != null) {
    result[output] = JSON.stringify(data);
  }
  return result;
}

export default handleSummary;
