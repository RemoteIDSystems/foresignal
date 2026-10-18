// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchlist_import_result_invalid_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchlistImportResultInvalidInnerCWProxy {
  WatchlistImportResultInvalidInner row(int? row);

  WatchlistImportResultInvalidInner value(String? value);

  WatchlistImportResultInvalidInner reason(String reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistImportResultInvalidInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistImportResultInvalidInner(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchlistImportResultInvalidInner call({
    int? row,
    String? value,
    String reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchlistImportResultInvalidInner.copyWith(...)` or call `instanceOfWatchlistImportResultInvalidInner.copyWith.fieldName(value)` for a single field.
class _$WatchlistImportResultInvalidInnerCWProxyImpl
    implements _$WatchlistImportResultInvalidInnerCWProxy {
  const _$WatchlistImportResultInvalidInnerCWProxyImpl(this._value);

  final WatchlistImportResultInvalidInner _value;

  @override
  WatchlistImportResultInvalidInner row(int? row) => call(row: row);

  @override
  WatchlistImportResultInvalidInner value(String? value) => call(value: value);

  @override
  WatchlistImportResultInvalidInner reason(String reason) =>
      call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistImportResultInvalidInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistImportResultInvalidInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchlistImportResultInvalidInner call({
    Object? row = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return WatchlistImportResultInvalidInner(
      row: row == const $CopyWithPlaceholder()
          ? _value.row
          // ignore: cast_nullable_to_non_nullable
          : row as int?,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as String?,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
    );
  }
}

extension $WatchlistImportResultInvalidInnerCopyWith
    on WatchlistImportResultInvalidInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchlistImportResultInvalidInner.copyWith(...)` or `instanceOfWatchlistImportResultInvalidInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchlistImportResultInvalidInnerCWProxy get copyWith =>
      _$WatchlistImportResultInvalidInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchlistImportResultInvalidInner _$WatchlistImportResultInvalidInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WatchlistImportResultInvalidInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['reason']);
  final val = WatchlistImportResultInvalidInner(
    row: $checkedConvert('row', (v) => (v as num?)?.toInt()),
    value: $checkedConvert('value', (v) => v as String?),
    reason: $checkedConvert('reason', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$WatchlistImportResultInvalidInnerToJson(
  WatchlistImportResultInvalidInner instance,
) => <String, dynamic>{
  'row': ?instance.row,
  'value': ?instance.value,
  'reason': instance.reason,
};
