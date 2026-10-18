// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attachment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttachmentCWProxy {
  Attachment filename(String filename);

  Attachment contentType(String contentType);

  Attachment bytes(int bytes);

  Attachment mediaId(String? mediaId);

  Attachment url(String? url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Attachment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Attachment(...).copyWith(id: 12, name: "My name")
  /// ```
  Attachment call({
    String filename,
    String contentType,
    int bytes,
    String? mediaId,
    String? url,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttachment.copyWith(...)` or call `instanceOfAttachment.copyWith.fieldName(value)` for a single field.
class _$AttachmentCWProxyImpl implements _$AttachmentCWProxy {
  const _$AttachmentCWProxyImpl(this._value);

  final Attachment _value;

  @override
  Attachment filename(String filename) => call(filename: filename);

  @override
  Attachment contentType(String contentType) => call(contentType: contentType);

  @override
  Attachment bytes(int bytes) => call(bytes: bytes);

  @override
  Attachment mediaId(String? mediaId) => call(mediaId: mediaId);

  @override
  Attachment url(String? url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Attachment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Attachment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Attachment call({
    Object? filename = const $CopyWithPlaceholder(),
    Object? contentType = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? mediaId = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return Attachment(
      filename: filename == const $CopyWithPlaceholder() || filename == null
          ? _value.filename
          // ignore: cast_nullable_to_non_nullable
          : filename as String,
      contentType:
          contentType == const $CopyWithPlaceholder() || contentType == null
          ? _value.contentType
          // ignore: cast_nullable_to_non_nullable
          : contentType as String,
      bytes: bytes == const $CopyWithPlaceholder() || bytes == null
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int,
      mediaId: mediaId == const $CopyWithPlaceholder()
          ? _value.mediaId
          // ignore: cast_nullable_to_non_nullable
          : mediaId as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
    );
  }
}

extension $AttachmentCopyWith on Attachment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttachment.copyWith(...)` or `instanceOfAttachment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttachmentCWProxy get copyWith => _$AttachmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Attachment _$AttachmentFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Attachment',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['filename', 'contentType', 'bytes']);
    final val = Attachment(
      filename: $checkedConvert('filename', (v) => v as String),
      contentType: $checkedConvert('contentType', (v) => v as String),
      bytes: $checkedConvert('bytes', (v) => (v as num).toInt()),
      mediaId: $checkedConvert('mediaId', (v) => v as String?),
      url: $checkedConvert('url', (v) => v as String?),
    );
    return val;
  },
);

Map<String, dynamic> _$AttachmentToJson(Attachment instance) =>
    <String, dynamic>{
      'filename': instance.filename,
      'contentType': instance.contentType,
      'bytes': instance.bytes,
      'mediaId': ?instance.mediaId,
      'url': ?instance.url,
    };
