
const WebSocket = require("ws");

const socket = new WebSocket("ws://localhost:8080/ws/logs");

socket.on("open", () => {
    console.log("SUCCESS: WebSocket connected!");
    socket.close();
});

socket.on("message", (data) => {
    console.log("Received:", data.toString());
});

socket.on("error", (error) => {
    console.error("WebSocket connection failed:", error.message);
});

socket.on("close", (code) => {
    console.log("Connection closed. Code:", code);
});
