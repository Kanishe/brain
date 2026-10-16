# --- build stage: compile the Java CLI to a fat jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
COPY templates ./templates
RUN mvn -B -q -DskipTests package

# --- runtime stage: Node (for Claude Code) + JRE (for the brain CLI) ---
# node 22: skills@latest needs >=22.20 (fs/promises glob)
FROM node:22-slim
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8

# graft a JRE 21 from the Temurin image (the agent is Node; the tools are Java)
COPY --from=eclipse-temurin:21-jre /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"

RUN apt-get update \
    && apt-get install -y --no-install-recommends git ca-certificates curl \
    && rm -rf /var/lib/apt/lists/*

# gh (GitHub CLI) — official apt repo, arch picked up automatically via
# dpkg --print-architecture (https://github.com/cli/cli/blob/trunk/docs/install_linux.md)
RUN mkdir -p -m 755 /etc/apt/keyrings \
    && curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg -o /etc/apt/keyrings/githubcli-archive-keyring.gpg \
    && chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" > /etc/apt/sources.list.d/github-cli.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends gh \
    && rm -rf /var/lib/apt/lists/*

# glab (GitLab CLI) — no apt repo published upstream, so fetch the release
# .deb for the image's arch directly (asset names verified against the
# gitlab-org/cli releases API: glab_<version>_linux_<arch>.deb)
RUN GLAB_TAG=$(curl -fsSL https://gitlab.com/api/v4/projects/gitlab-org%2Fcli/releases/permalink/latest | grep -oP '"tag_name":"\K[^"]+') \
    && GLAB_VERSION="${GLAB_TAG#v}" \
    && curl -fsSL "https://gitlab.com/gitlab-org/cli/-/releases/${GLAB_TAG}/downloads/glab_${GLAB_VERSION}_linux_$(dpkg --print-architecture).deb" -o /tmp/glab.deb \
    && dpkg -i /tmp/glab.deb \
    && rm /tmp/glab.deb

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
# docker-compose bind-mounts ~/brain-claude over /root/.claude, which hides the
# two COPYs above at runtime. Keep a staged copy outside the mount;
# docker-entrypoint.sh syncs it into /root/.claude on every start.
COPY CLAUDE.md /opt/brain/claude/CLAUDE.md
COPY skills/ /opt/brain/claude/skills/
# hooks: managed settings live in /etc, outside the mount, and merge with the
# user's own settings.json
COPY hooks/ /opt/brain/hooks/
COPY claude/managed-settings.json /etc/claude-code/managed-settings.json

# third-party skills via the `skills` CLI (npx skills, vercel-labs/skills):
# one owner/repo per line in npx-skills.txt (comments/blank lines ignored).
# `-g` installs into $HOME/.claude/skills; @latest avoids older CLI versions
# that skip the ~/.claude symlink. HOME points at a scratch dir so the result
# can be copied (symlinks resolved) into /opt/brain/claude/skills, which
# docker-entrypoint.sh syncs past the ~/brain-claude mount.
COPY npx-skills.txt /tmp/npx-skills.txt
RUN set -e; \
    export HOME=/tmp/npx-home; mkdir -p "$HOME/.claude/skills"; \
    while IFS= read -r skill; do \
      case "$skill" in ''|'#'*) continue ;; esac; \
      npx --yes skills@latest add "$skill" -a claude-code -g -y; \
    done < /tmp/npx-skills.txt; \
    cp -rL "$HOME/.claude/skills/." /opt/brain/claude/skills/; \
    rm -rf /tmp/npx-skills.txt "$HOME" /root/.npm/_npx

# The image has no SSH client/keys, but the github plugin source resolves to
# git@github.com and fails with "ssh: not found". Force HTTPS for GitHub clones
# (public repo) so both the build-time install and runtime auto-update work.
RUN git config --global url."https://github.com/".insteadOf "git@github.com:" \
    && git config --global --add url."https://github.com/".insteadOf "ssh://git@github.com/"

# enable + preinstall the superpowers plugin so it is ready at container start.
# settings.json declares it (same mechanism as a host install); the build-time
# install pre-populates the plugin cache so it works offline / instantly.
COPY claude/settings.json /root/.claude/settings.json
RUN claude plugin marketplace add obra/superpowers-marketplace \
    && claude plugin install superpowers@superpowers-marketplace \
    || true

# startup bootstrap: create brain areas after /brain is mounted, then run claude
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

VOLUME ["/brain"]
WORKDIR /brain
ENTRYPOINT ["docker-entrypoint.sh"]
