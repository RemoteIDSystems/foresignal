// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coverage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CoverageCWProxy {
  Coverage receivers(int? receivers);

  Coverage expected(int? expected);

  Coverage actual(int? actual);

  Coverage region(List<num>? region);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Coverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Coverage(...).copyWith(id: 12, name: "My name")
  /// ```
  Coverage call({
    int? receivers,
    int? expected,
    int? actual,
    List<num>? region,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCoverage.copyWith(...)` or call `instanceOfCoverage.copyWith.fieldName(value)` for a single field.
class _$CoverageCWProxyImpl implements _$CoverageCWProxy {
  const _$CoverageCWProxyImpl(this._value);

  final Coverage _value;

  @override
  Coverage receivers(int? receivers) => call(receivers: receivers);

  @override
  Coverage expected(int? expected) => call(expected: expected);

  @override
  Coverage actual(int? actual) => call(actual: actual);

  @override
  Coverage region(List<num>? region) => call(region: region);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Coverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Coverage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Coverage call({
    Object? receivers = const $CopyWithPlaceholder(),
    Object? expected = const $CopyWithPlaceholder(),
    Object? actual = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
  }) {
    return Coverage(
      receivers: receivers == const $CopyWithPlaceholder()
          ? _value.receivers
          // ignore: cast_nullable_to_non_nullable
          : receivers as int?,
      expected: expected == const $CopyWithPlaceholder()
          ? _value.expected
          // ignore: cast_nullable_to_non_nullable
          : expected as int?,
      actual: actual == const $CopyWithPlaceholder()
          ? _value.actual
          // ignore: cast_nullable_to_non_nullable
          : actual as int?,
      region: region == const $CopyWithPlaceholder()
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as List<num>?,
    );
  }
}

extension $CoverageCopyWith on Coverage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCoverage.copyWith(...)` or `instanceOfCoverage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CoverageCWProxy get copyWith => _$CoverageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Coverage _$CoverageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Coverage', json, ($checkedConvert) {
      final val = Coverage(
        receivers: $checkedConvert('receivers', (v) => (v as num?)?.toInt()),
        expected: $checkedConvert('expected', (v) => (v as num?)?.toInt()),
        actual: $checkedConvert('actual', (v) => (v as num?)?.toInt()),
        region: $checkedConvert(
          'region',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CoverageToJson(Coverage instance) => <String, dynamic>{
  'receivers': ?instance.receivers,
  'expected': ?instance.expected,
  'actual': ?instance.actual,
  'region': ?instance.region,
};
