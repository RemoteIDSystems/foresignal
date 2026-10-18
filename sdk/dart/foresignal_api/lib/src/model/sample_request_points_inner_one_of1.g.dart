// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_request_points_inner_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleRequestPointsInnerOneOf1CWProxy {
  SampleRequestPointsInnerOneOf1 place(String place);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleRequestPointsInnerOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleRequestPointsInnerOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleRequestPointsInnerOneOf1 call({String place});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleRequestPointsInnerOneOf1.copyWith(...)` or call `instanceOfSampleRequestPointsInnerOneOf1.copyWith.fieldName(value)` for a single field.
class _$SampleRequestPointsInnerOneOf1CWProxyImpl
    implements _$SampleRequestPointsInnerOneOf1CWProxy {
  const _$SampleRequestPointsInnerOneOf1CWProxyImpl(this._value);

  final SampleRequestPointsInnerOneOf1 _value;

  @override
  SampleRequestPointsInnerOneOf1 place(String place) => call(place: place);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleRequestPointsInnerOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleRequestPointsInnerOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleRequestPointsInnerOneOf1 call({
    Object? place = const $CopyWithPlaceholder(),
  }) {
    return SampleRequestPointsInnerOneOf1(
      place: place == const $CopyWithPlaceholder() || place == null
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as String,
    );
  }
}

extension $SampleRequestPointsInnerOneOf1CopyWith
    on SampleRequestPointsInnerOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleRequestPointsInnerOneOf1.copyWith(...)` or `instanceOfSampleRequestPointsInnerOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleRequestPointsInnerOneOf1CWProxy get copyWith =>
      _$SampleRequestPointsInnerOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleRequestPointsInnerOneOf1 _$SampleRequestPointsInnerOneOf1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SampleRequestPointsInnerOneOf1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['place']);
  final val = SampleRequestPointsInnerOneOf1(
    place: $checkedConvert('place', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$SampleRequestPointsInnerOneOf1ToJson(
  SampleRequestPointsInnerOneOf1 instance,
) => <String, dynamic>{'place': instance.place};
