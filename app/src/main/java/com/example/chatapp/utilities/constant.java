package
        com.example.chatapp.utilities;

import java.util.HashMap;

public class constant {

    //tạo ra những cột trong firebase
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String  KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATION = "conversations";
    public static final String KEY_SENDER_NAME  = "senderName";
    public static final String KEY_RECEIVER_NAME  = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE  = "receiverImage";
    public static final String KEY_LAST_MESSAGE  = "lastMessage";

    public static final String KEY_AVAILABILITY   = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content_Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static HashMap<String, String> remoteMsgHeader = null;
    public static HashMap<String, String> getRemoteMsgHeader() {
        if (remoteMsgHeader == null) {
            remoteMsgHeader = new HashMap<>();
            remoteMsgHeader.put(
                    //một header chứa mã thông báo (token) xác thực để máy chủ FCM biết là yêu cầu thông báo này được gửi từ ứng dụng của bạn.
                    // Giá trị của mã thông báo này là một chuỗi được cung cấp bởi FCM khi bạn đăng ký ứng dụng của mình trên nền tảng Firebase.
                    REMOTE_MSG_AUTHORIZATION, "key=AAAAKp461jg:APA91bEAgEt9Jm_mnEm0QAI-QbBpVUT9TmEUXrZ8MyujrImnmmuRu6a58aYzNHSQvM81rmDGd6XsUWIclFSFD3_SHKS39K-GhdVy7bHGbXyShs8H0Knks6XIilIjzGbUFdXFsHPyDPdB"
            );
            remoteMsgHeader.put(
                    // header chứa kiểu dữ liệu của nội dung yêu cầu. Trong trường hợp này, nó là "application/json", chỉ định rằng nội dung yêu cầu gửi đi sẽ là dữ liệu dạng JSON.
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeader;
    }


}

