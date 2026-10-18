// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_value_derived.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleValueDerivedCWProxy {
  SampleValueDerived speedMs(num? speedMs);

  SampleValueDerived fromDeg(int? fromDeg);

  SampleValueDerived compass(String? compass);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleValueDerived(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleValueDerived(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleValueDerived call({num? speedMs, int? fromDeg, String? compass});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleValueDerived.copyWith(...)` or call `instanceOfSampleValueDerived.copyWith.fieldName(value)` for a single field.
class _$SampleValueDerivedCWProxyImpl implements _$SampleValueDerivedCWProxy {
  const _$SampleValueDerivedCWProxyImpl(this._value);

  final SampleValueDerived _value;

  @override
  SampleValueDerived speedMs(num? speedMs) => call(speedMs: speedMs);

  @override
  SampleValueDerived fromDeg(int? fromDeg) => call(fromDeg: fromDeg);

  @override
  SampleValueDerived compass(String? compass) => call(compass: compass);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleValueDerived(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleValueDerived(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleValueDerived call({
    Object? speedMs = const $CopyWithPlaceholder(),
    Object? fromDeg = const $CopyWithPlaceholder(),
    Object? compass = const $CopyWithPlaceholder(),
  }) {
    return SampleValueDerived(
      speedMs: speedMs == const $CopyWithPlaceholder()
          ? _value.speedMs
          // ignore: cast_nullable_to_non_nullable
          : speedMs as num?,
      fromDeg: fromDeg == const $CopyWithPlaceholder()
          ? _value.fromDeg
          // ignore: cast_nullable_to_non_nullable
          : fromDeg as int?,
      compass: compass == const $CopyWithPlaceholder()
          ? _value.compass
          // ignore: cast_nullable_to_non_nullable
          : compass as String?,
    );
  }
}

extension $SampleValueDerivedCopyWith on SampleValueDerived {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleValueDerived.copyWith(...)` or `instanceOfSampleValueDerived.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleValueDerivedCWProxy get copyWith =>
      _$SampleValueDerivedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleValueDerived _$SampleValueDerivedFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SampleValueDerived', json, ($checkedConvert) {
      final val = SampleValueDerived(
        speedMs: $checkedConvert('speedMs', (v) => v as num?),
        fromDeg: $checkedConvert('fromDeg', (v) => (v as num?)?.toInt()),
        compass: $checkedConvert('compass', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SampleValueDerivedToJson(SampleValueDerived instance) =>
    <String, dynamic>{
      'speedMs': ?instance.speedMs,
      'fromDeg': ?instance.fromDeg,
      'compass': ?instance.compass,
    };
