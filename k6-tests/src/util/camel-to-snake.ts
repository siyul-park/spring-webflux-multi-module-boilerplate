function camelToSnake(value: string): string;
function camelToSnake(value: Record<string, unknown>): Record<string, unknown>;

function camelToSnake(value: string | Record<string, unknown>): string | Record<string, unknown> {
  if (value == null) {
    return value;
  }

  if (typeof value === 'string') {
    return value.replace(/[\w]([A-Z])/g, (m) => {
      return m[0] + '_' + m[1];
    }).toLowerCase();
  }

  const result: Record<string, unknown> = {};
  Array.from(Object.entries(value)).forEach(([key, value]) => {
    let converted = value;
    if (value instanceof Array) {
      converted = value.map((it) => camelToSnake(it));
    } else if (typeof value === 'object') {
      converted = camelToSnake(value as Record<string, unknown>);
    }

    result[camelToSnake(key)] = converted;
  });

  return result;
}

export default camelToSnake;
