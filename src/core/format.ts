export function formatPrice(value: number | null, currency = 'EUR') {
  if (value === null) return 'Price on request';
  return new Intl.NumberFormat('en-DE', { style: 'currency', currency, maximumFractionDigits: 0 }).format(value);
}

export function formatArea(value: number | null) {
  return value ? `${value} m²` : '—';
}

export function formatDateTime(value: string | null) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('en-GB', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatRelativeDays(value: string | null) {
  if (!value) return 'Unknown';
  const diffDays = Math.max(0, Math.floor((Date.now() - new Date(value).getTime()) / 86400000));
  if (diffDays == 0) return 'Today';
  if (diffDays == 1) return '1 day ago';
  return `${diffDays} days ago`;
}
