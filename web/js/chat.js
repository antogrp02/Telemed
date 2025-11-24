(function () {
    function scrollBottom(container) {
        if (container) {
            container.scrollTop = container.scrollHeight;
        }
    }

    function formatMessage(text) {
        if (typeof text !== "string") {
            return "";
        }

        const escaped = text.replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;");

        const urlRegex = /(https?:\/\/[\w\-._~:\/?#\[\]@!$&'()*+,;=%]+)/g;
        return escaped.replace(urlRegex, '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>');
    }

    function appendMessage(container, msg, labels) {
        if (!container || !msg)
            return;

        const wrapper = document.createElement("div");
        wrapper.className = "chat-msg-row " + (msg.mine ? "mine" : "other");

        const senderLabel = msg.mine ? labels.mine : labels.other;

        const box = document.createElement("div");

        const senderSpan = document.createElement("span");
        senderSpan.className = "chat-sender";
        senderSpan.textContent = senderLabel;

        const textDiv = document.createElement("div");
        textDiv.className = "chat-text";
        textDiv.innerHTML = formatMessage(msg.text || "");

        const time = document.createElement("div");
        time.className = "chat-time";

        try {
            const d = new Date(msg.sentAt);
            time.textContent = d.toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"});
        } catch (e) {
            time.textContent = "";
        }

        box.appendChild(senderSpan);
        box.appendChild(textDiv);
        box.appendChild(time);

        wrapper.appendChild(box);
        container.appendChild(wrapper);

        scrollBottom(container);
    }


    function setupSocket(opts, container, labels, input) {
        const proto = location.protocol === "https:" ? "wss://" : "ws://";
        const wsUrl = proto + location.host + (opts.contextPath || "") + "/ws/chat/" + opts.myId;

        const ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            scrollBottom(container);

            if (opts.enterChatOnOpen !== false && opts.otherId !== undefined && opts.otherId !== null) {
                ws.send(JSON.stringify({
                    type: "ENTER_CHAT",
                    otherUserId: opts.otherId
                }));
            }
        };

        ws.onmessage = (ev) => {
            const msg = JSON.parse(ev.data);

            if (msg.text !== undefined) {
                appendMessage(container, msg, labels);
                return;
            }

            if (msg.type === "READ_CONFIRM") {
                console.log("Messaggi segnati come letti.");
            }
        };

        ws.onerror = (e) => console.error("WebSocket error:", e);

        return ws;
    }

    window.initChat = function (options) {
        const opts = Object.assign({
            enterChatOnOpen: true,
            mineLabel: "Me",
            otherLabel: "Other"
        }, options || {});

        const container = document.querySelector(opts.chatContainerSelector);
        const input = document.querySelector(opts.inputSelector);
        const sendButton = opts.sendButtonSelector ? document.querySelector(opts.sendButtonSelector) : null;

        if (!container || !input) {
            console.error("Chat container o input non trovati.");
            return null;
        }

        const labels = {mine: opts.mineLabel, other: opts.otherLabel};
        const ws = setupSocket(opts, container, labels, input);

        function sendMessage() {
            const text = input.value.trim();
            if (!text || !ws || ws.readyState !== WebSocket.OPEN)
                return;

            ws.send(JSON.stringify({
                destId: opts.otherId,
                text: text
            }));

            input.value = "";
        }

        input.addEventListener("keyup", e => {
            if (e.key === "Enter")
                sendMessage();
        });

        if (sendButton) {
            sendButton.addEventListener("click", sendMessage);
        }

        return {sendMessage};
    };
})();
