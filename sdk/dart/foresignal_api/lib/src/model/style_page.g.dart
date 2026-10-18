// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'style_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StylePageCWProxy {
  StylePage items(List<Style> items);

  StylePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StylePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StylePage(...).copyWith(id: 12, name: "My name")
  /// ```
  StylePage call({List<Style> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStylePage.copyWith(...)` or call `instanceOfStylePage.copyWith.fieldName(value)` for a single field.
class _$StylePageCWProxyImpl implements _$StylePageCWProxy {
  const _$StylePageCWProxyImpl(this._value);

  final StylePage _value;

  @override
  StylePage items(List<Style> items) => call(items: items);

  @override
  StylePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StylePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StylePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StylePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return StylePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Style>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $StylePageCopyWith on StylePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStylePage.copyWith(...)` or `instanceOfStylePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StylePageCWProxy get copyWith => _$StylePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StylePage _$StylePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StylePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = StylePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Style.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$StylePageToJson(StylePage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
