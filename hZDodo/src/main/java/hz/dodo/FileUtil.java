package hz.dodo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;

public class FileUtil
{
	public static final int KB = 1024;
	public static final int MB = KB * KB;
	public static final int GB = KB * KB * KB;
	
	public static final String CODE_UTF8 = "UTF-8";
	public static final String CODE_UTF16 = "UTF-16BE";
	public static final String CODE_UNICODE = "Unicode";
	public static final String CODE_GBK = "GBK";
	public static final String CODE_GB18030 = "GB18030";
	
	public static final int rst_success = 0;
	public static final int rst_failed = 1;
	public static final int rst_user_cancel = 2;
	
	public static final int mode_private = Context.MODE_PRIVATE; // 私有覆盖模式 只能被当前应用访问，并且如果写入，则覆盖  -  rw-  rw-  ---
	public static final int mode_append = Context.MODE_APPEND; // 私有追加模式    只能被当前应用访问，并且如果写入，则追加 -   rw-  rw-  ---
	
	@SuppressWarnings ("deprecation")
	public static final int mode_public_readable = Context.MODE_WORLD_READABLE; // 公有只读模式      可以被其他应用读取 -  rw-  rw-   r--
	@SuppressWarnings ("deprecation")
	public static final int mode_public_writeable = Context.MODE_WORLD_WRITEABLE; // 公有可写模式   可以被其他应用写入，但不能读取  - rw-   rw-  -w-
	@SuppressWarnings ("deprecation")
	public static final int mode_readAndWrite = Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE;
//	注意，如果希望其他使得文件模式叠加，则可以使用加号连接；
//	比如：Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE 表示其他应用读写；
	
	boolean isContinue = true;
	int size;
	int complete;
	
	static public File isExists(final String abspath)
	{
		try
		{
			if (abspath == null|| abspath.length() <= 0) return null;
			File file = new File(abspath);
			if (file.exists() && file.canRead()) return file;
			else return null;
		}
		catch(Exception e1)
		{
			Logger.e("FileUtil fileExists error: " + e1.toString());
		}
		return null;
	}
	
	static public long fileLastModify(final String abspath)
	{
		File file = isExists(abspath);
		if(file == null) return 0;
		return file.lastModified();
	}
	
	static public long size(final String abspath)
	{
		try
		{
			File file = isExists(abspath);
			if(file != null) return file.length();
		}
		catch(Exception e1)
		{
			Logger.e("FileUtil size error: " + e1.toString());
		}
		return 0;
	}
	
	// 判断文件的编码格式
	public static String codeString(String fileName)
	{
		BufferedInputStream is = null;
		String code = null;
		try
		{
			is = new BufferedInputStream(new FileInputStream(fileName));
			int p = (is.read() << 8) + is.read();
			
			switch (p)
			{
				case 0xEFBB:
				case 0xE8BF:
				case 0xE5A4:
				case 0x3039:
				case 0x3230:
					code = CODE_UTF8;
					break;
				case 0xFFFE:
					code = CODE_UNICODE;
					break;
				case 0xFEFF:
					code = CODE_UTF16;
					break;
				case 0xD5E2:
					code = CODE_GB18030;
					break;
				default:
					code = CODE_GBK;
			}
		}
		catch(Exception e1)
		{
			Logger.e("codeString() " + e1.toString());
		}
		finally
		{
			try
			{
				if(is != null) is.close();
			}
			catch (IOException e1)
			{
				Logger.e("is.close() " + e1.toString());
			}
		}
		return code;
	}
	
	// 读取文件
	public String read(final String abspath)
	{
		return read(abspath, "UTF-8");
		/*FileInputStream fis = null;
		try
		{
			File file = isExists(abspath);
			if(file == null) return null;
			
			fis = new FileInputStream(file);
			InputStreamReader inputStrReader = new InputStreamReader(fis, "utf-8");
			BufferedReader buffereReader = new BufferedReader(inputStrReader);
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = buffereReader.readLine()) != null)
			{
				sb.append(line).append("\n");
			}
			buffereReader.close();
			inputStrReader.close();
//			fis.close();
			return sb.toString();
		}
		catch (Exception e1)
		{
			System.out.println("FileUtil read error: " + e1.toString());
		}
		finally
		{
			try
			{
				if(fis != null) fis.close();
			}
			catch(Exception e1)
			{
				Logger.e("read(abspath)" + e1.toString());
			}
		}
		return null;*/
	}
	/**
	 * 调用 codeString(String) 可以获取code
	 */
	public String read(final String abspath, final String code)
	{
		FileInputStream fis = null;
		try
		{
			File file = FileUtil.isExists(abspath);
			if (file == null)
				return null;

			fis = new FileInputStream(file);
			InputStreamReader inputStrReader = new InputStreamReader(fis, code);
//			InputStreamReader inputStrReader = new InputStreamReader(fis);
			BufferedReader buffereReader = new BufferedReader(inputStrReader);
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ( (line = buffereReader.readLine()) != null)
			{
				sb.append(line).append("\n");
			}
			buffereReader.close();
			inputStrReader.close();
			// fis.close();
			return sb.toString();
		}
		catch (Exception e1)
		{
			System.out.println("FileUtil read error: " + e1.toString());
		}
		finally
		{
			try
			{
				if (fis != null)
					fis.close();
			}
			catch (Exception e1)
			{
				Logger.e("read(abspath)" + e1.toString());
			}
		}
		return null;
	}

