function snakeToCamel(value: string): string;
function snakeToCamel(value: Record<string, unknown>): Record<string, unknown>;

function snakeToCamel(value: string | Record<string, unknown>): string | Record<string, unknown> {
  if (value == null) {
    return value;
  }

  if (typeof value === 'string') {
    return value.toLowerCase().replace(/([-_][a-z])/g, group =>
      group
        .toUpperCase()
        .replace('-', '')
        .replace('_', ''),
    );
  }

  const result: Record<string, unknown> = {};
  Array.from(Object.entries(value)).forEach(([key, value]) => {
    let converted = value;
    if (value instanceof Array) {
      converted = value.map((it) => snakeToCamel(it));
    } else if (typeof value === 'object' && value != null) {
      converted = snakeToCamel(value as Record<string, unknown>);
    }

    result[snakeToCamel(key)] = converted;
  });

  return result;
}

export default snakeToCamel;
