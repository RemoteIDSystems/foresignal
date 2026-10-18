// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_run_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkRunInputCWProxy {
  BenchmarkRunInput suite(BenchmarkRunInputSuiteEnum suite);

  BenchmarkRunInput components(List<String>? components);

  BenchmarkRunInput seed(int? seed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkRunInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkRunInput(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkRunInput call({
    BenchmarkRunInputSuiteEnum suite,
    List<String>? components,
    int? seed,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkRunInput.copyWith(...)` or call `instanceOfBenchmarkRunInput.copyWith.fieldName(value)` for a single field.
class _$BenchmarkRunInputCWProxyImpl implements _$BenchmarkRunInputCWProxy {
  const _$BenchmarkRunInputCWProxyImpl(this._value);

  final BenchmarkRunInput _value;

  @override
  BenchmarkRunInput suite(BenchmarkRunInputSuiteEnum suite) =>
      call(suite: suite);

  @override
  BenchmarkRunInput components(List<String>? components) =>
      call(components: components);

  @override
  BenchmarkRunInput seed(int? seed) => call(seed: seed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkRunInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkRunInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkRunInput call({
    Object? suite = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
    Object? seed = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkRunInput(
      suite: suite == const $CopyWithPlaceholder() || suite == null
          ? _value.suite
          // ignore: cast_nullable_to_non_nullable
          : suite as BenchmarkRunInputSuiteEnum,
      components: components == const $CopyWithPlaceholder()
          ? _value.components
          // ignore: cast_nullable_to_non_nullable
          : components as List<String>?,
      seed: seed == const $CopyWithPlaceholder()
          ? _value.seed
          // ignore: cast_nullable_to_non_nullable
          : seed as int?,
    );
  }
}

extension $BenchmarkRunInputCopyWith on BenchmarkRunInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkRunInput.copyWith(...)` or `instanceOfBenchmarkRunInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkRunInputCWProxy get copyWith =>
      _$BenchmarkRunInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkRunInput _$BenchmarkRunInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkRunInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['suite']);
      final val = BenchmarkRunInput(
        suite: $checkedConvert(
          'suite',
          (v) => $enumDecode(_$BenchmarkRunInputSuiteEnumEnumMap, v),
        ),
        components: $checkedConvert(
          'components',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        seed: $checkedConvert('seed', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkRunInputToJson(BenchmarkRunInput instance) =>
    <String, dynamic>{
      'suite': _$BenchmarkRunInputSuiteEnumEnumMap[instance.suite]!,
      'components': ?instance.components,
      'seed': ?instance.seed,
    };

const _$BenchmarkRunInputSuiteEnumEnumMap = {
  BenchmarkRunInputSuiteEnum.synthetic: 'synthetic',
  BenchmarkRunInputSuiteEnum.historical: 'historical',
  BenchmarkRunInputSuiteEnum.all: 'all',
};
