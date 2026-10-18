// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_context_near.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertContextNearCWProxy {
  AlertContextNear placeIds(List<String>? placeIds);

  AlertContextNear placeVersions(List<int>? placeVersions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertContextNear(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertContextNear(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertContextNear call({List<String>? placeIds, List<int>? placeVersions});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertContextNear.copyWith(...)` or call `instanceOfAlertContextNear.copyWith.fieldName(value)` for a single field.
class _$AlertContextNearCWProxyImpl implements _$AlertContextNearCWProxy {
  const _$AlertContextNearCWProxyImpl(this._value);

  final AlertContextNear _value;

  @override
  AlertContextNear placeIds(List<String>? placeIds) => call(placeIds: placeIds);

  @override
  AlertContextNear placeVersions(List<int>? placeVersions) =>
      call(placeVersions: placeVersions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertContextNear(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertContextNear(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertContextNear call({
    Object? placeIds = const $CopyWithPlaceholder(),
    Object? placeVersions = const $CopyWithPlaceholder(),
  }) {
    return AlertContextNear(
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
      placeVersions: placeVersions == const $CopyWithPlaceholder()
          ? _value.placeVersions
          // ignore: cast_nullable_to_non_nullable
          : placeVersions as List<int>?,
    );
  }
}

extension $AlertContextNearCopyWith on AlertContextNear {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertContextNear.copyWith(...)` or `instanceOfAlertContextNear.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertContextNearCWProxy get copyWith => _$AlertContextNearCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertContextNear _$AlertContextNearFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AlertContextNear', json, ($checkedConvert) {
      final val = AlertContextNear(
        placeIds: $checkedConvert(
          'placeIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        placeVersions: $checkedConvert(
          'placeVersions',
          (v) => (v as List<dynamic>?)?.map((e) => (e as num).toInt()).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AlertContextNearToJson(AlertContextNear instance) =>
    <String, dynamic>{
      'placeIds': ?instance.placeIds,
      'placeVersions': ?instance.placeVersions,
    };
