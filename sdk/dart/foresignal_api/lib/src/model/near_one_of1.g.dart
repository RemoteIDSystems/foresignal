// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'near_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NearOneOf1CWProxy {
  NearOneOf1 point(List<num> point);

  NearOneOf1 radiusKm(num radiusKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NearOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NearOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  NearOneOf1 call({List<num> point, num radiusKm});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNearOneOf1.copyWith(...)` or call `instanceOfNearOneOf1.copyWith.fieldName(value)` for a single field.
class _$NearOneOf1CWProxyImpl implements _$NearOneOf1CWProxy {
  const _$NearOneOf1CWProxyImpl(this._value);

  final NearOneOf1 _value;

  @override
  NearOneOf1 point(List<num> point) => call(point: point);

  @override
  NearOneOf1 radiusKm(num radiusKm) => call(radiusKm: radiusKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NearOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NearOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NearOneOf1 call({
    Object? point = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
  }) {
    return NearOneOf1(
      point: point == const $CopyWithPlaceholder() || point == null
          ? _value.point
          // ignore: cast_nullable_to_non_nullable
          : point as List<num>,
      radiusKm: radiusKm == const $CopyWithPlaceholder() || radiusKm == null
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num,
    );
  }
}

extension $NearOneOf1CopyWith on NearOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNearOneOf1.copyWith(...)` or `instanceOfNearOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NearOneOf1CWProxy get copyWith => _$NearOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NearOneOf1 _$NearOneOf1FromJson(Map<String, dynamic> json) =>
    $checkedCreate('NearOneOf1', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['point', 'radiusKm']);
      final val = NearOneOf1(
        point: $checkedConvert(
          'point',
          (v) => (v as List<dynamic>).map((e) => e as num).toList(),
        ),
        radiusKm: $checkedConvert('radiusKm', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$NearOneOf1ToJson(NearOneOf1 instance) =>
    <String, dynamic>{'point': instance.point, 'radiusKm': instance.radiusKm};
