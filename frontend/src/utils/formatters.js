export const formatPercent = (value, decimals = 2) => {
  if (value === null || value === undefined || isNaN(value)) return '0%';
  return Number(value).toFixed(decimals) + '%';
};

export const formatDecimal = (value, decimals = 2) => {
  if (value === null || value === undefined || isNaN(value)) return '0';
  return Number(value).toFixed(decimals);
};

export const formatTime = (value, decimals = 1) => {
  if (value === null || value === undefined || isNaN(value)) return '-';
  return Number(value).toFixed(decimals) + 's';
};