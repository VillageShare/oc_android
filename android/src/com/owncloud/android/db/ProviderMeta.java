/* ownCloud Android client application
 *   Copyright (C) 2011  Bartek Przybylski
 *   Copyright (C) 2012-2013 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.owncloud.android.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Meta-Class that holds various static field information
 * 
 * @author Bartek Przybylski
 * 
 */
public class ProviderMeta {

    //Database constants
    public static final String AUTHORITY = "org.owncloud";
    public static final String OC_DB = "owncloud.db";
    
    public static final int DB_VERSION = 4;

    private ProviderMeta() {
    }
    
    //Table constants
    static public class ProviderTableMeta implements BaseColumns {
        public static final String FILES_TABLE_NAME = "filelist";
        public static final String SHARED_WITH_ME_TABLE_NAME = "sharedwithme";  // files that are shared with user
        public static final String SHARED_BY_ME_TABLE_NAME = "sharedbyme";  // files that are shared by the user
        public static final String GROUPS_TABLE_NAME = "groups";  // groups that the user is in
        public static final String FRIENDS_TABLE_NAME = "friends"; //friends table
        
        //FILES
        //"tables"
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/");
        public static final Uri CONTENT_URI_FILE = Uri.parse("content://"
                + AUTHORITY + "/file");
        public static final Uri CONTENT_URI_DIR = Uri.parse("content://"
                + AUTHORITY + "/dir");
        
        //types
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.owncloud.file";
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.owncloud.file";

        public static final String FILE_PARENT = "parent";
        public static final String FILE_NAME = "filename";
        public static final String FILE_CREATION = "created";
        public static final String FILE_MODIFIED = "modified";
        public static final String FILE_MODIFIED_AT_LAST_SYNC_FOR_DATA = "modified_at_last_sync_for_data";
        public static final String FILE_CONTENT_LENGTH = "content_length";
        public static final String FILE_CONTENT_TYPE = "content_type";
        public static final String FILE_STORAGE_PATH = "media_path";
        public static final String FILE_PATH = "path";
        public static final String FILE_ACCOUNT_OWNER = "file_owner";
        public static final String FILE_LAST_SYNC_DATE = "last_sync_date";  // _for_properties, but let's keep it as it is
        public static final String FILE_LAST_SYNC_DATE_FOR_DATA = "last_sync_date_for_data";
        public static final String FILE_KEEP_IN_SYNC = "keep_in_sync";
        public static final String FILE_SERVER_ID = "server_id";
  
        
        
        public static final String FILE_DEFAULT_SORT_ORDER = FILE_NAME
                + " collate nocase asc";
        
        //SHARE WITH ME
        public static final Uri CONTENT_URI_SHARED_WITH_ME = Uri.parse("content://"
                + AUTHORITY + "/sharedwithme");
        
        public static final String SHAREDWM_OWNER_LOCATION = "sharedwm_file_owner_location";
        public static final String SHAREDWM_FILE_NAME = "sharedwm_file_name";
        public static final String SHAREDWM_FILE_SERVER_ID = "sharedwm_file_id";
        //sort orded
        public static final String SHAREDWM_FILE_DEFAULT_SORT_ORDER = SHAREDWM_FILE_NAME;
        
        
        //SHARE BY ME
        
        public static final Uri CONTENT_URI_SHARED_BY_ME = Uri.parse("content://"
                + AUTHORITY + "/sharedbyme");
        
        public static final String SHAREDBM_USER_LOCATION = "sharedbm_file_user_location";
        public static final String SHAREDBM_FILE_SERVER_ID = "sharedbm_file_id";
        //sort orded
        public static final String SHAREDBM_FILE_DEFAULT_SORT_ORDER = SHAREDBM_FILE_SERVER_ID;
        
        
        
        //GROUPS
        
        public static final Uri CONTENT_URI_GROUPS = Uri.parse("content://"
                + AUTHORITY + "/groups");
        
        public static final String GROUP = "groups";
        public static final String GROUP_ADMIN = "group_admin"; //am I admin of this group
        //sort order
        public static final String GROUP_DEFAULT_SORT_ORDER = GROUP;
        
        //FRIENDS
        public static final Uri CONTENT_URI_FRIENDS = Uri.parse("content://"
                + AUTHORITY + "/friends");
        
        public static final String FRIEND = "friend";

        //sort order
        public static final String FRIEND_DEFAULT_SORT_ORDER = FRIEND;
        

    }
}
