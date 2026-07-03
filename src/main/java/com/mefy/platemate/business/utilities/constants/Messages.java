package com.mefy.platemate.business.utilities.constants;

public final class Messages {

    // User Messages
    public static final String USER_ADDED = "user.added";
    public static final String USER_USERNAME_EXISTS = "user.username.exists";
    public static final String USER_EMAIL_EXISTS = "user.email.exists";
    public static final String USER_NOT_FOUND = "user.not.found";
    public static final String USER_DELETED = "user.deleted";
    public static final String USER_UPDATED = "user.updated";
    public static final String USER_FOUND = "user.found";
    public static final String USERS_LISTED = "users.listed";
    public static final String USER_ROLE_NOT_FOUND = "user.role.not.found";
    // Auth Messages
    public static final String AUTH_INVALID_CREDENTIALS = "auth.invalid.credentials";
    public static final String AUTH_LOGIN_SUCCESS = "auth.login.success";
    public static final String AUTH_REFRESH_SUCCESS = "auth.refresh.success";
    public static final String AUTH_LOGOUT_SUCCESS = "auth.logout.success";
    public static final String AUTH_REFRESH_EXPIRED = "auth.refresh.expired";
    public static final String AUTH_REFRESH_REVOKED = "auth.refresh.revoked";
    public static final String AUTH_REFRESH_INVALID = "auth.refresh.invalid";
    public static final String AUTH_PASSWORD_CHANGED = "auth.password.changed";
    public static final String AUTH_CURRENT_PASSWORD_INVALID = "auth.current.password.invalid";
    public static final String AUTH_PASSWORD_SAME_AS_CURRENT = "auth.password.same.as.current";
    public static final String AUTH_UNAUTHORIZED = "auth.unauthorized";
    public static final String AUTH_TOKEN_INVALID = "auth.token.invalid";
    // Profile Messages
    public static final String PROFILE_NOT_FOUND = "profile.not.found";
    public static final String PROFILE_LISTED = "profile.listed";
    public static final String PROFILE_FOUND = "profile.found";
    public static final String PROFILE_UPDATED = "profile.updated";
    // Plate Messages
    public static final String PLATE_INVALID = "plate.invalid";
    public static final String PLATE_FOUND = "plate.found";
    public static final String PLATE_NOT_FOUND = "plate.not.found";
    public static final String PLATE_NOT_AVAILABLE = "plate.not.available";
    public static final String SUBSCRIPTION_ACTIVATED = "subscription.activated";
    public static final String SUBSCRIPTION_STATUS_FOUND = "subscription.status.found";
    public static final String SUBSCRIPTION_HISTORY_LISTED = "subscription.history.listed";
    // Pagination Messages
    public static final String PAGINATION_INVALID = "pagination.invalid";
    public static final String PAGINATION_PAGE_INVALID = "pagination.page.invalid";
    public static final String PAGINATION_SIZE_MIN_INVALID = "pagination.size.min.invalid";
    public static final String PAGINATION_SIZE_MAX_INVALID = "pagination.size.max.invalid";
    // Review Messages
    public static final String REVIEW_DELETE_UNAUTHORIZED = "review.delete.unauthorized";
    public static final String REVIEW_ADDED = "review.added";
    public static final String REVIEW_UPDATED = "review.updated";
    public static final String REVIEWS_LISTED = "reviews.listed";
    public static final String REVIEW_NOT_FOUND = "review.not.found";
    public static final String REVIEW_DELETED = "review.deleted";
    public static final String REVIEW_RESPONSIBILITY_REQUIRED = "review.responsibility.required";
    public static final String REVIEW_CONTENT_NOT_ALLOWED = "review.content.not.allowed";
    public static final String REVIEW_PENDING_REVIEW = "review.pending.review";
    public static final String REVIEW_COMMENT_PREMIUM_REQUIRED = "review.comment.premium.required";
    public static final String REVIEW_REPORT_TYPE_REQUIRED_FOR_NON_PREMIUM = "review.report.type.required.for.non.premium";
    public static final String REVIEW_ALREADY_EXISTS_FOR_PLATE = "review.already.exists.for.plate";
    // Plate Report Messages
    public static final String PLATE_REPORT_TYPES_LISTED = "plate.report.types.listed";
    public static final String PLATE_REPORTS_SYNCED = "plate.reports.synced";
    public static final String REPORT_TYPE_INVALID = "report.type.invalid";
    public static final String REPORT_TYPE_ALREADY_EXISTS = "report.type.already.exists";
    public static final String REPORT_TYPE_NOT_FOUND = "report.type.not.found";
    public static final String PLATE_REPORT_TYPE_ADDED = "plate.report.type.added";
    public static final String PLATE_REPORT_TYPE_UPDATED = "plate.report.type.updated";
    public static final String PLATE_REPORT_TYPE_STATUS_UPDATED = "plate.report.type.status.updated";
    
