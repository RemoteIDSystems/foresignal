// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_case_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkCasePageCWProxy {
  BenchmarkCasePage items(List<BenchmarkCase> items);

  BenchmarkCasePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCasePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCasePage(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkCasePage call({List<BenchmarkCase> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkCasePage.copyWith(...)` or call `instanceOfBenchmarkCasePage.copyWith.fieldName(value)` for a single field.
class _$BenchmarkCasePageCWProxyImpl implements _$BenchmarkCasePageCWProxy {
  const _$BenchmarkCasePageCWProxyImpl(this._value);

  final BenchmarkCasePage _value;

  @override
  BenchmarkCasePage items(List<BenchmarkCase> items) => call(items: items);

  @override
  BenchmarkCasePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCasePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCasePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkCasePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkCasePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<BenchmarkCase>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $BenchmarkCasePageCopyWith on BenchmarkCasePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkCasePage.copyWith(...)` or `instanceOfBenchmarkCasePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkCasePageCWProxy get copyWith =>
      _$BenchmarkCasePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkCasePage _$BenchmarkCasePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkCasePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = BenchmarkCasePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => BenchmarkCase.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkCasePageToJson(BenchmarkCasePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
