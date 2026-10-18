// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_alerts_by_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestAlertsByStatusCWProxy {
  DigestAlertsByStatus firing(int? firing);

  DigestAlertsByStatus acknowledged(int? acknowledged);

  DigestAlertsByStatus resolved(int? resolved);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAlertsByStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAlertsByStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestAlertsByStatus call({int? firing, int? acknowledged, int? resolved});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestAlertsByStatus.copyWith(...)` or call `instanceOfDigestAlertsByStatus.copyWith.fieldName(value)` for a single field.
class _$DigestAlertsByStatusCWProxyImpl
    implements _$DigestAlertsByStatusCWProxy {
  const _$DigestAlertsByStatusCWProxyImpl(this._value);

  final DigestAlertsByStatus _value;

  @override
  DigestAlertsByStatus firing(int? firing) => call(firing: firing);

  @override
  DigestAlertsByStatus acknowledged(int? acknowledged) =>
      call(acknowledged: acknowledged);

  @override
  DigestAlertsByStatus resolved(int? resolved) => call(resolved: resolved);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAlertsByStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAlertsByStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestAlertsByStatus call({
    Object? firing = const $CopyWithPlaceholder(),
    Object? acknowledged = const $CopyWithPlaceholder(),
    Object? resolved = const $CopyWithPlaceholder(),
  }) {
    return DigestAlertsByStatus(
      firing: firing == const $CopyWithPlaceholder()
          ? _value.firing
          // ignore: cast_nullable_to_non_nullable
          : firing as int?,
      acknowledged: acknowledged == const $CopyWithPlaceholder()
          ? _value.acknowledged
          // ignore: cast_nullable_to_non_nullable
          : acknowledged as int?,
      resolved: resolved == const $CopyWithPlaceholder()
          ? _value.resolved
          // ignore: cast_nullable_to_non_nullable
          : resolved as int?,
    );
  }
}

extension $DigestAlertsByStatusCopyWith on DigestAlertsByStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestAlertsByStatus.copyWith(...)` or `instanceOfDigestAlertsByStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestAlertsByStatusCWProxy get copyWith =>
      _$DigestAlertsByStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestAlertsByStatus _$DigestAlertsByStatusFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DigestAlertsByStatus', json, ($checkedConvert) {
  final val = DigestAlertsByStatus(
    firing: $checkedConvert('firing', (v) => (v as num?)?.toInt()),
    acknowledged: $checkedConvert('acknowledged', (v) => (v as num?)?.toInt()),
    resolved: $checkedConvert('resolved', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$DigestAlertsByStatusToJson(
  DigestAlertsByStatus instance,
) => <String, dynamic>{
  'firing': ?instance.firing,
  'acknowledged': ?instance.acknowledged,
  'resolved': ?instance.resolved,
};
