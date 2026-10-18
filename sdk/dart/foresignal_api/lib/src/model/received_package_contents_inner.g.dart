// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'received_package_contents_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReceivedPackageContentsInnerCWProxy {
  ReceivedPackageContentsInner path(String path);

  ReceivedPackageContentsInner sha256(String sha256);

  ReceivedPackageContentsInner size(int? size);

  ReceivedPackageContentsInner mediaId(String? mediaId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReceivedPackageContentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReceivedPackageContentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ReceivedPackageContentsInner call({
    String path,
    String sha256,
    int? size,
    String? mediaId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReceivedPackageContentsInner.copyWith(...)` or call `instanceOfReceivedPackageContentsInner.copyWith.fieldName(value)` for a single field.
class _$ReceivedPackageContentsInnerCWProxyImpl
    implements _$ReceivedPackageContentsInnerCWProxy {
  const _$ReceivedPackageContentsInnerCWProxyImpl(this._value);

  final ReceivedPackageContentsInner _value;

  @override
  ReceivedPackageContentsInner path(String path) => call(path: path);

  @override
  ReceivedPackageContentsInner sha256(String sha256) => call(sha256: sha256);

  @override
  ReceivedPackageContentsInner size(int? size) => call(size: size);

  @override
  ReceivedPackageContentsInner mediaId(String? mediaId) =>
      call(mediaId: mediaId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReceivedPackageContentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReceivedPackageContentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReceivedPackageContentsInner call({
    Object? path = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? size = const $CopyWithPlaceholder(),
    Object? mediaId = const $CopyWithPlaceholder(),
  }) {
    return ReceivedPackageContentsInner(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      size: size == const $CopyWithPlaceholder()
          ? _value.size
          // ignore: cast_nullable_to_non_nullable
          : size as int?,
      mediaId: mediaId == const $CopyWithPlaceholder()
          ? _value.mediaId
          // ignore: cast_nullable_to_non_nullable
          : mediaId as String?,
    );
  }
}

extension $ReceivedPackageContentsInnerCopyWith
    on ReceivedPackageContentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReceivedPackageContentsInner.copyWith(...)` or `instanceOfReceivedPackageContentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReceivedPackageContentsInnerCWProxy get copyWith =>
      _$ReceivedPackageContentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReceivedPackageContentsInner _$ReceivedPackageContentsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReceivedPackageContentsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['path', 'sha256']);
  final val = ReceivedPackageContentsInner(
    path: $checkedConvert('path', (v) => v as String),
    sha256: $checkedConvert('sha256', (v) => v as String),
    size: $checkedConvert('size', (v) => (v as num?)?.toInt()),
    mediaId: $checkedConvert('mediaId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ReceivedPackageContentsInnerToJson(
  ReceivedPackageContentsInner instance,
) => <String, dynamic>{
  'path': instance.path,
  'sha256': instance.sha256,
  'size': ?instance.size,
  'mediaId': ?instance.mediaId,
};
