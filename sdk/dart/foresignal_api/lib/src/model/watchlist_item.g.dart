// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchlist_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchlistItemCWProxy {
  WatchlistItem value(String value);

  WatchlistItem label(String? label);

  WatchlistItem notes(String? notes);

  WatchlistItem tags(List<String>? tags);

  WatchlistItem addedAt(DateTime addedAt);

  WatchlistItem origin(WatchlistItemOriginEnum origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistItem(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchlistItem call({
    String value,
    String? label,
    String? notes,
    List<String>? tags,
    DateTime addedAt,
    WatchlistItemOriginEnum origin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchlistItem.copyWith(...)` or call `instanceOfWatchlistItem.copyWith.fieldName(value)` for a single field.
class _$WatchlistItemCWProxyImpl implements _$WatchlistItemCWProxy {
  const _$WatchlistItemCWProxyImpl(this._value);

  final WatchlistItem _value;

  @override
  WatchlistItem value(String value) => call(value: value);

  @override
  WatchlistItem label(String? label) => call(label: label);

  @override
  WatchlistItem notes(String? notes) => call(notes: notes);

  @override
  WatchlistItem tags(List<String>? tags) => call(tags: tags);

  @override
  WatchlistItem addedAt(DateTime addedAt) => call(addedAt: addedAt);

  @override
  WatchlistItem origin(WatchlistItemOriginEnum origin) => call(origin: origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchlistItem call({
    Object? value = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? addedAt = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
  }) {
    return WatchlistItem(
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as String,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as String?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      addedAt: addedAt == const $CopyWithPlaceholder() || addedAt == null
          ? _value.addedAt
          // ignore: cast_nullable_to_non_nullable
          : addedAt as DateTime,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as WatchlistItemOriginEnum,
    );
  }
}

extension $WatchlistItemCopyWith on WatchlistItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchlistItem.copyWith(...)` or `instanceOfWatchlistItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchlistItemCWProxy get copyWith => _$WatchlistItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchlistItem _$WatchlistItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WatchlistItem', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['value', 'addedAt', 'origin']);
      final val = WatchlistItem(
        value: $checkedConvert('value', (v) => v as String),
        label: $checkedConvert('label', (v) => v as String?),
        notes: $checkedConvert('notes', (v) => v as String?),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        addedAt: $checkedConvert('addedAt', (v) => DateTime.parse(v as String)),
        origin: $checkedConvert(
          'origin',
          (v) => $enumDecode(_$WatchlistItemOriginEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$WatchlistItemToJson(WatchlistItem instance) =>
    <String, dynamic>{
      'value': instance.value,
      'label': ?instance.label,
      'notes': ?instance.notes,
      'tags': ?instance.tags,
      'addedAt': instance.addedAt.toIso8601String(),
      'origin': _$WatchlistItemOriginEnumEnumMap[instance.origin]!,
    };

const _$WatchlistItemOriginEnumEnumMap = {
  WatchlistItemOriginEnum.manual: 'manual',
  WatchlistItemOriginEnum.csv: 'csv',
  WatchlistItemOriginEnum.json: 'json',
};
