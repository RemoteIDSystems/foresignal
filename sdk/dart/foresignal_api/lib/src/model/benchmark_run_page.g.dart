// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_run_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkRunPageCWProxy {
  BenchmarkRunPage items(List<BenchmarkRun> items);

  BenchmarkRunPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkRunPage call({List<BenchmarkRun> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkRunPage.copyWith(...)` or call `instanceOfBenchmarkRunPage.copyWith.fieldName(value)` for a single field.
class _$BenchmarkRunPageCWProxyImpl implements _$BenchmarkRunPageCWProxy {
  const _$BenchmarkRunPageCWProxyImpl(this._value);

  final BenchmarkRunPage _value;

  @override
  BenchmarkRunPage items(List<BenchmarkRun> items) => call(items: items);

  @override
  BenchmarkRunPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkRunPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkRunPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<BenchmarkRun>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $BenchmarkRunPageCopyWith on BenchmarkRunPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkRunPage.copyWith(...)` or `instanceOfBenchmarkRunPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkRunPageCWProxy get copyWith => _$BenchmarkRunPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkRunPage _$BenchmarkRunPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkRunPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = BenchmarkRunPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => BenchmarkRun.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkRunPageToJson(BenchmarkRunPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