    // Plate Removal Request Messages
    public static final String PLATE_REMOVAL_REQUEST_CREATED = "plate.removal.request.created";
    public static final String PLATE_REMOVAL_PLATE_NOT_FOUND = "plate.removal.plate.not.found";
    public static final String PLATE_REMOVAL_REQUESTS_LISTED = "plate.removal.requests.listed";
    public static final String PLATE_REMOVAL_REQUEST_NOT_FOUND = "plate.removal.request.not.found";
    public static final String PLATE_REMOVAL_REQUEST_REVIEWED = "plate.removal.request.reviewed";
    public static final String VALIDATION_PLATE_REMOVAL_REASON_NOTNULL = "validation.plate.removal.reason.notnull";
    public static final String VALIDATION_PLATE_REMOVAL_STATUS_NOTNULL = "validation.plate.removal.status.notnull";
    public static final String VALIDATION_REPORT_TYPE_SEVERITY_NOTNULL = "validation.report.type.severity.notnull";
    public static final String VALIDATION_SOCIAL_PLATFORM_NOTNULL = "validation.social.platform.notnull";
    public static final String VALIDATION_SOCIAL_PLATFORM_CODE_NOTBLANK = "validation.social.platform.code.notblank";
    public static final String VALIDATION_SOCIAL_PLATFORM_LABEL_NOTBLANK = "validation.social.platform.label.notblank";
    public static final String VALIDATION_SOCIAL_PLATFORM_COLOR_INVALID = "validation.social.platform.color.invalid";
    public static final String VALIDATION_SOCIAL_PLATFORM_SORT_NOTNULL = "validation.social.platform.sort.notnull";
    public static final String VALIDATION_SOCIAL_PLATFORM_SORT_MIN = "validation.social.platform.sort.min";
    public static final String VALIDATION_SOCIAL_PLATFORM_ACTIVE_NOTNULL = "validation.social.platform.active.notnull";

    // Comment Report Messages
    public static final String COMMENT_REPORT_CREATED = "comment.report.created";
    public static final String COMMENT_REPORTS_LISTED = "comment.reports.listed";
    public static final String COMMENT_REPORT_REVIEWED = "comment.report.reviewed";
    public static final String COMMENT_REPORT_DUPLICATE = "comment.report.duplicate";
    public static final String COMMENT_REPORT_NOT_FOUND = "comment.report.not.found";
    public static final String COMMENT_REPORT_REVIEW_INVALID_STATUS = "comment.report.review.invalid.status";

