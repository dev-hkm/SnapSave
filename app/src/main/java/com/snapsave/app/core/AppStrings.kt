package com.snapsave.app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

interface AppStrings {
    val appName: String
    val loading: String
    val cancel: String
    val save: String
    val delete: String
    val edit: String
    val back: String
    val copy: String
    val share: String
    val undo: String
    val all: String
    val auto: String
    fun autoWithLang(lang: String): String
    fun lines(count: Int): String
    fun chars(count: Int): String
    val justNow: String
    fun minutesAgo(min: Long): String
    fun hoursAgo(hr: Long): String
    val yesterday: String

    // Home Screen
    fun homeSnippetCount(count: Int, sizeStr: String): String
    val settingsDesc: String
    val addSnippet: String
    val searchPlaceholder: String
    val clearSearchDesc: String
    val searchEmptyHint: String
    val savedLanguagesHeader: String
    val tipCardTitle: String
    val tipCardDismiss: String
    val tipCardBody: String
    val emptyNoSnippetsTitle: String
    val emptyNoSnippetsMsg: String
    val emptySearchTitle: String
    val emptySearchMsg: String
    val snackbarDeleted: String

    // Create Screen
    val createTitle: String
    val saveButton: String
    val createCardTitle: String
    val createCardSubtitle: String
    val targetAppOnly: String
    val targetDevice: String
    val targetBoth: String
    val folderStorageTitle: String
    val folderNotSelected: String
    val deselect: String
    val selectFolder: String
    val changeFolder: String
    val fileNameLabel: String
    val pasteClipboard: String
    val contentLabel: String
    val createTip: String
    val selectFolderDialogError: String

    // Detail Screen
    val detailEditingTitle: String
    val exitEditDesc: String
    val editDesc: String
    val deleteDesc: String
    val saveToDevice: String
    val editFileName: String
    val editContent: String
    val saveEdits: String
    val deleteDialogTitle: String
    fun deleteDialogText(title: String): String
    fun copiedChars(count: Int): String
    val permissionRequired: String
    val shareTitle: String
    val savedEditsMsg: String
    fun savedToPath(path: String): String

    // Settings Screen
    val settingsTitle: String
    val languageGroupTitle: String
    val languageSubtitle: String
    val storageGroupTitle: String
    val storageSubtitle: String
    val defaultSaveDestinationTitle: String
    val defaultSaveDestinationSubtitle: String
    val appearanceGroupTitle: String
    val themeTitle: String
    val themeSystem: String
    val themeLight: String
    val themeDark: String
    val dynamicColorTitle: String
    val dynamicColorSubtitle: String
    val dynamicColorRequires: String
    val hapticsTitle: String
    val hapticsSubtitle: String
    val dataGroupTitle: String
    fun statsFormat(count: Int, sizeStr: String): String
    val resetTipTitle: String
    val resetTipSubtitle: String
    val deleteAllTitle: String
    val deleteAllSubtitle: String
    val clearDialogTitle: String
    val clearDialogText: String
    val aboutGroupTitle: String
    val aboutSubtitle: String
    val githubSource: String

    // Quick Save Sheet
    val quickSaveTitle: String
    fun detectedFormat(label: String): String
    val saveDestination: String
    fun folderLabel(name: String): String
    val tapToSelectFolder: String
    val selectAction: String
    val changeAction: String
    val fileName: String
    val fileType: String
    val saveFile: String
    val savingState: String
    val saveSuccessTitle: String
    val autoClosing: String
    val noFolderSelectedError: String
    val saveError: String
    fun savedToAppAndFolder(folderName: String): String
    fun savedToAppOnly(fileName: String): String
    fun savedToDeviceOnly(folderPath: String): String
    val customChip: String
    val customExtensionTitle: String
    val customExtensionPrompt: String
    val apply: String
    val openWithAction: String
    val openWithTitle: String
    val doneAction: String
}

