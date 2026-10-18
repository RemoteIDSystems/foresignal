// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_case_expectations_findings_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkCaseExpectationsFindingsInnerCWProxy {
  BenchmarkCaseExpectationsFindingsInner findingType(String findingType);

  BenchmarkCaseExpectationsFindingsInner present(bool present);

  BenchmarkCaseExpectationsFindingsInner regionCodes(List<String>? regionCodes);

  BenchmarkCaseExpectationsFindingsInner seriesRef(String? seriesRef);

  BenchmarkCaseExpectationsFindingsInner from(DateTime? from);

  BenchmarkCaseExpectationsFindingsInner to(DateTime? to);

  BenchmarkCaseExpectationsFindingsInner minSeverity(
    FindingSeverity? minSeverity,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseExpectationsFindingsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseExpectationsFindingsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkCaseExpectationsFindingsInner call({
    String findingType,
    bool present,
    List<String>? regionCodes,
    String? seriesRef,
    DateTime? from,
    DateTime? to,
    FindingSeverity? minSeverity,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkCaseExpectationsFindingsInner.copyWith(...)` or call `instanceOfBenchmarkCaseExpectationsFindingsInner.copyWith.fieldName(value)` for a single field.
class _$BenchmarkCaseExpectationsFindingsInnerCWProxyImpl
    implements _$BenchmarkCaseExpectationsFindingsInnerCWProxy {
  const _$BenchmarkCaseExpectationsFindingsInnerCWProxyImpl(this._value);

  final BenchmarkCaseExpectationsFindingsInner _value;

  @override
  BenchmarkCaseExpectationsFindingsInner findingType(String findingType) =>
      call(findingType: findingType);

  @override
  BenchmarkCaseExpectationsFindingsInner present(bool present) =>
      call(present: present);

  @override
  BenchmarkCaseExpectationsFindingsInner regionCodes(
    List<String>? regionCodes,
  ) => call(regionCodes: regionCodes);

  @override
  BenchmarkCaseExpectationsFindingsInner seriesRef(String? seriesRef) =>
      call(seriesRef: seriesRef);

  @override
  BenchmarkCaseExpectationsFindingsInner from(DateTime? from) =>
      call(from: from);

  @override
  BenchmarkCaseExpectationsFindingsInner to(DateTime? to) => call(to: to);

  @override
  BenchmarkCaseExpectationsFindingsInner minSeverity(
    FindingSeverity? minSeverity,
  ) => call(minSeverity: minSeverity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseExpectationsFindingsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseExpectationsFindingsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkCaseExpectationsFindingsInner call({
    Object? findingType = const $CopyWithPlaceholder(),
    Object? present = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? seriesRef = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkCaseExpectationsFindingsInner(
      findingType:
          findingType == const $CopyWithPlaceholder() || findingType == null
          ? _value.findingType
          // ignore: cast_nullable_to_non_nullable
          : findingType as String,
      present: present == const $CopyWithPlaceholder() || present == null
          ? _value.present
          // ignore: cast_nullable_to_non_nullable
          : present as bool,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      seriesRef: seriesRef == const $CopyWithPlaceholder()
          ? _value.seriesRef
          // ignore: cast_nullable_to_non_nullable
          : seriesRef as String?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as FindingSeverity?,
    );
  }
}

extension $BenchmarkCaseExpectationsFindingsInnerCopyWith
    on BenchmarkCaseExpectationsFindingsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkCaseExpectationsFindingsInner.copyWith(...)` or `instanceOfBenchmarkCaseExpectationsFindingsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkCaseExpectationsFindingsInnerCWProxy get copyWith =>
      _$BenchmarkCaseExpectationsFindingsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkCaseExpectationsFindingsInner
_$BenchmarkCaseExpectationsFindingsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkCaseExpectationsFindingsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['findingType', 'present']);
      final val = BenchmarkCaseExpectationsFindingsInner(
        findingType: $checkedConvert('findingType', (v) => v as String),
        present: $checkedConvert('present', (v) => v as bool),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        seriesRef: $checkedConvert('seriesRef', (v) => v as String?),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        minSeverity: $checkedConvert(
          'minSeverity',
          (v) => $enumDecodeNullable(_$FindingSeverityEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkCaseExpectationsFindingsInnerToJson(
  BenchmarkCaseExpectationsFindingsInner instance,
) => <String, dynamic>{
  'findingType': instance.findingType,
  'present': instance.present,
  'regionCodes': ?instance.regionCodes,
  'seriesRef': ?instance.seriesRef,
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'minSeverity': ?_$FindingSeverityEnumMap[instance.minSeverity],
};

const _$FindingSeverityEnumMap = {
  FindingSeverity.info: 'info',
  FindingSeverity.notable: 'notable',
  FindingSeverity.significant: 'significant',
};
