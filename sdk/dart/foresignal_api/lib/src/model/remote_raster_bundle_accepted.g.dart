// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'remote_raster_bundle_accepted.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RemoteRasterBundleAcceptedCWProxy {
  RemoteRasterBundleAccepted uploadId(String uploadId);

  RemoteRasterBundleAccepted passFolder(String passFolder);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RemoteRasterBundleAccepted(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RemoteRasterBundleAccepted(...).copyWith(id: 12, name: "My name")
  /// ```
  RemoteRasterBundleAccepted call({String uploadId, String passFolder});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRemoteRasterBundleAccepted.copyWith(...)` or call `instanceOfRemoteRasterBundleAccepted.copyWith.fieldName(value)` for a single field.
class _$RemoteRasterBundleAcceptedCWProxyImpl
    implements _$RemoteRasterBundleAcceptedCWProxy {
  const _$RemoteRasterBundleAcceptedCWProxyImpl(this._value);

  final RemoteRasterBundleAccepted _value;

  @override
  RemoteRasterBundleAccepted uploadId(String uploadId) =>
      call(uploadId: uploadId);

  @override
  RemoteRasterBundleAccepted passFolder(String passFolder) =>
      call(passFolder: passFolder);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RemoteRasterBundleAccepted(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RemoteRasterBundleAccepted(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RemoteRasterBundleAccepted call({
    Object? uploadId = const $CopyWithPlaceholder(),
    Object? passFolder = const $CopyWithPlaceholder(),
  }) {
    return RemoteRasterBundleAccepted(
      uploadId: uploadId == const $CopyWithPlaceholder() || uploadId == null
          ? _value.uploadId
          // ignore: cast_nullable_to_non_nullable
          : uploadId as String,
      passFolder:
          passFolder == const $CopyWithPlaceholder() || passFolder == null
          ? _value.passFolder
          // ignore: cast_nullable_to_non_nullable
          : passFolder as String,
    );
  }
}

extension $RemoteRasterBundleAcceptedCopyWith on RemoteRasterBundleAccepted {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRemoteRasterBundleAccepted.copyWith(...)` or `instanceOfRemoteRasterBundleAccepted.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RemoteRasterBundleAcceptedCWProxy get copyWith =>
      _$RemoteRasterBundleAcceptedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RemoteRasterBundleAccepted _$RemoteRasterBundleAcceptedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RemoteRasterBundleAccepted', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['uploadId', 'passFolder']);
  final val = RemoteRasterBundleAccepted(
    uploadId: $checkedConvert('uploadId', (v) => v as String),
    passFolder: $checkedConvert('passFolder', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$RemoteRasterBundleAcceptedToJson(
  RemoteRasterBundleAccepted instance,
) => <String, dynamic>{
  'uploadId': instance.uploadId,
  'passFolder': instance.passFolder,
};