object StringsEn : AppStrings {
    override val appName = "SnapSave"
    override val loading = "Loading…"
    override val cancel = "Cancel"
    override val save = "Save"
    override val delete = "Delete"
    override val edit = "Edit"
    override val back = "Back"
    override val copy = "Copy"
    override val share = "Share"
    override val undo = "Undo"
    override val all = "All"
    override val auto = "Auto"
    override fun autoWithLang(lang: String) = "Auto · $lang"
    override fun lines(count: Int) = if (count <= 1) "$count line" else "$count lines"
    override fun chars(count: Int) = if (count <= 1) "$count char" else "$count chars"
    override val justNow = "just now"
    override fun minutesAgo(min: Long) = "$min min ago"
    override fun hoursAgo(hr: Long) = "${hr}h ago"
    override val yesterday = "yesterday"

    // Home Screen
    override fun homeSnippetCount(count: Int, sizeStr: String) =
        if (count <= 1) "$count snippet · $sizeStr" else "$count snippets · $sizeStr"
    override val settingsDesc = "Settings"
    override val addSnippet = "Add snippet"
    override val searchPlaceholder = "Search by title, content, language…"
    override val clearSearchDesc = "Clear search"
    override val searchEmptyHint = "Type to search snippets…"
    override val savedLanguagesHeader = "Saved languages"
    override val tipCardTitle = "Quick save from any app"
    override val tipCardDismiss = "Dismiss tip"
    override val tipCardBody =
        "1. Select text in any app (even thousands of lines).\n" +
        "2. Tap SnapSave on the floating toolbar (or menu ⋮).\n" +
        "3. Check file name & language → Save.\n\n" +
        "SnapSave captures your selection directly, bypassing Android clipboard limits."
    override val emptyNoSnippetsTitle = "No snippets yet"
    override val emptyNoSnippetsMsg =
        "Select text in any app → tap SnapSave on the floating toolbar (or menu ⋮). Or tap + to add manually."
    override val emptySearchTitle = "No results found"
    override val emptySearchMsg = "Try a different keyword or clear language filter."
    override val snackbarDeleted = "Snippet deleted"

    // Create Screen
    override val createTitle = "New snippet"
    override val saveButton = "Save"
    override val createCardTitle = "Save destination & Folder"
    override val createCardSubtitle = "Choose where this snippet will be saved"
    override val targetAppOnly = "App only"
    override val targetDevice = "Device"
    override val targetBoth = "Both"
    override val folderStorageTitle = "Storage folder on device"
    override val folderNotSelected = "Not selected (tap button below to choose)"
    override val deselect = "Deselect"
    override val selectFolder = "Select folder"
    override val changeFolder = "Change folder"
    override val fileNameLabel = "File name (optional)"
    override val pasteClipboard = "Paste"
    override val contentLabel = "Paste or type content here"
    override val createTip =
        "If your clipboard was truncated, go back to the app, select the text, and tap SnapSave from the toolbar for 100% full content."
    override val selectFolderDialogError = "Please select a storage folder first!"

    // Detail Screen
    override val detailEditingTitle = "Edit snippet"
    override val exitEditDesc = "Exit edit"
    override val editDesc = "Edit"
    override val deleteDesc = "Delete"
    override val saveToDevice = "Save to device"
    override val editFileName = "File name"
    override val editContent = "Content"
    override val saveEdits = "Save changes"
    override val deleteDialogTitle = "Delete snippet?"
    override fun deleteDialogText(title: String) = "“$title” will be permanently deleted from your device."
    override fun copiedChars(count: Int) = "Copied $count characters"
    override val permissionRequired = "Storage write permission required"
    override val shareTitle = "Share file"
    override val savedEditsMsg = "Changes saved"
    override fun savedToPath(path: String) = "Saved to $path"

