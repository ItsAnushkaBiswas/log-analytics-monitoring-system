
const WS_URL = "ws://localhost:8081/ws/logs";

export function connectLiveTail({ onLog, onStatus, onError } = {}) {
  let socket;
  let reconnectTimer;
  let manuallyClosed = false;
  let reconnectDelay = 1000;

  const setStatus = (status) => onStatus?.(status);

  const connect = () => {
    if (manuallyClosed) return;

    setStatus("connecting");
    socket = new WebSocket(WS_URL);

    socket.onopen = () => {
      reconnectDelay = 1000;
      setStatus("connected");
    };

    socket.onmessage = (event) => {
      try {
        const log = JSON.parse(event.data);
        onLog?.(log);
      } catch (error) {
        onError?.(error);
      }
    };

    socket.onerror = () => {
      onError?.(new Error("Live Tail WebSocket connection error"));
    };

    socket.onclose = () => {
      if (manuallyClosed) {
        setStatus("disconnected");
        return;
      }

      setStatus("reconnecting");

      reconnectTimer = setTimeout(() => {
        reconnectDelay = Math.min(reconnectDelay * 2, 10000);
        connect();
      }, reconnectDelay);
    };
  };

  connect();

  return () => {
    manuallyClosed = true;
    clearTimeout(reconnectTimer);
    socket?.close();
  };
}
