const tls = require('tls');
const net = require('net');

class ElectrumClient {
  constructor(host, port, useTls = true) {
    this.host = host; this.port = port; this.useTls = useTls;
    this.id = 0; this.pending = new Map(); this.buf = ''; this.ready = false;
  }
  connect() {
    return new Promise((resolve, reject) => {
      const opts = { host: this.host, port: this.port, rejectUnauthorized: false };
      this.sock = this.useTls ? tls.connect(opts) : net.connect(opts);
      this.sock.setEncoding('utf8');
      this.sock.on('secureConnect', () => { this.ready = true; resolve(); });
      this.sock.on('connect', () => { if (!this.useTls) { this.ready = true; resolve(); } });
      this.sock.on('error', e => { reject(e); for (const p of this.pending.values()) p.reject(e); this.pending.clear(); });
      this.sock.on('data', chunk => {
        this.buf += chunk;
        let idx;
        while ((idx = this.buf.indexOf('\n')) >= 0) {
          const line = this.buf.slice(0, idx); this.buf = this.buf.slice(idx + 1);
          if (!line.trim()) continue;
          let obj;
          try { obj = JSON.parse(line); } catch (e) { console.error('bad json', line.slice(0, 200)); continue; }
          const arr = Array.isArray(obj) ? obj : [obj];
          for (const o of arr) {
            if (o.id === undefined) continue;
            const p = this.pending.get(o.id);
            if (!p) continue;
            this.pending.delete(o.id);
            if (o.error) p.reject(new Error(JSON.stringify(o.error)));
            else p.resolve(o.result);
          }
        }
      });
    });
  }
  request(method, params) {
    const id = ++this.id;
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject });
      this.sock.write(JSON.stringify({ id, method, params }) + '\n');
    });
  }
  batch(calls) {
    const reqs = calls.map(c => ({ id: ++this.id, method: c.method, params: c.params }));
    return new Promise((resolve, reject) => {
      for (const r of reqs) this.pending.set(r.id, { resolve, reject });
      this.sock.write(JSON.stringify(reqs) + '\n');
    }).then(() => Promise.all(reqs.map(r => this._collect(r.id))));
  }
  // resolved values are stored via a separate map because Promise.all on pending resolves individually
  close() { this.sock.end(); }
}

// Simpler batch: override to collect results into an array in order.
class EClient extends ElectrumClient {
  batch(calls) {
    const reqs = calls.map(c => ({ id: ++this.id, method: c.method, params: c.params }));
    const results = new Array(reqs.length);
    return new Promise((resolve, reject) => {
      let remaining = reqs.length;
      for (let i = 0; i < reqs.length; i++) {
        this.pending.set(reqs[i].id, {
          resolve: v => { results[i] = v; if (--remaining === 0) resolve(results); },
          reject: e => { if (--remaining === 0) reject(e); }
        });
      }
      this.sock.write(JSON.stringify(reqs) + '\n');
    });
  }
}

module.exports = { EClient };