    // Settings Screen
    override val settingsTitle = "Settings"
    override val languageGroupTitle = "Language"
    override val languageSubtitle = "Display language"
    override val storageGroupTitle = "Storage & Quick Save"
    override val storageSubtitle = "Default destination when capturing selected text"
    override val defaultSaveDestinationTitle = "Default Save Destination"
    override val defaultSaveDestinationSubtitle = "Choose where Quick Save stores files by default: App only, Device folder, or Both"
    override val appearanceGroupTitle = "Appearance"
    override val themeTitle = "Theme"
    override val themeSystem = "System"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val dynamicColorTitle = "Dynamic Color (Material You)"
    override val dynamicColorSubtitle = "Sync colors with device wallpaper"
    override val dynamicColorRequires = "Requires Android 12+"
    override val hapticsTitle = "Haptic feedback"
    override val hapticsSubtitle = "Subtle vibrations on touch and action"
    override val dataGroupTitle = "Data & Storage"
    override fun statsFormat(count: Int, sizeStr: String) =
        if (count <= 1) "$count snippet · $sizeStr" else "$count snippets · $sizeStr"
    override val resetTipTitle = "Show quick save tip again"
    override val resetTipSubtitle = "Display guide card on home screen"
    override val deleteAllTitle = "Delete all snippets"
    override val deleteAllSubtitle = "Permanently delete database and saved files"
    override val clearDialogTitle = "Delete all data?"
    override val clearDialogText =
        "All snippets and local files will be permanently deleted. This action cannot be undone."
    override val aboutGroupTitle = "About"
    override val aboutSubtitle = "Direct text selection capture without clipboard limits."
    override val githubSource = "GitHub Source Code"

    // Quick Save Sheet
    override val quickSaveTitle = "Quick Save"
    override fun detectedFormat(label: String) = "Detected: $label"
    override val saveDestination = "Save destination"
    override fun folderLabel(name: String) = "Folder: $name"
    override val tapToSelectFolder = "Tap to choose storage folder on device"
    override val selectAction = "Select"
    override val changeAction = "Change"
    override val fileName = "File name"
    override val fileType = "File type"
    override val saveFile = "Save file"
    override val savingState = "Saving…"
    override val saveSuccessTitle = "Saved successfully!"
    override val autoClosing = "Closing automatically…"
    override val noFolderSelectedError = "No storage folder selected. Please choose a folder!"
    override val saveError = "Error saving file"
    override fun savedToAppAndFolder(folderName: String) = "Saved to SnapSave & $folderName"
    override fun savedToAppOnly(fileName: String) = "Saved to SnapSave: $fileName"
    override fun savedToDeviceOnly(folderPath: String) = "Saved to device: $folderPath"
    override val customChip = "+ Custom"
    override val customExtensionTitle = "Custom File Extension"
    override val customExtensionPrompt = "Enter file extension (e.g. vue, go, rs, env, log):"
    override val apply = "Apply"
    override val openWithAction = "Open with…"
    override val openWithTitle = "Open file with…"
    override val doneAction = "Done"
}

object StringsVi : AppStrings {
    override val appName = "SnapSave"
    override val loading = "Đang tải…"
    override val cancel = "Hủy"
    override val save = "Lưu"
    override val delete = "Xóa"
    override val edit = "Chỉnh sửa"
    override val back = "Quay lại"
    override val copy = "Sao chép"
    override val share = "Chia sẻ"
    override val undo = "Hoàn tác"
    override val all = "Tất cả"
    override val auto = "Tự động"
    override fun autoWithLang(lang: String) = "Tự động · $lang"
    override fun lines(count: Int) = "$count dòng"
    override fun chars(count: Int) = "$count ký tự"
    override val justNow = "vừa xong"
    override fun minutesAgo(min: Long) = "$min phút trước"
    override fun hoursAgo(hr: Long) = "$hr giờ trước"
    override val yesterday = "hôm qua"

