// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_case_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkCaseInputCWProxy {
  BenchmarkCaseInput name(String name);

  BenchmarkCaseInput description(String? description);

  BenchmarkCaseInput windowFrom(DateTime windowFrom);

  BenchmarkCaseInput windowTo(DateTime windowTo);

  BenchmarkCaseInput regionCodes(List<String>? regionCodes);

  BenchmarkCaseInput expectations(BenchmarkCaseExpectations expectations);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseInput(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkCaseInput call({
    String name,
    String? description,
    DateTime windowFrom,
    DateTime windowTo,
    List<String>? regionCodes,
    BenchmarkCaseExpectations expectations,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkCaseInput.copyWith(...)` or call `instanceOfBenchmarkCaseInput.copyWith.fieldName(value)` for a single field.
class _$BenchmarkCaseInputCWProxyImpl implements _$BenchmarkCaseInputCWProxy {
  const _$BenchmarkCaseInputCWProxyImpl(this._value);

  final BenchmarkCaseInput _value;

  @override
  BenchmarkCaseInput name(String name) => call(name: name);

  @override
  BenchmarkCaseInput description(String? description) =>
      call(description: description);

  @override
  BenchmarkCaseInput windowFrom(DateTime windowFrom) =>
      call(windowFrom: windowFrom);

  @override
  BenchmarkCaseInput windowTo(DateTime windowTo) => call(windowTo: windowTo);

  @override
  BenchmarkCaseInput regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  BenchmarkCaseInput expectations(BenchmarkCaseExpectations expectations) =>
      call(expectations: expectations);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkCaseInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? windowFrom = const $CopyWithPlaceholder(),
    Object? windowTo = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? expectations = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkCaseInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      windowFrom:
          windowFrom == const $CopyWithPlaceholder() || windowFrom == null
          ? _value.windowFrom
          // ignore: cast_nullable_to_non_nullable
          : windowFrom as DateTime,
      windowTo: windowTo == const $CopyWithPlaceholder() || windowTo == null
          ? _value.windowTo
          // ignore: cast_nullable_to_non_nullable
          : windowTo as DateTime,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      expectations:
          expectations == const $CopyWithPlaceholder() || expectations == null
          ? _value.expectations
          // ignore: cast_nullable_to_non_nullable
          : expectations as BenchmarkCaseExpectations,
    );
  }
}

extension $BenchmarkCaseInputCopyWith on BenchmarkCaseInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkCaseInput.copyWith(...)` or `instanceOfBenchmarkCaseInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkCaseInputCWProxy get copyWith =>
      _$BenchmarkCaseInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkCaseInput _$BenchmarkCaseInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkCaseInput', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['name', 'windowFrom', 'windowTo', 'expectations'],
      );
      final val = BenchmarkCaseInput(
        name: $checkedConvert('name', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        windowFrom: $checkedConvert(
          'windowFrom',
          (v) => DateTime.parse(v as String),
        ),
        windowTo: $checkedConvert(
          'windowTo',
          (v) => DateTime.parse(v as String),
        ),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        expectations: $checkedConvert(
          'expectations',
          (v) => BenchmarkCaseExpectations.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkCaseInputToJson(BenchmarkCaseInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'description': ?instance.description,
      'windowFrom': instance.windowFrom.toIso8601String(),
      'windowTo': instance.windowTo.toIso8601String(),
      'regionCodes': ?instance.regionCodes,
      'expectations': instance.expectations.toJson(),
    };
