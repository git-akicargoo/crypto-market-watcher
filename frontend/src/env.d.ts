/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly MODE: string;
  readonly VITE_ACTIVE_PROFILE: string;
  
  readonly VITE_DEV_WS_PROTOCOL: string;
  readonly VITE_DEV_WS_HOST: string;
  readonly VITE_DEV_WS_PORT: string;
  readonly VITE_DEV_WS_ENDPOINT: string;
  readonly VITE_DEV_LOGGING_LEVEL: string;

  readonly VITE_PROD_WS_PROTOCOL: string;
  readonly VITE_PROD_WS_HOST: string;
  readonly VITE_PROD_WS_PORT: string;
  readonly VITE_PROD_WS_ENDPOINT: string;
  readonly VITE_PROD_LOGGING_LEVEL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
} 