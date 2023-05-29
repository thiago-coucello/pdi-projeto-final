import { FastifyInstance, FastifyReply, FastifyRequest } from "fastify";
import fs from "fs";
import path from "path";
import util from "util";
import { pipeline } from "stream";
import { execSync } from "child_process";

const pump = util.promisify(pipeline);

type QueryType = {
  task: "all" | "blood" | "dna";
  RBC: number;
  WBC: number;
};

export async function appRoutes(app: FastifyInstance) {
  app.post("/", async (request: FastifyRequest, reply: FastifyReply) => {
    // Receiving file from request
    const part = await request.file();
    let {task, RBC, WBC} = request.query as QueryType;

    if (!task) {
      task = "all";
    }

    if (!RBC) {
      RBC = 10;
    }

    if (!WBC) {
      WBC = 13;
    }
    
    if (part)  {
      const {file, filename} = part;
      const splittedFile = filename.split(".")!;
      const fileExtension = splittedFile[splittedFile?.length - 1];
      const savedFilename = path.resolve(__dirname, "..", "images", `originalImage.${fileExtension}`);
      const processedFilename = path.resolve(__dirname, "..", "images", `processedImage.png`);
      const process_folder = path.resolve(__dirname, "..", "..");
      const command = `cd ${process_folder} & executar "${savedFilename}" ${task} ${RBC} ${WBC}`;

      await pump(file, fs.createWriteStream(savedFilename));

      console.log(command);
      execSync(command);

      const returnStream = fs.createReadStream(processedFilename);
      return reply.send(returnStream);
    }
    else {
      return reply.code(400).send({
        message: "Nenhuma imagem enviada! Verifique..."
      });
    }
  });
}