	public int write(final String rsc, final String abspath)
	{
		if(rsc == null || abspath == null) return rst_failed;
		FileOutputStream fos = null;
		try
		{
			File dirfile = new File(abspath.substring(0, abspath.lastIndexOf("/")));
			// 如果不存在
			if (!dirfile.exists()) dirfile.mkdirs(); // 创建多级目录结构
			
			// 删除已存在的临时文件
			File filetmp = new File(abspath + ".hdd");
			if(filetmp.exists()) filetmp.delete();
			
			if(filetmp.createNewFile())
			{
				fos = new FileOutputStream(filetmp, true);
//				filefos.write(src.getBytes(), 0, src.length());
				fos.write(rsc.getBytes());
				
				// 删除已存在的完整文件
				File file = new File(abspath);
				if(file.exists()) file.delete();
				filetmp.renameTo(file);
				return rst_success;
			}
		}
		catch(Exception e1)
		{
			System.out.println("FileUtil write(bytes[]...) error: " + e1.toString());
		}
		finally
		{
			try
			{
				if(fos != null)
				{
					fos.flush();
					fos.close();
				}
			}
			catch(Exception e1)
			{
				Logger.e("writeAppend(fos)=" + e1.toString());
			}
		}
		return rst_failed;
	}
	
	public int writeAppend(final String rsc, final String abspath)
	{
		if(rsc == null || abspath == null) return rst_failed;
		FileOutputStream fos = null;
		try
		{
			File dirfile = new File(abspath.substring(0, abspath.lastIndexOf("/")));
			// 如果不存在
			if (!dirfile.exists()) dirfile.mkdirs(); // 创建多级目录结构
			
			// 不存在则创建
			File filetmp = new File(abspath);
			if(!filetmp.exists()) filetmp.createNewFile();
			
			fos = new FileOutputStream(filetmp, true);
			fos.write(rsc.getBytes());
			return rst_success;
		}
		catch(Exception e1)
		{
			Logger.e("FileUtil write(String) error: " + e1.toString());
		}
		finally
		{
			try
			{
				if(fos != null)
				{
					fos.flush();
					fos.close();
				}
			}
			catch(Exception e1)
			{
				Logger.e("writeAppend(fos)=" + e1.toString());
			}
		}
		return rst_failed;
	}
	
//	public int writeAppend(final byte[] bts, final String abspath)
//	{
//		if(bts == null || abspath == null) return rst_failed;
//		
//		try
//		{
//			File dirfile = new File(abspath.substring(0, abspath.lastIndexOf("/")));
//			// 如果不存在
//			if (!dirfile.exists()) dirfile.mkdirs(); // 创建多级目录结构
//			
//			// 不存在则创建
//			File filetmp = new File(abspath);
//			if(!filetmp.exists()) filetmp.createNewFile();
//			
//			FileOutputStream filefos = new FileOutputStream(filetmp, true);
//			filefos.write(bts);
//			filefos.close();
//			return rst_success;
//		}
//		catch(Exception e1)
//		{
//			System.out.println("FileUtil write(bytes[]...) error: " + e1.toString());
//		}
//		
//		return rst_failed;
//	}
	
