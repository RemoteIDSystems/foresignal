// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_bundle_attachments_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBundleAttachmentsInnerCWProxy {
  ReportBundleAttachmentsInner sha256(String sha256);

  ReportBundleAttachmentsInner filename(String? filename);

  ReportBundleAttachmentsInner contentType(String? contentType);

  ReportBundleAttachmentsInner size(int? size);

  ReportBundleAttachmentsInner attachmentId(String? attachmentId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBundleAttachmentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBundleAttachmentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBundleAttachmentsInner call({
    String sha256,
    String? filename,
    String? contentType,
    int? size,
    String? attachmentId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBundleAttachmentsInner.copyWith(...)` or call `instanceOfReportBundleAttachmentsInner.copyWith.fieldName(value)` for a single field.
class _$ReportBundleAttachmentsInnerCWProxyImpl
    implements _$ReportBundleAttachmentsInnerCWProxy {
  const _$ReportBundleAttachmentsInnerCWProxyImpl(this._value);

  final ReportBundleAttachmentsInner _value;

  @override
  ReportBundleAttachmentsInner sha256(String sha256) => call(sha256: sha256);

  @override
  ReportBundleAttachmentsInner filename(String? filename) =>
      call(filename: filename);

  @override
  ReportBundleAttachmentsInner contentType(String? contentType) =>
      call(contentType: contentType);

  @override
  ReportBundleAttachmentsInner size(int? size) => call(size: size);

  @override
  ReportBundleAttachmentsInner attachmentId(String? attachmentId) =>
      call(attachmentId: attachmentId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBundleAttachmentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBundleAttachmentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBundleAttachmentsInner call({
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? filename = const $CopyWithPlaceholder(),
    Object? contentType = const $CopyWithPlaceholder(),
    Object? size = const $CopyWithPlaceholder(),
    Object? attachmentId = const $CopyWithPlaceholder(),
  }) {
    return ReportBundleAttachmentsInner(
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      filename: filename == const $CopyWithPlaceholder()
          ? _value.filename
          // ignore: cast_nullable_to_non_nullable
          : filename as String?,
      contentType: contentType == const $CopyWithPlaceholder()
          ? _value.contentType
          // ignore: cast_nullable_to_non_nullable
          : contentType as String?,
      size: size == const $CopyWithPlaceholder()
          ? _value.size
          // ignore: cast_nullable_to_non_nullable
          : size as int?,
      attachmentId: attachmentId == const $CopyWithPlaceholder()
          ? _value.attachmentId
          // ignore: cast_nullable_to_non_nullable
          : attachmentId as String?,
    );
  }
}

extension $ReportBundleAttachmentsInnerCopyWith
    on ReportBundleAttachmentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBundleAttachmentsInner.copyWith(...)` or `instanceOfReportBundleAttachmentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBundleAttachmentsInnerCWProxy get copyWith =>
      _$ReportBundleAttachmentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBundleAttachmentsInner _$ReportBundleAttachmentsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportBundleAttachmentsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['sha256']);
  final val = ReportBundleAttachmentsInner(
    sha256: $checkedConvert('sha256', (v) => v as String),
    filename: $checkedConvert('filename', (v) => v as String?),
    contentType: $checkedConvert('contentType', (v) => v as String?),
    size: $checkedConvert('size', (v) => (v as num?)?.toInt()),
    attachmentId: $checkedConvert('attachmentId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ReportBundleAttachmentsInnerToJson(
  ReportBundleAttachmentsInner instance,
) => <String, dynamic>{
  'sha256': instance.sha256,
  'filename': ?instance.filename,
  'contentType': ?instance.contentType,
  'size': ?instance.size,
  'attachmentId': ?instance.attachmentId,
};
