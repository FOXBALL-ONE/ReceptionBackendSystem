export type SidebarTab = "messages" | "friends" | "groups" | "notifications";

export type ChatTargetType = "friend" | "group" | "system";
export type BackendChatType = "SINGLE" | "GROUP";
export type BackendDeliveryType = "REALTIME" | "HISTORY_REPLAY" | "RETRY";
export type BackendMessageStatus = "WAITING" | "SUCCESS" | "FAILED";
export type ChatBubbleKind = "text" | "file";
export type BackendMessageKind = "TEXT" | "FILE";

export type OnlineStatus = "online" | "busy" | "offline";

export interface SidebarSelection {
  id: string;
  name: string;
  type: ChatTargetType;
  avatarText: string;
  avatarColor: string;
  onlineStatus?: OnlineStatus;
  subtitle?: string;
  previewKind?: ChatBubbleKind;
  time?: string;
  contentText?: string;
  detailTime?: string;
  chatId?: number;
  chatType?: BackendChatType;
  receiveUserId?: number;
}

export interface ChatBubble {
  id: string;
  from: "me" | "other" | "system";
  content: string;
  time: string;
  senderName: string;
  avatarText: string;
  avatarColor: string;
  avatarUrl?: string;
  kind?: ChatBubbleKind;
  messageId?: number;
  fileName?: string;
}

export interface BackendMessage {
  id: number;
  messageId?: number;
  chatId: number;
  chatType: BackendChatType;
  senderUserId: number;
  sendUserId?: number | string;
  send_user_id?: number | string;
  sender_user_id?: number | string;
  receiveUserId?: number | string;
  receive_user_id?: number | string;
  receiverUserId?: number | string;
  receiver_user_id?: number | string;
  targetUserId?: number | string;
  target_user_id?: number | string;
  friendUserId?: number | string;
  friend_user_id?: number | string;
  userIdOne?: number | string;
  user_id_one?: number | string;
  userIdTwo?: number | string;
  user_id_two?: number | string;
  senderNickname?: string | null;
  sender_nickname?: string | null;
  sendUserNickname?: string | null;
  send_user_nickname?: string | null;
  senderUsername?: string | null;
  sender_username?: string | null;
  sendUserName?: string | null;
  send_user_name?: string | null;
  username?: string | null;
  nickname?: string | null;
  name?: string | null;
  senderAvatar?: string | null;
  sender_avatar?: string | null;
  sendUserAvatar?: string | null;
  send_user_avatar?: string | null;
  avatar?: string | null;
  messageKind?: BackendMessageKind | string | null;
  message_kind?: BackendMessageKind | string | null;
  content?: string | null;
  createTime: string;
  status?: BackendMessageStatus | string | null;
  deliveryType?: BackendDeliveryType | string | null;
  acked?: boolean;
  ackAt?: string;
}

export interface MessageListResponse {
  messages: BackendMessage[];
  count: number;
}
