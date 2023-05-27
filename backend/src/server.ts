import Fastify, { fastify } from "fastify";
import cors from "@fastify/cors"
import multipart from "@fastify/multipart";
import { appRoutes } from "./routes";

const app = Fastify({
  bodyLimit: 1048576 * 100,
});

app.register(cors)
app.register(multipart);

app.register(appRoutes)

app.listen({port: 3333, host: "0.0.0.0"}, (error, address) => {
  if (error) {
    console.error(error);
    process.exit(1);
  }
  console.log(`HTTP server running on: ${address}`)
});