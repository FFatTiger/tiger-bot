package com.fffattiger.wechatbot.shared.util;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.w3c.dom.Document;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xml.sax.InputSource;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MarkdownToImageConverter {

    /**
     * 将你的HTML渲染结果转换为图片
     */
    public static boolean convertHtmlToImage(String htmlContent, String outputPath,
                                             int width, String imageFormat) {
        try {
            // 1. 包装HTML为完整文档
            String fullHtml = wrapHtmlWithStyles(htmlContent, width);

            // 2. 解析HTML为DOM文档
            Document document = parseHtmlToDocument(fullHtml);

            // 3. 渲染为图片
            BufferedImage image = renderDocumentToImage(document, width);

            // 4. 保存图片
            File outputFile = new File(outputPath);


            return ImageIO.write(image, imageFormat, outputFile);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 包装HTML内容并添加样式
     */
    private static String wrapHtmlWithStyles(String htmlBody, int width) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                       <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                       <html xmlns="http://www.w3.org/1999/xhtml">
                
                       <head>
                         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                         <style type="text/css">
                           .margin-tb-zero,
                           .markdown-body ol ol,
                           .markdown-body ul ol,
                           .markdown-body ol ul,
                           .markdown-body ul ul,
                           .markdown-body ol ul ol,
                           .markdown-body ul ul ol,
                           .markdown-body ol ul ul,
                           .markdown-body ul ul ul {
                             margin-top: 0;
                             margin-bottom: 0;
                           }
                
                           .markdown-body {
                             font-family: "Helvetica Neue", Helvetica, "Segoe UI", Arial, freesans, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
                             font-size: 16px;
                             color: #333;
                             line-height: 1.6;
                             word-wrap: break-word;
                             /*padding: 45px;*/
                             padding: 5px;
                             background: #fff;
                             border: 1px solid #ddd;
                             -webkit-border-radius: 0 0 3px 3px;
                             border-radius: 0 0 3px 3px;
                           }
                
                           .markdown-body>*:first-child {
                             margin-top: 0 !important;
                           }
                
                           .markdown-body>*:last-child {
                             margin-bottom: 0 !important;
                           }
                
                           .markdown-body * {
                             -webkit-box-sizing: border-box;
                             -moz-box-sizing: border-box;
                             box-sizing: border-box;
                           }
                
                           .markdown-body h1,
                           .markdown-body h2,
                           .markdown-body h3,
                           .markdown-body h4,
                           .markdown-body h5,
                           .markdown-body h6 {
                             margin-top: 1em;
                             margin-bottom: 16px;
                             font-weight: bold;
                             line-height: 1.4;
                           }
                
                           .markdown-body p,
                           .markdown-body blockquote,
                           .markdown-body ul,
                           .markdown-body ol,
                           .markdown-body dl,
                           .markdown-body table,
                           .markdown-body pre {
                             margin-top: 0;
                             margin-bottom: 16px;
                           }
                
                           .markdown-body h1 {
                             margin: 0.67em 0;
                             padding-bottom: 0.3em;
                             font-size: 2.25em;
                             line-height: 1.2;
                             border-bottom: 1px solid #eee;
                           }
                
                           .markdown-body h2 {
                             padding-bottom: 0.3em;
                             font-size: 1.75em;
                             line-height: 1.225;
                             border-bottom: 1px solid #eee;
                           }
                
                           .markdown-body h3 {
                             font-size: 1.5em;
                             line-height: 1.43;
                           }
                
                           .markdown-body h4 {
                             font-size: 1.25em;
                           }
                
                           .markdown-body h5 {
                             font-size: 1em;
                           }
                
                           .markdown-body h6 {
                             font-size: 1em;
                             color: #777;
                           }
                
                           .markdown-body ol,
                           .markdown-body ul {
                             padding-left: 2em;
                           }
                
                           .markdown-body ol ol,
                           .markdown-body ul ol {
                             list-style-type: lower-roman;
                           }
                
                           .markdown-body ol ul,
                           .markdown-body ul ul {
                             list-style-type: circle;
                           }
                
                           .markdown-body ol ul ul,
                           .markdown-body ul ul ul {
                             list-style-type: square;
                           }
                
                           .markdown-body ol {
                             list-style-type: decimal;
                           }
                
                           .markdown-body ul {
                             list-style-type: disc;
                           }
                
                           .markdown-body blockquote {
                             margin-left: 0;
                             margin-right: 0;
                             padding: 0 15px;
                             color: #777;
                             border-left: 4px solid #ddd;
                           }
                
                           .markdown-body table {
                             display: block;
                             width: 100%;
                             overflow: auto;
                             word-break: normal;
                             word-break: keep-all;
                             border-collapse: collapse;
                             border-spacing: 0;
                           }
                
                           .markdown-body table tr {
                             background-color: #fff;
                             border-top: 1px solid #ccc;
                           }
                
                           .markdown-body table tr:nth-child(2n) {
                             background-color: #f8f8f8;
                           }
                
                           .markdown-body table th,
                           .markdown-body table td {
                             padding: 6px 13px;
                             border: 1px solid #ddd;
                           }
                
                           .markdown-body pre {
                             word-wrap: normal;
                             padding: 16px;
                             overflow: auto;
                             font-size: 85%;
                             line-height: 1.45;
                             background-color: #f7f7f7;
                             -webkit-border-radius: 3px;
                             border-radius: 3px;
                           }
                
                           .markdown-body pre code {
                             display: inline;
                             max-width: initial;
                             padding: 0;
                             margin: 0;
                             overflow: initial;
                             font-size: 100%;
                             line-height: inherit;
                             word-wrap: normal;
                             white-space: pre;
                             border: 0;
                             -webkit-border-radius: 3px;
                             border-radius: 3px;
                             background-color: transparent;
                           }
                
                           .markdown-body pre code:before,
                           .markdown-body pre code:after {
                             content: normal;
                           }
                
                           .markdown-body code {
                             font-family: Consolas, "Liberation Mono", Menlo, Courier, monospace;
                             padding: 0;
                             padding-top: 0.2em;
                             padding-bottom: 0.2em;
                             margin: 0;
                             font-size: 85%;
                             background-color: rgba(0, 0, 0, 0.04);
                             -webkit-border-radius: 3px;
                             border-radius: 3px;
                           }
                
                           .markdown-body code:before,
                           .markdown-body code:after {
                             letter-spacing: -0.2em;
                             content: "\\00a0";
                           }
                
                           .markdown-body a {
                             color: #4078c0;
                             text-decoration: none;
                             background: transparent;
                           }
                
                           .markdown-body img {
                             max-width: 100%;
                             max-height: 100%;
                             -webkit-border-radius: 4px;
                             border-radius: 4px;
                             -webkit-box-shadow: 0 0 10px #555;
                             box-shadow: 0 0 10px #555;
                           }
                
                           .markdown-body strong {
                             font-weight: bold;
                           }
                
                           .markdown-body em {
                             font-style: italic;
                           }
                
                           .markdown-body del {
                             text-decoration: line-through;
                           }
                
                           .task-list-item {
                             list-style-type: none;
                           }
                
                           .task-list-item input {
                             font: 13px/1.4 Helvetica, arial, nimbussansl, liberationsans, freesans, clean, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
                             margin: 0 0.35em 0.25em -1.6em;
                             vertical-align: middle;
                           }
                
                           .task-list-item input[disabled] {
                             cursor: default;
                           }
                
                           .task-list-item input[type="checkbox"] {
                             -webkit-box-sizing: border-box;
                             -moz-box-sizing: border-box;
                             box-sizing: border-box;
                             padding: 0;
                           }
                
                           .task-list-item input[type="radio"] {
                             -webkit-box-sizing: border-box;
                             -moz-box-sizing: border-box;
                             box-sizing: border-box;
                             padding: 0;
                           }
                         </style>
                       </head>
                
                       <body>
                         <div class='markdown-body'>
                         """
                +htmlBody
                + """
                         </div>
                
                       </body>
                
                       </html>
            """;
    }

    /**
     * 解析HTML字符串为DOM文档
     */
    private static Document parseHtmlToDocument(String html) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);

        // 忽略DTD验证以提高性能
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder builder = factory.newDocumentBuilder();

        // 处理编码问题
        ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes("UTF-8"));
        InputSource inputSource = new InputSource(inputStream);
        inputSource.setEncoding("UTF-8");

        return builder.parse(inputSource);
    }

    /**
     * 渲染DOM文档为BufferedImage
     */
    private static BufferedImage renderDocumentToImage(Document document, int width) throws Exception {
        Graphics2DRenderer renderer = new Graphics2DRenderer();
        renderer.setDocument(document, null);

        // 先创建一个临时图像来测量内容高度
        BufferedImage tempImage = new BufferedImage(width, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG2d = tempImage.createGraphics();

        // 设置字体抗锯齿
        tempG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        tempG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // 布局文档以获取实际尺寸
        renderer.layout(tempG2d, new Dimension(width, 1000));
        Rectangle contentSize = renderer.getMinimumSize();
        tempG2d.dispose();

        // 计算实际需要的高度，添加一些缓冲
        int actualHeight = Math.max((int) contentSize.getHeight() + 50, 200);

        // 创建最终图像
        BufferedImage finalImage = new BufferedImage(width, actualHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D finalG2d = finalImage.createGraphics();

        // 设置高质量渲染选项
        finalG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        finalG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        finalG2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        finalG2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // 填充白色背景
        finalG2d.setColor(Color.WHITE);
        finalG2d.fillRect(0, 0, width, actualHeight);

        // 重新布局并渲染到最终图像
        renderer.layout(finalG2d, new Dimension(width, actualHeight));
        renderer.render(finalG2d);

        finalG2d.dispose();
        return finalImage;
    }

    /**
     * 结合你的代码的完整转换方法
     */
    public static boolean convertMarkdownToImage(String markdownText, String outputPath,
                                                 int width, String imageFormat) {
        try {
            // 使用你的commonmark代码
            java.util.List<Extension> extensions = java.util.List.of(TablesExtension.create());
            Parser parser = Parser.builder().extensions(extensions).build();
            Node document = parser.parse(markdownText);
            HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
            String htmlContent = renderer.render(document);

            // 转换为图片
            return convertHtmlToImage(htmlContent, outputPath, width, imageFormat);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从文件转换
     */
    public static boolean convertMarkdownFileToImage(String markdownFilePath, String outputPath,
                                                     int width, String imageFormat) {
        try {
            String markdownContent = Files.readString(Paths.get(markdownFilePath));
            return convertMarkdownToImage(markdownContent, outputPath, width, imageFormat);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
