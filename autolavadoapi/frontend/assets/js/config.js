(() => {
  const samePortAsApi = window.location.port === "8085";
  if (!window.APP_API_BASE) {
    window.APP_API_BASE = samePortAsApi ? "" : "http://localhost:8085";
  }
  if (!window.APP_ASSET_BASE) {
    window.APP_ASSET_BASE = window.APP_API_BASE || "";
  }
})();

