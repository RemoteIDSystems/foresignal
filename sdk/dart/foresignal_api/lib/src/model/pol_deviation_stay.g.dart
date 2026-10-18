// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_deviation_stay.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolDeviationStayCWProxy {
  PolDeviationStay arrive(DateTime? arrive);

  PolDeviationStay depart(DateTime? depart);

  PolDeviationStay centroid(Geometry? centroid);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolDeviationStay(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolDeviationStay(...).copyWith(id: 12, name: "My name")
  /// ```
  PolDeviationStay call({
    DateTime? arrive,
    DateTime? depart,
    Geometry? centroid,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolDeviationStay.copyWith(...)` or call `instanceOfPolDeviationStay.copyWith.fieldName(value)` for a single field.
class _$PolDeviationStayCWProxyImpl implements _$PolDeviationStayCWProxy {
  const _$PolDeviationStayCWProxyImpl(this._value);

  final PolDeviationStay _value;

  @override
  PolDeviationStay arrive(DateTime? arrive) => call(arrive: arrive);

  @override
  PolDeviationStay depart(DateTime? depart) => call(depart: depart);

  @override
  PolDeviationStay centroid(Geometry? centroid) => call(centroid: centroid);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolDeviationStay(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolDeviationStay(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolDeviationStay call({
    Object? arrive = const $CopyWithPlaceholder(),
    Object? depart = const $CopyWithPlaceholder(),
    Object? centroid = const $CopyWithPlaceholder(),
  }) {
    return PolDeviationStay(
      arrive: arrive == const $CopyWithPlaceholder()
          ? _value.arrive
          // ignore: cast_nullable_to_non_nullable
          : arrive as DateTime?,
      depart: depart == const $CopyWithPlaceholder()
          ? _value.depart
          // ignore: cast_nullable_to_non_nullable
          : depart as DateTime?,
      centroid: centroid == const $CopyWithPlaceholder()
          ? _value.centroid
          // ignore: cast_nullable_to_non_nullable
          : centroid as Geometry?,
    );
  }
}

extension $PolDeviationStayCopyWith on PolDeviationStay {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolDeviationStay.copyWith(...)` or `instanceOfPolDeviationStay.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolDeviationStayCWProxy get copyWith => _$PolDeviationStayCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolDeviationStay _$PolDeviationStayFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PolDeviationStay', json, ($checkedConvert) {
      final val = PolDeviationStay(
        arrive: $checkedConvert(
          'arrive',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        depart: $checkedConvert(
          'depart',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        centroid: $checkedConvert(
          'centroid',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PolDeviationStayToJson(PolDeviationStay instance) =>
    <String, dynamic>{
      'arrive': ?instance.arrive?.toIso8601String(),
      'depart': ?instance.depart?.toIso8601String(),
      'centroid': ?instance.centroid?.toJson(),
    };