	public int write(final InputStream filefis, final int size, final String abspath)
	{
		FileOutputStream fos = null;
		
		try
		{
			this.size = size;
			this.complete = 0;
			Logger.i("写文件路径:" + abspath);
			
			// 如果不存,创建多级目录结构
			File dirfile = new File(abspath.substring(0, abspath.lastIndexOf("/")));
			if (!dirfile.exists()) dirfile.mkdirs();
			
			// 删除已存在的临时文件
			File filetmp = new File(abspath + ".hdd");
			if(filetmp.exists()) filetmp.delete();
			if(filetmp.createNewFile())
			{
				fos = new FileOutputStream(filetmp, true);
				
				int reading, writed = 0;
				ByteArrayOutputStream babuf = new ByteArrayOutputStream();
				byte[] buf = new byte[5120];
				
				while ((reading = filefis.read(buf)) != -1)
				{
					if(!isContinue)
					{
//						fos.flush();
//						fos.close();
						return rst_user_cancel;
					}
						
					babuf.write(buf, 0, reading);
					writed += reading;
					
					complete += reading;
					
					if(writed >= 65536/*64k*/) // 256 * Dodo.KB = 262144
					{
						// 写文件
						fos.write(babuf.toByteArray());
						babuf.reset();
						writed = 0;
						
						if(size > 0 && isContinue)
						{
							// 发广播
							/*Intent intent = new Intent(NetWork.broad_apk_cnt);
							intent.putExtra("_id", id);
							intent.putExtra("progress", ((float)complete / size));
							ctx.sendBroadcast(intent);*/
						}
					}
				}
				
				// 最后一部分
				try
				{
					fos.write(babuf.toByteArray());
				}
				catch(Exception e1)
				{
					Logger.e("最后一部分不用写=" + e1.toString());
				}
				
				if(size < 0 || size == filetmp.length())
				{
					// 改名
//					// 删除已存在的完整文件
					File file = new File(abspath);
					if(file.exists()) file.delete();
					if(filetmp.renameTo(file))
					{
						return rst_success;
					}
				}
			}
		}
		catch(Exception e1)
		{
			Logger.e("FileUtil write(filefis...) error: " + e1.toString());
		}
		finally
		{
			try
			{
				if(fos != null)
				{
					fos.flush();
					fos.close();
				}
			}
			catch(Exception e1)
			{
				Logger.e("write(fos)=" + e1.toString());
			}
		}
		
		return rst_failed;
	}
	
	// 删除,参数可以是文件夹/也可是单个文
	static public int delete(final File dirorfile)
	{
		try
		{
			if(dirorfile == null) return rst_success;
			if (dirorfile.isDirectory())
			{
				String[] childrens = dirorfile.list();
				int i1 = 0;
				while(i1 < childrens.length)
				{
					File file = new File(dirorfile, childrens[i1]);
					if (file.isDirectory())
					{
						delete(file);
					}
					else if (file.isFile())
					{
						file.delete();
					}
					i1++;
				}
			}
			
			return dirorfile.delete() ? rst_success : rst_failed;
		}
		catch(Exception e1)
		{
			Logger.e("FileUtil delete error: " + e1.toString());
		}
		return rst_failed;
	}
	
	// 内部文件的读写
	public String readPrivate(Context ctx, final String filename)
	{
		try
		{
			return read(ctx.getFilesDir().getPath() + "/" + filename);
		}
		catch(Exception e1)
		{
			Logger.e("readPrivate()=" + e1.toString());
		}
		return null;
	}
	
	public int writePrivate(Context ctx, final String filename, final String rsc)
	{
		return writep(ctx, mode_private, filename, rsc);
	}
	
	public int writeAppendPrivate(Context ctx, final String filename, final String rsc)
	{
		return writep(ctx, mode_append, filename, rsc);
	}
	
	public int writePublic(Context ctx, final String filename, final String rsc)
	{
		return writep(ctx, mode_public_readable, filename, rsc);
	}
	
	static public long sizePrivate(Context ctx, final String filename)
	{
		return size(ctx.getFilesDir().getPath() + "/" + filename);
	}
	
	public static int deletePrivate(Context ctx, final String filename)
	{
		return delete(new File(ctx.getFilesDir().getPath() + "/" + filename));
	}
	
	private int writep(Context ctx, final int mode, final String filename, final String rsc)
	{
		if(rsc == null) return rst_failed;
		
		FileOutputStream fos = null;
		try
		{
			fos = ctx.openFileOutput(filename, mode);
			fos.write(rsc.getBytes());
			return rst_success;
		}
		catch (Exception e1)
		{
			Logger.e("writePrivate()=" + e1.toString());
		}
		finally
		{
			try
			{
				if (fos != null)
				{
					fos.flush();
					fos.close();
				}
			}
			catch (Exception e1)
			{
				Logger.e("writePrivate()=" + e1.toString());
			}
		}
		
		return rst_failed;
	}
	
