package community.solace.ep.wrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileUtil {


	public static String loadTextFile(File f) throws FileNotFoundException {
		StringBuilder sb = new StringBuilder();
		Scanner scanner = new Scanner(f);
		while (scanner.hasNextLine()) {
			sb.append(scanner.nextLine());
		}
		scanner.close();
		return sb.toString();
	}


	public static void decompress(InputStream input, File destDir) throws IOException {
		
		ZipInputStream zin = new ZipInputStream(input);
		ZipEntry entry = null;
		if (!destDir.exists()) destDir.mkdirs();
		byte[] buffer = new byte[1024];
		while ((entry = zin.getNextEntry()) != null) {
			File f = new File(destDir, entry.getName());
			if (entry.isDirectory()) {
				f.mkdirs();
				continue;
			}
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}

			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));

			int n = -1;
			while ((n = zin.read(buffer)) != -1) {
				out.write(buffer, 0, n);
			}

			out.flush();
			out.close();
		}
	}

	public static void decompress(final File inputFile, final File destDir) throws IOException {
		ZipFile zipFile = new ZipFile(inputFile);

		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			InputStream stream = zipFile.getInputStream(entry);

			if (entry.isDirectory()) {
				// Assume directories are stored parents first then children.
				(new File(destDir, entry.getName())).mkdir();
				continue;
			}

			File newFile = new File(destDir, entry.getName());
			FileOutputStream fos = new FileOutputStream(newFile);
			try {
				byte[] buf = new byte[1024];
				int len;

				while ((len = stream.read(buf)) >= 0)
					saveCompressedStream(buf, fos, len);

			} catch (IOException e) {
				zipFile.close();
				IOException ioe = new IOException("Not valid COAMPS archive file type.");
				ioe.initCause(e);
				throw ioe;
			} finally {
				fos.flush();
				fos.close();

				stream.close();
			}
		}
		zipFile.close();
	}

	/**
	 * @param len 
	 * @param stream
	 * @param fos
	 * @throws IOException
	 */
	public static void saveCompressedStream(final byte[] buffer, final OutputStream out, final int len)
			throws IOException {
		try {
			out.write(buffer, 0, len);

		} catch (Exception e) {
			out.flush();
			out.close();
			IOException ioe = new IOException("Not valid archive file type.");
			ioe.initCause(e);
			throw ioe;
		}
	}

}
