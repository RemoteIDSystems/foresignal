// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchlist_item_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchlistItemInputCWProxy {
  WatchlistItemInput value(String value);

  WatchlistItemInput label(String? label);

  WatchlistItemInput notes(String? notes);

  WatchlistItemInput tags(List<String>? tags);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistItemInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistItemInput(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchlistItemInput call({
    String value,
    String? label,
    String? notes,
    List<String>? tags,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchlistItemInput.copyWith(...)` or call `instanceOfWatchlistItemInput.copyWith.fieldName(value)` for a single field.
class _$WatchlistItemInputCWProxyImpl implements _$WatchlistItemInputCWProxy {
  const _$WatchlistItemInputCWProxyImpl(this._value);

  final WatchlistItemInput _value;

  @override
  WatchlistItemInput value(String value) => call(value: value);

  @override
  WatchlistItemInput label(String? label) => call(label: label);

  @override
  WatchlistItemInput notes(String? notes) => call(notes: notes);

  @override
  WatchlistItemInput tags(List<String>? tags) => call(tags: tags);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistItemInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistItemInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchlistItemInput call({
    Object? value = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
  }) {
    return WatchlistItemInput(
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
    );
  }
}

extension $WatchlistItemInputCopyWith on WatchlistItemInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchlistItemInput.copyWith(...)` or `instanceOfWatchlistItemInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchlistItemInputCWProxy get copyWith =>
      _$WatchlistItemInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchlistItemInput _$WatchlistItemInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WatchlistItemInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['value']);
      final val = WatchlistItemInput(
        value: $checkedConvert('value', (v) => v as String),
        label: $checkedConvert('label', (v) => v as String?),
        notes: $checkedConvert('notes', (v) => v as String?),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$WatchlistItemInputToJson(WatchlistItemInput instance) =>
    <String, dynamic>{
      'value': instance.value,
      'label': ?instance.label,
      'notes': ?instance.notes,
      'tags': ?instance.tags,
    };
