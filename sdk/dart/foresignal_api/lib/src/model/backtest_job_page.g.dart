// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backtest_job_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BacktestJobPageCWProxy {
  BacktestJobPage items(List<BacktestJob> items);

  BacktestJobPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestJobPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestJobPage(...).copyWith(id: 12, name: "My name")
  /// ```
  BacktestJobPage call({List<BacktestJob> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBacktestJobPage.copyWith(...)` or call `instanceOfBacktestJobPage.copyWith.fieldName(value)` for a single field.
class _$BacktestJobPageCWProxyImpl implements _$BacktestJobPageCWProxy {
  const _$BacktestJobPageCWProxyImpl(this._value);

  final BacktestJobPage _value;

  @override
  BacktestJobPage items(List<BacktestJob> items) => call(items: items);

  @override
  BacktestJobPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestJobPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestJobPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BacktestJobPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return BacktestJobPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<BacktestJob>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $BacktestJobPageCopyWith on BacktestJobPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBacktestJobPage.copyWith(...)` or `instanceOfBacktestJobPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BacktestJobPageCWProxy get copyWith => _$BacktestJobPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BacktestJobPage _$BacktestJobPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BacktestJobPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = BacktestJobPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => BacktestJob.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BacktestJobPageToJson(BacktestJobPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
