package com.person.movieserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

/**
 * File Utils
 * <ul>
 * Read or write file
 * <li>{@link #readFile(String)} read file</li>
 * <li>{@link #readFileToList(String)} read file to string list</li>
 * <li>{@link #writeFile(String, String, boolean)} write file</li>
 * <li>{@link #writeFile(String, InputStream)} write file</li>
 * </ul>
 * <ul>
 * Operate file
 * <li>{@link #getFileExtension(String)}</li>
 * <li>{@link #getFileName(String)}</li>
 * <li>{@link #getFileNameWithoutExtension(String)}</li>
 * <li>{@link #getFileSize(String)}</li>
 * <li>{@link #deleteFile(String)}</li>
 * <li>{@link #isFileExist(String)}</li>
 * <li>{@link #isFolderExist(String)}</li>
 * <li>{@link #makeFolders(String)}</li>
 * <li>{@link #makeDirs(String)}</li>
 * </ul>
 * 
 * @author Trinea 2012-5-12
 */
public class FileUtils {

	public final static String FILE_EXTENSION_SEPARATOR = ".";

	/**
	 * 判断SD卡是否存在
	 * 
	 * @return true 存在 false 不存在
	 */
	public static boolean isSdcardExist() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 计算剩余空间
	 * 
	 * @param path
	 * @return
	 */
	public static long getAvailableSize(String path) {
		StatFs fileStats = new StatFs(path);
		fileStats.restat(path);
		// 注意与fileStats.getFreeBlocks()的区别
		return (long) fileStats.getAvailableBlocks() * fileStats.getBlockSize();
	}