	/**
	 * 移动文件
	 * @param srcFileName 源文件完整路径
	 * @param destDirName 目的目录完整路径
	 * @return 文件移动成功返回true，否则返回false
	 */
	public boolean moveFile(String srcFileName, String destDirName)
	{
		try
		{
			File srcFile = new File(srcFileName);
			if (!srcFile.exists() || !srcFile.isFile())
				return false;

			File destDir = new File(destDirName);
			if (!destDir.exists())
				destDir.mkdirs();

			return srcFile.renameTo(new File(destDirName + File.separator + srcFile.getName()));
		}
		catch(Exception e1)
		{
			Logger.i("moveFile() " + e1.toString());
		}
		return false;
	}

	/**
	 * 移动目录
	 * @param srcDirName 源目录完整路径
	 * @param destDirName 目的目录完整路径
	 * @return 目录移动成功返回true，否则返回false
	 */
	public boolean moveDirectory(String srcDirName, String destDirName)
	{
		try
		{
			File srcDir = new File(srcDirName);
			if (!srcDir.exists() || !srcDir.isDirectory())
				return false;

			File destDir = new File(destDirName);
			if (!destDir.exists())
				destDir.mkdirs();

			/**
			 * 如果是文件则移动，否则递归移动文件夹。删除最终的空源文件夹 注意移动文件夹时保持文件夹的树状结构
			 */
			File[] sourceFiles = srcDir.listFiles();
			for (File sourceFile : sourceFiles)
			{
				if (sourceFile.isFile())
					moveFile(sourceFile.getAbsolutePath(), destDir.getAbsolutePath());
				else if (sourceFile.isDirectory())
					moveDirectory(sourceFile.getAbsolutePath(), destDir.getAbsolutePath() + File.separator + sourceFile.getName());
			}
			return srcDir.delete();
		}
		catch(Exception e1)
		{
			Logger.e("moveDirectory() " + e1.toString());
		}
		return false;
	}
	
	// 耗时操作,外部另起线程来做
	// !!! 如果要解压到ZIP包所在目录,文件夹不要和ZIP同名
	public static boolean unZip(String zipFileName, String outputDirectory)
	{
		if(isExists(zipFileName) == null || outputDirectory == null || outputDirectory.length() <= 0) return false;
		
		File dest = new File(outputDirectory);
		delete(dest);
		if(!dest.mkdirs()) return false;

		ZipFile zipFile = null;
		
		try
		{
			if(null != (zipFile = new ZipFile(zipFileName)))
			{
				Enumeration<? extends ZipEntry> e = zipFile.entries();
				if(e != null)
				{
					InputStream in = null;
					FileOutputStream out = null;
					
					ZipEntry zipEntry = null;

					File
					fl,
					fOut;
					
					String
						entryName = null,
						strTmp = null;
					
					int
						index = 0,
						count = 0;
					
					byte[] bArr = new byte[8*KB];

					while (e.hasMoreElements())
					{
						if(null != (zipEntry = e.nextElement()))
						{
							if(null != (entryName = zipEntry.getName()))
							{
								try
								{
									if (zipEntry.isDirectory())
									{
										// 文件夹尾部为 "/"
										if(entryName.endsWith(File.separator))
										{
											strTmp = entryName.substring(0, entryName.length() - File.separator.length());
										}
										else
										{
											strTmp = entryName;
										}
										fl = new File(outputDirectory + File.separator + strTmp);
										fl.mkdirs();
									}
									else
									{
										index = entryName.lastIndexOf("\\");
										if (index != -1)
										{
											fl = new File(outputDirectory + File.separator + entryName.substring(0, index));
											fl.mkdirs();
										}
										index = entryName.lastIndexOf("/");
										if (index != -1)
										{
											fl = new File(outputDirectory + File.separator + entryName.substring(0, index));
											fl.mkdirs();
										}
										fOut = new File(outputDirectory + File.separator + entryName);
										// f.createNewFile();
										in = zipFile.getInputStream(zipEntry);
										out = new FileOutputStream(fOut);
										while ( (count = in.read(bArr)) != -1)
										{
											out.write(bArr, 0, count);
										}
										out.flush();
									}
								}
								catch (Exception ex)
								{
									return false;
								}
								finally
								{
									if (in != null)
									{
										try
										{
											in.close();
											in = null;
										}
										catch (IOException ex)
										{
											Logger.e("unZip in close " + ex.toString());
										}
									}
									if (out != null)
									{
										try
										{
											out.close();
											out = null;
										}
										catch (IOException ex)
										{
											Logger.e("unZip out close " + ex.toString());
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch (IOException ex)
		{
			Logger.e("unZip " + ex.toString());
			return false;
		}
		finally
		{
			if (zipFile != null)
			{
				try
				{
					zipFile.close();
					zipFile = null;
				}
				catch (IOException ex)
				{
					Logger.e("unZip zipFile close " + ex.toString());
				}
			}
		}
		return true;
	}
}
