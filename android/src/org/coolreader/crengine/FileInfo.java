package org.coolreader.crengine;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.coolreader.R;
import org.coolreader.plugins.OnlineStoreBook;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.ZipEntry;

public class FileInfo implements Parcelable {

	public final static String RECENT_DIR_TAG = "@recent";
	public final static String SEARCH_RESULT_DIR_TAG = "@searchResults";
	public final static String ROOT_DIR_TAG = "@root";
	public final static String OPDS_LIST_TAG = "@opds";
	public final static String OPDS_DIR_PREFIX = "@opds:";
	public final static String ONLINE_CATALOG_PLUGIN_PREFIX = "@plugin:";
	public final static String GENRES_TAG = "@genresRoot";
	public final static String GENRES_GROUP_PREFIX = "@genresGroup:";
	public final static String GENRES_PREFIX = "@genre:";
	public final static String AUTHORS_TAG = "@authorsRoot";
	public final static String AUTHOR_GROUP_PREFIX = "@authorGroup:";
	public final static String AUTHOR_PREFIX = "@author:";
	public final static String SERIES_TAG = "@seriesRoot";
	public final static String SERIES_GROUP_PREFIX = "@seriesGroup:";
	public final static String SERIES_PREFIX = "@series:";
	public final static String RATING_TAG = "@ratingRoot";
	public final static String STATE_TO_READ_TAG = "@stateToReadRoot";
	public final static String STATE_READING_TAG = "@stateReadingRoot";
	public final static String STATE_FINISHED_TAG = "@stateFinishedRoot";
	public final static String TITLE_TAG = "@titlesRoot";
	public final static String TITLE_GROUP_PREFIX = "@titleGroup:";
	public final static String SEARCH_SHORTCUT_TAG = "@search";
	
	
	
	public Long id; // db id
	public String title; // book title
	public String authors; // authors, delimited with '|'
	public String series; // series name w/o number
	public int seriesNumber; // number of book inside series
	public String genres; // genre codes, delimited with '|'
	public String path; // path to directory where file is located
	public String filename; // file name w/o path for normal file, with optional path for file inside archive 
	public String pathname; // full path+filename
	public String arcname; // archive file name with path
	public String language; // document language
	public String description;	// book description
	public String username; // username for online catalogs
	public String password; // password for online catalogs
	public DocumentFormat format;
	public long size; // full file size
	public long arcsize; // compressed size
	public long createTime;
	public long lastAccessTime;
	public int flags;
	public boolean isArchive;
	public boolean isDirectory;
	public boolean isListed;
	public boolean isScanned;
	public long crc32;
	public int domVersion;
	public int blockRenderingFlags;
	public FileInfo parent; // parent item
	public Object tag; // some additional information
	
	private ArrayList<FileInfo> files;// files
	private ArrayList<FileInfo> dirs; // directories

	// 16 lower bits reserved for document flags
	public static final int DONT_USE_DOCUMENT_STYLES_FLAG = 1;
	public static final int DONT_REFLOW_TXT_FILES_FLAG = 2;
	public static final int USE_DOCUMENT_FONTS_FLAG = 4;
	
	// bits 16..19 - reading state (0..15 max)
	public static final int READING_STATE_SHIFT = 16;
	public static final int READING_STATE_MASK = 0x0F;
	public static final int STATE_NEW = 0;
	public static final int STATE_TO_READ = 1;
	public static final int STATE_READING = 2;
	public static final int STATE_FINISHED = 3;

	// bits 20..23 - rate (0..15 max, 0..5 currently)
	public static final int RATE_SHIFT = 20;
	public static final int RATE_MASK = 0x0F;
	public static final int RATE_VALUE_NOT_RATED = 0;
	public static final int RATE_VALUE_1 = 1;
	public static final int RATE_VALUE_2 = 2;
	public static final int RATE_VALUE_3 = 3;
	public static final int RATE_VALUE_4 = 4;
	public static final int RATE_VALUE_5 = 5;

    //bit 24,25 - info type
    private static final int TYPE_SHIFT = 24;
    private static final int TYPE_MASK = 0x03;
    public static final int TYPE_NOT_SET = 0;
    public static final int TYPE_FS_ROOT = 1;
    public static final int TYPE_DOWNLOAD_DIR = 2;

    // bits 26..29 - profile id (0..15 max)
	public static final int PROFILE_ID_SHIFT = 26;
	public static final int PROFILE_ID_MASK = 0x0F;

	// bitmask for field 'tag' when obtained genres list as special folders
	public static final int GENRE_DATA_INCCHILD_MASK = 0x80000000;
	public static final int GENRE_DATA_BOOKCOUNT_MASK = 0x00FFFFFF;

	public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
		@Override
		public FileInfo createFromParcel(Parcel in) {
			return new FileInfo(in);
		}

