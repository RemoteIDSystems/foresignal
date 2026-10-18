// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorPageCWProxy {
  PrecursorPage items(List<Precursor> items);

  PrecursorPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorPage(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorPage call({List<Precursor> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorPage.copyWith(...)` or call `instanceOfPrecursorPage.copyWith.fieldName(value)` for a single field.
class _$PrecursorPageCWProxyImpl implements _$PrecursorPageCWProxy {
  const _$PrecursorPageCWProxyImpl(this._value);

  final PrecursorPage _value;

  @override
  PrecursorPage items(List<Precursor> items) => call(items: items);

  @override
  PrecursorPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PrecursorPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Precursor>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PrecursorPageCopyWith on PrecursorPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorPage.copyWith(...)` or `instanceOfPrecursorPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorPageCWProxy get copyWith => _$PrecursorPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorPage _$PrecursorPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PrecursorPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Precursor.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorPageToJson(PrecursorPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