    // Home Screen
    override fun homeSnippetCount(count: Int, sizeStr: String) = "$count snippet · $sizeStr"
    override val settingsDesc = "Cài đặt"
    override val addSnippet = "Thêm snippet"
    override val searchPlaceholder = "Tìm theo tên, nội dung, ngôn ngữ…"
    override val clearSearchDesc = "Xóa tìm kiếm"
    override val searchEmptyHint = "Gõ để tìm snippet…"
    override val savedLanguagesHeader = "Ngôn ngữ đã lưu"
    override val tipCardTitle = "Lưu nhanh từ bất kỳ app nào"
    override val tipCardDismiss = "Đóng gợi ý"
    override val tipCardBody =
        "1. Bôi đen đoạn văn bản (kể cả hàng nghìn dòng).\n" +
        "2. Chạm SnapSave trên thanh công cụ nổi — nếu không thấy, mở menu ⋮ của thanh công cụ.\n" +
        "3. Kiểm tra tên & loại file → Lưu.\n\n" +
        "SnapSave đọc trực tiếp phần bạn bôi đen nên không bị giới hạn như clipboard."
    override val emptyNoSnippetsTitle = "Chưa có snippet nào"
    override val emptyNoSnippetsMsg =
        "Bôi đen văn bản ở bất kỳ app nào → chạm SnapSave trên thanh công cụ nổi (hoặc menu ⋮). Hoặc nhấn nút + để tạo thủ công."
    override val emptySearchTitle = "Không thấy kết quả"
    override val emptySearchMsg = "Thử từ khóa khác hoặc xóa bộ lọc ngôn ngữ."
    override val snackbarDeleted = "Đã xóa snippet"

    // Create Screen
    override val createTitle = "Thêm snippet"
    override val saveButton = "Lưu"
    override val createCardTitle = "Nơi lưu & Thư mục"
    override val createCardSubtitle = "Chọn nơi lưu snippet này"
    override val targetAppOnly = "Vào App"
    override val targetDevice = "Vào Máy"
    override val targetBoth = "Cả hai"
    override val folderStorageTitle = "Thư mục lưu trên máy"
    override val folderNotSelected = "Chưa chọn (nhấn nút bên dưới để chọn)"
    override val deselect = "Hủy chọn"
    override val selectFolder = "Chọn thư mục"
    override val changeFolder = "Đổi thư mục"
    override val fileNameLabel = "Tên tệp (tùy chọn)"
    override val pasteClipboard = "Dán"
    override val contentLabel = "Dán hoặc nhập nội dung tại đây"
    override val createTip =
        "Nếu clipboard bị cắt mất phần đuôi, hãy quay lại app kia, bôi đen rồi chọn SnapSave — cách đó nhận đủ 100%."
    override val selectFolderDialogError = "Vui lòng chọn thư mục lưu trên máy trước!"

    // Detail Screen
    override val detailEditingTitle = "Chỉnh sửa"
    override val exitEditDesc = "Thoát chỉnh sửa"
    override val editDesc = "Chỉnh sửa"
    override val deleteDesc = "Xóa"
    override val saveToDevice = "Lưu máy"
    override val editFileName = "Tên tệp"
    override val editContent = "Nội dung"
    override val saveEdits = "Lưu chỉnh sửa"
    override val deleteDialogTitle = "Xóa snippet?"
    override fun deleteDialogText(title: String) = "“$title” sẽ bị xóa vĩnh viễn khỏi thiết bị."
    override fun copiedChars(count: Int) = "Đã sao chép $count ký tự"
    override val permissionRequired = "Chưa cấp quyền ghi bộ nhớ"
    override val shareTitle = "Chia sẻ tệp"
    override val savedEditsMsg = "Đã lưu chỉnh sửa"
    override fun savedToPath(path: String) = "Đã lưu vào $path"

