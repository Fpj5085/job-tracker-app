# A3: Next.js standalone image. node_modules is copied from the host because the
# local network MITM proxy makes in-container npm installs unreliable; the two
# linux-musl native binaries are added explicitly for the alpine runtime.
FROM docker.m.daocloud.io/library/node:22-alpine AS build
WORKDIR /build
ARG NEXT_PUBLIC_API_URL=http://localhost:8081
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
COPY . .
RUN npm install --no-save --fetch-retries=8 --fetch-retry-mintimeout=20000 lightningcss-linux-x64-musl @next/swc-linux-x64-musl || true
RUN npm run build

FROM docker.m.daocloud.io/library/node:22-alpine
WORKDIR /app
ENV NODE_ENV=production
ENV HOSTNAME=0.0.0.0
ENV PORT=3000
COPY --from=build /build/.next/standalone ./
COPY --from=build /build/.next/static ./.next/static
EXPOSE 3000
CMD ["node", "server.js"]
