// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaCWProxy {
  Media id(String id);

  Media sha256(String sha256);

  Media contentType(MediaContentTypeEnum contentType);

  Media width(int width);

  Media height(int height);

  Media bytes(int bytes);

  Media role(MediaRoleEnum role);

  Media sourceId(String? sourceId);

  Media recordSourceId(String? recordSourceId);

  Media recordExternalId(String? recordExternalId);

  Media recordRef(String? recordRef);

  Media licence(String? licence);

  Media createdAt(DateTime createdAt);

  Media expiresAt(DateTime? expiresAt);

  Media variantOf(String? variantOf);

  Media variantWidth(int? variantWidth);

  Media links(MediaLinks? links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Media(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Media(...).copyWith(id: 12, name: "My name")
  /// ```
  Media call({
    String id,
    String sha256,
    MediaContentTypeEnum contentType,
    int width,
    int height,
    int bytes,
    MediaRoleEnum role,
    String? sourceId,
    String? recordSourceId,
    String? recordExternalId,
    String? recordRef,
    String? licence,
    DateTime createdAt,
    DateTime? expiresAt,
    String? variantOf,
    int? variantWidth,
    MediaLinks? links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMedia.copyWith(...)` or call `instanceOfMedia.copyWith.fieldName(value)` for a single field.
class _$MediaCWProxyImpl implements _$MediaCWProxy {
  const _$MediaCWProxyImpl(this._value);

  final Media _value;

  @override
  Media id(String id) => call(id: id);

  @override
  Media sha256(String sha256) => call(sha256: sha256);

  @override
  Media contentType(MediaContentTypeEnum contentType) =>
      call(contentType: contentType);

  @override
  Media width(int width) => call(width: width);

  @override
  Media height(int height) => call(height: height);

  @override
  Media bytes(int bytes) => call(bytes: bytes);

  @override
  Media role(MediaRoleEnum role) => call(role: role);

  @override
  Media sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  Media recordSourceId(String? recordSourceId) =>
      call(recordSourceId: recordSourceId);

  @override
  Media recordExternalId(String? recordExternalId) =>
      call(recordExternalId: recordExternalId);

  @override
  Media recordRef(String? recordRef) => call(recordRef: recordRef);

  @override
  Media licence(String? licence) => call(licence: licence);

  @override
  Media createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Media expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  Media variantOf(String? variantOf) => call(variantOf: variantOf);

  @override
  Media variantWidth(int? variantWidth) => call(variantWidth: variantWidth);

  @override
  Media links(MediaLinks? links) => call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Media(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Media(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Media call({
    Object? id = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? contentType = const $CopyWithPlaceholder(),
    Object? width = const $CopyWithPlaceholder(),
    Object? height = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? recordSourceId = const $CopyWithPlaceholder(),
    Object? recordExternalId = const $CopyWithPlaceholder(),
    Object? recordRef = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? variantOf = const $CopyWithPlaceholder(),
    Object? variantWidth = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return Media(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      contentType:
          contentType == const $CopyWithPlaceholder() || contentType == null
          ? _value.contentType
          // ignore: cast_nullable_to_non_nullable
          : contentType as MediaContentTypeEnum,
      width: width == const $CopyWithPlaceholder() || width == null
          ? _value.width
          // ignore: cast_nullable_to_non_nullable
          : width as int,
      height: height == const $CopyWithPlaceholder() || height == null
          ? _value.height
          // ignore: cast_nullable_to_non_nullable
          : height as int,
      bytes: bytes == const $CopyWithPlaceholder() || bytes == null
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as MediaRoleEnum,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      recordSourceId: recordSourceId == const $CopyWithPlaceholder()
          ? _value.recordSourceId
          // ignore: cast_nullable_to_non_nullable
          : recordSourceId as String?,
      recordExternalId: recordExternalId == const $CopyWithPlaceholder()
          ? _value.recordExternalId
          // ignore: cast_nullable_to_non_nullable
          : recordExternalId as String?,
      recordRef: recordRef == const $CopyWithPlaceholder()
          ? _value.recordRef
          // ignore: cast_nullable_to_non_nullable
          : recordRef as String?,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      variantOf: variantOf == const $CopyWithPlaceholder()
          ? _value.variantOf
          // ignore: cast_nullable_to_non_nullable
          : variantOf as String?,
      variantWidth: variantWidth == const $CopyWithPlaceholder()
          ? _value.variantWidth
          // ignore: cast_nullable_to_non_nullable
          : variantWidth as int?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as MediaLinks?,
    );
  }
}

extension $MediaCopyWith on Media {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMedia.copyWith(...)` or `instanceOfMedia.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaCWProxy get copyWith => _$MediaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Media _$MediaFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Media', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'sha256',
      'contentType',
      'width',
      'height',
      'bytes',
      'role',
      'createdAt',
    ],
  );
  final val = Media(
    id: $checkedConvert('id', (v) => v as String),
    sha256: $checkedConvert('sha256', (v) => v as String),
    contentType: $checkedConvert(
      'contentType',
      (v) => $enumDecode(_$MediaContentTypeEnumEnumMap, v),
    ),
    width: $checkedConvert('width', (v) => (v as num).toInt()),
    height: $checkedConvert('height', (v) => (v as num).toInt()),
    bytes: $checkedConvert('bytes', (v) => (v as num).toInt()),
    role: $checkedConvert(
      'role',
      (v) => $enumDecode(_$MediaRoleEnumEnumMap, v),
    ),
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
    recordSourceId: $checkedConvert('recordSourceId', (v) => v as String?),
    recordExternalId: $checkedConvert('recordExternalId', (v) => v as String?),
    recordRef: $checkedConvert('recordRef', (v) => v as String?),
    licence: $checkedConvert('licence', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    variantOf: $checkedConvert('variantOf', (v) => v as String?),
    variantWidth: $checkedConvert('variantWidth', (v) => (v as num?)?.toInt()),
    links: $checkedConvert(
      'links',
      (v) => v == null ? null : MediaLinks.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$MediaToJson(Media instance) => <String, dynamic>{
  'id': instance.id,
  'sha256': instance.sha256,
  'contentType': _$MediaContentTypeEnumEnumMap[instance.contentType]!,
  'width': instance.width,
  'height': instance.height,
  'bytes': instance.bytes,
  'role': _$MediaRoleEnumEnumMap[instance.role]!,
  'sourceId': ?instance.sourceId,
  'recordSourceId': ?instance.recordSourceId,
  'recordExternalId': ?instance.recordExternalId,
  'recordRef': ?instance.recordRef,
  'licence': ?instance.licence,
  'createdAt': instance.createdAt.toIso8601String(),
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'variantOf': ?instance.variantOf,
  'variantWidth': ?instance.variantWidth,
  'links': ?instance.links?.toJson(),
};

const _$MediaContentTypeEnumEnumMap = {
  MediaContentTypeEnum.imageSlashPng: 'image/png',
  MediaContentTypeEnum.imageSlashJpeg: 'image/jpeg',
  MediaContentTypeEnum.imageSlashTiff: 'image/tiff',
  MediaContentTypeEnum.applicationSlashCzmlPlusJson: 'application/czml+json',
  MediaContentTypeEnum.audioSlashMpeg: 'audio/mpeg',
  MediaContentTypeEnum.audioSlashOgg: 'audio/ogg',
};

const _$MediaRoleEnumEnumMap = {
  MediaRoleEnum.image: 'image',
  MediaRoleEnum.thumbnail: 'thumbnail',
  MediaRoleEnum.projected: 'projected',
  MediaRoleEnum.map: 'map',
  MediaRoleEnum.document: 'document',
  MediaRoleEnum.audio: 'audio',
};