    // Moderation Admin Messages
    public static final String ADMIN_COMMENTS_PENDING_LISTED = "admin.comments.pending.listed";
    public static final String ADMIN_COMMENT_APPROVED = "admin.comment.approved";
    public static final String ADMIN_COMMENT_REJECTED = "admin.comment.rejected";
    public static final String ADMIN_COMMENT_REMOVED = "admin.comment.removed";
    public static final String ADMIN_PLATES_HIDDEN_LISTED = "admin.plates.hidden.listed";
    public static final String ADMIN_PLATE_HIDDEN = "admin.plate.hidden";
    public static final String ADMIN_PLATE_RESTORED = "admin.plate.restored";
    // Discovery Messages
    public static final String DISCOVERY_HOME_FOUND = "discovery.home.found";
    public static final String DISCOVERY_TAB_LISTED = "discovery.tab.listed";
    public static final String DISCOVERY_CITY_PLATES_LISTED = "discovery.city.plates.listed";
    // Chat & Message Messages
    public static final String CHAT_ROOM_FOUND = "chat.room.found";
    public static final String CHAT_ROOM_CREATED = "chat.room.created";
    public static final String CHAT_ROOM_LEFT = "chat.room.left";
    public static final String MESSAGE_SENT = "message.sent";
    public static final String MESSAGES_LISTED = "messages.listed";
    public static final String MESSAGES_READ = "messages.read";
    public static final String USER_ROOMS_LISTED = "user.rooms.listed";
    public static final String MESSAGING_BLOCKED = "messaging.blocked";
    public static final String MESSAGE_REQUEST_LIMIT_REACHED = "message.request.limit.reached";
    public static final String MESSAGE_REQUEST_DECLINED = "message.request.declined";
    public static final String MESSAGE_REQUEST_ACCEPTED = "message.request.accepted";
    public static final String MESSAGE_REQUEST_DECLINED_OK = "message.request.declined.ok";
    public static final String MESSAGE_REQUEST_RESPOND_UNAUTHORIZED = "message.request.respond.unauthorized";
    // User Block Messages
    public static final String USER_BLOCK_SUCCESS = "user.block.success";
    public static final String USER_UNBLOCK_SUCCESS = "user.unblock.success";
    public static final String USER_BLOCK_SELF_NOT_ALLOWED = "user.block.self.not.allowed";
    public static final String USER_BLOCK_ALREADY_EXISTS = "user.block.already.exists";
    public static final String USER_BLOCK_NOT_FOUND = "user.block.not.found";
    // User Report Messages
    public static final String USER_REPORT_SUCCESS = "user.report.success";
    public static final String USER_REPORT_SELF_NOT_ALLOWED = "user.report.self.not.allowed";
    // City Messages
    public static final String CITIES_LISTED = "cities.listed";
    public static final String CITY_NOT_FOUND = "city.not.found";
    public static final String CITY_FOUND = "city.found";
    // Social Media Messages
    public static final String SOCIAL_LINK_ADDED = "social.link.added";
    public static final String SOCIAL_LINK_UPDATED = "social.link.updated";
    public static final String SOCIAL_LINK_DELETED = "social.link.deleted";
    public static final String SOCIAL_LINK_DELETE_UNAUTHORIZED = "social.link.delete.unauthorized";
    public static final String SOCIAL_LINK_NOT_FOUND = "social.link.not.found";
    public static final String SOCIAL_PLATFORM_ALREADY_EXISTS = "social.platform.already.exists";
    public static final String SOCIAL_PLATFORMS_LISTED = "social.platforms.listed";
    public static final String SOCIAL_PLATFORM_ADDED = "social.platform.added";
    public static final String SOCIAL_PLATFORM_UPDATED = "social.platform.updated";
    public static final String SOCIAL_PLATFORM_STATUS_UPDATED = "social.platform.status.updated";
    public static final String SOCIAL_PLATFORM_NOT_FOUND = "social.platform.not.found";
    public static final String SOCIAL_PLATFORM_CODE_ALREADY_EXISTS = "social.platform.code.already.exists";
    // Follow Messages
    public static final String FOLLOW_SUCCESS = "follow.success";
    public static final String FOLLOW_SELF_NOT_ALLOWED = "follow.self.not.allowed";
    public static final String FOLLOW_ALREADY_EXISTS = "follow.already.exists";
    public static final String FOLLOW_NOT_FOUND = "follow.not.found";
    public static final String UNFOLLOW_SUCCESS = "unfollow.success";
    // Plate Follow Messages
    public static final String PLATE_FOLLOW_SUCCESS = "plate.follow.success";
    public static final String PLATE_UNFOLLOW_SUCCESS = "plate.unfollow.success";
    public static final String PLATE_FOLLOW_ALREADY_EXISTS = "plate.follow.already.exists";
    public static final String PLATE_FOLLOW_NOT_FOUND = "plate.follow.not.found";
    public static final String PLATE_FOLLOW_LIMIT_REACHED = "plate.follow.limit.reached";
    // Plate Saved / Alarm Messages
    public static final String PLATE_SAVED_SUCCESS = "plate.saved.success";
    public static final String PLATE_UNSAVED_SUCCESS = "plate.unsaved.success";
    public static final String PLATE_ALARM_SUCCESS = "plate.alarm.success";
    public static final String PLATE_ALARM_REMOVED = "plate.alarm.removed";
    public static final String PLATE_ALARM_ALREADY_EXISTS = "plate.alarm.already.exists";
    public static final String PLATE_ALARM_NOT_FOUND = "plate.alarm.not.found";
    public static final String PLATE_ALARM_LIMIT_REACHED = "plate.alarm.limit.reached";
    public static final String PLATE_LISTS_FETCHED = "plate.lists.fetched";
    // Friendship Messages
    public static final String FRIENDSHIP_REQUEST_SENT = "friendship.request.sent";
    public static final String FRIENDSHIP_ACCEPTED = "friendship.accepted";
    public static final String FRIENDSHIP_REJECTED = "friendship.rejected";
    public static final String FRIENDSHIP_REMOVED = "friendship.removed";
    public static final String FRIENDSHIP_REMOVE_UNAUTHORIZED = "friendship.remove.unauthorized";
    public static final String FRIENDSHIP_ALREADY_EXISTS = "friendship.already.exists";
    public static final String FRIENDSHIP_ALREADY_ANSWERED = "friendship.already.answered";
    public static final String FRIENDSHIP_NOT_FOUND = "friendship.not.found";
    public static final String FRIENDSHIP_SELF_REQUEST = "friendship.self.request";
    public static final String FRIENDS_LISTED = "friends.listed";
    public static final String PENDING_REQUESTS_LISTED = "pending.requests.listed";
    // Settings Messages
    public static final String SETTINGS_UPDATED = "settings.updated";
    public static final String SETTINGS_FOUND = "settings.found";
    public static final String MESSAGING_DISABLED = "messaging.disabled";

    // Notification Messages
    public static final String NOTIFICATION_NEW_MESSAGE_TITLE = "notification.new_message.title";
    public static final String NOTIFICATION_NEW_MESSAGE_CONTENT = "notification.new_message.content";
    public static final String NOTIFICATION_FRIEND_REQUEST_TITLE = "notification.friend_request.title";
    public static final String NOTIFICATION_FRIEND_REQUEST_CONTENT = "notification.friend_request.content";
    public static final String NOTIFICATION_PLATE_REVIEW_TITLE = "notification.plate_review.title";
    public static final String NOTIFICATION_PLATE_REVIEW_CONTENT = "notification.plate_review.content";
    public static final String NOTIFICATION_NEW_FOLLOWER_TITLE = "notification.new_follower.title";
    public static final String NOTIFICATION_NEW_FOLLOWER_CONTENT = "notification.new_follower.content";

    // Global Exception Messages
    public static final String VALIDATION_ERROR = "validation.error";
    public static final String INVALID_REQUEST_PARAM = "invalid.request.param";
    public static final String UNEXPECTED_ERROR = "unexpected.error";

    private Messages() {
    } // Prevent instantiation (Utility class)
}
