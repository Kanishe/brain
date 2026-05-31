FROM node:20-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends git \
    && rm -rf /var/lib/apt/lists/*

RUN npm install -g @anthropic-ai/claude-code

WORKDIR /opt/brain
COPY package.json package-lock.json ./
RUN npm install --omit=dev

COPY brain_tools/ ./brain_tools/
COPY conventions/ ./conventions/
COPY skills/ ./skills/
COPY templates/ ./templates/
COPY CLAUDE.md ./CLAUDE.md

# expose the `brain` CLI globally
RUN npm install -g .

# startup bootstrap: create brain areas after /brain is mounted, then run claude
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

VOLUME ["/brain"]
WORKDIR /brain
ENTRYPOINT ["docker-entrypoint.sh"]
