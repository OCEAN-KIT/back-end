package com.ocean.piuda.admin.report.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.openhtmltopdf.extend.FSSupplier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportPdfService {

    private final Parser mdParser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    private static FSSupplier<InputStream> cp(String path) {
        return () -> {
            try {
                return new ClassPathResource(path).getInputStream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    public byte[] renderMarkdownToPdf(String markdown, String title) {
        try {
            String safeMarkdown = stripEmoji(markdown == null ? "" : markdown);
            String safeTitle = stripEmoji(title);

            String htmlBody = htmlRenderer.render(mdParser.parse(safeMarkdown));
            String html = wrapHtml(htmlBody, safeTitle);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);

            builder.useFont(cp("fonts/NotoSansKR-Regular.ttf"), "NotoSansKR", 400,
                    PdfRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(cp("fonts/NotoSansKR-Bold.ttf"), "NotoSansKR", 700,
                    PdfRendererBuilder.FontStyle.NORMAL, true);

            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("PDF 렌더링 실패: {}", e.getMessage(), e);
            throw new RuntimeException("REPORT_PDF_RENDER_FAILED", e);
        }
    }

    private String wrapHtml(String body, String title) {
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return ("""
    <!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="ko">
    <head>
      <meta charset="UTF-8" />
      <style>
        @page { size: A4; margin: 18mm; }

        body {
          font-family: "NotoSansKR", sans-serif;
          font-size: 11pt;
          line-height: 1.6;
          color: #111;
        }

        h1 { font-size: 20pt; margin: 0 0 10pt 0; font-weight: 700; }
        h2 { font-size: 15pt; margin: 16pt 0 8pt 0; font-weight: 700; }
        h3 { font-size: 13pt; margin: 14pt 0 6pt 0; font-weight: 700; }

        p { margin: 6pt 0; }

        /*  리스트 bullet 크기/모양 커스텀 (기본 마커 대신 ::before 사용) */
        ul, ol {
          margin: 6pt 0 6pt 0;
          padding-left: 0;
        }

        ul { list-style: none; }
        ul > li {
          position: relative;
          padding-left: 12pt;   /* bullet + 간격 */
          margin: 2pt 0;
        }
        ul > li::before {
          content: "•";
          position: absolute;
          left: 0;
          top: 0;
          font-size: 8.5pt;     /*  bullet 크기 */
          line-height: 1.6;     /* 텍스트 라인과 맞춤 */
        }

        /* 중첩 ul은 더 작은 마커 */
        ul ul > li { padding-left: 11pt; }
        ul ul > li::before {
          content: "◦";
          font-size: 8pt;
          line-height: 1.6;
        }

        /* 번호 목록은 기본 유지 */
        ol { padding-left: 18pt; }
        ol li { margin: 2pt 0; }

        code, pre { font-family: monospace; font-size: 10pt; }
        pre {
          background: #f6f6f6;
          padding: 10pt;
          border-radius: 6pt;
          white-space: pre-wrap;
        }

        table { width: 100%%; border-collapse: collapse; margin: 10pt 0; }
        th, td { border: 1px solid #ddd; padding: 6pt; }
        th { background: #f2f2f2; }

        .meta { font-size: 9.5pt; color: #555; margin-bottom: 12pt; }
      </style>
      <title>%s</title>
    </head>
    <body>
      <div class="meta">생성일시: %s</div>
      %s
    </body>
    </html>
    """).formatted(escapeHtml(title), generatedAt, body).strip();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String stripEmoji(String s) {
        if (s == null || s.isBlank()) return s;

        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);

            if (isEmojiLike(cp)) {
                // 필요하면 공백 하나로 치환하고 싶으면 아래 주석 해제
                // out.append(' ');
                continue;
            }
            out.appendCodePoint(cp);
        }
        return out.toString();
    }

    /**
     * “대부분의” 이모지/이모지 조합을 제거하기 위한 범위 체크
     * (표시 안 해도 되는 목적이면 이 정도가 실무에서 가장 무난합니다)
     */
    private static boolean isEmojiLike(int cp) {
        // variation selectors (emoji 스타일 선택), ZWJ(결합자), keycap 결합자
        if (cp == 0xFE0E || cp == 0xFE0F || cp == 0x200D || cp == 0x20E3) return true;

        // skin tone modifiers
        if (cp >= 0x1F3FB && cp <= 0x1F3FF) return true;

        // flags (regional indicator symbols)
        if (cp >= 0x1F1E6 && cp <= 0x1F1FF) return true;

        // Misc symbols / dingbats (♻️ ☀️ ✨ 같은 것들)
        if (cp >= 0x2600 && cp <= 0x26FF) return true;
        if (cp >= 0x2700 && cp <= 0x27BF) return true;

        // Main emoji blocks
        if (cp >= 0x1F300 && cp <= 0x1F5FF) return true; // pictographs
        if (cp >= 0x1F600 && cp <= 0x1F64F) return true; // emoticons
        if (cp >= 0x1F680 && cp <= 0x1F6FF) return true; // transport
        if (cp >= 0x1F700 && cp <= 0x1F77F) return true; // alchemical
        if (cp >= 0x1F780 && cp <= 0x1F7FF) return true; // geometric ext
        if (cp >= 0x1F800 && cp <= 0x1F8FF) return true; // arrows-c
        if (cp >= 0x1F900 && cp <= 0x1F9FF) return true; // supplemental
        if (cp >= 0x1FA00 && cp <= 0x1FA6F) return true; // chess etc
        if (cp >= 0x1FA70 && cp <= 0x1FAFF) return true; // extended-a

        return false;
    }

}
