//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_post_media.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPostMedia {
  /// Returns a new [SocialPostMedia] instance.
  SocialPostMedia({

    required  this.type,

    required  this.url,

     this.thumbUrl,

     this.altText,

     this.fingerprintId,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final SocialPostMediaTypeEnum type;



  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;



  @JsonKey(
    
    name: r'thumbUrl',
    required: false,
    includeIfNull: false,
  )


  final String? thumbUrl;



  @JsonKey(
    
    name: r'altText',
    required: false,
    includeIfNull: false,
  )


  final String? altText;



      /// 136 media fingerprint, once computed
  @JsonKey(
    
    name: r'fingerprintId',
    required: false,
    includeIfNull: false,
  )


  final String? fingerprintId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPostMedia &&
      other.type == type &&
      other.url == url &&
      other.thumbUrl == thumbUrl &&
      other.altText == altText &&
      other.fingerprintId == fingerprintId;

    @override
    int get hashCode =>
        type.hashCode +
        url.hashCode +
        (thumbUrl == null ? 0 : thumbUrl.hashCode) +
        (altText == null ? 0 : altText.hashCode) +
        (fingerprintId == null ? 0 : fingerprintId.hashCode);

  factory SocialPostMedia.fromJson(Map<String, dynamic> json) => _$SocialPostMediaFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPostMediaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialPostMediaTypeEnum {
@JsonValue(r'image')
image(r'image'),
@JsonValue(r'video')
video(r'video'),
@JsonValue(r'gif')
gif(r'gif'),
@JsonValue(r'audio')
audio(r'audio'),
@JsonValue(r'link-card')
linkCard(r'link-card'),
@JsonValue(r'other')
other(r'other');

const SocialPostMediaTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


