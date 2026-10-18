// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'remote_raster_bundle_meta.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RemoteRasterBundleMetaCWProxy {
  RemoteRasterBundleMeta passFolder(String passFolder);

  RemoteRasterBundleMeta files(List<String> files);

  RemoteRasterBundleMeta product(String? product);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RemoteRasterBundleMeta(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RemoteRasterBundleMeta(...).copyWith(id: 12, name: "My name")
  /// ```
  RemoteRasterBundleMeta call({
    String passFolder,
    List<String> files,
    String? product,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRemoteRasterBundleMeta.copyWith(...)` or call `instanceOfRemoteRasterBundleMeta.copyWith.fieldName(value)` for a single field.
class _$RemoteRasterBundleMetaCWProxyImpl
    implements _$RemoteRasterBundleMetaCWProxy {
  const _$RemoteRasterBundleMetaCWProxyImpl(this._value);

  final RemoteRasterBundleMeta _value;

  @override
  RemoteRasterBundleMeta passFolder(String passFolder) =>
      call(passFolder: passFolder);

  @override
  RemoteRasterBundleMeta files(List<String> files) => call(files: files);

  @override
  RemoteRasterBundleMeta product(String? product) => call(product: product);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RemoteRasterBundleMeta(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RemoteRasterBundleMeta(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RemoteRasterBundleMeta call({
    Object? passFolder = const $CopyWithPlaceholder(),
    Object? files = const $CopyWithPlaceholder(),
    Object? product = const $CopyWithPlaceholder(),
  }) {
    return RemoteRasterBundleMeta(
      passFolder:
          passFolder == const $CopyWithPlaceholder() || passFolder == null
          ? _value.passFolder
          // ignore: cast_nullable_to_non_nullable
          : passFolder as String,
      files: files == const $CopyWithPlaceholder() || files == null
          ? _value.files
          // ignore: cast_nullable_to_non_nullable
          : files as List<String>,
      product: product == const $CopyWithPlaceholder()
          ? _value.product
          // ignore: cast_nullable_to_non_nullable
          : product as String?,
    );
  }
}

extension $RemoteRasterBundleMetaCopyWith on RemoteRasterBundleMeta {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRemoteRasterBundleMeta.copyWith(...)` or `instanceOfRemoteRasterBundleMeta.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RemoteRasterBundleMetaCWProxy get copyWith =>
      _$RemoteRasterBundleMetaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RemoteRasterBundleMeta _$RemoteRasterBundleMetaFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RemoteRasterBundleMeta', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['passFolder', 'files']);
  final val = RemoteRasterBundleMeta(
    passFolder: $checkedConvert('passFolder', (v) => v as String),
    files: $checkedConvert(
      'files',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    product: $checkedConvert('product', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$RemoteRasterBundleMetaToJson(
  RemoteRasterBundleMeta instance,
) => <String, dynamic>{
  'passFolder': instance.passFolder,
  'files': instance.files,
  'product': ?instance.product,
};
