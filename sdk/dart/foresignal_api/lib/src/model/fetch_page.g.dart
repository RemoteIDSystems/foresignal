// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'fetch_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FetchPageCWProxy {
  FetchPage items(List<FetchLogEntry> items);

  FetchPage nextAfter(int? nextAfter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FetchPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FetchPage(...).copyWith(id: 12, name: "My name")
  /// ```
  FetchPage call({List<FetchLogEntry> items, int? nextAfter});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFetchPage.copyWith(...)` or call `instanceOfFetchPage.copyWith.fieldName(value)` for a single field.
class _$FetchPageCWProxyImpl implements _$FetchPageCWProxy {
  const _$FetchPageCWProxyImpl(this._value);

  final FetchPage _value;

  @override
  FetchPage items(List<FetchLogEntry> items) => call(items: items);

  @override
  FetchPage nextAfter(int? nextAfter) => call(nextAfter: nextAfter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FetchPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FetchPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FetchPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextAfter = const $CopyWithPlaceholder(),
  }) {
    return FetchPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<FetchLogEntry>,
      nextAfter: nextAfter == const $CopyWithPlaceholder()
          ? _value.nextAfter
          // ignore: cast_nullable_to_non_nullable
          : nextAfter as int?,
    );
  }
}

extension $FetchPageCopyWith on FetchPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFetchPage.copyWith(...)` or `instanceOfFetchPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FetchPageCWProxy get copyWith => _$FetchPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FetchPage _$FetchPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FetchPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = FetchPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => FetchLogEntry.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextAfter: $checkedConvert('nextAfter', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$FetchPageToJson(FetchPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextAfter': ?instance.nextAfter,
};
