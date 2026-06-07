export const apiClient = {
  async get(url: string) {
    const res = await fetch(url);
    return res.json();
  },
  async post(url: string, body: any) {
    const res = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    return res.json();
  }
};
