// Centralized Enterprise API Service with Axios-style fetch wrapper, retries, and token refresh

import { API_BASE_URL } from '../constants/app.constants.js';

class ApiService {
  constructor() {
    this.baseUrl = API_BASE_URL;
  }

  getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    const token = localStorage.getItem('pubexchange_jwt_token');
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  }

  async request(endpoint, options = {}, retries = 2) {
    const url = `${this.baseUrl}${endpoint}`;
    const config = {
      ...options,
      headers: {
        ...this.getHeaders(),
        ...options.headers
      }
    };

    try {
      const response = await fetch(url, config);
      
      if (response.status === 401) {
        // Attempt Token Refresh
        const refreshed = await this.refreshToken();
        if (refreshed) {
          config.headers['Authorization'] = `Bearer ${localStorage.getItem('pubexchange_jwt_token')}`;
          return await (await fetch(url, config)).json();
        }
      }

      if (!response.ok) {
        throw new Error(`HTTP Error ${response.status}: ${response.statusText}`);
      }

      const text = await response.text();
      return text ? JSON.parse(text) : {};
    } catch (error) {
      if (retries > 0) {
        await new Promise(res => setTimeout(res, 500));
        return this.request(endpoint, options, retries - 1);
      }
      throw error;
    }
  }

  async refreshToken() {
    try {
      const res = await fetch(`${this.baseUrl}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: localStorage.getItem('pubexchange_refresh_token') })
      });
      if (res.ok) {
        const data = await res.json();
        if (data.token) {
          localStorage.setItem('pubexchange_jwt_token', data.token);
          return true;
        }
      }
    } catch (e) {}
    return false;
  }

  get(endpoint) { return this.request(endpoint, { method: 'GET' }); }
  post(endpoint, body) { return this.request(endpoint, { method: 'POST', body: JSON.stringify(body) }); }
  put(endpoint, body) { return this.request(endpoint, { method: 'PUT', body: JSON.stringify(body) }); }
  patch(endpoint, body) { return this.request(endpoint, { method: 'PATCH', body: JSON.stringify(body) }); }
  delete(endpoint) { return this.request(endpoint, { method: 'DELETE' }); }
}

export const apiService = new ApiService();