		@Override
		public FileInfo[] newArray(int size) {
			return new FileInfo[size];
		}
	};

	/**
	 * Get book reading state. 
	 * @return reading state (one of STATE_XXX constants)
	 */
	public int getReadingState() {
        return getBitValue(READING_STATE_SHIFT,READING_STATE_MASK);
    }

	/**
	 * Set new reading state.
	 * @param state is new reading state (one of STATE_XXX constants)
	 */
	public boolean setReadingState(int state) {
        return setBitValue(state, READING_STATE_SHIFT, READING_STATE_MASK);
	}

	/**
	 * Get book reading state. 
	 * @return reading state (one of STATE_XXX constants)
	 */
	public int getRate() {
        return getBitValue(RATE_SHIFT, RATE_MASK);
    }

	/**
	 * Set new rate.
	 * @param rate is new rate (one of RATE_XXX constants)
	 */
	public boolean setRate(int rate) {
        return setBitValue(rate, RATE_SHIFT, RATE_MASK);
	}

    /**
   	 * Get FileInfo type.
   	 * @return folder type (one of TYPE_XXX constants)
   	 */
   	public int getType() {
        return getBitValue(TYPE_SHIFT, TYPE_MASK);
    }

   	/**
   	 * Set FileInfo type.
   	 * @param type is new type
   	 */
   	public boolean setType(int type) {
        return setBitValue(type, TYPE_SHIFT, TYPE_MASK);
   	}

	/**
	 * To separate archive name from file name inside archive.
	 */
	public static final String ARC_SEPARATOR = "@/";


	public void setFlag( int flag, boolean value ) {
		flags = flags & (~flag) | (value? flag : 0);
	}
	
	public boolean getFlag( int flag ) {
		return (flags & flag)!=0;
	}
	
	public int getProfileId() {
        return getBitValue(PROFILE_ID_SHIFT,PROFILE_ID_MASK);
    }

    public void setProfileId(int id) {
        setBitValue(id,PROFILE_ID_SHIFT,PROFILE_ID_MASK);
	}

    private boolean setBitValue(int value, int shift, int mask) {
        int oldFlags = flags;
        flags = (flags & ~(mask << shift))
                | ((value & mask) << shift);
        return flags != oldFlags;
    }

    private int getBitValue(int shift, int mask) {
        return (flags >> shift) & mask;
    }

    public String getTitleOrFileName() {
		if (title != null && title.length() > 0)
			return title;
		if (authors != null && authors.length() > 0)
			return "";
		if (series != null && series.length() > 0)
			return "";
		return filename;
	}
	
	/**
	 * Split archive + file path name by ARC_SEPARATOR
	 * @param pathName is pathname like /arc_file_path@/filepath_inside_arc or /file_path 
	 * @return item[0] is pathname, item[1] is archive name (null if no archive)
	 */
	public static String[] splitArcName( String pathName )
	{
		String[] res = new String[2];
		int arcSeparatorPos = pathName.indexOf(ARC_SEPARATOR);
		if ( arcSeparatorPos>=0 ) {
			// from archive
			res[1] = pathName.substring(0, arcSeparatorPos);
			res[0] = pathName.substring(arcSeparatorPos + ARC_SEPARATOR.length());
		} else {
			res[0] = pathName;
		}
		return res;
	}

	protected FileInfo(Parcel in) {
		if (in.readByte() == 0) {
			id = null;
		} else {
			id = in.readLong();
		}
		title = in.readString();
		authors = in.readString();
		series = in.readString();
		seriesNumber = in.readInt();
		genres = in.readString();
		path = in.readString();
		filename = in.readString();
		pathname = in.readString();
		arcname = in.readString();
		language = in.readString();
		description = in.readString();
		username = in.readString();
		password = in.readString();
		size = in.readLong();
		arcsize = in.readLong();
		createTime = in.readLong();
		lastAccessTime = in.readLong();
		flags = in.readInt();
		isArchive = in.readByte() != 0;
		isDirectory = in.readByte() != 0;
		isListed = in.readByte() != 0;
		isScanned = in.readByte() != 0;
		crc32 = in.readLong();
		domVersion = in.readInt();
		blockRenderingFlags = in.readInt();
		parent = in.readParcelable(FileInfo.class.getClassLoader());
		files = in.createTypedArrayList(FileInfo.CREATOR);
		dirs = in.createTypedArrayList(FileInfo.CREATOR);
	}

	public FileInfo( String pathName )
	{
		String[] parts = splitArcName( pathName );
		if ( parts[1]!=null ) {
			// from archive
			isArchive = true;
			arcname = parts[1];
			pathname = parts[0];
			File f = new File(pathname);
			filename = f.getName();
			path = f.getPath();
			File arc = new File(arcname);
			if (arc.isFile() && arc.exists()) {
				arcsize = arc.length();
				isArchive = true;
				try {
					//ZipFile zip = new ZipFile(new File(arcname));
					ArrayList<ZipEntry> entries = Services.getEngine().getArchiveItems(arcname);
					//for ( Enumeration<?> e = zip.entries(); e.hasMoreElements(); ) {
					for (ZipEntry entry : entries) {
						String name = entry.getName();
						
						if ( !entry.isDirectory() && pathname.equals(name) ) {
							File itemf = new File(name);
							filename = itemf.getName();
							path = itemf.getPath();
							format = DocumentFormat.byExtension(name);
							size = entry.getSize();
							//arcsize = entry.getCompressedSize();
							createTime = entry.getTime();
							domVersion = Engine.DOM_VERSION_CURRENT;
							blockRenderingFlags = Engine.BLOCK_RENDERING_FLAGS_WEB;
							break;
						}
					}
				} catch ( Exception e ) {
					Log.e("cr3", "error while reading contents of " + arcname);
				}
			}
		} else {
			fromFile(new File(pathName));
		}
	}
	
	public String getFileNameToDisplay() {
		boolean isSingleFileArchive = (isArchive && parent!=null && !parent.isArchive && arcname!=null);
		return isSingleFileArchive
			? new File(arcname).getName() : filename;
	}
	
	private void fromFile( File f )
	{
		if ( !f.isDirectory() ) {
			DocumentFormat fmt = DocumentFormat.byExtension(f.getName());
			filename = f.getName();
			path = f.getParent();
			pathname = f.getAbsolutePath();
			format = fmt;
			createTime = f.lastModified();
			size = f.length();
			domVersion = Engine.DOM_VERSION_CURRENT;
			blockRenderingFlags = Engine.BLOCK_RENDERING_FLAGS_WEB;
		} else {
			filename = f.getName();
			path = f.getParent();
			pathname = f.getAbsolutePath();
			isDirectory = true;
		}
		File parent_ = f.getParentFile();
		if (null != parent_)
			parent = new FileInfo(parent_);
	}
	
	public FileInfo( File f )
	{
		fromFile(f);
	}
	
	public FileInfo()
	{
		domVersion = Engine.DOM_VERSION_CURRENT;
		blockRenderingFlags = Engine.BLOCK_RENDERING_FLAGS_WEB;
	}

	/// doesn't copy parent and children
	public FileInfo(FileInfo v)
	{
		assign(v);
	}

	public void assign(FileInfo v)
	{
		title = v.title;
		authors = v.authors;
		series = v.series;
		seriesNumber = v.seriesNumber;
		path = v.path;
		filename = v.filename;
		pathname = v.pathname;
		arcname = v.arcname;
		format = v.format;
		flags = v.flags;
		size = v.size;
		arcsize = v.arcsize;
		isArchive = v.isArchive;
		isDirectory = v.isDirectory;
		createTime = v.createTime;
		lastAccessTime = v.lastAccessTime;
		language = v.language;
		genres = v.genres;
		description = v.description;
		username = v.username;
		password = v.password;
		crc32 = v.crc32;
		domVersion = v.domVersion;
		blockRenderingFlags = v.blockRenderingFlags;
		id = v.id;
	}
	
	/**
	 * @return archive file path and name, null if this object is neither archive nor a file inside archive
	 */
	public String getArchiveName()
	{
		return arcname;
	}
	
	/**
	 * @return file name inside archive, null if this object is not a file inside archive
	 */
	public String getArchiveItemName()
	{
		if ( isArchive && !isDirectory && pathname!=null )
			return pathname;
		return null;
	}
	
	public boolean isRecentDir()
	{
		return RECENT_DIR_TAG.equals(pathname);
	}
	
	public boolean isSearchDir()
	{
		return SEARCH_RESULT_DIR_TAG.equals(pathname);
	}
	
	public boolean isRootDir()
	{
		return ROOT_DIR_TAG.equals(pathname);
	}
	
	public boolean isSpecialDir()
	{
		return pathname!=null && pathname.startsWith("@");
	}
	
	public boolean isOnlineCatalogPluginDir()
	{
		return pathname!=null && pathname.startsWith(ONLINE_CATALOG_PLUGIN_PREFIX);
	}
	
	public boolean isOnlineCatalogPluginBook()
	{
		return !isDirectory && pathname != null && pathname.startsWith(ONLINE_CATALOG_PLUGIN_PREFIX) && getOnlineStoreBookInfo() != null;
	}
	
	public boolean isOPDSDir()
	{
		return pathname!=null && pathname.startsWith(OPDS_DIR_PREFIX) && (getOPDSEntryInfo() == null || getOPDSEntryInfo().getBestAcquisitionLink() == null);
	}
	
	public boolean isOPDSBook()
	{
		return pathname!=null && pathname.startsWith(OPDS_DIR_PREFIX) && getOPDSEntryInfo() != null && getOPDSEntryInfo().getBestAcquisitionLink() != null;
	}
	
	private OPDSUtil.EntryInfo getOPDSEntryInfo() {
		if (tag !=null && tag instanceof OPDSUtil.EntryInfo)
			return (OPDSUtil.EntryInfo)tag;
		return null;
	}
	
	public OnlineStoreBook getOnlineStoreBookInfo() {
		if (tag !=null && tag instanceof OnlineStoreBook)
			return (OnlineStoreBook)tag;
		return null;
	}
	
	public boolean isOPDSRoot()
	{
		return OPDS_LIST_TAG.equals(pathname);
	}
	
	public boolean isSearchShortcut()
	{
		return SEARCH_SHORTCUT_TAG.equals(pathname);
	}

	public boolean isBooksByGenreRoot()
	{
		return GENRES_TAG.equals(pathname);
	}

	public boolean isBooksByAuthorRoot()
	{
		return AUTHORS_TAG.equals(pathname);
	}
	
	public boolean isBooksBySeriesRoot()
	{
		return SERIES_TAG.equals(pathname);
	}
	
	public boolean isBooksByRatingRoot()
	{
		return RATING_TAG.equals(pathname);
	}
	
	public boolean isBooksByStateToReadRoot()
	{
		return STATE_TO_READ_TAG.equals(pathname);
	}
	
	public boolean isBooksByStateReadingRoot()
	{
		return STATE_READING_TAG.equals(pathname);
	}
	
	public boolean isBooksByStateFinishedRoot()
	{
		return STATE_FINISHED_TAG.equals(pathname);
	}
	
	public boolean isBooksByTitleRoot()
	{
		return TITLE_TAG.equals(pathname);
	}

	public boolean isBooksByGenreDir()
	{
		return pathname!=null && pathname.startsWith(GENRES_PREFIX);
	}

	public boolean isBooksByAuthorDir()
	{
		return pathname!=null && pathname.startsWith(AUTHOR_PREFIX);
	}
	
	public boolean isBooksBySeriesDir()
	{
		return pathname!=null && pathname.startsWith(SERIES_PREFIX);
	}

	public boolean isOnSDCard() {
		if (null == parent)
			return false;
		if ( ( ( "SD".equals(filename) && "SD".equals(title)) ||
				("EXT SD".equals(filename) && "EXT SD".equals(title)) ) &&
				isDirectory && !isArchive && 0 == size && 0 == arcsize &&
				ROOT_DIR_TAG.equals(parent.pathname) )
			return true;
		return parent.isOnSDCard();
	}

	public String getGenreCode() {
		if (pathname.startsWith(GENRES_PREFIX)) {
			return pathname.substring(GENRES_PREFIX.length());
		}
		return "";
	}

	public long getAuthorId()
	{
		if (!isBooksByAuthorDir())
			return 0;
		return id;
	}
	
	public long getSeriesId()
	{
		if (!isBooksBySeriesDir())
			return 0;
		return id;
	}
	
	public boolean isHidden()
	{
		return pathname.startsWith(".");
	}
	
	public String getOPDSUrl()
	{
		if ( !pathname.startsWith(OPDS_DIR_PREFIX) )
			return null;
		return pathname.substring(OPDS_DIR_PREFIX.length());
	}
	
	public String getOnlineCatalogPluginPackage()
	{
		if ( !pathname.startsWith(ONLINE_CATALOG_PLUGIN_PREFIX) )
			return null;
		String s = pathname.substring(ONLINE_CATALOG_PLUGIN_PREFIX.length());
		int p = s.indexOf(":");
		if (p < 0)
			return s;
		else
			return s.substring(0, p);
	}
	
	public String getOnlineCatalogPluginPath()
	{
		if ( !pathname.startsWith(ONLINE_CATALOG_PLUGIN_PREFIX) )
			return null;
		String s = pathname.substring(ONLINE_CATALOG_PLUGIN_PREFIX.length());
		int p = s.indexOf(":");
		if (p < 0)
			return null;
		else
			return s.substring(p + 1);
	}
	
	public String getOnlineCatalogPluginId()
	{
		String s = getOnlineCatalogPluginPath();
		if (s == null)
			return null;
		int p = s.indexOf("=");
		if (p < 0)
			return null;
		else
			return s.substring(p + 1);
	}
	
	/**
	 * Get absolute path to file.
	 * For plain files, returns /abs_path_to_file/filename.ext
	 * For archives, returns /abs_path_to_archive/arc_file_name.zip@/filename_inside_archive.ext
	 * @return full path + filename
	 */
	public String getPathName()
	{
		if ( arcname!=null )
			return arcname + ARC_SEPARATOR + pathname;
		return pathname;
	}

	public String getBasePath()
	{
		if ( arcname!=null )
			return arcname;
		return pathname;
	}

	public int dirCount()
	{
		return dirs!=null ? dirs.size() : 0;
	}

	public int fileCount()
	{
		return files!=null ? files.size() : 0;
	}

	public int itemCount()
	{
		return dirCount() + fileCount();
	}

	public void addDir( FileInfo dir )
	{
		if ( dirs==null )
			dirs = new ArrayList<FileInfo>();
		dirs.add(dir);
		if (dir.parent == null)
			dir.parent = this;
	}
	public void addFile( FileInfo file )
	{
		if ( files==null )
			files = new ArrayList<FileInfo>();
		files.add(file);
	}
	public void addItems( Collection<FileInfo> items )
	{
		for ( FileInfo item : items ) {
			if ( item.isDirectory )
				addDir(item);
			else
				addFile(item);
			item.parent = this;
		}
	}
	public void replaceItems( Collection<FileInfo> items )
	{
		files = null;
		dirs = null;
		addItems( items );
	}
	public boolean updateItem( FileInfo item ) {
		if (null != dirs) {
			for (FileInfo dir : dirs) {
				if (dir.pathNameEquals(item)) {
					dir.assign(item);
					dir.setItems(item);
					return true;
				}
			}
		}
		if (null != files) {
			for (FileInfo file : files) {
				if (file.pathNameEquals(item)) {
					file.assign(item);
					return true;
				}
			}
		}
		return false;
	}
	public boolean isEmpty()
	{
		return fileCount()==0 && dirCount()==0;
	}
	public FileInfo getItem( int index )
	{
		if ( index<0 )
			throw new IndexOutOfBoundsException();
		if ( index<dirCount())
			return dirs.get(index);
		index -= dirCount();
		if ( index<fileCount())
			return files.get(index);
		Log.e("cr3", "Index out of bounds " + index + " at FileInfo.getItem() : returning 0");
		//throw new IndexOutOfBoundsException();
		return null;
	}
	public FileInfo findItemByPathName( String pathName )
	{
		if ( dirs!=null )
			for ( FileInfo dir : dirs )
				if ( isOnSDCard() && pathName.compareToIgnoreCase(dir.getPathName()) == 0 || pathName.equals(dir.getPathName()) )
					return dir;
		if ( files!=null )
			for ( FileInfo file : files ) {
				if ( isOnSDCard() && pathName.compareToIgnoreCase(file.getPathName()) == 0 || pathName.equals(file.getPathName()) )
					return file;
				if ( isOnSDCard() && file.getPathName().toLowerCase().startsWith(pathName.toLowerCase()+"@/") || file.getPathName().startsWith(pathName+"@/" ))
					return file;
			}
		return null;
	}

	public static boolean eq(String s1, String s2) {
		if (s1 == null)
			return s2 == null;
		return s1.equals(s2);
	}
	
	public boolean pathNameEquals(FileInfo item) {
		return isDirectory == item.isDirectory && eq(arcname, item.arcname) && eq(pathname, item.pathname);
	}
	
	public boolean hasItem(FileInfo item) {
		return getItemIndex(item) >= 0;
	}
	
	public int getItemIndex( FileInfo item )
	{
		if ( item==null )
			return -1;
		for ( int i=0; i<dirCount(); i++ ) {
			if ( item.pathNameEquals(getDir(i)) )
				return i;
		}
		for ( int i=0; i<fileCount(); i++ ) {
			if (item.pathNameEquals(getFile(i)))
				return i + dirCount();
		}
		return -1;
	}

	public int getFileIndex( FileInfo item )
	{
		if ( item==null )
			return -1;
		for ( int i=0; i<fileCount(); i++ ) {
			if (item.pathNameEquals(getFile(i)))
				return i;
		}
		return -1;
	}

	public FileInfo getDir( int index )
	{
		if ( index<0 )
			throw new IndexOutOfBoundsException();
		if ( index<dirCount())
			return dirs.get(index);
		throw new IndexOutOfBoundsException();
	}

	public FileInfo getFile( int index )
	{
		if ( index<0 )
			throw new IndexOutOfBoundsException();
		if ( index<fileCount())
			return files.get(index);
		throw new IndexOutOfBoundsException();
	}

	public boolean setFileProperties(FileInfo file)
	{
		boolean modified = false;
		modified = setTitle(file.getTitle()) || modified;
		modified = setAuthors(file.getAuthors()) || modified;
		modified = setSeriesName(file.getSeriesName()) || modified;
		modified = setSeriesNumber(file.getSeriesNumber()) || modified;
		modified = setReadingState(file.getReadingState()) || modified;
		modified = setRate(file.getRate()) || modified;
		return modified;
	}

    public void setFile(int index, FileInfo file)
    {
        if ( index<0 )
			throw new IndexOutOfBoundsException();
		if (index < fileCount()) {
			files.set(index, file);
			file.parent = this;
			return;
		}
		throw new IndexOutOfBoundsException();
    }
	
	public void setFile(FileInfo file)
	{
		int index = getFileIndex(file);
		if ( index<0 )
			return;
		setFile(index, file);
	}

	public void setItems(FileInfo copyFrom)
	{
		if (this == copyFrom)
			return;
		clear();
		for (int i=0; i<copyFrom.fileCount(); i++) {
			FileInfo file = copyFrom.getFile(i);
			file.parent = this;
			addFile(file);
		}
		for (int i=0; i<copyFrom.dirCount(); i++) {
			FileInfo dir = copyFrom.getDir(i);
			dir.parent = this;
			addDir(dir);
		}
		isListed = copyFrom.isListed;
		isScanned = copyFrom.isScanned;
	}

	public void setItems(Collection<FileInfo> list)
	{
		clear();
		if (list == null)
			return;
		for (FileInfo item : list) {
			if (item.isDirectory)
				addDir(item);
			else
				addFile(item);
			item.parent = this;
		}
		isListed = true;
	}

	public boolean removeEmptyDirs()
	{
		if ( parent==null || pathname.startsWith("@") || !isListed || dirs==null )
			return false;
		boolean removed = false;
		for ( int i=dirCount()-1; i>=0; i-- ) {
			FileInfo dir = getDir(i);
			if ( dir.isListed && dir.dirCount() == 0 && dir.fileCount() == 0) {
				dirs.remove(i);
				removed = true;
			}
		}
		return removed;
	}
	
	public void removeChild( FileInfo item )
	{
		if ( item.isSpecialDir() )
			return;
		if ( files!=null ) {
			int n = files.indexOf(item);
			if ( n>=0 && n<files.size() ) {
				files.remove(n);
				return;
			}
		}
		if ( dirs!=null ) {
			int n = dirs.indexOf(item);
			if ( n>=0 && n<dirs.size() ) {
				dirs.remove(n);
			}
		}
	}
	
	public boolean deleteFile()
	{
		if ( isArchive ) {
			if ( isDirectory )
				return false;
			File f = new File(arcname);
			if ( f.exists() && !f.isDirectory() ) {
				if ( !f.delete() )
					return false;
				if ( parent!=null ) {
					if ( parent.isArchive ) {
						// remove all files belonging to this archive
					} else {
						parent.removeChild(this);
					}
				}
				return true;
			}
		}
		if ( isDirectory )
			return false;
		if ( !fileExists() )
			return false;
		File f = new File(pathname);
		if ( f.delete() ) {
			if ( parent!=null ) {
				parent.removeChild(this);
			}
			return true;
		}
		return false;
	}

	public boolean fileExists()
	{
		if (isDirectory)
			return false;
		if ( isArchive ) {
			if ( arcname!=null )
				return new File(arcname).exists();
			return false;
		}
		return new File(pathname).exists();
	}
	
	/**
	 * @return true if item (file, directory, or archive) exists
	 */
	public boolean exists()
	{
		if ( isArchive ) {
			if ( arcname==null )
				return false;
			File f = new File(arcname);
			return f.exists();
		}
		File f = new File(pathname);
		return f.exists();
	}
	
	/**
	 * @return true if item is a directory, which exists and can be written to
	 */
	public boolean isWritableDirectory()
	{
		if (!isDirectory || isArchive || isSpecialDir())
			return false;
		File f = new File(pathname);
		boolean isDir = f.isDirectory();
		boolean canWr = f.canWrite();
//		if (!canWr) {
//			File testFile = new File(f, "cr3test.tmp");
//			try {
//				OutputStream os = new FileOutputStream(testFile, false);
//				os.close();
//				testFile.delete();
//				canWr = true;
//			} catch (FileNotFoundException e) {
//				L.e("cannot write " + testFile, e);
//			} catch (IOException e) {
//				L.e("cannot write " + testFile, e);
//			}
//		}
		return isDir && canWr;
	}
	
	/**
	 * @return true if item is a directory, which exists and can be written to
	 */
	public boolean isReadableDirectory()
	{
		if (!isDirectory || isArchive || isSpecialDir())
			return false;
		File f = new File(pathname);
		boolean isDir = f.isDirectory();
		boolean canRd = f.canRead();
		return isDir && canRd;
	}
	
	public String getAuthors() {
		return authors;
	}
	
	public boolean setAuthors(String authors) {
		if (eq(this.authors, authors))
			return false;
		this.authors = authors;
		return true;
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean setTitle(String title) {
		if (eq(this.title, title))
			return false;
		this.title = title;
		return true;
	}
	
	public String getSeriesName() {
		return series;
	}
	
	public boolean setSeriesName(String series) {
		if (eq(this.series, series))
			return false;
		this.series = series;
		return true;
	}
	
	public boolean setSeriesNumber(int seriesNumber) {
		if (this.seriesNumber == seriesNumber)
			return false;
		this.seriesNumber = seriesNumber;
		return true;
	}
	
	public int getSeriesNumber() {
		return series != null && series.length() > 0 ? seriesNumber : 0;
	}
	
	public String getLanguage() {
		return language;
	}

	public void clear()
	{
		dirs = null;
		files = null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (id == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeLong(id);
		}
		dest.writeString(title);
		dest.writeString(authors);
		dest.writeString(series);
		dest.writeInt(seriesNumber);
		dest.writeString(genres);
		dest.writeString(path);
		dest.writeString(filename);
		dest.writeString(pathname);
		dest.writeString(arcname);
		dest.writeString(language);
		dest.writeString(description);
		dest.writeString(username);
		dest.writeString(password);
		dest.writeLong(size);
		dest.writeLong(arcsize);
		dest.writeLong(createTime);
		dest.writeLong(lastAccessTime);
		dest.writeInt(flags);
		dest.writeByte((byte) (isArchive ? 1 : 0));
		dest.writeByte((byte) (isDirectory ? 1 : 0));
		dest.writeByte((byte) (isListed ? 1 : 0));
		dest.writeByte((byte) (isScanned ? 1 : 0));
		dest.writeLong(crc32);
		dest.writeInt(domVersion);
		dest.writeInt(blockRenderingFlags);
		dest.writeParcelable(parent, flags);
		dest.writeTypedList(files);
		dest.writeTypedList(dirs);
	}

	public static enum SortOrder {
		FILENAME(R.string.mi_book_sort_order_filename, new Comparator<FileInfo>() {
			public int compare( FileInfo f1, FileInfo f2 )
			{
				if ( f1==null || f2==null )
					return 0;
				return Utils.cmp(f1.getFileNameToDisplay(), f2.getFileNameToDisplay());
			}
		}),
		FILENAME_DESC(R.string.mi_book_sort_order_filename_desc, new Comparator<FileInfo>() {
			public int compare( FileInfo f1, FileInfo f2 )
			{
				if ( f1==null || f2==null )
					return 0;
				return Utils.cmp(f2.getFileNameToDisplay(), f1.getFileNameToDisplay());
			}
		}),
		TIMESTAMP(R.string.mi_book_sort_order_timestamp, new Comparator<FileInfo>() {
			public int compare( FileInfo f1, FileInfo f2 )
			{
				if ( f1==null || f2==null )
					return 0;
				return firstNz( cmp(f1.createTime, f2.createTime), Utils.cmp(f1.filename, f2.filename) );
			}
		}),
		TIMESTAMP_DESC(R.string.mi_book_sort_order_timestamp_desc, new Comparator<FileInfo>() {
			public int compare( FileInfo f1, FileInfo f2 )
			{
				if ( f1==null || f2==null )
					return 0;
				return firstNz( cmp(f2.createTime, f1.createTime), Utils.cmp(f2.filename, f1.filename) );
			}
		}),
		AUTHOR_TITLE(R.string.mi_book_sort_order_author, new Comparator<FileInfo>() {
			public int compare( FileInfo f1, FileInfo f2 )
			{
				if ( f1==null || f2==null )
					return 0;
				return firstNz(
						cmpNotNullFirst(Utils.formatAuthors(f1.authors), Utils.formatAuthors(f2.authors))
						,cmpNotNullFirst(f1.series, f2.series)
						,cmp(f1.getSeriesNumber(), f2.getSeriesNumber())
						,cmpNotNullFirst(f1.title, f2.title)
						,Utils.cmp(f1.filename, f2.filename) 
						);
			}
		}),
		AUTHOR_TITLE_DESC(R.string.mi_book_sort_order_author_desc, new Comparator<FileInfo>() {
			public int compare( FileInfo f1, FileInfo f2 )
			{
				if ( f1==null || f2==null )
					return 0;
				return firstNz(
						cmpNotNullFirst(Utils.formatAuthors(f2.authors), Utils.formatAuthors(f1.authors))
						,cmpNotNullFirst(f2.series, f1.series)
						,cmp(f2.getSeriesNumber(), f1.getSeriesNumber())
						,cmpNotNullFirst(f2.title, f1.title)
						,Utils.cmp(f2.filename, f1.filename)
				);
			}
		}),
		TITLE_AUTHOR(R.string.mi_book_sort_order_title, new Comparator<FileInfo>() {
			public int compare( FileInfo f1, FileInfo f2 )
			{
				if ( f1==null || f2==null )
					return 0;
				return firstNz(
						cmpNotNullFirst(f1.series, f2.series)
						,cmp(f1.getSeriesNumber(), f2.getSeriesNumber())
						,cmpNotNullFirst(f1.title, f2.title)
						,cmpNotNullFirst(Utils.formatAuthors(f1.authors), Utils.formatAuthors(f2.authors))
						,Utils.cmp(f1.filename, f2.filename) 
						);
			}
		}),
		TITLE_AUTHOR_DESC(R.string.mi_book_sort_order_title_desc, new Comparator<FileInfo>() {
			public int compare( FileInfo f1, FileInfo f2 )
			{
				if ( f1==null || f2==null )
					return 0;
				return firstNz(
						cmpNotNullFirst(f2.series, f1.series)
						,cmp(f2.getSeriesNumber(), f1.getSeriesNumber())
						,cmpNotNullFirst(f2.title, f1.title)
						,cmpNotNullFirst(Utils.formatAuthors(f2.authors), Utils.formatAuthors(f1.authors))
						,Utils.cmp(f2.filename, f1.filename)
				);
			}
		});
		//================================================
		private final Comparator<FileInfo> comparator;
		public final int resourceId;
		private SortOrder( int resourceId, Comparator<FileInfo> comparator )
		{
			this.resourceId = resourceId;
			this.comparator = comparator;
		}

		public final Comparator<FileInfo> getComparator()
		{
			return comparator;
		}
		
		/**
		 * Same as cmp, but not-null comes first
		 * @param str1
		 * @param str2
		 * @return
		 */
		private static int cmpNotNullFirst( String str1, String str2 )
		{
			if ( str1==null && str2==null )
				return 0;
			if ( str1==null )
				return 1;
			if ( str2==null )
				return -1;
			return Utils.cmp(str1, str2);
		}
		
		static int cmp( long n1, long n2 )
		{
			if ( n1<n2 )
				return -1;
			if ( n1>n2 )
				return 1;
			return 0;
		}
		
		private static int firstNz( int... v)
		{
			for ( int i=0; i<v.length; i++ ) {
				if ( v[i]!=0 )
					return v[i];
			}
			return 0;
		}
		public static SortOrder fromName( String name ) {
			if ( name!=null )
				for ( SortOrder order : values() )
					if ( order.name().equals(name) )
						return order;
			return DEF_SORT_ORDER;
		}
	}
	public final static SortOrder DEF_SORT_ORDER = SortOrder.AUTHOR_TITLE;
		
	public void sort( SortOrder SortOrder )
	{
		if ( dirs!=null ) {
			ArrayList<FileInfo> newDirs = new ArrayList<FileInfo>(dirs);
			Collections.sort( newDirs, SortOrder.getComparator() );
			dirs = newDirs;
		}
		if ( files!=null ) {
			ArrayList<FileInfo> newFiles = new ArrayList<FileInfo>(files);
			Collections.sort( newFiles, SortOrder.getComparator() );
			files = newFiles;
		}
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arcname == null) ? 0 : arcname.hashCode());
		result = prime * result + (int)arcsize;
		result = prime * result + ((authors == null) ? 0 : authors.hashCode());
		result = prime * result + (int) (createTime ^ (createTime >>> 32));
		result = prime * result + ((dirs == null) ? 0 : dirs.hashCode());
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + ((files == null) ? 0 : files.hashCode());
		result = prime * result + flags;
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + (isArchive ? 1231 : 1237);
		result = prime * result + (isDirectory ? 1231 : 1237);
		result = prime * result + (isListed ? 1231 : 1237);
		result = prime * result + (isScanned ? 1231 : 1237);
		result = prime * result
				+ ((language == null) ? 0 : language.hashCode());
		result = prime * result
				+ (int) (lastAccessTime ^ (lastAccessTime >>> 32));
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result
				+ ((pathname == null) ? 0 : pathname.hashCode());
		result = prime * result + ((series == null) ? 0 : series.hashCode());
		result = prime * result + seriesNumber;
		result = prime * result + (int)size;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileInfo other = (FileInfo) obj;
		if (arcname == null) {
			if (other.arcname != null)
				return false;
		} else if (!arcname.equals(other.arcname))
			return false;
		if (arcsize != other.arcsize)
			return false;
		if (authors == null) {
			if (other.authors != null)
				return false;
		} else if (!authors.equals(other.authors))
			return false;
		if (createTime != other.createTime)
			return false;
		if (dirs == null) {
			if (other.dirs != null)
				return false;
		} else if (!dirs.equals(other.dirs))
			return false;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		if (flags != other.flags)
			return false;
		if (format != other.format)
			return false;
		if (isArchive != other.isArchive)
			return false;
		if (isDirectory != other.isDirectory)
			return false;
		if (isListed != other.isListed)
			return false;
		if (isScanned != other.isScanned)
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		// do not compare genres of books, because in the absence of certain genres in the handbook,
		// the 'genres' field obtained from the database will not be equal to the field obtained when parsing the book file.
		/*
		if (!eqGenre(genres, other.genres))
			return false;
		*/
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (lastAccessTime != other.lastAccessTime)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (pathname == null) {
			if (other.pathname != null)
				return false;
		} else if (!pathname.equals(other.pathname))
			return false;
		if (series == null) {
			if (other.series != null && other.series.length() != 0)
				return false;
		} else if (!series.equals(other.series) && !(series.length() == 0 && other.series == null))
			return false;
		if (seriesNumber != other.seriesNumber)
			return false;
		if (size != other.size)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (domVersion != other.domVersion)
			return false;
		if (blockRenderingFlags != other.blockRenderingFlags)
			return false;
		// Base check: fingerprint (for now only crc32)
		return crc32 == other.crc32;
	}

	public boolean baseEquals(FileInfo other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (arcsize != other.arcsize)
			return false;
		if (authors == null) {
			if (other.authors.length() > 0)
				return false;
		} else if ( ! ( authors.equals(other.authors) || (authors.length() == 0 && other.authors == null) ) )
			return false;
		// TODO: potentially filename may not match. Perhaps we need to remove this check.
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (format != other.format)
			return false;
		// TODO: potentially isArchive may not match. Perhaps we need to remove this check.
		if (isArchive != other.isArchive)
			return false;
		if (isDirectory != other.isDirectory)
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		// do not compare genres of books, because in the absence of certain genres in the handbook,
		// the 'genres' field obtained from the database will not be equal to the field obtained when parsing the book file.
		/*
		if (!eqGenre(genres, other.genres))
			return false;
		*/
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (series == null) {
			if (other.series != null && other.series.length() != 0)
				return false;
		} else if (!series.equals(other.series) && !(series.length() == 0 && other.series == null))
			return false;
		if (seriesNumber != other.seriesNumber)
			return false;
		if (size != other.size)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		// Base check: fingerprint (for now only crc32)
		return crc32 == other.crc32;
	}

	private static boolean eqGenre(String g1, String g2) {
		if (g1 == null) {
			if (g2 != null && g2.length() != 0)
				return false;
		}
		if (g1.equals(g2))
			return true;
		String[] g1_array = g1.split("\\|");
		String[] g2_array = g2.split("\\|");
		if (g1_array.length == g2_array.length) {
			Arrays.sort(g1_array);
			Arrays.sort(g2_array);
			return Arrays.equals(g1_array, g2_array);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return pathname;
	}
	
	public boolean allowSorting() {
		return isDirectory && !isRootDir() && !isRecentDir() && !isOPDSDir() && !isBooksBySeriesDir();
	}
}
