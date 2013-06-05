package org.overture.alloy.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import junit.framework.TestCase;

public abstract class Vdm2AlloyBaseTest extends TestCase
{
protected final static String verbose = "";//-v
	public File getOutputDir()
	{
		File output = new File("generated");
		output = new File(output, this.getName().substring(this.getName().toLowerCase().indexOf("test")
				+ "test".length()));
		output.mkdirs();

		return output;
	}

	public File getInputDir()
	{
		File input = new File("specifications/" + getTestName()
				+ "/vdm/".replace('/', File.separatorChar));
		return input;
	}

	public File getAlloyInputDir()
	{
		File input = new File("specifications/" + getTestName()
				+ "/alloy/".replace('/', File.separatorChar));
		return input;
	}

	public String getTestName()
	{
		return this.getName().substring(this.getName().toLowerCase().indexOf("test")
				+ "test".length());
	}

	@Override
	protected void setUp() throws Exception
	{
		copyFile(new File("specifications/vdmutil.als"), new File(getOutputDir(), "vdmutil.als"));
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException
	{
		if (!destFile.exists())
		{
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try
		{
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (IOException e)
		{
			InputStream stream = null;
			OutputStream resStreamOut = null;
			try
			{
				String cppath = sourceFile.getPath().replace('\\', '/');
//				System.out.println("trying to copy:" + cppath);
				stream = Vdm2AlloyBaseTest.class.getClassLoader().getResourceAsStream(cppath);
//				System.out.println("stream:" + stream == null ? null : "ok");
				if (stream == null)
				{
					// send your exception or warning
				}

				int readBytes;
				byte[] buffer = new byte[4096];
				try
				{
					resStreamOut = new FileOutputStream(destFile);
					while ((readBytes = stream.read(buffer)) > 0)
					{
						resStreamOut.write(buffer, 0, readBytes);
					}
				} catch (IOException e1)
				{

				}
			} finally
			{
				if (stream != null)
				{
					stream.close();
				}
				if (resStreamOut != null)
				{
					resStreamOut.close();
				}
			}

		} finally
		{
			if (source != null)
			{
				source.close();
			}
			if (destination != null)
			{
				destination.close();
			}
		}

	}

	protected String copy(File vdm) throws IOException
	{
		if (!vdm.exists())
		{
			File tmp = new File(getOutputDir(), vdm.getPath());
			tmp.getParentFile().mkdirs();
			copyFile(vdm, tmp);
			return tmp.getPath();
		}
		return vdm.getPath();
	}

}
