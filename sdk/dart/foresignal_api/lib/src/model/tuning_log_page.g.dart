// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tuning_log_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TuningLogPageCWProxy {
  TuningLogPage items(List<TuningLogEntry> items);

  TuningLogPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningLogPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningLogPage(...).copyWith(id: 12, name: "My name")
  /// ```
  TuningLogPage call({List<TuningLogEntry> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTuningLogPage.copyWith(...)` or call `instanceOfTuningLogPage.copyWith.fieldName(value)` for a single field.
class _$TuningLogPageCWProxyImpl implements _$TuningLogPageCWProxy {
  const _$TuningLogPageCWProxyImpl(this._value);

  final TuningLogPage _value;

  @override
  TuningLogPage items(List<TuningLogEntry> items) => call(items: items);

  @override
  TuningLogPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningLogPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningLogPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TuningLogPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return TuningLogPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<TuningLogEntry>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $TuningLogPageCopyWith on TuningLogPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTuningLogPage.copyWith(...)` or `instanceOfTuningLogPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TuningLogPageCWProxy get copyWith => _$TuningLogPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TuningLogPage _$TuningLogPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TuningLogPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = TuningLogPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => TuningLogEntry.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$TuningLogPageToJson(TuningLogPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
