//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing_run_audio.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BriefingRunAudio {
  /// Returns a new [BriefingRunAudio] instance.
  BriefingRunAudio({

    required  this.status,

     this.mediaId,

     this.format,

     this.voice,

     this.characters,

     this.truncated,

     this.error,

     this.bytes,

     this.contentType,

     this.signature,
  });

      /// `ok` stored; `failed` provider, budget or storage failure (the text was delivered anyway); `skipped` no `tts` provider or no text
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BriefingRunAudioStatusEnum status;



      /// 043 media (role `audio`), signed with 109
  @JsonKey(
    
    name: r'mediaId',
    required: false,
    includeIfNull: false,
  )


  final String? mediaId;



  @JsonKey(
    
    name: r'format',
    required: false,
    includeIfNull: false,
  )


  final BriefingRunAudioFormatEnum? format;



  @JsonKey(
    
    name: r'voice',
    required: false,
    includeIfNull: false,
  )


  final String? voice;



      /// Characters synthesized (the `tts` budget unit)
  @JsonKey(
    
    name: r'characters',
    required: false,
    includeIfNull: false,
  )


  final int? characters;



      /// True when the text was cut to fit (always for long `opus`; MP3 beyond 25 requests)
  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



      /// Size of the stored audio
  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



      /// Media type of the stored audio
  @JsonKey(
    
    name: r'contentType',
    required: false,
    includeIfNull: false,
  )


  final BriefingRunAudioContentTypeEnum? contentType;



      /// Detached JWS (`header..signature`, Ed25519, b64:false, `typ` the audio media type) over the audio bytes; verify against /.well-known/jwks.json (109)
  @JsonKey(
    
    name: r'signature',
    required: false,
    includeIfNull: false,
  )


  final String? signature;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BriefingRunAudio &&
      other.status == status &&
      other.mediaId == mediaId &&
      other.format == format &&
      other.voice == voice &&
      other.characters == characters &&
      other.truncated == truncated &&
      other.error == error &&
      other.bytes == bytes &&
      other.contentType == contentType &&
      other.signature == signature;

    @override
    int get hashCode =>
        status.hashCode +
        (mediaId == null ? 0 : mediaId.hashCode) +
        (format == null ? 0 : format.hashCode) +
        (voice == null ? 0 : voice.hashCode) +
        (characters == null ? 0 : characters.hashCode) +
        truncated.hashCode +
        (error == null ? 0 : error.hashCode) +
        (bytes == null ? 0 : bytes.hashCode) +
        (contentType == null ? 0 : contentType.hashCode) +
        (signature == null ? 0 : signature.hashCode);

  factory BriefingRunAudio.fromJson(Map<String, dynamic> json) => _$BriefingRunAudioFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingRunAudioToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `ok` stored; `failed` provider, budget or storage failure (the text was delivered anyway); `skipped` no `tts` provider or no text
enum BriefingRunAudioStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'skipped')
skipped(r'skipped');

const BriefingRunAudioStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BriefingRunAudioFormatEnum {
@JsonValue(r'mp3')
mp3(r'mp3'),
@JsonValue(r'opus')
opus(r'opus');

const BriefingRunAudioFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Media type of the stored audio
enum BriefingRunAudioContentTypeEnum {
@JsonValue(r'audio/mpeg')
audioSlashMpeg(r'audio/mpeg'),
@JsonValue(r'audio/ogg')
audioSlashOgg(r'audio/ogg');

const BriefingRunAudioContentTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


