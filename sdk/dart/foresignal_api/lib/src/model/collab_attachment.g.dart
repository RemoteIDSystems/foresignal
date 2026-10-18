// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collab_attachment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollabAttachmentCWProxy {
  CollabAttachment id(String id);

  CollabAttachment mediaId(String mediaId);

  CollabAttachment filename(String filename);

  CollabAttachment contentType(String contentType);

  CollabAttachment size(int size);

  CollabAttachment sha256(String sha256);

  CollabAttachment text(String? text);

  CollabAttachment textTruncated(bool? textTruncated);

  CollabAttachment imageMeta(CollabAttachmentImageMeta? imageMeta);

  CollabAttachment uploadedBy(PrincipalSummary uploadedBy);

  CollabAttachment owner(PrincipalSummary? owner);

  CollabAttachment visibility(Visibility? visibility);

  CollabAttachment tlp(Tlp tlp);

  CollabAttachment createdAt(DateTime createdAt);

  CollabAttachment links(CollabAttachmentLinks? links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollabAttachment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollabAttachment(...).copyWith(id: 12, name: "My name")
  /// ```
  CollabAttachment call({
    String id,
    String mediaId,
    String filename,
    String contentType,
    int size,
    String sha256,
    String? text,
    bool? textTruncated,
    CollabAttachmentImageMeta? imageMeta,
    PrincipalSummary uploadedBy,
    PrincipalSummary? owner,
    Visibility? visibility,
    Tlp tlp,
    DateTime createdAt,
    CollabAttachmentLinks? links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollabAttachment.copyWith(...)` or call `instanceOfCollabAttachment.copyWith.fieldName(value)` for a single field.
class _$CollabAttachmentCWProxyImpl implements _$CollabAttachmentCWProxy {
  const _$CollabAttachmentCWProxyImpl(this._value);

  final CollabAttachment _value;

  @override
  CollabAttachment id(String id) => call(id: id);

  @override
  CollabAttachment mediaId(String mediaId) => call(mediaId: mediaId);

  @override
  CollabAttachment filename(String filename) => call(filename: filename);

  @override
  CollabAttachment contentType(String contentType) =>
      call(contentType: contentType);

  @override
  CollabAttachment size(int size) => call(size: size);

  @override
  CollabAttachment sha256(String sha256) => call(sha256: sha256);

  @override
  CollabAttachment text(String? text) => call(text: text);

  @override
  CollabAttachment textTruncated(bool? textTruncated) =>
      call(textTruncated: textTruncated);

  @override
  CollabAttachment imageMeta(CollabAttachmentImageMeta? imageMeta) =>
      call(imageMeta: imageMeta);

  @override
  CollabAttachment uploadedBy(PrincipalSummary uploadedBy) =>
      call(uploadedBy: uploadedBy);

  @override
  CollabAttachment owner(PrincipalSummary? owner) => call(owner: owner);

  @override
  CollabAttachment visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  CollabAttachment tlp(Tlp tlp) => call(tlp: tlp);

  @override
  CollabAttachment createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  CollabAttachment links(CollabAttachmentLinks? links) => call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollabAttachment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollabAttachment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollabAttachment call({
    Object? id = const $CopyWithPlaceholder(),
    Object? mediaId = const $CopyWithPlaceholder(),
    Object? filename = const $CopyWithPlaceholder(),
    Object? contentType = const $CopyWithPlaceholder(),
    Object? size = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? textTruncated = const $CopyWithPlaceholder(),
    Object? imageMeta = const $CopyWithPlaceholder(),
    Object? uploadedBy = const $CopyWithPlaceholder(),
    Object? owner = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return CollabAttachment(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      mediaId: mediaId == const $CopyWithPlaceholder() || mediaId == null
          ? _value.mediaId
          // ignore: cast_nullable_to_non_nullable
          : mediaId as String,
      filename: filename == const $CopyWithPlaceholder() || filename == null
          ? _value.filename
          // ignore: cast_nullable_to_non_nullable
          : filename as String,
      contentType:
          contentType == const $CopyWithPlaceholder() || contentType == null
          ? _value.contentType
          // ignore: cast_nullable_to_non_nullable
          : contentType as String,
      size: size == const $CopyWithPlaceholder() || size == null
          ? _value.size
          // ignore: cast_nullable_to_non_nullable
          : size as int,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      text: text == const $CopyWithPlaceholder()
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String?,
      textTruncated: textTruncated == const $CopyWithPlaceholder()
          ? _value.textTruncated
          // ignore: cast_nullable_to_non_nullable
          : textTruncated as bool?,
      imageMeta: imageMeta == const $CopyWithPlaceholder()
          ? _value.imageMeta
          // ignore: cast_nullable_to_non_nullable
          : imageMeta as CollabAttachmentImageMeta?,
      uploadedBy:
          uploadedBy == const $CopyWithPlaceholder() || uploadedBy == null
          ? _value.uploadedBy
          // ignore: cast_nullable_to_non_nullable
          : uploadedBy as PrincipalSummary,
      owner: owner == const $CopyWithPlaceholder()
          ? _value.owner
          // ignore: cast_nullable_to_non_nullable
          : owner as PrincipalSummary?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as CollabAttachmentLinks?,
    );
  }
}

extension $CollabAttachmentCopyWith on CollabAttachment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollabAttachment.copyWith(...)` or `instanceOfCollabAttachment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollabAttachmentCWProxy get copyWith => _$CollabAttachmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollabAttachment _$CollabAttachmentFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CollabAttachment', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'mediaId',
          'filename',
          'contentType',
          'size',
          'sha256',
          'uploadedBy',
          'tlp',
          'createdAt',
        ],
      );
      final val = CollabAttachment(
        id: $checkedConvert('id', (v) => v as String),
        mediaId: $checkedConvert('mediaId', (v) => v as String),
        filename: $checkedConvert('filename', (v) => v as String),
        contentType: $checkedConvert('contentType', (v) => v as String),
        size: $checkedConvert('size', (v) => (v as num).toInt()),
        sha256: $checkedConvert('sha256', (v) => v as String),
        text: $checkedConvert('text', (v) => v as String?),
        textTruncated: $checkedConvert('textTruncated', (v) => v as bool?),
        imageMeta: $checkedConvert(
          'imageMeta',
          (v) => v == null
              ? null
              : CollabAttachmentImageMeta.fromJson(v as Map<String, dynamic>),
        ),
        uploadedBy: $checkedConvert(
          'uploadedBy',
          (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        owner: $checkedConvert(
          'owner',
          (v) => v == null
              ? null
              : PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        visibility: $checkedConvert(
          'visibility',
          (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
        ),
        tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
        links: $checkedConvert(
          'links',
          (v) => v == null
              ? null
              : CollabAttachmentLinks.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CollabAttachmentToJson(CollabAttachment instance) =>
    <String, dynamic>{
      'id': instance.id,
      'mediaId': instance.mediaId,
      'filename': instance.filename,
      'contentType': instance.contentType,
      'size': instance.size,
      'sha256': instance.sha256,
      'text': ?instance.text,
      'textTruncated': ?instance.textTruncated,
      'imageMeta': ?instance.imageMeta?.toJson(),
      'uploadedBy': instance.uploadedBy.toJson(),
      'owner': ?instance.owner?.toJson(),
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'tlp': _$TlpEnumMap[instance.tlp]!,
      'createdAt': instance.createdAt.toIso8601String(),
      'links': ?instance.links?.toJson(),
    };

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
