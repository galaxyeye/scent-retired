package org.qiwur.scent.data.builder;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

public class Html2Wiki {

	public static String STYLE_H1 = "";
	public static String STYLE_H2 = "";
	public static String STYLE_H3 = "";
	public static String STYLE_H4 = "";
	public static String STYLE_H1_END = "";
	public static String STYLE_H2_END = "";
	public static String STYLE_H3_END = "";
	public static String STYLE_H4_END = "";

	public static String convert(String html) throws Exception {
		String line = "";
		BufferedReader inReader = new BufferedReader(new StringReader(html));

		StringWriter sw = new StringWriter();
		PrintWriter outWriter = new PrintWriter(sw);

		while ((line = inReader.readLine()) != null) {
			line = line.replaceAll("<code>", "{{{");
			line = line.replaceAll("</code>", "}}}");
//			line = line.replaceAll("<blockquote>", "<code>");
//			line = line.replaceAll("</blockquote>", "</code>");
			line = line.replaceAll("<blockquote>", "{{{");
			line = line.replaceAll("</blockquote>", "}}}");

			line = line.replaceAll("<h1>", "=" + STYLE_H1);
			line = line.replaceAll("</h1>", STYLE_H1_END + "=");
			line = line.replaceAll("<h2>", "==" + STYLE_H2);
			line = line.replaceAll("</h2>", STYLE_H2_END + "==");
			line = line.replaceAll("<h3>", "===" + STYLE_H3);
			line = line.replaceAll("</h3>", STYLE_H3_END + "===");
			line = line.replaceAll("<h4>", "====" + STYLE_H4);
			line = line.replaceAll("</h4>", STYLE_H4_END + "====");

			line = line.replaceAll("<br />", "");

			if (line.indexOf("<pre>") == 0) continue;
			if (line.indexOf("</pre>") == 0) continue;

			line = line.replaceAll("&gt;", ">");
			line = line.replaceAll("&lt;", "<");

			if (line.indexOf("<table") == 0) continue;
			if (line.indexOf("</table") == 0) continue;
			if (line.indexOf("<tbody") == 0) continue;
			if (line.indexOf("</tbody") == 0) continue;
			line = line.replaceAll("<tr><td>", "||");
			line = line.replaceAll("</td></tr>", "||");
			line = line.replaceAll("</td><td>", "||");

			line = line.replaceAll("</li>", "");
			line = line.replaceAll("<li>", "  * ");
			if (line.trim().indexOf("<ul>") == 0) continue;
			if (line.trim().indexOf("</ul>") == 0) continue;

			line = line.replaceAll("<a\\s(.*?)>", "");
			line = line.replaceAll("</a>", "");

			outWriter.println(line);
		}

		return sw.toString();
	}

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

}
	