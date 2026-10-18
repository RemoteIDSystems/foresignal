// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_case_expectations_precursors_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkCaseExpectationsPrecursorsInnerCWProxy {
  BenchmarkCaseExpectationsPrecursorsInner precursorId(String? precursorId);

  BenchmarkCaseExpectationsPrecursorsInner leader(String? leader);

  BenchmarkCaseExpectationsPrecursorsInner follower(String? follower);

  BenchmarkCaseExpectationsPrecursorsInner expected(
    BenchmarkCaseExpectationsPrecursorsInnerExpectedEnum expected,
  );

  BenchmarkCaseExpectationsPrecursorsInner lagHours(num? lagHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseExpectationsPrecursorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseExpectationsPrecursorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkCaseExpectationsPrecursorsInner call({
    String? precursorId,
    String? leader,
    String? follower,
    BenchmarkCaseExpectationsPrecursorsInnerExpectedEnum expected,
    num? lagHours,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkCaseExpectationsPrecursorsInner.copyWith(...)` or call `instanceOfBenchmarkCaseExpectationsPrecursorsInner.copyWith.fieldName(value)` for a single field.
class _$BenchmarkCaseExpectationsPrecursorsInnerCWProxyImpl
    implements _$BenchmarkCaseExpectationsPrecursorsInnerCWProxy {
  const _$BenchmarkCaseExpectationsPrecursorsInnerCWProxyImpl(this._value);

  final BenchmarkCaseExpectationsPrecursorsInner _value;

  @override
  BenchmarkCaseExpectationsPrecursorsInner precursorId(String? precursorId) =>
      call(precursorId: precursorId);

  @override
  BenchmarkCaseExpectationsPrecursorsInner leader(String? leader) =>
      call(leader: leader);

  @override
  BenchmarkCaseExpectationsPrecursorsInner follower(String? follower) =>
      call(follower: follower);

  @override
  BenchmarkCaseExpectationsPrecursorsInner expected(
    BenchmarkCaseExpectationsPrecursorsInnerExpectedEnum expected,
  ) => call(expected: expected);

  @override
  BenchmarkCaseExpectationsPrecursorsInner lagHours(num? lagHours) =>
      call(lagHours: lagHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseExpectationsPrecursorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseExpectationsPrecursorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkCaseExpectationsPrecursorsInner call({
    Object? precursorId = const $CopyWithPlaceholder(),
    Object? leader = const $CopyWithPlaceholder(),
    Object? follower = const $CopyWithPlaceholder(),
    Object? expected = const $CopyWithPlaceholder(),
    Object? lagHours = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkCaseExpectationsPrecursorsInner(
      precursorId: precursorId == const $CopyWithPlaceholder()
          ? _value.precursorId
          // ignore: cast_nullable_to_non_nullable
          : precursorId as String?,
      leader: leader == const $CopyWithPlaceholder()
          ? _value.leader
          // ignore: cast_nullable_to_non_nullable
          : leader as String?,
      follower: follower == const $CopyWithPlaceholder()
          ? _value.follower
          // ignore: cast_nullable_to_non_nullable
          : follower as String?,
      expected: expected == const $CopyWithPlaceholder() || expected == null
          ? _value.expected
          // ignore: cast_nullable_to_non_nullable
          : expected as BenchmarkCaseExpectationsPrecursorsInnerExpectedEnum,
      lagHours: lagHours == const $CopyWithPlaceholder()
          ? _value.lagHours
          // ignore: cast_nullable_to_non_nullable
          : lagHours as num?,
    );
  }
}

extension $BenchmarkCaseExpectationsPrecursorsInnerCopyWith
    on BenchmarkCaseExpectationsPrecursorsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkCaseExpectationsPrecursorsInner.copyWith(...)` or `instanceOfBenchmarkCaseExpectationsPrecursorsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkCaseExpectationsPrecursorsInnerCWProxy get copyWith =>
      _$BenchmarkCaseExpectationsPrecursorsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkCaseExpectationsPrecursorsInner
_$BenchmarkCaseExpectationsPrecursorsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkCaseExpectationsPrecursorsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['expected']);
      final val = BenchmarkCaseExpectationsPrecursorsInner(
        precursorId: $checkedConvert('precursorId', (v) => v as String?),
        leader: $checkedConvert('leader', (v) => v as String?),
        follower: $checkedConvert('follower', (v) => v as String?),
        expected: $checkedConvert(
          'expected',
          (v) => $enumDecode(
            _$BenchmarkCaseExpectationsPrecursorsInnerExpectedEnumEnumMap,
            v,
          ),
        ),
        lagHours: $checkedConvert('lagHours', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkCaseExpectationsPrecursorsInnerToJson(
  BenchmarkCaseExpectationsPrecursorsInner instance,
) => <String, dynamic>{
  'precursorId': ?instance.precursorId,
  'leader': ?instance.leader,
  'follower': ?instance.follower,
  'expected':
      _$BenchmarkCaseExpectationsPrecursorsInnerExpectedEnumEnumMap[instance
          .expected]!,
  'lagHours': ?instance.lagHours,
};

const _$BenchmarkCaseExpectationsPrecursorsInnerExpectedEnumEnumMap = {
  BenchmarkCaseExpectationsPrecursorsInnerExpectedEnum.validated: 'validated',
  BenchmarkCaseExpectationsPrecursorsInnerExpectedEnum.notValidated:
      'not-validated',
};
