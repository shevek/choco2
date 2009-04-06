package choco.visu.components.chart;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jfree.chart.JFreeChart;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public final class PdfExport {

	

	private PdfExport() {
		super();
	}
	/**
	 * Saves a chart to a PDF file.
	 *
	 * @param file the file.
	 * @param chart the chart.
	 * @param width the chart width.
	 * @param height the chart height.
	 */
	public static void saveChartAsPDF(File file,
			JFreeChart chart,
			int width,
			int height,
			FontMapper mapper) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		writeChartAsPDF(out, chart, width, height, mapper);
		out.close();
	}
	/**
	 * Writes a chart to an output stream in PDF format.
	 *
	 * @param out the output stream.
	 * @param chart the chart.
	 * @param width the chart width.
	 * @param height the chart height.
	 *
	 */
	public static void writeChartAsPDF(OutputStream out,
			JFreeChart chart,
			int width,
			int height,
			FontMapper mapper) throws IOException {
		Rectangle pagesize = new Rectangle(width, height);
		Document document = new Document(pagesize, 50, 50, 50, 50);
		try {
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.addAuthor("Choco Team");
			document.addSubject("Resource visualization");
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			Graphics2D g2 = tp.createGraphics(width, height, mapper);
			Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
			chart.draw(g2, r2D);
			g2.dispose();
			cb.addTemplate(tp, 0, 0);
		}
		catch (DocumentException de) {
			System.err.println(de.getMessage());
		}
		document.close();
	}

}

