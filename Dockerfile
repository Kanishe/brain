# --- build stage: compile the Java CLI to a fat jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
COPY templates ./templates
RUN mvn -B -q -DskipTests package

# --- runtime stage: Node (for Claude Code) + JRE (for the brain CLI) ---
FROM node:20-slim
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8

# graft a JRE 21 from the Temurin image (the agent is Node; the tools are Java)
COPY --from=eclipse-temurin:21-jre /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"

RUN apt-get update \
    && apt-get install -y --no-install-recommends git \
    && rm -rf /var/lib/apt/lists/*

RUN npm install -g @anthropic-ai/claude-code

# brain CLI: fat jar + wrapper on PATH
COPY --from=build /build/target/brain-tools.jar /opt/brain/brain-tools.jar
COPY bin/brain /usr/local/bin/brain
RUN chmod +x /usr/local/bin/brain

# conventions + templates (read by the agent), and instructions/skills on the
# Claude Code discovery path (~/.claude)
COPY conventions/ /opt/brain/conventions/
COPY templates/ /opt/brain/templates/
COPY CLAUDE.md /root/.claude/CLAUDE.md
COPY skills/ /root/.claude/skills/

# startup bootstrap: create brain areas after /brain is mounted, then run claude
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

VOLUME ["/brain"]
WORKDIR /brain
ENTRYPOINT ["docker-entrypoint.sh"]
