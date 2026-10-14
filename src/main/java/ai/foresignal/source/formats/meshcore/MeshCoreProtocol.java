package ai.foresignal.source.formats.meshcore;

/**
 * The MeshCore companion protocol constants Foresignal uses (prompt 083), as the companion firmware defines them
 * ({@code examples/companion_radio/MyMesh.cpp}, MIT) and {@code docs/companion_protocol.md} documents them.
 */
public final class MeshCoreProtocol {

    private MeshCoreProtocol() {}

    /** The largest frame either side sends ({@code MAX_FRAME_SIZE}); a longer frame is skipped. */
    public static final int MAX_FRAME_SIZE = 176;

    /** The TCP envelope marker of a frame from the radio to the app ({@code '>'}). */
    public static final byte RADIO_TO_APP = '>';

    /** The TCP envelope marker of a frame from the app to the radio ({@code '<'}). */
    public static final byte APP_TO_RADIO = '<';

    /** The public key size. */
    public static final int PUB_KEY_SIZE = 32;

    /** The stored path size of a contact. */
    public static final int MAX_PATH_SIZE = 64;

    /** The fixed name field of a contact. */
    public static final int NAME_SIZE = 32;

    // ---- commands (app to radio)

    /** {@code CMD_APP_START}: identify the app; answered by {@link #RESP_SELF_INFO}. */
    public static final int CMD_APP_START = 0x01;
    /** {@code CMD_SEND_TXT_MSG}: a direct text message (prompt 084 registers it). */
    public static final int CMD_SEND_TXT_MSG = 0x02;
    /** {@code CMD_SEND_CHANNEL_TXT_MSG}: a channel text message (prompt 084 registers it). */
    public static final int CMD_SEND_CHANNEL_TXT_MSG = 0x03;
    /** {@code CMD_GET_CONTACTS}: the contact list, ended by {@link #RESP_END_OF_CONTACTS}. */
    public static final int CMD_GET_CONTACTS = 0x04;
    /** {@code CMD_GET_DEVICE_TIME}: answered by {@link #RESP_CURR_TIME}. */
    public static final int CMD_GET_DEVICE_TIME = 0x05;
    /** {@code CMD_SYNC_NEXT_MESSAGE}: the next queued message, or {@link #RESP_NO_MORE_MESSAGES}. */
    public static final int CMD_SYNC_NEXT_MESSAGE = 0x0A;
    /** {@code CMD_GET_BATT_AND_STORAGE}: answered by {@link #RESP_BATT_AND_STORAGE}. */
    public static final int CMD_GET_BATT_AND_STORAGE = 0x14;
    /** {@code CMD_DEVICE_QUERY}: answered by {@link #RESP_DEVICE_INFO}. */
    public static final int CMD_DEVICE_QUERY = 0x16;
    /** {@code CMD_GET_CONTACT_BY_KEY}: one contact, answered by {@link #RESP_CONTACT}. */
    public static final int CMD_GET_CONTACT_BY_KEY = 0x1E;
    /** {@code CMD_GET_CHANNEL}: one channel slot, answered by {@link #RESP_CHANNEL_INFO}. */
    public static final int CMD_GET_CHANNEL = 0x1F;
    /** {@code CMD_SEND_CHANNEL_DATA}: a channel datagram (prompt 084 registers it). */
    public static final int CMD_SEND_CHANNEL_DATA = 0x3E;

    /** The protocol version {@code CMD_DEVICE_QUERY} asks for. */
    public static final int DEVICE_QUERY_VERSION = 0x03;

    // ---- responses and pushes (radio to app)

    /** {@code RESP_CODE_OK}. */
    public static final int RESP_OK = 0x00;
    /** {@code RESP_CODE_ERR}, followed by an error code. */
    public static final int RESP_ERR = 0x01;
    /** {@code RESP_CODE_CONTACTS_START}, followed by the total count (uint32). */
    public static final int RESP_CONTACTS_START = 0x02;
    /** {@code RESP_CODE_CONTACT}: one contact frame. */
    public static final int RESP_CONTACT = 0x03;
    /** {@code RESP_CODE_END_OF_CONTACTS}. */
    public static final int RESP_END_OF_CONTACTS = 0x04;
    /** {@code RESP_CODE_SELF_INFO}. */
    public static final int RESP_SELF_INFO = 0x05;
    /** {@code RESP_CODE_SENT}. */
    public static final int RESP_SENT = 0x06;
    /** {@code RESP_CODE_CONTACT_MSG_RECV} (protocol version below 3). */
    public static final int RESP_CONTACT_MSG_RECV = 0x07;
    /** {@code RESP_CODE_CHANNEL_MSG_RECV} (protocol version below 3). */
    public static final int RESP_CHANNEL_MSG_RECV = 0x08;
    /** {@code RESP_CODE_CURR_TIME}. */
    public static final int RESP_CURR_TIME = 0x09;
    /** {@code RESP_CODE_NO_MORE_MESSAGES}. */
    public static final int RESP_NO_MORE_MESSAGES = 0x0A;
    /** {@code RESP_CODE_BATT_AND_STORAGE}. */
    public static final int RESP_BATT_AND_STORAGE = 0x0C;
    /** {@code RESP_CODE_DEVICE_INFO}. */
    public static final int RESP_DEVICE_INFO = 0x0D;
    /** {@code RESP_CODE_CONTACT_MSG_RECV_V3}, with SNR. */
    public static final int RESP_CONTACT_MSG_RECV_V3 = 0x10;
    /** {@code RESP_CODE_CHANNEL_MSG_RECV_V3}, with SNR. */
    public static final int RESP_CHANNEL_MSG_RECV_V3 = 0x11;
    /** {@code RESP_CODE_CHANNEL_INFO}. */
    public static final int RESP_CHANNEL_INFO = 0x12;
    /** {@code RESP_CODE_CHANNEL_DATA_RECV}: a channel datagram (not text; ignored by prompt 083). */
    public static final int RESP_CHANNEL_DATA_RECV = 0x1B;
    /** {@code PUSH_CODE_ADVERT}: an advert from a known contact, carrying only its public key. */
    public static final int PUSH_ADVERT = 0x80;
    /** {@code PUSH_CODE_SEND_CONFIRMED}. */
    public static final int PUSH_SEND_CONFIRMED = 0x82;
    /** {@code PUSH_CODE_MSG_WAITING}: messages are queued; poll with {@link #CMD_SYNC_NEXT_MESSAGE}. */
    public static final int PUSH_MSG_WAITING = 0x83;
    /** {@code PUSH_CODE_NEW_ADVERT}: a new contact, as a full contact frame. */
    public static final int PUSH_NEW_ADVERT = 0x8A;

    // ---- advert types

    /** {@code ADV_TYPE_CHAT}. */
    public static final int ADV_TYPE_CHAT = 1;
    /** {@code ADV_TYPE_REPEATER}. */
    public static final int ADV_TYPE_REPEATER = 2;
    /** {@code ADV_TYPE_ROOM}. */
    public static final int ADV_TYPE_ROOM = 3;
    /** {@code ADV_TYPE_SENSOR}. */
    public static final int ADV_TYPE_SENSOR = 4;

    /** The payload {@code nodeType} of an advert type: chat, repeater, room or sensor; null for anything else. */
    public static String nodeType(int advType) {
        return switch (advType) {
            case ADV_TYPE_CHAT -> "chat";
            case ADV_TYPE_REPEATER -> "repeater";
            case ADV_TYPE_ROOM -> "room";
            case ADV_TYPE_SENSOR -> "sensor";
            default -> null;
        };
    }
}
