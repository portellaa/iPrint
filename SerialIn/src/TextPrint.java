import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class TextPrint implements Printable {

	public static final int DEFAULT_FONT_SIZE = 12;
	public static final String DEFAULT_FONT_NAME = "Serif";
	
	private PrinterJob job;
	private String typeName;
	private int typeSize;
	private Font typeFont;
	private Font typeFontBold;
	
	private String[] body;
	
	public TextPrint()
	{
		this(DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE);
	}
	
	public TextPrint(String name, int size)
	{
		if (size < 3 || size > 127)
			throw new IllegalArgumentException("Type size out of range");
		
		typeName = name;
		typeSize = size;
		typeFont = new Font(typeName, Font.PLAIN, typeSize);
		typeFontBold = new Font(typeName, Font.BOLD, typeSize);
		job = null;
	}
	
	public void initialize()
	{
		job = PrinterJob.getPrinterJob();
	}
	
	public int getCharsPerLine()
	{
		if (job == null)
			initialize();

		PageFormat pf;
		pf = job.defaultPage();
		double width = pf.getImageableWidth(); // in 72nd of a pt
		double ptsize = typeFont.getSize();
		double ptwid = ptsize * 3 / 4;
		double cnt = (width / ptwid); 
		return (int)(Math.round(cnt));
	}
	
	public boolean startPrint(InputStream is, int size) throws PrinterException
	{
		if (job == null)
			initialize();
		
		job.setPrintable(this);
		
		if (size < 1)
			try {
				size = is.available();
			} catch (IOException e1) {
				System.out.println("ERROR: Could not convert file to string");
				return false;
			}
		System.out.println("Data available: " + size);
		
		byte[] buffer = new byte[size];
		
		try {
			is.read(buffer);
		} catch (IOException e1) {
			System.out.println("ERROR: Could not convert file to string");
			return false;
		}
		
		this.body = new String(buffer).split("\\n");
		
		System.out.println(this.body);
		
		job.print();
		job = null;
		
		return true;
	}

	@Override
	public int print(Graphics arg0, PageFormat arg1, int arg2)
			throws PrinterException {
		
		
		if (arg2 != 0)
			return NO_SUCH_PAGE;

		FontMetrics fm;
		arg0.setFont(typeFont);
		arg0.setColor(Color.black);
		fm = arg0.getFontMetrics();

		double x, y;
		x = arg1.getImageableX();
		y = arg1.getImageableY() + fm.getMaxAscent();

		// do the body
		if (body != null)
		{
			arg0.setFont(typeFont);
			for (int i = 0; i < body.length; i++)
			{
				arg0.drawString(body[i],(int)x,(int)y);
				y += fm.getHeight();
			}
		}
		
		return PAGE_EXISTS;
	}
}