	/**
	 * 获取SD卡路径
	 * 
	 * @return SD卡路径，以"/"结尾， 如"/mnt/sdcard/"
	 */
	public static String getSdcardPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator;
	}

	/**
	 * 判断sd卡上的某个目录是否存在，不存在就创建
	 * 
	 * @param directoryName
	 *            ：SD卡内的文件目录，如".wasu/img_cache"
	 * @return true 存在 false 不存在
	 */
	public static boolean isDirExistInSdcard(String directoryName) {
		File file = new File(getSdcardPath() + directoryName + File.separator);
		if (!file.exists())
			// 如果不存在则创建
			return file.mkdirs();
		else {
			return true;
		}
	}

	/**
	 * 判断本地某个目录是否存在，不存在就创建
	 * 
	 * @param directoryName
	 *            ：本地文件目录，如"./wasu/img_cache"
	 * @return true 存在 false 不存在
	 */
	public static boolean isDirExistInLocal(Context c, String directoryName) {
		File f = new File(c.getFilesDir(), directoryName + File.separator);
		if (!f.exists()) {
			f.mkdirs();
		}
		return true;
	}

	/**
	 * @param c
	 * @return 返回cache目录 如： /mnt/sdcard/Android/data/com.wasu.app/cache/
	 */
	public static String getExtenalCacheDir(Context c) {
		File f = c.getExternalCacheDir();
		if (f != null) {
			f.mkdirs();
		} else {
			return null;
		}
		return f.getAbsolutePath() + File.separator;
	}

	/**
	 * @param c
	 * @return 返回cache目录 如： /data/data/com.xhmm.BeautyChina/cache/
	 */
	public static String getInternalCacheDir(Context c) {
		File f = c.getCacheDir();
		if (f != null) {
			f.mkdirs();
		}
		return f.getAbsolutePath() + File.separator;
	}

	/**
	 * 产生独一无二的文件名，如"1363594141180"
	 * 
	 * @return 产生的文件名
	 */
	public static String generateUniqueName() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis());
		return sb.toString();
	}

	/**
	 * read file
	 * 
	 * @param filePath
	 * @return if file not exist, return null, else return content of file
	 * @throws IOException
	 *             if an error occurs while operator BufferedReader
	 */
	public static StringBuilder readFile(String filePath) {
		File file = new File(filePath);
		StringBuilder fileContent = new StringBuilder("");
		if (file != null && file.isFile()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (!fileContent.toString().equals("")) {
						fileContent.append("\r\n");
					}
					fileContent.append(line);
				}
				reader.close();
				return fileContent;
			} catch (IOException e) {
				throw new RuntimeException("IOException occurred. ", e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						throw new RuntimeException("IOException occurred. ", e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * write file
	 * 
	 * @param filePath
	 * @param content
	 * @param append
	 *            is append, if true, write to the end of file, else clear
	 *            content of file and write into it
	 * @return return true
	 * @throws IOException
	 *             if an error occurs while operator FileWriter
	 */
	public static boolean writeFile(String filePath, String content,
			boolean append) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(filePath, append);
			fileWriter.write(content);
			fileWriter.close();
			return true;
		} catch (IOException e) {
			throw new RuntimeException("IOException occurred. ", e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					throw new RuntimeException("IOException occurred. ", e);
				}
			}
		}
	}

	/**
	 * write file
	 * 
	 * @param filePath
	 * @param stream
	 * @return return true
	 * @throws IOException
	 *             if an error occurs while operator FileWriter
	 */
	public static boolean writeFile(String filePath, InputStream stream) {
		OutputStream o = null;
		try {
			o = new FileOutputStream(filePath);
			byte data[] = new byte[1024];
			int length = -1;
			while ((length = stream.read(data)) != -1) {
				o.write(data, 0, length);
			}
			o.flush();
			return true;
		} catch (FileNotFoundException e) {
			throw new RuntimeException("FileNotFoundException occurred. ", e);
		} catch (IOException e) {
			throw new RuntimeException("IOException occurred. ", e);
		} finally {
			if (o != null) {
				try {
					o.close();
					stream.close();
				} catch (IOException e) {
					throw new RuntimeException("IOException occurred. ", e);
				}
			}
		}
	}

	/**
	 * read file to string list, a element of list is a line
	 * 
	 * @param filePath
	 * @return if file not exist, return null, else return content of file
	 * @throws IOException
	 *             if an error occurs while operator BufferedReader
	 */
	public static List<String> readFileToList(String filePath) {
		File file = new File(filePath);
		List<String> fileContent = new ArrayList<String>();
		if (file != null && file.isFile()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line = null;
				while ((line = reader.readLine()) != null) {
					fileContent.add(line);
				}
				reader.close();
				return fileContent;
			} catch (IOException e) {
				throw new RuntimeException("IOException occurred. ", e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						throw new RuntimeException("IOException occurred. ", e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Indicates if this file represents a file on the underlying file system.
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean isFileExist(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return false;
		}

		File file = new File(filePath);
		return (file.exists() && file.isFile());
	}
	
	// /////////////////////////////////////////////////////////////////////////
	/*** 获取文件大小 ***/
	@SuppressWarnings("resource")
	public static long getFileSizes(File f) throws Exception {
		long s = 0;
		if (f.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(f);
			s = fis.available();
		} else {
			f.createNewFile();
		}
		return s;
	}

	/**
	 * 获取目录大小
	 * 
	 * @param f
	 *            目录
	 * @return 大小
	 * @throws Exception
	 */
	public static long getDirSize(File f) throws Exception {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getDirSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}

	/**
	 * 转换文件大小， 转换文件大小单位(b/kb/mb/gb)
	 * 
	 * @param fileS
	 *            文件大小
	 * @return b/kb/mb/gb数值
	 */
	public static String FormetFileSize(long fileS) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("0.00");
		String fileSizeString = "";
		if (fileS <= 0) {
			return "0M";
		}
		fileSizeString = df.format((double) fileS / 1048576) + "M";
		return fileSizeString;
	}

	/**
	 * 获取该目录下所有文件个数
	 * 
	 * @param f
	 *            文件目录
	 * @return 所有文件个数
	 */
	public long getlist(File f) {
		long size = 0;
		File flist[] = f.listFiles();
		size = flist.length;
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getlist(flist[i]);
				size--;
			}
		}
		return size;
	}

	/**
	 * 清空某个目录
	 * 
	 * @param f
	 *            文件目录
	 * @throws Exception
	 */
	public static void clearDir(File f) throws Exception {
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				clearDir(flist[i]);
			} else {
				flist[i].delete();
			}
		}
		f.delete();
	}
}
