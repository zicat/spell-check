package name.zicat.spell.check.core.datasource.txt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import name.zicat.spell.check.core.datasource.DataSourceReader;
import name.zicat.spell.check.core.datasource.Record;
import name.zicat.spell.check.core.datasource.RowConvertor;

/**
 * 
 * @author zicat
 *
 */
public class TXTLineSourceReader implements DataSourceReader {
	
	private RowConvertor rowConvertor;
	private BufferedReader br;
	private String currentLine;
	private int lineCount = 0;
	
	public TXTLineSourceReader(Reader in, RowConvertor rowConvertor) throws IOException {
		
		if(in == null)
			throw new NullPointerException("Reader is null");
		
		if(rowConvertor == null)
			throw new NullPointerException("rowConvertor is null");
		
		this.br = new BufferedReader(in);
		this.rowConvertor = rowConvertor;
		this.currentLine = br.readLine();
	}

	@Override
	public boolean hasNext() throws IOException {
		return currentLine != null;
	}

	@Override
	public Record next() throws IOException {
		
		if(!hasNext())
			return null;
		
		lineCount++;
		Record record = null;
		try {
			record = rowConvertor.convert(currentLine);
		} catch(Exception e) {
			throw new IOException("convert error, line value = " + currentLine + ", lineCount = " + lineCount, e);
		}
		currentLine = br.readLine();
		return record;
	}
}
