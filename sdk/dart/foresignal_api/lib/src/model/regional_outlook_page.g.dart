// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_outlook_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalOutlookPageCWProxy {
  RegionalOutlookPage items(List<RegionalOutlook> items);

  RegionalOutlookPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalOutlookPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalOutlookPage(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalOutlookPage call({List<RegionalOutlook> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalOutlookPage.copyWith(...)` or call `instanceOfRegionalOutlookPage.copyWith.fieldName(value)` for a single field.
class _$RegionalOutlookPageCWProxyImpl implements _$RegionalOutlookPageCWProxy {
  const _$RegionalOutlookPageCWProxyImpl(this._value);

  final RegionalOutlookPage _value;

  @override
  RegionalOutlookPage items(List<RegionalOutlook> items) => call(items: items);

  @override
  RegionalOutlookPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalOutlookPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalOutlookPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalOutlookPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return RegionalOutlookPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<RegionalOutlook>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $RegionalOutlookPageCopyWith on RegionalOutlookPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalOutlookPage.copyWith(...)` or `instanceOfRegionalOutlookPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalOutlookPageCWProxy get copyWith =>
      _$RegionalOutlookPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalOutlookPage _$RegionalOutlookPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RegionalOutlookPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = RegionalOutlookPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => RegionalOutlook.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$RegionalOutlookPageToJson(
  RegionalOutlookPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
