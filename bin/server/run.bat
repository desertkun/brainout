call java -XX:-OmitStackTraceInFastThrow -server -Xms64m -Xmx512m -XX:MaxHeapFreeRatio=25 -XX:MinHeapFreeRatio=25 -jar brainout-server.jar %*
