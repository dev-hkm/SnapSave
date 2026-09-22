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
    val shareAction: String
    val deleteCustomExtensionTitle: String
    fun deleteCustomExtensionPrompt(ext: String): String
    val doneAction: String

    // Home layout & toggles
    val showSearchBarTitle: String
    val showSearchBarSubtitle: String
    val showCategoryBarTitle: String
    val showCategoryBarSubtitle: String
    val viewLayoutTitle: String
    val viewLayoutList: String
    val viewLayoutGrid: String
    val switchViewModeDesc: String

    // Long press action sheet
    val cardActionSheetTitle: String
    val actionViewDetails: String
    val actionCopyCode: String
    val actionShareFile: String
    val actionDeleteFile: String
    val actionOpenWith: String
    val copiedToClipboard: String

    // Pin, Sort & Export
    val actionPinSnippet: String
    val actionUnpinSnippet: String
    val pinnedHeader: String
    val sortMenuTitle: String
    val sortNewest: String
    val sortOldest: String
    val sortTitle: String
    val sortSize: String
    val quickCopied: String
    val actionExportFile: String

    // Floating Overlay (Quick Snippets)
    val floatingOverlayTitle: String
    val floatingOverlaySubtitle: String
    val floatingOverlayPermissionRequired: String
    val overlayAfterCopyTitle: String
    val overlayAfterCopyClose: String
    val overlayAfterCopyKeep: String
    val quickSaveClipboard: String
    val clipboardSavedSuccess: String
    val clipboardEmpty: String
    val overlayAppearanceSection: String
    val overlayPresets: String
    val overlayPresetBalanced: String
    val overlayPresetBalancedDesc: String
    val overlayPresetFloating: String
    val overlayPresetFloatingDesc: String
    val overlayPresetDiscreet: String
    val overlayPresetDiscreetDesc: String
    val overlayBubbleGroup: String
    val overlayBubbleSize: String
    val overlayBubbleOpacity: String
    val overlayPopupComposition: String
    val overlayCompositionTip: String
    val overlayMasterOpacity: String
    val overlayMasterOpacitySubtitle: String
    val overlaySurfaceOpacity: String
    val overlaySurfaceOpacitySubtitle: String
    val overlaySnippetsOpacity: String
    val overlaySnippetsOpacitySubtitle: String
    val overlayChromeOpacity: String
    val overlayChromeOpacitySubtitle: String
    val overlayCloseOpacity: String
    val overlayCloseOpacitySubtitle: String
    val overlayResizeOpacity: String
    val overlayResizeOpacitySubtitle: String
    val overlaySnippetClarity: String
    val overlayShadowStrength: String
    val overlayShadowStrengthSubtitle: String
    val overlayRevealControls: String
    val overlayRevealControlsSubtitle: String
    val overlayResetAppearance: String
    val overlayResetAppearanceSubtitle: String
    val overlayResetConfirmTitle: String
    val overlayResetConfirmMessage: String
    val overlayResetConfirmButton: String
    val overlayOpeningBehavior: String
    val overlayOpenPopupWith: String
    val overlayFilterAll: String
    val overlayFilterPinned: String
    val overlayFilterFrequent: String
    val overlayFilterLastUsed: String
    val overlayFilterCustom: String
    val overlayPopupContent: String
    val overlayShowTitle: String
    val overlayShowTitleSubtitle: String
    val overlayShowSearch: String
    val overlayShowSearchSubtitle: String
    val overlayShowCategories: String
    val overlayShowCategoriesSubtitle: String
    val overlayGrantPermission: String
    val overlayPermissionGranted: String
    fun overlayVisiblePercent(percent: Int): String
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
    override val shareAction = "Share"
    override val deleteCustomExtensionTitle = "Remove Extension"
    override fun deleteCustomExtensionPrompt(ext: String) = "Do you want to remove .$ext from your saved extensions?"
    override val doneAction = "Done"

    // Home layout & toggles
    override val showSearchBarTitle = "Show search bar"
    override val showSearchBarSubtitle = "Display search field on home screen"
    override val showCategoryBarTitle = "Show category tags"
    override val showCategoryBarSubtitle = "Display language filter chips on home screen"
    override val viewLayoutTitle = "Default View Layout"
    override val viewLayoutList = "List"
    override val viewLayoutGrid = "Grid"
    override val switchViewModeDesc = "Switch between list and grid view"

    // Long press action sheet
    override val cardActionSheetTitle = "Snippet Actions"
    override val actionViewDetails = "View details"
    override val actionCopyCode = "Copy code"
    override val actionShareFile = "Share snippet"
    override val actionDeleteFile = "Delete snippet"
    override val actionOpenWith = "Open with…"
    override val copiedToClipboard = "Snippet copied to clipboard"

    // Pin, Sort & Quick Copy
    override val actionPinSnippet = "Pin to top"
    override val actionUnpinSnippet = "Unpin snippet"
    override val pinnedHeader = "Pinned"
    override val sortMenuTitle = "Sort snippets"
    override val sortNewest = "Newest first"
    override val sortOldest = "Oldest first"
    override val sortTitle = "Title (A–Z)"
    override val sortSize = "File size"
    override val quickCopied = "Code copied!"
    override val actionExportFile = "Export to device (Download)"

    // Floating Overlay
    override val floatingOverlayTitle = "Floating Quick Snippets"
    override val floatingOverlaySubtitle = "Draggable floating bubble & popup to copy/paste long content anywhere"
    override val floatingOverlayPermissionRequired = "Requires 'Display over other apps' permission"
    override val overlayAfterCopyTitle = "After copying snippet"
    override val overlayAfterCopyClose = "Close popup"
    override val overlayAfterCopyKeep = "Keep popup open"
    override val quickSaveClipboard = "Save clipboard"
    override val clipboardSavedSuccess = "Saved clipboard content as snippet!"
    override val clipboardEmpty = "Clipboard is empty"
    override val overlayAppearanceSection = "Appearance"
    override val overlayPresets = "Appearance presets"
    override val overlayPresetBalanced = "Balanced"
    override val overlayPresetBalancedDesc = "Solid contrast with discreet controls"
    override val overlayPresetFloating = "Floating snippets"
    override val overlayPresetFloatingDesc = "No panel background; full-strength snippets"
    override val overlayPresetDiscreet = "Discreet"
    override val overlayPresetDiscreetDesc = "A softer panel without dimming your snippets"
    override val overlayBubbleGroup = "Bubble"
    override val overlayBubbleSize = "Bubble size"
    override val overlayBubbleOpacity = "Bubble opacity"
    override val overlayPopupComposition = "Popup composition"
    override val overlayCompositionTip = "Tip: For snippets floating directly on chat without a background, set Master to 100%, Popup background to 0%, and Snippets to 100%."
    override val overlayMasterOpacity = "Whole popup opacity (master)"
    override val overlayMasterOpacitySubtitle = "Master multiplier for all popup elements"
    override val overlaySurfaceOpacity = "Popup background opacity"
    override val overlaySurfaceOpacitySubtitle = "Surface background and border"
    override val overlaySnippetsOpacity = "Snippet cards opacity"
    override val overlaySnippetsOpacitySubtitle = "Snippet items in the list"
    override val overlayChromeOpacity = "Header / search / tags opacity"
    override val overlayChromeOpacitySubtitle = "Title, search box, and category chips"
    override val overlayCloseOpacity = "Close button opacity"
    override val overlayCloseOpacitySubtitle = "Top-right X button"
    override val overlayResizeOpacity = "Resize button opacity"
    override val overlayResizeOpacitySubtitle = "Bottom-right resize handle"
    override val overlaySnippetClarity = "Snippet clarity & elevation"
    override val overlayShadowStrength = "Snippet shadow strength"
    override val overlayShadowStrengthSubtitle = "Card elevation and outline contrast to stand out over background apps."
    override val overlayRevealControls = "Reveal overlay controls"
    override val overlayRevealControlsSubtitle = "Temporarily sets bubble and all popup layers to 100% visibility for 5 seconds"
    override val overlayResetAppearance = "Reset Quick Snippets appearance"
    override val overlayResetAppearanceSubtitle = "Reset all opacities, shadow, and bubble size to defaults without changing position or data"
    override val overlayResetConfirmTitle = "Reset appearance?"
    override val overlayResetConfirmMessage = "This will reset all bubble and popup opacities and sizes to defaults. Your saved snippets and filters will not be affected."
    override val overlayResetConfirmButton = "Reset"
    override val overlayOpeningBehavior = "Opening behavior"
    override val overlayOpenPopupWith = "Open popup with"
    override val overlayFilterAll = "All snippets"
    override val overlayFilterPinned = "Pinned snippets"
    override val overlayFilterFrequent = "Frequently used"
    override val overlayFilterLastUsed = "Last used filter"
    override val overlayFilterCustom = "Custom category"
    override val overlayPopupContent = "Popup content"
    override val overlayShowTitle = "Show title in popup"
    override val overlayShowTitleSubtitle = "Display app name and quick actions at the top"
    override val overlayShowSearch = "Show search in popup"
    override val overlayShowSearchSubtitle = "Display search input in quick snippets"
    override val overlayShowCategories = "Show categories in popup"
    override val overlayShowCategoriesSubtitle = "Display language tabs in quick snippets"
    override val overlayGrantPermission = "Grant"
    override val overlayPermissionGranted = "Permission granted"
    override fun overlayVisiblePercent(percent: Int) = "$percent% visible"
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
    override val shareAction = "Chia sẻ"
    override val deleteCustomExtensionTitle = "Xóa định dạng"
    override fun deleteCustomExtensionPrompt(ext: String) = "Bạn có muốn xóa .$ext khỏi danh sách đã lưu không?"
    override val doneAction = "Xong"

    // Home layout & toggles
    override val showSearchBarTitle = "Hiện thanh tìm kiếm"
    override val showSearchBarSubtitle = "Hiển thị ô tìm kiếm nhanh trên màn hình chính"
    override val showCategoryBarTitle = "Hiện thanh danh mục"
    override val showCategoryBarSubtitle = "Hiển thị hàng chip lọc ngôn ngữ trên màn hình chính"
    override val viewLayoutTitle = "Chế độ xem mặc định"
    override val viewLayoutList = "Danh sách"
    override val viewLayoutGrid = "Lưới"
    override val switchViewModeDesc = "Chuyển đổi giữa dạng danh sách và lưới"

    // Long press action sheet
    override val cardActionSheetTitle = "Thao tác tệp"
    override val actionViewDetails = "Xem chi tiết"
    override val actionCopyCode = "Sao chép mã"
    override val actionShareFile = "Chia sẻ tệp"
    override val actionDeleteFile = "Xóa tệp"
    override val actionOpenWith = "Mở bằng…"
    override val copiedToClipboard = "Đã sao chép vào bộ nhớ tạm"

    // Pin, Sort & Quick Copy
    override val actionPinSnippet = "Ghim lên đầu"
    override val actionUnpinSnippet = "Bỏ ghim"
    override val pinnedHeader = "Đã ghim"
    override val sortMenuTitle = "Sắp xếp"
    override val sortNewest = "Mới nhất"
    override val sortOldest = "Cũ nhất"
    override val sortTitle = "Tên (A–Z)"
    override val sortSize = "Dung lượng"
    override val quickCopied = "Đã chép mã!"
    override val actionExportFile = "Xuất ra bộ nhớ máy (Download)"

    // Floating Overlay
    override val floatingOverlayTitle = "Bóng nổi Quick Snippets"
    override val floatingOverlaySubtitle = "Bóng nổi & popup kéo thả để sao chép/dán nội dung dài ở mọi ứng dụng"
    override val floatingOverlayPermissionRequired = "Cần cấp quyền 'Hiển thị trên các ứng dụng khác'"
    override val overlayAfterCopyTitle = "Sau khi sao chép snippet"
    override val overlayAfterCopyClose = "Đóng cửa sổ nổi"
    override val overlayAfterCopyKeep = "Giữ cửa sổ nổi mở"
    override val quickSaveClipboard = "Lưu clipboard"
    override val clipboardSavedSuccess = "Đã lưu nội dung clipboard thành snippet mới!"
    override val clipboardEmpty = "Bộ nhớ tạm hiện đang trống"
    override val overlayAppearanceSection = "Giao diện"
    override val overlayPresets = "Giao diện mẫu (Presets)"
    override val overlayPresetBalanced = "Cân bằng"
    override val overlayPresetBalancedDesc = "Độ tương phản rõ nét với điều khiển kín đáo"
    override val overlayPresetFloating = "Snippet nổi không nền"
    override val overlayPresetFloatingDesc = "Ẩn hoàn toàn nền panel; chỉ hiện các thẻ snippet nổi"
    override val overlayPresetDiscreet = "Kín đáo"
    override val overlayPresetDiscreetDesc = "Panel mờ êm dịu, không làm tối thẻ snippet"
    override val overlayBubbleGroup = "Bong bóng nổi (Bubble)"
    override val overlayBubbleSize = "Kích thước bóng nổi"
    override val overlayBubbleOpacity = "Độ mờ bóng nổi"
    override val overlayPopupComposition = "Độ mờ các lớp Popup (Composition)"
    override val overlayCompositionTip = "Mẹo: Để snippet nổi trực tiếp trên cửa sổ ứng dụng khác mà không có khung nền, đặt Tổng thể 100%, Nền popup 0%, và Thẻ snippet 100%."
    override val overlayMasterOpacity = "Toàn bộ popup (Tổng thể)"
    override val overlayMasterOpacitySubtitle = "Hệ số mờ chung cho tất cả thành phần của popup"
    override val overlaySurfaceOpacity = "Nền popup (Background surface)"
    override val overlaySurfaceOpacitySubtitle = "Lớp nền và đường viền của cửa sổ nổi"
    override val overlaySnippetsOpacity = "Thẻ snippet (Cards)"
    override val overlaySnippetsOpacitySubtitle = "Nội dung các thẻ snippet trong danh sách"
    override val overlayChromeOpacity = "Thanh tiêu đề / tìm kiếm / thẻ tag"
    override val overlayChromeOpacitySubtitle = "Tiêu đề, ô tìm kiếm và hàng chip ngôn ngữ"
    override val overlayCloseOpacity = "Nút đóng (Close button)"
    override val overlayCloseOpacitySubtitle = "Nút tròn X ở góc trên bên phải"
    override val overlayResizeOpacity = "Nút co giãn (Resize handle)"
    override val overlayResizeOpacitySubtitle = "Tay cầm co giãn ở góc dưới bên phải"
    override val overlaySnippetClarity = "Độ rõ nét & Đổ bóng thẻ"
    override val overlayShadowStrength = "Độ tương phản đổ bóng thẻ"
    override val overlayShadowStrengthSubtitle = "Đổ bóng và viền nổi giúp thẻ snippet nổi bật trên nền ứng dụng khác."
    override val overlayRevealControls = "Hiện lại điều khiển popup"
    override val overlayRevealControlsSubtitle = "Tạm thời đặt bóng nổi và mọi lớp popup lên 100% rõ nét trong 5 giây"
    override val overlayResetAppearance = "Khôi phục giao diện mặc định"
    override val overlayResetAppearanceSubtitle = "Đặt lại toàn bộ độ mờ, bóng đổ và kích thước về mặc định mà không ảnh hưởng dữ liệu"
    override val overlayResetConfirmTitle = "Khôi phục giao diện?"
    override val overlayResetConfirmMessage = "Thao tác này sẽ đặt lại kích thước và độ mờ của bóng nổi và popup về mặc định ban đầu. Dữ liệu snippet của bạn sẽ không bị thay đổi."
    override val overlayResetConfirmButton = "Khôi phục"
    override val overlayOpeningBehavior = "Hành vi khi mở popup"
    override val overlayOpenPopupWith = "Mở popup với"
    override val overlayFilterAll = "Tất cả snippet"
    override val overlayFilterPinned = "Snippet đã ghim"
    override val overlayFilterFrequent = "Thường xuyên dùng"
    override val overlayFilterLastUsed = "Bộ lọc dùng gần nhất"
    override val overlayFilterCustom = "Danh mục tùy chọn"
    override val overlayPopupContent = "Nội dung hiển thị popup"
    override val overlayShowTitle = "Hiện tiêu đề trong popup"
    override val overlayShowTitleSubtitle = "Hiển thị tên app và nút lưu clipboard ở trên cùng"
    override val overlayShowSearch = "Hiện tìm kiếm trong popup"
    override val overlayShowSearchSubtitle = "Hiển thị ô nhập tìm kiếm nhanh trong popup"
    override val overlayShowCategories = "Hiện danh mục trong popup"
    override val overlayShowCategoriesSubtitle = "Hiển thị các chip lọc ngôn ngữ trong popup"
    override val overlayGrantPermission = "Cấp quyền"
    override val overlayPermissionGranted = "Đã cấp quyền"
    override fun overlayVisiblePercent(percent: Int) = "$percent% hiển thị"
}

val LocalAppStrings = compositionLocalOf<AppStrings> { StringsEn }

val S: AppStrings
    @Composable
    get() = LocalAppStrings.current
