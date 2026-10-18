// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'eta_result_target.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EtaResultTargetCWProxy {
  EtaResultTarget placeId(String? placeId);

  EtaResultTarget point(Geometry point);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EtaResultTarget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EtaResultTarget(...).copyWith(id: 12, name: "My name")
  /// ```
  EtaResultTarget call({String? placeId, Geometry point});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEtaResultTarget.copyWith(...)` or call `instanceOfEtaResultTarget.copyWith.fieldName(value)` for a single field.
class _$EtaResultTargetCWProxyImpl implements _$EtaResultTargetCWProxy {
  const _$EtaResultTargetCWProxyImpl(this._value);

  final EtaResultTarget _value;

  @override
  EtaResultTarget placeId(String? placeId) => call(placeId: placeId);

  @override
  EtaResultTarget point(Geometry point) => call(point: point);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EtaResultTarget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EtaResultTarget(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EtaResultTarget call({
    Object? placeId = const $CopyWithPlaceholder(),
    Object? point = const $CopyWithPlaceholder(),
  }) {
    return EtaResultTarget(
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      point: point == const $CopyWithPlaceholder() || point == null
          ? _value.point
          // ignore: cast_nullable_to_non_nullable
          : point as Geometry,
    );
  }
}

extension $EtaResultTargetCopyWith on EtaResultTarget {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEtaResultTarget.copyWith(...)` or `instanceOfEtaResultTarget.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EtaResultTargetCWProxy get copyWith => _$EtaResultTargetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EtaResultTarget _$EtaResultTargetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EtaResultTarget', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['point']);
      final val = EtaResultTarget(
        placeId: $checkedConvert('placeId', (v) => v as String?),
        point: $checkedConvert(
          'point',
          (v) => Geometry.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EtaResultTargetToJson(EtaResultTarget instance) =>
    <String, dynamic>{
      'placeId': ?instance.placeId,
      'point': instance.point.toJson(),
    };
