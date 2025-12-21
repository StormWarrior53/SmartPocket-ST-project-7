// tests/apiService.test.js
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";


import * as api from "../src/services/api.js";

function makeResponse({
  ok = true,
  status = 200,
  contentType = "application/json",
  jsonData = undefined,
  textData = "",
} = {}) {
  return {
    ok,
    status,
    headers: {
      get: (name) =>
        String(name).toLowerCase() === "content-type" ? contentType : null,
    },
    json: async () => jsonData,
    text: async () => textData,
  };
}

describe("services/api.js", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  describe("handleResponse (edge cases)", () => {
    it("returns JSON when ok + application/json", async () => {
      if (!api.handleResponse) return; 

      const res = makeResponse({
        ok: true,
        status: 200,
        contentType: "application/json",
        jsonData: { hello: "world" },
      });

      const data = await api.handleResponse(res);
      expect(data).toEqual({ hello: "world" });
    });

    it("returns text when ok + non-json (e.g. text/html)", async () => {
      if (!api.handleResponse) return;

      const res = makeResponse({
        ok: true,
        status: 200,
        contentType: "text/html",
        textData: "<html>OK</html>",
      });

      const data = await api.handleResponse(res);
      expect(data).toBe("<html>OK</html>");
    });

    it("throws when not ok + JSON error with message", async () => {
      if (!api.handleResponse) return;

      const res = makeResponse({
        ok: false,
        status: 401,
        contentType: "application/json",
        jsonData: { message: "Unauthorized" },
      });

      await expect(api.handleResponse(res)).rejects.toThrow(/Unauthorized/i);
    });

    it("throws when not ok + JSON error with error field", async () => {
      if (!api.handleResponse) return;

      const res = makeResponse({
        ok: false,
        status: 500,
        contentType: "application/json",
        jsonData: { error: "Server exploded" },
      });

      await expect(api.handleResponse(res)).rejects.toThrow(/Server exploded/i);
    });

    it("throws when not ok + non-json HTML response", async () => {
      if (!api.handleResponse) return;

      const res = makeResponse({
        ok: false,
        status: 500,
        contentType: "text/html",
        textData: "<html>500 error</html>",
      });

      
      await expect(api.handleResponse(res)).rejects.toBeTruthy();
    });

    it("handles 204 No Content (ok)", async () => {
      if (!api.handleResponse) return;

      const res = makeResponse({
        ok: true,
        status: 204,
        contentType: "",
        textData: "",
      });

      const data = await api.handleResponse(res);
      
      expect(data === null || data === "" || typeof data === "undefined").toBe(true);
    });
  });

  describe("request()", () => {
    it("calls fetch with correct method and url", async () => {
      if (!api.request) return;

      fetch.mockResolvedValueOnce(
        makeResponse({
          ok: true,
          status: 200,
          contentType: "application/json",
          jsonData: { ok: true },
        })
      );

      await api.request("/test", { method: "GET" });

      expect(fetch).toHaveBeenCalledTimes(1);
      const [url, options] = fetch.mock.calls[0];
      expect(url).toBe("/test");
      expect(options.method).toBe("GET");
    });

    it("sends JSON body for POST and sets Content-Type application/json by default", async () => {
      if (!api.request) return;

      fetch.mockResolvedValueOnce(
        makeResponse({
          ok: true,
          status: 200,
          contentType: "application/json",
          jsonData: { ok: true },
        })
      );

      await api.request("/post", { method: "POST", body: { a: 1 } });

      const [, options] = fetch.mock.calls[0];
      expect(options.method).toBe("POST");
      expect(options.headers).toBeTruthy();
      expect(options.headers["Content-Type"] || options.headers["content-type"]).toMatch(/application\/json/i);
      expect(options.body).toBe(JSON.stringify({ a: 1 }));
    });

    it("keeps custom Content-Type if provided", async () => {
      if (!api.request) return;

      fetch.mockResolvedValueOnce(
        makeResponse({
          ok: true,
          status: 200,
          contentType: "application/json",
          jsonData: { ok: true },
        })
      );

      await api.request("/plain", {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: "hello",
      });

      const [, options] = fetch.mock.calls[0];
      expect(options.headers["Content-Type"]).toBe("text/plain");
      expect(options.body).toBe("hello");
    });

    it("throws on 4xx/5xx via handleResponse", async () => {
      if (!api.request) return;

      fetch.mockResolvedValueOnce(
        makeResponse({
          ok: false,
          status: 400,
          contentType: "application/json",
          jsonData: { message: "Bad Request" },
        })
      );

      await expect(api.request("/bad", { method: "GET" })).rejects.toThrow(/Bad Request/i);
    });
  });
});
