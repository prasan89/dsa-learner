import axios from "axios";
import Cookies from "js-cookie";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

const api = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const token = Cookies.get("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let refreshing: Promise<string> | null = null;

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      const refreshToken = Cookies.get("refreshToken");
      if (!refreshToken) {
        Cookies.remove("accessToken");
        window.location.href = "/login";
        return Promise.reject(error);
      }
      original._retry = true;
      try {
        if (!refreshing) {
          refreshing = axios
            .post<{ accessToken: string; refreshToken: string }>(`${BASE_URL}/auth/refresh`, { refreshToken })
            .then((r) => {
              Cookies.set("accessToken", r.data.accessToken, { expires: 1 / 96 });
              if (r.data.refreshToken) {
                Cookies.set("refreshToken", r.data.refreshToken, { expires: 7 });
              }
              return r.data.accessToken;
            })
            .finally(() => { refreshing = null; });
        }
        const newToken = await refreshing;
        original.headers.Authorization = `Bearer ${newToken}`;
        return api(original);
      } catch {
        Cookies.remove("accessToken");
        Cookies.remove("refreshToken");
        window.location.href = "/login";
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  }
);

export default api;
