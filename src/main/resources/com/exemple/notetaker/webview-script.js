(function () {
  function waitForHljs() {
    if (typeof hljs !== "undefined" && hljs.highlightAll) {
      hljs.highlightAll();
      if (typeof hljs.initLineNumbersOnLoad === "function") {
        hljs.initLineNumbersOnLoad({ singleLine: true });
      }
      addCopyButtons();
    } else {
      setTimeout(waitForHljs, 100);
    }
  }

  function copyText(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      return navigator.clipboard.writeText(text).catch(function (e) {
        return fallbackCopy(text);
      });
    } else {
      return fallbackCopy(text);
    }
  }

  function fallbackCopy(text) {
    var textarea = document.createElement("textarea");
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
    return Promise.resolve();
  }

  function addCopyButtons() {
    document.querySelectorAll("pre").forEach(function (pre) {
      if (pre.querySelector(".copy-button")) return;
      var button = document.createElement("button");
      button.className = "copy-button";
      button.textContent = "Copy";
      button.onclick = function () {
        var code = pre.querySelector("code");
        var text = code ? code.innerText : pre.innerText;
        copyText(text)
          .then(function () {
            button.textContent = "✔️";
            button.classList.add("copied");
            setTimeout(function () {
              button.textContent = "Copy";
              button.classList.remove("copied");
            }, 2000);
          })
          .catch(function (err) {
            console.error("Copy failed", err);
            button.textContent = "Erreur";
            setTimeout(function () {
              button.textContent = "Copy";
            }, 1500);
          });
      };
      pre.style.position = "relative";
      pre.appendChild(button);
    });
  }

  waitForHljs();
})();
