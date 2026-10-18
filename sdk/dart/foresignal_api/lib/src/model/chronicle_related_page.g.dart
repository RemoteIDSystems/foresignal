// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_related_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleRelatedPageCWProxy {
  ChronicleRelatedPage items(List<ChronicleRelatedPageItemsInner> items);

  ChronicleRelatedPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleRelatedPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleRelatedPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleRelatedPage call({
    List<ChronicleRelatedPageItemsInner> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleRelatedPage.copyWith(...)` or call `instanceOfChronicleRelatedPage.copyWith.fieldName(value)` for a single field.
class _$ChronicleRelatedPageCWProxyImpl
    implements _$ChronicleRelatedPageCWProxy {
  const _$ChronicleRelatedPageCWProxyImpl(this._value);

  final ChronicleRelatedPage _value;

  @override
  ChronicleRelatedPage items(List<ChronicleRelatedPageItemsInner> items) =>
      call(items: items);

  @override
  ChronicleRelatedPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleRelatedPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleRelatedPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleRelatedPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ChronicleRelatedPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ChronicleRelatedPageItemsInner>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ChronicleRelatedPageCopyWith on ChronicleRelatedPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleRelatedPage.copyWith(...)` or `instanceOfChronicleRelatedPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleRelatedPageCWProxy get copyWith =>
      _$ChronicleRelatedPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleRelatedPage _$ChronicleRelatedPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleRelatedPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
  final val = ChronicleRelatedPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ChronicleRelatedPageItemsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleRelatedPageToJson(
  ChronicleRelatedPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
