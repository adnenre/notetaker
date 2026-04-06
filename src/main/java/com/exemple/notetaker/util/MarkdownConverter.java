package com.exemple.notetaker.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MarkdownConverter {

        private static final Parser parser;
        private static final HtmlRenderer renderer;
        private static final String HIGHLIGHT_CSS;
        private static final String HIGHLIGHT_JS;
        private static final String LINE_NUMBERS_JS;

        static {
                parser = Parser.builder()
                                .extensions(Arrays.asList(
                                                TablesExtension.create(),
                                                AutolinkExtension.create(),
                                                StrikethroughExtension.create()))
                                .build();
                renderer = HtmlRenderer.builder()
                                .extensions(Arrays.asList(
                                                TablesExtension.create(),
                                                AutolinkExtension.create(),
                                                StrikethroughExtension.create()))
                                .build();

                HIGHLIGHT_CSS = loadResource("/com/exemple/notetaker/highlight.css");
                HIGHLIGHT_JS = loadResource("/com/exemple/notetaker/highlight.js");
                LINE_NUMBERS_JS = loadResource("/com/exemple/notetaker/highlight-line-numbers.js");
        }

        private static String loadResource(String path) {
                try (InputStream is = MarkdownConverter.class.getResourceAsStream(path)) {
                        if (is == null) {
                                System.err.println("Resource not found: " + path);
                                return "";
                        }
                        try (BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(is, StandardCharsets.UTF_8))) {
                                return reader.lines().collect(Collectors.joining("\n"));
                        }
                } catch (Exception e) {
                        System.err.println("Failed to load resource: " + path);
                        e.printStackTrace();
                        return "";
                }
        }

        public static String toHtml(String markdown) {
                if (markdown == null || markdown.isEmpty())
                        return "";
                Node document = parser.parse(markdown);
                String htmlContent = renderer.render(document);
                return wrapWithHtmlTemplate(htmlContent);
        }

        private static String wrapWithHtmlTemplate(String content) {
                StringBuilder sb = new StringBuilder();
                sb.append("<!DOCTYPE html>\n")
                                .append("<html>\n")
                                .append("<head>\n")
                                .append("    <meta charset=\"UTF-8\">\n")
                                .append("    <style>\n")
                                .append("        body {\n")
                                .append("            font-family: 'Segoe UI', Arial, sans-serif;\n")
                                .append("            margin: 20px;\n")
                                .append("            line-height: 1.5;\n")
                                .append("            background-color: #2b2b2b;\n")
                                .append("            color: #e0e0e0;\n")
                                .append("        }\n")
                                .append("        pre {\n")
                                .append("            border: solid 1px #cdcdcd;\n")
                                .append("            border-radius: 20px;\n")
                                .append("            overflow-x: auto;\n")
                                .append("            position: relative;\n")
                                .append("            padding: 2px;\n")
                                .append("            background-color: #1e1e1e;\n")
                                .append("        }\n")
                                .append("        code {\n")
                                .append("            font-family: 'Courier New', monospace;\n")
                                .append("        }\n")
                                .append("        blockquote {\n")
                                .append("            border-left: 4px solid #00bfff;\n")
                                .append("            margin: 10px 0;\n")
                                .append("            padding-left: 15px;\n")
                                .append("            color: #b0b0b0;\n")
                                .append("        }\n")
                                .append("        table {\n")
                                .append("            border-collapse: collapse;\n")
                                .append("            width: 100%;\n")
                                .append("        }\n")
                                .append("        th, td {\n")
                                .append("            border: 1px solid transparent;\n")
                                .append("            padding: 8px;\n")
                                .append("            text-align: left;\n")
                                .append("        }\n")
                                .append("        th {\n")
                                .append("            background-color: #3a3a3a;\n")
                                .append("        }\n")
                                .append("        .hljs-ln-numbers {\n")
                                .append("            padding-right: 15px !important;\n")
                                .append("            text-align: right;\n")
                                .append("            user-select: none;\n")
                                .append("        }\n")
                                .append("        .copy-button {\n")
                                .append("            position: absolute;\n")
                                .append("            top: 5px;\n")
                                .append("            right: 5px;\n")
                                .append("            background-color: #3a3a3a;\n")
                                .append("            color: white;\n")
                                .append("            border: none;\n")
                                .append("            border-radius: 4px;\n")
                                .append("            padding: 4px 8px;\n")
                                .append("            font-size: 12px;\n")
                                .append("            cursor: pointer;\n")
                                .append("            opacity: 0.7;\n")
                                .append("            transition: opacity 0.2s;\n")
                                .append("            font-family: 'Segoe UI', Arial, sans-serif;\n")
                                .append("        }\n")
                                .append("        .copy-button:hover {\n")
                                .append("            opacity: 1;\n")
                                .append("            background-color: #555;\n")
                                .append("        }\n")
                                .append("        .copy-button.copied {\n")
                                .append("            background-color: #4caf50;\n")
                                .append("            opacity: 1;\n")
                                .append("        }\n")
                                .append("    </style>\n")
                                .append("    <style>").append(HIGHLIGHT_CSS).append("</style>\n")
                                .append("    <script>").append(HIGHLIGHT_JS).append("</script>\n")
                                .append("    <script>").append(LINE_NUMBERS_JS).append("</script>\n")
                                .append("    <script>\n")
                                .append("        (function() {\n")
                                .append("            function addCopyButtons() {\n")
                                .append("                var pres = document.querySelectorAll('pre');\n")
                                .append("                for (var i = 0; i < pres.length; i++) {\n")
                                .append("                    var pre = pres[i];\n")
                                .append("                    if (pre.querySelector('.copy-button')) continue;\n")
                                .append("                    var button = document.createElement('button');\n")
                                .append("                    button.className = 'copy-button';\n")
                                .append("                    button.textContent = 'Copy';\n")
                                .append("                    (function(pre, btn) {\n")
                                .append("                        btn.onclick = function() {\n")
                                .append("                            var code = pre.querySelector('code');\n")
                                .append("                            var text = code ? code.innerText : pre.innerText;\n")
                                .append("                            if (navigator.clipboard && navigator.clipboard.writeText) {\n")
                                .append("                                navigator.clipboard.writeText(text).then(function() {\n")
                                .append("                                    btn.textContent = '✔️';\n")
                                .append("                                    btn.classList.add('copied');\n")
                                .append("                                    setTimeout(function() {\n")
                                .append("                                        btn.textContent = 'Copy';\n")
                                .append("                                        btn.classList.remove('copied');\n")
                                .append("                                    }, 2000);\n")
                                .append("                                }).catch(function(e) { console.error('Clipboard error', e); });\n")
                                .append("                            } else {\n")
                                .append("                                var textarea = document.createElement('textarea');\n")
                                .append("                                textarea.value = text;\n")
                                .append("                                document.body.appendChild(textarea);\n")
                                .append("                                textarea.select();\n")
                                .append("                                document.execCommand('copy');\n")
                                .append("                                document.body.removeChild(textarea);\n")
                                .append("                                btn.textContent = '✔️';\n")
                                .append("                                btn.classList.add('copied');\n")
                                .append("                                setTimeout(function() {\n")
                                .append("                                    btn.textContent = 'Copy';\n")
                                .append("                                    btn.classList.remove('copied');\n")
                                .append("                                }, 2000);\n")
                                .append("                            }\n")
                                .append("                        };\n")
                                .append("                    })(pre, button);\n")
                                .append("                    pre.style.position = 'relative';\n")
                                .append("                    pre.appendChild(button);\n")
                                .append("                }\n")
                                .append("            }\n")
                                .append("            function run() {\n")
                                .append("                addCopyButtons();\n")
                                .append("                if (typeof hljs !== 'undefined') {\n")
                                .append("                    hljs.highlightAll();\n")
                                .append("                    if (typeof hljs.initLineNumbersOnLoad === 'function') hljs.initLineNumbersOnLoad({ singleLine: true });\n")
                                .append("                }\n")
                                .append("            }\n")
                                .append("            if (document.readyState === 'loading') {\n")
                                .append("                document.addEventListener('DOMContentLoaded', run);\n")
                                .append("            } else {\n")
                                .append("                run();\n")
                                .append("            }\n")
                                .append("        })();\n")
                                .append("    </script>\n")
                                .append("</head>\n")
                                .append("<body>\n")
                                .append(content).append("\n")
                                .append("</body>\n")
                                .append("</html>");
                return sb.toString();
        }
}