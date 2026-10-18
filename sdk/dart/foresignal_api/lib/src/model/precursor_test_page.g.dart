// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestPageCWProxy {
  PrecursorTestPage items(List<PrecursorTest> items);

  PrecursorTestPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestPage(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestPage call({List<PrecursorTest> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestPage.copyWith(...)` or call `instanceOfPrecursorTestPage.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestPageCWProxyImpl implements _$PrecursorTestPageCWProxy {
  const _$PrecursorTestPageCWProxyImpl(this._value);

  final PrecursorTestPage _value;

  @override
  PrecursorTestPage items(List<PrecursorTest> items) => call(items: items);

  @override
  PrecursorTestPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<PrecursorTest>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PrecursorTestPageCopyWith on PrecursorTestPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestPage.copyWith(...)` or `instanceOfPrecursorTestPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestPageCWProxy get copyWith =>
      _$PrecursorTestPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestPage _$PrecursorTestPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorTestPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PrecursorTestPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => PrecursorTest.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorTestPageToJson(PrecursorTestPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
