// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkStatusCWProxy {
  BenchmarkStatus generatedAt(DateTime generatedAt);

  BenchmarkStatus overall(BenchmarkStatusOverallEnum overall);

  BenchmarkStatus components(List<BenchmarkStatusComponentsInner> components);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkStatus call({
    DateTime generatedAt,
    BenchmarkStatusOverallEnum overall,
    List<BenchmarkStatusComponentsInner> components,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkStatus.copyWith(...)` or call `instanceOfBenchmarkStatus.copyWith.fieldName(value)` for a single field.
class _$BenchmarkStatusCWProxyImpl implements _$BenchmarkStatusCWProxy {
  const _$BenchmarkStatusCWProxyImpl(this._value);

  final BenchmarkStatus _value;

  @override
  BenchmarkStatus generatedAt(DateTime generatedAt) =>
      call(generatedAt: generatedAt);

  @override
  BenchmarkStatus overall(BenchmarkStatusOverallEnum overall) =>
      call(overall: overall);

  @override
  BenchmarkStatus components(List<BenchmarkStatusComponentsInner> components) =>
      call(components: components);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkStatus call({
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? overall = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkStatus(
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
      overall: overall == const $CopyWithPlaceholder() || overall == null
          ? _value.overall
          // ignore: cast_nullable_to_non_nullable
          : overall as BenchmarkStatusOverallEnum,
      components:
          components == const $CopyWithPlaceholder() || components == null
          ? _value.components
          // ignore: cast_nullable_to_non_nullable
          : components as List<BenchmarkStatusComponentsInner>,
    );
  }
}

extension $BenchmarkStatusCopyWith on BenchmarkStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkStatus.copyWith(...)` or `instanceOfBenchmarkStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkStatusCWProxy get copyWith => _$BenchmarkStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkStatus _$BenchmarkStatusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkStatus', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['generatedAt', 'overall', 'components'],
      );
      final val = BenchmarkStatus(
        generatedAt: $checkedConvert(
          'generatedAt',
          (v) => DateTime.parse(v as String),
        ),
        overall: $checkedConvert(
          'overall',
          (v) => $enumDecode(_$BenchmarkStatusOverallEnumEnumMap, v),
        ),
        components: $checkedConvert(
          'components',
          (v) => (v as List<dynamic>)
              .map(
                (e) => BenchmarkStatusComponentsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkStatusToJson(BenchmarkStatus instance) =>
    <String, dynamic>{
      'generatedAt': instance.generatedAt.toIso8601String(),
      'overall': _$BenchmarkStatusOverallEnumEnumMap[instance.overall]!,
      'components': instance.components.map((e) => e.toJson()).toList(),
    };

const _$BenchmarkStatusOverallEnumEnumMap = {
  BenchmarkStatusOverallEnum.pass: 'pass',
  BenchmarkStatusOverallEnum.warn: 'warn',
  BenchmarkStatusOverallEnum.fail: 'fail',
  BenchmarkStatusOverallEnum.unknown: 'unknown',
};