    // Settings Screen
    override val settingsTitle = "Cài đặt"
    override val languageGroupTitle = "Ngôn ngữ"
    override val languageSubtitle = "Ngôn ngữ hiển thị ứng dụng"
    override val storageGroupTitle = "Nơi lưu tệp & Lưu nhanh"
    override val storageSubtitle = "Tùy chọn đích lưu khi bôi đen văn bản"
    override val defaultSaveDestinationTitle = "Vị trí lưu mặc định cho Lưu nhanh"
    override val defaultSaveDestinationSubtitle = "Chọn nơi Lưu nhanh sẽ tự động lưu: Vào app, Vào máy, hoặc Cả hai"
    override val appearanceGroupTitle = "Giao diện"
    override val themeTitle = "Chủ đề"
    override val themeSystem = "Hệ thống"
    override val themeLight = "Sáng"
    override val themeDark = "Tối"
    override val dynamicColorTitle = "Màu động (Material You)"
    override val dynamicColorSubtitle = "Đồng bộ màu theo hình nền máy"
    override val dynamicColorRequires = "Yêu cầu Android 12 trở lên"
    override val hapticsTitle = "Rung phản hồi"
    override val hapticsSubtitle = "Rung nhẹ khi chạm và thao tác"
    override val dataGroupTitle = "Dữ liệu & Bộ nhớ"
    override fun statsFormat(count: Int, sizeStr: String) = "$count snippet · $sizeStr"
    override val resetTipTitle = "Hiện lại mẹo Lưu nhanh"
    override val resetTipSubtitle = "Hiển thị lại card hướng dẫn trên trang chủ"
    override val deleteAllTitle = "Xóa toàn bộ dữ liệu snippet"
    override val deleteAllSubtitle = "Xóa vĩnh viễn cơ sở dữ liệu và các tệp đã lưu"
    override val clearDialogTitle = "Xóa toàn bộ dữ liệu?"
    override val clearDialogText =
        "Tất cả snippet và tệp cục bộ sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác."
    override val aboutGroupTitle = "Giới thiệu"
    override val aboutSubtitle = "Bắt văn bản bôi đen trực tiếp không giới hạn clipboard."
    override val githubSource = "Mã nguồn GitHub"

    // Quick Save Sheet
    override val quickSaveTitle = "Lưu nhanh văn bản"
    override fun detectedFormat(label: String) = "Nhận dạng: $label"
    override val saveDestination = "Nơi lưu"
    override fun folderLabel(name: String) = "Thư mục: $name"
    override val tapToSelectFolder = "Chạm để chọn thư mục trên máy"
    override val selectAction = "Chọn"
    override val changeAction = "Đổi"
    override val fileName = "Tên tệp"
    override val fileType = "Loại tệp"
    override val saveFile = "Lưu file"
    override val savingState = "Đang lưu…"
    override val saveSuccessTitle = "Đã lưu thành công!"
    override val autoClosing = "Tự động đóng…"
    override val noFolderSelectedError = "Chưa chọn thư mục lưu trên máy. Vui lòng chọn thư mục!"
    override val saveError = "Lỗi khi lưu tệp"
    override fun savedToAppAndFolder(folderName: String) = "Đã lưu vào SnapSave & $folderName"
    override fun savedToAppOnly(fileName: String) = "Đã lưu vào SnapSave: $fileName"
    override fun savedToDeviceOnly(folderPath: String) = "Đã lưu vào máy: $folderPath"
    override val customChip = "+ Tùy chỉnh"
    override val customExtensionTitle = "Định dạng tệp tùy chỉnh"
    override val customExtensionPrompt = "Nhập đuôi tệp (ví dụ: vue, go, rs, env, log):"
    override val apply = "Áp dụng"
    override val openWithAction = "Mở bằng…"
    override val openWithTitle = "Mở tệp bằng…"
    override val doneAction = "Xong"
}

val LocalAppStrings = compositionLocalOf<AppStrings> { StringsEn }

val S: AppStrings
    @Composable
    get() = LocalAppStrings.current
