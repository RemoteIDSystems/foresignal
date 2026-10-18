// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_post_media.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPostMediaCWProxy {
  SocialPostMedia type(SocialPostMediaTypeEnum type);

  SocialPostMedia url(String url);

  SocialPostMedia thumbUrl(String? thumbUrl);

  SocialPostMedia altText(String? altText);

  SocialPostMedia fingerprintId(String? fingerprintId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostMedia(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostMedia(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPostMedia call({
    SocialPostMediaTypeEnum type,
    String url,
    String? thumbUrl,
    String? altText,
    String? fingerprintId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPostMedia.copyWith(...)` or call `instanceOfSocialPostMedia.copyWith.fieldName(value)` for a single field.
class _$SocialPostMediaCWProxyImpl implements _$SocialPostMediaCWProxy {
  const _$SocialPostMediaCWProxyImpl(this._value);

  final SocialPostMedia _value;

  @override
  SocialPostMedia type(SocialPostMediaTypeEnum type) => call(type: type);

  @override
  SocialPostMedia url(String url) => call(url: url);

  @override
  SocialPostMedia thumbUrl(String? thumbUrl) => call(thumbUrl: thumbUrl);

  @override
  SocialPostMedia altText(String? altText) => call(altText: altText);

  @override
  SocialPostMedia fingerprintId(String? fingerprintId) =>
      call(fingerprintId: fingerprintId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostMedia(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostMedia(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPostMedia call({
    Object? type = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? thumbUrl = const $CopyWithPlaceholder(),
    Object? altText = const $CopyWithPlaceholder(),
    Object? fingerprintId = const $CopyWithPlaceholder(),
  }) {
    return SocialPostMedia(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SocialPostMediaTypeEnum,
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
      thumbUrl: thumbUrl == const $CopyWithPlaceholder()
          ? _value.thumbUrl
          // ignore: cast_nullable_to_non_nullable
          : thumbUrl as String?,
      altText: altText == const $CopyWithPlaceholder()
          ? _value.altText
          // ignore: cast_nullable_to_non_nullable
          : altText as String?,
      fingerprintId: fingerprintId == const $CopyWithPlaceholder()
          ? _value.fingerprintId
          // ignore: cast_nullable_to_non_nullable
          : fingerprintId as String?,
    );
  }
}

extension $SocialPostMediaCopyWith on SocialPostMedia {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPostMedia.copyWith(...)` or `instanceOfSocialPostMedia.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPostMediaCWProxy get copyWith => _$SocialPostMediaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPostMedia _$SocialPostMediaFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialPostMedia', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'url']);
      final val = SocialPostMedia(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$SocialPostMediaTypeEnumEnumMap, v),
        ),
        url: $checkedConvert('url', (v) => v as String),
        thumbUrl: $checkedConvert('thumbUrl', (v) => v as String?),
        altText: $checkedConvert('altText', (v) => v as String?),
        fingerprintId: $checkedConvert('fingerprintId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialPostMediaToJson(SocialPostMedia instance) =>
    <String, dynamic>{
      'type': _$SocialPostMediaTypeEnumEnumMap[instance.type]!,
      'url': instance.url,
      'thumbUrl': ?instance.thumbUrl,
      'altText': ?instance.altText,
      'fingerprintId': ?instance.fingerprintId,
    };

const _$SocialPostMediaTypeEnumEnumMap = {
  SocialPostMediaTypeEnum.image: 'image',
  SocialPostMediaTypeEnum.video: 'video',
  SocialPostMediaTypeEnum.gif: 'gif',
  SocialPostMediaTypeEnum.audio: 'audio',
  SocialPostMediaTypeEnum.linkCard: 'link-card',
  SocialPostMediaTypeEnum.other: 'other',
};
