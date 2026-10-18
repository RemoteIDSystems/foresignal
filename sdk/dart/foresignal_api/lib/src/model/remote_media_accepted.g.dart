// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'remote_media_accepted.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RemoteMediaAcceptedCWProxy {
  RemoteMediaAccepted mediaId(String mediaId);

  RemoteMediaAccepted sha256(String sha256);

  RemoteMediaAccepted passId(String? passId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RemoteMediaAccepted(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RemoteMediaAccepted(...).copyWith(id: 12, name: "My name")
  /// ```
  RemoteMediaAccepted call({String mediaId, String sha256, String? passId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRemoteMediaAccepted.copyWith(...)` or call `instanceOfRemoteMediaAccepted.copyWith.fieldName(value)` for a single field.
class _$RemoteMediaAcceptedCWProxyImpl implements _$RemoteMediaAcceptedCWProxy {
  const _$RemoteMediaAcceptedCWProxyImpl(this._value);

  final RemoteMediaAccepted _value;

  @override
  RemoteMediaAccepted mediaId(String mediaId) => call(mediaId: mediaId);

  @override
  RemoteMediaAccepted sha256(String sha256) => call(sha256: sha256);

  @override
  RemoteMediaAccepted passId(String? passId) => call(passId: passId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RemoteMediaAccepted(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RemoteMediaAccepted(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RemoteMediaAccepted call({
    Object? mediaId = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? passId = const $CopyWithPlaceholder(),
  }) {
    return RemoteMediaAccepted(
      mediaId: mediaId == const $CopyWithPlaceholder() || mediaId == null
          ? _value.mediaId
          // ignore: cast_nullable_to_non_nullable
          : mediaId as String,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      passId: passId == const $CopyWithPlaceholder()
          ? _value.passId
          // ignore: cast_nullable_to_non_nullable
          : passId as String?,
    );
  }
}

extension $RemoteMediaAcceptedCopyWith on RemoteMediaAccepted {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRemoteMediaAccepted.copyWith(...)` or `instanceOfRemoteMediaAccepted.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RemoteMediaAcceptedCWProxy get copyWith =>
      _$RemoteMediaAcceptedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RemoteMediaAccepted _$RemoteMediaAcceptedFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RemoteMediaAccepted', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['mediaId', 'sha256']);
      final val = RemoteMediaAccepted(
        mediaId: $checkedConvert('mediaId', (v) => v as String),
        sha256: $checkedConvert('sha256', (v) => v as String),
        passId: $checkedConvert('passId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$RemoteMediaAcceptedToJson(
  RemoteMediaAccepted instance,
) => <String, dynamic>{
  'mediaId': instance.mediaId,
  'sha256': instance.sha256,
  'passId': ?instance.passId,
};
